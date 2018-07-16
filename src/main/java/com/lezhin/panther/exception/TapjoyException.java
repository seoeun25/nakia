package com.lezhin.panther.exception;

import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * @author taemmy
 * @since 2018. 7. 5.
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
public class TapjoyException extends PantherException {
    public TapjoyException(String message) {
        super(message);
    }

    public TapjoyException(Executor.Type type, String message) {
        super(type, message);
    }

    public TapjoyException(Executor.Type type, Throwable e) {
        super(type, e);
    }
}
