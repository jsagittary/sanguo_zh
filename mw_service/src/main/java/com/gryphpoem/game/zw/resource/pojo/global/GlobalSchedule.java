package com.gryphpoem.game.zw.resource.pojo.global;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.ScheduleConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticSchedule;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.WorldScheduleRankService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName GlobalSchedule.java
 * @Description 所有的世界进度
 * @date 2019年2月21日
 */
public class GlobalSchedule {

    /**
     * 当前进度
     */
    private int currentScheduleId;

    /**
     * key=scheduleId, val=世界进度
     */
    private Map<Integer, WorldSchedule> scheduleMap = new HashMap<>(10);


    /**
     * 构造
     */
    public GlobalSchedule() {
    }

    /**
     * 根据具体的世界进度来初始化<br/>
     * "注意:" 如果世界任务进度是5或者9, 需要初始化boss
     *
     * @param curScheduleId
     */
    public GlobalSchedule(int curScheduleId) {
        this.currentScheduleId = curScheduleId;
        StaticWorldDataMgr.getScheduleMap().values().forEach(ss -> scheduleMap.put(ss.getId(), WorldSchedule.create(ss, getWorldScheduleStatus(curScheduleId, ss.getId()))));
    }

    /**
     * 反序列化
     *
     * @param ser
     */
    public GlobalSchedule(SerializePb.SerWroldSchedule ser) {
        this();
        this.currentScheduleId = ser.getCurrentScheduleId();
        for (CommonPb.WorldSchedule schedule : ser.getWorldScheduleList()) {
            scheduleMap.put(schedule.getId(), new WorldSchedule(schedule));
        }
    }

    /**
     * 尝试完成世界进度
     */
    public void tryProcessRefreshSchedule(WorldScheduleRankService worldScheduleRankService) {
        WorldSchedule currentSchedule = getWorldSchedule(currentScheduleId);

        StaticSchedule sSche = StaticWorldDataMgr.getScheduleById(currentScheduleId);
        if (CheckNull.isNull(sSche)) {
            return;
        }
        if (!sSche.canEndAhead()) {
            return;
        }

        if (!currentSchedule.isAllGoalFinish()) {
            return;
        }

        int curId = currentScheduleId;
        // 处理刷新世界进度
        processRefreshSchedule();
        // 发放世界进度奖励 scheduleId
        worldScheduleRankService.worldScheduleRankAward(curId);

        // 尝试完成下一阶段世界进度
        tryProcessRefreshSchedule(worldScheduleRankService);
    }


    /**
     * 处理刷新世界进度
     */
    public void processRefreshSchedule() {
        // 还未到最后进度
        if (currentScheduleId < StaticWorldDataMgr.SCHEDULE_MAX_ID) {
            int curId = this.currentScheduleId;
            WorldSchedule curSch = this.getWorldSchedule(curId);
            // 进度结束时处理
            curSch.finishSchedule();

            LogLordHelper.otherLog("worldScheduleEnd", DataResource.ac.getBean(ServerSetting.class).getServerID(), curId,
                    curSch.getStartTime(), curSch.getFinishTime(), curSch.getGoalString(), this.currentScheduleId + 1);

            // 进度开始时处理
            int nextId = ++this.currentScheduleId;
            // 开启德意志
            if (ScheduleConstant.SCHEDULE_BERLIN_ID == nextId) {
                // 初始化柏林会战
                ScheduleManager.getInstance().initBerlinJob();
                // 记录世界进程阶段
                BerlinWar berlinWar = DataResource.ac.getBean(GlobalDataManager.class).getGameGlobal().getBerlinWar();
                if (Objects.nonNull(berlinWar)) {
                    LogUtil.debug("worldScheduleUpdate, berlinWar.scheduleId: ", berlinWar.getScheduleId(), ", nextId: ", nextId);
                    berlinWar.updateScheduleId(nextId);
                    LogUtil.debug("worldScheduleUpdate, berlinWar.scheduleId: ", berlinWar.getScheduleId());
                }
            }
            // 世界进程第5，9阶段时，添加广播和跑马灯消息
            if (ScheduleConstant.SCHEDULE_BOOS_1_ID == nextId || ScheduleConstant.SCHEDULE_BOOS_2_ID == nextId) {
                // 发送跑马灯
                DataResource.ac.getBean(ChatDataManager.class).sendSysChat(ChatConst.CHAT_SCHEDULE_5_OR_9, 0, 0, nextId);
            }
            // 触发飞艇的开放
            // DataResource.ac.getBean(AirshipService.class).triggerInitAirship();
            WorldSchedule nextSch = this.getWorldSchedule(nextId);
            nextSch.progressSchedule(StaticWorldDataMgr.getScheduleById(nextId));

//            貂蝉任务-世界进程
//            ActivityDiaoChanService.completeTask(null, ETask.WORLD_PROGRESS,nextId);
            // 尝试完成世界进度
            tryProcessRefreshSchedule(DataResource.ac.getBean(WorldScheduleRankService.class));
        }
    }

