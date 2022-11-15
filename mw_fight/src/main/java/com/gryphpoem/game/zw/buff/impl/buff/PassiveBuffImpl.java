package com.gryphpoem.game.zw.buff.impl.buff;

import com.gryphpoem.game.zw.buff.abs.buff.AbsPassiveBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuff;
import com.gryphpoem.game.zw.util.FightUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Description: 立即释放且生效的buff
 * Author: zhangpeng
 * createTime: 2022-10-26 11:49
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.BUFF, type = FightConstant.BuffEffectiveType.PASSIVE)
public class PassiveBuffImpl extends AbsPassiveBuff {

    public PassiveBuffImpl(StaticBuff staticBuff) {
        this.buffKeyId = FightUtil.uniqueId();
        this.staticBuff = staticBuff;
        this.buffEffectiveRounds = this.staticBuff.getContinuousRound();
    }

    @Override
    public void releaseBuff(LinkedList actingBuffList, FightContextHolder contextHolder, List staticBuffConfig, Object... params) {
        super.releaseBuff(actingBuffList, contextHolder, staticBuffConfig, params);
        // 释放buff时, 立马释放效果
        releaseEffect(contextHolder, FightConstant.BuffEffectTiming.PASSIVE_RELEASE, params);
    }

    @Override
    public void releaseEffect(FightContextHolder contextHolder, int timing, Object... params) {
        if (timing != FightConstant.BuffEffectTiming.PASSIVE_RELEASE)
            return;
        if (!hasRemainEffectiveTimes(contextHolder)) {
            LogUtil.fight("buff持有人: ", this.force.ownerId, "-", this.forceId, ", buff作用效果使用完, 无法再生效, buffConfig: ", this.staticBuff);
            return;
        }
        // 被动释放一次后, 不再释放
        releaseBuffEffect(contextHolder, timing);
    }

    @Override
    public void buffLoseEffectiveness(FightContextHolder contextHolder, Object... params) {
        super.buffLoseEffectiveness(contextHolder, params);
        buffEffectiveness(contextHolder);
    }
}
