package com.gryphpoem.game.zw.buff.impl.buff;

import com.gryphpoem.game.zw.buff.abs.buff.AbsPassiveBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;

import java.util.LinkedList;
import java.util.List;

/**
 * Description: 立即释放且生效的buff
 * Author: zhangpeng
 * createTime: 2022-10-26 11:49
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.BUFF, type = FightConstant.BuffEffectiveType.PASSIVE)
public class PassiveBuffImpl extends AbsPassiveBuff {
    @Override
    public void releaseBuff(LinkedList actingBuffList, FightLogic fightLogic, List staticBuffConfig, FightResult fightResult, Object... params) {
        super.releaseBuff(actingBuffList, fightLogic, staticBuffConfig, fightResult, params);
        // 释放buff时, 立马释放效果
        releaseEffect(this.force, fightLogic, fightResult, -1, params);
    }

    @Override
    public void releaseEffect(Force actingForce, FightLogic fightLogic, FightResult fightResult, int timing, Object... params) {
        if (timing != -1)
            return;

        // 被动释放一次后, 不再释放
        releaseBuffEffect(actingForce, fightLogic, fightResult);
    }

    @Override
    public void buffLoseEffectiveness(Force attacker, Force defender, FightLogic fightLogic, FightResult fightResult, Object... params) {
        buffEffectiveness(this.force, fightLogic, fightResult);
    }
}
