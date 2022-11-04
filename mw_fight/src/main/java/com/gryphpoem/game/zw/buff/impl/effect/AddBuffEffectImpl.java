package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 加buff
 * Author: zhangpeng
 * createTime: 2022-11-04 18:05
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class AddBuffEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.ADD_BUFF_TO_THE_EFFECT};
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
        return null;
    }

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(effectConfig_.get(1));
        if (CheckNull.isNull(buffObjective)) {
            LogUtil.error("effectConfig: ", effectConfig_, ", not found buffObjective");
            return;
        }
        Force executor = executorForce(fightBuff, contextHolder, effectConfig_, buffObjective);
        if (CheckNull.isNull(executor) || CheckNull.isEmpty(executor.effectExecutor)) {
            return;
        }

        StaticBuff staticBuff = DataResource.ac.getBean(StaticFightManager.class).getStaticBuff(effectConfig_.get(3));
        if (CheckNull.isNull(staticBuff)) {
            LogUtil.error("add buff, config: ", effectConfig, ", staticBuff not found");
            return;
        }

        BattleLogic battleLogic = DataResource.ac.getBean(BattleLogic.class);
        List<IFightBuff> removedList = new ArrayList<>();
        for (Integer heroId : executor.effectExecutor) {
            if (!RandomHelper.isHitRangeIn10000(effectConfig_.get(4))) {
                continue;
            }
            // 释放buff
            battleLogic.releaseBuff(executor.buffList(heroId), staticBuff, removedList, executor, heroId, contextHolder, null);
        }
    }
}
