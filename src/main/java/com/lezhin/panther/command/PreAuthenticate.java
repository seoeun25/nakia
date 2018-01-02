package com.lezhin.panther.command;

import com.lezhin.constant.PaymentState;
import com.lezhin.panther.Context;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.util.JsonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Reserve 이후 Authenticate의 전 step 으로 internalPayment에는 없지만 pg에서 처리할 일이 있을 때 사용.
 *
 * @author seoeun
 * @since 2018.01.08
 */
@Component
@Scope("prototype")
public class PreAuthenticate<T extends PGPayment> extends Command<T> {

    private static final Logger logger = LoggerFactory.getLogger(PreAuthenticate.class);

    public PreAuthenticate() {
        super();
        this.commandType = Type.PREAUTHENTICATE;
    }

    public PreAuthenticate(RequestInfo requestInfo) {
        super(requestInfo);
        this.commandType = Type.PREAUTHENTICATE;
    }

    public PreAuthenticate(Context<T> context) {
        super(context);
        this.commandType = Type.PREAUTHENTICATE;
    }

    public void verifyPrecondition() throws PreconditionException {
        if (payment.getState() != PaymentState.R) {
            throw new PreconditionException(requestInfo.getExecutorType(),
                    String.format("Payment state should be %s but %s", PaymentState.R, payment.getState()));
        }
    }

    @Override
    public Payment execute() {
        initExecutor();

        logger.info("{} start. {}", commandType.name(), context.printPretty());

        payment = executor.preAuthenticate();
        context = context.payment(payment).response(executor.getContext().getResponseInfo());
        logger.info("{} [{}] done. {}", commandType.name(), executor.getType(),
                context.getResponseInfo().toString());
        logger.debug("payment = {}", JsonUtil.toJson(payment));
        executor.handleResponseCode(context.getResponseInfo().getCode());

        logger.info("{} [{}] complete. {} ", commandType.name(), executor.getType(),
                context.getResponseInfo().toString());

        return processNextStep();
    }

}
