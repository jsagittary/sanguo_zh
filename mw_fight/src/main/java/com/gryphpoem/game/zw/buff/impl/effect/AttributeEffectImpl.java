package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.FightBuffEffect;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.FightEffectData;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;

/**
 * Description: 属性变化效果
 * Author: zhangpeng
 * createTime: 2022-11-01 16:46
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class AttributeEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.SKILL_DAMAGE,
                FightConstant.EffectLogicId.ATTACK_INCREASED,
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
                FightConstant.EffectLogicId.SPEED_REDUCTION};
    }

    @Override
    public boolean compareTo(List sameIdBuffList, List effectConfig, FightBuffEffect fightBuffEffect) {
        return true;
    }

    /**
     * 计算属性结果值
     *
     * @param force
     * @param heroId
     * @param effectType
     * @param tenThousandthRatio
     * @param fixValue
     * @return
     */
    private double calValue(Force force, int heroId, int effectType, int tenThousandthRatio, int fixValue) {
        switch (effectType) {
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

    /**
     * 计算属性值
     *
     * @param effectType
     * @param tenThousandthRatio
     * @param fixValue
     * @return
     */
    private boolean compareValue(Force force, int heroId, int effectType, int tenThousandthRatio, int fixValue, Object config) {
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

        switch (effectType) {
            case FightConstant.EffectLogicId.ATTACK_INCREASED:
            case FightConstant.EffectLogicId.DEFENSE_INCREASED:
            case FightConstant.EffectLogicId.SPEED_INCREASE:
            case FightConstant.EffectLogicId.BROKEN_CITY_PROMOTION:
            case FightConstant.EffectLogicId.PROTECTION_LIFTING:
            case FightConstant.EffectLogicId.ARMOR_PIERCING_ENHANCEMENT:
            case FightConstant.EffectLogicId.UPWARD_GUARDING:
                return calValue(force, heroId, effectType, tenThousandthRatio_, fixValue_) >
                        calValue(force, heroId, effectType, tenThousandthRatio, fixValue);
            case FightConstant.EffectLogicId.REDUCED_ATTACK:
            case FightConstant.EffectLogicId.REDUCED_DEFENSE:
            case FightConstant.EffectLogicId.BROKEN_CITY_REDUCED:
            case FightConstant.EffectLogicId.GUARD_CITY_REDUCED:
            case FightConstant.EffectLogicId.ARMOR_PIERCING_REDUCTION:
            case FightConstant.EffectLogicId.REDUCED_PROTECTION:
            case FightConstant.EffectLogicId.SPEED_REDUCTION:
                return calValue(force, heroId, effectType, tenThousandthRatio_, fixValue_) <
                        calValue(force, heroId, effectType, tenThousandthRatio, fixValue);
            default:
                return false;
        }
    }

    @Override
    public Object effectCalculateValue(Force actingForce, int actingHeroId, int effectLogicId, Object... params) {
        if (CheckNull.isEmpty(actingForce.getFightEffectMap(actingHeroId).getEffectMap()))
            return null;
        Map<Integer, List<FightEffectData>> effectDataMap = actingForce.getFightEffectMap(actingHeroId).getEffectMap().get(effectLogicId);
        if (CheckNull.isEmpty(effectDataMap)) {
            return null;
        }

        Map<Integer, Map<Integer, Turple<Integer, Integer>>> effectValue = new HashMap<>();
        StaticFightManager staticFightManager = DataResource.ac.getBean(StaticFightManager.class);
        // 合并相同buff来源的效果
        for (Map.Entry<Integer, List<FightEffectData>> entry : effectDataMap.entrySet()) {
            StaticEffectRule rule = staticFightManager.getStaticEffectRule(entry.getKey());
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
                        if (compareValue(actingForce, actingHeroId, effectLogicId, value.getA(), value.getB(), data.getData())) {
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
            StaticEffectRule rule = staticFightManager.getStaticEffectRule(entry.getKey());
            if (CheckNull.isNull(rule))
                continue;
            if (CheckNull.isEmpty(entry.getValue()))
                continue;
            Turple<Integer, Integer> tuple = null;
            for (Turple<Integer, Integer> t : entry.getValue().values()) {
                switch (rule.getDiffBuffRule()) {
                    case 1:
                        if (tuple == null) {
                            tuple = t;
                            resultMap.put(entry.getKey(), tuple);
                            continue;
                        }
                        if (compareValue(actingForce, actingHeroId, effectLogicId, tuple.getA(), tuple.getB(), t)) {
                            tuple = t;
                        }
                        break;
                    case 0:
                    case 2:
                        if (tuple == null) {
                            tuple = t;
                            resultMap.put(entry.getKey(), tuple);
                            continue;
                        }
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

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(effectConfig_.get(1));
        if (CheckNull.isNull(buffObjective)) {
            LogUtil.error("effectConfig: ", effectConfig_, ", not found buffObjective");
            return;
        }
        Force executor = executorForce(fightBuff, contextHolder, effectConfig_, buffObjective);
        if (CheckNull.isNull(executor)) {
            LogUtil.error("fightBuff: ", fightBuff, ", effectConfig: ", effectConfig_, ", executor is null");
            return;
        }
        if (!CheckNull.isEmpty(executor.effectExecutor)) {
            StaticEffectRule rule = DataResource.ac.getBean(StaticFightManager.class).getStaticEffectRule(effectConfig_.get(2));
            if (Objects.nonNull(rule)) {
                FightEffectData data = new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId(), effectConfig_.subList(4, 6));
                for (Integer heroId : executor.effectExecutor) {
                    FightBuffEffect fbe = executor.getFightEffectMap(heroId);
                    fbe.getEffectMap().computeIfAbsent(rule.getEffectLogicId(), m -> new HashMap<>()).
                            computeIfAbsent(effectConfig_.get(2), l -> new ArrayList<>()).add(data);
                    // TODO 客户端pb添加

                }
            }
        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(effectConfig_.get(1));
        if (CheckNull.isNull(buffObjective)) {
            LogUtil.error("effectConfig: ", effectConfig_, ", not found buffObjective");
            return;
        }
        Force executor = executorForce(fightBuff, contextHolder, effectConfig_, buffObjective);
        if (CheckNull.isNull(executor)) {
            LogUtil.error("fightBuff: ", fightBuff, ", effectConfig: ", effectConfig_, ", executor is null");
            return;
        }

        StaticFightManager staticFightManager = DataResource.ac.getBean(StaticFightManager.class);
        StaticEffectRule rule = staticFightManager.getStaticEffectRule(effectConfig_.get(2));
        if (CheckNull.isNull(rule)) {
            LogUtil.error("buffConfig: ", fightBuff.getBuffConfig(), ", effectConfig: ", effectConfig_, ", not found!");
            return;
        }
        if (!CheckNull.isEmpty(executor.effectExecutor)) {
            for (Integer heroId : executor.effectExecutor) {
                FightBuffEffect fbe = executor.getFightEffectMap(heroId);
                Map<Integer, List<FightEffectData>> effectIdMap = fbe.getEffectMap().get(rule.getEffectLogicId());
                if (CheckNull.isEmpty(effectIdMap)) continue;
                List<FightEffectData> effectList = effectIdMap.get(effectConfig_.get(2));
                if (CheckNull.isEmpty(effectList)) continue;
                Iterator<FightEffectData> it = effectList.iterator();
                while (it.hasNext()) {
                    FightEffectData data = it.next();
                    if (CheckNull.isNull(data)) {
                        it.remove();
                        continue;
                    }
                    if (data.getBuffKeyId() == fightBuff.uniqueId()) {
                        it.remove();
                        // TODO 客户端pb添加
                        
                    }
                }
            }
        }
    }
}
