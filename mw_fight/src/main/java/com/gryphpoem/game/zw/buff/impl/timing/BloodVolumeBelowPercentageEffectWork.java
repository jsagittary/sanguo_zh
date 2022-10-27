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
 * Description: 血量低于百分比
 * Author: zhangpeng
 * createTime: 2022-10-27 11:59
 */
public class BloodVolumeBelowPercentageEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.BLOOD_VOLUME_BELOW_PERCENTAGE};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightLogic fightLogic, List<Integer> conditionConfig, StaticBuff staticBuff, Object... params) {
        // 0 代表任何人
        Force triggerForce;
        if (conditionConfig.get(0) > 0) {
            FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(conditionConfig.get(0));
            if (CheckNull.isNull(buffObjective)) return false;

            triggerForce = triggerForce(fightBuff, fightLogic, conditionConfig, buffObjective);
            if ((triggerForce.hp / triggerForce.maxHp * 1.0d) < (conditionConfig.get(1) / FightConstant.TEN_THOUSAND)) {
                return true;
            }
        } else if (conditionConfig.get(0) == 0) {
            if ((fightLogic.attacker.hp / fightLogic.attacker.maxHp * 1.0d) < (conditionConfig.get(1) / FightConstant.TEN_THOUSAND))
                return true;
            if ((fightLogic.defender.hp / fightLogic.defender.maxHp * 1.0d) < (conditionConfig.get(1) / FightConstant.TEN_THOUSAND))
                return true;
        }

        return false;
    }
}
