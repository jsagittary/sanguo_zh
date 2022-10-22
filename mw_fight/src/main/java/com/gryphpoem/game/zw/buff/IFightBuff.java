package com.gryphpoem.game.zw.buff;

import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;

import java.util.List;

/**
 * Description: buff接口
 * Author: zhangpeng
 * createTime: 2022-10-21 10:09
 */
public interface IFightBuff<T extends StaticBuff> extends IUniqueId {
    /**
     * 获取buff配置
     *
     * @return
     */
    T getBuffConfig();

    /**
     * 扣除buff次数
     */
    void deductBuffTimes();

    /**
     * 校验buff共存
     *
     * @param targetBuff
     * @param removeBuff
     * @return
     */
    boolean buffCoexistenceCheck(StaticBuff targetBuff, List<IFightBuff> removeBuff);

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
