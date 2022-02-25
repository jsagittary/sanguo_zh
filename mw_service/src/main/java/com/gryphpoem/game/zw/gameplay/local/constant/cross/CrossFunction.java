package com.gryphpoem.game.zw.gameplay.local.constant.cross;

public enum CrossFunction {
    /** 跨服战火燎原*/
    CROSS_WAR_FIRE(3001),;


    private int functionId;

    public int getFunctionId() {
        return functionId;
    }

    CrossFunction(int functionId) {
        this.functionId = functionId;
    }

    public static CrossFunction convertTo(int functionId) {
        for (CrossFunction function : values()) {
            if (function.getFunctionId() == functionId)
                return function;
        }

        return null;
    }
}
