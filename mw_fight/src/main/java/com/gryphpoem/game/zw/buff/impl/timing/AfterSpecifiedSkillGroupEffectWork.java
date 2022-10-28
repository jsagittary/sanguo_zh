package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.timing.AbsFightEffectWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;

/**
 * Description: 指定技能组施放后
 * Author: zhangpeng
 * createTime: 2022-10-28 9:30
 */
public class AfterSpecifiedSkillGroupEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.AFTER_CASTING_THE_SPECIFIED_SKILL_GROUP};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightLogic fightLogic, List<Integer> conditionConfig, StaticBuff staticBuff, Object... params) {
        StaticHeroSkill heroSkill = (StaticHeroSkill) params[0];
        if (CheckNull.isNull(heroSkill) || heroSkill.getSkillGroupId() != conditionConfig.get(2)) {
            LogUtil.debug("conditionConfig: ", conditionConfig, ", heroSkillGroupId: ", CheckNull.isNull(heroSkill) ? -1 : heroSkill.getSkillGroupId());
            return false;
        }

        if (conditionConfig.get(1) > 0) {
            FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(conditionConfig.get(0));
            if (CheckNull.isNull(buffObjective)) return false;
            Force triggerForce = triggerForce(fightBuff, fightLogic, conditionConfig, buffObjective);
            if (CheckNull.isEmpty(triggerForce.buffTriggerId)) return false;
            return triggerForce.buffTriggerId.get(0) == fightLogic.attacker.actionId;
        } else if (conditionConfig.get(1) == 0) {
            return true;
        }
        return false;
    }
}
