package com.lezhin.avengers.panther.controller;

import com.lezhin.avengers.panther.CertificationService;
import com.lezhin.avengers.panther.ErrorCode;
import com.lezhin.avengers.panther.model.Certification;
import com.lezhin.avengers.panther.model.ResponseInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author seoeun
 * @since 2017.11.08
 */
@Controller
@RequestMapping("/v1/certification")
public class CertifcationController {

    private static final Logger logger = LoggerFactory.getLogger(CertifcationController.class);

    @Autowired
    private CertificationService certificationService;

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

        certificationService.saveCertification(certification);
        
        // FIXME only for debugging. Need to Remove
        try{
            Thread.sleep(100);
        } catch (Exception e) {

        }
        Certification result = certificationService.getCertification(certification.getUserId());
        logger.info("saved = {}", result);

        return new ResponseInfo(ErrorCode.LEZHIN_OK.getCode(), ErrorCode.LEZHIN_OK.getMessage());

    }

}
