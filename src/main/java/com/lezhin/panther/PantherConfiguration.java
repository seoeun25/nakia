package com.lezhin.panther;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author seoeun
 * @since 2017.11.04
 */
@Configuration
public class PantherConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PantherConfiguration.class);

    private @Value("${spring.redis.host}")
    String redisHost;
    private @Value("${spring.redis.port}")
    int redisPort;
    private @Value("${spring.redis.database}")
    int redisDatabase;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://www.lezhin.com", "https://www.lezhin.com",
                                "http://s.lezhin.com", "https://s.lezhin.com",
                                "http://beta-www.lezhin.com", "https://beta-www.lezhin.com",
                                "http://q-www.lezhin.com", "https://q-www.lezhin.com",
                                "http://mirror-www.lezhin.com", "https://q-www.lezhin.com",
                                "http://a-www.lezhin.com", "https://a-www.lezhin.com",
                                "http://local.lezhin.com", "https://local.lezhin.com",
                                "https://xpay.lgdacom.net:7443", "https://xpay.lgdacom.net"
                        )
                        .maxAge(1800);
            }
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        return objectMapper;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        int timeout = 20000;
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout);
        return clientHttpRequestFactory;

    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        try {
            logger.info("redis host= {}, port = {}, database={}", redisHost, redisPort, redisDatabase);
            JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
            jedisConnectionFactory.setHostName(redisHost);
            jedisConnectionFactory.setPort(redisPort);
            jedisConnectionFactory.setDatabase(redisDatabase);
            logger.info("Jedis host={}, port={}, database={}", jedisConnectionFactory.getHostName(),
                    jedisConnectionFactory.getPort(),
                    jedisConnectionFactory.getDatabase());
            return jedisConnectionFactory;
        } catch (Exception e) {
            logger.warn("Failed to JedisConnection", e);
            throw e;
        }
    }

    @Qualifier("simpleRedisTemplate")
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

}
