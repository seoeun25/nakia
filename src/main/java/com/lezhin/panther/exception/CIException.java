package com.lezhin.panther.exception;

import com.lezhin.panther.executor.Executor;

/**
 * 본인 인증 정보가 사라졌을 때. TTL 이 지나서 다시 본인 인증 해야 할 때.
 *
 * @author seoeun
 * @since 2017.12.13
 */
public class CIException extends PantherException {

    public CIException(String message) {
        super(message);
    }

    public CIException(Executor.Type type, String message) {
        super(type, message);
    }

    public CIException(Executor.Type type, Throwable e) {
        super(type, e);
    }

}
