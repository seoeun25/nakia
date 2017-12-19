package com.lezhin.avengers.panther.model;

import lombok.Data;

import java.io.Serializable;

/**
 * TODO 임시 클래스. refactoring으로 deserialize 할 때 이전 버전 호환성을 맞춰주기 위함
 *
 * @author seoeun
 * @since 2017.12.19
 */
@Data
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