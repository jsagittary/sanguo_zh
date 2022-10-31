package com.gryphpoem.game.zw.buff;

import com.gryphpoem.game.zw.pojo.p.FightContextHolder;

import java.util.List;

/**
 * Description: buff生效判断 不同生效时机逻辑不同
 * Author: zhangpeng
 * createTime: 2022-10-20 18:29
 */
public interface IFightBuffWork {
    /**
     * buff作用时机类型
     *
     * @return
     */
    int[] effectTiming();

    /**
     * buff 生效
     *
     * @param fightBuff buff
     * @param params
     * @return
     */
    boolean buffCanEffect(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> conditionConfig, Object... params);
}
