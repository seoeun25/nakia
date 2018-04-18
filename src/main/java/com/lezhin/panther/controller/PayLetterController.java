package com.lezhin.panther.controller;

import com.lezhin.panther.exception.ExceedException;
import com.lezhin.panther.exception.ParameterException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.internal.Result;
import com.lezhin.panther.payletter.PayLetterLog;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;
import com.lezhin.panther.payletter.PayLetterService;
import com.lezhin.panther.util.ApiKeyManager;
import com.lezhin.panther.util.DateUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;

/**
 * Payletter 의 결제 정보 데이터를 제공하는 REST API.
 *
 * @author benjamin
 * @since 2017.12.19
 */
@RestController
@RequestMapping("/payletter/v1")
public class PayLetterController {

    private static final Logger logger = LoggerFactory.getLogger(PayLetterController.class);
    private PayLetterService payLetterService;
    private ApiKeyManager apiKeyManager;

    public PayLetterController(final PayLetterService payLetterService, final ApiKeyManager apiKeyManager) {
        this.payLetterService = payLetterService;
        this.apiKeyManager = apiKeyManager;
    }

    @RequestMapping(value = "/logs", method = RequestMethod.GET)
    @ResponseBody
    public <T> ResponseEntity<T> onLogs(HttpServletRequest request, HttpServletResponse response,
                                             @RequestParam String fromYMD,
                                             @RequestParam String toYMD,
                                             @RequestParam(required = false, defaultValue = "") String locale) {
        logger.info("payletter.logs.params = {}", request.getQueryString());

        if (!apiKeyManager.validate(request.getHeader("clientName"), request.getHeader("apiKey"))) {
            return new ResponseEntity("Fraud", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Instant startDateTime;
        Instant endDateTime;
        try {
            startDateTime = DateUtil.toInstantFromDate(fromYMD, "yyyyMMdd", DateUtil.ASIA_SEOUL_ZONE);
            endDateTime = DateUtil.toInstantFromDate(toYMD, "yyyyMMdd", DateUtil.ASIA_SEOUL_ZONE).plusSeconds(86400);
        } catch (Exception e) {
            throw new ParameterException("fromYMD and toYMD should be 'yyyyMMdd'");
        }

        Long period = endDateTime.getEpochSecond() - startDateTime.getEpochSecond();
        Long days = period > 0 ? period / 86400 : 0;
        if (days > 3) {
            throw new ParameterException("Search range can not be over 3 days");
        }

        List<PayLetterLog> logs = payLetterService.getLogs(startDateTime, endDateTime, locale);
        Result<List<PayLetterLog>> result = new Result<>();
        result.setCode(Integer.parseInt(ResponseCode.LEZHIN_OK.getCode()));
        result.setData(logs);

        return new ResponseEntity(result, HttpStatus.OK);
    }

}
