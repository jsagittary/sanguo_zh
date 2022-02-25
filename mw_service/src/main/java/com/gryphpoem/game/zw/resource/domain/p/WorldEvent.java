package com.gryphpoem.game.zw.resource.domain.p;

/**
 * @ClassName WorldEvent.java
 * @Description 世界地图事件
 * @author QiuKun
 * @date 2018年8月21日
 */
public class WorldEvent {

    public static final int EVENT_HIT_FLY = 1;
    private int pos;// 位置
    private int eventType;// 地图事件

    private WorldEvent() {

    }

    public static WorldEvent createWorldEvent(int pos, int eventType) {
        WorldEvent e = new WorldEvent();
        e.setPos(pos);
        e.setEventType(eventType);
        return e;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "WorldEvent [pos=" + pos + ", eventType=" + eventType + "]";
    }
}
