package com.gryphpoem.game.zw.skill;

import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;

/**
 * Description: hero技能接口
 * Author: zhangpeng
 * createTime: 2022-10-20 16:40
 */
public interface IHeroSkill {
    /**
     * 技能伤害释放
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @return
     */
    FightResult releaseDamage(Force attacker, Force defender, FightLogic fightLogic, Object... params);

    /**
     * buff释放
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param params
     * @return
     */
    FightResult releaseBuff(Force attacker, Force defender, FightLogic fightLogic, Object... params);
}
