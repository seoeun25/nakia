package com.lezhin.panther.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lezhin.constant.PGCompany;
import com.lezhin.panther.HttpClientService;
import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.exception.HttpClientException;
import com.lezhin.panther.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author taemmy
 * @since 2018. 7. 30.
 */
@Service
public class InternalCouponService {
    public static final Logger logger = LoggerFactory.getLogger(InternalCouponService.class);

    private PantherProperties pantherProperties;
    private HttpClientService httpClientService;

    private String _ISSUE_COUPON = "/v2/coupon_groups/%s/coupons";
    private String _GET_COUPON = "/v2/coupons/%s";

    public InternalCouponService(final PantherProperties pantherProperties, final HttpClientService httpClientService) {
        this.pantherProperties = pantherProperties;
        this.httpClientService = httpClientService;
    }

    public LzCoupon get(final PGCompany pg, final String couponId) {
        String url = pantherProperties.getCmsUrl() + String.format(_GET_COUPON, couponId);
        logger.info("get: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Authorization", "Bearer " + pantherProperties.getCmsToken());
        HttpEntity<Result> request = new HttpEntity<>(headers);

        ResponseEntity<Result> response = httpClientService.exchange(pg, url, HttpMethod.GET, request, Result.class);
        if(response.getBody() == null) {
            throw new HttpClientException(pg, String.format("%s: response is empty", response.getStatusCode()));
        }

        String jsonData = JsonUtil.toJson(response.getBody().getData());
        logger.info("get: status={}, code={}, body={}", response.getStatusCode(), response.getBody().getCode(), jsonData);

        if(response.getBody().getCode() != 0) {
            throw new HttpClientException(pg, String.format("[%s] %s", response.getBody().getCode(), response.getBody().getDescription()));
        }

        return JsonUtil.fromJson(jsonData, LzCoupon.class);
    }

    public LzCoupon issue(final PGCompany pg, final Long groupId, final LzCoupon lzCoupon) {
        String url = pantherProperties.getCmsUrl() + String.format(_ISSUE_COUPON, groupId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Authorization", "Bearer " + pantherProperties.getCmsToken());
        HttpEntity<LzCoupon> request = new HttpEntity<>(lzCoupon, headers);

        ResponseEntity<Result> response = httpClientService.exchange(pg, url, HttpMethod.POST, request, Result.class);
        if(response.getBody() == null) {
            throw new HttpClientException(pg, String.format("%s: response is empty", response.getStatusCode()));
        }

        String jsonData = JsonUtil.toJson(response.getBody().getData());
        logger.info("issue: status={}, code={}, body={}", response.getStatusCode(), response.getBody().getCode(), jsonData);

        if(response.getBody().getCode() != 0) {
            throw new HttpClientException(pg, String.format("[%s] %s", response.getBody().getCode(), response.getBody().getDescription()));
        }

        return JsonUtil.fromJson(jsonData, LzCoupon.class);
    }

    public LzCoupon discard(final PGCompany pg, final String couponId) {
        String url = pantherProperties.getCmsUrl() + String.format(_GET_COUPON, couponId);
        logger.info("discard: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Authorization", "Bearer " + pantherProperties.getCmsToken());
        HttpEntity<Result> request = new HttpEntity<>(headers);

        ResponseEntity<Result> response = httpClientService.exchange(pg, url, HttpMethod.DELETE, request, Result.class);
        if(response.getBody() == null) {
            throw new HttpClientException(pg, String.format("%s: response is empty", response.getStatusCode()));
        }

        String jsonData = JsonUtil.toJson(response.getBody().getData());
        logger.info("discard: status={}, code={}, body={}", response.getStatusCode(), response.getBody().getCode(), jsonData);

        if(response.getBody().getCode() != 0) {
            throw new HttpClientException(pg, String.format("[%s] %s", response.getBody().getCode(), response.getBody().getDescription()));
        }

        List<LzCoupon> lzCoupons = JsonUtil.fromJson(jsonData, new TypeReference<List<LzCoupon>>(){});
        // 1개씩 처리할 것이므로 무조건 0 번째를 가져온다
        if(lzCoupons.size() == 0) {
            throw new HttpClientException(pg, "response data is empty");
        }

        return lzCoupons.get(0);
    }
}
