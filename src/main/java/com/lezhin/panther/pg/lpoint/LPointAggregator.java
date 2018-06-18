package com.lezhin.panther.pg.lpoint;

import com.lezhin.panther.exception.ExceedException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.ResponseInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author taemmy
 * @since 2018. 6. 12.
 */
@Data
public class LPointAggregator implements Serializable {
    private static int MAX_DAY = 30000;
    private static int MAX_MONTH = 300000;
    /**
     * cache key: {pgCompany.name}:[yyyymm]:mbrNo:[yyyymmdd]
     */
    private String ym;
    private String mbrNo;
    private List<Point> points;

    LPointAggregator(String ym, String mbrNo) {
        this.ym = ym;
        this.mbrNo = mbrNo;
        this.points = new ArrayList<>();
    }

    public void add(String ymd, int amount) {
        // update point
        Optional<Point> optional = this.points.stream()
                .filter(p -> ymd.equals(p.ymd))
                .findFirst();

        if (optional.isPresent()) {
            optional.get().amount += amount;
        } else {
            this.points.add(new Point(ymd, amount));
        }
    }

    public void isExceed(String ymd, int amount) {
        // 일 최대 체크
        Optional<Point> optional = this.points.stream()
                .filter(p -> ymd.equals(p.ymd))
                .findFirst();

        optional.ifPresent(p -> {
            if (p.amount + amount > MAX_DAY) {
                throw new ExceedException(Executor.Type.LPOINT, ResponseInfo.ResponseCode.LPOINT_EXCEED_DAY.getMessage());
            }
        });

        // 월 최대 체크
        int monthSum = this.points.stream().mapToInt(p -> p.amount).sum();
        if (monthSum + amount > MAX_MONTH) {
            throw new ExceedException(Executor.Type.LPOINT, ResponseInfo.ResponseCode.LPOINT_EXCEED_MONTH.getMessage());
        }
    }

    class Point implements Serializable {
        private String ymd;
        private int amount;

        Point(String ymd, int amount) {
            this.ymd = ymd;
            this.amount = amount;
        }

        public String getYmd() {
            return ymd;
        }

        public int getAmount() {
            return amount;
        }
    }
}
