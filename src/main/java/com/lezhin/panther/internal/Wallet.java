package com.lezhin.panther.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author taemmy
 * @since 2018. 7. 3.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Wallet {
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
}
