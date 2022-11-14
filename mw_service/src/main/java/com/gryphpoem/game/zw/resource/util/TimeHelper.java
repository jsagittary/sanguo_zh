package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.core.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;

import java.util.Calendar;
import java.util.Date;

public class TimeHelper {
    public final static long SECOND_MS = 1000L;
    public final static long MINUTE_MS = 60 * 1000L;
    public final static long DAY_MS = 24 * 60 * 60 * 1000L;
    public final static int MINUTE_S = 60;
    public final static int DAY_S = 24 * 60 * 60;
    public final static int HOUR_S = 60 * 60;
    public final static int HALF_HOUR_S = 30 * 60;
    public final static int WEEK_SECOND = 7 * 24 * 60 * 60;

    public static final int MINUTE = 60;

    public static int getCurrentSecond() {
        return (int) (System.currentTimeMillis() / SECOND_MS);
    }

    public static int getCurrentMinute() {
        return (int) (System.currentTimeMillis() / MINUTE_MS);
    }

    // public static int getCurrentDay() {
    // return (int) (System.currentTimeMillis() / DAY_MS);
    // }

    public static boolean isSameWeek(Date beginDate, Date endDate) {
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginDate);
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);

        // 换算beginDate的周一时间
        int beginDayOfWeek = begin.get(Calendar.DAY_OF_WEEK);
        if (beginDayOfWeek == 1) {
            begin.add(Calendar.DAY_OF_YEAR, -6);
        } else if (beginDayOfWeek > 2) {
            begin.add(Calendar.DAY_OF_YEAR, 2 - beginDayOfWeek);
        }

        // 换算endDate的周一时间
        int endDayOfWeek = end.get(Calendar.DAY_OF_WEEK);
        if (endDayOfWeek == 1) {
            end.add(Calendar.DAY_OF_YEAR, -6);
        } else if (endDayOfWeek > 2) {
            end.add(Calendar.DAY_OF_YEAR, 2 - endDayOfWeek);
        }
        return ((end.get(Calendar.YEAR) == begin.get(Calendar.YEAR)) && (end.get(Calendar.DAY_OF_YEAR) == begin.get(Calendar.DAY_OF_YEAR)));
    }


    public static int getCurrentDay() {
        Calendar c = Calendar.getInstance();
        int d = c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH);
        return d;
    }

    public static int getCurrentDay0(){
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) * 1000000 + (c.get(Calendar.MONTH) + 1) * 10000 + c.get(Calendar.DAY_OF_MONTH) * 100;
    }

    public static int getCurrentWeek() {
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        int d = c.get(Calendar.YEAR) * 100 + c.get(Calendar.WEEK_OF_YEAR);
        return d;
    }

    public static int getCurrentHour() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public static int getHour(int second) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(second * SECOND_MS));
        return c.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 
     * @param time 格式为20150108
     * @param passTime 格式为20150108
     * @return
     */
    public static int subDay(int time, int passTime) {
        if (passTime == 0 || passTime == 0) {
            return 0;
        }
        long time1 = getDate(time).getTime();
        long time2 = getDate(passTime).getTime();
        return (int) ((time1 - time2) / (DAY_S * 1000));
    }

    public static Date getDate(int today) {
        int passYear = today / 10000;
        if (passYear == 0) {
            return null;
        }
        int passMonth = (today - passYear * 10000) / 100;
        int passToday = (today - passYear * 10000 - passMonth * 100);
        String date = passYear + "-" + passMonth + "-" + passToday + " 00:00:00.000";
        Date d1 = DateHelper.parseDate(date);
        return d1;
    }

    public static boolean isMonday() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 2) {
            return true;
        }
        return false;
    }

    public static boolean isTuesday() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.TUESDAY;
    }

    public static boolean isDayOfWeek(int dayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        return weekDay == dayOfWeek;
    }

    /** 是否是星期五 */
    public static boolean isFriday() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.FRIDAY;
    }

    /**
     * 返回今天是星期几，按中国习惯，星期一返回1，星期天返回7
     * 
     * @return
     */
    public static int getCNDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (weekDay == 0) {
            weekDay = 7;
        }
        return weekDay;
    }

    /**
     * 返回今天是星期几，按中国习惯，星期一返回1，星期天返回7
     * 
     * @param date
     * @return
     */
    public static int getCNDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (weekDay == 0) {
            weekDay = 7;
        }
        return weekDay;
    }

    /**
     * 返回日历的星期, 按照日历习惯, 星期天返回1, 星期六返回7
     *
     * @param weekDay
     * @return
     */
    public static int getCalendarDayOfWeek(int weekDay) {
        if (weekDay == 7) {
            weekDay = 0;
        }
        return weekDay + 1;
    }

    /**
     * 基础星期转换成Num
     * 
     * @param weekDay
     * @return
     */
    public static int getWeekEnConverNum(String weekDay) {
        int weekNum = 0;
        switch (weekDay) {
            case "MON":
                weekNum = 1;
                break;
            case "TUE":
                weekNum = 2;
                break;
            case "WED":
                weekNum = 3;
                break;
            case "THU":
                weekNum = 4;
                break;
            case "FRI":
                weekNum = 5;
                break;
            case "SAT":
                weekNum = 6;
                break;
            case "SUN":
                weekNum = 7;
                break;
        }
        return weekNum;
    }

    public static int getDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int d = c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH);
        return d;
    }

    public static int getDay(long second) {
        return getDay(new Date(second * 1000L));
        // Calendar c = Calendar.getInstance();
        // c.setTimeInMillis(second * 1000);
        // int d = c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) *
        // 100 + c.get(Calendar.DAY_OF_MONTH);
        // return d;
    }

    public static Date getDate(long second) {
        return new Date(second * 1000L);
    }

    public static Date getDateByStamp(int sec){
        return new Date(sec * 1000L);
    }

    public static int getMonthAndDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int monthAndDay = (c.get(Calendar.MONTH) + 1) * 10000 + (c.get(Calendar.DAY_OF_MONTH)) * 100;
        return monthAndDay;
    }

    public static boolean isTimeSecond(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);
        return h == hour && m == minute && s == second;
    }

    public static boolean isTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        return h == hour && m == minute;
    }

    public static int getSecond(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return (int) (calendar.getTimeInMillis() / 1000);
    }

    /**
     * 获取若干天后的，某时分秒的时间，从今天到明天算一天
     * 
     * @param addDays
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public static int getSomeDayAfter(int addDays, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + addDays);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return (int) (calendar.getTimeInMillis() / 1000);
    }

    /**
     * 获取某个时间点若干天后的，某时分秒的时间
     * 
     * @param origin
     * @param addDays
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public static int getSomeDayAfter(Date origin, int addDays, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(origin);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + addDays);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return (int) (calendar.getTimeInMillis() / 1000);
    }

    public static int getHour() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MINUTE);
    }

    /**
     * 获取下个小时的整点时间
     * 
     * @return
     */
    public static int getNextHourTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return (int) (calendar.getTimeInMillis() / 1000);
    }

    /**
     * 获取离现在最近的整点时间，如果当前时间是整点，直接返回当前时间，否则返回下一个小时的整点时间
     * 
     * @return
     */
    public static int getNearlyHourTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        if (second != 0 || minute != 0) {
            calendar.set(Calendar.HOUR_OF_DAY, hour + 1);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }
        calendar.set(Calendar.MILLISECOND, 0);
        return (int) (calendar.getTimeInMillis() / 1000);
    }

    /**
     * 
     * 周日20点15到周六19:30之间
     * 
     * @return
     */
    public static boolean isThisWeekSaturday1930ToSunday2015() {
        Calendar c = Calendar.getInstance();

        // 判断今天是否周日
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // 周日
        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 15);
        c.set(Calendar.SECOND, 0);
        int s1 = (int) (c.getTime().getTime() / SECOND_MS);

        // 本周六19.30
        c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); // 获取周六的
        c.set(Calendar.HOUR_OF_DAY, 19);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 0);
        int s2 = (int) (c.getTime().getTime() / SECOND_MS);

        int cc = getCurrentSecond();

        return s1 <= cc && cc <= s2;
    }

    /**
     * 是否小于本周六19.30
     * 
     * @return
     */
    public static boolean isLessThanThisWeekSaturday1930() {
        Calendar c = Calendar.getInstance();
        // 本周六19.30
        c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); // 获取周六的
        c.set(Calendar.HOUR_OF_DAY, 19);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 0);
        int s = (int) (c.getTime().getTime() / SECOND_MS);
        int cc = getCurrentSecond();
        return cc < s;
    }

    /**
     * 是否小于本周四20:00
     *
     * @return
     */
    public static boolean isLessThanThisWeekWednesday2000() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY); // 获取周四的
        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        int s = (int) (c.getTime().getTime() / SECOND_MS);
        int cc = getCurrentSecond();
        return cc < s;
    }

    /**
     * 当前时间是否大于今天的19:30
     * 
     * @return
     */
    public static boolean isMoreThan1930() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 19);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 0);
        int s = (int) (c.getTime().getTime() / SECOND_MS);
        int cc = getCurrentSecond();
        return cc > s;
    }

    /**
     * 获取该天的凌晨时刻（秒）
     * 
     * @param currentSecond
     * @return
     */
    public static int getTodayZone(int currentSecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentSecond * 1000L);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return (int) (calendar.getTimeInMillis() / 1000);
    }

    public static int getTodayZone() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return (int) (calendar.getTimeInMillis() / 1000);
    }

    public static int getTomorrowZone() {
        return getTodayZone() + DAY_S;
    }

    /**
     * 判断时间是否本周
     * 
     * @param dayTime
     * @return
     */
    public static boolean isThisWeek(int dayTime) {
        Calendar c = Calendar.getInstance();

        // 判断是否周日
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == Calendar.SUNDAY) {
            c.add(Calendar.WEEK_OF_YEAR, -1);
        }

        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // 获取本周一的
        int mondayTime = c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100
                + c.get(Calendar.DAY_OF_MONTH);

        c.add(Calendar.WEEK_OF_YEAR, 1);
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // 获取本周日的
        int sundayTime = c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100
                + c.get(Calendar.DAY_OF_MONTH);

        return dayTime >= mondayTime && dayTime <= sundayTime;
    }

    /**
     * 判断是否周日
     * 
     * @return
     */
    public static boolean isSunDay() {
        Calendar c = Calendar.getInstance();
        // 判断是否周日
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SUNDAY;
    }

    /**
     * 获取本周一
     * 
     * @return 返回格式:yyyyMMdd
     */
    public static int getThisWeekMonday() {
        // 判断若是星期天的话,则需要-1周
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == Calendar.SUNDAY) {
            c.add(Calendar.WEEK_OF_YEAR, -1);
        }

        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // 获取本周一的
        return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取本周日
     * 
     * @return 返回格式:yyyyMMdd
     */
    public static int getThisWeekSunday() {
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == Calendar.SUNDAY) {
        } else {
            c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // 获取本周日的
            c.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH);
    }

    public static void main(String[] args) {
        // System.out.println(getCurrentSecond());
        // System.out.println(getCurrentSecond() + 3600);
        // System.out.println(DateHelper.dateFormat1.format(getDate(new Long(afterSecondTime(new Date(), 3600)))));
        // System.out.println(DateHelper.dateFormat1.format(getDayOfWeekByDate(new Date(), 1)));
//        Date now = new Date();
//        System.out.println("now:" + now);
//        System.out.println(getDayOfWeekByDate(now, 1));
//        System.out.println(getDayOfWeekByDate(now, 2));
//        System.out.println(getDayOfWeekByDate(now, 3));
//        System.out.println(getDayOfWeekByDate(now, 4));
//        System.out.println(getDayOfWeekByDate(now, 5));
//        System.out.println(getDayOfWeekByDate(now, 6));
//        System.out.println(getDayOfWeekByDate(now, 7));
//        System.out.println("getSomeDayAfterOrBerfore(now, 7, 0, 0, 0) = " + getSomeDayAfterOrBerfore(now, 28, 0, 0, 0));
//        System.out.println("getHour() = " + getHour());
//        System.out.println("Stream.of(1,2,3,4,5,6,7,8,9).limit(3).collect(Collectors.toList()).indexOf(1) = " + Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9).limit(3).collect(Collectors.toList()).indexOf(1) + 1);
//        Stream.of(1,2,3,4,5,6,7,8,9).limit(3).filter(en -> true).collect(Collectors.toList()).forEach(System.out::println);
        // Stream.iterate(1 , id -> ++id).limit(11).forEach(System.out::println);
        // System.out.println(DateHelper.dateFormat1.format(getDayOfWeekByDate(new Date(), 2)));
        // System.out.println(DateHelper.dateFormat1.format(getDayOfWeekByDate(new Date(), 3)));
        // System.out.println(DateHelper.dateFormat1.format(getDayOfWeekByDate(new Date(), 4)));
        // System.out.println(DateHelper.dateFormat1.format(getDayOfWeekByDate(new Date(), 5)));
        // System.out.println(DateHelper.dateFormat1.format(getDayOfWeekByDate(new Date(), 6)));
        // System.out.println(DateHelper.dateFormat1.format(getDayOfWeekByDate(new Date(), 7)));
//        Date date1 = DateHelper.parseDate("2021-12-31 12:00:00");
//        Date date2 = DateHelper.parseDate("2022-01-02 00:00:00");
//        System.out.println("is same week :" + isSameWeek(date2, date1));
        int day = getDay(getCurrentSecond());
        Date delayDay = getSomeDayAfterOrBerfore(getDate(day), 2, 8, 0, 0);
        System.out.println("delayDay = " + delayDay);
    }


    public static Date getDateZeroTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * date的second秒以后
     *
     * @param date
     * @param second
     * @return
     */
    public static int afterSecondTime(Date date, int second) {
        return (int) (date.getTime() / SECOND_MS + second);
    }

    public static Date beforeMinuteTime(Date date,int minute){
        return new Date(date.getTime() - minute * MINUTE_MS);
    }

    public static Date afterSecondTime1(Date date,int second){
        return new Date(date.getTime() + second * SECOND_MS);
    }

    /**
     * 获取这个月的第几周
     * 
     * @param date
     * @return
     */
    public static int getWeekOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY); // 周1位第一天
        calendar.setTime(date);
        int weekOfMoth = calendar.get(Calendar.WEEK_OF_MONTH);
        return weekOfMoth;
    }

    /**
     * 获取某个时间的第几周时间
     * 
     * @param date
     * @param week
     * @return
     */
    public static Date getDayOfWeekByDate(Date date, int week) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, getCalendarDayOfWeek(week));
        return cal.getTime();
    }

    /**
     * 获取前几天或后几天的时间
     * 
     * @param date      原时间
     * @param addDays   天数
     * @param hour      小时
     * @param minute    分钟
     * @param second    秒
     * @return          变动后的Date
     */
    public static Date getSomeDayAfterOrBerfore(Date date, int addDays, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + addDays);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date secondToDate(int second) {
        return new Date(second * 1000L);
    }

    public static int dateToSecond(Date date) {
        return (int) (date.getTime() / 1000);
    }
    
    //当前时间加上指定天数后转换成毫秒值返回
    public static  int getNextDaySencod(int time) {
		   Date date = new Date();
		   Calendar calendar = Calendar.getInstance();
	       calendar.setTime(date);
	       calendar.add(Calendar.DAY_OF_MONTH, +time);//+1今天的时间加一天
	       date = calendar.getTime();
		  return (int) (date.getTime() / 1000);
	}

    public static int getNextTimeStampByCron(String cron){
        if (StringUtils.isBlank(cron))
            return -1;
        try {
            CronExpression cronExpression = new CronExpression(cron);
            return (int) (cronExpression.getNextValidTimeAfter(new Date()).getTime()/SECOND_MS);
        }catch (Exception e) {
            LogUtil.error("根据CRON表达式获取下次执行的时间发生异常, ", e);
        }
        return -1;
    }

    public static int getWeekByCron(String cronWeek){////MON TUE WED THU FRI SAT SUN
        if(cronWeek.equals("MON")){
            return Calendar.MONDAY;
        }else if(cronWeek.equals("TUE")){
            return Calendar.TUESDAY;
        }else if(cronWeek.equals("WED")){
            return Calendar.WEDNESDAY;
        }else if(cronWeek.equals("THU")){
            return Calendar.THURSDAY;
        }else if(cronWeek.equals("FRI")){
            return Calendar.FRIDAY;
        }else if(cronWeek.equals("SAT")){
            return Calendar.SATURDAY;
        }else {
            return Calendar.SUNDAY;
        }
    }

    public static int getDayEndStamp(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,23);
        c.set(Calendar.MINUTE,59);
        c.set(Calendar.SECOND,59);
        c.set(Calendar.MILLISECOND,0);
        return (int) (c.getTimeInMillis()/SECOND_MS);
    }

    public static int getDayLostSecond(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        Date now = new Date();
        return (int) ((now.getTime() - c.getTimeInMillis())/SECOND_MS);
    }

}
