package com.lezhin.panther.controller;

import com.lezhin.constant.PGCompany;
import com.lezhin.constant.PaymentType;
import com.lezhin.panther.Context;
import com.lezhin.panther.ErrorCode;
import com.lezhin.panther.PagePayService;
import com.lezhin.panther.SimpleCacheService;
import com.lezhin.panther.command.Command;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.ExecutorException;
import com.lezhin.panther.exception.InternalPaymentException;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.lguplus.LguDepositExecutor;
import com.lezhin.panther.lguplus.LguplusPayment;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.notification.SlackEvent;
import com.lezhin.panther.notification.SlackMessage;
import com.lezhin.panther.notification.SlackNotifier;
import com.lezhin.panther.util.JsonUtil;
import com.lezhin.panther.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Page Controller. /webapp/WEB-INF/jsp/에 있는 jsp 를 리턴.
 *
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
    private SimpleCacheService simpleCacheService;

    public PageController(final PantherProperties pantherProperties, final PagePayService pagePayService,
                          final SlackNotifier slackNotifier, final SimpleCacheService simpleCacheService) {
        this.pantherProperties = pantherProperties;
        this.pagePayService = pagePayService;
        this.slackNotifier = slackNotifier;
        this.simpleCacheService = simpleCacheService;
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

    @RequestMapping(value = "/{pg}/sample_org", method = RequestMethod.GET)
    public ModelAndView sampleOrg(HttpServletRequest request, HttpServletResponse response,
                                  @PathVariable String pg) {

        String pageKey = String.format("pg/%s/sample_org", pg);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "there, This is panther");
        modelAndView.setViewName(pageKey);

        return modelAndView;
    }

    @RequestMapping(value = "/{pg}/reservation_org", method = {RequestMethod.GET, RequestMethod.POST})
    public <T extends PGPayment> String reservationOrg(HttpServletRequest request, HttpServletResponse response,
                                                       Model model, @PathVariable String pg) {

        // TODO cookie 에 token 저장이 필요?
        //RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();
        //logger.info("HTTP. Page reservation. requestInfo = {}", requestInfo);
        //Payment<T> payment = pagePayService.doCommand(Command.Type.RESERVE, requestInfo);

        //Class<T> claz = (Class<T>) payment.getPgPayment().getClass();
        //Map<String, Object> map = JsonUtil.toMap(payment.getPgPayment());


        model.addAttribute("pantherUrl", pantherProperties.getPantherUrl());
        //model.addAllAttributes(map);

        model.asMap().entrySet().stream().forEach(e -> logger.info("model. {} = {}", e.getKey(), e.getValue()));

        String pageKey = String.format("pg/%s/reservation_org", pg);

        logger.info("reservation. pg = {}, pageKey = {}", pg, pageKey);

        return pageKey;

    }


    @RequestMapping(value = "/{pg}/{paymentType}/sample", method = RequestMethod.GET)
    public ModelAndView sample(HttpServletRequest request, HttpServletResponse response, @PathVariable String pg,
                               @PathVariable String paymentType) {

        String jpsName = String.format("pg/%s/%s/sample", pg, paymentType);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "there, This is panther");
        modelAndView.setViewName(jpsName);

        return modelAndView;
    }

    @RequestMapping(value = "/{pg}/{paymentType}/reservation", method = {RequestMethod.GET, RequestMethod.POST})
    public <T extends PGPayment> ModelAndView reservation(HttpServletRequest request, HttpServletResponse response,
                                                          @PathVariable String pg, @PathVariable String paymentType) {

        logger.info("PAGE reservation [{}-{}]", pg, paymentType);
        // TODO cookie 에 token 저장이 필요?
        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();
        logger.info("PAGE reservation. requestInfo = {}", requestInfo);
        Payment<T> payment = null;
        try {
            payment = pagePayService.doCommand(Command.Type.RESERVE, requestInfo);
        } catch (Throwable e) {
            logger.warn("Failed to reserve !!!", e);
            throw e;
        }

        Map<String, Object> map = JsonUtil.toMap(payment.getPgPayment());

        String jspName = String.format("pg/%s/%s/reservation", pg, paymentType);
        logger.info("PAGE [{}] will show = {}", pg, jspName);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("pantherUrl", pantherProperties.getPantherUrl());
        modelAndView.addObject("failUrl", pantherProperties.getWebUrl() + "/ko/payment/");
        modelAndView.addAllObjects(map);
        modelAndView.setViewName(jspName);

        modelAndView.getModel().entrySet().stream().forEach(e -> logger.debug("model.  {} = {}", e.getKey(),
                e.getValue()));

        return modelAndView;

    }

    @RequestMapping(value = "/{pg}/{paymentType}/preauth/done", method = RequestMethod.POST)
    public ModelAndView preAuthDone(HttpServletRequest request, HttpServletResponse response,
                                    @PathVariable String pg, @PathVariable String paymentType) {

        logger.info("PAGE preauth_done. [{}-{}]", pg, paymentType);
        Payment payment = null;

        Map<String, Object> transformedParams = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()[0]));

        if (PGCompany.lguplus.name().equals(pg) &&
                (PaymentType.deposit.name().equals(paymentType) || PaymentType.mdeposit.name().equals(paymentType))) {

            String resCode = request.getParameter("LGD_RESPCODE").toString();
            String resMsg = request.getParameter("LGD_RESPMSG").toString();
            logger.info("pg.preauth RESCODE = {}, RESPMSG = {}", resCode, resMsg);

            if (!ErrorCode.LGUPLUS_OK.getCode().equals(resCode)) {
                // encoding 깨짐. 내용 파악은 브라우저에서 직접. 대충의 내용만.
                String newResMsg = LguDepositExecutor.extractResMsg(resMsg);
                transformedParams.put("LGD_RESPMSG", newResMsg);
            }
            // TODO builder default=true.
            Context context = null;
            // TODO builder default=true
            RequestInfo requestInfo = null;
            try {
                LguplusPayment pgPayment = JsonUtil.fromMap(transformedParams, LguplusPayment.class);
                requestInfo = simpleCacheService.getRequestInfo(Long.valueOf(pgPayment.getLGD_OID()));
                Payment requestPayment = Executor.Type.LGUDEPOSIT.createPayment(pgPayment);
                requestInfo = new RequestInfo.Builder(requestInfo).withPayment(requestPayment).build();

                logger.info("PAGE preauth [{}]. requestPayment = {}", pg, JsonUtil.toJson(requestPayment));

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
                payment = pagePayService.doCommand(Command.Type.PREAUTHENTICATE, context);
            } catch (Throwable e) {
                // FIXME handle exception. Redirect to GCS
                logger.warn("Failed to PREAUTHENTICATE", e);
                throw e;
            }

        }

        String jspName = String.format("pg/%s/%s/preauth_done", pg, paymentType);
        logger.info("PAGE [{}] will show = {}", pg, jspName);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("pantherUrl", pantherProperties.getPantherUrl());
        modelAndView.addObject("failUrl", pantherProperties.getWebUrl() + "/ko/payment/");
        modelAndView.addAllObjects(transformedParams);
        modelAndView.setViewName(jspName);

        modelAndView.getModel().entrySet().stream().forEach(e -> logger.debug("model.  {} = {}", e.getKey(),
                e.getValue()));

        return modelAndView;
    }


    @RequestMapping(value = "/{pg}/{paymentType}/authentication", method = RequestMethod.POST)
    public ModelAndView authenticate(HttpServletRequest request, HttpServletResponse response,
                                     @PathVariable String pg, @PathVariable String paymentType) {
        logger.info("PAGE authentication. [{}-{}]", pg, paymentType);
        Payment payment = null;

        Map<String, String[]> params = new HashMap(request.getParameterMap());
        Map<String, Object> transformedParams = params.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()[0]));

        params.entrySet().stream().forEach(e -> logger.debug("param. {} = {}", e.getKey(), e.getValue()[0]));
        if (PGCompany.lguplus.name().equals(pg) &&
                (PaymentType.deposit.name().equals(paymentType) || PaymentType.mdeposit.name().equals(paymentType))) {

            // TODO builder default=true.
            Context context = null;
            // TODO builder default=true
            RequestInfo requestInfo = null;
            try {
                LguplusPayment pgPayment = JsonUtil.fromMap(transformedParams, LguplusPayment.class);
                Payment requestPayment = Executor.Type.LGUDEPOSIT.createPayment(pgPayment);

                requestInfo = simpleCacheService.getRequestInfo(Long.valueOf(pgPayment.getLGD_OID()));
                requestInfo = new RequestInfo.Builder(requestInfo).withPayment(requestPayment).build();

                logger.info("PAGE [{}]. requestPayment = {}", pg, JsonUtil.toJson(requestPayment));

                context = Context.builder()
                        .requestInfo(requestInfo)
                        .payment(requestPayment)
                        .responseInfo(ResponseInfo.builder().code(pgPayment
                                .getLGD_RESPCODE()).description(pgPayment.getLGD_RESPMSG()).build()).build();

            } catch (Throwable e) {
                throw new PantherException(Executor.Type.LGUDEPOSIT, "Failed to convert to pgPayment", e);
            }

            try {
                payment = pagePayService.doCommand(Command.Type.AUTHENTICATE, context);
            } catch (Throwable e) {
                // FIXME handle exception. Redirect to GCS
                logger.warn("Failed to AUTHENTICATE ==> GCS /payment/result/fail", e);
                throw e;
            }

            String redirectUrl = String.format("%s/%s/payment/%s/result", pantherProperties.getWebUrl(),
                    Util.getLang(requestInfo.getLocale()), payment.getPaymentId());
            if (pantherProperties.getPantherUrl().contains("localhost")) {
                // for localhost test
                redirectUrl = String.format("%s/page/v1/%s/result",
                        pantherProperties.getPantherUrl(), payment.getPaymentId());
            }
            LguplusPayment finalPayment = (LguplusPayment) payment.getPgPayment();
            return redirect(redirectUrl, requestInfo, finalPayment.getLGD_FINANCENAME(),
                    finalPayment.getLGD_ACCOUNTNUM(), finalPayment.getLGD_CLOSEDATE(), null);
        }

        logger.info("payment = {}", payment);
        String pageKey = String.format("pg/%s/%s/authentication_done_tmp", pg, paymentType);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addAllObjects(JsonUtil.toMap(payment.getPgPayment()));
        modelAndView.setViewName(pageKey);

        return modelAndView;

    }

    @RequestMapping(value = "/{pg}/{paymentType}/payment", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> payment(HttpServletRequest request, HttpServletResponse response,
                                          @PathVariable String pg,
                                          @PathVariable String paymentType) {

        logger.info("PAGE payment. [{}-{}]", pg, paymentType);
        Payment payment = null;
        Map<String, String[]> params = new HashMap(request.getParameterMap());
        Map<String, Object> transformedParams = params.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()[0]));

        params.entrySet().stream().forEach(e -> logger.info("request param. {} = {}", e.getKey(), e.getValue()[0]));
        if (PGCompany.lguplus.name().equals(pg) &&
                (PaymentType.deposit.name().equals(paymentType) || PaymentType.mdeposit.name().equals(paymentType))) {

            String LGD_CASFLAG = Optional.ofNullable(transformedParams.get("LGD_CASFLAG")).orElse("").toString();
            if (!LGD_CASFLAG.equals("I")) {
                logger.info("LGD_CASFLAG = {}, LGD_RESCODE = {}, LGD_RESMSG = {}",
                        transformedParams.get("LGD_CASFLAG"), transformedParams.get("LGD_RESPCODE"),
                        transformedParams.get("LGD_RESPMSG") );
                return new ResponseEntity("OK", HttpStatus.OK);
            }

            // TODO default builder.
            Context context = null;
            try {
                LguplusPayment pgPayment = JsonUtil.fromMap(transformedParams, LguplusPayment.class);
                Payment requestPayment = Executor.Type.LGUDEPOSIT.createPayment(pgPayment);
                requestPayment.setPaymentType(PaymentType.valueOf(paymentType));
                RequestInfo requestInfo = new RequestInfo.Builder(requestPayment, pg).build();

                logger.info("PAGE [{}]. requestPayment = {}", pg, JsonUtil.toJson(requestPayment));

                context = Context.builder()
                        .requestInfo(requestInfo)
                        .payment(requestPayment)
                        .responseInfo(ResponseInfo.builder().code(pgPayment
                                .getLGD_RESPCODE()).description(pgPayment.getLGD_RESPMSG()).build()).build();

            } catch (Throwable e) {
                throw new PantherException(Executor.Type.LGUDEPOSIT, "Failed to convert to pgPayment", e);
            }
            try {

                payment = pagePayService.doCommand(Command.Type.PAY, context);

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

    private ModelAndView redirect(String redirectTargetUrl, RequestInfo requestInfo, String bank,
                                  String accountNumber, String date, String failReason) {
        RedirectView redirectView = new RedirectView(redirectTargetUrl);
        logger.info("REDIRECT to = {}", redirectView.getUrl());
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("__u", requestInfo.getUserId());
        attrs.put("isMobile", requestInfo.getIsMobile());
        attrs.put("isApp", requestInfo.getIsApp());
        if (!StringUtils.isEmpty(requestInfo.getReturnToUrl())) {
            attrs.put("returnTo", requestInfo.getReturnToUrl());
        }
        if (!StringUtils.isEmpty(failReason)) {
            attrs.put("reason", failReason);
        }
        if (!StringUtils.isEmpty(bank)) {
            attrs.put("bank", bank);
        }
        if (!StringUtils.isEmpty(accountNumber)) {
            attrs.put("accountNumber", accountNumber);
        }
        if (!StringUtils.isEmpty(date)) {
            attrs.put("date", date);
        }
        redirectView.setAttributesMap(attrs);
        return new ModelAndView(redirectView);
    }

    @RequestMapping(value = "/{paymentId}/result", method = RequestMethod.GET)
    public ModelAndView paymentResult(HttpServletRequest request, HttpServletResponse response,
                                      @PathVariable String paymentId,
                                      @RequestParam(value = "__u") Long userId,
                                      @RequestParam(required = false) String returnTo,
                                      @RequestParam(required = false, defaultValue = "false") Boolean isMobile,
                                      @RequestParam(required = false, defaultValue = "false") Boolean isApp,
                                      @RequestParam(required = false) String bank,
                                      @RequestParam(required = false) String accountNumber,
                                      @RequestParam(required = false) String date,
                                      @RequestParam(required = false) String reason) {

        String jspName = String.format("payment_result");

        logger.info("PAGE payment_result [{}], jspName = {}", paymentId, jspName);
        logger.info("PAGE paymentId={}, userId={}, returnTo={}, reason={}, isMobile={}, isApp={}, bank={}," +
                        "accountNumber={}, date={} ",
                paymentId, userId, returnTo, reason, isMobile, isApp, bank, accountNumber, date);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "NEW there, This is panther");
        modelAndView.setViewName(jspName);

        return modelAndView;

    }


    @RequestMapping(value = "/{pg}/ping", method = RequestMethod.GET)
    public String ping(HttpServletRequest request, HttpServletResponse response, Model model,
                       @PathVariable String pg) {

        String pageKey = String.format("pg/%s/ping", pg);

        logger.info("ping. pg = {}, pageKey = {}", pg, pageKey);

        String confDirStr = pantherProperties.getLguplus().getConfDir();
        model.addAttribute("confDir", confDirStr);
        model.addAttribute("CST_PLATFORM", "test");
        model.addAttribute("CST_MID", "lezhin001");
        File file = new File(confDirStr);
        File mallConf = new File(file, "/conf/mall.conf");
        logger.info("confDirStr = {}, file = {}, mallConf.exists = {}", confDirStr, file.getAbsoluteFile(),
                mallConf.exists());

        return pageKey;

    }

    @ExceptionHandler({InternalPaymentException.class, PantherException.class})
    public ModelAndView handleInternalPaymentExceptionException(HttpServletRequest request,
                                                                PantherException e) {
        logger.error("panther error. requestedUrl = " + request.getRequestURI(), e);
        slackNotifier.notify(SlackEvent.builder()
                .header(e.getType().name())
                .level(SlackMessage.LEVEL.ERROR)
                .title(e.getClass().getName())
                .message(e.getMessage())
                .exception(e)
                .build());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("exception", e);
        modelAndView.addObject("url", request.getRequestURL());

        modelAndView.setViewName("error_internal");
        return modelAndView;
    }

    @ExceptionHandler({ExecutorException.class})
    public ModelAndView handleException(HttpServletRequest request, Exception e) {
        logger.error("PAGE executor error. requestedUrl = " + request.getRequestURI(), e);
        slackNotifier.notify(SlackEvent.builder()
                .header(((PantherException) e).getType().name())
                .level(SlackMessage.LEVEL.ERROR)
                .title(e.getClass().getName())
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
        logger.error("PAGE unexpected error. requestedUrl = " + request.getRequestURI(), e);
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
