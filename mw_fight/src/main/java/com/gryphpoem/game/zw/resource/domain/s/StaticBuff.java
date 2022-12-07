package com.gryphpoem.game.zw.resource.domain.s;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Description: buff配置
 * Author: zhangpeng
 * createTime: 2022-10-20 17:29
 */
@Data
@ToString
public class StaticBuff {
    /**
     * 唯一id
     */
    private int id;
    /**
     * buffId
     */
    private int buffId;
    /**
     * effect效果列表
     */
    private List<List<Integer>> effects;
    /**
     * buff生效方式
     */
    private int buffEffectiveWay;
    /**
     * 持续回合
     */
    private int continuousRound;
    /**
     * buff触发条件
     */
    private List<List<Integer>> buffTriggerCondition;
    /**
     * buff生效次数限制
     */
    private int buffEffectiveTimes;
    /**
     * 类型分组
     */
    private List<Integer> typeGrouping;
    /**
     * buff不共存组
     */
    private List<Integer> notCoExistGroup;
    /**
     * 优先级
     */
    private int priority;
    /**
     * 同buffId共存数量
     */
    private int coexistingIdNum;
    /**
     * 同ID顶替规则
     */
    private int sameIdReplacementRule;
    /**
     * buff单回合生效次数
     */
    private int effectiveTimesSingleRound;
    /**
     * buff触发的概率
     */
    private int triggerProb;
    /**
     * buff是否跟随技能等级成长  <buff效果是否随等级成长, buff触发概率挂载概率>
     */
    private List<Integer> whetherGrow;
}
