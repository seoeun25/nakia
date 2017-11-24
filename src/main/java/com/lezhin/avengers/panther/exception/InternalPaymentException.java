package com.lezhin.avengers.panther.exception;

import com.lezhin.avengers.panther.executor.Executor;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class InternalPaymentException extends PantherException {

    public InternalPaymentException(Executor.Type type, String message) {
        super(type, message);
    }

    public InternalPaymentException(Executor.Type type, Throwable e) {
        super(type, e);
    }

    public InternalPaymentException(Executor.Type type, String message, Throwable e) {
        super(type, message, e);
    }
}
