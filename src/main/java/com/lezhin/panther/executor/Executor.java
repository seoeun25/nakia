package com.lezhin.panther.executor;

import com.lezhin.panther.Context;
import com.lezhin.panther.command.Command;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.dummy.DummyExecutor;
import com.lezhin.panther.dummy.DummyPayment;
import com.lezhin.panther.happypoint.HappyPointExecutor;
import com.lezhin.panther.happypoint.HappyPointPayment;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.constant.PaymentType;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public abstract class Executor<T extends PGPayment> {

    public enum Type {
        DUMMY("dummy") {
            @Override
            public Payment createPayment(Context context) {
                return new Payment<DummyPayment>(System.currentTimeMillis());
            }

            @Override
            public PaymentType getPaymentType(String externalStoreProductId) {
                return PaymentType.unknown;
            }

            @Override
            public Class getExecutorClass() {
                return DummyExecutor.class;
            }
        },
        HAPPYPOINT("happypoint") {
            @Override
            public Payment createPayment(Context context) {
                return new Payment<HappyPointPayment>(System.currentTimeMillis());
            }

            @Override
            public PaymentType getPaymentType(String externalStoreProductId) {
                if (externalStoreProductId == null) {
                    return PaymentType.happypoint;
                }
                return externalStoreProductId.contains("mhappypoint") ? PaymentType.mhappypoint :
                        PaymentType.happypoint;
            }

            @Override
            public Class getExecutorClass() {
                return HappyPointExecutor.class;
            }
        };

        private String name;

        Type(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

        public abstract Payment createPayment(Context context);

        public abstract <E> Class<E> getExecutorClass();

        public abstract PaymentType getPaymentType(String externalStoreProductId);
    }

    protected Type type;
    protected Context<T> context;
    @Autowired
    protected PantherProperties pantherProperties;

    public Executor() {

    }

    public Executor(Context<T> context) {
        this.context = context;
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

    public Payment<T> complete() {
        return  context.getPayment();
    }

    public Payment refund() {
        return  context.getPayment();
    }

    public Command.Type nextTransition(Command.Type currentStep) {

        return Command.Type.DONE;
    }

    public Context<T> getContext() {
        return context;
    }

    /**
     * Throws Exception if the responseCode is not OK state.
     * @param responseCode
     * @throws RuntimeException
     */
    public void handleResponseCode(String responseCode) {

    }

}
