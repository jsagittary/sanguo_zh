package com.gryphpoem.game.zw.resource.domain.p;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName PitchCombat.java
 * @Description 挑战荣耀演副本
 * @author QiuKun
 * @date 2018年11月28日
 */
public class PitchCombat {
    private int type;
    private int highestCombatId;// 历史最副本id
    private int wipeCnt;// 今日已经扫荡次数
    private int todayCombatId; // 今日挑战的副本id
    private Map<Integer, Integer> buyCnt = new HashMap<>(); // 物品购买次数, key:物品id,val,购买次数
    private int combatPoint;// 副本点数 ,可以在副本商店换取东西
    private int lastRefreshDate; // 最近一次刷新的日期 存储的是 20181224这种,TimeHelper.getCurrentDay()

    public CommonPb.PitchCombat ser() {
        CommonPb.PitchCombat.Builder builder = CommonPb.PitchCombat.newBuilder();
        builder.setType(this.type);
        builder.setHighestCombatId(this.highestCombatId);
        builder.setWipeCnt(this.wipeCnt);
        builder.setTodayCombatId(this.todayCombatId);
        builder.setCombatPoint(this.combatPoint);
        builder.setLastRefreshDate(this.lastRefreshDate);
        for (Entry<Integer, Integer> kv : buyCnt.entrySet()) {
            builder.addBuyCnt(PbHelper.createTwoIntPb(kv.getKey(), kv.getValue()));
        }
        builder.setCombatPoint(this.getCombatPoint());
        return builder.build();
    }

    public void dser(CommonPb.PitchCombat ser) {
        this.highestCombatId = ser.getHighestCombatId();
        this.wipeCnt = ser.getWipeCnt();
        this.todayCombatId = ser.getTodayCombatId();
        this.combatPoint = ser.getCombatPoint();
        this.lastRefreshDate = ser.getLastRefreshDate();
        List<TwoInt> buyCntList = ser.getBuyCntList();
        if (!CheckNull.isEmpty(buyCntList)) {
            for (TwoInt ti : buyCntList) {
                buyCnt.put(ti.getV1(), ti.getV2());
            }
        }
    }

    /**
     * 刷新状态
     */
    public void refresh() {
        int currentDay = TimeHelper.getCurrentDay();
        if (this.lastRefreshDate != currentDay) {
            if (this.highestCombatId > 0) {
                this.wipeCnt = 0;
            }
            this.todayCombatId = 0;
            this.lastRefreshDate = currentDay;
        }
    }

    public void updateCombatId(int combatId) {
        this.todayCombatId = combatId;
        if (combatId > highestCombatId) {
            this.highestCombatId = combatId;
        }
    }

    public PitchCombat(int type) {
        this.type = type;
    }

    public void firstCreateInit() {
        this.wipeCnt = 1;
        setLastRefreshDate(TimeHelper.getCurrentDay());
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getHighestCombatId() {
        return highestCombatId;
    }

    public void setHighestCombatId(int highestCombatId) {
        this.highestCombatId = highestCombatId;
    }

    public int getWipeCnt() {
        return wipeCnt;
    }

    public void setWipeCnt(int wipeCnt) {
        this.wipeCnt = wipeCnt;
    }

    public int getTodayCombatId() {
        return todayCombatId;
    }

    // public void setTodayCombatId(int todayCombatId) {
    // this.todayCombatId = todayCombatId;
    // }

    public Map<Integer, Integer> getBuyCnt() {
        return buyCnt;
    }

    public void setBuyCnt(Map<Integer, Integer> buyCnt) {
        this.buyCnt = buyCnt;
    }

    public int getCombatPoint() {
        return combatPoint;
    }

    public void setCombatPoint(int combatPoint) {
        this.combatPoint = combatPoint;
    }

    public void addCombatPoint(int point) {
        if (point > 0) {
            this.combatPoint += point;
        }
    }

    public void subCombatPoint(int point) {
        if (point > 0) {
            this.combatPoint -= point;
            if (this.combatPoint < 0) {
                this.combatPoint = 0;
            }
        }
    }

    public void setLastRefreshDate(int lastRefreshDate) {
        this.lastRefreshDate = lastRefreshDate;
    }

}
