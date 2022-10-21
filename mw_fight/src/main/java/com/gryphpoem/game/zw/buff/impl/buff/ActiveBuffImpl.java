package com.gryphpoem.game.zw.buff.impl.buff;

import com.gryphpoem.game.zw.buff.abs.buff.AbsActiveBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;

/**
 * Description: 主动生效buff
 * Author: zhangpeng
 * createTime: 2022-10-21 10:43
 */
@BuffEffectType(buffEffect = FightConstant.BuffEffect.BUFF, type = FightConstant.BuffEffectiveType.ACTIVE)
public class ActiveBuffImpl extends AbsActiveBuff {
    @Override
    public boolean hasRemainBuffTimes(Force attacker, Force defender, FightLogic fightLogic, Object... params) {
        return this.buffActionTimes > 0;
    }

    @Override
    public FightResult releaseSkill(Force attacker, Force defender, FightLogic fightLogic, StaticHeroSkill staticHeroSkill, Object... params) {
        if (CheckNull.isNull(staticHeroSkill)) {
            // 技能配置为控
            LogUtil.error("skill config is null, activeBuffImpl");
            return null;
        }

        if (!CheckNull.isEmpty(staticHeroSkill.getSkillEffect())) {
            // 释放技能效果

        }

        if (!CheckNull.isEmpty(staticHeroSkill.getBuff())) {
            // 释放buff
            for (List<Integer> buffConfig : staticHeroSkill.getBuff()) {
                
            }
        }
        return null;
    }

    @Override
    public FightResult releaseBuff(Force attacker, Force defender, FightLogic fightLogic, int timing, Object... params) {
        return null;
    }

    @Override
    public FightResult buffLoseEffectiveness(Force attacker, Force defender, FightLogic fightLogic, Object... params) {
        return null;
    }
}
