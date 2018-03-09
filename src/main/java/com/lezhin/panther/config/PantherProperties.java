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
    private boolean happypointAvailable;
    private boolean lgudepositAvailable;
    private boolean pincruxAvailable;
    private Happypoint happypoint;
    private Lguplus lguplus;
    private String pushUrl;
    private Pincrux pincrux;
    private Wallets wallets;

    @Data
    public static class Happypoint {
        private String hpcUrl;
        private String mchtNo;
    }

    @Data
    public static class Lguplus {
        private String confDir;
        private String logDir;
        private String cstPlatform;
        private String cstMid;
        private String txName;
    }

    @Data
    public static class Pincrux {
        private Boolean testFlag = true;
    }

    @Data
    public static class Wallets {
        private String apiUrl;
        private Integer companyEventIdPinCrux;
        private Integer usageRestrictionIdPinCrux;
        private String cmsToken;
    }
}
