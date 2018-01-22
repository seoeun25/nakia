package com.lezhin.panther.lguplus;

import com.lezhin.panther.Context;
import com.lezhin.panther.ErrorCode;
import com.lezhin.panther.exception.ExecutorException;
import com.lezhin.panther.exception.HappyPointParamException;
import com.lezhin.panther.exception.HappyPointSystemException;
import com.lezhin.panther.exception.LguDepositException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.util.DateUtil;
import com.lezhin.panther.util.JsonUtil;

import lgdacom.XPayClient.XPayClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lezhin.panther.ErrorCode.LEZHIN_EXECUTION;
import static com.lezhin.panther.ErrorCode.LGUPLUS_OK;

/**
 * LguPlus의 무통장입금을 처리
 *
 * @author seoeun
 * @since 2017.12.16
 */
@Component
@Qualifier("lgudeposit")
@Scope("prototype")
public class LguDepositExecutor extends Executor<LguplusPayment> {

    private static final Logger logger = LoggerFactory.getLogger(LguDepositExecutor.class);

    public static final int CLOSE_PERIOD = 3; // 3 days. 무통장 입금 마감시간.

    public LguDepositExecutor() {
        this.type = Type.LGUDEPOSIT;
    }

    public LguDepositExecutor(Context<LguplusPayment> context) {
        super(context);
        this.type = Type.LGUDEPOSIT;
    }

    public Payment<LguplusPayment> reserve() {
        LguplusPayment pgPayment = new LguplusPayment.LguplusPaymentBuilder()
                .CST_PLATFORM(pantherProperties.getLguplus().getCstPlatform())
                .CST_MID(pantherProperties.getLguplus().getCstMid())
                .LGD_OID(context.getPayment().getPaymentId().toString())
                .LGD_AMOUNT(String.valueOf(context.getPayment().getAmount().intValue())) // 소숫점 제거
                .LGD_BUYER(context.getPayment().getUserId().toString()) // BUYER = userId
                .LGD_PRODUCTINFO(context.getPayment().getCoinProductName())
                .LGD_BUYEREMAIL(Optional.ofNullable(context.getPayment().getUserEmail()).orElse(""))
                .LGD_TIMESTAMP(DateUtil.getDateTimeString(Instant.now().toEpochMilli()))
                .LGD_CLOSEDATE(getLGD_CLOSEDATE(DateUtil.getDateTimeString(Instant.now().toEpochMilli())))
                .LGD_CUSTOM_USABLEPAY("SC0040") // 무통장입금
                .LGD_CUSTOM_SWITCHINGTYPE(LguplusPayment.STORETYPE.getAPI(context.getRequestInfo().getIsMobile())
                        .getLGD_CUSTOM_SWITCHINGTYPE())
                .LGD_WINDOW_TYPE(LguplusPayment.STORETYPE.getAPI(context.getRequestInfo().getIsMobile()).getLGD_WINDOW_TYPE())
                .LGD_OSTYPE_CHECK(LguplusPayment.STORETYPE.getAPI(context.getRequestInfo().getIsMobile()).getLGD_OSTYPE_CHECK())
                .build();

        // do nothing
        Payment<LguplusPayment> payment = context.getPayment();
        payment.setPgPayment(pgPayment);
        context = context.payment(payment);
        context = context.response(new ResponseInfo(LGUPLUS_OK));
        return payment;
    }

    public Payment<LguplusPayment> preAuthenticate() {
        // pg.preAuthenticate는 이미 page를 통해서 처리 되었고, 결과가 인자로 들어옴

        // do nothing. response ok
        Payment<LguplusPayment> payment = context.getPayment();
        context = context.response(new ResponseInfo(LGUPLUS_OK));
        return payment;
    }

