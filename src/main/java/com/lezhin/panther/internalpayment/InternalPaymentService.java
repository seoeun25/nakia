package com.lezhin.panther.internalpayment;

import com.lezhin.panther.Context;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.InternalPaymentException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.Meta;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;
import com.lezhin.panther.util.JsonUtil;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * @author seoeun
 * @since 2017.11.08
 */
@Service
public class InternalPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(InternalPaymentService.class);
    private static final List<Class<? extends Exception>> TRANSIENT_EXECPTIONS = ImmutableList.of(
            IOException.class,
            ResourceAccessException.class,
            HttpServerErrorException.class);
    private static final int RETRY_COUNT = 3;
    private PantherProperties pantherProperties;
    private ClientHttpRequestFactory clientHttpRequestFactory;

    public InternalPaymentService(final ClientHttpRequestFactory clientHttpRequestFactory,
                                  final PantherProperties pantherProperties) {
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.pantherProperties = pantherProperties;
    }


    public <T extends PGPayment> Payment<T> reserve(Context<T> context) {

        String url = pantherProperties.getApiUrl() + "/reserve";
        logger.info("RESERVE. to {}. token = {}", url, context.getRequestInfo().getToken());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + context.getRequestInfo().getToken());
        headers.add("Cookie", "JSESSIONID=");

        Payment payment = context.getPayment();
        T pgPayment = (T) payment.getPgPayment();
        payment.setPgPayment(null); // internal에 없는 모델 제외. TODO annotation
        HttpEntity<Payment> request = new HttpEntity<>(payment, headers);
        logger.info("RESERVE. send : \n{}", JsonUtil.toJson(payment));

        HttpEntity<Result> response = exchange(url, HttpMethod.POST, request,
                context.getRequestInfo().getExecutorType());
        logger.info("RESERVE. internal.reserve response code = {}, {} ", response.getBody().getCode(),
                response.getBody().getDescription());

        return convert(response.getBody(), (Class<T>) pgPayment.getClass(), pgPayment, payment.getExternalStoreProductId(),
                context.getRequestInfo().getExecutorType());

    }

    public <T extends PGPayment> Payment<T> authenticate(Context<T> context) {

        // 각 PGExecutor.authenticate 실패했는지 성공했는 지.
        boolean executionSucceed = context.getRequestInfo().getExecutorType()
                .succeeded(context.getResponseInfo());
        String url = pantherProperties.getApiUrl() + "/" + context.getPayment().getPaymentId();
        if (executionSucceed) {
            url += "/authentication/success";
        } else {
            url += "/authentication/fail";
        }
        logger.info("AUTHENTICATE. to {}, token = {}", url, context.getRequestInfo().getToken());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + context.getRequestInfo().getToken());
        headers.add("Cookie", "JSESSIONID=");

        Payment payment = context.getPayment();
        T pgPayment = (T) payment.getPgPayment();
        Meta meta = payment.getMeta();
        if (meta == null) {
            meta = new Meta();
        }
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
            meta.setMeta(receipt);
        }

        HttpEntity<Meta> request = new HttpEntity<>(meta, headers);
        logger.info("AUTHENTICATE send : \n{}", JsonUtil.toJson(meta));

        HttpEntity<Result> response = exchange(url, HttpMethod.PUT, request,
                context.getRequestInfo().getExecutorType());
        logger.info("AUTHENTICATE. internal.authenticate response code = {}, {} ", response.getBody().getCode(),
                response.getBody().getDescription());

        return convert(response.getBody(), (Class<T>) pgPayment.getClass(), pgPayment, payment.getExternalStoreProductId(),
                context.getRequestInfo().getExecutorType());
    }

    public <T extends PGPayment> Payment<T> pay(Context<T> context) {


        // 각 pg 사의 pay execution이 실패했는지 성공했는 지.
        boolean executionSucceed = context.getRequestInfo().getExecutorType()
                .succeeded(context.getResponseInfo());

        String url = pantherProperties.getApiUrl() + "/" + context.getPayment().getPaymentId();
        if (executionSucceed) {
            url += "/complete";
        } else {
            url += "/fail";
        }
        logger.info("PAY. to {}. token = {}", url, context.getRequestInfo().getToken());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + context.getRequestInfo().getToken());
        headers.add("Cookie", "JSESSIONID=");

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
            // TODO fail 일 때 어떤 타입을 줘야 하나.
            meta.setMeta(receipt);
        }

        HttpEntity<Meta> request = new HttpEntity<>(meta, headers);
        logger.info("PAY. send : \n{}", JsonUtil.toJson(meta));

        HttpEntity<Result> response = exchange(url, HttpMethod.PUT, request,
                context.getRequestInfo().getExecutorType());

        logger.info("PAY. internal.pay response code = {}, {} ", response.getBody().getCode(),
                response.getBody().getDescription());

        return convert(response.getBody(), (Class<T>) pgPayment.getClass(), pgPayment, payment.getExternalStoreProductId(),
                context.getRequestInfo().getExecutorType());

    }

    public <T extends PGPayment> Payment<T> get(Context<T> context) {

        String url = pantherProperties.getApiUrl() + "/" + context.getPayment().getPaymentId();
        logger.debug("GET. to {}, token={}", url, context.getRequestInfo().getToken());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + context.getRequestInfo().getToken());    // FIXME TOKEN
        headers.add("Cookie", "JSESSIONID=");

        Payment<T> payment = context.getPayment();
        T pgPayment = payment.getPgPayment();

        HttpEntity request = new HttpEntity<>(headers);

        HttpEntity<Result> response = exchange(url, HttpMethod.GET, request,
                context.getRequestInfo().getExecutorType());

        return convert(response.getBody(), (Class<T>) pgPayment.getClass(), pgPayment, payment.getExternalStoreProductId(),
                context.getRequestInfo().getExecutorType());
    }

    /**
     * Execute the HTTP method to the given URI template, writing the given request entity to the request, and
     * returns the response as {@link ResponseEntity}.
     * It will retry {@linkplain #RETRY_COUNT} times if failed and then will throw
     * {@linkplain InternalPaymentException} if failed.
     *
     * @return
     * @throws {@linkplain InternalPaymentException}
     */
    public HttpEntity<Result> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Executor.Type type) {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpEntity<Result> response = null;
        for (int i = 1; i <= RETRY_COUNT + 1; i++) {
            try {
                response = restTemplate.exchange(url, method, requestEntity, Result.class);
                break;
            } catch (Throwable e) {
                if (TRANSIENT_EXECPTIONS.contains(e.getClass()) && i <= RETRY_COUNT) {
                    logger.info("Failed to exchange: " + e.getMessage());
                    logger.info("Retrying ...... [{}]", i);
                    try {
                        Thread.sleep(500 * i);
                    } catch (Exception ea) {
                        logger.warn("Failed", ea);
                    }
                } else {
                    if (i == RETRY_COUNT + 1) {
                        logger.warn("All retry failed : " + e.getMessage());
                    }
                    throw new InternalPaymentException(type, e);
                }
            }
        }
        if (response.getBody() == null) {
            ResponseEntity<Result> re = (ResponseEntity<Result>) response;
            logger.warn("Response is null. Status = {}", re.getStatusCode());
            throw new InternalPaymentException(type, "InternalPaymentService Error. Status:" + re.getStatusCode());
        }
        return response;
    }


    /**
     * Returns the {@linkplain Payment} from given {@code result}.
     *
     * @return
     * @throws {@linkplain InternalPaymentException}
     */
    public <T extends PGPayment> Payment<T> convert(Result result, Class<T> pgPaymentClass, T pgPayment,
                                                    String externalStoreProductId, Executor.Type type) {
        if (result == null) {
            throw new InternalPaymentException(type, "Internal. result is null");
        }

        if (!ResponseCode.INTERNAL_OK.getCode().equals(String.valueOf(result.getCode()))) {
            throw new InternalPaymentException(type, "InternalFailed: " + result.getCode() + ":" +
                    result.getDescription());
        }

        String jsonData = JsonUtil.toJson(result.getData());
        if (jsonData == null) {
            throw new InternalPaymentException(type,
                    "Internal. result.data is null : " + String.valueOf(result.getCode()));
        }

        Payment<T> responsePayment = null;
        try {
            responsePayment = JsonUtil.fromJsonToPayment(jsonData, pgPaymentClass);
            logger.info("RESPONSE. paymentId={}, state={}, userId={}, coinProductId={}, coinProductName={}, " +
                            "amount={}", responsePayment.getPaymentId(), responsePayment.getState(),
                    responsePayment.getUserId(), responsePayment.getCoinProductId(),
                    responsePayment.getCoinProductName(), responsePayment.getAmount());
            logger.debug("responsedPayment = \n{}", JsonUtil.toJson(responsePayment));
            responsePayment.setPgPayment(pgPayment);
            responsePayment.setExternalStoreProductId(externalStoreProductId);
        } catch (Exception e) {
            throw new InternalPaymentException(type, e);
        }

        if (responsePayment == null) {
            throw new InternalPaymentException(type, String.valueOf(result.getCode()));
        }

        return responsePayment;

    }

}
