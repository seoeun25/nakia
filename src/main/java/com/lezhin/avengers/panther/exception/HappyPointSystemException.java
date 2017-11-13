package com.lezhin.avengers.panther.exception;

import com.lezhin.avengers.panther.ErrorCode;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class HappyPointSystemException extends PantherException {

    private String code;

    public HappyPointSystemException(String code) {
        super(code);
        this.code = code;
    }

    public HappyPointSystemException(String code, String message) {
        super(code + ":" + message);
        this.code = code;
    }

    public HappyPointSystemException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }


    public HappyPointSystemException(Throwable e) {
        super(e);
    }

    public String getCode() {
        return code;
    }
}
