package com.lezhin.panther.coupon;

import com.lezhin.constant.PGCompany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

/**
 * @author taemmy
 * @since 2018. 7. 30.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name = "coupon_product")
public class CouponProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "pg_company", nullable = false)
    private PGCompany pgCompany;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "coin")
    private int coin;

    @Column(name = "bonus_coin")
    private int bonusCoin;

    @Column(name = "point")
    private int point;

    @Enumerated(EnumType.STRING)
    @Column(name = "expire_type", nullable = false)
    private ExpireType expireType;

    @Column(name = "expire_value", nullable = false)
    private Long expireValue;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    public enum Status {
        open, close, pause
    }

    public enum ExpireType {
        interval, expiredAt
    }
}
