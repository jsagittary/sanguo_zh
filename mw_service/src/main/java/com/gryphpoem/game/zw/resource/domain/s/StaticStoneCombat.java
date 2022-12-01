package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * 宝石副本
 *
 */
public class StaticStoneCombat {
    private int combatId;
    private int type;// 1普通副本,2资源副本,3国器碎片副本,4招募副本,5资源建筑图纸副本,6装备图纸副本,7建筑副本
    private int sectionId;// 所属章节
    private int index;// 编号
    private int preId;
    private int exp;
    private int cnt;// 挑战次数 通关次数（0-不限制）
    private int cost;// 消耗
    private List<List<Integer>> winAward1;// 通关奖励必掉1种[[道具，个数下限，个数上限，权重],········]
    private List<List<Integer>> winAward2;// 通关奖励全随机[[道具，个数下限，个数上限，独立概率万分比],········]
    private List<List<Integer>> firstForm; // 首次攻打的阵型
    private List<List<Integer>> form;
    private List<Integer> hero;
    private List<List<Integer>> attr;
    private List<Integer> extand;// 高级副本属性 根据每个
    private int totalArm = -1;

    /**
     * 获取城池NPC总兵力
     * 
     * @return
     */
    public int getTotalArm() {
        if (totalArm < 0) {
            totalArm = 0;
            for (List<Integer> npcId : form) {
                if (CheckNull.isEmpty(npcId)) continue;
                StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId.get(0));
                totalArm += npc.getTotalArm();
            }
        }
        return totalArm;
    }

    public int getCombatId() {
        return combatId;
    }

    public void setCombatId(int combatId) {
        this.combatId = combatId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getPreId() {
        return preId;
    }

    public void setPreId(int preId) {
        this.preId = preId;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public List<List<Integer>> getWinAward1() {
        return winAward1;
    }

    public void setWinAward1(List<List<Integer>> winAward1) {
        this.winAward1 = winAward1;
    }

    public List<List<Integer>> getWinAward2() {
        return winAward2;
    }

    public void setWinAward2(List<List<Integer>> winAward2) {
        this.winAward2 = winAward2;
    }

    public List<List<Integer>> getFirstForm() {
        return firstForm;
    }

    public void setFirstForm(List<List<Integer>> firstForm) {
        this.firstForm = firstForm;
    }

    public List<List<Integer>> getForm() {
        return form;
    }

    public void setForm(List<List<Integer>> form) {
        this.form = form;
    }

    public List<Integer> getHero() {
        return hero;
    }

    public void setHero(List<Integer> hero) {
        this.hero = hero;
    }

    public List<List<Integer>> getAttr() {
        return attr;
    }

    public void setAttr(List<List<Integer>> attr) {
        this.attr = attr;
    }

    public List<Integer> getExtand() {
        return extand;
    }

    public void setExtand(List<Integer> extand) {
        this.extand = extand;
    }

}
