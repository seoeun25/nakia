package com.lezhin.panther;

import com.lezhin.panther.command.Authenticate;
import com.lezhin.panther.command.Command;
import com.lezhin.panther.command.Complete;
import com.lezhin.panther.command.Pay;
import com.lezhin.panther.command.PreAuthenticate;
import com.lezhin.panther.command.Reserve;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * @author seoeun
 * @since 2017.12.19
 */
@Service
public class PagePayService {

    private static final Logger logger = LoggerFactory.getLogger(PagePayService.class);

    @Autowired
    private BeanFactory beanFactory;

    public <T extends PGPayment> Payment<T> doCommand(final Command.Type type, final RequestInfo requestInfo) {
        logger.info("[{}, {}, u={}, p={}]", type, requestInfo.getExecutorType(), requestInfo.getUserId(),
                Optional.ofNullable(requestInfo.getPayment()).map(e -> e.getPaymentId()).orElse(-1l));
        Payment<T> resultPayment = null;
        Command<T> command = null;

        switch (type) {
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
            default:
                throw new PantherException(Executor.Type.DUMMY, "Unsupported type = " + type);
        }
        command.loadState();
        resultPayment = command.execute();


        return resultPayment;
    }

    public <T extends PGPayment> Payment<T> doCommand(final Command.Type type, final Context<T> context) {
        logger.info("[{}, {}, u={}, p={}]", type, context.getRequestInfo().getExecutorType(),
                context.getRequestInfo().getUserId(),
                Optional.ofNullable(context.getRequestInfo().getPayment()).map(e -> e.getPaymentId()).orElse(-1l));
        Payment<T> resultPayment = null;
        Command<T> command = null;

        switch (type) {
            case PREAUTHENTICATE:
                command = beanFactory.getBean(PreAuthenticate.class, context);
                break;
            case AUTHENTICATE:
                command = beanFactory.getBean(Authenticate.class, context);
                break;
            case PAY:
                command = beanFactory.getBean(Pay.class, context);
                break;
            case COMPLETE:
                command = beanFactory.getBean(Complete.class, context);
                break;
            default:
                throw new PantherException(Executor.Type.DUMMY, "Unsupported type = " + type);
        }
        command.loadState();
        resultPayment = command.execute();

        return resultPayment;
    }

    private void mergeParam(HttpServletRequest request, HttpServletResponse response, PGPayment pgPayment) {


    }

}
