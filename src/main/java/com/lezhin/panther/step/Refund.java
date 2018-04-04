package com.lezhin.panther.step;

import com.lezhin.panther.Context;
import com.lezhin.panther.exception.PantherException;
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
 * 결제 완료된 것을 환불하는 step.
 *
 * @author seoeun
 * @since 2018.02.28
 */
@Component
@Scope("prototype")
public class Refund<T extends PGPayment> extends Command<T> {

    private static final Logger logger = LoggerFactory.getLogger(Refund.class);

    public Refund() {
        super();
        this.commandType = Type.REFUND;
    }

    public Refund(RequestInfo requestInfo) {
        super(requestInfo);
        this.commandType = Type.REFUND;
    }

    public Refund(Context<T> context) {
        super(context);
        this.commandType = Type.REFUND;
    }

    public void verifyPrecondition() throws PreconditionException {

    }

    @Override
    public Payment execute() {
        initExecutor();

        logger.info("{} start {}", commandType.name(), context.printPretty());

        try {
            executor.refund();
        } catch (PantherException e) {
            // do nothing. executor.context.responseInfo에 이미 세팅되어 있음.
        } finally {
            payment = executor.getContext().getPayment();
            context = context.payment(payment).response(executor.getContext().getResponseInfo());
            logger.info("{} [{}] done. {}", commandType.name(), executor.getType(),
                    context.getResponseInfo().toString());
            logger.debug("payment = {}, \n{}", payment.getPaymentId(), JsonUtil.toJson(payment));
        }

        // no need to InternalPayment call.
        logger.info("{} complete. {} ", commandType.name(), context.getResponseInfo().toString());
        executor.handleResponseCode(context.getResponseInfo().getCode());

        return processNextStep();
    }

}
