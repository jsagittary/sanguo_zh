package com.gryphpoem.game.zw.resource.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.gryphpoem.game.zw.pb.CommonPb.RankEquip;
import com.gryphpoem.game.zw.pb.SerializePb.SerGlobalTrophy;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import io.netty.util.internal.ConcurrentSet;

/**
 * 全服成就
 */
public class Trophy { // key:vip等级,value:人数
    private Map<Integer, Integer> vipCnt = new ConcurrentHashMap<>();// 全服VIP
    private AtomicLong gold = new AtomicLong(0);// 全服充值
    private volatile int solarTermsStartTime;// 节气活动开启时间 0 表示未开启,会序列化
    private Map<Integer, List<Long>> rankEquips = new ConcurrentHashMap<>();// 装备打造排行 key:装备id value:上榜的角色id
    private Set<Integer> passMultCombat = new ConcurrentSet<>(); // 本服通过的多人副本id

    public Trophy() {
    }

    public Trophy(SerGlobalTrophy ser) {
        if (ser.hasSolarTermsStartTime()) {
            setSolarTermsStartTime(ser.getSolarTermsStartTime());
        }
        if (ser.hasGlod()) {
            getGold().set(ser.getGlod());
        }
        if (!CheckNull.isEmpty(ser.getRankEquipsList())) {
            for (RankEquip r : ser.getRankEquipsList()) {
                List<Long> list = new ArrayList<>();
                for (long v : r.getLorIdList()) {
                    list.add(v);
                }
                rankEquips.put(r.getQuality(), list);
            }
        }
        for (Integer combatId : ser.getPassMultCombatList()) {
            passMultCombat.add(combatId);
        }
    }

    public Map<Integer, Integer> getVipCnt() {
        return vipCnt;
    }

    public void setVipCnt(Map<Integer, Integer> vipCnt) {
        this.vipCnt = vipCnt;
    }

    public AtomicLong getGold() {
        return gold;
    }

    public void setGold(AtomicLong gold) {
        this.gold = gold;
    }

    public int getSolarTermsStartTime() {
        return solarTermsStartTime;
    }

    public void setSolarTermsStartTime(int solarTermsStartTime) {
        this.solarTermsStartTime = solarTermsStartTime;
    }

    public Map<Integer, List<Long>> getRankEquips() {
        return rankEquips;
    }

    public void setRankEquips(Map<Integer, List<Long>> rankEquips) {
        this.rankEquips = rankEquips;
    }

    public Set<Integer> getPassMultCombat() {
        return passMultCombat;
    }
}
