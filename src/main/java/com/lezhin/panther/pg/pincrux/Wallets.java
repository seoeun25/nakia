package com.lezhin.panther.pg.pincrux;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.io.Serializable;

/**
 * 선물함 지급
 *
 * @author benjamin
 * @since 2018.02.01
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Wallets implements Serializable {
    private Long userId;
    private String locale;
    private String platform;
    private Integer companyEventId;
    private Integer usageRestrictionId;
    private String purchaseType;
    private String purchaseTitle;
    private Boolean sendPresent;
    private String presentTitle;
    private String presentDescription;
    private Integer amount;
    private Boolean immediate;

    public Wallets(){}
}
