package com.gryphpoem.game.zw.resource.domain.p;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.gryphpoem.game.zw.pb.SerializePb.SerCrossPersonalData;

/**
 * @ClassName CrossPersonalData.java
 * @Description 跨服个人数据
 * @author QiuKun
 * @date 2019年5月15日
 */
public class CrossPersonalData {

    /** 跨服最高杀敌数 */
    private int killNum;
    /** 本轮的杀敌数 */
    private int curKillNum;

    /** 已经领取的成就奖励的id */
    private final Set<Integer> gainTrophyId = new HashSet<>();
    /**
     * 是否在跨服里面
     */
    private boolean isInCross;

    // key:buffType
    private final Map<Integer, CrossBuff> buffs = new HashMap<>();
    // 复活将领的次数 <heroId,使用复活次数>
    private final Map<Integer, Integer> reiveHeroCnt = new HashMap<>();

    /**
     * 跨服结束清除数据
     */
    public void crossFinishClear() {
        this.reiveHeroCnt.clear();
        this.buffs.clear();
        this.curKillNum = 0;
        this.isInCross = false;
    }

    public SerCrossPersonalData toSerCrossPersonalDataPb() {
        SerCrossPersonalData.Builder builder = SerCrossPersonalData.newBuilder();
        builder.setKillNum(killNum);
        builder.addAllGainTrophyId(gainTrophyId);
        return builder.build();
    }

    public void dser(SerCrossPersonalData ser) {
        this.killNum = ser.getKillNum();
        this.gainTrophyId.addAll(ser.getGainTrophyIdList());
    }

    public Map<Integer, CrossBuff> getBuffs() {
        return buffs;
    }

    public boolean isInCross() {
        return isInCross;
    }

    public void enterCross() {
        isInCross = true;
    }

    public void exitCross() {
        isInCross = false;
    }

    public int getKillNum() {
        return killNum;
    }

    public void setKillNum(int killNum) {
        this.killNum = killNum;
    }

    public Set<Integer> getGainTrophyId() {
        return gainTrophyId;
    }

    public void addCurKillNum(int num) {
        if (num > 0) {
            this.curKillNum += num;
        }
        if (this.curKillNum > killNum) { // 超过最高杀敌数,进行替换
            killNum = this.curKillNum;
        }
    }

    public Map<Integer, Integer> getReiveHeroCnt() {
        return reiveHeroCnt;
    }

    public int getCurKillNum() {
        return curKillNum;
    }

    public void setInCross(boolean isInCross) {
        this.isInCross = isInCross;
    }

    @Override
    public String toString() {
        return "CrossPersonalData [killNum=" + killNum + ", curKillNum=" + curKillNum + ", gainTrophyId=" + gainTrophyId
                + ", isInCross=" + isInCross + ", buffs=" + buffs + ", reiveHeroCnt=" + reiveHeroCnt + "]";
    }

}
