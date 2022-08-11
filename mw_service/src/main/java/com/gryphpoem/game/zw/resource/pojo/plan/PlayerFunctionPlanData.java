package com.gryphpoem.game.zw.resource.pojo.plan;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.manager.FunctionPlanDataManager;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-16 10:10
 */
public class PlayerFunctionPlanData implements GamePb<SerializePb.SerFunctionPlanData> {
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

    public void removeData(int planKeyId) {
        this.functionPlanDataMap.remove(planKeyId);
    }

    public FunctionPlanData updateData(FunctionPlanData data) {
        if (CheckNull.isNull(data))
            return null;
        functionPlanDataMap.put(data.getKeyId(), data);
        return data;
    }

    public void dePlanFunctionPb(SerializePb.SerFunctionPlanData builder) {
        if (CheckNull.nonEmpty(builder.getDataList())) {
            builder.getDataList().forEach(dataPb -> {
                FunctionPlanData functionPlanData = functionPlanDataMap.computeIfAbsent(dataPb.getKeyId(), data -> {
                    PlanFunction planFunction = PlanFunction.convertTo(dataPb.getFunctionId());
                    if (CheckNull.isNull(planFunction))
                        return null;
                    return DataResource.ac.getBean(FunctionPlanDataManager.class).newFunctionPlanData(planFunction, dataPb.getKeyId());
                });
                if (Objects.nonNull(functionPlanData)) {
                    functionPlanData.deBaseFunctionPlanPb(dataPb);
                    this.functionPlanDataMap.put(dataPb.getKeyId(), functionPlanData);
                }
            });
        }
    }

    @Override
    public SerializePb.SerFunctionPlanData createPb(boolean isSaveDb) {
        SerializePb.SerFunctionPlanData.Builder builder = SerializePb.SerFunctionPlanData.newBuilder();
        this.functionPlanDataMap.values().forEach(data -> builder.addData(data.createBasePb()));
        return builder.build();
    }
}
