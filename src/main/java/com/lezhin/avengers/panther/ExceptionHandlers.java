package com.lezhin.avengers.panther;

import com.lezhin.avengers.panther.exception.ExceedException;
import com.lezhin.avengers.panther.exception.ExecutorException;
import com.lezhin.avengers.panther.exception.HappyPointParamException;
import com.lezhin.avengers.panther.exception.HappyPointSystemException;
import com.lezhin.avengers.panther.exception.PantherException;
import com.lezhin.avengers.panther.exception.ParameterException;
import com.lezhin.avengers.panther.exception.PreconditionException;

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
        logger.error("ParameterException", e);
        return new ErrorInfo(ErrorCode.LEZHIN_PARAM.getCode(), e.getMessage());
    }

    @ExceptionHandler(HappyPointParamException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo handleHappypointParamException(final HappyPointParamException e) {
        logger.error("HappyPointParamException", e);
        return new ErrorInfo(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(HappyPointSystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleHappypointSystemException(final HappyPointSystemException e) {
        logger.error("HappyPointSystemException", e);
        return new ErrorInfo(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(PreconditionException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    @ResponseBody
    public ErrorInfo handlePreconditionException(final PreconditionException e) {
        logger.error("PreconditionException", e);
        return new ErrorInfo(ErrorCode.LEZHIN_PRECONDITION.getCode(), e.getMessage());
    }

    @ExceptionHandler(ExceedException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    @ResponseBody
    public ErrorInfo handleExceedException(final ExceedException e) {
        logger.error("ExceedException", e);
        return new ErrorInfo(ErrorCode.LEZHIN_EXCEED.getCode(), e.getMessage());
    }

    @ExceptionHandler(ExecutorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleExecutorException(final ExecutorException e) {
        logger.error("ExecutorException", e);
        return new ErrorInfo(ErrorCode.LEZHIN_EXECUTION.getCode(), e.getMessage());
    }

    @ExceptionHandler(PantherException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handlePantherException(final PantherException e) {
        logger.error("PantherException", e);
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
        private String description; // FRONT에서 description 사용. 고치면 안됨.

        public ErrorInfo(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
