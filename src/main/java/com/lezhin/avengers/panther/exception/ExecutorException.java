package com.lezhin.avengers.panther.exception;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class ExecutorException extends RuntimeException{

    public ExecutorException(String message) {
        super(message);
    }

    public ExecutorException(Throwable e) {
        super(e);
    }
}
