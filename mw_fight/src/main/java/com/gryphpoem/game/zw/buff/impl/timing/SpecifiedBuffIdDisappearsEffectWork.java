package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.timing.AbsFightEffectWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.pojo.p.ActionDirection;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.push.util.CheckNull;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-27 17:36
 */
public class SpecifiedBuffIdDisappearsEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.SPECIFIED_BUFF_ID_DISAPPEARS};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, Object... params) {
        if (ObjectUtils.isEmpty(params)) return false;
        IFightBuff lostBuff;
        if (params[0] instanceof Object[]) {
            Object[] objArr = (Object[]) params[0];
            lostBuff = (IFightBuff) objArr[0];
        } else {
            lostBuff = (IFightBuff) params[0];
        }

        if (lostBuff.getBuffConfig().getBuffId() != conditionConfig.get(2)) {
            return false;
        }
        if (conditionConfig.get(0) == 0) {
            return true;
        }

        ActionDirection actionDirection = triggerForce(fightBuff, contextHolder, conditionConfig);
        if (CheckNull.isNull(actionDirection) || CheckNull.isEmpty(actionDirection.getAtkHeroList())) {
            return false;
        }

        return actionDirection.getAtkHeroList().get(0) == lostBuff.getForceId();
    }
}
