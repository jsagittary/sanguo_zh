package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.game.zw.util.FightPbUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.Iterator;
import java.util.List;

/**
 * Description: 净化
 * Author: zhangpeng
 * createTime: 2022-11-04 17:02
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class PurifyEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.PURIFY, FightConstant.EffectLogicId.DISPEL};
    }

    @Override
    public IFightBuff compareTo(List sameIdBuffList, List effectConfig, FightBuffEffect fightBuffEffect, FightContextHolder contextHolder) {
        return (IFightBuff) sameIdBuffList.get(0);
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
        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection) || CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            return;
        }

        boolean release = false;
        Force executor = actionDirection.getDef();
        for (Integer heroId : actionDirection.getDefHeroList()) {
            List<IFightBuff> buffList = executor.buffList(heroId.intValue());
            if (CheckNull.isEmpty(buffList))
                continue;
            Iterator<IFightBuff> it = buffList.iterator();
            while (it.hasNext()) {
                IFightBuff buff = it.next();
                StaticBuff staticBuff;
                if (CheckNull.isNull(buff) || CheckNull.isNull(staticBuff = buff.getBuffConfig()))
                    continue;
                switch (rule.getEffectLogicId()) {
                    case FightConstant.EffectLogicId.PURIFY:
                        if (staticBuff.getTypeGrouping().contains(2) || staticBuff.getTypeGrouping().contains(3)) {
                            fightBuff.buffLoseEffectiveness(contextHolder);
                            it.remove();
                            LogUtil.fight("执行净化效果, 净化被执行方: ", actionDirection.getDef().ownerId,
                                    ", 武将: ", heroId, ", 被消除的buff: ", buff.getBuffConfig());
                            release = true;
                        }
                        break;
                    case FightConstant.EffectLogicId.DISPEL:
                        if (staticBuff.getTypeGrouping().contains(1)) {
                            fightBuff.buffLoseEffectiveness(contextHolder);
                            it.remove();
                            LogUtil.fight("执行驱散效果, 驱散被执行方: ", actionDirection.getDef().ownerId,
                                    ", 武将: ", heroId, ", 被驱散的buff: ", buff.getBuffConfig());
                            release = true;
                        }
                        break;
                }
            }
        }

        if (release) {
            addEffectPb(fightBuff, contextHolder, rule.getEffectLogicId(), timing);
        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
    }

    /**
     * 添加效果pb
     *
     * @param fightBuff
     * @param contextHolder
     * @param effectLogicId
     * @param timing
     */
    private void addEffectPb(IFightBuff fightBuff, FightContextHolder contextHolder, int effectLogicId, int timing) {
        BattlePb.CommonEffectAction.Builder builder = BattlePb.CommonEffectAction.newBuilder();
        SimpleHeroSkill simpleHeroSkill = (SimpleHeroSkill) fightBuff.getSkill();
        BattlePb.BaseEffectAction.Builder basePb = FightPbUtil.createBaseEffectActionPb(BattlePb.CommonEffectAction.effect, builder.build(), effectLogicId,
                FightPbUtil.getActingSize(fightBuff.getBuffGiver(), fightBuff.getBuffGiverId()), FightPbUtil.getActingSize(fightBuff.getForce(), fightBuff.
                        getForceId()), timing, FightConstant.EffectStatus.APPEAR, simpleHeroSkill.isOnStageSkill(), simpleHeroSkill.getS_skill().getSkillId());
        FightPbUtil.addEffectActionList(contextHolder, basePb);
    }
}
