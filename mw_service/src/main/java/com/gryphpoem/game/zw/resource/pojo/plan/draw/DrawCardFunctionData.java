package com.gryphpoem.game.zw.resource.pojo.plan.draw;

import com.gryphpoem.game.zw.resource.pojo.plan.FunctionPlanData;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-12 11:40
 */
public abstract class DrawCardFunctionData<T> extends FunctionPlanData<T> {
    public DrawCardFunctionData(Integer keyId) {
        super(keyId);
    }

    public abstract int getTotalDrawCount();
}
