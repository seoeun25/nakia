package com.lezhin.avengers.panther.exception;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class SPCException extends PantherException{

    private String code;
    public SPCException(String code) {
        super(code);
        this.code = code;
    }

    public SPCException(Throwable e) {
        super(e);
    }

    public String getCode() {
        return code;
    }
}
