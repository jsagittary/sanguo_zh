package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.timing.AbsFightEffectWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.pojo.p.ActionDirection;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-27 9:54
 */
public class DefenderEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.BEFORE_BEING_ATTACKED, FightConstant.BuffEffectTiming.AFTER_BEING_ATTACKED,
                FightConstant.BuffEffectTiming.BEFORE_SKILL_DAMAGE, FightConstant.BuffEffectTiming.AFTER_SKILL_DAMAGE,
                FightConstant.BuffEffectTiming.BEFORE_BEING_HIT, FightConstant.BuffEffectTiming.AFTER_BEING_HIT,
                FightConstant.BuffEffectTiming.BEFORE_BLEEDING, FightConstant.BuffEffectTiming.AFTER_BLEEDING};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, Object... params) {
        // 0 代表任何人
        if (conditionConfig.get(0) == 0) return true;
        FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(conditionConfig.get(0));
        if (CheckNull.isNull(buffObjective)) return false;
        ActionDirection actionDirection = triggerForce(fightBuff, contextHolder, conditionConfig);
        if (CheckNull.isNull(actionDirection)) return false;
        // 无触发者
        if (CheckNull.isEmpty(actionDirection.getAtkHeroList()))
            return false;

        return canRelease(actionDirection, contextHolder.getDefHeroList(), buffObjective);
    }
}
