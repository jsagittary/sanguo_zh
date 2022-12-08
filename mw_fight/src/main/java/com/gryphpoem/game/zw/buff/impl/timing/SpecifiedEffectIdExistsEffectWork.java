package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.timing.AbsFightEffectWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-12-08 23:39
 */
public class SpecifiedEffectIdExistsEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.SPECIFIED_EFFECT_ID_EXISTS};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, Object... params) {
        // 0 代表任何人
        ActionDirection actionDirection;
        boolean canRelease = false;
        StaticEffectRule staticData = StaticFightManager.getStaticEffectRule(conditionConfig.get(2));
        if (CheckNull.isNull(staticData))
            return false;

        if (conditionConfig.get(0) > 0) {
            FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(conditionConfig.get(0));
            if (CheckNull.isNull(buffObjective)) return false;
            actionDirection = triggerForce(fightBuff, contextHolder, conditionConfig);
            if (CheckNull.isNull(actionDirection) || CheckNull.isEmpty(actionDirection.getAtkHeroList())) {
                return false;
            }


            Force triggerForce = actionDirection.getAtk();
            switch (buffObjective) {
                case AT_LEAST_ONE_HERO_FROM_MY_SIDE:
                case AT_LEAST_ONE_HERO_FROM_ENEMY_SIDE:
                    for (Integer heroId : actionDirection.getAtkHeroList()) {
                        FightBuffEffect fightBuffEffect = triggerForce.getFightEffectMap(heroId);
                        if (CheckNull.isNull(fightBuffEffect) ||
                                CheckNull.isEmpty(fightBuffEffect.getEffectMap())) continue;
                        if (!fightBuffEffect.getEffectMap().containsKey(staticData.getEffectLogicId()))
                            continue;
                        Map<Integer, List<FightEffectData>> effectList = fightBuffEffect.getEffectMap().get(staticData.getEffectLogicId());
                        if (CheckNull.isEmpty(effectList))
                            continue;
                        if (effectList.containsKey(conditionConfig.get(2))) {
                            canRelease = true;
                            break;
                        }
                    }
                    break;
                default:
                    boolean notFoundOne = false;
                    for (Integer heroId : actionDirection.getAtkHeroList()) {
                        FightBuffEffect fightBuffEffect = triggerForce.getFightEffectMap(heroId);
                        if (CheckNull.isNull(fightBuffEffect) ||
                                CheckNull.isEmpty(fightBuffEffect.getEffectMap())) continue;
                        if (!fightBuffEffect.getEffectMap().containsKey(staticData.getEffectLogicId())) {
                            notFoundOne = true;
                            break;
                        }
                        Map<Integer, List<FightEffectData>> effectList = fightBuffEffect.getEffectMap().get(staticData.getEffectLogicId());
                        if (CheckNull.isEmpty(effectList)) {
                            notFoundOne = true;
                            break;
                        }
                        if (!effectList.containsKey(conditionConfig.get(2))) {
                            notFoundOne = true;
                            break;
                        }
                    }
                    if (!notFoundOne) canRelease = true;
                    break;
            }
        } else if (conditionConfig.get(0) == 0) {
            return canRelease0(contextHolder.getCurAttacker(), conditionConfig, staticData) ||
                    canRelease0(contextHolder.getCurDefender(), conditionConfig, staticData);
        }

        return canRelease;
    }

    /**
     * 校验能否释放无监控对象的效果
     *
     * @param force
     * @param conditionConfig
     * @param staticData
     * @return
     */
    private boolean canRelease0(Force force, List<Integer> conditionConfig, StaticEffectRule staticData) {
        FightBuffEffect fightBuffEffect = force.getFightEffectMap(force.id);
        if (!CheckNull.isNull(fightBuffEffect) &&
                !CheckNull.isEmpty(fightBuffEffect.getEffectMap())) {
            if (fightBuffEffect.getEffectMap().containsKey(staticData.getEffectLogicId())) {
                Map<Integer, List<FightEffectData>> effectList = fightBuffEffect.getEffectMap().get(staticData.getEffectLogicId());
                if (!CheckNull.isEmpty(effectList) && effectList.containsKey(conditionConfig.get(2))) {
                    return true;
                }
            }
        }

        if (!CheckNull.isEmpty(force.assistantHeroList)) {
            for (FightAssistantHero ass : force.assistantHeroList) {
                FightBuffEffect fightBuffEffect_ = force.getFightEffectMap(ass.getHeroId());
                if (!CheckNull.isNull(fightBuffEffect_) &&
                        !CheckNull.isEmpty(fightBuffEffect_.getEffectMap())) {
                    if (fightBuffEffect_.getEffectMap().containsKey(staticData.getEffectLogicId())) {
                        Map<Integer, List<FightEffectData>> effectList = fightBuffEffect_.getEffectMap().get(staticData.getEffectLogicId());
                        if (!CheckNull.isEmpty(effectList) && effectList.containsKey(conditionConfig.get(2))) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
