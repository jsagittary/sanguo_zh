package com.gryphpoem.game.zw.buff.impl.buff;

import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.buff.abs.buff.AbsActiveBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
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
    public void releaseEffect(Force actingForce, FightLogic fightLogic, FightResult fightResult, int timing, Object... params) {
        if (timing != FightConstant.BuffEffectTiming.ROUND_START)
            return;

        if (CheckNull.isNull(staticBuff) || CheckNull.isEmpty(staticBuff.getEffects())) return;
        StaticFightManager staticFightManager = DataResource.ac.getBean(StaticFightManager.class);
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        for (List<Integer> effectList : staticBuff.getEffects()) {
            if (CheckNull.isEmpty(effectList)) continue;
            IFightEffect fightEffect = fightManager.getSkillEffect(effectList.get(0));
            if (CheckNull.isNull(fightEffect))
                continue;
            
        }

    }

    @Override
    public void buffLoseEffectiveness(Force attacker, Force defender, FightLogic fightLogic, FightResult fightResult, Object... params) {
    }
}
