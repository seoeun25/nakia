package com.lezhin.panther.pg.pincrux;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author seoeun
 * @since 2018.03.20
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CruxADs implements Serializable {

    private String status;
    private String pubkey;
    private Integer itemCount;
    private String usrTargetTel;
    private String pubTitle;
    private String colorFlag;
    private String skinFlag;
    private String daFlag;
    private String devFlag;
    private DisplayAd displayAd;
    private List<Item> items;
    private String code;
    private String msg;

    private Integer totalCoin; // panther

    @JsonProperty("item_cnt")
    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    @JsonProperty("itemCount")
    public Integer getItemCount() {
        return itemCount;
    }

    @JsonProperty("usr_target_tel")
    public void setUsrTargetTel(String usrTargetTel) {
        this.usrTargetTel = usrTargetTel;
    }

    @JsonProperty("usrTargetTel")
    public String getUsrTargetTel() {
        return usrTargetTel;
    }

    @JsonProperty("pub_title")
    public void setPubTitle(String pubTitle) {
        this.pubTitle = pubTitle;
    }

    @JsonProperty("pubTitle")
    public String getPubTitle() {
        return pubTitle;
    }

    @JsonProperty("color_flag")
    public void setColorFlag(String colorFlag) {
        this.colorFlag = colorFlag;
    }

    @JsonProperty("colorFlag")
    public String getColorFlag() {
        return colorFlag;
    }

    @JsonProperty("skin_flag")
    public void setSkinFlag(String skinFlag) {
        this.skinFlag = skinFlag;
    }

    @JsonProperty("skinFlag")
    public String getSkinFlag() {
        return skinFlag;
    }

    @JsonProperty("da_flag")
    public void setDaFlag(String daFlag) {
        this.daFlag = daFlag;
    }

    @JsonProperty("daFlag")
    public String getDaFlag() {
        return daFlag;
    }

    @JsonProperty("dev_flag")
    public void setDevFlag(String devFlag) {
        this.devFlag = devFlag;
    }

    @JsonProperty("devFlag")
    public String getDevFlag() {
        return devFlag;
    }

    @JsonProperty("item_list")
    public void setItems(List<Item> items) {
        this.items = items;
    }

    @JsonProperty("items")
    public List<Item> getItems() {
        return items;
    }

    public DisplayAd getDisplayAd() {
        return displayAd;
    }

    public void setDisplayAd(DisplayAd displayAd) {
        this.displayAd = displayAd;
    }

    @Data
    public static class DisplayAd implements Serializable {
        private String image;
        private String url;
        private String os;
    }

}
