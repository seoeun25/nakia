package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.ErrorCode;
import com.lezhin.avengers.panther.exception.ExecutorException;
import com.lezhin.avengers.panther.exception.InternalPaymentException;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;
import com.lezhin.avengers.panther.model.ResponseInfo;
import com.lezhin.avengers.panther.util.JsonUtil;
import com.lezhin.avengers.panther.util.Util;
import com.lezhin.constant.PaymentState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * PG에 결제요청하기. 결제 요청을 한 후 {@linkplain PaymentState#R}로 셋팅.
 *
 * @author seoeun
 * @since 2017.10.24
 */
@Component
@Scope("prototype")
public class Reserve<T extends PGPayment> extends Command<T> {

    private static final Logger logger = LoggerFactory.getLogger(Reserve.class);

    public Reserve() {
        super();
        commandType = Type.RESERVE;
    }

    public Reserve(RequestInfo requestInfo) {
        super(requestInfo);
        commandType = Type.RESERVE;
    }

    public void loadState() throws PreconditionException {
        payment = requestInfo.getPayment();
        verifyPrecondition();
    }

    public void verifyPrecondition() throws PreconditionException {
        // InternalPaymentService reserve 에 필요한 property set. check.
        payment.setLocale(Util.of(requestInfo.getLocale()));
        if (payment.getStore() == null) {
            throw new PreconditionException("store can not be bull");
        }
        if (payment.getStoreVersion() == null) {
            throw new PreconditionException("storeVersion can not be bull");
        }
        if (payment.getPgCompany() == null) {
            throw new PreconditionException("pgCompany can not be bull");
        }
        if (payment.getPaymentType() == null) {
            throw new PreconditionException("paymentType can not be bull");
        }
    }

    @Override
    public Payment<T> execute() throws PreconditionException, ExecutorException {
        initExecutor();

        logger.info("start reserve. {}", context.printPretty());

        payment = executor.reserve();
        logger.info("reserve. executed = {}", JsonUtil.toJson(payment));
        context = context.withPayment(payment);
        context = context.withResponse(executor.getContext().getResponseInfo());

        try {
            payment = internalPaymentService.reserve(context);
            context = context.withPayment(payment);
            logger.info("internalPayment. reserved. {}", JsonUtil.toJson(payment));
        } catch (Throwable e) {
            context = context.withResponse(
                    new ResponseInfo(ErrorCode.LEZHIN_INTERNAL_PAYMNENT.getCode(), e.getMessage()));
            logger.warn("Failed to InternalPayment.reserve");
            throw new InternalPaymentException(e);
        }

        return processNextStep();

    }



}
