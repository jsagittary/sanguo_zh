package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Description: 加buff
 * Author: zhangpeng
 * createTime: 2022-11-04 18:05
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.EFFECT)
public class AddBuffEffectImpl extends AbsFightEffect {
    @Override
    public int[] effectType() {
        return new int[]{FightConstant.EffectLogicId.ADD_BUFF_TO_THE_EFFECT};
    }

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, int timing, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection) || CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            return;
        }

        StaticBuff staticBuff = StaticFightManager.getStaticBuff(effectConfig_.get(3));
        if (CheckNull.isNull(staticBuff)) {
            LogUtil.error("add buff, config: ", CheckNull.isEmpty(effectConfig_) ? "" : Arrays.toString(effectConfig_.toArray()), ", staticBuff not found");
            return;
        }

        // 判断加buff是否随着技能等级成长
        int prob = effectConfig_.get(4);
        if (Objects.nonNull(fightBuff) && Objects.nonNull(fightBuff.getBuffConfig()) &&
                Objects.nonNull(fightBuff.getSkill()) && fightBuff.getBuffConfig().getEffectWhetherGrow() == 1) {
            if (fightBuff.getSkill() instanceof SimpleHeroSkill) {
                SimpleHeroSkill skill = (SimpleHeroSkill) fightBuff.getSkill();
                prob = (int) Math.ceil(prob * (1 + ((skill.getS_skill().getLevel() - 1) / 9d)));
            }
        }
        if (!RandomHelper.isHitRangeIn10000(prob)) {
            LogUtil.debug("添加buff随机值不够, 无法添加buff, buffConfig: ", fightBuff.getBuffConfig());
            return;
        }

        // buff的施与者是当前buff的施与者
        Force executor = actionDirection.getDef();
        BattleLogic battleLogic = DataResource.ac.getBean(BattleLogic.class);
        List<IFightBuff> removedList = new ArrayList<>();
        for (Integer heroId : actionDirection.getDefHeroList()) {
            // 释放buff
            LogUtil.fight("释放添加buff效果, 效果逻辑id: ", rule.getEffectLogicId(), "， 被加buff方: ", actionDirection.getDef().ownerId, ", 被加buff武将: ", heroId);
            battleLogic.releaseBuff(executor.buffList(heroId), staticBuff, removedList, actionDirection, contextHolder, null, fightBuff.getSkill(), fightBuff.getBuffGiver());
        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {

    }
}
