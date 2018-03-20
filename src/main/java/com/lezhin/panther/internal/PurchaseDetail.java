package com.lezhin.panther.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author seoeun
 * @since 2018.03.14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Getter
@ToString
public class PurchaseDetail implements Serializable {

    private Purchase charge;
    private List<Purchase> purchases;
    private List<Voucher> vouchers;

}
