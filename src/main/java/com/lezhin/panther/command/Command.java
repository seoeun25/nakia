package com.lezhin.panther.command;

import com.lezhin.panther.Context;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.ExecutorException;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.internalpayment.InternalPaymentService;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;

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


    public Command() {

    }

    public Command(final RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
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

        // TODO set payment from InteranlPaymentService(GCS)
        // payment = internalService.getPayment(); 임시로 executor 에서.
        payment = requestInfo.getExecutorType().createPayment(context);
        verifyPrecondition();
    }

    public void verifyPrecondition() throws PreconditionException {

    }

    /**
     * Create context and executor.
     */
    protected void initExecutor() {
        if (context == null) {
            context = new Context.Builder<T>(requestInfo, payment).build();
        }
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
            logger.info("DONE {}", getClass().getSimpleName());
            return payment;
        } else {
            Context<T> nextStepContext = context.withPayment(context.getPayment());
            logger.info("nextStep = {}, context = {} ", next, nextStepContext.printPretty());
            Command nextCommand = beanFactory.getBean(next.getCommandClass(), nextStepContext);
            Payment<T> nextResult = nextCommand.execute();
            return nextResult;
        }
    }
}
