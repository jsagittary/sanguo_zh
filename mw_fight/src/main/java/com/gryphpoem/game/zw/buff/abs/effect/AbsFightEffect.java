package com.gryphpoem.game.zw.buff.abs.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.FightBuffEffect;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.FightEffectData;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;

/**
 * Description: 效果抽象类
 * Author: zhangpeng
 * createTime: 2022-10-20 18:31
 */
public abstract class AbsFightEffect implements IFightEffect {

    /**
     * 计算效果执行人与被执行人
     *
     * @param fightBuff
     * @param contextHolder
     * @param effectConfig
     * @param buffObjective
     * @return
     */
    protected Force executorForce(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> effectConfig, FightConstant.BuffObjective buffObjective) {
        Force executorForce = FightUtil.getActingForce(fightBuff, contextHolder, buffObjective);
        if (CheckNull.isNull(executorForce)) {
            LogUtil.error("fightBuff: ", fightBuff, ", effectConfig: ", effectConfig, ", executor is null");
            return null;
        }

        FightUtil.fillActingHeroList(fightBuff, executorForce, executorForce.effectExecutor, buffObjective);
        if (CheckNull.isEmpty(executorForce.effectExecutor)) {
            LogUtil.error("buffId: ", fightBuff.getBuffConfig().getBuffId(),
                    ", conditionConfig: ", effectConfig, ", effectExecutor list is empty");
        }
        return executorForce;
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
            StaticEffectRule rule = staticFightManager.getStaticEffectRule(entry.getKey());
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

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(effectConfig_.get(1));
        if (CheckNull.isNull(buffObjective)) {
            LogUtil.error("effectConfig: ", effectConfig_, ", not found buffObjective");
            return;
        }
        Force executor = executorForce(fightBuff, contextHolder, effectConfig_, buffObjective);
        if (CheckNull.isNull(executor)) {
            return;
        }
        if (!CheckNull.isEmpty(executor.effectExecutor)) {
            if (Objects.nonNull(rule)) {
                int nextIndex = 0;
                FightEffectData data = new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId(), effectConfig_.subList(4, 6));
                for (Integer heroId : executor.effectExecutor) {
                    FightBuffEffect fbe = executor.getFightEffectMap(heroId);
                    Map<Integer, List<FightEffectData>> dataMap = fbe.getEffectMap().get(rule.getEffectLogicId());
                    if (CheckNull.isEmpty(dataMap)) {
                        nextIndex = 0;
                    } else {
                        nextIndex = dataMap.entrySet().stream().mapToInt(m -> m.getValue().size()).sum();
                    }
                    fbe.getEffectMap().computeIfAbsent(rule.getEffectLogicId(), m -> new HashMap<>()).
                            computeIfAbsent(effectConfig_.get(2), l -> new ArrayList<>()).add(data);
                    data.setIndex(nextIndex);
                    // TODO 客户端pb添加

                }
            }
        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
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

    @Override
    public void randomRoundValue(FightBuffEffect fightBuffEffect) {
    }

    /**
     * 比较效果值大小
     *
     * @param actingForce
     * @param actingHeroId
     * @param effectLogicId
     * @param params
     * @return
     */
    protected abstract boolean compareValue(Force actingForce, int actingHeroId, int effectLogicId, Object... params);

    /**
     * 计算效果值
     *
     * @param force
     * @param heroId
     * @param effectLogicId
     * @param params
     * @return
     */
    protected abstract double calValue(Force force, int heroId, int effectLogicId, Object... params);
}
