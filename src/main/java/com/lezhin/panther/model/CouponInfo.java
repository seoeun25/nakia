package com.lezhin.panther.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lezhin.panther.internal.LzCoupon;
import lombok.Data;

/**
 * @author taemmy
 * @since 2018. 7. 30.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CouponInfo {
    private String requestId;
    private Long productId;
    private String couponId;
    private String name;
    private int coin;
    private int bonusCoin;
    private int point;
    private Long expiredAt;
    private String status;

    public CouponInfo() {
    }

    public CouponInfo(String requestId, LzCoupon lzCoupon) {
        this.requestId = requestId;
        this.productId = lzCoupon.getGroupId();
        this.couponId = lzCoupon.getId();
        this.name = lzCoupon.getName();
        this.coin = lzCoupon.getCoin();
        this.bonusCoin = lzCoupon.getBonusCoin();
        this.point = lzCoupon.getPoint();
        this.expiredAt = lzCoupon.getExpiredAt();
        this.status = lzCoupon.getCouponState();
    }
}
