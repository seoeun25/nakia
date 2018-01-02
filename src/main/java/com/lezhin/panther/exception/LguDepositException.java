package com.lezhin.panther.exception;

import com.lezhin.panther.ErrorCode;
import com.lezhin.panther.executor.Executor;

/**
 * @author seoeun
 * @since 2018.01.08
 */
public class LguDepositException extends PantherException {

    private String code;

    public LguDepositException(Executor.Type type, String code) {
        super(type, code);
        this.code = code;
    }

    public LguDepositException(Executor.Type type, String code, String message) {
        super(type, code + ":" + message);
        this.code = code;
    }

    public LguDepositException(Executor.Type type, ErrorCode errorCode) {
        super(type, errorCode.getMessage());
        this.code = errorCode.getCode();
    }


    public LguDepositException(Executor.Type type, Throwable e) {
        super(type, e);
    }

    public String getCode() {
        return code;
    }

}
