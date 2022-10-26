package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightBuffWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;

/**
 * Description: 释放技能之前 buff生效
 * Author: zhangpeng
 * createTime: 2022-10-20 18:41
 */
public class BeforeSkillAttackEffectWork implements IFightBuffWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.SKILL_BEFORE, FightConstant.BuffEffectTiming.SKILL_AFTER};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightLogic fightLogic, List<Integer> conditionConfig, StaticBuff staticBuff, Object... params) {
        if (CheckNull.isEmpty(conditionConfig)) return true;
        FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(conditionConfig.get(0));
        if (CheckNull.isNull(buffObjective)) return false;

        Force buffAttacker;
        Force buffDefender;
        if (fightLogic.attacker.ownerId == fightBuff.getBuffGiver().ownerId) {
            buffAttacker = fightLogic.attacker;
            buffDefender = fightLogic.defender;
        } else {
            buffAttacker = fightLogic.defender;
            buffDefender = fightLogic.attacker;
        }
        Force triggerForce = FightUtil.actingForce(buffAttacker, buffDefender, buffObjective);
        if (fightLogic.attacker.ownerId == triggerForce.ownerId) {
            return true;
        }

        return false;
    }

}
