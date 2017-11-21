package com.lezhin.avengers.panther.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * @author seoeun
 * @since 2017.11.19
 */
@Component
public class EmbededRedis {

    private static final Logger logger = LoggerFactory.getLogger(EmbededRedis.class);
    private final RedisServer redisServer;
    private int redisPort = 6379;

    public EmbededRedis() throws IOException {
        this.redisServer = new RedisServer(redisPort);
        logger.info("EmbededRedis initiated. port = {}", redisPort);
    }

    @PostConstruct
    public void startRedis() throws IOException {
        redisServer.start();
        logger.info("EmbededRedis start ....");
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
        logger.info("EmbededRedis stop ....");
    }
}
