package com.lezhin.panther;

import com.lezhin.panther.exception.CIException;
import com.lezhin.panther.exception.ExceedException;
import com.lezhin.panther.exception.ExecutorException;
import com.lezhin.panther.exception.HappyPointParamException;
import com.lezhin.panther.exception.HappyPointSystemException;
import com.lezhin.panther.exception.InternalPaymentException;
import com.lezhin.panther.exception.LPointException;
import com.lezhin.panther.exception.OwnerException;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.exception.TapjoyException;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;
import com.lezhin.panther.notification.SlackEvent;
import com.lezhin.panther.notification.SlackMessage;
import com.lezhin.panther.notification.SlackNotifier;
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

    private SlackNotifier slackNotifier;

    public ExceptionHandlers(final SlackNotifier slackNotifier) {
        this.slackNotifier = slackNotifier;
    }

    @ExceptionHandler(ParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo handleParameterException(final ParameterException e) {
        logger.error("ParameterException", e);
        slackNotifier.notify(e);
        return new ErrorInfo(ResponseCode.LEZHIN_PARAM.getCode(), e.getMessage());
    }

    @ExceptionHandler(TapjoyException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorInfo handleTapjoyException(final TapjoyException e) {
        /**
         * 만일 탭조이의 콜백 요청에 대해서 200 혹은 403 코드를 리턴해주지 않는다면 탭조이 서버가 4일 동안 2분간격으로 재시도 요청을 하게 됩니다.
         * 또한 서버 응답이 5초 이상 소요되면 해당 통신은 실패로 처리됩니다.
         * - 콜백 요청시 전달된 verifier 값과 secret key를 사용하여 직접 계산한 값이 일치하지 않을때
         * - snuid 값이 서버에서 식별되지 않을때
         * - 이외 보상 지급이 재시도되지 말아야할 오류 발생 시
         */
        slackNotifier.notify(SlackEvent.builder()
                .header("TAPJOY")
                .level(SlackMessage.LEVEL.WARN)
                .title("tapjoy.postback warning")
                .message("return HttpStatus.FORBIDDEN")
                .exception(e)
                .build());
        return new ErrorInfo(ResponseCode.LEZHIN_UNKNOWN.getCode(), e.getMessage());
    }

    @ExceptionHandler(HappyPointParamException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo handleHappypointParamException(final HappyPointParamException e) {
        logger.error("HappyPointParamException", e);
        slackNotifier.notify(e);
        return new ErrorInfo(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(HappyPointSystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleHappypointSystemException(final HappyPointSystemException e) {
        logger.error("HappyPointSystemException", e);
        return new ErrorInfo(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(LPointException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleLPointException(final LPointException e) {
        logger.error("LPointException", e);
        return new ErrorInfo(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(PreconditionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handlePreconditionException(final PreconditionException e) {
        logger.error("PreconditionException", e);
        slackNotifier.notify(e);
        return new ErrorInfo(ResponseCode.LEZHIN_PRECONDITION.getCode(), e.getMessage());
    }

    @ExceptionHandler(ExceedException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    @ResponseBody
    public ErrorInfo handleExceedException(final ExceedException e) {
        logger.error("ExceedException", e);
        return new ErrorInfo(ResponseCode.LEZHIN_EXCEED.getCode(), e.getMessage());
    }

    @ExceptionHandler(CIException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleCIException(final CIException e) {
        logger.error("CIException", e);
        slackNotifier.notify(e);
        return new ErrorInfo(ResponseCode.LEZHIN_CI.getCode(), e.getMessage());
    }

    @ExceptionHandler(ExecutorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleExecutorException(final ExecutorException e) {
        logger.error("ExecutorException", e);
        slackNotifier.notify(e);
        return new ErrorInfo(ResponseCode.LEZHIN_EXECUTION.getCode(), e.getMessage());
    }

    @ExceptionHandler(OwnerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleOwnerException(final OwnerException e) {
        logger.error("OwnerException", e);
        slackNotifier.notify(e);
        // User에게 보여질 수 있는 메시지라 general 하게 변경
        return new ErrorInfo(ResponseCode.LEZHIN_OWNER.getCode(), "Payment Owner Error");
    }

    @ExceptionHandler(InternalPaymentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleInternalPaymentException(final InternalPaymentException e) {
        logger.error("InternalPaymentException", e);
        slackNotifier.notify(e);
        // User에게 보여질 수 있는 메시지라 general 하게 변경
        return new ErrorInfo(ResponseCode.LEZHIN_INTERNAL_PAYMNENT.getCode(), "Internal Server Error");
    }

    @ExceptionHandler(PantherException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handlePantherException(final PantherException e) {
        logger.error("PantherException", e);
        slackNotifier.notify(e);
        return new ErrorInfo(ResponseCode.LEZHIN_PANTHER.getCode(), e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleThrowable(final Throwable e) {
        logger.error("Unexpected error", e);
        slackNotifier.notify(e);
        return new ErrorInfo(ResponseCode.LEZHIN_THROWABLE.getCode(), e.getMessage());
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
