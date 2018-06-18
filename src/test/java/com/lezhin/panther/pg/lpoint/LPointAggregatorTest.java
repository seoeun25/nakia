package com.lezhin.panther.pg.lpoint;

import com.lezhin.panther.exception.ExceedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author taemmy
 * @since 2018. 6. 13.
 */
class LPointAggregatorTest {

    private String mbrNo = "2200001002";
    private String ym = "201608";
    private String ymd = "20160825";

    @Test
    public void testAdd() {
        LPointAggregator aggregator = new LPointAggregator(ym, mbrNo);

        aggregator.add(ymd, 1000);

        assertEquals(1, aggregator.getPoints().size());

        LPointAggregator.Point point = aggregator.getPoints().get(0);
        assertEquals(ymd, point.getYmd());
        assertEquals(1000, point.getAmount());
    }

    @Test
    public void testAddMergeAmount() {
        LPointAggregator aggregator = new LPointAggregator(ym, mbrNo);

        aggregator.add(ymd, 1000);
        aggregator.add(ymd, 1000);

        assertEquals(1, aggregator.getPoints().size());

        LPointAggregator.Point point = aggregator.getPoints().get(0);
        assertEquals(ymd, point.getYmd());
        assertEquals(2000, point.getAmount());
    }

    @Test
    public void testIsExceed() {
        LPointAggregator aggregator = new LPointAggregator(ym, mbrNo);

        aggregator.add(ymd, 20000);
    }

    @Test
    public void testIsExceedPointsEmpty() {
        LPointAggregator aggregator = new LPointAggregator(ym, mbrNo);

        aggregator.isExceed(ymd, 1000);
    }

    @Test
    public void testIsExceedExceedDay() {
        LPointAggregator aggregator = new LPointAggregator(ym, mbrNo);
        aggregator.add(ymd, 30000);

        assertThrows(ExceedException.class, () -> aggregator.isExceed(ymd, 1000));
    }

    @Test
    public void testIsExceedExceedMonth() {
        LPointAggregator aggregator = new LPointAggregator(ym, mbrNo);
        aggregator.add(ym + "01", 5000);
        aggregator.add(ym + "02", 25000);
        aggregator.add(ym + "03", 30000);
        aggregator.add(ym + "04", 30000);
        aggregator.add(ym + "05", 30000);
        aggregator.add(ym + "06", 30000);
        aggregator.add(ym + "07", 30000);
        aggregator.add(ym + "08", 30000);
        aggregator.add(ym + "09", 30000);
        aggregator.add(ym + "10", 30000);
        aggregator.add(ym + "11", 30000);

        assertThrows(ExceedException.class, () -> aggregator.isExceed(ymd, 1000));
    }


}