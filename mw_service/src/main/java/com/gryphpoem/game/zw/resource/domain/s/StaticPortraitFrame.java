package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 头像框
 *
 * @description:
 * @author: zhou jie
 * @time: 2021/3/9 10:28
 */
public class StaticPortraitFrame {
    /**
     * 头像框id
     */
    private int id;
    /**
     * 道具转换
     */
    private List<List<Integer>> consume;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }
}
