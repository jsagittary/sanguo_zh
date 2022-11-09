package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: 能量变更
 * Author: zhangpeng
 * createTime: 2022-11-03 10:27
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class EnergyChangeEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.ENERGY_RECOVERY, FightConstant.EffectLogicId.ENERGY_DEDUCTION};
    }

    @Override
    public IFightBuff compareTo(List sameIdBuffList, List effectConfig, FightBuffEffect fightBuffEffect, FightContextHolder contextHolder) {
        return (IFightBuff) sameIdBuffList.get(0);
    }

    @Override
    protected boolean compareValue(Force actingForce, int actingHeroId, int effectLogicId, Object... params) {
        return false;
    }

    @Override
    protected double calValue(Force force, int heroId, int effectLogicId, Object... params) {
        return 0d;
    }

    @Override
    public Object effectCalculateValue(FightBuffEffect fightBuffEffect, int effectLogicId, Object... params) {
        return null;
    }

    @Override
    protected FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe) {
        return new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId(), effectConfig.subList(4, 6));
    }

    /**
     * 效果生效，技能能量直接加上去
     *
     * @param fightBuff
     * @param contextHolder
     * @param effectConfig
     * @param params
     */
    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig);
        if (CheckNull.isNull(actionDirection)) {
            return;
        }
        if (!CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            Force force = actionDirection.getDef();
            for (Integer heroId : actionDirection.getDefHeroList()) {
                List<SimpleHeroSkill> skillList = force.getSkillList(heroId.intValue());
                if (CheckNull.isEmpty(skillList)) continue;
                List<SimpleHeroSkill> activeSkillList = skillList.stream().filter(skill -> !skill.isOnStageSkill()).collect(Collectors.toList());
                if (CheckNull.isEmpty(activeSkillList)) continue;
                // 公式计算
                switch (rule.getEffectLogicId()) {
                    case FightConstant.EffectLogicId.ENERGY_RECOVERY:
                        activeSkillList.forEach(skill -> {
                            int beforeRecoveredEnergy = skill.getCurEnergy();
                            double originValue = ((skill.getS_skill().getEnergyUpperLimit()) * effectConfig_.get(4) / FightConstant.TEN_THOUSAND) + effectConfig_.get(5);
                            int recoveredEnergy = FightCalc.skillEnergyRecovery(force, heroId, originValue);
                            skill.setCurEnergy(skill.getCurEnergy() + recoveredEnergy);
                            LogUtil.fight("执行能量恢复效果, 能量恢复方: ", actionDirection.getDef().ownerId,
                                    ", 武将: ", heroId, ", 技能: ", skill.getS_skill().getSkillId(), ", 恢复的能量: ", recoveredEnergy,
                                    ", 恢复前能量: ", beforeRecoveredEnergy, ", 恢复后能量: ", skill.getCurEnergy());
                            // TODO pb修改变更

                        });
                        break;
                    case FightConstant.EffectLogicId.ENERGY_DEDUCTION:
                        activeSkillList.forEach(skill -> {
                            int beforeReducedEnergy = skill.getCurEnergy();
                            int reducedEnergy = FightCalc.skillEnergyDeduction(skill.getS_skill(), effectConfig_);
                            skill.setCurEnergy(skill.getCurEnergy() - reducedEnergy);
                            LogUtil.fight("执行能量减少效果, 能量减少方: ", actionDirection.getDef().ownerId,
                                    ", 武将: ", heroId, ", 技能: ", skill.getS_skill().getSkillId(), ", 减少的能量: ", reducedEnergy,
                                    ", 减少前前能量: ", beforeReducedEnergy, ", 减少后能量: ", skill.getCurEnergy());
                            // TODO pb修改变更

                        });
                        break;
                }
            }
        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
    }
}
