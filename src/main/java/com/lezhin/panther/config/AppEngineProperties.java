package com.lezhin.panther.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author benjamin
 * @since 2017.11.06
 */
@Data
@Configuration
@ConfigurationProperties("appengine")
public class AppEngineProperties {
    private String serviceAccountId;
    private String privateKey;
    private String datasetId;
}
