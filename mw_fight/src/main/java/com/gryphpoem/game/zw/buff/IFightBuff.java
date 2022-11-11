package com.gryphpoem.game.zw.buff;

import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuff;

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
     * 获取buff拥有者武将id
     *
     * @return
     */
    int getForceId();

    /**
     * 设置buff拥有者武将id
     *
     * @param heroId
     */
    void setForceId(int heroId);

    /**
     * 获取buff释放者武将id
     *
     * @return
     */
    int getBuffGiverId();

    /**
     * 设置技能释放者武将id
     *
     * @param heroId
     */
    void setBuffGiverId(int heroId);

    /**
     * 设置被作用方
     *
     * @param force
     */
    void setForce(S force);

    /**
     * 获取buff作用方
     *
     * @return
     */
    Force getForce();

    /**
     * 设置buff施与方
     *
     * @param force
     */
    void setBuffGiver(Force force);

    /**
     * 获取buff施与方
     *
     * @return
     */
    Force getBuffGiver();

    /**
     * buff关联的技能配置id
     *
     * @return
     */
    int getSkillConfigId();

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
     * @param contextHolder
     * @param params
     * @return
     */
    boolean hasRemainBuffRoundTimes(FightContextHolder contextHolder, Object... params);

    /**
     * 剩余生效次数
     *
     * @param contextHolder
     * @param params
     * @return
     */
    boolean hasRemainEffectiveTimes(FightContextHolder contextHolder, Object... params);

    /**
     * 释放技能, buff添加
     *
     * @param actingBuffList 被作用方的buff列表
     * @param contextHolder
     * @param params
     * @return
     */
    void releaseBuff(LinkedList<IFightBuff> actingBuffList, FightContextHolder contextHolder, List<Integer> staticBuffConfig, Object... params);

    /**
     * buff的效果添加
     *
     * @param contextHolder
     * @param params
     * @return
     */
    void releaseEffect(FightContextHolder contextHolder, int timing, Object... params);

    /**
     * buff失效, 效果还原
     *
     * @param contextHolder
     * @param params
     * @return
     */
    void buffLoseEffectiveness(FightContextHolder contextHolder, Object... params);
}
