package com.gryphpoem.game.zw.buff.impl.buff;

import com.gryphpoem.game.zw.buff.abs.buff.AbsConditionBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;
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
    @Override
    public void releaseBuff(LinkedList actingBuffList, FightLogic fightLogic, List staticBuffConfig, FightResult fightResult, Object... params) {
        super.releaseBuff(actingBuffList, fightLogic, staticBuffConfig, fightResult, params);
        if (CheckNull.isEmpty(this.staticBuff.getBuffTriggerCondition())) {
            // 若触发条件为空, 则直接触发
            releaseEffect(this.force, fightLogic, fightResult, -1, params);
        }
    }

    @Override
    public void releaseEffect(Force actingForce, FightLogic fightLogic, FightResult fightResult, int timing, Object... params) {
        if (CheckNull.isNull(this.staticBuff)) {
            LogUtil.error(String.format("staticBuff config is null", -1));
            return;
        }
        if (!CheckNull.isEmpty(this.staticBuff.getBuffTriggerCondition())) {
            FightManager fightManager = DataResource.ac.getBean(FightManager.class);
            List<Integer> conditionConfig = this.staticBuff.getBuffTriggerCondition().stream().filter(config ->
                    !CheckNull.isEmpty(config) && config.get(0) == timing).findFirst().orElse(null);
            if (CheckNull.isEmpty(conditionConfig)) {
                return;
            }

            boolean canRelease = true;
            for (List<Integer> config : this.staticBuff.getBuffTriggerCondition()) {
                if (CheckNull.isEmpty(config) || config.size() < 2) continue;
                if (!fightManager.buffCanRelease(this, fightLogic, config.get(1), this.staticBuff, config)) {
                    canRelease = false;
                    break;
                }
            }
            // 触发条件未通过, 无法触发效果
            if (!canRelease) {
                return;
            }
        }

        releaseBuffEffect(actingForce, fightLogic, fightResult);
        this.effect = true;
        if (this.buffEffectiveTimes > 0) {
            this.buffEffectiveTimes--;
        }
    }

    @Override
    public void buffLoseEffectiveness(Force attacker, Force defender, FightLogic fightLogic, FightResult fightResult, Object... params) {
        if (!this.effect) {
            // buff一次都未作用
            return;
        }

        buffEffectiveness(this.force, fightLogic, fightResult);
    }
}
