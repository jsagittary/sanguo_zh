package com.gryphpoem.game.zw.gameplay.local.util;

/**
 * @ClassName MapEventType.java
 * @Description
 * @author QiuKun
 * @date 2019年3月23日
 */
public enum MapEventType {

    // 地图上的点,关注了的人进行推送
    MAP_ENTITY(1),
    // 地图上的行军线,全地的人图推送
    MAP_LINE(2),
    // 区域,全地的人图推送
    MAP_AREA(3),
    // 重新加载清除缓存
    MAP_RELOAD(4),
    ;

    private int value;

    private MapEventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    
}
