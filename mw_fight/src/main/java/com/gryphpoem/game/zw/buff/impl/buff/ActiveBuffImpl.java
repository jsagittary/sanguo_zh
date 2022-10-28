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
    public void releaseEffect(Force actingForce, FightLogic fightLogic, FightResult fightResult, int timing, Object... params) {
        if (timing != FightConstant.BuffEffectTiming.ROUND_START)
            return;

        releaseBuffEffect(fightLogic, fightResult);
        // buff作用次数扣除
        this.effect = true;
        if (this.buffEffectiveTimes > 0) {
            this.buffEffectiveTimes--;
        }
    }

    @Override
    public void buffLoseEffectiveness(Force attacker, Force defender, FightLogic fightLogic, FightResult fightResult, Object... params) {
        if (!effect) {
            // buff一次都未作用
            return;
        }

        buffEffectiveness(this.force, fightLogic, fightResult);
    }
}
