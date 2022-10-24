package com.gryphpoem.game.zw.skill;

import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;

/**
 * Description: hero技能接口
 * Author: zhangpeng
 * createTime: 2022-10-20 16:40
 */
public interface IHeroSkill {
    /**
     * 释放技能
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param staticHeroSkill
     * @param fightResult
     * @param params
     */
    void releaseSkill(Force attacker, Force defender, FightLogic fightLogic, StaticHeroSkill staticHeroSkill, FightResult fightResult, Object... params);

    /**
     * 释放技能主体效果
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param staticHeroSkill
     * @param fightResult
     * @param params
     */
    void releaseSkillEffect(Force attacker, Force defender, FightLogic fightLogic, StaticHeroSkill staticHeroSkill, FightResult fightResult, Object... params);

    /**
     * 释放技能buff
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param staticHeroSkill
     * @param fightResult
     * @param params
     */
    void releaseSkillBuff(Force attacker, Force defender, FightLogic fightLogic, StaticHeroSkill staticHeroSkill, FightResult fightResult, Object... params);
}
