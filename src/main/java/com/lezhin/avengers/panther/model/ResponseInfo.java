package com.lezhin.avengers.panther.model;

import com.lezhin.avengers.panther.ErrorCode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.MoreObjects;
import lombok.Data;

import java.io.Serializable;

/**
 * @author seoeun
 * @since 2017.10.25
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ResponseInfo implements Serializable {

    private String code;
    private String description;

    public ResponseInfo() {

    }

    public ResponseInfo(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.description = errorCode.getMessage();
    }

    public ResponseInfo(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
