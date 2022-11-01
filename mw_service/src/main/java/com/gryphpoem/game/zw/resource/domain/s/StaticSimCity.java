package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 城镇事件
 *
 * @Author: GeYuanpeng
 * @Date: 2022/10/28 14:33
 */
public class StaticSimCity {

    private int type;

    private List<List<Integer>> buildLv; // 此城镇事件计入刷新池的前置建筑等级需求[[buildId,buildLv]]

    private int lordLv; // 此城镇事件计入刷新池的前置领主等级需求

    private List<Integer> open; // [1,绑定建筑id/绑定NPCid]，1后面填建筑id，2后面填NPCid

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<List<Integer>> getBuildLv() {
        return buildLv;
    }

    public void setBuildLv(List<List<Integer>> buildLv) {
        this.buildLv = buildLv;
    }

    public int getLordLv() {
        return lordLv;
    }

    public void setLordLv(int lordLv) {
        this.lordLv = lordLv;
    }

    public List<Integer> getOpen() {
        return open;
    }

    public void setOpen(List<Integer> open) {
        this.open = open;
    }
}
