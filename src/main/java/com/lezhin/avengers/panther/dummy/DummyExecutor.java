package com.lezhin.avengers.panther.dummy;

import com.lezhin.avengers.panther.Context;
import com.lezhin.avengers.panther.executor.Executor;
import com.lezhin.avengers.panther.model.Payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@Component
@Qualifier("dummy")
public class DummyExecutor<P extends Payment> extends Executor<P> {

    private static final Logger logger = LoggerFactory.getLogger(DummyExecutor.class);

    DummyExecutor() {

    }

    public DummyExecutor(Builder builder) {
        super(builder);
    }

    public P reserve() {
        // do nothing
        try {
            for (int i = 0; i< 3; i++) {
                logger.info("dummy reserve = {}, {}", i, context.getPayment().getPaymentId());
                Thread.sleep(500);
            }
        } catch (Exception e) {

        }
        return context.getPayment();
    }

    public P authenticate() {
        // do nothing
        return context.getPayment();
    }

    public P pay() {
        // TODO request http call

        P payment = context.getPayment();
        // TODO request http call

        return payment;
    }

    public void complete() {

    }

    public static class DummyExecutorBuilder extends Builder {

        public DummyExecutorBuilder(Context context) {
            super(context);
            this.type = Type.DUMMY;
        }

        @Override
        public Executor build() {
            DummyExecutor executor = new DummyExecutor(this);
            return executor;
        }
    }
}
