package com.lezhin.panther.controller;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.lezhin.constant.PGCompany;
import com.lezhin.constant.PaymentType;
import com.lezhin.panther.Context;
import com.lezhin.panther.PayService;
import com.lezhin.panther.SimpleCacheService;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.SessionException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;
import com.lezhin.panther.pg.happypoint.HappyPointPayment;
import com.lezhin.panther.pg.lguplus.LguplusPayment;
import com.lezhin.panther.step.Command;
import com.lezhin.panther.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * API controller. Json을 return.
 *
 * @author seoeun
 * @since 2017.10.24
 */
@RestController
@RequestMapping("/api/v1")
public class APIController {

    private static final Logger logger = LoggerFactory.getLogger(APIController.class);
    private PayService payService;
    private SimpleCacheService simpleCacheService;


    @Autowired
    public APIController(final PayService commandService, final SimpleCacheService simpleCacheService) {
        this.payService = commandService;
        this.simpleCacheService = simpleCacheService;
    }

    @RequestMapping(value = "/{pg}/preparation", method = RequestMethod.POST)
    @ResponseBody
    public Payment prepare(HttpServletRequest request, HttpServletResponse response, @PathVariable String pg) {
        logger.info("  >>> api.prepare. pg = {}", pg);
        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();
        Context context = Context.builder(requestInfo)
                .payment(requestInfo.getPayment())
                .responseInfo(new ResponseInfo(ResponseCode.LEZHIN_UNKNOWN))
                .build();

        Payment payment = payService.doCommand(Command.Type.PREPARE, context);
        return payment;
    }

    @RequestMapping(value = "/{pg}/reservation", method = RequestMethod.POST)
    @ResponseBody
    public Payment reservation(HttpServletRequest request, HttpServletResponse response, @PathVariable String pg) {
        logger.info("  >>> api.reservation, pg = {}", pg);

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();
        Payment<LguplusPayment> requestPayment = requestInfo.getPayment();
        Context context = Context.builder(requestInfo)
                .payment(requestPayment)
                .responseInfo(new ResponseInfo(ResponseCode.LEZHIN_UNKNOWN))
                .build();

        Payment payment = payService.doCommand(Command.Type.RESERVE, context);
        return payment;
    }

    @RequestMapping(value = "/{pg}/preauth", method = RequestMethod.POST)
    @ResponseBody
    public Payment preauthentication(HttpServletRequest request, HttpServletResponse response,
                                     @PathVariable String pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();
        Context context = Context.builder(requestInfo)
                .payment(requestInfo.getPayment())
                .responseInfo(new ResponseInfo(ResponseCode.LEZHIN_UNKNOWN))
                .build();

        Payment payment = payService.doCommand(Command.Type.PREAUTHENTICATE, context);
        return payment;
    }

    @RequestMapping(value = "/{pg}/authentication", method = RequestMethod.POST)
    @ResponseBody
    public Payment authentication(HttpServletRequest request, HttpServletResponse response,
                                  @PathVariable String pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();
        Context context = Context.builder(requestInfo)
                .payment(requestInfo.getPayment())
                .responseInfo(new ResponseInfo(ResponseCode.LEZHIN_UNKNOWN))
                .build();

        Payment payment = payService.doCommand(Command.Type.AUTHENTICATE, context);
        return payment;
    }

    @RequestMapping(value = "/{pg}/payment", method = RequestMethod.POST)
    @ResponseBody
    public Payment payment(HttpServletRequest request, HttpServletResponse response, @PathVariable String pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();
        Context context = Context.builder(requestInfo)
                .payment(requestInfo.getPayment())
                .responseInfo(new ResponseInfo(ResponseCode.LEZHIN_UNKNOWN))
                .build();

        Payment payment = payService.doCommand(Command.Type.PAY, context);
        return payment;
    }

