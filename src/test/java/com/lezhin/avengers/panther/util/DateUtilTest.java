package com.lezhin.avengers.panther.util;

import com.lezhin.avengers.panther.happypoint.HappyPointPayment;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author seoeun
 * @since 2017.11.16
 */
public class DateUtilTest {

    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

    @Test
    public void testDateFormat() {

        String dtStr = "2017-11-16 14:10:11";
        Instant instant = DateUtil.toInstant(dtStr, "yyyy-MM-dd HH:mm:ss", DateUtil.ASIA_SEOUL_ZONE);

        String convertedStr =
                DateUtil.format(instant.toEpochMilli(), DateUtil.ASIA_SEOUL_ZONE, "yyyy-MM-dd HH:mm:ss");

        assertEquals(dtStr, convertedStr);

    }

    @Test
    public void testDateTimeStr() {

        String dateStr = "20171117";
        String seoulTimeStr = "174650";
        String utcTimeStr = "084650";

        Instant instance = DateUtil.toInstant(dateStr + " " + utcTimeStr, "yyyyMMdd HHmmss",
                DateUtil.UTC_ZONE);
        logger.info("utcInstance = {}, {}", instance.toEpochMilli(), dateStr + " " + utcTimeStr);

        String dStr = DateUtil.getDateString(instance.toEpochMilli());
        String t1Str = DateUtil.getTimeString(instance.toEpochMilli());

        assertEquals(dateStr, dStr);
        // seoul timezone으로 변경
        assertEquals(seoulTimeStr, DateUtil.getTimeString(instance.toEpochMilli()));
        // utc timezone
        assertEquals(dateStr + " " + utcTimeStr,
                DateUtil.format(instance.toEpochMilli(), DateUtil.UTC_ZONE, "yyyyMMdd HHmmss"));
    }

    @Test
    public void testToInstant() {
        String trxDt = "20171114";
        Instant trxInstant = DateUtil.toInstantFromDate(trxDt, "yyyyMMdd", DateUtil.ASIA_SEOUL_ZONE);
        String ym = DateUtil.format(trxInstant.toEpochMilli(), DateUtil.ASIA_SEOUL_ZONE, "yyyyMM");
        assertEquals("201711", ym);
    }

    @Test
    public void testNewDate() {

        DateUtil.printDate();

    }


}
