package com.lezhin.panther.pg.pincrux;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Transient;
import java.io.Serializable;

/**
 * @author seoeun
 * @since 2018.03.20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Item implements Serializable {

    private Integer appkey;
    private Integer inFlag;
    private Integer osFlag; // 필수항목
    private String adFlag;
    private Integer agFlag;
    private String adCategory;
    private String appIcon;
    private String addImg1;
    private String addImg2;
    private String addImg3;
    private String appName; //TODO wallet. 필수 항목.
    private String actionPlan; // 필수 항목
    private String context; // 필수
    private String packageName; // packageNM
    private String urlScheme;
    private Integer totalLimitCnt;
    private Integer dayLimitCnt;
    private String startDate;
    private String endDate;
    private Integer targetFlag;
    private String targetTel;
    private String targetSex;
    private String targetAgeMin;
    private String targetAgeMax;
    private String targetOkPackage;
    private String targetNoPackage;
    private Integer fee; // TODO wallet
    private Integer coinInt; // TODO panther에서 만들어서 셋팅.
    private String adCategoryInt;
    private Integer tryFlag;
    private String coin;
    private String grp;
    private String ordby;
    private String viewTitle; // 필수
    private String viewSubTitle; // 필수
    private String viewButton; // 필수
    @Transient
    private CruxImage listImg; // 필수 항목
    @Transient
    private CruxImage viewTitleImg;
    @Transient
    private CruxImage viewContentImg;

    public Integer getAppkey() {
        return appkey;
    }

    public void setAppkey(Integer appkey) {
        this.appkey = appkey;
    }

    @JsonProperty("inFlag")
    public Integer getInFlag() {
        return inFlag;
    }

    @JsonProperty("in_flag")
    public void setInFlag(Integer inFlag) {
        this.inFlag = inFlag;
    }

    @JsonProperty("osFlag")
    public Integer getOsFlag() {
        return osFlag;
    }

    @JsonProperty("os_flag")
    public void setOsFlag(Integer osFlag) {
        this.osFlag = osFlag;
    }

    @JsonProperty("adFlag")
    public String getAdFlag() {
        return adFlag;
    }

    @JsonProperty("ad_flag")
    public void setAdFlag(String adFlag) {
        this.adFlag = adFlag;
    }

    @JsonProperty("agFlag")
    public Integer getAgFlag() {
        return agFlag;
    }

    @JsonProperty("ag_flag")
    public void setAgFlag(Integer agFlag) {
        this.agFlag = agFlag;
    }

    @JsonProperty("adCategory")
    public String getAdCategory() {
        return adCategory;
    }

    @JsonProperty("ad_category")
    public void setAdCategory(String adCategory) {
        this.adCategory = adCategory;
    }

    @JsonProperty("appIcon")
    public String getAppIcon() {
        return appIcon;
    }

    @JsonProperty("app_icon")
    public void setAppIcon(String appIcon) {
        this.appIcon = appIcon;
    }

    @JsonProperty("addImg1")
    public String getAddImg1() {
        return addImg1;
    }

    @JsonProperty("add_img1")
    public void setAddImg1(String addImg1) {
        this.addImg1 = addImg1;
    }

    @JsonProperty("addImg2")
    public String getAddImg2() {
        return addImg2;
    }

    @JsonProperty("add_img2")
    public void setAddImg2(String addImg2) {
        this.addImg2 = addImg2;
    }

    @JsonProperty("addImg3")
    public String getAddImg3() {
        return addImg3;
    }

    @JsonProperty("add_img3")
    public void setAddImg3(String addImg3) {
        this.addImg3 = addImg3;
    }

    @JsonProperty("appName")
    public String getAppName() {
        return appName;
    }

    @JsonProperty("app_nm")
    public void setAppName(String appName) {
        this.appName = appName;
    }

    @JsonProperty("actionPlan")
    public String getActionPlan() {
        return actionPlan;
    }

    @JsonProperty("action_plan")
    public void setActionPlan(String actionPlan) {
        this.actionPlan = actionPlan;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @JsonProperty("packageName")
    public String getPackageName() {
        return packageName;
    }

    @JsonProperty("package_nm")
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @JsonProperty("urlScheme")
    public String getUrlScheme() {
        return urlScheme;
    }

    @JsonProperty("url_scheme")
    public void setUrlScheme(String urlScheme) {
        this.urlScheme = urlScheme;
    }

    @JsonProperty("totalLimitCnt")
    public Integer getTotalLimitCnt() {
        return totalLimitCnt;
    }

    @JsonProperty("total_limit_cnt")
    public void setTotalLimitCnt(Integer totalLimitCnt) {
        this.totalLimitCnt = totalLimitCnt;
    }

    @JsonProperty("dayLimitCnt")
    public Integer getDayLimitCnt() {
        return dayLimitCnt;
    }

    @JsonProperty("day_limit_cnt")
    public void setDayLimitCnt(Integer dayLimitCnt) {
        this.dayLimitCnt = dayLimitCnt;
    }

    @JsonProperty("startDate")
    public String getStartDate() {
        return startDate;
    }

    @JsonProperty("start_date")
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @JsonProperty("endDate")
    public String getEndDate() {
        return endDate;
    }

    @JsonProperty("end_date")
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @JsonProperty("targetFlag")
    public Integer getTargetFlag() {
        return targetFlag;
    }

    @JsonProperty("target_flag")
    public void setTargetFlag(Integer targetFlag) {
        this.targetFlag = targetFlag;
    }

    @JsonProperty("targetTel")
    public String getTargetTel() {
        return targetTel;
    }

    @JsonProperty("target_tel")
    public void setTargetTel(String targetTel) {
        this.targetTel = targetTel;
    }

    @JsonProperty("targetSex")
    public String getTargetSex() {
        return targetSex;
    }

    @JsonProperty("target_sex")
    public void setTargetSex(String targetSex) {
        this.targetSex = targetSex;
    }

    @JsonProperty("targetAgeMin")
    public String getTargetAgeMin() {
        return targetAgeMin;
    }

    @JsonProperty("target_age_min")
    public void setTargetAgeMin(String targetAgeMin) {
        this.targetAgeMin = targetAgeMin;
    }

    @JsonProperty("targetAgeMax")
    public String getTargetAgeMax() {
        return targetAgeMax;
    }

    @JsonProperty("target_age_max")
    public void setTargetAgeMax(String targetAgeMax) {
        this.targetAgeMax = targetAgeMax;
    }

    @JsonProperty("targetOkPackage")
    public String getTargetOkPackage() {
        return targetOkPackage;
    }

    @JsonProperty("target_ok_package")
    public void setTargetOkPackage(String targetOkPackage) {
        this.targetOkPackage = targetOkPackage;
    }

    @JsonProperty("targetNoPackage")
    public String getTargetNoPackage() {
        return targetNoPackage;
    }

    @JsonProperty("target_no_package")
    public void setTargetNoPackage(String targetNoPackage) {
        this.targetNoPackage = targetNoPackage;
    }

    @JsonProperty("fee")
    public Integer getFee() {
        return fee;
    }

    @JsonProperty("fee")
    public void setFee(Integer fee) {
        this.fee = fee;
    }

    public Integer getCoinInt() {
        return coinInt;
    }

    public void setCoinInt(Integer coinInt) {
        this.coinInt = coinInt;
    }

    @JsonProperty("adCategoryInt")
    public String getAdCategoryInt() {
        return adCategoryInt;
    }

    @JsonProperty("ad_category_int")
    public void setAdCategoryInt(String adCategoryInt) {
        this.adCategoryInt = adCategoryInt;
    }

    @JsonProperty("tryFlag")
    public Integer getTryFlag() {
        return tryFlag;
    }

    @JsonProperty("try_flag")
    public void setTryFlag(Integer tryFlag) {
        this.tryFlag = tryFlag;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public String getGrp() {
        return grp;
    }

    public void setGrp(String grp) {
        this.grp = grp;
    }

    public String getOrdby() {
        return ordby;
    }

    public void setOrdby(String ordby) {
        this.ordby = ordby;
    }

    @JsonProperty("viewTitle")
    public String getViewTitle() {
        return viewTitle;
    }

    @JsonProperty("view_title")
    public void setViewTitle(String viewTitle) {
        this.viewTitle = viewTitle;
    }

    @JsonProperty("viewSubTitle")
    public String getViewSubTitle() {
        return viewSubTitle;
    }

    @JsonProperty("view_sub_title")
    public void setViewSubTitle(String viewSubTitle) {
        this.viewSubTitle = viewSubTitle;
    }

    @JsonProperty("viewButton")
    public String getViewButton() {
        return viewButton;
    }

    @JsonProperty("view_button")
    public void setViewButton(String viewButton) {
        this.viewButton = viewButton;
    }

    public CruxImage getListImg() {
        return listImg;
    }

    public void setListImg(CruxImage listImg) {
        this.listImg = listImg;
    }

    public CruxImage getViewTitleImg() {
        return viewTitleImg;
    }

    public void setViewTitleImg(CruxImage viewTitleImg) {
        this.viewTitleImg = viewTitleImg;
    }

    public CruxImage getViewContentImg() {
        return viewContentImg;
    }

    public void setViewContentImg(CruxImage viewContentImg) {
        this.viewContentImg = viewContentImg;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public class CruxImage implements Serializable {
        public String src;
        public String ext;
        public Integer width;
        public Integer height;
    }


}
