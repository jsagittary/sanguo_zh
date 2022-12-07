package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.cross.constants.FightCommonConstant;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSkill;
import com.gryphpoem.push.util.CheckNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author TanDonghai
 * @ClassName FightCalc.java
 * @Description 战斗数值计算
 * @date 创建时间: 2017年4月1日 下午3:12:07
 */
public class FightCalc {

    /**
     * 士气值修正
     *
     * @param force
     * @param heroId
     * @param logicId1
     * @param logicId2
     * @param originValue
     * @return
     */
    public static final int moraleCorrection(Force force, int heroId, int logicId1, int logicId2, double originValue) {
        int tenThousandthRatio1 = 0, fixValue1 = 0, tenThousandthRatio2 = 0, fixValue2 = 0;
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        IFightEffect fightEffect1 = fightManager.getSkillEffect(logicId1);
        IFightEffect fightEffect2 = fightManager.getSkillEffect(logicId2);

        FightBuffEffect fightBuffEffect = force.getFightEffectMap(heroId);
        if (Objects.nonNull(fightEffect1)) {
            Turple<Integer, Integer> var65 = (Turple<Integer, Integer>) fightEffect1.effectCalculateValue(fightBuffEffect,
                    logicId1, originValue);
            if (Objects.nonNull(var65)) {
                tenThousandthRatio1 = var65.getA();
                fixValue1 = var65.getB();
            }
        }

        if (Objects.nonNull(fightEffect2)) {
            Turple<Integer, Integer> var66 = (Turple<Integer, Integer>) fightEffect2.effectCalculateValue(fightBuffEffect,
                    logicId2, originValue);
            if (Objects.nonNull(var66)) {
                tenThousandthRatio2 = var66.getA();
                fixValue2 = var66.getB();
            }
        }

        // originValue *（1+士气恢复值提升万分比【效果1】-士气恢复值降低万分比【效果2】）+士气恢复值提升固定值【效果1】-士气恢复值降低固定值【效果2】
        return (int) (originValue * (1 +
                ((tenThousandthRatio1 - tenThousandthRatio2) / FightConstant.TEN_THOUSAND)) + fixValue1 - fixValue2);
    }

    /**
     * 技能能量槽恢复
     *
     * @param force
     * @param heroId
     * @return
     */
    public static final int skillEnergyRecovery(Force force, int heroId, double originValue) {
        int tenThousandthRatio65 = 0, fixValue65 = 0, tenThousandthRatio66 = 0, fixValue66 = 0;
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        IFightEffect fightEffect65 = fightManager.getSkillEffect(FightConstant.EffectLogicId.ENERGY_RECOVERY_VALUE_INCREASED);
        IFightEffect fightEffect66 = fightManager.getSkillEffect(FightConstant.EffectLogicId.ENERGY_RECOVERY_VALUE_DECREASES);

        FightBuffEffect fightBuffEffect = force.getFightEffectMap(heroId);
        if (Objects.nonNull(fightEffect65)) {
            Turple<Integer, Integer> var65 = (Turple<Integer, Integer>) fightEffect65.effectCalculateValue(fightBuffEffect,
                    FightConstant.EffectLogicId.ENERGY_RECOVERY_VALUE_INCREASED, originValue);
            if (Objects.nonNull(var65)) {
                tenThousandthRatio65 = var65.getA();
                fixValue65 = var65.getB();
            }
        }

        if (Objects.nonNull(fightEffect66)) {
            Turple<Integer, Integer> var66 = (Turple<Integer, Integer>) fightEffect66.effectCalculateValue(fightBuffEffect,
                    FightConstant.EffectLogicId.ENERGY_RECOVERY_VALUE_DECREASES, originValue);
            if (Objects.nonNull(var66)) {
                tenThousandthRatio66 = var66.getA();
                fixValue66 = var66.getB();
            }
        }

        // 技能能量恢复=(能量槽上限*能量恢复万分比【效果4】+能量恢复固定值【效果4】)*（1+能量恢复值提升万分比【效果65】-能量恢复值降低万分比【效果66】）+能量恢复值提升固定值【效果65】-能量恢复值降低固定值【效果66】
        return (int) (originValue * (1 +
                ((tenThousandthRatio65 - tenThousandthRatio66) / FightConstant.TEN_THOUSAND)) + fixValue65 - fixValue66);
    }

    /**
     * 技能能量扣除 公式
     *
     * @param skill
     * @param effectConfig
     * @return
     */
    public static final int skillEnergyDeduction(StaticHeroSkill skill, List<Integer> effectConfig) {
        // 能量槽上限*能量扣除万分比【效果5】+能量扣除固定值【效果5】
        return (int) (skill.getEnergyUpperLimit() * effectConfig.get(4) / FightConstant.TEN_THOUSAND + effectConfig.get(5));
    }

