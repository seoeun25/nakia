package com.lezhin.avengers.panther.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Map;

/**
 * InternalPayment에서 사용하는 meta
 *
 * @author seoeun
 * @since 2017.11.12
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Meta implements Serializable {

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

    private String ip;
    private String country;
    private String manual;
    private String paymentComplete;
    private String fail;
    private String unverified;
    private String rollback;
    private String approvalId;
    private String receipt;
    private Map<String, String> receiptMap;
    private String meta;
    private Map<String, String> metaMap;

    private String dynamicAmount = "0";

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getManual() {
        return manual;
    }

    public void setManual(String manual) {
        this.manual = manual;
    }

    public String getPaymentComplete() {
        return paymentComplete;
    }

    public void setPaymentComplete(String paymentComplete) {
        this.paymentComplete = paymentComplete;
    }

    public String getFail() {
        return fail;
    }

    public void setFail(String fail) {
        this.fail = fail;
    }

    public String getUnverified() {
        return unverified;
    }

    public void setUnverified(String unverified) {
        this.unverified = unverified;
    }

    public String getRollback() {
        return rollback;
    }

    public void setRollback(String rollback) {
        this.rollback = rollback;
    }

    public String getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public String getDynamicAmount() {
        return dynamicAmount;
    }

    public void setDynamicAmount(String dynamicAmount) {
        this.dynamicAmount = dynamicAmount;
    }

    public Map<String, String> getReceiptMap() {
        return receiptMap;
    }

    public void setReceiptMap(Map<String, String> receiptMap) {
        this.receiptMap = receiptMap;
    }

    public Map<String, String> getMetaMap() {
        return metaMap;
    }

    public void setMetaMap(Map<String,String> metaMap) {
        this.metaMap = metaMap;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }
}
