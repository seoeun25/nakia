package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.exception.ExecutorException;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PG에 payment를 요청을 하기 전의 준비 단계.
 * 이 단계는 PG에 따라 다르게 사용될 수 있다.
 * 예를 들면, happypoint에서는 point 를 조회 하는 데 사용.
 *
 * @author seoeun
 * @since 2017.10.24
 */
public class Prepare<T extends PGPayment> extends Command<T> {

    private static final Logger logger = LoggerFactory.getLogger(Prepare.class);

    public Prepare(RequestInfo requestInfo) {
        super(requestInfo);
    }

    public void loadState() throws PreconditionException {
        logger.info("prepare. loadState");
        payment = requestInfo.getPayment();
        verifyPrecondition();
    }

    @Override
    public Payment<T> execute() throws PreconditionException, ExecutorException {
        initExecutor();
        logger.info("execute. Executor={}, PGPayment = {}", executor.getClass().getName(),
                payment.getPgPayment().getClass().getName());
        logger.info("requestInfo = {}", context.getRequestInfo().toString());
        logger.info("payment. userId = {}", payment.getUserId());
        Payment<T> payment = executor.prepare();
        return payment;
    }
}
