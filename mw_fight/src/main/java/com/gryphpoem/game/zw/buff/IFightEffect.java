package com.gryphpoem.game.zw.buff;

import com.gryphpoem.game.zw.pojo.p.FightBuffEffect;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;

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
    void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> effectConfig, StaticEffectRule rule, int timing, Object... params);

    /**
     * 效果结束, 属性等还原
     *
     * @param fightBuff     战斗buff
     * @param contextHolder
     * @param params
     * @return
     */
    void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> effectConfig, StaticEffectRule rule, Object... params);

    /**
     * 相同buff下比较相同效果值大小
     *
     * @param sameIdBuffList  相同的buff列表
     * @param effectConfig    被比较的效果配置
     * @param fightBuffEffect buff持有人的buff效果总览
     * @return
     */
    IFightBuff compareTo(List<IFightBuff> sameIdBuffList, List<Integer> effectConfig, FightBuffEffect fightBuffEffect, FightContextHolder contextHolder);

    /**
     * 公式结算时, 算出效果值参数
     *
     * @param fightBuffEffect
     * @param params
     * @return
     */
    Object effectCalculateValue(FightBuffEffect fightBuffEffect, int effectLogicId, Object... params);

    /**
     * 可否触发效果
     *
     * @param contextHolder
     * @return
     */
    boolean canEffect(FightContextHolder contextHolder, StaticEffectRule staticEffectRule, Object... params);
}
