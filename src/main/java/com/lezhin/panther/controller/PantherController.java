package com.lezhin.panther.controller;

import com.lezhin.constant.PaymentType;
import com.lezhin.panther.SimpleCacheService;
import com.lezhin.panther.ErrorCode;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.lguplus.LguplusPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.util.DateUtil;
import com.lezhin.panther.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

/**
 * Panther controller. Json을 return.
 *
 * @author seoeun
 * @since 2017.11.14
 */
@Controller
@RequestMapping("/panther")
public class PantherController {

    private static final Logger logger = LoggerFactory.getLogger(PantherController.class);

    private final SimpleCacheService simpleCacheService;

    public PantherController(SimpleCacheService simpleCacheService) {
        this.simpleCacheService = simpleCacheService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo basicInfo(HttpServletRequest request, HttpServletResponse response) {

        logger.info("basicInfo");


        ResponseInfo responseInfo = new ResponseInfo("hello", "panther");
        return responseInfo;

    }

    @RequestMapping(value = "/version", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo date(HttpServletRequest request, HttpServletResponse response) {

        logger.info("version");

        DateUtil.printDate();

        // TODO version 정보를 version.txt 에서 읽지 말고, artifact의 verion 정보를 읽도록.
        String version = Optional.ofNullable(Util.loadVersion()).orElse("NULL");

        ResponseInfo responseInfo = new ResponseInfo("version", version);
        return responseInfo;

    }

    /**
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/cache", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo check(HttpServletRequest request, HttpServletResponse response,
                              @RequestBody Map<String, Object> map) {

        Object key = map.get("key");
        Boolean delete = Boolean.valueOf(Optional.ofNullable(map.get("delete")).orElse("false").toString());
        if (key != null) {
            try {
                Object value = simpleCacheService.get(key.toString());
                logger.info("key = {}, value = {}", key, value);

                if (delete) {
                    logger.info("deleted = {}", simpleCacheService.delete(key.toString()));
                }
            } catch (Exception e) {
                logger.warn("Failed to get cache value", e);
            }
        }

        return new ResponseInfo(ErrorCode.LEZHIN_OK.getCode(), ErrorCode.LEZHIN_OK.getMessage());
    }

    @RequestMapping(value = "/info", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo saveRequestInfo(HttpServletRequest request, HttpServletResponse response,
                                        @RequestBody Map<String, Object> map) {

        LguplusPayment lguplusPayment = LguplusPayment.builder()
                .LGD_BUYER(map.get("LGD_BUYER").toString())
                .LGD_OID(map.get("LGD_OID").toString())
                .LGD_AMOUNT(map.get("LGD_AMOUNT").toString())
                .LGD_PRODUCTINFO(map.get("LGD_PRODUCTINFO").toString()).build();
        Payment payment = Executor.Type.LGUDEPOSIT.createPayment(lguplusPayment);
        payment.setPaymentType(PaymentType.deposit);
        RequestInfo requestInfo = new RequestInfo.Builder(payment, "lguplus")
                .withToken(map.get("token").toString()).build();

        simpleCacheService.saveRequestInfo(requestInfo);



        return new ResponseInfo(ErrorCode.LEZHIN_OK.getCode(), ErrorCode.LEZHIN_OK.getMessage());
    }



}
