package com.lezhin.panther.controller;

import com.lezhin.panther.payment2.Payment2Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Datastore의 Payment2 에 대한 rest API. Datastore를 직접 access 하는 API 이므로 사용에 주의할 것 !!
 *
 * @author seoeun
 * @since 2018.02.21
 */
@RestController
@RequestMapping("/payment2/v1")
public class Payment2Controller {

    private static final Logger logger = LoggerFactory.getLogger(Payment2Controller.class);

    private Payment2Service payment2Service;

    public Payment2Controller(final Payment2Service payment2Service) {
        this.payment2Service = payment2Service;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public <T> ResponseEntity<T> getPayment(HttpServletRequest request, HttpServletResponse response,
                                            @RequestParam Long userId,
                                            @RequestParam (required = false) Long startCreatedAt,
                                            @RequestParam (required = false) Long endCreatedAt,
                                            @RequestParam (required = false) Long paymentId) {

        logger.info("request getPayment. userId={}, startCreatedAt={}, endCreatedAt={}, paymentId={}",
                userId, startCreatedAt, endCreatedAt, paymentId);

        if (request.getHeader("__x") == null || !request.getHeader("__x").toString().equals("nakia")) {
            return new ResponseEntity("Fraud", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (paymentId == null) {
            payment2Service.get(userId, startCreatedAt, endCreatedAt);
        } else {
            payment2Service.getPayment(userId, paymentId);
        }

        return new ResponseEntity("OK", HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    @ResponseBody
    public <T> ResponseEntity<T> updatePayment(HttpServletRequest request, HttpServletResponse response,
                                               @RequestParam Long userId,
                                               @RequestParam Long startCreatedAt,
                                               @RequestParam Long endCreatedAt,
                                               @RequestParam String state) {

        logger.info("request updatePayment. userId={}, startCreatedAt={}, endCreatedAt={}, state={}",
                userId, startCreatedAt, endCreatedAt, state);
        if (request.getHeader("__x") == null || !request.getHeader("__x").toString().equals("nakia")) {
            return new ResponseEntity("Fraud", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        payment2Service.updateState(userId, startCreatedAt, endCreatedAt, state);

        return new ResponseEntity("OK", HttpStatus.OK);
    }

}