    /**
     * 计算不同属性值在效果影响下结果
     *
     * @param attrId
     * @param force
     * @param heroId
     * @return
     */
    public static final double attributeValue(int attrId, Force force, int heroId) {
        switch (attrId) {
            case FightCommonConstant.AttrId.ATTACK:
                return calAttrValue(force, heroId, attrId, force.calcAttack(heroId), FightConstant.EffectLogicId.ATTACK_INCREASED, FightConstant.EffectLogicId.REDUCED_ATTACK);
            case FightCommonConstant.AttrId.DEFEND:
                return calAttrValue(force, heroId, attrId, force.calcDefend(heroId), FightConstant.EffectLogicId.DEFENSE_INCREASED, FightConstant.EffectLogicId.REDUCED_DEFENSE);
            case FightCommonConstant.AttrId.SPEED:
                return calAttrValue(force, heroId, attrId, force.calSpeed(heroId), FightConstant.EffectLogicId.SPEED_INCREASE, FightConstant.EffectLogicId.SPEED_REDUCTION);
            case FightCommonConstant.AttrId.ATTACK_TOWN:
                return calAttrValue(force, heroId, attrId, force.calcAtkTown(heroId), FightConstant.EffectLogicId.BROKEN_CITY_PROMOTION, FightConstant.EffectLogicId.BROKEN_CITY_REDUCED);
            case FightCommonConstant.AttrId.DEFEND_TOWN:
                return calAttrValue(force, heroId, attrId, force.calcDefTown(heroId), FightConstant.EffectLogicId.UPWARD_GUARDING, FightConstant.EffectLogicId.GUARD_CITY_REDUCED);
            default:
                return 0d;
        }
    }

    /**
     * 计算属性值 (属性+e1-e2)
     *
     * @param force
     * @param heroId
     * @param effectLogicId1
     * @param effectLogicId2
     * @return
     */
    private static final double calAttrValue(Force force, int heroId, int attrId, int attrValue, int effectLogicId1, int effectLogicId2) {
        int tenThousandthRatio_ = 0, fixValue_ = 0, tenThousandthRatio = 0, fixValue = 0;
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        FightBuffEffect fightBuffEffect = force.getFightEffectMap(heroId);

        IFightEffect fightEffect_ = fightManager.getSkillEffect(effectLogicId1);
        IFightEffect fightEffect = fightManager.getSkillEffect(effectLogicId2);
        if (Objects.nonNull(fightEffect_)) {
            Turple<Integer, Integer> var_ = (Turple<Integer, Integer>) fightEffect_.effectCalculateValue(fightBuffEffect, effectLogicId1);
            if (Objects.nonNull(var_)) {
                tenThousandthRatio_ = var_.getA();
                fixValue_ = var_.getB();
            }
        }
        if (Objects.nonNull(fightEffect)) {
            Turple<Integer, Integer> var = (Turple<Integer, Integer>) fightEffect.effectCalculateValue(fightBuffEffect, effectLogicId2);
            if (Objects.nonNull(var)) {
                tenThousandthRatio = var.getA();
                fixValue = var.getB();
            }
        }
        double attributeVal = attrValue * (1 + ((
                tenThousandthRatio_ - tenThousandthRatio) / FightConstant.TEN_THOUSAND)) + fixValue_ - fixValue;
        LogUtil.fight("战斗伤害计算-计算属性值, 属性id: ", attrId, ", 效果", effectLogicId1, " 万分比: ", tenThousandthRatio_,
                ", 固定值: ", fixValue_, "; 效果", effectLogicId2, " 万分比: ", tenThousandthRatio, ", 固定值: ", fixValue,
                ", 归属方: ", force.ownerId, ", 武将id: ", heroId, ", 原属性值: ", attrValue, ", 效果加成后: ", attributeVal);
        return attributeVal;
    }

