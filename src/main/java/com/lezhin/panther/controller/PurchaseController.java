package com.lezhin.panther.controller;

import com.lezhin.panther.internal.PurchaseDetail;
import com.lezhin.panther.internal.InternalPurchaseService;
import com.lezhin.panther.util.ApiKeyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author seoeun
 * @since 2018.03.15
 */
@RestController
@RequestMapping("/purchase/v1")
public class PurchaseController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseController.class);

    private InternalPurchaseService purchaseService;
    private ApiKeyManager apiKeyManager;

    public PurchaseController(final InternalPurchaseService payment2Service, final ApiKeyManager apiKeyManager) {
        this.purchaseService = payment2Service;
        this.apiKeyManager = apiKeyManager;
    }

    @RequestMapping(value = "/users/{userId}/payments/{paymentId}", method = RequestMethod.GET)
    @ResponseBody
    public <T> ResponseEntity<T> getPurchaseDetail(HttpServletRequest request, HttpServletResponse response,
                                            @PathVariable Long userId,
                                            @PathVariable Long paymentId) {

        logger.info("request. getPurchaseDetail. paymentId={}, apiKey={}", paymentId, request.getHeader("apiKey"));

        if (StringUtils.isEmpty(request.getHeader("apiKey"))) {
            return new ResponseEntity("Fraud", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!apiKeyManager.validate(request.getHeader("clientName"), request.getHeader("apiKey"))) {
            return new ResponseEntity("Fraud", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // FIXME clientName, apiKey를 기준으로 paymentType을 비교하여 ACL 적용 필요
        PurchaseDetail purchaseDetail = purchaseService.getPurchaseDetail(userId, paymentId);

        return new ResponseEntity(purchaseDetail, HttpStatus.OK);
    }

}
