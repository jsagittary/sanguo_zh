package com.gryphpoem.game.zw.buff;

import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;

/**
 * Description: 效果接口
 * <p>
 * Author: zhangpeng
 * createTime: 2022-10-20 17:24
 */
public interface IFightEffect extends IUniqueId {
    /**
     * 扣除效果次数
     */
    void deductEffectTimes();

    /**
     * 是否还有生效次数
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param params
     * @return
     */
    boolean hasRemainEffect(Force attacker, Force defender, FightLogic fightLogic, Object... params);

    /**
     * 效果生效
     *
     * @param attacker   攻击者
     * @param defender   被攻击者
     * @param fightLogic
     * @param params
     * @return
     */
    FightResult effectiveness(Force attacker, Force defender, FightLogic fightLogic, Object... params);

    /**
     * 效果结束, 属性等还原
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param params
     * @return
     */
    FightResult effectRestoration(Force attacker, Force defender, FightLogic fightLogic, Object... params);
}
