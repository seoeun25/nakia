package com.lezhin.panther.pg.lpoint;

import com.lezhin.constant.PaymentType;
import com.lezhin.panther.Context;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author taemmy
 * @since 2018. 5. 25.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
class LPointExecutorTest {
    private static final Logger logger = LoggerFactory.getLogger(LPointExecutorTest.class);

    @Test
    public void testPrepareParamFromContext() {
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        String requestBody = "{\"_lz_userId\":\"6068546800189440\",\"returnToUrl\":\"a-www.lezhin.com\"," +
                "\"locale\":\"ko-KR\",\"isMobile\":true,\"isApp\":true}";
        request1.setContent(requestBody.getBytes());
        request1.setParameter("_lz", "4ea0f867-ad9c-4ad7-b024-0b8c258f853d");

        RequestInfo requestInfo = new RequestInfo.Builder(request1, "lpoint").build();

        Context context = Context.builder(requestInfo).payment(requestInfo.getPayment())
                .responseInfo(new ResponseInfo(ResponseInfo.ResponseCode.LEZHIN_UNKNOWN)).build();

        Optional<Long> userId = context.getUserId();

        assertEquals(6068546800189440L, userId.get().longValue());
        assertEquals(ResponseInfo.ResponseCode.LEZHIN_UNKNOWN.getCode(), context.getResponseInfo().getCode());

    }

    @Test
    public void testReserveParamFromContext() {
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        String requestBody = "{\"returnTo\":\"a-www.lezhin.com\"," +
                "\"_lz_externalStoreProductId\":\"lz_dynamic_lpoint_mlpoint_200\"," +
                "\"locale\":\"ko-KR\",\"_lz_userId\":6068546800189440,\"_lz_storeVersion\":\"1.1\"," +
                "\"_lz_store\":\"plus\",\"isMobile\":true,\"isApp\":true," +
                "\"pgPayment_ctfCno\":\"2200001002\",\"pgPayment_akCvPt\":1000}";
        request1.setContent(requestBody.getBytes());
        request1.setParameter("_lz", "4ea0f867-ad9c-4ad7-b024-0b8c258f853d");

        RequestInfo requestInfo = new RequestInfo.Builder(request1, "lpoint").build();

        Context context = Context.builder(requestInfo).payment(requestInfo.getPayment())
                .responseInfo(new ResponseInfo(ResponseInfo.ResponseCode.LEZHIN_UNKNOWN)).build();

        Optional<Long> userId = context.getUserId();
        assertEquals(6068546800189440L, userId.get().longValue());

        Payment<LPointPayment> payment = context.getPayment();

        LPointPayment pgPayment = payment.getPgPayment();
        assertEquals("2200001002", pgPayment.getCtfCno());
        assertEquals(1000, pgPayment.getAkCvPt().intValue());

        assertEquals(ResponseInfo.ResponseCode.LEZHIN_UNKNOWN.getCode(), context.getResponseInfo().getCode());
    }

    @Test
    public void testPreauthParamFromContext() {
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        String requestBody = "{\"returnTo\":\"a-www.lezhin.com\",\"_lz_externalStoreProductId\":\"lz_dynamic_lpoint_mlpoint_200\"," +
                "\"locale\":\"ko-KR\",\"_lz_userId\":6068546800189440," +
                "\"_lz_storeVersion\":\"1.1\",\"_lz_store\":\"plus\",\"isMobile\":true,\"isApp\":true," +
                "\"pgPayment_ctfCno\":\"2200001002\",\"pgPayment_akCvPt\":1000," +
                "\"pgPayment_pswd\":\"lotte123\",\"paymentId\":1528867471101352}";
        request1.setContent(requestBody.getBytes());
        request1.setParameter("_lz", "4ea0f867-ad9c-4ad7-b024-0b8c258f853d");

        RequestInfo requestInfo = new RequestInfo.Builder(request1, "lpoint").build();

        Context context = Context.builder(requestInfo).payment(requestInfo.getPayment())
                .responseInfo(new ResponseInfo(ResponseInfo.ResponseCode.LEZHIN_UNKNOWN)).build();

        Optional<Long> userId = context.getUserId();
        assertEquals(6068546800189440L, userId.get().longValue());

        assertEquals(true, context.getRequestInfo().getIsMobile());
        assertEquals(true, context.getRequestInfo().getIsApp());

        Payment<LPointPayment> payment = context.getPayment();
        assertEquals(1528867471101352L, payment.getPaymentId().longValue());

        LPointPayment pgPayment = payment.getPgPayment();
        assertEquals("2200001002", pgPayment.getCtfCno());
        assertEquals(1000, pgPayment.getAkCvPt().intValue());
        assertEquals("lotte123", pgPayment.getPswd());

        assertEquals(ResponseInfo.ResponseCode.LEZHIN_UNKNOWN.getCode(), context.getResponseInfo().getCode());
    }

