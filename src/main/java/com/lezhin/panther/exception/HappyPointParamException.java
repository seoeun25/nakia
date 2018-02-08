package com.lezhin.panther.exception;

import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;

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

    public HappyPointParamException(Executor.Type type, ResponseCode errorCode) {
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
