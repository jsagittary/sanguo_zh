package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 战火燎原个人积分排名奖励
 * @Description
 * @Author zhangdh
 * @Date 2021-01-05 15:22
 */
public class StaticWarFireRankGr {
    //唯一ID
    private int id;
    //个人积分段
    private int gr;
    //奖励的铸铁币
    private List<Integer> awardList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGr() {
        return gr;
    }

    public void setGr(int gr) {
        this.gr = gr;
    }

    public List<Integer> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<Integer> awardList) {
        this.awardList = awardList;
    }
}
