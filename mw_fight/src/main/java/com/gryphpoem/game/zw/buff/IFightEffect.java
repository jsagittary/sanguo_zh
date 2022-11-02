package com.gryphpoem.game.zw.buff;

import com.gryphpoem.game.zw.pojo.p.FightBuffEffect;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;

import java.util.List;

/**
 * Description: 效果接口
 * <p>
 * Author: zhangpeng
 * createTime: 2022-10-20 17:24
 */
public interface IFightEffect<T> {

    int[] effectType();

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
     * 比较效果值大小
     *
     * @param sameIdBuffList  相同的buff列表
     * @param effectConfig    被比较的效果配置
     * @param fightBuffEffect buff持有人的buff效果总览
     * @return
     */
    boolean compareTo(List<IFightBuff> sameIdBuffList, List<Integer> effectConfig, FightBuffEffect fightBuffEffect);

    /**
     * 公式结算时, 算出效果值参数
     *
     * @param actingForce
     * @param params
     * @return
     */
    Object effectCalculateValue(Force actingForce, int actingHeroId, int effectLogicId, Object... params);
}
