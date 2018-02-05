package com.lezhin.panther.exception;

import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * @author seoeun
 * @since 2018.01.05
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
public class FraudException extends PantherException {

    public FraudException(Executor.Type type, String message) {
        super(type, message);
    }

    public FraudException(Executor.Type type, Throwable e) {
        super(type, e);
    }

}
