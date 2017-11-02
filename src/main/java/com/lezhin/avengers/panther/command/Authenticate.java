package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class Authenticate<P extends Payment> extends Command<P> {
    public Authenticate(RequestInfo requestInfo) {
        super(requestInfo);
    }

    @Override
    public P execute() {
        return null;
    }
}