    public Payment<LguplusPayment> authenticate() {
        // lguplus 무통장 결제 처리. 결제라지만 panther 입장에서는 인증 과정임. 가상계좌 얻기.

        // do nothing. response ok
        Payment<LguplusPayment> payment = context.getPayment();
        LguplusPayment lguplusPayment = payment.getPgPayment();
        if (lguplusPayment.getCST_PLATFORM() == null) {
            lguplusPayment = lguplusPayment.toBuilder().CST_PLATFORM(pantherProperties.getLguplus().getCstPlatform())
                    .build();
        }
        if (lguplusPayment.getCST_MID() == null) {
            lguplusPayment = lguplusPayment.toBuilder().CST_MID(pantherProperties.getLguplus().getCstMid()).build();
        }

        String configPath = pantherProperties.getLguplus().getConfDir();

        // 결제 요청 - BEGIN
        String CST_PLATFORM = lguplusPayment.getCST_PLATFORM();
        String CST_MID = lguplusPayment.getCST_MID();
        String LGD_MID = ("test".equals(CST_PLATFORM.trim()) ? "t" : "") + CST_MID;
        String LGD_PAYKEY = payment.getPgPayment().getLGD_PAYKEY();

        logger.info("[TX] init config with {}", configPath);
        XPayClient xpay = new XPayClient();
        boolean isInitOK = xpay.Init(configPath, CST_PLATFORM);


        if (!isInitOK) {
            //API 초기화 실패 화면처리. Panther internal.
            logger.info("결제요청을 초기화 하는데 실패하였습니다.<br>");
            logger.info("LG유플러스에서 제공한 환경파일이 정상적으로 설치 되었는지 확인하시기 바랍니다.<br>");
            logger.info("mall.conf에는 Mert ID = Mert Key 가 반드시 등록되어 있어야 합니다.<br><br>");
            logger.info("문의전화 LG유플러스 1544-7772<br>");
            context = context.response(ResponseInfo.builder().code(LEZHIN_EXECUTION.getCode())
                    .description("mall.conf is not valid").build());
            throw new ExecutorException(type, "Failed to load XPayClient. Check the mall.conf and Mert Key");
        } else {
            logger.info("[TX] init config done");
            logger.info("[TX]   CST_PLATFORM = " + CST_PLATFORM);
            logger.info("[TX]   CST_MID = " + CST_MID);
            logger.info("[TX]   LGD_MID = " + LGD_MID);
            logger.info("[TX]   LGD_PAYKEY = " + LGD_PAYKEY);
            logger.info("[TX]   LGD_AMOUNT = " + lguplusPayment.getLGD_AMOUNT());

            try {
                logger.info("[TX] Init_TX ......");
                xpay.Init_TX(LGD_MID);
                xpay.Set("LGD_TXNAME", "PaymentByKey");
                xpay.Set("LGD_PAYKEY", LGD_PAYKEY);

                String DB_AMOUNT = String.valueOf(payment.getAmount().intValue());
                xpay.Set("LGD_AMOUNTCHECKYN", "Y");
                xpay.Set("LGD_AMOUNT", DB_AMOUNT);

            } catch (Exception e) {
                logger.info("LG유플러스 제공 API를 사용할 수 없습니다. 환경파일 설정을 확인해 주시기 바랍니다. ");
                logger.info("" + e.getMessage());
                context = context.response(ResponseInfo.builder().code(LEZHIN_EXECUTION.getCode())
                        .description("XPayClient.INIT_TX failed").build());
                throw new ExecutorException(type, "Failed to init XPayClient.INIT_TX : " + e.getMessage());
            }
        }

        // 최종 결제 요청 결과처리
        logger.info("[TX PaymentByKey] start transaction ......");
        if (xpay.TX()) {
            ResponseInfo responseInfo = ResponseInfo.builder()
                    .code(xpay.m_szResCode).description(xpay.m_szResMsg).build();
            if (!succeeded(responseInfo)) {
                context = context.response(responseInfo);
                logger.info("[TX] done. failed. PaymentByKey= {}", responseInfo.toString());
                throw new LguDepositException(type, "Tx failed : " + xpay.m_szResCode +
                        ":" + xpay.m_szResMsg);
            }

            logger.info("[TX PaymentByKey] done = {}", responseInfo.toString());
            logger.info("[TX]   LGD_TID = {}", xpay.Response("LGD_TID", 0));
            logger.info("[TX]   LGD_MID = {}", xpay.Response("LGD_MID", 0));
            logger.info("[TX]   LGD_OID = {}", xpay.Response("LGD_OID", 0));
            logger.info("[TX]   LGD_AMOUNT = {}", xpay.Response("LGD_AMOUNT", 0));
            logger.info("[TX]   LGD_FINANCENAME = {}", xpay.Response("LGD_FINANCENAME", 0));
            logger.info("[TX]   LGD_ACCOUNTNUM = {}", xpay.Response("LGD_ACCOUNTNUM", 0));
            logger.info("[TX]   LGD_PAYER = {}", xpay.Response("LGD_PAYER", 0));
            logger.info("[TX]   LGD_TIMESTAMP = {}", xpay.Response("LGD_TIMESTAMP", 0));
            logger.info("[TX]   LGD_BUYER = {}", xpay.Response("LGD_BUYER", 0));

            if (StringUtils.isEmpty(xpay.Response("LGD_FINANCENAME", 0))) {
                throw new LguDepositException(type, "LGD_FINANCENAME can not be empty");
            }
            if (StringUtils.isEmpty(xpay.Response("LGD_ACCOUNTNUM", 0))) {
                throw new LguDepositException(type, "LGD_ACCOUNTNUM can not be empty");
            }

            Map<String, Object> responseMap = new HashMap<>();
            for (int i = 0; i < xpay.ResponseNameCount(); i++) {
                responseMap.put(xpay.ResponseName(i), xpay.Response(xpay.ResponseName(i), 0));
            }
            responseMap.entrySet().stream().forEach(e -> logger.debug("responseMap. " + e.getKey() + " = " + e.getValue()));

            LguplusPayment resPgPayment = JsonUtil.fromMap(responseMap, LguplusPayment.class);
            if (resPgPayment.getLGD_CLOSEDATE() == null) {
                resPgPayment = resPgPayment.toBuilder().LGD_CLOSEDATE(
                        LguDepositExecutor.getLGD_CLOSEDATE(resPgPayment.getLGD_TIMESTAMP())).build();
            }
            logger.info("resultPayment = {}\n", JsonUtil.toJson(resPgPayment));
            // TX 결과 셋팅
            payment.setPgPayment(resPgPayment);
            context.payment(payment).response(responseInfo);

            // succeed!!
            logger.info("[TX PaymentByKey] Succeed !!!!. paymentId = {}, LGD_TID = {}", payment.getPaymentId(),
                    payment.getPgPayment().getLGD_TID());

        } else {
            ResponseInfo responseInfo = ResponseInfo.builder()
                    .code(xpay.m_szResCode).description(xpay.m_szResMsg).build();
            logger.info("[TX PaymentByKey] result is false !!!!. FAILED. response = {}", responseInfo.toString());
            context.response(responseInfo);
            throw new LguDepositException(type, "Tx result is false. TX(PaymentByKey): " + xpay.m_szResCode +
                    ":" + xpay.m_szResMsg);
        }

        return payment;
    }

