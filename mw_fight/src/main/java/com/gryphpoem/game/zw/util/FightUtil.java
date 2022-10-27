package com.gryphpoem.game.zw.util;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.pojo.p.FightAssistantHero;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-22 9:34
 */
public class FightUtil {
    /**
     * BUFF 与 EFFECT 唯一id
     */
    private static final AtomicLong idGenerator = new AtomicLong();

    /**
     * 获取buff作用方, 以及被作用方的buff列表
     *
     * @param affectedForce
     * @param buffObjective
     * @return
     */
    public static Map<Integer, LinkedList<IFightBuff>> actingForceBuff(Force affectedForce, FightConstant.BuffObjective buffObjective) {
        if (CheckNull.isNull(buffObjective))
            return null;
        if (CheckNull.isNull(affectedForce) || CheckNull.isEmpty(affectedForce.beActionId))
            return null;

        Map<Integer, LinkedList<IFightBuff>> actingForce = new HashMap<>(affectedForce.beActionId.size());
        for (Integer heroId : affectedForce.beActionId) {
            if (heroId == affectedForce.id) {
                actingForce.put(affectedForce.id, affectedForce.buffList);
                continue;
            }
            if (CheckNull.isEmpty(affectedForce.assistantHeroList))
                continue;
            FightAssistantHero assistantHero = affectedForce.assistantHeroList.stream().filter(ass ->
                    ass.getHeroId() == heroId.intValue()).findFirst().orElse(null);
            if (CheckNull.isNull(assistantHero)) continue;
            actingForce.put(assistantHero.getHeroId(), assistantHero.getBuffList());
        }

        return actingForce;
    }


    /**
     * 自增唯一id
     *
     * @return
     */
    public static long uniqueId() {
        return idGenerator.incrementAndGet();
    }

    /**
     * 获得buff被作用方
     *
     * @param attacker
     * @param defender
     * @param buffObjective
     * @return
     */
    public static Force actingForce(Force attacker, Force defender, FightConstant.BuffObjective buffObjective, boolean release) {
        Force affectedForce = null;
        Boolean atk = buffObjective.isAttackerSize(FightConstant.ForceSide.ATTACKER);
        if (Objects.nonNull(attacker) && Objects.nonNull(atk) && atk) {
            affectedForce = attacker;
        } else {
            Boolean def = buffObjective.isAttackerSize(FightConstant.ForceSide.DEFENDER);
            if (Objects.nonNull(defender) && Objects.nonNull(def) && def) {
                affectedForce = defender;
            }
        }

        if (release) {
            if (CheckNull.isNull(affectedForce.beActionId))
                affectedForce.beActionId = new ArrayList<>();
            if (!CheckNull.isEmpty(affectedForce.beActionId))
                affectedForce.beActionId.clear();

            switch (buffObjective) {
                case MY_PRINCIPAL_HERO:
                case ENEMY_PRINCIPAL_HERO:
                    affectedForce.beActionId.add(affectedForce.id);
                    break;
                case MY_DEPUTY_HERO:
                case ENEMY_DEPUTY_HERO:
                    if (!CheckNull.isEmpty(affectedForce.assistantHeroList)) {
                        affectedForce.beActionId.addAll(affectedForce.assistantHeroList.stream().map(FightAssistantHero::getHeroId).collect(Collectors.toList()));
                    }
                    break;
                case RANDOM_MY_HERO:
                case RANDOM_ENEMY_HERO:
                    int randomSize = 1;
                    if (!CheckNull.isEmpty(affectedForce.assistantHeroList))
                        randomSize += affectedForce.assistantHeroList.size();
                    List<Integer> randomHeroIdList = new ArrayList<>(randomSize);
                    randomHeroIdList.add(affectedForce.id);
                    affectedForce.assistantHeroList.stream().filter(h -> Objects.nonNull(h)).forEach(h -> randomHeroIdList.add(h.getHeroId()));
                    affectedForce.beActionId.add(randomHeroIdList.get(RandomHelper.randomInSize(randomHeroIdList.size())));
                    break;
                case ALL_MY_HERO:
                case ALL_ENEMY_HERO:
                    affectedForce.beActionId.add(affectedForce.id);
                    if (!CheckNull.isEmpty(affectedForce.assistantHeroList)) {
                        affectedForce.beActionId.addAll(affectedForce.assistantHeroList.stream().map(FightAssistantHero::getHeroId).collect(Collectors.toList()));
                    }
                    break;
            }
        }

        return affectedForce;
    }

    /**
     * 释放效果
     *
     * @param attacker
     * @param fightLogic
     * @param fightResult
     */
    public static void releaseAllBuffEffect(Force attacker, FightLogic fightLogic, FightResult fightResult, int timing) {
        // 触发技能后buff释放
        if (!CheckNull.isEmpty(attacker.buffList)) {
            attacker.buffList.forEach(fightBuff -> {
                fightBuff.releaseEffect(attacker, fightLogic, fightResult, timing);
            });
        }
    }

    public static boolean effectSameSize(FightConstant.BuffObjective buffObjective, IFightBuff fightBuff, FightLogic fightLogic, Force force) {
        // 定义己方含义
        Force myForce = fightLogic.attacker.ownerId == fightBuff.getBuffGiver().ownerId ? fightLogic.attacker : fightLogic.defender;
        switch (buffObjective) {
            case MY_PRINCIPAL_HERO:

        }
    }
}
