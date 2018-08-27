package com.lezhin.panther.exception;

import com.lezhin.constant.PGCompany;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * @author seoeun
 * @since 2018.01.05
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
public class FraudException extends PantherException {
    public FraudException(PGCompany pg, String message) {
        super(pg, message);
    }

    public FraudException(PGCompany pg, Throwable e) {
        super(pg, e);
    }

    public FraudException(PGCompany pg, String message, Throwable e) {
        super(pg, message, e);
    }

    public FraudException(String message) {
        super(message);
    }

    public FraudException(Executor.Type type, String message) {
        super(type, message);
    }

    public FraudException(Executor.Type type, Throwable e) {
        super(type, e);
    }

}
