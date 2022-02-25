package com.gryphpoem.game.zw.gameplay.local.util;

/**
 * @ClassName MapCurdEvent.java
 * @Description 节点的新增删除
 * @author QiuKun
 * @date 2019年3月23日
 */
public enum MapCurdEvent {

    NONE(0), CREATE(1), UPDATE(2), DELETE(3);

    private int value;

    private MapCurdEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
