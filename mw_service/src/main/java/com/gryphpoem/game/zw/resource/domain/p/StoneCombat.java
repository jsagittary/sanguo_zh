package com.gryphpoem.game.zw.resource.domain.p;

/**
 * @ClassName StoneCombat.java
 * @Description 宝石副本
 * @author QiuKun
 * @date 2018年5月10日
 */
public class StoneCombat {
    private int combatId;// 关卡ID
    private int passCnt; // 已通关次数

    public StoneCombat(int combatId, int passCnt) {
        this.combatId = combatId;
        this.passCnt = passCnt;
    }

    public int getCombatId() {
        return combatId;
    }

    public void setCombatId(int combatId) {
        this.combatId = combatId;
    }

    public int getPassCnt() {
        return passCnt;
    }

    public void setPassCnt(int passCnt) {
        this.passCnt = passCnt;
    }

    public void addPassCnt(int cnt) {
        this.passCnt += cnt;
    }

    @Override
    public String toString() {
        return "StoneCombat [combatId=" + combatId + ", passCnt=" + passCnt + "]";
    }

}
