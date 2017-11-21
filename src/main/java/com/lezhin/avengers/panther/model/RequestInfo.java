package com.lezhin.avengers.panther.model;

import com.lezhin.avengers.panther.exception.ParameterException;
import com.lezhin.avengers.panther.executor.Executor;
import com.lezhin.avengers.panther.happypoint.HappyPointPayment;
import com.lezhin.avengers.panther.util.JsonUtil;
import com.lezhin.avengers.panther.util.Util;
import com.lezhin.constant.LezhinStore;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class RequestInfo {

    private static final Logger logger = LoggerFactory.getLogger(RequestInfo.class);

    private String pg;
    private String ip;
    private String token;
    private Boolean isMobile;
    private Boolean isApp;
    private String returnToUrl;
    private String locale;
    private Long userId;
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
        this.userId = builder.userId;
        this.executorType = builder.executorType;
        this.payment = builder.payment;
    }

    public String getIp() {
        return ip;
    }

    public String getToken() {
        return token;
    }

    public Boolean getIsMobile() {
        return isMobile;
    }

    public Boolean getIsApp() {
        return isApp;
    }

    public String getReturnToUrl() {
        return returnToUrl;
    }

    public String getLocale() {
        return locale;
    }

    public Long getUserId() {
        return this.userId;
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
                .add("userId", userId)
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
        private Boolean isMobile;
        private Boolean isApp;
        private String returnToUrl;
        private String locale;
        private Long userId;
        private Executor.Type executorType;
        private Payment payment;

        public Builder(RequestInfo requestInfo) {
            this.pg = requestInfo.pg;
            this.ip = requestInfo.ip;
            this.token = requestInfo.token;
            this.isMobile = requestInfo.isMobile;
            this.isApp = requestInfo.isApp;
            this.returnToUrl = requestInfo.returnToUrl;
            this.locale = requestInfo.locale;
            this.userId = requestInfo.userId;
            this.executorType = requestInfo.executorType;
            this.payment = requestInfo.payment;
        }

        public Builder(HttpServletRequest request, String pg) {

            Map<String, Object> requestMap = new HashMap<>();

            try {
                String result = CharStreams.toString(new InputStreamReader(request.getInputStream(), Charsets.UTF_8));
                logger.info("requestBody = {}", result);
                requestMap = JsonUtil.fromJson(result, Map.class);
            } catch (IOException e) {
                throw new ParameterException("Failed to read requestBody");
            }

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
            String token = Optional.ofNullable(request.getHeader("Authorization"))
                    .orElse(Optional.ofNullable(request.getAttribute("Authorization")).map(Object::toString)
                            .orElse(Optional.ofNullable(WebUtils.getCookie(request, "_lz")).map(Cookie::getValue)
                                    .orElse(null)));
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(token.indexOf("Bearer ") + 7);
            }
            if (token == null) {
                token = request.getParameter("_lz");
            }
            Optional.ofNullable(token).orElseThrow(() -> new ParameterException("token can not be null"));
            withToken(token);

            withPg(pg);
            // TODO executor setting을 뭐 좀 다른 방법으로.
            if ("happypoint".equals(pg)) {
                withExecutor(Executor.Type.HAPPYPOINT);
            } else if ("dummy".equals(pg)) {
                withExecutor(Executor.Type.DUMMY);
            } else {
                throw new ParameterException("Unknown PG = " + pg);
            }

            withLocale(Optional.ofNullable(requestMap.get("locale")).orElse("ko-KR").toString());
            withIsMobile(((Boolean) Optional.ofNullable(requestMap.get("isMobile")).orElse(Boolean.FALSE)));
            withIsApp(((Boolean) Optional.ofNullable(requestMap.get("isApp")).orElse(Boolean.FALSE)));
            withUserId(Long.valueOf((Optional.ofNullable(requestMap.get("_lz_userId")).orElseThrow(
                    () -> new ParameterException("_lz_userId can not be null")
            )).toString()));
            withReturnToUrl(Optional.ofNullable(requestMap.get("returnToUrl")).orElse("").toString());


            // check the request param
            Payment payment = new Payment();
            payment.setPgCompany(pg);
            payment.setPaymentType(executorType.getPaymentType(isMobile));
            payment.setLocale(Util.of(locale));
            if (executorType == Executor.Type.HAPPYPOINT) {
                payment.setUserId(userId);

                payment.setExternalStoreProductId(
                        Optional.ofNullable(requestMap.get("_lz_externalStoreProductId")).orElse("").toString());
                payment.setStore(LezhinStore.valueOf(Optional.ofNullable(requestMap.get("_lz_store"))
                        .orElse("base").toString()));
                payment.setStoreVersion(Optional.ofNullable(requestMap.get("_lz_storeVersion")).orElse("").toString());
                HappyPointPayment pgPayment = new HappyPointPayment();
                pgPayment.setMbrNo(Optional.ofNullable(requestMap.get("pgPayment_mbrNo")).orElse("").toString());
                pgPayment.setMbrNm(Optional.ofNullable(requestMap.get("pgPayment_mbrNm")).orElse("").toString());
                pgPayment.setUseReqPt((Integer) Optional.ofNullable(requestMap.get("pgPayment_useReqPt"))
                        .orElse(0));
                payment.setPgPayment(pgPayment);
                Meta meta = new Meta();
                meta.setDynamicAmount(pgPayment.getUseReqPt());
                payment.setMeta(meta);
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

        public Builder withIsMobile(Boolean isMobile) {
            this.isMobile = isMobile;
            return this;
        }

        public Builder withIsApp(Boolean isApp) {
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

        public Builder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public RequestInfo build() {
            RequestInfo requestInfo = new RequestInfo(this);
            return requestInfo;
        }

    }

}
