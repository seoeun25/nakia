package com.lezhin.avengers.panther.exception;

import com.lezhin.avengers.panther.executor.Executor;

/**
 * Command가 실행되기 전에 precondition 체크
 *
 * @author seoeun
 * @since 2017.10.24
 */
public class PreconditionException extends PantherException {

    public PreconditionException(Executor.Type type, String message) {
        super(type, message);
    }

    public PreconditionException(Executor.Type type, Throwable e) {
        super(type, e);
    }
}
