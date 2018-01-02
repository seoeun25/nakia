package com.lezhin.panther.command;

import com.lezhin.constant.LezhinError;
import com.lezhin.panther.Context;
import com.lezhin.panther.ErrorCode;
import com.lezhin.panther.SimpleCacheService;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.ExecutorException;
import com.lezhin.panther.exception.FraudException;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.internalpayment.InternalPaymentService;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@Component
public abstract class Command<T extends PGPayment> {

    public enum Type {
        PREPARE {
            @Override
            public Class getCommandClass() {
                return Prepare.class;
            }
        }, RESERVE {
            @Override
            public Class getCommandClass() {
                return Reserve.class;
            }
        }, PREAUTHENTICATE {
            @Override
            public Class getCommandClass() {
                return PreAuthenticate.class;
            }
        }, AUTHENTICATE {
            @Override
            public Class getCommandClass() {
                return Authenticate.class;
            }
        }, PAY {
            @Override
            public Class getCommandClass() {
                return Pay.class;
            }
        }, COMPLETE {
            @Override
            public Class getCommandClass() {
                return Complete.class;
            }
        }, DONE {
            @Override
            public Class getCommandClass() {
                return null;
            }
        };

        public abstract <E> Class<E> getCommandClass();

    }

    private static final Logger logger = LoggerFactory.getLogger(Command.class);

    protected Command.Type commandType;
    protected RequestInfo requestInfo;
    protected Payment<T> payment;
    protected Executor<T> executor;
    protected Context<T> context = null;

    @Autowired
    protected BeanFactory beanFactory;
    @Autowired
    protected PantherProperties pantherProperties;
    @Autowired
    protected InternalPaymentService internalPaymentService;
    @Autowired
    protected SimpleCacheService simpleCacheService;

    public Command() {

    }

    public Command(final RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
        this.payment = requestInfo.getPayment();
        initContext();
    }

    public Command(final Context<T> context) {
        this.context = context;
        this.requestInfo = context.getRequestInfo();
        this.payment = context.getPayment();
    }

    /**
     * Load the payment from persistence and verify precondition.
     * @throws PreconditionException
     */
    public void loadState() throws PreconditionException {

        if (requestInfo.getExecutorType().isAsync()) {
            // callback과 같이 async한 request로 pay step을  요청하는 경우, persisted data와 비교하여 fraud 방지.
            // TODO set payment from InteranlPaymentService(GCS)
            Payment persistedPayment = internalPaymentService.get(context);
            Payment requestPayment = payment;
            checkFraud(requestPayment, persistedPayment);
            payment = persistedPayment;
        } else {
            payment = requestInfo.getExecutorType().createPayment(context);
        }
        verifyPrecondition();
    }

    /**
     * Request로 온 Payment와 DB에 저장된 Payment의 amount를 비교하여 fraud check.
     * @param requestPayment
     * @param persistedPayment
     */
    private void checkFraud(Payment requestPayment, Payment persistedPayment) {
        if (requestPayment.getUserId().longValue() != persistedPayment.getUserId().longValue()) {
            throw new FraudException(executor.getType(),
                    String.format("request.user[%s] is diff with expected[%s]", requestPayment.getUserId(),
                            persistedPayment.getUserId()));
        }

        if (requestPayment.getPaymentId().longValue() != persistedPayment.getPaymentId().longValue()) {
            throw new FraudException(executor.getType(),
                    String.format("request.payment[%s] is diff with expected[%s]", requestPayment.getPaymentId(),
                            persistedPayment.getPaymentId()));
        }

        if (requestPayment.getAmount().floatValue() != persistedPayment.getAmount().floatValue()) {
            throw new FraudException(executor.getType(),
                    String.format("request.amount[%s] is diff with expected[%s]", requestPayment.getAmount(),
                            persistedPayment.getAmount()));
        }
    }

    public void verifyPrecondition() throws PreconditionException {

    }

    protected void initContext() {
        context = Context.builder().requestInfo(requestInfo).payment(payment)
                .responseInfo(new ResponseInfo(ErrorCode.LEZHIN_UNKNOWN)).build();
    }

    /**
     * Create context and executor.
     */
    protected void initExecutor() {
        if (context == null) {
            initContext();
        }
        executor = createExecutor(context);
    }

    protected void initExecutor(final Context context) {
        executor = createExecutor(context);
    }

    protected Executor<T> createExecutor(final Context context) {
        return beanFactory.getBean(requestInfo.getExecutorType().getExecutorClass(), context);
    }

    public abstract Payment<T> execute() throws PreconditionException, ExecutorException;

    public void saveState() {

    }

    public void onFinish() {

    }

    public void onError() {

    }

    public Context<T> getContext() {
        return context;
    }

    /**
     * Process next step. If next step is DONE, this command return payment.
     * @return
     */
    public Payment<T> processNextStep() {
        Command.Type next = executor.nextTransition(commandType);
        if (Type.DONE == next) {
            return payment;
        } else {
            // TODO toBudiler로 create
            Context<T> nextStepContext = context.payment(context.getPayment());
            logger.info("nextStep = {}, context = {} ", next, nextStepContext.printPretty());
            Command nextCommand = beanFactory.getBean(next.getCommandClass(), nextStepContext);
            Payment<T> nextResult = nextCommand.execute();
            return nextResult;
        }
    }
}
