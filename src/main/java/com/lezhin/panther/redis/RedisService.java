package com.lezhin.panther.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author seoeun
 * @since 2017.11.13
 */
@Service
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    private final RedisTemplate<String, Object> template;

    public RedisService(@Qualifier("simpleRedisTemplate") RedisTemplate template) {
        this.template = template;
    }

    public Object getValue(final String key) {
        return template.opsForValue().get(key);
    }

    public void setValue(final String key, final Object value) {
        template.opsForValue().set(key, value);

        logger.info("Save to redis. key = {}", key);
    }

    public void setValue(final String key, final Object value, final long timeout, final TimeUnit unit) {
        template.opsForValue().set(key, value);

        template.expire(key, timeout, unit);
        logger.info("Save to redis. key = {}", key);
    }

    public Boolean deleteValue(final String key) {
        return template.delete(key);
    }

}
