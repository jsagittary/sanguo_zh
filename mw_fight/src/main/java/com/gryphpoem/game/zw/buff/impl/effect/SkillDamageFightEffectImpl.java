package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.data.p.EffectValueData;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;

/**
 * Description: 技能伤害效果 填表格式：[[执行者,执行对象,效果ID1,0,万分比,固定值],[执行者,执行对象,效果ID2,0,万分比,固定值]…]
 * Author: zhangpeng
 * createTime: 2022-10-28 10:13
 */
public class SkillDamageFightEffectImpl extends AbsFightEffect {

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, Object... params) {
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, Object... params) {

    }

    @Override
    public Object calEffectValue(IFightBuff fightBuff, FightContextHolder contextHolder, Object... params) {
        return null;
    }

    @Override
    public EffectValueData compareTo(EffectValueData e1, EffectValueData e2) {
        return null;
    }

}
