package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pojo.p.FightBuffEffect;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.FightEffectData;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.util.FightPbUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Description: 伤害变化效果
 * Author: zhangpeng
 * createTime: 2022-11-03 19:55
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class DamageChangeEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{
                FightConstant.EffectLogicId.DAMAGE_INCREASED_FIGHTING,
                FightConstant.EffectLogicId.DAMAGE_REDUCED_ARM_RESTRAINED,
                FightConstant.EffectLogicId.INCREASE_COMMON_ATTACK_DAMAGE,
                FightConstant.EffectLogicId.COMMON_ATTACK_DAMAGE_REDUCED,
                FightConstant.EffectLogicId.SKILL_DAMAGE_INCREASED,
                FightConstant.EffectLogicId.SKILL_DAMAGE_REDUCED,
                FightConstant.EffectLogicId.INCREASE_FINAL_DAMAGE,
                FightConstant.EffectLogicId.FINAL_DAMAGE_REDUCED,
                FightConstant.EffectLogicId.Increased_damage_general_attack,
                FightConstant.EffectLogicId.BE_COMMON_ATTACK_DAMAGE_REDUCED,
                FightConstant.EffectLogicId.BE_INCREASED_SKILL_DAMAGE,
                FightConstant.EffectLogicId.BE_SKILL_DAMAGE_REDUCED,
                FightConstant.EffectLogicId.BE_INCREASE_FINAL_DAMAGE,
                FightConstant.EffectLogicId.BE_FINAL_DAMAGE_REDUCED,
                FightConstant.EffectLogicId.BE_SKILL_DAMAGE_INCREASED,
                FightConstant.EffectLogicId.BE_SKILL_DAMAGE_REDUCED_DECREASED,
                FightConstant.EffectLogicId.BE_INCREASE_FINAL_DAMAGE_PROMOTION,
                FightConstant.EffectLogicId.FINAL_DAMAGE_REDUCED_DECREASED
        };
    }

    @Override
    public IFightBuff compareTo(List sameIdBuffList, List effectConfig, FightBuffEffect fightBuffEffect, FightContextHolder contextHolder) {
        return (IFightBuff) sameIdBuffList.get(0);
    }

    @Override
    protected boolean compareValue(Force actingForce, int actingHeroId, int effectLogicId, Object... params) {
        return (int) params[0] < (int) params[1];
    }

    @Override
    protected double calValue(Force force, int heroId, int effectLogicId, Object... params) {
        return 0;
    }

    @Override
    protected FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe, Object... params) {
        return new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId(), effectConfig.get(5));
    }

    @Override
    protected void addPbValue(BattlePb.CommonEffectAction.Builder builder, Object... params) {
        FightEffectData data = (FightEffectData) params[0];
        FightBuffEffect fbe = (FightBuffEffect) params[1];
        StaticEffectRule rule = (StaticEffectRule) params[2];
        if (CheckNull.isNull(data) || CheckNull.isEmpty(data.getData())) return;
        builder.addData(FightPbUtil.createDataInt(FightConstant.ValueType.RATIO, data.getData().get(0)));

        // 计算效果影响最终值
        Object value = effectCalculateValue(fbe, rule.getEffectLogicId());
        int curValue = 0;
        if (Objects.nonNull(value)) {
            curValue = (int) value;
        }
        builder.addData(FightPbUtil.createDataInt(FightConstant.ValueType.RATIO, curValue));
    }

    @Override
    public Object effectCalculateValue(FightBuffEffect fightBuffEffect, int effectLogicId, Object... params) {
        if (CheckNull.isEmpty(fightBuffEffect.getEffectMap()))
            return 0;
        Map<Integer, List<FightEffectData>> effectDataMap = fightBuffEffect.getEffectMap().get(effectLogicId);
        if (CheckNull.isEmpty(effectDataMap)) {
            return 0;
        }

        Map<Integer, Map<Integer, Integer>> effectValue = new HashMap<>();
        // 合并相同效果id, 相同buff来源的效果
        for (Map.Entry<Integer, List<FightEffectData>> entry : effectDataMap.entrySet()) {
            StaticEffectRule rule = StaticFightManager.getStaticEffectRule(entry.getKey());
            if (CheckNull.isNull(rule)) continue;
            if (CheckNull.isEmpty(entry.getValue())) continue;
            Map<Integer, Integer> buffIdMap = effectValue.computeIfAbsent(entry.getKey(), m -> new HashMap<>());
            entry.getValue().forEach(data -> {
                int value = buffIdMap.computeIfAbsent(data.getBuffId(), l -> 0);
                if (value == 0) {
                    value = data.getValue();
                    return;
                }
                switch (rule.getSameBuffRule()) {
                    case 1:
                        if (compareValue(fightBuffEffect.getForce(), fightBuffEffect.getHeroId(),
                                effectLogicId, value, data.getValue())) {
                            buffIdMap.put(data.getBuffId(), data.getValue());
                        }
                        break;
                    case 0:
                    case 2:
                        buffIdMap.merge(data.getBuffId(), data.getValue(), Integer::sum);
                        break;
                    default:
                        break;
                }
            });
        }
        if (CheckNull.isEmpty(effectValue))
            return null;

        // 合并相同效果id, 不同buff来源的效果
        Map<Integer, Integer> resultMap = new HashMap<>(effectValue.size());
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : effectValue.entrySet()) {
            StaticEffectRule rule = StaticFightManager.getStaticEffectRule(entry.getKey());
            if (CheckNull.isNull(rule))
                continue;
            if (CheckNull.isEmpty(entry.getValue()))
                continue;
            Integer tuple = null;
            for (Integer t : entry.getValue().values()) {
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
                                effectLogicId, tuple, t)) {
                            resultMap.put(entry.getKey(), t);
                        }
                        break;
                    case 0:
                    case 2:
                        resultMap.merge(entry.getKey(), t, Integer::sum);
                        break;
                }
            }
        }
        if (CheckNull.isEmpty(resultMap)) {
            return null;
        }

        // 合并不同效果id的效果值
        return resultMap.values().stream().mapToInt(i -> i.intValue()).sum();
    }
}
