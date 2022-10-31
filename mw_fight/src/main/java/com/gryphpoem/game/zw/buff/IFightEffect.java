package com.gryphpoem.game.zw.buff;

import com.gryphpoem.game.zw.data.p.EffectValueData;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;

/**
 * Description: 效果接口
 * <p>
 * Author: zhangpeng
 * createTime: 2022-10-20 17:24
 */
public interface IFightEffect<T> {
    /**
     * 效果生效
     *
     * @param fightBuff     战斗buff
     * @param contextHolder
     * @param params
     * @return
     */
    void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, Object... params);

    /**
     * 效果结束, 属性等还原
     *
     * @param fightBuff     战斗buff
     * @param contextHolder
     * @param params
     * @return
     */
    void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, Object... params);

    /**
     * 算出效果强度值
     *
     * @param fightBuff     战斗buff
     * @param contextHolder
     * @param params
     * @return
     */
    T calEffectValue(IFightBuff fightBuff, FightContextHolder contextHolder, Object... params);

    /**
     * 比较两个效果强度大小
     *
     * @param e1
     * @param e2
     * @return
     */
    EffectValueData compareTo(EffectValueData<T> e1, EffectValueData<T> e2);

    /**
     * 相同效果之前的覆盖或叠加
     *
     * @param originFightBuff
     * @param effectValue
     * @param contextHolder
     * @param params
     */
    void effectCalculateValue(IFightBuff originFightBuff, T effectValue, FightContextHolder contextHolder, Object... params);
}
