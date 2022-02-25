package com.gryphpoem.game.zw.core.rank;

import java.io.Serializable;

/**
 * @Description
 * @Author zhangdh
 * @Date 2020-12-21 9:37
 */
public class RankItem<T extends Comparable<T>> implements Serializable {
    private long lordId;//角色ID
    private long lastModifyTime;
    private T rankValue;

    //默认不设置修改时间,因为该值在对应的数据库中没设置修改时间字段
    public RankItem(long lordId, T rankValue) {
        this.lordId = lordId;
        this.rankValue = rankValue;
    }

    public RankItem(long lordId, T rankValue, long lastModifyTime){
        this(lordId, rankValue);
        this.lastModifyTime = lastModifyTime;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(long lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public T getRankValue() {
        return rankValue;
    }

    public void setRankValue(T rankValue) {
        this.rankValue = rankValue;
    }

    @Override
    public String toString() {
        return "RankItem{" +
                "lordId=" + lordId +
                ", rankValue=" + rankValue +
                '}';
    }
}
