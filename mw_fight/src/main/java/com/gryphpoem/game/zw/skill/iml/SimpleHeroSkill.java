package com.gryphpoem.game.zw.skill.iml;

import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.skill.abs.AbstractHeroSkill;

/**
 * Description: 通用技能实现
 * Author: zhangpeng
 * createTime: 2022-10-20 17:08
 */
public class SimpleHeroSkill extends AbstractHeroSkill {
    public SimpleHeroSkill(StaticHeroSkill s_skill) {
        super(s_skill);
    }

    @Override
    protected boolean releaseDamageFirst() {
        return false;
    }

    @Override
    public FightResult releaseDamage(Force attacker, Force defender, FightLogic fightLogic, Object... params) {
        // TODO 伤害计算
        return null;
    }

    @Override
    public FightResult releaseBuff(Force attacker, Force defender, FightLogic fightLogic, Object... params) {
        return null;
    }
}
