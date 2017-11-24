package com.lezhin.avengers.panther.internalpayment;

import com.lezhin.avengers.panther.Context;
import com.lezhin.avengers.panther.ErrorCode;
import com.lezhin.avengers.panther.config.LezhinProperties;
import com.lezhin.avengers.panther.exception.InternalPaymentException;
import com.lezhin.avengers.panther.executor.Executor;
import com.lezhin.avengers.panther.model.Meta;
import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.util.JsonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


/**
 * @author seoeun
 * @since 2017.11.08
 */
@Service
public class InternalPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(InternalPaymentService.class);

    @Autowired
    private LezhinProperties lezhinProperties;

    @Autowired
    private ClientHttpRequestFactory clientHttpRequestFactory;

    public InternalPaymentService() {

    }


    public <T extends PGPayment> Payment<T> reserve(Context<T> context) {

        String url = lezhinProperties.getApiUrl() + "/reserve";
        logger.info("RESERVE. call to {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + context.getRequestInfo().getToken());

        Payment payment = context.getPayment();
        T pgPayment = (T) payment.getPgPayment();
        payment.setPgPayment(null); // internal에 없는 모델 제외. TODO annotation
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpEntity<Payment> request = new HttpEntity<>(payment, headers);
        logger.info("RESERVE. send : \n{}", JsonUtil.toJson(payment));
        Result<Payment<T>> response = restTemplate.postForObject(url, request, Result.class);

        logger.info("RESERVE internal.reserve response code = {}, {} ", response.getCode(), response.getDescription());

        Payment<T> responsePayment = JsonUtil.fromJsonToPayment(JsonUtil.toJson(response.getData()),
                (Class<T>) pgPayment.getClass());
        responsePayment.setPgPayment(pgPayment);
        return responsePayment;

    }

    public <T extends PGPayment> Payment<T> authenticate(Context<T> context) {

        // 각 PGExecutor.authenticate 실패했는지 성공했는 지.
        boolean executionSucceed = true;
        if (context.getRequestInfo().getExecutorType() == Executor.Type.HAPPYPOINT) {
            executionSucceed = context.getResponseInfo().getCode().equals(ErrorCode.SPC_OK.getCode());
        }
        String url = lezhinProperties.getApiUrl() + "/" + context.getPayment().getPaymentId();
        if (executionSucceed) {
            url += "/authentication/success";
        } else {
            url += "/authentication/fail";
        }
        logger.info("AUTHENTICATE. call to {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + context.getRequestInfo().getToken());

        Payment payment = context.getPayment();
        T pgPayment = (T) payment.getPgPayment();
        Meta meta = payment.getMeta();
        Map<String, Object> receiptMap = context.getPayment().getPgPayment().createReceipt();
        // receipt 은 map을 json으로
        String receipt = JsonUtil.toJson(receiptMap);
        // Internal Datastore meta. String properties must be 1500 bytes or less.
        if (receipt.length() > 1400) {
            logger.info("receipt length = {}", receipt.length());
        }
        if (executionSucceed) {
            // do nothing
        } else {
            meta.setMeta(receipt);
        }

        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpEntity<Meta> request = new HttpEntity<>(meta, headers);
        logger.info("AUTHENTICATE send : \n{}", JsonUtil.toJson(meta));

        HttpEntity<Result> response = restTemplate.exchange(url, HttpMethod.PUT, request, Result.class);

        logger.info("AUTHENTICATE. internal.authenticate response code = {}, {} ", response.getBody().getCode(),
                response.getBody().getDescription());

        Result data = response.getBody();
        String jsonData = JsonUtil.toJson(data.getData());
        logger.info("jsonData :: \n{}", jsonData);
        //response.getBody();
        if (data.getData() != null) {
            Payment<T> responsePayment = convert(jsonData, (Class<T>) context.getPayment().getPgPayment().getClass(),
                    pgPayment, payment.getExternalStoreProductId());
            return responsePayment;
        }

        // fail
        throw new InternalPaymentException(context.getRequestInfo().getExecutorType(),
                String.valueOf(response.getBody().getCode()));
    }

    public <T extends PGPayment> Payment<T> pay(Context<T> context) {

        // 각 pg 사의 pay execution이 실패했는지 성공했는 지.
        boolean executionSucceed = true;
        if (context.getRequestInfo().getExecutorType() == Executor.Type.HAPPYPOINT) {
            executionSucceed = context.getResponseInfo().getCode().equals(ErrorCode.SPC_OK.getCode());
        }

        String url = lezhinProperties.getApiUrl() + "/" + context.getPayment().getPaymentId();
        if (executionSucceed) {
            url += "/complete";
        } else {
            url += "/fail";
        }
        logger.info("PAY. call to {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + context.getRequestInfo().getToken());

        Payment<T> payment = context.getPayment();
        T pgPayment = payment.getPgPayment();
        Meta meta = payment.getMeta();
        meta.setApprovalId(pgPayment.getApprovalId());
        Map<String, Object> receiptMap = context.getPayment().getPgPayment().createReceipt();
        // receipt 은 map을 json으로
        String receipt = JsonUtil.toJson(receiptMap);
        // Internal Datastore meta. String properties must be 1500 bytes or less.
        if (receipt.length() > 1400) {
            logger.info("receipt length = {}", receipt.length());
        }
        if (executionSucceed) {
            meta.setReceipt(receipt);
        } else {
            // FIXME fail 일 때 어떤 타입을 줘야 하나.
            meta.setMeta(receipt);
        }

        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpEntity<Meta> request = new HttpEntity<>(meta, headers);
        logger.info("PAY. send : \n{}", JsonUtil.toJson(meta));

        HttpEntity<Result> response = restTemplate.exchange(url, HttpMethod.PUT, request, Result.class);
        logger.info("PAY. internal.pay response code = {}, {} ", response.getBody().getCode(),
                response.getBody().getDescription());

        Result data = response.getBody();
        String jsonData = JsonUtil.toJson(response.getBody().getData());
        logger.info("jsonData :: \n{}", jsonData);
        //response.getBody();
        if (data.getData() != null) {
            Payment<T> responsePayment = convert(jsonData, (Class<T>) context.getPayment().getPgPayment().getClass(),
                    pgPayment, payment.getExternalStoreProductId());
            return responsePayment;
        }

        // fail
        throw new InternalPaymentException(context.getRequestInfo().getExecutorType(),
                String.valueOf(response.getBody().getCode()));
    }

    public <T extends PGPayment> Payment<T> convert(String jsonData, Class<T> pgPaymentClass, T pgPayment, String
            externalStoreProductId) {
        Payment<T> responsePayment = JsonUtil.fromJsonToPayment(jsonData, pgPaymentClass);
        logger.info("responsePayment = {} \n{}", responsePayment.getPaymentId(),
                JsonUtil.toJson(responsePayment));
        responsePayment.setPgPayment(pgPayment);
        responsePayment.setExternalStoreProductId(externalStoreProductId);
        return responsePayment;
    }

}
