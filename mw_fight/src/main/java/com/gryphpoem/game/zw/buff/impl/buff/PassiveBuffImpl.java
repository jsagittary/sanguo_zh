package com.gryphpoem.game.zw.buff.impl.buff;

import com.gryphpoem.game.zw.buff.abs.buff.AbsPassiveBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;

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
    public void releaseBuff(LinkedList actingBuffList, FightContextHolder contextHolder, List staticBuffConfig, Object... params) {
        super.releaseBuff(actingBuffList, contextHolder, staticBuffConfig, params);
        // 释放buff时, 立马释放效果
        releaseEffect(contextHolder, -1, params);
    }

    @Override
    public void releaseEffect(FightContextHolder contextHolder, int timing, Object... params) {
        if (timing != -1)
            return;

        // 被动释放一次后, 不再释放
        releaseBuffEffect(contextHolder);
    }

    @Override
    public void buffLoseEffectiveness(FightContextHolder contextHolder, Object... params) {
        buffEffectiveness(contextHolder);
    }
}
