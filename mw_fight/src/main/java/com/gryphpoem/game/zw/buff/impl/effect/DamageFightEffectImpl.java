package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.data.p.EffectValueData;
import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;

/**
 * Description: buff伤害效果
 * Author: zhangpeng
 * createTime: 2022-10-28 10:13
 */
public class DamageFightEffectImpl extends AbsFightEffect {

    @Override
    public void effectiveness(IFightBuff fightBuff, Force buffGiver, FightLogic fightLogic, FightResult fightResult, StaticBuff staticBuff, Object... params) {
        
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightLogic fightLogic, FightResult fightResult, StaticBuff staticBuff, Object... params) {

    }

    @Override
    public Object calEffectValue(IFightBuff fightBuff, FightLogic fightLogic, StaticBuff staticBuff, Object... params) {
        return null;
    }

    @Override
    public EffectValueData compareTo(EffectValueData e1, EffectValueData e2) {
        return null;
    }
}
