package com.lezhin.panther.internal;

import com.lezhin.beans.entity.common.LezhinLocale;
import com.lezhin.constant.LezhinCurrency;
import com.lezhin.constant.LezhinObjectType;
import com.lezhin.constant.LezhinPlatform;
import com.lezhin.constant.LezhinStore;
import com.lezhin.panther.util.DateSerializer;
import com.lezhin.panther.util.DateUtil;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * @author seoeun
 * @since 2018.03.14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Getter
@ToString
public class Purchase implements Serializable {
    private Long id;
    private Long userId;
    private Long paymentId;
    private LezhinPlatform platform;
    private LezhinStore store;
    private Integer coin;
    private Integer bonusCoin;
    private Boolean revenueShare;
    private Integer point;
    private String type; //PurchaseType
    private LezhinObjectType lezhinObjectType;
    private Map<String, String> meta;
    @JsonSerialize(using = DateSerializer.class)
    private Long createdAt;
    private String title;
    private Long purchaseGroup;
    private LezhinLocale lezhinLocale;
    private Long companyEventId;
    private Boolean canceled;
    private String companyName;
    private String companyEventName;
    private Long voucherId;
    private Float price;
    private LezhinCurrency currency;
    private String idApproval;
    private Integer balanceCoin;
    private Integer balanceBonusCoin;
    private Integer balancePoint;

    @JsonProperty("user")
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @JsonProperty("idPayment")
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
}
