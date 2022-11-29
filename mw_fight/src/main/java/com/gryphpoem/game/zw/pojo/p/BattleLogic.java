package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.skill.IHeroSkill;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.game.zw.util.FightPbUtil;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-04 18:34
 */
@Component
public class BattleLogic {
    @Autowired
    private FightManager fightManager;

    /**
     * 释放buff通用逻辑
     *
     * @param buffs          buff挂载者的buff列表
     * @param staticBuff     当前buff的配置
     * @param removeBuffList 即将移除buff的列表
     * @param contextHolder  战斗上下文
     * @return
     */
    public IFightBuff releaseBuff(LinkedList<IFightBuff> buffs, StaticBuff staticBuff,
                                  List<IFightBuff> removeBuffList, ActionDirection actionDirection,
                                  FightContextHolder contextHolder, List<Integer> buffConfig, IHeroSkill heroSkill, Object... params) {
        boolean removed;
        boolean addBuff = true;
        Force actingForce = actionDirection.getDef();
        if (!CheckNull.isEmpty(buffs)) {
            for (IFightBuff fightBuff : buffs) {
                if (CheckNull.isNull(fightBuff)) continue;
                if (!fightBuff.buffCoexistenceCheck(staticBuff, removeBuffList)) {
                    addBuff = false;
                    break;
                }
            }
        }

        if (!addBuff) {
            return null;
        }

        // buff列表是否重复
        if (!CheckNull.isEmpty(buffs)) {
            List<IFightBuff> sameIdBuffList = buffs.stream().filter(b ->
                    b.getBuffConfig().getBuffId() == staticBuff.getBuffId()).collect(Collectors.toList());
            if (!CheckNull.isEmpty(sameIdBuffList)) {
                if (sameIdBuffList.size() >= staticBuff.getCoexistingIdNum()) {
                    switch (staticBuff.getSameIdReplacementRule()) {
                        // 比较buff留存规则
                        case FightConstant.ReplacementBuffRule.LONGER_ROUNDS:
                            // 保留更长回合数buff
                            IFightBuff fightBuff = sameIdBuffList.stream().filter(b ->
                                            b.getBuffEffectiveRounds() <= staticBuff.getContinuousRound()).
                                    min(Comparator.comparingInt(IFightBuff::getBuffEffectiveRounds)).
                                    orElse(null);
                            if (Objects.nonNull(fightBuff)) removeBuffList.add(fightBuff);
                            break;
                        case FightConstant.ReplacementBuffRule.MORE_STRONG:
                            // 保留更强buff
                            if (!CheckNull.isEmpty(staticBuff.getEffects())) {
                                removed = false;
                                IFightBuff removedBuff;
                                FightBuffEffect fightBuffEffect = actingForce.getFightEffectMap(actionDirection.getCurDefHeroId());
                                for (List<Integer> effectConfig : staticBuff.getEffects()) {
                                    if (CheckNull.isEmpty(effectConfig))
                                        continue;
                                    StaticEffectRule rule = StaticFightManager.getStaticEffectRule(effectConfig.get(2));
                                    if (CheckNull.isNull(rule))
                                        continue;
                                    IFightEffect fightEffect = fightManager.getSkillEffect(rule.getEffectLogicId());
                                    if (CheckNull.isNull(fightEffect)) continue;
                                    if ((removedBuff = fightEffect.compareTo(sameIdBuffList, effectConfig, fightBuffEffect, contextHolder)) != null) {
                                        removeBuffList.add(removedBuff);
                                        removed = true;
                                        break;
                                    }
                                }
                                if (!removed) addBuff = false;
                            }
                            break;
                    }
                }
            }
        }

        if (!addBuff)
            return null;

        IFightBuff fightBuff = fightManager.createFightBuff(staticBuff.getBuffEffectiveWay(), staticBuff);
        fightBuff.setForce(actingForce);
        fightBuff.setForceId(actionDirection.getCurDefHeroId());
        fightBuff.setBuffGiver(actionDirection.getAtk());
        fightBuff.setBuffGiverId(actionDirection.getCurAtkHeroId());
        fightBuff.setSkill(heroSkill);
        contextHolder.addBuff(fightBuff);

        LogUtil.fight("执行方: ", actionDirection.getAtk().ownerId, ", 被执行方: ", actingForce.ownerId, "的武将: ",
                actingForce.id, ", 加buff: ", fightBuff.getBuffConfig());
        // 触发buff
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BUFF_GROUP_EXISTS);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BUFF_GROUP_NUM_EXISTS);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.SPECIFIED_BUFF_ID_EXISTS);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.SPECIFY_BUFF_TO_STACK_TO_THE_SPECIFIED_LAYER_NUM);
        fightBuff.releaseBuff(buffs, contextHolder, buffConfig, params);
        if (!CheckNull.isEmpty(removeBuffList)) {
            buffs.removeAll(removeBuffList);
            // TODO 预留buff失效还原逻辑
            removeBuffList.forEach(buff -> {
                buff.buffLoseEffectiveness(contextHolder, params);
            });
            // TODO 客户端表现PB 处理
            removeBuffList.clear();
        }
        return fightBuff;
    }

    /**
     * 释放技能
     *
     * @param atk
     * @param fe
     * @param contextHolder
     */
    public void releaseSkill(Force atk, Force def, FightEntity fe, int atkHeroId, FightContextHolder contextHolder) {
        contextHolder.resetActionDirection(atk, def, atkHeroId, 0);
        // 能量恢复, 在释放技能前
        int lowerEnergy = atk.calLowerEnergyCharging(atkHeroId);
        int upperEnergy = atk.calUpperEnergyCharging(atkHeroId);
        int upper = Math.max(lowerEnergy, upperEnergy);
        int lower = Math.min(lowerEnergy, upperEnergy);
        int randomValue = lower + RandomHelper.randomInSize(upper - lower + 1);
        int recoveryValue = FightCalc.skillEnergyRecovery(atk, atkHeroId, randomValue);

        // 主动技能充能, 找到能量足够的主动技能列表
        List<SimpleHeroSkill> simpleHeroSkills = atk.getSkillList(fe.getHeroId());
        if (!CheckNull.isEmpty(simpleHeroSkills)) {
            List<SimpleHeroSkill> skillList = simpleHeroSkills.stream().filter(skill -> {
                if (Objects.isNull(skill) || skill.isOnStageSkill())
                    return false;
                skill.setCurEnergy(skill.getCurEnergy() + recoveryValue);
                return skill.getCurEnergy() >= skill.getS_skill().getReleaseNeedEnergy();
            }).collect(Collectors.toList());
            if (atk.canReleaseSkill(fe.getHeroId()) && !CheckNull.isEmpty(skillList)) {
                // 释放技能
                skillList.forEach(skill -> {
                    skill.releaseSkill(contextHolder);
                });
            }
        }
    }

    /**
     * 技能攻击
     *
     * @param contextHolder
     * @param effectConfig
     * @param battleType
     */
    public void skillAttack(ActionDirection actionDirection, FightContextHolder contextHolder, List<Integer> effectConfig, int battleType) {
        Object[] effectParams = new Object[]{actionDirection};
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BEING_HIT, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_SKILL_DAMAGE, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BLEEDING, effectParams);

        // TODO 扣血
        hurt(actionDirection, FightCalc.calSkillAttack(actionDirection, effectConfig, battleType), contextHolder);

        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BEING_HIT, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_SKILL_DAMAGE, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BLEEDING, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BLOOD_VOLUME_BELOW_PERCENTAGE);
    }


    /**
     * 普攻, 随机敌方一个武将攻击
     *
     * @param contextHolder
     * @param targetList
     * @param battleType
     */
    public void ordinaryAttack(Force atk, Force def, int atkHeroId, List<Integer> targetList, int battleType, FightContextHolder contextHolder) {
        targetList.clear();
        targetList.add(def.id);
        if (!CheckNull.isEmpty(def.assistantHeroList)) {
            def.assistantHeroList.forEach(ass -> targetList.add(ass.getHeroId()));
        }
        int targetId = targetList.get(RandomHelper.randomInSize(targetList.size()));
        contextHolder.resetActionDirection(atk, def, atkHeroId, targetId);

        // 触发buff
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_GENERAL_ATTACK);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BEING_HIT);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BEING_ATTACKED);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BLEEDING);

        // 计算普攻伤害
        hurt(contextHolder.getActionDirection(), FightCalc.calAttack(contextHolder.getActionDirection(), battleType), contextHolder);

        // 触发buff
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BEING_HIT);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BEING_ATTACKED);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BLEEDING);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BLOOD_VOLUME_BELOW_PERCENTAGE);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_GENERAL_ATTACK);
    }

    /**
     * buff连击
     *
     * @param contextHolder
     * @param battleType
     */
    public void buffOrdinaryAttack(ActionDirection actionDirection, FightContextHolder contextHolder, int battleType) {
        // 触发buff
        Object[] effectParams = new Object[]{actionDirection};
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_GENERAL_ATTACK, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BEING_HIT, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BEING_ATTACKED, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BLEEDING, effectParams);

        // 计算普攻伤害
        hurt(actionDirection, FightCalc.calAttack(actionDirection, battleType), contextHolder);

        // 触发buff
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BEING_HIT, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BEING_ATTACKED, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BLEEDING, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BLOOD_VOLUME_BELOW_PERCENTAGE);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_GENERAL_ATTACK, effectParams);
    }

    /**
     * buff反击伤害
     *
     * @param actionDirection
     * @param contextHolder
     * @param effectConfig
     */
    public void buffCounterAttack(ActionDirection actionDirection, FightContextHolder contextHolder, List<Integer> effectConfig) {
        // 触发buff
        Object[] effectParams = new Object[]{actionDirection};
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_GENERAL_ATTACK, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BEING_HIT, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BEING_ATTACKED, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BLEEDING, effectParams);

        // 计算普攻伤害
        hurt(actionDirection, FightCalc.calCounterAttack(actionDirection, effectConfig, contextHolder.getBattleType()), contextHolder);

        // 触发buff
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BEING_HIT, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BEING_ATTACKED, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BLEEDING, effectParams);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BLOOD_VOLUME_BELOW_PERCENTAGE);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_GENERAL_ATTACK, effectParams);
    }

    /**
     * 回合士气扣除
     *
     * @param force
     * @param target
     */
    public void roundMoraleDeduction(Force force, Force target) {
//        force.morale -= oneSideMoraleDeduction(force);
//        if (force.morale < 0)
//            force.morale = 0;
//        target.morale -= oneSideMoraleDeduction(target);
//        if (target.morale < 0)
//            target.morale = 0;
        // TODO pb

    }

    /**
     * 一方士气回合变化
     *
     * @param force
     * @return
     */
    private double oneSideMoraleDeduction(Force force) {
//        double originValue = force.maxRoundMorale * 0.5d / FightConstant.HUNDRED;
//        return FightCalc.moraleCorrection(force, force.id, FightConstant.EffectLogicId.MORALE_DEDUCTION_VALUE_INCREASED,
//                FightConstant.EffectLogicId.REDUCED_MORALE_DEDUCTION, originValue);
        return 0d;
    }


    /**
     * 伤害计算通用接口
     *
     * @param damage
     */
    public void hurt(ActionDirection actionDirection, int damage, FightContextHolder contextHolder) {
        // TODO 扣血
        damage = actionDirection.getDef().hurt(damage);
        actionDirection.getAtk().killed += damage;
        actionDirection.getDef().lost = damage;
        actionDirection.getAtk().fighter.hurt += damage;
        actionDirection.getDef().fighter.lost += damage;
        actionDirection.getDef().addRoundLost(damage);
        actionDirection.getDef().subHp(actionDirection.getAtk());
        deductMorale(actionDirection, damage);
        if (actionDirection.getSkill() == null) {
            actionDirection.getAtk().addAttackDamage(damage, actionDirection.getCurAtkHeroId());
        } else {
            actionDirection.getSkill().addSkillDamage(damage);
        }

        BattlePb.ActionResult.Builder builder = BattlePb.ActionResult.newBuilder();
        builder.setAtk(FightPbUtil.getActingSize(actionDirection.getAtk(), actionDirection.getCurAtkHeroId()));
        builder.setDef(FightPbUtil.getActingSize(actionDirection.getDef(), actionDirection.getCurDefHeroId()));
        builder.setLost(damage);
        builder.setRemainArms(actionDirection.getDef().hp);
        if (Objects.nonNull(actionDirection.getSkill())) {
            builder.setSkillId(actionDirection.getSkill().getS_skill().getSkillId());
        }

        FightPbUtil.setActionResult(contextHolder, builder.build());
    }

    /**
     * 掉血时扣除士气值
     *
     * @param actionDirection
     * @param damage
     */
    private void deductMorale(ActionDirection actionDirection, int damage) {
//        Force force = actionDirection.getDef();
//        int heroId = actionDirection.getCurDefHeroId();
//        int beforeReducedMorale = force.morale;
//        int reducedMorale = FightCalc.moraleCorrection(force, heroId, FightConstant.EffectLogicId.MORALE_DEDUCTION_VALUE_INCREASED,
//                FightConstant.EffectLogicId.REDUCED_MORALE_DEDUCTION, damage);
//        force.morale -= reducedMorale;
//        if (force.morale < 0)
//            force.morale = 0;
//        LogUtil.fight("扣血时, 执行士气扣除效果, 士气扣除方: ", force.ownerId,
//                ", 武将: ", heroId, ", 扣除的士气: ", reducedMorale,
//                ", 扣除前士气: ", beforeReducedMorale, ", 扣除后士气: ", force.morale);
    }

