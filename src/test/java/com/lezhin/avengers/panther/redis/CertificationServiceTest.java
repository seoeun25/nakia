package com.lezhin.avengers.panther.redis;

import com.lezhin.avengers.panther.CertificationService;
import com.lezhin.avengers.panther.model.Certification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
    private CertificationService certificationService;

    // TODO https://stackoverflow.com/questions/32524194/embedded-redis-for-spring-boot
//    @Test
//    public void testSaveAndGet() {
//        Certification certification = new Certification();
//        certification.setUserId(123L);
//        certification.setName("azrael");
//        certification.setCI("CI_ZZZZ");
//
//        certificationService.saveCertification(certification);
//
//        try {
//            Thread.sleep(500);
//        } catch (Exception e) {
//
//        }
//        Certification result = certificationService.getCertification(123L);
//        logger.info("result = {}", result);
//    }
//
//    @Test
//    public void testGet() {
//        Certification certification = certificationService.getCertification(123L);
//
//        logger.info("get certification = {}", certification);
//
//    }
}
