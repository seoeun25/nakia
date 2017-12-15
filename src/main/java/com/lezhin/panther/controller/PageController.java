package com.lezhin.panther.controller;

import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Optional;

/**
 * Page Controller. /webapp/WEB-INF/jsp/에 있는 jsp 를 리턴.
 * @author seoeun
 * @since 2017.12.06
 */
@Controller
@RequestMapping("/page/v1")
public class PageController {

    private static final Logger logger = LoggerFactory.getLogger(PageController.class);

    private PantherProperties pantherProperties;

    public PageController(final PantherProperties pantherProperties) {
        this.pantherProperties = pantherProperties;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String hello(HttpServletRequest request, HttpServletResponse response, Model model) {

        String version = Optional.ofNullable(Util.loadVersion()).orElse("NULL");
        model.addAttribute("name", "there, This is panther");
        model.addAttribute("version", version);

        return "hello";
    }

    @RequestMapping(value = "/{pg}/sample", method = RequestMethod.GET)
    public String sample(HttpServletRequest request, HttpServletResponse response, Model model, @PathVariable String pg) {

        model.addAttribute("name", "there, This is panther");

        String pageKey = String.format("pg/%s/sample", pg);

        logger.info("sample. pg = {}, pageKey = {}", pg, pageKey);


        return pageKey;
    }

    @RequestMapping(value = "/{pg}/reservation", method = RequestMethod.POST)
    public String reservation(HttpServletRequest request, HttpServletResponse response, Model model,
                                      @PathVariable String pg) {

        model.addAttribute("pantherUrl", pantherProperties.getPantherUrl());

        String pageKey = String.format("pg/%s/reservation", pg);

        logger.info("reservation. pg = {}, pageKey = {}", pg, pageKey);

        return pageKey;

    }

    @RequestMapping(value = "/{pg}/authentication/done", method = RequestMethod.POST)
    public String authenticationDone(HttpServletRequest request, HttpServletResponse response, Model model,
                            @PathVariable String pg) {

        String pageKey = String.format("pg/%s/authentication_done", pg);

        logger.info("authentication_done returnurl. pg = {}, pageKey = {}", pg, pageKey);

        return pageKey;

    }


    @RequestMapping(value = "/{pg}/payment", method = RequestMethod.POST)
    public String payment(HttpServletRequest request, HttpServletResponse response, Model model,
                         @PathVariable String pg) {

        String pageKey = String.format("pg/%s/payment", pg);

        logger.info("payment. payres. pg = {}, pageKey = {}", pg, pageKey);

        String confDirStr = pantherProperties.getLguplus2().getConfDir();
        model.addAttribute("confDir", confDirStr); // for lguplus2
        File file = new File(confDirStr);
        logger.info("confDirStr = {}, file = {}, mallConf.exists = {}", confDirStr, file.getAbsoluteFile(),
                new File(file, "/conf/mall.conf").exists());

        return pageKey;

    }

    @RequestMapping(value = "/{pg}/payment/done", method = RequestMethod.POST)
    public String paymentDone(HttpServletRequest request, HttpServletResponse response, Model model,
                             @PathVariable String pg) {

        String pageKey = String.format("pg/%s/payment_done", pg);

        logger.info("payment_done. cas_noteurl. pg = {}, pageKey = {}", pg, pageKey);

        return pageKey;

    }


    @RequestMapping(value = "/{pg}/ping", method = RequestMethod.GET)
    public String ping(HttpServletRequest request, HttpServletResponse response, Model model,
                            @PathVariable String pg) {

        String pageKey = String.format("pg/%s/ping", pg);

        logger.info("ping. pg = {}, pageKey = {}", pg, pageKey);

        String confDirStr = pantherProperties.getLguplus2().getConfDir();
        model.addAttribute("confDir", confDirStr);
        model.addAttribute("CST_PLATFORM", "test");
        model.addAttribute("CST_MID", "lezhin001");
        File file = new File(confDirStr);
        File mallConf = new File(file, "/conf/mall.conf");
        logger.info("confDirStr = {}, file = {}, mallConf.exists = {}", confDirStr, file.getAbsoluteFile(),
                mallConf.exists());

        return pageKey;

    }
}
