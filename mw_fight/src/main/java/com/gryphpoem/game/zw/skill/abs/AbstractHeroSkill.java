package com.gryphpoem.game.zw.skill.abs;

import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.skill.IHeroSkill;

import java.util.HashMap;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-20 16:00
 */
public abstract class AbstractHeroSkill<SkillConfig> implements IHeroSkill {
    /**
     * 技能配置
     */
    protected StaticHeroSkill s_skill;
    /**
     * 是否可以释放技能 (作为开场技的切换)
     */
    protected boolean release = true;
    /**
     * 是否有触发
     */
    protected boolean roll;
    /**
     * 临时存储变量
     */
    private HashMap<Integer, Integer> param = new HashMap<>();

    public AbstractHeroSkill(StaticHeroSkill s_skill) {
        this.s_skill = s_skill;
    }

    public StaticHeroSkill getS_skill() {
        return s_skill;
    }

    public void setS_skill(StaticHeroSkill s_skill) {
        this.s_skill = s_skill;
    }

    public boolean isRelease() {
        return release;
    }

    public void setRelease(boolean release) {
        this.release = release;
    }

    public boolean isRoll() {
        return roll;
    }

    public void setRoll(boolean roll) {
        this.roll = roll;
    }

    public HashMap<Integer, Integer> getParam() {
        return param;
    }

    public void setParam(HashMap<Integer, Integer> param) {
        this.param = param;
    }

    /**
     * 释放技能
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param params
     */
    public void releaseSkill(Force attacker, Force defender, FightLogic fightLogic, Object... params) {
        if (releaseDamageFirst()) {
            releaseDamage(attacker, defender, fightLogic, params);
            releaseBuff(attacker, defender, fightLogic, params);
        } else {
            releaseBuff(attacker, defender, fightLogic, params);
            releaseDamage(attacker, defender, fightLogic, params);
        }
    }

    /**
     * 释放伤害与buff的先后顺序
     *
     * @return
     */
    protected abstract boolean releaseDamageFirst();
}
