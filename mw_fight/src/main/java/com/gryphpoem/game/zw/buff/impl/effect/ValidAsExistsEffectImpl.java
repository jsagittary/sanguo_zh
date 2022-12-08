package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.game.zw.util.FightPbUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Description: 无敌, 沉默效果
 * Author: zhangpeng
 * createTime: 2022-11-04 15:59
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class ValidAsExistsEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.INVINCIBLE_DAMAGE, FightConstant.EffectLogicId.SILENCE};
    }

    @Override
    protected FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe, Object... params) {
        return new FightEffectData(fightBuff.uniqueId(), fightBuff.getBuffConfig().getBuffId(), -1);
    }

    @Override
    public Object effectCalculateValue(FightBuffEffect fightBuffEffect, int effectLogicId, Object... params) {
        return !CheckNull.isEmpty(fightBuffEffect.getEffectMap().get(effectLogicId));
    }

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, int timing, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection)) {
            return;
        }
        if (!CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            if (Objects.nonNull(rule)) {
                for (Integer heroId : actionDirection.getDefHeroList()) {
                    Force def = actionDirection.getDef();
                    FightBuffEffect fbe = def.getFightEffectMap(heroId);
                    List<FightEffectData> dataList = fbe.getDataList(rule.getEffectLogicId(), rule.getEffectId());
                    if (!CheckNull.isEmpty(dataList))
                        continue;

                    FightEffectData data = createFightEffectData(fightBuff, effectConfig_, fbe);
                    fbe.getEffectMap().computeIfAbsent(rule.getEffectLogicId(), m -> new HashMap<>()).
                            computeIfAbsent(effectConfig_.get(2), l -> new ArrayList<>()).add(data);
                    addEffectPb(fightBuff, contextHolder, rule.getEffectLogicId(), timing);
                }
            }
        }
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
