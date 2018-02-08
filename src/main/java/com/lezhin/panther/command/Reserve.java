package com.lezhin.panther.command;

import com.lezhin.constant.PaymentState;
import com.lezhin.panther.exception.ExecutorException;
import com.lezhin.panther.exception.InternalPaymentException;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
        payment.setLocale(requestInfo.getLocale());
        if (StringUtils.isEmpty(payment.getStore())) {
            throw new ParameterException(requestInfo.getExecutorType(), "store can not be null nor empty");
        }
        if (StringUtils.isEmpty(payment.getPgCompany())) {
            throw new ParameterException(requestInfo.getExecutorType(), "pgCompany can not be null nor empty");
        }
        if (payment.getPaymentType() == null) {
            throw new ParameterException(requestInfo.getExecutorType(), "paymentType can not be null");
        }
    }

    @Override
    public Payment<T> execute() throws PreconditionException, ExecutorException {
        initExecutor();

        logger.info("{} start. {}", commandType.name(), context.printPretty());

        // Reserve 는 internalPayment call을 먼저 실행
        executor.verifyPrecondition(Type.RESERVE);
        try {
            payment = internalPaymentService.reserve(context);
            context = context.payment(payment);
        } catch (Throwable e) {
            context = context.response(
                    new ResponseInfo(ResponseCode.LEZHIN_INTERNAL_PAYMNENT.getCode(), e.getMessage()));
            logger.warn("Failed to InternalPayment.reserve");
            throw new InternalPaymentException(requestInfo.getExecutorType(), e);
        }

        initExecutor(context);
        payment = executor.reserve();
        context = context.payment(payment).response(executor.getContext().getResponseInfo());
        logger.info("{} [{}] done. {}", commandType.name(), executor.getType(),
                context.getResponseInfo().toString());
        executor.handleResponseCode(context.getResponseInfo().getCode());

        logger.info("{} [{}] complete. {}. userId={}, paymentId={}, coinProductId={} ", commandType.name(),
                executor.getType(), context.getResponseInfo().toString(), payment.getUserId(), payment.getPaymentId(),
                payment.getCoinProductId());

        requestInfo = new RequestInfo.Builder(requestInfo).withPayment(payment).build();
        simpleCacheService.saveRequestInfo(requestInfo);

        return processNextStep();

    }


}
