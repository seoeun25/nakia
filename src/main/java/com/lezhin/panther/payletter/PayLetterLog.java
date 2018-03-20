package com.lezhin.panther.payletter;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.Max;
import java.io.Serializable;
import java.sql.Timestamp;


/**
 * 페이레터 간략 결제 데이터
 *
 * @author benjamin
 * @since 2017.12.19
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PayLetterLog implements Serializable {


    private Long lezhinPaymentId;
    private String idApproval;
    private Long userId;
    private Float paidAmount;
    private String currency;//USD, JPY
    private String state;
    private String ymd;
    private String regDate;
    private String paymentType;
    private String pgCompany;
    private String locale;
    private Long refIdPayment;

}
