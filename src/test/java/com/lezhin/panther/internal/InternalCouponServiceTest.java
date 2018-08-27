package com.lezhin.panther.internal;

import com.lezhin.constant.PGCompany;
import com.lezhin.panther.exception.HttpClientException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author taemmy
 * @since 2018. 7. 30.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
class InternalCouponServiceTest {
    public static final Logger logger = LoggerFactory.getLogger(InternalCouponService.class);

    @Autowired
    private InternalCouponService internalCouponService;

    private String couponId = "NIWP-RMAL-AFJE-JZYL";
    private Long groupId = 5233832242708480L;

    @Test
    public void test_get_coupon() {
        // [beta] test-coupon-id: NIWP-RMAL-AFJE-JZYL
        LzCoupon lzCoupon = internalCouponService.get(PGCompany.wincube, couponId);

        assertNotNull(lzCoupon);
        logger.info("coupon: {}", lzCoupon.toString());
    }

    @Test
    public void test_get_coupon_not_found() {
        // 발행되지 않은 coupon-id 조회한 경우
        assertThrows(HttpClientException.class, () -> internalCouponService.get(PGCompany.wincube, "NIWP-RMAL-AFJE-"));
    }

    @Test
    public void test_issue_coupon() {
        LzCoupon requestLzCoupon = new LzCoupon();
        requestLzCoupon.setExpiredAt(1535673600000L);
        LzCoupon lzCoupon = internalCouponService.issue(PGCompany.wincube, groupId, requestLzCoupon);

        assertNotNull(lzCoupon);
        logger.info("coupon: {}", lzCoupon.toString());
        assertNotNull(lzCoupon.getId());
        assertNotNull(lzCoupon.getExpiredAt());
        assertEquals("not_used", lzCoupon.getCouponState());
    }

    @Test
    public void test_issue_coupon_bad_request() {
        // 생성되지 않은 coupon-group-id 로 발행을 요청하는 경우
        LzCoupon requestLzCoupon = new LzCoupon();
        requestLzCoupon.setExpiredAt(1535673600000L);
        assertThrows(HttpClientException.class, () -> internalCouponService.issue(PGCompany.wincube, groupId * 100, requestLzCoupon));
    }

    @Test
    public void test_discard_coupon() {
        LzCoupon lzCoupon = internalCouponService.discard(PGCompany.wincube, couponId);
        assertNotNull(lzCoupon);
        logger.info("coupon: {}", lzCoupon.toString());
        assertEquals(couponId, lzCoupon.getId());
        assertEquals("manual_expired", lzCoupon.getCouponState());
    }
}