package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName AbsDailyClear.java
 * @Description 每日清除逻辑的基础类
 * @author QiuKun
 * @date 2019年1月22日
 */
public abstract class AbsDailyClear {
    protected int lastRefreshDate; // 最近一次刷新的日期 存储的是 20181224这种,TimeHelper.getCurrentDay()

    public void refresh() {
        int currentDay = TimeHelper.getCurrentDay();
        if (this.lastRefreshDate != currentDay) {
            dailyclearData();
            this.lastRefreshDate = currentDay;
        }
    }

    protected abstract void dailyclearData();

    public int getLastRefreshDate() {
        return lastRefreshDate;
    }

    public void setLastRefreshDate(int lastRefreshDate) {
        this.lastRefreshDate = lastRefreshDate;
    }

}
