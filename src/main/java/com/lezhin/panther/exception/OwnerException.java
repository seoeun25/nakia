package com.lezhin.panther.exception;

import com.lezhin.panther.Context;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * @author seoeun
 * @since 2018.02.25
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
    public class OwnerException extends PantherException{

    public OwnerException(String message) {
        super(message);
    }

    public OwnerException(Context context, String message) {
        super(context, message);
    }

    public OwnerException(Executor.Type type, Throwable e) {
        super(type, e);
    }
}
