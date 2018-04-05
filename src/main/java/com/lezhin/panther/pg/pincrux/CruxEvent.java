package com.lezhin.panther.pg.pincrux;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author seoeun
 * @since 2018.04.06
 * @see {@link ADEvent}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CruxEvent {

    private Integer pubkey;
    private Integer appkey;
    private Long usrkey;
    private String adv_id;
    private String mtype;
    private String client_ip;
    private Integer os_flag;
    private String appName; // attp
    private Integer coinInt; // attp
    private String app_title; // postback
    private Integer coin; // postback

    private String code;
    private String msg;
    private String customUrl; // attp
    private String transid; // postabck

    // -- panther 에서 사용.
    @JsonIgnore
    private String authToken;

    @JsonProperty("customUrl")
    public String getCustomUrl() {
        return customUrl;
    }

    @JsonProperty("custom_url")
    public void setCustomUrl(String customUrl) {
        this.customUrl = customUrl;
    }
}
