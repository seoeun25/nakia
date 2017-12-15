package com.lezhin.panther.exception;

import com.lezhin.panther.ErrorCode;
import com.lezhin.panther.executor.Executor;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class HappyPointSystemException extends PantherException {

    private String code;

    public HappyPointSystemException(Executor.Type type, String code) {
        super(type, code);
        this.code = code;
    }

    public HappyPointSystemException(Executor.Type type, String code, String message) {
        super(type, code + ":" + message);
        this.code = code;
    }

    public HappyPointSystemException(Executor.Type type, ErrorCode errorCode) {
        super(type, errorCode.getMessage());
        this.code = errorCode.getCode();
    }


    public HappyPointSystemException(Executor.Type type, Throwable e) {
        super(type, e);
    }

    public String getCode() {
        return code;
    }
}