    public Payment<LguplusPayment> pay() {
        // lguplus 무통장 입금된 후 callback. panther 입장에서는 pay.

        // do nothing. response ok
        Payment<LguplusPayment> payment = context.getPayment();
        LguplusPayment lguplusPayment = payment.getPgPayment();

        String configPath = pantherProperties.getLguplus().getConfDir();

        logger.info("request lguplusPayment = {}", JsonUtil.toJson(lguplusPayment));

        String LGD_RESPCODE = lguplusPayment.getLGD_RESPCODE();
        String LGD_RESPMSG = lguplusPayment.getLGD_RESPMSG();
        String LGD_MID = lguplusPayment.getLGD_MID();
        String LGD_OID = lguplusPayment.getLGD_OID();
        String LGD_AMOUNT = lguplusPayment.getLGD_AMOUNT();
        String LGD_TIMESTAMP = lguplusPayment.getLGD_TIMESTAMP();
        String LGD_CASFLAG = lguplusPayment.getLGD_CASFLAG();
        String LGD_HASHDATA = lguplusPayment.getLGD_HASHDATA();

        logger.info("LGD_TIMESTAMP = {}", LGD_TIMESTAMP);

        String LGD_MERTKEY = LguplusPayment.LGD_MERTKEY;
        logger.info("LGD_MERTKEY = {}", LGD_MERTKEY);

        String LGD_HASHDATA2 = "";
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(LGD_MID);
            sb.append(LGD_OID);
            sb.append(LGD_AMOUNT);
            sb.append(LGD_RESPCODE);
            sb.append(LGD_TIMESTAMP);
            sb.append(LGD_MERTKEY);

            logger.info(LGD_MID);
            logger.info(LGD_OID);
            logger.info(LGD_AMOUNT);
            logger.info(LGD_RESPCODE);
            logger.info(LGD_TIMESTAMP);
            logger.info(LGD_MERTKEY);
            logger.info("sb = {}", sb.toString());

            byte[] bNoti = sb.toString().getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bNoti);

