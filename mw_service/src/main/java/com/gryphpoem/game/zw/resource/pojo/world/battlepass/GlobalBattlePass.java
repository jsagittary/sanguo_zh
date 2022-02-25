package com.gryphpoem.game.zw.resource.pojo.world.battlepass;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePassPlan;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 战令功能的全局数据
 *
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-12-04 11:16
 */
public class GlobalBattlePass {

    // 自增id
    private static int INCREMENT_KEY = 0;
    // 唯一标识
    private int key;
    // 配置表的Id, 之前对应s_battlepass_plan的keyId, 现在对应planId
    private int staticKey;
    // 开启时间
    private Date beginDate;
    // 结束时间
    private Date endDate;
    /*
     * 记录战令任务的刷新时间
     * key
     * 1 每日
     * 2 每周
     * 3 每月
     * value 如果之前有刷新过, 这里就会存刷新时间, 如果没有就按开启时间来计算
     */
    private Map<Integer, Integer> refreshTime = new HashMap<>();
    /**
     * refreshTime的key
     */
    public static final int REFRESH_TIME_KEY_1 = 1;
    public static final int REFRESH_TIME_KEY_2 = 2;
    public static final int REFRESH_TIME_KEY_3 = 3;
    public static final List<Integer> REFRESH_TIME_KEYS = Arrays.asList(REFRESH_TIME_KEY_1, REFRESH_TIME_KEY_2, REFRESH_TIME_KEY_3);


    public GlobalBattlePass() {
    }

    /**
     * 构造方法, 初始化战令数据
     *
     * @param sPassPlan 战令的配置
     */
    public GlobalBattlePass(StaticBattlePassPlan sPassPlan) {
        this();
        this.key = ++INCREMENT_KEY;
        this.staticKey = sPassPlan.getKeyId();
        this.beginDate = sPassPlan.getRealBeginDate();
        this.endDate = sPassPlan.getRealEndDate();
    }

    /**
     * 是否是相同的战令活动
     * 这里相同的配置id, 并且开启时间一样就认为是同一个
     *
     * @param sPassPlan 战令开启配置
     * @return true 是同一个
     */
    public boolean isSamePlan(StaticBattlePassPlan sPassPlan) {
        return this.staticKey == sPassPlan.getKeyId()/* && DateHelper.dayiy(this.beginDate,sPassPlan.getRealBeginDate()) == 1*/;
    }

    /**
     * 获取上次刷新的时间
     *
     * @param key 刷新的key
     * @return 上次刷新的时间
     */
    public int lastRefreshTimeByKey(int key) {
        int lastRefresh = this.refreshTime.getOrDefault(key, 0);
        return lastRefresh == 0 ? Optional.ofNullable(this.beginDate).map(TimeHelper::dateToSecond).orElse(0) : lastRefresh;
        // 这里有可能会空指针this.beginDate
        // return lastRefresh == 0 ? TimeHelper.dateToSecond(this.beginDate) : lastRefresh;
    }

    /**
     * 返回需要刷新的key
     *
     * @param now 现在的时间
     * @return 需要刷新的key
     */
    public List<Integer> needRefreshKey(Date now) {
        int nowSecond = TimeHelper.dateToSecond(now);
        return REFRESH_TIME_KEYS.stream().filter(key -> {
            int lastRefreshTime = lastRefreshTimeByKey(key);
            Date date = TimeHelper.secondToDate(lastRefreshTime);
            if (key == REFRESH_TIME_KEY_1) {
                // 每日刷新
                return true;
            } else if (key == REFRESH_TIME_KEY_2) {
                // 这里减一天的原因是加了23小时59分59秒
                Date afterDay1 = TimeHelper.getSomeDayAfterOrBerfore(date, 7 - 1, 23, 59, 59);
                // 每周刷新
                return now.after(afterDay1);
            } else {
                // 这里减一天的原因是加了23小时59分59秒
                Date afterDay2 = TimeHelper.getSomeDayAfterOrBerfore(date, 30 - 1, 23, 59, 59);
                return now.after(afterDay2);
            }
        }).peek(key -> {
            // 记录刷新的时间
            this.refreshTime.put(key, nowSecond);
        }).collect(Collectors.toList());
    }

    public int getKey() {
        return key;
    }

    public int getStaticKey() {
        return staticKey;
    }

    public void setStaticKey(int staticKey) {
        this.staticKey = staticKey;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Map<Integer, Integer> getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(Map<Integer, Integer> refreshTime) {
        this.refreshTime = refreshTime;
    }

    @Override
    public String toString() {
        return "GlobalBattlePass{" +
                "key=" + key +
                ", staticKey=" + staticKey +
                ", beginDate=" + beginDate +
                ", endDate=" + endDate +
                ", refreshTime=" + refreshTime +
                '}';
    }

    /**
     * 序列化
     *
     * @return GlobalBattlePass序列化对象
     */
    public SerializePb.GlobalBattlePass ser() {
        SerializePb.GlobalBattlePass.Builder builder = SerializePb.GlobalBattlePass.newBuilder();
        builder.setIncrementKey(INCREMENT_KEY);
        builder.setKey(key);
        builder.setStaticKey(staticKey);
        Optional.ofNullable(this.beginDate).ifPresent(beginDate -> builder.setBeginTime(TimeHelper.dateToSecond(beginDate)));
        Optional.ofNullable(this.endDate).ifPresent(endDate -> builder.setEndTime(TimeHelper.dateToSecond(endDate)));
        if (!CheckNull.isEmpty(refreshTime)) {
            refreshTime.forEach((k, v) -> builder.addRefreshTime(PbHelper.createTwoIntPb(k, v)));
        }
        return builder.build();
    }

    /**
     * 反序列化
     *
     * @param ser 序列化的数据
     */
    public void dser(SerializePb.GlobalBattlePass ser) {
        INCREMENT_KEY = ser.getIncrementKey();
        this.key = ser.getKey();
        this.staticKey = ser.getStaticKey();
        this.beginDate = TimeHelper.secondToDate(ser.getBeginTime());
        this.endDate = TimeHelper.secondToDate(ser.getEndTime());
        for (CommonPb.TwoInt twoInt : ser.getRefreshTimeList()) {
            this.refreshTime.put(twoInt.getV1(), twoInt.getV2());
        }
    }
}