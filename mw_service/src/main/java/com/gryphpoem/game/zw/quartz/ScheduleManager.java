package com.gryphpoem.game.zw.quartz;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.crosssimple.service.CrossDataService;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticLightningWarDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.SiLiDominateWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.StateDominateWorldMap;
import com.gryphpoem.game.zw.manager.DressUpDataManager;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.quartz.jobs.*;
import com.gryphpoem.game.zw.quartz.jobs.sandtable.SandTableJob;
import com.gryphpoem.game.zw.quartz.jobs.sandtable.SandTableOpenEndRoundJob;
import com.gryphpoem.game.zw.quartz.jobs.sandtable.SandTablePreviewJob;
import com.gryphpoem.game.zw.quartz.jobs.worldwar.WorldWarDailyAttackCityCampIntegralJob;
import com.gryphpoem.game.zw.quartz.jobs.worldwar.WorldWarDailyJob;
import com.gryphpoem.game.zw.quartz.jobs.worldwar.WorldWarWeekJob;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.MedalConst;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.s.StaticActivityPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticLightningWar;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldRule;
import com.gryphpoem.game.zw.resource.pojo.sandtable.SandTableContest;
import com.gryphpoem.game.zw.resource.pojo.sandtable.SandTableGroup;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.BerlinWarService;
import com.gryphpoem.game.zw.service.CounterAtkService;
import com.gryphpoem.game.zw.service.LightningWarService;
import com.gryphpoem.game.zw.service.RebelService;
import com.gryphpoem.game.zw.service.activity.ActivityAuctionService;
import com.gryphpoem.game.zw.service.activity.ActivityChristmasService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.ActivityTemplateService;
import com.gryphpoem.game.zw.service.*;
import com.gryphpoem.game.zw.service.activity.*;
import com.gryphpoem.game.zw.service.dominate.DominateWorldMapService;
import com.gryphpoem.game.zw.service.plan.DrawCardPlanTemplateService;
import com.gryphpoem.game.zw.service.session.SeasonService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.quartz.*;
import org.quartz.DateBuilder.IntervalUnit;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gryphpoem.game.zw.core.util.QuartzHelper.addJob;

/**
 * @author QiuKun
 * @ClassName ScheduleManager.java
 * @Description 复杂的定时器规则
 * @date 2017年8月17日
 */
public class ScheduleManager {

    private Scheduler sched;
    private volatile static ScheduleManager instance;

    private ScheduleManager() {
        this.sched = (Scheduler) DataResource.ac.getBean("schedulerFactoryBean");
    }

    public static ScheduleManager getInstance() {
        ScheduleManager result = instance;
        if (result == null) {
            synchronized (ScheduleManager.class) {
                result = instance;
                if (result == null) {
                    instance = result = new ScheduleManager();
                }
            }
        }
        return result;
    }

    public JobDetail getJobDetail(String group, String name) {
        try {
            return sched.getJobDetail(JobKey.jobKey(name, group));
        } catch (SchedulerException e) {
            LogUtil.error("获取JobDetail发生错误", e);
            return null;
        }
    }

