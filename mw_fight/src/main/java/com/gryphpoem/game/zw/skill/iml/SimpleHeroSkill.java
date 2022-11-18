package com.gryphpoem.game.zw.skill.iml;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.ActionDirection;
import com.gryphpoem.game.zw.pojo.p.BattleLogic;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSkill;
import com.gryphpoem.game.zw.skill.abs.AbstractHeroSkill;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;

/**
 * Description: 通用技能实现
 * Author: zhangpeng
 * createTime: 2022-10-20 17:08
 */
public class SimpleHeroSkill extends AbstractHeroSkill {

    /**
     * 技能唯一id
     */
    private long skillKeyId;
    /**
     * 技能当前能量值
     */
    private int curEnergy;
    /**
     * 是否是登场技能
     */
    private boolean isOnStageSkill = false;
    /**
     * 技能伤害
     */
    private int skillDamage;
    /**
     * 技能释放次数
     */
    private int releaseCount;

    public SimpleHeroSkill(StaticHeroSkill s_skill) {
        super(s_skill);
        this.skillKeyId = FightUtil.uniqueId();
        this.curEnergy = s_skill.getDebutEnergy();
    }

    public SimpleHeroSkill(StaticHeroSkill s_skill, boolean isOnStageSkill) {
        super(s_skill);
        this.isOnStageSkill = isOnStageSkill;
        this.curEnergy = s_skill.getDebutEnergy();
    }

    @Override
    public boolean isOnStageSkill() {
        return isOnStageSkill;
    }

    public int getCurEnergy() {
        return curEnergy;
    }

    public void setCurEnergy(int curEnergy) {
        if (curEnergy < 0) {
            this.curEnergy = 0;
            return;
        }
        if (curEnergy > s_skill.getEnergyUpperLimit()) {
            this.curEnergy = s_skill.getEnergyUpperLimit();
            return;
        }
        this.curEnergy = curEnergy;
    }

    @Override
    public void releaseSkillEffect(FightContextHolder contextHolder, Object... params) {
        if (CheckNull.isEmpty(s_skill.getSkillEffect())) {
            LogUtil.fight("进攻方: ", contextHolder.getCurAttacker().ownerId, "-", contextHolder.getCurAtkHeroId(), ", s_skill: ", s_skill, ", 技能主体效果为空");
            return;
        }

        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        for (List<Integer> list : s_skill.getSkillEffect()) {
            if (CheckNull.isEmpty(list)) continue;
            StaticEffectRule rule = StaticFightManager.getStaticEffectRule(list.get(2));
            if (CheckNull.isNull(rule)) continue;
            IFightEffect fightEffect = fightManager.getSkillEffect(rule.getEffectLogicId());
            if (CheckNull.isNull(fightEffect)) continue;
            LogUtil.fight("进攻方: ", contextHolder.getCurAttacker().ownerId, "-", contextHolder.getCurAtkHeroId(), isOnStageSkill ? " 释放开场技能: " : "释放主动技能: ",
                    s_skill.getSkillId(), ", 技能主体效果: ", CheckNull.isEmpty(list) ? "null" : Arrays.toString(list.toArray()));
            fightEffect.effectiveness(null, contextHolder, list, rule, FightConstant.BuffEffectTiming.ACTIVE_RELEASE);
        }
    }

    @Override
    public void releaseSkillBuff(FightContextHolder contextHolder, Object... params) {
        if (CheckNull.isNull(s_skill)) {
            // 技能配置为空
            LogUtil.error("skill config is null, activeBuffImpl");
            return;
        }

        // 添加技能的buff
        if (!CheckNull.isEmpty(s_skill.getBuff())) {
            ActionDirection actionDirection = new ActionDirection();
            // 设置当前释放技能武将
            actionDirection.setAtk(contextHolder.getCurAttacker());
            actionDirection.setCurAtkHeroId(contextHolder.getCurAtkHeroId());
            actionDirection.setSkill(this);
            LinkedList<IFightBuff> removeBuffList = new LinkedList<>();
            BattleLogic battleLogic = DataResource.ac.getBean(BattleLogic.class);

            // 释放buff
            for (List<Integer> buffConfig : s_skill.getBuff()) {
                // 概率释放
                if (!RandomHelper.isHitRangeIn10000(buffConfig.get(2)))
                    continue;
                FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(buffConfig.get(0));
                if (CheckNull.isNull(buffObjective))
                    continue;
                StaticBuff staticBuff = StaticFightManager.getStaticBuff(buffConfig.get(1));
                if (CheckNull.isNull(staticBuff))
                    continue;

                actionDirection.clearDef();
                FightUtil.releaseBuffSet(contextHolder, buffObjective, actionDirection, false);
                Map<Integer, LinkedList<IFightBuff>> buffMap = FightUtil.actingForceBuff(actionDirection);
                if (CheckNull.isNull(buffMap)) {
                    continue;
                }

                for (Map.Entry<Integer, LinkedList<IFightBuff>> buffsEntry : buffMap.entrySet()) {
                    LinkedList<IFightBuff> buffs = buffsEntry.getValue();
                    actionDirection.setCurDefHeroId(buffsEntry.getKey());
                    // 释放buff
                    battleLogic.releaseBuff(buffs, staticBuff, removeBuffList, actionDirection, contextHolder, buffConfig, this);
                }
            }
        }
    }

    @Override
    public long uniqueId() {
        return this.skillKeyId;
    }

    @Override
    public void releaseSkill(FightContextHolder contextHolder, Object... params) {
        // 初始化技能pb
        contextHolder.getActionDirection().setSkill(this);
        contextHolder.getCurSkillActionPb().setSkillId(this.s_skill.getSkillId());
        super.releaseSkill(contextHolder, params);
        if (!this.isOnStageSkill) {
            contextHolder.getCurSkillActionPb().
                    setEnergyConsumed(this.s_skill.getReleaseNeedEnergy());
        }
        this.curEnergy = this.curEnergy - this.s_skill.getReleaseNeedEnergy();
        // 将当前技能pb添加进stage中
        if (this.isOnStageSkill) {
            // 登场技能
            contextHolder.getCurPreparationStagePb().addSkill(contextHolder.getCurSkillActionPb().build());
        } else {
            contextHolder.getRoundActionPb().setSkill(contextHolder.getCurSkillActionPb().build());
        }

        this.setReleaseCount(this.getReleaseCount() + 1);
        contextHolder.getActionDirection().setSkill(null);
    }

    @Override
    public int getSkillDamage() {
        return this.skillDamage;
    }

    @Override
    public void addSkillDamage(int damage) {
        this.skillDamage += damage;
    }

    public int getReleaseCount() {
        return releaseCount;
    }

    public void setReleaseCount(int releaseCount) {
        this.releaseCount = releaseCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleHeroSkill that = (SimpleHeroSkill) o;
        return skillKeyId == that.skillKeyId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillKeyId);
    }
}
