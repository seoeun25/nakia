package com.lezhin.avengers.panther.model;

import com.lezhin.beans.entity.common.LezhinLocale;
import com.lezhin.constant.LezhinCurrency;
import com.lezhin.constant.LezhinStore;
import com.lezhin.constant.PaymentState;
import com.lezhin.constant.PaymentType;
import com.lezhin.model.CohortData;
import com.lezhin.avengers.panther.exception.ParameterException;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Payment를 표현하는 entity. Lezhin Server의 InteralPaymentView
 * @author seoeun
 * @since 2017.10.24
 */
@Component // TODO entity
public abstract class Payment {

    // from server. InternalPaymentView.

    protected Long paymentId;
    protected Long refPaymentId; // TODO ?
    protected boolean isManual;

    protected Long userId;
    protected String userName;
    protected String userEmail;

    protected Long coinProductId;
    protected String coinProductName;
    protected Integer coin;

    protected Float amount;
    protected Integer pointAmount; // TODO 사용 안함. 삭제 예정.
    protected LezhinCurrency currency; // TODO 사용 안함. 삭제 예정.

    protected String pgId;
    protected String pgCompany;
    protected PaymentType paymentType;
    protected LezhinStore store;
    protected String storeVersion;
    protected LezhinLocale locale;

    protected PaymentState state;

    /**
     * 외부결제 시스템 결제 승인 아이디
     */
    protected String approvalId;

    protected Long createdAt;
    protected Long confirmedAt;
    protected Map<String, Long> timestamps;

    /**
     * ip : 결제 IP
     * country : 결제 국가
     * manual : 수동충전 1 : true, 0 or "nothing" : fasle
     * *
     * packageName : (Android) 패키지 명
     * purchaseToke : (Android) 구매 토큰
     * appleReceiptData : (iOS) 구매 영수증
     * naverReceiptData : naver 결제 정보
     * tstoreReceiptData : tstore 결제 정보
     * playReceiptData : play 결제 정보
     * webReceiptData : web 결제 정보 (결제 수단별로 파싱해서 사용)
     */
    protected Map<String, Object> meta;

    /**
     * 추가 정보
     */
    protected Map<String, Object> extra;

    protected CohortData cohortData; //TODO 통계용. not used.

    // ---------- from pg ---------------------

    protected String environment;
    protected String lezhinPgUrl;
    protected Boolean isMobile;
    protected Boolean isApp;
    protected String returnToUrl;

    public Payment() {

    }

    public Payment(String environment, String lezhinPgUrl, Long paymentId,
                   Boolean isMobile, Boolean isApp, String returnToUrl, LezhinLocale locale) {
        this.pgId = getPGId();
        this.environment = environment;
        this.isMobile = isMobile;
        this.isApp = isApp;
        this.lezhinPgUrl = lezhinPgUrl;
        this.paymentId = paymentId;
        if (returnToUrl != null) {
            this.returnToUrl = ulrDecode(returnToUrl);
        }
        this.locale = locale;
    }

    //---- GET/SET

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getRefPaymentId() {
        return refPaymentId;
    }

    public void setRefPaymentId(Long refPaymentId) {
        this.refPaymentId = refPaymentId;
    }

    public boolean isManual() {
        return isManual;
    }

