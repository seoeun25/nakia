package com.lezhin.panther.command;

import com.lezhin.panther.Context;
import com.lezhin.panther.exception.InternalPaymentException;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.util.JsonUtil;
import com.lezhin.constant.PaymentState;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;

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
                    String.format("Payment[%s] state should be %s but %s",
                            payment.getPaymentId(), PaymentState.R, payment.getState()));
        }
    }

    @Override
    public Payment execute() {
        initExecutor();

        logger.info("{} start. {}", commandType.name(), context.printPretty());

        try {
            payment = executor.authenticate();
        } catch (PantherException e) {
            // do nothing. executor.context.responseInfo에 setting 되어 있음
        } finally {
            payment = executor.getContext().getPayment();
            context = context.payment(payment).response(executor.getContext().getResponseInfo());
            logger.info("{} [{}] done. {}", commandType.name(), executor.getType(),
                    context.getResponseInfo().toString());
            logger.debug("payment = {}, \n{}", payment.getPaymentId(), JsonUtil.toJson(payment));
        }

        try {
            payment = internalPaymentService.authenticate(context);
            context = context.payment(payment);
        } catch (Throwable e) {
            logger.info("status = {}", context.getResponseInfo());
            logger.warn("Failed to internal.authenticate", e);
            if (context.executionSucceed()) {
                // pg.execution 은 성공. internal 은 실패.
                logger.error("{} !!! pg.authentication succeed, but internalPayment.authentication failed.\n" +
                                " === This payment is going to FAIL to {}. paymentId = {}", commandType.name(),
                        executor.getType(), payment.getPaymentId());

                // response는 panther.
                context = context.response(
                        new ResponseInfo(ResponseCode.LEZHIN_INTERNAL_PAYMNENT.getCode(), e.getMessage()));
                throw new InternalPaymentException(requestInfo.getExecutorType(), e);

            } else {
                // pg.execution 도 실패. internal 도 실패. 그냥 pg.execution 실패로 남김.
                logger.error("{} !!! pg.authentication failed and internalPayment.authentication failed",
                        commandType.name());
            }
            logger.info("{} internal.error {} ", commandType.name(), context.getResponseInfo().toString());

        }

        logger.info("{} [{}] complete. {} ", commandType.name(), executor.getType(),
                context.getResponseInfo().toString());
        executor.handleResponseCode(context.getResponseInfo().getCode());

        return processNextStep();
    }

}
