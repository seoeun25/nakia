package com.lezhin.panther.exception;

import com.lezhin.constant.PGCompany;
import com.lezhin.panther.Context;
import com.lezhin.panther.executor.Executor;

/**
 * Command가 실행되기 전에 precondition 체크
 *
 * @author seoeun
 * @since 2017.10.24
 */
public class PreconditionException extends PantherException {

    public PreconditionException(String message) {
        super(message);
    }

    public PreconditionException(PGCompany pg, String message) {
        super(pg, message);
    }

    public PreconditionException(PGCompany pg, Throwable e) {
        super(pg, e);
    }

    public PreconditionException(PGCompany pg, String message, Throwable e) {
        super(pg, message, e);
    }

    public PreconditionException(Executor.Type type, String message) {
        super(type, message);
    }

    public PreconditionException(Executor.Type type, Throwable e) {
        super(type, e);
    }

    public PreconditionException(Context context, String message) {
        super(context, message);
    }

    public PreconditionException(Context context, Throwable e) {
        super(context, e);
    }

}
