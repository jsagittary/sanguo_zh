package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.timing.AbsFightEffectWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.pojo.p.ActionDirection;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-12-09 0:06
 */
public class EffectExecuteEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.BEFORE_IMPLEMENTATION_OF_ANY_EFFECT_ID,
                FightConstant.BuffEffectTiming.AFTER_IMPLEMENTATION_OF_ANY_EFFECT_ID};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, Object... params) {
        if (ObjectUtils.isEmpty(params)) return false;
        Force actingForce;
        int actingHeroId;
        int effectId;
        if (params[0] instanceof Object[]) {
            Object[] objArr = (Object[]) params[0];
            actingForce = (Force) objArr[0];
            actingHeroId = (int) objArr[1];
            effectId = (int) objArr[2];
        } else {
            actingForce = (Force) params[0];
            actingHeroId = (int) params[1];
            effectId = (int) params[2];
        }
        if (CheckNull.isNull(actingForce) || actingHeroId == 0 || effectId != conditionConfig.get(2))
            return false;

        if (conditionConfig.get(1) > 0) {
            ActionDirection actionDirection = triggerForce(fightBuff, contextHolder, conditionConfig);
            if (CheckNull.isNull(actionDirection) || CheckNull.isNull(actionDirection.getAtk()) ||
                    CheckNull.isEmpty(actionDirection.getAtkHeroList())) return false;
            return actionDirection.getAtkHeroList().get(0) == actingHeroId && actionDirection.getAtk().ownerId == actingForce.ownerId;
        } else if (conditionConfig.get(1) == 0) {
            return true;
        }
        return false;
    }
}
