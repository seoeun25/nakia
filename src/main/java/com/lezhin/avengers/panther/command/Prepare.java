package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.exception.ExecutorException;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;
import com.lezhin.avengers.panther.util.JsonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * PG에 payment를 요청을 하기 전의 준비 단계.
 * 이 단계는 PG에 따라 다르게 사용될 수 있다.
 * 예를 들면, happypoint에서는 point 를 조회 하는 데 사용.
 *
 * @author seoeun
 * @since 2017.10.24
 */
@Component
@Scope("prototype")
public class Prepare<T extends PGPayment> extends Command<T> {

    private static final Logger logger = LoggerFactory.getLogger(Prepare.class);

    public Prepare() {
        super();
        this.commandType = Type.PREPARE;
    }

    public Prepare(RequestInfo requestInfo) {
        super(requestInfo);
        this.commandType = Type.PREPARE;
    }

    public void loadState() throws PreconditionException {
        payment = requestInfo.getPayment();
        verifyPrecondition();
    }

    @Override
    public Payment<T> execute() throws PreconditionException, ExecutorException {
        initExecutor();
        logger.info("start prepare. {}", context.printPretty());
        Payment<T> payment = executor.prepare();
        context = context.withResponse(executor.getContext().getResponseInfo());
        logger.info("prepare executed = {}", JsonUtil.toJson(payment));

        return payment;
    }
}
