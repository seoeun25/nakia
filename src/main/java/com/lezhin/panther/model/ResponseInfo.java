package com.lezhin.panther.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author seoeun
 * @since 2017.10.25
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Getter
@ToString
public class ResponseInfo implements Serializable {

    private String code;
    private String description;

    public ResponseInfo() {

    }

    public ResponseInfo(ResponseCode errorCode) {
        this.code = errorCode.getCode();
        this.description = errorCode.getMessage();
    }

    public ResponseInfo(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public enum ResponseCode {
        //spc
        SPC_OK("00", "SPC_OK"),
        SPC_DENY_77("77", "SPC_DENY_77"),
        SPC_DENY_88("88", "SPC_DENY_88"),
        SPC_DENY_44("44", "SPC_DENY_44"),
        SPC_ERROR_80("80", "SPC_ERROR_80"),
        SPC_ERROR_92("92", "SPC_ERROR_92"),
        SPC_ERROR_22("22", "SPC_ERROR_22"),
        SPC_ERROR_99("99", "SPC_ERROR_99"),

        // lguplus
        LGUPLUS_OK("0000", "OK"),
        LGUPLUS_ERROR("X0X0", "LGUPLUS_ERROR"),

        PINCRUX_OK("00", "PINCRUX_OK"),
        PINCRUX_ERROR("01", "PINCRUX_ERROR"),
        PINCRUX_NOT_AVAILABLE("99", "Service Not available"),

        // lpoint (하위 몇개를 제외하곤 L.Point code 이용)
        LPOINT_OK("00", "OK"),
        LPOINT_EXCEED_DAY("44", "Exceed Day Throttle"),
        LPOINT_EXCEED_MONTH("44", "Exceed Month Throttle"),

        // internalPayment
        INTERNAL_OK("0", "OK"),

        // panther
        LEZHIN_OK("0000", "OK"),
        LEZHIN_UNKNOWN("1100", "UNKNOWN"),
        LEZHIN_PARAM("1001", "Param Error"),
        LEZHIN_PRECONDITION("1002", "Precondition Error"),
        LEZHIN_EXECUTION("1003", "Execution Error"),
        LEZHIN_PANTHER("1004", "Panther Error"),
        LEZHIN_INTERNAL_PAYMNENT("1005", "InternalPayment Error"),
        LEZHIN_EXCEED("1007", "Exceed throttle"),
        LEZHIN_CI("1008", "No ConnectionInfo"),
        LEZHIN_SESSION("1009", "No Session"),
        LEZHIN_OWNER("1010", "Payment Owner Error"),

        LEZHIN_UNAUTHORIZATION("5001", "Unauthorized Request"),
        LEZHIN_THROWABLE("9001", "Unexpected Error");

        private String code;
        private String message;

        ResponseCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

    }

}
