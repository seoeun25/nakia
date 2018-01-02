package com.lezhin.panther.util;

import com.lezhin.panther.happypoint.HappyPointPayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * // FIXME json dependnecy가 2개라는 워닝.
 * @author seoeun
 * @since 2017.11.07
 */
@ExtendWith(SpringExtension.class)
public class UtilTest {

    private static final Logger logger = LoggerFactory.getLogger(UtilTest.class);

    @Test
    public void testHappyPointPayment() {

        HappyPointPayment basePayment = new HappyPointPayment();
        basePayment.setMbrNo("meta_mbrNo_XXX");
        basePayment.setMbrNm("meta_mbrNm_YYY");
        basePayment.setUseReqPt(1000);
        logger.info("request vo = {}", JsonUtil.toJson(basePayment));
        assertEquals("meta_mbrNo_XXX", basePayment.getMbrNo());
        assertEquals("meta_mbrNm_YYY", basePayment.getMbrNm());
        assertEquals(1000, basePayment.getUseReqPt().intValue());

        assertNull(basePayment.getMbrIdfNo());

        HappyPointPayment updatePayment = HappyPointPayment.API.authentication.createRequest();
        String CI = "REDIS_X_CI";
        String NAME = "REDIS_X_NAME";
        updatePayment.setMbrNm(NAME);
        updatePayment.setMbrIdfNo(CI);
        updatePayment.setAprvDt("20171108");
        assertNull(updatePayment.getMbrNo());
        assertEquals("REDIS_X_NAME", updatePayment.getMbrNm()); // update
        assertEquals("REDIS_X_CI", updatePayment.getMbrIdfNo());  // new data

        logger.info("happyPointPayment before send = {}", JsonUtil.toJson(updatePayment));

        HappyPointPayment merge = Util.merge(basePayment, updatePayment, HappyPointPayment.class);
        assertEquals("meta_mbrNo_XXX", merge.getMbrNo()); // from basePayment
        assertEquals(1000, merge.getUseReqPt().intValue()); // from basePayment
        assertEquals("REDIS_X_NAME", merge.getMbrNm()); // overwritten from authentication
        assertEquals("REDIS_X_CI", merge.getMbrIdfNo()); // from authentication

    }

    @Test
    public void testJson() {
        List<Map<String, String>> a = new ArrayList();

        Map<String, String> product1 = new HashMap<>();
        product1.put("coin", "5");
        product1.put("price", "1000");
        a.add(product1);

        System.out.println(JsonUtil.toJson(a));
    }

    @Test
    public void testLangFromLocale() {
        assertEquals("en", Util.getLang("en-US"));
        assertEquals("ko", Util.getLang("a"));
        assertEquals("ko", Util.getLang("ko-KR"));
        assertEquals("ko", Util.getLang(null));
    }

}
