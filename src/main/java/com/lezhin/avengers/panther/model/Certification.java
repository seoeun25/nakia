package com.lezhin.avengers.panther.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author seoeun
 * @since 2017.11.07
 */
@Data
public class Certification implements Serializable {

    private Long userId;
    private String name;
    private String birthday;
    private String gender;
    private String CI;
    private String DI;

}
