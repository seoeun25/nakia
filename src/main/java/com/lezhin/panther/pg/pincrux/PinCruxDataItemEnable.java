package com.lezhin.panther.pg.pincrux;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper=false)
public class PinCruxDataItemEnable extends PinCruxbase {
    private String custom_url;
    public String customUrl;

    public PinCruxDataItemEnable(){}
}

