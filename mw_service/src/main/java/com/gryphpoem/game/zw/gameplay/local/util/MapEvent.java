package com.gryphpoem.game.zw.gameplay.local.util;

/**
 * @ClassName MapEvent.java
 * @Description
 * @author QiuKun
 * @date 2019年3月23日
 */
public class MapEvent {

    private int pos = -1;

    private long armyRoleId;
    private int armyKey;

    private MapEventType eventType;

    private MapCurdEvent curdEvent;

    private MapEvent(MapEventType eventType, MapCurdEvent curdEvent) {
        this.eventType = eventType;
        this.curdEvent = curdEvent;
    }

    public int getPos() {
        return pos;
    }

    public MapEventType getEventType() {
        return eventType;
    }

    public MapCurdEvent getCurdEvent() {
        return curdEvent;
    }

    public long getArmyRoleId() {
        return armyRoleId;
    }

    public int getArmyKey() {
        return armyKey;
    }

    /**
     * 是否是全地图通知
     * 
     * @return
     */
    public boolean isAllMapNotice() {
        return MapEventType.MAP_LINE == eventType || MapEventType.MAP_AREA == eventType
                || MapEventType.MAP_RELOAD == eventType;
    }

    /**
     * 行军线的改变
     * 
     * @param armyRoleId
     * @param armyKey
     * @param curdEvent
     * @return
     */
    public static MapEvent mapLine(long armyRoleId, int armyKey, MapCurdEvent curdEvent) {
        MapEvent mapEvent = new MapEvent(MapEventType.MAP_LINE, curdEvent);
        mapEvent.armyRoleId = armyRoleId;
        mapEvent.armyKey = armyKey;
        return mapEvent;
    }

    /**
     * 区域
     * 
     * @param pos
     * @param curdEvent
     * @return
     */
    public static MapEvent mapArea(int pos, MapCurdEvent curdEvent) {
        MapEvent mapEvent = new MapEvent(MapEventType.MAP_AREA, curdEvent);
        mapEvent.pos = pos;
        return mapEvent;
    }

    /**
     * 地图点
     * 
     * @param pos
     * @param curdEvent
     * @return
     */
    public static MapEvent mapEntity(int pos, MapCurdEvent curdEvent) {
        MapEvent mapEvent = new MapEvent(MapEventType.MAP_ENTITY, curdEvent);
        mapEvent.pos = pos;
        return mapEvent;
    }

    /**
     * 重新加载地图事件
     * 
     * @return
     */
    public static MapEvent mapReload() {
        return new MapEvent(MapEventType.MAP_RELOAD, MapCurdEvent.NONE);
    }

}
