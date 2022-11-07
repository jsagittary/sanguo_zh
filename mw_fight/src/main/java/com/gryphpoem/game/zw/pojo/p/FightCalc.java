package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.cross.constants.FightCommonConstant;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.manager.FightManager;
import org.apache.commons.lang3.RandomUtils;

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
                return calAttrValue(force, heroId, force.calcAttack(heroId), FightConstant.EffectLogicId.ATTACK_INCREASED, FightConstant.EffectLogicId.REDUCED_ATTACK);
            case FightCommonConstant.AttrId.DEFEND:
                return calAttrValue(force, heroId, force.calcDefend(heroId), FightConstant.EffectLogicId.DEFENSE_INCREASED, FightConstant.EffectLogicId.REDUCED_DEFENSE);
            case FightCommonConstant.AttrId.SPEED:
                return calAttrValue(force, heroId, force.calSpeed(heroId), FightConstant.EffectLogicId.SPEED_INCREASE, FightConstant.EffectLogicId.SPEED_REDUCTION);
            case FightCommonConstant.AttrId.ATTACK_TOWN:
                return calAttrValue(force, heroId, force.calcAtkTown(heroId), FightConstant.EffectLogicId.BROKEN_CITY_PROMOTION, FightConstant.EffectLogicId.BROKEN_CITY_REDUCED);
            case FightCommonConstant.AttrId.DEFEND_TOWN:
                return calAttrValue(force, heroId, force.calcDefTown(heroId), FightConstant.EffectLogicId.UPWARD_GUARDING, FightConstant.EffectLogicId.GUARD_CITY_REDUCED);
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
    private static final double calAttrValue(Force force, int heroId, int attrValue, int effectLogicId1, int effectLogicId2) {
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
        return attrValue * (1 + ((
                tenThousandthRatio_ - tenThousandthRatio) / FightConstant.TEN_THOUSAND)) + fixValue_ - fixValue;
    }

    public static int calSkillAttack(Force force, Force target, int battleType) {
        // 基础伤害
        double baseHurt = baseHurt(force, target);
        
    }

    /**
     * 计算普攻伤害
     *
     * @param force
     * @param target
     * @param battleType
     */
    public static int calAttack(Force force, Force target, int battleType) {
        // 基础伤害
        double baseHurt = baseHurt(force, target);
        // 血量衰减
        double bloodValueAttenuation = bloodValueAttenuation(force);
        // 普攻修正
        double generalAttackCorrection = attackCorrection(force, target);
        // 终伤修正
        double finalDamageCorrection = finalDamageCorrection(force, target, target.beActionId.get(0));
        // 兵种克制修正
        double armsRestraintCorrection = armsRestraintCorrection(force, target, target.beActionId.get(0));
        // 暴击伤害修正
        double attackCriticalDamageCorrection = attackCriticalDamageCorrection(force);

        // 浮动修正=[0.9,1.1]
        double floatCorrection = RandomUtils.nextFloat(0.9f, 1.1f);
        // 克制=1+克制关系系数
        double finalRestrain = getFinalRestrain(force, target, battleType);

        return (int) (baseHurt * bloodValueAttenuation * generalAttackCorrection * finalDamageCorrection * armsRestraintCorrection *
                attackCriticalDamageCorrection * floatCorrection * finalRestrain);
    }

    /**
     * 计算公式参与运算部分(基础伤害)
     *
     * @param force
     * @param target
     * @return
     */
    private static double baseHurt(Force force, Force target) {
        double atk = attributeValue(FightCommonConstant.AttrId.ATTACK, force, force.actionId);
        double atkDown = attributeValue(FightCommonConstant.AttrId.ATTACK_TOWN, force, force.actionId);
        double def = attributeValue(FightCommonConstant.AttrId.DEFEND, target, target.beActionId.get(0));
        double defDown = attributeValue(FightCommonConstant.AttrId.DEFEND_TOWN, target, target.beActionId.get(0));

        // 伤害类型1 (A综合攻击-B综合防御) / 3
        // A综合攻击 = A战中攻击 + A战中攻坚
        // B综合防御 = B战中防御+B战中据守
        double hurt1 = (atk + atkDown - def - defDown) / 3;

        // 伤害类型2=（A战中穿甲-B战中防护）/ 3
        double attackExt = attributeValue(FightCommonConstant.AttrId.ATTACK_EXT, force, force.actionId);
        double defendExt = attributeValue(FightCommonConstant.AttrId.DEFEND_EXT, target, target.beActionId.get(0));
        double hurt2 = (attackExt - defendExt) / 3;

        // 基础伤害=max ( RANDIEST ( 1 , 10 ) , 伤害类型1 ) + max ( RANDIEST ( 1 , 10 ) , 伤害类型2 )
        return Math.max(RandomUtils.nextFloat(1f, 10f), hurt1) + Math.max(RandomUtils.nextFloat(1f, 10f), hurt2);
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
        float K3 = 0.65f;
        return (K3 * force.count / force.lead + 1 - K3);
    }

    /**
     * 伤害计算公式参与计算部分 普攻修正
     * 普攻修正 1+A普攻伤害结果提升万分比【效果71】-A普攻伤害结果降低万分比【效果72】+B受普攻伤害结果提升万分比【效果81】-B受普攻伤害结果降低万分比【效果82】
     *
     * @param force
     * @param target
     * @return
     */
    private static double attackCorrection(Force force, Force target) {
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        IFightEffect fightEffect = fightManager.getSkillEffect(FightConstant.EffectLogicId.INCREASE_COMMON_ATTACK_DAMAGE);
        FightBuffEffect atkBe = force.getFightEffectMap(force.actionId);
        FightBuffEffect defBe = target.getFightEffectMap(target.beActionId.get(0));
        return 1 + (((int) fightEffect.effectCalculateValue(atkBe, FightConstant.EffectLogicId.INCREASE_COMMON_ATTACK_DAMAGE) -
                (int) fightEffect.effectCalculateValue(atkBe, FightConstant.EffectLogicId.COMMON_ATTACK_DAMAGE_REDUCED) + (int) fightEffect.effectCalculateValue(
                defBe, FightConstant.EffectLogicId.BE_INCREASE_FINAL_DAMAGE) - (int) fightEffect.effectCalculateValue(defBe, FightConstant.EffectLogicId.BE_FINAL_DAMAGE_REDUCED)) / FightConstant.TEN_THOUSAND);
    }

    /**
     * 伤害计算公式参与计算部分 终伤修正
     *
     * @param force
     * @param target
     * @param targetHeroId
     * @return
     */
    private static double finalDamageCorrection(Force force, Force target, int targetHeroId) {
        IFightEffect fightEffect = DataResource.ac.getBean(FightManager.class).getSkillEffect(FightConstant.EffectLogicId.FINAL_DAMAGE_REDUCED);
        FightBuffEffect atkBe = force.getFightEffectMap(force.actionId);
        FightBuffEffect defBe = target.getFightEffectMap(targetHeroId);

        // 终伤修正 1+A终伤伤害结果提升万分比【效果75】-A终伤伤害结果降低万分比【效果76】+B受终伤伤害结果提升万分比【效果85】-B受终伤伤害结果降低万分比【效果86】
        return 1 + ((int) fightEffect.effectCalculateValue(atkBe, FightConstant.EffectLogicId.INCREASE_FINAL_DAMAGE) -
                (int) fightEffect.effectCalculateValue(atkBe, FightConstant.EffectLogicId.FINAL_DAMAGE_REDUCED) + (int) fightEffect.effectCalculateValue(
                defBe, FightConstant.EffectLogicId.BE_INCREASE_FINAL_DAMAGE_PROMOTION) - (int) fightEffect.effectCalculateValue(defBe,
                FightConstant.EffectLogicId.FINAL_DAMAGE_REDUCED_DECREASED) / FightConstant.TEN_THOUSAND);
    }

    /**
     * 伤害计算公式参与计算部分 兵种克制修正
     *
     * @param force
     * @param target
     * @param targetHeroId
     * @return
     */
    private static double armsRestraintCorrection(Force force, Force target, int targetHeroId) {
        IFightEffect fightEffect = DataResource.ac.getBean(FightManager.class).getSkillEffect(FightConstant.EffectLogicId.DAMAGE_INCREASED_FIGHTING);
        FightBuffEffect atkBe = force.getFightEffectMap(force.actionId);
        FightBuffEffect defBe = target.getFightEffectMap(targetHeroId);

        // 兵种克制修正
        double armsRestraintCorrection = 1d;
        boolean haveArmyRestraint = haveArmyRestraint(force.armyType(force.actionId), target.armyType(targetHeroId));
        if (haveArmyRestraint) {
            armsRestraintCorrection += ((int) fightEffect.effectCalculateValue(atkBe, FightConstant.EffectLogicId.DAMAGE_INCREASED_FIGHTING) -
                    (int) fightEffect.effectCalculateValue(defBe, FightConstant.EffectLogicId.DAMAGE_REDUCED_ARM_RESTRAINED) / FightConstant.TEN_THOUSAND);
        }
        return armsRestraintCorrection;
    }

    /**
     * 伤害计算公式参与部分 暴击伤害修正
     *
     * @param force
     * @return
     */
    private static double attackCriticalDamageCorrection(Force force) {
        // 普攻暴击率=初始暴击率+暴击率提升万分比         【效果102】
        IFightEffect fightEffect = DataResource.ac.getBean(FightManager.class).getSkillEffect(FightConstant.EffectLogicId.INCREASE_CRITICAL_HIT_RATE);
        int criticalChanceCorrection = force.calCriticalChance(force.actionId) + ((Turple<Integer, Integer>)
                fightEffect.effectCalculateValue(force.getFightEffectMap(force.actionId), FightConstant.EffectLogicId.INCREASE_CRITICAL_HIT_RATE)).getB();
        // 普攻暴击伤害修正=原始暴击伤害+暴击伤害修正万分比         【效果103】
        double attackCriticalDamageCorrection = 2d + (force.calCriticalChance(force.actionId) + ((Turple<Integer, Integer>)
                fightEffect.effectCalculateValue(force.getFightEffectMap(force.actionId), FightConstant.EffectLogicId.CRITICAL_DAMAGE_INCREASED)).getB() / FightConstant.TEN_THOUSAND);
        if (!RandomHelper.isHitRangeIn10000(criticalChanceCorrection)) {
            attackCriticalDamageCorrection = 1d;
        }

        return attackCriticalDamageCorrection;
    }

    /**
     * 兵种克制关系系数
     *
     * @param force
     * @param target
     * @return
     */
    public static double getFinalRestrain(Force force, Force target, int battleType) {
        double restrain;
        int atkHeroLv = 1;          //进攻方 强化等级(默认1级)
        int defHeroLv = 1;          //防守方 强化等级(默认1级)

        // 获取进攻方 强化等级 和  克制值
        if (force.intensifyLv != 0) {
            atkHeroLv = force.intensifyLv;
        }

        // 获取防守方 强化等级 和 克制值
        if (target.intensifyLv != 0) {
            defHeroLv = target.intensifyLv;
        }

        // 若 (兵种阶级 - 对方兵种阶级) < 0, 则为0
        int lvDiff = atkHeroLv - defHeroLv;
        lvDiff = lvDiff < 0 ? 0 : lvDiff;

        /**
         * 克制时	(兵种阶级-对方兵种阶级) * K9 + [基础K8 + (兵种阶级 * K10)]
         * 被克制时	(兵种阶级-对方兵种阶级) * K9 - [基础K8 + (对方兵种阶级 * K10)] — 兵种克制减伤加成
         * 不可制	(兵种阶级-对方兵种阶级) * K9
         */

        restrain = lvDiff * 0.01f;
        if (haveArmyRestraint(force.armType, target.armType)) {
            restrain = restrain + (0.05f + (atkHeroLv * 0.05f));
        } else if (haveArmyRestraint(target.armType, force.armType)) {
            // 兵种克制减伤属性加成
            double lessHurtFromArmyRestraint = getLessHurtFromArmyRestraint(force, target);
            restrain = restrain - (0.09 + (defHeroLv * 0.01f));
            LogUtil.fight("进攻方角色id: ", force.ownerId, ",防守方角色id: ", target.ownerId, ", " +
                            "战斗回合===》战斗类型: ", FightCalc.battleType2String(battleType),
                    "赛季天赋-以短攻长-加成比例: ", restrain, " - ", (0.09 + (defHeroLv * 0.01f)),
                    "兵种克制减伤加成比例: ", lessHurtFromArmyRestraint);
            restrain -= lessHurtFromArmyRestraint;
        }

        return restrain;
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
     * @param force
     * @param target
     * @return
     */
    private static double getLessHurtFromArmyRestraint(Force force, Force target) {
        double lessHurt = 0.00;
        if (force.armyType(force.actionId) == FightCommonConstant.ARM1) {
            lessHurt = target.attrData.lessInfantryMut;
        }

        if (force.armyType(force.actionId) == FightCommonConstant.ARM2) {
            lessHurt = target.attrData.lessCavalryMut;
        }

        if (force.armyType(force.actionId) == FightCommonConstant.ARM3) {
            lessHurt = target.attrData.lessArcherMut;
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
}
