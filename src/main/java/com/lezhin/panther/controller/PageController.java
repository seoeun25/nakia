package com.lezhin.panther.controller;

import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.lguplus2.Lguplus2Executor;
import com.lezhin.panther.lguplus2.Lguplus2Payment;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.util.JsonUtil;
import com.lezhin.panther.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private PagePayService pagePayService;
    private SlackNotifier slackNotifier;


    public PageController(final PantherProperties pantherProperties, final PagePayService pagePayService,
                          final SlackNotifier slackNotifier) {
        this.pantherProperties = pantherProperties;
        this.pagePayService = pagePayService;
        this.slackNotifier = slackNotifier;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView hello(HttpServletRequest request, HttpServletResponse response) {

        String version = Optional.ofNullable(Util.loadVersion()).orElse("NULL");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "NEW there, This is panther");
        modelAndView.addObject("version", version);
        modelAndView.setViewName("hello");

        return modelAndView;
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

        Map<String, String[]> params = new HashMap(request.getParameterMap());
        Map<String, Object> newParams = params.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e
                .getValue()[0]));




        params.entrySet().stream().forEach(e -> logger.info("param. {} = {}", e.getKey(), e.getValue()[0]));
        Lguplus2Payment pgPayment = JsonUtil.fromMap(newParams, Lguplus2Payment.class);
        logger.info("lgu. Payement = {}", pgPayment.toString());

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

    @ExceptionHandler(InternalPaymentException.class)
    public ModelAndView handleInternalPaymentExceptionException(HttpServletRequest request,
                                                                InternalPaymentException e) {
        logger.error("panther error. requestedUrl = " + request.getRequestURI(), e);
        slackNotifier.notify(SlackEvent.builder()
                .header(e.getType().name())
                .level(SlackMessage.LEVEL.ERROR)
                .title("Unexpected error")
                .message(e.getMessage())
                .exception(e)
                .build());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("exception", e);
        modelAndView.addObject("url", request.getRequestURL());

        modelAndView.setViewName("error_internal");
        return modelAndView;
    }

    @ExceptionHandler({ExecutorException.class, PantherException.class})
    public ModelAndView handleException(HttpServletRequest request, Exception e) {
        logger.error("panther error. requestedUrl = " + request.getRequestURI(), e);
        slackNotifier.notify(SlackEvent.builder()
                .header(((PantherException) e).getType().name())
                .level(SlackMessage.LEVEL.ERROR)
                .title("Unexpected error")
                .message(e.getMessage())
                .exception(e)
                .build());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("exception", e);
        modelAndView.addObject("url", request.getRequestURL());

        modelAndView.setViewName("error_page");
        return modelAndView;
    }

    @ExceptionHandler(Throwable.class)
    public ModelAndView handleUnexpectedException(HttpServletRequest request, Exception e) {
        logger.error("unexpected error. requestedUrl = " + request.getRequestURI(), e);
        slackNotifier.notify(SlackEvent.builder()
                .header(Executor.Type.DUMMY.name())
                .level(SlackMessage.LEVEL.ERROR)
                .title("Unexpected error")
                .message(e.getMessage())
                .exception(e)
                .build());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("exception", e);
        modelAndView.addObject("url", request.getRequestURL());

        modelAndView.setViewName("error_page");
        return modelAndView;
    }
}
