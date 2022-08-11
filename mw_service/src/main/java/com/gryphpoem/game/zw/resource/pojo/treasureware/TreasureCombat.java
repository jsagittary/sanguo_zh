package com.gryphpoem.game.zw.resource.pojo.treasureware;


import com.gryphpoem.game.zw.dataMgr.StaticTreasureWareDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 宝具副本
 *
 * @description:
 * @author: zhou jie
 * @time: 2021/11/16 11:20
 */
public class TreasureCombat {

    /**
     * 通过的副本id, 快速挂机和挂机功能奖励结算都需要用到
     */
    private int curCombatId;
    /**
     * key: combatId, value: 暂无用处
     */
    private Map<Integer, Integer> combatInfo = new HashMap<>();

    /**
     * 开启的章节id
     */
    private int sectionId;
    /**
     * 章节奖励领取记录
     */
    private Map<Integer, Integer> sectionStatus = new HashMap<>();
    /**
     * 宝具挂机
     */
    private TreasureOnHook onHook = new TreasureOnHook();
    /** 道具今日产出数量 <道具id, 已获得数量>, 用于处理有产出数量限制的道具 */
    private Map<Integer, Integer> randomPropNumMap = new HashMap<>();

    /**
     * 序列化
     */
    public CommonPb.TreasureCombatPb ser(boolean showC) {
        CommonPb.TreasureCombatPb.Builder builder = CommonPb.TreasureCombatPb.newBuilder();
        builder.setCombatId(curCombatId);
        if (!showC) {
            if (CheckNull.nonEmpty(combatInfo)) {
                builder.addAllCombatInfo(combatInfo.entrySet().stream().map(en -> PbHelper.createTwoIntPb(en.getKey(), en.getValue())).collect(Collectors.toList()));
            }
        }

        if (Objects.nonNull(onHook)) {
            builder.setOnHook(onHook.ser());
        }
        if (CheckNull.nonEmpty(sectionStatus)) {
            builder.addAllSectionStatus(sectionStatus.entrySet().stream().map(en -> PbHelper.createTwoIntPb(en.getKey(), en.getValue())).collect(Collectors.toList()));
        }
        builder.setSectionId(sectionId);

        if (CheckNull.nonEmpty(randomPropNumMap)) {
            builder.addAllRandomPropNum(randomPropNumMap.entrySet().stream().map(en -> PbHelper.createTwoIntPb(en.getKey(), en.getValue())).collect(Collectors.toList()));
        }
        return builder.build();
    }

    public void dSer(CommonPb.TreasureCombatPb ser) {
        this.curCombatId = ser.getCombatId();
        this.combatInfo.putAll(ser.getCombatInfoList().stream().collect(Collectors.toMap(CommonPb.TwoInt::getV1, CommonPb.TwoInt::getV2)));

        if (ser.hasOnHook()) {
            this.onHook.dSer(ser.getOnHook());
        }
        this.sectionStatus.putAll(ser.getSectionStatusList().stream().collect(Collectors.toMap(CommonPb.TwoInt::getV1, CommonPb.TwoInt::getV2)));
        this.sectionId = ser.getSectionId();
        this.randomPropNumMap.putAll(ser.getRandomPropNumList().stream().collect(Collectors.toMap(CommonPb.TwoInt::getV1, CommonPb.TwoInt::getV2)));
    }

    /**
     * 推进副本进度
     *
     * @param combat 副本id
     */
    public void promoteCombat(int combat) {
        if (combat > curCombatId) {
            if (curCombatId == 0) {
                // 首次通关副本, 开启挂机
                int startTime = onHook.getStartTime();
                if (startTime == 0) {
                    onHook.setStartTime(TimeHelper.getCurrentSecond());
                }
            }
            curCombatId = combat;
        }
    }

    public int getCurCombatId() {
        return curCombatId;
    }

    public void setCurCombatId(int curCombatId) {
        this.curCombatId = curCombatId;
    }

    public Map<Integer, Integer> getCombatInfo() {
        return combatInfo;
    }

    public TreasureOnHook getOnHook() {
        return onHook;
    }

    public Map<Integer, Integer> getSectionStatus() {
        return sectionStatus;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public Map<Integer, Integer> getRandomPropNumMap() {
        return randomPropNumMap;
    }

    /**
     * 副本挂机记录
     */
    public class TreasureOnHook {
        /**
         * 开始挂机时间
         */
        private int startTime;

        public TreasureOnHook() {
            this.startTime = 0;
        }

        public int getStartTime() {
            return startTime;
        }

        public void setStartTime(int startTime) {
            this.startTime = startTime;
        }

        public CommonPb.TreasureOnHookPb ser() {
            CommonPb.TreasureOnHookPb.Builder builder = CommonPb.TreasureOnHookPb.newBuilder();
            builder.setStartTime(startTime);
            return builder.build();
        }

        public void dSer(CommonPb.TreasureOnHookPb ser) {
            this.startTime = ser.getStartTime();
        }
    }

    public TreasureCombat() {
        this.sectionId = StaticTreasureWareDataMgr.getInitSectionId();
    }
}
