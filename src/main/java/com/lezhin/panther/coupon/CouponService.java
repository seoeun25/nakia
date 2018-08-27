package com.lezhin.panther.coupon;

import com.lezhin.constant.PGCompany;
import com.lezhin.panther.exception.FraudException;
import com.lezhin.panther.exception.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author taemmy
 * @since 2018. 7. 30.
 */
@Service
public class CouponService {
    private final static Logger logger = LoggerFactory.getLogger(CouponService.class);

    private CouponProductRepository couponProductRepository;
    private CouponRepository couponRepository;

    public CouponService(final CouponProductRepository couponProductRepository,
                         final CouponRepository couponRepository) {
        this.couponProductRepository = couponProductRepository;
        this.couponRepository = couponRepository;
    }

    @Transactional(value = "pantherTransactionManager", readOnly = true)
    public List<CouponProduct> findCouponProducts(final PGCompany pg) {
        List<CouponProduct> all = couponProductRepository.findAllByPgCompany(pg);
        return all.stream()
                .filter(p -> CouponProduct.Status.open.equals(p.getStatus()))
                .collect(Collectors.toList());
    }

    @Transactional(value = "pantherTransactionManager", readOnly = true)
    public CouponProduct findCouponProduct(final PGCompany pg, final Long groupId) {
        List<CouponProduct> products = findCouponProducts(pg);
        Optional<CouponProduct> targetProduct = products.stream()
                .filter(p -> p.getGroupId().equals(groupId))
                .findFirst();

        targetProduct.orElseThrow(() -> new ParameterException(pg, "Not supported CouponProduct. productId=" + groupId));
        return targetProduct.get();
    }

    @Transactional(value = "pantherTransactionManager", readOnly = true)
    public Coupon findCoupon(final PGCompany pg, final String requestId, final String couponId) {
        // coupon-id 는 lezhin.Coupon unique
        Optional<Coupon> optional = couponRepository.findByCouponId(couponId);

        optional.orElseThrow(() -> new ParameterException(pg, "Not found Coupon. couponId=" + couponId));
        Coupon coupon = optional.get();
        if (!coupon.getPgCompany().equals(pg) || !coupon.getRequestId().equals(requestId)) {
            throw new ParameterException(pg, String.format("Doesn't match pg-couponId. pg=%s, couponId=%s, requestId=%s", pg, couponId, requestId));
        }
        logger.debug("get coupon - pg={}, requestId={}, couponId={}, status={}",
                coupon.getPgCompany(), coupon.getRequestId(), coupon.getCouponId(), coupon.getStatus());
        return coupon;
    }

    @Transactional(value = "pantherTransactionManager", readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
    public void checkIssued(final PGCompany pg, final String requestId) {
        List<Coupon> coupons = couponRepository.findAllByPgCompanyAndRequestId(pg, requestId);
        Optional<Coupon> issuedCoupon = coupons.stream()
                .filter(c -> c.getStatus().equals(Coupon.Status.issue) || c.getStatus().equals(Coupon.Status.discard))
                .findFirst();

        if (issuedCoupon.isPresent()) {
            Coupon c = issuedCoupon.get();
            throw new FraudException(pg, String.format("Coupon already issue/discard. pg=%s, requestId=%s, couponId=%s, status=%s",
                    pg, c.getRequestId(), c.getCouponId(), c.getStatus()));
        }
    }

    @Transactional(value = "pantherTransactionManager")
    public Coupon persist(final Coupon coupon) {
        try {
            return couponRepository.save(coupon);
        } catch (Throwable e) {
            logger.error("Fail to persist Coupon={}", coupon.toString());
            throw e;
        }
    }

    public Long convertExpiredAt(final CouponProduct couponProduct) {
        if (CouponProduct.ExpireType.interval.equals(couponProduct.getExpireType())) {
            // calculate expiredAt
            return Instant.now().plus(couponProduct.getExpireValue(), ChronoUnit.MILLIS).toEpochMilli();
        }

        // 리딤만료기한이 없는 경우, 기본 180일
        return couponProduct.getExpireValue() != null ?
                couponProduct.getExpireValue() : Instant.now().plus(180, ChronoUnit.DAYS).toEpochMilli();
    }


}
