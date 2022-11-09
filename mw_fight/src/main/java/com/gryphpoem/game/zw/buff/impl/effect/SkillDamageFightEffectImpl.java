package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.push.util.CheckNull;

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
    public IFightBuff compareTo(List sameIdBuffList, List effectConfig, FightBuffEffect fightBuffEffect, FightContextHolder contextHolder) {
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

    @Override
    protected FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe) {
        return new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId());
    }

    @Override
    public Object effectCalculateValue(FightBuffEffect fightBuffEffect, int effectLogicId, Object... params) {
        return null;
    }

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection) || CheckNull.isEmpty(actionDirection.getAtkHeroList()) || CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            return;
        }

        if (!CheckNull.isEmpty(actionDirection.getAtkHeroList()) && !CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            BattleLogic battleLogic = DataResource.ac.getBean(BattleLogic.class);
            for (Integer atkHeroId : actionDirection.getAtkHeroList()) {
                actionDirection.setCurAtkHeroId(atkHeroId);
                for (Integer heroId : actionDirection.getDefHeroList()) {
                    actionDirection.setCurDefHeroId(heroId);
                    LogUtil.fight("执行技能伤害效果, 伤害攻击方: ", actionDirection.getAtk() == null ? 0 : actionDirection.getAtk().ownerId,
                            ", 攻击武将: ", atkHeroId, ", 伤害被攻击方: ", actionDirection.getDef() == null ? 0 : actionDirection.getDef().ownerId,
                            ", 被攻击武将: ", heroId);
                    battleLogic.skillAttack(actionDirection, contextHolder, effectConfig_, contextHolder.getBattleType());
                }
            }
        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
    }
}
