package com.lezhin.panther.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author seoeun
 * @since 2017.11.06
 */
@Data
@Configuration
@ConfigurationProperties("panther")
public class PantherProperties {

    private String webUrl;
    private String apiUrl;
    private String pantherUrl;
    private String slackChannel;
    private String cloudWatch;
    private Happypoint happypoint;
    private Lguplus lguplus;

    @Data
    public static class Happypoint {
        private String hpcUrl;
        private String mchtNo;
    }

    @Data
    public static class Lguplus {
        private String confDir;
        private String cstPlatform;
        private String cstMid;
        private String txName;
    }
}
