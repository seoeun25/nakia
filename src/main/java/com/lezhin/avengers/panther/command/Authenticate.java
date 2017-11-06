package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class Authenticate<T extends PGPayment> extends Command<T> {

    public Authenticate(RequestInfo requestInfo) {
        super(requestInfo);
    }

    @Override
    public Payment execute() {
        return null;
    }
}
