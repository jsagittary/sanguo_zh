package com.gryphpoem.game.zw.resource.pojo.plan;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-15 19:04
 */
public enum PlanFunction {

    DRAW_CARD(3001),;

    private int functionId;

    public int getFunctionId() {
        return functionId;
    }

    PlanFunction(int functionId) {
        this.functionId = functionId;
    }

    public static PlanFunction convertTo(int functionId) {
        for (PlanFunction function : values()) {
            if (function.getFunctionId() == functionId)
                return function;
        }
        return null;
    }

    /**
     * 计划状态
     */
    public enum PlanStatus {
        NOT_START(),
        PREVIEW(),
        OPEN(),
        DISPLAY(),
        OVER(),;
    }
}
