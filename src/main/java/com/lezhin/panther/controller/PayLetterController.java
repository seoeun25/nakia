package com.lezhin.panther.controller;

import com.lezhin.panther.ErrorCode;
import com.lezhin.panther.PayLetterService;
import com.lezhin.panther.exception.ExceedException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.internalpayment.Result;
import com.lezhin.panther.model.PayLetterLog;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.util.DateUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;
import java.time.Instant;
import java.util.List;

/**
 * TODO
 *
 * @author benjamin
 * @since 2017.12.19
 */
@RestController
@RequestMapping("/payletter/v1")
public class PayLetterController {

    private static final Logger logger = LoggerFactory.getLogger(PayLetterController.class);
    private PayLetterService payLetterService;
    public PayLetterController(PayLetterService payLetterService) {
        this.payLetterService = payLetterService;
    }

    @RequestMapping(value = "/logs", method = RequestMethod.GET)
    @ResponseBody
    public Result<List<PayLetterLog>> onLogs(HttpServletRequest request, HttpServletResponse response,
                                             @RequestParam(required = true) String fromYMD,
                                             @RequestParam(required = true) String toYMD,
                                             @RequestParam(required = false, defaultValue = "") String locale
    ) {
        logger.info("payletter.logs.params = {}, locale = {}", request.getQueryString(), locale);
        Instant startDateTime = DateUtil.toInstantFromDate(DateUtil.toDatePattern(fromYMD), "yyyy-MM-dd", DateUtil.ASIA_SEOUL_ZONE);
        Instant endDateTime = DateUtil.toInstantFromDate(DateUtil.toDatePattern(toYMD), "yyyy-MM-dd", DateUtil.ASIA_SEOUL_ZONE).plusSeconds(86400);

        Long period = endDateTime.getEpochSecond() - startDateTime.getEpochSecond();
        Long days = period > 0 ? period / 86400 : 0;
        if (days > 3) {
            throw new ExceedException(Executor.Type.DUMMY, "Search range can not be over 3 days");
        } else {

            List<PayLetterLog> logs = this.payLetterService.getLogs(startDateTime, endDateTime, locale);
            Result<List<PayLetterLog>> result = new Result<>();
            result.setCode(Integer.parseInt(ErrorCode.LEZHIN_OK.getCode()));
            result.setData(logs);
            return result;
        }


    }

}
