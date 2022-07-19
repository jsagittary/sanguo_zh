package com.gryphpoem.game.zw.resource.pojo.plan.draw;

import com.gryphpoem.game.zw.pb.ActivityPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.pojo.FunctionPlan;
import com.gryphpoem.game.zw.resource.pojo.plan.FunctionPlanData;
import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    /** 抽出英雄次数下标*/
    private static final int HERO_DRAW_COUNT_INDEX = -1003;
    /** 抽出英雄碎片次数下标*/
    private static final int HERO_FRAGMENT_DRAW_COUNT_INDEX = -1004;
    /** 总共抽卡次数*/
    private static final int TOTAL_HERO_DRAW_COUNT_INDEX = -1005;

    /**
     * 存储任务完成进度
     */
    private Map<Integer, Integer> saveMap = new HashMap<>();

    public DrawCardTimeLimitedFunctionPlanData(Integer keyId) {
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
        if (getReceiveStatus() == CAN_RECEIVE_STATUS) {
            operationFreeNum(HeroConstant.TIME_LIMITED_DRAW_DEFEATED_REBELS_NUM_AND_FREE_TIMES.get(1));
        }
        this.saveMap.remove(PROGRESS_INDEX);
        this.saveMap.remove(RECEIVED_STATUS_INDEX);
    }

    public int getHeroDrawCount() {
        return this.saveMap.getOrDefault(HERO_DRAW_COUNT_INDEX, 0);
    }

    public void clearHeroDrawCount() {
        this.saveMap.put(HERO_DRAW_COUNT_INDEX, 0);
    }

    public int getFragmentDrawCount() {
        return this.saveMap.getOrDefault(HERO_FRAGMENT_DRAW_COUNT_INDEX, 0);
    }

    public void clearFragmentDrawCount() {
        this.saveMap.put(HERO_FRAGMENT_DRAW_COUNT_INDEX, 0);
    }

    public void addHeroDrawCount() {
        this.saveMap.merge(HERO_DRAW_COUNT_INDEX, 1, Integer::sum);
    }

    public void addFragmentDrawCount() {
        this.saveMap.merge(HERO_FRAGMENT_DRAW_COUNT_INDEX, 1, Integer::sum);
    }

    public int getTotalDrawHeroCount() {
        return this.saveMap.getOrDefault(TOTAL_HERO_DRAW_COUNT_INDEX, 0);
    }

    public void addTotalDrawHeroCount() {
        this.saveMap.merge(TOTAL_HERO_DRAW_COUNT_INDEX, 1, Integer::sum);
    }

    public String toDebugStr() {
        return "[hero_fragment_draw_count:" + getFragmentDrawCount() + ", hero_draw_count:" + getHeroDrawCount() + "]";
    }

    @Override
    public PlanFunction[] functionId() {
        return new PlanFunction[]{PlanFunction.DRAW_CARD};
    }

    @Override
    public SerializePb.SerBaseFunctionPlanData createBasePb() {
        SerializePb.SerTimeLimitedDrawCardActData.Builder dataPb = SerializePb.SerTimeLimitedDrawCardActData.newBuilder();
        dataPb.addAllSaveData(this.saveMap.entrySet().stream().map(entry -> PbHelper.createTwoIntPb(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
        SerializePb.SerBaseFunctionPlanData.Builder builder = SerializePb.SerBaseFunctionPlanData.newBuilder();
        builder.setKeyId(keyId);
        builder.setFunctionId(PlanFunction.DRAW_CARD.getFunctionId());
        builder.setExtension(SerializePb.SerTimeLimitedDrawCardActData.ext, dataPb.build());
        return builder.build();
    }

    @Override
    public void deBaseFunctionPlanPb(SerializePb.SerBaseFunctionPlanData pb) {
        SerializePb.SerTimeLimitedDrawCardActData dataPb = pb.getExtension(SerializePb.SerTimeLimitedDrawCardActData.ext);
        if (CheckNull.isNull(dataPb))
            return;
        this.keyId = pb.getKeyId();
        if (CheckNull.nonEmpty(dataPb.getSaveDataList())) {
            dataPb.getSaveDataList().forEach(dataPbBase -> saveMap.put(dataPbBase.getV1(), dataPbBase.getV2()));
        }
    }

    @Override
    public ActivityPb.TimeLimitedDrawCardActData createPb(boolean isSaveDb) {
        ActivityPb.TimeLimitedDrawCardActData.Builder builder = ActivityPb.TimeLimitedDrawCardActData.newBuilder();
        builder.setHeroDrawCount(getHeroDrawCount());
        builder.setFunctionId(PlanFunction.DRAW_CARD.getFunctionId());
        builder.setExtNum(getProgress());
        builder.setKeyId(getKeyId());
        builder.setStatus(getReceiveStatus());
        builder.setFreeNum(getFreeNum());
        return builder.build();
    }
}
