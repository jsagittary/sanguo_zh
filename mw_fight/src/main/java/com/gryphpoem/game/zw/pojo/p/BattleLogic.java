package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
                                  FightContextHolder contextHolder, List<Integer> buffConfig, Object... params) {
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
        LogUtil.fight("执行方: ", actionDirection.getAtk().ownerId, ", 被执行方: ", actingForce.ownerId, "的武将: ", actingForce.id, "加buff: ", fightBuff.getBuffConfig());
        // 触发buff
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.SPECIFIED_BUFF_ID_EXISTS);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BUFF_GROUP_EXISTS);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BUFF_GROUP_NUM_EXISTS);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.SPECIFY_BUFF_TO_STACK_TO_THE_SPECIFIED_LAYER_NUM);
        fightBuff.releaseBuff(buffs, contextHolder, buffConfig, params);
        if (!CheckNull.isEmpty(removeBuffList)) {
            buffs.removeAll(removeBuffList);
            // TODO 预留buff失效还原逻辑
            removeBuffList.forEach(buff -> buff.buffLoseEffectiveness(contextHolder, params));
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

        List<SimpleHeroSkill> skillList = atk.getSkillList(fe.getHeroId()).stream().filter(skill -> Objects.nonNull(skill) && !skill.isOnStageSkill()).collect(Collectors.toList());
        if (!CheckNull.isEmpty(skillList)) {
            skillList.forEach(skill -> skill.setCurEnergy(skill.getCurEnergy() + recoveryValue));
        }
        if (atk.canReleaseSkill(fe.getHeroId())) {
            // 释放技能
            skillList = atk.getSkillList(fe.getHeroId()).stream().filter(skill -> skill.getCurEnergy() >= skill.getS_skill().getReleaseNeedEnergy()).collect(Collectors.toList());
            if (!CheckNull.isEmpty(skillList)) {
                skillList.forEach(skill -> skill.releaseSkill(contextHolder));
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
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_SKILL_DAMAGE);
        // TODO 扣血
        hurt(actionDirection, contextHolder,
                FightCalc.calSkillAttack(actionDirection, effectConfig, actionDirection.getCurDefHeroId(), battleType));

        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_SKILL_DAMAGE);
    }


    /**
     * 普攻, 随机敌方一个武将攻击
     *
     * @param contextHolder
     * @param targetList
     * @param battleType
     */
    public void ordinaryAttack(Force atk, Force def, int atkHeroId, List<Integer> targetList, int battleType, FightContextHolder contextHolder) {
        // 触发buff
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_SKILL_DAMAGE);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BEING_ATTACKED);
        targetList.clear();
        targetList.add(def.id);
        if (!CheckNull.isEmpty(def.assistantHeroList)) {
            def.assistantHeroList.forEach(ass -> targetList.add(ass.getHeroId()));
        }
        int targetId = targetList.get(RandomHelper.randomInSize(targetList.size()));
        contextHolder.resetActionDirection(atk, def, atkHeroId, targetId);

        // 计算普攻伤害
        hurt(contextHolder.getActionDirection(), contextHolder, FightCalc.calAttack(contextHolder.getActionDirection(), battleType));
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_SKILL_DAMAGE);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BEING_ATTACKED);
    }

    /**
     * buff连击
     *
     * @param contextHolder
     * @param battleType
     */
    public void ordinaryAttack(ActionDirection actionDirection, FightContextHolder contextHolder, int battleType) {
        // 触发buff
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_SKILL_DAMAGE);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BEING_ATTACKED);
        // 计算普攻伤害
        hurt(actionDirection, contextHolder, FightCalc.calAttack(actionDirection, battleType));
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_SKILL_DAMAGE);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BEING_ATTACKED);
    }

    /**
     * 回合士气扣除
     *
     * @param force
     * @param target
     */
    public void roundMoraleDeduction(Force force, Force target) {
        force.morale -= oneSideMoraleDeduction(force);
        if (force.morale < 0)
            force.morale = 0;
        target.morale -= oneSideMoraleDeduction(target);
        if (target.morale < 0)
            target.morale = 0;
        // TODO pb

    }

    /**
     * 一方士气回合变化
     *
     * @param force
     * @return
     */
    private double oneSideMoraleDeduction(Force force) {
        double originValue = force.maxRoundMorale * 0.5d / FightConstant.HUNDRED;
        return FightCalc.moraleCorrection(force, force.id, FightConstant.EffectLogicId.MORALE_DEDUCTION_VALUE_INCREASED,
                FightConstant.EffectLogicId.REDUCED_MORALE_DEDUCTION, originValue);
    }


    /**
     * 伤害计算通用接口
     *
     * @param contextHolder
     * @param damage
     */
    public void hurt(ActionDirection actionDirection, FightContextHolder contextHolder, int damage) {
        // 扣血前
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BEING_HIT);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_BLEEDING);

        // TODO 扣血
        actionDirection.getAtk().killed += damage;
        actionDirection.getDef().lost = damage;
        actionDirection.getAtk().fighter.hurt += damage;
        actionDirection.getDef().fighter.lost += damage;
        actionDirection.getDef().subHp(actionDirection.getAtk());

        // 扣血后
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BEING_HIT);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_BLEEDING);
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BLOOD_VOLUME_BELOW_PERCENTAGE);
    }
}
