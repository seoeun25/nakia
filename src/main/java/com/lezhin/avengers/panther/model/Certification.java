package com.lezhin.avengers.panther.model;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * @author seoeun
 * @since 2017.11.07
 */
public class Certification implements Serializable{

    private Long userId;
    private String name;
    private String birthday;
    private String gender;
    private String CI;
    private String DI;

    public Certification() {

    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCI() {
        return CI;
    }

    public void setCI(String CI) {
        this.CI = CI;
    }

    public String getDI() {
        return DI;
    }

    public void setDI(String DI) {
        this.DI = DI;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("name", name)
                .add("birthday", birthday)
                .add("gender", gender)
                .add("CI", CI)
                .add("DI", DI)
                .toString();
    }

}
