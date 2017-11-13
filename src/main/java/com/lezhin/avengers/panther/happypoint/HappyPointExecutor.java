package com.lezhin.avengers.panther.happypoint;

import com.lezhin.avengers.panther.CertificationService;
import com.lezhin.avengers.panther.Context;
import com.lezhin.avengers.panther.ErrorCode;
import com.lezhin.avengers.panther.command.Command;
import com.lezhin.avengers.panther.exception.HappyPointParamException;
import com.lezhin.avengers.panther.exception.HappyPointSystemException;
import com.lezhin.avengers.panther.exception.PantherException;
import com.lezhin.avengers.panther.exception.ParameterException;
import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.executor.Executor;
import com.lezhin.avengers.panther.model.Certification;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.ResponseInfo;
import com.lezhin.avengers.panther.util.JsonUtil;
import com.lezhin.avengers.panther.util.Util;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

/**
 * @author seoeun
 * @since 2017.10.24
 */
@Component
@Qualifier("happypoint")
@Scope("prototype")
public class HappyPointExecutor extends Executor<HappyPointPayment> {

    private static final Logger logger = LoggerFactory.getLogger(HappyPointExecutor.class);

    private Map<Command.Type, Command.Type> transitionMap = ImmutableMap.of(
            Command.Type.RESERVE, Command.Type.AUTHENTICATE,
            Command.Type.AUTHENTICATE, Command.Type.PAY);

    @Autowired
    private CertificationService cacheService;
    @Autowired
    private ClientHttpRequestFactory clientHttpRequestFactory;

    public HappyPointExecutor() {
        this.type = Type.HAPPYPOINT;
    }

    public HappyPointExecutor(Context<HappyPointPayment> context) {
        super(context);
        this.type = Type.HAPPYPOINT;
    }

    public Payment<HappyPointPayment> checkPoint() throws HappyPointParamException, HappyPointSystemException {
        logger.info("checkPoint. {}", context.printPretty());

        Payment<HappyPointPayment> payment = context.getPayment();
        HappyPointPayment requestPayment = Util.merge(payment.getPgPayment(),
                HappyPointPayment.API.pointcheck.createRequest(), HappyPointPayment.class);
        logger.info("requestPayment. merged = {}", JsonUtil.toJson(requestPayment));
        if (requestPayment.getTracNo() ==  null) {
            requestPayment.setTracNo(createTraceNo(payment));
        }
        requestPayment.setMchtNo(lezhinProperties.getHappypoint().getMchtNo());
        requestPayment = clearResponseField(requestPayment);
        payment.setPgPayment(requestPayment);

        logger.info("send for checkPoint. {}", JsonUtil.toJson(requestPayment));

        // 포인트 조회
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        HttpEntity<HappyPointPayment> request = new HttpEntity<>(requestPayment);
        HappyPointPayment response = restTemplate.postForObject(lezhinProperties.getHappypoint().getHpcUrl(),
                request, HappyPointPayment.class);
        logger.info("response from happypoint {}, {}, \n{}: ", response.getRpsCd(), response.getRpsDtlMsg(),
                JsonUtil.toJson(response));

        payment.setPgPayment(response);

        context = context.withPayment(payment);
        context = context.withResponse(new ResponseInfo(payment.getPgPayment().getRpsCd(),
                payment.getPgPayment().getRpsDtlMsg()));

        String responseCode = context.getResponseInfo().getCode();
        handleResponseCode(responseCode);

        return payment;
    }

    /**
     * Happypoint의 prepare 단계는 point 조회.(회원 인증 후)
     * @return
     * @throws HappyPointParamException
     * @throws HappyPointSystemException
     */
    public Payment<HappyPointPayment> prepare() throws HappyPointParamException, HappyPointSystemException {

        logger.info("PREPARE. {}", context.printPretty());

        Payment<HappyPointPayment> payment = context.getPayment();
        HappyPointPayment requestPayment = Util.merge(payment.getPgPayment(),
                HappyPointPayment.API.authentication.createRequest(), HappyPointPayment.class);
        payment.setPgPayment(requestPayment);
        requestPayment.setTracNo(createTraceNo(payment));

        Certification certification = cacheService.getCertification(Long.valueOf(context.getRequestInfo().getUserId()));
        if (certification == null) {
            throw new PantherException("No ConnectionInfo. Certification failed");
        }
        String name = certification.getName();
        String ci = certification.getCI();
        logger.info("name = {}, ci = {}", name, ci);

        requestPayment.setMbrNm(name);
        requestPayment.setMbrIdfNo(ci);
        payment.setPgPayment(requestPayment);

        logger.info("send for auth. {}", JsonUtil.toJson(requestPayment));

        // 회원인증
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        HttpEntity<HappyPointPayment> request = new HttpEntity<>(requestPayment);
        HappyPointPayment response = restTemplate.postForObject(lezhinProperties.getHappypoint().getHpcUrl(),
                request, HappyPointPayment.class);
        logger.info("response from happypoint: {}", JsonUtil.toJson(response));

        payment.setPgPayment(response);

        context = context.withPayment(payment);
        context = context.withResponse(new ResponseInfo(payment.getPgPayment().getRpsCd(),
                payment.getPgPayment().getRpsDtlMsg()));

        String responseCode = context.getResponseInfo().getCode();
        handleResponseCode(responseCode);

        // 포인트조회.
        payment = checkPoint();

        return payment;

    }

