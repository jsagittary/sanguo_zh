package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-04-15 10:33
 */
public class StaticHeroSeasonSkill {
    private int heroId;
    private int skillId;
    private int skillLv;
    private int needHeroLv;
    private List<List<Integer>> upgradeCost;
    private int skillActionId;

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getSkillLv() {
        return skillLv;
    }

    public void setSkillLv(int skillLv) {
        this.skillLv = skillLv;
    }

    public List<List<Integer>> getUpgradeCost() {
        return upgradeCost;
    }

    public void setUpgradeCost(List<List<Integer>> upgradeCost) {
        this.upgradeCost = upgradeCost;
    }

    public int getSkillActionId() {
        return skillActionId;
    }

    public void setSkillActionId(int skillActionId) {
        this.skillActionId = skillActionId;
    }

    public int getNeedHeroLv() {
        return needHeroLv;
    }

    public void setNeedHeroLv(int needHeroLv) {
        this.needHeroLv = needHeroLv;
    }
}
