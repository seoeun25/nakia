package com.lezhin.panther.redis;

import com.lezhin.panther.PantherApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    public <T> T getValue(final String key, final Class<T> clz) {
        Object obj = getValue(key);
        if (obj != null) {
            try {
                return clz.cast(obj);
            } catch (ClassCastException cce) {}
        }
        return null;
    }

    public void setValue(final String key, final Object value) {
        template.opsForValue().set(key, value);

        logger.debug("Save to redis. key = {}", key);
    }

    public void setValue(final String key, final Object value, final long timeout, final TimeUnit unit) {
        template.opsForValue().set(key, value);

        template.expire(key, timeout, unit);
        logger.debug("Save to redis. key = {}", key);
    }

    public Boolean deleteValue(final String key) {
        return template.delete(key);
    }

    public static String generateKey(String module, String name, String value) {
        if (StringUtils.isEmpty(module) || StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Argument can not be empty");
        }
        String key = String.format("%s:%s:%s:%s", PantherApplication.APP_NAME, module, name, value);
        return key;
    }


}
