package com.lezhin.panther.exception;

import com.lezhin.constant.PGCompany;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.notification.NotificationLevel;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@NotificationLevel(level = NotificationLevel.Level.ERROR)
public class HttpClientException extends PantherException {

    public HttpClientException(String message) {
        super(message);
    }

    public HttpClientException(PGCompany pg, String message) {
        super(pg, message);
    }

    public HttpClientException(PGCompany pg, Throwable e) {
        super(pg, e);
    }

    public HttpClientException(PGCompany pg, String message, Throwable e) {
        super(pg, message, e);
    }

    public HttpClientException(Executor.Type type, String message) {
        super(type, message);
    }

    public HttpClientException(Executor.Type type, Throwable e) {
        super(type, e);
    }

    public HttpClientException(Executor.Type type, String message, Throwable e) {
        super(type, message, e);
    }

}
