package com.lezhin.avengers.panther.controller;

import com.lezhin.avengers.panther.CommandService;
import com.lezhin.avengers.panther.command.Command;
import com.lezhin.avengers.panther.model.Payment;
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
 * Panther controller. Json을 return.
 *
 * @author seoeun
 * @since 2017.11.14
 */
@Controller
@RequestMapping("/panther")
public class PantherController {

    private static final Logger logger = LoggerFactory.getLogger(PantherController.class);

    public PantherController() {

    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo basicInfo(HttpServletRequest request, HttpServletResponse response) {

        logger.info("basicInfo");


        ResponseInfo responseInfo = new ResponseInfo("hello", "panther");
        return responseInfo;

    }

}
