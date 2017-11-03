package com.lezhin.avengers.panther;

import com.lezhin.avengers.panther.exception.ExecutorException;
import com.lezhin.avengers.panther.exception.PantherException;
import com.lezhin.avengers.panther.exception.ParameterException;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.exception.SPCException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author seoeun
 * @since 2017.11.04
 */
@ControllerAdvice
public class ExceptionHandlers {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

    @ExceptionHandler(ParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo handleParameterException(final ParameterException e) {
        logger.error("Parameter error", e);
        return new ErrorInfo(ErrorCode.LEZHIN_PARAM.getCode(), e.getMessage());
    }

    @ExceptionHandler(SPCException.class)
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    @ResponseBody
    public ErrorInfo handleSPCException(final SPCException e) {
        logger.error("SPC error", e);
        return new ErrorInfo(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(PreconditionException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    @ResponseBody
    public ErrorInfo handlePreconditionException(final PreconditionException e) {
        logger.error("Precondition error", e);
        return new ErrorInfo(ErrorCode.LEZHIN_PRECONDITION.getCode(), e.getMessage());
    }

    @ExceptionHandler(ExecutorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleExecutorException(final ExecutorException e) {
        logger.error("Execution error", e);
        return new ErrorInfo(ErrorCode.LEZHIN_EXECUTION.getCode(), e.getMessage());
    }

    @ExceptionHandler(PantherException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handlePantherException(final PantherException e) {
        logger.error("Execution error", e);
        return new ErrorInfo(ErrorCode.LEZHIN_PANTHER.getCode(), e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleThrowable(final Throwable e) {
        logger.error("Unexpected error", e);
        return new ErrorInfo(ErrorCode.LEZHIN_THROWABLE.getCode(), e.getMessage());
    }

    public static class ErrorInfo {
        private String code;
        private String message;

        public ErrorInfo(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
