package com.gryphpoem.game.zw.buff.abs.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;

/**
 * Description: 循环效果
 * Author: zhangpeng
 * createTime: 2022-10-20 18:31
 */
public abstract class AbsFightEffect implements IFightEffect {
    @Override
    public void effectCalculateValue(IFightBuff originFightBuff, Object effectValue, FightContextHolder contextHolder, Object... params) {
        
    }
}
