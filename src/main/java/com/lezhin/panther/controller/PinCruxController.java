package com.lezhin.panther.controller;

import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.FraudException;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.internal.Result;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.pg.pincrux.ADEvent;
import com.lezhin.panther.pg.pincrux.CruxADs;
import com.lezhin.panther.pg.pincrux.CruxEvent;
import com.lezhin.panther.pg.pincrux.OsFlag;
import com.lezhin.panther.pg.pincrux.PinCruxService;
import com.lezhin.panther.util.ApiKeyManager;
import com.lezhin.panther.util.DateUtil;
import com.lezhin.panther.util.JsonUtil;

import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author benjamin
 * @since 2018.1.10
 */
@RestController
@RequestMapping("/pincrux/v1")
public class PinCruxController {

    private static final Logger logger = LoggerFactory.getLogger(PinCruxController.class);
    private PinCruxService pinCruxService;
    private PantherProperties pantherProperties;
    private ApiKeyManager apiKeyManager;

    public PinCruxController(final PinCruxService pinCruxService, final PantherProperties pantherProperties,
                             final ApiKeyManager apiKeyManager) {
        this.pinCruxService = pinCruxService;
        this.pantherProperties = pantherProperties;
        this.apiKeyManager = apiKeyManager;
    }

    @Deprecated
    public static final String CRUXKEY = "lezhinKey";

    @RequestMapping(value = "/ads", method = RequestMethod.GET)
    @ResponseBody
    public Result<CruxADs> getAds(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam String usrkey,
                                  @RequestParam(required = false, defaultValue = "true") Boolean list,
                                  @RequestParam(required = false, defaultValue = "false") Boolean cpi) {
        logger.debug("getAds.params = {}", request.getQueryString());
        try {
            Optional.ofNullable(usrkey).map(e -> Long.parseLong(usrkey)).orElse(-1L);
        } catch (Exception e) {
            throw new ParameterException(Executor.Type.UNKNOWN,
                    "usrkey should can be parsed to Long. usrkey=" + usrkey);
        }
        //list=false&usrkey=6623614697734144
        //list=0&usrkey=6151969840037888
        Result<CruxADs> result = new Result<>();
        if (request.getHeader("X-LZ-Locale") == null
                || !request.getHeader("X-LZ-Locale").toLowerCase().equals("ko-kr")
                || !pantherProperties.isPincruxAvailable()
                || (usrkey == null)) {
            logger.info("return no item. locale={}, pincruxAvailable={}, usrky={}",
                    request.getHeader("X-LZ-Locale"), pantherProperties.isPincruxAvailable(), usrkey);
            CruxADs data = new CruxADs();
            data.setDaFlag("N");
            data.setItems(new ArrayList<>());
            data.setItemCount(0);
            data.setTotalCoin(0);
            result.setCode(Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_OK.getCode()));
            result.setData(data);
            return result;
        }

