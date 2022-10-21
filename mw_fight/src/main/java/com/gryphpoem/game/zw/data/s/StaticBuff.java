package com.gryphpoem.game.zw.data.s;

import lombok.Data;

import java.util.List;

/**
 * Description: buff配置
 * Author: zhangpeng
 * createTime: 2022-10-20 17:29
 */
@Data
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
     * 持续回合
     */
    private int continuousRound;
    /**
     * buff触发条件
     */
    private List<List<Integer>> buffTriggerCondition;
    /**
     * 效果生效次数限制
     */
    private List<List<Integer>> effectEffectiveTimes;
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
}
