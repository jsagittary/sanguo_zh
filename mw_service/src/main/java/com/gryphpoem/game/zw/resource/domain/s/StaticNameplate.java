package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 铭牌配置
 *
 * @description:
 * @author: zhou jie
 * @time: 2021/3/9 10:32
 */
public class StaticNameplate {

    /**
     * 配置的id
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
