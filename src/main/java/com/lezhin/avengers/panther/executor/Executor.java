package com.lezhin.avengers.panther.executor;

import com.lezhin.avengers.panther.Context;
import com.lezhin.avengers.panther.dummy.DummyExecutor;
import com.lezhin.avengers.panther.dummy.DummyPayment;
import com.lezhin.avengers.panther.happypoint.HappyPointExecutor;
import com.lezhin.avengers.panther.happypoint.HappyPointPayment;
import com.lezhin.avengers.panther.model.Payment;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public abstract class Executor<P extends Payment> {

    public enum Type {
        DUMMY("dummy") {
            @Override
            public Executor createExecutor(Context context) {
                return new DummyExecutor.DummyExecutorBuilder(context).build();
            }

            @Override
            public Payment createPayment(Context context) {
                return new DummyPayment(System.currentTimeMillis());
            }
        },
        HAPPYPOINT("happypoint") {
            @Override
            public Executor createExecutor(Context context) {
                return new HappyPointExecutor.HappyPointExecutorBuilder(context).build();
            }

            @Override
            public Payment createPayment(Context context) {
                return new HappyPointPayment(System.currentTimeMillis());
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
    protected Context<P> context;

    public Executor() {

    }

    public Executor(Builder builder) {
        this.type = builder.type;
        this.context = builder.context;
    }

    public P prepare() {
        return context.getPayment();
    }

    public P reserve() {
        return context.getPayment();
    }

    public P authenticate() {
        return context.getPayment();
    }

    public P pay() {

        return context.getPayment();
    }

    public void complete() {

    }

    public static abstract class Builder<P extends Payment> {
        protected Type type;
        protected Context<P> context;

        public Builder(Context context) {
            this.context = context;
        }

        public abstract Executor<P> build();

    }

}
