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

import java.util.*;

import static com.gryphpoem.game.zw.constant.FightConstant.EffectLogicId.COUNTERATTACK;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-04 19:14
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class OrdinaryAttackEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.DOUBLE_HIT, COUNTERATTACK};
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
    protected FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe, Object... params) {
        return new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId(), (Integer) params[0]);
    }

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, int timing, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection) || CheckNull.isEmpty(actionDirection.getAtkHeroList()) || CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            return;
        }

        FightPbUtil.initNextMultiEffectAction(contextHolder, false);
        BattleLogic battleLogic = DataResource.ac.getBean(BattleLogic.class);
        MultiEffectActionPb curMultiEffectActionPb = contextHolder.getMultiEffectActionList().peekFirst();
        if (rule.getEffectLogicId() == COUNTERATTACK) {
            curMultiEffectActionPb.setCounterattack(false);
        }

        for (Integer atkHeroId : actionDirection.getAtkHeroList()) {
            actionDirection.setCurAtkHeroId(atkHeroId);
            int actionAtkId = FightPbUtil.getActingSize(actionDirection.getAtk(), atkHeroId);
            for (Integer heroId : actionDirection.getDefHeroList()) {
                actionDirection.setCurDefHeroId(heroId);
                switch (rule.getEffectLogicId()) {
                    case COUNTERATTACK:
                        LogUtil.fight("执行反击效果, 攻击方: ", actionDirection.getAtk().ownerId,
                                ", 武将: ", atkHeroId, ", 被攻击方: ", actionDirection.getDef().ownerId, ", 被攻击武将: ", heroId);
                        battleLogic.buffCounterAttack(actionDirection, contextHolder, effectConfig_);
                        break;
                    case FightConstant.EffectLogicId.DOUBLE_HIT:
                        LogUtil.fight("执行连击效果, 攻击方: ", actionDirection.getAtk().ownerId,
                                ", 武将: ", atkHeroId, ", 被攻击方: ", actionDirection.getDef().ownerId, ", 被攻击武将: ", heroId);
                        battleLogic.buffOrdinaryAttack(actionDirection, contextHolder, contextHolder.getBattleType());

                        // 记录释放过的连击效果
                        FightBuffEffect fbe = actionDirection.getAtk().getFightEffectMap(atkHeroId);
                        FightEffectData data = createFightEffectData(fightBuff, effectConfig_, fbe, contextHolder.getRoundNum());
                        fbe.getEffectMap().computeIfAbsent(rule.getEffectLogicId(), m -> new HashMap<>()).
                                computeIfAbsent(effectConfig_.get(2), l -> new ArrayList<>()).add(data);
                        break;
                }

                // 将当前动作添加进嵌套效果里
                curMultiEffectActionPb.getCurMultiEffectActionPb().addAction(FightPbUtil.createBaseActionPb(BattlePb.OrdinaryAttackAction.action,
                        curMultiEffectActionPb.getCurAttackPb().build(), BattlePb.ActionTypeDefine.ORDINARY_ATTACK_VALUE, actionAtkId));
                curMultiEffectActionPb.getCurAttackPb().clear();
            }
        }

        // 创建baseEffectAction pb
        SimpleHeroSkill simpleHeroSkill = (SimpleHeroSkill) fightBuff.getSkill();
        BattlePb.BaseEffectAction.Builder basePb = FightPbUtil.createBaseEffectActionPb(BattlePb.MultiEffectAction.effect,
                curMultiEffectActionPb.getCurMultiEffectActionPb().build(), rule.getEffectLogicId(),
                FightPbUtil.getActingSize(fightBuff.getBuffGiver(), fightBuff.getBuffGiverId()),
                FightPbUtil.getActingSize(fightBuff.getForce(), fightBuff.getForceId()), timing, FightConstant.EffectStatus.APPEAR,
                simpleHeroSkill.isOnStageSkill(), simpleHeroSkill.getS_skill().getSkillId());
        boolean padding = false;
        if (curMultiEffectActionPb.getLastSkillPb() != null) {
            curMultiEffectActionPb.getLastSkillPb().addEffectAction(basePb.build());
            padding = true;
        }
        if (curMultiEffectActionPb.getLastAttackPb() != null) {
            curMultiEffectActionPb.getLastAttackPb().addEffectAction(basePb.build());
            padding = true;
        }
        if (!padding) {
            if (curMultiEffectActionPb.getRoundActionPb() != null) {
                curMultiEffectActionPb.getRoundActionPb().addEffectAction(basePb.build());
            } else {
                LogUtil.error("嵌套普攻伤害有问题, rule: ", rule.getEffectId(), ", curList: ", contextHolder.getMultiEffectActionList());
            }
        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
        switch (rule.getEffectLogicId()) {
            case FightConstant.EffectLogicId.DOUBLE_HIT:
                Force force = fightBuff.getForce();
                FightBuffEffect fightBuffEffect = force.getFightEffectMap(fightBuff.getForceId());
                if (Objects.nonNull(fightBuffEffect)) {
                    Map<Integer, List<FightEffectData>> fightEffectDataMap = fightBuffEffect.getEffectMap().get(rule.getEffectLogicId());
                    if (!CheckNull.isEmpty(fightEffectDataMap) && !CheckNull.isEmpty(fightEffectDataMap.get(rule.getEffectId()))) {
                        fightEffectDataMap.get(rule.getEffectId()).clear();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean canEffect(FightContextHolder contextHolder, StaticEffectRule staticEffectRule, Object... params) {
        switch (staticEffectRule.getEffectId()) {
            case FightConstant.EffectLogicId.DOUBLE_HIT:
                if (Objects.nonNull(contextHolder.getCurAttackActionPb())) {
                    FightBuffEffect effect = contextHolder.getActionDirection().getAtk().
                            getFightEffectMap(contextHolder.getActionDirection().getCurAtkHeroId());
                    if (Objects.nonNull(effect)) {
                        Map<Integer, List<FightEffectData>> fightEffectDataMap = effect.getEffectMap().get(staticEffectRule.getEffectLogicId());
                        if (!CheckNull.isEmpty(fightEffectDataMap)) {
                            List<FightEffectData> fightEffectDataList = fightEffectDataMap.get(staticEffectRule.getEffectId());
                            if (!CheckNull.isEmpty(fightEffectDataList)) {
                                // 若当前回合释放过连击, 则不再释放
                                return Objects.isNull(fightEffectDataList.stream().filter(fightEffectData ->
                                        fightEffectData.getValue() == contextHolder.getRoundNum()).findFirst().orElse(null));
                            }
                        }
                    }
                }
                return true;
            case COUNTERATTACK:
                return FightPbUtil.curActionCounterattack(contextHolder);
            default:
                return true;
        }
    }
}
