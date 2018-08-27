package com.lezhin.panther.exception;

import com.lezhin.constant.PGCompany;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;
import com.mysql.fabric.xmlrpc.base.Param;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@NotificationLevel(level = NotificationLevel.Level.WARN)
public class ParameterException extends PantherException {

    public ParameterException(String message) {
        super(message);
    }

    public ParameterException(Executor.Type type, String message) {
        super(type, message);
    }

    public ParameterException(Executor.Type type, Throwable e) {
        super(type, e);
    }

    public ParameterException(PGCompany pg, String message) {
        super(pg, message);
    }

    public ParameterException(PGCompany pg, Throwable e) {
        super(pg, e);
    }

    public ParameterException(PGCompany pg, String message, Throwable e) {
        super(pg, message, e);
    }
}
