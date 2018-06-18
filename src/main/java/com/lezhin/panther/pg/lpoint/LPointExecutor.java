package com.lezhin.panther.pg.lpoint;

import com.google.common.collect.ImmutableMap;
import com.lezhin.panther.Context;
import com.lezhin.panther.SimpleCacheService;
import com.lezhin.panther.exception.CIException;
import com.lezhin.panther.exception.LPointException;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.Certification;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.step.Command;
import com.lezhin.panther.util.DateUtil;
import com.lezhin.panther.util.JsonUtil;
import com.lezhin.panther.util.Util;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Created by taemmy on 2018. 5. 25.
 */
@Component
@Qualifier("lpoint")
@Scope("prototype")
public class LPointExecutor extends Executor<LPointPayment> {
    private static final Logger logger = LoggerFactory.getLogger(LPointExecutor.class);

    private Map<Command.Type, Command.Type> transitionMap = ImmutableMap.of(
            Command.Type.PREAUTHENTICATE, Command.Type.AUTHENTICATE,
            Command.Type.AUTHENTICATE, Command.Type.PAY,
            Command.Type.PAY, Command.Type.COMPLETE);

    private final String AES_IV = "l-members-lpoint";
    private final String CHAR_TYPE = "UTF-8";

    @Autowired
    private ClientHttpRequestFactory clientHttpRequestFactory;
    @Autowired
    private SimpleCacheService cacheService;

    protected LPointExecutor() {
        this.type = Type.LPOINT;
        logger.warn("Context can not be null");
    }

    public LPointExecutor(Context<LPointPayment> context) {
        super(context);
        this.type = Type.LPOINT;
    }

    @Override
    public Payment<LPointPayment> prepare() throws LPointException {
        Payment<LPointPayment> payment = context.getPayment();
        LPointPayment pgPayment = payment.getPgPayment();

        // 인증정보조회(from Cache)
        Certification certification = cacheService.getCertification(context.getRequestInfo().getUserId());
        if (certification == null || certification.getCI().isEmpty()) {
            throw new CIException(type,
                    "No ConnectionInfo. Certification CI failed. userId = " + context.getRequestInfo().getUserId());
        }

        // 회원인증
        pgPayment.setCiNo(certification.getCI());

        LPointPayment rspAuth = request(makePayload(LPointPayment.API.AUTHENTICATION));
        payment.setPgPayment(rspAuth);

        context = context.payment(payment)
                .response(new ResponseInfo(rspAuth.getControl().getRspC(), rspAuth.getMsgCn1()));

        handleResponse(context);

        // 전환가능 포인트조회
        LPointPayment rspData = request(makePayload(LPointPayment.API.POINT_CHECK));
        // 가용포인트(전환가능포인트조회에 없음)
        rspData.setAvlPt(rspAuth.getAvlPt());
        payment.setPgPayment(rspData);

        context = context.payment(payment)
                .response(new ResponseInfo(rspData.getControl().getRspC(), rspData.getMsg()));

        handleResponse(context);
        return payment;
    }

    @Override
    public Payment<LPointPayment> reserve() {

        // do nothing
        Payment<LPointPayment> payment = context.getPayment();
        context = context.response(new ResponseInfo(ResponseInfo.ResponseCode.LPOINT_OK));
        return payment;
    }

    @Override
    public Payment<LPointPayment> preAuthenticate() {
        // 비밀번호 체크를 status 변경없이 처리하기 위해서
        Payment<LPointPayment> payment = context.getPayment();

        if (payment.getPgPayment().getPswd() == null || payment.getPgPayment().getPswd().isEmpty()) {
            throw new ParameterException(type, "pswd can not be null or empty");
        }

        // 포인트 사용 비밀번호 체크
        LPointPayment rspData = request(makePayload(LPointPayment.API.PSWD_CHECK));
        payment.setPgPayment(rspData);

        String rspC = rspData.getControl().getRspC();
        if(!"0".equals(rspData.getRspC())) {
            // O720 실패시 440X 로 ResponseInfo code 변경
            rspC += "0" + rspData.getRspC();
        }

        context = context.payment(payment)
                .response(new ResponseInfo(rspC, rspData.getRspMsgCn()));

        handleResponse(context);
        return payment;
    }

    @Override
    public Payment<LPointPayment> authenticate() {

        // do nothing
        Payment<LPointPayment> payment = context.getPayment();
        context = context.response(new ResponseInfo(ResponseInfo.ResponseCode.LPOINT_OK));
        return payment;
    }

