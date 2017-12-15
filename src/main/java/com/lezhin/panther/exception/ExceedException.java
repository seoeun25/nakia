package com.lezhin.panther.exception;

import com.lezhin.panther.executor.Executor;

/**
 * 특정 throttle 을 넘겼을 때
 *
 * @author seoeun
 * @since 2017.10.24
 */
public class ExceedException extends PantherException{

    public ExceedException(Executor.Type type, String message) {
        super(type, message);
    }

    public ExceedException(Executor.Type type, Throwable e) {
        super(type, e);
    }
}
