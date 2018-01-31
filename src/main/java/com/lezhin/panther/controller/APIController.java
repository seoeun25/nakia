package com.lezhin.panther.controller;

import com.lezhin.constant.PGCompany;
import com.lezhin.constant.PaymentType;
import com.lezhin.panther.Context;
import com.lezhin.panther.ErrorCode;
import com.lezhin.panther.PayService;
import com.lezhin.panther.SimpleCacheService;
import com.lezhin.panther.command.Command;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.SessionException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.lguplus.LguplusPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;
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
        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();

        logger.info("HTTP prepare. requestInfo = {}", pg, requestInfo);

        Payment payment = payService.doCommand(Command.Type.PREPARE, requestInfo);
        return payment;
    }

    @RequestMapping(value = "/{pg}/reservation", method = RequestMethod.POST)
    @ResponseBody
    public Payment reservation(HttpServletRequest request, HttpServletResponse response, @PathVariable String pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();

        logger.info("HTTP reservation. requestInfo = {}", requestInfo);

        Payment payment = payService.doCommand(Command.Type.RESERVE, requestInfo);
        return payment;
    }

    @RequestMapping(value = "/{pg}/authentication", method = RequestMethod.POST)
    @ResponseBody
    public Payment authentication(HttpServletRequest request, HttpServletResponse response,
                                  @PathVariable String pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();

        Payment payment = payService.doCommand(Command.Type.AUTHENTICATE, requestInfo);
        return payment;
    }

    @RequestMapping(value = "/{pg}/payment", method = RequestMethod.POST)
    @ResponseBody
    public Payment payment(HttpServletRequest request, HttpServletResponse response, @PathVariable String pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();

        Payment payment = payService.doCommand(Command.Type.PAY, requestInfo);
        return payment;
    }

    @RequestMapping(value = "/{pg}/{paymentType}/payment/done", method = RequestMethod.POST)
    @ResponseBody
    public <T> ResponseEntity<T> paymentDone(HttpServletRequest request, HttpServletResponse response,
                                             @PathVariable String pg,
                                             @PathVariable String paymentType) {

        logger.info("PAYMENT_DONE [{}-{}]", pg, paymentType);
        Payment payment = null;
        Map<String, Object> transformedParams = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()[0]));

        transformedParams.entrySet().stream().forEach(
                e -> logger.debug("request param. {} = {}", e.getKey(), e.getValue()));
        if (PGCompany.lguplus.name().equals(pg) &&
                (PaymentType.deposit.name().equals(paymentType) || PaymentType.mdeposit.name().equals(paymentType))) {
            // LGU는 post인데도 param
            String LGD_CASFLAG = Optional.ofNullable(transformedParams.get("LGD_CASFLAG")).orElse("").toString();
            if (!LGD_CASFLAG.equals("I")) {
                logger.info(" LGD_CASFLAG = {}, LGD_RESCODE = {}, LGD_RESMSG = {}",
                        transformedParams.get("LGD_CASFLAG"), transformedParams.get("LGD_RESPCODE"),
                        transformedParams.get("LGD_RESPMSG"));
                return new ResponseEntity("OK", HttpStatus.OK);
            }

            Context context = null;
            RequestInfo requestInfo = null;
            try {
                requestInfo = simpleCacheService.getRequestInfo(Long.valueOf(transformedParams.get("LGD_OID").toString()));

                LguplusPayment pgPayment = JsonUtil.fromMap(transformedParams, LguplusPayment.class);
                Payment requestPayment = Executor.Type.LGUDEPOSIT.createPayment(pgPayment);
                requestInfo = new RequestInfo.Builder(requestInfo).withPayment(requestPayment).build();

                logger.debug("API [{}]. requestPayment = {}", pg, JsonUtil.toJson(requestPayment));

                context = Context.builder()
                        .requestInfo(requestInfo)
                        .payment(requestPayment)
                        .responseInfo(ResponseInfo.builder().code(pgPayment
                                .getLGD_RESPCODE()).description(pgPayment.getLGD_RESPMSG()).build()).build();
            } catch (SessionException e) {
                // TODO 만약에 redis가 죽었다가 살아나서 requestInfo가 모두 reset 될 수도 있다면.
                // 그런데 입금했다면, purchase는 안되어서 결국은 CR로.
                logger.warn("Failed to get RequestInfo. paymentId = {}",
                        Long.valueOf(transformedParams.get("LGD_OID").toString()));
                throw new SessionException(Executor.Type.LGUDEPOSIT, e);
            } catch (Throwable e) {
                throw new PantherException(Executor.Type.LGUDEPOSIT, "Failed to convert to pgPayment", e);
            }

            // 실패시 exceptionHandler에 의해 처리됨
            payment = payService.doCommand(Command.Type.PAY, requestInfo);
            logger.info("PAYMENT_DONE. OK. \n{}", JsonUtil.toJson(payment));
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

        logger.info("CANCEL. [{}-{}], paymentId = {}", pg, paymentType, paymentId);
        if (request.getHeader("__x") == null || !request.getHeader("__x").toString().equals("nakia")) {
            // TODO 임시로 fraud detecting.
            logger.info("We need __x nakia");
            return new ResponseEntity("Fraud", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (paymentId == null) {
            return new ResponseEntity(ErrorCode.LEZHIN_PARAM.getCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Payment payment = null;
        if (PGCompany.lguplus.name().equals(pg) &&
                (PaymentType.deposit.name().equals(paymentType) || PaymentType.mdeposit.name().equals(paymentType))) {

            Context context = null;
            RequestInfo requestInfo = null;
            try {
                requestInfo = simpleCacheService.getRequestInfo(paymentId);

                Payment<LguplusPayment> requestPayment = requestInfo.getPayment();

                context = Context.builder()
                        .requestInfo(requestInfo)
                        .payment(requestPayment)
                        .responseInfo(ResponseInfo.builder().code(ErrorCode.LEZHIN_UNKNOWN.getCode()
                                ).description(ErrorCode.LEZHIN_UNKNOWN.getMessage()).build()).build();
            } catch (SessionException e) {
                // TODO 만약에 redis가 죽었다가 살아나서 requestInfo가 모두 reset 될 수도 있다면.
                // 그런데 입금했다면, purchase는 안되어서 결국은 CR로.
                logger.warn("Failed to get RequestInfo. paymentId = {}", paymentId);
                throw new SessionException(Executor.Type.LGUDEPOSIT, e);
            } catch (Throwable e) {
                throw new PantherException(Executor.Type.LGUDEPOSIT, "Failed to convert to pgPayment", e);
            }

            // 실패시 exceptionHandler에 의해 처리됨
            payment = payService.doCommand(Command.Type.CANCEL, requestInfo);
            logger.info("CANCEL OK. paymentId={}, userId={}", payment.getPaymentId(), payment.getUserId());
            return new ResponseEntity("OK", HttpStatus.OK);

        } else {
            logger.warn("Unsupported executor. pg = " + pg + ", paymentType = " + paymentType);
        }

        // reached here. error.
        return new ResponseEntity("Error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
