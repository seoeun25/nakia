package com.lezhin.avengers.panther.happypoint;


import com.lezhin.avengers.panther.model.DefaultPayment;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author seoeun
 * @since 2017.10.25
 */
public class HappyPointPayment extends DefaultPayment {

    public HappyPointPayment() {
        
    }

    public HappyPointPayment(Long paymentId) {
        super(paymentId);
    }
    @Override
    protected Map<String, Object> buildMetaData() {
        return null;
    }

    @Override
    protected Map<String, Object> buildReceiptData() {
        return null;
    }

    @Override
    protected Map<String, Object> buildReserveViewData(String encoding) {
        return null;
    }

    @Override
    protected Map<String, Object> buildVerifyViewData(String encoding) {
        return null;
    }

    @Override
    protected Map<String, Object> buildPaymentViewData(String encoding) {
        return null;
    }

    @Override
    protected String[] onParse(HttpServletRequest request) {
        return new String[0];
    }

    @Override
    public String getPGId() {
        return null;
    }
}
