package com.lezhin.panther.model;

import com.lezhin.panther.ErrorCode;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author seoeun
 * @since 2017.10.25
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder (toBuilder = true)
@Getter
@ToString
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
