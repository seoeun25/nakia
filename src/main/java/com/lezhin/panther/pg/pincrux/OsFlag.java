package com.lezhin.panther.pg.pincrux;

/**
 * @author seoeun
 * @since 2018.03.23
 */
public enum OsFlag {
    ALL(0), // 모든 os에 노출 가능.
    ANDROID(1),
    IOS(2);

    private int flag;

    OsFlag(int flag) {
        this.flag = flag;
    }

    public int flag() {
        return flag;
    }
}
