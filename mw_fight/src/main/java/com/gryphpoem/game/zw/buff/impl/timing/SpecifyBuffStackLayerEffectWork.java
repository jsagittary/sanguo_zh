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
import java.util.stream.Collectors;

/**
 * Description: 指定BUFF_ID叠加到指定层数
 * Author: zhangpeng
 * createTime: 2022-10-27 18:28
 */
public class SpecifyBuffStackLayerEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.SPECIFY_BUFF_TO_STACK_TO_THE_SPECIFIED_LAYER_NUM};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, Object... params) {
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
                case AT_LEAST_ONE_HERO_FROM_ENEMY_SIDE:
                case AT_LEAST_ONE_HERO_FROM_MY_SIDE:
                    for (Integer heroId : triggerForce.buffTriggerId) {
                        LinkedList<IFightBuff> buffs = triggerForce.buffList(heroId.intValue());
                        List<IFightBuff> sameIdBuffList = buffs.stream().filter(b ->
                                b.getBuffConfig().getBuffId() == conditionConfig.get(2)).collect(Collectors.toList());
                        if (sameIdBuffList.size() >= conditionConfig.get(3)) {
                            canRelease = true;
                            break;
                        }
                    }
                    break;
                default:
                    for (Integer heroId : triggerForce.buffTriggerId) {
                        LinkedList<IFightBuff> buffs = triggerForce.buffList(heroId.intValue());
                        List<IFightBuff> sameIdBuffList = buffs.stream().filter(b ->
                                b.getBuffConfig().getBuffId() == conditionConfig.get(2)).collect(Collectors.toList());
                        if (sameIdBuffList.size() < conditionConfig.get(3)) {
                            break;
                        }
                    }
                    canRelease = true;
                    break;
            }
        } else if (conditionConfig.get(0) == 0) {
            return canRelease0(contextHolder.getAttacker(), conditionConfig) || canRelease0(contextHolder.getDefender(), conditionConfig);
        }
        return canRelease;
    }

    private boolean canRelease0(Force force, List<Integer> conditionConfig) {
        LinkedList<IFightBuff> buffList = force.buffList(force.id);
        if (!CheckNull.isEmpty(buffList)) {
            List<IFightBuff> sameIdBuffList = buffList.stream().filter(b ->
                    b.getBuffConfig().getBuffId() == conditionConfig.get(2)).collect(Collectors.toList());
            if (sameIdBuffList.size() >= conditionConfig.get(3))
                return true;
        }
        if (!CheckNull.isEmpty(force.assistantHeroList)) {
            for (FightAssistantHero ass : force.assistantHeroList) {
                List<IFightBuff> sameIdBuffList = ass.getBuffList().stream().filter(b ->
                        b.getBuffConfig().getBuffId() == conditionConfig.get(2)).collect(Collectors.toList());
                if (sameIdBuffList.size() >= conditionConfig.get(3)) {
                    return true;
                }
            }
        }

        return false;
    }
}
