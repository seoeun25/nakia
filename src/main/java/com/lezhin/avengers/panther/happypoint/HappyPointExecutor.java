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
public class HappyPointExecutor extends Executor<HappyPointPayment> {

    private static final Logger logger = LoggerFactory.getLogger(HappyPointExecutor.class);

    HappyPointExecutor() {

    }

    public HappyPointExecutor(Builder builder) {
        super(builder);
    }

    public Payment<HappyPointPayment> prepare() {
        Payment<HappyPointPayment> payment = context.getPayment();
        logger.info("context. payment. userId={}", payment.getUserId());
        logger.info("context. happyPointPayment 11 = {}", payment.getPgPayment().printCommonRequest());
        logger.info("context. happyPointPayment 22 = {}", payment.getPgPayment().printA());

        // FIXME context.payment.pgpayment 에 다음 happypoint 를 merge.
        HappyPointPayment happyPointPayment = HappyPointPayment.API.authentication.createRequest();
        logger.info("happyPointPayment commonRequest = {}", happyPointPayment.printCommonRequest());

        // FIXME Get the CI from redis
        String CI = "REDIS_X_CI";
        String NAME = "REDIS_X_NAME";
        happyPointPayment.setMbrNm(NAME);
        happyPointPayment.setMbrIdfNo(CI);

        logger.info("happyPointPayment printA = {}", happyPointPayment.printA());

        // FIXME 회원인증. http call to spc

        // FIXME 포인트조회. http call to spc

        payment.setPgPayment(happyPointPayment);

        // TODO request http call

        return payment;
    }

    public Payment<HappyPointPayment> reserve() {
        Payment<HappyPointPayment> payment = context.getPayment();
        // param check
        // TODO request http call

        return payment;
    }

    public Payment<HappyPointPayment> authenticate() {
        Payment<HappyPointPayment> payment = context.getPayment();
        // TODO request http call

        return payment;
    }

    public Payment<HappyPointPayment> pay() {
        // TODO request http call

        Payment<HappyPointPayment> payment = context.getPayment();
        // TODO request http call

        return payment;
    }

    public Payment<HappyPointPayment> checkPoint() {
        // spc에 포인트 조회

        return context.getPayment();
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
