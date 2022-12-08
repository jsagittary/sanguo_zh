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
 * Description: 恢复型的战斗属性(士气, 能量)
 * Author: zhangpeng
 * createTime: 2022-11-07 15:04
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class RecoveredInBattleEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.ENERGY_RECOVERY_VALUE_INCREASED, FightConstant.EffectLogicId.ENERGY_RECOVERY_VALUE_DECREASES,
                FightConstant.EffectLogicId.MORALE_RECOVERY_VALUE_INCREASED, FightConstant.EffectLogicId.MORALE_RECOVERY_VALUE_REDUCED,
                FightConstant.EffectLogicId.MORALE_DEDUCTION_VALUE_INCREASED, FightConstant.EffectLogicId.REDUCED_MORALE_DEDUCTION};
    }

    @Override
    protected boolean compareValue(Force actingForce, int actingHeroId, int effectLogicId, Object... params) {
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

        double originValue = (double) params[3];
        return originValue * (1 + (tenThousandthRatio / FightConstant.TEN_THOUSAND)) + fixValue >
                originValue * (1 + (tenThousandthRatio_ / FightConstant.TEN_THOUSAND)) + fixValue_;
    }

    @Override
    protected FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe, Object... params) {
        List<Integer> effectDataList = new ArrayList<>(effectConfig.subList(4, 6));
        Integer ratio = Optional.ofNullable(effectDataList.remove(0)).orElse(0);
        Integer fixValue = Optional.ofNullable(effectDataList.remove(1)).orElse(0);
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
    public Object effectCalculateValue(FightBuffEffect fightBuffEffect, int effectLogicId, Object... params) {
        double originValue = (double) params[0];
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
                                effectLogicId, value.getA(), value.getB(), data.getData(), originValue)) {
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
                                effectLogicId, tuple.getA(), tuple.getB(), t, originValue)) {
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

    @Override
    protected void addPbValue(BattlePb.CommonEffectAction.Builder builder, Object... params) {
        FightEffectData data = (FightEffectData) params[0];
        FightBuffEffect fbe = (FightBuffEffect) params[1];
        StaticEffectRule rule = (StaticEffectRule) params[2];
        if (CheckNull.isNull(data) || CheckNull.isEmpty(data.getData())) return;
        builder.addData(FightPbUtil.createDataInt(FightConstant.ValueType.RATIO, data.getData().get(0)));

        int curValue = 0;
        Turple<Integer, Integer> valueTuple = (Turple<Integer, Integer>) effectCalculateValue(fbe, rule.getEffectLogicId(), 10000);
        if (Objects.nonNull(valueTuple)) {
            curValue = valueTuple.getA();
        }
        builder.addData(FightPbUtil.createDataInt(FightConstant.ValueType.RATIO, curValue));
    }
}
