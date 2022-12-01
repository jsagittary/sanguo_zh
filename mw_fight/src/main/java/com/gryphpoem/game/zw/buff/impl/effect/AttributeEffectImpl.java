package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pojo.p.FightBuffEffect;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.FightEffectData;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.util.FightPbUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;
import java.util.Objects;

/**
 * Description: 武将属性变化效果
 * Author: zhangpeng
 * createTime: 2022-11-01 16:46
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class AttributeEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.ATTACK_INCREASED,
                FightConstant.EffectLogicId.REDUCED_ATTACK,
                FightConstant.EffectLogicId.DEFENSE_INCREASED,
                FightConstant.EffectLogicId.REDUCED_DEFENSE,
                FightConstant.EffectLogicId.BROKEN_CITY_PROMOTION,
                FightConstant.EffectLogicId.BROKEN_CITY_REDUCED,
                FightConstant.EffectLogicId.UPWARD_GUARDING,
                FightConstant.EffectLogicId.GUARD_CITY_REDUCED,
                FightConstant.EffectLogicId.ARMOR_PIERCING_ENHANCEMENT,
                FightConstant.EffectLogicId.ARMOR_PIERCING_REDUCTION,
                FightConstant.EffectLogicId.PROTECTION_LIFTING,
                FightConstant.EffectLogicId.REDUCED_PROTECTION,
                FightConstant.EffectLogicId.SPEED_INCREASE,
                FightConstant.EffectLogicId.SPEED_REDUCTION,
                FightConstant.EffectLogicId.INCREASE_CRITICAL_HIT_RATE,
                FightConstant.EffectLogicId.CRITICAL_DAMAGE_INCREASED,
                FightConstant.EffectLogicId.INCREASED_EXTRA_CRITICAL_HIT_RATE_OF_SKILL,
                FightConstant.EffectLogicId.INCREASED_EXTRA_CRITICAL_DAMAGE_OF_SKILL};
    }

    @Override
    public IFightBuff compareTo(List sameIdBuffList, List effectConfig, FightBuffEffect fightBuffEffect, FightContextHolder contextHolder) {
        return (IFightBuff) sameIdBuffList.get(0);
    }

    /**
     * 计算属性结果值
     *
     * @param force
     * @param heroId
     * @param effectLogicId
     * @param params
     * @return
     */
    protected double calValue(Force force, int heroId, int effectLogicId, Object... params) {
        int tenThousandthRatio = (int) params[0], fixValue = (int) params[1];
        switch (effectLogicId) {
            case FightConstant.EffectLogicId.ATTACK_INCREASED:
                return force.calcAttack(heroId) * (1 + (tenThousandthRatio / FightConstant.TEN_THOUSAND)) + fixValue;
            case FightConstant.EffectLogicId.REDUCED_ATTACK:
                return force.calcAttack(heroId) * (1 - (tenThousandthRatio / FightConstant.TEN_THOUSAND)) - fixValue;
            case FightConstant.EffectLogicId.DEFENSE_INCREASED:
                return force.calcDefend(heroId) * (1 + (tenThousandthRatio / FightConstant.TEN_THOUSAND)) + fixValue;
            case FightConstant.EffectLogicId.REDUCED_DEFENSE:
                return force.calcDefend(heroId) * (1 - (tenThousandthRatio / FightConstant.TEN_THOUSAND)) - fixValue;
            case FightConstant.EffectLogicId.BROKEN_CITY_PROMOTION:
                return force.calcAtkTown(heroId) * (1 + (tenThousandthRatio / FightConstant.TEN_THOUSAND)) + fixValue;
            case FightConstant.EffectLogicId.BROKEN_CITY_REDUCED:
                return force.calcAtkTown(heroId) * (1 - (tenThousandthRatio / FightConstant.TEN_THOUSAND)) - fixValue;
            case FightConstant.EffectLogicId.UPWARD_GUARDING:
                return force.calcDefTown(heroId) * (1 + (tenThousandthRatio / FightConstant.TEN_THOUSAND)) + fixValue;
            case FightConstant.EffectLogicId.GUARD_CITY_REDUCED:
                return force.calcDefTown(heroId) * (1 - (tenThousandthRatio / FightConstant.TEN_THOUSAND)) - fixValue;
            case FightConstant.EffectLogicId.ARMOR_PIERCING_ENHANCEMENT:
                return force.calcAtkExt(heroId) * (1 + (tenThousandthRatio / FightConstant.TEN_THOUSAND)) + fixValue;
            case FightConstant.EffectLogicId.ARMOR_PIERCING_REDUCTION:
                return force.calcAtkExt(heroId) * (1 - (tenThousandthRatio / FightConstant.TEN_THOUSAND)) - fixValue;
            case FightConstant.EffectLogicId.PROTECTION_LIFTING:
                return force.calcDefExt(heroId) * (1 + (tenThousandthRatio / FightConstant.TEN_THOUSAND)) + fixValue;
            case FightConstant.EffectLogicId.REDUCED_PROTECTION:
                return force.calcDefExt(heroId) * (1 - (tenThousandthRatio / FightConstant.TEN_THOUSAND)) - fixValue;
            case FightConstant.EffectLogicId.SPEED_INCREASE:
                return force.calSpeed(heroId) * (1 + (tenThousandthRatio / FightConstant.TEN_THOUSAND)) + fixValue;
            case FightConstant.EffectLogicId.SPEED_REDUCTION:
                return force.calSpeed(heroId) * (1 - (tenThousandthRatio / FightConstant.TEN_THOUSAND)) - fixValue;
            default:
                return 0d;
        }
    }

    @Override
    protected FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe, Object... params) {
        return new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId(), effectConfig.subList(4, 6));
    }

    @Override
    protected void addPbValue(BattlePb.CommonEffectAction.Builder builder, Object... params) {
        FightEffectData data = (FightEffectData) params[0];
        FightBuffEffect fbe = (FightBuffEffect) params[1];
        StaticEffectRule rule = (StaticEffectRule) params[2];
        if (CheckNull.isNull(data) || CheckNull.isEmpty(data.getData())) return;
        builder.addData(FightPbUtil.createDataInt(FightConstant.ValueType.RATIO, data.getData().get(0)));
        builder.addData(FightPbUtil.createDataInt(FightConstant.ValueType.FIX_VALUE, data.getData().get(1)));
        builder.addData(FightPbUtil.createDataInt(FightConstant.ValueType.FIX_VALUE, (int) calValue(fbe.getForce(), fbe.getHeroId(),
                rule.getEffectLogicId(), data.getData().get(0), data.getData().get(1))));

        // 计算效果影响最终值
        Object value = effectCalculateValue(fbe, rule.getEffectLogicId());
        Turple<Integer, Integer> dataValue = null;
        if (Objects.nonNull(value)) {
            dataValue = (Turple<Integer, Integer>) value;
        }
        int curValue = 0;
        if (Objects.nonNull(dataValue)) {
            curValue = (int) calValue(fbe.getForce(), fbe.getHeroId(),
                    rule.getEffectLogicId(), dataValue.getA(), dataValue.getB());
        }
        builder.addData(FightPbUtil.createDataInt(FightConstant.ValueType.FIX_VALUE, curValue));
    }

    /**
     * 计算属性值
     *
     * @param effectLogicId
     * @param params
     * @return
     */
    @Override
    protected boolean compareValue(Force force, int heroId, int effectLogicId, Object... params) {
        int tenThousandthRatio = (int) params[0], fixValue = (int) params[1];
        Object config = params[2];
        int tenThousandthRatio_ = 0;
        int fixValue_ = 0;
        if (config instanceof List) {
            List<Integer> config_ = (List<Integer>) config;
            tenThousandthRatio_ = config_.get(0);
            fixValue_ = config_.get(1);
        }
        if (config instanceof Turple) {
            Turple<Integer, Integer> config_ = (Turple<Integer, Integer>) config;
            tenThousandthRatio_ = config_.getA();
            fixValue_ = config_.getB();
        }

        switch (effectLogicId) {
            case FightConstant.EffectLogicId.ATTACK_INCREASED:
            case FightConstant.EffectLogicId.DEFENSE_INCREASED:
            case FightConstant.EffectLogicId.SPEED_INCREASE:
            case FightConstant.EffectLogicId.BROKEN_CITY_PROMOTION:
            case FightConstant.EffectLogicId.PROTECTION_LIFTING:
            case FightConstant.EffectLogicId.ARMOR_PIERCING_ENHANCEMENT:
            case FightConstant.EffectLogicId.UPWARD_GUARDING:
                return calValue(force, heroId, effectLogicId, tenThousandthRatio_, fixValue_) >
                        calValue(force, heroId, effectLogicId, tenThousandthRatio, fixValue);
            case FightConstant.EffectLogicId.REDUCED_ATTACK:
            case FightConstant.EffectLogicId.REDUCED_DEFENSE:
            case FightConstant.EffectLogicId.BROKEN_CITY_REDUCED:
            case FightConstant.EffectLogicId.GUARD_CITY_REDUCED:
            case FightConstant.EffectLogicId.ARMOR_PIERCING_REDUCTION:
            case FightConstant.EffectLogicId.REDUCED_PROTECTION:
            case FightConstant.EffectLogicId.SPEED_REDUCTION:
                return calValue(force, heroId, effectLogicId, tenThousandthRatio_, fixValue_) <
                        calValue(force, heroId, effectLogicId, tenThousandthRatio, fixValue);
            case FightConstant.EffectLogicId.INCREASE_CRITICAL_HIT_RATE:
            case FightConstant.EffectLogicId.CRITICAL_DAMAGE_INCREASED:
            case FightConstant.EffectLogicId.INCREASED_EXTRA_CRITICAL_HIT_RATE_OF_SKILL:
            case FightConstant.EffectLogicId.INCREASED_EXTRA_CRITICAL_DAMAGE_OF_SKILL:
                return fixValue_ > fixValue;
            default:
                return false;
        }
    }
}
