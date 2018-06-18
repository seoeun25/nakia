package com.lezhin.panther.exception;

import com.lezhin.panther.Context;
import com.lezhin.panther.executor.Executor;

/**
 * @author taemmy
 * @since 2018. 5. 29.
 */
public class LPointException extends PantherException {

    private String code;

    public LPointException(String message) {
        super(message);
    }

    public LPointException(Executor.Type type, String code) {
        super(type, code);
        this.code = code;
    }

    public LPointException(Context context, String code, String message) {
        super(context, code + ":" + message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
