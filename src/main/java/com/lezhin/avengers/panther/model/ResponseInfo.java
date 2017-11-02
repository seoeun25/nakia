package com.lezhin.avengers.panther.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

/**
 * @author seoeun
 * @since 2017.10.25
 */
public class ResponseInfo<T> implements Serializable{

    @JsonSerialize
    private T data;

    public ResponseInfo() {

    }

    public ResponseInfo(T data) {
        this.data = data;
    }
}
