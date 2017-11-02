package com.lezhin.avengers.panther;

import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class Context<P extends Payment> {

    private P payment;
    private RequestInfo requestInfo;

    public Context(Builder<P> builder) {
        this.payment = builder.payment;
        this.requestInfo = builder.requestInfo;
    }

    public P getPayment() {
        return payment;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public static class Builder<P extends Payment> {
        private P payment;
        private RequestInfo requestInfo;

        public Builder(RequestInfo requestInfo, P payment) {
            this.requestInfo = requestInfo;
            this.payment = payment;
        }

        public Context build() {
            Context context = new Context(this);
            return context;
        }
    }
}
