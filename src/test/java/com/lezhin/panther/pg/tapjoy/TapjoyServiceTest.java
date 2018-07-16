package com.lezhin.panther.pg.tapjoy;

import com.lezhin.beans.entity.common.LezhinLocale;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.exception.TapjoyException;
import com.lezhin.panther.internal.InternalWalletService;
import com.lezhin.panther.internal.Result;
import com.lezhin.panther.internal.Wallet;
import com.lezhin.panther.util.MessageManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

/**
 * @author taemmy
 * @since 2018. 6. 29.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
class TapjoyServiceTest {

    private final Logger logger = LoggerFactory.getLogger(TapjoyServiceTest.class);

    private String _VERIFIER = "aeeb31706828f720cbf9b7d7b63949e8";
    private String _CURRENCY = "1";
    private String _REQUEST_ID = "fe93c3ca-6331-4954-8e5a-8754d9778598";
    private String _SNUID = "5213654654124032";

    @Autowired
    private TapjoyService tapjoyService;
    @Autowired
    private PantherProperties pantherProperties;
    @Autowired
    private TapjoyEventRepository repository;
    @Autowired
    private MessageManager messageManager;

    @Mock
    InternalWalletService mockInternalWalletService;

    @Test
    public void test_postback() {
        MockitoAnnotations.initMocks(this);

        doReturn(new Result(0, "OK")).when(mockInternalWalletService).sendCoinReward(Mockito.any(Wallet.class), Mockito.isNull());
        doNothing().when(mockInternalWalletService).sendPresentPush(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        TapjoyService mockTapjoyService = new TapjoyService(repository, mockInternalWalletService, pantherProperties, messageManager);

        String locale = "en";
        String platform = "ios";
        Map<String, String> params = new HashMap<>();
        params.put("verifier", _VERIFIER);
        params.put("currency", _CURRENCY);
        params.put("id", _REQUEST_ID);
        params.put("snuid", _SNUID);
        params.put("display_multiplier", "1.0");

        TapjoyEvent event = mockTapjoyService.postback(locale, platform, params);
        assertNotNull(event);
        assertNotNull(event.getId());
        assertEquals(TapjoyEvent.Status.reward, event.getStatus());
    }

    @Test
    public void test_postback_parameter_exception() {
        MockitoAnnotations.initMocks(this);

        doReturn(new Result(0, "OK")).when(mockInternalWalletService).sendCoinReward(Mockito.any(Wallet.class), Mockito.isNull());
        doNothing().when(mockInternalWalletService).sendPresentPush(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        TapjoyService mockTapjoyService = new TapjoyService(repository, mockInternalWalletService, pantherProperties, messageManager);

        String locale = "en";
        String platform = "ios";
        Map<String, String> params = new HashMap<>();
        params.put("currency", _CURRENCY);
        params.put("id", _REQUEST_ID);
        params.put("snuid", _SNUID);
        params.put("display_multiplier", "1.0");
        assertThrows(ParameterException.class, () -> mockTapjoyService.postback(locale, platform, params));
    }

    @Test
    public void test_convert_tapjoy_event() {
        String locale = "en";
        String platform = "ios";

        Map<String, String> data = new HashMap<>();
        data.put("id", _REQUEST_ID);
        data.put("snuid", _SNUID);
        data.put("currency", _CURRENCY);
        data.put("verifier", _VERIFIER);

        TapjoyEvent event = tapjoyService.convertTapjoyEvent(locale, platform, data);

        assertEquals(LezhinLocale.EN_US.getId(), event.getLocale());
        assertEquals("ios", event.getPlatform());
        assertEquals(_REQUEST_ID, event.getRequestId());
        assertEquals(_SNUID, event.getSnuid().toString());
        assertEquals(_CURRENCY, event.getCurrency().toString());
        assertEquals(_VERIFIER, event.getVerifier());
        assertEquals(TapjoyEvent.Status.postback, event.getStatus());
    }

    @Test
    public void test_convert_tapjoy_event_not_null() {
        String locale = "en";
        String platform = "ios";

        Map<String, String> data = new HashMap<>();
        data.put("id", _REQUEST_ID);
        data.put("snuid", _SNUID);
        data.put("currency", _CURRENCY);
        assertThrows(ParameterException.class, () -> tapjoyService.convertTapjoyEvent(locale, platform, data));

        Map<String, String> data2 = new HashMap<>();
        data2.put("id", _REQUEST_ID);
        data2.put("snuid", _SNUID);
        data2.put("verifier", _VERIFIER);
        assertThrows(ParameterException.class, () -> tapjoyService.convertTapjoyEvent(locale, platform, data2));

        Map<String, String> data3 = new HashMap<>();
        data3.put("id", _REQUEST_ID);
        data3.put("currency", _CURRENCY);
        data3.put("verifier", _VERIFIER);
        assertThrows(ParameterException.class, () -> tapjoyService.convertTapjoyEvent(locale, platform, data3));

        Map<String, String> data4 = new HashMap<>();
        data4.put("snuid", _SNUID);
        data4.put("currency", _CURRENCY);
        data4.put("verifier", _VERIFIER);
        assertThrows(ParameterException.class, () -> tapjoyService.convertTapjoyEvent(locale, platform, data4));
    }

    @Test
    public void test_convert_tapjoy_not_supported() {
        Map<String, String> data = new HashMap<>();
        data.put("id", _REQUEST_ID);
        data.put("snuid", _SNUID);
        data.put("currency", _CURRENCY);
        data.put("verifier", _VERIFIER);

        assertThrows(ParameterException.class, () -> tapjoyService.convertTapjoyEvent("kr", "ios", data));
        assertThrows(ParameterException.class, () -> tapjoyService.convertTapjoyEvent("ko", "web", data));
    }

    @Test
    public void test_verify() {
        TapjoyEvent event = new TapjoyEvent();
        event.setRequestId(_REQUEST_ID);
        event.setSnuid(Long.parseLong(_SNUID));
        event.setCurrency(Integer.parseInt(_CURRENCY));
        event.setVerifier(_VERIFIER);

        tapjoyService.verify(event);
    }

    @Test
    public void test_verifier_data_not_null() {
        TapjoyEvent event = new TapjoyEvent();
        event.setSnuid(Long.parseLong(_SNUID));
        event.setCurrency(Integer.parseInt(_CURRENCY));
        event.setVerifier(_VERIFIER);

        assertThrows(TapjoyException.class, () -> tapjoyService.verify(event));
    }

    @Test
    public void test_persist() {
        TapjoyEvent event = TapjoyEvent.builder()
                .locale("en-US")
                .platform("ios")
                .requestId("e4fb0833-0459-4431-8019-eaca7b565aa7")
                .snuid(6068546800189440L)
                .currency(1)
                .verifier("216aa4ad1571616f4f2d79a87deb1e04")
                .postbackAt(new Timestamp(Instant.now().toEpochMilli()))
                .status(TapjoyEvent.Status.postback)
                .build();

        TapjoyEvent saved = tapjoyService.persist(event);
        assertNotNull(saved);
        assertNotNull(saved.getId());
        logger.info("saved. id: {}, status: {}, postbackAt: {}", saved.getId(), saved.getStatus(), saved.getPostbackAt());

        saved.setStatus(TapjoyEvent.Status.reward);
        saved.setRewardAt(new Timestamp(Instant.now().toEpochMilli()));
        TapjoyEvent updated = tapjoyService.persist(saved);
        assertNotNull(updated);
        assertNotNull(updated.getRewardAt());
        assertEquals(saved.getId(), updated.getId());
    }

    @Test
    public void test_check_rewarded() {
        TapjoyEvent event = TapjoyEvent.builder()
                .locale("en-US")
                .platform("ios")
                .requestId("e4fb0833-0459-4431-8019-eaca7b565aa7")
                .snuid(6068546800189440L)
                .currency(1)
                .verifier("216aa4ad1571616f4f2d79a87deb1e04")
                .postbackAt(new Timestamp(Instant.now().toEpochMilli()))
                .status(TapjoyEvent.Status.postback)
                .build();
        TapjoyEvent event2 = TapjoyEvent.builder()
                .locale("en-US")
                .platform("ios")
                .requestId("e4fb0833-0459-4431-8019-eaca7b565aa7")
                .snuid(6068546800189440L)
                .currency(1)
                .verifier("216aa4ad1571616f4f2d79a87deb1e04")
                .postbackAt(new Timestamp(Instant.now().toEpochMilli()))
                .status(TapjoyEvent.Status.postback)
                .build();
        TapjoyEvent event3 = TapjoyEvent.builder()
                .locale("en-US")
                .platform("ios")
                .requestId("e4fb0833-0459-4431-8019-eaca7b565aa7")
                .snuid(6068546800189440L)
                .currency(1)
                .verifier("216aa4ad1571616f4f2d79a87deb1e04")
                .postbackAt(new Timestamp(Instant.now().toEpochMilli()))
                .rewardAt(new Timestamp(Instant.now().toEpochMilli()))
                .status(TapjoyEvent.Status.reward)
                .build();

        tapjoyService.persist(event);
        tapjoyService.persist(event2);
        tapjoyService.persist(event3);

        TapjoyEvent event4 = TapjoyEvent.builder()
                .locale("en-US")
                .platform("ios")
                .requestId("e4fb0833-0459-4431-8019-eaca7b565aa7")
                .snuid(6068546800189440L)
                .currency(1)
                .verifier("216aa4ad1571616f4f2d79a87deb1e04")
                .postbackAt(new Timestamp(Instant.now().toEpochMilli()))
                .status(TapjoyEvent.Status.postback)
                .build();

        assertThrows(TapjoyException.class, () -> tapjoyService.checkRewarded(event4));
    }

    @Test
    public void test_verifier_generate() {
        String generated = DigestUtils.md5Hex(_REQUEST_ID + ":" + _SNUID + ":" + _CURRENCY + ":" + pantherProperties.getTapjoy().getSecretKey());
        logger.info("userId: {}, verifier(beta): {}", _REQUEST_ID, generated);
    }

}