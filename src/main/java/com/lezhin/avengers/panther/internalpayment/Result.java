package com.lezhin.avengers.panther.internalpayment;

import java.io.Serializable;

/**
 * LezhinServer com.lezhin.beans.view.Result 를 copy. InternalPaymentService의 response obeject.
 * @author seoeun
 * @since 2017.11.08
 */
public class Result<T> implements Serializable{

    private int code = 0;
    private String description = "정상 처리 되었습니다.";
    private T data;
    private boolean hasDetail;

    public Result() {
        hasDetail = false;
    }

    public Result(int code, String description) {
        super();
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }
    public Result<T> setDescription(String description) {
        this.description = description;
        return this;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }

    public boolean getHasDetail() {
        return hasDetail;
    }

    public void setHasDetail(boolean hasDetail_) {
        this.hasDetail = hasDetail_;
    }
}
