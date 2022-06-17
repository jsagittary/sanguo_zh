package com.gryphpoem.game.zw.resource.domain.p;

import java.util.*;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticActivity;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.gryphpoem.game.zw.service.activity.PersonalActService;

public class Activity {
    private int activityId;
    private int activityKeyId;
    private Map<Integer, Long> statusCnt = new HashMap<>();// key, cnt完成多少次 0排行值 1时间戳 3最高排名  配表中clean=1会每日清除 key(0 1 3)不可用
    private Map<Integer, Integer> statusMap = new HashMap<>();// 活动keyID,领取状态1已领取 clean=1 每日清理
    private Map<Integer, Integer> propMap = new HashMap<>(); // 设置每日重置 clean=1 每日清理
    private Map<Integer, Integer> saveMap = new HashMap<>(); // 该集合不会重置
    private Map<String,String> dataMap = new HashMap<>();//存放其他的活动数据

    private Map<Integer, Turple<Integer,Integer>> dayScore = new HashMap<>();//每日的积分数据，用于排行
    private Map<Integer, List<ActivityTask>> dayTasks = new HashMap<>();//每日任务数据

    private int beginTime; // 开始时间,个人活动存储的值20171111(TimeHelper.getCurrentDay());全局活动存储的值是:毫秒值/6000
    private int endTime;
    private int open;
    private int activityType;
    //private int surplusNum;//矿石转盘剩余次数
    //private int alreadyNum;//矿石转盘已转次数
    //private int surplusGold;//矿石转盘当前档位已消费的金币数
    /**
     * 玩家秋季拍卖活动数据
     */
    private ActivityAuction activityAuction;

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int type) {
        this.activityType = type;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public Map<Integer, Long> getStatusCnt() {
        return statusCnt;
    }

    public void setStatusCnt(Map<Integer, Long> statusCnt) {
        this.statusCnt = statusCnt;
    }

    public Map<Integer, Integer> getStatusMap() {
        return statusMap;
    }

    public void setStatusMap(Map<Integer, Integer> statusMap) {
        this.statusMap = statusMap;
    }

    public Map<Integer, Integer> getPropMap() {
        return propMap;
    }

    public void setPropMap(Map<Integer, Integer> propMap) {
        this.propMap = propMap;
    }

    public Map<Integer, Integer> getSaveMap() {
        return saveMap;
    }

    public void setSaveMap(Map<Integer, Integer> saveMap) {
        this.saveMap = saveMap;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public ActivityAuction getActivityAuction() {
        if (CheckNull.isNull(activityAuction)) {
            activityAuction = new ActivityAuction();
        }

        return activityAuction;
    }

    public void setActivityAuction(ActivityAuction activityAuction) {
        this.activityAuction = activityAuction;
    }

    public Activity() {
    }

    /**
     * 清除秋季拍卖活动信息
     * @param activityBase
     */
    public void cleanActivityAuction(ActivityBase activityBase) {
        if (activityBase.getActivityId() != this.getActivityId() && Objects.nonNull(this.activityAuction)) {
            this.activityAuction.clean();
        }
    }

    /**
     * 反序列化构造
     * 
     * @param e
     */
    public Activity(SerializePb.DbActivity e) {
        setActivityId(e.getActivityId());
        setBeginTime(e.getBeginTime());
        setEndTime(e.getEndTime());
        setOpen(e.getOpen());
        setActivityType(e.getActivityType());
        Map<Integer, Long> statusCnt = new HashMap<>();
        if (e.getStatusList() != null) {
            for (CommonPb.IntLong status : e.getStatusList()) {
                statusCnt.put(status.getV1(), status.getV2());
            }
        }
        setStatusCnt(statusCnt);
        Map<Integer, Integer> statusMap = new HashMap<>();
        if (e.getTowIntList() != null) {
            for (CommonPb.TwoInt towInt : e.getTowIntList()) {
                statusMap.put(towInt.getV1(), towInt.getV2());
            }
        }
        setStatusMap(statusMap);

        Map<Integer, Integer> propMap = new HashMap<>();
        if (e.getPropList() != null) {
            for (CommonPb.TwoInt towInt : e.getPropList()) {
                propMap.put(towInt.getV1(), towInt.getV2());
            }
        }
        setPropMap(propMap);

        Map<Integer, Integer> saveMap = new HashMap<>();
        if (e.getSaveList() != null) {
            for (CommonPb.TwoInt towInt : e.getSaveList()) {
                saveMap.put(towInt.getV1(), towInt.getV2());
            }
        }
        setSaveMap(saveMap);

        Optional.ofNullable(e.getDataList()).ifPresent(obj -> obj.forEach(o -> this.dataMap.put(o.getV1(),o.getV2())));

        Optional.ofNullable(e.getSerDayScoreList()).ifPresent(tmps -> tmps.forEach(o -> {
            Turple<Integer,Integer> turple = new Turple<>();
            turple.setA(o.getScore().getV1());
            turple.setB(o.getScore().getV2());
            this.dayScore.put(o.getDay(),turple);
        }));
        Optional.ofNullable(e.getSerDayTaskList()).ifPresent(tmps -> tmps.forEach(o -> {
            List<ActivityTask> taskList = new ArrayList<>();
            if(o.getTaskList() != null){
                o.getTaskList().forEach(o1 -> {
                    ActivityTask task = new ActivityTask(o1.getTaskId(), o1.getProgress(), o1.getCount());
                    task.setDrawCount(o1.getDrawCnt());
                    task.setUid(o1.getUid());
                    taskList.add(task);
                });
            }
            this.dayTasks.put(o.getDay(),taskList);
        }));

        //秋季拍卖活动反序列化
        Optional.ofNullable(e.getActivityAuction()).ifPresent(tmp -> {
            this.activityAuction = new ActivityAuction();
            this.activityAuction.deserialization(tmp);
        });
        this.activityKeyId = e.getActKeyId();
    }

    public Activity(ActivityBase activityBase, int begin) {
        this.activityId = activityBase.getActivityId();
        this.activityKeyId = activityBase.getPlanKeyId();
        this.activityType = activityBase.getStaticActivity().getType();
        this.beginTime = begin;
        this.statusMap = new HashMap<>();
        this.propMap = new HashMap<>();
        this.saveMap = new HashMap<>();
    }

    /**
     * 开启时间不同,则为两次开启活动,重置活动数据
     *
     * @param begin
     * @param player
     */
    public boolean isReset(int begin, Player player) {
        if (PersonalActService.isPersonalAct(activityType))
            return false;
//        LogUtil.debug("活动重开检测 isReset=" + (this.beginTime == begin) + ",activityType=" + activityType + ", actvityId="
//                + activityId + ",beginTime=" + beginTime + ",begin=" + begin);
        if(activityType==111)
        {
//        	 LogUtil.common("活动重开检测 isReset=" + (this.beginTime == begin) + ",activityType=" + activityType + ", actvityId="
//                     + activityId + ",beginTime=" + beginTime + ",begin=" + begin);
        }
        if (this.beginTime == begin) {
            return false;
        }
//        LogUtil.debug("自动清理活动数据begin=" + begin + ",beginTime=" + beginTime);
        this.beginTime = begin;
        this.endTime = begin;
        this.propMap.clear();
        this.cleanActivity(false);
        this.saveMap.clear();
        this.dayScore.clear();
        this.dayTasks.clear();
        this.dataMap.clear();
        return true;
    }

    /**
     * 自动清理活动数据
     * 
     * @param activityBase
     */
    public void autoDayClean(ActivityBase activityBase) {
        StaticActivity sa = activityBase.getStaticActivity();
        this.activityId = activityBase.getActivityId();
        this.activityKeyId = activityBase.getPlanKeyId();
        if (sa.getClean() == ActivityConst.CLEAN_DAY) {
            int nowDay = TimeHelper.getCurrentDay();
            if (this.endTime != nowDay) {
                LogUtil.debug("自动清理活动数据 每日清理 actId:", activityBase.getActivityId());
                cleanActivity(true);
                this.endTime = nowDay;
            }
        }
    }

    /**
     * 清理活动中记录的数据
     */
    public void cleanActivity(boolean isDayClean) {
        cleanStatusMap(isDayClean);
        if (this.statusCnt != null && this.statusCnt.size() > 0) {
            statusCnt.clear();
        }
    }

    /**
     * 清除数据特别处理
     * 
     * @param isDayClean
     */
    private void cleanStatusMap(boolean isDayClean) {
        if (!isDayClean) {
            this.statusMap.clear();
            this.propMap.clear();
        } else { // 每日重置 有些活动 设置了每日重置 但又含有排行榜领奖的 需要保留排行榜领奖状态
            switch (activityType) {
                // case ActivityConst.ACT_PIRATE:
                // Integer receive = this.statusMap.get(0);
                // this.statusMap.clear();
                // if (receive != null) {
                // this.statusMap.put(0, receive);
                // }
                // break;
                // case ActivityConst.ACT_WORSHIP_ID:
                // break;
                default:
                    this.statusMap.clear();
                    this.propMap.clear();
                    break;
            }
        }
    }

    @Override
    public String toString() {
        return "Activity [activityId=" + activityId + ", statusCnt=" + statusCnt + ", statusMap=" + statusMap
                + ", propMap=" + propMap + ", saveMap=" + saveMap + ", beginTime=" + beginTime + ", endTime=" + endTime
                + ", open=" + open + ", activityType=" + activityType + "]";
    }

    public Map<Integer, Turple<Integer, Integer>> getDayScore() {
        return dayScore;
    }

    public void setDayScore(Map<Integer, Turple<Integer, Integer>> dayScore) {
        this.dayScore = dayScore;
    }

    public Map<Integer, List<ActivityTask>> getDayTasks() {
        return dayTasks;
    }

    public void setDayTasks(Map<Integer, List<ActivityTask>> dayTasks) {
        this.dayTasks = dayTasks;
    }

    public Map<String, String> getDataMap() {
        return dataMap;
    }

    public int getActivityKeyId() {
        return activityKeyId;
    }

    public void setActivityKeyId(int activityKeyId) {
        this.activityKeyId = activityKeyId;
    }
}
