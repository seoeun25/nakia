package com.lezhin.panther.lguplus;


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

}
