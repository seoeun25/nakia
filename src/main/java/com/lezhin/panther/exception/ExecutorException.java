package com.lezhin.panther.exception;

import com.lezhin.panther.Context;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
public class ExecutorException extends PantherException {

    public ExecutorException(String message) {
        super(message);
    }


    public ExecutorException(Context context, String message) {
        super(context, message);
    }

    public ExecutorException(Context context, Throwable e) {
        super(context, e);
    }
}
