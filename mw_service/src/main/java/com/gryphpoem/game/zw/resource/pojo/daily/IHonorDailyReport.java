package com.gryphpoem.game.zw.resource.pojo.daily;

/**
 * @ClassName IHonorDailyReport.java
 * @Description 荣耀日报的
 * @author QiuKun
 * @date 2018年8月25日
 */
public interface IHonorDailyReport {

    /**
     * 获取创建时间
     * 
     * @return
     */
    int getCreateTime();

    /**
     * 是否是DailyReport类的实例
     * 
     * @return
     */
    default boolean isDailyReportIns() {
        return (this instanceof DailyReport);
    }

    /**
     * 是否是HonorReport2类的实例
     * 
     * @return
     */
    default boolean isHonorReport2Ins() {
        return (this instanceof HonorReport2);
    }
}
