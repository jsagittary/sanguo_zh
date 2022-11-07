package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;

/**
 * Description: 技能伤害效果 填表格式：[[执行者,执行对象,效果ID1,0,万分比,固定值],[执行者,执行对象,效果ID2,0,万分比,固定值]…]
 * Author: zhangpeng
 * createTime: 2022-10-28 10:13
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class SkillDamageFightEffectImpl extends AbsFightEffect {

    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.SKILL_DAMAGE};
    }

    @Override
    public IFightBuff compareTo(List sameIdBuffList, List effectConfig, FightBuffEffect fightBuffEffect, FightContextHolder contextHolder) {
        return null;
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
        return new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId());
    }

    @Override
    public Object effectCalculateValue(FightBuffEffect fightBuffEffect, int effectLogicId, Object... params) {
        return null;
    }

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        FightConstant.BuffObjective defBuffObjective = FightConstant.BuffObjective.convertTo(effectConfig_.get(1));
        FightConstant.BuffObjective atkBuffObjective = FightConstant.BuffObjective.convertTo(effectConfig_.get(0));
        if (CheckNull.isNull(defBuffObjective) || CheckNull.isNull(atkBuffObjective)) {
            LogUtil.error("effectConfig: ", effectConfig_, ", not found buffObjective");
            return;
        }

        Force atk = executorForce(fightBuff, contextHolder, effectConfig_, atkBuffObjective);
        Force def = beExecutorForce(fightBuff, contextHolder, effectConfig_, defBuffObjective);
        if (CheckNull.isNull(def) || CheckNull.isNull(atk)) {
            return;
        }

        if (!CheckNull.isEmpty(def.beEffectExecutor) && !CheckNull.isEmpty(atk.effectExecutor)) {
            BattleLogic battleLogic = DataResource.ac.getBean(BattleLogic.class);
            contextHolder.resetForce(atk, def);
            for (Integer atkHeroId : atk.effectExecutor) {
                atk.actionId = atkHeroId;
                for (Integer heroId : def.beEffectExecutor) {
                    battleLogic.skillAttack(atk, def, contextHolder, heroId, effectConfig_, contextHolder.getBattleType());
                }
            }
        }
    }
}
