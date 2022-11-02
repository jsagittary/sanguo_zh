package com.gryphpoem.game.zw.buff.abs.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

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
}
