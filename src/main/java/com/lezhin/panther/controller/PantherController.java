package com.lezhin.panther.controller;

import com.lezhin.panther.CertificationService;
import com.lezhin.panther.ErrorCode;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.util.DateUtil;
import com.lezhin.panther.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

/**
 * Panther controller. Json을 return.
 *
 * @author seoeun
 * @since 2017.11.14
 */
@Controller
@RequestMapping("/panther")
public class PantherController {

    private static final Logger logger = LoggerFactory.getLogger(PantherController.class);

    private final CertificationService certificationService;

    public PantherController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo basicInfo(HttpServletRequest request, HttpServletResponse response) {

        logger.info("basicInfo");


        ResponseInfo responseInfo = new ResponseInfo("hello", "panther");
        return responseInfo;

    }

    @RequestMapping(value = "/version", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo date(HttpServletRequest request, HttpServletResponse response) {

        logger.info("version");

        DateUtil.printDate();

        // TODO version 정보를 version.txt 에서 읽지 말고, artifact의 verion 정보를 읽도록.
        String version = Optional.ofNullable(Util.loadVersion()).orElse("NULL");

        ResponseInfo responseInfo = new ResponseInfo("version", version);
        return responseInfo;

    }

    /**
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/cache", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo check(HttpServletRequest request, HttpServletResponse response,
                              @RequestBody Map<String, Object> map) {

        Object key = map.get("key");
        Boolean delete = Boolean.valueOf(Optional.ofNullable(map.get("delete")).orElse("false").toString());
        if (key != null) {
            try {
                Object value = certificationService.get(key.toString());
                logger.info("key = {}, value = {}", key, value);

                if (delete) {
                    logger.info("deleted = {}", certificationService.delete(key.toString()));
                }
            } catch (Exception e) {
                logger.warn("Failed to get cache value", e);
            }
        }

        return new ResponseInfo(ErrorCode.LEZHIN_OK.getCode(), ErrorCode.LEZHIN_OK.getMessage());
    }

}
