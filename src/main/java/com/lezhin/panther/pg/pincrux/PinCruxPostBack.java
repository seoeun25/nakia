package com.lezhin.panther.pg.pincrux;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;


/**
 * 핀크럭스 포스트백 파라미터
 *
 * @author benjamin
 * @since 2017.1.12
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PinCruxPostBack implements Serializable {

    private Integer pubkey;
    private Integer appkey;
    private Long usrkey;
    private String cruxkey;
    private String dev_id;
    private String adv_id;
    private String transid;


    public PinCruxPostBack(Integer pubkey,
                           Integer appkey,
                           Long usrkey,
                           String cruxkey,
                           String dev_id,
                           String adv_id,
                           String transid
                           ){
        this.pubkey = pubkey;
        this.appkey = appkey;
        this.usrkey = usrkey;
        this.cruxkey= cruxkey;
        this.dev_id = dev_id;
        this.adv_id = adv_id;
        this.transid= transid;
    }
}
