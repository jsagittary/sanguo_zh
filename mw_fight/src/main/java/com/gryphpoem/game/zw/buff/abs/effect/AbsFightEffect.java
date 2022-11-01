package com.gryphpoem.game.zw.buff.abs.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pojo.p.FightBuffEffect;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.FightEffectData;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Description: 效果抽象类
 * Author: zhangpeng
 * createTime: 2022-10-20 18:31
 */
public abstract class AbsFightEffect implements IFightEffect {

    protected Force executorForce(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, FightConstant.BuffObjective buffObjective) {
        Force executorForce = FightUtil.getActingForce(fightBuff, contextHolder, buffObjective);
        FightUtil.fillActingHeroList(fightBuff, executorForce, executorForce.effectExecutor, buffObjective);
        if (CheckNull.isEmpty(executorForce.effectExecutor)) {
            LogUtil.error("buffId: ", fightBuff.getBuffConfig().getBuffId(),
                    ", conditionConfig: ", conditionConfig, ", effectExecutor list is empty");
        }
        return executorForce;
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
            FightEffectData data = new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId(), effectConfig_.subList(4, 6));
            for (Integer heroId : executor.effectExecutor) {
                FightBuffEffect fbe = executor.getFightEffectMap(heroId);
                fbe.getEffectMap().computeIfAbsent(effectConfig_.get(2), l -> new ArrayList<>()).add(data);
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
        if (!CheckNull.isEmpty(executor.effectExecutor)) {
            for (Integer heroId : executor.effectExecutor) {
                FightBuffEffect fbe = executor.getFightEffectMap(heroId);
                List<FightEffectData> effectList = fbe.getEffectMap().get(effectConfig_.get(2));
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
                    }
                }
            }
        }
    }
}
