package com.lezhin.panther.pg.unknown;

import com.lezhin.panther.Context;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.Payment;

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
@Qualifier("unknown")
@Scope("prototype")
public class UnknownExecutor extends Executor<UnknownPayment> {

    private static final Logger logger = LoggerFactory.getLogger(UnknownExecutor.class);

    UnknownExecutor() {
        this.type = Type.UNKNOWN;
    }

    public UnknownExecutor(Context<UnknownPayment> context) {
        super(context);
        this.type = Type.UNKNOWN;
    }

    public Payment<UnknownPayment> reserve() {
        // do nothing
        try {
            for (int i = 0; i < 3; i++) {
                logger.info("unknown reserve = {}, {}", i, context.getPayment().getPaymentId());
                Thread.sleep(500);
            }
        } catch (Exception e) {

        }
        return context.getPayment();
    }

    public Payment<UnknownPayment> authenticate() {
        // do nothing
        return context.getPayment();
    }

    public Payment pay() {

        return context.getPayment();
    }

    public Payment complete() {
        return context.getPayment();
    }

}
