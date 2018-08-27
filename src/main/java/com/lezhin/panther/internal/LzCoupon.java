package com.lezhin.panther.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author taemmy
 * @since 2018. 7. 30.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class LzCoupon {
    private String id;
    private String name;
    private Long groupId;
    private int coin;
    private int bonusCoin;
    private int point;
    private String platform;
    private String redeemPlatform;
    private String redeemStore;
    private Long userId;
    private Long expiredAt;
    private Long idPurchase;
    private Long redeemedAt;
    private String couponState;
    private Long companyEventPresentId;
}
