package com.lezhin.panther.step;

import com.lezhin.panther.Context;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@Component
@Scope("prototype")
public class Complete<T extends PGPayment> extends Command<T> {

    public Complete() {
        super();
        this.commandType = Type.COMPLETE;
    }

    public Complete(Context<T> context) {
        super(context);
        this.commandType = Type.COMPLETE;
    }

    public void loadState() throws PreconditionException {
        payment = requestInfo.getPayment();
        verifyPrecondition();
    }

    public void verifyPrecondition() throws PreconditionException {

    }

    @Override
    public Payment<T> execute() {
        initExecutor();

        Payment<T> payment = executor.complete();

        return payment;
    }
}
