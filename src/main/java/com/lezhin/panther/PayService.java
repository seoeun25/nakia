package com.lezhin.panther;

import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.ExecutorException;
import com.lezhin.panther.exception.InternalPaymentException;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.step.Authenticate;
import com.lezhin.panther.step.Cancel;
import com.lezhin.panther.step.Command;
import com.lezhin.panther.step.Complete;
import com.lezhin.panther.step.Pay;
import com.lezhin.panther.step.Prepare;
import com.lezhin.panther.step.Refund;
import com.lezhin.panther.step.Reserve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@Service
public class PayService {

    private static final Logger logger = LoggerFactory.getLogger(PayService.class);

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private PantherProperties pantherProperties;

    @Autowired
    public PayService() {

    }

    public <T extends PGPayment> Payment<T> doCommand(final Command.Type type, final Context context) {
        logger.info("[{}, u={}, p={}] {}", context.getRequestInfo().getExecutorType(),
                context.getRequestInfo().getUserId(),
                Optional.ofNullable(context.getRequestInfo().getPayment()).map(e -> e.getPaymentId()).orElse(null),
                type);
        Payment<T> resultPayment = null;
        Command<T> command = null;
        try {

            switch (type) {
                case PREPARE:
                    command = beanFactory.getBean(Prepare.class, context);
                    break;
                case RESERVE:
                    command = beanFactory.getBean(Reserve.class, context);
                    break;
                case AUTHENTICATE:
                    command = beanFactory.getBean(Authenticate.class, context);
                    break;
                case PAY:
                    command = beanFactory.getBean(Pay.class, context);
                    break;
                case CANCEL:
                    command = beanFactory.getBean(Cancel.class, context);
                    break;
                case REFUND:
                    command = beanFactory.getBean(Refund.class, context);
                    break;
                case COMPLETE:
                    command = beanFactory.getBean(Complete.class, context);
                    break;
            }
            command.loadState();
            resultPayment = command.execute();

        } catch (ParameterException e) {
            logger.warn("Parameter check failed !!!");

        } catch (PreconditionException e) {
            logger.warn("Precondition check failed !!!");
            throw e;
        } catch (InternalPaymentException e) {
            logger.warn("InternalPayment failed !!!");
            throw e;
        } catch (ExecutorException e) {
            logger.warn("Execution failed !!!");
            throw e;
        } catch (PantherException e) {
            logger.warn("Panther failed !!!");
            throw e;
        } catch (Throwable e) {
            logger.warn("Unexpected error. failed !!!");
            throw new PantherException(context.getRequestInfo().getExecutorType(), e);
        }

        return resultPayment;
    }

}
