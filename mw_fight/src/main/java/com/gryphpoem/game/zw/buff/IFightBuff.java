package com.gryphpoem.game.zw.buff;

import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;

import java.util.LinkedList;
import java.util.List;

/**
 * Description: buff接口
 * Author: zhangpeng
 * createTime: 2022-10-21 10:09
 */
public interface IFightBuff<T extends StaticBuff, S extends Force> extends IUniqueId {
    /**
     * 获取buff配置
     *
     * @return
     */
    T getBuffConfig();

    /**
     * 获取buff剩余作用回合数
     *
     * @return
     */
    int getBuffEffectiveRounds();

    /**
     * 设置被作用方
     *
     * @param force
     */
    void setForce(S force);

    /**
     * 扣除buff次数
     */
    void deductBuffRounds();

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
     * @param actingBuffList 被作用方的buff列表
     * @param fightLogic
     * @param params
     * @return
     */
    void releaseBuff(LinkedList<IFightBuff> actingBuffList, FightLogic fightLogic, List<Integer> staticBuffConfig, FightResult fightResult, Object... params);

    /**
     * buff的效果添加
     *
     * @param actingForce 攻击者
     * @param fightLogic
     * @param params
     * @return
     */
    void releaseEffect(Force actingForce, FightLogic fightLogic, FightResult fightResult, int timing, Object... params);

    /**
     * buff失效, 效果还原
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param params
     * @return
     */
    void buffLoseEffectiveness(Force attacker, Force defender, FightLogic fightLogic, FightResult fightResult, Object... params);
}
