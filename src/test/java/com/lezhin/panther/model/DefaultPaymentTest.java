package com.lezhin.panther.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
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
        map.put("LGD_OID", "123456");
        assertNull(map.get("happy"));
        assertNotNull(map.get("LGD_OID"));

        String aa = Optional.ofNullable(map.get("happy")).map(Object::toString).orElse(null);
        logger.info("a = {}", aa);
        assertNull(aa);

        assertEquals("100", Optional.ofNullable(map.get("point")).map(Object::toString).orElse(null));

        // LGD_OID가 null 이 아닌 경우
        Long pId = Optional.ofNullable(map.get("LGD_OID")).map(o -> Long.valueOf(o.toString())).orElse(-1L);
        assertEquals(123456L, pId.longValue());
        // LGD_OID가 null인 경우, -1L 리턴.
        map.put("LGD_OID", null);
        assertNull(map.get("LGD_OID"));
        pId = Optional.ofNullable(map.get("LGD_OID")).map(o -> Long.valueOf(o.toString())).orElse(-1L);
        assertEquals(-1L, pId.longValue());

    }
}
