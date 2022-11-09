package com.gryphpoem.game.zw.skill.abs;

import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSkill;
import com.gryphpoem.game.zw.skill.IHeroSkill;
import com.gryphpoem.game.zw.util.FightUtil;

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
     * @param contextHolder
     * @param params
     */
    @Override
    public void releaseSkill(FightContextHolder contextHolder, Object... params) {
        // 释放技能主体效果之前
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.SKILL_BEFORE);
        // 释放技能主体效果
        releaseSkillEffect(contextHolder, params);
        // 释放技能buff
        releaseSkillBuff(contextHolder, params);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.SKILL_AFTER);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_CASTING_THE_SPECIFIED_SKILL_GROUP, s_skill);
    }
}
