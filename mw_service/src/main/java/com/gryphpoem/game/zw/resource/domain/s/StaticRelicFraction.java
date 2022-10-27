package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author xwind
 * @date 2022/8/9
 */
public class StaticRelicFraction {
    private int id;
    private List<Integer> area;
    private int searchTime;
    private int kill;
    private int loss;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(int searchTime) {
        this.searchTime = searchTime;
    }

    public int getKill() {
        return kill;
    }

    public void setKill(int kill) {
        this.kill = kill;
    }

    public List<Integer> getArea() {
        return area;
    }

    public void setArea(List<Integer> area) {
        this.area = area;
    }

    public int getLoss() {
        return loss;
    }

    public void setLoss(int loss) {
        this.loss = loss;
    }
}
