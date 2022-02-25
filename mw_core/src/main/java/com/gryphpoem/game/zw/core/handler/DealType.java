package com.gryphpoem.game.zw.core.handler;

public enum DealType {
    PUBLIC(0, "PUBLIC") {
    },
    MAIN(1, "MAIN") {
    },
    BUILD_QUE(2, "BUILD_QUE") {
    },
    TANK_QUE(3, "TANK_QUE") {
    },
    BACKGROUND(4, "BACKGROUND")// 执行耗时线程
    ;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private DealType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    private int code;
    private String name;
}
