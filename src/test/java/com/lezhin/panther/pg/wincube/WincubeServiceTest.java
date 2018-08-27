package com.lezhin.panther.pg.wincube;

import com.lezhin.constant.PGCompany;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.internal.LzCoupon;
import com.lezhin.panther.model.CouponInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author taemmy
 * @since 2018. 8. 1.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
class WincubeServiceTest {
    public static final Logger logger = LoggerFactory.getLogger(WincubeServiceTest.class);

    private Long couponProductId = 5233832242708480L;

    @Autowired
    private WincubeService wincubeService;

    @Test
    public void test_precondition() {
        CouponInfo reqCouponInfo = new CouponInfo();
        reqCouponInfo.setRequestId("W-123456789012345");
        reqCouponInfo.setProductId(couponProductId);

        // pass precondition
        wincubeService.precondition(reqCouponInfo);
    }

    @Test
    public void test_precondition_parameter_null() {
        CouponInfo reqCouponInfo2 = new CouponInfo();
        reqCouponInfo2.setRequestId("W-123456789012345");

        // case: product-id can not be null
        assertThrows(ParameterException.class, () -> wincubeService.precondition(reqCouponInfo2));

        CouponInfo reqCouponInfo3 = new CouponInfo();
        reqCouponInfo3.setProductId(couponProductId);

        // case: request-id can not be null
        assertThrows(ParameterException.class, () -> wincubeService.precondition(reqCouponInfo3));
    }

    @Test
    public void test_validate() {
        LzCoupon lzCoupon = new LzCoupon();
        lzCoupon.setId("XOHK-WQNY-FYWC-LMGP");
        lzCoupon.setCouponState("not_used");

        // pass validate
        wincubeService.validate(PGCompany.wincube, lzCoupon);
    }

    @Test
    public void test_validate_panther_exception() {
        // case: coupon is null
        assertThrows(PantherException.class, () -> wincubeService.validate(PGCompany.wincube, null));

        // case: coupon.id is null
        LzCoupon lzCoupon2 = new LzCoupon();
        assertThrows(PantherException.class, () -> wincubeService.validate(PGCompany.wincube, lzCoupon2));
    }
}
