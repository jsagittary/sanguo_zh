package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticCrossWarRank.java
 * @Description 跨服排行
 * @author QiuKun
 * @date 2019年5月29日
 */
public class StaticCrossWarRank {
    /** 阵营排行 */
    public static final int RANK_TYPE_CAMP = 1;
    /** 个人排行 */
    public static final int RANK_TYPE_PERSONAL = 2;

    private int id;
    private int type;// 排行榜类型 1.阵营排行, 2 个人排行
    private int ranking;// 排名
    private List<List<Integer>> award;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    @Override
    public String toString() {
        return "StaticCrossWarRank [id=" + id + ", type=" + type + ", ranking=" + ranking + ", award=" + award + "]";
    }

    
}
