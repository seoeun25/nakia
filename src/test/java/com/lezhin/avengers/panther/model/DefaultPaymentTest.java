package com.lezhin.avengers.panther.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class DefaultPaymentTest {

    private static Logger logger = LoggerFactory.getLogger(DefaultPaymentTest.class);

    @Autowired
    @Qualifier("defaultPayment")
    private DefaultPayment payment;

    private Payment payment1;

    @Test
    public void testInstance() {
        assertNotNull(payment);
        logger.info("testInstance succeed!!");
    }

    @Test
    public void testOptional() {

        Map<String, Object> map = new HashMap<>();
        map.put("happy", null);
        map.put("point", "100");
        assertNull(map.get("happy"));

        String aa = Optional.ofNullable(map.get("happy")).map(Object::toString).orElse(null);
        logger.info("a = {}", aa);
        assertNull(aa);

        assertEquals("100", Optional.ofNullable(map.get("point")).map(Object::toString).orElse(null));

    }
}
