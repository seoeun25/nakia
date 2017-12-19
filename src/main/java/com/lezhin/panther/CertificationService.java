package com.lezhin.panther;

import com.lezhin.avengers.panther.model.HappypointAggregator;
import com.lezhin.panther.model.Certification;
import com.lezhin.panther.redis.RedisService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author seoeun
 * @since 2017.11.10
 */
@Service
public class CertificationService {

    private static final Logger logger = LoggerFactory.getLogger(CertificationService.class);

    private final RedisService redisService;

    public CertificationService(RedisService redisService) {
        this.redisService = redisService;
    }

    public void saveCertification(Certification certification) {
        String key = String.format("user:%s", certification.getUserId());
        redisService.setValue(key, certification, 10, TimeUnit.MINUTES);
    }

    /**
     * return {@code Certification}
     *
     * @param userId
     * @return
     */
    public Certification getCertification(Long userId) {
        String key = String.format("user:%s", userId);
        Certification value = (Certification) redisService.getValue(key);
        return value;
    }

    public void addPaymentResult(final HappypointAggregator info) {
        String key = String.format("happypoint:%s:mbrNo:%s", info.getYm(), info.getMbrNo());

        HappypointAggregator value = getPaymentResult(info.getMbrNo(), info.getYm());
        if (value != null) {
            info.setPointSum(value.getPointSum() + info.getPointSum());
        }
        logger.info("addHappypointAggregator = {}", info.toString());
        redisService.setValue(key, info, 31, TimeUnit.DAYS);
    }

    public HappypointAggregator getPaymentResult(final String mbrNo, String ym) {
        String key = String.format("happypoint:%s:mbrNo:%s", ym, mbrNo);

        HappypointAggregator value = (HappypointAggregator) redisService.getValue(key);
        return value;
    }

    public Object get(final String key) {
        return redisService.getValue(key);
    }

    public Boolean delete(final String key) {
        return redisService.deleteValue(key);
    }

}
