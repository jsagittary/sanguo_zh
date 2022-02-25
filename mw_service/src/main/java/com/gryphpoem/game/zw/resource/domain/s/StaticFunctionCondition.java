package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @Description 功能解锁条件配置
 * @author TanDonghai
 * @date 创建时间：2017年6月7日 下午7:08:15
 *
 */
public class StaticFunctionCondition {
    private int conditionId;// 条件id
    private int unlockType;// 解锁类型，对应s_function_unlock_type_define表中的unlockType字段
    private String param;// 条件参数，格式：param1,param2...

    public int getConditionId() {
        return conditionId;
    }

    public void setConditionId(int conditionId) {
        this.conditionId = conditionId;
    }

    public int getUnlockType() {
        return unlockType;
    }

    public void setUnlockType(int unlockType) {
        this.unlockType = unlockType;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "StaticFunctionCondition [conditionId=" + conditionId + ", unlockType=" + unlockType + ", param=" + param
                + "]";
    }

}
