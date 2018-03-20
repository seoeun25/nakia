package com.lezhin.panther.controller;

import com.lezhin.panther.PinCruxService;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.internal.Result;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.pg.pincrux.PinCruxData;
import com.lezhin.panther.pg.pincrux.PinCruxDataInstallResult;
import com.lezhin.panther.pg.pincrux.PinCruxDataItemEnable;
import com.lezhin.panther.pg.pincrux.PinCruxRequest;
import com.lezhin.panther.util.DateUtil;

import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
import java.util.Map;

/**
 * ø
 * TODO
 *
 * @author benjamin
 * @since 2018.1.10
 */
@RestController
@RequestMapping("/pincrux/v1")
public class PinCruxController {

    private static final Logger logger = LoggerFactory.getLogger(PinCruxController.class);
    private PinCruxService pinCruxService;
    private PantherProperties pantherProperties;

    public PinCruxController(final PinCruxService pinCruxService, final PantherProperties pantherProperties) {
        this.pinCruxService = pinCruxService;
        this.pantherProperties = pantherProperties;
    }

    private final String cruxkey = "lezhinKey";
    private final Integer pubkey = 910277;

    @RequestMapping(value = "/ads", method = RequestMethod.GET)
    @ResponseBody
    public Result<PinCruxData> getAds(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam(required = true) Long usrkey,
                                      @RequestParam(required = false, defaultValue = "true" ) Boolean list) {
        logger.info("PinCruxController.getAds.params = {}", request.getQueryString());

        Result<PinCruxData> result = new Result<>();
        if (!request.getHeader("X-LZ-Locale").toLowerCase().equals("ko-kr")){
            PinCruxData data = new PinCruxData();
            data.setItemCount(0);
            result.setCode(Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_OK.getCode()));
            result.setData(data);

            return result;
        }else{
            if (pantherProperties.isPincruxAvailable()) {
                PinCruxData data = this.pinCruxService.getAds(pubkey, usrkey, this.getOsFlag(request));
                if (data.getStatus().equals("S")) {
                    data.setItemCount(data.getItems().size());
                    if (!list) {
                        data.setItems(null);
                    }
                    result.setCode(Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_OK.getCode()));
                    result.setData(data);
                } else {
                    result.setCode(-1 * Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_PARAM.getCode()));
                    result.setDescription(data.getMsg());
                }
            } else {
                logger.info("pincrux is not available");
                PinCruxData data = new PinCruxData();
                data.setDa_flag("N");
                data.setItem_list(new ArrayList<>());
                data.setItem_cnt(0);
                data.setTotal_coin(0);
                data.initCamel();
                result.setCode(Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_OK.getCode()));
                result.setData(data);
            }
            logger.info("return result.code = {}", result.getCode());
            if (result.getData() != null) {
                logger.info("totalCoin = {}, itemCount = {}",
                        result.getData().getTotalCoin(), result.getData().getItemCount());
            }
            return result;
        }


    }

    @RequestMapping(value = "/ads", method = RequestMethod.POST)
    @ResponseBody
    public Result getAttAd(HttpServletRequest request, HttpServletResponse response,
                           @RequestBody Map<String, String> map) {
        logger.info("PinCruxController.getAdInfo.params = {}, Platform = {}", map, request.getHeader("X-LZ-Platform" ));
        String checkParam;
        if (!StringUtil.isNullOrEmpty(checkParam = CheckMap(map, "appkey" ))) {
            return new Result(-1*Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_PARAM.getCode()), checkParam);
        } else if (!StringUtil.isNullOrEmpty(checkParam = CheckMap(map, "usrkey" ))) {
            return new Result(-1*Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_PARAM.getCode()), checkParam);
        }
        if (this.getOsFlag(request) == 0) {
            return new Result(-1*Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_PARAM.getCode()), "Need OS header info" );
        }

        PinCruxDataItemEnable data = this.pinCruxService.checkEnable((new PinCruxRequest(
                this.pubkey,
                Integer.parseInt(map.get("appkey" )),
                Long.parseLong(map.get("usrkey" )),
                this.cruxkey,//아직 분명한 용도가 없다.
                map.get("subpid" ),
                map.get("dev_id" ),
                map.get("adv_id" ),
                map.get("acc_id" ),
                map.get("and_id" ),
                map.get("device_brand" ),
                map.get("device_model" ),
                null,
                request.getHeader("X-Forwarded-For"),
                request.getHeader("Authorization" ),
                null,
                null,
                this.getOsFlag(request)
        )
        ));

        Result<PinCruxDataItemEnable> result = new Result<>();
        if (Integer.parseInt(data.getCode()) == 0) {
            result.setCode(Integer.parseInt(data.getCode()));
            result.setData(data);
        } else {
            result.setCode(-1*Integer.parseInt(data.getCode()));
            result.setDescription(data.getMsg());
        }


        return result;
    }

    @RequestMapping(value = "/ads", method = RequestMethod.PUT)
    @ResponseBody
    public Result setInstall(HttpServletRequest request, HttpServletResponse response,
                             @RequestBody Map<String, String> map) {
        logger.info("PinCruxController.setInstall.params = {}", map);
        String checkParam;
        if (!StringUtil.isNullOrEmpty(checkParam = CheckMap(map, "appkey" ))) {
            return new Result(Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_PARAM.getCode()), checkParam);
        } else if (!StringUtil.isNullOrEmpty(checkParam = CheckMap(map, "usrkey" ))) {
            return new Result(Integer.parseInt(ResponseInfo.ResponseCode.PINCRUX_PARAM.getCode()), checkParam);
        }

        PinCruxDataInstallResult data = this.pinCruxService.setInstall((new PinCruxRequest(
                this.pubkey,
                Integer.parseInt(map.get("appkey" )),
                Long.parseLong(map.get("usrkey" )),
                this.cruxkey, //아직 분명한 용도가 없다.
                null,
                map.get("dev_id" ),
                map.get("adv_id" ),
                map.get("acc_id" ),
                map.get("and_id" ),
                map.get("device_brand" ),
                map.get("device_model" ),
                null,
                map.get("client_ip" ),
                request.getHeader("Authorization" ),
                null,
                null,
                this.getOsFlag(request)
        )
        ));

        Result<PinCruxDataInstallResult> result = new Result<>();
        if (data.getCode().equals("00" )) {
            result.setCode(Integer.parseInt(data.getCode()));
            result.setData(data);
        } else {
            result.setCode(-1*Integer.parseInt(data.getCode()));
            result.setDescription(data.getMsg());
        }


        return result;
    }

    @PostMapping(value = "/postback")
    @ResponseBody
    public <T> ResponseEntity<T> setPostBack(HttpServletRequest request, HttpServletResponse response,
                                             @RequestParam Map<String, String> map) {
        logger.info("PinCruxController.setPostBack.params = {}", map);
        String checkParam;
        if (!StringUtil.isNullOrEmpty(checkParam = CheckMap(map, "appkey"))) {
            return new ResponseEntity(checkParam, HttpStatus.BAD_REQUEST);
        } else if (!StringUtil.isNullOrEmpty(checkParam = CheckMap(map, "usrkey"))) {
            return new ResponseEntity(checkParam, HttpStatus.BAD_REQUEST);
        }
        String appKey = map.get("appkey");
        String userKey = map.get("usrkey");
        String osFlag = map.get("os_flag");
        String transId = map.get("transid");
        String appTitle = map.get("app_title");
        String coin = map.get("coin");
        String dt = DateUtil.format(Instant.now().toEpochMilli(), DateUtil.ASIA_SEOUL_ZONE, "yyyy-MM-dd HH:mm:ss");
        try {
            this.pinCruxService.setPostBack((new PinCruxRequest(
                    this.pubkey,
                    Integer.parseInt(map.get("appkey")),
                    Long.parseLong(map.get("usrkey")),
                    this.cruxkey, //아직 분명한 용도가 없다.
                    null,
                    map.get("dev_id"),
                    map.get("adv_id"),
                    map.get("acc_id"),
                    map.get("and_id"),
                    map.get("device_brand"),
                    map.get("device_model"),
                    null,
                    map.get("client_ip"),
                    null,
                    map.get("app_title"),
                    map.get("transid"),
                    Integer.parseInt(map.get("os_flag"))
            )
            ));
        } catch (PantherException e) {
            String data = String.format("%s,%s,%s,%s,%s,%s,%s", appKey, userKey, osFlag, transId, appTitle, coin, dt);
            logger.warn("!!! PinCruxController.NoCache. Need Manual REFUND: " + data , e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            String data = String.format("%s,%s,%s,%s,%s", appKey, userKey, osFlag, transId, appTitle);
            logger.warn("!!! PinCruxController.setPostBack.error. Need Manual REFUND: " + data , e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity("OK", HttpStatus.OK);
    }

    private String CheckMap(Map<String, String> map, String param) {
        if (StringUtil.isNullOrEmpty(map.get(param))) {
            return String.format("miss param :{%s}", param);
        }

        return null;
    }

    private Integer getOsFlag(HttpServletRequest request) {
        String platform = request.getHeader("X-LZ-Platform" );
        if (StringUtil.isNullOrEmpty(platform)) {
            return 0;//전체
        } else if (platform.toLowerCase().equals("ka" )) {
            return 2;//앱스토어
        } else {
            return 1;//구글 K?
        }
    }


}


