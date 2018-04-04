package com.lezhin.panther.pg.happypoint;

import com.lezhin.avengers.panther.model.HappypointAggregator;
import com.lezhin.panther.Context;
import com.lezhin.panther.SimpleCacheService;
import com.lezhin.panther.exception.CIException;
import com.lezhin.panther.exception.ExceedException;
import com.lezhin.panther.exception.HappyPointParamException;
import com.lezhin.panther.exception.HappyPointSystemException;
import com.lezhin.panther.exception.PreconditionException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.Certification;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;
import com.lezhin.panther.step.Command;
import com.lezhin.panther.util.DateUtil;
import com.lezhin.panther.util.JsonUtil;
import com.lezhin.panther.util.Util;

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

import java.time.Instant;
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

    /**
     * 2000point/mbrNo/month
     */
    public static final Integer POINT_LIMITATION = new Integer(2000);
    private static final Logger logger = LoggerFactory.getLogger(HappyPointExecutor.class);
    private Map<Command.Type, Command.Type> transitionMap = ImmutableMap.of(
            Command.Type.RESERVE, Command.Type.AUTHENTICATE,
            Command.Type.AUTHENTICATE, Command.Type.PAY,
            Command.Type.PAY, Command.Type.COMPLETE);

    @Autowired
    private SimpleCacheService cacheService;
    @Autowired
    private ClientHttpRequestFactory clientHttpRequestFactory;

    public HappyPointExecutor() {
        this.type = Type.HAPPYPOINT;
    }

    public HappyPointExecutor(Context<HappyPointPayment> context) {
        super(context);
        this.type = Type.HAPPYPOINT;
    }

    /**
     * TraceNo = 기관코드 + 전송일자 + 추적번호는 Unique 해야 함. 20 byte
     *
     * @param instCd 기관코드
     * @return
     */
    public static String createTraceNo(String instCd, String date, String time, Long userId, Long paymentId) {
        String traceNo = String.format("%s%s%s", instCd, date, time);
        return traceNo;
    }

    public void verifyPrecondition(Command.Type commandType) throws PreconditionException {
        if (commandType == Command.Type.RESERVE) {
            if (context.getPayment().getPgPayment().getMbrNo() == null ||
                    context.getPayment().getPgPayment().getMbrNo().equals("")) {
                throw new PreconditionException(type, "mbrNo can not be null nor empty");
            }
            if (context.getPayment().getPgPayment().getUseReqPt() == null) {
                throw new PreconditionException(type, "useReqPt can not be null");
            }
            // 2000point/mbrNo/month
            PointAggregator aggregator = cacheService.getHappypointAggregator(
                    context.getPayment().getPgPayment().getMbrNo(),
                    DateUtil.format(Instant.now().toEpochMilli(), DateUtil.ASIA_SEOUL_ZONE, "yyyyMM"));
            if (aggregator != null &&
                    aggregator.getPointSum().intValue() + context.getPayment().getPgPayment().getUseReqPt().intValue() >
                            POINT_LIMITATION.intValue()) {
                throw new ExceedException(type, "Exceed 2000 point/ month");
            }
        }
    }

    public Payment<HappyPointPayment> checkPoint() throws HappyPointParamException, HappyPointSystemException {
        logger.info("checkPoint. {}", context.printPretty());

        Payment<HappyPointPayment> payment = context.getPayment();
        HappyPointPayment requestPayment = Util.merge(payment.getPgPayment(),
                HappyPointPayment.API.pointcheck.createRequest(), HappyPointPayment.class);
        logger.info("requestPayment. merged = \n{}", JsonUtil.toJson(requestPayment));
        if (requestPayment.getTracNo() == null) {
            requestPayment.setTracNo(createTraceNo(payment));
        }
        requestPayment.setMchtNo(pantherProperties.getHappypoint().getMchtNo());
        requestPayment = clearResponseField(requestPayment);
        payment.setPgPayment(requestPayment);

        logger.info("send for checkPoint. \n{}", JsonUtil.toJson(requestPayment));

        // 포인트 조회
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        HttpEntity<HappyPointPayment> request = new HttpEntity<>(requestPayment);
        HappyPointPayment response = restTemplate.postForObject(pantherProperties.getHappypoint().getHpcUrl(),
                request, HappyPointPayment.class);
        logger.info("CHECK POINT response from happypoint: {} = {} \n{}", response.getRpsCd(),
                response.getRpsDtlMsg(),
                JsonUtil.toJson(response));

        payment.setPgPayment(response);

        context = context.payment(payment);
        context = context.response(new ResponseInfo(payment.getPgPayment().getRpsCd(),
                payment.getPgPayment().getRpsDtlMsg()));

        String responseCode = context.getResponseInfo().getCode();
        handleResponseCode(responseCode);

        return payment;
    }

    /**
     * Happypoint의 prepare 단계는 point 조회.(회원 인증 후)
     *
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
            throw new CIException(type,
                    "No ConnectionInfo. Certification CI failed. userId = " + context.getRequestInfo().getUserId());
        }
        String name = certification.getName();
        String ci = certification.getCI();
        logger.info("userId = {}, name = {}, ci = {}", context.getRequestInfo().getUserId(), name, ci);

        requestPayment.setMbrNm(name);
        requestPayment.setMbrIdfNo(ci);
        payment.setPgPayment(requestPayment);

        logger.info("send for auth. \n{}", JsonUtil.toJson(requestPayment));

        // 회원인증
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        HttpEntity<HappyPointPayment> request = new HttpEntity<>(requestPayment);
        HappyPointPayment response = restTemplate.postForObject(pantherProperties.getHappypoint().getHpcUrl(),
                request, HappyPointPayment.class);
        logger.info("PREPARE.(auth) response from happypoint: {} = {} \n{}", response.getRpsCd(),
                response.getRpsDtlMsg(),
                JsonUtil.toJson(response));

        payment.setPgPayment(response);

        context = context.payment(payment)
                .response(new ResponseInfo(payment.getPgPayment().getRpsCd(),
                        payment.getPgPayment().getRpsDtlMsg()));

        String responseCode = context.getResponseInfo().getCode();
        handleResponseCode(responseCode);

        // 포인트조회.
        payment = checkPoint();

        return payment;

    }

    public Payment<HappyPointPayment> reserve() {

        // do nothing
        Payment<HappyPointPayment> payment = context.getPayment();
        context = context.response(new ResponseInfo(ResponseCode.SPC_OK));
        return payment;
    }

    public Payment<HappyPointPayment> authenticate() {
        if (context.getPayment().getPgPayment().getUseReqPt() == null) {
            throw new PreconditionException(type, "useReqPt can not be null");
        }

        // do nothing. response ok
        Payment<HappyPointPayment> payment = context.getPayment();
        context = context.response(new ResponseInfo(ResponseCode.SPC_OK));
        return payment;
    }

    public Payment<HappyPointPayment> pay() {

        Payment<HappyPointPayment> payment = context.getPayment();
        logger.debug("base payment = \n{}", JsonUtil.toJson(payment.getPgPayment()));
        HappyPointPayment requestPayment = Util.merge(payment.getPgPayment(),
                HappyPointPayment.API.pointuse.createRequest(), HappyPointPayment.class);
        payment.setPgPayment(requestPayment);
        logger.debug("merged payment = \n{}", JsonUtil.toJson(requestPayment));
        requestPayment.setTracNo(createTraceNo(payment));
        requestPayment.setTrxTypCd(HappyPointPayment.trxTypCd_USE);
        requestPayment.setMchtNo(pantherProperties.getHappypoint().getMchtNo());
        requestPayment.setTrxDt(requestPayment.getTrsDt()); // 전송일자를 거래일자로 셋팅
        requestPayment.setTrxTm(requestPayment.getTrsTm()); // 전송시간을 거래시간으로 셋팅
        requestPayment.setTrxAmt(Long.valueOf(String.valueOf(payment.getAmount().intValue())));
        requestPayment = clearResponseField(requestPayment);
        payment.setPgPayment(requestPayment);

        logger.info("PAY. send. = \n{}", JsonUtil.toJson(requestPayment));

        // 포인트 사용
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        HttpEntity<HappyPointPayment> request = new HttpEntity<>(requestPayment);
        HappyPointPayment response = restTemplate.postForObject(pantherProperties.getHappypoint().getHpcUrl(),
                request, HappyPointPayment.class);
        logger.info("PAY. response from happypoint: {} = {}, \n{}", response.getRpsCd(), response.getRpsDtlMsg(),
                JsonUtil.toJson(response));

        payment.setPgPayment(response);

        context = context.payment(payment);
        context = context.response(new ResponseInfo(payment.getPgPayment().getRpsCd(),
                payment.getPgPayment().getRpsDtlMsg()));

        handleResponseCode(context.getResponseInfo().getCode());

        return payment;
    }

    public Payment<HappyPointPayment> complete() {
        if (context.getResponseInfo().getCode().equals(ResponseCode.SPC_OK.getCode())) {
            // on Success
            HappyPointPayment happyPointPayment = context.getPayment().getPgPayment();
            String trxDt = happyPointPayment.getTrxDt();
            String mbrNo = happyPointPayment.getMbrNo();
            Instant trxInstant = DateUtil.toInstantFromDate(trxDt, "yyyyMMdd", DateUtil.ASIA_SEOUL_ZONE);
            String ym = DateUtil.format(trxInstant.toEpochMilli(), DateUtil.ASIA_SEOUL_ZONE, "yyyyMM");

            PointAggregator aggregator = new PointAggregator(mbrNo, ym,
                    happyPointPayment.getUsePt());
            cacheService.saveHappypointAggregator(aggregator);
        }
        return context.getPayment();
    }

    public Payment refund() {

        Payment<HappyPointPayment> payment = context.getPayment();
        String mbrNo = payment.getPgPayment().getMbrNo();
        String orglTrxAprvDt = payment.getPgPayment().getAprvDt();
        String orglTrxAprvNo = payment.getPgPayment().getAprvNo();
        Long amount = payment.getPgPayment().getTrxAmt();
        logger.info("REFUND. orglTrxAprvNo = {}, orglTrxAprvDt = {}", orglTrxAprvNo, orglTrxAprvDt);
        HappyPointPayment requestPayment = Util.merge(payment.getPgPayment(),
                HappyPointPayment.API.pointrefund.createRequest(), HappyPointPayment.class);
        payment.setPgPayment(requestPayment);
        requestPayment.setTracNo(createTraceNo(payment));
        requestPayment.setTrxTypCd(HappyPointPayment.trxTypCd_CANCEL);
        requestPayment.setMchtNo(pantherProperties.getHappypoint().getMchtNo());
        requestPayment.setTrxDt(requestPayment.getTrsDt()); // 전송일자를 거래일자로 셋팅
        requestPayment.setTrxTm(requestPayment.getTrsTm()); // 전송시간을 거래시간으로 셋팅
        requestPayment.setTrxAmt(amount);
        requestPayment.setOrglTrxAprvDt(orglTrxAprvDt);
        requestPayment.setOrglTrxAprvNo(orglTrxAprvNo);
        requestPayment = clearResponseField(requestPayment);
        payment.setPgPayment(requestPayment);

        logger.info("REFUND. send. = \n{}", JsonUtil.toJson(requestPayment));

        // 포인트 취소
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        HttpEntity<HappyPointPayment> request = new HttpEntity<>(requestPayment);
        HappyPointPayment response = restTemplate.postForObject(pantherProperties.getHappypoint().getHpcUrl(),
                request, HappyPointPayment.class);
        logger.info("REFUND. response from happypoint: {} = {}, \n{}", response.getRpsCd(), response.getRpsDtlMsg(),
                JsonUtil.toJson(response));

        payment.setPgPayment(response);

        if (response.getRpsCd().equals(ResponseCode.SPC_OK.getCode())) {
            String ym = orglTrxAprvDt.substring(0, 6);
            PointAggregator aggregator = cacheService.getHappypointAggregator(mbrNo, ym);
            if (aggregator != null) {
                int pointSum = aggregator.getPointSum();
                int newPointSum = pointSum - amount.intValue();
                HappypointAggregator newAggregator = new HappypointAggregator(mbrNo, ym, newPointSum);
                cacheService.resetHappypointAggregator(newAggregator);
                logger.info("happypoint aggregator: {}, {} --> {}", ym, pointSum, newPointSum);
            }
        }

        context = context.payment(payment)
                .response(new ResponseInfo(payment.getPgPayment().getRpsCd(),
                        payment.getPgPayment().getRpsDtlMsg()));

        handleResponseCode(context.getResponseInfo().getCode());

        return payment;
    }

    public String createTraceNo(Payment<HappyPointPayment> payment) {
        return createTraceNo(payment.getPgPayment().getInstCd(), payment.getPgPayment().getTrsDt(),
                payment.getPgPayment().getTrsTm(), payment.getUserId(), payment.getPaymentId());
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
     *
     * @param responseCode
     * @throws HappyPointParamException
     * @throws HappyPointSystemException
     */
    public void handleResponseCode(String responseCode) throws HappyPointParamException, HappyPointSystemException {
        if (ResponseCode.SPC_DENY_44.getCode().equals(responseCode)
                || ResponseCode.SPC_DENY_77.getCode().equals(responseCode)
                || ResponseCode.SPC_DENY_88.getCode().equals(responseCode)) {
            throw new HappyPointParamException(type, context.getResponseInfo().getCode(),
                    context.getResponseInfo().getDescription());
        }
        if (ResponseCode.SPC_ERROR_22.getCode().equals(responseCode)
                || ResponseCode.SPC_ERROR_80.getCode().equals(responseCode)
                || ResponseCode.SPC_ERROR_92.getCode().equals(responseCode)
                || ResponseCode.SPC_ERROR_99.getCode().equals(responseCode)) {
            throw new HappyPointSystemException(type, context.getResponseInfo().getCode(),
                    context.getResponseInfo().getDescription());
        }
        // 문서에 정의 되지 않은 response code가 올 수도 있음.
        if (!responseCode.equals(ResponseCode.SPC_OK.getCode())) {
            throw new HappyPointSystemException(type, context.getResponseInfo().getCode(),
                    context.getResponseInfo().getDescription());
        }
    }

}
