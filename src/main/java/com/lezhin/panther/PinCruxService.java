package com.lezhin.panther;

import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.pg.pincrux.Wallets;
import com.lezhin.panther.pg.pincrux.PinCruxData;
import com.lezhin.panther.pg.pincrux.PinCruxDataInstallResult;
import com.lezhin.panther.pg.pincrux.PinCruxDataItem;
import com.lezhin.panther.pg.pincrux.PinCruxDataItemEnable;
import com.lezhin.panther.pg.pincrux.PinCruxPushRequest;
import com.lezhin.panther.pg.pincrux.PinCruxRequest;
import com.lezhin.panther.pg.pincrux.PinCruxUser;
import com.lezhin.panther.redis.RedisService;

import com.lezhin.panther.util.JsonUtil;


import io.netty.util.internal.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author benjamin
 * @since 2017.12.19
 */

@Service
public class PinCruxService {


    private static final String PINCRUX_PROTOCOL= "http";
    private static final String PINCRUX_HOST    = "api.pincrux.com";
    private static final String PINCRUX_LIST    = "/offer.pin";
    private static final String PINCRUX_CHECK   = "/attp.pin";
    private static final String PINCRUX_INSTALL = "/comp.pin";
    private static final Logger logger = LoggerFactory.getLogger(PinCruxService.class);
    private static final double lezhinVal = 160f;
    private static final String redisKeyBase = "panther/pincrux/users/";
    private static final Integer walletExpireMonth = 6;


    @Autowired
    private ClientHttpRequestFactory clientHttpRequestFactory;
    private PantherProperties pantherProperties;
    private RedisService redisService;
    private String urlCheckAd;
    private String urlSendInstall;


    public PinCruxService(PantherProperties pantherProperties, RedisService redisService) {
        this.pantherProperties = pantherProperties;
        this.redisService = redisService;
    }

    public PinCruxData getAds(Integer pubkey, Long usrkey, Integer os_flag) {

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(PINCRUX_PROTOCOL).host(PINCRUX_HOST).path(PINCRUX_LIST)
                .queryParam("pubkey", pubkey)
                .queryParam("usrkey", usrkey)
                .queryParam("os_flag", os_flag)
                .queryParam("test_flag", pantherProperties.getPincrux().getTestFlag()?"y":"n")//상용화시 이 플래그를 n 으로
                .build()
                .encode();
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        /*젠장 핀크럭스 리턴이 text/html 로 json을 준다*/
        logger.info("REQ getAds = {}", uriComponents.toUri());
        String responseText = restTemplate.getForObject(uriComponents.toUri(), String.class);
        logger.info("RES getAds = {}", responseText);
        PinCruxData  res = JsonUtil.fromJson(responseText, PinCruxData.class);
        //filter By os_flag
        if(os_flag != 0 && res.getItem_cnt() > 0){
            List<PinCruxDataItem> itemsFiltered = res.getItem_list().stream().filter(x->(x.getOs_flag() == 0 || x.getOs_flag() == os_flag) && !StringUtil.isNullOrEmpty(x.getView_title())).collect(Collectors.toList());
            res.setItem_list(itemsFiltered);
        }else{
            res.setItem_list(new ArrayList<>());
        }
        Integer totalCoin = 0;
        for(PinCruxDataItem item : res.getItem_list()){
            Integer coin = this.getFee2Coin( item.getFee() );
            item.setCoin_int(coin);
            totalCoin += !StringUtil.isNullOrEmpty(item.getGrp()) && item.getGrp().equals("pincrux") ? coin : 0;
            item.initCamel();
        }
        res.setTotal_coin(totalCoin);
        res.initCamel();

        return res;
    }

