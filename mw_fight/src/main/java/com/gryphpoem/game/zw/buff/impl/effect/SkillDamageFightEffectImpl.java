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
    public IFightBuff compareTo(List sameIdBuffList, List effectConfig, IFightBuff fightBuff, FightBuffEffect fightBuffEffect, FightContextHolder contextHolder) {
        return null;
    }

    @Override
    protected FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe, Object... params) {
        return new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId());
    }

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, int timing, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection) || CheckNull.isEmpty(actionDirection.getAtkHeroList()) || CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            return;
        }

        if (!CheckNull.isEmpty(actionDirection.getAtkHeroList()) && !CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            if (timing != FightConstant.BuffEffectTiming.ACTIVE_RELEASE) {
                FightPbUtil.initNextMultiEffectAction(contextHolder, actionDirection, true);
            }

            BattleLogic battleLogic = DataResource.ac.getBean(BattleLogic.class);
            for (Integer atkHeroId : actionDirection.getAtkHeroList()) {
                actionDirection.setCurAtkHeroId(atkHeroId);
                int actionAtkId = FightPbUtil.getActingSize(actionDirection.getAtk(), atkHeroId);
                for (Integer heroId : actionDirection.getDefHeroList()) {
                    actionDirection.setCurDefHeroId(heroId);
                    LogUtil.fight("执行技能伤害效果, 伤害攻击方: ", actionDirection.getAtk() == null ? 0 : actionDirection.getAtk().ownerId,
                            ", 攻击武将: ", atkHeroId, ", 伤害被攻击方: ", actionDirection.getDef() == null ? 0 : actionDirection.getDef().ownerId,
                            ", 被攻击武将: ", heroId);
                    battleLogic.skillAttack(actionDirection, contextHolder, effectConfig_, contextHolder.getBattleType());

                    if (timing != FightConstant.BuffEffectTiming.ACTIVE_RELEASE) {
                        SimpleHeroSkill simpleHeroSkill = (SimpleHeroSkill) fightBuff.getSkill();
                        // 将当前动作添加进嵌套动作效果里
                        MultiEffectActionPb curMultiAction = contextHolder.getMultiEffectActionList().peekFirst();
                        curMultiAction.getCurMultiEffectActionPb().addAction(FightPbUtil.createBaseActionPb(BattlePb.SkillAction.action,
                                curMultiAction.getCurSkillPb().setSkillId(simpleHeroSkill.getS_skill().getSkillId()).build(),
                                BattlePb.ActionTypeDefine.SKILL_ATTACK_VALUE, actionAtkId));
                        curMultiAction.getCurSkillPb().clear();
                    }
                }
            }

            if (timing != FightConstant.BuffEffectTiming.ACTIVE_RELEASE) {
                FightPbUtil.handleAfterSkillDamageEffect(fightBuff, contextHolder, rule, timing);
            }
        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
    }
}
