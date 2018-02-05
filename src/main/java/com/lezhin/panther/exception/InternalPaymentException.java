package com.lezhin.panther.exception;

import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
public class InternalPaymentException extends PantherException {

    public InternalPaymentException(Executor.Type type, String message) {
        super(type, message);
    }

    public InternalPaymentException(Executor.Type type, Throwable e) {
        super(type, e);
    }

    public InternalPaymentException(Executor.Type type, String message, Throwable e) {
        super(type, message, e);
    }

}
