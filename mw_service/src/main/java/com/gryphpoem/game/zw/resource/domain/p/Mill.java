package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

public class Mill {
    private int type;
    private int pos; // 对应的 buildingId
    private int lv;
    private int resCnt;// 能领取资源的次数(每小时加一次)
    private int resTime;// 上次领取时间
    private boolean unlock; // 是否已解锁 true为解锁

    public int getPos() {
        return pos;
    }

    public int getResCnt() {
        return resCnt;
    }

    public void setResCnt(int resCnt) {
        this.resCnt = resCnt;
    }

    public int getResTime() {
        return resTime;
    }

    public void setResTime(int resTime) {
        this.resTime = resTime;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public boolean isUnlock() {
        return unlock;
    }

    public void setUnlock(boolean unlock) {
        this.unlock = unlock;
    }

    public Mill(int pos, int type, int lv, int resCnt) {
        this.pos = pos;
        this.type = type;
        this.lv = lv;
        this.resCnt = resCnt;
    }

    public Mill(CommonPb.Mill mill) {
        this.pos = mill.getId();
        this.type = mill.getType();
        this.lv = mill.getLv();
        this.resCnt = mill.getGainCnt();
        this.resTime = mill.getResTime();
        this.unlock = mill.getUnlock();
    }

}
