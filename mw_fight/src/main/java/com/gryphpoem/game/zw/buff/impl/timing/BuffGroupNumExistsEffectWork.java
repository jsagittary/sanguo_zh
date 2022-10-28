package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.timing.AbsFightEffectWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.pojo.p.FightAssistantHero;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-28 9:57
 */
public class BuffGroupNumExistsEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.BUFF_GROUP_NUM_EXISTS};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightLogic fightLogic, List<Integer> conditionConfig, StaticBuff staticBuff, Object... params) {
        // 0 代表任何人
        Force triggerForce;
        boolean canRelease = false;
        if (conditionConfig.get(0) > 0) {
            FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(conditionConfig.get(0));
            if (CheckNull.isNull(buffObjective)) return false;
            triggerForce = triggerForce(fightBuff, fightLogic, conditionConfig, buffObjective);
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
                        List<IFightBuff> buffs = buffList.stream().filter(b -> b.getBuffConfig().getTypeGrouping().contains(conditionConfig.get(2))).collect(Collectors.toList());
                        if (buffs.size() >= conditionConfig.get(3)) {
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
                        List<IFightBuff> buffs = buffList.stream().filter(b -> b.getBuffConfig().getTypeGrouping().contains(conditionConfig.get(2))).collect(Collectors.toList());
                        if (buffs.size() < conditionConfig.get(3)) {
                            break;
                        }
                    }
                    canRelease = true;
                    break;
            }
        } else if (conditionConfig.get(0) == 0) {
            return canRelease0(fightLogic.attacker, conditionConfig) || canRelease0(fightLogic.defender, conditionConfig);
        }

        return canRelease;
    }

    private boolean canRelease0(Force force, List<Integer> conditionConfig) {
        LinkedList<IFightBuff> buffList = force.buffList(force.id);
        if (!CheckNull.isEmpty(buffList)) {
            List<IFightBuff> buffs = buffList.stream().filter(b -> b.getBuffConfig().getTypeGrouping().contains(conditionConfig.get(2))).collect(Collectors.toList());
            if (buffs.size() >= conditionConfig.get(3))
                return true;
        }
        if (!CheckNull.isEmpty(force.assistantHeroList)) {
            for (FightAssistantHero ass : force.assistantHeroList) {
                if (CheckNull.isNull(ass) || CheckNull.isEmpty(ass.getBuffList()))
                    continue;
                buffList = ass.getBuffList();
                List<IFightBuff> buffs = buffList.stream().filter(b -> b.getBuffConfig().getTypeGrouping().contains(conditionConfig.get(2))).collect(Collectors.toList());
                if (buffs.size() >= conditionConfig.get(3))
                    return true;
            }
        }

        return false;
    }
}
