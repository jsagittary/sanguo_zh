package com.gryphpoem.game.zw.resource.pojo;

public class ActRank {
    private int rank;
    private long lordId;// 玩家ID
    private int rankType;// 组别
    private long rankValue;// 值
    private String param;// 参数
    private int rankTime;// 上榜时间，秒数

    public ActRank(long lordId, int rankType, long rankValue, int rankTime) {
        this.lordId = lordId;
        this.rankType = rankType;
        this.rankValue = rankValue;
        this.rankTime = rankTime;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getRankType() {
        return rankType;
    }

    public void setRankType(int rankType) {
        this.rankType = rankType;
    }

    public long getRankValue() {
        return rankValue;
    }

    public void setRankValue(long rankValue) {
        this.rankValue = rankValue;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public int getRankTime() {
        return rankTime;
    }

    public void setRankTime(int rankTime) {
        this.rankTime = rankTime;
    }

    @Override
    public String toString() {
        return "ActRank [rank=" + rank + ", lordId=" + lordId + ", rankType=" + rankType + ", rankValue=" + rankValue
                + ", param=" + param + ", rankTime=" + rankTime + "]";
    }

}
