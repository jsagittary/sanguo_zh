package com.gryphpoem.game.zw.util;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.event.FightEvent;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;
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
    private static long idGenerator = 0;

    /**
     * 获取buff作用方, 以及被作用方的buff列表
     *
     * @param actionDirection
     * @return
     */
    public static Map<Integer, LinkedList<IFightBuff>> actingForceBuff(ActionDirection actionDirection) {
        if (CheckNull.isNull(actionDirection) || CheckNull.isEmpty(actionDirection.getDefHeroList()))
            return null;

        Force affectedForce = actionDirection.getDef();
        Map<Integer, LinkedList<IFightBuff>> actingForce = new HashMap<>(actionDirection.getDefHeroList().size());
        for (Integer heroId : actionDirection.getDefHeroList()) {
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
        if (idGenerator >= 100000000) {
            idGenerator = 0;
        }
        return idGenerator++;
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
                    heroList.add(contextHolder.getCurAtkHeroId());
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
     * buff效果释放时, 计算敌我双方将领作用方
     *
     * @param fightBuff     buff的释放
     * @param contextHolder
     * @param buffObjective
     * @return
     */
    public static void buffEffectActionDirection(IFightBuff fightBuff, FightContextHolder contextHolder,
                                                 FightConstant.BuffObjective buffObjective, ActionDirection direction, boolean performer) {
        Force buffAttacker;
        Force buffDefender;
        if (CheckNull.isNull(fightBuff)) {
            // buff为空, 则是在执行技能主体效果
            buffAttacker = contextHolder.getCurAttacker();
            buffDefender = contextHolder.getCurDefender();
        } else {
            if (contextHolder.getCurAttacker().ownerId == fightBuff.getBuffGiver().ownerId) {
                buffAttacker = contextHolder.getCurAttacker();
                buffDefender = contextHolder.getCurDefender();
            } else {
                buffAttacker = contextHolder.getCurDefender();
                buffDefender = contextHolder.getCurAttacker();
            }
        }

        switch (buffObjective) {
            case ANYONE:
                break;
            case RELEASE_SKILL:
                if (performer) {
                    direction.setAtk(buffAttacker);
                    if (CheckNull.isNull(fightBuff))
                        direction.getAtkHeroList().add(contextHolder.getActionDirection().getCurAtkHeroId());
                    else
                        direction.getAtkHeroList().add(fightBuff.getBuffGiverId());
                } else {
                    direction.setDef(buffAttacker);
                    if (CheckNull.isNull(fightBuff)) {
                        direction.getDefHeroList().add(contextHolder.getActionDirection().getCurDefHeroId());
                    } else {
                        direction.getDefHeroList().add(fightBuff.getBuffGiverId());
                    }
                }
                break;
            case BUFF_LOADER:
                if (performer) {
                    direction.setAtk(fightBuff.getForce());
                    direction.getAtkHeroList().add(fightBuff.getForceId());
                } else {
                    direction.setDef(fightBuff.getForce());
                    direction.getDefHeroList().add(fightBuff.getForceId());
                }
                break;
            default:
                actingForce(buffAttacker, buffDefender, buffObjective, direction, performer);
                break;
        }
    }

    /**
     * 回合内的buff释放时的执行者或被执行者
     *
     * @param contextHolder
     * @param buffObjective
     * @param performer     是否是执行者
     */
    public static void releaseBuffSet(FightContextHolder contextHolder, FightConstant.BuffObjective buffObjective, ActionDirection direction, boolean performer) {
        if (FightConstant.BuffObjective.RELEASE_SKILL.equals(buffObjective)) {
            if (performer) {
                direction.setAtk(contextHolder.getCurAttacker());
                direction.getAtkHeroList().add(contextHolder.getCurAttacker().id);
            } else {
                direction.setDef(contextHolder.getCurAttacker());
                direction.getDefHeroList().add(contextHolder.getCurAttacker().id);
            }
            return;
        }

        // buff释放时不存在buff挂载者类型
        Force affectedForce = null;
        Force attacker = contextHolder.getCurAttacker();
        Force defender = contextHolder.getCurDefender();
        Boolean atk = buffObjective.isAttackerSize(FightConstant.ForceSide.ATTACKER);
        if (Objects.nonNull(attacker) && Objects.nonNull(atk) && atk) {
            affectedForce = attacker;
        } else {
            Boolean def = buffObjective.isAttackerSize(FightConstant.ForceSide.DEFENDER);
            if (Objects.nonNull(defender) && Objects.nonNull(def) && def) {
                affectedForce = defender;
            }
        }

        // 清除技能主体效果作用方与被作用方
        List<Integer> heroList;
        if (performer) {
            direction.setAtk(affectedForce);
            heroList = direction.getAtkHeroList();
        } else {
            direction.setDef(affectedForce);
            heroList = direction.getDefHeroList();
        }

        switch (buffObjective) {
            case MY_PRINCIPAL_HERO:
            case ENEMY_PRINCIPAL_HERO:
                heroList.add(affectedForce.id);
                break;
            case MY_DEPUTY_HERO:
            case ENEMY_DEPUTY_HERO:
                if (!CheckNull.isEmpty(affectedForce.assistantHeroList)) {
                    heroList.addAll(affectedForce.assistantHeroList.stream().map(FightAssistantHero::getHeroId).collect(Collectors.toList()));
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
                heroList.add(randomHeroIdList.get(RandomHelper.randomInSize(randomHeroIdList.size())));
                break;
            case ALL_MY_HERO:
            case ALL_ENEMY_HERO:
                heroList.add(affectedForce.id);
                if (!CheckNull.isEmpty(affectedForce.assistantHeroList)) {
                    heroList.addAll(affectedForce.assistantHeroList.stream().map(FightAssistantHero::getHeroId).collect(Collectors.toList()));
                }
                break;
        }
    }


    /**
     * 设置buff或效果的执行者或被执行者
     *
     * @param attacker
     * @param defender
     * @param buffObjective
     * @return
     */
    public static void actingForce(Force attacker, Force defender, FightConstant.BuffObjective buffObjective, ActionDirection direction, boolean performer) {
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

        List<Integer> heroList;
        if (performer) {
            direction.setAtk(affectedForce);
            heroList = direction.getAtkHeroList();
        } else {
            direction.setDef(affectedForce);
            heroList = direction.getDefHeroList();
        }

        switch (buffObjective) {
            case MY_PRINCIPAL_HERO:
            case ENEMY_PRINCIPAL_HERO:
                heroList.add(affectedForce.id);
                break;
            case MY_DEPUTY_HERO:
            case ENEMY_DEPUTY_HERO:
                if (!CheckNull.isEmpty(affectedForce.assistantHeroList)) {
                    heroList.addAll(affectedForce.assistantHeroList.stream().map(FightAssistantHero::getHeroId).collect(Collectors.toList()));
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
                heroList.add(randomHeroIdList.get(RandomHelper.randomInSize(randomHeroIdList.size())));
                break;
            case ALL_MY_HERO:
            case ALL_ENEMY_HERO:
                heroList.add(affectedForce.id);
                if (!CheckNull.isEmpty(affectedForce.assistantHeroList)) {
                    heroList.addAll(affectedForce.assistantHeroList.stream().map(FightAssistantHero::getHeroId).collect(Collectors.toList()));
                }
                break;
        }
    }

    /**
     * 触发类型buff
     *
     * @param contextHolder
     * @param timing
     */
    public static void releaseAllBuffEffect(FightContextHolder contextHolder, int timing) {
        EventBus.getDefault().post(new FightEvent.BuffTriggerEvent(contextHolder, timing, null));
    }

    /**
     * 触发类型的buff
     *
     * @param contextHolder
     * @param timing
     * @param params
     */
    public static void releaseAllBuffEffect(FightContextHolder contextHolder, int timing, Object[] params) {
        EventBus.getDefault().post(new FightEvent.BuffTriggerEvent(contextHolder, timing, params));
    }

    public static CommonPb.Form createFormPb(Force force) {
        CommonPb.Form.Builder formPb = CommonPb.Form.newBuilder();
        formPb.setId(force.id);
        formPb.setCount(force.hp);
        formPb.setLine(force.maxLine);
        formPb.setCamp(force.getCamp());
        formPb.setHeroType(force.roleType);
        formPb.setCurLine(force.curLine);
        formPb.setIntensifyLv(force.getIntensifyLv() == 0 ? 1 : force.getIntensifyLv());
        return formPb.build();
    }

    public static void packAura(Fighter fighter, FightContextHolder contextHolder) {
        CommonPb.Record.Builder recordData = contextHolder.getRecordData();
        for (Map.Entry<Long, List<AuraInfo>> entry : fighter.auraInfos.entrySet()) {
            Long roleId = entry.getKey();
            for (AuraInfo auraInfo : entry.getValue()) {
                CommonPb.Aura auraPb = createAuraPb(roleId, auraInfo);
                recordData.addAura(auraPb);
            }
        }
    }

    public static CommonPb.Aura createAuraPb(long roleId, AuraInfo auraInfo) {
        CommonPb.Aura.Builder auraPb = CommonPb.Aura.newBuilder();
        auraPb.setRoleId(roleId);
        auraPb.setHeroId(auraInfo.getHeroId());
        auraPb.setId(auraInfo.getMedalAuraId());
        auraPb.setNick(auraInfo.getNick());
        return auraPb.build();
    }
}
