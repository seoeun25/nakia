package com.lezhin.panther.payletter;

import com.lezhin.panther.internal.InternalPaymentServiceTest;
import com.lezhin.panther.util.DateUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author seoeun
 * @since 2018.03.13
 */
@ExtendWith(SpringExtension.class)
public class PayLetterServiceTest {

    private static Logger logger = LoggerFactory.getLogger(InternalPaymentServiceTest.class);

    @Test
    public void testStartEndDate() {
        String fromYMD = "20180311";
        String toYMD = "20180312";

        Instant startDateTime = DateUtil.toInstantFromDate(DateUtil.toDatePattern(fromYMD), "yyyy-MM-dd", DateUtil
                .ASIA_SEOUL_ZONE);
        Instant endDateTime = DateUtil.toInstantFromDate(DateUtil.toDatePattern(toYMD), "yyyy-MM-dd", DateUtil
                .ASIA_SEOUL_ZONE).plusSeconds(86400);

        Instant sDate = DateUtil.toInstantFromDate(fromYMD, "yyyyMMdd", DateUtil.ASIA_SEOUL_ZONE);
        Instant eDate = DateUtil.toInstantFromDate(toYMD, "yyyyMMdd", DateUtil.ASIA_SEOUL_ZONE);
        assertEquals(sDate.toEpochMilli(), startDateTime.toEpochMilli());

        assertEquals(sDate.toEpochMilli(), 1520694000000L);
        assertEquals(eDate.toEpochMilli(), 1520780400000L);

        assertEquals(fromYMD, DateUtil.format(1520694000000L, DateUtil.ASIA_SEOUL_ZONE, "yyyyMMdd"));
        assertEquals(toYMD, DateUtil.format(1520780400000L, DateUtil.ASIA_SEOUL_ZONE, "yyyyMMdd"));

    }

}
