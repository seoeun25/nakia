package com.lezhin.panther.exception;

import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * Panther의 최상위 Exception.
 *
 * @author seoeun
 * @since 2017.10.24
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
public class PantherException extends RuntimeException {

    protected Executor.Type type;

    public PantherException(Executor.Type type, String message) {
        super(message);
        this.type = type;
    }

    public PantherException(Executor.Type type, Throwable e) {
        super(e);
        this.type = type;
    }

    public PantherException(Executor.Type type, String message, Throwable e) {
        super(message, e);
        this.type = type;
    }

    public Executor.Type getType() {
        return type;
    }

}
