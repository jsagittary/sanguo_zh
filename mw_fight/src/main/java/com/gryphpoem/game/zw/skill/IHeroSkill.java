package com.gryphpoem.game.zw.skill;

import com.gryphpoem.game.zw.buff.IUniqueId;
import com.gryphpoem.game.zw.listener.impl.SessionListener;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;

/**
 * Description: hero技能接口
 * Author: zhangpeng
 * createTime: 2022-10-20 16:40
 */
public interface IHeroSkill extends IUniqueId, SessionListener {
    /**
     * 释放技能
     *
     * @param contextHolder
     * @param params
     */
    void releaseSkill(FightContextHolder contextHolder, Object... params);

    /**
     * 释放技能主体效果
     *
     * @param contextHolder
     * @param params
     */
    void releaseSkillEffect(FightContextHolder contextHolder, Object... params);

    /**
     * 释放技能buff
     *
     * @param contextHolder
     * @param params
     */
    void releaseSkillBuff(FightContextHolder contextHolder, Object... params);

    /**
     * 是否是登场技能
     *
     * @return
     */
    boolean isOnStageSkill();

    /**
     * 获取技能伤害
     *
     * @return
     */
    int getSkillDamage();

    /**
     * 添加技能伤害
     *
     * @param damage
     */
    void addSkillDamage(int damage);

    /**
     * 获取技能拥有者
     *
     * @return
     */
    Force getSkillOwner();

    /**
     * 获取技能释放者武将id
     *
     * @return
     */
    int getSkillHeroId();
}
