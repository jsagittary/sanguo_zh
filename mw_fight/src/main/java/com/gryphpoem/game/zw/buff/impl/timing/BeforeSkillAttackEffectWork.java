package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuffWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;

/**
 * Description: 释放技能之前 buff生效
 * Author: zhangpeng
 * createTime: 2022-10-20 18:41
 */
public class BeforeSkillAttackEffectWork implements IFightBuffWork {
    @Override
    public int effectTiming() {
        return FightConstant.BuffEffectTiming.SKILL_BEFORE;
    }

    @Override
    public boolean buffCanEffect(Force attacker, Force defender, FightLogic fightLogic, int timing, StaticBuff staticBuff, Object... params) {
        return true;
    }
}
