package com.lezhin.panther.coupon;

import com.lezhin.constant.PGCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author taemmy
 * @since 2018. 7. 30.
 */
@Repository
public interface CouponProductRepository extends JpaRepository<CouponProduct, Long> {
    List<CouponProduct> findAllByPgCompany(PGCompany pg);
}
