package com.lezhin.panther;

import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;

import java.util.Optional;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class Context<T extends PGPayment> {

    private Payment<T> payment;
    private RequestInfo requestInfo;
    private ResponseInfo responseInfo;
    private Long paymentId;
    private Long userId;
    private Executor.Type type;

    public Context(Builder<T> builder) {
        this.payment = builder.payment;
        this.requestInfo = builder.requestInfo;
        this.responseInfo = builder.responseInfo;
        Optional.ofNullable(requestInfo).orElseThrow(() -> new PantherException("RequestInfo can not be null"));
        this.userId = requestInfo.getUserId();
        this.type = requestInfo.getExecutorType();
        this.paymentId = Optional.ofNullable(payment).map(a -> payment.getPaymentId()).orElse(null);
    }

    public static Builder builder(RequestInfo requestInfo) {
        return new Builder(requestInfo);
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

    public Optional<Long> getPaymentId() {
        return Optional.ofNullable(new Long(-1L));
    }

    public Optional<Long> getUserId() {
        return Optional.ofNullable(userId);
    }

    public Executor.Type getType() {
        return type;
    }

    public boolean executionSucceed() {
        return getRequestInfo().getExecutorType().succeeded(getResponseInfo());
    }

    public Context<T> request(RequestInfo request) {
        this.requestInfo = request;
        return this;
    }

    public Context<T> payment(Payment<T> payment) {
        if (payment == null) {
            throw new PantherException(this, "Payment can not be null");
        }
        this.payment = payment;
        this.paymentId = payment.getPaymentId();
        // TODO userId 셋팅은 ??
        return this;
    }

    public Context<T> response(ResponseInfo response) {
        this.responseInfo = response;
        return this;
    }

    public String print() {
        StringBuilder builder = new StringBuilder();
        builder.append("[" + type);
        builder.append(", u=" + userId);
        builder.append(", p=" + paymentId + "]");
        return builder.toString();
    }

    public static class Builder<T extends PGPayment> {
        private Payment<T> payment;
        private RequestInfo requestInfo;
        private ResponseInfo responseInfo;

        public Builder(RequestInfo requestInfo) {
            this.requestInfo = requestInfo;
        }

        public Builder payment(Payment<T> payment) {
            this.payment = payment;
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
