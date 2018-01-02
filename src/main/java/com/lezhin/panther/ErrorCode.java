package com.lezhin.panther;

/**
 * @author seoeun
 * @since 2017.11.04
 */
public enum ErrorCode {
    // FIXME 정상코드, ResponseInfo와 통합
    SPC_OK("00", "SPC_OK"),
    SPC_DENY_77("77", "SPC_DENY_77"),
    SPC_DENY_88("88", "SPC_DENY_88"),
    SPC_DENY_44("44", "SPC_DENY_44"),
    SPC_ERROR_80("80", "SPC_ERROR_80"),
    SPC_ERROR_92("92", "SPC_ERROR_92"),
    SPC_ERROR_22("22", "SPC_ERROR_22"),
    SPC_ERROR_99("99", "SPC_ERROR_99"),
    LGUPLUS_OK("0000", "OK"),
    LGUPLUS_ERROR("X0X0", "LGUPLUS_ERROR"),
    LEZHIN_OK("0000", "OK"),
    LEZHIN_UNKNOWN("1100", "UNKNOWN"),
    LEZHIN_PARAM("1001", "Param Error"),
    LEZHIN_PRECONDITION("1002", "Precondition Error"),
    LEZHIN_EXECUTION("1003", "Execution Error"),
    LEZHIN_PANTHER("1004", "Panther Error"),
    LEZHIN_INTERNAL_PAYMNENT("1005", "InternalPayment Error"),
    LEZHIN_EXCEED("1007", "Exceed throttle"),
    LEZHIN_CI("1008", "No ConnectionInfo"),
    LEZHIN_THROWABLE("9001", "Unexpected Error");

    private String code;
    private String message;

    ErrorCode(String code, String message) {
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
