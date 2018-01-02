package com.lezhin.panther.controller;

import com.lezhin.panther.PayService;
import com.lezhin.panther.command.Command;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API controller. Json을 return.
 *
 * TODO requestMapping 이 잘못되었음. api/v1 으로 해서 api 관련 controller 에 대해서 versioning 해야 함.
 * @deprecated
 * APIController로 이전 중.
 *
 * @author seoeun
 * @since 2017.10.24
 */
@RestController
@RequestMapping("/v1/api")
public class APIControllerOld {

    private static final Logger logger = LoggerFactory.getLogger(APIControllerOld.class);

    private PayService payService;

    @Autowired
    public APIControllerOld(PayService commandService) {
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

}
