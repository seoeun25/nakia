package com.lezhin.avengers.panther.exception;

/**
 * Command가 실행되기 전에 precondition 체크
 *
 * @author seoeun
 * @since 2017.10.24
 */
public class PreconditionException extends RuntimeException{

    public PreconditionException(String message) {
        super(message);
    }

    public PreconditionException(Throwable e) {
        super(e);
    }
}
