package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.core.util.LogUtil;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DateHelper {
    public static final String format1 = "yyyy-MM-dd HH:mm:ss";
    public static final String format2 = "yyyy-MM-dd";
    public static final String format3 = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String timeFormat = "HH:mm:ss";
    public static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private static final SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(format1);
    private static final SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(format2);

    /**
     * 德意志反攻是星期二开放
     */
    public static final int TUESDAY = 2;

    /**
     * 星期六, 很多活动都有周六的判断
     */
    public static final int SATURDAY = 6;

    static public boolean isSameDate(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return DateUtils.isSameDay(date1, date2);
    }

    static public boolean isSameDate(Calendar cal1, Calendar cal2) {
        return DateUtils.isSameDay(cal1, cal2);
    }

    public static SimpleDateFormat getDateFormat1(){
        return new SimpleDateFormat(format1);
    }

    static public boolean isBeforeOneDay(Calendar cal1) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return DateUtils.isSameDay(cal1, calendar);
    }

    static public boolean isToday(Date date) {
        return DateUtils.isSameDay(date, new Date());
    }

    static public String displayDateTime() {
        SimpleDateFormat dateFormat3 = new SimpleDateFormat(format3);
        return dateFormat3.format(new Date());
    }

    static public int getNowMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH + 1);
    }

    static public String displayNowDateTime() {
        return getDateFormat1().format(new Date());
    }

    static public String formatDateTime(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    static public String formatDateMiniTime(Date date) {
        SimpleDateFormat dateFormat3 = new SimpleDateFormat(format3);
        return dateFormat3.format(date);
    }

    static public String simpleTimeFormat(Date date) {
        return simpleDateFormat1.format(date);
    }

    static public String simpleDateFormat(Date date) {
        return simpleDateFormat2.format(date);
    }

    static public Date getInitDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2008, 1, 1);
        return calendar.getTime();
    }

    static public long getServerTime() {
        return Calendar.getInstance().getTime().getTime() / 1000;
    }

    static public long dvalue(Calendar calendar, Date date) {
        if (date == null || calendar == null) {
            return 0;
        }
        long dvalue = (calendar.getTimeInMillis() - date.getTime()) / 1000;
        return dvalue;
    }

    // cdTime --秒数
    static public boolean isOutCdTime(Date date, long cdTime) {
        Date nowDate = new Date();
        return (nowDate.getTime() - date.getTime()) > cdTime * 1000;
    }

    static public Date parseDate(String dateString) {
        try {
            SimpleDateFormat dateFormat1 = new SimpleDateFormat(format1);
            return dateFormat1.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date parseDate(int secStamp){
        if(secStamp <= 0){
            return new Date();
        }
        return new Date(secStamp * 1000L);
    }

    static public boolean isInTime(Date now, Date begin, Date end) {
        long nowTime = now.getTime();
        long beginTime = begin.getTime();
        long endTime = end.getTime();
        return nowTime >= beginTime && nowTime <= endTime;
        // if (now.before(end) && now.after(begin)) {
        //     return true;
        // }
    }

    static public boolean isAfterTime(Date now, Date after) {
        long nowTime = now.getTime();
        long afterTime = after.getTime();
        // if ()
        // if (now.after(after)) {
        //     return true;
        // }
        return nowTime >= afterTime;
    }


    /**
     * 第几天,同一天为第一天
     *
     * @param origin
     * @param now
     * @return
     */
    static public int dayiy(Date origin, Date now) {
        Calendar orignC = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        orignC.setTime(origin);
        orignC.set(Calendar.HOUR_OF_DAY, 0);
        orignC.set(Calendar.MINUTE, 0);
        orignC.set(Calendar.SECOND, 0);
        orignC.set(Calendar.MILLISECOND, 0);

        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return (int) ((calendar.getTimeInMillis() - orignC.getTimeInMillis()) / (24 * 3600 * 1000)) + 1;
    }

    /**
     * 第多少天后的时间点(秒值),和dayiy()对应
     *
     * @param origin
     * @param addDay
     * @return
     */
    public static int afterDayTime(Date origin, int addDay) {
        Calendar orignC = Calendar.getInstance();
        addDay -= 1;
        orignC.setTime(origin);
        orignC.set(Calendar.DAY_OF_YEAR, orignC.get(Calendar.DAY_OF_YEAR) + addDay);
        orignC.set(Calendar.HOUR_OF_DAY, 0);
        orignC.set(Calendar.MINUTE, 0);
        orignC.set(Calendar.SECOND, 0);
        orignC.set(Calendar.MILLISECOND, 0);
        return (int) (orignC.getTimeInMillis() / 1000);
    }

    public static Date afterDayTimeDate(Date origin, int addDay) {
        Calendar orignC = Calendar.getInstance();
        addDay -= 1;
        orignC.setTime(origin);
        orignC.set(Calendar.DAY_OF_YEAR, orignC.get(Calendar.DAY_OF_YEAR) + addDay);
        orignC.set(Calendar.HOUR_OF_DAY, 0);
        orignC.set(Calendar.MINUTE, 0);
        orignC.set(Calendar.SECOND, 0);
        orignC.set(Calendar.MILLISECOND, 0);
        return orignC.getTime();
    }

    /**
     * 获取当前是今年的第几个星期
     */
    public static int getWeekOfYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    public static int getWeekOfYear(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * 获取传入秒的当前天0点date对象
     */
    public static Date getTimeZoneDate(int currentSecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentSecond * 1000L);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * @param currentSecond
     * @param hour
     * @return int
     * @Title: getTimeZoneDate
     * @Description: 获取传入秒的当前天 hour点 时间戳
     */
    public static int getTimeZoneDate(int currentSecond, int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentSecond * 1000L);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return (int) (calendar.getTime().getTime() / 1000);
    }

    /**
     * 是否在此区时间段
     *
     * @param now
     * @param start 格式 HH:mm:ss
     * @param end 格式 HH:mm:ss
     * @return
     */
    public static boolean inThisTime(Date now, String start, String end) {
        Date beginDate = null;
        Date endDate = null;
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(timeFormat);
        try {
            now = dateTimeFormat.parse(dateTimeFormat.format(new Date()));
            beginDate = dateTimeFormat.parse(start);
            endDate = dateTimeFormat.parse(end);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (beginDate == null || endDate == null || now == null) {
            return false;
        }
        return DateHelper.isInTime(now, beginDate, endDate);
    }

    public static boolean isAfterTime(Date now, String after) {
        Date beginDate = null;
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(timeFormat);
        try {
            now = dateTimeFormat.parse(dateTimeFormat.format(new Date()));
            beginDate = dateTimeFormat.parse(after);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (beginDate == null || now == null) {
            return false;
        }
        return DateHelper.isAfterTime(now, beginDate);
    }

    public static boolean nowBefore(Date when) {
        try {
            Date now = new Date();
            return now.before(when);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return false;
    }

    /**
     * origin的second秒以后,转成date
     *
     * @param origin
     * @param second
     * @return
     */
    public static Date afterSecondDate(String origin, int second) {
        Date afterDate = null;
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(timeFormat);
        try {
            Date originC = dateTimeFormat.parse(origin);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(originC);
            calendar.set(Calendar.SECOND, second);
            long afterTime = calendar.getTimeInMillis();
            afterDate = dateTimeFormat.parse(dateTimeFormat.format(new Date(afterTime)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return afterDate;
    }



    /**
     * 将origin的时分秒设置到date上
     *
     * @param date
     * @param origin
     * @return
     */
    public static Date afterStringTime(Date date, String origin) {
        Date afterDate = null;
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(timeFormat);
        try {
            Date originC = dateTimeFormat.parse(origin);
            Calendar beforeC = Calendar.getInstance();
            beforeC.setTime(originC);
            Calendar afterC = Calendar.getInstance();
            afterC.setTime(date);
            afterC.set(Calendar.HOUR_OF_DAY, beforeC.get(Calendar.HOUR_OF_DAY));
            afterC.set(Calendar.MINUTE, beforeC.get(Calendar.MINUTE));
            afterC.set(Calendar.SECOND, beforeC.get(Calendar.SECOND));
            afterC.set(Calendar.MILLISECOND, beforeC.get(Calendar.MILLISECOND));
            afterDate = afterC.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return afterDate;
    }

    /**
     * 将origin的时分秒设置到date上
     *
     * @param date
     * @param origin
     * @return
     */
    public static Date afterStringTime(Date date, String origin, int week) {
        Date afterDate = null;
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(timeFormat);
        try {
            Date originC = dateTimeFormat.parse(origin);
            Calendar beforeC = Calendar.getInstance();
            beforeC.setTime(originC);
            Calendar afterC = Calendar.getInstance();
            afterC.setTime(date);
            afterC.setFirstDayOfWeek(Calendar.MONDAY);
            afterC.set(Calendar.DAY_OF_WEEK, week);
            afterC.set(Calendar.HOUR_OF_DAY, beforeC.get(Calendar.HOUR_OF_DAY));
            afterC.set(Calendar.MINUTE, beforeC.get(Calendar.MINUTE));
            afterC.set(Calendar.SECOND, beforeC.get(Calendar.SECOND));
            afterC.set(Calendar.MILLISECOND, beforeC.get(Calendar.MILLISECOND));
            afterDate = afterC.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return afterDate;
    }

    /**
     * 将origin的时分秒设置到date上
     *
     * @param date
     * @param origin
     * @return
     */
    public static Date afterStringTime(Date date, Date origin) {
        Date afterDate;
        // Date originC = dateTimeFormat.parse(origin);
        Calendar beforeC = Calendar.getInstance();
        beforeC.setTime(origin);
        Calendar afterC = Calendar.getInstance();
        afterC.setTime(date);
        afterC.set(Calendar.HOUR_OF_DAY, beforeC.get(Calendar.HOUR_OF_DAY));
        afterC.set(Calendar.MINUTE, beforeC.get(Calendar.MINUTE));
        afterC.set(Calendar.SECOND, beforeC.get(Calendar.SECOND));
        afterC.set(Calendar.MILLISECOND, beforeC.get(Calendar.MILLISECOND));
        afterDate = afterC.getTime();
        return afterDate;
    }

    /**
     * 判断两个时间是否是同一个月
     *
     * @param date1
     * @param date2
     * @return
     */
    static public boolean isMonth(Date date1, Date date2) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        int year1 = calendar.get(Calendar.YEAR);
        int month1 = calendar.get(Calendar.MONTH);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        int year2 = calendar2.get(Calendar.YEAR);
        int month2 = calendar2.get(Calendar.MONTH);

        return year1 == year2 && month1 == month2;
    }


    public static void main(String[] args) throws ParseException {
        // int createRoleTime = 1585558092;
        // int now = 1585970742;
        // int activityOpenTime = 432000;
        // System.out.println(createRoleTime);
        // int activityEndTime = createRoleTime + activityOpenTime;
        // System.out.println(activityEndTime);
        // System.out.println(activityEndTime - now < 0);
        // long createRoleTime2 = parseDate("2020-03-30 01:48:12").getTime() / 1000;
        // int endTime = afterDayTime(parseDate("2020-03-30 01:48:12"), 5);
        // int endTime2 = TimeHelper.afterSecondTime(parseDate("2020-03-30 01:48:12"), activityOpenTime);
        // System.out.println("createRoleTime2 :" + createRoleTime2);
        // System.out.println("endTime :" + endTime);
        // System.out.println("endTime2 :" + endTime2);
        // long robinHoodEndTime =  (createRoleTime2 + activityOpenTime);
        // System.out.println("createRoleTime2 + activityOpenTime:" + robinHoodEndTime);
        // System.out.println("createRoleTime2 + activityOpenTime:" + (createRoleTime2 + (TimeHelper.DAY_S * 5)));
        // System.out.println("now:" + now);
        // System.out.println("createRoleTime2 + activityOpenTime - now : " + (robinHoodEndTime - now));
        // System.out.println("createRoleTime2 + activityOpenTime - now < 0 : " + (robinHoodEndTime - now < 0));
        // System.out.println("now < createRoleTime2 + activityOpenTime: " + (now >= robinHoodEndTime));
        // System.out.println("Math.ceil(0.1) = " + Math.ceil(0.1));
        SimpleDateFormat dateFormat1 = new SimpleDateFormat(format3);
        Date parse = dateFormat1.parse("2020-03-30 12:00:00.010");
        System.out.println("inThisTime(parse, \"12:00:00\", \"15:00:00\") = " + inThisTime(parse, "12:00:00", "15:00:00"));
        // ArrayList<Integer> sorts = new ArrayList<>();
        // sorts.add(2);
        // sorts.add(3);
        // sorts.add(4);
        // sorts.add(1);
        // sorts.sort((t1, t2) -> t1 - t2);
        // sorts.stream().forEach(System.out::println);
        // Stream.of(1,2,3).sorted(Comparator.comparingInt(Integer::intValue)).forEach(System.out::println);
        // System.out.println(dayiy(parseDate("2020-05-08 00:00:00"), parseDate("2020-05-22 00:00:00")));
        // System.out.println(parseDate("2020-04-01 12:41:45"));
    }
}
