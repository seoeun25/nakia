package com.lezhin.panther;

import com.lezhin.panther.util.DateUtil;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author seoeun
 * @since 2017.11.04
 */
@Configuration
public class PantherConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PantherConfiguration.class);

    private static int CONNECT_TIMEOUT = Long.valueOf(DateUtil.ONE_MINUTE * 2).intValue();
    private static int READ_TIMEOUT = Long.valueOf(DateUtil.ONE_MINUTE * 2).intValue();

    private @Value("${spring.redis.host}")
    String redisHost;
    private @Value("${spring.redis.port}")
    int redisPort;
    private @Value("${spring.redis.database}")
    int redisDatabase;

    private @Value("${spring.datasource.url}")
    String datasourceUrl;
    private @Value("${spring.datasource.username}")
    String datasourceUsername;
    private @Value("${spring.datasource.password}")
    String datasourcePassword;
    private @Value("${spring.datasource.driver-class-name}")
    String datasourceDriver;

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
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        clientHttpRequestFactory.setReadTimeout(READ_TIMEOUT);

        HttpClient httpClient = HttpClientBuilder.create()
                .disableCookieManagement()
                .useSystemProperties()
                .build();

        clientHttpRequestFactory.setHttpClient(httpClient);

        return clientHttpRequestFactory;

    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        try {
            logger.info("redis host= {}, port = {}, database={}", redisHost, redisPort, redisDatabase);
            logger.info("datastore url= {}, username = {}, password={}, driver ={}",
                    datasourceUrl, datasourceUsername, datasourcePassword, datasourceDriver);

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

    @Bean
    public RestTemplate restTemplate(@Autowired ClientHttpRequestFactory clientHttpRequestFactory) {
        return new RestTemplate(clientHttpRequestFactory);
    }

}
