package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.Context;
import com.lezhin.avengers.panther.ErrorCode;
import com.lezhin.avengers.panther.exception.ExecutorException;
import com.lezhin.avengers.panther.exception.HappyPointSystemException;
import com.lezhin.avengers.panther.exception.InternalPaymentException;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;
import com.lezhin.avengers.panther.model.ResponseInfo;
import com.lezhin.avengers.panther.util.JsonUtil;
import com.lezhin.constant.PaymentState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@Component
@Scope("prototype")
public class Authenticate<T extends PGPayment> extends Command<T> {

    private static final Logger logger = LoggerFactory.getLogger(Authenticate.class);

    public Authenticate() {
        super();
        this.commandType = Type.AUTHENTICATE;
    }

    public Authenticate(RequestInfo requestInfo) {
        super(requestInfo);
        this.commandType = Type.AUTHENTICATE;
    }

    public Authenticate(Context<T> context) {
        super(context);
        this.commandType = Type.AUTHENTICATE;
    }

    public void verifyPrecondition() throws PreconditionException {
        if (payment.getState() != PaymentState.R) {
            throw new PreconditionException(String.format("Payment state should be %s but %s", PaymentState.R,
                    payment.getState()));
        }
        logger.info("verifyPrecondition done");
    }

    @Override
    public Payment execute() {
        initExecutor();

        logger.info("start authenticate. {}", context.printPretty());

        payment = executor.authenticate();
        logger.info("authenticate. executed = {}", JsonUtil.toJson(payment));
        context = context.withPayment(payment);
        context = context.withResponse(executor.getContext().getResponseInfo());

        try {
            payment = internalPaymentService.authenticate(context);
            context = context.withPayment(payment);
            logger.info("internalPayment. authenticated. {}", JsonUtil.toJson(payment));
        } catch (Throwable e) {
            context = context.withResponse(
                    new ResponseInfo(ErrorCode.LEZHIN_INTERNAL_PAYMNENT.getCode(), e.getMessage()));
            logger.warn("Failed to InternalPayment.reserve");
            throw new InternalPaymentException(e);
        }

        return processNextStep();
    }

}