    /**
     * 计算技能伤害
     *
     * @param effectConfig
     * @param battleType
     * @return
     */
    public static int calSkillAttack(ActionDirection actionDirection, List<Integer> effectConfig, int battleType) {
        LogUtil.fight("******进攻方: ", actionDirection.getAtk().ownerId, "-", actionDirection.getCurAtkHeroId(), ", 对防守方: ",
                actionDirection.getDef().ownerId, "-", actionDirection.getCurDefHeroId(), " 开始技能伤害攻击计算*******");
        // 基础伤害
        double baseHurt = baseHurt(actionDirection, battleType);
        // （基础伤害*伤害系数【效果3万分比】+固伤【效果3固定值】）
        baseHurt = baseHurt * (effectConfig.get(4) / FightConstant.TEN_THOUSAND) + effectConfig.get(5);
        LogUtil.fight("计算技能伤害公式部分-固伤部分, 攻击方基础伤害: ", baseHurt, ", 技能效果: ",
                Arrays.toString(effectConfig.toArray()), ", 计算完固伤部分的基础伤害: ", baseHurt);
        // 技能修正
        double skillCorrection = skillCorrection(actionDirection);
        // 终伤修正
        double finalDamageCorrection = finalDamageCorrection(actionDirection);
        // 兵种克制修正
        double armsRestraintCorrection = armsRestraintCorrection(actionDirection);
        // 技能暴击伤害修正
        double skillAttackCriticalDamageCorrection = skillAttackCriticalDamageCorrection(actionDirection);

        // 浮动修正=[0.9,1.1]
        double floatCorrection = RandomUtils.nextFloat(0.9f, 1.1f);
        LogUtil.fight("技能伤害参与计算部分-浮动修正, 修正值: ", floatCorrection);
        // 克制=1+克制关系系数
        double finalRestrain = getFinalRestrain(actionDirection, battleType);
        LogUtil.fight("技能伤害参与计算部分-克制, 克制值: ", finalRestrain);

        double damage = (baseHurt * skillCorrection * finalDamageCorrection * armsRestraintCorrection * skillAttackCriticalDamageCorrection *
                floatCorrection * finalRestrain);
        double damage_ = calDamageChange(actionDirection, damage);

        LogUtil.fight(
                "<<<<<<伤害结算--进攻方: ", actionDirection.getAtk().ownerId,
                "-", actionDirection.getCurAtkHeroId(),
                ", 对防守方: ", actionDirection.getDef().ownerId,
                "-", actionDirection.getCurDefHeroId(),
                " ,战斗回合===》战斗类型: ", battleType,
                ", 普攻伤害汇总结算, 基础伤害: ", baseHurt,
                ", 技能修正: ", skillCorrection,
                ", 重伤修正: ", finalDamageCorrection,
                ", 兵种克制修正: ", armsRestraintCorrection,
                ", 技能暴击伤害修正: ", skillAttackCriticalDamageCorrection,
                "， 浮动修正: ", floatCorrection,
                ", 克制: ", finalRestrain,
                ", 无敌或护盾效果作用前, 技能最终伤害值:  ", damage,
                ", 无敌或护盾效果作用后, 技能最终伤害值:  ", damage_,
                ">>>>>>");
        return (int) Math.ceil(damage_);
    }

    /**
     * 计算普攻伤害
     *
     * @param actionDirection
     * @param battleType
     */
    public static int calAttack(ActionDirection actionDirection, int battleType) {
        LogUtil.fight("*******进攻方: ", actionDirection.getAtk().ownerId, "-", actionDirection.getCurAtkHeroId(), ", 对防守方: ",
                actionDirection.getDef().ownerId, "-", actionDirection.getCurDefHeroId(), " 开始进行普攻计算******");
        // 基础伤害
        double baseHurt = baseHurt(actionDirection, battleType);
        // 血量衰减
        double bloodValueAttenuation = bloodValueAttenuation(actionDirection.getAtk());
        LogUtil.fight("普攻伤害参与计算部分-血量衰减, 修正值: ", bloodValueAttenuation);
        // 普攻修正
        double generalAttackCorrection = attackCorrection(actionDirection);
        // 终伤修正
        double finalDamageCorrection = finalDamageCorrection(actionDirection);
        // 兵种克制修正
        double armsRestraintCorrection = armsRestraintCorrection(actionDirection);
        // 普攻暴击伤害修正
        double attackCriticalDamageCorrection = attackCriticalDamageCorrection(actionDirection);

        // 浮动修正=[0.9,1.1]
        double floatCorrection = RandomUtils.nextFloat(0.9f, 1.1f);
        LogUtil.fight("普攻伤害参与计算部分-浮动修正, 修正值: ", floatCorrection);
        // 克制=1+克制关系系数
        double finalRestrain = getFinalRestrain(actionDirection, battleType);
        LogUtil.fight("普攻伤害参与计算部分-克制, 克制值: ", finalRestrain);

        double damage = (baseHurt * bloodValueAttenuation * generalAttackCorrection * finalDamageCorrection * armsRestraintCorrection *
                attackCriticalDamageCorrection * floatCorrection * finalRestrain);
        double damage_ = calDamageChange(actionDirection, damage);
        LogUtil.fight(
                "<<<<<<伤害结算--进攻方: ", actionDirection.getAtk().ownerId,
                "-", actionDirection.getCurAtkHeroId(),
                ", 对防守方: ", actionDirection.getDef().ownerId,
                "-", actionDirection.getCurDefHeroId(),
                ", 战斗回合===》战斗类型: ", battleType,
                ", 普攻伤害汇总结算, 基础伤害: ", baseHurt,
                ", 血量衰减: ", bloodValueAttenuation,
                ", 普攻修正: ", generalAttackCorrection,
                ", 重伤修正: ", finalDamageCorrection,
                ", 兵种克制修正: ", armsRestraintCorrection,
                ", 普攻暴击伤害修正: ", attackCriticalDamageCorrection,
                "， 浮动修正: ", floatCorrection,
                ", 克制: ", finalRestrain,
                ", 无敌或护盾效果作用前, 普攻最终伤害值:  ", damage,
                ", 无敌或护盾效果作用后, 普攻最终伤害值:  ", damage_,
                ">>>>>>");
        return (int) Math.ceil(damage_);
    }

