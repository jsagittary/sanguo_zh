package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.manager.FightManager;

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
     * 技能能量槽恢复
     *
     * @param force
     * @param heroId
     * @param skill
     * @param effectConfig
     * @return
     */
    public static final int skillEnergyRecovery(Force force, int heroId, StaticHeroSkill skill, List<Integer> effectConfig) {
        int tenThousandthRatio65 = 0, fixValue65 = 0, tenThousandthRatio66 = 0, fixValue66 = 0;
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        IFightEffect fightEffect65 = fightManager.getSkillEffect(FightConstant.EffectLogicId.ENERGY_RECOVERY_VALUE_INCREASED);
        IFightEffect fightEffect66 = fightManager.getSkillEffect(FightConstant.EffectLogicId.ENERGY_RECOVERY_VALUE_DECREASES);

        FightBuffEffect fightBuffEffect = force.getFightEffectMap(heroId);
        if (Objects.nonNull(fightEffect65)) {
            Turple<Integer, Integer> var65 = (Turple<Integer, Integer>) fightEffect65.effectCalculateValue(fightBuffEffect,
                    FightConstant.EffectLogicId.ENERGY_RECOVERY_VALUE_INCREASED);
            if (Objects.nonNull(var65)) {
                tenThousandthRatio65 = var65.getA();
                fixValue65 = var65.getB();
            }
        }

        if (Objects.nonNull(fightEffect66)) {
            Turple<Integer, Integer> var66 = (Turple<Integer, Integer>) fightEffect66.effectCalculateValue(fightBuffEffect,
                    FightConstant.EffectLogicId.ENERGY_RECOVERY_VALUE_DECREASES);
            if (Objects.nonNull(var66)) {
                tenThousandthRatio66 = var66.getA();
                fixValue66 = var66.getB();
            }
        }

        // 技能能量恢复=(能量槽上限*能量恢复万分比【效果4】+能量恢复固定值【效果4】)*（1+能量恢复值提升万分比【效果65】-能量恢复值降低万分比【效果66】）+能量恢复值提升固定值【效果65】-能量恢复值降低固定值【效果66】
        return (int) (((skill.getEnergyUpperLimit() * effectConfig.get(4) / FightConstant.TEN_THOUSAND) + effectConfig.get(5)) * (1 +
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
    public static final int attributeValue(int attrId, Force force, int heroId) {
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        FightBuffEffect fightBuffEffect = force.getFightEffectMap(heroId);
        switch (attrId) {
            case 1:
                return calAttrValue(force, heroId, FightConstant.EffectLogicId.ATTACK_INCREASED, FightConstant.EffectLogicId.REDUCED_ATTACK);
            case 2:
                return calAttrValue(force, heroId, FightConstant.EffectLogicId.DEFENSE_INCREASED, FightConstant.EffectLogicId.REDUCED_DEFENSE);
            default:
                return 0;
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
    private static final int calAttrValue(Force force, int heroId, int effectLogicId1, int effectLogicId2) {
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
        return (int) (force.calcAttack(heroId) * (1 + ((
                tenThousandthRatio_ - tenThousandthRatio) / FightConstant.TEN_THOUSAND)) + fixValue_ - fixValue);
    }
}
