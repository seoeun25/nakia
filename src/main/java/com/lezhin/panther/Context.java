package com.lezhin.panther;

import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;

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

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder();
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

    public Long getPaymentId() {
        return Optional.ofNullable(payment.getPaymentId()).orElse(-1L);
    }

    public Long getUserId() {
        return Optional.ofNullable(payment.getUserId()).orElse(-1L);
    }

    public boolean executionSucceed() {
        boolean executionSucceed = true;
        if (getRequestInfo().getExecutorType() == Executor.Type.HAPPYPOINT) {
            executionSucceed = getResponseInfo().getCode().equals(ErrorCode.SPC_OK.getCode());
        } else if (getRequestInfo().getExecutorType() == Executor.Type.LGUDEPOSIT) {
            executionSucceed = getResponseInfo().getCode().equals(ErrorCode.LGUPLUS_OK.getCode());
        }
        return executionSucceed;
    }

    public Context<T> request(RequestInfo request) {
        this.requestInfo = request;
        return this;
    }

    public Context<T> payment(Payment<T> payment) {
        this.payment = payment;
        return this;
    }

    public Context<T> response(ResponseInfo response) {
        this.responseInfo = response;
        return this;
    }

    public String printPretty() {
        return MoreObjects.toStringHelper(this)
                .add("type", requestInfo.getExecutorType())
                .add("request.user", requestInfo.getUserId())
                .add("payment.user", getUserId())
                .add("paymert.id", getPaymentId())
                .toString();
    }

    public static class Builder<T extends PGPayment> {
        private Payment<T> payment;
        private RequestInfo requestInfo;
        private ResponseInfo responseInfo;

        public Builder() {

        }

        Builder(RequestInfo requestInfo, Payment<T> payment, ResponseInfo responseInfo) {
            this.requestInfo = requestInfo;
            this.payment = payment;
            this.responseInfo = responseInfo;
        }

        public Builder payment(Payment<T> payment) {
            this.payment = payment;
            return this;
        }

        public Builder requestInfo(RequestInfo requestInfo) {
            this.requestInfo = requestInfo;
            return this;
        }

        public Builder responseInfo(ResponseInfo responseInfo) {
            this.responseInfo = responseInfo;
            return this;
        }

        public Context build() {
            Context context = new Context(this);
            return context;
        }
    }
}