    /**
     * 计算反击普攻伤害计算
     *
     * @param actionDirection
     * @param effectConfig
     * @param battleType
     * @return
     */
    public static int calCounterAttack(ActionDirection actionDirection, List<Integer> effectConfig, int battleType) {
        LogUtil.fight("*******进攻方: ", actionDirection.getAtk().ownerId, "-", actionDirection.getCurAtkHeroId(), ", 对防守方: ",
                actionDirection.getDef().ownerId, "-", actionDirection.getCurDefHeroId(), " 开始进行反击普攻计算******");
        // 基础伤害
        double baseHurt = baseHurt(actionDirection, battleType);
        // （基础伤害*伤害系数【效果3万分比】+固伤【效果3固定值】）
        baseHurt = baseHurt * (effectConfig.get(4) / FightConstant.TEN_THOUSAND) + effectConfig.get(5);
        LogUtil.fight("计算反击伤害公式部分-固伤部分, 攻击方基础伤害: ", baseHurt, ", 技能效果: ",
                Arrays.toString(effectConfig.toArray()), ", 计算完固伤部分的基础伤害: ", baseHurt);
        // 血量衰减
        double bloodValueAttenuation = bloodValueAttenuation(actionDirection.getAtk());
        LogUtil.fight("反击普攻伤害参与计算部分-血量衰减, 修正值: ", bloodValueAttenuation);
        // 普攻修正
        double generalAttackCorrection = attackCorrection(actionDirection);
        // 终伤修正
        double finalDamageCorrection = finalDamageCorrection(actionDirection);
        // 兵种克制修正
        double armsRestraintCorrection = armsRestraintCorrection(actionDirection);
        // 普攻暴击伤害修正
        double attackCriticalDamageCorrection = attackCriticalDamageCorrection(actionDirection);

        // 浮动修正=[0.9,1.1]
        double floatCorrection = RandomUtils.nextFloat(0.9f, 1.1f);
        LogUtil.fight("反击普攻伤害参与计算部分-浮动修正, 修正值: ", floatCorrection);
        // 克制=1+克制关系系数
        double finalRestrain = getFinalRestrain(actionDirection, battleType);
        LogUtil.fight("反击普攻伤害参与计算部分-克制, 克制值: ", finalRestrain);

        double damage = (baseHurt * bloodValueAttenuation * generalAttackCorrection * finalDamageCorrection * armsRestraintCorrection *
                attackCriticalDamageCorrection * floatCorrection * finalRestrain);
        double damage_ = calDamageChange(actionDirection, damage);
        LogUtil.fight(
                "<<<<<<伤害结算--进攻方: ", actionDirection.getAtk().ownerId,
                "-", actionDirection.getCurAtkHeroId(),
                ", 对防守方: ", actionDirection.getDef().ownerId,
                "-", actionDirection.getCurDefHeroId(),
                ", 战斗回合===》战斗类型: ", battleType,
                ", 反击普攻伤害汇总结算, 基础伤害: ", baseHurt,
                ", 血量衰减: ", bloodValueAttenuation,
                ", 普攻修正: ", generalAttackCorrection,
                ", 重伤修正: ", finalDamageCorrection,
                ", 兵种克制修正: ", armsRestraintCorrection,
                ", 普攻暴击伤害修正: ", attackCriticalDamageCorrection,
                "， 浮动修正: ", floatCorrection,
                ", 克制: ", finalRestrain,
                ", 无敌或护盾效果作用前, 普攻最终伤害值:  ", damage,
                ", 无敌或护盾效果作用后, 普攻最终伤害值:  ", damage_,
                ">>>>>>");
        return (int) Math.ceil(damage_);
    }

    /**
     * 计算无敌, 护盾的效果值
     *
     * @param actionDirection
     * @param damage
     * @return
     */
    private static double calDamageChange(ActionDirection actionDirection, double damage) {
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        IFightEffect fightEffect = fightManager.getSkillEffect(FightConstant.EffectLogicId.INVINCIBLE_DAMAGE);
        if (Objects.nonNull(fightEffect)) {
            boolean exist = (boolean) fightEffect.effectCalculateValue(actionDirection.getDef().getFightEffectMap(
                    actionDirection.getCurDefHeroId()), FightConstant.EffectLogicId.INVINCIBLE_DAMAGE);
            if (exist) {
                LogUtil.fight("战斗伤害计算, 防守方: ", actionDirection.getDef().ownerId, "-",
                        actionDirection.getCurDefHeroId(), " 无敌效果作用, 伤害为0");
                return 0d;
            }
        }

        fightEffect = fightManager.getSkillEffect(FightConstant.EffectLogicId.SHIELD);
        if (Objects.nonNull(fightEffect)) {
            ArrayList<FightEffectData> shieldList = (ArrayList<FightEffectData>) fightEffect.effectCalculateValue(actionDirection.getDef().
                    getFightEffectMap(actionDirection.getCurDefHeroId()), FightConstant.EffectLogicId.SHIELD);
            if (!CheckNull.isEmpty(shieldList)) {
                double beforeDamage = damage;
                for (FightEffectData shield : shieldList) {
                    if (CheckNull.isNull(shield)) continue;
                    if (shield.getValue() <= 0) continue;
                    if (shield.getValue() > damage) {
                        shield.setValue((int) (shield.getValue() - damage));
                        damage = 0;
                    } else {
                        damage -= shield.getValue();
                        shield.setValue(0);
                    }
                }

                LogUtil.fight("战斗伤害计算,防守方: ", actionDirection.getDef().ownerId, "-",
                        actionDirection.getCurDefHeroId(), " 护盾效果返回作用, 护盾值抵消: ", beforeDamage - damage);
            }
        }

        return damage;
    }