    public PinCruxDataItemEnable checkEnable(PinCruxRequest reqData){
        if(StringUtil.isNullOrEmpty(urlCheckAd)){
            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme(PINCRUX_PROTOCOL).host(PINCRUX_HOST).path(PINCRUX_CHECK)
                    .build()
                    .encode();
            urlCheckAd = uriComponents.toUriString();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        /*reflection 쓸까 했는데 속도 생각해서 노가다로...*/
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("appkey", reqData.getAppkey().toString());
        map.add("pubkey", reqData.getPubkey().toString());
        map.add("usrkey", reqData.getUsrkey().toString());
        map.add("cruxkey", reqData.getCruxkey());
        map.add("subpid", reqData.getSubpid());
        map.add("dev_id", reqData.getDev_id());
        map.add("adv_id", reqData.getAdv_id());
        map.add("acc_id", reqData.getAcc_id());
        map.add("and_id", reqData.getAnd_id());
        map.add("device_brand", reqData.getDevice_brand());
        map.add("device_model", reqData.getDevice_model());
        map.add("mtype", reqData.getMtype());
        map.add("client_ip", reqData.getClient_ip());
        map.add("os_flag", reqData.getOs_flag().toString());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
         /*크럭스는 json 으로 넘기면 못 받는다..*/
        ResponseEntity<String> responseText = restTemplate.postForEntity(urlCheckAd, request,String.class );
        PinCruxDataItemEnable res = JsonUtil.fromJson(responseText.getBody(), PinCruxDataItemEnable.class);
        res.setCustomUrl(res.getCustom_url());
        logger.info("checkEnable() = {}, resBody = {}, res = {}", request, responseText.getBody(), res);

        //save at redis
        String redisKey = redisKeyBase+reqData.getUsrkey();
        logger.info("redis save = {}", redisKey);
        PinCruxData pcd = this.getAds(reqData.getPubkey(), reqData.getUsrkey(), reqData.getOs_flag());
        logger.info("redis save = {}", JsonUtil.toJson(pcd));
        PinCruxUser pcu = new PinCruxUser(reqData.getUsrkey(), reqData.getAuthHeader(), pcd.getItems());
        this.redisService.setValue(redisKey, pcu, 86400, TimeUnit.SECONDS);

        return res;
    }

    public PinCruxDataInstallResult setInstall(PinCruxRequest reqData) {

        if(StringUtil.isNullOrEmpty(urlSendInstall)){
            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme(PINCRUX_PROTOCOL).host(PINCRUX_HOST).path(PINCRUX_INSTALL)
                    .build()
                    .encode();
            urlSendInstall = uriComponents.toUriString();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("appkey", reqData.getAppkey().toString());
        map.add("pubkey", reqData.getPubkey().toString());
        map.add("usrkey", reqData.getUsrkey().toString());
        map.add("dev_id", reqData.getDev_id());
        map.add("adv_id", reqData.getAdv_id());
        map.add("acc_id", reqData.getAcc_id());
        map.add("and_id", reqData.getAnd_id());
        map.add("device_brand", reqData.getDevice_brand());
        map.add("device_model", reqData.getDevice_model());
        map.add("client_ip", reqData.getClient_ip());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        ResponseEntity<String> responseText = restTemplate.postForEntity(urlSendInstall, request,String.class );
        PinCruxDataInstallResult res = JsonUtil.fromJson(responseText.getBody(), PinCruxDataInstallResult.class);
        //logger.info("setInstall() = {}, resBody = {}, res = {}", request, responseText.getBody(), res);

        //save at redis
        String redisKey = redisKeyBase+reqData.getUsrkey();
        logger.info("redis save = {}", redisKey);
        PinCruxData pcd = this.getAds(reqData.getPubkey(), reqData.getUsrkey(), reqData.getOs_flag());
        logger.info("redis save = {}", JsonUtil.toJson(pcd));
        PinCruxUser pcu = new PinCruxUser(reqData.getUsrkey(), reqData.getAuthHeader(), pcd.getItems());
        this.redisService.setValue(redisKey, pcu, 86400, TimeUnit.SECONDS);


        return res;
    }

    public void setPostBack(PinCruxRequest reqData) throws Exception{
        String postBackKey = String.format("%s/%s/trans", redisKeyBase+reqData.getUsrkey(), reqData.getTransid());
        //load from redis
        Object postBack = this.redisService.getValue(postBackKey);
        if(postBack != null){
            throw new Exception(String.format("[%s] user's [%s] transId is already completed.", reqData.getUsrkey(),reqData.getTransid()));
        }
        String redisKey = redisKeyBase+reqData.getUsrkey();
        logger.info("redis get = {}", redisKey);
        PinCruxUser pcu = (PinCruxUser)this.redisService.getValue(redisKey);
        if(pcu == null || pcu.getAds() == null){
            throw new Exception("This postBack has no ad list at Cache, Do get List or Check Enable Ad First.");
        }
        reqData.setAuthHeader(pcu.getAuthHeader());
        PinCruxDataItem appItem = pcu.getAds().stream().filter(x->x.getAppkey().equals(reqData.getAppkey()) ).collect(Collectors.toList()).get(0);
        logger.info("pcu.getAds()[0] = {}", JsonUtil.toJson(appItem));
        Integer coin = this.getCoin(reqData.getPubkey(), appItem);
        setSendUserCoinReward(reqData, coin,  appItem);
        //Regist transid for prevent duple reward
        this.redisService.setValue(postBackKey, pcu, 100, TimeUnit.DAYS);
        setSendPushMessage(reqData, coin, pcu, appItem);

    }

    private Integer getCoin(Integer pubkey, PinCruxDataItem appItem)throws Exception{
        double fee = appItem.getFee();
        return getFee2Coin(fee);
    }

    private Integer getFee2Coin(double fee){
        Integer coin = (int)Math.ceil(fee / lezhinVal);
        return  coin;
    }

    private void setSendUserCoinReward(PinCruxRequest reqData, Integer coin,  PinCruxDataItem appItem){
        //DateTime dtOrg = new DateTime(new Date());
        //DateTime dtExpire = dtOrg.plusMonths(walletExpireMonth);
        //String dtExpireString = DateUtil.format(dtExpire.getMillis(), DateUtil.ASIA_SEOUL_ZONE, DateUtil.DATE_TIME_FORMATTER);
        String presentDescription = String.format("무료코인존: <%s>이벤트에 참여해주셔서 감사합니다."
                ,appItem.getAppName());
        String purchaseTitle = String.format("무료코인존: [%s]"
                , appItem.getAppName());//appItem.getViewTitle().length() > 50 ? appItem.getViewTitle().substring(0,50) : appItem.getViewTitle());
        String presentTitle  = String.format("%s 보너스코인", coin);
        Wallets wallets = new Wallets();
        wallets.setUserId(reqData.getUsrkey());
        wallets.setLocale("ko-KR");
        wallets.setPlatform(reqData.getOs_flag() == 1 ? "android" : "ios");
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
        headers.add("Authorization", "Bearer " + pantherProperties.getWallets().getCmsToken());
        HttpEntity<Wallets> request = new HttpEntity<Wallets>(wallets, headers);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        logger.info("setSendUserCoinReward() = {}, reqBody = {}", this.pantherProperties.getWallets().getApiUrl(), JsonUtil.toJson(request));
        String responseText = restTemplate.postForObject(this.pantherProperties.getWallets().getApiUrl(),
                request,
                String.class );
        logger.info("setSendUserCoinReward() resBody = {}",responseText);
    }

    private void setSendPushMessage(PinCruxRequest reqData, Integer coin, PinCruxUser pcu, PinCruxDataItem appItem){
        if(!StringUtil.isNullOrEmpty(pantherProperties.getPushUrl())){
            String msg = String.format("%s 보너스코인 지급 완료!  무료코인존: <%s>이벤트에 참여해주셔서 감사합니다. (지급일로부터 %s개월간 사용 가능)"
                    ,coin , appItem.getAppName(), walletExpireMonth);
            PinCruxPushRequest pcr  = new PinCruxPushRequest(reqData.getUsrkey(),
                    "lezhin://present", "레진코믹스", msg);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

            String responseText = restTemplate.postForObject(this.pantherProperties.getPushUrl(),
                    pcr,
                    String.class );
            logger.info("setSendPushMessage() = {}, resBody = {}", pcr, responseText);
        }


    }
}
