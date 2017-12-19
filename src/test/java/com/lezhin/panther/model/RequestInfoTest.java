package com.lezhin.panther.model;

import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.executor.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author seoeun
 * @since 2017.10.25
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class RequestInfoTest {

    private static Logger logger = LoggerFactory.getLogger(RequestInfoTest.class);

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DispatcherServlet servlet;

    private MockMvc mockMvc;
    private MockHttpServletRequest request1;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    private MockHttpServletRequest createHttpServletRequest() {
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.setParameter("_lz_userId", "10101");
        return request1;
    }

    @Test
    public void testIp() throws Exception {

        // ip from remoteAddr
        MockHttpServletRequest request1 = createHttpServletRequest();
        request1.setParameter("_lz", "4ea0f867-ad9c-4ad7-b024-0b8c258f853d");
        request1.setContent("{\"_lz_userId\": 6066665726148608}".getBytes());
        request1.addHeader("No", "127.0.0.1");
        request1.setRemoteAddr("192.168.0.11");
        assertEquals("192.168.0.11", new RequestInfo.Builder(request1, "happypoint").build().getIp());

        // ip from Forwarded
        request1 = createHttpServletRequest();
        request1.setParameter("_lz", "4ea0f867-ad9c-4ad7-b024-0b8c258f853d");
        request1.setContent("{\"_lz_userId\": 10101}".getBytes());
        request1.addHeader("Forwarded", "192.168.0.1");
        request1.addHeader("X-Forwarded-For", "192.168.0.2");
        request1.addHeader("X-Forwarded", "192.168.0.3");
        request1.addHeader("X-Cluster-Client-Ip", "192.168.0.4");
        request1.addHeader("Client-Ip", "192.168.0.5");
        request1.setRemoteAddr("192.168.0.11");
        assertEquals("192.168.0.1", new RequestInfo.Builder(request1, "happypoint").build().getIp());

        // ip from X-Forwarded-For
        request1 = createHttpServletRequest();
        request1.setParameter("_lz", "4ea0f867-ad9c-4ad7-b024-0b8c258f853d");
        request1.setContent("{\"_lz_userId\": 10101}".getBytes());
        request1.addHeader("X-Forwarded-For", "192.168.0.2");
        request1.addHeader("X-Forwarded", "192.168.0.3");
        assertEquals("192.168.0.2", new RequestInfo.Builder(request1, "happypoint").build().getIp());

        // ip from X-Forwarded
        request1 = createHttpServletRequest();
        request1.setParameter("_lz", "4ea0f867-ad9c-4ad7-b024-0b8c258f853d");
        request1.setContent("{\"_lz_userId\": 10101}".getBytes());
        request1.addHeader("X-Forwarded", "192.168.0.3");
        request1.addHeader("Client-Ip", "192.168.0.5");
        assertEquals("192.168.0.3", new RequestInfo.Builder(request1, "happypoint").build().getIp());

        // ip from X-Cluster-Client-Ip
        request1 = createHttpServletRequest();
        request1.setParameter("_lz", "4ea0f867-ad9c-4ad7-b024-0b8c258f853d");
        request1.setContent("{\"_lz_userId\": 10101}".getBytes());
        request1.addHeader("X-Cluster-Client-Ip", "192.168.0.4");
        assertEquals("192.168.0.4", new RequestInfo.Builder(request1, "happypoint").build().getIp());

        // ip from Client-Ip
        request1 = createHttpServletRequest();
        request1.setParameter("_lz", "4ea0f867-ad9c-4ad7-b024-0b8c258f853d");
        request1.setContent("{\"_lz_userId\": 10101}".getBytes());
        request1.addHeader("Client-Ip", "192.168.0.5");
        request1.setRemoteAddr("192.168.0.11");
        assertEquals("192.168.0.5", new RequestInfo.Builder(request1, "happypoint").build().getIp());
    }

    @Test
    public void testToken() throws Exception {

        assertNotNull(mockMvc);
        assertNotNull(servlet);

        // token = null
        try {
            MockHttpServletRequest request1 = createHttpServletRequest();
            request1.setContent("{\"_lz_userId\": 10101}".getBytes());
            request1.addHeader("No", "1ea0f867-ad9c-4ad7-b024-0b8c258f853a");
            new RequestInfo.Builder(request1, "happypoint").build().getToken();
            fail("Should throw ParameterException");
        } catch (ParameterException e) {

        }

        // token from parameter
        request1 = createHttpServletRequest();
        request1.setContent("{\"_lz_userId\": 10101}".getBytes());
        request1.addHeader("No", "1ea0f867-ad9c-4ad7-b024-0b8c258f853a");
        request1.setAttribute("No", "2ea0f867-ad9c-4ad7-b024-0b8c258f853b");
        request1.setCookies(new Cookie("_lz_no", "3ea0f867-ad9c-4ad7-b024-0b8c258f853c"));
        request1.setParameter("_lz", "4ea0f867-ad9c-4ad7-b024-0b8c258f853d");
        assertEquals("4ea0f867-ad9c-4ad7-b024-0b8c258f853d", new RequestInfo.Builder(request1, "happypoint").build().getToken());

        // token from cookie
        request1 = createHttpServletRequest();
        request1.setContent("{ \"_lz_userId\": 10101 , \"_lz\": \"4ea0f867-ad9c-4ad7-b024-0b8c258f853d\"}"
                .getBytes());
        request1.setCookies(new Cookie("_lz", "3ea0f867-ad9c-4ad7-b024-0b8c258f853c"));
        assertEquals("3ea0f867-ad9c-4ad7-b024-0b8c258f853c", new RequestInfo.Builder(request1, "happypoint").build().getToken());

        // token from header
        request1 = createHttpServletRequest();
        request1.setContent("{\"_lz_userId\": 10101}".getBytes());
        request1.addHeader("Authorization", "1ea0f867-ad9c-4ad7-b024-0b8c258f853a");
        request1.setAttribute("Authorization", "2ea0f867-ad9c-4ad7-b024-0b8c258f853b");
        assertEquals("1ea0f867-ad9c-4ad7-b024-0b8c258f853a", new RequestInfo.Builder(request1, "happypoint").build().getToken());

        request1 = createHttpServletRequest();
        request1.setContent("{\"_lz_userId\": 10101}".getBytes());
        request1.addHeader("Authorization", "Bearer 3ea0f867-ad9c-4ad7-b024-0b8c258f853a");
        request1.setAttribute("Authorization", "2ea0f867-ad9c-4ad7-b024-0b8c258f853b");
        assertEquals("3ea0f867-ad9c-4ad7-b024-0b8c258f853a", new RequestInfo.Builder(request1, "happypoint").build()
                .getToken());


        // token from attribute
        request1 = createHttpServletRequest();
        request1.setContent("{\"_lz_userId\": 10101}".getBytes());
        request1.addHeader("No", "1ea0f867-ad9c-4ad7-b024-0b8c258f853a");
        request1.setAttribute("Authorization", "2ea0f867-ad9c-4ad7-b024-0b8c258f853b");
        request1.setCookies(new Cookie("_lz", "3ea0f867-ad9c-4ad7-b024-0b8c258f853c"));
        assertEquals("2ea0f867-ad9c-4ad7-b024-0b8c258f853b", new RequestInfo.Builder(request1, "happypoint").build().getToken());

    }

    @Test
    public void testExecutor() throws Exception {

        MockHttpServletRequest request1 = createHttpServletRequest();
        request1.setContent("{\"_lz_userId\": 10101}".getBytes());
        request1.setParameter("_lz", "4ea0f867-ad9c-4ad7-b024-0b8c258f853d");
        assertEquals(Executor.Type.HAPPYPOINT, new RequestInfo.Builder(request1, "happypoint").build().getExecutorType());

    }


}
