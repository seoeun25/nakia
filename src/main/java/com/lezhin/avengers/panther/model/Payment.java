package com.lezhin.avengers.panther.model;

import com.lezhin.beans.entity.common.LezhinLocale;
import com.lezhin.constant.LezhinCurrency;
import com.lezhin.constant.LezhinStore;
import com.lezhin.constant.PaymentState;
import com.lezhin.constant.PaymentType;
import com.lezhin.model.CohortData;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

/**
 * Payment를 표현하는 entity. Lezhin Server의 InternalPaymentView
 *
 * @author seoeun
 * @since 2017.10.24
 */
@Component // TODO entity
@JsonSerialize
public class Payment<T extends PGPayment> implements Serializable{

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

    // panther
    protected String externalStoreProductId;
    protected T pgPayment;

    public Payment() {

    }

    @VisibleForTesting
    public Payment(Long paymentId) {
        this.paymentId = paymentId;
    }

    // FIXME IntenalPaymentView로 로딩했을 때 Payment 생성 ???
//    public Payment(String environment, String lezhinPgUrl, Long paymentId,
//                   Boolean isMobile, Boolean isApp, String returnToUrl, LezhinLocale locale) {
//        this.pgId = getPGId();
//        this.environment = environment;
//        this.isMobile = isMobile;
//        this.isApp = isApp;
//        this.lezhinPgUrl = lezhinPgUrl;
//        this.paymentId = paymentId;
//        if (returnToUrl != null) {
//            this.returnToUrl = ulrDecode(returnToUrl);
//        }
//        this.locale = locale;
//    }

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

    public String getExternalStoreProductId() {
        return externalStoreProductId;
    }

    public void setExternalStoreProductId(String externalStoreProductId) {
        this.externalStoreProductId = externalStoreProductId;
    }

    @JsonProperty("meta")
    public T getPgPayment() {
        return pgPayment;
    }

    public void setPgPayment(T pgPayment) {
        this.pgPayment = pgPayment;
    }

}
