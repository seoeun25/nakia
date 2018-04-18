package com.lezhin.panther.internal;

import com.lezhin.panther.Context;
import com.lezhin.panther.HttpClientService;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import javax.management.openmbean.OpenDataException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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
            HttpServerErrorException.class,
            HttpClientErrorException.class);
    private static final int RETRY_COUNT = 3;
    private PantherProperties pantherProperties;
    private ClientHttpRequestFactory clientHttpRequestFactory;
    private HttpClientService httpClientService;

    public InternalPaymentService(final ClientHttpRequestFactory clientHttpRequestFactory,
                                  final PantherProperties pantherProperties,
                                  final HttpClientService httpClientService) {
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.pantherProperties = pantherProperties;
        this.httpClientService = httpClientService;
    }


    public <T extends PGPayment> Payment<T> reserve(final Context<T> context) {

        String url = pantherProperties.getApiUrl() + "/reserve";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + context.getRequestInfo().getToken());
        headers.add("Cookie", "JSESSIONID=");

        Payment payment = context.getPayment();
        T pgPayment = (T) payment.getPgPayment();
        payment.setPgPayment(null); // internal에 없는 모델 제외. TODO annotation
        HttpEntity<Payment> request = new HttpEntity<>(payment, headers);
        logger.info("{} internal.reserve. \nto {}. token={}, request={}", context.print(), url, context
                        .getRequestInfo().getToken(),
                JsonUtil.toJson(payment));

        HttpEntity<Result> response = exchange(url, HttpMethod.POST, request, context);
        logger.info("{} internal.reserve. {}={} ", context.print(), response.getBody().getCode(),
                response.getBody().getDescription());

        Payment<T> reserved = convert(context, response.getBody(), (Class<T>) pgPayment.getClass(), pgPayment,
                payment.getExternalStoreProductId());

        logger.info("{} reserved. u={}, p={}, state={}, coinProductId={}, " +
                        "coinProductName={}, amount={}",
                context.print(), reserved.getUserId(), reserved.getPaymentId(),
                reserved.getState(), reserved.getCoinProductId(),
                reserved.getCoinProductName(), reserved.getAmount());
        return reserved;

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
        logger.info("{} internal.authenticate \nto {}. token={}, request={}", context.print(), url,
                context.getRequestInfo().getToken(),
                JsonUtil.toJson(payment));

        HttpEntity<Result> response = exchange(url, HttpMethod.PUT, request, context);
        logger.info("{} internal.authenticate. {}={} ", context.print(),
                response.getBody().getCode(),
                response.getBody().getDescription());

        return convert(context, response.getBody(), (Class<T>) pgPayment.getClass(), pgPayment,
                payment.getExternalStoreProductId());
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
        logger.info("{} internal.pay \nto {}. token={}, request={}", context.print(), url,
                context.getRequestInfo().getToken(),
                JsonUtil.toJson(payment));

        HttpEntity<Result> response = exchange(url, HttpMethod.PUT, request, context);

        logger.info("{} internal.pay {}={} ", context.print(),
                response.getBody().getCode(),
                response.getBody().getDescription());

        return convert(context, response.getBody(), (Class<T>) pgPayment.getClass(), pgPayment,
                payment.getExternalStoreProductId());

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

        HttpEntity<Result> response = exchange(url, HttpMethod.GET, request, context);

        return convert(context, response.getBody(), (Class<T>) pgPayment.getClass(), pgPayment,
                payment.getExternalStoreProductId());
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
    public HttpEntity<Result> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Context context) {
        HttpEntity<Result> response = null;
        Executor.Type type = Optional.ofNullable(context).map(c -> c.getType()).orElse(Executor.Type.UNKNOWN);
        try {
            response = httpClientService.exchange(url, method, requestEntity, type, Result.class);
        } catch (Exception e) {
            throw new InternalPaymentException(context, "InternalPaymentService.Error:" + e.getMessage(), e);
        }
        return response;

    }


    /**
     * Returns the {@linkplain Payment} from given {@code result}.
     *
     * @return
     * @throws {@linkplain InternalPaymentException}
     */
    public <T extends PGPayment> Payment<T> convert(final Context context,
                                                    final Result result, final Class<T> pgPaymentClass, final T pgPayment,
                                                    final String externalStoreProductId) {
        if (result == null) {
            throw new InternalPaymentException(context, "Internal. result is null");
        }

        if (!ResponseCode.INTERNAL_OK.getCode().equals(String.valueOf(result.getCode()))) {
            throw new InternalPaymentException(context, "InternalFailed: " + result.getCode() + ":" +
                    result.getDescription());
        }

        String jsonData = JsonUtil.toJson(result.getData());
        if (jsonData == null) {
            throw new InternalPaymentException(context,
                    "Internal. result.data is null : " + String.valueOf(result.getCode()));
        }

        Payment<T> responsePayment = null;
        try {
            responsePayment = JsonUtil.fromJsonToPayment(jsonData, pgPaymentClass);
            logger.debug("{} internal.response. u={}, p={}, state={}, coinProductId={}, " +
                            "coinProductName={}, amount={}",
                    context.print(), responsePayment.getUserId(), responsePayment.getPaymentId(),
                    responsePayment.getState(), responsePayment.getCoinProductId(),
                    responsePayment.getCoinProductName(), responsePayment.getAmount());
            logger.debug("{} responsedPayment = \n{}", context.print(), JsonUtil.toJson(responsePayment));
            responsePayment.setPgPayment(pgPayment);
            responsePayment.setExternalStoreProductId(externalStoreProductId);
        } catch (Exception e) {
            throw new InternalPaymentException(context, e);
        }

        if (responsePayment == null) {
            throw new InternalPaymentException(context, String.valueOf(result.getCode()));
        }

        return responsePayment;

    }

}
