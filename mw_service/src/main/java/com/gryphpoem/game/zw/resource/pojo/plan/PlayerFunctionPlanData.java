package com.gryphpoem.game.zw.resource.pojo.plan;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.manager.FunctionPlanDataManager;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
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
    /** 功能信息额外参数*/
    private Map<Integer, Integer> extDataMap = new HashMap<>();

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

    public Map<Integer, Integer> getExtDataMap() {
        return extDataMap;
    }

    public void setExtDataMap(Map<Integer, Integer> extDataMap) {
        this.extDataMap = extDataMap;
    }

    public int getExtData(int index) {
        return this.extDataMap.getOrDefault(index, 0);
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
        if (CheckNull.nonEmpty(builder.getExtDataList())) {
            builder.getExtDataList().forEach(data -> this.extDataMap.put(data.getV1(), data.getV2()));
        }
    }

    @Override
    public SerializePb.SerFunctionPlanData createPb(boolean isSaveDb) {
        SerializePb.SerFunctionPlanData.Builder builder = SerializePb.SerFunctionPlanData.newBuilder();
        this.functionPlanDataMap.values().forEach(data -> builder.addData(data.createBasePb()));
        this.extDataMap.entrySet().forEach(entry -> builder.addExtData(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue())));
        return builder.build();
    }
}
