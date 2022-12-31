package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
import java.util.Arrays;
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
    protected FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe, Object... params) {
        return null;
    }

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, int timing, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection) || CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            return;
        }

        StaticBuff staticBuff = StaticFightManager.getStaticBuff(effectConfig_.get(3));
        if (CheckNull.isNull(staticBuff)) {
            LogUtil.error("add buff, config: ", CheckNull.isEmpty(effectConfig_) ? "" : Arrays.toString(effectConfig_.toArray()), ", staticBuff not found");
            return;
        }
        if (!RandomHelper.isHitRangeIn10000(effectConfig_.get(4))) {
            LogUtil.debug("添加buff随机值不够, 无法添加buff, buffConfig: ", fightBuff.getBuffConfig());
            return;
        }

        // buff的施与者是当前buff的施与者
        Force executor = actionDirection.getDef();
        BattleLogic battleLogic = DataResource.ac.getBean(BattleLogic.class);
        List<IFightBuff> removedList = new ArrayList<>();
        for (Integer heroId : actionDirection.getDefHeroList()) {
            // 释放buff
            LogUtil.fight("释放添加buff效果, 效果逻辑id: ", rule.getEffectLogicId(), "， 被加buff方: ", actionDirection.getDef().ownerId, ", 被加buff武将: ", heroId);
            battleLogic.releaseBuff(executor.buffList(heroId), staticBuff, removedList, actionDirection, contextHolder, null, fightBuff.getSkill(), fightBuff.getBuffGiver());
        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
        
    }
}
