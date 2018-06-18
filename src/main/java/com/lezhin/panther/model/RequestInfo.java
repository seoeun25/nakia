package com.lezhin.panther.model;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.io.CharStreams;
import com.lezhin.constant.LezhinCurrency;
import com.lezhin.constant.LezhinStore;
import com.lezhin.constant.PaymentType;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.pg.happypoint.HappyPointPayment;
import com.lezhin.panther.pg.lguplus.LguplusPayment;
import com.lezhin.panther.pg.lpoint.LPointPayment;
import com.lezhin.panther.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class RequestInfo implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(RequestInfo.class);

    private String pg;
    private String ip;
    private String token;
    private Boolean isMobile;
    private Boolean isApp;
    private String returnTo;
    private String locale;
    private Long userId;
    private PaymentType paymentType;
    private Executor.Type executorType;
    private Payment payment;

    RequestInfo(Builder builder) {
        this.pg = builder.pg;
        this.ip = builder.ip;
        this.token = builder.token;
        this.isMobile = builder.isMobile;
        this.isApp = builder.isApp;
        this.returnTo = builder.returnTo;
        this.locale = builder.locale;
        this.userId = builder.userId;
        this.paymentType = builder.paymentType;
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

    public String getReturnTo() {
        return returnTo;
    }

    public String getLocale() {
        return locale;
    }

    public Long getUserId() {
        return this.userId;
    }

    public PaymentType getPaymentType() {
        return this.paymentType;
    }

    public Executor.Type getExecutorType() {
        return executorType;
    }

    public Payment getPayment() {
        return payment;
    }

    public RequestInfo withPayment(Payment payment) {
        this.payment = payment;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("ip", ip)
                .add("token", token)
                .add("isMobile", isMobile)
                .add("isApp", isApp)
                .add("returnTo", returnTo)
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
        private String returnTo;
        private String locale;
        private Long userId;
        private PaymentType paymentType;
        private Executor.Type executorType;
        private Payment payment;

        public Builder(RequestInfo requestInfo) {
            this.pg = requestInfo.pg;
            this.ip = requestInfo.ip;
            this.token = requestInfo.token;
            this.isMobile = requestInfo.isMobile;
            this.isApp = requestInfo.isApp;
            this.returnTo = requestInfo.returnTo;
            this.locale = requestInfo.locale;
            this.userId = requestInfo.userId;
            this.paymentType = requestInfo.paymentType;
            this.executorType = requestInfo.executorType;
            this.payment = requestInfo.payment;
        }

        public Builder(Payment payment, String pg) {
            findExecutor(pg, payment.getPaymentType().name());
            withPayment(payment);
        }

        public Builder(HttpServletRequest request, String pg) {
            findExecutor(pg, request.getParameter("paymentType"));

            Map<String, Object> requestMap = new HashMap<>();

            try {
                if (executorType == Executor.Type.LGUDEPOSIT) {
                    // from pageController. lguplus
                    requestMap.put("locale", request.getParameter("locale"));
                    requestMap.put("isMobile", request.getParameter("isMobile"));
                    requestMap.put("isApp", request.getParameter("isApp"));
                    requestMap.put("_lz_userId", request.getParameter("_lz_userId"));
                    requestMap.put("returnTo", request.getParameter("returnTo"));
                    requestMap.put("store", request.getParameter("store"));
                    requestMap.put("platform", request.getParameter("platform")); // notUsed.
                } else {
                    // from apiController. happypoint
                    String result = CharStreams.toString(new InputStreamReader(request.getInputStream(), Charsets.UTF_8));
                    requestMap = JsonUtil.fromJson(result, Map.class);
                    if (requestMap.containsKey("returnToUrl")) {
                        // TODO "returnToUrl" is deprecated. Use "returnTo"
                        // https://wiki.lezhin.com/display/BIZDEV/happypoint
                        Object value = requestMap.get("returnToUrl");
                        requestMap.put("returnTo", value);
                    }
                }
            } catch (IOException e) {
                throw new ParameterException(executorType, "Failed to read requestBody");
            }
            requestMap.entrySet().stream().forEach(e -> {
                logger.info("request. {} = {} / [{}]", e.getKey(), e.getValue(),
                        e.getValue() == null ? "null" : e.getValue().getClass().getSimpleName());
            });

            Optional.ofNullable(request).orElseThrow(() ->
                    new ParameterException(executorType, "HttpServletRequest can not be null"));

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
            Optional.ofNullable(token).orElseThrow(() -> new ParameterException(executorType, "token can not be null"));
            logger.info("request. token = {}", token);
            withToken(token);

            withLocale(Optional.ofNullable(requestMap.get("locale")).orElse("ko-KR").toString());
            withIsMobile(Boolean.valueOf(Optional.ofNullable(requestMap.get("isMobile"))
                    .orElse(Boolean.FALSE).toString()));
            withIsApp(Boolean.valueOf(Optional.ofNullable(requestMap.get("isApp")).orElse(Boolean.FALSE).toString()));
            withUserId(Long.valueOf((Optional.ofNullable(requestMap.get("_lz_userId")).orElseThrow(
                    () -> new ParameterException(executorType, "_lz_userId can not be null")
            )).toString()));
            withReturnTo(Optional.ofNullable(requestMap.get("returnTo")).orElse("").toString());


            // check the request param
            Payment payment = new Payment();
            payment.setPgCompany(pg);
            payment.setLocale(locale);
            if (executorType == Executor.Type.HAPPYPOINT) {
                payment.setUserId(userId);

                // happypoint 일 경우 coinProduct
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
                // TODO happypoint 의 경우 request에 paymentType을 받지 않아서, 임시로 externalStoreProductId를 사용
                payment.setPaymentType(executorType.getPaymentType(payment.getExternalStoreProductId()));
                withPaymentType(payment.getPaymentType());
                payment.setPgPayment(pgPayment);
                Meta meta = new Meta();
                meta.setDynamicAmount(pgPayment.getUseReqPt());
                payment.setMeta(meta);
                withPayment(payment);
            } else if (executorType == Executor.Type.LPOINT) {
                payment.setUserId(userId);

                payment.setPaymentId((Long) Optional.ofNullable(requestMap.get("paymentId")).orElse(-1L));
                payment.setExternalStoreProductId(
                        Optional.ofNullable(requestMap.get("_lz_externalStoreProductId")).orElse("").toString());
                payment.setStore(LezhinStore.valueOf(Optional.ofNullable(requestMap.get("_lz_store"))
                        .orElse("base").toString()));
                payment.setStoreVersion(Optional.ofNullable(requestMap.get("_lz_storeVersion"))
                        .orElse("").toString());
                payment.setPaymentType(executorType.getPaymentType(payment.getExternalStoreProductId()));

                LPointPayment pgPayment = new LPointPayment();
                pgPayment.setCtfCno(Optional.ofNullable(requestMap.get("pgPayment_ctfCno"))
                        .orElse("").toString());
                pgPayment.setPswd(Optional.ofNullable(requestMap.get("pgPayment_pswd"))
                        .orElse("").toString());
                pgPayment.setAkCvPt((Integer) Optional.ofNullable(requestMap.get("pgPayment_akCvPt"))
                        .orElse(0));

                payment.setAmount(pgPayment.getAkCvPt().floatValue());

                Meta meta = new Meta();
                meta.setDynamicAmount(pgPayment.getAkCvPt());

                payment.setMeta(meta);
                payment.setPgPayment(pgPayment);

                withPaymentType(payment.getPaymentType());
                withPayment(payment);
            } else if (executorType == Executor.Type.LGUDEPOSIT) {
                payment.setUserId(userId); // token base라 reserve에서 payment 만들 때 다시 세팅됨. 여기서는 딱히 필요 없음

                // coin product
                payment.setPaymentType(paymentType);
                payment.setCoinProductId(Long.parseLong(Optional.ofNullable(request.getParameter("productId")
                ).orElseThrow(
                        () -> new ParameterException(executorType, "productId(coin) can not be null")
                ).toString()));
                payment.setCurrency(LezhinCurrency.valueOf(Optional.ofNullable(request.getParameter("currency")
                ).orElseThrow(
                        () -> new ParameterException(executorType, "currency can not be null")
                ).toString()));
                payment.setAmount(Float.parseFloat(Optional.ofNullable(request.getParameter("amount")
                ).orElseThrow(
                        () -> new ParameterException(executorType, "amount cat not be null")
                ).toString()));
                if (payment.getAmount() <= 0) {
                    throw new ParameterException(executorType, "amount can not be less and equal than 0");
                }
                if (payment.getAmount() % 10 > 0) {
                    throw new ParameterException(executorType, "amount should be integer, not float neither double");
                }
                payment.setPointAmount(Integer.parseInt(Optional.ofNullable(request.getParameter("point")
                ).orElse("0").toString()));

                payment.setStore(LezhinStore.valueOf(Optional.ofNullable(request.getParameter("store")
                ).orElseThrow(
                        () -> new ParameterException(executorType, "store can not be null")
                ).toString()));
                LguplusPayment pgPayment = LguplusPayment.builder().build();
                payment.setPgPayment(pgPayment);
                payment.setMeta(new Meta());
                withPayment(payment);
            }
            logger.info("request. payment = {}", JsonUtil.toJson(payment));

        }

        private void findExecutor(final String pg, final String paymentType) {
            withPg(pg);
            // TODO executor setting을 뭐 좀 다른 방법으로.
            if ("happypoint".equals(pg)) {
                withExecutor(Executor.Type.HAPPYPOINT);
            } else if ("lguplus".equals(pg)) {
                PaymentType pType = PaymentType.valueOf(Optional.ofNullable(paymentType).orElseThrow(
                        () -> new ParameterException(Executor.Type.UNKNOWN, "lguplus paymentType can not be null")));
                withPaymentType(pType);
                if (pType == PaymentType.deposit || pType == PaymentType.mdeposit) {
                    withExecutor(Executor.Type.LGUDEPOSIT);
                } else {
                    throw new ParameterException(Executor.Type.UNKNOWN, "Not support. PG = " + pg + ", paymentType = " +
                            paymentType);
                }
            } else if ("lpoint".equals(pg)) {
                withExecutor(Executor.Type.LPOINT);
            } else if ("unknown".equals(pg)) {
                withPaymentType(PaymentType.unknown);
                withExecutor(Executor.Type.UNKNOWN);
            } else {
                throw new ParameterException(Executor.Type.UNKNOWN, "Not support. PG = " + pg + ", paymentType = " +
                        paymentType);
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

        public Builder withReturnTo(String returnTo) {
            this.returnTo = returnTo;
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

        public Builder withPaymentType(PaymentType paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public RequestInfo build() {
            RequestInfo requestInfo = new RequestInfo(this);
            return requestInfo;
        }

    }

}
