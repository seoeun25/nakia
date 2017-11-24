package com.lezhin.avengers.panther.exception;

import com.lezhin.avengers.panther.executor.Executor;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class ExecutorException extends PantherException {

    public ExecutorException(Executor.Type type, String message) {
        super(type, message);
    }

    public ExecutorException(Executor.Type type, Throwable e) {
        super(type, e);
    }
}
