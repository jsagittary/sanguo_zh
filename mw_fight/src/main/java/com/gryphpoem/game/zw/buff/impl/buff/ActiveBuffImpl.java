package com.gryphpoem.game.zw.buff.impl.buff;

import com.gryphpoem.game.zw.buff.abs.buff.AbsActiveBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;

/**
 * Description: 主动生效buff
 * Author: zhangpeng
 * createTime: 2022-10-21 10:43
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.BUFF, type = FightConstant.BuffEffectiveType.ACTIVE)
public class ActiveBuffImpl extends AbsActiveBuff {
    @Override
    public FightResult releaseBuff(Force attacker, Force defender, FightLogic fightLogic, int timing, Object... params) {
        if (timing != FightConstant.BuffEffectTiming.ROUND_START)
            return null;

        // 释放buff效果
        
        return null;
    }

    @Override
    public FightResult buffLoseEffectiveness(Force attacker, Force defender, FightLogic fightLogic, Object... params) {
        return null;
    }
}
