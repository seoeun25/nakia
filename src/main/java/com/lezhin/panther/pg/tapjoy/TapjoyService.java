package com.lezhin.panther.pg.tapjoy;

import com.google.common.collect.ImmutableList;
import com.lezhin.beans.entity.common.LezhinLocale;
import com.lezhin.constant.LezhinPlatform;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.exception.TapjoyException;
import com.lezhin.panther.internal.InternalWalletService;
import com.lezhin.panther.internal.Result;
import com.lezhin.panther.internal.Wallet;
import com.lezhin.panther.util.MessageManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * @author taemmy
 * @since 2018. 6. 27.
 */
@Service
public class TapjoyService {
    private static final Logger logger = LoggerFactory.getLogger(TapjoyService.class);

    // supported locale, platform
    private static final List<LezhinLocale> supportedLocales = ImmutableList.of(LezhinLocale.KO_KR,
            LezhinLocale.EN_US,
            LezhinLocale.JA_JP);
    private static final List<LezhinPlatform> supportedPlatforms = ImmutableList.of(LezhinPlatform.ios,
            LezhinPlatform.android);

    // message-key
    private final String PRESENT_TITLE = "tapjoy.present.title";
    private final String PRESENT_DESCRIPTION = "tapjoy.present.description";
    private final String PURCHASE_TITLE = "tapjoy.purchase.title";
    private final String PUSH_TITLE = "tapjoy.push.title";
    private final String PUSH_MESSAGE = "tapjoy.push.message";
    private final String PRESENT_CUSTOM_URI = "lezhin://present";

    private final Integer WALLET_EXPIRE_MONTH = 6;

    private TapjoyEventRepository tapjoyEventRepository;
    private InternalWalletService internalWalletService;
    private PantherProperties pantherProperties;
    private MessageManager messageManager;

    public TapjoyService(final TapjoyEventRepository tapjoyEventRepository, final InternalWalletService internalWalletService,
                         final PantherProperties pantherProperties, final MessageManager messageManager) {
        this.tapjoyEventRepository = tapjoyEventRepository;
        this.internalWalletService = internalWalletService;
        this.pantherProperties = pantherProperties;
        this.messageManager = messageManager;
    }

    public TapjoyEvent postback(final String locale, final String platform, final Map<String, String> data) {
        TapjoyEvent event = convertTapjoyEvent(locale, platform, data);

        // STEP-1: check verifier
        verify(event);

        // STEP-2: check rewarded
        checkRewarded(event);

        // STEP-3: insert postback raw
        persist(event);

        // STEP-4: wallets
        Locale walletLocale = messageManager.getLocale(event.getLocale());
        Wallet wallet = Wallet.builder().userId(event.getSnuid())
                .locale(event.getLocale())
                .platform(event.getPlatform())
                .companyEventId(pantherProperties.getTapjoy().getCompanyEventId())
                .usageRestrictionId(pantherProperties.getTapjoy().getUsageRestrictionId())
                .purchaseType("R")
                .purchaseTitle(messageManager.get(PURCHASE_TITLE, walletLocale))
                .sendPresent(true)
                .presentTitle(messageManager.get(PRESENT_TITLE, walletLocale, event.getCurrency()))
                .presentDescription(messageManager.get(PRESENT_DESCRIPTION, walletLocale))
                .amount(event.getCurrency())
                .immediate(true)
                .build();
        Result response = internalWalletService.sendCoinReward(wallet, null);
        if(response.getCode() == -10404) {
            throw new TapjoyException(String.format("user not found. requestId: %s, user: %s", event.getRequestId(), event.getSnuid()));
        }else if(response.getCode() != 0) {
            throw new PantherException(String.format("sendCoinReward fail. requestId: %s, userId: %s, responseCode: %s",
                    event.getRequestId(), event.getSnuid(), response.getCode()));
        }

        // STEP-5: update reward
        event.setStatus(TapjoyEvent.Status.reward);
        event.setRewardAt(new Timestamp(Instant.now().toEpochMilli()));
        persist(event);

        // STEP-6: send push
        try {
            String pushTitle = messageManager.get(PUSH_TITLE, walletLocale);
            String pushMessage = messageManager.get(PUSH_MESSAGE, walletLocale,
                    event.getCurrency(), WALLET_EXPIRE_MONTH);
            internalWalletService.sendPresentPush(event.getSnuid(), PRESENT_CUSTOM_URI, pushTitle, pushMessage);
        } catch (Exception e) {
            logger.warn("sendPresentPush fail. requestId: {}, userId: {}", event.getSnuid(), event.getRequestId(), e);
        }
        return persist(event);
    }

