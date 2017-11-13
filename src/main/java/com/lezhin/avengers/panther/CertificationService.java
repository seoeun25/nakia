package com.lezhin.avengers.panther;

import com.lezhin.avengers.panther.model.Certification;
import com.lezhin.avengers.panther.redis.RedisService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author seoeun
 * @since 2017.11.10
 */
@Service
@Slf4j
public class CertificationService {

    @Autowired
    private RedisService redisService;

    public CertificationService() {

    }

    public void saveCertification(Certification certification) {
        String key = String.format("user:%s", certification.getUserId());
        //String value = String.format("%s_%s", certification.getName(), certification.getCI());
        redisService.setValue(key, certification);
    }

    /**
     * return String[name, CI]
     * @param userId
     * @return
     */
    public Certification getCertification(Long userId) {
        String key = String.format("user:%s", userId);
        Certification value = (Certification) redisService.getValue(key);
        return value;
    }

}
