package com.gryphpoem.game.zw.buff;

import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;

/**
 * Description: buff接口
 * Author: zhangpeng
 * createTime: 2022-10-21 10:09
 */
public interface IFightBuff {
    /**
     * 扣除buff次数
     */
    void deductBuffTimes();

    /**
     * buff是否还有生效次数
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param params
     * @return
     */
    boolean hasRemainBuffTimes(Force attacker, Force defender, FightLogic fightLogic, Object... params);

    /**
     * 释放技能, buff添加
     *
     * @param attacker   攻击者
     * @param defender   被攻击者
     * @param fightLogic
     * @param params
     * @return
     */
    FightResult releaseSkill(Force attacker, Force defender, FightLogic fightLogic, StaticHeroSkill staticHeroSkill, Object... params);

    /**
     * buff释放, 效果添加
     *
     * @param attacker   攻击者
     * @param defender   被攻击者
     * @param fightLogic
     * @param params
     * @return
     */
    FightResult releaseBuff(Force attacker, Force defender, FightLogic fightLogic, int timing, Object... params);

    /**
     * buff失效, 效果还原
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param params
     * @return
     */
    FightResult buffLoseEffectiveness(Force attacker, Force defender, FightLogic fightLogic, Object... params);
}
