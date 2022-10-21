package com.gryphpoem.game.zw.buff.abs.effect;

import com.gryphpoem.game.zw.buff.IFightEffect;

/**
 * Description: 循环效果
 * Author: zhangpeng
 * createTime: 2022-10-20 18:31
 */
public abstract class AbsCycleFightEffect implements IFightEffect {
    /**
     * 剩余生效次数
     */
    protected int remainTimesNum;

}
