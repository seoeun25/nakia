package com.lezhin.avengers.panther.model;

import com.lezhin.avengers.panther.exception.ParameterException;
import com.lezhin.avengers.panther.executor.Executor;
import com.lezhin.avengers.panther.happypoint.HappyPointPayment;

import com.google.common.base.MoreObjects;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Optional;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class RequestInfo {

    private String pg;
    private String ip;
    private String token;
    private String isMobile;
    private String isApp;
    private String returnToUrl;
    private String locale;
    private Executor.Type executorType;
    private Payment payment;

    RequestInfo(Builder builder) {
        this.pg = builder.pg;
        this.ip = builder.ip;
        this.token = builder.token;
        this.isMobile = builder.isMobile;
        this.isApp = builder.isApp;
        this.returnToUrl = builder.returnToUrl;
        this.locale = builder.locale;
        this.executorType = builder.executorType;
        this.payment = builder.payment;
    }

    public String getIp() {
        return ip;
    }

    public String getToken() {
        return token;
    }

    public String getIsMobile() {
        return isMobile;
    }

    public String getIsApp() {
        return isApp;
    }

    public String getReturnToUrl() {
        return returnToUrl;
    }

    public String getLocale() {
        return locale;
    }

    public Executor.Type getExecutorType() {
        return executorType;
    }

    public Payment getPayment() {
        return payment;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ip", ip)
                .add("token", token)
                .add("isMobile", isMobile)
                .add("isApp", isApp)
                .add("returnToUrl", returnToUrl)
                .add("locale", locale)
                .add("executorType", executorType)
                .toString();
    }

    public static class Builder {

        private String pg;
        private String ip;
        private String token;
        private String isMobile;
        private String isApp;
        private String returnToUrl;
        private String locale;
        private Executor.Type executorType;
        private Payment payment;

        public Builder(HttpServletRequest request, String pg) {
            Optional.ofNullable(request).orElseThrow(() ->
                    new ParameterException("HttpServletRequest can not be null"));

            // Header 에서 다음 순서로 extract.
            String ip = Optional.ofNullable(request.getHeader("Forwarded")).map(Object::toString)
                    .orElse(Optional.ofNullable(request.getHeader("X-Forwarded-For")).map(Object::toString)
                            .orElse(Optional.ofNullable(request.getHeader("X-Forwarded")).map(Object::toString)
                                    .orElse(Optional.ofNullable(request.getHeader("X-Cluster-Client-Ip"))
                                            .map(Object::toString)
                                            .orElse(Optional.ofNullable(request.getHeader("Client-Ip"))
                                                    .map(Object::toString).orElse(null)))));
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            withIp(ip);

            // header(Authorization) -> attribute(Authorization)-> cookie(_lz) -> param(_lz)
            // header 에서는 Bearer 없이 보내야 함.
            String token = Optional.ofNullable(request.getHeader("Authorization"))
                    .orElse(Optional.ofNullable(request.getAttribute("Authorization")).map(Object::toString)
                            .orElse(Optional.ofNullable(WebUtils.getCookie(request, "_lz")).map(Cookie::getValue)
                                    .orElse(null)));
            if (token == null) {
                token = request.getParameter("_lz");
            }
            withToken(token);

            withIsMobile(request.getParameter("isMobile"));
            withIsApp(request.getParameter("isApp"));
            withReturnToUrl(request.getParameter("returnTo"));
            withLocale(Optional.ofNullable(request.getParameter("locale")).orElse("ko-KR"));

            withPg(pg);
            // TODO executor setting을 뭐 좀 다른 방법으로.
            if ("happypoint".equals(pg)) {
                withExecutor(Executor.Type.HAPPYPOINT);
            } else if ("dummy".equals(pg)) {
                withExecutor(Executor.Type.DUMMY);
            } else {
                throw new ParameterException("Unknown PG = " + pg);
            }

            // payment // FIXME Where to check param?
            if (executorType == Executor.Type.HAPPYPOINT) {
                Payment payment = new Payment();
                payment.setUserId(Long.valueOf((Optional.ofNullable(request.getParameter("_lz_userId"))).orElseThrow(
                        () -> new ParameterException("_lz_userId can not be null")
                )));

                payment.setExternalStoreProductId(request.getParameter("_lz_externalStoreProductId"));
                HappyPointPayment pgPayment = new HappyPointPayment();
                pgPayment.setMbrNo(request.getParameter("meta_mbrNo"));
                pgPayment.setMbrNm(request.getParameter("meta_mbrNm"));
                pgPayment.setUseReqPt(request.getParameter("meta_useReqPt"));
                payment.setPgPayment(pgPayment);
                withPayment(payment);
            }

        }

        public Builder withPg(String pg) {
            this.pg = pg;
            return this;
        }

        public Builder withIp(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder withToken(String token) {
            this.token = token;
            return this;
        }

        public Builder withIsMobile(String isMobile) {
            this.isMobile = isMobile;
            return this;
        }

        public Builder withIsApp(String isApp) {
            this.isApp = isApp;
            return this;
        }

        public Builder withReturnToUrl(String returnToUrl) {
            this.returnToUrl = returnToUrl;
            return this;
        }

        public Builder withLocale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder withExecutor(Executor.Type type) {
            this.executorType = type;
            return this;
        }

        public Builder withPayment(Payment payment) {
            this.payment = payment;
            return this;
        }

        public RequestInfo build() {
            RequestInfo requestInfo = new RequestInfo(this);
            return requestInfo;
        }

    }

}
