package com.lezhin.panther;

import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.HttpClientException;
import com.lezhin.panther.exception.InternalPaymentException;
import com.lezhin.panther.executor.Executor;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

/**
 * @author seoeun
 * @since 2018.03.15
 */
@Service
public class HttpClientService {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientService.class);
    private static final List<Class<? extends Exception>> TRANSIENT_EXECPTIONS = ImmutableList.of(
            IOException.class,
            ResourceAccessException.class,
            HttpServerErrorException.class,
            HttpClientErrorException.class);
    private static final int RETRY_COUNT = 3;
    private PantherProperties pantherProperties;
    private ClientHttpRequestFactory clientHttpRequestFactory;

    public HttpClientService(final ClientHttpRequestFactory clientHttpRequestFactory,
                             final PantherProperties pantherProperties) {
        this.clientHttpRequestFactory = clientHttpRequestFactory;
        this.pantherProperties = pantherProperties;
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
    public <T> HttpEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Executor.Type type,
                                    Class<T> responseType) {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpEntity<T> response = null;
        for (int i = 1; i <= RETRY_COUNT + 1; i++) {
            try {
                response = restTemplate.exchange(url, method, requestEntity, responseType);
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
                    throw new HttpClientException(type, e);
                }
            }
        }
        if (response.getBody() == null) {
            ResponseEntity<T> re = (ResponseEntity<T>) response;
            logger.warn("Response is null. Status = {}", re.getStatusCode());
            throw new HttpClientException(type, "HttpClientService.exchange.Status:" + re.getStatusCode());
        }
        return response;
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
    public <T> HttpEntity<T> postForEntity(String url, HttpEntity<?> requestEntity,
                                           Class<T> responseType, Executor.Type type ) {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpEntity<T> response = null;
        for (int i = 1; i <= RETRY_COUNT + 1; i++) {
            try {
                response = restTemplate.postForEntity(url, requestEntity, responseType);
                break;
            } catch (Throwable e) {
                if (TRANSIENT_EXECPTIONS.contains(e.getClass()) && i <= RETRY_COUNT) {
                    logger.info("Failed to postForEntity: " + e.getMessage());
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
                    throw new HttpClientException(type, e);
                }
            }
        }
        if (response.getBody() == null) {
            ResponseEntity<T> re = (ResponseEntity<T>) response;
            logger.warn("Response is null. Status = {}", re.getStatusCode());
            throw new HttpClientException(type, "HttpClientService.postForEntity.Status:" + re.getStatusCode());
        }
        return response;
    }
}