    /**
     * 初始化quartz定时器
     */
    public void initRegisterJob() {
        // addJob(sched, "test1", "testG", PushTestJob.class, "0/5 * * * * ?");// 每5秒钟执行一次
//        addJob(sched, "Monitor", "GameServerQos", ServerMonitorJob.class, "0 */1 * * * ?");
        addJob(sched, "HonorRankSettleJob", "Camp", HonorRankSettleJob.class, "0 59 23 ? * THU");

        // 海外不要体力赠送的活动
        addJob(sched, "SyncActPowerGiveJob", "Act", SyncActPowerGiveJob.class, "0 0 12 * * ?");// 能量赠送推送 每天12点推一次
        addJob(sched, "SyncActPowerGiveJob2", "Act", SyncActPowerGiveJob.class, "0 0 18 * * ?");// 每日18点推一次
        addJob(sched, "CityLordRewardJob", "CityService", CityLordRewardJob.class, "0 0 21 * * ?");// 每日21发城主奖励
        addJob(sched, "AcrossTheDayJob", "AcrossTheDay", AcrossTheDayJob.class, "1 0 0 * * ?");// 转点处理
        addJob(sched, "AcrossTheHourAfterJob", "AcrossTheHourAfter", AcrossTheHourAfterJob.class, "5 0 * * * ? *");// 整点处理, 有五秒的偏移, [21:00:5, 22:00:5, 23:00:5, 00:00:5]
        addJob(sched, "AcrossTheDayBeforeJob", "AcrossTheDayBefore", AcrossTheDayBeforeJob.class, "50 59 23 * * ?");// 转点前的处理
        addJob(sched, "WesternPointSpecialTraining", "MedalSpecial", WesternPointSpecialTrainingJob.class,
                "0 0 5 * * ?");// 每日5点执行 勋章特技-西点特训 逻辑
        addJob(sched, "RefreshPartyJob", "RefreshParty", RefreshPartyJob.class, "0 0 0/6 * * ? "); // 每6小时执行一次, [0:00,
        // 6:00, 12:00,
        // 18:00]

        // 世界进程2、3、4、6、7、8 （12、21统计排行榜军威值）
        addJob(sched, "WorldScheduleRankJob", "WorldScheduleRankJob", WorldScheduleRankJob.class, "0 0 12,21 * * ?");

        // 世界争霸 每日结束领取奖励
        addJob(sched, "WorldWarDailyJob", "WorldWar", WorldWarDailyJob.class, "45 59 23 * * ?");
        // 世界争霸 每天12点和21点，根据当前阵营占领的城市，结算一次阵营的军威值
        addJob(sched, "WorldWarDailyAttackCityCampIntegralJob", "WorldWar",
                WorldWarDailyAttackCityCampIntegralJob.class, "0 0 12,21 * * ?");
        // 世界争霸 每周结束领取奖励
        addJob(sched, "WorldWarWeekJob", "WorldWarWeekJob", WorldWarWeekJob.class, "45 59 23 ? * 4");

        // 过期宝具定时清除
        addJob(sched, "DelTreasureWareJob", "DelTreasureWareJob", DelTreasureWareJob.class, "5 0 * * * ? *");// 整点处理, 有五秒的偏移, [21:00:5, 22:00:5, 23:00:5, 00:00:5]

        // 定时刷新土匪
//        List<Integer> banditRefreshTime = Constant.BANDIT_REFRESH_TIME;
//        addJob(sched, "RefreshBanditJob", "RefreshBanditJob", RefreshBanditJob.class, "0 " + banditRefreshTime.get(1) + " " + banditRefreshTime.get(0) + " * * * *");

        initBerlinJob();
        initLightningWarJob();
        // addJob(sched, jobName, jobGroupName, jobClass, startAt);
        loadOnConfigChange("load");
        initRebellionSched();// 初始化匪军叛乱
        initBerlinLightningWar(); // 初始化柏林闪电战
        initAirship();
        // 移除有期限皮肤
        processPlayerTimerDressUp();
        // 纽约争霸预显示
        NewYorkWarJob.initPreNewYorkWarJob();
        initCrossInfo(); // 跨服初始化

        initSandTableContest();

        DataResource.ac.getBean(SeasonService.class).initSchedule(sched);
        DataResource.getBean(SeasonTalentService.class).initSchedule(sched);
        DataResource.getBean(CrossGamePlayService.class).initSchedule(sched);
        DataResource.ac.getBean(DominateWorldMapService.class).initSchedule();
    }

