package com.lezhin.avengers.panther.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author seoeun
 * @since 2017.11.06
 */
@Configuration
@ConfigurationProperties("lezhin")
public class LezhinProperties {

    private String webUrl;
    private String apiUrl;
    private String pantherUrl;
    private String slackChannel;
    private String cloudWatch;
    private Happypoint happypoint;

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getPantherUrl() {
        return pantherUrl;
    }

    public void setPantherUrl(String pantherUrl) {
        this.pantherUrl = pantherUrl;
    }

    public String getSlackChannel() {
        return slackChannel;
    }

    public void setSlackChannel(String slackChannel) {
        this.slackChannel = slackChannel;
    }

    public Happypoint getHappypoint() {
        return happypoint;
    }

    public void setHappypoint(Happypoint happypoint) {
        this.happypoint = happypoint;
    }

    public String getCloudWatch() {
        return cloudWatch;
    }

    public void setCloudWatch(String cloudWatch) {
        this.cloudWatch = cloudWatch;
    }

    public static class Happypoint {
        private String hpcUrl;
        private String mchtNo;

        public String getHpcUrl() {
            return hpcUrl;
        }

        public void setHpcUrl(String hpcUrl) {
            this.hpcUrl = hpcUrl;
        }

        public String getMchtNo() {
            return mchtNo;
        }

        public void setMchtNo(String mchtNo) {
            this.mchtNo = mchtNo;
        }

    }
}
