package com.gryphpoem.game.zw.buff.impl.buff;

import com.gryphpoem.game.zw.buff.abs.buff.AbsConditionBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Description: 条件触发buff实现
 * Author: zhangpeng
 * createTime: 2022-10-26 15:55
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.BUFF, type = FightConstant.BuffEffectiveType.CONDITION)
public class ConditionBuffImpl extends AbsConditionBuff {

    public ConditionBuffImpl(StaticBuff staticBuff) {
        this.buffKeyId = FightUtil.uniqueId();
        this.staticBuff = staticBuff;
        this.buffEffectiveRounds = this.staticBuff.getContinuousRound();
    }

    @Override
    public void releaseBuff(LinkedList actingBuffList, FightContextHolder contextHolder, List staticBuffConfig, Object... params) {
        super.releaseBuff(actingBuffList, contextHolder, staticBuffConfig, params);
        if (CheckNull.isEmpty(this.staticBuff.getBuffTriggerCondition())) {
            // 若触发条件为空, 则直接触发
            releaseEffect(contextHolder, -1, params);
        }
    }

    @Override
    public void releaseEffect(FightContextHolder contextHolder, int timing, Object... params) {
        if (CheckNull.isNull(this.staticBuff)) {
            LogUtil.error(String.format("staticBuff config is null", -1));
            return;
        }
        if (!hasRemainEffectiveTimes(contextHolder)) {
            // 清除存在的效果
            buffEffectiveness(contextHolder);
            LogUtil.fight("buff持有人: ", this.force.ownerId, "-", this.forceId, ", buff作用效果使用完, 无法再生效, buffConfig: ", this.staticBuff);
            return;
        }
        if (!hasEffectiveTimesSingleRound()) {
            return;
        }


        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        if (!CheckNull.isEmpty(this.staticBuff.getEffects())) {
            // 此段代码目的在于 动作嵌套动作只能嵌套一层, 当前嵌套了动作时, 无法再次嵌套动作
            boolean canRelease = true;
            for (List<Integer> config : this.staticBuff.getEffects()) {
                if (CheckNull.isEmpty(config) || config.size() < 3) continue;
                StaticEffectRule rule = StaticFightManager.getStaticEffectRule(config.get(2));
                if (CheckNull.isNull(rule)) continue;
                if (!fightManager.effectCanRelease(contextHolder, rule, params)) {
                    canRelease = false;
                    break;
                }
            }
            // 有些效果无法释放
            if (!canRelease) {
                return;
            }
        }
        if (!CheckNull.isEmpty(this.staticBuff.getBuffTriggerCondition())) {
            boolean canRelease = true;
            for (List<Integer> config : this.staticBuff.getBuffTriggerCondition()) {
                if (CheckNull.isEmpty(config) || config.size() < 2) continue;
                if (!fightManager.buffCanRelease(this, contextHolder, config.get(1), config, params)) {
                    canRelease = false;
                    break;
                }
            }
            // 触发条件未通过, 无法触发效果
            if (!canRelease) {
                return;
            }
        }

        // 释放此buff所有效果
        releaseBuffEffect(contextHolder, timing);
        this.effect = true;
        if (this.staticBuff.getBuffEffectiveTimes() > 0) {
            this.buffEffectiveTimes++;
        }
        if (this.staticBuff.getEffectiveTimesSingleRound() > 0) {
            this.effectiveTimesSingleRound++;
        }
    }

    @Override
    public void buffLoseEffectiveness(FightContextHolder contextHolder, Object... params) {
        super.buffLoseEffectiveness(contextHolder, params);
        if (!this.effect) {
            // buff一次都未作用
            return;
        }

        buffEffectiveness(contextHolder);
    }
}
