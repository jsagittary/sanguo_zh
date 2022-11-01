package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 性格
 *
 * @Author: GeYuanpeng
 * @Date: 2022/10/27 10:30
 */
public class StaticCharacter {

    private int id;

    private List<Integer> range;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getRange() {
        return range;
    }

    public void setRange(List<Integer> range) {
        this.range = range;
    }
}