    /**
     * 计算公式参与运算部分(基础伤害)
     *
     * @param actionDirection
     * @return
     */
    private static double baseHurt(ActionDirection actionDirection, int battleType) {
        Force force = actionDirection.getAtk();
        Force target = actionDirection.getDef();
        double atk = attributeValue(FightCommonConstant.AttrId.ATTACK, force, actionDirection.getCurAtkHeroId());
        double atkDown = attributeValue(FightCommonConstant.AttrId.ATTACK_TOWN, force, actionDirection.getCurAtkHeroId());
        double def = attributeValue(FightCommonConstant.AttrId.DEFEND, target, actionDirection.getCurDefHeroId());
        double defDown = attributeValue(FightCommonConstant.AttrId.DEFEND_TOWN, target, actionDirection.getCurDefHeroId());
        if (!isCityBattle(battleType)) {
            atkDown = 0;
            defDown = 0;
        }

        // 伤害类型1 伤害类型1 结果 = A综合攻击*A综合攻击/(综合攻击a+B综合防御)
        // A综合攻击 = A战中攻击 + A战中攻坚 - B战中据守
        // B综合防御 = B战中防御
        double attackerAtk = Math.max(atk / 2, atk + atkDown - defDown);
        double hurt1 = attackerAtk * attackerAtk / (attackerAtk + def) * StaticFightManager.K2;

        // 伤害类型2=（A战中穿甲-B战中防护）/ 3
        double attackExt = attributeValue(FightCommonConstant.AttrId.ATTACK_EXT, force, actionDirection.getCurAtkHeroId());
        double defendExt = attributeValue(FightCommonConstant.AttrId.DEFEND_EXT, target, actionDirection.getCurDefHeroId());
        double hurt2 = Math.max(0, (attackExt - defendExt) * StaticFightManager.K2);

        // 基础伤害=max ( RANDIEST ( 1 , 10 ) , 伤害类型1 ) + max ( RANDIEST ( 1 , 10 ) , 伤害类型2 )
        double baseHurt = Math.max(RandomUtils.nextFloat(1f, 10f), hurt1) + Math.max(RandomUtils.nextFloat(1f, 10f), hurt2);
        LogUtil.fight("计算基础伤害公式部分, 攻击方攻击: ", atk, ", 攻击方攻坚: ", atkDown, ", 防守方防御: ", def, ", 防守方据守: ", defDown,
                ", 伤害类型1: ", hurt1, ", 攻击方穿甲: ", attackExt, ", 防守方防护: ", defendExt, ", 伤害类型2: ", hurt2, ", 基础伤害: ", baseHurt);
        return baseHurt;
    }

    /**
     * 伤害参与计算部分
     * 血量衰减
     *
     * @param force
     * @return
     */
    private static double bloodValueAttenuation(Force force) {
        // 血量衰减=K3*单排当前兵力/单排兵力上限+1-K3
        return (StaticFightManager.K3 * force.count / force.lead + 1 - StaticFightManager.K3);
    }

    /**
     * 伤害计算公式参与计算部分 普攻修正
     * 普攻修正 1+A普攻伤害结果提升万分比【效果71】-A普攻伤害结果降低万分比【效果72】+B受普攻伤害结果提升万分比【效果81】-B受普攻伤害结果降低万分比【效果82】
     *
     * @param actionDirection
     * @return
     */
    private static double attackCorrection(ActionDirection actionDirection) {
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        IFightEffect fightEffect = fightManager.getSkillEffect(FightConstant.EffectLogicId.INCREASE_COMMON_ATTACK_DAMAGE);

        Force force = actionDirection.getAtk();
        Force target = actionDirection.getDef();
        FightBuffEffect atkBe = force.getFightEffectMap(actionDirection.getCurAtkHeroId());
        FightBuffEffect defBe = target.getFightEffectMap(actionDirection.getCurDefHeroId());

        int effect71 = (int) fightEffect.effectCalculateValue(atkBe, FightConstant.EffectLogicId.INCREASE_COMMON_ATTACK_DAMAGE);
        int effect72 = (int) fightEffect.effectCalculateValue(atkBe, FightConstant.EffectLogicId.COMMON_ATTACK_DAMAGE_REDUCED);
        int effect81 = (int) fightEffect.effectCalculateValue(defBe, FightConstant.EffectLogicId.BE_INCREASE_FINAL_DAMAGE);
        int effect82 = (int) fightEffect.effectCalculateValue(defBe, FightConstant.EffectLogicId.BE_FINAL_DAMAGE_REDUCED);

        double attackCorrection = 1 + ((effect71 - effect72 + effect81 - effect82) / FightConstant.TEN_THOUSAND);
        LogUtil.fight("计算普攻伤害修正部分, 效果71: ", effect71, ", 效果72: ", effect72, ", 效果81: ", effect81, ", 效果82: ", effect82, ", 技能伤害修正参数: ", attackCorrection);
        return attackCorrection;
    }

