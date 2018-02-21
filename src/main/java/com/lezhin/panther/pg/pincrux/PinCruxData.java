package com.lezhin.panther.pg.pincrux;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


/**
 * 핀크럭스 광고 단위 데이터
 *
 * @author benjamin
 * @since 2017.12.19
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper=false)
public class PinCruxData extends PinCruxbase {

    private String pubkey;
    private String pub_title;
    private String color_flag;
    private String skin_flag;
    private String da_flag;
    private String da_img;
    private String da_url;
    private String da_os;
    private String daOs;
    private DisplayAd displayAd;
    private Integer item_cnt;
    private Integer itemCount;
    private String usr_target_tel;
    private Integer total_coin;
    private Integer totalCoin;
    private List<PinCruxDataItem> item_list;
    private List<PinCruxDataItem> items;

    @Data
    public class DisplayAd {
        private String image;
        private String url;
        public  DisplayAd(){};
    }

    public void initCamel(){
        if(this.da_flag.equals("Y")){
            this.displayAd = new DisplayAd();
            this.displayAd.image = this.da_img;
            this.displayAd.url   = this.da_url;
        }
        this.totalCoin  = this.total_coin;
        this.itemCount  = this.item_cnt;
        this.items      = this.item_list;
        this.item_list  = null;
        this.da_flag    = null;
        this.da_img     = null;
        this.da_url     = null;
        this.da_os      = null;
        this.item_cnt   = null;
        this.total_coin = null;
        this.usr_target_tel = null;
        this.pub_title  = null;
    }
}

