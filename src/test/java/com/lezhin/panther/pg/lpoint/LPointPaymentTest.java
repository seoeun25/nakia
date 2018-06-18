package com.lezhin.panther.pg.lpoint;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author taemmy
 * @since 2018. 5. 28.
 */
class LPointPaymentTest {
    private static final Logger logger = LoggerFactory.getLogger(LPointExecutorTest.class);

    @Test
    public void testFlwNo() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        LPointPayment lpointPayment = LPointPayment.API.HEALTHCHECK.createReqeust();

        String flwNo = lpointPayment.getControl().getFlwNo();
        logger.info("flwNo: {}, [length: {}]", flwNo, flwNo.length());

        assertTrue(flwNo.startsWith("9900O730"+today.format(dateFmt)));
        assertEquals(22, flwNo.length());
    }

}