    /**
     * 检测是否配置了新的世界进程
     */
    public void checkNewWorldSchedule() {
        int maxScId = this.getScheduleMap().values().stream().mapToInt(WorldSchedule::getId).max().orElse(0);
        // 说明策划配置了新的世界进程
        if (maxScId < StaticWorldDataMgr.SCHEDULE_MAX_ID) {
            if (this.getCurrentScheduleId() == maxScId) {
                // 当前进程已经是老配置的最大进程
                WorldSchedule olgMaxSch = this.getWorldSchedule(maxScId);
                if (Objects.isNull(olgMaxSch)) {
                    return;
                }
                StaticSchedule olgMaxStaSch = StaticWorldDataMgr.getScheduleById(maxScId);
                if (Objects.isNull(olgMaxStaSch)) {
                    return;
                }
                int finishTime = olgMaxSch.getFinishTime();
                // 策划给之前的世界进程结束条件了
                if (finishTime <= 0 && olgMaxStaSch.getDurationTime() > 0) {
                    // 持续时间
                    int durationTime = olgMaxStaSch.getDurationTime();
                    // 结束时间
                    finishTime = TimeHelper.getSomeDayAfter(TimeHelper.secondToDate(olgMaxSch.getStartTime()), durationTime - 1, 23, 59, 59);
                    // 如果结束时间已经过了, 结束时间就是当天的23点59分59秒
                    finishTime = finishTime < TimeHelper.getCurrentSecond() ? TimeHelper.dateToSecond(TimeHelper.getSomeDayAfterOrBerfore(new Date(), 0, 23, 59, 59)) : finishTime;
                    // 给进程设置结束时间
                    olgMaxSch.setFinishTime(finishTime);
                    // 初始化限时目标
                    olgMaxSch.initGoal(olgMaxStaSch);
                }
            }
            // 更新新的世界进程
            StaticWorldDataMgr.getScheduleMap()
                    .values()
                    .stream()
                    .filter(sConf -> sConf.getId() > maxScId)
                    .forEach(sConf -> {
                        scheduleMap.put(sConf.getId(), WorldSchedule.create(sConf, getWorldScheduleStatus(this.currentScheduleId, sConf.getId())));
                    });
        }
    }


    /**
     * 获取指定进度的状态
     *
     * @param curId
     * @param schId
     * @return
     */
    private int getWorldScheduleStatus(int curId, int schId) {
        int status = ScheduleConstant.SCHEDULE_STATUS_NOT_YET_BEGIN;
        if (curId == schId) {
            status = ScheduleConstant.SCHEDULE_STATUS_PROGRESS;
        } else if (curId > schId) {
            status = ScheduleConstant.SCHEDULE_STATUS_FINISH;
        }
        return status;
    }

    /**
     * 获取boss
     *
     * @return
     */
    public List<ScheduleBoss> bosses() {
        return scheduleMap.values().stream().filter(schedule -> !CheckNull.isNull(schedule.getBoss())).map(sched -> sched.getBoss()).collect(Collectors.toList());
    }

    public int getCurrentScheduleId() {
        return currentScheduleId;
    }

    public void setCurrentScheduleId(int currentScheduleId) {
        this.currentScheduleId = currentScheduleId;
    }

    public Map<Integer, WorldSchedule> getScheduleMap() {
        return scheduleMap;
    }

    public void setScheduleMap(Map<Integer, WorldSchedule> scheduleMap) {
        this.scheduleMap = scheduleMap;
    }

    public WorldSchedule getWorldSchedule(int scheduleId) {
        return scheduleMap.get(scheduleId);
    }
}
