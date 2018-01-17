package com.lezhin.panther.executor;

import com.lezhin.constant.PaymentType;
import com.lezhin.panther.Context;
import com.lezhin.panther.ErrorCode;
import com.lezhin.panther.command.Command;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.dummy.DummyExecutor;
import com.lezhin.panther.dummy.DummyPayment;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.happypoint.HappyPointExecutor;
import com.lezhin.panther.happypoint.HappyPointPayment;
import com.lezhin.panther.lguplus.LguDepositExecutor;
import com.lezhin.panther.lguplus.LguplusPayment;
import com.lezhin.panther.model.Meta;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.ResponseInfo;

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
            public Payment createPayment(PGPayment pgPayment) {
                return null;
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
            public Payment createPayment(PGPayment pgPayment) {
                return null;
            }

            @Override
            public PaymentType getPaymentType(String externalStoreProductId) {
                if (externalStoreProductId == null) {
                    return PaymentType.happypoint;
                }
                return externalStoreProductId.contains("mhappypoint") ? PaymentType.mhappypoint :
                        PaymentType.happypoint;
            }

            public boolean succeeded(ResponseInfo responseInfo) {
                return responseInfo.getCode().equals(ErrorCode.SPC_OK.getCode());
            }

            @Override
            public Class getExecutorClass() {
                return HappyPointExecutor.class;
            }
        },
        LGUDEPOSIT("lgudeposit") { // LGUplus를 사용하여 무통장입금 (deposit)

            @Override
            public Payment createPayment(Context context) {
                return new Payment<com.lezhin.panther.lguplus.LguplusPayment>(System.currentTimeMillis());
            }

            @Override
            public Payment createPayment(PGPayment pgPayment) {
                LguplusPayment lguplusPayment = (LguplusPayment) pgPayment;
                Payment<LguplusPayment> payment = new Payment<>();
                payment.setUserId(Long.parseLong(lguplusPayment.getLGD_BUYER()));
                payment.setPaymentId(Long.parseLong(lguplusPayment.getLGD_OID()));
                payment.setAmount(Float.parseFloat(lguplusPayment.getLGD_AMOUNT()));
                payment.setCoinProductName(lguplusPayment.getLGD_PRODUCTINFO());
                payment.setPgPayment(lguplusPayment);
                payment.setMeta(new Meta());
                return payment;
            }

            @Override
            public Class getExecutorClass() {
                return LguDepositExecutor.class;
            }

            @Override
            public PaymentType getPaymentType(String externalStoreProductId) {
                return PaymentType.deposit;
            }

            public boolean succeeded(ResponseInfo responseInfo) {
                return responseInfo.getCode().equals(ErrorCode.LGUPLUS_OK.getCode());
            }

            @Override
            public boolean isAsync() {
                return true;
            }
        };

        private String label;

        Type(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public abstract Payment createPayment(Context context);

        public abstract Payment createPayment(PGPayment pgPayment);

        public abstract <E> Class<E> getExecutorClass();

        public abstract PaymentType getPaymentType(String externalStoreProductId);

        /**
         * Determines if the execution is successful by {@code responseInfo}
         *
         * @param responseInfo
         * @return
         */
        public boolean succeeded(ResponseInfo responseInfo) {
            return true;
        }

        /**
         * PG에서 callback으로 async하게 호출하는 경우.
         *
         * @return
         */
        public boolean isAsync() {
            return false;
        }
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

    public Payment<T> preAuthenticate() {
        return context.getPayment();
    }

    public Payment<T> authenticate() {
        return context.getPayment();
    }

    public Payment<T> pay() {

        return context.getPayment();
    }

    public Payment<T> complete() {
        return context.getPayment();
    }

    public Payment refund() {
        return context.getPayment();
    }

    public Command.Type nextTransition(Command.Type currentStep) {

        return Command.Type.DONE;
    }

    public Context<T> getContext() {
        return context;
    }

    /**
     * Determines if the execution is successful by {@code responseInfo}
     *
     * @param responseInfo
     * @return
     */
    public boolean succeeded(ResponseInfo responseInfo) {
        return getType().succeeded(responseInfo);
    }

    /**
     * Throws Exception if the responseCode is not OK state.
     *
     * @param responseCode
     * @throws RuntimeException
     */
    public void handleResponseCode(String responseCode) {

    }

    public void verifyPrecondition(Command.Type type) throws PreconditionException {

    }

    public Type getType() {
        return type;
    }
}
