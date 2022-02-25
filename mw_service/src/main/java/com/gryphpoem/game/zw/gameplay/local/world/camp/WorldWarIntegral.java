package com.gryphpoem.game.zw.gameplay.local.world.camp;

/**
 * Created by pengshuo on 2019/3/23 15:21
 * <br>Description: 世界阵营 积分（可使用为 阵营积分排行、城市征战积分、个人积分）
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class WorldWarIntegral {
    /** 阵营 */
    private int camp;
    /** 玩家id */
    private long lordId;
    /** 积分 */
    private int value;
    /** 时间 */
    private int second;

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public WorldWarIntegral() {
    }

    /**
     * 阵营积分
     * @param camp
     * @param value
     */
    public WorldWarIntegral(int camp, int value) {
        this.camp = camp;
        this.value = value;
    }

    /**
     * 阵营玩家积分
     * @param camp
     * @param lordId
     * @param value
     */
    public WorldWarIntegral(int camp, long lordId, int value) {
        this.camp = camp;
        this.lordId = lordId;
        this.value = value;
    }

    public WorldWarIntegral(int camp, long lordId, int value, int second) {
        this.camp = camp;
        this.lordId = lordId;
        this.value = value;
        this.second = second;
    }

    @Override
    public String toString() {
        return "WorldWarIntegral{" +
                "camp=" + camp +
                ", lordId=" + lordId +
                ", value=" + value +
                ", second=" + second +
                '}';
    }
}
