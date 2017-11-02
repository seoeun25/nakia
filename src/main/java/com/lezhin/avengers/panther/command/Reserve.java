package com.lezhin.avengers.panther.command;

import com.lezhin.constant.PaymentState;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class Reserve<P extends Payment> extends Command<P> {
    public Reserve(RequestInfo requestInfo) {
        super(requestInfo);
    }

    public void verifyPrecondition() throws PreconditionException {
        try {
            if (payment.getState() != PaymentState.R) {
                throw new PreconditionException(String.format("Payment state should be %s but %s", PaymentState.R,
                        payment.getState()));
            }
        } catch (Exception e) {
            throw new PreconditionException(e);
        }
    }

    @Override
    public P execute() {
        initExecutor();
        executor.reserve();
        return null;
    }
}