    public void setManual(boolean manual) {
        isManual = manual;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getCoinProductId() {
        return coinProductId;
    }

    public void setCoinProductId(Long coinProductId) {
        this.coinProductId = coinProductId;
    }

    public String getCoinProductName() {
        return coinProductName;
    }

    public void setCoinProductName(String coinProductName) {
        this.coinProductName = coinProductName;
    }

    public Integer getCoin() {
        return coin;
    }

    public void setCoin(Integer coin) {
        this.coin = coin;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Integer getPointAmount() {
        return pointAmount;
    }

    public void setPointAmount(Integer pointAmount) {
        this.pointAmount = pointAmount;
    }

    public LezhinCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(LezhinCurrency currency) {
        this.currency = currency;
    }

    public String getPgId() {
        return pgId;
    }

    public void setPgId(String pgId) {
        this.pgId = pgId;
    }

    public String getPgCompany() {
        return pgCompany;
    }

    public void setPgCompany(String pgCompany) {
        this.pgCompany = pgCompany;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public LezhinStore getStore() {
        return store;
    }

    public void setStore(LezhinStore store) {
        this.store = store;
    }

    public String getStoreVersion() {
        return storeVersion;
    }

    public void setStoreVersion(String storeVersion) {
        this.storeVersion = storeVersion;
    }

    public LezhinLocale getLocale() {
        return locale;
    }

    public void setLocale(LezhinLocale locale) {
        this.locale = locale;
    }

    public PaymentState getState() {
        return state;
    }

    public void setState(PaymentState state) {
        this.state = state;
    }

    public String getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Long confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Map<String, Long> getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(Map<String, Long> timestamps) {
        this.timestamps = timestamps;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public CohortData getCohortData() {
        return cohortData;
    }

    public void setCohortData(CohortData cohortData) {
        this.cohortData = cohortData;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getLezhinPgUrl() {
        return lezhinPgUrl;
    }

    public void setLezhinPgUrl(String lezhinPgUrl) {
        this.lezhinPgUrl = lezhinPgUrl;
    }

    public Boolean isMobile() {
        return isMobile;
    }

    public void setMobile(Boolean mobile) {
        isMobile = mobile;
    }

    public Boolean isApp() {
        return isApp;
    }

    public void setApp(Boolean app) {
        isApp = app;
    }

    public String getReturnToUrl() {
        return returnToUrl;
    }

    public void setReturnToUrl(String returnToUrl) {
        this.returnToUrl = returnToUrl;
    }

    //-------------
    public String jsonSerialize(String encoding) {
        if (encoding == null) {
            encoding = "UTF-8";
        }
        // FIXME fasterxml
        return "";
    }

    public Map<String, Object> getMetaData() {
        return array_merge(Optional.ofNullable(meta).orElse(new HashMap<>()), buildMetaData());
    }

    public Map<String, Object> getReceiptData() {
        Map<String, Object> map1 = ImmutableMap.of(
                "_lz_coin_product_id", coinProductId,
                "_lz_currency", currency,
                "_lz_point_amount", pointAmount
        );
        return array_merge(map1, buildReceiptData());
    }

    public Map<String, Object> getReserveViewData(String encodeing) {
        Map<String, Object> map1 = ImmutableMap.of(
                "_lz_user_id", userId,
                "_lz_payment_id", paymentId,
                "_lz_coin_product_id", coinProductId,
                "_lz_currency", currency,
                "_lz_point_amount", pointAmount);
        return array_merge(map1, buildReserveViewData(encodeing));
    }

    public Map<String, Object> getVerifyViewData(String encoding) {
        Map<String, Object> map1 = ImmutableMap.of(
                "_lz_user_id", userId,
                "_lz_payment_id", paymentId,
                "_lz_coin_product_id", coinProductId,
                "_lz_currency", currency,
                "_lz_point_amount", pointAmount);
        return array_merge(map1, buildVerifyViewData(encoding));

    }

    public Map<String, Object> getPaymentViewData(String encoding) {
        Map<String, Object> map1 = ImmutableMap.of(
                "_lz_user_id", userId,
                "_lz_payment_id", paymentId,
                "_lz_coin_product_id", coinProductId,
                "_lz_currency", currency,
                "_lz_point_amount", pointAmount);
        return array_merge(map1, buildPaymentViewData(encoding));
    }

    abstract protected Map<String, Object> buildMetaData();

    abstract protected Map<String, Object> buildReceiptData();

    abstract protected Map<String, Object> buildReserveViewData(String encoding);

    abstract protected Map<String, Object> buildVerifyViewData(String encoding);

    abstract protected Map<String, Object> buildPaymentViewData(String encoding);

    abstract protected String[] onParse(HttpServletRequest request);

    abstract public String getPGId();

    public String getDynamicAmount() {
        return null;
    }

    //TODO 이렇게 처리하는게 맞을까?
    public void parse(HttpServletRequest request) {
        //TODO parse for request params and body.

        // FIXME null check. param check.
        try {
            // toss sub class. (PG detail info.)
            this.pgId = getPGId();
            this.userId = Long.valueOf(request.getParameter("_lz_user_id"));
            this.paymentId = Long.valueOf(request.getParameter("_lz_payment_id"));
            this.coinProductId = Long.valueOf(request.getParameter("_lz_coin_product_id"));
            this.currency = LezhinCurrency.valueOf(request.getParameter("_lz_currency"));
            this.pointAmount = Integer.valueOf(request.getParameter("_lz_point_amount"));
            onParse(request);
        } catch (Exception e) {
            throw new ParameterException(e);
        }

        onParse(request);
    }

    public void parseFromJson(String $json) {
        // FIXME convert json to Pay
//        paymentId = $json -> paymentId;
//        approvalId = $json -> approvalId;
//        userId = $json -> userId;
//        userName = $json -> userName;
//        userEmail = $json -> userEmail;
//        coinProductId = $json -> coinProductId;
//        coinProductName = $json -> coinProductName;
//        coin = $json -> coin;
//        amount = $json -> amount;
//        pointAmount = $json -> pointAmount;
//        currency = $json -> currency;
//        paymentType = $json -> paymentType;
//        store = $json -> store;
//        storeVersion = $json -> storeVersion;
//        locale = $json -> locale;
//        state = $json -> state;
//        createdAt = $json -> createdAt;
//        if (isset($json -> cohortData) && (is_object($json -> cohortData) || is_array($json -> cohortData))) {
//            cohort =[
//            "year" = >$json -> cohortData -> joinYear,
//                    "month", $json -> cohortData -> joinMonth,
//                    "week", $json -> cohortData -> joinWeek,
//                    "day", $json -> cohortData -> joinDate
//            );
//        }
//
//        // toss sub class. (PG detail info.)
//        pgId = getPGId();
    }

    public String getPaymentUrl() {

        // FIXME returnTo 가 null 이면 paramString 만들 필요 없음. empty string 처리로 대체. URL 다시 만들어야 함.
        // pay 관련은 모두 https
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("https").host(lezhinPgUrl).path(pgId + "/pay")
                .queryParam("isMobile", Optional.ofNullable(isMobile).orElse(Boolean.TRUE))
                .queryParam("isApp", Optional.ofNullable(isApp).orElse(Boolean.TRUE))
                .queryParam("locale", locale)
                .queryParam("returnTo", Optional.ofNullable(returnToUrl).orElse(""))
                .build()
                .encode();

        return uriComponents.toUriString();

//        String $url =  "$this->lezhinPgUrl/$this->pgId/pay?isMobile=".var_export($this->isMobile, true)."&isApp="
//            .var_export($this->isApp, true)."&locale=$this->locale";
//        if ($this->returnToUrl) {
//            $url .= "&returnTo=".urlencode($this->returnToUrl);
//        }
//        return $url;
    }

    public String getAuthenticationUrl() {
        // FIXME returnTo 가 null 이면 paramString 만들 필요 없음. empty string 처리로 대체
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http").host(lezhinPgUrl).path(pgId + "/authentication")
                .queryParam("isMobile", Optional.ofNullable(isMobile).orElse(Boolean.TRUE))
                .queryParam("isApp", Optional.ofNullable(isApp).orElse(Boolean.TRUE))
                .queryParam("locale", locale)
                .queryParam("returnTo", Optional.ofNullable(returnToUrl).orElse(""))
                .build()
                .encode();

        return uriComponents.toUriString();
    }

    public String getFailUrl() {
        // FIXME returnTo 가 null 이면 paramString 만들 필요 없음. empty string 처리로 대체
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http").host(lezhinPgUrl).path(pgId + "/fail")
                .queryParam("isMobile", Optional.ofNullable(isMobile).orElse(Boolean.TRUE))
                .queryParam("isApp", Optional.ofNullable(isApp).orElse(Boolean.TRUE))
                .queryParam("locale", locale)
                .queryParam("returnTo", Optional.ofNullable(returnToUrl).orElse(""))
                .build()
                .encode();

        return uriComponents.toUriString();
    }

    public String getResultUrl(String host) {

        Long $paymentId = Optional.ofNullable(paymentId).orElse(Long.valueOf(0L));
        Long $userId = Optional.ofNullable(userId).orElse(Long.valueOf(0L));
        Boolean $isMobile = Optional.ofNullable(isMobile).orElse(Boolean.TRUE);
        Boolean $isApp = Optional.ofNullable(isApp).orElse(Boolean.TRUE);
        String $returnTo = Optional.ofNullable(returnToUrl).orElse("");
        String $lang = Optional.ofNullable(locale).orElse(LezhinLocale.KO_KR).getLanguageCode().toLowerCase();

        // FIXME
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("https").host(host).path($lang + "/pay/" + paymentId + "/result")
                .queryParam("__u", userId)
                .queryParam("isMobile", Optional.ofNullable(isMobile).orElse(Boolean.TRUE))
                .queryParam("isApp", Optional.ofNullable(isApp).orElse(Boolean.TRUE))
                .queryParam("locale", locale)
                .queryParam("returnTo", Optional.ofNullable(returnToUrl).orElse(""))
                .build()
                .encode();

        return uriComponents.toUriString();

//        $url = "$host/$lang/pay/$paymentId/result?__u=$userId&isMobile=$isMobile&isApp=$isApp";
//        if (!empty($returnTo)) {
//            $encodeUrl = urlencode($returnTo);
//            $url .= "&returnTo=$encodeUrl";
//        }
//        return $url;
    }

    //-------------


    public String ulrDecode(String url) {
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            throw new ParameterException(String.format("Failed to urlDecode [%s]", url));
        }
    }

    public Map<String, Object> array_merge(@NotNull final Map<String, Object> args1, final Map<String, Object> args2) {
        Map<String, Object> merged = new HashMap<>(args1);
        // 나중에 추가된 것
        Optional.ofNullable(args2).orElse(new HashMap<>()).forEach((k, v) -> merged.merge(k, v, (o1, o2) -> o2));
        return merged;
    }


}
