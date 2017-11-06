package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.constant.PaymentState;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class Pay<T extends PGPayment> extends Command<T> {

    public Pay(RequestInfo requestInfo) {
        super(requestInfo);
    }

    public void verifyPrecondition() throws PreconditionException {
        if (payment.getState() != PaymentState.PC) {
            throw new PreconditionException(String.format("Payment state should be %s but %s", PaymentState.PC,
                    payment.getState()));
        }
    }

    @Override
    public Payment execute() {
        initExecutor();
        Payment payment = executor.pay();
        return payment;
    }
}
