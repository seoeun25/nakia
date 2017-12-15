package com.lezhin.panther.util;

import com.lezhin.panther.PantherConfiguration;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author seoeun
 * @since 2017.11.07
 */

public class JsonUtil {

    private static ObjectMapper getObjectMapper() {
        return new PantherConfiguration().objectMapper();
    }

    public static <T> T fromJson(String jsonString, Class<T> claz) {
        try {
            return getObjectMapper().readValue(jsonString, claz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJson(Object obj) {
        try {
            return getObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends PGPayment> Payment<T> fromJsonToPayment(String jsonString, Class<T> claz) {
        return fromJsonToPayment(new ByteArrayInputStream(jsonString.getBytes()), claz);
    }

    public static <T extends PGPayment> Payment<T> fromJsonToPayment(InputStream inputStream, Class<T> claz) {
        try {
            JavaType javaType = getObjectMapper().getTypeFactory().constructParametricType(Payment.class, claz);
            Payment<T> payment = getObjectMapper().readValue(inputStream, javaType);
            return payment;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
