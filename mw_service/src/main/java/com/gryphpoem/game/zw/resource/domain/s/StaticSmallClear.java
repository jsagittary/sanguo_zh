package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @program: empire_en
 * @description:
 * @author: zhou jie
 * @create: 2020-08-17 15:01
 */
public class StaticSmallClear {

    /**
     * 主键
     */
    private int id;
    /**
     * 等级区间
     */
    private List<Integer> level;
    /**
     * 最近登录天数
     */
    private int log;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getLevel() {
        return level;
    }

    public void setLevel(List<Integer> level) {
        this.level = level;
    }

    public int getLog() {
        return log;
    }

    public void setLog(int log) {
        this.log = log;
    }
}