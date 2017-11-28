package com.lezhin.avengers.panther;

import com.lezhin.avengers.panther.executor.Executor;
import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;
import com.lezhin.avengers.panther.model.ResponseInfo;

import com.google.common.base.MoreObjects;

import java.util.Optional;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class Context<T extends PGPayment> {

    private Payment<T> payment;
    private RequestInfo requestInfo;
    private ResponseInfo responseInfo;

    public Context(Builder<T> builder) {
        this.payment = builder.payment;
        this.requestInfo = builder.requestInfo;
        this.responseInfo = builder.responseInfo;
    }

    public Payment<T> getPayment() {
        return payment;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public ResponseInfo getResponseInfo() {
        return responseInfo;
    }

    public String getPaymentId() {
        return Optional.ofNullable(payment.getPaymentId()).orElse(-1L).toString();
    }

    public String getUserId() {
        return Optional.ofNullable(payment.getUserId()).orElse(-1L).toString();
    }

    // FIXME
    public boolean isPantherFail() {
        return responseInfo.getCode().length() == 4 && !responseInfo.getCode().equals(ErrorCode.LEZHIN_OK);
    }

    // FIXME
    public boolean isHappyPointFail() {
        return responseInfo.getCode().length() == 2 && !responseInfo.getCode().equals(ErrorCode.SPC_OK);
    }

    public boolean executionSucceed() {
        boolean executionSucceed = true;
        if (getRequestInfo().getExecutorType() == Executor.Type.HAPPYPOINT) {
            executionSucceed = getResponseInfo().getCode().equals(ErrorCode.SPC_OK);
        }
        return executionSucceed;
    }

    public Context<T> withPayment(Payment<T> payment) {
        return new Context.Builder(requestInfo, payment).build();
    }

    public Context<T> withResponse(ResponseInfo response) {
        return new Context.Builder(requestInfo, payment, response).build();
    }

    public static class Builder<T extends PGPayment> {
        private Payment<T> payment;
        private RequestInfo requestInfo;
        private ResponseInfo responseInfo;

        public Builder(RequestInfo requestInfo, Payment<T> payment) {
            this.requestInfo = requestInfo;
            this.payment = payment;
            this.responseInfo = new ResponseInfo(ErrorCode.LEZHIN_UNKNOWN);
        }

        public Builder(RequestInfo requestInfo, Payment<T> payment, ResponseInfo responseInfo) {
            this.requestInfo = requestInfo;
            this.payment = payment;
            this.responseInfo = responseInfo;
        }

        public Context build() {
            Context context = new Context(this);
            return context;
        }
    }

    public String printPretty() {
        return MoreObjects.toStringHelper(this)
                .add("type", requestInfo.getExecutorType())
                .add("request.user", requestInfo.getUserId())
                .add("payment.user", getUserId())
                .add("paymert.id", getPaymentId())
                .toString();
    }
}