    @Test
    public void testCreateReceipt() throws Exception {
        Resource resource = new ClassPathResource("/example/lpoint/o420_use_loint.json");
        assertNotNull(resource.getInputStream());

        logger.info("url = " + resource.getURI());
        LPointPayment pgPayment = JsonUtil.fromJson(resource.getInputStream(), LPointPayment.class);

        Map<String, Object> receipt = pgPayment.createReceipt();
        receipt.entrySet().stream().forEach(entry -> System.out.println(entry.getKey() + " = " + entry.getValue()));
        assertEquals("O440O73020180613113555", receipt.get("flwNo"));
        assertEquals("00", receipt.get("rspC"));
        assertEquals("031006753", receipt.get("aprno"));
        assertEquals("20180613", receipt.get("aprDt"));
        assertEquals("113556", receipt.get("aprHr"));
        assertEquals(200, receipt.get("ttnCvPt"));
        assertEquals(998740, receipt.get("avlPt"));
        assertEquals(29800, receipt.get("cvAvlPt"));
    }

    @Test
    public void testExecutorResult() {
        assertEquals(true, Executor.Type.LPOINT.succeeded(ResponseInfo.builder()
                .code(ResponseInfo.ResponseCode.LPOINT_OK.getCode())
                .description(ResponseInfo.ResponseCode.LPOINT_OK.getMessage()).build()));
        assertEquals(false, Executor.Type.LPOINT.succeeded(ResponseInfo.builder()
                .code(ResponseInfo.ResponseCode.LPOINT_EXCEED_DAY.getCode())
                .description(ResponseInfo.ResponseCode.LPOINT_EXCEED_DAY.getMessage()).build()));
        assertEquals(false, Executor.Type.LPOINT.succeeded(ResponseInfo.builder()
                .code("88")
                .description("DB미등록").build()));
    }

    /**
     * lpoint I/F 연동은 사무실, AWS환경에서만 가능
     *
    @Autowired
    private BeanFactory beanFactory;
    private Payment<LPointPayment> payment;

    private String _CI = "F1oTBgLV/6LRb9gUpxcM/VIRiLGUfTw5GaIZT1dFKZFaeEtxv8TjmK/bgaFriIPjgUE4f/OSfhENo+cb/DsFRQ==";
    private String _CTF_CNO = "2200001002";
    private String _PSWD = "lotte123";

    @BeforeEach
    public void setup() {
        payment = new Payment<>();
        payment.setPaymentType(PaymentType.lpoint);
        payment.setUserId(6068546800189440L);

        LPointPayment dummy = new LPointPayment();
        payment.setPgPayment(dummy);

    }

    @Test
    public void test_find_lpoint_member() {
        payment.getPgPayment().setCiNo(_CI);

        LPointExecutor executor = getExecutor();
        assertNotNull(executor);

        LPointPayment req = executor.makePayload(LPointPayment.API.AUTHENTICATION);
        LPointPayment response = executor.request(req);

        assertNotNull(response);
        logger.info("response - rspC: {}, msg: {}", response.getControl().getRspC(), response.getMsgCn1());
        assertEquals("00", response.getControl().getRspC());
    }

    private LPointExecutor getExecutor() {
        RequestInfo requestInfo = new RequestInfo.Builder(payment, "lpoint").build();
        Context context = Context.builder(requestInfo)
                .payment(requestInfo.getPayment())
                .responseInfo(new ResponseInfo(ResponseInfo.ResponseCode.LEZHIN_UNKNOWN))
                .build();

        return beanFactory.getBean(requestInfo.getExecutorType().getExecutorClass(), context);
    }
     */
}