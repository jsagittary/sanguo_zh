package com.gryphpoem.game.zw.buff.abs.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
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
     * 计算效果被执行人
     *
     * @param fightBuff
     * @param contextHolder
     * @param effectConfig
     * @return
     */
    protected ActionDirection actionDirection(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> effectConfig) {
        FightConstant.BuffObjective atkObj = FightConstant.BuffObjective.convertTo(effectConfig.get(0));
        FightConstant.BuffObjective defObj = FightConstant.BuffObjective.convertTo(effectConfig.get(1));
        if (CheckNull.isNull(atkObj) || CheckNull.isNull(defObj)) {
            LogUtil.error("fightBuff: ", fightBuff, ", effectConfig: ", effectConfig, ", 执行方或被执行方未找到");
            return null;
        }

        ActionDirection actionDirection = new ActionDirection();
        FightUtil.buffEffectActionDirection(fightBuff, contextHolder, atkObj, actionDirection, true);
        FightUtil.buffEffectActionDirection(fightBuff, contextHolder, defObj, actionDirection, false);
        if (CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            LogUtil.error("fightBuff: ", fightBuff, ", effectConfig: ", effectConfig, ", 被执行人为空");
            return null;
        }

        return actionDirection;
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

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection)) {
            return;
        }
        if (!CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            if (Objects.nonNull(rule)) {
                for (Integer heroId : actionDirection.getDefHeroList()) {
                    Force def = actionDirection.getDef();
                    FightBuffEffect fbe = def.getFightEffectMap(heroId);
                    FightEffectData data = createFightEffectData(fightBuff, effectConfig_, fbe);
                    fbe.getEffectMap().computeIfAbsent(rule.getEffectLogicId(), m -> new HashMap<>()).
                            computeIfAbsent(effectConfig_.get(2), l -> new ArrayList<>()).add(data);
                    // TODO 客户端pb添加

                }
            }
        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection)) {
            return;
        }

        if (!CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            for (Integer heroId : actionDirection.getDefHeroList()) {
                Force def = actionDirection.getDef();
                FightBuffEffect fbe = def.getFightEffectMap(heroId);
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
                        LogUtil.fight("buff效果失效, 效果持有人: ", def.ownerId, "-", heroId,
                                ", 损失的效果: ", Arrays.toString(effectConfig_.toArray()), ", 消失的效果参数: ", data);
                        // TODO 客户端pb添加

                    }
                }
                if (CheckNull.isEmpty(effectList)) {
                    effectIdMap.remove(effectConfig_.get(2));
                    if (CheckNull.isEmpty(effectIdMap)) {
                        fbe.getEffectMap().remove(rule.getEffectLogicId());
                    }
                }
            }
        }
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

    /**
     * 创建战斗效果实体
     *
     * @param fightBuff
     * @param effectConfig
     * @param fbe
     * @return
     */
    protected abstract FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe);
}