    /**
     * 伤害计算公式参与计算部分 终伤修正
     *
     * @return
     */
    private static double finalDamageCorrection(ActionDirection actionDirection) {
        IFightEffect fightEffect = DataResource.ac.getBean(FightManager.class).getSkillEffect(FightConstant.EffectLogicId.FINAL_DAMAGE_REDUCED);
        FightBuffEffect atkBe = actionDirection.getAtk().getFightEffectMap(actionDirection.getCurAtkHeroId());
        FightBuffEffect defBe = actionDirection.getDef().getFightEffectMap(actionDirection.getCurDefHeroId());
        int effect75 = (int) fightEffect.effectCalculateValue(atkBe, FightConstant.EffectLogicId.INCREASE_FINAL_DAMAGE);
        int effect76 = (int) fightEffect.effectCalculateValue(atkBe, FightConstant.EffectLogicId.FINAL_DAMAGE_REDUCED);
        int effect85 = (int) fightEffect.effectCalculateValue(defBe, FightConstant.EffectLogicId.BE_INCREASE_FINAL_DAMAGE_PROMOTION);
        int effect86 = (int) fightEffect.effectCalculateValue(defBe, FightConstant.EffectLogicId.FINAL_DAMAGE_REDUCED_DECREASED);

        double finalDamageCorrection = 1 + ((effect75 - effect76 + effect85 - effect86) / FightConstant.TEN_THOUSAND);
        LogUtil.fight("计算伤害终伤修正部分, 效果75: ", effect75, ", 效果76: ", effect76, ", 效果85: ", effect86, ", 效果82: ", effect86, "终伤修正参数: ", finalDamageCorrection);
        // 终伤修正 1+A终伤伤害结果提升万分比【效果75】-A终伤伤害结果降低万分比【效果76】+B受终伤伤害结果提升万分比【效果85】-B受终伤伤害结果降低万分比【效果86】
        return finalDamageCorrection;
    }

    /**
     * 伤害计算公式参与计算部分 兵种克制修正
     *
     * @param actionDirection
     * @return
     */
    private static double armsRestraintCorrection(ActionDirection actionDirection) {
        IFightEffect fightEffect = DataResource.ac.getBean(FightManager.class).getSkillEffect(FightConstant.EffectLogicId.DAMAGE_INCREASED_FIGHTING);
        FightBuffEffect atkBe = actionDirection.getAtk().getFightEffectMap(actionDirection.getCurAtkHeroId());
        FightBuffEffect defBe = actionDirection.getDef().getFightEffectMap(actionDirection.getCurDefHeroId());

        // 兵种克制修正
        double armsRestraintCorrection = 1d;
        boolean haveArmyRestraint = haveArmyRestraint(actionDirection.getAtk().armyType(actionDirection.getCurAtkHeroId()),
                actionDirection.getDef().armyType(actionDirection.getCurDefHeroId()));
        if (haveArmyRestraint) {
            int effect45 = (int) fightEffect.effectCalculateValue(atkBe, FightConstant.EffectLogicId.DAMAGE_INCREASED_FIGHTING);
            int effect46 = (int) fightEffect.effectCalculateValue(defBe, FightConstant.EffectLogicId.DAMAGE_REDUCED_ARM_RESTRAINED);
            armsRestraintCorrection += ((effect45 - effect46) / FightConstant.TEN_THOUSAND);
            LogUtil.fight("计算伤害公式-兵种克制部分, effect45: ", effect45, ", effect46: ", effect46, ", 兵种克制系数: ", armsRestraintCorrection);
        }

        LogUtil.fight("计算伤害公式-兵种克制部分, 兵种克制系数: ", armsRestraintCorrection);
        return armsRestraintCorrection;
    }

    /**
     * 伤害计算公式参与部分 普攻暴击伤害修正
     *
     * @param actionDirection
     * @return
     */
    private static double attackCriticalDamageCorrection(ActionDirection actionDirection) {
        Force force = actionDirection.getAtk();
        int atkId = actionDirection.getCurAtkHeroId();
        // 普攻暴击率=初始暴击率+暴击率提升万分比         【效果102】
        IFightEffect fightEffect = DataResource.ac.getBean(FightManager.class).getSkillEffect(FightConstant.EffectLogicId.INCREASE_CRITICAL_HIT_RATE);
        Turple<Integer, Integer> effect102 = (Turple<Integer, Integer>)
                fightEffect.effectCalculateValue(force.getFightEffectMap(atkId), FightConstant.EffectLogicId.INCREASE_CRITICAL_HIT_RATE);
        int effect102Val = 0;
        if (Objects.nonNull(effect102)) {
            effect102Val = effect102.getB();
        }

        int criticalChanceCorrection = force.calCriticalChance(atkId) + effect102Val;
        // 普攻暴击伤害修正=原始暴击伤害+暴击伤害修正万分比         【效果103】
        Turple<Integer, Integer> effect103 = (Turple<Integer, Integer>)
                fightEffect.effectCalculateValue(force.getFightEffectMap(atkId), FightConstant.EffectLogicId.CRITICAL_DAMAGE_INCREASED);
        int effect103Val = 0;
        if (Objects.nonNull(effect103)) {
            effect103Val = effect103.getB();
        }

        double attackCriticalDamageCorrection = 2d + (force.calCriticalChance(atkId) + (effect103Val / FightConstant.TEN_THOUSAND));
        if (!RandomHelper.isHitRangeIn10000(criticalChanceCorrection)) {
            attackCriticalDamageCorrection = 1d;
        }

        return attackCriticalDamageCorrection;
    }

