package com.lezhin.panther.coupon;

import com.lezhin.constant.PGCompany;
import com.lezhin.panther.exception.FraudException;
import com.lezhin.panther.exception.ParameterException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author taemmy
 * @since 2018. 7. 30.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(profiles = "test")
class CouponServiceTest {
    public static final Logger logger = LoggerFactory.getLogger(CouponServiceTest.class);

    @Autowired
    private CouponProductRepository couponProductRepository;
    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponService couponService;

    private Long couponProductId = 5233832242708480L;

    @BeforeAll
    public void before() {
        logger.info("external-coin-service before-all");
        // CouponProduct
        CouponProduct dummyProduct = CouponProduct.builder()
                .pgCompany(PGCompany.wincube)
                .groupId(couponProductId)
                .name("Coupon 10Coins")
                .coin(10)
                .bonusCoin(0)
                .point(0)
                .expireType(CouponProduct.ExpireType.interval)
                .expireValue(600_000L)
                .status(CouponProduct.Status.open)
                .createdAt(new Timestamp(Instant.now().toEpochMilli()))
                .build();
        couponProductRepository.save(dummyProduct);
        CouponProduct dummyProduct2 = CouponProduct.builder()
                .pgCompany(PGCompany.wincube)
                .groupId(couponProductId)
                .name("Coupon 10Coins")
                .coin(10)
                .bonusCoin(0)
                .point(0)
                .expireType(CouponProduct.ExpireType.interval)
                .expireValue(600_000L)
                .status(CouponProduct.Status.close)
                .createdAt(new Timestamp(Instant.now().toEpochMilli()))
                .build();
        couponProductRepository.save(dummyProduct2);

        // Coupon
        Coupon issuedDummyCoupon = Coupon.builder()
                .pgCompany(PGCompany.wincube)
                .requestId("ISSUE-123456")
                .groupId(couponProductId)
                .couponId("HPMK-ZBRP-IXXF-YWKY")
                .expiredAt(1533116336616L)
                .status(Coupon.Status.issue)
                .createdAt(new Timestamp(Instant.now().toEpochMilli()))
                .issuedAt(new Timestamp(Instant.now().toEpochMilli()))
                .build();
        couponRepository.save(issuedDummyCoupon);
        // Coupon
        Coupon discardDummyCoupon = Coupon.builder()
                .pgCompany(PGCompany.wincube)
                .requestId("DISCARD-123456")
                .groupId(couponProductId)
                .couponId("HPMK-ZBRP-IXXF-YWKA")
                .expiredAt(1533116336616L)
                .status(Coupon.Status.discard)
                .createdAt(new Timestamp(Instant.now().toEpochMilli()))
                .issuedAt(new Timestamp(Instant.now().toEpochMilli()))
                .discardedAt(new Timestamp(Instant.now().toEpochMilli()))
                .build();
        couponRepository.save(discardDummyCoupon);
    }

    @Test
    public void test_get_external_coupon_products() {
        logger.info("test-get-coin-product start");
        List<CouponProduct> products = couponService.findCouponProducts(PGCompany.wincube);

        assertNotNull(products);
        assertEquals(1, products.size());

        List<CouponProduct> products2 = couponService.findCouponProducts(PGCompany.galaxia);
        assertNotNull(products2);
        assertEquals(0, products2.size());
    }

    @Test
    public void test_get_coupon_product() {
        CouponProduct product = couponService.findCouponProduct(PGCompany.wincube, couponProductId);

        assertNotNull(product);
        logger.info("product: {}", product.toString());
        assertEquals(couponProductId, product.getGroupId());
        assertEquals(CouponProduct.Status.open, product.getStatus());
    }

    @Test
    public void test_get_coupon_product_parameter_exception() {
        // case: productId is null
        assertThrows(ParameterException.class, () -> couponService.findCouponProduct(PGCompany.wincube, null));
        // case: CouponProduct not found
        assertThrows(ParameterException.class, () -> couponService.findCouponProduct(PGCompany.wincube, 15233832242708480L));
    }

    @Test
    public void test_get_coupon() {
        String requestId = "ISSUE-123456";
        String couponId = "HPMK-ZBRP-IXXF-YWKY";
        Coupon coupon = couponService.findCoupon(PGCompany.wincube, requestId, couponId);

        assertNotNull(coupon);
        logger.info("coupon: {}", coupon.toString());
    }

    @Test
    public void test_get_coupon_not_found() {
        String requestId = "ISSUE-123456";
        String couponId = "HPMK-ZBRP-IXXF-YWKY";
        assertThrows(ParameterException.class, ()
                -> couponService.findCoupon(PGCompany.wincube, requestId, null));
        assertThrows(ParameterException.class, ()
                -> couponService.findCoupon(PGCompany.wincube, requestId, "HPMK-ZBRP-IXXF-YWKB"));
        assertThrows(ParameterException.class, ()
                -> couponService.findCoupon(PGCompany.galaxia, requestId, couponId));
        assertThrows(ParameterException.class, ()
                -> couponService.findCoupon(PGCompany.wincube, "ISSUE-1234567", couponId));
    }

    @Test
    public void test_check_issued() {
        // not exist Coupon by requestId
        couponService.checkIssued(PGCompany.wincube, "REQ-123456");
    }

    @Test
    public void test_check_issued_fail() {
        // case: Coupon.Status == issue (exception)
        assertThrows(FraudException.class, () -> couponService.checkIssued(PGCompany.wincube, "ISSUE-123456"));
        // case: Coupon.Status == discard (exception)
        assertThrows(FraudException.class, () -> couponService.checkIssued(PGCompany.wincube, "DISCARD-123456"));
    }

    @Test
    public void test_convert_expired_at() {
        Long interval = 10 * 60 * 1000L;
        CouponProduct couponProduct = new CouponProduct();
        couponProduct.setExpireType(CouponProduct.ExpireType.interval);
        couponProduct.setExpireValue(interval);

        Long expiredAt = couponService.convertExpiredAt(couponProduct);
        logger.info("expiredAt: {}", expiredAt);

        assertNotNull(expiredAt);
    }

    @Test
    public void test_save_coupon() {
        Coupon coupon = Coupon.builder()
                .pgCompany(PGCompany.wincube)
                .requestId("A-12345")
                .groupId(couponProductId)
                .status(Coupon.Status.attempt)
                .createdAt(new Timestamp(Instant.now().toEpochMilli()))
                .build();
        coupon = couponService.persist(coupon);

        assertNotNull(coupon);
        logger.info("coupon: {}", coupon.toString());
        assertNotNull(coupon.getId());
    }

}