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
import javax.persistence.Index;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @author taemmy
 * @since 2018. 7. 30.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name = "coupon")
@Table(indexes = {@Index(name = "coupon_pg_request_id", columnList = "pg_company, request_id"),
        @Index(name = "coupon_lz_coupon_id", columnList = "coupon_id")})
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "pg_company", nullable = false, length = 50)
    private PGCompany pgCompany;

    @Column(name = "request_id", nullable = false)
    private String requestId;       // external-company unique id

    @Column(name = "group_id")
    private Long groupId;           // IssueCoupon.Id

    @Column(name = "coupon_id", length = 100)
    private String couponId;        // Coupon.id

    @Column(name = "expired_at")
    private Long expiredAt;         // Coupon.expiredAt

    @Column(name = "meta", length = 1000)
    private String meta;            // internal-coupon-receipt

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;    // request 발생일자

    @Column(name = "issued_at")
    private Timestamp issuedAt;     // internal-coupon 발급일자

    @Column(name = "discarded_at")
    private Timestamp discardedAt;  // discard 처리일자

    public enum Status {
        attempt, issue, discard
    }
}
