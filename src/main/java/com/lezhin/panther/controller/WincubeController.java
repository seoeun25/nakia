package com.lezhin.panther.controller;

import com.lezhin.constant.PGCompany;
import com.lezhin.panther.exception.UnauthorizedException;
import com.lezhin.panther.pg.wincube.WincubeService;
import com.lezhin.panther.model.CouponInfo;
import com.lezhin.panther.model.CouponProductInfo;
import com.lezhin.panther.util.ApiKeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author taemmy
 * @since 2018. 7. 30.
 * e쿠폰(기프팅) 레진 쿠폰 발급용 api
 */
@RestController
@RequestMapping("/wincube/v1")
public class WincubeController {
    // TODO 쿠폰업체가 늘어날 경우, stark 로 이전 필요!!
    private static final Logger logger = LoggerFactory.getLogger(WincubeController.class);

    private WincubeService wincubeService;
    private ApiKeyManager apiKeyManager;

    public WincubeController(final WincubeService wincubeService, final ApiKeyManager apiKeyManager) {
        this.wincubeService = wincubeService;
        this.apiKeyManager = apiKeyManager;
    }

    @GetMapping("/coupon_products")
    public List<CouponProductInfo> getProducts() {
        List<CouponProductInfo> products = wincubeService.getProducts(PGCompany.wincube);
        return products;
    }

    @PostMapping("/coupons")
    public CouponInfo issueCoupon(
            @RequestHeader(value = "X-LZ-Client-Key", defaultValue = "") String clientKey,
            @RequestHeader(value = "X-LZ-API-Key", defaultValue = "") String apiKey,
            @RequestBody CouponInfo reqCouponInfo) {
        checkAuthorization(clientKey, apiKey);

        logger.info("issue coupon: product-id={}, request-id={}", reqCouponInfo.getProductId(), reqCouponInfo.getRequestId());
        CouponInfo coupon = wincubeService.issue(PGCompany.wincube, reqCouponInfo);

        return coupon;
    }

    @GetMapping("/coupons/{id}")
    public CouponInfo getCoupon(@PathVariable String id,
                                @RequestHeader(value = "X-LZ-Client-Key", defaultValue = "") String clientKey,
                                @RequestHeader(value = "X-LZ-API-Key", defaultValue = "") String apiKey,
                                @RequestParam(name = "requestId") String requestId,
                                @RequestParam(name = "productId", required = false) String productId) {
        checkAuthorization(clientKey, apiKey);

        CouponInfo reqCouponInfo = new CouponInfo();
        reqCouponInfo.setRequestId(requestId);
        reqCouponInfo.setProductId(Long.parseLong(productId));
        reqCouponInfo.setCouponId(id);
        CouponInfo coupon = wincubeService.get(PGCompany.wincube, reqCouponInfo);

        return coupon;
    }

    @DeleteMapping("/coupons/{id}")
    public CouponInfo discardCoupon(@PathVariable String id,
                                    @RequestHeader(value = "X-LZ-Client-Key", defaultValue = "") String clientKey,
                                    @RequestHeader(value = "X-LZ-API-Key", defaultValue = "") String apiKey,
                                    @RequestBody CouponInfo reqCouponInfo) {
        checkAuthorization(clientKey, apiKey);

        logger.info("discard coupon: coupon-id={}, request-id={}", id, reqCouponInfo.getRequestId());
        reqCouponInfo.setCouponId(id);
        CouponInfo coupon = wincubeService.discard(PGCompany.wincube, reqCouponInfo);

        return coupon;
    }

    private void checkAuthorization(final String clientKey, final String apiKey) {
        if (!apiKeyManager.validate(clientKey, apiKey)) {
            throw new UnauthorizedException(PGCompany.wincube, "Unauthorized");
        }
    }
}
