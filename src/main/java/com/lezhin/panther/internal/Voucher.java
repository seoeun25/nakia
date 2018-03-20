package com.lezhin.panther.internal;

import com.lezhin.beans.entity.common.LezhinLocale;
import com.lezhin.constant.LezhinPlatform;
import com.lezhin.constant.LezhinStore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author seoeun
 * @since 2018.03.15
 */
@JsonIgnoreProperties(value = {"usageRestriction"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Getter
@ToString
public class Voucher implements Serializable {

    private Long id;
    private Long userId;
    private String coinType;
    private Long amount;
    private Long orgAmount;
    @JsonIgnore
    private Long expiredAt;
    @JsonIgnore
    private Boolean available;
    @JsonIgnore
    private Boolean refund;
    @JsonIgnore
    private Long companyEventId;
    @JsonIgnore
    private Long usageRestrictionId;
    @JsonIgnore
    private Long purchaseId;
    @JsonIgnore
    private Long paymentId;
    @JsonIgnore
    private Long coinProductId;
    @JsonIgnore
    private LezhinStore store;
    @JsonIgnore
    private LezhinLocale locale;
    @JsonIgnore
    private LezhinPlatform platform;

    @JsonIgnore
    private Boolean internal;

    @JsonIgnore
    private Long updatedAt;
    @JsonIgnore
    private Long createdAt;


}
