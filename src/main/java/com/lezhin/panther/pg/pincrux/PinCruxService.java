package com.lezhin.panther.pg.pincrux;

import com.lezhin.panther.HttpClientService;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.HttpClientException;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.exception.PincruxException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.internal.Result;
import com.lezhin.panther.notification.SlackEvent;
import com.lezhin.panther.notification.SlackMessage;
import com.lezhin.panther.notification.SlackNotifier;
import com.lezhin.panther.redis.RedisService;
import com.lezhin.panther.util.DateUtil;
import com.lezhin.panther.util.JsonUtil;

import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author benjamin
 * @since 2017.12.19
 */

@Service
public class PinCruxService {

    private static final Logger logger = LoggerFactory.getLogger(PinCruxService.class);

    private static final String OFFER = "/offer.pin";
    private static final String ATTP = "/attp.pin";
    private static final String COMP = "/comp.pin";

    private static final Integer walletExpireMonth = 6;

    private static final String MODULE_NAME = "pincrux";
    private static final Long DEFAULT_USER = Long.valueOf(-1L);
    private static final String ADS_CACHE_KEY = "ads_cache_key";

    @Autowired
    private RestTemplate restTemplate;
    private PantherProperties pantherProperties;
    private RedisService redisService;
    private SlackNotifier slackNotifier;
    private HttpClientService httpClientService;
    private ADEventRepository adEventRepository;

    public PinCruxService(final PantherProperties pantherProperties, final RedisService redisService,
                          final SlackNotifier slackNotifier, final HttpClientService httpClientService,
                          final ADEventRepository adEventRepository) {
        this.pantherProperties = pantherProperties;
        this.redisService = redisService;
        this.slackNotifier = slackNotifier;
        this.httpClientService = httpClientService;
        this.adEventRepository = adEventRepository;
    }

    @Transactional(value = "pantherTransactionManager")
    public ADEvent persistADEvent(final ADEvent adEvent) {
        try {
            return adEventRepository.save(adEvent);
        } catch (Throwable e) {
            // Nothing to rollback
            logger.warn("Failed to persist ADEvent = " + JsonUtil.toJson(adEvent), e);
            throw e;
        }
    }

    /**
     * @return the latest one or Optional.empty
     */
    public Optional<ADEvent> findADEventBy(Long usrkey, Integer appkey, Integer osFlag) {

        List<ADEvent> events = adEventRepository.findByUsrkeyAndAppkeyAndOsFlagOrderByIdDesc(usrkey, appkey, osFlag);
        events.stream().forEach(e -> logger.debug("{} = {}", e.getId(), JsonUtil.toJson(e)));

        return events.stream().max((o1, o2) -> (int) (o1.getId() - o2.getId()));

    }

    public Optional<ADEvent> findADEventBy(Long usrkey, Integer appkey, Integer osFlag, String transid) {

        List<ADEvent> events = adEventRepository.findByUsrkeyAndAppkeyAndOsFlagAndTransidOrderByIdDesc(usrkey, appkey,
                osFlag, transid);
        events.stream().forEach(e -> logger.debug("{}, {} = {}", e.getId(), e.getTransid(),
                JsonUtil.toJson(e)));

        return events.stream().max((o1, o2) -> (int) (o1.getId() - o2.getId()));
    }

