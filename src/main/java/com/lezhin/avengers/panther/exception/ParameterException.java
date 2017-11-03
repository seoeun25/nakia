package com.lezhin.avengers.panther.exception;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class ParameterException extends PantherException{

    public ParameterException(String message) {
        super(message);
    }

    public ParameterException(Throwable e) {
        super(e);
    }
}
