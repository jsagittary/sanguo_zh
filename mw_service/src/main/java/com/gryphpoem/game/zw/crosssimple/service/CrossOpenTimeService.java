package com.gryphpoem.game.zw.crosssimple.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.pojo.PeriodTime;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName CrossOpenService.java
 * @Description 跨服和游戏服都使用该类
 * @author QiuKun
 * @date 2019年5月15日
 */
@Component
public class CrossOpenTimeService {
    // 当前时间
    private PeriodTime curOpentTime;

    public void refreshTime() {
        if (curOpentTime == null) {
            curOpentTime = createOpenCrossTime();
        } else {
            int now = TimeHelper.getCurrentSecond();
            if (now > curOpentTime.getEndTime()) {
                curOpentTime = createOpenCrossTime();
            }
        }
    }

    /**
     * 是否在跨服玩法活动期间类
     */
    public boolean isInCrossTimeCond() {
        if (curOpentTime == null) {
            return false;
        }
        int nowTime = TimeHelper.getCurrentSecond();
        return nowTime > curOpentTime.getStartTime() && nowTime < curOpentTime.getEndTime();
    }

    public PeriodTime getCurOpentTime() {
        return curOpentTime;
    }

    /**
     * 创建开始时间
     * 
     * @return
     */
    public static PeriodTime createOpenCrossTime() {
        List<List<Integer>> timeCfg = Constant.CROSS_FIGHT_OPEN_TIME;
        if (timeCfg == null) {
            return null;
        }
        int weekCfg = timeCfg.get(0).get(0).intValue(); // 周几
        Date nowDate = new Date();
        Date openTimeDate = TimeHelper.getDayOfWeekByDate(nowDate, weekCfg);
        int preViewTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(openTimeDate, -1, 0, 0, 0).getTime() / 1000L); // 预显示的时间
        int hourCfg = timeCfg.get(0).get(1).intValue(); // 几点
        int startTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(openTimeDate, 0, hourCfg, 0, 0).getTime() / 1000L);
        int duringCfg = timeCfg.get(0).get(2).intValue(); // 持续多久
        int endTime = startTime + duringCfg;

        PeriodTime periodTime = new PeriodTime();
        periodTime.setPreViewTime(preViewTime);
        periodTime.setStartTime(startTime);
        periodTime.setEndTime(endTime);
        return periodTime;
    }
}
