package com.lezhin.panther.command;

import com.lezhin.constant.PaymentState;
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
 * Cancel은 결제요청을 취소하는 것으로 아직 결제는 이뤄지지 않음. 결제과 완료된 것을 환불하는 step은 REFUND.
 * Cancel을 사용하는 결제방법은 무통장입금 정도.
 * @author seoeun
 * @since 2018.01.29
 */
@Component
@Scope("prototype")
public class Cancel<T extends PGPayment> extends Command<T> {

    private static final Logger logger = LoggerFactory.getLogger(Cancel.class);

    public Cancel() {
        super();
        this.commandType = Type.CANCEL;
    }

    public Cancel(RequestInfo requestInfo) {
        super(requestInfo);
        this.commandType = Type.CANCEL;
    }

    public Cancel(Context<T> context) {
        super(context);
        this.commandType = Type.CANCEL;
    }

    public void verifyPrecondition() throws PreconditionException {
        if (payment.getState() != PaymentState.PC) {
            throw new PreconditionException(requestInfo.getExecutorType(),
                    String.format("Payment[%s] state should be %s but %s",
                            payment.getPaymentId(), PaymentState.PC, payment.getState()));
        }
    }

    @Override
    public Payment execute() {
        initExecutor();

        logger.info("{} start {}", commandType.name(), context.printPretty());

        try {
            executor.cancel();
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
