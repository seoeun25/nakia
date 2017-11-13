package com.lezhin.avengers.panther.controller;

import com.lezhin.avengers.panther.CommandService;
import com.lezhin.avengers.panther.command.Command;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API controller. Json을 return.
 *
 * @author seoeun
 * @since 2017.10.24
 */
@Controller
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
    public Payment prepare(HttpServletRequest request, HttpServletResponse response, @PathVariable String pg
    ) {
        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();

        logger.info("HTTP prepare. requestInfo = {}", pg, requestInfo);

        Payment payment = commandService.doCommand(Command.Type.PREPARE, requestInfo);
        //return "reserve";
        return payment;
    }

    @RequestMapping(value = "/{pg}/reservation", method = RequestMethod.POST)
    @ResponseBody
    public Payment reserve(HttpServletRequest request, HttpServletResponse response, @PathVariable String pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();

        logger.info("HTTP reservation. requestInfo = {}", requestInfo);

        Payment payment = commandService.doCommand(Command.Type.RESERVE, requestInfo);
        //return "reserve";
        return payment;
    }

    @RequestMapping(value = "/{pg}/authentication", method = RequestMethod.POST)
    @ResponseBody
    public Payment authentication(HttpServletRequest request, HttpServletResponse response, @PathVariable String
            pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();

        Payment payment = commandService.doCommand(Command.Type.AUTHENTICATE, requestInfo);
        return payment;
    }

    @RequestMapping(value = "/{pg}/payment", method = RequestMethod.POST)
    @ResponseBody
    public Payment payment(HttpServletRequest request, HttpServletResponse response, @PathVariable String
            pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();

        Payment payment = commandService.doCommand(Command.Type.PAY, requestInfo);
        return payment;
    }

}
