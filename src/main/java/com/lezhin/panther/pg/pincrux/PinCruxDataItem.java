package com.lezhin.panther.pg.pincrux;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PinCruxDataItem implements Serializable {

    private Integer appkey;//
    private Integer os_flag;
    private String grp;
    private Integer osFlag;
    private String app_nm;//
    private String appName;
    private String action_plan;//
    private String actionPlan;
    private String context;//
    private Integer fee;
    private Integer coin_int;//
    private Integer coinInt;
    private String view_title;//
    private String viewTitle;
    private String view_sub_title;//
    private String viewSubTitle;//
    private String view_button;//
    private String viewButton;
    private PinCruxImage listImg;
    private PinCruxImage viewTitleImg;
    private PinCruxImage viewContentImg;


    public void initCamel(){
        this.appName = this.app_nm;
        this.app_nm = null;
        this.actionPlan = this.action_plan;
        this.action_plan = null;
        this.coinInt = this.coin_int;
        this.coin_int = null;
        this.viewTitle = this.view_title;
        this.view_title = null;
        this.viewSubTitle = this.view_sub_title;
        this.view_sub_title = null;
        this.viewButton = this.view_button;
        this.view_button = null;
        this.osFlag = this.os_flag;
        this.os_flag = null;
    }
}


