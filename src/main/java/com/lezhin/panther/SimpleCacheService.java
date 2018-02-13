package com.lezhin.panther;

import com.lezhin.avengers.panther.model.HappypointAggregator;
import com.lezhin.panther.exception.PantherException;
import com.lezhin.panther.exception.SessionException;
import com.lezhin.panther.executor.Executor;
import com.lezhin.panther.model.Certification;
import com.lezhin.panther.model.Payment;
import com.lezhin.panther.model.RequestInfo;
import com.lezhin.panther.pg.lguplus.LguplusPayment;
import com.lezhin.panther.redis.RedisService;
import com.lezhin.panther.util.JsonUtil;

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
        if (requestInfo.getPayment().getPaymentId() == null) {
            throw new SessionException(Executor.Type.UNKNOWN, "Failed to save session. paymentId is null");
        }
        String key = RedisService.generateKey("reservation", "requestinfo",
                String.valueOf(requestInfo.getPayment().getPaymentId()));
        redisService.setValue(key, requestInfo, 4, TimeUnit.DAYS);
    }

    public RequestInfo getRequestInfo(long paymentId) {
        String key = RedisService.generateKey("reservation", "requestinfo", String.valueOf(paymentId));
        RequestInfo value = (RequestInfo) redisService.getValue(key);
        if (value == null) {
            throw new SessionException(Executor.Type.UNKNOWN, "RequestInfo not found: paymentId=" + paymentId);
        }
        try {
            if (value.getPayment().getPgPayment() instanceof com.lezhin.panther.lguplus.LguplusPayment) {
                com.lezhin.panther.lguplus.LguplusPayment oldPgPayment = (com.lezhin.panther.lguplus.LguplusPayment) value.getPayment().getPgPayment();
                String json = JsonUtil.toJson(oldPgPayment);
                LguplusPayment newPgPayment = JsonUtil.fromJson(json, LguplusPayment.class);

                Payment payment = value.getPayment();
                payment.setPgPayment(newPgPayment);
                value = value.withPayment(payment);

                logger.info("Succeed to convert from oldPagPayment to newPgPayment");
            }
        } catch (Exception e) {
            throw new PantherException(Executor.Type.LGUDEPOSIT, "Failed to convert from oldPgPayment to " +
                    "newPgPayment", e);
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
