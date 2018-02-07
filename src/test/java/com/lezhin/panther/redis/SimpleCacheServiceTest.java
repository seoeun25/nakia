package com.lezhin.panther.redis;

import com.lezhin.avengers.panther.model.HappypointAggregator;
import com.lezhin.constant.LezhinStore;
import com.lezhin.constant.PaymentType;
import com.lezhin.panther.SimpleCacheService;
import com.lezhin.panther.exception.SessionException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.pg.lguplus.LguplusPayment;
import com.lezhin.panther.model.Certification;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.util.DateUtil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author seoeun
 * @since 2017.11.13
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class SimpleCacheServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCacheServiceTest.class);

    @Autowired
    private static EmbededRedis redis;

    @Autowired
    private SimpleCacheService simpleCacheService;

    @BeforeClass
    public void setupClass() throws Exception {
        redis.startRedis();
    }

    @AfterClass
    public void teardDown() {
        if (redis != null) {
            redis.stopRedis();
        }
    }

    @Test
    public void testSaveAndGet() {
        Certification certification = new Certification();
        certification.setUserId(123L);
        certification.setName("azrael");
        certification.setCI("CI_ZZZZ");

        simpleCacheService.saveCertification(certification);

        try {
            Thread.sleep(500);
        } catch (Exception e) {

        }
        Certification result = simpleCacheService.getCertification(123L);
        logger.info("result = {}", result.toString());
        assertEquals(123L, result.getUserId().longValue());
        assertEquals("azrael", result.getName());
        assertEquals("CI_ZZZZ", result.getCI());
    }

    @Test
    public void testHappypointAtestHappypointAggregatorggregator() throws InterruptedException {

        String mbrNo = "mbrNo_abc";
        String trxDt = "20171114";
        Integer point = 5;

        HappypointAggregator aggregator = new HappypointAggregator(mbrNo,
                DateUtil.format(Instant.now().toEpochMilli(), DateUtil.ASIA_SEOUL_ZONE, "yyyyMM"),
                new Integer(10));
        logger.info("aggregator =  {}", aggregator.toString());


        Instant trxInstant = DateUtil.toInstantFromDate(trxDt, "yyyyMMdd", DateUtil.ASIA_SEOUL_ZONE);
        String ym = DateUtil.format(trxInstant.toEpochMilli(), DateUtil.ASIA_SEOUL_ZONE, "yyyyMM");

        // 11월
        HappypointAggregator aggregator1 = new HappypointAggregator(mbrNo, ym, point);
        simpleCacheService.saveHappypointAggregator(aggregator1);

        HappypointAggregator aggregator2 = new HappypointAggregator(mbrNo, "201711", new Integer(10));
        simpleCacheService.saveHappypointAggregator(aggregator2);

        Thread.sleep(100);
        HappypointAggregator result = simpleCacheService.getHappypointAggregator(mbrNo, "201711");
        assertNotNull(result);
        assertEquals(mbrNo, result.getMbrNo());
        assertEquals("201711", result.getYm());
        assertEquals(15, result.getPointSum().intValue()); // sum = 15

        // 12월
        HappypointAggregator aggregator12 = new HappypointAggregator(mbrNo, "201712", new Integer(3));
        simpleCacheService.saveHappypointAggregator(aggregator12);
        Thread.sleep(100);
        HappypointAggregator result12 = simpleCacheService.getHappypointAggregator(mbrNo, "201712");
        assertNotNull(result12);
        assertEquals(mbrNo, result12.getMbrNo());
        assertEquals("201712", result12.getYm());
        assertEquals(3, result12.getPointSum().intValue());
    }

    @Test
    public void testRequestInfoSaveAndGet() {
        long paymentId = 123L;

        LguplusPayment lguPayment = LguplusPayment.builder()
                .CST_MID("9876")
                .LGD_OID(String.valueOf(paymentId))
                .LGD_AMOUNT("100")
                .build();
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setUserId(2L);
        payment.setAmount(Float.parseFloat("20"));
        payment.setCoinProductId(3L);
        payment.setPgPayment(lguPayment);
        payment.setStore(LezhinStore.web);
        payment.setLocale("ko-KR");
        payment.setPgCompany("lguplus");
        payment.setPaymentType(PaymentType.deposit);

        RequestInfo requestInfo = new RequestInfo.Builder(payment, "lguplus")
                .withToken("4ea0f867-ad9c-4ad7-b024-0b8c258f853d")
                .withUserId(2L).build();

        simpleCacheService.saveRequestInfo(requestInfo);
        try {
            Thread.sleep(500);
        } catch (Exception e) {

        }
        // Get
        RequestInfo resultRequest = simpleCacheService.getRequestInfo(paymentId);
        logger.info("result = {}", resultRequest.toString());
        assertEquals(2L, resultRequest.getUserId().longValue());
        assertEquals("4ea0f867-ad9c-4ad7-b024-0b8c258f853d", resultRequest.getToken());
        assertEquals(Executor.Type.LGUDEPOSIT, resultRequest.getExecutorType());
        assertEquals(2L, resultRequest.getPayment().getUserId().longValue());
        assertEquals(20F, resultRequest.getPayment().getAmount().floatValue());
        assertEquals(3L, resultRequest.getPayment().getCoinProductId().longValue());
        assertEquals(paymentId, resultRequest.getPayment().getPaymentId().longValue());
        com.lezhin.panther.pg.lguplus.LguplusPayment resultPgPayment = (com.lezhin.panther.pg.lguplus.LguplusPayment) resultRequest.getPayment().getPgPayment();
        assertEquals("9876", resultPgPayment.getCST_MID());
        assertEquals("123", resultPgPayment.getLGD_OID());
        assertEquals("100", resultPgPayment.getLGD_AMOUNT());

    }

    /**
     * paymentId == null 이라 saving 실패
     */
    @Test
    public void testRequestInfoSaveFail() {
        LguplusPayment lguPayment = LguplusPayment.builder()
                .CST_MID("9876")
                .LGD_OID(null)
                .LGD_AMOUNT("100")
                .build();
        Payment payment = new Payment();
        payment.setPaymentId(null);
        payment.setUserId(2L);
        payment.setAmount(Float.parseFloat("20"));
        payment.setCoinProductId(3L);
        payment.setPgPayment(lguPayment);
        payment.setStore(LezhinStore.web);
        payment.setLocale("ko-KR");
        payment.setPgCompany("lguplus");
        payment.setPaymentType(PaymentType.deposit);

        RequestInfo requestInfo = new RequestInfo.Builder(payment, "lguplus")
                .withToken("4ea0f867-ad9c-4ad7-b024-0b8c258f853d")
                .withUserId(2L).build();

        try {
            simpleCacheService.saveRequestInfo(requestInfo);
            fail("Saving session should be failed");
        } catch (SessionException e) {
            
        }

    }


}
