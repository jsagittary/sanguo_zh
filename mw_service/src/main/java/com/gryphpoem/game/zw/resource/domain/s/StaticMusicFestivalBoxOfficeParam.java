package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/10/27 15:22
 */
public class StaticMusicFestivalBoxOfficeParam {
    private int activityId;
    private int propId;
    private List<Integer> points;
    private int exp;
    private Map<Integer, Integer> payId;
    private Map<Integer, Integer> goldCost;

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public List<Integer> getPoints() {
        return points;
    }

    public void setPoints(List<Integer> points) {
        this.points = points;
    }

    public int getPropId() {
        return propId;
    }

    public void setPropId(int propId) {
        this.propId = propId;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public Map<Integer, Integer> getPayId() {
        return payId;
    }

    public void setPayId(Map<Integer, Integer> payId) {
        this.payId = payId;
    }

    public Map<Integer, Integer> getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(Map<Integer, Integer> goldCost) {
        this.goldCost = goldCost;
    }

    /**
     * 获取经验对应的等级
     * @param afterExp 升级后的经验
     * @return 等级
     */
    public Integer getLevel(int afterExp) {
        int level = 0;
        for (int i = 0; i < points.size(); i++) {
            int exp = points.get(i);
            if (afterExp >= exp) {
                level = i + 1;
            }
        }
        return level;
    }
}
