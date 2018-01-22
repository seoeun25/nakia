package com.lezhin.panther.util;

import com.lezhin.panther.lguplus.LguplusPayment;
import com.lezhin.panther.model.PGPayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author seoeun
 * @since 2017.12.21
 */
@ExtendWith(SpringExtension.class)
public class JsonUtilTest {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtilTest.class);

    /**
     * PgPayment를 map으로, 또 반대로 변환하는 기능 테스트 .
     */
    @Test
    public void testMapPgPaymentConvert() {
        PGPayment pgPayment = LguplusPayment.builder().CST_MID("cst_mid")
                .LGD_AMOUNT("20.5").build();
        Map<String, Object> map = JsonUtil.toMap(pgPayment);
        logger.info("map = {}", map);
        assertEquals(2, map.size());
        assertEquals("cst_mid", map.get("CST_MID"));
        assertEquals("20.5", map.get("LGD_AMOUNT"));
        assertNull(map.get("LGD_BUYERIP"));

        LguplusPayment pgPayment1 = JsonUtil.fromMap(map, LguplusPayment.class);
        logger.info("pgPayment1 = {}", pgPayment1);
        assertEquals("cst_mid", pgPayment1.getCST_MID());
        assertEquals("20.5", pgPayment1.getLGD_AMOUNT());
        assertNull(pgPayment1.getLGD_BUYER());
    }

}
