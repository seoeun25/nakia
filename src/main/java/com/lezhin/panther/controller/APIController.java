package com.lezhin.panther.controller;

import com.lezhin.constant.PGCompany;
import com.lezhin.constant.PaymentType;
import com.lezhin.panther.Context;
import com.lezhin.panther.PayService;
import com.lezhin.panther.command.Command;
import com.lezhin.panther.exception.PantherException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * API controller. Json을 return.
 *
 *
 * @author seoeun
 * @since 2017.10.24
 */
@RestController
@RequestMapping("/api/v1")
public class APIController {

    private static final Logger logger = LoggerFactory.getLogger(APIController.class);

    private PayService payService;

    @Autowired
    public APIController(PayService commandService) {
        this.payService = commandService;
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
    public ResponseEntity<String> paymentDone(HttpServletRequest request, HttpServletResponse response,
                                          @PathVariable String pg,
                                          @PathVariable String paymentType) {

        logger.info("API payment. [{}-{}]", pg, paymentType);
        Payment payment = null;
        Map<String, String[]> params = new HashMap(request.getParameterMap());
        Map<String, Object> transformedParams = params.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()[0]));

        params.entrySet().stream().forEach(e -> logger.info("request param. {} = {}", e.getKey(), e.getValue()[0]));
        if (PGCompany.lguplus.name().equals(pg) &&
                (PaymentType.deposit.name().equals(paymentType) || PaymentType.mdeposit.name().equals(paymentType))) {

            String LGD_CASFLAG = Optional.ofNullable(transformedParams.get("LGD_CASFLAG")).orElse("").toString();
            if (!LGD_CASFLAG.equals("I")) {
                logger.info("API LGD_CASFLAG = {}, LGD_RESCODE = {}, LGD_RESMSG = {}",
                        transformedParams.get("LGD_CASFLAG"), transformedParams.get("LGD_RESPCODE"),
                        transformedParams.get("LGD_RESPMSG") );
                return new ResponseEntity("OK", HttpStatus.OK);
            }

            // TODO builder default=true.
            Context context = null;
            // TODO builder default=true
            RequestInfo requestInfo = null;
            try {
                LguplusPayment pgPayment = JsonUtil.fromMap(transformedParams, LguplusPayment.class);
                Payment requestPayment = Executor.Type.LGUDEPOSIT.createPayment(pgPayment);
                requestPayment.setPaymentType(PaymentType.valueOf(paymentType));
                requestInfo = new RequestInfo.Builder(requestPayment, pg).build();

                logger.info("API [{}]. requestPayment = {}", pg, JsonUtil.toJson(requestPayment));

                context = Context.builder()
                        .requestInfo(requestInfo)
                        .payment(requestPayment)
                        .responseInfo(ResponseInfo.builder().code(pgPayment
                                .getLGD_RESPCODE()).description(pgPayment.getLGD_RESPMSG()).build())
                        .build();
            } catch (Throwable e) {
                throw new PantherException(Executor.Type.LGUDEPOSIT, "Failed to convert to pgPayment", e);
            }
            try {

                payment = payService.doCommand(Command.Type.PAY, requestInfo);

            } catch (Throwable e) {
                // FIXME handle exception. Redirect to GCS
                logger.warn("!!!! Failed to Payment ==> GCS /payment/result/fail", e);
            }
            // 성공이든 실패든 처리가 잘 되면 OK
            return new ResponseEntity("OK", HttpStatus.OK);

        } else {
            logger.warn("Unsupported executor. pg = " + pg + ", paymentType = " + paymentType);
            // TODO error noti
        }

        // 성공이든 실패든 처리가 잘 되면 OK
        return new ResponseEntity("Error", HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
