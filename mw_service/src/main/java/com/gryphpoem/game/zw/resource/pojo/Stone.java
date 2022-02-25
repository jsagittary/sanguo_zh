package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName Stone.java
 * @Description 宝石
 * @author QiuKun
 * @date 2018年5月7日
 */
public class Stone {
    private int stoneId; // 宝石id
    private int cnt; // 宝石的数量

    public Stone(int stoneId) {
        this.stoneId = stoneId;
    }

    public Stone(CommonPb.Stone cStone) {
        this.stoneId = cStone.getStoneId();
        this.cnt = cStone.getCnt();
    }

    public int getStoneId() {
        return stoneId;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public int addStoneCntAndGet(int count) {
        this.cnt += count;
        return cnt;
    }

    public int subStoneCntAndGet(int count) {
        this.cnt -= count;
        if (cnt <= 0) cnt = 0;
        return cnt;
    }

    @Override
    public String toString() {
        return "Stone [stoneId=" + stoneId + ", cnt=" + cnt + "]";
    }

}
