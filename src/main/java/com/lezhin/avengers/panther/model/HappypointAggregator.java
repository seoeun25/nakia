package com.lezhin.avengers.panther.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Happypoint에서 user 별로 3000point/month 제한을 요구.
 * FIXME Persistence layer 추가 해서, payment 결과를 저장하면 거기서 조회. 후에 삭제할 임시 클래스.
 *
 * @author seoeun
 * @since 2017.11.19
 */
@Data
public class HappypointAggregator implements Serializable{

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
