package com.lezhin.panther.pg.pincrux;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;


/**
 * 핀크럭스 광고 요청 데이터
 *
 * @author benjamin
 * @since 2017.1.12
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PinCruxRequest implements Serializable {

    private Integer pubkey;
    private Integer appkey;
    private Long usrkey;
    private String cruxkey;
    private String subpid;
    private String dev_id;
    private String adv_id;
    private String acc_id;
    private String and_id;
    private String device_brand;
    private String device_model;
    private String mtype;
    private String client_ip;
    private String authHeader;
    private String app_title;
    private String transid;
    private Integer os_flag;


    public PinCruxRequest(Integer pubkey,
                          Integer appkey,
                          Long usrkey,
                          String cruxkey,
                          String subpid,
                          String dev_id,
                          String adv_id,
                          String acc_id,
                          String and_id,
                          String device_brand,
                          String device_model,
                          String mtype,
                          String client_ip,
                          String authHeader,
                          String app_title,
                          String transid,
                          Integer os_flag
    ){
        this.pubkey = pubkey;
        this.appkey = appkey;
        this.usrkey = usrkey;
        this.cruxkey= cruxkey;
        this.subpid = subpid;
        this.dev_id = dev_id;
        this.adv_id = adv_id;
        this.acc_id = acc_id;
        this.and_id = and_id;
        this.device_brand = device_brand;
        this.device_model = device_model;
        this.mtype = mtype;
        this.client_ip = client_ip;
        this.authHeader = authHeader;
        this.app_title = app_title;
        this.transid = transid;
        this.os_flag = os_flag;
    }
}
