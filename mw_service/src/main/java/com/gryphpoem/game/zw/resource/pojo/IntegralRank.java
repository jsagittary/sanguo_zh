package com.gryphpoem.game.zw.resource.pojo;

/**
 * Created by pengshuo on 2019/5/10 16:29
 * <br>Description:
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class IntegralRank {
    /** 阵营 */
    private int camp;
    /** 玩家id */
    private long lordId;
    /** 积分 */
    private long value;
    /** 最后获取积分时间 */
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

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public IntegralRank() {
    }

    /**
     * 阵营积分
     * @param camp
     * @param value
     * @param second
     */
    public IntegralRank(int camp,long value,int second) {
        this.camp = camp;
        this.value = value;
        this.second = second;
    }

    /**
     * 玩家积分
     * @param camp
     * @param lordId
     * @param value
     * @param second
     */
    public IntegralRank(int camp, long lordId, long value, int second) {
        this.camp = camp;
        this.lordId = lordId;
        this.value = value;
        this.second = second;
    }

    @Override
    public String toString() {
        return "IntegralRank{" +
                "camp=" + camp +
                ", lordId=" + lordId +
                ", value=" + value +
                ", second=" + second +
                '}';
    }
}
