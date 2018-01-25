package com.lezhin.panther.internalpayment;

import com.lezhin.panther.Context;
import com.lezhin.panther.PayService;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.InternalPaymentException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.lguplus.LguplusPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * @author seoeun
 * @since 2018.01.15
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class InternalPaymentServiceTest {

    private static Logger logger = LoggerFactory.getLogger(InternalPaymentServiceTest.class);

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Mock
    private PayService payService;

    private HttpComponentsClientHttpRequestFactory mockClientHttpRequestFactory;

    @Mock
    private PantherProperties mockPantherProperties;

    @Mock
    private Context mockContext;
    @Mock
    private RequestInfo mockRequestInfo;

    @InjectMocks
    private InternalPaymentService internalPaymentService;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
        mockPantherProperties = new PantherProperties();
        mockClientHttpRequestFactory = new
                HttpComponentsClientHttpRequestFactory();
        mockClientHttpRequestFactory.setConnectTimeout(1);
        internalPaymentService = new InternalPaymentService(mockClientHttpRequestFactory, mockPantherProperties);
        MockitoAnnotations.initMocks(this);
    }

    /**
     * InternalPayment의 backoff test. 3번 retry 하고 그것도 실패하면 InternalPaymentException 을 throw 한다
     */
    @Test
    public void testBackoff() {
        LguplusPayment lguplusPayment = LguplusPayment.builder().LGD_BUYER("123").LGD_OID("987")
                .LGD_AMOUNT("1100").LGD_PRODUCTINFO("100coin").build();
        Payment payment = Executor.Type.LGUDEPOSIT.createPayment(lguplusPayment);

        when(mockContext.getPayment()).thenReturn(payment);
        when(mockContext.getRequestInfo()).thenReturn(mockRequestInfo);
        when(mockRequestInfo.getToken()).thenReturn("ABCDE-TOKEN");
        when(mockPantherProperties.getApiUrl()).thenReturn("https://alpha-gcs.lezhin.com:9443");

        try {
            // TODO error dump 해서 Retrying ...... 하는 log 체크.
            internalPaymentService.get(mockContext);
            fail("InternalPaymentService.get should throw exception");
        } catch (InternalPaymentException e) {
            logger.info("Failed aaa", e);
        }

    }

}
