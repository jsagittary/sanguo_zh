package com.gryphpoem.game.zw.resource.domain.p;

import java.util.ArrayList;
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
 * @ClassName MultCombat.java
 * @Description 多人副本信息
 * @author QiuKun
 * @date 2018年12月25日
 */
public class MultCombat {
    private int teamId;// 队伍id, 0表示没有加入队伍,不会进行序列化
    private int chatCd;// 喊话的cd时间 时间的秒值,不存储

    private int highestCombatId;// 历史最副本id
    private int wipeCnt;// 今日已经扫荡次数
    private List<Integer> todayCombatId = new ArrayList<>(); // 今日挑战的副本id
    private Map<Integer, Integer> buyCnt = new HashMap<>(); // 物品购买次数, key:物品id,val,购买次数
    private int combatPoint;// 副本点数 ,可以在副本商店换取东西
    private int lastRefreshDate; // 最近一次刷新的日期 存储的是 20181224这种,TimeHelper.getCurrentDay()
    private int teamAwardCnt;// 今天协助奖励次数

    public CommonPb.MultCombat ser() {
        CommonPb.MultCombat.Builder builder = CommonPb.MultCombat.newBuilder();
        builder.setHighestCombatId(this.highestCombatId);
        builder.setWipeCnt(this.wipeCnt);
        for (Integer comabtId : todayCombatId) {
            builder.addTodayCombatId(comabtId);
        }
        builder.setCombatPoint(this.combatPoint);
        builder.setLastRefreshDate(this.lastRefreshDate);
        for (Entry<Integer, Integer> kv : buyCnt.entrySet()) {
            builder.addBuyCnt(PbHelper.createTwoIntPb(kv.getKey(), kv.getValue()));
        }
        builder.setCombatPoint(this.getCombatPoint());
        builder.setTeamAwardCnt(this.teamAwardCnt);
        return builder.build();
    }

    public void dser(CommonPb.MultCombat ser) {
        this.highestCombatId = ser.getHighestCombatId();
        this.wipeCnt = ser.getWipeCnt();
        List<Integer> todayCombatIdList = ser.getTodayCombatIdList();
        if (!CheckNull.isEmpty(todayCombatIdList)) {
            for (Integer combatId : todayCombatIdList) {
                this.todayCombatId.add(combatId);
            }
        }
        this.combatPoint = ser.getCombatPoint();
        this.lastRefreshDate = ser.getLastRefreshDate();
        List<TwoInt> buyCntList = ser.getBuyCntList();
        if (!CheckNull.isEmpty(buyCntList)) {
            for (TwoInt ti : buyCntList) {
                buyCnt.put(ti.getV1(), ti.getV2());
            }
        }
        if (ser.hasTeamAwardCnt()) {
            this.teamAwardCnt = ser.getTeamAwardCnt();
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
            this.todayCombatId.clear();
            this.lastRefreshDate = currentDay;
            this.teamAwardCnt = 0;
        }
    }

    public void updateCombatId(int combatId) {
        if (!todayCombatId.contains(combatId)) {
            todayCombatId.add(combatId);
        }
        if (combatId > highestCombatId) {
            this.highestCombatId = combatId;
        }
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

    public void firstCreateInit() {
        this.wipeCnt = 1;
        setLastRefreshDate(TimeHelper.getCurrentDay());
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
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

    public List<Integer> getTodayCombatId() {
        return todayCombatId;
    }

    public Map<Integer, Integer> getBuyCnt() {
        return buyCnt;
    }

    public int getCombatPoint() {
        return combatPoint;
    }

    public void setCombatPoint(int combatPoint) {
        this.combatPoint = combatPoint;
    }

    public int getLastRefreshDate() {
        return lastRefreshDate;
    }

    public void setLastRefreshDate(int lastRefreshDate) {
        this.lastRefreshDate = lastRefreshDate;
    }

    public int getChatCd() {
        return chatCd;
    }

    public void setChatCd(int chatCd) {
        this.chatCd = chatCd;
    }

    public int getTeamAwardCnt() {
        return teamAwardCnt;
    }

    public void setTeamAwardCnt(int teamAwardCnt) {
        this.teamAwardCnt = teamAwardCnt;
    }


}
