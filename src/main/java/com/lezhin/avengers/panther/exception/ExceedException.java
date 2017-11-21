package com.lezhin.avengers.panther.exception;

/**
 * 특정 throttle 을 넘겼을 때
 *
 * @author seoeun
 * @since 2017.10.24
 */
public class ExceedException extends PantherException{

    public ExceedException(String message) {
        super(message);
    }

    public ExceedException(Throwable e) {
        super(e);
    }
}
