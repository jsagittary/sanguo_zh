package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.timing.AbsFightEffectWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-27 17:36
 */
public class SpecifiedBuffIdDisappearsEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.SPECIFIED_BUFF_ID_DISAPPEARS};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightLogic fightLogic, List<Integer> conditionConfig, StaticBuff staticBuff, Object... params) {
        IFightBuff lostBuff = (IFightBuff) params[0];
        if (lostBuff.getBuffConfig().getBuffId() != conditionConfig.get(2)) {
            return false;
        }
        if (conditionConfig.get(0) == 0) {
            return true;
        }

        FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(conditionConfig.get(0));
        if (CheckNull.isNull(buffObjective)) return false;
        Force triggerForce = triggerForce(fightBuff, fightLogic, conditionConfig, buffObjective);
        if (CheckNull.isEmpty(triggerForce.buffTriggerId)) {
            return false;
        }

        return triggerForce.buffTriggerId.get(0) == lostBuff.getForceId();
    }
}
