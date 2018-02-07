package com.lezhin.panther.util;

import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.Payment;
import com.lezhin.constant.LezhinCurrency;

import com.google.common.collect.ImmutableMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * PG 에서 사용하던 util 성 method들. 임시로 여기. FIXME
 *
 * @author seoeun
 * @since 2017.11.06
 */
@Deprecated
public class PaymentUtil extends Payment {

    //-------------
    public String jsonSerialize(String encoding) {
        if (encoding == null) {
            encoding = "UTF-8";
        }
        // FIXME fasterxml
        return "";
    }

    public Map<String, Object> getMetaData() {
        return new HashMap<>();
        //return array_merge(Optional.ofNullable(meta).orElse(new HashMap<>()), buildMetaData());
    }

    public Map<String, Object> getReceiptData() {
        Map<String, Object> map1 = ImmutableMap.of(
                "_lz_coin_product_id", coinProductId,
                "_lz_currency", currency,
                "_lz_point_amount", pointAmount
        );
        return array_merge(map1, buildReceiptData());
    }

    public Map<String, Object> getReserveViewData(String encodeing) {
        Map<String, Object> map1 = ImmutableMap.of(
                "_lz_user_id", userId,
                "_lz_payment_id", paymentId,
                "_lz_coin_product_id", coinProductId,
                "_lz_currency", currency,
                "_lz_point_amount", pointAmount);
        return array_merge(map1, buildReserveViewData(encodeing));
    }

    public Map<String, Object> getVerifyViewData(String encoding) {
        Map<String, Object> map1 = ImmutableMap.of(
                "_lz_user_id", userId,
                "_lz_payment_id", paymentId,
                "_lz_coin_product_id", coinProductId,
                "_lz_currency", currency,
                "_lz_point_amount", pointAmount);
        return array_merge(map1, buildVerifyViewData(encoding));

    }

    public Map<String, Object> getPaymentViewData(String encoding) {
        Map<String, Object> map1 = ImmutableMap.of(
                "_lz_user_id", userId,
                "_lz_payment_id", paymentId,
                "_lz_coin_product_id", coinProductId,
                "_lz_currency", currency,
                "_lz_point_amount", pointAmount);
        return array_merge(map1, buildPaymentViewData(encoding));
    }

    protected Map<String, Object> buildMetaData() {
        return null;
    }

    protected Map<String, Object> buildReceiptData() {
        return null;
    }

    protected Map<String, Object> buildReserveViewData(String encoding) {
        return null;
    }

    protected Map<String, Object> buildVerifyViewData(String encoding) {
        return null;
    }

    protected Map<String, Object> buildPaymentViewData(String encoding) {
        return null;
    }

    protected String[] onParse(HttpServletRequest request) {
        return new String[0];
    }

    public String getPGId() {
        return "unknown";
    }

    public String getDynamicAmount() {
        return null;
    }

    //TODO 이렇게 처리하는게 맞을까?
    public void parse(HttpServletRequest request) {
        //TODO parse for request params and body.

        // FIXME null check. param check.
        try {
            // toss sub class. (PG detail info.)
            this.pgId = getPGId();
            this.userId = Long.valueOf(request.getParameter("_lz_user_id"));
            this.paymentId = Long.valueOf(request.getParameter("_lz_payment_id"));
            this.coinProductId = Long.valueOf(request.getParameter("_lz_coin_product_id"));
            this.currency = LezhinCurrency.valueOf(request.getParameter("_lz_currency"));
            this.pointAmount = Integer.valueOf(request.getParameter("_lz_point_amount"));
            onParse(request);
        } catch (Exception e) {
            throw new ParameterException(Executor.Type.UNKNOWN, e);
        }

        onParse(request);
    }

    public void parseFromJson(String $json) {
        // FIXME convert json to Pay
//        paymentId = $json -> paymentId;
//        approvalId = $json -> approvalId;
//        userId = $json -> userId;
//        userName = $json -> userName;
//        userEmail = $json -> userEmail;
//        coinProductId = $json -> coinProductId;
//        coinProductName = $json -> coinProductName;
//        coin = $json -> coin;
//        amount = $json -> amount;
//        pointAmount = $json -> pointAmount;
//        currency = $json -> currency;
//        paymentType = $json -> paymentType;
//        store = $json -> store;
//        storeVersion = $json -> storeVersion;
//        locale = $json -> locale;
//        state = $json -> state;
//        createdAt = $json -> createdAt;
//        if (isset($json -> cohortData) && (is_object($json -> cohortData) || is_array($json -> cohortData))) {
//            cohort =[
//            "year" = >$json -> cohortData -> joinYear,
//                    "month", $json -> cohortData -> joinMonth,
//                    "week", $json -> cohortData -> joinWeek,
//                    "day", $json -> cohortData -> joinDate
//            );
//        }
//
//        // toss sub class. (PG detail info.)
//        pgId = getPGId();
    }

