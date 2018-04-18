package com.lezhin.panther.exception;

import com.lezhin.panther.Context;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
public class InternalPaymentException extends PantherException {

    public InternalPaymentException(String message) {
        super(message);
    }

    public InternalPaymentException(Context context, String message) {
        super(context, message);
    }

    public InternalPaymentException(Context context, Throwable e) {
        super(context, e);
    }

    public InternalPaymentException(Context context, String message, Throwable e) {
        super(context, message, e);
    }

}