    /**
     * 伤害计算公式参与部分 技能暴击伤害修正
     *
     * @param actionDirection
     * @return
     */
    private static double skillAttackCriticalDamageCorrection(ActionDirection actionDirection) {
        Force force = actionDirection.getAtk();
        int atkId = actionDirection.getCurAtkHeroId();
        IFightEffect fightEffect = DataResource.ac.getBean(FightManager.class).getSkillEffect(FightConstant.EffectLogicId.INCREASE_CRITICAL_HIT_RATE);
        Turple<Integer, Integer> effect102 = (Turple<Integer, Integer>)
                fightEffect.effectCalculateValue(force.getFightEffectMap(atkId), FightConstant.EffectLogicId.INCREASE_CRITICAL_HIT_RATE);
        int effect102Val = 0;
        if (Objects.nonNull(effect102)) effect102Val = effect102.getB();

        Turple<Integer, Integer> effect104 = (Turple<Integer, Integer>)
                fightEffect.effectCalculateValue(force.getFightEffectMap(atkId), FightConstant.EffectLogicId.INCREASED_EXTRA_CRITICAL_HIT_RATE_OF_SKILL);
        int effect104Val = 0;
        if (Objects.nonNull(effect104)) effect104Val = effect104.getB();

        // 技能暴击率=初始暴击率+暴击率提升万分比【效果102】+技能额外暴击率提升万分比【效果104】
        int criticalChanceCorrection = force.calCriticalChance(atkId) + effect102Val + effect104Val;

        // 原始暴击伤害+暴击伤害修正万分比【效果103】+技能额外暴击伤害提升万分比【效果105】
        Turple<Integer, Integer> effect103 = (Turple<Integer, Integer>)
                fightEffect.effectCalculateValue(force.getFightEffectMap(atkId), FightConstant.EffectLogicId.CRITICAL_DAMAGE_INCREASED);
        int effect103Val = 0;
        if (Objects.nonNull(effect103)) effect103Val = effect103.getB();
        Turple<Integer, Integer> effect105 = (Turple<Integer, Integer>)
                fightEffect.effectCalculateValue(force.getFightEffectMap(atkId), FightConstant.EffectLogicId.INCREASED_EXTRA_CRITICAL_DAMAGE_OF_SKILL);
        int effect105Val = 0;
        if (Objects.nonNull(effect105)) effect105Val = effect105.getB();

        double attackCriticalDamageCorrection = 2d + ((effect103Val + effect105Val) / FightConstant.TEN_THOUSAND);
        if (!RandomHelper.isHitRangeIn10000(criticalChanceCorrection)) {
            attackCriticalDamageCorrection = 1d;
        }

        LogUtil.fight("计算技能伤害部分-技能暴击, 技能暴击几率效果102值: ", effect102Val, ", 技能暴击几率效果104值: ", effect104Val,
                ", 技能暴击几率结算: ", criticalChanceCorrection, ", 技能伤害倍率效果103值: ", effect103Val, ", 技能伤害倍率效果105值: ", effect105Val,
                ", 技能伤害倍率最终值: ", attackCriticalDamageCorrection);
        return attackCriticalDamageCorrection;
    }

    /**
     * 技能修正
     *
     * @param actionDirection
     * @return
     */
    private static double skillCorrection(ActionDirection actionDirection) {
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        IFightEffect fightEffect = fightManager.getSkillEffect(FightConstant.EffectLogicId.SKILL_DAMAGE_INCREASED);
        FightBuffEffect atkBe = actionDirection.getAtk().getFightEffectMap(actionDirection.getCurAtkHeroId());
        FightBuffEffect defBe = actionDirection.getDef().getFightEffectMap(actionDirection.getCurDefHeroId());
        int effect73 = (int) fightEffect.effectCalculateValue(atkBe, FightConstant.EffectLogicId.SKILL_DAMAGE_INCREASED);
        int effect74 = (int) fightEffect.effectCalculateValue(atkBe, FightConstant.EffectLogicId.SKILL_DAMAGE_REDUCED);
        int effect83 = (int) fightEffect.effectCalculateValue(defBe, FightConstant.EffectLogicId.BE_SKILL_DAMAGE_INCREASED);
        int effect84 = (int) fightEffect.effectCalculateValue(defBe, FightConstant.EffectLogicId.BE_SKILL_DAMAGE_REDUCED_DECREASED);

        double skillCorrection = 1 + ((effect73 - effect74 + effect83 - effect84) / FightConstant.TEN_THOUSAND);
        LogUtil.fight("计算技能伤害修正部分, 效果73: ", effect73, ", 效果74: ", effect74, ", 效果83: ", effect83, ", 效果84: ", effect84, ", 技能伤害修正参数: ", skillCorrection);
        return skillCorrection;
    }