    public String getPaymentUrl() {

        // FIXME returnTo 가 null 이면 paramString 만들 필요 없음. empty string 처리로 대체. URL 다시 만들어야 함.
        // pay 관련은 모두 https
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("https").host(lezhinPgUrl).path(pgId + "/pay")
                .queryParam("isMobile", Optional.ofNullable(isMobile).orElse(Boolean.TRUE))
                .queryParam("isApp", Optional.ofNullable(isApp).orElse(Boolean.TRUE))
                .queryParam("locale", locale)
                .queryParam("returnTo", Optional.ofNullable(returnTo).orElse(""))
                .build()
                .encode();

        return uriComponents.toUriString();

//        String $url =  "$this->lezhinPgUrl/$this->pgId/pay?isMobile=".var_export($this->isMobile, true)."&isApp="
//            .var_export($this->isApp, true)."&locale=$this->locale";
//        if ($this->returnTo) {
//            $url .= "&returnTo=".urlencode($this->returnTo);
//        }
//        return $url;
    }

    public String getAuthenticationUrl() {
        // FIXME returnTo 가 null 이면 paramString 만들 필요 없음. empty string 처리로 대체
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http").host(lezhinPgUrl).path(pgId + "/authentication")
                .queryParam("isMobile", Optional.ofNullable(isMobile).orElse(Boolean.TRUE))
                .queryParam("isApp", Optional.ofNullable(isApp).orElse(Boolean.TRUE))
                .queryParam("locale", locale)
                .queryParam("returnTo", Optional.ofNullable(returnTo).orElse(""))
                .build()
                .encode();

        return uriComponents.toUriString();
    }

    public String getFailUrl() {
        // FIXME returnTo 가 null 이면 paramString 만들 필요 없음. empty string 처리로 대체
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http").host(lezhinPgUrl).path(pgId + "/fail")
                .queryParam("isMobile", Optional.ofNullable(isMobile).orElse(Boolean.TRUE))
                .queryParam("isApp", Optional.ofNullable(isApp).orElse(Boolean.TRUE))
                .queryParam("locale", locale)
                .queryParam("returnTo", Optional.ofNullable(returnTo).orElse(""))
                .build()
                .encode();

        return uriComponents.toUriString();
    }

    public String getResultUrl(String host) {

        Long $paymentId = Optional.ofNullable(paymentId).orElse(Long.valueOf(0L));
        Long $userId = Optional.ofNullable(userId).orElse(Long.valueOf(0L));
        Boolean $isMobile = Optional.ofNullable(isMobile).orElse(Boolean.TRUE);
        Boolean $isApp = Optional.ofNullable(isApp).orElse(Boolean.TRUE);
        String $returnTo = Optional.ofNullable(returnTo).orElse("");
        //String $lang = Optional.ofNullable(locale).orElse(LezhinLocale.KO_KR).getLanguageCode().toLowerCase();
        String $lang = "ko";
        // FIXME
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("https").host(host).path($lang + "/pay/" + paymentId + "/result")
                .queryParam("__u", userId)
                .queryParam("isMobile", Optional.ofNullable(isMobile).orElse(Boolean.TRUE))
                .queryParam("isApp", Optional.ofNullable(isApp).orElse(Boolean.TRUE))
                .queryParam("locale", locale)
                .queryParam("returnTo", Optional.ofNullable(returnTo).orElse(""))
                .build()
                .encode();

        return uriComponents.toUriString();

//        $url = "$host/$lang/pay/$paymentId/result?__u=$userId&isMobile=$isMobile&isApp=$isApp";
//        if (!empty($returnTo)) {
//            $encodeUrl = urlencode($returnTo);
//            $url .= "&returnTo=$encodeUrl";
//        }
//        return $url;
    }

    //-------------

    public String ulrDecode(String url) {
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            throw new ParameterException(Executor.Type.UNKNOWN, String.format("Failed to urlDecode [%s]", url));
        }
    }

    public Map<String, Object> array_merge(@NotNull final Map<String, Object> args1, final Map<String, Object> args2) {
        Map<String, Object> merged = new HashMap<>(args1);
        // 나중에 추가된 것
        Optional.ofNullable(args2).orElse(new HashMap<>()).forEach((k, v) -> merged.merge(k, v, (o1, o2) -> o2));
        return merged;
    }

}
