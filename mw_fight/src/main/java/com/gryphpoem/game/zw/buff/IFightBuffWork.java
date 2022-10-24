package com.gryphpoem.game.zw.buff;

import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;

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
    int effectTiming();

    /**
     * buff 生效
     *
     * @param attacker   攻击者
     * @param defender   被攻击者
     * @param fightLogic
     * @param timing     攻击(被攻击)时机
     * @param params
     * @return
     */
    boolean buffCanEffect(Force attacker, Force defender, FightLogic fightLogic, int timing, StaticBuff staticBuff, Object... params);
}
