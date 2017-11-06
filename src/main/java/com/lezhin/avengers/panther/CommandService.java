package com.lezhin.avengers.panther;

import com.lezhin.avengers.panther.command.Authenticate;
import com.lezhin.avengers.panther.command.Command;
import com.lezhin.avengers.panther.command.Complete;
import com.lezhin.avengers.panther.command.Pay;
import com.lezhin.avengers.panther.command.Prepare;
import com.lezhin.avengers.panther.command.Reserve;
import com.lezhin.avengers.panther.exception.ExecutorException;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public CommandService() {

    }

    public <P extends Payment> P doCommand(final Command.Type type, final RequestInfo requestInfo) {
        logger.info("doCommand = {}, {}", type, requestInfo.getExecutorType());
        P resultPayment = null;
        Command<P> command = null;
        try {

            switch (type) {
                case PREPARE:
                    command = new Prepare(requestInfo);
                    break;
                case RESERVE:
                    command = new Reserve(requestInfo);
                    break;
                case AUTHENTICATE:
                    command = new Authenticate(requestInfo);
                    break;
                case PAY:
                    command = new Pay(requestInfo);
                    break;
                case COMPLETE:
                    command = new Complete(requestInfo);
                    break;
            }

            command.loadState();
            resultPayment = command.execute();

        } catch (PreconditionException e) {
            logger.warn("---- precondition check failed", e);

        } catch (ExecutorException e) {
            logger.warn("---- execution failed ", e);

        } catch (Exception e) {
            logger.warn("---- unknown error. failed. ", e);
        }

        return resultPayment;
    }

}