    @Override
    public Payment<LPointPayment> pay() {
        Payment<LPointPayment> payment = context.getPayment();

        // 포인트전환 요청
        LPointPayment rspData = request(makePayload(LPointPayment.API.POINT_USE));
        rspData.setApprovalId(rspData.getAprno());
        payment.setPgPayment(rspData);

        context = context.payment(payment)
                .response(new ResponseInfo(rspData.getControl().getRspC(), rspData.getMsg()));

        handleResponse(context);
        return payment;
    }

    @Override
    public Payment<LPointPayment> complete() {
        // 기본적으로 L.Point에서 전환가능 포인트를 제한해주고 있으나 레진 자체적으로 제한처리를 변동할 수 있으므로 cache
        if (!ResponseInfo.ResponseCode.LPOINT_OK.getCode().equals(context.getResponseInfo().getCode())) {
            return context.getPayment();
        }

        LPointPayment pgPayment = context.getPayment().getPgPayment();

        String deDt = pgPayment.getAprDt(); // 승인날짜
        Instant deDtInstant = DateUtil.toInstantFromDate(deDt, "yyyyMMdd", DateUtil.ASIA_SEOUL_ZONE);
        String ym = DateUtil.format(deDtInstant.toEpochMilli(), DateUtil.ASIA_SEOUL_ZONE, "yyyyMM");
        String mbrNo = pgPayment.getCstDrmV();
        int amount = pgPayment.getTtnCvPt(); // 전환포인트

        Optional<LPointAggregator> optional = cacheService.getLPointAggregator(ym, mbrNo);
        LPointAggregator aggregator = optional.orElseGet(() -> new LPointAggregator(ym, mbrNo));
        aggregator.add(deDt, amount);

        cacheService.saveLPointAggregator(aggregator);
        return context.getPayment();
    }

    @Override
    public Payment refund() {
        // pay persist 에러가 발생한 경우
        Payment<LPointPayment> payment = context.getPayment();

        // 포인트전환 취소
        LPointPayment pgPayment = payment.getPgPayment();
        pgPayment.getControl().setRspC(LPointPayment.RequestCodeType.CANCEL_REQUEST.getCode());
        LPointPayment rspData = request(pgPayment);

        context = context.payment(payment)
                .response(new ResponseInfo(rspData.getControl().getRspC(), rspData.getMsg()));

        handleResponse(context);
        return payment;
    }

    @Override
    public void verifyPrecondition(Command.Type commandType) throws PreconditionException {
        logger.info("verify lpoint");
        LPointPayment pgPayment = context.getPayment().getPgPayment();
        if (Command.Type.RESERVE == commandType) {
            if (pgPayment.getCtfCno() == null || pgPayment.getCtfCno().isEmpty()) {
                throw new PreconditionException(type, "ctfCno can not be null or empty");
            }
            if (pgPayment.getAkCvPt() == null || pgPayment.getAkCvPt() <= 0) {
                throw new PreconditionException(type, "akCvPt can not be null or zero");
            }

            // 일 최대 3만, 월 최대 30만 엘포인트 전환가능
            String ym = DateUtil.format(Instant.now().toEpochMilli(), DateUtil.ASIA_SEOUL_ZONE, "yyyyMM");
            String ymd = DateUtil.format(Instant.now().toEpochMilli(), DateUtil.ASIA_SEOUL_ZONE, "yyyyMMdd");
            Optional<LPointAggregator> optionalAggregator = cacheService.getLPointAggregator(ym, pgPayment.getCtfCno());
            optionalAggregator.ifPresent(lPointAggregator -> lPointAggregator.isExceed(ymd, pgPayment.getAkCvPt()));

        }
    }

    @Override
    public void handleResponse(Context context) throws LPointException {
        // rspC == "00" 을 제외하고 모두 Exception
        if (!ResponseInfo.ResponseCode.LPOINT_OK.getCode().equals(context.getResponseInfo().getCode())) {
            throw new LPointException(context,
                    context.getResponseInfo().getCode(), context.getResponseInfo().getDescription());
        }
    }

    @Override
    public Command.Type nextTransition(Command.Type currentStep) {
        return Optional.ofNullable(transitionMap.get(currentStep)).orElse(Command.Type.DONE);
    }

