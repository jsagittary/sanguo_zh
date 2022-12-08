package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pojo.p.FightBuffEffect;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.FightEffectData;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.game.zw.util.FightPbUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;

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

    /**
     * 计算属性结果值
     *
     * @param force
     * @param heroId
     * @param effectLogicId
     * @param params
     * @return
     */
    @Override
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
        List<Integer> effectDataList = new ArrayList<>(effectConfig.subList(4, 6));
        Integer ratio = effectDataList.remove(0);
        Integer fixValue = effectDataList.remove(1);
        if (!CheckNull.isEmpty(effectDataList) && Objects.nonNull(fightBuff) && Objects.nonNull(fightBuff.getSkill())) {
            if (fightBuff.getSkill() instanceof SimpleHeroSkill) {
                SimpleHeroSkill skill = (SimpleHeroSkill) fightBuff.getSkill();
                double lvRatio = (1 + ((skill.getS_skill().getLevel() - 1) / 9d));
                if (Objects.nonNull(ratio) && ratio > 0) {
                    ratio = (int) Math.ceil(ratio * lvRatio);
                }
                if (Objects.nonNull(fixValue) && fixValue > 0) {
                    fixValue = (int) Math.ceil(fixValue * lvRatio);
                }
            }
        }

        effectDataList.add(ratio);
        effectDataList.add(fixValue);
        return new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId(), effectDataList);
    }

    @Override
    protected void addPbValue(BattlePb.CommonEffectAction.Builder builder, Object... params) {
        FightEffectData data = (FightEffectData) params[0];
        FightBuffEffect fbe = (FightBuffEffect) params[1];
        StaticEffectRule rule = (StaticEffectRule) params[2];
        if (CheckNull.isNull(data) || CheckNull.isEmpty(data.getData())) return;
        builder.addData(FightPbUtil.createDataInt(FightConstant.ValueType.RATIO, data.getData().get(0)));
        builder.addData(FightPbUtil.createDataInt(FightConstant.ValueType.FIX_VALUE, data.getData().get(1)));
        // 计算当前效果加成后的增加值
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
            // 计算加成后的属性最终值
            curValue = (int) calValue(fbe.getForce(), fbe.getHeroId(),
                    rule.getEffectLogicId(), dataValue.getA(), dataValue.getB());
        }
        builder.addData(FightPbUtil.createDataInt(FightConstant.ValueType.FIX_VALUE, curValue));
    }

    @Override
    public Object effectCalculateValue(FightBuffEffect fightBuffEffect, int effectLogicId, Object... params) {
        if (CheckNull.isEmpty(fightBuffEffect.getEffectMap()))
            return null;
        Map<Integer, List<FightEffectData>> effectDataMap = fightBuffEffect.getEffectMap().get(effectLogicId);
        if (CheckNull.isEmpty(effectDataMap)) {
            return null;
        }

        Map<Integer, Map<Integer, Turple<Integer, Integer>>> effectValue = new HashMap<>();
        // 合并相同buff来源的效果
        for (Map.Entry<Integer, List<FightEffectData>> entry : effectDataMap.entrySet()) {
            StaticEffectRule rule = StaticFightManager.getStaticEffectRule(entry.getKey());
            if (CheckNull.isNull(rule)) continue;
            if (CheckNull.isEmpty(entry.getValue())) continue;
            Map<Integer, Turple<Integer, Integer>> buffIdMap = effectValue.computeIfAbsent(entry.getKey(), m -> new HashMap<>());
            entry.getValue().forEach(data -> {
                Turple<Integer, Integer> value = buffIdMap.computeIfAbsent(data.getBuffId(), l -> new Turple<>(0, 0));
                if (value.getA() == 0 && value.getB() == 0) {
                    value.setA(data.getData().get(0));
                    value.setB(data.getData().get(1));
                    return;
                }
                switch (rule.getSameBuffRule()) {
                    case 1:
                        if (compareValue(fightBuffEffect.getForce(), fightBuffEffect.getHeroId(),
                                effectLogicId, value.getA(), value.getB(), data.getData())) {
                            value.setA(data.getData().get(0));
                            value.setB(data.getData().get(1));
                        }
                        break;
                    case 0:
                    case 2:
                        value.setA(value.getA() + data.getData().get(0));
                        value.setB(value.getB() + data.getData().get(1));
                        break;
                    default:
                        break;
                }
            });
        }
        if (CheckNull.isEmpty(effectValue))
            return null;

        // 合并不同buff来源的效果
        Map<Integer, Turple<Integer, Integer>> resultMap = new HashMap<>(effectValue.size());
        for (Map.Entry<Integer, Map<Integer, Turple<Integer, Integer>>> entry : effectValue.entrySet()) {
            StaticEffectRule rule = StaticFightManager.getStaticEffectRule(entry.getKey());
            if (CheckNull.isNull(rule))
                continue;
            if (CheckNull.isEmpty(entry.getValue()))
                continue;
            Turple<Integer, Integer> tuple = null;
            for (Turple<Integer, Integer> t : entry.getValue().values()) {
                if (CheckNull.isNull(t))
                    continue;
                if (tuple == null) {
                    tuple = t;
                    resultMap.put(entry.getKey(), tuple);
                    continue;
                }
                switch (rule.getDiffBuffRule()) {
                    case 1:
                        if (compareValue(fightBuffEffect.getForce(), fightBuffEffect.getHeroId(),
                                effectLogicId, tuple.getA(), tuple.getB(), t)) {
                            tuple = t;
                        }
                        break;
                    case 0:
                    case 2:
                        tuple.setA(tuple.getA() + t.getA());
                        tuple.setB(tuple.getB() + t.getB());
                        break;
                }
            }
        }
        if (CheckNull.isEmpty(resultMap)) {
            return null;
        }

        // 合并不同效果id的效果值
        Turple<Integer, Integer> data = new Turple<>(0, 0);
        resultMap.values().forEach(t -> {
            data.setA(data.getA() + t.getA());
            data.setB(data.getB() + t.getB());
        });

        return data;
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
