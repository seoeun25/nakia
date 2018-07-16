package com.lezhin.panther.internal;

import com.lezhin.panther.Context;
import com.lezhin.panther.HttpClientService;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.HttpClientException;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.util.JsonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * @author taemmy
 * @since 2018. 7. 2.
 */
@Service
public class InternalWalletService {

    private static final Logger logger = LoggerFactory.getLogger(InternalWalletService.class);

    private PantherProperties pantherProperties;
    private HttpClientService httpClientService;
    private RestTemplate restTemplate;

    public InternalWalletService(final PantherProperties pantherProperties,
                                 final HttpClientService httpClientService,
                                 final RestTemplate restTemplate) {
        this.pantherProperties = pantherProperties;
        this.httpClientService = httpClientService;
        this.restTemplate = restTemplate;
    }

    public Result sendCoinReward(final Wallet wallet, final Context context) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Authorization", "Bearer " + pantherProperties.getCmsToken());
        HttpEntity<Wallet> request = new HttpEntity<>(wallet, headers);

        logger.info("sendCoinReward. {}", wallet.toString());

        Executor.Type type = Optional.ofNullable(context).map(Context::getType).orElse(Executor.Type.UNKNOWN);
        HttpEntity<Result> response;
        try {
            response = httpClientService.postForEntity(pantherProperties.getWalletUrl(),
                    request, Result.class, Executor.Type.UNKNOWN);
        } catch (Exception e) {
            throw new HttpClientException(type, "sendCoinReward fail. " + e.getMessage(), e);
        }

        if(response == null || response.getBody() == null ){
            throw new HttpClientException(type, "sendCoinReward response can not be null");
        }

        logger.info("sendCoinReward. code: {}, description: {}", response.getBody().getCode(), response.getBody().getDescription());
        return response.getBody();
    }

    public void sendPresentPush(final Long userId, final String customUri, final String title, final String msg) {
        PresentPush reqPush = new PresentPush(userId, customUri, title, msg);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<PresentPush> request = new HttpEntity<>(reqPush, headers);

        String response = restTemplate.postForObject(pantherProperties.getPushUrl(), request, String.class);
        logger.info("sendPresentPush. request: {}, request: {}", request, response);
    }
}
