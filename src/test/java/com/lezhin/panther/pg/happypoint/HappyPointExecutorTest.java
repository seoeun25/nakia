package com.lezhin.panther.pg.happypoint;

import com.lezhin.panther.Context;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;
import com.lezhin.panther.util.JsonUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static junit.framework.TestCase.assertFalse;
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

    private static final Logger logger = LoggerFactory.getLogger(HappyPointExecutorTest.class);

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
        request1.setContent("{\"_lz_userId\": 10101}".getBytes());
        request1.setParameter("_lz", "4ea0f867-ad9c-4ad7-b024-0b8c258f853d");
        RequestInfo requestInfo = new RequestInfo.Builder(request1, "happypoint").build();

        Context context = Context.builder().requestInfo(requestInfo).payment(requestInfo.getPayment())
                .responseInfo(new ResponseInfo(ResponseCode.LEZHIN_UNKNOWN)).build();

        Long paymentId = context.getPaymentId();
        Long userId = context.getUserId();

        System.out.println(paymentId);
        System.out.println(userId);

        assertEquals(-1L, paymentId.longValue());
        assertEquals(10101L, userId.longValue());
        assertEquals(ResponseCode.LEZHIN_UNKNOWN.getCode(), context.getResponseInfo().getCode());

    }

    /**
     * happypoint 결제 실패시 InternalPaymentService에 보낼 meta(receipt) 테스트.
     *
     * @throws Exception
     */
    @Test
    public void testCreateReceipt() throws Exception {
        Resource resource = new ClassPathResource("/example/happypoint/payment_pay_fail.json");
        assertNotNull(resource.getInputStream());

        System.out.println("length = " + resource.getURI());
        Payment<HappyPointPayment> payment = JsonUtil.fromJsonToPayment(resource.getInputStream(), HappyPointPayment
                .class);
        assertNotNull(payment);
        System.out.println(JsonUtil.toJson(payment));

        Map<String, Object> receipt = payment.getPgPayment().createReceipt();
        receipt.entrySet().stream().forEach(entry -> System.out.println(entry.getKey() + " = " + entry.getValue()));
        assertEquals("1003023370", receipt.get("mbrNo").toString());
        assertEquals("ORJN20171107161021", receipt.get("tracNo").toString());
        assertEquals("2000", receipt.get("trxAmt").toString());
        assertTrue(receipt.containsKey("rpsMsgCtt"));
        assertFalse(receipt.containsKey("rcgnKey"));
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

    /**
     * InternalPayment 에서 verified 되고 complete 되어서 purchase 가 만들어 졌을 때 받는 json 테스트
     *
     * @throws Exception
     */
    @Test
    public void testInteranlPaymentVerified() throws Exception {
        Resource resource = new ClassPathResource("/example/happypoint/internal_verify_response.json");
        assertNotNull(resource.getInputStream());

        System.out.println("length = " + resource.getURI());
        Payment<HappyPointPayment> payment = JsonUtil.fromJsonToPayment(resource.getInputStream(), HappyPointPayment
                .class);
        assertNotNull(payment);
        System.out.println(JsonUtil.toJson(payment));

        // receipt으로 internal로 갔지만 가서 webReceiptData 로 저장. 리턴 json 도 webReceiptData.
        String webReceiptData = payment.getMeta().getWebReceiptData();
        logger.info("webReceiptData = {}", webReceiptData);

    }

    /**
     * ResponseInfo를 기준으로 executor가 succeeded 한지 결정하는 메서드 테스트
     */
    @Test
    public void testExcecutorResult() {

        assertEquals(false, Executor.Type.HAPPYPOINT.succeeded(ResponseInfo.builder()
                .code(ResponseCode.SPC_DENY_44.getCode())
                .description(ResponseCode.SPC_DENY_44.getMessage()).build()));
        assertEquals(false, Executor.Type.HAPPYPOINT.succeeded(ResponseInfo.builder()
                .code("XZXZ")
                .description("hello").build()));
        assertEquals(true, Executor.Type.HAPPYPOINT.succeeded(ResponseInfo.builder()
                .code(ResponseCode.SPC_OK.getCode())
                .description(ResponseCode.SPC_OK.getMessage()).build()));

    }

}
