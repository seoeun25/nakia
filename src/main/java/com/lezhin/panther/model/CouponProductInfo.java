package com.lezhin.panther.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author taemmy
 * @since 2018. 7. 30.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CouponProductInfo {
    private Long productId;
    private String name;
    private String status;
}