        CruxADs data = pinCruxService.getPincruxADs(pantherProperties.getPincrux().getPubkey(),
                Long.parseLong(usrkey), getOsFlag(request), cpi);
        if (data.getStatus().equals("S")) {
            data.setItemCount(data.getItems().size());
            if (!list) {
                data.setItems(null);
            }
            result.setCode(Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_OK.getCode()));
            result.setData(data);
        } else {
            // TODO Error 상황도 HTTP.OK 로 내려주고 있음. 이미 client에 배포되어서 고치지 못함.
            result.setCode(-1 * Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_ERROR.getCode()));
            result.setDescription(data.getMsg());
        }

        logger.debug("return result.code = {}. description={}, totalCoin={}, itemCount={}",
                result.getCode(), result.getDescription(),
                Optional.ofNullable(result.getData()).map(ads -> ads.getTotalCoin()).orElse(0),
                Optional.ofNullable(result.getData()).map(ads -> ads.getItemCount()).orElse(0));

        return result;
    }

    @RequestMapping(value = "/ads", method = RequestMethod.POST)
    @ResponseBody
    public Result attp(HttpServletRequest request, HttpServletResponse response,
                       @RequestBody Map<String, String> map) {
        logger.info("-- attp.requestBody = {}, platform = {}", map, request.getHeader("X-LZ-Platform"));
        // {appkey=102694, adv_id=72f8517d-71eb-45e0-8d80-8e9828130702, mtype=wifi, usrkey=6267614872469504}, Platform = KG

        if (!pantherProperties.isPincruxAvailable()) {
            return new Result(-1 * Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_NOT_AVAILABLE.getCode()),
                    "Service not available temporally");
        }
        CruxEvent cruxRequest = null;
        try {
            cruxRequest = toCruxEvent(map, request);
        } catch (ParameterException e) {
            return new Result(-1 * Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_ERROR.getCode()),
                    e.getMessage());
        }

        if (Objects.equals(cruxRequest.getOs_flag(), OsFlag.ALL.flag())) {
            return new Result(-1 * Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_ERROR.getCode()), "Need OS header info");
        }

        try {
            CruxEvent data = pinCruxService.attp(cruxRequest);
            Result<CruxEvent> result = toResult(data);
            logger.info("attp.return. appkey={}, usrkey={}, res={}",
                    map.get("appkey"), map.get("usrkey"),
                    JsonUtil.toJson(result));
            return result;
        } catch (Exception e) {
            logger.warn("Failed to attp.", e);
            return new Result(-1 * Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_ERROR.getCode()),
                    "Internal Error");
        }

    }

    @RequestMapping(value = "/comp", method = RequestMethod.POST)
    @ResponseBody
    public Result comp(HttpServletRequest request, HttpServletResponse response,
                       @RequestBody Map<String, String> map) {
        logger.info("-- comp.requestBody = {}", map);
        if (!pantherProperties.isPincruxAvailable()) {
            return new Result(-1 * Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_NOT_AVAILABLE.getCode()),
                    "Service not available temporally");
        }

        CruxEvent cruxRequest = null;
        try {
            cruxRequest = toCruxEvent(map, request);
        } catch (ParameterException e) {
            return new Result(-1 * Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_ERROR.getCode()),
                    e.getMessage());
        }

        try {
            CruxEvent data = pinCruxService.comp(cruxRequest);
            Result<CruxEvent> result = toResult(data);
            logger.info("comp.return. appkey={}, usrkey={}, res={}",
                    map.get("appkey"), map.get("usrkey"),
                    JsonUtil.toJson(result));
            return result;
        } catch (Exception e) {
            logger.warn("Failed to comp.", e);
            return new Result(-1 * Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_ERROR.getCode()),
                    "Internal Error");
        }
    }

    @RequestMapping(value = "/postback", method = RequestMethod.POST)
    @ResponseBody
    public <T> ResponseEntity<T> postback(HttpServletRequest request, HttpServletResponse response,
                                          @RequestParam Map<String, String> map) {
        logger.info("-- postback.params = {}", map);

        // PinCruxController.setPostBack.params =
        // {appkey=101596, pubkey=910277, usrkey=5557547582619648, app_title=하이마트 인스타그램(최초 팔로우),
        // coin=2, transid=d23a8c34a6e1faae0236366ad995b31a740823d4, commission=200, os_flag=1}

        CruxEvent cruxRequest = null;
        try {
            cruxRequest = toCruxEvent(map, request);
            Optional.ofNullable(cruxRequest.getPubkey()).orElseThrow(() ->
                    new ParameterException(Executor.Type.UNKNOWN, "pubkey can not be empty"));
            Optional.ofNullable(cruxRequest.getPubkey()).filter(e -> Objects.equals(e, pantherProperties
                    .getPincrux().getPubkey())).orElseThrow(() -> new ParameterException(Executor.Type.UNKNOWN,
                    "pubkey is should be " + pantherProperties.getPincrux().getPubkey()));
            Optional.ofNullable(cruxRequest.getOs_flag()).orElseThrow(() ->
                    new ParameterException(Executor.Type.UNKNOWN, "osFlag can not be empty"));
            Optional.ofNullable(cruxRequest.getApp_title()).orElseThrow(() ->
                    new ParameterException(Executor.Type.UNKNOWN, "appTitle can not be empty"));
            Optional.ofNullable(cruxRequest.getTransid()).orElseThrow(() ->
                    new ParameterException(Executor.Type.UNKNOWN, "transid can not be empty"));
            Optional.ofNullable(cruxRequest.getCoin()).orElseThrow(() ->
                    new ParameterException(Executor.Type.UNKNOWN, "coin can not be empty"));
        } catch (ParameterException e) {
            logger.warn("BAD request. {}", e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        String dt = DateUtil.format(Instant.now().toEpochMilli(), DateUtil.ASIA_SEOUL_ZONE, "yyyy-MM-dd HH:mm:ss");
        try {
            pinCruxService.postback(cruxRequest);
        } catch (ParameterException e) {
            String data = String.format("%s,%s,%s,%s,%s,%s,%s", cruxRequest.getAppkey(),
                    cruxRequest.getUsrkey(), cruxRequest.getOs_flag(),
                    cruxRequest.getTransid(), cruxRequest.getApp_title(), cruxRequest.getCoin(), dt);
            logger.warn("PinCruxController.postback.paramerror: {}, data = [{}]", e.getMessage(), data);
            return new ResponseEntity(e.getMessage(), HttpStatus.OK);
        } catch (Exception e) {
            String data = String.format("%s,%s,%s,%s,%s,%s,%s", cruxRequest.getAppkey(),
                    cruxRequest.getUsrkey(), cruxRequest.getOs_flag(),
                    cruxRequest.getTransid(), cruxRequest.getApp_title(), cruxRequest.getCoin(), dt);
            logger.warn("!!! PinCruxController.postback.error. Need Manual REFUND: [" + data + "]", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.info("postback. return OK");
        return new ResponseEntity("OK", HttpStatus.OK);
    }

    private Integer getOsFlag(HttpServletRequest request) {
        String platform = request.getHeader("X-LZ-Platform");
        if (StringUtil.isNullOrEmpty(platform)) {
            return OsFlag.ALL.flag(); //전체
        } else if (platform.toLowerCase().equals("ka")) {
            return OsFlag.IOS.flag(); //앱스토어
        } else {
            return OsFlag.ANDROID.flag(); //구글 K?
        }
    }

    /**
     * @throws ParameterException
     */
    private CruxEvent toCruxEvent(Map<String, String> requestMap, HttpServletRequest request) {
        CruxEvent cruxRequest = null;

        if (StringUtils.isEmpty(requestMap.get("appkey"))) {
            throw new ParameterException(Executor.Type.UNKNOWN, "appkey can not be null");
        }
        if (StringUtils.isEmpty(requestMap.get("usrkey"))) {
            throw new ParameterException(Executor.Type.UNKNOWN, "usrkey can not be null");
        }

        try {
            cruxRequest = JsonUtil.fromJson(JsonUtil.toJson(requestMap), CruxEvent.class);
        } catch (Exception e) {
            logger.warn("Failed to convert to CruxEvent", e);
            throw new ParameterException(Executor.Type.UNKNOWN, e);
        }
        cruxRequest.setClient_ip(request.getHeader("X-Forwarded-For"));
        cruxRequest.setAuthToken(request.getHeader("Authorization"));
        if (StringUtils.isEmpty(cruxRequest.getOs_flag())) {
            cruxRequest.setOs_flag(getOsFlag(request));
        }
        if (StringUtils.isEmpty(cruxRequest.getPubkey())) {
            cruxRequest.setPubkey(pantherProperties.getPincrux().getPubkey());
        }
        return cruxRequest;
    }

    private Result toResult(CruxEvent data) {

        Result<CruxEvent> result = new Result<>();
        if (ResponseInfo.ResponseCode.PINCRUX_OK.getCode().equals(data.getCode())) {
            result.setCode(Integer.parseInt(data.getCode()));
            result.setData(data);
        } else {
            // TODO Error 상황도 HTTP.OK 로 내려주고 있음. 이미 client에 배포되어서 고치지 못함.
            result.setCode(-1 * Integer.parseInt(data.getCode()));
            result.setDescription(data.getMsg());
        }
        return result;
    }

    @RequestMapping(value = "/adevents", method = RequestMethod.GET)
    @ResponseBody
    public <T> ResponseEntity<T> getADEvents(HttpServletRequest request, HttpServletResponse response,
                                             @RequestParam String start,
                                             @RequestParam String end) {
        logger.info("pincrux.adevents. params = {}", request.getQueryString());

        Optional.ofNullable(request.getHeader("clientName")).orElseThrow(
                () -> new FraudException(Executor.Type.UNKNOWN, "You are not authorized")
        );
        Optional.ofNullable(request.getHeader("clientName")).filter(h -> h.equals("lezhin")).orElseThrow(
                () -> new FraudException(Executor.Type.UNKNOWN, "You are not authorized.")
        );

        if (!apiKeyManager.validate(request.getHeader("clientName"), request.getHeader("apiKey"))) {
            return new ResponseEntity("Fraud", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Instant startDateTime;
        Instant endDateTime;
        try {
            startDateTime = DateUtil.toInstantFromDate(start, "yyyyMMdd", DateUtil.ASIA_SEOUL_ZONE);
            endDateTime = DateUtil.toInstantFromDate(end, "yyyyMMdd", DateUtil.ASIA_SEOUL_ZONE);
        } catch (Exception e) {
            throw new ParameterException(Executor.Type.UNKNOWN, "start and end should be 'yyyyMMdd'");
        }

        List<ADEvent> adEvents = pinCruxService.findByAttpAtBetween(startDateTime.toEpochMilli(),
                endDateTime.toEpochMilli());

        Result<List<ADEvent>> result = new Result<>();
        result.setCode(Integer.parseInt(ResponseInfo.ResponseCode.LEZHIN_OK.getCode()));
        result.setData(adEvents);

        return new ResponseEntity(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/adevents/users/{usrkey}", method = RequestMethod.GET)
    @ResponseBody
    public <T> ResponseEntity<T> getADEventsBy(HttpServletRequest request, HttpServletResponse response,
                                             @PathVariable Long usrkey) {
        logger.info("pincrux.getADEventsBy. usrkey = {}", usrkey);

        Optional.ofNullable(request.getHeader("clientName")).orElseThrow(
                () -> new FraudException(Executor.Type.UNKNOWN, "You are not authorized")
        );
        Optional.ofNullable(request.getHeader("clientName")).filter(h -> h.equals("lezhin")).orElseThrow(
                () -> new FraudException(Executor.Type.UNKNOWN, "You are not authorized.")
        );

        if (!apiKeyManager.validate(request.getHeader("clientName"), request.getHeader("apiKey"))) {
            return new ResponseEntity("Fraud", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        List<ADEvent> adEvents = pinCruxService.findByUsrkeyOrderByIdDesc(usrkey);

        Result<List<ADEvent>> result = new Result<>();
        result.setCode(Integer.parseInt(ResponseInfo.ResponseCode.LEZHIN_OK.getCode()));
        result.setData(adEvents);

        return new ResponseEntity(result, HttpStatus.OK);
    }


}
