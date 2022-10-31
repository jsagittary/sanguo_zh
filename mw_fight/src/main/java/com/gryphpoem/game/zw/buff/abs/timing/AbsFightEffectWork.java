package com.gryphpoem.game.zw.buff.abs.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightBuffWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pojo.p.FightAssistantHero;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-27 14:11
 */
public abstract class AbsFightEffectWork implements IFightBuffWork {
    /**
     * 触发者
     *
     * @param fightBuff
     * @param contextHolder
     * @param buffObjective
     * @return
     */
    protected Force triggerForce(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, FightConstant.BuffObjective buffObjective) {
        Force buffAttacker;
        Force buffDefender;
        Force triggerForce;
        if (contextHolder.getContext().getAttacker().ownerId == fightBuff.getBuffGiver().ownerId) {
            buffAttacker = contextHolder.getContext().getAttacker();
            buffDefender = contextHolder.getContext().getDefender();
        } else {
            buffAttacker = contextHolder.getContext().getDefender();
            buffDefender = contextHolder.getContext().getAttacker();
        }

        if (FightConstant.BuffObjective.BUFF_LOADER.equals(buffObjective)) {
            triggerForce = fightBuff.getForce();
        } else {
            triggerForce = FightUtil.actingForce(buffAttacker, buffDefender, buffObjective, false);
        }

        if (CheckNull.isNull(triggerForce.buffTriggerId))
            triggerForce.buffTriggerId = new ArrayList<>();
        if (!CheckNull.isEmpty(triggerForce.buffTriggerId))
            triggerForce.buffTriggerId.clear();

        switch (buffObjective) {
            case BUFF_LOADER:
                if (!CheckNull.isEmpty(triggerForce.buffList)) {
                    IFightBuff buff = triggerForce.buffList.stream().filter(t -> t.uniqueId() == fightBuff.uniqueId()).findFirst().orElse(null);
                    if (Objects.nonNull(buff)) {
                        triggerForce.buffTriggerId.add(triggerForce.id);
                        break;
                    }
                }
                if (!CheckNull.isEmpty(triggerForce.assistantHeroList)) {
                    for (FightAssistantHero assistantHero : triggerForce.assistantHeroList) {
                        if (CheckNull.isNull(assistantHero) || CheckNull.isEmpty(assistantHero.getBuffList()))
                            continue;
                        IFightBuff buff = assistantHero.getBuffList().stream().filter(t -> t.uniqueId() == fightBuff.uniqueId()).findFirst().orElse(null);
                        if (Objects.nonNull(buff)) {
                            triggerForce.buffTriggerId.add(assistantHero.getHeroId());
                            break;
                        }
                    }
                }
                break;
            case MY_PRINCIPAL_HERO:
            case ENEMY_PRINCIPAL_HERO:
                triggerForce.buffTriggerId.add(triggerForce.id);
                break;
            case MY_DEPUTY_HERO:
            case ENEMY_DEPUTY_HERO:
                if (!CheckNull.isEmpty(triggerForce.assistantHeroList)) {
                    triggerForce.buffTriggerId.addAll(triggerForce.assistantHeroList.stream().map(FightAssistantHero::getHeroId).collect(Collectors.toList()));
                }
                break;
            case ALL_MY_HERO:
            case ALL_ENEMY_HERO:
            case AT_LEAST_ONE_HERO_FROM_MY_SIDE:
            case AT_LEAST_ONE_HERO_FROM_ENEMY_SIDE:
                triggerForce.buffTriggerId.add(triggerForce.id);
                if (!CheckNull.isEmpty(triggerForce.assistantHeroList)) {
                    triggerForce.buffTriggerId.addAll(triggerForce.assistantHeroList.stream().map(FightAssistantHero::getHeroId).collect(Collectors.toList()));
                }
                break;
        }

        if (CheckNull.isEmpty(triggerForce.buffTriggerId)) {
            LogUtil.error("buffId: ", fightBuff.getBuffConfig().getBuffId(),
                    ", conditionConfig: ", conditionConfig, ", triggerList is empty");
        }
        return triggerForce;
    }

    protected boolean canRelease(Force triggerForce, List<Integer> forceList, FightConstant.BuffObjective buffObjective) {
        boolean canEffect = true;
        switch (buffObjective) {
            case AT_LEAST_ONE_HERO_FROM_ENEMY_SIDE:
            case AT_LEAST_ONE_HERO_FROM_MY_SIDE:
                for (Integer heroId : forceList) {
                    if (triggerForce.buffTriggerId.contains(heroId)) {
                        break;
                    }
                }
                break;
            default:
                for (Integer heroId : triggerForce.buffTriggerId) {
                    if (!forceList.contains(heroId)) {
                        canEffect = false;
                        break;
                    }
                }
                break;
        }

        return canEffect;
    }
}
