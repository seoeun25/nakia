package com.lezhin.avengers.panther.executor;

import com.lezhin.avengers.panther.Context;
import com.lezhin.avengers.panther.dummy.DummyExecutor;
import com.lezhin.avengers.panther.dummy.DummyPayment;
import com.lezhin.avengers.panther.happypoint.HappyPointExecutor;
import com.lezhin.avengers.panther.happypoint.HappyPointPayment;
import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.avengers.panther.model.Payment;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public abstract class Executor<T extends PGPayment> {

    public enum Type {
        DUMMY("dummy") {
            @Override
            public Executor createExecutor(Context context) {
                return new DummyExecutor.DummyExecutorBuilder(context).build();
            }

            @Override
            public Payment createPayment(Context context) {
                return new Payment<DummyPayment>(System.currentTimeMillis());
            }
        },
        HAPPYPOINT("happypoint") {
            @Override
            public Executor createExecutor(Context context) {
                return new HappyPointExecutor.HappyPointExecutorBuilder(context).build();
            }

            @Override
            public Payment createPayment(Context context) {
                return new Payment<HappyPointPayment>(System.currentTimeMillis());
            }
        };

        private String name;
        Type(String name) {
            this.name = name;
        }
        String getName() {
            return name;
        }

        public abstract Executor createExecutor(Context context);
        public abstract Payment createPayment(Context context);
    }

    protected Type type;
    protected Context<T> context;

    public Executor() {

    }

    public Executor(Builder builder) {
        this.type = builder.type;
        this.context = builder.context;
    }

    public Payment<T> prepare() {
        return context.getPayment();
    }

    public Payment<T> reserve() {
        return context.getPayment();
    }

    public Payment<T> authenticate() {
        return context.getPayment();
    }

    public Payment<T> pay() {

        return context.getPayment();
    }

    public void complete() {

    }

    public static abstract class Builder<T extends PGPayment> {
        protected Type type;
        protected Context<T> context;

        public Builder(Context context) {
            this.context = context;
        }

        public abstract Executor<T> build();

    }

}
