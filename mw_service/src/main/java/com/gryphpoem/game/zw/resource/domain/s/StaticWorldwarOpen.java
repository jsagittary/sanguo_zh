package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticWorldwarOpen.java
 * @Description 地图开放规则
 * @author QiuKun
 * @date 2019年3月26日
 */
public class StaticWorldwarOpen {

    private int id;
    private int open; // 赛季开始后多少天后开启

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

}