    void verify(final TapjoyEvent tapjoyEvent) {
        String generated = DigestUtils.md5Hex(tapjoyEvent.getRequestId() + ":" + tapjoyEvent.getSnuid() + ":" + tapjoyEvent.getCurrency() + ":" + pantherProperties.getTapjoy().getSecretKey());
        if (!generated.equals(tapjoyEvent.getVerifier())) {
            throw new TapjoyException(String.format("verifier not match. requestId: %s, userId: %s", tapjoyEvent.getRequestId(), tapjoyEvent.getSnuid()));
        }
    }

    @Transactional(value = "pantherTransactionManager")
    TapjoyEvent persist(final TapjoyEvent tapjoyEvent) {
        try {
            return tapjoyEventRepository.save(tapjoyEvent);
        } catch (Throwable e) {
            logger.error("Failed to persist TapjoyEvent, {}, {}", tapjoyEvent.toString(), e);
            throw e;
        }
    }

    @Transactional(value = "pantherTransactionManager", readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
    public void checkRewarded(final TapjoyEvent event) {
        List<TapjoyEvent> events = tapjoyEventRepository.findBySnuidAndRequestId(event.getSnuid(), event.getRequestId());
        Optional<TapjoyEvent> optional = events.stream()
                .filter(e -> TapjoyEvent.Status.reward.equals(e.getStatus()))
                .findFirst();

        logger.info("postback attempt count: {}, rewarded: {}", events.size(), optional.isPresent());
        if (optional.isPresent()) {
            TapjoyEvent exist = optional.get();
            logger.warn("Already rewarded. id: {}, users: {}, requestId: {}, rewaredAt: {}", exist.getId(), exist.getSnuid(), exist.getRequestId(), exist.getRewardAt());
            throw new TapjoyException(String.format("TapjoyEvent is already rewarded. requestId: %s, user: %s", exist.getRequestId(), exist.getSnuid()));
        }
    }

    public TapjoyEvent convertTapjoyEvent(final String lang, final String platform, final Map<String, String> data) {
        // check supported locale & platform
        LezhinLocale lzLocale = supportedLocales.stream()
                .filter(l -> l.getLanguageCode().equals(lang))
                .findFirst()
                .orElseThrow(() -> new ParameterException("convertTapjoyEvent fail. not supported locale"));

        LezhinPlatform lzPlatform = supportedPlatforms.stream()
                .filter(p -> p.name().equalsIgnoreCase(platform))
                .findFirst()
                .orElseThrow(() -> new ParameterException("convertTapjoyEvent fail. not supported platform"));

        // verify id, snuid, currency, verifier
        Optional.ofNullable(data.get("id")).orElseThrow(()
                -> new ParameterException("convertTapjoyEvent fail. id can not be empty"));
        Optional.ofNullable(data.get("snuid")).orElseThrow(()
                -> new ParameterException("convertTapjoyEvent fail. snuid can not be empty"));
        Optional.ofNullable(data.get("currency")).orElseThrow(()
                -> new ParameterException("convertTapjoyEvent fail. currency can not be empty"));
        Optional.ofNullable(data.get("verifier")).orElseThrow(()
                -> new ParameterException("convertTapjoyEvent fail. verifier can not be empty"));

        return TapjoyEvent.builder()
                .locale(lzLocale.getId())
                .platform(lzPlatform.name().toLowerCase())
                .requestId(data.get("id"))
                .snuid(Long.parseLong(data.get("snuid")))
                .currency(Integer.parseInt(data.get("currency")))
                .verifier(data.get("verifier"))
                .postbackAt(new Timestamp(Instant.now().toEpochMilli()))
                .status(TapjoyEvent.Status.postback)
                .build();
    }
}