    public void initSandTableContest() {
        try {
            SandTableContest sandTableContest = DataResource.ac.getBean(GlobalDataManager.class).getGameGlobal().getSandTableContest();
            sandTableContest.setPreviewCron(Constant.SAND_TABLE_PREVIEW);
            sandTableContest.setOpenBeginCron(Constant.SAND_TABLE_OPEN_END.get(0));
            sandTableContest.setOpenEndCron(Constant.SAND_TABLE_OPEN_END.get(1));

            if (!StringUtils.isEmpty(Constant.SAND_TABLE_PREVIEW))
                addJob(sched, SandTableJob.name_preview, SandTableJob.groupName, SandTablePreviewJob.class, Constant.SAND_TABLE_PREVIEW);
            if (!StringUtils.isEmpty(Constant.SAND_TABLE_OPEN_END) && !StringUtils.isEmpty(Constant.SAND_TABLE_OPEN_END.get(0)))
                addJob(sched, SandTableJob.name_open, SandTableJob.groupName, SandTableOpenEndRoundJob.class, Constant.SAND_TABLE_OPEN_END.get(0));
            if (!StringUtils.isEmpty(Constant.SAND_TABLE_OPEN_END) && !StringUtils.isEmpty(Constant.SAND_TABLE_OPEN_END.get(1)))
                addJob(sched, SandTableJob.name_end, SandTableJob.groupName, SandTableOpenEndRoundJob.class, Constant.SAND_TABLE_OPEN_END.get(1));

//            DataResource.ac.getBean(SandTableContestService.class).addJob(sandTableContest);
            int now = TimeHelper.getCurrentSecond();
            Map<Integer, SandTableGroup> groupMap = sandTableContest.getMatchGroup();
            groupMap.entrySet().forEach(o -> {
                int round = o.getKey();
                SandTableGroup group = o.getValue();
                if (now <= group.beginTime) {
                    QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), SandTableJob.name_round + round, SandTableJob.groupName, SandTableOpenEndRoundJob.class, TimeHelper.getDateByStamp(group.beginTime));
                }
            });

