package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
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
    @Autowired
    private StaticFightManager staticFightManager;

    /**
     * 释放buff通用逻辑
     *
     * @param buffs          buff挂载者的buff列表
     * @param staticBuff     当前buff的配置
     * @param removeBuffList 即将移除buff的列表
     * @param actingForce    buff挂载者
     * @param heroId         buff挂载者武将id
     * @param contextHolder  战斗上下文
     * @return
     */
    public IFightBuff releaseBuff(LinkedList<IFightBuff> buffs, StaticBuff staticBuff,
                                  List<IFightBuff> removeBuffList, Force actingForce, int heroId,
                                  FightContextHolder contextHolder, List<Integer> buffConfig, Object... params) {
        boolean removed;
        boolean addBuff = true;
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
                                FightBuffEffect fightBuffEffect = actingForce.getFightEffectMap(heroId);
                                for (List<Integer> effectConfig : staticBuff.getEffects()) {
                                    if (CheckNull.isEmpty(effectConfig))
                                        continue;
                                    StaticEffectRule rule = staticFightManager.getStaticEffectRule(effectConfig.get(2));
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
        fightBuff.setBuffGiver(contextHolder.getAttacker());
        fightBuff.setForceId(heroId);

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
    public void releaseSkill(Force atk, FightEntity fe, FightContextHolder contextHolder) {
        // 能量恢复, 在释放技能前
        int lowerEnergy = atk.calLowerEnergyCharging(atk.actionId);
        int upperEnergy = atk.calUpperEnergyCharging(atk.actionId);
        int upper = Math.max(lowerEnergy, upperEnergy);
        int lower = Math.min(lowerEnergy, upperEnergy);
        int randomValue = lower + RandomHelper.randomInSize(upper - lower + 1);
        int recoveryValue = FightCalc.skillEnergyRecovery(atk, atk.actionId, randomValue);

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

    public void skillAttack(FightContextHolder contextHolder, int battleType) {
        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_SKILL_DAMAGE);
        // TODO 扣血
        

        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_SKILL_DAMAGE);
    }


    /**
     * 普攻, 随机敌方一个武将攻击
     *
     * @param atk
     * @param def
     */
    public void ordinaryAttack(Force atk, Force def, List<Integer> targetList, int battleType) {
        def.beActionId.clear();
        targetList.add(def.id);
        if (!CheckNull.isEmpty(def.assistantHeroList)) {
            def.assistantHeroList.forEach(ass -> targetList.add(ass.getHeroId()));
        }
        def.beActionId.add(targetList.get(RandomHelper.randomInSize(targetList.size())));
        // 计算普攻伤害
        hurt(atk, def, def.beActionId.get(0), FightCalc.calAttack(atk, def, battleType));
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
     * @param attacker
     * @param defender
     * @param targetId 被打的武将id
     * @param damage
     */
    public void hurt(Force attacker, Force defender, int targetId, int damage) {

    }
}
