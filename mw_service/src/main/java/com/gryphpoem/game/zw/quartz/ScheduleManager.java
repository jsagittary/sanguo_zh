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
import com.gryphpoem.game.zw.service.*;
import com.gryphpoem.game.zw.service.activity.*;
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
 * @Description ????????????????????????
 * @date 2017???8???17???
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

    public JobDetail getJobDetail(String group,String name){
        try {
            return sched.getJobDetail(JobKey.jobKey(name,group));
        } catch (SchedulerException e) {
            LogUtil.error("??????JobDetail????????????",e);
            return null;
        }
    }

    /**
     * ?????????quartz?????????
     */
    public void initRegisterJob() {
        // addJob(sched, "test1", "testG", PushTestJob.class, "0/5 * * * * ?");// ???5??????????????????
//        addJob(sched, "Monitor", "GameServerQos", ServerMonitorJob.class, "0 */1 * * * ?");
        addJob(sched, "HonorRankSettleJob", "Camp", HonorRankSettleJob.class, "0 59 23 ? * THU");

        // ?????????????????????????????????
        addJob(sched, "SyncActPowerGiveJob", "Act", SyncActPowerGiveJob.class, "0 0 12 * * ?");// ?????????????????? ??????12????????????
        addJob(sched, "SyncActPowerGiveJob2", "Act", SyncActPowerGiveJob.class, "0 0 18 * * ?");// ??????18????????????
        addJob(sched, "CityLordRewardJob", "CityService", CityLordRewardJob.class, "0 0 21 * * ?");// ??????21???????????????
        addJob(sched, "AcrossTheDayJob", "AcrossTheDay", AcrossTheDayJob.class, "1 0 0 * * ?");// ????????????
        addJob(sched, "AcrossTheHourAfterJob", "AcrossTheHourAfter", AcrossTheHourAfterJob.class, "5 0 * * * ? *");// ????????????, ??????????????????, [21:00:5, 22:00:5, 23:00:5, 00:00:5]
        addJob(sched, "AcrossTheDayBeforeJob", "AcrossTheDayBefore", AcrossTheDayBeforeJob.class, "50 59 23 * * ?");// ??????????????????
        addJob(sched, "WesternPointSpecialTraining", "MedalSpecial", WesternPointSpecialTrainingJob.class,
                "0 0 5 * * ?");// ??????5????????? ????????????-???????????? ??????
        addJob(sched, "RefreshPartyJob", "RefreshParty", RefreshPartyJob.class, "0 0 0/6 * * ? "); // ???6??????????????????, [0:00,
        // 6:00, 12:00,
        // 18:00]

        // ????????????2???3???4???6???7???8 ???12???21???????????????????????????
        addJob(sched, "WorldScheduleRankJob", "WorldScheduleRankJob", WorldScheduleRankJob.class, "0 0 12,21 * * ?");

        // ???????????? ????????????????????????
        addJob(sched, "WorldWarDailyJob", "WorldWar", WorldWarDailyJob.class, "45 59 23 * * ?");
        // ???????????? ??????12??????21????????????????????????????????????????????????????????????????????????
        addJob(sched, "WorldWarDailyAttackCityCampIntegralJob", "WorldWar",
                WorldWarDailyAttackCityCampIntegralJob.class, "0 0 12,21 * * ?");
        // ???????????? ????????????????????????
        addJob(sched, "WorldWarWeekJob", "WorldWarWeekJob", WorldWarWeekJob.class, "45 59 23 ? * 4");

        // ????????????????????????
        addJob(sched, "DelTreasureWareJob", "DelTreasureWareJob", DelTreasureWareJob.class, "5 0 * * * ? *");// ????????????, ??????????????????, [21:00:5, 22:00:5, 23:00:5, 00:00:5]

        initBerlinJob();
        initLightningWarJob();
        // addJob(sched, jobName, jobGroupName, jobClass, startAt);
        loadOnConfigChange("load");
        initRebellionSched();// ?????????????????????
        initBerlinLightningWar(); // ????????????????????????
        initAirship();
        // ?????????????????????
        processPlayerTimerDressUp();
        // ?????????????????????
        NewYorkWarJob.initPreNewYorkWarJob();
        initCrossInfo(); // ???????????????

        initSandTableContest();

        DataResource.ac.getBean(SeasonService.class).initSchedule(sched);
        DataResource.getBean(SeasonTalentService.class).initSchedule(sched);
        DataResource.getBean(CrossGamePlayService.class).initSchedule(sched);
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
            LogUtil.error("????????????????????????????????????, Current State = " + state, ", SandTableContest=" + JSON.toJSONString(sandTableContest));
        } catch (Exception e) {
            LogUtil.error("?????????????????????????????????????????????", e);
        }
    }

    private void initCrossInfo() {
        DataResource.ac.getBean(CrossDataService.class).initAndRefresh(true);
    }

    /**
     * ?????????????????????
     */
    private void processPlayerTimerDressUp() {
        DataResource.ac.getBean(DressUpDataManager.class).processPlayerTimerDressUp();
    }

    /**
     * ??????
     */
    private void initAirship() {
        // AirshipService service = DataResource.ac.getBean(AirshipService.class);
        // service.triggerInitAirship();
        // QuartzHelper.addJobForCirc(sched, "AirShip", "runSec", AirShipJob.class, 1);// ???????????????
        // addJob(sched, "AirShipJob", "RefreshAirShip", AirShipJob.class, "0/1 * * * * ? "); // ???????????????
        addJob(sched, "AirShipJob", "RefreshAirShip", AirShipJob.class, "0/1 * * * * ?"); // ???????????????
    }

    /**
     * ????????????????????????
     */
    private void initBerlinLightningWar() {
        CounterAtkService service = DataResource.ac.getBean(CounterAtkService.class);
        service.initCounterAtk();
    }

    /**
     * ?????????????????????
     */
    private void initRebellionSched() {
        RebelService service = DataResource.ac.getBean(RebelService.class);
        service.initRebellion();
    }

    /**
     * ??????????????????????????????
     */
    public void initBerlinJob() {
        BerlinWarService berlinWarService = DataResource.ac.getBean(BerlinWarService.class);
        berlinWarService.initBerlinJob();
    }


    /**
     * ????????????????????????????????????
     */
    public void removeBerlinJob() {
        // ?????????????????????????????????????????????????????????
        QuartzHelper.removeJob(sched, "BerlinWarJob", DefultJob.DEFULT_GROUP);
        QuartzHelper.removeJob(sched, "battlefrontWarLogic", DefultJob.DEFULT_GROUP);
        QuartzHelper.removeJob(sched, "BerlinWarColsingJob", DefultJob.DEFULT_GROUP);
        /*GlobalDataManager globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            LogUtil.error("??????????????? BerlinWar??????????????????");
            return;
        }
        // ???????????????????????????
        berlinWar.getBattlefronts().values().forEach(battleFront -> {
            StringBuffer sb = new StringBuffer("battlefront_");
            String jobName = sb.append(battleFront.getCityId()).toString();
            QuartzHelper.removeJob(sched, jobName, DefultJob.DEFULT_GROUP);
        });*/
    }

    /**
     * ?????????????????????????????????
     */
    public void removeCounterAtkJob() {
        QuartzHelper.removeJob(sched, WorldConstant.BL_LW_START_CALLBACK_NAME, DefultJob.DEFULT_GROUP);
        QuartzHelper.removeJob(sched, WorldConstant.BL_LW_END_CALLBACK_NAME, DefultJob.DEFULT_GROUP);
    }

