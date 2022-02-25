package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @Description 功能解锁配置
 * @author TanDonghai
 * @date 创建时间：2017年6月7日 下午7:06:42
 *
 */
public class StaticFunctionUnlock {
    private int functionId;// 功能id
    private List<Integer> condition;// 解锁条件，格式:[conditionId1,conditionId2...],conditionId对应s_function_condition表中的conditionId

    public int getFunctionId() {
        return functionId;
    }

    public void setFunctionId(int functionId) {
        this.functionId = functionId;
    }

    public List<Integer> getCondition() {
        return condition;
    }

    public void setCondition(List<Integer> condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "StaticFunctionUnlock [functionId=" + functionId + ", condition=" + condition + "]";
    }
}
