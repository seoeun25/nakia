package com.lezhin.panther.exception;

import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * @author seoeun
 * @since 2018.03.26
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
public class PincruxException extends PantherException {

    public PincruxException(Executor.Type type, String message) {
        super(type, message);
    }

    public PincruxException(Executor.Type type, Throwable e) {
        super(type, e);
    }
}
