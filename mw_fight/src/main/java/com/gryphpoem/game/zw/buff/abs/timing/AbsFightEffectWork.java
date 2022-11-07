package com.gryphpoem.game.zw.buff.abs.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightBuffWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;

/**
 * Description: buff触发时机抽象类
 * Author: zhangpeng
 * createTime: 2022-10-27 14:11
 */
public abstract class AbsFightEffectWork implements IFightBuffWork {
    /**
     * 触发者
     *
     * @param fightBuff
     * @param contextHolder
     * @param buffObjective
     * @return
     */
    protected Force triggerForce(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, FightConstant.BuffObjective buffObjective) {
        Force triggerForce = FightUtil.getActingForce(fightBuff, contextHolder, buffObjective);
        FightUtil.fillActingHeroList(fightBuff, triggerForce, triggerForce.buffTriggerId, contextHolder, buffObjective);
        if (CheckNull.isEmpty(triggerForce.buffTriggerId)) {
            LogUtil.error("buffId: ", fightBuff.getBuffConfig().getBuffId(),
                    ", conditionConfig: ", conditionConfig, ", triggerList is empty");
        }
        return triggerForce;
    }

    protected boolean canRelease(Force triggerForce, List<Integer> forceList, FightConstant.BuffObjective buffObjective) {
        boolean canEffect = true;
        switch (buffObjective) {
            case AT_LEAST_ONE_HERO_FROM_ENEMY_SIDE:
            case AT_LEAST_ONE_HERO_FROM_MY_SIDE:
                for (Integer heroId : forceList) {
                    if (triggerForce.buffTriggerId.contains(heroId)) {
                        break;
                    }
                }
                break;
            default:
                for (Integer heroId : triggerForce.buffTriggerId) {
                    if (!forceList.contains(heroId)) {
                        canEffect = false;
                        break;
                    }
                }
                break;
        }

        return canEffect;
    }
}
