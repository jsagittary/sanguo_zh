package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.FightBuffEffect;
import com.gryphpoem.game.zw.pojo.p.Force;

import java.util.List;

/**
 * Description: 技能伤害效果 填表格式：[[执行者,执行对象,效果ID1,0,万分比,固定值],[执行者,执行对象,效果ID2,0,万分比,固定值]…]
 * Author: zhangpeng
 * createTime: 2022-10-28 10:13
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class SkillDamageFightEffectImpl extends AbsFightEffect {

    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.SKILL_DAMAGE};
    }

    @Override
    public IFightBuff compareTo(List sameIdBuffList, List effectConfig, FightBuffEffect fightBuffEffect) {
        return null;
    }

    @Override
    protected boolean compareValue(Force actingForce, int actingHeroId, int effectLogicId, Object... params) {
        return false;
    }

    @Override
    protected double calValue(Force force, int heroId, int effectLogicId, Object... params) {
        return 0;
    }
}
