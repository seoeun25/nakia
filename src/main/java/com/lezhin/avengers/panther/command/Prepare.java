package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

/**
 * //TODO 이 단계가 필요 없을 수도.
 *
 * @author seoeun
 * @since 2017.10.24
 */
public class Prepare<P extends Payment> extends Command<P> {

    public Prepare(RequestInfo requestInfo) {
        super(requestInfo);
    }

    @Override
    public P execute() {
        return payment;
    }
}
