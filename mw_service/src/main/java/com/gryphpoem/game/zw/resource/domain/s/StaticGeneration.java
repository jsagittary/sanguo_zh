package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 服务器等级限制配置
 * @author xwind
 * @date 2022/2/7
 */
public class StaticGeneration {
    private int id;
    private List<Integer> duration;
    private int generation;
    private int maxLordLv;
    private int bottomLordLv;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getDuration() {
        return duration;
    }

    public void setDuration(List<Integer> duration) {
        this.duration = duration;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public int getMaxLordLv() {
        return maxLordLv;
    }

    public void setMaxLordLv(int maxLordLv) {
        this.maxLordLv = maxLordLv;
    }

    public int getBottomLordLv() {
        return bottomLordLv;
    }

    public void setBottomLordLv(int bottomLordLv) {
        this.bottomLordLv = bottomLordLv;
    }
}
