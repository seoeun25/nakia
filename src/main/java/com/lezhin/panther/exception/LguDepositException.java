package com.lezhin.panther.exception;

import com.lezhin.panther.Context;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * @author seoeun
 * @since 2018.01.08
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
public class LguDepositException extends PantherException {

    private String code;

    public LguDepositException(String message) {
        super(message);
    }

    public LguDepositException(Executor.Type type, String code, String message) {
        super(type, code + ":" + message);
        this.code = code;
    }

    public LguDepositException(Executor.Type type, Throwable e) {
        super(type, e);
    }

    public LguDepositException(Context context, String code) {
        super(context, code);
        this.code = code;
    }

    public LguDepositException(Context context, String code, String message) {
        super(context, code + ":" + message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
