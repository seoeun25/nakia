package com.lezhin.panther.controller;

import com.lezhin.panther.CommandService;
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
 *
 * @author seoeun
 * @since 2017.10.24
 */
@RestController
@RequestMapping("/v1/api")
public class APIController {

    private static final Logger logger = LoggerFactory.getLogger(APIController.class);

    private CommandService commandService;

    @Autowired
    public APIController(CommandService commandService) {
        this.commandService = commandService;
    }

    @RequestMapping(value = "/{pg}/preparation", method = RequestMethod.POST)
    @ResponseBody
    public Payment prepare(HttpServletRequest request, HttpServletResponse response, @PathVariable String pg) {
        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();

        logger.info("HTTP prepare. requestInfo = {}", pg, requestInfo);

        Payment payment = commandService.doCommand(Command.Type.PREPARE, requestInfo);
        return payment;
    }

    @RequestMapping(value = "/{pg}/reservation", method = RequestMethod.POST)
    @ResponseBody
    public Payment reservation(HttpServletRequest request, HttpServletResponse response, @PathVariable String pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();

        logger.info("HTTP reservation. requestInfo = {}", requestInfo);

        Payment payment = commandService.doCommand(Command.Type.RESERVE, requestInfo);
        return payment;
    }

    @RequestMapping(value = "/{pg}/authentication", method = RequestMethod.POST)
    @ResponseBody
    public Payment authentication(HttpServletRequest request, HttpServletResponse response,
                                  @PathVariable String pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();

        Payment payment = commandService.doCommand(Command.Type.AUTHENTICATE, requestInfo);
        return payment;
    }

    @RequestMapping(value = "/{pg}/payment", method = RequestMethod.POST)
    @ResponseBody
    public Payment payment(HttpServletRequest request, HttpServletResponse response, @PathVariable String pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();

        Payment payment = commandService.doCommand(Command.Type.PAY, requestInfo);
        return payment;
    }

}
