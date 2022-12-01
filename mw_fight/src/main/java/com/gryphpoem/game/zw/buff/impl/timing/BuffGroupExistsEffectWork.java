package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.timing.AbsFightEffectWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.pojo.p.ActionDirection;
import com.gryphpoem.game.zw.pojo.p.FightAssistantHero;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-28 9:42
 */
public class BuffGroupExistsEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.BUFF_GROUP_EXISTS};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, Object... params) {
        // 0 代表任何人
        ActionDirection actionDirection;
        boolean canRelease = false;
        if (conditionConfig.get(0) > 0) {
            FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(conditionConfig.get(0));
            if (CheckNull.isNull(buffObjective)) return false;
            actionDirection = triggerForce(fightBuff, contextHolder, conditionConfig);
            if (CheckNull.isNull(actionDirection))
                return false;
            if (CheckNull.isEmpty(actionDirection.getAtkHeroList())) {
                return false;
            }

            switch (buffObjective) {
                case AT_LEAST_ONE_HERO_FROM_MY_SIDE:
                case AT_LEAST_ONE_HERO_FROM_ENEMY_SIDE:
                    for (Integer heroId : actionDirection.getAtkHeroList()) {
                        Force triggerForce = actionDirection.getAtk();
                        LinkedList<IFightBuff> buffList = triggerForce.buffList(heroId.intValue());
                        if (CheckNull.isEmpty(buffList))
                            continue;
                        IFightBuff buff = buffList.stream().filter(b -> b.getBuffConfig().getTypeGrouping().contains(conditionConfig.get(2))).findFirst().orElse(null);
                        if (Objects.nonNull(buff)) {
                            canRelease = true;
                            break;
                        }
                    }
                    break;
                default:
                    for (Integer heroId : actionDirection.getAtkHeroList()) {
                        Force triggerForce = actionDirection.getAtk();
                        LinkedList<IFightBuff> buffList = triggerForce.buffList(heroId.intValue());
                        if (CheckNull.isEmpty(buffList))
                            continue;
                        IFightBuff buff = buffList.stream().filter(b -> b.getBuffConfig().getTypeGrouping().contains(conditionConfig.get(2))).findFirst().orElse(null);
                        if (Objects.isNull(buff)) {
                            break;
                        }
                    }
                    canRelease = true;
                    break;
            }
        } else if (conditionConfig.get(0) == 0) {
            return canRelease0(contextHolder.getCurAttacker(), conditionConfig) || canRelease0(contextHolder.getCurDefender(), conditionConfig);
        }

        return canRelease;
    }

    private boolean canRelease0(Force force, List<Integer> conditionConfig) {
        LinkedList<IFightBuff> buffList = force.buffList(force.id);
        if (!CheckNull.isEmpty(buffList)) {
            IFightBuff buff = buffList.stream().filter(b -> b.getBuffConfig().getTypeGrouping().contains(conditionConfig.get(2))).findFirst().orElse(null);
            if (Objects.nonNull(buff))
                return true;
        }
        if (!CheckNull.isEmpty(force.assistantHeroList)) {
            for (FightAssistantHero ass : force.assistantHeroList) {
                if (CheckNull.isNull(ass) || CheckNull.isEmpty(ass.getBuffList()))
                    continue;
                buffList = ass.getBuffList();
                IFightBuff buff = buffList.stream().filter(b -> b.getBuffConfig().getTypeGrouping().contains(conditionConfig.get(2))).findFirst().orElse(null);
                if (Objects.nonNull(buff))
                    return true;
            }
        }

        return false;
    }
}