package com.lezhin.panther.pg.lguplus;

import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author seoeun
 * @since 2018.01.04
 */
@ExtendWith(SpringExtension.class)
public class LguDepositExecutorTest {

    @Test
    public void testExtractLGDField() {
        assertEquals("Invalid Format: [LGD_AMOUNT]", LguDepositExecutor.extractResMsg("???[LGD_AMOUNT]?? ?????:19900.0"));
        assertEquals("Invalid Format: [LGD_AMOUNT]", LguDepositExecutor.extractResMsg("[LGD_AMOUNT]?? ?�???19900.0"));
        assertEquals("Invalid Format: [LGD_AMOUNT]", LguDepositExecutor.extractResMsg("???[LGD_AMOUNT]"));
        assertEquals("???[XXX0", LguDepositExecutor.extractResMsg("???[XXX0"));
        assertEquals("??ZZ]0", LguDepositExecutor.extractResMsg("??ZZ]0"));
    }

    /**
     * ResponseInfo를 기준으로 executor가 succeeded 한지 결정하는 메서드 테스트
     */
    @Test
    public void testExcecutorResult() {

        assertEquals(false, Executor.Type.LGUDEPOSIT.succeeded(ResponseInfo.builder()
                .code(ResponseCode.LGUPLUS_ERROR.getCode())
                .description(ResponseCode.LGUPLUS_ERROR.getMessage()).build()));
        assertEquals(false, Executor.Type.LGUDEPOSIT.succeeded(ResponseInfo.builder()
                .code("XZXZ")
                .description("hello").build()));
        assertEquals(true, Executor.Type.LGUDEPOSIT.succeeded(ResponseInfo.builder()
                .code(ResponseCode.LGUPLUS_OK.getCode())
                .description(ResponseCode.LGUPLUS_OK.getMessage()).build()));

        // executor는 lgudeposit. SPC_OK는 failed
        assertEquals(false, Executor.Type.LGUDEPOSIT.succeeded(ResponseInfo.builder()
                .code(ResponseCode.SPC_OK.getCode())
                .description(ResponseCode.SPC_OK.getMessage()).build()));

    }

}