    public Payment<HappyPointPayment> reserve() {
        if (context.getPayment().getPgPayment().getMbrNo() == null) {
            throw new PreconditionException("mbrNo can not be bull");
        }
        if (context.getPayment().getPgPayment().getMbrNm() == null) {
            throw new PreconditionException("mbrNm can not be bull");
        }
        if (context.getPayment().getPgPayment().getUseReqPt() == null) {
            throw new PreconditionException("useReqPt can not be bull");
        }

        // do nothing
        Payment<HappyPointPayment> payment = context.getPayment();
        context = context.withResponse(new ResponseInfo(ErrorCode.SPC_OK));
        return payment;
    }

    public Payment<HappyPointPayment> authenticate() {
        if (context.getPayment().getPgPayment().getUseReqPt() == null) {
            throw new PreconditionException("useReqPt can not be bull");
        }

        // do nothing. response ok
        Payment<HappyPointPayment> payment = context.getPayment();
        context = context.withResponse(new ResponseInfo(ErrorCode.SPC_OK));
        return payment;
    }

    public Payment<HappyPointPayment> pay() {

        Payment<HappyPointPayment> payment = context.getPayment();
        logger.debug("base payment = {}", JsonUtil.toJson(payment.getPgPayment()));
        HappyPointPayment requestPayment = Util.merge(payment.getPgPayment(),
                HappyPointPayment.API.pointuse.createRequest(), HappyPointPayment.class);
        payment.setPgPayment(requestPayment);
        logger.debug("merged payment = {}", JsonUtil.toJson(requestPayment));
        requestPayment.setTracNo(createTraceNo(payment));
        requestPayment.setTrxTypCd(HappyPointPayment.trxTypCd_USE);
        requestPayment.setMchtNo(lezhinProperties.getHappypoint().getMchtNo());
        requestPayment.setTrxDt(requestPayment.getTrsDt()); // 전송일자를 거래일자로 셋팅
        requestPayment.setTrxTm(requestPayment.getTrsTm()); // 전송시간을 거래시간으로 셋팅
        requestPayment.setTrxAmt(payment.getAmount().toString()); // FIXME int 로 변환??
        requestPayment = clearResponseField(requestPayment);
        payment.setPgPayment(requestPayment);

        logger.info("PAY. send = {}", JsonUtil.toJson(requestPayment));

        // 포인트 사용
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        HttpEntity<HappyPointPayment> request = new HttpEntity<>(requestPayment);
        HappyPointPayment response = restTemplate.postForObject(lezhinProperties.getHappypoint().getHpcUrl(),
                request, HappyPointPayment.class);
        logger.info("PAY. response from happypoint: {} = {}, {}", response.getRpsCd(), response.getRpsMsgCtt(),
                JsonUtil.toJson(response));

        payment.setPgPayment(response);

        context = context.withPayment(payment);
        context = context.withResponse(new ResponseInfo(payment.getPgPayment().getRpsCd(),
                payment.getPgPayment().getRpsDtlMsg()));

        handleResponseCode(context.getResponseInfo().getCode());

        return payment;
    }

    public void complete() {

    }

    public String createTraceNo(Payment<HappyPointPayment> payment) {
        return createTraceNo(payment.getPgPayment().getInstCd(), payment.getPgPayment().getTrsDt(),
                payment.getPgPayment().getTrsTm(), payment.getUserId(), payment.getPaymentId());
    }

    /**
     * TraceNo = 기관코드 + 전송일자 + 추적번호는 Unique 해야 함. 20 byte
     *
     * @param instCd 기관코드
     * @return
     */
    public String createTraceNo(String instCd, String date, String time, Long userId, Long paymentId) {
        String traceNo = String.format("%s%s%s", instCd, date, time);
        logger.info("createTraceNo. u={}, p={}, traceNo={}", userId, paymentId, traceNo);
        return traceNo;
    }

    public Command.Type nextTransition(Command.Type currentStep) {
        Command.Type nextStep = Optional.ofNullable(transitionMap.get(currentStep)).orElse(Command.Type.DONE);
        return nextStep;
    }

    public HappyPointPayment clearResponseField(final HappyPointPayment happyPointPayment) {
        happyPointPayment.setRpsCd(null);
        happyPointPayment.setRpsMsgCtt(null);
        happyPointPayment.setRpsDtlCd(null);
        happyPointPayment.setRpsDtlMsg(null);
        return happyPointPayment;
    }

    /**
     * Throws Exception if happypoint execution failed.
     * @param responseCode
     * @throws HappyPointParamException
     * @throws HappyPointSystemException
     */
    public void handleResponseCode(String responseCode) throws HappyPointParamException, HappyPointSystemException{
        if (ErrorCode.SPC_DENY_44.getCode().equals(responseCode)
                || ErrorCode.SPC_DENY_77.getCode().equals(responseCode)
                || ErrorCode.SPC_DENY_88.getCode().equals(responseCode)) {
            throw new HappyPointParamException(context.getResponseInfo().getCode(),
                    context.getResponseInfo().getMessage());
        }
        if (ErrorCode.SPC_ERROR_22.getCode().equals(responseCode)
                || ErrorCode.SPC_ERROR_80.getCode().equals(responseCode)
                || ErrorCode.SPC_ERROR_92.getCode().equals(responseCode)
                || ErrorCode.SPC_ERROR_99.getCode().equals(responseCode)) {
            throw new HappyPointSystemException(context.getResponseInfo().getCode(),
                    context.getResponseInfo().getMessage());
        }
        // FXIME response code 에 17 있음. 문서에 정의 되지 않은 errorCode 임.
        if (!responseCode.equals(ErrorCode.SPC_OK.getCode())) {
            throw new HappyPointSystemException(context.getResponseInfo().getCode(),
                    context.getResponseInfo().getMessage());
        }
    }

}
