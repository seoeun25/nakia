package com.lezhin.panther.controller;

import com.lezhin.panther.SimpleCacheService;
import com.lezhin.panther.model.Certification;
import com.lezhin.panther.model.ResponseInfo;
import com.lezhin.panther.model.ResponseInfo.ResponseCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO requestMapping 이 잘못되었음. certification/v1 으로 해서 api 관련 controller 에 대해서 versioning 해야 함.
 * @author seoeun
 * @since 2017.11.08
 */
@RestController
@RequestMapping("/v1/certification")
public class CertifcationController {

    private static final Logger logger = LoggerFactory.getLogger(CertifcationController.class);

    @Autowired
    private SimpleCacheService simpleCacheService;

    /**
     * 본인 인증이 성공했을 때 호출된다. ex-module에서는 api를 호출 한 후 다시 /callback 호출.
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/success", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo onSuccess(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam(required = false) String certificationType,
                                  @RequestBody Certification certification) {

        logger.info("certification. userId = {}, name = {}, ci = {}",
                certification.getUserId(), certification.getName(), certification.getCI());

        simpleCacheService.saveCertification(certification);

        // FIXME only for debugging. Need to Remove
        try {
            Thread.sleep(100);
            Certification result = simpleCacheService.getCertification(certification.getUserId());
            logger.info("saved = {}", result.toString());
        } catch (Exception e) {
            logger.warn("Failed to retrieve ", e);
        }

        return new ResponseInfo(ResponseCode.LEZHIN_OK);

    }

}
