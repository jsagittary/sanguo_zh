package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-07 15:42
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class MoraleChangeEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.MORALE_RECOVERY, FightConstant.EffectLogicId.MORALE_DEDUCTION};
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
        return 0;
    }

    @Override
    protected FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe) {
        return new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId(), effectConfig.subList(4, 6));
    }

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(effectConfig_.get(1));
        if (CheckNull.isNull(buffObjective)) {
            LogUtil.error("staticBuff: ", fightBuff.getBuffConfig(), ", effectConfig: ", effectConfig_, ", not found buffObjective");
            return;
        }
        Force force = executorForce(fightBuff, contextHolder, effectConfig, buffObjective);
        if (CheckNull.isNull(force)) {
            return;
        }
        if (!CheckNull.isEmpty(force.effectExecutor)) {
            for (Integer heroId : force.effectExecutor) {
                double originValue = (force.maxRoundMorale * effectConfig_.get(4) / FightConstant.TEN_THOUSAND) + effectConfig_.get(5);
                // 公式计算
                switch (rule.getEffectLogicId()) {
                    case FightConstant.EffectLogicId.MORALE_RECOVERY:
                        force.morale += FightCalc.moraleCorrection(force, heroId, FightConstant.EffectLogicId.MORALE_RECOVERY_VALUE_INCREASED,
                                FightConstant.EffectLogicId.MORALE_RECOVERY_VALUE_REDUCED, originValue);
                        // TODO pb

                        break;
                    case FightConstant.EffectLogicId.MORALE_DEDUCTION:
                        force.morale -= FightCalc.moraleCorrection(force, heroId, FightConstant.EffectLogicId.MORALE_DEDUCTION_VALUE_INCREASED,
                                FightConstant.EffectLogicId.REDUCED_MORALE_DEDUCTION, originValue);
                        if (force.morale < 0)
                            force.morale = 0;
                        // TODO PB
                        
                        break;
                }
            }
        }
    }
}
