package com.lezhin.avengers.panther;

import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class Context<T extends PGPayment> {

    private Payment<T> payment;
    private RequestInfo requestInfo;

    public Context(Builder<T> builder) {
        this.payment = builder.payment;
        this.requestInfo = builder.requestInfo;
    }

    public Payment<T> getPayment() {
        return payment;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public static class Builder<T extends PGPayment> {
        private Payment<T> payment;
        private RequestInfo requestInfo;

        public Builder(RequestInfo requestInfo, Payment<T> payment) {
            this.requestInfo = requestInfo;
            this.payment = payment;
        }

        public Context build() {
            Context context = new Context(this);
            return context;
        }
    }
}
