package com.lezhin.avengers.panther.exception;

import com.lezhin.avengers.panther.executor.Executor;

/**
 * Panther의 최상위 Exception.
 * @author seoeun
 * @since 2017.10.24
 */
public class PantherException extends RuntimeException{

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
