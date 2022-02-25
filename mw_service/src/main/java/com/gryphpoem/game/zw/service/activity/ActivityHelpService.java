package com.gryphpoem.game.zw.service.activity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.ActParamConstant;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.Java8DateUtil;

/**
 * @ClassName ActivityHelpService.java
 * @Description 活动相关的帮助类
 * @author QiuKun
 * @date 2018年7月20日
 */
@Component
public class ActivityHelpService {

    @Autowired
    private ServerSetting serverSetting;

    /**
     * 获取礼物特惠活动开始时间结束时间
     * 
     * @return 返回开时间和结束时间 , null说明不在活动期间内
     */
    public Date[] getGiftPromotionDate() {
        // 开服的第一周不开启
        LocalDate openServerDate = LocalDateTime
                .parse(serverSetting.getOpenTime(), DateTimeFormatter.ofPattern(DateHelper.format1)).toLocalDate();

        LocalDate openServerMonday = openServerDate.with(ChronoField.DAY_OF_WEEK, 1);
        LocalDate openServerSunday = openServerDate.with(ChronoField.DAY_OF_WEEK, 7);
        LocalDate today = LocalDate.now();
        if (today.compareTo(openServerMonday) >= 0 && today.compareTo(openServerSunday) <= 0) {// 在开服的第1周不开
            return null;
        }

        List<List<Integer>> config = ActParamConstant.ACT_GIFT_PROMOTION_WEEKOFMONTH_WEEK_DURATION;
        List<Integer> weekOfMonthConfig = config.get(0);// 每月的第几周
        int weekConfig = config.get(1).get(0); // 周几
        int duringDayConfig = config.get(2).get(0); // 持续第几天

        // int weekOfMoth = today.get(ChronoField.ALIGNED_WEEK_OF_MONTH);
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(Java8DateUtil.toDate(today));
        int weekOfMoth = calendar.get(Calendar.WEEK_OF_MONTH);
        if (weekOfMonthConfig.contains(weekOfMoth)) {
            LocalDate startDate = today.with(ChronoField.DAY_OF_WEEK, weekConfig);
            LocalDate endDate = startDate.plusDays(duringDayConfig);
            Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().minusSeconds(1);
            Instant nowInstant = Instant.now();
            if (nowInstant.isAfter(startInstant) && nowInstant.isBefore(endInstant)) {
                Date[] dateArr = new Date[2];
                dateArr[0] = Date.from(startInstant);
                dateArr[1] = Date.from(endInstant);
                return dateArr;
            }
        }
        return null;
    }

}
