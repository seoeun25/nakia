package com.lezhin.panther.command;

import com.lezhin.panther.Context;
import com.lezhin.panther.ErrorCode;
import com.lezhin.panther.exception.InternalPaymentException;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.util.JsonUtil;
import com.lezhin.constant.PaymentState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
            throw new PreconditionException(requestInfo.getExecutorType(),
                    String.format("Payment state should be %s but %s", PaymentState.R, payment.getState()));
        }
        logger.info("verifyPrecondition done");
    }

    @Override
    public Payment execute() {
        initExecutor();

        logger.info("{} start. {}", commandType.name(), context.printPretty());

        payment = executor.authenticate();
        context = context.withPayment(payment);
        context = context.withResponse(executor.getContext().getResponseInfo());
        logger.info("{} [{}] done. {}", commandType.name(), executor.getClass().getSimpleName(),
                context.getResponseInfo().toString());
        logger.debug("payment = {}", JsonUtil.toJson(payment));
        executor.handleResponseCode(context.getResponseInfo().getCode());

        try {
            payment = internalPaymentService.authenticate(context);
            context = context.withPayment(payment);
        } catch (Throwable e) {
            context = context.withResponse(
                    new ResponseInfo(ErrorCode.LEZHIN_INTERNAL_PAYMNENT.getCode(), e.getMessage()));
            logger.warn("Failed to InternalPayment.authenticate");
            throw new InternalPaymentException(requestInfo.getExecutorType(), e);
        }

        logger.info("{} complete. {} ", commandType.name(), context.getResponseInfo().toString());

        return processNextStep();
    }

}
