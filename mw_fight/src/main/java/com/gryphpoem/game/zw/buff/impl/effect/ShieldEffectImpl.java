package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description: 护盾
 * Author: zhangpeng
 * createTime: 2022-11-03 19:37
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class ShieldEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.SHIELD};
    }

    @Override
    public IFightBuff compareTo(List sameIdBuffList, List effectConfig, FightBuffEffect fightBuffEffect, FightContextHolder contextHolder) {
        List<Integer> effectConfig_ = effectConfig;
        FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(effectConfig_.get(1));
        if (CheckNull.isNull(buffObjective)) {
            LogUtil.error("effectConfig: ", effectConfig_, ", not found buffObjective");
            return null;
        }
        List<IFightBuff> sameIdBuffList_ = sameIdBuffList;
        Force executorForce = executorForce(sameIdBuffList_.get(0), contextHolder, effectConfig_, buffObjective);
        if (CheckNull.isNull(executorForce) || CheckNull.isEmpty(executorForce.effectExecutor)) {
            return null;
        }

        // 找到最低护盾值
        int curShieldValue = 0;
        Map<Integer, List<FightEffectData>> effectMap;
        Map<IFightBuff, Integer> effectValue = new HashMap<>(sameIdBuffList_.size());
        for (Integer heroId : executorForce.effectExecutor) {
            curShieldValue += calEffectValue(executorForce, heroId, effectConfig_);
            FightBuffEffect buffEffect = executorForce.getFightEffectMap(heroId.intValue());
            if (CheckNull.isNull(buffEffect) || (effectMap = buffEffect.getEffectMap().get(FightConstant.EffectLogicId.SHIELD)) == null)
                continue;
            List<FightEffectData> dataList = effectMap.get(effectConfig.get(2));
            if (CheckNull.isEmpty(dataList)) continue;
            sameIdBuffList_.stream().forEach(fightBuff -> {
                List<FightEffectData> dataList_ = dataList.stream().filter(data -> data.getBuffKeyId() == fightBuff.uniqueId()).collect(Collectors.toList());
                if (CheckNull.isEmpty(dataList_)) return;
                effectValue.putIfAbsent(fightBuff, 0);
                effectValue.merge(fightBuff, dataList_.stream().mapToInt(FightEffectData::getValue).sum(), Integer::sum);
            });
        }

        if (CheckNull.isEmpty(effectValue))
            return sameIdBuffList_.get(0);
        Map.Entry<IFightBuff, Integer> minShieldValue = effectValue.entrySet().stream().min(Comparator.comparingInt(e -> e.getValue())).orElse(null);
        if (CheckNull.isNull(minShieldValue)) return sameIdBuffList_.get(0);
        return curShieldValue > minShieldValue.getValue() ? minShieldValue.getKey() : null;
    }

    private int calEffectValue(Force force, int heroId, List<Integer> effectConfig) {
        double attributeValue = FightCalc.attributeValue(effectConfig.get(3), force, heroId);
        return (int) (attributeValue * (effectConfig.get(4) / FightConstant.TEN_THOUSAND) + effectConfig.get(5));
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
    protected FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe) {
        return new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId(), calEffectValue(fbe.getForce(), fbe.getHeroId(), effectConfig));
    }

    @Override
    public Object effectCalculateValue(FightBuffEffect fightBuffEffect, int effectLogicId, Object... params) {
        if (CheckNull.isEmpty(fightBuffEffect.getEffectMap()))
            return null;
        Map<Integer, List<FightEffectData>> effectDataMap = fightBuffEffect.getEffectMap().get(effectLogicId);
        if (CheckNull.isEmpty(effectDataMap)) {
            return null;
        }

        Map<Integer, Map<Integer, ArrayList<FightEffectData>>> effectValue = new HashMap<>();
        StaticFightManager staticFightManager = DataResource.ac.getBean(StaticFightManager.class);
        // 合并相同buff来源的效果
        for (Map.Entry<Integer, List<FightEffectData>> entry : effectDataMap.entrySet()) {
            StaticEffectRule rule = staticFightManager.getStaticEffectRule(entry.getKey());
            if (CheckNull.isNull(rule)) continue;
            if (CheckNull.isEmpty(entry.getValue())) continue;
            Map<Integer, ArrayList<FightEffectData>> buffIdMap = effectValue.computeIfAbsent(entry.getKey(), m -> new HashMap<>());
            entry.getValue().forEach(data -> {
                ArrayList<FightEffectData> valueList = buffIdMap.computeIfAbsent(data.getBuffId(), l -> new ArrayList<>());
                if (valueList.size() == 0) {
                    valueList.add(data);
                    return;
                }
                switch (rule.getSameBuffRule()) {
                    case 1:
                        if (compareValue(fightBuffEffect.getForce(), fightBuffEffect.getHeroId(),
                                effectLogicId, valueList.get(0).getValue(), data.getValue())) {
                            valueList.clear();
                            valueList.add(data);
                        }
                        break;
                    case 0:
                    case 2:
                        valueList.add(data);
                        break;
                    default:
                        break;
                }
            });
        }
        if (CheckNull.isEmpty(effectValue))
            return null;

        // 合并不同buff来源的效果
        Map<Integer, ArrayList<FightEffectData>> resultMap = new HashMap<>(effectValue.size());
        for (Map.Entry<Integer, Map<Integer, ArrayList<FightEffectData>>> entry : effectValue.entrySet()) {
            StaticEffectRule rule = staticFightManager.getStaticEffectRule(entry.getKey());
            if (CheckNull.isNull(rule))
                continue;
            if (CheckNull.isEmpty(entry.getValue()))
                continue;
            ArrayList<FightEffectData> tuple = null;
            for (ArrayList<FightEffectData> t : entry.getValue().values()) {
                if (CheckNull.isEmpty(t))
                    continue;
                if (tuple == null) {
                    tuple = t;
                    resultMap.put(entry.getKey(), tuple);
                    continue;
                }

                switch (rule.getDiffBuffRule()) {
                    case 1:
                        int nextShieldValue = t.stream().mapToInt(d -> d.getValue()).sum();
                        int curShieldValue = resultMap.get(entry.getKey()).stream().mapToInt(d -> d.getValue()).sum();
                        if (compareValue(fightBuffEffect.getForce(), fightBuffEffect.getHeroId(),
                                effectLogicId, curShieldValue, nextShieldValue)) {
                            tuple = t;
                        }
                        break;
                    case 0:
                    case 2:
                        tuple.addAll(t);
                        break;
                }
            }
        }
        if (CheckNull.isEmpty(resultMap)) {
            return null;
        }

        // 合并不同效果id的效果值
        ArrayList<FightEffectData> dataList = new ArrayList<>();
        resultMap.values().forEach(dataList::addAll);
        Collections.sort(dataList, Comparator.comparing(FightEffectData::getIndex));
        return dataList;
    }
}
