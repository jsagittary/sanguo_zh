package com.gryphpoem.game.zw.util;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.pojo.p.FightAssistantHero;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Description: 战斗工具类
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
     * 填充玩家作用列表
     *
     * @param fightBuff
     * @param triggerForce
     * @param buffObjective
     */
    public static void fillActingHeroList(IFightBuff fightBuff, Force triggerForce, List<Integer> heroList, FightContextHolder contextHolder, FightConstant.BuffObjective buffObjective) {
        if (!CheckNull.isEmpty(heroList))
            heroList.clear();

        switch (buffObjective) {
            case RELEASE_SKILL:
                if (CheckNull.isNull(fightBuff)) {
                    // 执行主体效果
                    heroList.add(contextHolder.getAttacker().actionId);
                    break;
                }
                heroList.add(fightBuff.getBuffGiverId());
                break;
            case BUFF_LOADER:
                if (!CheckNull.isEmpty(triggerForce.buffList)) {
                    IFightBuff buff = triggerForce.buffList.stream().filter(t -> t.uniqueId() == fightBuff.uniqueId()).findFirst().orElse(null);
                    if (Objects.nonNull(buff)) {
                        heroList.add(triggerForce.id);
                        break;
                    }
                }
                if (!CheckNull.isEmpty(triggerForce.assistantHeroList)) {
                    for (FightAssistantHero assistantHero : triggerForce.assistantHeroList) {
                        if (CheckNull.isNull(assistantHero) || CheckNull.isEmpty(assistantHero.getBuffList()))
                            continue;
                        IFightBuff buff = assistantHero.getBuffList().stream().filter(t -> t.uniqueId() == fightBuff.uniqueId()).findFirst().orElse(null);
                        if (Objects.nonNull(buff)) {
                            heroList.add(assistantHero.getHeroId());
                            break;
                        }
                    }
                }
                break;
            case MY_PRINCIPAL_HERO:
            case ENEMY_PRINCIPAL_HERO:
                heroList.add(triggerForce.id);
                break;
            case MY_DEPUTY_HERO:
            case ENEMY_DEPUTY_HERO:
                if (!CheckNull.isEmpty(triggerForce.assistantHeroList)) {
                    heroList.addAll(triggerForce.assistantHeroList.stream().map(FightAssistantHero::getHeroId).collect(Collectors.toList()));
                }
                break;
            case ALL_MY_HERO:
            case ALL_ENEMY_HERO:
            case AT_LEAST_ONE_HERO_FROM_MY_SIDE:
            case AT_LEAST_ONE_HERO_FROM_ENEMY_SIDE:
                heroList.add(triggerForce.id);
                if (!CheckNull.isEmpty(triggerForce.assistantHeroList)) {
                    heroList.addAll(triggerForce.assistantHeroList.stream().map(FightAssistantHero::getHeroId).collect(Collectors.toList()));
                }
                break;
        }
    }

    /**
     * buff的施予方(释放技能方)为己方时, 计算敌我双方将领作用方
     *
     * @param fightBuff
     * @param contextHolder
     * @param buffObjective
     * @return
     */
    public static Force getActingForce(IFightBuff fightBuff, FightContextHolder contextHolder, FightConstant.BuffObjective buffObjective) {
        Force buffAttacker;
        Force buffDefender;
        Force executorForce;
        if (contextHolder.getAttacker().ownerId == fightBuff.getBuffGiver().ownerId) {
            buffAttacker = contextHolder.getAttacker();
            buffDefender = contextHolder.getDefender();
        } else {
            buffAttacker = contextHolder.getDefender();
            buffDefender = contextHolder.getAttacker();
        }

        switch (buffObjective) {
            case RELEASE_SKILL:
                if (CheckNull.isNull(fightBuff)) {
                    executorForce = contextHolder.getAttacker();
                    break;
                }
                executorForce = fightBuff.getBuffGiver();
                break;
            case BUFF_LOADER:
                executorForce = fightBuff.getForce();
                break;
            default:
                executorForce = FightUtil.actingForce(buffAttacker, buffDefender, buffObjective, false);
                break;
        }

        return executorForce;
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
        if (FightConstant.BuffObjective.RELEASE_SKILL.equals(buffObjective)) {
            return attacker;
        }

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
            // 释放技能或buff 算出被作用方(被攻击方)是哪些武将
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

    public static void releaseAllBuffEffect(FightContextHolder contextHolder, int timing, Object... params) {
        if (!contextHolder.getAttacker().isBuffListEmpty()) {
            releaseBuffEffect(contextHolder.getAttacker(), contextHolder, timing, params);
        }
        if (!contextHolder.getDefender().isBuffListEmpty()) {
            releaseBuffEffect(contextHolder.getDefender(), contextHolder, timing, params);
        }
    }

    /**
     * 释放效果
     *
     * @param attacker
     * @param contextHolder
     */
    public static void releaseBuffEffect(Force attacker, FightContextHolder contextHolder, int timing, Object... params) {
        attacker.releaseBuffEffect(contextHolder, timing, params);
    }
}
