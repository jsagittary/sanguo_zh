package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;

import java.util.List;

public class StaticCombat {
    private int combatId;
    private int sectionId;
    private int exp;
    private List<List<Integer>> winAward;
    private List<List<Integer>> firstAward;
    private List<Integer> firstform; // 首次攻打的阵型
    private List<Integer> form;
    private List<Integer> hero;
    private List<List<Integer>> attr;
    private int preId;
    private int cnt;// 挑战次数
    private int type;// 1普通副本,2资源副本,3国器碎片副本,4招募副本,5资源建筑图纸副本,6装备图纸副本,7建筑副本
    private List<Integer> extand;// 高级副本属性 根据每个
    private int cost;// 消耗体力
    private int index;
    private int totalArm = -1;
    private int planeExp;       // 关卡的战机经验
    /**
     * 掉落红色勋章升级的物品
     */
    private List<List<Integer>> titanDrop;

    /**
     * 获取城池NPC总兵力
     * 
     * @return
     */
    public int getTotalArm() {
        if (totalArm < 0) {
            totalArm = 0;
            for (Integer npcId : form) {
                StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
                totalArm += npc.getTotalArm();
            }
        }
        return totalArm;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public int getCombatId() {
        return combatId;
    }

    public void setCombatId(int combatId) {
        this.combatId = combatId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public List<List<Integer>> getWinAward() {
        return winAward;
    }

    public void setWinAward(List<List<Integer>> winAward) {
        this.winAward = winAward;
    }

    public List<List<Integer>> getFirstAward() {
        return firstAward;
    }

    public void setFirstAward(List<List<Integer>> firstAward) {
        this.firstAward = firstAward;
    }

    public List<Integer> getForm() {
        return form;
    }

    public void setForm(List<Integer> form) {
        this.form = form;
    }

    public List<List<Integer>> getAttr() {
        return attr;
    }

    public void setAttr(List<List<Integer>> attr) {
        this.attr = attr;
    }

    public int getPreId() {
        return preId;
    }

    public void setPreId(int preId) {
        this.preId = preId;
    }

    public List<Integer> getHero() {
        return hero;
    }

    public void setHero(List<Integer> hero) {
        this.hero = hero;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Integer> getExtand() {
        return extand;
    }

    public void setExtand(List<Integer> extand) {
        this.extand = extand;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Integer> getFirstform() {
        return firstform;
    }

    public void setFirstform(List<Integer> firstform) {
        this.firstform = firstform;
    }

    public int getPlaneExp() {
        return planeExp;
    }

    public void setPlaneExp(int planeExp) {
        this.planeExp = planeExp;
    }

    public List<List<Integer>> getTitanDrop() {
        return titanDrop;
    }

    public void setTitanDrop(List<List<Integer>> titanDrop) {
        this.titanDrop = titanDrop;
    }
}
