package com.gryphpoem.game.zw.skill.abs;

import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.skill.IHeroSkill;
import com.gryphpoem.push.util.CheckNull;

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
    /**
     * 是否是副将技能
     */
    private boolean assistantHeroSkill;

    public AbstractHeroSkill(StaticHeroSkill s_skill) {
        this.s_skill = s_skill;
    }

    public AbstractHeroSkill(StaticHeroSkill s_skill, boolean assistantHeroSkill) {
        this.s_skill = s_skill;
        this.assistantHeroSkill = assistantHeroSkill;
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

    public boolean isAssistantHeroSkill() {
        return assistantHeroSkill;
    }

    public void setAssistantHeroSkill(boolean assistantHeroSkill) {
        this.assistantHeroSkill = assistantHeroSkill;
    }

    /**
     * 释放技能
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param staticHeroSkill
     * @param fightResult
     * @param params
     */
    @Override
    public void releaseSkill(Force attacker, Force defender, FightLogic fightLogic, StaticHeroSkill staticHeroSkill, FightResult fightResult, Object... params) {
        releaseSkillBuff(attacker, defender, fightLogic, staticHeroSkill, fightResult, params);
        releaseSkillEffect(attacker, defender, fightLogic, staticHeroSkill, fightResult, params);
        // 触发技能后buff释放
        if (!CheckNull.isEmpty(attacker.buffList)) {
            attacker.buffList.values().forEach(list -> {
                list.values().forEach(fightBuffList -> {
                    if (CheckNull.isEmpty(fightBuffList)) return;
                    fightBuffList.forEach(fightBuff -> {
                        fightBuff.releaseEffect(attacker, fightLogic, fightResult, FightConstant.BuffEffectTiming.SKILL_AFTER);
                    });
                });
            });
        }

        if (!CheckNull.isEmpty(defender.buffList)) {
            defender.buffList.values().forEach(list -> {
                list.values().forEach(fightBuffList -> {
                    if (CheckNull.isEmpty(fightBuffList)) return;
                    fightBuffList.forEach(fightBuff -> {
                        fightBuff.releaseEffect(defender, fightLogic, fightResult, FightConstant.BuffEffectTiming.SKILL_AFTER);
                    });
                });
            });
        }
    }
}
