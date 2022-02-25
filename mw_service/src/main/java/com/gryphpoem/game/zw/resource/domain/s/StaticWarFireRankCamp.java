package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 阵营排名奖励
 * @Description
 * @Author zhangdh
 * @Date 2021-01-05 15:28
 */
public class StaticWarFireRankCamp {
    //唯一ID (阵营排名)
    private int rankId;
    //铸铁币奖励
    private List<Integer> awardList;

    public int getRankId() {
        return rankId;
    }

    public void setRankId(int rankId) {
        this.rankId = rankId;
    }

    public List<Integer> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<Integer> awardList) {
        this.awardList = awardList;
    }
}
