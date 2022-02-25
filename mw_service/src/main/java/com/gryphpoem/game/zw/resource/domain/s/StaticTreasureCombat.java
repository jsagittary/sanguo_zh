package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/11/16 14:29
 */
public class StaticTreasureCombat {

    private int combatId;
    private int sectionId;
    private List<List<Integer>> minuteAward;
    private List<List<Integer>> minuteRandomAward;
    private List<Integer> form;
    private List<List<Integer>> firstAward;
    private List<List<Integer>> sectionAward;
    private int preId;


    public int getCombatId() {
        return combatId;
    }

    public void setCombatId(int combatId) {
        this.combatId = combatId;
    }

    public List<List<Integer>> getMinuteAward() {
        return minuteAward;
    }

    public void setMinuteAward(List<List<Integer>> minuteAward) {
        this.minuteAward = minuteAward;
    }

    public List<List<Integer>> getMinuteRandomAward() {
        return minuteRandomAward;
    }

    public void setMinuteRandomAward(List<List<Integer>> minuteRandomAward) {
        this.minuteRandomAward = minuteRandomAward;
    }

    public List<Integer> getForm() {
        return form;
    }

    public void setForm(List<Integer> form) {
        this.form = form;
    }

    public List<List<Integer>> getFirstAward() {
        return firstAward;
    }

    public void setFirstAward(List<List<Integer>> firstAward) {
        this.firstAward = firstAward;
    }

    public List<List<Integer>> getSectionAward() {
        return sectionAward;
    }

    public void setSectionAward(List<List<Integer>> sectionAward) {
        this.sectionAward = sectionAward;
    }

    public int getPreId() {
        return preId;
    }

    public void setPreId(int preId) {
        this.preId = preId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }
}
