package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightBuffWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;

/**
 * Description: 指定回合buff生效
 * Author: zhangpeng
 * createTime: 2022-10-27 10:55
 */
public class DesignatedRoundEffectWork implements IFightBuffWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.START_OF_DESIGNATED_ROUND};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightLogic fightLogic, List<Integer> conditionConfig, StaticBuff staticBuff, Object... params) {
        if (CheckNull.isEmpty(conditionConfig))
            return false;
        if (conditionConfig.size() < 2)
            return false;
        List<Integer> roundsNum = conditionConfig.subList(1, conditionConfig.size() - 1);
        if (CheckNull.isEmpty(roundsNum))
            return false;
        Integer configRounds = roundsNum.stream().filter(r -> r.intValue() == fightLogic.roundNum).findFirst().orElse(null);
        if (CheckNull.isNull(configRounds))
            return false;
        return true;
    }
}
