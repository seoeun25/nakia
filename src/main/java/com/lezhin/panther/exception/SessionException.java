package com.lezhin.panther.exception;

import com.lezhin.panther.executor.Executor;

/**
 * RequestInfo 가 사라졌을 때. Session이 만료되었을 때
 *
 * @author seoeun
 * @since 2018.01.17
 */
public class SessionException extends PantherException {

    public SessionException(Executor.Type type, String message) {
        super(type, message);
    }

    public SessionException(Executor.Type type, Throwable e) {
        super(type, e);
    }
}