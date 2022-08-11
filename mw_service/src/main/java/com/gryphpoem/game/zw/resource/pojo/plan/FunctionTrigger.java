package com.gryphpoem.game.zw.resource.pojo.plan;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-16 17:08
 */
public enum FunctionTrigger {
    DEFEAT_THE_ROBBER(new PlanFunction[]{PlanFunction.DRAW_CARD}),;

    private PlanFunction[] functions;

    public PlanFunction[] getFunctions() {
        return functions;
    }

    FunctionTrigger(PlanFunction[] functions) {
        this.functions = functions;
    }
}
