package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.FightBuffEffect;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.FightEffectData;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;

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
//        List<Integer> effectConfig_ = effectConfig;
//        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig);
//        if (CheckNull.isNull(actionDirection)) {
//            return;
//        }
//        if (!CheckNull.isEmpty(actionDirection.getDefHeroList())) {
//            Force force = actionDirection.getDef();
//            for (Integer heroId : actionDirection.getDefHeroList()) {
//                double originValue = (force.maxRoundMorale * effectConfig_.get(4) / FightConstant.TEN_THOUSAND) + effectConfig_.get(5);
//                // 公式计算
//                switch (rule.getEffectLogicId()) {
//                    case FightConstant.EffectLogicId.MORALE_RECOVERY:
//                        int beforeRecoveredMorale = force.morale;
//                        int recoveredMorale = FightCalc.moraleCorrection(force, heroId, FightConstant.EffectLogicId.MORALE_RECOVERY_VALUE_INCREASED,
//                                FightConstant.EffectLogicId.MORALE_RECOVERY_VALUE_REDUCED, originValue);
//                        force.morale += recoveredMorale;
//                        if (force.morale > force.maxRoundMorale)
//                            force.morale = force.maxRoundMorale;
//                        LogUtil.fight("执行士气恢复效果, 士气恢复方: ", actionDirection.getDef().ownerId,
//                                ", 武将: ", heroId, ", 恢复的士气: ", recoveredMorale,
//                                ", 恢复前士气: ", beforeRecoveredMorale, ", 恢复后士气: ", force.morale);
//                        // TODO pb
//
//                        break;
//                    case FightConstant.EffectLogicId.MORALE_DEDUCTION:
//                        int beforeReducedMorale = force.morale;
//                        int reducedMorale = FightCalc.moraleCorrection(force, heroId, FightConstant.EffectLogicId.MORALE_DEDUCTION_VALUE_INCREASED,
//                                FightConstant.EffectLogicId.REDUCED_MORALE_DEDUCTION, originValue);
//                        force.morale -= reducedMorale;
//                        if (force.morale < 0)
//                            force.morale = 0;
//                        LogUtil.fight("执行士气扣除效果, 士气扣除方: ", actionDirection.getDef().ownerId,
//                                ", 武将: ", heroId, ", 扣除的士气: ", reducedMorale,
//                                ", 扣除前士气: ", beforeReducedMorale, ", 扣除后士气: ", force.morale);
//                        // TODO PB
//
//                        break;
//                }
//            }
//        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
    }
}
