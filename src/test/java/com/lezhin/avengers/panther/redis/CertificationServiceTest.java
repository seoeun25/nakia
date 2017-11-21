package com.lezhin.avengers.panther.redis;

import com.lezhin.avengers.panther.CertificationService;
import com.lezhin.avengers.panther.happypoint.HappyPointExecutor;
import com.lezhin.avengers.panther.model.Certification;
import com.lezhin.avengers.panther.model.HappypointAggregator;
import com.lezhin.avengers.panther.util.DateUtil;

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

/**
 * @author seoeun
 * @since 2017.11.13
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class CertificationServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(CertificationServiceTest.class);

    @Autowired
    private static EmbededRedis redis;

    @Autowired
    private CertificationService certificationService;

    @Autowired
    private HappyPointExecutor happyPointExecutor;

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

        certificationService.saveCertification(certification);

        try {
            Thread.sleep(500);
        } catch (Exception e) {

        }
        Certification result = certificationService.getCertification(123L);
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
        certificationService.addPaymentResult(aggregator1);

        HappypointAggregator aggregator2 = new HappypointAggregator(mbrNo, "201711", new Integer(10));
        certificationService.addPaymentResult(aggregator2);

        Thread.sleep(100);
        HappypointAggregator result = certificationService.getPaymentResult(mbrNo, "201711");
        assertNotNull(result);
        assertEquals(mbrNo, result.getMbrNo());
        assertEquals("201711", result.getYm());
        assertEquals(15, result.getPointSum().intValue()); // sum = 15

        // 12월
        HappypointAggregator aggregator12 = new HappypointAggregator(mbrNo, "201712", new Integer(3));
        certificationService.addPaymentResult(aggregator12);
        Thread.sleep(100);
        HappypointAggregator result12 = certificationService.getPaymentResult(mbrNo, "201712");
        assertNotNull(result12);
        assertEquals(mbrNo, result12.getMbrNo());
        assertEquals("201712", result12.getYm());
        assertEquals(3, result12.getPointSum().intValue());
    }



}
