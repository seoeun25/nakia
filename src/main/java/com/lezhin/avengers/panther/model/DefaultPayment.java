package com.lezhin.avengers.panther.model;

import com.lezhin.beans.entity.common.LezhinLocale;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@Component
@Qualifier("defaultPayment")
public class DefaultPayment extends Payment {

    public DefaultPayment() {
        super();
    }

    public DefaultPayment(Long paymentId) {
        super();
        super.paymentId = paymentId;
    }

    protected Map<String, Object> buildReceiptData() {
        return null;
    }

    protected Map<String, Object> buildReserveViewData(String encoding) {
        return null;
    }

    protected Map<String, Object> buildVerifyViewData(String encoding) {
        return null;
    }

    protected Map<String, Object> buildPaymentViewData(String encoding) {
        return null;
    }

    protected String[] onParse(HttpServletRequest request) {
        return new String[0];
    }

    public String getPGId() {
        return "unknown";
    }
}
