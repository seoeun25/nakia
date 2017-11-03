package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.Context;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.executor.Executor;
import com.lezhin.avengers.panther.happypoint.HappyPointPayment;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public abstract class Command<P extends Payment> {

    public enum Type {
        PREPARE, RESERVE, AUTHENTICATE, PAY, COMPLETE
    }

    protected RequestInfo requestInfo;
    protected P payment;
    protected Executor<P> executor;
    protected Context<P> context = null;

    public Command(final RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public Command(final Context<P> context) {
        this.context = context;
        this.requestInfo = context.getRequestInfo();
        this.payment = context.getPayment();
    }

    public void loadState() throws PreconditionException {
        // TODO set payment from InteranlPaymentService(GCS)
        // payment = internalService.getPayment(); 임시로 executor 에서.
        payment = (P) requestInfo.getExecutorType().createPayment(context);
        verifyPrecondition();
    }

    public void verifyPrecondition() throws PreconditionException {

    }

    protected void initExecutor() {
        if (context == null) {
            context = new Context.Builder<P>(requestInfo, payment).build();
        }
        executor = createExecutor(context);
    }

    protected Executor<P> createExecutor(final Context context) {
        // TODO how to initiate executor.
        return requestInfo.getExecutorType().createExecutor(context);
    }

    public abstract P execute();

    public void saveState() {

    }

    public void onFinish() {

    }

    public void onError() {

    }

}