            int state = sandTableContest.state();
            LogUtil.error("初始化沙盘演武定时器完成, Current State = " + state, ", SandTableContest=" + JSON.toJSONString(sandTableContest));
        } catch (Exception e) {
            LogUtil.error("初始化沙盘演武定时器发生异常，", e);
        }
    }

    private void initCrossInfo() {
        DataResource.ac.getBean(CrossDataService.class).initAndRefresh(true);
    }

    /**
     * 移除有期限皮肤
     */
    private void processPlayerTimerDressUp() {
        DataResource.ac.getBean(DressUpDataManager.class).processPlayerTimerDressUp();
    }

    /**
     * 飞艇
     */
    private void initAirship() {
        // AirshipService service = DataResource.ac.getBean(AirshipService.class);
        // service.triggerInitAirship();
        // QuartzHelper.addJobForCirc(sched, "AirShip", "runSec", AirShipJob.class, 1);// 跑秒定时器
        // addJob(sched, "AirShipJob", "RefreshAirShip", AirShipJob.class, "0/1 * * * * ? "); // 跑秒定时器
        addJob(sched, "AirShipJob", "RefreshAirShip", AirShipJob.class, "0/1 * * * * ?"); // 跑秒定时器
    }

    /**
     * 初始化柏林闪电战
     */
    private void initBerlinLightningWar() {
        CounterAtkService service = DataResource.ac.getBean(CounterAtkService.class);
        service.initCounterAtk();
    }

    /**
     * 初始化匪军叛乱
     */
    private void initRebellionSched() {
        RebelService service = DataResource.ac.getBean(RebelService.class);
        service.initRebellion();
    }

    /**
     * 初始化柏林会战定时器
     */
    public void initBerlinJob() {
        BerlinWarService berlinWarService = DataResource.ac.getBean(BerlinWarService.class);
        berlinWarService.initBerlinJob();
    }


    /**
     * 移除柏林会战本周期定时器
     */
    public void removeBerlinJob() {
        // 移除柏林会战的战斗逻辑和计算逻辑定时器
        QuartzHelper.removeJob(sched, "BerlinWarJob", DefultJob.DEFULT_GROUP);
        QuartzHelper.removeJob(sched, "battlefrontWarLogic", DefultJob.DEFULT_GROUP);
        QuartzHelper.removeJob(sched, "BerlinWarColsingJob", DefultJob.DEFULT_GROUP);
        /*GlobalDataManager globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            LogUtil.error("柏林会战， BerlinWar对象未初始化");
            return;
        }
        // 移除前线阵地定时器
        berlinWar.getBattlefronts().values().forEach(battleFront -> {
            StringBuffer sb = new StringBuffer("battlefront_");
            String jobName = sb.append(battleFront.getCityId()).toString();
            QuartzHelper.removeJob(sched, jobName, DefultJob.DEFULT_GROUP);
        });*/
    }

    /**
     * 移除反攻德意志的定时器
     */
    public void removeCounterAtkJob() {
        QuartzHelper.removeJob(sched, WorldConstant.BL_LW_START_CALLBACK_NAME, DefultJob.DEFULT_GROUP);
        QuartzHelper.removeJob(sched, WorldConstant.BL_LW_END_CALLBACK_NAME, DefultJob.DEFULT_GROUP);
    }

    /*    *//**
     * 添加前线阵地定时任务
     *
     * @param battlefront
     *//*
    @Deprecated
    public void addBattlefrontJob(BerlinCityInfo battlefront) {
        Date now = new Date();
        BerlinWarService service = DataResource.ac.getBean(BerlinWarService.class);
        List<String> berlinCronInfo = WorldConstant.getBerlinCronInfo(WorldConstant.BERLIN_CRON_DATE,
                WorldConstant.BERLIN_BEGIN_CRON);
        if (!CheckNull.isEmpty(berlinCronInfo)) {
            String endTime = berlinCronInfo.get(1);
            Date endDate = DateHelper.afterStringTime(now, endTime);
            StringBuffer sb = new StringBuffer("battlefront_");
            // 进攻间隔
            int intervalInSeconds = WorldConstant.BATTLE_FRONT_ATK_CD;
            if (!CheckNull.isNull(battlefront)) {
                String jobName = sb.append(battlefront.getCityId()).toString();
                // 更新下次进攻时间
                battlefront.setNextAtkTime(TimeHelper.afterSecondTime(now, intervalInSeconds));
                addOrModifyDefultJob(DefultJob.createDefult(jobName), (job) -> {
                    service.battlefrontTimeLogic(battlefront.getCityId());
                }, now, endDate, intervalInSeconds);
            }
        }
    }*/

    /**
     * 初始化闪电战定时器,以及boss
     */
    public void initLightningWarJob() {
        // 获取闪电战s_act_lightningwar表第一条数据
        StaticLightningWar lightningWar = StaticLightningWarDataMgr.getLightningWar();
        if (CheckNull.isNull(lightningWar)) {
            return;
        }
        Date now = new Date();
        WorldDataManager worldDataManager = DataResource.ac.getBean(WorldDataManager.class);
        LightningWarService service = DataResource.ac.getBean(LightningWarService.class);
        // 初始化闪电战boss
        worldDataManager.initLightningWarBoss(lightningWar);
        ActivityBase actBase = StaticActivityDataMgr.getActivityByTypeIgnoreStep(ActivityConst.ACT_LIGHTNING_WAR);
        if (!CheckNull.isNull(actBase)) {
            Date beginDate = DateHelper.afterStringTime(actBase.getBeginTime(), lightningWar.getStartTime());
            Date endDate = DateHelper.afterStringTime(actBase.getEndTime(), lightningWar.getEndTime());
            Date chatDate = DateHelper.afterStringTime(actBase.getBeginTime(), lightningWar.getChatTime());
            Date announceDate = DateHelper.afterStringTime(actBase.getBeginTime(), lightningWar.getAnnounceTime());// 活动预告时间
            final int conTime = lightningWar.getIntervalTime() + lightningWar.getBattleTime();
            if (!DateHelper.isAfterTime(now, chatDate)) {
                addOrModifyDefultJob(DefultJob.createDefult("LightningWarActBegin"), (job) -> {
                    service.sendchat(ActivityConst.ACT_BEGIN, 0);
                }, chatDate);
            }
            if (!DateHelper.isAfterTime(now, endDate)) {
                addOrModifyDefultJob(DefultJob.createDefult("LightningWarActEnd"), (job) -> {
                    service.sendchat(ActivityConst.ACT_END, 0);
                }, endDate);
            }
            if (!DateHelper.isAfterTime(now, endDate)) {
                addOrModifyDefultJob(DefultJob.createDefult("LightningWarAtkCnt"), (job) -> {
                    SimpleTrigger trigger = (SimpleTrigger) job.getContext().getTrigger();
                    int cnt = trigger.getTimesTriggered();
                    LogUtil.error("执行了sendchat() cnt:", cnt);
                    service.sendchat(ActivityConst.ATTACK_CNT, cnt);
                }, beginDate, endDate, conTime);
            }
            if (!DateHelper.isAfterTime(now, announceDate)) {// 满足当前时间小于任务起始时间 才执行
                addOrModifyDefultJob(DefultJob.createDefult("LightningWarActAnnounce"), (job) -> {
                    SimpleTrigger trigger = (SimpleTrigger) job.getContext().getTrigger();
                    int ann = trigger.getTimesTriggered();// 获取当前执行次数
                    LogUtil.error("执行了sendchat() ann:", ann);
                    // 计算剩余时间 任务结束时间减当前时间
                    Long remainingTime = ((beginDate.getTime() / TimeHelper.SECOND_MS)
                            - (new Date().getTime() / TimeHelper.SECOND_MS)) / TimeHelper.MINUTE_S;
                    service.sendchat(ActivityConst.ACT_ANN, remainingTime.intValue());
                }, announceDate, beginDate, lightningWar.getRepeatTime());
            }
        }
    }

    /**
     * 当配置表数据发生改变时加载
     */
    public void loadOnConfigChange(Object... objects) {
        loadActMailSchedule(objects);
        loadWorldRule();
        loadActRankAwardScheule();
        loadActBeginScheule();
        loadMedalGoodsSchedule();
        loadFunctionPlanSchedule();
    }

    /**
     * 加在所有plan定时器
     */
    private void loadFunctionPlanSchedule() {
        DataResource.ac.getBean(DrawCardPlanTemplateService.class).loadFunctionPlanJob();
    }

    /**
     * 活动开启时间添加
     */
    private void loadActBeginScheule() {
        List<ActivityBase> actBaseList = StaticActivityDataMgr.getActivityList();
        Date nowDate = new Date();
        for (ActivityBase ab : actBaseList) {
            StaticActivityPlan plan = ab.getPlan();
            int activityType = plan.getActivityType();
            int activityId = plan.getActivityId();
            int keyId = plan.getKeyId();

            Date jobTime = ab.getBeginTime();
            if (CheckNull.isNull(ab.getBeginTime()) || CheckNull.isNull(ab.getEndTime()) || CheckNull.isNull(jobTime)) {
                continue;
            }
            if (activityType == ActivityConst.ACT_VISIT_ALTAR && DateHelper.isInTime(nowDate, plan.getBeginTime(), plan.getEndTime())) {
                // 在活动开放过程中, 偏移30秒执行
                jobTime = TimeHelper.secondToDate(TimeHelper.dateToSecond(nowDate) + 30);
            }
            if (jobTime.getTime() <= nowDate.getTime()) {
                continue;
            }
            StringBuilder name = new StringBuilder();
            name.append(activityType).append("_").append(activityId).append("_").append(keyId);
            QuartzHelper.addJob(sched, name.toString(), "ActBegin", ActBeginJob.class, jobTime);
            LogUtil.debug("----------活动开启时间 :", name.toString(), ", Date,", DateHelper.formatDateMiniTime(jobTime),
                    "-------------------");
        }
    }

    public String getActBeginJobName(int actType, int actId, int keyId) {
        return new StringBuilder().append(actType).append("_").append(actId).append("_").append(keyId).toString();
    }

    /**
     * 流寇和矿点定时器加载
     */
    private void loadWorldRule() {
        List<StaticWorldRule> ruleList = StaticWorldDataMgr.getWroldRuleList();
        for (StaticWorldRule rule : ruleList) {
            StringBuilder name = new StringBuilder();
            name.append(rule.getType()).append("_").append(rule.getRule());
            QuartzHelper.addJob(sched, name.toString(), "worldRule", WorldRuleJob.class, rule.getCron());
            LogUtil.world("----------添加流寇和矿点定时器  :", name.toString(), ", cron:", rule.getCron(), "-------------------");
        }
    }

    /**
     * 初始化邮件定时器
     */
    private void loadActMailSchedule(Object... objects) {
        // 移除之前的所有定时器
        List<ActivityBase> actBaseList = StaticActivityDataMgr.getActivityList();
        Date nowDate = new Date();
        ActivityTemplateService activityTemplateService = DataResource.ac.getBean(ActivityTemplateService.class);
        for (ActivityBase ab : actBaseList) {
            StaticActivityPlan plan = ab.getPlan();
            if (ab.getActivityType() == ActivityConst.ACT_SUPPLY_DORP) {  //空降补给结束不发，每天会检测
                continue;
            }
            Date jobTime = ab.getSendMailTime();// 发送邮件的时间

            // 用ActivityBase中的时间是可适应两套时间配置规则的
            if (CheckNull.isNull(ab.getBeginTime()) || CheckNull.isNull(ab.getEndTime()) || CheckNull.isNull(jobTime)) {
                continue;
            }
            if (ab.getActivityType() == ActivityConst.ACT_CHRISTMAS || ab.getActivityType() == ActivityConst.ACT_REPAIR_CASTLE) {
                ActivityChristmasService.addJob(sched, ab, nowDate);
            } else if (ab.getActivityType() == ActivityConst.ACT_DIAOCHAN || ab.getActivityType() == ActivityConst.ACT_SEASON_HERO) {
                ActivityDiaoChanService.addScheduleJob(ab, nowDate, sched);
            } else if (ab.getActivityType() == ActivityConst.ACT_AUCTION) {
                DataResource.getBean(ActivityAuctionService.class).addSchedule(ab, nowDate, sched, objects);
            }
            //新的常规活动按照新的方式处理
            else if (Objects.nonNull(activityTemplateService.getActivityService(ab.getActivityType()))) {
                this.addActivityJob(ab, nowDate);
            } else {
                if (jobTime.getTime() <= nowDate.getTime()) {
                    continue;
                }

                // 先移除之前的定时器
                QuartzHelper.removeJob(sched, String.valueOf(plan.getActivityType()), "actMail");

                StringBuilder name = new StringBuilder();
                name.append(plan.getActivityType()).append("_").append(plan.getActivityId()).append("_").append(plan.getKeyId());
                // 加入定时器
                if (ab.getActivityType() == ActivityConst.FAMOUS_GENERAL_TURNPLATE) {
                    QuartzHelper.addJob(sched, name.toString(), "actMail", ActMailJob.class, ab.getEndTime());
                } else {
                    QuartzHelper.addJob(sched, name.toString(), "actMail", ActMailJob.class, jobTime);
                }

                LogUtil.debug("----------添加活动定时任务 :", plan.getName(), name.toString(), ", Date,", DateHelper.formatDateMiniTime(jobTime), "-------------------");
            }
        }
    }


    private void addActivityJob(ActivityBase activityBase, Date now) {
        String jobName = activityBase.getActivityType() + "_" + activityBase.getActivityId() + "_" + activityBase.getPlan().getKeyId();
        if (activityBase.getEndTime().after(now)) {
            QuartzHelper.removeJob(sched, jobName, ActJob.NAME_END);
            QuartzHelper.addJob(sched, jobName, ActJob.NAME_END, ActEndJob.class, activityBase.getEndTime());
        }
        if (Objects.nonNull(activityBase.getDisplayTime()) && activityBase.getDisplayTime().after(now)) {
            QuartzHelper.removeJob(sched, jobName, ActJob.NAME_OVER);
            QuartzHelper.addJob(sched, jobName, ActJob.NAME_OVER, ActOverJob.class, activityBase.getDisplayTime());
        }
        ActivityTemplateService activityTemplateService = DataResource.getBean(ActivityTemplateService.class);
        activityTemplateService.addOtherJob(activityBase, now);
    }

    /**
     * @return void
     * @Title: loadMedalGoodsSchedule
     * @Description: 初始化勋章商品定时器
     */
    private void loadMedalGoodsSchedule() {
        List<Integer> list = MedalConst.MEDAL_GOODS_REFRESH_EVERYDAY;
        for (int time : list) {
            addJob(sched, "MedalGoodsRefreshJob" + time, "MedalGoods", MedalGoodsRefreshJob.class,
                    "0 0 " + time + " * * ?");// 每日time点执行一次
        }
    }

    /**
     * 排行活动领奖时间处理
     */
    private void loadActRankAwardScheule() {
        // 移除之前的所有定时器
        List<ActivityBase> actBaseList = StaticActivityDataMgr.getActivityList();
        Date nowDate = new Date();
        for (ActivityBase ab : actBaseList) {
            StaticActivityPlan plan = ab.getPlan();
            int actType = plan.getActivityType();
            if (!StaticActivityDataMgr.isActTypeRank(actType)) {
                continue;
            }
            if (plan.getBeginTime() == null || plan.getEndTime() == null || plan.getAwardTime() == null) {
                continue;
            }
            if (plan.getAwardTime().getTime() <= nowDate.getTime()) {
                continue;
            }
            StringBuffer name = new StringBuffer();
            name.append(actType).append("_").append(plan.getActivityId()).append("_").append(plan.getKeyId());
            QuartzHelper.addJob(sched, name.toString(), "actAward", ActRankAwardJob.class, plan.getAwardTime());
            LogUtil.debug("----------添加排行活动领取奖励任务 :", name.toString(), ", Date,",
                    DateHelper.formatDateMiniTime(plan.getAwardTime()), "-------------------");
        }
    }

    /**
     * 添加执行一次的任务
     *
     * @param job
     * @param run
     * @param startAt
     * @return
     */
    public boolean addOrModifyDefultJob(DefultJob job, QuartzCallBack run, Date startAt) {
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getName(), job.getGroup());
        JobKey jobKey = JobKey.jobKey(job.getName(), job.getGroup());
        try {
            Trigger trigger = sched.getTrigger(triggerKey);
            if (trigger != null) {// 移除定时任务
                QuartzHelper.removeJob(sched, job.getName(), job.getGroup());
            }
            JobDetail detail = JobBuilder.newJob(job.getClass()).withIdentity(jobKey).build();
            detail.getJobDataMap().put(DefultJob.RUN, run);
            trigger = TriggerBuilder.newTrigger().withIdentity(job.getName(), job.getGroup()).startAt(startAt).build();
            sched.scheduleJob(detail, trigger);
            LogUtil.debug("----------添加定时器成功 :", job, ", Date,", DateHelper.formatDateMiniTime(startAt),
                    "---------------");
            return true;
        } catch (SchedulerException ex) {
            LogUtil.error("无法添加延时任务：", job.toString(), ":", ex);
        }
        return false;
    }

    /**
     * 延迟执行
     *
     * @param job
     * @param run
     * @param delaySeconds
     * @return
     */
    public boolean addOrModifyDefultJob(DefultJob job, QuartzCallBack run, int delaySeconds) {
        return addOrModifyDefultJob(job, run, DateBuilder.futureDate(delaySeconds, IntervalUnit.SECOND));
    }

    /**
     * 周期性执行
     *
     * @param job
     * @param run
     * @param startAt           开始时间
     * @param endAt             结束时间
     * @param intervalInSeconds 间隔时间
     * @return
     */
    public boolean addOrModifyDefultJob(DefultJob job, QuartzCallBack run, Date startAt, Date endAt,
                                        int intervalInSeconds) {
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getName(), job.getGroup());
        JobKey jobKey = JobKey.jobKey(job.getName(), job.getGroup());
        try {
            Trigger trigger = sched.getTrigger(triggerKey);
            if (trigger != null) {// 移除定时任务
                QuartzHelper.removeJob(sched, job.getName(), job.getGroup());
            }
            JobDetail detail = JobBuilder.newJob(job.getClass()).withIdentity(jobKey).build();
            detail.getJobDataMap().put(DefultJob.RUN, run);
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getName(), job.getGroup()).withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(intervalInSeconds).repeatForever())
                    .startAt(startAt).endAt(endAt).build();
            sched.scheduleJob(detail, trigger);
            LogUtil.debug("----------添加定时器成功 :", job, DateHelper.formatDateMiniTime(startAt), "---------------",
                    DateHelper.formatDateMiniTime(endAt), "intervalInSeconds: ", intervalInSeconds);
            return true;
        } catch (SchedulerException ex) {
            LogUtil.error("无法添加延时任务：", job.toString(), ":", ex);
        }
        return false;
    }

    /**
     * 周期性执行
     * <p>
     * SimpleTrigger trigger = (SimpleTrigger) context.getTrigger();
     * <p>
     * int cnt = trigger.getTimesTriggered(); 获取次数
     *
     * @param job
     * @param run
     * @param startAt
     * @param intervalInSeconds
     * @param repeatCount
     * @return
     */
    public boolean addOrModifyDefultJob(DefultJob job, QuartzCallBack run, Date startAt, int intervalInSeconds,
                                        int repeatCount) {
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getName(), job.getGroup());
        JobKey jobKey = JobKey.jobKey(job.getName(), job.getGroup());
        try {
            Trigger trigger = sched.getTrigger(triggerKey);
            if (trigger != null) {// 移除定时任务
                QuartzHelper.removeJob(sched, job.getName(), job.getGroup());
            }
            JobDetail detail = JobBuilder.newJob(job.getClass()).withIdentity(jobKey).build();
            detail.getJobDataMap().put(DefultJob.RUN, run);
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getName(), job.getGroup()).withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(intervalInSeconds).withRepeatCount(repeatCount))
                    .startAt(startAt).build();
            sched.scheduleJob(detail, trigger);
            LogUtil.debug("----------添加定时器成功 :", job, "---------------");
            return true;
        } catch (SchedulerException ex) {
            LogUtil.error("无法添加延时任务：", job.toString(), ":", ex);
        }
        return false;
    }

    /**
     * 周期性执行
     *
     * @param job
     * @param run
     * @param intervalInSeconds
     * @param repeatCount
     * @param delaySeconds
     * @return
     */
    public boolean addOrModifyDefultJob(DefultJob job, QuartzCallBack run, int delaySeconds, int intervalInSeconds,
                                        int repeatCount) {
        return addOrModifyDefultJob(job, run, DateBuilder.futureDate(delaySeconds, IntervalUnit.SECOND),
                intervalInSeconds, repeatCount);
    }

    public Scheduler getSched() {
        return sched;
    }

}
