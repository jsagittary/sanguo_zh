package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.FightBuffEffect;
import com.gryphpoem.game.zw.pojo.p.FightCalc;
import com.gryphpoem.game.zw.pojo.p.FightEffectData;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;

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
    public IFightBuff compareTo(List sameIdBuffList, List effectConfig, FightBuffEffect fightBuffEffect) {
        Map<Integer, List<FightEffectData>> effectMap = fightBuffEffect.getEffectMap().get(FightConstant.EffectLogicId.SHIELD);
        if (CheckNull.isEmpty(effectMap)) {
            LogUtil.error("异常情况, 有相同的buff, 但在效果里找不到护盾?");
            return (IFightBuff) sameIdBuffList.get(0);
        }
        List<IFightBuff> sameIdBuffList_ = sameIdBuffList;
        // 找到护盾值最低的护盾效果
        FightEffectData effectData = sameIdBuffList_.stream().map(buff -> {
            List<FightEffectData> dataList = effectMap.get(effectConfig.get(2));
            if (CheckNull.isEmpty(dataList)) return null;
            return dataList.stream().filter(data -> data.getBuffKeyId() == buff.uniqueId()).findFirst().orElse(null);
        }).filter(data -> Objects.nonNull(data)).min(Comparator.comparingInt(FightEffectData::getValue)).orElse(null);
        if (CheckNull.isNull(effectData)) return sameIdBuffList_.get(0);
        if (FightCalc.attributeValue((int) effectConfig.get(3), fightBuffEffect.getForce(), fightBuffEffect.getHeroId()) > effectData.getValue()) {
            return sameIdBuffList_.stream().filter(buff -> buff.uniqueId() == effectData.getBuffKeyId()).findFirst().orElse(null);
        }

        return null;
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