    /**
     * 兵种克制关系系数
     *
     * @param actionDirection
     * @return
     */
    public static double getFinalRestrain(ActionDirection actionDirection, int battleType) {
        double restrain;
        int atkHeroLv = 1;          //进攻方 强化等级(默认1级)
        int defHeroLv = 1;          //防守方 强化等级(默认1级)

        Force force = actionDirection.getAtk();
        Force target = actionDirection.getDef();
        int atkId = actionDirection.getCurAtkHeroId();
        int defId = actionDirection.getCurDefHeroId();
        // 获取进攻方 强化等级 和  克制值
        int tmpLv;
        if ((tmpLv = force.armyTypeLv(atkId)) != 0) {
            atkHeroLv = tmpLv;
        }

        // 获取防守方 强化等级 和 克制值
        if ((tmpLv = target.armyTypeLv(defId)) != 0) {
            defHeroLv = tmpLv;
        }

        // 若 (兵种阶级 - 对方兵种阶级) < 0, 则为0
        int lvDiff = atkHeroLv - defHeroLv;
        lvDiff = lvDiff < 0 ? 0 : lvDiff;

        /**
         * 克制时	(兵种阶级-对方兵种阶级) * K9 + [基础K8 + (兵种阶级 * K10)]
         * 被克制时	(兵种阶级-对方兵种阶级) * K9 - [基础K8 + (对方兵种阶级 * K10)] — 兵种克制减伤加成
         * 不可制	(兵种阶级-对方兵种阶级) * K9
         */

        restrain = lvDiff * StaticFightManager.K9;
        int forceArmyType = force.armyType(atkId);
        int targetArmyType = target.armyType(defId);
        if (haveArmyRestraint(forceArmyType, targetArmyType)) {
            restrain = restrain + (StaticFightManager.K8 + (atkHeroLv * StaticFightManager.K10));
        } else if (haveArmyRestraint(targetArmyType, forceArmyType)) {
            // 兵种克制减伤属性加成
            double lessHurtFromArmyRestraint = getLessHurtFromArmyRestraint(forceArmyType, target, defId);
            restrain = restrain - (StaticFightManager.K8 + (defHeroLv * StaticFightManager.K10));
            LogUtil.fight("进攻方角色id: ", force.ownerId, ",防守方角色id: ", target.ownerId, ", " +
                            "战斗回合===》战斗类型: ", FightCalc.battleType2String(battleType),
                    ", 兵种克制伤害加成比例: ", restrain, " - ", (StaticFightManager.K8 + (defHeroLv * StaticFightManager.K10)),
                    ", 兵种克制减伤加成比例: ", lessHurtFromArmyRestraint);
            restrain -= lessHurtFromArmyRestraint;
        }

        return 1 + restrain;
    }

    /**
     * 战斗类型转文字日志
     *
     * @param battleType
     * @return
     */
    public static String battleType2String(int battleType) {
        //战斗类型
        String strType = "打副本";
        switch (battleType) {
            case 1:
                strType = "城战 [打玩家]";
                break;
            case 2:
                strType = "国战、阵营战 [打城池]";
                break;
            case 3:
                strType = "盖世太保战";
                break;
            case 4:
                strType = "闪电战";
                break;
            case 5:
                strType = "柏林会战";
                break;
            case 6:
                strType = "超级矿点战斗";
                break;
        }
        return strType;
    }

    /**
     * 根据兵种克制关系，获取减伤系数
     *
     * @param forceArmyType
     * @param target
     * @return
     */
    private static double getLessHurtFromArmyRestraint(int forceArmyType, Force target, int defId) {
        double lessHurt = 0.00;
        if (forceArmyType == FightCommonConstant.ARM1) {
            lessHurt = target.attrData(defId).lessInfantryMut;
        }

        if (forceArmyType == FightCommonConstant.ARM2) {
            lessHurt = target.attrData(defId).lessCavalryMut;
        }

        if (forceArmyType == FightCommonConstant.ARM3) {
            lessHurt = target.attrData(defId).lessArcherMut;
        }

        return lessHurt;
    }

    /**
     * 兵种之间是否存在克制关系
     * 步克弓，弓克骑，骑克步
     *
     * @param atkArm
     * @param defArm
     * @return
     */
    public static boolean haveArmyRestraint(int atkArm, int defArm) {
        boolean isRestraint = false;
        switch (atkArm) {
            case FightCommonConstant.ARM1:
                if (defArm == FightCommonConstant.ARM3) {
                    isRestraint = true;
                }
                break;
            case FightCommonConstant.ARM2:
                if (defArm == FightCommonConstant.ARM1) {
                    isRestraint = true;
                }
                break;
            case FightCommonConstant.ARM3:
                if (defArm == FightCommonConstant.ARM2) {
                    isRestraint = true;
                }
                break;
        }
        return isRestraint;
    }

    public static boolean isCityBattle(int battleType) {
        return ArrayUtils.contains(StaticFightManager.CITY_BATTLE_TYPE, battleType);
    }
}
