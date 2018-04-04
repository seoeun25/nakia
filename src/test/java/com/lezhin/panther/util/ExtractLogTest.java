package com.lezhin.panther.util;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * log에서 결제 정보 추출할 때 사용.
 *
 * @author seoeun
 * @since 2018.03.01
 */
@ExtendWith(SpringExtension.class)
public class ExtractLogTest {

    /**
     * log 로 부터 happypoint paymentId, receipt 정보 추출.
     *
     * @throws Exception
     */
    @Test
    public void testExtractHappypointLog() throws Exception {
        Resource resource = new ClassPathResource("/example/happypoint/logs/newerror.log");
        assertNotNull(resource.getInputStream());

        System.out.println("length = " + resource.getURI());

        BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

        String line = null;
        boolean complete = false;
        String payComplente = "/complete";
        boolean send = false;
        String paySend = "PAY. send :";
        String paymentId = null;
        while ((line = reader.readLine()) != null) {
            //System.out.println(line);
            if (line.contains(payComplente)) {
                complete = true;
                //System.out.println(line);
                paymentId = line.substring(line.indexOf("payments/") + 9, line.indexOf("payments/") + 9 + 16);
                //System.out.println(paymentId);
                continue;
            }
            if (complete) {
                if (line.contains(paySend)) {
                    send = true;
                    complete = false;
                } else {
                    System.out.println("aa = " + line);
                }
                continue;
            }
            if (send) {
                if (line.contains("\"receipt\":\"{\\\"mbrNo\\\"")) {
                    String receipt = line.substring(line.indexOf("\"receipt\":\"{\\\"mbrNo\\\""),
                            line.indexOf("," + "\"dynamicAmount\""));
                    System.out.println(paymentId);
                    //System.out.println(receipt);
                } else {
                    //System.out.println("bb = " + line);
                }
                complete = false;
                send = false;

            }

        }

    }

    /**
     * log 로 부터 happypoint userId 추출. testExtractHappypointLog() 와 merge.
     *
     * @throws Exception
     * @see {{@link #testExtractHappypointLog()}}
     */
    @Test
    public void testExtractHappypointLogUser() throws Exception {
        Resource resource = new ClassPathResource("/example/happypoint/logs/newerror.log");
        assertNotNull(resource.getInputStream());

        System.out.println("length = " + resource.getURI());

        BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

        String line = null;
        boolean complete = false;
        String payComplente = "PAY start Context{type=HAPPYPOINT, request.user=";
        boolean send = false;
        String paySend = "PAY. send :";
        String userId = null;
        String paymentId = null;
        while ((line = reader.readLine()) != null) {
            //System.out.println(line);
            if (line.contains(payComplente)) {
                complete = true;
                //System.out.println(line);
                userId = line.substring(line.indexOf(payComplente) + 17 + 18);
                paymentId = line.substring(line.indexOf("payments/") + 9, line.indexOf("payments/") + 9 + 16);
                System.out.println(userId);
                continue;
            }

        }

    }

    /**
     * log로부터 LGUDEPOSIT 정보 추출.
     *
     * @throws Exception
     */
    @Test
    public void testExtractLguDepositLog() throws Exception {
        Resource resource = new ClassPathResource("/example/happypoint/logs/newerror.log");
        assertNotNull(resource.getInputStream());

        System.out.println("length = " + resource.getURI());

        BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

        String line = null;
        boolean complete = false;
        String payComplente = "[AUTHENTICATE, LGUDEPOSIT";
        boolean send = false;
        String paySend = "[LGUDEPOSIT] authentication done.";
        String paymentId = null;
        while ((line = reader.readLine()) != null) {
            //System.out.println(line);
            if (line.contains(payComplente)) {
                paymentId = line.substring(line.indexOf(payComplente));
                //System.out.println(paymentId);
                complete = true;
                continue;
            }
            if (line.contains(paySend)) {
                String receipt = line.substring(line.indexOf(paySend));
                //System.out.println(paymentId);
                System.out.println(receipt);
                complete = false;
            } else {
                //System.out.println("bb = " + line);
            }

        }

    }

    /**
     * happypoint. mbrNo에 해당하는 receipt 정보 조합.
     *
     * @throws Exception
     */
    @Test
    public void extractHappyPointForRefundData() throws Exception {

        Resource resource = new ClassPathResource("/example/happypoint/logs/mbrno1.txt");
        assertNotNull(resource.getInputStream());
        BufferedReader mbrNoReader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        List<String> mbrNos = IOUtils.readLines(mbrNoReader);


        Resource resource2 = new ClassPathResource("/example/happypoint/logs/receipt1.txt");
        assertNotNull(resource2.getInputStream());
        BufferedReader receiptReader = new BufferedReader(new InputStreamReader(resource2.getInputStream()));
        List<String> receipts = IOUtils.readLines(receiptReader);

        for (String mbrNo : mbrNos) {
            boolean find = false;
            for (String receipt : receipts) {
                if (receipt.contains(mbrNo)) {
                    String tmp = receipt.substring(10);
                    //String aprNo = receipt.substring(receipt.indexOf("aprNo"))
                    //System.out.println(mbrNo + "," + receipt);
                    System.out.println(receipt);
                    find = true;
                    break;
                }
            }
            if (!find) {
                System.out.println("Not found = " + mbrNo);
            }
        }
    }

    /**
     * happypoint. receipt 정보에서 refund 용 데이터 추출 및 curl step 생성.
     *
     * @throws Exception
     */
    @Test
    public void extractRefundData() throws Exception {

        Resource resource2 = new ClassPathResource("/example/happypoint/logs/receipt.txt");
        assertNotNull(resource2.getInputStream());
        BufferedReader receiptReader = new BufferedReader(new InputStreamReader(resource2.getInputStream()));
        List<String> receipts = IOUtils.readLines(receiptReader);

        String mbrNo;
        String orglTrxAprvNo;
        String orglTrxAprvDt;
        String trxAmt;

        String curl = "curl -X POST -H \"Content-Type: application/json\" -H \"__x: nakia\" " +
                "https://localhost:9443/api/v1/happypoint/refund -d ";

        for (String receipt : receipts) {

            String tmp = receipt.substring(9);
            tmp = StringUtils.replace(tmp, "\\", "");
            //System.out.println(tmp);
            Map<String, Object> map = JsonUtil.fromJson(tmp, Map.class);
            Map<String, String> requestMap = new HashMap<>();
            Instant now = Instant.now();

            if (map != null) {
                //System.out.println("aprvNo=" + map.get("aprvNo") + ",aprvDt=" + map.get("aprvDt") );
                mbrNo = map.get("mbrNo").toString();
                orglTrxAprvDt = map.get("aprvDt").toString();
                orglTrxAprvNo = map.get("aprvNo").toString();
                trxAmt = map.get("trxAmt").toString();
                requestMap.put("mbrNo", mbrNo);
                requestMap.put("aprvDt", orglTrxAprvDt);
                requestMap.put("aprvNo", orglTrxAprvNo);
                requestMap.put("trxAmt", trxAmt);

                String data = JsonUtil.toJson(requestMap);
                String command = curl + "'" + data + "' -k";
                System.out.println(command);
            }
        }
    }
}
