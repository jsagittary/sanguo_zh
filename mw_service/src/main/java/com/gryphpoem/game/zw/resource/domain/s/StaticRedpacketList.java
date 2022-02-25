package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticRedpacketList.java
 * @Description 红包物品
 * @author QiuKun
 * @date 2018年6月8日
 */
public class StaticRedpacketList {
    private int id;
    private List<Integer> content;// 奖励的物品
    private int value;// 价值

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getContent() {
        return content;
    }

    public void setContent(List<Integer> content) {
        this.content = content;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