    @RequestMapping(value = "/{pg}/{paymentType}/payment/done", method = RequestMethod.POST)
    @ResponseBody
    public <T> ResponseEntity<T> paymentDone(HttpServletRequest request, HttpServletResponse response,
                                             @PathVariable String pg,
                                             @PathVariable String paymentType) {

        logger.info("  >>> api.paymentdone, paymentType = {}", paymentType);

        Payment payment = null;
        Map<String, Object> transformedParams = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()[0]));

        transformedParams.entrySet().stream().forEach(
                e -> logger.debug("request param. {} = {}", e.getKey(), e.getValue()));
        if (PGCompany.lguplus.name().equals(pg) &&
                (PaymentType.deposit.name().equals(paymentType) || PaymentType.mdeposit.name().equals(paymentType))) {
            // LGU는 post인데도 param
            String LGD_CASFLAG = Optional.ofNullable(transformedParams.get("LGD_CASFLAG")).orElse("").toString();
            Long paymentId = Optional.ofNullable(transformedParams.get("LGD_OID")).map(o -> Long.valueOf(o.toString()))
                    .orElse(Long.valueOf(-1L));
            if (!LGD_CASFLAG.equals("I")) {
                logger.info(" LGD_CASFLAG = {}, LGD_RESCODE = {}, LGD_RESMSG = {}",
                        transformedParams.get("LGD_CASFLAG"), transformedParams.get("LGD_RESPCODE"),
                        transformedParams.get("LGD_RESPMSG"));
                return new ResponseEntity("OK", HttpStatus.OK);
            }

            Context context = null;
            RequestInfo requestInfo = null;
            try {
                requestInfo = simpleCacheService.getRequestInfo(paymentId);

                LguplusPayment pgPayment = JsonUtil.fromMap(transformedParams, LguplusPayment.class);
                Payment requestPayment = Executor.Type.LGUDEPOSIT.createPayment(pgPayment);
                requestInfo = new RequestInfo.Builder(requestInfo).withPayment(requestPayment).build();

                logger.debug("api.paymentdone [{}]. requestPayment = {}", pg, JsonUtil.toJson(requestPayment));

                context = Context.builder(requestInfo)
                        .payment(requestPayment)
                        .responseInfo(new ResponseInfo(pgPayment.getLGD_RESPCODE(), pgPayment.getLGD_RESPMSG()))
                        .build();
                logger.info("{}, requestInfo = {}", context.print(), requestInfo);

            } catch (SessionException e) {
                // TODO 만약에 redis가 죽었다가 살아나서 requestInfo가 모두 reset 될 수도 있다면.
                // 그런데 입금했다면, purchase는 안되어서 결국은 CR로.
                throw new SessionException(Executor.Type.LGUDEPOSIT,
                        "Failed to get session: " + paymentId.longValue(), e);
            } catch (Throwable e) {
                throw new PantherException(Executor.Type.LGUDEPOSIT,
                        "Failed to convert to pgPayment: " + paymentId.longValue(), e);
            }

            // 실패시 exceptionHandler에 의해 처리됨
            payment = payService.doCommand(Command.Type.PAY, context);
            logger.info("{} api.paymentdone. OK. \n{}", context.print(), JsonUtil.toJson(payment));
            return new ResponseEntity("OK", HttpStatus.OK);

        } else {
            logger.warn("Unsupported executor. pg = " + pg + ", paymentType = " + paymentType);
        }

        // reached here. error.
        return new ResponseEntity("Error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(value = "/{pg}/{paymentType}/{paymentId}/cancel", method = RequestMethod.PUT)
    @ResponseBody
    public <T> ResponseEntity<T> paymentCancel(HttpServletRequest request, HttpServletResponse response,
                                               @PathVariable String pg,
                                               @PathVariable String paymentType,
                                               @PathVariable Long paymentId) {
        logger.info("  >>> api.cancel, paymentType = {}, paymentId = {}", paymentType, paymentId);
        if (request.getHeader("__x") == null || !request.getHeader("__x").toString().equals("nakia")) {
            // TODO 임시로 fraud detecting.
            logger.info("We need __x nakia");
            return new ResponseEntity("Fraud", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (paymentId == null) {
            return new ResponseEntity(ResponseCode.LEZHIN_PARAM.getCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Payment payment = null;
        if (PGCompany.lguplus.name().equals(pg) &&
                (PaymentType.deposit.name().equals(paymentType) || PaymentType.mdeposit.name().equals(paymentType))) {

            Context context = null;
            RequestInfo requestInfo = null;
            try {
                requestInfo = simpleCacheService.getRequestInfo(paymentId);

                Payment<LguplusPayment> requestPayment = requestInfo.getPayment();

                context = Context.builder(requestInfo)
                        .payment(requestPayment)
                        .responseInfo(new ResponseInfo(ResponseCode.LEZHIN_UNKNOWN))
                        .build();
                logger.info("{} api.cancel requestInfo = {}", context.print(), requestInfo);
            } catch (SessionException e) {
                // TODO 만약에 redis가 죽었다가 살아나서 requestInfo가 모두 reset 될 수도 있다면.
                // 그런데 입금했다면, purchase는 안되어서 결국은 CR로.
                throw new SessionException(Executor.Type.LGUDEPOSIT, "Failed to get session: " + paymentId, e);
            } catch (Throwable e) {
                throw new PantherException(Executor.Type.LGUDEPOSIT, "Failed to convert to pgPayment: " + paymentId, e);
            }

            // 실패시 exceptionHandler에 의해 처리됨
            payment = payService.doCommand(Command.Type.CANCEL, context);
            logger.info("{} api.cancel OK. paymentId={}, userId={}", context.print(),
                    payment.getPaymentId(), payment.getUserId());
            return new ResponseEntity("OK", HttpStatus.OK);

        } else {
            logger.warn("Unsupported executor. pg = " + pg + ", paymentType = " + paymentType);
        }

        // reached here. error.
        return new ResponseEntity("Error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(value = "/{pg}/refund", method = RequestMethod.POST)
    @ResponseBody
    public <T> ResponseEntity<T> paymentRefund(HttpServletRequest request, HttpServletResponse response,
                                               @PathVariable String pg) {

        logger.info("  >>> api.refund, pg = {}", pg);
        if (request.getHeader("__x") == null || !request.getHeader("__x").toString().equals("nakia")) {
            return new ResponseEntity("Fraud", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Map<String, Object> requestMap = new HashMap<>();

        try {
            String result = CharStreams.toString(new InputStreamReader(request.getInputStream(), Charsets.UTF_8));
            requestMap = JsonUtil.fromJson(result, Map.class);
        } catch (Exception e) {
            logger.warn("Failed to parse body", e);
            return new ResponseEntity("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Payment payment = null;
        if (PGCompany.happypoint.name().equals(pg)) {

            RequestInfo requestInfo;
            String result; // TODO context의 ResponseInfo.
            try {
                HappyPointPayment happyPointPayment = new HappyPointPayment();
                happyPointPayment.setMbrNo(requestMap.get("mbrNo").toString());
                happyPointPayment.setAprvNo(requestMap.get("aprvNo").toString());
                happyPointPayment.setAprvDt(requestMap.get("aprvDt").toString());
                happyPointPayment.setTrxAmt(Long.valueOf(requestMap.get("trxAmt").toString()));
                Payment<HappyPointPayment> requestPayment = Executor.Type.HAPPYPOINT.createPayment(happyPointPayment);
                requestPayment.setPaymentId(Optional.ofNullable(requestMap.get("paymentId"))
                        .map(o -> Long.valueOf(o.toString())).orElse(-1L));
                requestPayment.setPaymentId(Optional.ofNullable(requestMap.get("userId"))
                        .map(o -> Long.valueOf(o.toString())).orElse(-1L));
                requestPayment.setPaymentType(PaymentType.happypoint); // refund용은 정확한 값대신 null을 피하기 위해 임시 세팅.

                requestInfo = new RequestInfo.Builder(requestPayment, "happypoint").build();
                Context context = Context.builder(requestInfo)
                        .payment(requestInfo.getPayment())
                        .responseInfo(new ResponseInfo(ResponseCode.LEZHIN_UNKNOWN))
                        .build();

                logger.info("{} api.refund. requestInfo = {}", context.print(), requestInfo);

                payment = payService.doCommand(Command.Type.REFUND, context);
                HappyPointPayment resultHappyPoint = (HappyPointPayment) payment.getPgPayment();
                result = String.format("REFUND OK. paymentId=%s, userId=%s, mbrNo=%s, orgAprvNo=%s, orgAprvDt=%s",
                        payment.getPaymentId(), payment.getUserId(), happyPointPayment.getMbrNo(),
                        happyPointPayment.getAprvNo(), happyPointPayment.getAprvDt());
                logger.info(result);

            } catch (Throwable e) {
                throw new PantherException(Executor.Type.HAPPYPOINT, "Failed to refund:" + e.getMessage(), e);
            }

            return new ResponseEntity(result, HttpStatus.OK);

        } else {
            logger.warn("Unsupported executor. pg = " + pg);
        }

        // reached here. error.
        return new ResponseEntity("Error", HttpStatus.INTERNAL_SERVER_ERROR);

    }


}
