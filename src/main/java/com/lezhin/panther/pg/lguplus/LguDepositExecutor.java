package com.lezhin.panther.pg.lguplus;

import com.lezhin.panther.Context;
import com.lezhin.panther.exception.ExecutorException;
import com.lezhin.panther.exception.HappyPointParamException;
import com.lezhin.panther.exception.HappyPointSystemException;
import com.lezhin.panther.exception.LguDepositException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;
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

import static com.lezhin.panther.model.ResponseInfo.ResponseCode.LEZHIN_EXECUTION;
import static com.lezhin.panther.model.ResponseInfo.ResponseCode.LGUPLUS_OK;

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

    public static final int CLOSE_PERIOD = 3; // 무통장 입금 마감시간. days.

    protected LguDepositExecutor() {
        this.type = Type.LGUDEPOSIT;
        logger.warn("Context can not be null");
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
        Payment<LguplusPayment> payment = context.getPayment();
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
        logger.info("LGD_OID={}, LGD_BUYER={}, LGD_FINANCENAME={}, LGD_ACCOUNTNUM={}, LGD_PAYER={}",
                lguplusPayment.getLGD_OID(), lguplusPayment.getLGD_BUYER(), lguplusPayment.getLGD_FINANCENAME(),
                lguplusPayment.getLGD_ACCOUNTNUM(), lguplusPayment.getLGD_PAYER()
        );

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
            throw new ExecutorException(context, "Failed to load XPayClient. Check the mall.conf and Mert Key");
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
                throw new ExecutorException(context, "Failed to init XPayClient.INIT_TX : " + e.getMessage());
            }
        }

        // 최종 결제 요청 결과처리
        logger.info("[TX PaymentByKey] start transaction ......");
        if (xpay.TX()) {
            ResponseInfo responseInfo = ResponseInfo.builder()
                    .code(xpay.m_szResCode).description(xpay.m_szResMsg).build();
            context = context.response(responseInfo);
            if (!succeeded(responseInfo)) {
                logger.info("[TX] done. failed. PaymentByKey= {}", responseInfo.toString());
                throw new LguDepositException(context, "Tx failed : " + xpay.m_szResCode +
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
                throw new LguDepositException(context, "LGD_FINANCENAME can not be empty");
            }
            if (StringUtils.isEmpty(xpay.Response("LGD_ACCOUNTNUM", 0))) {
                throw new LguDepositException(context, "LGD_ACCOUNTNUM can not be empty");
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
            logger.debug("resultPayment = {}\n", JsonUtil.toJson(resPgPayment));
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
            throw new LguDepositException(context, "Tx result is false. TX(PaymentByKey): " + xpay.m_szResCode +
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

        logger.info("{} request lguplusPayment = {}", context.print(), JsonUtil.toJson(lguplusPayment));

        String LGD_RESPCODE = lguplusPayment.getLGD_RESPCODE();
        String LGD_RESPMSG = lguplusPayment.getLGD_RESPMSG();
        String LGD_MID = lguplusPayment.getLGD_MID();
        String LGD_OID = lguplusPayment.getLGD_OID();
        String LGD_AMOUNT = lguplusPayment.getLGD_AMOUNT();
        String LGD_TIMESTAMP = lguplusPayment.getLGD_TIMESTAMP();
        String LGD_CASFLAG = lguplusPayment.getLGD_CASFLAG();
        String LGD_HASHDATA = lguplusPayment.getLGD_HASHDATA();

        String LGD_MERTKEY = LguplusPayment.LGD_MERTKEY;
        String LGD_HASHDATA2 = "";
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(LGD_MID);
            sb.append(LGD_OID);
            sb.append(LGD_AMOUNT);
            sb.append(LGD_RESPCODE);
            sb.append(LGD_TIMESTAMP);
            sb.append(LGD_MERTKEY);

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
                    throw new LguDepositException(context, "CASFLAG should be 'I' but 'R'");

                } else if ("I".equals(LGD_CASFLAG.trim())) {
                    // 입금
                    context = context.response(responseInfo);
                    logger.info("{} ==== Deposit Succeed !!!! ==== \n" +
                                    "payment={}, user={}, approvalId={}, bank={}, amount={}",
                            context.print(),
                            payment.getPaymentId(), payment.getUserId(),
                            lguplusPayment.getApprovalId(), lguplusPayment.getLGD_FINANCENAME(),
                            lguplusPayment.getLGD_AMOUNT());
                } else if ("C".equals(LGD_CASFLAG.trim())) {
                    // 입금 취소
                    responseInfo = responseInfo.toBuilder().description("CASFLAG should be 'I' but C").build();
                    context = context.response(responseInfo);
                    throw new LguDepositException(context, "CASFLAG should be 'I' but C");
                }
            } else { //결제가 실패이면
                responseInfo = responseInfo.toBuilder().description("LGU payment failed").build();
                context = context.response(responseInfo);
                throw new LguDepositException(context, "LGU deposit payment failed:" + LGD_RESPMSG);
            }
        } else { //해쉬값이 검증이 실패이면
            responseInfo = responseInfo.toBuilder().code(ResponseCode.LGUPLUS_ERROR.getCode())
                    .description("LGD_HASHDATA is not valid").build();
            context = context.response(responseInfo);
            throw new LguDepositException(context, "LGD_HASHDATA is not valid");
        }

        return payment;
    }

    public Payment cancel() {
        // 무통장 가상계좌를 close 한다. (closeDate를 현재 시간으로 업데이트 하여 만료시킨다)

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
        logger.debug("request lguplusPayment = {}", JsonUtil.toJson(lguplusPayment));

        String CST_PLATFORM = lguplusPayment.getCST_PLATFORM();
        String CST_MID = lguplusPayment.getCST_MID();
        String LGD_MID = ("test".equals(CST_PLATFORM.trim()) ? "t" : "") + CST_MID;
        String LGD_METHOD = "CHANGE";                   //ASSIGN:할당, CHANGE:변경
        String LGD_OID = lguplusPayment.getLGD_OID();
        String LGD_AMOUNT = lguplusPayment.getLGD_AMOUNT();
        String LGD_PRODUCTINFO = lguplusPayment.getLGD_PRODUCTINFO();
        String LGD_BUYER = lguplusPayment.getLGD_BUYER();
        String LGD_ACCOUNTOWNER = lguplusPayment.getLGD_ACCOUNTNUM();
        String LGD_ACCOUNTPID = "";                  //구매자 개인식별변호 (6자리~13자리)(옵션)
        String LGD_BUYERPHONE = lguplusPayment.getLGD_BUYERPHONE();
        String LGD_BUYEREMAIL = lguplusPayment.getLGD_BUYEREMAIL();
        String LGD_BANKCODE = "";                  //입금계좌은행코드
        String LGD_CASHRECEIPTUSE = "";              //현금영수증 발행구분('1':소득공제, '2':지출증빙)
        String LGD_CASHCARDNUM = "";              //현금영수증 카드번호
        String LGD_CLOSEDATE = DateUtil.getDateTimeString(Instant.now().toEpochMilli());
        String LGD_TAXFREEAMOUNT = "";              //면세금액
        String LGD_CASNOTEURL = pantherProperties.getPantherUrl() + "/api/v1/lguplus/deposit/payment/done";

        LGD_METHOD = (LGD_METHOD == null) ? "" : LGD_METHOD;
        LGD_OID = (LGD_OID == null) ? "" : LGD_OID;
        LGD_AMOUNT = (LGD_AMOUNT == null) ? "" : LGD_AMOUNT;
        LGD_PRODUCTINFO = (LGD_PRODUCTINFO == null) ? "" : LGD_PRODUCTINFO;
        LGD_BANKCODE = (LGD_BANKCODE == null) ? "" : LGD_BANKCODE;
        LGD_CASHRECEIPTUSE = (LGD_CASHRECEIPTUSE == null) ? "" : LGD_CASHRECEIPTUSE;
        LGD_CASHCARDNUM = (LGD_CASHCARDNUM == null) ? "" : LGD_CASHCARDNUM;
        LGD_CLOSEDATE = (LGD_CLOSEDATE == null) ? "" : LGD_CLOSEDATE;
        LGD_CASNOTEURL = (LGD_CASNOTEURL == null) ? "" : LGD_CASNOTEURL;
        LGD_TAXFREEAMOUNT = (LGD_TAXFREEAMOUNT == null) ? "" : LGD_TAXFREEAMOUNT;
        LGD_BUYER = (LGD_BUYER == null) ? "" : LGD_BUYER;
        LGD_ACCOUNTOWNER = (LGD_ACCOUNTOWNER == null) ? "" : LGD_ACCOUNTOWNER;
        LGD_ACCOUNTPID = (LGD_ACCOUNTPID == null) ? "" : LGD_ACCOUNTPID;
        LGD_BUYERPHONE = (LGD_BUYERPHONE == null) ? "" : LGD_BUYERPHONE;
        LGD_BUYEREMAIL = (LGD_BUYEREMAIL == null) ? "" : LGD_BUYEREMAIL;

        XPayClient xpay = new XPayClient();
        xpay.Init(configPath, CST_PLATFORM);
        xpay.Init_TX(LGD_MID);
        xpay.Set("LGD_TXNAME", "CyberAccount");
        xpay.Set("LGD_METHOD", LGD_METHOD);
        xpay.Set("LGD_OID", LGD_OID);
        xpay.Set("LGD_AMOUNT", LGD_AMOUNT);
        xpay.Set("LGD_PRODUCTINFO", LGD_PRODUCTINFO);
        xpay.Set("LGD_BANKCODE", LGD_BANKCODE);
        xpay.Set("LGD_CASHRECEIPTUSE", LGD_CASHRECEIPTUSE);
        xpay.Set("LGD_CASHCARDNUM", LGD_CASHCARDNUM);
        xpay.Set("LGD_CLOSEDATE", LGD_CLOSEDATE);
        xpay.Set("LGD_CASNOTEURL", LGD_CASNOTEURL);
        xpay.Set("LGD_TAXFREEAMOUNT", LGD_TAXFREEAMOUNT);
        xpay.Set("LGD_BUYER", LGD_BUYER);
        xpay.Set("LGD_ACCOUNTOWNER", LGD_ACCOUNTOWNER);
        xpay.Set("LGD_ACCOUNTPID", LGD_ACCOUNTPID);
        xpay.Set("LGD_BUYERPHONE", LGD_BUYERPHONE);
        xpay.Set("LGD_BUYEREMAIL", LGD_BUYEREMAIL);

        logger.info("[TX CyberAccount] start transaction ......");
        if (xpay.TX()) {
            if (LGD_METHOD.equals("ASSIGN")) { //가상계좌 발급의 경우

                //1)가상계좌 발급/변경결과 화면처리(성공,실패 결과 처리를 하시기 바랍니다.)
                logger.info("가상계좌 발급 요청처리가 완료되었습니다.  <br>");
                logger.info("TX Response_code = " + xpay.m_szResCode + "<br>");
                logger.info("TX Response_msg = " + xpay.m_szResMsg + "<p>");
                logger.info("거래번호 : " + xpay.Response("LGD_TID", 0) + "<br>");
                logger.info("결과코드 : " + xpay.Response("LGD_RESPCODE", 0) + "<p>");

                for (int i = 0; i < xpay.ResponseNameCount(); i++) {
                    logger.info(xpay.ResponseName(i) + " = ");
                    for (int j = 0; j < xpay.ResponseCount(); j++) {
                        logger.info("\t" + xpay.Response(xpay.ResponseName(i), j) + "<br>");
                    }
                }

            } else {        //가상계좌 변경의 경우

                ResponseInfo responseInfo = new ResponseInfo(xpay.m_szResCode, xpay.m_szResMsg);
                context = context.response(responseInfo);
                if (!succeeded(responseInfo)) {
                    logger.info("[TX] done. failed. CyberAccount= {}", responseInfo.toString());
                    throw new LguDepositException(context, "Tx failed : " + xpay.m_szResCode +
                            ":" + xpay.m_szResMsg);
                }

                logger.info("[TX CyberAccount] done = {}", responseInfo.toString());
                logger.info("[TX]   LGD_OID = {}", xpay.Response("LGD_OID", 0));
                // succeed!!
                logger.info("[TX CyberAccount] Succeed !!!!. paymentId = {}, LGD_TID = {}, LGD_CLOSEDATE: {} ==> {}",
                        payment.getPaymentId(), payment.getPgPayment().getLGD_TID(),
                        lguplusPayment.getLGD_CLOSEDATE(), LGD_CLOSEDATE);

                lguplusPayment.setLGD_CLOSEDATE(LGD_CLOSEDATE);
                payment.setPgPayment(lguplusPayment);
                context.payment(payment).response(responseInfo);

            }

        } else {
            ResponseInfo responseInfo = new ResponseInfo(xpay.m_szResCode, xpay.m_szResMsg);
            logger.info("[TX CyberAccount] result is false !!!!. FAILED. response = {}", responseInfo.toString());
            context.response(responseInfo);
            throw new LguDepositException(context, "Tx result is false. TX(CyberAccount): " + xpay.m_szResCode +
                    ":" + xpay.m_szResMsg);
        }

        return payment;
    }

    public Payment refund() {
        // 무통장 입금은 API로 refund 불가(계약자체가 CR을 통해서만 refund 하도록)
        Payment payment = context.getPayment();
        LguplusPayment lguplusPayment = context.getPayment().getPgPayment();
        if (lguplusPayment.getCST_PLATFORM() == null) {
            lguplusPayment = lguplusPayment.toBuilder().CST_PLATFORM(pantherProperties.getLguplus().getCstPlatform())
                    .build();
        }
        if (lguplusPayment.getCST_MID() == null) {
            lguplusPayment = lguplusPayment.toBuilder().CST_MID(pantherProperties.getLguplus().getCstMid()).build();
        }
        logger.warn("== CR need REFUND. LGD_TID={}, paymentId(LGD_OID)={}, userId={}, amount={}",
                lguplusPayment.getLGD_TID(), lguplusPayment.getLGD_OID(), payment.getUserId(), payment.getAmount());

        return context.getPayment();
    }

    /**
     * Throws Exception if lguplus execution failed.
     *
     * @param context
     * @throws LguDepositException
     */
    public void handleResponse(Context context) throws LguDepositException {
        String resCode = context.getResponseInfo().getCode();
        if (!resCode.equals(ResponseCode.LGUPLUS_OK.getCode())) {
            throw new LguDepositException(context, context.getResponseInfo().getCode(),
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
     * LGD_CLOSEDATE는 LGD_TIMESTAMP + 3 일 한 날의 자정까지.
     * <p>
     * <p>
     * ex> 2018-01-24 13:01:01 -> 2018-01-28 00:00:00
     * </p>
     *
     * @param LGD_TIMESTAMP
     * @return
     */
    public static String getLGD_CLOSEDATE(String LGD_TIMESTAMP) {
        long timestamp = DateUtil.toInstant(LGD_TIMESTAMP, "yyyyMMddHHmmss", DateUtil.ASIA_SEOUL_ZONE).toEpochMilli();
        String dateStr = DateUtil.getDateString(timestamp + (1000 * 60 * 60 * 24) * CLOSE_PERIOD);
        return dateStr + "235959";
        //String dateStr = DateUtil.getDateTimeString(timestamp + (1000 * 60 * 60 * 1)); // For test. 1 hour
        //return dateStr;
    }
}
