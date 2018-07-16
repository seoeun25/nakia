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
    private String cmsUrl;
    private String walletUrl;
    private String cmsToken;
    private String pantherUrl;
    private String slackChannel;
    private String cloudWatch;
    private boolean happypointAvailable;
    private boolean lgudepositAvailable;
    private boolean pincruxAvailable;
    private Happypoint happypoint;
    private Lguplus lguplus;
    private LPoint lpoint;
    private String pushUrl;
    private Pincrux pincrux;
    private Tapjoy tapjoy;

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
    public static class LPoint {
        private String lpointUrl;
        private String copMcnoWeb;
        private String copMcnoMobile;
        private String keyPath;
    }

    @Data
    public static class Pincrux {
        private String pincruxUrl;
        private Integer pubkey;
        @Deprecated
        private String testFlag;
        private int cacheRetention;
        private int timeout; // milliseconds
        private Integer companyEventId;
        private Integer usageRestrictionId;
    }

    @Data
    public static class Tapjoy {
        private String secretKey;
        private Integer companyEventId;
        private Integer usageRestrictionId;
    }

    @Data
    @Deprecated
    public static class Wallets {
        private String apiUrl;
        private Integer companyEventIdPinCrux;
        private Integer usageRestrictionIdPinCrux;
    }
}
