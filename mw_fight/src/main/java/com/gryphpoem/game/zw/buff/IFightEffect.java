package com.gryphpoem.game.zw.buff;

import com.gryphpoem.game.zw.data.p.EffectValueData;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;

import java.util.List;

/**
 * Description: 效果接口
 * <p>
 * Author: zhangpeng
 * createTime: 2022-10-20 17:24
 */
public interface IFightEffect<T> {

    int effectType();

    /**
     * 效果生效
     *
     * @param fightBuff     战斗buff
     * @param contextHolder
     * @param params
     * @return
     */
    void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> effectConfig, Object... params);

    /**
     * 效果结束, 属性等还原
     *
     * @param fightBuff     战斗buff
     * @param contextHolder
     * @param params
     * @return
     */
    void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> effectConfig, Object... params);

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
     * 公式结算时, 算出效果值参数
     *
     * @param contextHolder
     * @param params
     * @return
     */
    List<Integer> effectCalculateValue(FightContextHolder contextHolder, Object... params);
}