            StringBuffer strBuf = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                int c = digest[i] & 0xff;
                if (c <= 15) {
                    strBuf.append("0");
                }
                strBuf.append(Integer.toHexString(c));
            }
            logger.info("strBuf ::" + strBuf.toString());
            LGD_HASHDATA2 = strBuf.toString();
        } catch (Exception e) {
            logger.warn("Failed to generate LGD_HASHDATA");
        }

        /*
         * 상점 처리결과 리턴메세지
         *
         * OK  : 상점 처리결과 성공
         * 그외 : 상점 처리결과 실패
         *
         * ※ 주의사항 : 성공시 'OK' 문자이외의 다른문자열이 포함되면 실패처리 되오니 주의하시기 바랍니다.
         */
        //String resultMSG = "결제결과 상점 DB처리(LGD_CASNOTEURL) 결과값을 입력해 주시기 바랍니다.";
        ResponseInfo responseInfo = ResponseInfo.builder().code(LGD_RESPCODE).description(LGD_RESPMSG).build();
        if (LGD_HASHDATA2.trim().equals(LGD_HASHDATA)) { //해쉬값 검증이 성공이면
            if (("0000".equals(LGD_RESPCODE.trim()))) { //결제가 성공이면
                if ("R".equals(LGD_CASFLAG.trim())) {
                    // 가상 계좌 생성
                    responseInfo = responseInfo.toBuilder().description("CASFLAG should be 'I' but R").build();
                    context = context.response(responseInfo);
                    throw new LguDepositException(type, "CASFLAG should be 'I' but 'R'");

                } else if ("I".equals(LGD_CASFLAG.trim())) {
                    // 입금
                    context = context.response(responseInfo);
                    logger.info("==== Deposit Succeed !!!! ====");
                } else if ("C".equals(LGD_CASFLAG.trim())) {
                    // 입금 취소
                    responseInfo = responseInfo.toBuilder().description("CASFLAG should be 'I' but C").build();
                    context = context.response(responseInfo);
                    throw new LguDepositException(type, "CASFLAG should be 'I' but C");
                }
            } else { //결제가 실패이면
                responseInfo = responseInfo.toBuilder().description("LGU payment failed").build();
                context = context.response(responseInfo);
                throw new LguDepositException(type, "LGU deposit payment failed:" + LGD_RESPMSG);
            }
        } else { //해쉬값이 검증이 실패이면
            responseInfo = responseInfo.toBuilder().code(ErrorCode.LGUPLUS_ERROR.getCode())
                    .description("LGD_HASHDATA is not valid").build();
            context = context.response(responseInfo);
            throw new LguDepositException(type, "LGD_HASHDATA is not valid");
        }

        return payment;
    }

    public Payment refund() {
        Payment payment = context.getPayment();
        LguplusPayment lguplusPayment = context.getPayment().getPgPayment();
        if (lguplusPayment.getCST_PLATFORM() == null) {
            lguplusPayment = lguplusPayment.toBuilder().CST_PLATFORM(pantherProperties.getLguplus().getCstPlatform())
                    .build();
        }
        if (lguplusPayment.getCST_MID() == null) {
            lguplusPayment = lguplusPayment.toBuilder().CST_MID(pantherProperties.getLguplus().getCstMid()).build();
        }
        logger.info("== REFUND LGD_TID={}, CST_MID={}", lguplusPayment.getLGD_TID(), lguplusPayment.getCST_MID());

        String CST_PLATFORM = lguplusPayment.getCST_PLATFORM();
        String CST_MID = lguplusPayment.getCST_MID();
        String LGD_MID = ("test".equals(CST_PLATFORM.trim()) ? "t" : "") + CST_MID;
        String LGD_TID = lguplusPayment.getLGD_TID();

        String configPath = pantherProperties.getLguplus().getConfDir();
        LGD_TID = (LGD_TID == null) ? "" : LGD_TID;

        logger.info("[TX Cancel] init config with {}", configPath);
        XPayClient xpay = new XPayClient();
        xpay.Init(configPath, CST_PLATFORM);
        xpay.Init_TX(LGD_MID);
        xpay.Set("LGD_TXNAME", "Cancel");
        xpay.Set("LGD_TID", LGD_TID);

        /*
         * 1. 결제취소 요청 결과처리
         *
         * 취소결과 리턴 파라미터는 연동메뉴얼을 참고하시기 바랍니다.
         *
         * [[[중요]]] 고객사에서 정상취소 처리해야할 응답코드
         * 1. 신용카드 : 0000, AV11
         * 2. 계좌이체 : 0000, RF00, RF10, RF09, RF15, RF19, RF23, RF25 (환불진행중 응답건-> 환불결과코드.xls 참고)
         * 3. 나머지 결제수단의 경우 0000(성공) 만 취소성공 처리
         *
         */
        logger.info("[TX Cancel] start transaction ......");
        if (xpay.TX()) {
            ResponseInfo responseInfo = ResponseInfo.builder()
                    .code(xpay.m_szResCode).description(xpay.m_szResMsg).build();
            if (!succeeded(responseInfo)) {
                context = context.response(responseInfo);
                logger.info("[TX] done. failed = {}", responseInfo.toString());
                throw new LguDepositException(type, "Tx failed : " + xpay.m_szResCode +
                        ":" + xpay.m_szResMsg);
            }
            logger.info("[TX] done = {}", responseInfo.toString());
            logger.info("[TX]   LGD_TID = {}", xpay.Response("LGD_TID", 0));
            logger.info("[TX]   LGD_MID = {}", xpay.Response("LGD_MID", 0));
            logger.info("[TX]   LGD_OID = {}", xpay.Response("LGD_OID", 0));
            logger.info("[TX]   LGD_AMOUNT = {}", xpay.Response("LGD_AMOUNT", 0));

            // succeed!!
            logger.info("[TX Cancel] Succeed !!!!. paymentId = {}, LGD_TID = {}", payment.getPaymentId(),
                    lguplusPayment.getLGD_TID());

        } else {
            ResponseInfo responseInfo = ResponseInfo.builder()
                    .code(xpay.m_szResCode).description(xpay.m_szResMsg).build();
            logger.info("[TX Cancel] result is false !!!!. FAILED. response = {}", responseInfo.toString());
            context.response(responseInfo);
            throw new LguDepositException(type, "Tx result is false. TX(Cancel): " + xpay.m_szResCode +
                    ":" + xpay.m_szResMsg);
        }

        return context.getPayment();

    }

    /**
     * Throws Exception if lguplus execution failed.
     *
     * @param responseCode
     * @throws LguDepositException
     */
    public void handleResponseCode(String responseCode) throws HappyPointParamException, HappyPointSystemException {
        if (!responseCode.equals(ErrorCode.LGUPLUS_OK.getCode())) {
            throw new HappyPointSystemException(type, context.getResponseInfo().getCode(),
                    context.getResponseInfo().getDescription());
        }
    }

    /**
     * Extract [XXX_XXX] from given {@code input} and return with errorMsg.
     *
     * @param input ex> ????[LGD_AMOUNT]?????
     * @return ex> Invalid Foramt: [LGD_AMOUNT]
     */
    public static String extractResMsg(String input) {
        if (input == null) {
            return input;
        }
        String regEx = ".*(\\[.*\\]).*";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(input);
        String field = null;
        while (m.find()) {
            field = m.group(1);
            break;
        }
        if (field != null) {
            return "Invalid Format: " + field;
        }
        return input;
    }

    /**
     * Return LGD_CLOSEDATE which is after 3days than LGD_TIMESTAMP
     *
     * @param LGD_TIMESTAMP
     * @return
     */
    public static String getLGD_CLOSEDATE(String LGD_TIMESTAMP) {
        long timestamp = DateUtil.toInstant(LGD_TIMESTAMP, "yyyyMMddHHmmss", DateUtil.ASIA_SEOUL_ZONE).toEpochMilli();
        return DateUtil.getDateTimeString(timestamp + (1000 * 60 * 60 * 24 * CLOSE_PERIOD));
    }
}
