package com.gryphpoem.game.zw.buff.abs.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightBuffWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pojo.p.ActionDirection;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
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
     * @return
     */
    protected ActionDirection triggerForce(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig) {
        FightConstant.BuffObjective atkObj = FightConstant.BuffObjective.convertTo(conditionConfig.get(0));
        if (CheckNull.isNull(atkObj)) {
            LogUtil.error("fightBuff: ", fightBuff, ", effectConfig: ", conditionConfig, ", 执行方或被执行方未找到");
            return null;
        }

        ActionDirection actionDirection = new ActionDirection();
        actionDirection.setAtkHeroList(new ArrayList<>());
        FightUtil.buffEffectActionDirection(fightBuff, contextHolder, atkObj, actionDirection, true);
        if (CheckNull.isEmpty(actionDirection.getAtkHeroList())) {
            LogUtil.error("fightBuff: ", fightBuff, ", effectConfig: ", conditionConfig, ", 触发者为空!");
            return null;
        }

        return actionDirection;
    }

    protected boolean canRelease(ActionDirection actionDirection, List<Integer> forceList, FightConstant.BuffObjective buffObjective) {
        boolean canEffect = true;
        switch (buffObjective) {
            case AT_LEAST_ONE_HERO_FROM_ENEMY_SIDE:
            case AT_LEAST_ONE_HERO_FROM_MY_SIDE:
                for (Integer heroId : forceList) {
                    if (actionDirection.getAtkHeroList().contains(heroId)) {
                        break;
                    }
                }
                break;
            default:
                for (Integer heroId : actionDirection.getAtkHeroList()) {
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
