package com.gryphpoem.game.zw.util;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.pojo.p.FightAssistantHero;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

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
        if (CheckNull.isNull(affectedForce) || CheckNull.isNull(affectedForce.buffList))
            return null;

        Map<Integer, Map<Integer, LinkedList<IFightBuff>>> affectedBuffList = affectedForce.buffList;
        Map<Integer, LinkedList<IFightBuff>> actingForce = null;
        switch (buffObjective) {
            case MY_PRINCIPAL_HERO:
            case ENEMY_PRINCIPAL_HERO:
                if (CheckNull.isNull(affectedBuffList)) return null;
                actingForce = new HashMap<>();
                actingForce.put(affectedForce.id, affectedBuffList.computeIfAbsent(FightConstant.HeroType.PRINCIPAL_HERO, m -> new HashMap<>()).
                        computeIfAbsent(affectedForce.id, l -> new LinkedList<>()));
                break;
            case MY_DEPUTY_HERO:
            case ENEMY_DEPUTY_HERO:
                if (CheckNull.isNull(affectedBuffList)) return null;
                if (CheckNull.isEmpty(affectedForce.assistantHeroList)) return null;
                actingForce = new HashMap<>(affectedForce.assistantHeroList.size());
                for (FightAssistantHero assistantHero : affectedForce.assistantHeroList) {
                    if (CheckNull.isNull(assistantHero)) continue;
                    actingForce.put(assistantHero.getHeroId(), affectedBuffList.computeIfAbsent(FightConstant.HeroType.DEPUTY_HERO, m -> new HashMap<>()).
                            computeIfAbsent(assistantHero.getHeroId(), l -> new LinkedList<>()));
                }
                break;
            case RANDOM_MY_HERO:
            case RANDOM_ENEMY_HERO:
                if (CheckNull.isNull(affectedBuffList)) return null;
                int randomSize = 1;
                if (!CheckNull.isEmpty(affectedForce.assistantHeroList))
                    randomSize += affectedForce.assistantHeroList.size();
                List<Integer> randomHeroIdList = new ArrayList<>(randomSize);
                randomHeroIdList.add(affectedForce.id);
                affectedForce.assistantHeroList.stream().filter(h -> Objects.nonNull(h)).forEach(h -> randomHeroIdList.add(h.getHeroId()));
                int randomHeroId = randomHeroIdList.get(RandomHelper.randomInSize(randomHeroIdList.size()));
                actingForce = new HashMap<>(1);
                int heroType = randomHeroId == affectedForce.id ? FightConstant.HeroType.PRINCIPAL_HERO : FightConstant.HeroType.DEPUTY_HERO;
                actingForce.put(randomHeroId, affectedBuffList.computeIfAbsent(heroType, m -> new HashMap<>()).
                        computeIfAbsent(randomHeroId, l -> new LinkedList<>()));
                break;
            case ALL_MY_HERO:
            case ALL_ENEMY_HERO:
                if (CheckNull.isNull(affectedBuffList)) return null;
                int totalSize = 1;
                if (!CheckNull.isEmpty(affectedForce.assistantHeroList))
                    totalSize += affectedForce.assistantHeroList.size();
                actingForce = new HashMap<>(totalSize);
                actingForce.put(affectedForce.id, affectedBuffList.computeIfAbsent(FightConstant.HeroType.PRINCIPAL_HERO, m -> new HashMap<>()).
                        computeIfAbsent(affectedForce.id, l -> new LinkedList<>()));
                for (FightAssistantHero assistantHero : affectedForce.assistantHeroList) {
                    if (CheckNull.isNull(assistantHero)) continue;
                    actingForce.put(assistantHero.getHeroId(), affectedBuffList.computeIfAbsent(FightConstant.HeroType.DEPUTY_HERO, m -> new HashMap<>()).
                            computeIfAbsent(assistantHero.getHeroId(), l -> new LinkedList<>()));
                }
                break;
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
    public static Force actingForce(Force attacker, Force defender, FightConstant.BuffObjective buffObjective) {
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

        return affectedForce;
    }
}