/*    *//**
     * ??????????????????????????????
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
            // ????????????
            int intervalInSeconds = WorldConstant.BATTLE_FRONT_ATK_CD;
            if (!CheckNull.isNull(battlefront)) {
                String jobName = sb.append(battlefront.getCityId()).toString();
                // ????????????????????????
                battlefront.setNextAtkTime(TimeHelper.afterSecondTime(now, intervalInSeconds));
                addOrModifyDefultJob(DefultJob.createDefult(jobName), (job) -> {
                    service.battlefrontTimeLogic(battlefront.getCityId());
                }, now, endDate, intervalInSeconds);
            }
        }
    }*/

    /**
     * ???????????????????????????,??????boss
     */
    public void initLightningWarJob() {
        // ???????????????s_act_lightningwar??????????????????
        StaticLightningWar lightningWar = StaticLightningWarDataMgr.getLightningWar();
        if (CheckNull.isNull(lightningWar)) {
            return;
        }
        Date now = new Date();
        WorldDataManager worldDataManager = DataResource.ac.getBean(WorldDataManager.class);
        LightningWarService service = DataResource.ac.getBean(LightningWarService.class);
        // ??????????????????boss
        worldDataManager.initLightningWarBoss(lightningWar);
        ActivityBase actBase = StaticActivityDataMgr.getActivityByTypeIgnoreStep(ActivityConst.ACT_LIGHTNING_WAR);
        if (!CheckNull.isNull(actBase)) {
            Date beginDate = DateHelper.afterStringTime(actBase.getBeginTime(), lightningWar.getStartTime());
            Date endDate = DateHelper.afterStringTime(actBase.getEndTime(), lightningWar.getEndTime());
            Date chatDate = DateHelper.afterStringTime(actBase.getBeginTime(), lightningWar.getChatTime());
            Date announceDate = DateHelper.afterStringTime(actBase.getBeginTime(), lightningWar.getAnnounceTime());// ??????????????????
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
                    LogUtil.error("?????????sendchat() cnt:", cnt);
                    service.sendchat(ActivityConst.ATTACK_CNT, cnt);
                }, beginDate, endDate, conTime);
            }
            if (!DateHelper.isAfterTime(now, announceDate)) {// ?????????????????????????????????????????? ?????????
                addOrModifyDefultJob(DefultJob.createDefult("LightningWarActAnnounce"), (job) -> {
                    SimpleTrigger trigger = (SimpleTrigger) job.getContext().getTrigger();
                    int ann = trigger.getTimesTriggered();// ????????????????????????
                    LogUtil.error("?????????sendchat() ann:", ann);
                    // ?????????????????? ?????????????????????????????????
                    Long remainingTime = ((beginDate.getTime() / TimeHelper.SECOND_MS)
                            - (new Date().getTime() / TimeHelper.SECOND_MS)) / TimeHelper.MINUTE_S;
                    service.sendchat(ActivityConst.ACT_ANN, remainingTime.intValue());
                }, announceDate, beginDate, lightningWar.getRepeatTime());
            }
        }
    }

    /**
     * ???????????????????????????????????????
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
     * ????????????plan?????????
     */
    private void loadFunctionPlanSchedule() {
        DataResource.ac.getBean(DrawCardPlanTemplateService.class).loadFunctionPlanJob();
    }

    /**
     * ????????????????????????
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
                // ????????????????????????, ??????30?????????
                jobTime = TimeHelper.secondToDate(TimeHelper.dateToSecond(nowDate) + 30);
            }
            if (jobTime.getTime() <= nowDate.getTime()) {
                continue;
            }
            StringBuilder name = new StringBuilder();
            name.append(activityType).append("_").append(activityId).append("_").append(keyId);
            QuartzHelper.addJob(sched, name.toString(), "ActBegin", ActBeginJob.class, jobTime);
            LogUtil.debug("----------?????????????????? :", name.toString(), ", Date,", DateHelper.formatDateMiniTime(jobTime),
                    "-------------------");
        }
    }

    public String getActBeginJobName(int actType,int actId,int keyId){
        return new StringBuilder().append(actType).append("_").append(actId).append("_").append(keyId).toString();
    }

    /**
     * ??????????????????????????????
     */
    private void loadWorldRule() {
        List<StaticWorldRule> ruleList = StaticWorldDataMgr.getWroldRuleList();
        for (StaticWorldRule rule : ruleList) {
            StringBuilder name = new StringBuilder();
            name.append(rule.getType()).append("_").append(rule.getRule());
            QuartzHelper.addJob(sched, name.toString(), "worldRule", WorldRuleJob.class, rule.getCron());
            LogUtil.world("----------??????????????????????????????  :", name.toString(), ", cron:", rule.getCron(), "-------------------");
        }
    }

    /**
     * ????????????????????????
     */
    private void loadActMailSchedule(Object... objects) {
        // ??????????????????????????????
        List<ActivityBase> actBaseList = StaticActivityDataMgr.getActivityList();
        Date nowDate = new Date();
        ActivityTemplateService activityTemplateService = DataResource.ac.getBean(ActivityTemplateService.class);
        for (ActivityBase ab : actBaseList) {
            StaticActivityPlan plan = ab.getPlan();
            if (ab.getActivityType() == ActivityConst.ACT_SUPPLY_DORP) {  //??????????????????????????????????????????
                continue;
            }
            Date jobTime = ab.getSendMailTime();// ?????????????????????

            // ???ActivityBase???????????????????????????????????????????????????
            if (CheckNull.isNull(ab.getBeginTime()) || CheckNull.isNull(ab.getEndTime()) || CheckNull.isNull(jobTime)) {
                continue;
            }
            if (ab.getActivityType() == ActivityConst.ACT_CHRISTMAS || ab.getActivityType() == ActivityConst.ACT_REPAIR_CASTLE) {
                ActivityChristmasService.addJob(sched,ab,nowDate);
            } else if(ab.getActivityType() == ActivityConst.ACT_DIAOCHAN || ab.getActivityType() == ActivityConst.ACT_SEASON_HERO){
                ActivityDiaoChanService.addScheduleJob(ab,nowDate,sched);
            } else if (ab.getActivityType() == ActivityConst.ACT_AUCTION) {
                DataResource.getBean(ActivityAuctionService.class).addSchedule(ab, nowDate, sched, objects);
            }
            //??????????????????????????????????????????
            else if(Objects.nonNull(activityTemplateService.getActivityService(ab.getActivityType()))){
                this.addActivityJob(ab,nowDate);
            }else {
                if (jobTime.getTime() <= nowDate.getTime()) {
                    continue;
                }

                // ???????????????????????????
                QuartzHelper.removeJob(sched, String.valueOf(plan.getActivityType()), "actMail");

                StringBuilder name = new StringBuilder();
                name.append(plan.getActivityType()).append("_").append(plan.getActivityId()).append("_").append(plan.getKeyId());
                // ???????????????
                if (ab.getActivityType() == ActivityConst.FAMOUS_GENERAL_TURNPLATE) {
                    QuartzHelper.addJob(sched, name.toString(), "actMail", ActMailJob.class, ab.getEndTime());
                } else {
                    QuartzHelper.addJob(sched, name.toString(), "actMail", ActMailJob.class, jobTime);
                }

                LogUtil.debug("----------???????????????????????? :", plan.getName(), name.toString(), ", Date,", DateHelper.formatDateMiniTime(jobTime), "-------------------");
            }
        }
    }



    private void addActivityJob(ActivityBase activityBase,Date now){
        String jobName = activityBase.getActivityType() + "_" + activityBase.getActivityId() + "_" + activityBase.getPlan().getKeyId();
        if(activityBase.getEndTime().after(now)){
            QuartzHelper.removeJob(sched,jobName,ActJob.NAME_END);
            QuartzHelper.addJob(sched,jobName,ActJob.NAME_END, ActEndJob.class,activityBase.getEndTime());
        }
        if(Objects.nonNull(activityBase.getDisplayTime()) && activityBase.getDisplayTime().after(now)){
            QuartzHelper.removeJob(sched,jobName,ActJob.NAME_OVER);
            QuartzHelper.addJob(sched,jobName,ActJob.NAME_OVER, ActOverJob.class,activityBase.getDisplayTime());
        }
        ActivityTemplateService activityTemplateService = DataResource.getBean(ActivityTemplateService.class);
        activityTemplateService.addOtherJob(activityBase,now);
    }

    /**
     * @return void
     * @Title: loadMedalGoodsSchedule
     * @Description: ??????????????????????????????
     */
    private void loadMedalGoodsSchedule() {
        List<Integer> list = MedalConst.MEDAL_GOODS_REFRESH_EVERYDAY;
        for (int time : list) {
            addJob(sched, "MedalGoodsRefreshJob" + time, "MedalGoods", MedalGoodsRefreshJob.class,
                    "0 0 " + time + " * * ?");// ??????time???????????????
        }
    }

    /**
     * ??????????????????????????????
     */
    private void loadActRankAwardScheule() {
        // ??????????????????????????????
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
            LogUtil.debug("----------???????????????????????????????????? :", name.toString(), ", Date,",
                    DateHelper.formatDateMiniTime(plan.getAwardTime()), "-------------------");
        }
    }

    /**
     * ???????????????????????????
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
            if (trigger != null) {// ??????????????????
                QuartzHelper.removeJob(sched, job.getName(), job.getGroup());
            }
            JobDetail detail = JobBuilder.newJob(job.getClass()).withIdentity(jobKey).build();
            detail.getJobDataMap().put(DefultJob.RUN, run);
            trigger = TriggerBuilder.newTrigger().withIdentity(job.getName(), job.getGroup()).startAt(startAt).build();
            sched.scheduleJob(detail, trigger);
            LogUtil.debug("----------????????????????????? :", job, ", Date,", DateHelper.formatDateMiniTime(startAt),
                    "---------------");
            return true;
        } catch (SchedulerException ex) {
            LogUtil.error("???????????????????????????", job.toString(), ":", ex);
        }
        return false;
    }

    /**
     * ????????????
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
     * ???????????????
     *
     * @param job
     * @param run
     * @param startAt           ????????????
     * @param endAt             ????????????
     * @param intervalInSeconds ????????????
     * @return
     */
    public boolean addOrModifyDefultJob(DefultJob job, QuartzCallBack run, Date startAt, Date endAt,
                                        int intervalInSeconds) {
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getName(), job.getGroup());
        JobKey jobKey = JobKey.jobKey(job.getName(), job.getGroup());
        try {
            Trigger trigger = sched.getTrigger(triggerKey);
            if (trigger != null) {// ??????????????????
                QuartzHelper.removeJob(sched, job.getName(), job.getGroup());
            }
            JobDetail detail = JobBuilder.newJob(job.getClass()).withIdentity(jobKey).build();
            detail.getJobDataMap().put(DefultJob.RUN, run);
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getName(), job.getGroup()).withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(intervalInSeconds).repeatForever())
                    .startAt(startAt).endAt(endAt).build();
            sched.scheduleJob(detail, trigger);
            LogUtil.debug("----------????????????????????? :", job, DateHelper.formatDateMiniTime(startAt), "---------------",
                    DateHelper.formatDateMiniTime(endAt), "intervalInSeconds: ", intervalInSeconds);
            return true;
        } catch (SchedulerException ex) {
            LogUtil.error("???????????????????????????", job.toString(), ":", ex);
        }
        return false;
    }

    /**
     * ???????????????
     * <p>
     * SimpleTrigger trigger = (SimpleTrigger) context.getTrigger();
     * <p>
     * int cnt = trigger.getTimesTriggered(); ????????????
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
            if (trigger != null) {// ??????????????????
                QuartzHelper.removeJob(sched, job.getName(), job.getGroup());
            }
            JobDetail detail = JobBuilder.newJob(job.getClass()).withIdentity(jobKey).build();
            detail.getJobDataMap().put(DefultJob.RUN, run);
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getName(), job.getGroup()).withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(intervalInSeconds).withRepeatCount(repeatCount))
                    .startAt(startAt).build();
            sched.scheduleJob(detail, trigger);
            LogUtil.debug("----------????????????????????? :", job, "---------------");
            return true;
        } catch (SchedulerException ex) {
            LogUtil.error("???????????????????????????", job.toString(), ":", ex);
        }
        return false;
    }

    /**
     * ???????????????
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
