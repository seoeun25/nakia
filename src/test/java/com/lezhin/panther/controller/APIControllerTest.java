package com.lezhin.panther.controller;

import com.lezhin.panther.PayService;
import com.lezhin.panther.ErrorCode;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author seoeun
 * @since 2017.11.03
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class APIControllerTest {

    private static Logger logger = LoggerFactory.getLogger(APIControllerTest.class);

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Mock
    private PayService payService;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Test
    public void testHealthCheck() throws Exception {

        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/panther");
        request1.setParameter("_lz_userId", "10101");
        Payment<PGPayment> mockPayment = new Payment<>();

        this.mockMvc.perform(get
                ("/panther").accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .param("_lz_userId", "121212"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }

    /**
     * Origin이 www.lezhin.com 일 때만 200 return.
     *
     * @throws Exception
     */
    @Test
    public void testCORS() throws Exception {

        // PreFlight from www.lezhin.com. OK
        this.mockMvc
                .perform(options("/v1/api/happypoint/reservation")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://www.lezhin.com")
                        .param("_lz_userId", "1212121"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,HEAD,POST"));

        this.mockMvc
                .perform(options("/v1/api/happypoint/reservation")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://beta-www.lezhin.com")
                        .param("_lz_userId", "1212121"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,HEAD,POST"));


        // PreFlight from abc.lezhin.com. forbidden.
        this.mockMvc
                .perform(options("/v1/api/happypoint/reservation")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://abc.lezhin.com")
                        .param("_lz_userId", "1212121"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Methods"));

    }

    /**
     * ParameterException Handle.
     */
    @Test
    public void testParameterException() throws Exception {

        this.mockMvc
                .perform(post("/v1/api/hello/reservation").content("{\"_lz_userId\":\"10101\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.LEZHIN_PARAM.getCode()));

    }

    /**
     * ParameterException Handle.
     */
    @Test
    public void testPayLetter() throws Exception {

        this.mockMvc
                .perform(get("/payletter/v1/logs")
                        .param("fromYMD", "20171207")
                        .param("toYMD", "20171208")
                        .param("locale", "ja-JP")
                )
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("code").value(Integer.parseInt(ErrorCode.LEZHIN_OK.getCode())));
    }

}
