package com.lezhin.avengers.panther.controller;

import com.lezhin.avengers.panther.model.ResponseInfo;
import com.lezhin.avengers.panther.util.DateUtil;
import com.lezhin.avengers.panther.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public PantherController() {

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

}
