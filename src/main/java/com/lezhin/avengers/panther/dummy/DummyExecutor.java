package com.lezhin.avengers.panther.dummy;

import com.lezhin.avengers.panther.Context;
import com.lezhin.avengers.panther.executor.Executor;
import com.lezhin.avengers.panther.model.Payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@Component
@Qualifier("dummy")
@Scope("prototype")
public class DummyExecutor extends Executor<DummyPayment> {

    private static final Logger logger = LoggerFactory.getLogger(DummyExecutor.class);

    DummyExecutor() {
        this.type = Type.DUMMY;
    }

    public DummyExecutor(Context<DummyPayment> context) {
        super(context);
        this.type = Type.DUMMY;
    }

    public Payment<DummyPayment> reserve() {
        // do nothing
        try {
            for (int i = 0; i < 3; i++) {
                logger.info("dummy reserve = {}, {}", i, context.getPayment().getPaymentId());
                Thread.sleep(500);
            }
        } catch (Exception e) {

        }
        return context.getPayment();
    }

    public Payment<DummyPayment> authenticate() {
        // do nothing
        return context.getPayment();
    }

    public Payment pay() {

        return  context.getPayment();
    }

    public Payment complete() {
        return  context.getPayment();
    }

}
