package com.lezhin.avengers.panther;

/**
 * @author seoeun
 * @since 2017.11.04
 */
public enum ErrorCode {

    SPC_DENY_77("77", "SPC_DENY_77"),
    SPC_DENY_88("88", "SPC_DENY_88"),
    SPC_DENY_44("44", "SPC_DENY_44"),
    SPC_ERROR_80("80", "SPC_ERROR_80"),
    SPC_ERROR_92("92", "SPC_ERROR_92"),
    SPC_ERROR_22("22", "SPC_ERROR_22"),
    SPC_ERROR_99("99", "SPC_ERROR_99"),
    LEZHIN_PARAM("1001", "Param Error"),
    LEZHIN_PRECONDITION("1002", "Precondition Error"),
    LEZHIN_EXECUTION("1003", "Execution Error"),
    LEZHIN_PANTHER("1004", "Panther Error"),
    LEZHIN_THROWABLE("1005", "Unexpected Error"),;


    private String code;
    private String message;
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

}
