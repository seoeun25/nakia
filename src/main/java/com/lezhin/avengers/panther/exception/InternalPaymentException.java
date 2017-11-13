package com.lezhin.avengers.panther.exception;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class InternalPaymentException extends PantherException {

    public InternalPaymentException(String message) {
        super(message);
    }

    public InternalPaymentException(Throwable e) {
        super(e);
    }

    public InternalPaymentException(String message, Throwable e) {
        super(message, e);
    }
}
