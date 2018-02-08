package com.lezhin.panther.controller;

import com.lezhin.constant.PGCompany;
import com.lezhin.constant.PaymentType;
import com.lezhin.panther.Context;
import com.lezhin.panther.PagePayService;
import com.lezhin.panther.SimpleCacheService;
import com.lezhin.panther.command.Command;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.LguDepositException;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.SessionException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.pg.lguplus.LguDepositExecutor;
import com.lezhin.panther.pg.lguplus.LguplusPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.notification.SlackEvent;
import com.lezhin.panther.notification.SlackMessage;
import com.lezhin.panther.notification.SlackNotifier;
import com.lezhin.panther.util.DateUtil;
import com.lezhin.panther.util.JsonUtil;
import com.lezhin.panther.util.Util;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public ModelAndView reservation(HttpServletRequest request, HttpServletResponse response,
                                                          @PathVariable String pg, @PathVariable String paymentType) {

        logger.info("  >>  RESERVATION [{}-{}]", pg, paymentType);
        RequestInfo requestInfo = new RequestInfo.Builder(request, pg).build();
        Payment payment = null;
        try {
            payment = pagePayService.doCommand(Command.Type.RESERVE, requestInfo);
        } catch (Throwable e) {
            logger.warn("Failed to reserve !!!", e);
            String redirectUrl = getPaymentUrl(requestInfo, -1L);
            return redirect(redirectUrl, requestInfo, null, null, null, e);
        }

        Map<String, Object> map = JsonUtil.toMap(payment.getPgPayment());

        String jspName = String.format("pg/%s/%s/reservation", pg, paymentType);
        logger.info("[{}] will show = {}", pg, jspName);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("pantherUrl", pantherProperties.getPantherUrl());
        modelAndView.addObject("failUrl",
                getFailUrl(getPaymentUrl(requestInfo, payment.getPaymentId()), requestInfo));
        modelAndView.addAllObjects(map);
        modelAndView.setViewName(jspName);

        modelAndView.getModel().entrySet().stream().forEach(e -> logger.debug("model.  {} = {}", e.getKey(),
                e.getValue()));

        return modelAndView;

    }

    @RequestMapping(value = "/{pg}/{paymentType}/preauth/done", method = RequestMethod.POST)
    public ModelAndView preAuthDone(HttpServletRequest request, HttpServletResponse response,
                                    @PathVariable String pg, @PathVariable String paymentType) {

        logger.info("PREAUTH_DONE [{}-{}]", pg, paymentType);
        Payment payment = null;

        Map<String, Object> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()[0]));

        String failUrl = null;
        if (PGCompany.lguplus.name().equals(pg) &&
                (PaymentType.deposit.name().equals(paymentType) || PaymentType.mdeposit.name().equals(paymentType))) {

            String resCode = request.getParameter("LGD_RESPCODE").toString();
            String resMsg = request.getParameter("LGD_RESPMSG").toString();
            logger.info("LguReuslt: RESCODE = {}, RESPMSG = {}", resCode, resMsg);

            RequestInfo requestInfo = null;
            String redirectUrl = null;
            try {
                requestInfo = simpleCacheService.getRequestInfo(Long.valueOf(params.get("LGD_OID").toString().trim()));
                redirectUrl = getPaymentUrl(requestInfo, Long.valueOf(params.get("LGD_OID").toString()));
                failUrl = getFailUrl(redirectUrl, requestInfo);
                if (!ResponseCode.LGUPLUS_OK.getCode().equals(resCode)) {
                    // encoding 깨짐. 내용 파악은 브라우저에서 직접. 대충의 내용만.
                    String newResMsg = LguDepositExecutor.extractResMsg(resMsg);
                    params.put("LGD_RESPMSG", newResMsg);
                    throw new LguDepositException(Executor.Type.LGUDEPOSIT, resCode, newResMsg);
                }

                LguplusPayment pgPayment = JsonUtil.fromMap(params, LguplusPayment.class);
                Payment requestPayment = Executor.Type.LGUDEPOSIT.createPayment(pgPayment);
                requestInfo = new RequestInfo.Builder(requestInfo).withPayment(requestPayment).build();
                params.put("isMobile", requestInfo.getIsMobile().booleanValue());

                Context context = Context.builder()
                        .requestInfo(requestInfo)
                        .payment(requestPayment)
                        .responseInfo(new ResponseInfo(pgPayment.getLGD_RESPCODE(), pgPayment.getLGD_RESPMSG()))
                        .build();

                payment = pagePayService.doCommand(Command.Type.PREAUTHENTICATE, context);
            } catch (SessionException e) {
                logger.warn("Failed to get RequestInfo. paymentId = {}",
                        Long.valueOf(params.get("LGD_OID").toString()));
                redirectUrl = getPaymentUrl(requestInfo, Long.valueOf(params.get("LGD_OID").toString()));
                failUrl = getFailUrl(redirectUrl, requestInfo);
                // jsp 내의 script에서 failUrl로 redirect 시킴.
            } catch (PantherException e) {
                logger.warn("Failed to PREAUTHENTICATE: {}", e.getMessage());
            } catch (Throwable e) {
                logger.warn("Failed to PREAUTHENTICATE: {}", e.getMessage());
            }

        }

        String jspName = String.format("pg/%s/%s/preauth_done", pg, paymentType);
        logger.info("[{}] will show = {}", pg, jspName);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("pantherUrl", pantherProperties.getPantherUrl());
        modelAndView.addObject("failUrl", failUrl);
        modelAndView.addAllObjects(params);
        modelAndView.setViewName(jspName);

        modelAndView.getModel().entrySet().stream().forEach(e -> logger.debug("model.  {} = {}", e.getKey(),
                e.getValue()));

        return modelAndView;
    }


    @RequestMapping(value = "/{pg}/{paymentType}/authentication", method = RequestMethod.POST)
    public ModelAndView authenticate(HttpServletRequest request, HttpServletResponse response,
                                     @PathVariable String pg, @PathVariable String paymentType) {
        logger.info("Authentication. [{}-{}]", pg, paymentType);
        Payment payment = null;

        Map<String, Object> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()[0]));
        params.entrySet().stream().forEach(e -> logger.debug("param. {} = {}", e.getKey(), e.getValue()));
        if (PGCompany.lguplus.name().equals(pg) &&
                (PaymentType.deposit.name().equals(paymentType) || PaymentType.mdeposit.name().equals(paymentType))) {


            RequestInfo requestInfo = null;
            String redirectUrl = null;
            try {
                requestInfo = simpleCacheService.getRequestInfo(Long.valueOf(params.get("LGD_OID").toString()));
                redirectUrl = getPaymentUrl(requestInfo, Long.valueOf(params.get("LGD_OID").toString()));

                LguplusPayment pgPayment = JsonUtil.fromMap(params, LguplusPayment.class);
                Payment requestPayment = Executor.Type.LGUDEPOSIT.createPayment(pgPayment);
                requestInfo = new RequestInfo.Builder(requestInfo).withPayment(requestPayment).build();

                Context context = Context.builder()
                        .requestInfo(requestInfo)
                        .payment(requestPayment)
                        .responseInfo(new ResponseInfo(pgPayment.getLGD_RESPCODE(), pgPayment.getLGD_RESPMSG()))
                        .build();

                payment = pagePayService.doCommand(Command.Type.AUTHENTICATE, context);

            } catch (SessionException e) {
                logger.warn("Failed to get RequestInfo. paymentId = {}",
                        Long.valueOf(params.get("LGD_OID").toString()));
                redirectUrl = getPaymentUrl(requestInfo, Long.valueOf(params.get("LGD_OID").toString()));
                return redirect(redirectUrl, requestInfo, null, null, null, e);
            } catch (Throwable e) {
                logger.warn("Failed to AUTHENTICATE: {}. need to redirect ==> {}", e.getMessage(), redirectUrl);
                return redirect(redirectUrl, requestInfo, null, null, null, new PantherException(Executor.Type
                        .LGUDEPOSIT, e));
            }

            requestInfo = requestInfo.withPayment(payment);
            simpleCacheService.saveRequestInfo(requestInfo);
            LguplusPayment finalPayment = (LguplusPayment) payment.getPgPayment();
            String date = DateUtil.format(
                    DateUtil.toInstant(finalPayment.getLGD_CLOSEDATE(), "yyyyMMddHHmmss", DateUtil.ASIA_SEOUL_ZONE)
                            .toEpochMilli(),
                    DateUtil.ASIA_SEOUL_ZONE, "yyyy/MM/dd");
            logger.info("  >>>  [LGUDEPOSIT] authentication done. payment={}, user={}, bank={}, account={}",
                    finalPayment.getLGD_OID(), finalPayment.getLGD_BUYER(), finalPayment.getLGD_FINANCENAME(),
                    finalPayment.getLGD_ACCOUNTNUM());
            return redirect(redirectUrl, requestInfo, finalPayment.getLGD_FINANCENAME(),
                    finalPayment.getLGD_ACCOUNTNUM(), date, null);
        }

        String jspName = String.format("pg/%s/%s/authentication", pg, paymentType);
        logger.info("PAGE [{}] will show = {}", pg, jspName);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addAllObjects(JsonUtil.toMap(payment.getPgPayment()));
        modelAndView.setViewName(jspName);

        return modelAndView;

    }

    private String getPaymentUrl(RequestInfo requestInfo, Long paymentId) {
        String paymentUrl = String.format("%s/%s/payment/%s/result", pantherProperties.getWebUrl(),
                Util.getLang(Optional.ofNullable(requestInfo)
                        .map(requestInfo1 -> requestInfo1.getLocale())
                        .orElse(null)),
                paymentId);
        if (pantherProperties.getPantherUrl().contains("localhost")) {
            // for localhost test
            paymentUrl = String.format("%s/page/v1/%s/result",
                    pantherProperties.getPantherUrl(), paymentId);
        }
        return paymentUrl;
    }

    private String getFailUrl(String redirectTargetUrl, RequestInfo requestInfo) {
        UriComponents target = UriComponentsBuilder.fromHttpUrl(redirectTargetUrl).build();
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.queryParam("__u",
                Optional.ofNullable(requestInfo).map(requestInfo1 -> requestInfo1.getUserId()).orElse(-1L));
        builder.queryParam("isMobile",
                Optional.ofNullable(requestInfo).map(requestInfo1 -> requestInfo1.getIsMobile())
                .orElse(Boolean.FALSE));
        builder.queryParam("isApp", Optional.ofNullable(requestInfo).map(requestInfo1 -> requestInfo1.getIsApp())
                .orElse(Boolean.FALSE));
        if (!StringUtils.isEmpty(requestInfo) && !StringUtils.isEmpty(requestInfo.getReturnTo())) {
            builder.queryParam("returnTo", requestInfo.getReturnTo());
        }
        builder.uriComponents(target);
        UriComponents uriComponents = builder.build().encode();

        return uriComponents.toUriString();
    }

    private ModelAndView redirect(String redirectTargetUrl, RequestInfo requestInfo, String bank,
                                  String accountNumber, String date, Throwable e) {
        RedirectView redirectView = new RedirectView(redirectTargetUrl);
        logger.info("REDIRECT to = {}", redirectView.getUrl());
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("__u", Optional.ofNullable(requestInfo).map(requestInfo1 -> requestInfo1.getUserId()).orElse(-1L));
        attrs.put("isMobile", Optional.ofNullable(requestInfo).map(requestInfo1 -> requestInfo1.getIsMobile())
                .orElse(Boolean.FALSE));
        attrs.put("isApp", Optional.ofNullable(requestInfo).map(requestInfo1 -> requestInfo1.getIsApp())
                .orElse(Boolean.FALSE));
        if (!StringUtils.isEmpty(requestInfo) && !StringUtils.isEmpty(requestInfo.getReturnTo())) {
            attrs.put("returnTo", requestInfo.getReturnTo());
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
        if (!StringUtils.isEmpty(e)) {
            String failReason = null;
            switch (e.getClass().getSimpleName()) {
                case "SessionException":
                    failReason = "Session expired";
                    break;
                case "ParameterException":
                case "PreconditionException":
                case "FraudException":
                case "LguDepositException":
                    failReason = e.getMessage();
                    break;
                default:
                    failReason = "Internal Error";
                    break;
            }

            attrs.put("reason", failReason);
            slackNotifier.notify(e);
            logger.error("Failed. will redirect. ", e);
        }
        redirectView.setAttributesMap(attrs);
        return new ModelAndView(redirectView);
    }

    /**
     * For local test. Mock payment_result page.
     */
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

    @ExceptionHandler(Throwable.class)
    public ModelAndView handleUnexpectedException(HttpServletRequest request, Throwable e) {
        logger.error("PAGE unexpected error. requestedUrl = " + request.getRequestURI(), e);
        Executor.Type executorType = Util.getType(e);
        slackNotifier.notify(SlackEvent.builder()
                .header(executorType.name())
                .level(SlackMessage.LEVEL.ERROR)
                .title(e.getMessage())
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
