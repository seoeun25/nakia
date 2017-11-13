package com.lezhin.avengers.panther.redis;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RedisTemplate<String, Object> template;

    public Object getValue(final String key) {
        return template.opsForValue().get(key);
    }

    public void setValue(final String key, final Object value) {
        template.opsForValue().set(key, value);

        // set a expire for a message
        template.expire(key, 10, TimeUnit.MINUTES);
        logger.info("Save to redis. key = {}", key);
    }

}
