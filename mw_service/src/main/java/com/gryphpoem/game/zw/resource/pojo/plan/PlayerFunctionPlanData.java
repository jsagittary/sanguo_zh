package com.gryphpoem.game.zw.resource.pojo.plan;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.manager.FunctionPlanDataManager;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.pojo.plan.constant.FunctionPlanConstant;
import com.gryphpoem.game.zw.resource.pojo.plan.draw.DrawCardFunctionData;
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
    /** plan其他信息*/
    private Map<PlanFunction, Map<Integer, Integer>> extDataMap = new HashMap<>();

    public PlayerFunctionPlanData() {
    }

    public PlayerFunctionPlanData(long roleId) {
        this.roleId = roleId;
    }

    public FunctionPlanData getData(int planKeyId) {
        return functionPlanDataMap.get(planKeyId);
    }

    public FunctionPlanData removeData(int planKeyId) {
        return this.functionPlanDataMap.remove(planKeyId);
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
            builder.getExtDataList().forEach(dataPb -> {
                Map<Integer, Integer> dataPb_ = extDataMap.computeIfAbsent(
                        PlanFunction.convertTo(dataPb.getFunction()), m -> new HashMap<>());
                if (CheckNull.nonEmpty(dataPb.getExtDataList())) {
                    dataPb.getExtDataList().forEach(d -> dataPb_.put(d.getV1(), d.getV2()));
                }
            });
        }
    }

    /**
     * 更新抽卡额外数据
     *
     * @param planFunction
     * @param index
     * @param value
     */
    public void updateExtData(PlanFunction planFunction, int index, int value) {
        this.extDataMap.computeIfAbsent(planFunction, map -> new HashMap<>()).merge(index, value, Integer::sum);
    }

    /**
     * 获取所有抽卡活动总抽卡数
     *
     * @return
     */
    public int getTotalDrawCount() {
        int total = 0;
        if (CheckNull.nonEmpty(this.functionPlanDataMap)) {
            for (FunctionPlanData data : this.functionPlanDataMap.values()) {
                if (CheckNull.isNull(data)) {
                    continue;
                }
                if (data instanceof DrawCardFunctionData == true) total += ((DrawCardFunctionData) data).getTotalDrawCount();
            }
        }

        Map<Integer, Integer> dataMap = extDataMap.get(PlanFunction.DRAW_CARD);
        if (CheckNull.nonEmpty(dataMap)) {
            total += dataMap.getOrDefault(FunctionPlanConstant.TOTAL_DRAW_CARD_COUNT, 0);
        }
        return total;
    }

    @Override
    public SerializePb.SerFunctionPlanData createPb(boolean isSaveDb) {
        SerializePb.SerFunctionPlanData.Builder builder = SerializePb.SerFunctionPlanData.newBuilder();
        this.functionPlanDataMap.values().forEach(data -> builder.addData(data.createBasePb()));
        if (CheckNull.nonEmpty(this.extDataMap)) {
            SerializePb.FunctionPlanExtData.Builder extBuilder = SerializePb.FunctionPlanExtData.newBuilder();
            this.extDataMap.forEach((function, map) -> {
                extBuilder.setFunction(function.getFunctionId());
                map.forEach((index, value) -> {
                    extBuilder.addExtData(PbHelper.createTwoIntPb(index, value));
                });
            });
            builder.addExtData(extBuilder.build());
            extBuilder.clear();
        }

        return builder.build();
    }
}
