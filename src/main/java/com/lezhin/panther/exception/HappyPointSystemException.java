package com.lezhin.panther.exception;

import com.lezhin.panther.Context;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class HappyPointSystemException extends PantherException {

    private String code;

    public HappyPointSystemException(String message) {
        super(message);
    }

    public HappyPointSystemException(Context context, String code) {
        super(context, code);
        this.code = code;
    }

    public HappyPointSystemException(Context context, String code, String message) {
        super(context, code + ":" + message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
