package com.gryphpoem.game.zw.resource.pojo;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-17 14:48
 * @description: 战机碎片
 * @modified By:
 */
public class PlaneChip {

    private int chipId;     // 碎片Id
    private int cnt;        // 宝石的数量

    public int getChipId() {
        return chipId;
    }

    public void setChipId(int chipId) {
        this.chipId = chipId;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public int addChipCnt(int count) {
        this.cnt += count;
        return cnt;
    }

    public int subChipCnt(int count) {
        this.cnt -= count;
        if (cnt <= 0) cnt = 0;
        return cnt;
    }
}
