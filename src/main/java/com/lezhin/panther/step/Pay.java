package com.lezhin.panther.step;

import com.lezhin.constant.PaymentState;
import com.lezhin.panther.Context;
import com.lezhin.panther.exception.InternalPaymentException;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;
import com.lezhin.panther.util.JsonUtil;

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
public class Pay<T extends PGPayment> extends Command<T> {

    private static final Logger logger = LoggerFactory.getLogger(Pay.class);

    public Pay() {
        super();
        this.commandType = Type.PAY;
    }

    public Pay(Context<T> context) {
        super(context);
        this.commandType = Type.PAY;
    }

    public void verifyPrecondition() throws PreconditionException {
        if (payment.getState() != PaymentState.PC) {
            throw new PreconditionException(context,
                    String.format("Payment[%s] state should be %s but %s",
                            payment.getPaymentId(), PaymentState.PC, payment.getState()));
        }
    }

    @Override
    public Payment execute() {
        initExecutor();

        logger.info("{} {} start.", context.print(), commandType.name());

        try {
            executor.pay();
        } catch (PantherException e) {
            // do nothing. executor.context.responseInfo에 이미 세팅되어 있음.
        } finally {
            payment = executor.getContext().getPayment();
            context = context.payment(payment).response(executor.getContext().getResponseInfo());
            logger.info("{} {} done. {}", context.print(), commandType.name(),
                    context.getResponseInfo().toString());
            logger.debug("payment = {}, \n{}", payment.getPaymentId(), JsonUtil.toJson(payment));
        }

        try {
            // execution이 성공이든 실패이든 internalPayment call.
            payment = internalPaymentService.pay(context);
            context = context.payment(payment);
        } catch (Throwable e) {
            logger.info("status = {}", context.getResponseInfo());
            logger.warn("Failed to internal.pay", e);
            if (context.executionSucceed()) {
                // execution이 성공하고 internalPayment.paymentVerified 가 실패했다면,
                // purchase가 만들어 지지 않음. pg 취소
                logger.error("{} !!! pg.pay succeed, but internalPayment.paymentVerified failed.\n" +
                                " === This payment is going to REFUND to {}. paymentId = {}", commandType.name(),
                        executor.getType(), payment.getPaymentId());

                // 환불
                executor.refund();

                // response는 panther.
                context = context.response(
                        new ResponseInfo(ResponseCode.LEZHIN_INTERNAL_PAYMNENT.getCode(), e.getMessage()));
                throw new InternalPaymentException(context, e);
            } else {
                // execution이 fail되었다면 internalPayment의 update가 fail 되어도 그냥 둔다.
                // response는 pg.errorCode
                logger.error("{} !!! pg.pay failed and internalPayment.paymentUnverified failed", commandType.name());
            }
            logger.info("{} internal.error {} ", commandType.name(), context.getResponseInfo().toString());
        }

        logger.info("{} {} complete. {} ", context.print(), commandType.name(),
                context.getResponseInfo().toString());
        executor.handleResponse(context);

        return processNextStep();
    }

}
