package com.lezhin.panther;

import com.lezhin.panther.exception.SessionException;
import com.lezhin.panther.model.Certification;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.pg.happypoint.PointAggregator;
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

    public void saveHappypointAggregator(final PointAggregator info) {
        String key = String.format("happypoint:%s:mbrNo:%s", info.getYm(), info.getMbrNo());

        PointAggregator value = getHappypointAggregator(info.getMbrNo(), info.getYm());
        if (value != null) {
            info.setPointSum(value.getPointSum() + info.getPointSum());
        }
        redisService.setValue(key, info, 31, TimeUnit.DAYS);
    }

    public void resetHappypointAggregator(final PointAggregator info) {
        String key = String.format("happypoint:%s:mbrNo:%s", info.getYm(), info.getMbrNo());
        logger.info("reset HappypointAggregator = {}", info.toString());
        redisService.setValue(key, info, 31, TimeUnit.DAYS);
    }

    public PointAggregator getHappypointAggregator(final String mbrNo, String ym) {
        String key = String.format("happypoint:%s:mbrNo:%s", ym, mbrNo);

        PointAggregator value;
        try {
            value = (PointAggregator) redisService.getValue(key);
        } catch (Exception e) {
            logger.info("Failed to deserialize PointAggregator. set and return 0 point. : " + e.getMessage());
            value = new PointAggregator(mbrNo, ym, 0);
            redisService.setValue(key, value, 31, TimeUnit.DAYS);
        }
        return value;
    }

    public void saveRequestInfo(RequestInfo requestInfo) {
        if (requestInfo.getPayment().getPaymentId() == null) {
            throw new SessionException("Failed to save session. paymentId is null");
        }
        String key = RedisService.generateKey("reservation", "requestinfo",
                String.valueOf(requestInfo.getPayment().getPaymentId()));
        redisService.setValue(key, requestInfo, 4, TimeUnit.DAYS);
    }

    public RequestInfo getRequestInfo(long paymentId) {
        String key = RedisService.generateKey("reservation", "requestinfo", String.valueOf(paymentId));
        RequestInfo value = (RequestInfo) redisService.getValue(key);
        if (value == null) {
            throw new SessionException("RequestInfo not found: paymentId=" + paymentId);
        }
        return value;
    }

    public Object get(final String key) {
        return redisService.getValue(key);
    }

    public Boolean delete(final String key) {
        return redisService.deleteValue(key);
    }

}
