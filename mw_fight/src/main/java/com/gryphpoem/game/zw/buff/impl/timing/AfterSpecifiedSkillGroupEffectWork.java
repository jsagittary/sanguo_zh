package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.timing.AbsFightEffectWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.pojo.p.ActionDirection;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
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
    public boolean buffCanEffect(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, Object... params) {
        StaticHeroSkill heroSkill = (StaticHeroSkill) params[0];
        if (CheckNull.isNull(heroSkill) || heroSkill.getSkillGroupId() != conditionConfig.get(2)) {
            LogUtil.debug("conditionConfig: ", conditionConfig, ", heroSkillGroupId: ", CheckNull.isNull(heroSkill) ? -1 : heroSkill.getSkillGroupId());
            return false;
        }

        if (conditionConfig.get(1) > 0) {
            ActionDirection actionDirection = triggerForce(fightBuff, contextHolder, conditionConfig);
            if (CheckNull.isNull(actionDirection) || CheckNull.isEmpty(actionDirection.getAtkHeroList())) return false;
            return actionDirection.getAtkHeroList().get(0) == contextHolder.getCurAtkHeroId();
        } else if (conditionConfig.get(1) == 0) {
            return true;
        }
        return false;
    }
}
