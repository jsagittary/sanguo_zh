package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 战火燎原刷新规则
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2021-01-27 16:05
 */
public class StaticWarFireRange {

    /**
     * 刷新类型1, 战火燎原资源点
     */
    public static final int RANGE_TYPE_1 = 1;

    /**
     * 主键
     */
    private int id;
    /**
     * 类型
     */
    private int type;
    /**
     * 刷新区块
     */
    private List<Integer> block;
    /**
     * 产出上限
     */
    private int resource;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Integer> getBlock() {
        return block;
    }

    public void setBlock(List<Integer> block) {
        this.block = block;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }
}