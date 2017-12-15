package com.lezhin.panther.exception;

import com.lezhin.panther.executor.Executor;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class ParameterException extends PantherException{

    public ParameterException(Executor.Type type, String message) {
        super(type, message);
    }

    public ParameterException(Executor.Type type, Throwable e) {
        super(type, e);
    }
}
