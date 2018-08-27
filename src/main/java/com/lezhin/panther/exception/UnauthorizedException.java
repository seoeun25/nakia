package com.lezhin.panther.exception;

import com.lezhin.constant.PGCompany;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * @author seoeun
 * @since 2018.01.05
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
public class UnauthorizedException extends PantherException {
    public UnauthorizedException(PGCompany pg, String message) {
        super(pg, message);
    }

    public UnauthorizedException(PGCompany pg, Throwable e) {
        super(pg, e);
    }

    public UnauthorizedException(PGCompany pg, String message, Throwable e) {
        super(pg, message, e);
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
