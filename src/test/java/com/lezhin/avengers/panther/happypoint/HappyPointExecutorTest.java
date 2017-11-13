package com.lezhin.avengers.panther.happypoint;

import com.lezhin.avengers.panther.Context;
import com.lezhin.avengers.panther.model.Certification;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;
import com.lezhin.avengers.panther.util.JsonUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author seoeun
 * @since 2017.11.07
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class HappyPointExecutorTest {

    @Autowired
    private HappyPointExecutor executor;

    @Test
    public void testTrancNo() {
        assertNotNull(executor);

        String traceNo = executor.createTraceNo("ORJN", "20171109", "103116", 12345L, null);
        System.out.println(traceNo);

        System.out.println("size = " + traceNo.length());

        assertTrue(traceNo.length() <= 20);

        assertTrue(executor.createTraceNo("ORJN", "20171109", "103116", 12345L, null)
                .startsWith("ORJN20171109103116"));
    }

    @Test
    public void testUserIdPaymentIdFromContext() {

        MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.setContent("{\"_lz_userId\": \"10101\"}".getBytes());
        RequestInfo requestInfo = new RequestInfo.Builder(request1, "happypoint").build();

        Context context = new Context.Builder(requestInfo, requestInfo.getPayment()).build();

        String paymentId = context.getPaymentId();
        String userId = context.getUserId();

        System.out.println(paymentId);
        System.out.println(userId);

        assertEquals("-1", paymentId);
        assertEquals("10101", userId);

    }

    @Test
    public void test() {
        Certification certification = new Certification();
        certification.setBirthday("19980101");
        certification.setName("홍길동");
        certification.setCI("CI_ZZZZZ_XXXX");
        certification.setDI("DI_XXXXXXXXXXX");
        certification.setGender("M");

        System.out.println(JsonUtil.toJson(certification));
    }

    @Test
    public void testCreateReceipt() throws Exception {
        Resource resource = new ClassPathResource("/example/happypoint/internal_payment_fail_request.json");
        assertNotNull(resource.getInputStream());

        System.out.println("length = " + resource.getURI());
        // TODO
        //Payment<HappyPointPayment> payment = JsonUtil.fromJsonToPayment(resource.getInputStream(), HappyPointPayment
        //        .class);
        //assertNotNull(payment);
        //System.out.println(JsonUtil.toJson(payment));

        //Map<String,String> receipt = payment.getPgPayment().createReceipt();
        //receipt.entrySet().stream().forEach(entry -> System.out.println(entry.getKey() + " = " + entry.getValue()));
    }

    @Test
    public void testInternalAuth() throws Exception {
        // InternalPayment authenticate 로 받은 response
        Resource resource = new ClassPathResource("/example/happypoint/internal_auth_response.json");
        assertNotNull(resource.getInputStream());

        System.out.println("length = " + resource.getURI());
        Payment<HappyPointPayment> payment = JsonUtil.fromJsonToPayment(resource.getInputStream(), HappyPointPayment.class);
        assertNotNull(payment);
        System.out.println(JsonUtil.toJson(payment));

    }
}
