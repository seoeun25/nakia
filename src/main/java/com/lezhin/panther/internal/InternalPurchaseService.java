package com.lezhin.panther.internal;

import com.lezhin.panther.HttpClientService;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.HttpClientException;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.util.JsonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * @author seoeun
 * @since 2018.03.15
 */
@Service
public class InternalPurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(InternalPurchaseService.class);


    private PantherProperties pantherProperties;
    private HttpClientService httpClientService;

    public InternalPurchaseService(final HttpClientService httpClientService, final PantherProperties pantherProperties) {
        this.httpClientService = httpClientService;
        this.pantherProperties = pantherProperties;
    }

    public PurchaseDetail getPurchaseDetail(Long userId, Long paymentId) {

        String url = String.format(pantherProperties.getCmsUrl() + "/v2/internal/purchases/%s/payments/%s/detail",
                userId.longValue(), paymentId.longValue());
        logger.info("getPurchaseDetail. to {}, token={}", url, pantherProperties.getCmsToken());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + pantherProperties.getCmsToken());

        HttpEntity request = new HttpEntity<>(headers);

        HttpEntity<Result> response = exchange(url, HttpMethod.POST, request,
                Executor.Type.UNKNOWN);

        return convert(response.getBody(), Executor.Type.UNKNOWN);

    }

    public HttpEntity<Result> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Executor.Type type) {
        HttpEntity<Result> response = null;
        try {
            response = httpClientService.exchange(url, method, requestEntity, type, Result.class);
        } catch (Exception e) {
            throw new HttpClientException(type, "HttpClient.Error:" + e.getMessage(), e);
        }
        return response;
    }

    public PurchaseDetail convert(Result result, Executor.Type type) {
        if (result == null) {
            throw new HttpClientException(type, "InternalPurchase. result is null");
        }

        if (!ResponseInfo.ResponseCode.INTERNAL_OK.getCode().equals(String.valueOf(result.getCode()))) {
            throw new HttpClientException(type, "InternalPurchase Failed: " + result.getCode() + ":" +
                    result.getDescription());
        }

        String jsonData = JsonUtil.toJson(result.getData());
        logger.debug("jsonData = \n" + jsonData);
        if (jsonData == null) {
            throw new HttpClientException(type,
                    "InternalPurchase. result.data is null : " + String.valueOf(result.getCode()));
        }

        PurchaseDetail responsePurchase = null;
        try {
            responsePurchase = JsonUtil.fromJson(jsonData, PurchaseDetail.class);
            logger.info("RESPONSE. purchase.charge: {}, purchaseId={}, approvedId={}",
                    responsePurchase.getCharge().getTitle(), responsePurchase.getCharge().getId(),
                    responsePurchase.getCharge().getIdApproval());
        } catch (Exception e) {
            throw new PantherException(type, "Failed to get PurchaseDetail", e);
        }

        if (responsePurchase == null) {
            throw new PantherException(type, "Failed to get PurchaseDetail. response is null. "
                    + String.valueOf(result.getCode()));
        }

        return responsePurchase;

    }


}
