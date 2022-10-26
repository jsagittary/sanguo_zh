package com.gryphpoem.game.zw.buff;

import com.gryphpoem.game.zw.data.p.EffectValueData;
import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;

/**
 * Description: 效果接口
 * <p>
 * Author: zhangpeng
 * createTime: 2022-10-20 17:24
 */
public interface IFightEffect {
    /**
     * 效果生效
     *
     * @param actingForce 被效果作用方
     * @param fightLogic
     * @param params
     * @return
     */
    void effectiveness(Force actingForce, Force buffGiver, FightLogic fightLogic, FightResult fightResult, StaticBuff staticBuff, Object... params);

    /**
     * 效果结束, 属性等还原
     *
     * @param actingForce 被效果作用方
     * @param fightLogic
     * @param params
     * @return
     */
    void effectRestoration(Force actingForce, FightLogic fightLogic, FightResult fightResult, StaticBuff staticBuff, Object... params);

    /**
     * 算出效果强度值
     *
     * @param actingForce
     * @param fightLogic
     * @param params
     * @return
     */
    Object calEffectValue(Force actingForce, Force attacker, FightLogic fightLogic, StaticBuff staticBuff, Object... params);

    /**
     * 比较两个效果强度大小
     *
     * @param e1
     * @param e2
     * @return
     */
    EffectValueData compareTo(EffectValueData e1, EffectValueData e2);
}
