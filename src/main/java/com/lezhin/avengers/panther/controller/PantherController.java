package com.lezhin.avengers.panther.controller;

import com.lezhin.avengers.panther.CommandService;
import com.lezhin.avengers.panther.command.Command;
import com.lezhin.avengers.panther.model.RequestInfo;
import com.lezhin.avengers.panther.model.ResponseInfo;

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
 * @author seoeun
 * @since 2017.10.24
 */
@Controller
@RequestMapping("/panther")
public class PantherController {

    private static final Logger logger = LoggerFactory.getLogger(PantherController.class);

    private CommandService commandService;

    @Autowired
    public PantherController(CommandService commandService) {
        this.commandService = commandService;
    }

    @RequestMapping(value = "/{pg}/reservation", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo reserve(HttpServletRequest request, HttpServletResponse response, @PathVariable String pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request).build();

        logger.info("---- pg = {}, requestInfo = {}", pg, requestInfo);

        commandService.doCommand(Command.Type.RESERVE, requestInfo);
        //return "reserve";
        return new ResponseInfo<String>("hello reserve");
    }

    @RequestMapping(value = "/{pg}/authentication", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo authentication(HttpServletRequest request, HttpServletResponse response, @PathVariable String
            pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request).build();

        commandService.doCommand(Command.Type.AUTHENTICATE, requestInfo);
        return new ResponseInfo<String>("hello authentication");
    }

    @RequestMapping(value = "/{pg}/payment", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo payment(HttpServletRequest request, HttpServletResponse response, @PathVariable String
            pg) {

        RequestInfo requestInfo = new RequestInfo.Builder(request).build();

        commandService.doCommand(Command.Type.PAY, requestInfo);
        return new ResponseInfo<String>("hello payment");
    }

}
