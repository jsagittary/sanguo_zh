package com.gryphpoem.game.zw.resource.pojo.plan.draw;

import com.gryphpoem.game.zw.pb.ActivityPb;
import com.gryphpoem.game.zw.resource.pojo.FunctionPlan;
import com.gryphpoem.game.zw.resource.pojo.plan.FunctionPlanData;
import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-15 13:48
 */
@FunctionPlan(functions = PlanFunction.DRAW_CARD)
public class DrawCardTimeLimitedFunctionPlanData extends FunctionPlanData<ActivityPb.TimeLimitedDrawCardActData> {
    /** 进度下标*/
    private static final int PROGRESS_INDEX = -1000;
    /** 领取状态下标*/
    private static final int RECEIVED_STATUS_INDEX = -1001;
    /** 免费次数下标*/
    private static final int FREE_NUM_INDEX = -1002;

    /**
     * 存储任务完成进度
     */
    private Map<Integer, Integer> saveMap = new HashMap<>();

    public DrawCardTimeLimitedFunctionPlanData(int keyId) {
        super(keyId);
    }

    public Map<Integer, Integer> getSaveMap() {
        return saveMap;
    }

    public void setSaveMap(Map<Integer, Integer> saveMap) {
        this.saveMap = saveMap;
    }

    public int getProgress() {
        return saveMap.getOrDefault(PROGRESS_INDEX, 0);
    }

    public void operationProgress(int add) {
        saveMap.merge(PROGRESS_INDEX, add, Integer::sum);
    }

    public int getReceiveStatus() {
        return saveMap.getOrDefault(RECEIVED_STATUS_INDEX, CANNOT_RECEIVE_STATUS);
    }

    public void updateReceiveStatus(int status) {
        this.saveMap.put(RECEIVED_STATUS_INDEX, status);
    }

    public int getFreeNum() {
        return this.saveMap.getOrDefault(FREE_NUM_INDEX, 0);
    }

    public void operationFreeNum(int num) {
        this.saveMap.merge(FREE_NUM_INDEX, num, Integer::sum);
    }

    public void resetDaily() {
        this.saveMap.remove(PROGRESS_INDEX);
        this.saveMap.remove(RECEIVED_STATUS_INDEX);
    }

    @Override
    public PlanFunction[] functionId() {
        return new PlanFunction[]{PlanFunction.DRAW_CARD};
    }

    @Override
    public ActivityPb.TimeLimitedDrawCardActData createPb(boolean isSaveDb) {
        ActivityPb.TimeLimitedDrawCardActData.Builder builder = ActivityPb.TimeLimitedDrawCardActData.newBuilder();
        builder.setFunctionId(PlanFunction.DRAW_CARD.getFunctionId());
        builder.setExtNum(getProgress());
        builder.setKeyId(getKeyId());
        builder.setStatus(getReceiveStatus());
        builder.setFreeNum(getFreeNum());
        return builder.build();
    }
}
