package com.lezhin.panther.executor;

import com.lezhin.constant.PaymentType;
import com.lezhin.panther.Context;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.model.Meta;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;
import com.lezhin.panther.pg.happypoint.HappyPointExecutor;
import com.lezhin.panther.pg.happypoint.HappyPointPayment;
import com.lezhin.panther.pg.lguplus.LguDepositExecutor;
import com.lezhin.panther.pg.lguplus.LguplusPayment;
import com.lezhin.panther.pg.lpoint.LPointExecutor;
import com.lezhin.panther.pg.lpoint.LPointPayment;
import com.lezhin.panther.pg.unknown.UnknownExecutor;
import com.lezhin.panther.pg.unknown.UnknownPayment;
import com.lezhin.panther.step.Command;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public abstract class Executor<T extends PGPayment> {

    public enum Type {
        UNKNOWN("unknown") {
            @Override
            public Payment createPayment(Context context) {
                return new Payment<UnknownPayment>(System.currentTimeMillis());
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
                return UnknownExecutor.class;
            }
        },
        HAPPYPOINT("happypoint") {
            @Override
            public Payment createPayment(Context context) {
                return new Payment<HappyPointPayment>(System.currentTimeMillis());
            }

            @Override
            public Payment createPayment(PGPayment pgPayment) {
                HappyPointPayment happyPointPayment = (HappyPointPayment) pgPayment;
                Payment<HappyPointPayment> payment = new Payment<>();
                payment.setUserId(-1L);
                payment.setPaymentId(-1L);
                payment.setPgPayment(happyPointPayment);
                payment.setMeta(new Meta());
                return payment;
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
                return responseInfo.getCode().equals(ResponseCode.SPC_OK.getCode());
            }

            @Override
            public Class getExecutorClass() {
                return HappyPointExecutor.class;
            }
        },
        LGUDEPOSIT("lgudeposit") { // LGUplus를 사용하여 무통장입금 (deposit)

            @Override
            public Payment createPayment(Context context) {
                return new Payment<com.lezhin.panther.pg.lguplus.LguplusPayment>(System.currentTimeMillis());
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
                return responseInfo.getCode().equals(ResponseCode.LGUPLUS_OK.getCode());
            }

            @Override
            public boolean isAsync() {
                return true;
            }
        },
        LPOINT("lpoint") {
            @Override
            public Payment createPayment(Context context) {
                return new Payment<LPointPayment>(System.currentTimeMillis());
            }

            @Override
            public Payment createPayment(PGPayment pgPayment) {
                Payment<LPointPayment> payment = new Payment<>();
                payment.setUserId(-1L);
                payment.setPaymentId(-1L);
                payment.setPgPayment((LPointPayment) pgPayment);
                payment.setMeta(new Meta());
                return payment;
            }

            public PaymentType getPaymentType(String externalStoreProductId) {
                if (externalStoreProductId == null) {
                    return PaymentType.lpoint;
                }
                return externalStoreProductId.contains("mlpoint") ? PaymentType.mlpoint :
                        PaymentType.lpoint;
            }

            @Override
            public Class getExecutorClass() {
                return LPointExecutor.class;
            }

            @Override
            public boolean isAsync() {
                return true;
            }

            @Override
            public boolean succeeded(ResponseInfo responseInfo) {
                return responseInfo.getCode().equals(ResponseCode.LPOINT_OK.getCode());
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

    protected Executor() {

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

    public Payment cancel() {
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
     * @param context
     * @throws RuntimeException
     */
    public void handleResponse(Context context) {

    }

    public void verifyPrecondition(Command.Type type) throws PreconditionException {

    }

    public Type getType() {
        return type;
    }
}
