package com.lezhin.panther.exception;

import com.lezhin.panther.Context;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class HappyPointParamException extends PantherException {

    private String code;

    public HappyPointParamException(String message) {
        super(message);
    }

    public HappyPointParamException(Executor.Type type, String code) {
        super(type, code);
        this.code = code;
    }

    public HappyPointParamException(Context context, String code, String message) {
        super(context, code + ":" + message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
