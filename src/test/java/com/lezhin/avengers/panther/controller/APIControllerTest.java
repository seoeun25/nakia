package com.lezhin.avengers.panther.controller;

import com.lezhin.avengers.panther.CommandService;
import com.lezhin.avengers.panther.ErrorCode;
import com.lezhin.avengers.panther.command.Command;
import com.lezhin.avengers.panther.exception.ExecutorException;
import com.lezhin.avengers.panther.happypoint.HappyPointPayment;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
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
    private CommandService commandService;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Test
    public void testHealthCheck() throws Exception {

        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/v1/api/happypoint/reservation");
        Payment mockPayment = new HappyPointPayment();

        Mockito.when(commandService.doCommand(Command.Type.RESERVE, new RequestInfo.Builder(request1).build()))
                .thenReturn(mockPayment);

        this.mockMvc.perform(get
                ("/v1/api/happypoint/reservation").accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
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
        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/v1/api/happypoint/reservation");
        Payment mockPayment = new HappyPointPayment();

        Mockito.when(commandService.doCommand(Command.Type.RESERVE, new RequestInfo.Builder(request1).build()))
                .thenReturn(mockPayment);

        // PreFlight from www.lezhin.com. OK
        this.mockMvc
                .perform(options("/v1/api/happypoint/reservation")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://www.lezhin.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,HEAD,POST"));


        // PreFlight from abc.lezhin.com. forbidden.
        this.mockMvc
                .perform(options("/v1/api/happypoint/reservation")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://abc.lezhin.com"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Methods"));

    }

    /**
     * ParameterException Handle.
     */
    @Test
    public void testParameterException() throws Exception {
        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/v1/api/hello/reservation");
        Payment mockPayment = new HappyPointPayment();

        this.mockMvc
                .perform(get("/v1/api/hello/reservation"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.LEZHIN_PARAM.getCode()));

    }

}
