package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.Context;
import com.lezhin.avengers.panther.ErrorCode;
import com.lezhin.avengers.panther.exception.ExecutorException;
import com.lezhin.avengers.panther.exception.InternalPaymentException;
import com.lezhin.avengers.panther.exception.PantherException;
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
public class Pay<T extends PGPayment> extends Command<T> {

    private static final Logger logger = LoggerFactory.getLogger(Pay.class);

    public Pay() {
        super();
        this.commandType = Type.PAY;
    }

    public Pay(RequestInfo requestInfo) {
        super(requestInfo);
        this.commandType = Type.PAY;
    }

    public Pay(Context<T> context) {
        super(context);
        this.commandType = Type.PAY;
    }

    public void verifyPrecondition() throws PreconditionException {
        if (payment.getState() != PaymentState.PC) {
            throw new PreconditionException(String.format("Payment state should be %s but %s", PaymentState.PC,
                    payment.getState()));
        }
        logger.info("verifyPrecondition done");
    }

    @Override
    public Payment execute() {
        initExecutor();

        logger.info("start pay. {}", context.printPretty());
        try {
            executor.pay();
        } catch (PantherException e) {
            // do nothing. responseInfo에 이미 세팅되어 있음.
        } finally {
            payment = executor.getContext().getPayment();
        }
        logger.info("pay. executed = {}", JsonUtil.toJson(payment));
        context = context.withPayment(payment);
        context = context.withResponse(executor.getContext().getResponseInfo());
        logger.info("pay. executed = {}", context.getResponseInfo().toString());

        try {
            // execution이 성공이든 실패이든 internalPayment call.
            payment = internalPaymentService.pay(context);
            context = context.withPayment(payment);
            logger.info("internalPayment. pay. {}", JsonUtil.toJson(payment));
        } catch (Throwable e) {
            if (context.executionSucceed()) {
                // execution이 성공하고 internalPayment.paymentVerified 가 실패했다면,
                // purchase가 만들어 지지 않음. pg 취소
                logger.error("pg.pay succeed, but internalPayment.payVerfied failed. cancel pg pay");

                // FIXME 환불

                // response는 panther.
                context = context.withResponse(
                        new ResponseInfo(ErrorCode.LEZHIN_INTERNAL_PAYMNENT.getCode(), e.getMessage()));

            } else {
                // execution이 fail되었다면 internalPayment의 update가 fail 되어도 그냥 둔다.
                // response는 pg.errorCode
                logger.error("pg.pay failed and internalPayment.paymentUnverified failed");
            }
            logger.warn("responseCode = {}", JsonUtil.toJson(context.getResponseInfo()));
            executor.handleResponseCode(context.getResponseInfo().getCode());
        }

        return processNextStep();
    }

}
