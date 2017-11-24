package com.lezhin.avengers.panther;

import com.lezhin.avengers.panther.command.Authenticate;
import com.lezhin.avengers.panther.command.Command;
import com.lezhin.avengers.panther.command.Complete;
import com.lezhin.avengers.panther.command.Pay;
import com.lezhin.avengers.panther.command.Prepare;
import com.lezhin.avengers.panther.command.Reserve;
import com.lezhin.avengers.panther.config.LezhinProperties;
import com.lezhin.avengers.panther.exception.ExecutorException;
import com.lezhin.avengers.panther.exception.InternalPaymentException;
import com.lezhin.avengers.panther.exception.PantherException;
import com.lezhin.avengers.panther.exception.ParameterException;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@Service
public class CommandService {

    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private LezhinProperties lezhinProperties;

    @Autowired
    public CommandService() {

    }

    public <T extends PGPayment> Payment<T> doCommand(final Command.Type type, final RequestInfo requestInfo) {
        logger.info("doCommand = [{}, {}, u={}, p={}]", type, requestInfo.getExecutorType(), requestInfo.getUserId(),
                requestInfo.getToken());
        Payment<T> resultPayment = null;
        Command<T> command = null;
        try {

            switch (type) {
                case PREPARE:
                    command = beanFactory.getBean(Prepare.class, requestInfo);
                    break;
                case RESERVE:
                    command = beanFactory.getBean(Reserve.class, requestInfo);
                    break;
                case AUTHENTICATE:
                    command = beanFactory.getBean(Authenticate.class, requestInfo);
                    break;
                case PAY:
                    command = beanFactory.getBean(Pay.class, requestInfo);
                    break;
                case COMPLETE:
                    command = beanFactory.getBean(Complete.class, requestInfo);
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
            throw new PantherException(requestInfo.getExecutorType(), e);
        }

        return resultPayment;
    }

}
