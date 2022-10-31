package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.timing.AbsFightEffectWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 攻击者出手与触发者匹配 buff生效
 * Author: zhangpeng
 * createTime: 2022-10-20 18:41
 */
public class AttackEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.SKILL_BEFORE, FightConstant.BuffEffectTiming.SKILL_AFTER,
                FightConstant.BuffEffectTiming.BEFORE_GENERAL_ATTACK, FightConstant.BuffEffectTiming.AFTER_GENERAL_ATTACK};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, Object... params) {
        if (CheckNull.isEmpty(conditionConfig)) return true;
        // 0 代表任何人
        if (conditionConfig.get(0) == 0) return true;
        FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(conditionConfig.get(0));
        if (CheckNull.isNull(buffObjective)) return false;

        Force triggerForce = triggerForce(fightBuff, contextHolder, conditionConfig, buffObjective);
        if (contextHolder.getContext().getAttacker().actionId == 0) {
            LogUtil.error("buffId: ", fightBuff.getBuffConfig().getBuffId(), ", attacker.actionId = 0");
            return false;
        }
        if (CheckNull.isEmpty(triggerForce.buffTriggerId)) {
            return false;
        }

        List<Integer> forceList = new ArrayList<>(1);
        forceList.add(contextHolder.getContext().getAttacker().actionId);
        return canRelease(triggerForce, forceList, buffObjective);
    }

}