//    /**
//     * 战斗开始时, 初始化武将士气
//     */
//    public void battleStart(FightContextHolder contextHolder) {
//        initMorale(contextHolder.getAtkFighter());
//        initMorale(contextHolder.getDefFighter());
//    }
//
//    private void initMorale(Fighter fighter) {
//        if (Objects.nonNull(fighter)) {
//            if (!CheckNull.isEmpty(fighter.getForces())) {
//                for (Force force : fighter.getForces()) {
//                    force.morale = force.hp * 2;
//                }
//            }
//        }
//    }

    /**
     * 计算buff
     *
     * @param buffList
     */
    public void settlementBuff(LinkedList<IFightBuff> buffList, FightContextHolder contextHolder) {
        if (!CheckNull.isEmpty(buffList)) {
            Iterator<IFightBuff> it = buffList.iterator();
            while (it.hasNext()) {
                IFightBuff fightBuff = it.next();
                if (CheckNull.isNull(fightBuff)) {
                    it.remove();
                    continue;
                }
                if (!fightBuff.hasRemainBuffRoundTimes(contextHolder)) {
                    fightBuff.buffLoseEffectiveness(contextHolder);
                    it.remove();
                    continue;
                }
            }
        }
    }

    /**
     * 扣除buff回合次数
     *
     * @param buffList
     */
    public void deductBuffRounds(LinkedList<IFightBuff> buffList) {
        if (CheckNull.isEmpty(buffList)) return;
        buffList.forEach(fightBuff -> fightBuff.deductBuffRounds());
    }

    /**
     * 下回合之前, 当前回合结束之后
     *
     * @param force
     * @param target
     */
    public void nextRoundBefore(Force force, Force target, FightContextHolder contextHolder) {
        // 清除战败武将施加的buff
        clearDeadBuff(force, target, contextHolder);
        // 战败武将去掉光环
        subAuraSkill(force, target);
        // 清除出手顺序列表
        if (!CheckNull.isEmpty(contextHolder.getFightEntity()))
            contextHolder.getFightEntity().clear();
        // 清除回合数
        contextHolder.clearRoundNum();
    }

    /**
     * 有一方战败, 清除战败方施加的buff
     *
     * @param force
     * @param target
     */
    private void clearDeadBuff(Force force, Force target, FightContextHolder contextHolder) {
        if (!force.alive() && !target.alive()) {
            if (CheckNull.isEmpty(contextHolder.getBuffMap()))
                return;
            contextHolder.getBuffMap().clear();
            return;
        }
        if (!force.alive()) {
            clearForceBuff(force, target, contextHolder);
        } else {
            clearForceBuff(target, force, contextHolder);
        }
    }

    /**
     * 清除战败武将施加的buff
     *
     * @param force
     * @param target
     */
    private void clearForceBuff(Force force, Force target, FightContextHolder contextHolder) {
        Iterator<IFightBuff> it = target.buffList.iterator();
        while (it.hasNext()) {
            IFightBuff fightBuff = it.next();
            if (CheckNull.isNull(fightBuff)) continue;
            if (fightBuff.getBuffGiver().ownerId == force.ownerId) {
                fightBuff.buffLoseEffectiveness(contextHolder);
                it.remove();
            }
        }

        if (!CheckNull.isEmpty(target.assistantHeroList)) {
            for (FightAssistantHero assistantHero : target.assistantHeroList) {
                it = assistantHero.getBuffList().iterator();
                while (it.hasNext()) {
                    IFightBuff fightBuff = it.next();
                    if (CheckNull.isNull(fightBuff)) continue;
                    if (fightBuff.getBuffGiver().ownerId == force.ownerId) {
                        fightBuff.buffLoseEffectiveness(contextHolder);
                        it.remove();
                    }
                }
            }
        }
    }

    /**
     * 阵亡的将领去掉光环效果
     *
     * @param force
     * @param target
     */
    private void subAuraSkill(Force force, Force target) {
        // 双方都存活
        if (force.alive() && target.alive()) {
            return;
        }
        // 阵亡的将领去掉光环效果
        target.fighter.subAuraSkill(target);
        force.fighter.subAuraSkill(force);
    }

    /**
     * 释放主动buff
     *
     * @param buffList
     * @param contextHolder
     */
    public void releaseSingleBuff(LinkedList<IFightBuff> buffList, FightContextHolder contextHolder, int timing) {
        if (CheckNull.isEmpty(buffList)) return;
        buffList.forEach(buff -> {
            if (CheckNull.isNull(buff) || (timing != FightConstant.BuffEffectTiming.ROUND_START && timing !=
                    FightConstant.BuffEffectTiming.START_OF_DESIGNATED_ROUND))
                return;
            buff.releaseEffect(contextHolder, timing);
        });
    }

    /**
     * 清除双方没有作用次数的buff
     *
     * @param force
     * @param target
     * @param contextHolder
     */
    public void clearBothForceBuffEffectTimes(Force force, Force target, FightContextHolder contextHolder) {
        clearForceBuffEffectTimes(force, contextHolder);
        clearForceBuffEffectTimes(target, contextHolder);
    }

    /**
     * 清除没有作用次数的buff
     *
     * @param force
     */
    private void clearForceBuffEffectTimes(Force force, FightContextHolder contextHolder) {
        clearNoBuffEffectTimes(force.buffList, contextHolder);
        if (!CheckNull.isEmpty(force.assistantHeroList)) {
            for (FightAssistantHero assistantHero : force.assistantHeroList) {
                if (CheckNull.isNull(assistantHero)) continue;
                clearNoBuffEffectTimes(assistantHero.getBuffList(), contextHolder);
            }
        }
    }

    /**
     * 清除没有作用次数的buff
     *
     * @param buffList
     */
    private void clearNoBuffEffectTimes(LinkedList<IFightBuff> buffList, FightContextHolder contextHolder) {
        if (CheckNull.isEmpty(buffList)) return;
        Iterator<IFightBuff> it = buffList.iterator();
        while (it.hasNext()) {
            IFightBuff fightBuff = it.next();
            if (CheckNull.isNull(fightBuff)) continue;
            if (fightBuff.hasRemainEffectiveTimes(contextHolder)) continue;
            fightBuff.buffLoseEffectiveness(contextHolder);
            it.remove();
        }
    }

    /**
     * 双方武将新的回合开始
     *
     * @param force
     * @param target
     */
    public void newRoundStart(Force force, Force target) {
        clearEffectiveTimesSingleRound(force);
        clearEffectiveTimesSingleRound(target);
    }

    private void clearEffectiveTimesSingleRound(Force force) {
        if (!CheckNull.isEmpty(force.buffList)) {
            force.buffList.forEach(fightBuff -> {
                if (CheckNull.isNull(fightBuff)) return;
                fightBuff.clearEffectiveTimesSingleRound();
            });
        }
        if (!CheckNull.isEmpty(force.assistantHeroList)) {
            force.assistantHeroList.forEach(ass -> {
                if (CheckNull.isNull(ass)) return;
                if (CheckNull.isEmpty(ass.getBuffList())) return;
                ass.getBuffList().forEach(fightBuff -> {
                    if (CheckNull.isNull(fightBuff)) return;
                    fightBuff.clearEffectiveTimesSingleRound();
                });
            });
        }
    }

}
