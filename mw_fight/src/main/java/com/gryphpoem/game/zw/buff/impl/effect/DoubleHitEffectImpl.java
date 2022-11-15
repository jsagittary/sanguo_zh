package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.game.zw.util.FightPbUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-04 19:14
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class DoubleHitEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.DOUBLE_HIT};
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
        return null;
    }

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, int timing, Object... params) {
        if (contextHolder.getCurMultiEffectActionPb() != null) {
            // 当前动作嵌套动作已有一层
            return;
        }

        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection) || CheckNull.isEmpty(actionDirection.getAtkHeroList()) || CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            return;
        }

        contextHolder.setCurMultiEffectActionPb(BattlePb.MultiEffectAction.newBuilder());
        contextHolder.setEffectAttackActionPb(BattlePb.OrdinaryAttackAction.newBuilder());
        BattleLogic battleLogic = DataResource.ac.getBean(BattleLogic.class);
        for (Integer atkHeroId : actionDirection.getAtkHeroList()) {
            actionDirection.setCurAtkHeroId(atkHeroId);
            int actionAtkId = FightPbUtil.getActingSize(actionDirection.getAtk(), atkHeroId);
            for (Integer heroId : actionDirection.getDefHeroList()) {
                actionDirection.setCurDefHeroId(heroId);
                LogUtil.fight("执行连击效果, 攻击方: ", actionDirection.getAtk().ownerId,
                        ", 武将: ", atkHeroId, ", 被攻击方: ", actionDirection.getDef().ownerId, ", 被攻击武将: ", heroId);
                battleLogic.buffOrdinaryAttack(actionDirection, contextHolder, contextHolder.getBattleType());
                // 将当前动作添加进嵌套动作里
                contextHolder.getCurMultiEffectActionPb().addAction(FightPbUtil.createBaseActionPb(BattlePb.OrdinaryAttackAction.action,
                        contextHolder.getCurEffectAttackActionPb().build(), BattlePb.ActionTypeDefine.ORDINARY_ATTACK_VALUE, actionAtkId));
            }
        }

        // 创建baseEffectAction pb
        SimpleHeroSkill simpleHeroSkill = (SimpleHeroSkill) fightBuff.getSkill();
        BattlePb.BaseEffectAction.Builder basePb = FightPbUtil.createBaseEffectActionPb(BattlePb.MultiEffectAction.effect,
                contextHolder.getCurMultiEffectActionPb().build(), FightConstant.EffectLogicId.DOUBLE_HIT,
                FightPbUtil.getActingSize(fightBuff.getBuffGiver(), fightBuff.getBuffGiverId()),
                FightPbUtil.getActingSize(fightBuff.getForce(), fightBuff.getForceId()), timing, FightConstant.EffectStatus.APPEAR,
                simpleHeroSkill.isOnStageSkill(), simpleHeroSkill.getS_skill().getSkillId());
        if (contextHolder.getCurSkillActionPb() != null) {
            contextHolder.getCurSkillActionPb().addEffectAction(basePb.build());
        } else {
            contextHolder.getCurAttackActionPb().addEffectAction(basePb.build());
        }

        // 清除嵌套效果
        contextHolder.setCurMultiEffectActionPb(null);
        contextHolder.setEffectAttackActionPb(null);
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
    }

    @Override
    public boolean canEffect(FightContextHolder contextHolder, Object... params) {
        // 若当前没有嵌套动作, 则可以触发连击效果
        return CheckNull.isNull(contextHolder.getCurMultiEffectActionPb());
    }
}
