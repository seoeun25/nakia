package com.lezhin.panther;

import com.lezhin.avengers.panther.model.HappypointAggregator;
import com.lezhin.panther.model.Certification;
import com.lezhin.panther.model.RequestInfo;
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
public class SimpleCacheService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCacheService.class);

    private final RedisService redisService;

    public SimpleCacheService(RedisService redisService) {
        this.redisService = redisService;
    }

    public void saveCertification(Certification certification) {
        String key = RedisService.generateKey("certification", "user", certification.getUserId().toString());
        redisService.setValue(key, certification, 10, TimeUnit.MINUTES);
    }

    /**
     * return {@code Certification}
     *
     * @param userId
     * @return
     */
    public Certification getCertification(Long userId) {
        String key = RedisService.generateKey("certification", "user", userId.toString());
        Certification value = (Certification) redisService.getValue(key);
        return value;
    }

    public void saveHappypointAggregator(final HappypointAggregator info) {
        String key = String.format("happypoint:%s:mbrNo:%s", info.getYm(), info.getMbrNo());

        HappypointAggregator value = getHappypointAggregator(info.getMbrNo(), info.getYm());
        if (value != null) {
            info.setPointSum(value.getPointSum() + info.getPointSum());
        }
        logger.info("addHappypointAggregator = {}", info.toString());
        redisService.setValue(key, info, 31, TimeUnit.DAYS);
    }

    public HappypointAggregator getHappypointAggregator(final String mbrNo, String ym) {
        String key = String.format("happypoint:%s:mbrNo:%s", ym, mbrNo);

        HappypointAggregator value;
        try {
            value = (HappypointAggregator) redisService.getValue(key);
        } catch (Exception e) {
            // TODO package refactoring의 side effect. 1월에 update.
            logger.info("Failed to deserialize HappypointAggregator. set and return 2000 point. : " + e.getMessage());
            value = new HappypointAggregator(mbrNo, ym, 2000);
            redisService.setValue(key, value, 31, TimeUnit.DAYS);
        }
        return value;
    }

    public void saveRequestInfo(RequestInfo requestInfo) {
        String key = RedisService.generateKey("reservation", "requestinfo",
                String.valueOf(requestInfo.getPayment().getPaymentId()));
        redisService.setValue(key, requestInfo, 4, TimeUnit.DAYS);
    }

    public RequestInfo getRequestInfo(Long paymentId) {
        String key = RedisService.generateKey("reservation", "requestinfo", String.valueOf(paymentId));
        RequestInfo value = (RequestInfo) redisService.getValue(key);
        return value;
    }

    public Object get(final String key) {
        return redisService.getValue(key);
    }

    public Boolean delete(final String key) {
        return redisService.deleteValue(key);
    }

}
