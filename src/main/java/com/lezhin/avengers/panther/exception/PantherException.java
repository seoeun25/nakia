package com.lezhin.avengers.panther.exception;

/**
 * Panther의 최상위 Exception.
 * @author seoeun
 * @since 2017.10.24
 */
public class PantherException extends RuntimeException{

    public PantherException(String message) {
        super(message);
    }

    public PantherException(Throwable e) {
        super(e);
    }

    public PantherException(String message, Throwable e) {
        super(message, e);
    }
}
