package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2019-03-21 15:37
 * @description:
 * @modified By:
 */
public class StaticJewel {
    /**
     * 宝石等级
     */
    private int level;
    /**
     * 合成道具列表
     */
    private List<Integer> synthesis;
    /**
     * 万份比加成数值
     */
    private int value;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<Integer> getSynthesis() {
        return synthesis;
    }

    public void setSynthesis(List<Integer> synthesis) {
        this.synthesis = synthesis;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
