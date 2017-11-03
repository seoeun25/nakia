package com.lezhin.avengers.panther.happypoint;

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
@Qualifier("happypoint")
public class HappyPointExecutor<P extends Payment> extends Executor<P> {

    private static final Logger logger = LoggerFactory.getLogger(HappyPointExecutor.class);

    HappyPointExecutor() {

    }

    public HappyPointExecutor(Builder builder) {
        super(builder);
    }

    public P prepare() {
        P payment = context.getPayment();
        // TODO request http call

        return payment;
    }

    public P reserve() {
        P payment = context.getPayment();
        // TODO request http call

        return payment;
    }

    public P authenticate() {
        P payment = context.getPayment();
        // TODO request http call

        return payment;
    }

    public P pay() {
        // TODO request http call

        P payment = context.getPayment();
        // TODO request http call

        return payment;
    }

    public void complete() {

    }

    public static class HappyPointExecutorBuilder extends Executor.Builder {

        public HappyPointExecutorBuilder(Context context) {
            super(context);
            this.type = Type.HAPPYPOINT;
        }

        @Override
        public Executor build() {
            HappyPointExecutor executor = new HappyPointExecutor(this);
            return executor;
        }
    }
}
