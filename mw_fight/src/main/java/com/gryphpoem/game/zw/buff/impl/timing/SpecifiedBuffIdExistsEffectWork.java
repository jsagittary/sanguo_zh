package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.timing.AbsFightEffectWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.pojo.p.FightAssistantHero;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Description: 触发者存在指定BUFF_ID时触发效果
 * Author: zhangpeng
 * createTime: 2022-10-27 14:00
 */
public class SpecifiedBuffIdExistsEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.SPECIFIED_BUFF_ID_EXISTS};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, Object... params) {
        // 0 代表任何人
        Force triggerForce;
        boolean canRelease = false;
        if (conditionConfig.get(0) > 0) {
            FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(conditionConfig.get(0));
            if (CheckNull.isNull(buffObjective)) return false;
            triggerForce = triggerForce(fightBuff, contextHolder, conditionConfig, buffObjective);
            if (CheckNull.isEmpty(triggerForce.buffTriggerId)) {
                return false;
            }

            switch (buffObjective) {
                case AT_LEAST_ONE_HERO_FROM_MY_SIDE:
                case AT_LEAST_ONE_HERO_FROM_ENEMY_SIDE:
                    for (Integer heroId : triggerForce.buffTriggerId) {
                        LinkedList<IFightBuff> buffList = triggerForce.buffList(heroId.intValue());
                        if (CheckNull.isEmpty(buffList))
                            continue;
                        IFightBuff buff = buffList.stream().filter(b -> b.getBuffConfig().getBuffId() == conditionConfig.get(2)).findFirst().orElse(null);
                        if (Objects.nonNull(buff)) {
                            canRelease = true;
                            break;
                        }
                    }
                    break;
                default:
                    for (Integer heroId : triggerForce.buffTriggerId) {
                        LinkedList<IFightBuff> buffList = triggerForce.buffList(heroId.intValue());
                        if (CheckNull.isEmpty(buffList))
                            continue;
                        IFightBuff buff = buffList.stream().filter(b -> b.getBuffConfig().getBuffId() == conditionConfig.get(2)).findFirst().orElse(null);
                        if (Objects.isNull(buff)) {
                            break;
                        }
                    }
                    canRelease = true;
                    break;
            }
        } else if (conditionConfig.get(0) == 0) {
            return canRelease0(contextHolder.getContext().getAttacker(), conditionConfig) || canRelease0(contextHolder.getContext().getDefender(), conditionConfig);
        }

        return canRelease;
    }

    private boolean canRelease0(Force force, List<Integer> conditionConfig) {
        LinkedList<IFightBuff> buffList = force.buffList(force.id);
        if (!CheckNull.isEmpty(buffList)) {
            IFightBuff buff = buffList.stream().filter(b -> b.getBuffConfig().getBuffId() == conditionConfig.get(2)).findFirst().orElse(null);
            if (Objects.nonNull(buff))
                return true;
        }
        if (!CheckNull.isEmpty(force.assistantHeroList)) {
            for (FightAssistantHero ass : force.assistantHeroList) {
                if (CheckNull.isNull(ass) || CheckNull.isEmpty(ass.getBuffList()))
                    continue;
                buffList = ass.getBuffList();
                IFightBuff buff = buffList.stream().filter(b -> b.getBuffConfig().getBuffId() == conditionConfig.get(2)).findFirst().orElse(null);
                if (Objects.nonNull(buff))
                    return true;
            }
        }

        return false;
    }
}
