package com.gryphpoem.game.zw.buff.impl.timing;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.timing.AbsFightEffectWork;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.pojo.p.FightLogic;

import java.util.List;

/**
 * Description: 指定BUFF_ID叠加到指定层数
 * Author: zhangpeng
 * createTime: 2022-10-27 18:28
 */
public class SpecifyBuffStackLayerEffectWork extends AbsFightEffectWork {
    @Override
    public int[] effectTiming() {
        return new int[]{FightConstant.BuffEffectTiming.SPECIFY_BUFF_TO_STACK_TO_THE_SPECIFIED_LAYER_NUM};
    }

    @Override
    public boolean buffCanEffect(IFightBuff fightBuff, FightLogic fightLogic, List<Integer> conditionConfig, StaticBuff staticBuff, Object... params) {
        
        return false;
    }
}
