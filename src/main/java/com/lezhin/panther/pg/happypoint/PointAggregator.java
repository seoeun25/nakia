package com.lezhin.panther.pg.happypoint;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author seoeun
 * @since 2017.12.19
 */
@Data
public class PointAggregator implements Serializable {

    private String mbrNo;
    /**
     * yyyyMM 형식의 string. 201711
     */
    private String ym;
    private Integer pointSum;

    public PointAggregator(String mbrNo, String ym, Integer pointSum) {
        this.mbrNo = mbrNo;
        this.ym = ym;
        this.pointSum = pointSum;
    }

}