package com.lezhin.panther.pg.wincube;

import com.lezhin.constant.PGCompany;
import com.lezhin.panther.coupon.Coupon;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.coupon.CouponProduct;
import com.lezhin.panther.coupon.CouponService;
import com.lezhin.panther.internal.LzCoupon;
import com.lezhin.panther.internal.InternalCouponService;
import com.lezhin.panther.model.CouponInfo;
import com.lezhin.panther.model.CouponProductInfo;
import com.lezhin.panther.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author taemmy
 * @since 2018. 7. 30.
 */
@Service
public class WincubeService {
    public static final Logger logger = LoggerFactory.getLogger(WincubeService.class);

    private CouponService couponService;
    private InternalCouponService internalCouponService;

    public WincubeService(final CouponService couponService,
                          final InternalCouponService internalCouponService) {
        this.couponService = couponService;
        this.internalCouponService = internalCouponService;
    }

    public List<CouponProductInfo> getProducts(final PGCompany pg) {
        List<CouponProduct> products = couponService.findCouponProducts(pg);

        return products.stream()
                .map(p -> CouponProductInfo.builder()
                        .productId(p.getGroupId())
                        .name(p.getName())
                        .status(p.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    public CouponInfo issue(final PGCompany pg, final CouponInfo reqCouponInfo) {
        // parameter validate
        precondition(reqCouponInfo);

        // check issued
        couponService.checkIssued(pg, reqCouponInfo.getRequestId());

        // find coupon-product
        CouponProduct couponProduct = couponService.findCouponProduct(pg, reqCouponInfo.getProductId());

        // save attempt coupon
        Coupon pCoupon = Coupon.builder()
                .pgCompany(pg)
                .requestId(reqCouponInfo.getRequestId())
                .groupId(reqCouponInfo.getProductId())
                .expiredAt(couponService.convertExpiredAt(couponProduct))
                .status(Coupon.Status.attempt)
                .createdAt(new Timestamp(Instant.now().toEpochMilli()))
                .build();
        pCoupon = couponService.persist(pCoupon);

        // request issue coupon(cms)
        LzCoupon lzCoupon = new LzCoupon();
        lzCoupon.setExpiredAt(pCoupon.getExpiredAt());
        lzCoupon = internalCouponService.issue(pg, reqCouponInfo.getProductId(), lzCoupon);

        // validate coupon
        validate(pg, lzCoupon);

        // update issue coupon(panther)
        pCoupon.setCouponId(lzCoupon.getId());
        pCoupon.setExpiredAt(lzCoupon.getExpiredAt());
        pCoupon.setStatus(Coupon.Status.issue);
        pCoupon.setIssuedAt(new Timestamp(Instant.now().toEpochMilli()));
        pCoupon.setMeta(JsonUtil.toJson(lzCoupon));
        couponService.persist(pCoupon);

        return new CouponInfo(reqCouponInfo.getRequestId(), lzCoupon);
    }

    public CouponInfo get(final PGCompany pg, final CouponInfo reqCouponInfo) {
        // parameter validate
        precondition(reqCouponInfo);

        // find issued coupon by panther
        Coupon pCoupon = couponService.findCoupon(pg, reqCouponInfo.getRequestId(), reqCouponInfo.getCouponId());

        // request get coupon(cms)
        LzCoupon lzCoupon = internalCouponService.get(pg, pCoupon.getCouponId());
        return new CouponInfo(reqCouponInfo.getRequestId(), lzCoupon);
    }

    public CouponInfo discard(final PGCompany pg, final CouponInfo reqCouponInfo) {
        // parameter validate
        precondition(reqCouponInfo);

        // find issued coupon by panther
        Coupon pCoupon = couponService.findCoupon(pg, reqCouponInfo.getRequestId(), reqCouponInfo.getCouponId());

        // request get coupon(cms)
        LzCoupon lzCoupon = internalCouponService.get(pg, pCoupon.getCouponId());

        // validate coupon
        validate(pg, lzCoupon);

        // check coupon state != not_used
        if(!"not_used".equals(lzCoupon.getCouponState())) {
            throw new PreconditionException(PGCompany.wincube, "coupon state isn't not_used.");
        }

        // request discard coupon(cms)
        lzCoupon = internalCouponService.discard(pg, reqCouponInfo.getCouponId());

        // update discard coupon(panther)
        pCoupon.setStatus(Coupon.Status.discard);
        pCoupon.setDiscardedAt(new Timestamp(Instant.now().toEpochMilli()));
        pCoupon.setMeta(JsonUtil.toJson(lzCoupon));
        couponService.persist(pCoupon);
        return new CouponInfo(pCoupon.getRequestId(), lzCoupon);
    }

    void precondition(final CouponInfo reqCouponInfo) {
        Optional.ofNullable(reqCouponInfo.getProductId()).orElseThrow(()
                -> new ParameterException(PGCompany.wincube, "productId can not be null"));
        Optional.ofNullable(reqCouponInfo.getRequestId()).orElseThrow(()
                -> new ParameterException(PGCompany.wincube, "requestId can not be null"));
    }

    void validate(final PGCompany pg, final LzCoupon lzCoupon) {
        // validate coupon(cms)
        Optional.ofNullable(lzCoupon).orElseThrow(() -> new PantherException(pg, "coupon can not be null"));
        Optional.ofNullable(lzCoupon.getId()).orElseThrow(() -> new PantherException(pg, "coupon.id can not be null"));
        Optional.ofNullable(lzCoupon.getCouponState()).orElseThrow(() -> new PantherException(pg, "coupon.state can not be null"));
    }

}
