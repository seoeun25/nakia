package com.lezhin.panther.pg.pincrux;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 핀크럭스 광고 요청 데이터
 *
 * @author benjamin
 * @since 2017.1.12
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PinCruxUser implements Serializable{
    private Long usrKey;
    private String authHeader;
    private List<PinCruxDataItem> ads;

    public PinCruxUser(Long usrKey, String authHeader, List<PinCruxDataItem> ads){
        this.usrKey = usrKey;
        this.authHeader = authHeader;
        this.ads = ads;
    }
}
