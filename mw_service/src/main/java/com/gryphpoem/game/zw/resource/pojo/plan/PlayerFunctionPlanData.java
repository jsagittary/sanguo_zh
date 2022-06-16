package com.gryphpoem.game.zw.resource.pojo.plan;

import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-16 10:10
 */
public class PlayerFunctionPlanData {
    /** 玩家id*/
    private long roleId;
    /** 功能信息*/
    private Map<Integer, FunctionPlanData> functionPlanDataMap = new ConcurrentHashMap<>();

    public PlayerFunctionPlanData() {
    }

    public PlayerFunctionPlanData(long roleId) {
        this.roleId = roleId;
    }

    public FunctionPlanData getData(int planKeyId) {
        return functionPlanDataMap.get(planKeyId);
    }

    public void updateData(FunctionPlanData data) {
        if (CheckNull.isNull(data))
            return;
        functionPlanDataMap.put(data.getKeyId(), data);
    }
}
