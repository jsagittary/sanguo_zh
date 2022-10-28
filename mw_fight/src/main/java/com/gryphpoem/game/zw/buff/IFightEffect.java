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
     * @param fightBuff  战斗buff
     * @param fightLogic
     * @param params
     * @return
     */
    void effectiveness(IFightBuff fightBuff, Force buffGiver, FightLogic fightLogic, FightResult fightResult, StaticBuff staticBuff, Object... params);

    /**
     * 效果结束, 属性等还原
     *
     * @param fightBuff  战斗buff
     * @param fightLogic
     * @param params
     * @return
     */
    void effectRestoration(IFightBuff fightBuff, FightLogic fightLogic, FightResult fightResult, StaticBuff staticBuff, Object... params);

    /**
     * 算出效果强度值
     *
     * @param fightBuff  战斗buff
     * @param fightLogic
     * @param params
     * @return
     */
    Object calEffectValue(IFightBuff fightBuff, FightLogic fightLogic, StaticBuff staticBuff, Object... params);

    /**
     * 比较两个效果强度大小
     *
     * @param e1
     * @param e2
     * @return
     */
    EffectValueData compareTo(EffectValueData e1, EffectValueData e2);
}