    LPointPayment makePayload(LPointPayment.API api) {
        Payment<LPointPayment> payment = context.getPayment();
        LPointPayment reqPayment = payment.getPgPayment();
        LPointPayment payload = api.createReqeust();
        // 공용
        payload.setCopMcno(getMallCode());
        payload.setWcc("3");

        if(LPointPayment.API.AUTHENTICATION.equals(api)) {
            // 회원인증
            payload.setAprAkMdDc("6");
            payload.setCiNo(reqPayment.getCiNo());

        }else if(LPointPayment.API.POINT_CHECK.equals(api)) {
            // 포인트조회
            payload.setCstDrmDc("2");       // 고객실별구분코드 "2": 고객번호
            payload.setCstDrmV(reqPayment.getCtfCno());

        }else if(LPointPayment.API.PSWD_CHECK.equals(api)) {
            // 비밀번호 확인
            payload.setCno(reqPayment.getCtfCno());
            payload.setPswd(md5(reqPayment.getPswd()));
        }else if(LPointPayment.API.POINT_USE.equals(api)) {
            // 포인트 전환요청
            payload.setAprAkMdDc("6");                                     // 승인요청방식 구분코드 6: CI
            payload.setCstDrmDc("2");                                      // 고객식별구분코드 2: 고객번호
            payload.setCstDrmV(reqPayment.getCno());                     // 고객식별값
            payload.setPswd(reqPayment.getPswd());                          // 비밀번호
            payload.setCcoAprno(payment.getPaymentId().toString());        // 제휴사승인번호
            payload.setTfmviDc("3");                                       // 이수관구분코드 3: 이관재적립
            payload.setDeDc("20");                                         // 거래구분코드 이관일 경우 -> "20": 포인트사용
            payload.setDeRsc("230");                                       // 거래사유코드 이관일 경우 -> "230": 포인트전환(이관)
            payload.setRvUDc("1");                                         // 적립사용구분코드
            payload.setAkCvPt(payment.getAmount().intValue());             // 요청전환포인트 (L.Point amount)
            payload.setCcoCstDrmDc("4");                                   // 제휴사고객식별구분코드 4: 제휴사회원번호
            payload.setCcoCstDrmV(payment.getUserId().toString());         // 제휴사고객식별값
        }

        return payload;
    }

    LPointPayment request(LPointPayment req) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Openpoint", "burC=" + LPointPayment.INST_CD + "|aesYn=Y");

            String requestMsg = JsonUtil.toJson(req);
            String encrypted = encrypt(requestMsg);

            logger.debug("request control: {}. \n{}", req.getControl().toString(), requestMsg);

            HttpEntity<String> request = new HttpEntity<>(encrypted, headers);

            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
            ResponseEntity<String> response = restTemplate.postForEntity(pantherProperties.getLpoint().getLpointUrl(), request, String.class);

            Optional.ofNullable(response.getBody()).orElseThrow(() -> new LPointException("response body is empty"));

            String decrypted = decrypt(response.getBody());

            logger.debug("response control: {}. \n{}", req.getControl().toString(), decrypted);

            LPointPayment rsp = JsonUtil.fromJson(decrypted, LPointPayment.class);
            return Util.merge(req, rsp, LPointPayment.class);
        } catch (Exception e) {
            logger.error("request fail - control: {}, {}", req.getControl().toString(), e.getMessage(), e);
            throw new LPointException(context, HttpStatus.INTERNAL_SERVER_ERROR.toString(), "request internal exception");
        }
    }

    private String encrypt(final String message) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new IvParameterSpec(AES_IV.getBytes(CHAR_TYPE)));

        byte[] encrypted = cipher.doFinal(message.getBytes(CHAR_TYPE));
        return new String(Base64.encodeBase64(encrypted));
    }

    private String decrypt(final String message) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new IvParameterSpec(AES_IV.getBytes(CHAR_TYPE)));

        byte[] decoding = Base64.decodeBase64(message.getBytes(CHAR_TYPE));
        return new String(cipher.doFinal(decoding), CHAR_TYPE);
    }

    private SecretKey getSecretKey() throws IOException, ClassNotFoundException {
        // TODO LPointExecutor 로딩할때, 1번만(?) 읽을 수 있는지 생각해보자
        try (FileInputStream fis = new FileInputStream(pantherProperties.getLpoint().getKeyPath() + "/O730.dat");
             ObjectInput in = new ObjectInputStream(fis)) {
            return (SecretKeySpec) in.readObject();
        }
    }

    private String md5(final String message) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(message.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            logger.error("fail encrypt pswd");
        }

        return "";
    }

    private String getMallCode() {
        RequestInfo requestInfo = context.getRequestInfo();

        boolean isMobile = requestInfo.getIsMobile() == null ? false : requestInfo.getIsMobile();
        boolean isApp = requestInfo.getIsApp() == null ? false : requestInfo.getIsApp();

        if (isMobile && isApp) {
            return pantherProperties.getLpoint().getCopMcnoMobile();
        }

        return pantherProperties.getLpoint().getCopMcnoWeb();
    }

}
