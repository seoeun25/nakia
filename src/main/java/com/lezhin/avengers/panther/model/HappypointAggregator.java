package com.lezhin.avengers.panther.model;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author seoeun
 * @since 2017.12.19
 */
@Data
@Deprecated
public class HappypointAggregator implements Serializable {

    private String mbrNo;
    /**
     * yyyyMM 형식의 string. 201711
     */
    private String ym;
    private Integer pointSum;

    public HappypointAggregator(String mbrNo, String ym, Integer pointSum) {
        this.mbrNo = mbrNo;
        this.ym = ym;
        this.pointSum = pointSum;
    }

}