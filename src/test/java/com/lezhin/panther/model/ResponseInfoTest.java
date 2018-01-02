package com.lezhin.panther.model;

/**
 * @author seoeun
 * @since 2018.01.14
 */

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class ResponseInfoTest {

    @Test
    public void testToBuider() {
        ResponseInfo responseInfo = ResponseInfo.builder().code("AAA").description("AAA-DESC").build();
        responseInfo = responseInfo.toBuilder().code("BBB").build();

        assertEquals("BBB", responseInfo.getCode());
        assertEquals("AAA-DESC", responseInfo.getDescription());
    }
}
