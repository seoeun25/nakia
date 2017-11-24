package com.lezhin.avengers.panther.exception;

import com.lezhin.avengers.panther.ErrorCode;
import com.lezhin.avengers.panther.executor.Executor;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class HappyPointParamException extends PantherException {

    private String code;

    public HappyPointParamException(Executor.Type type, String code) {
        super(type, code);
        this.code = code;
    }

    public HappyPointParamException(Executor.Type type, String code, String message) {
        super(type, code + ":" + message);
        this.code = code;
    }

    public HappyPointParamException(Executor.Type type, ErrorCode errorCode) {
        super(type, errorCode.getMessage());
        this.code = errorCode.getCode();
    }


    public HappyPointParamException(Executor.Type type, Throwable e) {
        super(type, e);
    }

    public String getCode() {
        return code;
    }
}
