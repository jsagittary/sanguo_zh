package com.gryphpoem.game.zw.skill;

import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;

/**
 * Description: hero技能接口
 * Author: zhangpeng
 * createTime: 2022-10-20 16:40
 */
public interface IHeroSkill {
    /**
     * 释放技能
     *
     * @param contextHolder
     * @param staticHeroSkill
     * @param params
     */
    void releaseSkill(FightContextHolder contextHolder, StaticHeroSkill staticHeroSkill, Object... params);

    /**
     * 释放技能主体效果
     *
     * @param contextHolder
     * @param staticHeroSkill
     * @param params
     */
    void releaseSkillEffect(FightContextHolder contextHolder, StaticHeroSkill staticHeroSkill, Object... params);

    /**
     * 释放技能buff
     *
     * @param contextHolder
     * @param staticHeroSkill
     * @param params
     */
    void releaseSkillBuff(FightContextHolder contextHolder, StaticHeroSkill staticHeroSkill, Object... params);
}
