package com.lezhin.panther.internal;

import com.lezhin.panther.util.JsonUtil;

import com.google.api.client.util.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author seoeun
 * @since 2018.03.15
 */
@ExtendWith(SpringExtension.class)
public class InternalPurchaseServiceTest {

    private final Logger logger = LoggerFactory.getLogger(InternalPurchaseServiceTest.class);


    /**
     * Cms에서 받은 json response를 PurchaseDetail 로 변환.
     *
     * @throws IOException
     */
    @Test
    public void testJsonParse() throws IOException {

        Resource resource = new ClassPathResource("/example/purchase/purchaseDetailOrg.json");
        assertNotNull(resource.getInputStream());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(resource.getInputStream(), outputStream);
        String abc = outputStream.toString();

        PurchaseDetail purchaseDetail = JsonUtil.fromJson(abc, PurchaseDetail.class);

        assertNotNull(purchaseDetail);
        assertEquals(2, purchaseDetail.getPurchases().size());
        assertEquals(6723762783161268L, purchaseDetail.getCharge().getId().longValue());
        assertEquals(50, purchaseDetail.getCharge().getCoin().intValue());
        assertEquals("50 Coins & 100P", purchaseDetail.getCharge().getTitle());

        Purchase purchase0 = purchaseDetail.getPurchases().get(0);
        assertEquals("A Guy Like You/22", purchase0.getTitle());
        Purchase purchase1 = purchaseDetail.getPurchases().get(1);
        assertEquals("A Guy Like You/23", purchase1.getTitle());

        assertEquals(2, purchaseDetail.getVouchers().size());
        Voucher voucher0 = purchaseDetail.getVouchers().get(0);
        Voucher voucher1 = purchaseDetail.getVouchers().get(1);
        logger.info("{}\n", JsonUtil.toJson(voucher0));
        logger.info("{}\n", JsonUtil.toJson(voucher1));


        String converted = JsonUtil.toJson(purchaseDetail);
        logger.info("converted \n{}\n", converted);

        int firstIndex = converted.indexOf("createdAt");
        assertEquals(true, firstIndex != -1);
        String createdAt = converted.substring(firstIndex + 12, firstIndex + 31);
        assertEquals("2017-12-30 08:31:39", createdAt);
        logger.info(createdAt);
    }
}
