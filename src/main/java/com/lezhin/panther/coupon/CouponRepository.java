package com.lezhin.panther.coupon;

import com.lezhin.constant.PGCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author taemmy
 * @since 2018. 8. 1.
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findAllByPgCompanyAndRequestId(PGCompany pg, String requestId);
    Optional<Coupon> findByCouponId(String couponId);
}
