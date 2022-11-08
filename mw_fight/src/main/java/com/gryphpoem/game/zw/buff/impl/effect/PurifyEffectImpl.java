package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.push.util.CheckNull;

import java.util.Iterator;
import java.util.List;

/**
 * Description: 净化
 * Author: zhangpeng
 * createTime: 2022-11-04 17:02
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class PurifyEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.PURIFY};
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
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection) || CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            return;
        }

        Force executor = actionDirection.getDef();
        for (Integer heroId : actionDirection.getDefHeroList()) {
            List<IFightBuff> buffList = executor.buffList(heroId.intValue());
            if (CheckNull.isEmpty(buffList))
                continue;
            Iterator<IFightBuff> it = buffList.iterator();
            while (it.hasNext()) {
                IFightBuff buff = it.next();
                StaticBuff staticBuff;
                if (CheckNull.isNull(buff) || CheckNull.isNull(staticBuff = buff.getBuffConfig()))
                    continue;
                if (staticBuff.getTypeGrouping().contains(2) || staticBuff.getTypeGrouping().contains(3)) {
                    it.remove();
                    // TODO 战报记录

                }
            }
        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
    }
}
