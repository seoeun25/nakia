package com.lezhin.avengers.panther.exception;

import com.lezhin.avengers.panther.ErrorCode;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class HappyPointParamException extends PantherException {

    private String code;

    public HappyPointParamException(String code) {
        super(code);
        this.code = code;
    }

    public HappyPointParamException(String code, String message) {
        super(code + ":" + message);
        this.code = code;
    }

    public HappyPointParamException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }


    public HappyPointParamException(Throwable e) {
        super(e);
    }

    public String getCode() {
        return code;
    }
}
