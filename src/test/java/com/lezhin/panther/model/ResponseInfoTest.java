package com.lezhin.panther.model;

/**
 * @author seoeun
 * @since 2018.01.14
 */

import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(SpringExtension.class)
public class ResponseInfoTest {

    @Test
    public void testToBuider() {
        ResponseInfo responseInfo = ResponseInfo.builder().code("AAA").description("AAA-DESC").build();
        responseInfo = responseInfo.toBuilder().code("BBB").build();

        assertEquals("BBB", responseInfo.getCode());
        assertEquals("AAA-DESC", responseInfo.getDescription());
    }

    @Test
    public void testResponseCode() {
        ResponseInfo responseInfo = ResponseInfo.builder()
                .code(ResponseCode.LGUPLUS_ERROR.getCode())
                .description(ResponseCode.LGUPLUS_ERROR.getMessage())
                .build();

        ResponseInfo responseInfo2 = new ResponseInfo(ResponseCode.LGUPLUS_ERROR);

        assertEquals(responseInfo.getCode(), responseInfo2.getCode());
        assertEquals(responseInfo.getDescription(), responseInfo2.getDescription());

        // description 만 다르게 셋팅.
        ResponseInfo responseInfo3 = new ResponseInfo(ResponseCode.LGUPLUS_ERROR.getCode(), "Hello az");
        assertEquals(responseInfo.getCode(), responseInfo3.getCode());
        assertNotEquals(responseInfo.getDescription(), responseInfo3.getDescription());

        // description 만 다를 뿐, 실패한 execution.
        assertEquals(false, Executor.Type.LGUDEPOSIT.succeeded(responseInfo2));
        assertEquals(false, Executor.Type.LGUDEPOSIT.succeeded(responseInfo3));

    }
}
