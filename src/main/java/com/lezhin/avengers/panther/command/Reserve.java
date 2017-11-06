package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.constant.PaymentState;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

/**
 * PG에 결제요청하기. 결제 요청을 한 후 {@linkplain PaymentState#R}로 셋팅.
 * @author seoeun
 * @since 2017.10.24
 */
public class Reserve<T extends PGPayment> extends Command<T> {

    public Reserve(RequestInfo requestInfo) {
        super(requestInfo);
    }

    public void verifyPrecondition() throws PreconditionException {
        // TODO reqeustInfo check.
    }

    @Override
    public Payment<T> execute() {
        initExecutor();
        executor.reserve();
        return null;
    }
}