    public CruxADs cacheCruxADs(final Integer osFlag) {
        int cacheRetention = pantherProperties.getPincrux().getCacheRetention(); //ms

        String cacheKey = RedisService.generateKey(MODULE_NAME, ADS_CACHE_KEY, String.valueOf(osFlag));

        UriComponents uriComponents = null;
        try {
            uriComponents = UriComponentsBuilder.newInstance()
                    .uri(new URI(pantherProperties.getPincrux().getPincruxUrl()))
                    .path(OFFER)
                    .queryParam("pubkey", pantherProperties.getPincrux().getPubkey())
                    .queryParam("usrkey", DEFAULT_USER.toString())
                    .queryParam("cpi_flag", "y")
                    .queryParam("os_flag", osFlag)
                    .build()
                    .encode();
        } catch (URISyntaxException e) {
            throw new PantherException(e);
        }

        // 핀크럭스 리턴이 text/html 로 json을 준다
        logger.info("REQ offer = {}", uriComponents.toUri());
        CruxADs ads = null;
        Exception occurred = null;
        String responseText = null;

        long startTime = Instant.now().toEpochMilli();
        try {
            responseText = restTemplate.getForObject(uriComponents.toUri(), String.class);
            long endTime = Instant.now().toEpochMilli();
            long responseTime = endTime - startTime;
            logger.info("pincrux.offer response time = {} ms", responseTime);
            processSLA(responseTime);
            logger.debug("pincrux.offer = {}", responseText);
            ads = JsonUtil.fromJson(responseText, CruxADs.class);
            if (ads != null && !"S".equals(ads.getStatus())) {
                processSLA(new Exception(
                        "pincrux.offer.response status=" + ads.getStatus() +", item_cnt=" + ads.getItemCount()));
            }
            logger.info("cacheCruxADs. status = {}, item_cnt = {}, osFlag= {}",
                    ads.getStatus(), ads.getItemCount(), osFlag);
            redisService.setValue(cacheKey, ads, cacheRetention, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            occurred = e;
        } finally {
            if (occurred != null) {
                logger.info("Failed to getPincruxADs. exception= {}, response = {}", occurred.getMessage(),
                        responseText);
                processSLA(occurred);
                ads = new CruxADs();
                ads.setStatus("F");
                ads.setCode("99");
                ads.setMsg(occurred.getMessage());
            }
        }

        return ads;
    }

    public CruxADs getPincruxADs(Integer pupkey, Long usrkey, Integer osFlag, Boolean includeCPI) {
        String cacheKey = RedisService.generateKey(MODULE_NAME, ADS_CACHE_KEY, String.valueOf(osFlag));
        CruxADs ads = redisService.getValue(cacheKey, CruxADs.class);

        if (ads == null) {
            ads = cacheCruxADs(osFlag);
        }

        if (ads == null) {
            logger.info("getCruxADs is null. return empty CruxADs");
            ads = new CruxADs();
            ads.setStatus("F");
            ads.setItems(new ArrayList<>());
        }

        if (ads.getItems() == null) {
            ads.setItems(new ArrayList());
        }

        if (osFlag != 0) { // android(ios) 일 경우, osFlag = 0인 항목도 추가. 
            List<Item> itemsFiltered = ads.getItems().stream()
                    .filter(x -> (Objects.equals(x.getOsFlag(), Integer.valueOf(0))
                            || Objects.equals(x.getOsFlag(), osFlag)) && !StringUtil.isNullOrEmpty(x.getViewTitle())
                            && (includeCPI.booleanValue() || Optional.ofNullable(x.getAdFlag())
                            .map(s -> !s.equals("CPI")).orElse(Boolean.FALSE)))
                    .collect(Collectors.toList());
            ads.setItems(itemsFiltered);
        }

        Integer totalCoin = 0;
        for (Item item : ads.getItems()) {
            Integer coin = item.getCoinInt();
            Optional.ofNullable(coin).orElseThrow((() -> new PincruxException(Executor.Type.UNKNOWN,
                    "CoinInt can not be null. Check the offered list")));
            totalCoin += !StringUtils.isEmpty(item.getGrp()) && item.getGrp().equals("pincrux") ? coin : 0;
        }

        ads.setTotalCoin(totalCoin);

        return ads;
    }

    public CruxEvent attp(CruxEvent reqData) {
        String attpUrl = pantherProperties.getPincrux().getPincruxUrl() + ATTP;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap();
        map.add("appkey", reqData.getAppkey().toString());
        map.add("pubkey", reqData.getPubkey().toString());
        map.add("usrkey", reqData.getUsrkey().toString());
        map.add("adv_id", reqData.getAdv_id());
        map.add("mtype", reqData.getMtype());
        map.add("client_ip", reqData.getClient_ip());
        map.add("os_flag", reqData.getOs_flag().toString());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        logger.info("attp.request url = {}, request = {}", attpUrl, JsonUtil.toJson(request));
        // parameter로 보내기.
        HttpEntity<String> responseString = postForEntity(attpUrl, request, String.class, Executor.Type.UNKNOWN);

        CruxEvent res = convert(responseString, Executor.Type.UNKNOWN);

        CruxADs pincruxADs = getPincruxADs(reqData.getPubkey(), reqData.getUsrkey(), reqData.getOs_flag(), Boolean.TRUE);
        List<Item> items = pincruxADs.getItems().stream()
                .filter(item -> Objects.equals(item.getAppkey(), reqData.getAppkey())
                        && (Objects.equals(item.getOsFlag(), reqData.getOs_flag())
                        || Objects.equals(OsFlag.ALL.flag(), item.getOsFlag())))
                .collect(Collectors.toList());
        if (items.size() == 0) {
            throw new PantherException("Item not found for attp. request = " + JsonUtil.toJson(reqData));
        }
        Item item = items.get(0);
        logger.info("attp.item. appkey={}, coin={}, appName={}, viewTitle={}, ",
                item.getAppkey(), item.getCoinInt(), item.getAppName(), item.getViewTitle());
        ADEvent adEvent = ADEvent.builder()
                .usrkey(reqData.getUsrkey())
                .appkey(reqData.getAppkey())
                .osFlag(reqData.getOs_flag())
                .token(reqData.getAuthToken())
                .cointInt(item.getCoinInt())
                .appName(item.getAppName())
                .customUrl(res.getCustomUrl())
                .attpAt(new Timestamp(Instant.now().toEpochMilli()))
                .status(ADEvent.Status.attp)
                .build();

        try {
            persistADEvent(adEvent);
        } catch (Exception e) {
            // persist failed but do nothing.
            logger.warn("Persist ADEvent failed but to nothing");
        }

        return res;
    }

    public CruxEvent comp(CruxEvent reqData) {
        String compUrl = pantherProperties.getPincrux().getPincruxUrl() + COMP;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("appkey", reqData.getAppkey().toString());
        map.add("pubkey", reqData.getPubkey().toString());
        map.add("usrkey", reqData.getUsrkey().toString());
        map.add("adv_id", reqData.getAdv_id());
        map.add("client_ip", reqData.getClient_ip());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(map, headers);
        logger.info("-- comp.request url = {}, request = {}", compUrl, request);
        // parameter로 보내기.
        HttpEntity<String> responseString = postForEntity(compUrl, request, String.class, Executor.Type.UNKNOWN);

        CruxEvent res = convert(responseString, Executor.Type.UNKNOWN);
        return res;
    }

    /**
     * @param reqData
     * @throws {@link ParameterException} if ADEvent is already rewarded.
     * @throws {@link PincruxException} if ADEvent reward failed
     */
    public void postback(CruxEvent reqData) {
        // check the rewarded ADEvent
        findADEventBy(reqData.getUsrkey(), reqData.getAppkey(), reqData.getOs_flag(), reqData.getTransid())
                .filter(adEvent1 -> adEvent1.getStatus() == ADEvent.Status.reward
                        && !StringUtils.isEmpty(adEvent1.getRewardAt()))
                .ifPresent(ad -> {
                    throw new ParameterException(
                            String.format("ADEvent is already rewarded. user=%s, appkey=%s, transid=%s, rewardAt=%s",
                                    reqData.getUsrkey(), reqData.getAppkey(), reqData.getTransid(),
                                    DateUtil.getDateTimeString(ad.getRewardAt().getTime())));
                });

        // not yet reward by transid
        ADEvent adEvent = findADEventBy(reqData.getUsrkey(), reqData.getAppkey(), reqData.getOs_flag())
                .orElseGet(() -> {
                    // TODO fraud check. adEvent of attp가 없다면, postback이 fraud 일 수 있음.
                    logger.warn("--- No adEvent of attp. Build for postback.");
                    return ADEvent.builder().status(ADEvent.Status.attp).build();
                });
        adEvent.setAppkey(reqData.getAppkey());
        adEvent.setUsrkey(reqData.getUsrkey());
        adEvent.setTransid(reqData.getTransid());
        adEvent.setOsFlag(reqData.getOs_flag());
        adEvent.setAppTitle(reqData.getApp_title());
        adEvent.setCoin(reqData.getCoin());

        logger.info("postback.adEvent = {}", JsonUtil.toJson(adEvent));
        if (!Objects.equals(adEvent.getCoin(), adEvent.getCointInt())) {
            logger.warn("Coin is different. postback={}, attp={}",
                    reqData.getCoin(), adEvent.getCointInt());
        }

        adEvent.setPostbackAt(new Timestamp(Instant.now().toEpochMilli()));
        adEvent.setStatus(ADEvent.Status.postback);
        persistADEvent(adEvent);
        logger.info("saved. postback. usrkey={}, appkey={}, appTitle={}, coin={}",
                adEvent.getUsrkey(), adEvent.getAppkey(), adEvent.getAppTitle(), adEvent.getCoin());

        try {
            sendCoinReward(adEvent.getCoin(), adEvent);
        } catch (Exception e) {
            logger.info("Failed to coin reward. Check CMS wallets={}, user={}",
                    pantherProperties.getWallets().getApiUrl(), adEvent.getUsrkey());
            throw new PantherException(Executor.Type.UNKNOWN,
                    "Failed to coin reward. ADEvent: " + JsonUtil.toJson(adEvent));
        }
        adEvent.setRewardAt(new Timestamp(Instant.now().toEpochMilli()));
        adEvent.setStatus(ADEvent.Status.reward);
        persistADEvent(adEvent);
        logger.info("saved. reward. {}", JsonUtil.toJson(adEvent));

        sendPushMessage(adEvent.getCoin(), adEvent);

    }


    private void sendCoinReward(Integer coin, ADEvent adEvent) {

        // TODO message
        String presentDescription = String.format("무료코인존: <%s>이벤트에 참여해주셔서 감사합니다."
                , adEvent.getAppName());
        String purchaseTitle = String.format("무료코인존: [%s]"
                , adEvent.getAppName());//appItem.getViewTitle().length() > 50 ? appItem.getViewTitle().substring(0,50) : appItem.getViewTitle());
        String presentTitle = String.format("%s 보너스코인", coin);
        Wallets wallets = new Wallets();
        wallets.setUserId(adEvent.getUsrkey());
        wallets.setLocale("ko-KR");
        wallets.setPlatform(adEvent.getOsFlag() == 1 ? "android" : "ios");
        wallets.setCompanyEventId(this.pantherProperties.getWallets().getCompanyEventIdPinCrux());
        wallets.setUsageRestrictionId(this.pantherProperties.getWallets().getUsageRestrictionIdPinCrux());
        wallets.setPurchaseType("R");
        wallets.setPurchaseTitle(purchaseTitle);
        wallets.setSendPresent(true);
        wallets.setPresentTitle(presentTitle);
        wallets.setPresentDescription(presentDescription);
        wallets.setAmount(coin);
        wallets.setImmediate(true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Authorization", "Bearer " + pantherProperties.getCmsToken());
        HttpEntity<Wallets> request = new HttpEntity<Wallets>(wallets, headers);

        logger.info("sendCoinReward. url = {}, reqBody = {}",
                pantherProperties.getWallets().getApiUrl(), JsonUtil.toJson(request));

        HttpEntity<Result> responseText = postForEntity(pantherProperties.getWallets().getApiUrl(),
                request, Result.class, Executor.Type.UNKNOWN);
        if (responseText.getBody() != null) {
            logger.info("sendCoinReward. {} = {}", responseText.getBody().getCode(), responseText.getBody().getDescription());
            if (responseText.getBody().getCode() != 0) {
                throw new PantherException(Executor.Type.UNKNOWN, "Failed to reward. Request: "
                        + JsonUtil.toJson(request));
            }
        }

        logger.info("sendCoinReward. resBody = {}", responseText.getBody());
    }

    private void sendPushMessage(Integer coin, ADEvent appItem) {
        try {
            // TODO message
            String msg = String.format("%s 보너스코인 지급 완료!  무료코인존: <%s>이벤트에 참여해주셔서 감사합니다. (지급일로부터 %s개월간 사용 가능)"
                    , coin, appItem.getAppName(), walletExpireMonth);
            PinCruxPushRequest pcr = new PinCruxPushRequest(appItem.getUsrkey(),
                    "lezhin://present", "레진코믹스", msg);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<PinCruxPushRequest> request = new HttpEntity<>(pcr, headers);

//            HttpEntity<String> responseText = postForEntity(pantherProperties.getPushUrl(), request, String.class,
//                    Executor.Type.UNKNOWN);

            String responseText = restTemplate.postForObject(this.pantherProperties.getPushUrl(),
                    pcr,
                    String.class);


            logger.info("sendPushMessage() = {}, resBody = {}", pcr, responseText);
        } catch (Exception e) {
            logger.warn("Failed to send PushMessage", e);
        }
    }

    public <T> HttpEntity<T> postForEntity(String url, HttpEntity<?> requestEntity, Class<T> responseType,
                                           Executor.Type type) {
        HttpEntity<T> response = null;
        try {
            response = httpClientService.postForEntity(url, requestEntity, responseType, type);
        } catch (Exception e) {
            throw new PantherException(type, "PincruxService.postForEntity.error: " + e.getMessage(), e);
        }
        return response;

    }

    public CruxEvent convert(HttpEntity<String> result, Executor.Type type) {
        if (result == null) {
            throw new HttpClientException(type, "PincruxService. result is null");
        }

        if (result.getBody() == null) {
            throw new HttpClientException(type, "PincruxService. response.getBody is null");
        }

        CruxEvent cruxResponse = null;
        try {
            cruxResponse = JsonUtil.fromJson(result.getBody(), CruxEvent.class);
            logger.info("pincrux.response = {}", JsonUtil.toJson(cruxResponse));
        } catch (Exception e) {
            throw new PantherException(type, "Failed to convert CruxEvent: result.body=" + result.getBody(), e);
        }

        return cruxResponse;
    }

    @Transactional(value = "pantherTransactionManager", readOnly = true)
    public List<ADEvent> findByAttpAtBetween(long start, long end) {
        logger.info("start = {}, end = {}", start, end);
        return adEventRepository.findByAttpAtBetween(new Timestamp(start), new Timestamp(end));
    }

    @Transactional(value = "pantherTransactionManager", readOnly = true)
    public List<ADEvent> findByUsrkeyOrderByIdDesc(Long usrkey) {
        return adEventRepository.findByUsrkeyOrderByIdDesc(usrkey);
    }


    /**
     * TODO 임시 메서드. response time aggregator.
     * 현재는 warn만 발행. 이를 기준은로 circuit breaker 가동 필요.
     *
     * @param responseTime
     */
    private void processSLA(long responseTime) {
        int timeout = pantherProperties.getPincrux().getTimeout(); //ms
        if (responseTime > timeout) {
            slackNotifier.notify(SlackEvent.builder()
                    .header("PINCRUX")
                    .level(SlackMessage.LEVEL.WARN)
                    .title("pincrux.offer responseTime > " + timeout + " ms")
                    .message("responseTime = " + responseTime + " ms")
                    .build());

        }

    }

    /**
     * TODO 임시 메서드.
     *
     * 현재는 warn만 발행. 이를 기준은로 circuit breaker 가동 필요.
     *
     * @param e
     */
    private void processSLA(Throwable e) {
        slackNotifier.notify(SlackEvent.builder()
                .header("PINCRUX")
                .level(SlackMessage.LEVEL.WARN)
                .title("pincrux.offer error ")
                .message("pincrux.offer error occurred")
                .exception(e)
                .build());


    }
}
