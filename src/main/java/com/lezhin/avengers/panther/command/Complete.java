package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.Context;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

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

    public Complete(RequestInfo requestInfo) {
        super(requestInfo);
        this.commandType = Type.COMPLETE;
    }

    public Complete(Context<T> context) {
        super(context);
        this.commandType = Type.COMPLETE;
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
