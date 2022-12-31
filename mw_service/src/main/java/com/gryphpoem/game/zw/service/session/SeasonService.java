package com.gryphpoem.game.zw.service.session;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticVipDataMgr;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.SeasonJob;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.task.TaskCategory;
import com.gryphpoem.game.zw.resource.constant.task.TaskConst;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.AwardItem;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ActRank;
import com.gryphpoem.game.zw.resource.pojo.FunCard;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.season.*;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.PlayerService;
import com.gryphpoem.game.zw.service.TaskService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 赛季
 *
 * @author xwind
 * @date 2021/4/13
 */
@Service
public class SeasonService {

    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private SeasonTalentService seasonTalentService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RewardDataManager rewardDataManager;

    public GamePb4.SeasonGetInfoRs getSeasonInfo(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GamePb4.SeasonGetInfoRs.Builder resp = GamePb4.SeasonGetInfoRs.newBuilder();
        resp.setSeasonInfo(buildSeasonInfo());
        return resp.build();
    }

    public GamePb4.SeasonGetTreasuryInfoRs getTreasuryInfo(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int state = this.getSeasonState();
        if (state < SeasonConst.STATE_OPEN || state > SeasonConst.STATE_DISPLAY) {
            throw new MwException(GameError.SEASON_NON_OPEN.getCode(), GameError.SEASON_NON_OPEN.errMsg(roleId));
        }

        if (player.lord.getLevel() < 100) {
            throw new MwException(GameError.SEASON_TREASURY_NON.getCode(), GameError.SEASON_TREASURY_NON.errMsg(roleId));
        }
        PlayerSeasonData playerSeasonData = player.getPlayerSeasonData();

        this.initTreasuryTaskData(player);

        GamePb4.SeasonGetTreasuryInfoRs.Builder resp = GamePb4.SeasonGetTreasuryInfoRs.newBuilder();
        resp.setInfo(buildSeasonTreasuryInfo(playerSeasonData));
        LogUtil.c2sMessage("奖励时间:" + playerSeasonData.getTreasuryAwardTime() + ",重置时间:" + playerSeasonData.getTreasuryResetTime() + ",状态:" + playerSeasonData.getTreasuryState(), roleId);
        return resp.build();
    }

//    private void initTreasuryTaskData(Player player){
//        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
//        if(globalSeasonData.getCurrSeasonId() == 0 || Objects.isNull(globalSeasonData.getStaticSeasonPlan())){
//            return;
//        }
//        PlayerSeasonData playerSeasonData = player.getPlayerSeasonData();
//        if(player.lord.getLevel() < 100){
//            return;
//        }
//
//        StaticSeasonPlan staticSeasonPlan = globalSeasonData.getStaticSeasonPlan();
//        int season = staticSeasonPlan.getSeason();
//        int now = TimeHelper.getCurrentSecond();
//        int awardTime = getNextAwardTime();
//        int resetTime = getNextResetTime();
//
//        if(playerSeasonData.getTreasuryMap().isEmpty()){
//            this.onlyInitTreasuryTaskData(player,season,awardTime,resetTime);
//            return;
//        }
//
//        //有宝库任务数据、且过了重置时间、且下一个重置时间<=赛季的endTime，则重置任务数据并处理未领取的奖励
//        if(!playerSeasonData.getTreasuryMap().isEmpty() && playerSeasonData.getTreasuryResetTime() < now
//                && resetTime <= globalSeasonData.getStaticSeasonPlan().getEndTime().getTime()/1000
//                && playerSeasonData.getTreasuryState() == SeasonConst.TREASURY_STATE_GENERATED){
//            //根据任务分类，再取每个大类下的第一个已完成了的任务奖励
//            List<AwardItem> awardItemList = this.getTreasuryUnreward(playerSeasonData);
//
//            List<CommonPb.Award> awards = PbHelper.createAwards(awardItemList);
//            if(!awards.isEmpty()){
//                mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_SEASON_TREASURY_AWARD, AwardFrom.SEASON_TREASURY_GET_AWARD, now);
//            }
//
//            playerSeasonData.getTreasuryMap().clear();
//
//            //init treasury task data
//            this.onlyInitTreasuryTaskData(player,season,awardTime,resetTime);
//        }
//    }

//    private void addTreasuryJob(PlayerSeasonData playerSeasonData, int season) {
//        Date awardDate = TimeHelper.getDateByStamp(playerSeasonData.getTreasuryAwardTime());
//        String awardJobKey = SeasonJob.NAME_TREASURY_AWARD + season;
//        QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(), awardJobKey, SeasonJob.GROUP_SEASON);
//        QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), awardJobKey, SeasonJob.GROUP_SEASON, SeasonJob.class, awardDate);
//        Date resetDate = TimeHelper.getDateByStamp(playerSeasonData.getTreasuryResetTime());
//        String resetJobKey = SeasonJob.NAME_TREASURY_RESET + season;
//        QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(), resetJobKey, SeasonJob.GROUP_SEASON);
//        QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), resetJobKey, SeasonJob.GROUP_SEASON, SeasonJob.class, resetDate);
//    }

    /**
     * 初始化宝库任务的前置条件，下一次的重置时间需<=赛季的结束时间displaytime
     *
     * @param player
     */
    private void initTreasuryTaskData(Player player) {
        //没开赛季或等级不足则不初始化宝库任务
        int season = this.getCurrSeason();
        if (player.lord.getLevel() < 100 || getSeasonState() != SeasonConst.STATE_OPEN) {
            return;
        }
        PlayerSeasonData playerSeasonData = player.getPlayerSeasonData();
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        int seasonOverTime = (int) (globalSeasonData.getStaticSeasonPlan().getDisplayTime().getTime() / 1000);
        int awardTime = getNextAwardTime();
        int resetTime = getNextResetTime();
        if (playerSeasonData.getTreasuryMap().isEmpty() && resetTime <= seasonOverTime) {
            List<StaticSeasonTreasury> staticSeasonTreasuryList = StaticIniDataMgr.getStaticSeasonTreasuryMap().values().stream().filter(tmp -> tmp.getSeason() == season).collect(Collectors.toList());
            staticSeasonTreasuryList.forEach(tmp -> {
                SeasonTreasury seasonTreasury = new SeasonTreasury(tmp.getTaskId());
                playerSeasonData.getTreasuryMap().put(tmp.getTaskId(), seasonTreasury);

                //检查是否已经完成
                ETask eTask = ETask.getByType(tmp.getTaskType());
                if (Objects.nonNull(eTask) && eTask.isHandle()) {
                    this.handleTreasuryTask(player, eTask);
                }
            });
            playerSeasonData.setTreasuryState(SeasonConst.TREASURY_STATE_DOING);
            playerSeasonData.setTreasuryAwardTime(awardTime);
            playerSeasonData.setTreasuryResetTime(resetTime);
        }
    }

    public void resetTreasuryDataInAwardTime(Player player) {
        try {
            GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
            if (globalSeasonData.getCurrSeasonId() == 0) {
                return;
            }
            int now = TimeHelper.getCurrentSecond();
            PlayerSeasonData playerSeasonData = player.getPlayerSeasonData();
            if (!playerSeasonData.getTreasuryMap().isEmpty() && playerSeasonData.getTreasuryAwardTime() < now) {
                boolean isReset = true;
                for (SeasonTreasury value : playerSeasonData.getTreasuryMap().values()) {
                    if (value.getStatus() == TaskConst.STATUS_FINISH) {
                        isReset = false;
                        break;
                    }
                }
                if (isReset) {
                    playerSeasonData.getTreasuryMap().clear();
                    this.initTreasuryTaskData(player);
                    LogUtil.error("赛季宝库过了奖励时间,重置玩家的宝库任务数据, roleId=" + player.roleId);
                }
            }
        } catch (Exception e) {
            LogUtil.error("重置赛季宝库任务错误InAwardTime,roleId=" + player.roleId);
        }
    }

    public void resetTreasuryDataInResetTime(Player player) {
        try {
            GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
            if (globalSeasonData.getCurrSeasonId() == 0) {
                return;
            }
            int now = TimeHelper.getCurrentSecond();
            int resetTime = getNextResetTime();
            PlayerSeasonData playerSeasonData = player.getPlayerSeasonData();
            //有宝库任务数据、且过了重置时间、且下一个重置时间<=赛季的endTime，则重置任务数据并处理未领取的奖励
            if (!playerSeasonData.getTreasuryMap().isEmpty() && playerSeasonData.getTreasuryResetTime() < now
                    && resetTime <= globalSeasonData.getStaticSeasonPlan().getEndTime().getTime() / 1000) {
                if (playerSeasonData.getTreasuryState() == SeasonConst.TREASURY_STATE_GENERATED) {
                    //根据任务分类，再取每个大类下的第一个已完成了的任务奖励
                    List<AwardItem> awardItemList = this.getTreasuryUnreward(playerSeasonData);
                    List<CommonPb.Award> awards = PbHelper.createAwards(awardItemList);
                    if (!awards.isEmpty()) {
                        mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_SEASON_525, null, now);
                    }
                }

                playerSeasonData.getTreasuryMap().clear();
                this.initTreasuryTaskData(player);
                LogUtil.error("赛季宝库过了重置时间,重置玩家的宝库任务数据, roleId=" + player.roleId);
            }
        } catch (Exception e) {
            LogUtil.error("重置赛季宝库任务错误InResetTime,roleId=" + player.roleId);
        }
    }

    private List<AwardItem> getTreasuryUnreward(PlayerSeasonData playerSeasonData) {
        Map<Integer, List<StaticSeasonTreasury>> tmpMap = new HashMap<>();
        for (SeasonTreasury value : playerSeasonData.getTreasuryMap().values()) {
            StaticSeasonTreasury staticSeasonTreasury = StaticIniDataMgr.getStaticSeasonTreasuryMap().get(value.getTaskId());
            List<StaticSeasonTreasury> tmpList = tmpMap.get(staticSeasonTreasury.getCategory());
            if (Objects.isNull(tmpList)) {
                tmpList = new ArrayList<>();
                tmpMap.put(staticSeasonTreasury.getCategory(), tmpList);
            }
            tmpList.add(staticSeasonTreasury);
        }
        tmpMap.values().forEach(tmps -> Collections.sort(tmps, Comparator.comparingInt(StaticSeasonTreasury::getSerial)));
        List<AwardItem> awardItemList = new ArrayList<>();
        tmpMap.entrySet().forEach(entry -> {
            for (StaticSeasonTreasury staticSeasonTreasury : entry.getValue()) {
                SeasonTreasury seasonTreasury = playerSeasonData.getTreasuryMap().get(staticSeasonTreasury.getTaskId());
                if (Objects.nonNull(seasonTreasury) && ListUtils.isNotBlank(seasonTreasury.getAwards())) {
                    awardItemList.addAll(seasonTreasury.getAwards());
                    break;
                }
            }
        });
        return awardItemList;
    }

    private static int getNextAwardTime() {
        Calendar cal = Calendar.getInstance();
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayWeek == 1) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - cal.get(Calendar.DAY_OF_WEEK));
        cal.add(Calendar.DATE, 7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.SECOND, -1);
        return (int) (cal.getTimeInMillis() / 1000);
    }

    private static int getNextResetTime() {
        return getNextAwardTime() + TimeHelper.WEEK_SECOND;
    }

    private CommonPb.SeasonTreasuryInfo buildSeasonTreasuryInfo(PlayerSeasonData playerSeasonData) {
        CommonPb.SeasonTreasuryInfo.Builder builder = CommonPb.SeasonTreasuryInfo.newBuilder();
        playerSeasonData.getTreasuryMap().entrySet().forEach(entry -> builder.addTreasuryTask(buildSeasonTreasuryTask(entry.getValue())));
        builder.setState(playerSeasonData.getTreasuryState());
        int diff = TimeHelper.getCurrentSecond() - playerSeasonData.getTreasuryAwardTime();
        builder.setAwardTime(diff > 0 ? 0 : playerSeasonData.getTreasuryAwardTime());
        return builder.build();
    }

    private CommonPb.SeasonTreasuryTask buildSeasonTreasuryTask(SeasonTreasury treasury) {
        CommonPb.SeasonTreasuryTask.Builder builder = CommonPb.SeasonTreasuryTask.newBuilder();
        builder.setTaskId(treasury.getTaskId());
        builder.setProgress((int) treasury.getSchedule());
        builder.setState(treasury.getStatus());
        Optional.ofNullable(treasury.getAwards()).ifPresent(tmps -> tmps.forEach(obj -> {
            builder.addAward(PbHelper.createAward(obj));
        }));
        return builder.build();
    }

    public GamePb4.SeasonGetTaskInfoRs getSeasonTaskInfo(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int state = this.getSeasonState();
        if (state < SeasonConst.STATE_OPEN || state > SeasonConst.STATE_DISPLAY) {
            throw new MwException(GameError.SEASON_NON_OPEN.getCode(), GameError.SEASON_NON_OPEN.errMsg(roleId));
        }
        int season = this.getCurrSeason();

        PlayerSeasonData playerSeasonData = player.getPlayerSeasonData();

        this.initSeasonTaskData(player, season);

        GamePb4.SeasonGetTaskInfoRs.Builder resp = GamePb4.SeasonGetTaskInfoRs.newBuilder();
        resp.setTaskInfo(buildSeasonTaskInfo(playerSeasonData));
        return resp.build();
    }

    private void initSeasonTaskData(Player player, int season) {
        if (getSeasonState() != SeasonConst.STATE_OPEN) {
            return;
        }
        if (season > 0 && player.getPlayerSeasonData().getCurrTasks().isEmpty() && player.getPlayerSeasonData().getFinishedTasks().isEmpty()) {
            //init task data
            Optional.ofNullable(StaticIniDataMgr.getStaticSeasonTaskGroupMap().get(season)).ifPresent(tmps -> {
                tmps.forEach(tmp -> {
                    if (tmp.getPreTaskId() == 0) {
                        SeasonTask seasonTask = new SeasonTask(tmp.getTaskId());
                        player.getPlayerSeasonData().getCurrTasks().put(seasonTask.getTaskId(), seasonTask);

                        ETask eTask = ETask.getByType(tmp.getTaskType());
                        if (Objects.nonNull(eTask) && eTask.isHandle()) {
                            if (eTask == ETask.FINISHED_TASK || eTask == ETask.GET_TASKAWARD) {
                                this.handleJourneyTask(player, eTask, TaskCategory.TREASURY.getCategory());
                            } else {
                                this.handleJourneyTask(player, eTask);
                            }
                        }
                    }
                });
            });
        }
    }

    private CommonPb.SeasonTaskInfo buildSeasonTaskInfo(PlayerSeasonData playerSeasonData) {
        CommonPb.SeasonTaskInfo.Builder builder = CommonPb.SeasonTaskInfo.newBuilder();
        playerSeasonData.getCurrTasks().values().forEach(tmp -> builder.addTask(PbHelper.buildTask(tmp.getTaskId(), tmp.getSchedule(), tmp.getStatus())));
        builder.setTaskScore(playerSeasonData.getTaskScore());
        playerSeasonData.getGotScoreAward().entrySet().forEach(tmp -> builder.addGotAward(PbHelper.createTwoIntPb(tmp.getKey(), tmp.getValue())));
        return builder.build();
    }

    public FunCard getCurrSeasonCard(Player player) {
        int season = this.getCurrSeason();
        if (season == 0) {
            return null;
        }
        StaticFunCard staticFunCard = StaticVipDataMgr.getFunCardByGroupType(FunCard.CARD_TYPE[9]).stream().filter(tmp -> tmp.getSeason() == season).findFirst().orElse(null);
        FunCard funCard = player.getPlayerSeasonData().getMonthCards().get(staticFunCard.getId());
        return funCard;
    }

    public boolean buyMonthCard(Player player, StaticPay staticPay, StaticFunCard sfc) {
        int seasonState = this.getSeasonState();
        if (seasonState != SeasonConst.STATE_OPEN) {
            LogUtil.error("购买赛季月卡失败，当前未开放赛季");
            return false;
        }
        int season = this.getCurrSeason();
        if (sfc.getSeason() != season) {
            LogUtil.error("购买赛季月卡失败，月卡和当前赛季不匹配，" + season + ", " + sfc.getSeason());
            return false;
        }
        FunCard funCard = player.getPlayerSeasonData().getMonthCards().get(sfc.getId());
        if (Objects.isNull(funCard)) {
            funCard = new FunCard(sfc.getId());
            player.getPlayerSeasonData().getMonthCards().put(funCard.getType(), funCard);
        }
        funCard.addExpireDay(sfc.getDay());

        this.handleMonthCardReward(player);
        return true;
    }

    public void handleMonthCardReward(Player player) {
        Optional.ofNullable(player).ifPresent(p -> {
            int today = TimeHelper.getCurrentDay();
            Map<Integer, FunCard> monthCards = p.getPlayerSeasonData().getMonthCards();
            monthCards.values().forEach(funCard -> {
                StaticFunCard staticFunCard = StaticVipDataMgr.getFunCardMap().get(funCard.getType());
                if (Objects.nonNull(staticFunCard)) {
                    if (today != funCard.getLastTime() && funCard.getRemainCardDay() > 0) {
                        funCard.subRemainCardDay();
                        funCard.setLastTime(today);
                        List<CommonPb.Award> awards = PbHelper.createAwardsPb(staticFunCard.getAward());
                        mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_FUN_CARD_AWARD, AwardFrom.FUN_CARD_AWARD, TimeHelper.getCurrentSecond()
                                , staticFunCard.getPayId(), funCard.getRemainCardDay(), staticFunCard.getPayId(), funCard.getRemainCardDay());
                    }
                }
            });
        });
    }

    public GamePb4.SeasonGenerateTreasuryAwardRs generateTreasuryAward(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        PlayerSeasonData playerSeasonData = player.getPlayerSeasonData();
        int now = TimeHelper.getCurrentSecond();
        if (now < playerSeasonData.getTreasuryAwardTime()) {
            throw new MwException(GameError.SEASON_TREASURY_AWARD_NOGEN.getCode(), GameError.SEASON_TREASURY_AWARD_NOGEN.errMsg(roleId));
        }
        if (playerSeasonData.getTreasuryState() == SeasonConst.TREASURY_STATE_GENERATED) {
            throw new MwException(GameError.SEASON_TREASURY_AWARD_GEND.getCode(), GameError.SEASON_TREASURY_AWARD_GEND.errMsg(roleId));
        }
        playerSeasonData.getTreasuryMap().values().forEach(tmp -> {
            if (tmp.getStatus() == TaskConst.STATUS_FINISH) {
                StaticSeasonTreasury staticSeasonTreasury = StaticIniDataMgr.getStaticSeasonTreasuryMap().get(tmp.getTaskId());
                List<AwardItem> awardList = GameUtil.randomAwardByWeight(staticSeasonTreasury.getRdmAward(), staticSeasonTreasury.getWeight());
                tmp.setAwards(awardList);
            }
        });
        playerSeasonData.setTreasuryState(SeasonConst.TREASURY_STATE_GENERATED);

        GamePb4.SeasonGenerateTreasuryAwardRs.Builder resp = GamePb4.SeasonGenerateTreasuryAwardRs.newBuilder();
        resp.setInfo(buildSeasonTreasuryInfo(playerSeasonData));
        return resp.build();
    }

    public GamePb4.SeasonGetTreasuryAwardRs getTreasuryAward(long roleId, GamePb4.SeasonGetTreasuryAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        PlayerSeasonData playerSeasonData = player.getPlayerSeasonData();
//        int now = TimeHelper.getCurrentSecond();
//        if (now < playerSeasonData.getTreasuryAwardTime() || now > playerSeasonData.getTreasuryResetTime()) {
//            throw new MwException(GameError.SEASON_TREASURY_GET_AWARD_NO.getCode(), GameError.SEASON_TREASURY_GET_AWARD_NO.errMsg(roleId));
//        }
//        int state = getSeasonState();
        if (playerSeasonData.getTreasuryState() != SeasonConst.TREASURY_STATE_GENERATED) {
            throw new MwException(GameError.SEASON_TREASURY_GET_AWARD_NOTGEN.getCode(), GameError.SEASON_TREASURY_GET_AWARD_NOTGEN.errMsg(roleId));
        }
        if (req.getTaskIdList().isEmpty()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), roleId + "领取宝库奖励，参数错误，选中的taskid列表为空");
        }

        List<AwardItem> awardItemList = new ArrayList<>();

        for (int taskId : req.getTaskIdList()) {
            SeasonTreasury seasonTreasury = playerSeasonData.getTreasuryMap().get(taskId);
            if (Objects.isNull(seasonTreasury)) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), roleId + "领取宝库奖励，taskid不存在" + taskId);
            }
            if (ListUtils.isBlank(seasonTreasury.getAwards())) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), roleId + "领取宝库奖励，选中的任务没有奖励" + taskId);
            }
            StaticSeasonTreasury staticSeasonTreasury = StaticIniDataMgr.getStaticSeasonTreasuryMap().get(taskId);
            List<Integer> tmps = new ArrayList<>();
            tmps.addAll(req.getTaskIdList());
            tmps.remove(Integer.valueOf(taskId));
            for (int tmp : tmps) {
                StaticSeasonTreasury staticSeasonTreasury1 = StaticIniDataMgr.getStaticSeasonTreasuryMap().get(tmp);
                if (staticSeasonTreasury.getCategory() == staticSeasonTreasury1.getCategory()) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), roleId + "领取宝库奖励，一个大类只能选择一个任务奖励" + taskId);
                }
            }
            awardItemList.addAll(seasonTreasury.getAwards());
        }

        List<CommonPb.Award> awards = PbHelper.createAwards(awardItemList);
        if (!awards.isEmpty()) {
            rewardDataManager.sendRewardByAwardList(player, awards, AwardFrom.SEASON_TREASURY_GET_AWARD, req.getTaskIdList());
        }
        playerSeasonData.setTreasuryState(SeasonConst.TREASURY_STATE_GOT);
        playerSeasonData.getTreasuryMap().clear();
        this.initTreasuryTaskData(player);

        playerSeasonData.setGetAwardCount(playerSeasonData.getGetAwardCount() + 1);

        //任务 - 领取宏伟宝库任务奖励
        TaskService.handleTask(player, ETask.GET_TASKAWARD, TaskCategory.TREASURY.getCategory());
        ActivityDiaoChanService.completeTask(player, ETask.GET_TASKAWARD, TaskCategory.TREASURY.getCategory());
        TaskService.processTask(player, ETask.GET_TASKAWARD, TaskCategory.TREASURY.getCategory());

        GamePb4.SeasonGetTreasuryAwardRs.Builder resp = GamePb4.SeasonGetTreasuryAwardRs.newBuilder();
        resp.setInfo(buildSeasonTreasuryInfo(playerSeasonData));
        return resp.build();
    }

    public GamePb4.SeasonGetTaskAwardRs getJourneyAward(long roleId, GamePb4.SeasonGetTaskAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int taskId = req.getTaskId();
        int scoreId = req.getScoreId();
        PlayerSeasonData playerSeasonData = player.getPlayerSeasonData();
        int season = this.getCurrSeason();
        if (season == 0) {
            throw new MwException(GameError.SEASON_NON_OPEN.getCode(), GameError.SEASON_NON_OPEN.errMsg(roleId));
        }
        if (taskId > 0 && scoreId == 0) {//任务奖励
            SeasonTask seasonTask = playerSeasonData.getCurrTasks().get(taskId);
            if (Objects.isNull(seasonTask)) {
                throw new MwException(GameError.SEASON_TASK_GET_AWARD_NO.getCode(), GameError.SEASON_TASK_GET_AWARD_NO.errMsg(roleId, taskId));
            }
            if (seasonTask.getStatus() != TaskConst.STATUS_FINISH) {
                throw new MwException(GameError.SEASON_TASK_GET_AWARD_NO.getCode(), GameError.SEASON_TASK_GET_AWARD_NO.errMsg(roleId, taskId), "任务未完成");
            }
            StaticSeasonTask staticSeasonTask = StaticIniDataMgr.getStaticSeasonTaskMap().get(taskId);
            if (Objects.nonNull(staticSeasonTask)) {
                rewardDataManager.sendReward(player, staticSeasonTask.getTaskAward(), AwardFrom.SEASON_TASK_GET_AWARD, taskId);

                int taskScore = player.getPlayerSeasonData().getTaskScore();
                player.getPlayerSeasonData().setTaskScore(taskScore + staticSeasonTask.getTaskScore());

                seasonTask.setStatus(TaskConst.STATUS_REWARD);

                StaticSeasonTask nextStatic = StaticIniDataMgr.getStaticSeasonTaskMap().values().stream().filter(tmp -> tmp.getPreTaskId() == taskId).findFirst().orElse(null);
                if (Objects.nonNull(nextStatic)) {
                    player.getPlayerSeasonData().getCurrTasks().remove(taskId);
                    player.getPlayerSeasonData().getFinishedTasks().put(taskId, seasonTask);

                    SeasonTask nextTask = new SeasonTask(nextStatic.getTaskId());
                    player.getPlayerSeasonData().getCurrTasks().put(nextTask.getTaskId(), nextTask);
                    ETask eTask = ETask.getByType(nextStatic.getTaskType());
                    if (Objects.nonNull(eTask) && eTask.isHandle()) {
                        if (eTask == ETask.FINISHED_TASK || eTask == ETask.GET_TASKAWARD) {
                            this.handleJourneyTask(player, eTask, TaskCategory.TREASURY.getCategory());
                        } else {
                            this.handleJourneyTask(player, eTask);
                        }
                    }
                }
            }
        }
        if (scoreId > 0 && taskId == 0) {//积分奖励
            StaticSeasonTaskScore staticSeasonTaskScore = StaticIniDataMgr.getStaticSeasonTaskScoreMap().values().stream().filter(tmp -> tmp.getId() == scoreId && tmp.getSeason() == season).findFirst().orElse(null);
            if (Objects.isNull(staticSeasonTaskScore)) {
                throw new MwException(GameError.SEASON_TASK_GET_AWARD_NO.getCode(), "领取旅程任务积分奖励失败");
            }
            int got = playerSeasonData.getGotScoreAward().getOrDefault(scoreId, 0);
            if (got != 0) {
                throw new MwException(GameError.SEASON_TASK_GET_AWARD_NO.getCode(), "领取旅程任务积分奖励失败, 已领取scoreId=" + scoreId);
            }
            if (playerSeasonData.getTaskScore() < staticSeasonTaskScore.getScore()) {
                throw new MwException(GameError.SEASON_TASK_GET_AWARD_NO.getCode(), "领取旅程任务积分奖励失败, 积分不足scoreId=" + scoreId);
            }
            rewardDataManager.sendReward(player, staticSeasonTaskScore.getAward(), AwardFrom.SEASON_TASK_GET_SCORE_AWARD);

            player.getPlayerSeasonData().getGotScoreAward().put(scoreId, 1);
        }

        GamePb4.SeasonGetTaskAwardRs.Builder resp = GamePb4.SeasonGetTaskAwardRs.newBuilder();
        resp.setTaskInfo(buildSeasonTaskInfo(playerSeasonData));
        return resp.build();
    }

    public void execJob4Pre(int planId) {
        //设置赛季
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        globalSeasonData.setCurrSeasonId(planId);
        StaticSeasonPlan staticSeasonPlan = StaticIniDataMgr.getStaticSeasonPlanById(planId);
        globalSeasonData.setStaticSeasonPlan(staticSeasonPlan);
        globalSeasonData.getRanks().clear();
        globalSeasonData.getCampRank().clear();

        //清理之前的赛季数据
        playerDataManager.getPlayers().values().forEach(p -> p.getPlayerSeasonData().clearData());


        syncSeasonInfo();

        LogUtil.error("赛季预告, GlobalSeasonData=" + JSON.toJSONString(globalSeasonData));
    }

    public void execJob4Begin(int planId) {
        //开放宝库建筑
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        globalSeasonData.setLastSeasonId(planId);

        syncSeasonInfo();

        LogUtil.error("赛季开始, planId=" + planId);
    }

    public void execJob4End(int planId) {
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        int currSeason = this.getCurrSeason();
        int now = TimeHelper.getCurrentSecond();

        //处理未领取的旅程奖励
        playerDataManager.getPlayers().values().forEach(p -> {
            PlayerSeasonData playerSeasonData = p.getPlayerSeasonData();
            List<List<Integer>> unrewardList = new ArrayList<>();
            //任务奖励
            playerSeasonData.getCurrTasks().values().forEach(tmp -> {
                if (tmp.getStatus() == TaskConst.STATUS_FINISH) {
                    StaticSeasonTask staticSeasonTask = StaticIniDataMgr.getStaticSeasonTaskMap().get(tmp.getTaskId());
                    if (Objects.nonNull(staticSeasonTask)) {
                        unrewardList.addAll(staticSeasonTask.getTaskAward());
                    }
                    tmp.setStatus(TaskConst.STATUS_REWARD);
                }
            });
            //积分奖励
            int taskScore = playerSeasonData.getTaskScore();
            StaticIniDataMgr.getStaticSeasonTaskScoreGroup().get(currSeason).forEach(tmp -> {
                if (taskScore >= tmp.getScore() && playerSeasonData.getGotScoreAward().getOrDefault(tmp.getId(), 0) == 0) {
                    unrewardList.addAll(tmp.getAward());
                }
            });
            List<CommonPb.Award> journeyAwards = PbHelper.createAwardsPb(unrewardList);
            if (ListUtils.isNotBlank(journeyAwards)) {
                rewardDataManager.sendRewardByAwardList(p, journeyAwards, AwardFrom.SEASON_JOURNEY_UNREWARD_ON_END);
//                mailDataManager.sendAttachMail(p, journeyAwards, MailConstant.MOLD_SEASON_524, null, now);
                mailDataManager.sendReportMail(p, null, MailConstant.MOLD_SEASON_524, journeyAwards, now);
            }
        });

        //处理玩家排行奖励
        int i = 0;
        int max = StaticIniDataMgr.getSeasonRankMax().getOrDefault(currSeason, 100);
        List<StaticSeasonRank> staticSeasonRankList0 = StaticIniDataMgr.getStaticSeasonRankGroupMap().get(currSeason).stream().filter(tmp -> tmp.getType() == 1).collect(Collectors.toList());
        for (ActRank rank : globalSeasonData.getRanks()) {
            if (i > max) {
                break;
            }
            int j = i + 1;
            StaticSeasonRank staticSeasonRank = staticSeasonRankList0.stream().filter(tmp -> j >= tmp.getRank().get(0) && j <= tmp.getRank().get(1)).findFirst().orElse(null);
            Optional.ofNullable(staticSeasonRank).ifPresent(tmp -> {
                List<CommonPb.Award> awardList = PbHelper.createAwardsPb(staticSeasonRank.getAward());
                Player player = playerDataManager.getPlayer(rank.getLordId());
                if (Objects.nonNull(player)) {
                    mailDataManager.sendAttachMail(player, awardList, MailConstant.MOLD_SEASON_522, AwardFrom.SEASON_PLAYER_RANK_AWARD, now + 2, j);
                }
            });
            i++;
        }

        //处理阵营排行奖励
        globalSeasonData.getCampRank().sort(COMPARATOR_CAMP_RANK);
        List<StaticSeasonRank> staticSeasonRankList1 = StaticIniDataMgr.getStaticSeasonRankGroupMap().get(currSeason).stream().filter(tmp -> tmp.getType() == 2).collect(Collectors.toList());
        playerDataManager.getPlayers().values().forEach(player -> {
            CampRankData campRankData = getCampRankData(player.getCamp(), globalSeasonData.getCampRank());
            if (Objects.nonNull(campRankData)) {
                StaticSeasonRank staticSeasonRank = staticSeasonRankList1.stream().filter(tmp -> campRankData.rank >= tmp.getRank().get(0) && campRankData.rank <= tmp.getRank().get(1) && player.lord.getJob() == tmp.getPartyJob()).findFirst().orElse(null);
                if (Objects.nonNull(staticSeasonRank)) {
                    List<CommonPb.Award> awardList = PbHelper.createAwardsPb(staticSeasonRank.getAward());
                    mailDataManager.sendAttachMail(player, awardList, MailConstant.MOLD_SEASON_523, AwardFrom.SEASON_CAMP_RANK_AWARD, now + 2, campRankData.rank);
                }
            }
        });
        //赛季结束时, 处理天赋失效, 赛季天赋有自己的结束时间
//        seasonTalentService.executeSeasonEnd();
        syncSeasonInfo();
        LogUtil.error("赛季截止, planId=" + planId);
    }

    public void execJob4Over(int planId) {
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        int currSeason = this.getCurrSeason();
        globalSeasonData.setCurrSeasonId(0);
        globalSeasonData.setStaticSeasonPlan(null);

        int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().forEach(p -> {
            PlayerSeasonData playerSeasonData = p.getPlayerSeasonData();
            //宝库奖励
            if (playerSeasonData.getTreasuryState() == SeasonConst.TREASURY_STATE_GENERATED) {
                List<AwardItem> awardItemList = this.getTreasuryUnreward(p.getPlayerSeasonData());
                if (!awardItemList.isEmpty()) {
                    List<CommonPb.Award> awardList = PbHelper.createAwards(awardItemList);
                    mailDataManager.sendAttachMail(p, awardList, MailConstant.MOLD_SEASON_525, null, now);
                }
                playerSeasonData.setTreasuryState(SeasonConst.TREASURY_STATE_GOT);
            }
        });

        syncSeasonInfo();

        LogUtil.error("赛季结束, planId=" + planId);
    }

    public void execJob4AwardTime(int planId) {

    }

    public void execJob4ResetTime(int planId) {

    }

    public void syncSeasonInfo() {
        GamePb4.SyncSeasonInfoRs.Builder builder = GamePb4.SyncSeasonInfoRs.newBuilder();
        builder.setSeasonInfo(buildSeasonInfo());
        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncSeasonInfoRs.EXT_FIELD_NUMBER, GamePb4.SyncSeasonInfoRs.ext, builder.build()).build();
        playerService.syncMsgToAll(msg);
    }

    public CommonPb.SeasonInfo buildSeasonInfo() {
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        StaticSeasonPlan staticSeasonPlan = StaticIniDataMgr.getStaticSeasonPlanById(globalSeasonData.getCurrSeasonId());
        StaticSeasonPlan lastPlan = StaticIniDataMgr.getStaticSeasonPlanById(globalSeasonData.getLastSeasonId());
        CommonPb.SeasonInfo.Builder builder = CommonPb.SeasonInfo.newBuilder();
        builder.setCurrSeason(staticSeasonPlan == null ? 0 : staticSeasonPlan.getSeason());
        builder.setState(getSeasonState(globalSeasonData.getCurrSeasonId()));
        builder.setLastSeasonId(lastPlan == null ? 0 : lastPlan.getSeason());
        if (Objects.nonNull(staticSeasonPlan)) {
            builder.setPreviewTime((int) (staticSeasonPlan.getPreviewTime().getTime() / 1000));
            builder.setBeginTime((int) (staticSeasonPlan.getBeginTime().getTime() / 1000));
            builder.setEndTime((int) (staticSeasonPlan.getEndTime().getTime() / 1000));
            builder.setDisplayTime((int) (staticSeasonPlan.getDisplayTime().getTime() / 1000));
        }

        boolean isOpenSeasonTalent = false;
        if (Objects.nonNull(staticSeasonPlan)) {
            StaticSeasonTalentPlan staticSeasonTalentPlan = StaticIniDataMgr.getOpenStaticSeasonTalentPlan(staticSeasonPlan.getId());
            if (Objects.nonNull(staticSeasonTalentPlan)) {
                isOpenSeasonTalent = true;
                builder.setTalentBeginTime((int) (staticSeasonTalentPlan.getBeginTime().getTime() / 1000));
                builder.setTalentEndTime((int) (staticSeasonTalentPlan.getEndTime().getTime() / 1000));
            }
        }
        builder.setIsOpenSeasonTalent(isOpenSeasonTalent);

        return builder.build();
    }

    private int getSeasonState(int planId) {
        StaticSeasonPlan staticSeasonPlan = StaticIniDataMgr.getStaticSeasonPlanById(planId);
        if (Objects.isNull(staticSeasonPlan)) {
            return SeasonConst.STATE_NON;
        }
        Date now = new Date();
        if (now.after(staticSeasonPlan.getPreviewTime()) && now.before(staticSeasonPlan.getBeginTime())) {
            return SeasonConst.STATE_PRE;
        } else if (now.after(staticSeasonPlan.getBeginTime()) && now.before(staticSeasonPlan.getEndTime())) {
            return SeasonConst.STATE_OPEN;
        } else if ((now.after(staticSeasonPlan.getEndTime()) || now.equals(staticSeasonPlan.getEndTime())) && now.before(staticSeasonPlan.getDisplayTime())) {
            return SeasonConst.STATE_DISPLAY;
        }
        return SeasonConst.STATE_NON;
    }

    public int getSeasonState() {
        GameGlobal gameGlobal = globalDataManager.getGameGlobal();
        GlobalSeasonData globalSeasonData = gameGlobal != null ? gameGlobal.getGlobalSeasonData() : null;
        return globalSeasonData != null ? getSeasonState(globalSeasonData.getCurrSeasonId()) : SeasonConst.STATE_NON;
    }

    public int getCurrSeason() {
        try {
            GameGlobal gameGlobal = globalDataManager.getGameGlobal();
            GlobalSeasonData globalSeasonData = gameGlobal != null ? gameGlobal.getGlobalSeasonData() : null;
            if (globalSeasonData != null && globalSeasonData.getCurrSeasonId() > 0) {
                StaticSeasonPlan staticSeasonPlan = StaticIniDataMgr.getStaticSeasonPlanById(globalSeasonData.getCurrSeasonId());
                return staticSeasonPlan != null ? staticSeasonPlan.getSeason() : 0;
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
        return 0;
    }

    public int getLastSeason() {
        try {
            GameGlobal gameGlobal = globalDataManager.getGameGlobal();
            GlobalSeasonData globalSeasonData = gameGlobal != null ? gameGlobal.getGlobalSeasonData() : null;
            if (globalSeasonData != null && globalSeasonData.getLastSeasonId() > 0) {
                StaticSeasonPlan staticSeasonPlan = StaticIniDataMgr.getStaticSeasonPlanById(globalSeasonData.getLastSeasonId());
                return staticSeasonPlan != null ? staticSeasonPlan.getSeason() : 0;
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
        return 0;
    }

    public int getCurSeasonPlanId() {
        GameGlobal gameGlobal = globalDataManager.getGameGlobal();
        GlobalSeasonData globalSeasonData = gameGlobal != null ? gameGlobal.getGlobalSeasonData() : null;
        return globalSeasonData != null ? globalSeasonData.getCurrSeasonId() : 0;
    }

    public static boolean checkServerId(List<List<Integer>> serverIds, int checkId) {
        if (ListUtils.isNotBlank(serverIds)) {
            for (List<Integer> tmps : serverIds) {
                if (checkId >= tmps.get(0) && checkId <= tmps.get(1)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void initSchedule(Scheduler scheduler) {
        Date now = new Date();
        int serverId = serverSetting.getServerID();
        StaticIniDataMgr.getStaticSeasonPlanList().stream().filter(tmp -> checkServerId(tmp.getServerId(), serverId)).forEach(tmp -> {
            this.addSeasonJob(scheduler, tmp, now);
        });

        this.initSeasonOnStartup();
    }

    private void addSeasonJob(Scheduler scheduler, StaticSeasonPlan tmp, Date now) {
        if (now.before(tmp.getPreviewTime())) {
            QuartzHelper.addJob(scheduler, SeasonJob.NAME_PRE + tmp.getId(), SeasonJob.GROUP_SEASON, SeasonJob.class, tmp.getPreviewTime());
        }
        if (now.before(tmp.getBeginTime())) {
            QuartzHelper.addJob(scheduler, SeasonJob.NAME_BEGIN + tmp.getId(), SeasonJob.GROUP_SEASON, SeasonJob.class, tmp.getBeginTime());
        }
        if (now.before(tmp.getEndTime())) {
            QuartzHelper.addJob(scheduler, SeasonJob.NAME_END + tmp.getId(), SeasonJob.GROUP_SEASON, SeasonJob.class, tmp.getEndTime());
        }
        if (now.before(tmp.getDisplayTime())) {
            QuartzHelper.addJob(scheduler, SeasonJob.NAME_OVER + tmp.getId(), SeasonJob.GROUP_SEASON, SeasonJob.class, tmp.getDisplayTime());
        }
    }

    /**
     * 服务器启动时处理赛季数据
     */
    private void initSeasonOnStartup() {
        Date now = new Date();
        int serverId = serverSetting.getServerID();
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        int currSeasonId = globalSeasonData.getCurrSeasonId();
        //当前没有开放的赛季，则根据配置设置当前赛季
        if (currSeasonId == 0) {
            StaticSeasonPlan staticSeasonPlan = StaticIniDataMgr.getStaticSeasonPlanList().stream().filter(tmp ->
                    checkServerId(tmp.getServerId(), serverId) && now.after(tmp.getPreviewTime()) && now.before(tmp.getDisplayTime())).findFirst().orElse(null);
            if (Objects.nonNull(staticSeasonPlan)) {
                globalSeasonData.setCurrSeasonId(staticSeasonPlan.getId());
                int state = this.getSeasonState();
                if (state == SeasonConst.STATE_PRE) {
                    this.execJob4Pre(staticSeasonPlan.getId());
                } else if (state == SeasonConst.STATE_OPEN) {
                    this.execJob4Pre(staticSeasonPlan.getId());
                    this.execJob4Begin(staticSeasonPlan.getId());
                } else if (state == SeasonConst.STATE_DISPLAY) {
                    this.execJob4Pre(staticSeasonPlan.getId());
                    this.execJob4Begin(staticSeasonPlan.getId());
                    this.execJob4End(staticSeasonPlan.getId());
                }
            }
        }
        //当前有开放的赛季，若当前时间已不在开放时间内则结束当前赛季并根据配表中当前开放的赛季设置；若当前时间在开放时间内则刷新配置
        else {
            StaticSeasonPlan staticSeasonPlan = StaticIniDataMgr.getStaticSeasonPlanById(currSeasonId);
            if (Objects.nonNull(staticSeasonPlan)) {
                if (now.after(staticSeasonPlan.getPreviewTime()) && now.before(staticSeasonPlan.getDisplayTime())) {
                    globalSeasonData.setStaticSeasonPlan(staticSeasonPlan);
                } else {
                    this.execJob4Over(currSeasonId);
                    staticSeasonPlan = StaticIniDataMgr.getStaticSeasonPlanList().stream().filter(tmp ->
                            checkServerId(tmp.getServerId(), serverId) && now.after(tmp.getPreviewTime()) && now.before(tmp.getDisplayTime())).findFirst().orElse(null);
                    if (Objects.nonNull(staticSeasonPlan)) {
                        this.execJob4Pre(staticSeasonPlan.getId());
                    }
                }
            }
        }
        LogUtil.error("服务器启动检查当前开放的赛季, GlobalSeasonData=" + JSON.toJSONString(globalSeasonData));
    }

    public void checkStaticValid() throws MwException {
        Date now = new Date();
        int serverId = serverSetting.getServerID();
        List<StaticSeasonPlan> staticSeasonPlanList = StaticIniDataMgr.getStaticSeasonPlanList().stream()
                .filter(tmp -> now.after(tmp.getPreviewTime()) && now.before(tmp.getDisplayTime()) && checkServerId(tmp.getServerId(), serverId)).collect(Collectors.toList());
        if (staticSeasonPlanList.size() > 1) {
            throw new MwException("赛季配表规则错误, 不同赛季的开放时间有交错: " + JSON.toJSONString(staticSeasonPlanList));
        }
    }

    public void handleTreasuryTask0(Player player, ETask eTask, int... params) {
        this.initTreasuryTaskData(player);

        this.handleTreasuryTask(player, eTask, params);
    }

    /**
     * 处理赛季宝库任务
     *
     * @param player
     * @param eTask
     */
    private void handleTreasuryTask(Player player, ETask eTask, int... params) {
        if (Objects.isNull(player)) {
            return;
        }

        int season = this.getCurrSeason();
        if (season == 0) {
            return;
        }

        int state = this.getSeasonState();
        if (state != SeasonConst.STATE_OPEN) {
            return;
        }

        PlayerSeasonData playerSeasonData = player.getPlayerSeasonData();

        if (playerSeasonData.getTreasuryAwardTime() <= TimeHelper.getCurrentSecond()) {
            return;
        }

        GamePb4.SyncSeasonTaskRs.Builder builder = GamePb4.SyncSeasonTaskRs.newBuilder();
        playerSeasonData.getTreasuryMap().values().forEach(task -> {
            StaticSeasonTreasury staticSeasonTreasury = StaticIniDataMgr.getStaticSeasonTreasuryMap().get(task.getTaskId());
            if (Objects.nonNull(staticSeasonTreasury) && staticSeasonTreasury.getTaskType() == eTask.getTaskType() && task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
                boolean b = taskService.checkTaskCondition(player, task, staticSeasonTreasury.getTaskCond(), eTask, params);
                if (b) {
                    builder.addTreasuryTask(buildSeasonTreasuryTask(task));

                    if (task.getStatus() == TaskType.TYPE_STATUS_FINISH) {
                        playerSeasonData.setFinishedCount(playerSeasonData.getFinishedCount() + 1);

                        this.handleJourneyTask(player, ETask.FINISHED_TASK, TaskCategory.TREASURY.getCategory());
                        ActivityDiaoChanService.completeTask(player, ETask.FINISHED_TASK, TaskCategory.TREASURY.getCategory());
                        TaskService.processTask(player, ETask.FINISHED_TASK, TaskCategory.TREASURY.getCategory());
                    }
                }
            }
        });

        if (builder.getTreasuryTaskCount() > 0 || builder.getTaskCount() > 0) {
            BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncSeasonTaskRs.EXT_FIELD_NUMBER, GamePb4.SyncSeasonTaskRs.ext, builder.build()).build();
            playerService.syncMsgToPlayer(msg, player);
        }
    }

    public void handleJourneyTask0(Player player, ETask eTask, int... params) {
        int season = this.getCurrSeason();
        this.initSeasonTaskData(player, season);

        this.handleJourneyTask(player, eTask, params);
    }

    /**
     * 处理赛季旅程任务
     *
     * @param player
     * @param eTask
     * @param params
     */
    private void handleJourneyTask(Player player, ETask eTask, int... params) {
        int season = this.getCurrSeason();
        if (season == 0) {
            return;
        }
        int state = this.getSeasonState();
        if (state != SeasonConst.STATE_OPEN) {
            return;
        }
        GamePb4.SyncSeasonTaskRs.Builder builder = GamePb4.SyncSeasonTaskRs.newBuilder();
        //旅程任务
        player.getPlayerSeasonData().getCurrTasks().values().forEach(task -> {
            StaticSeasonTask staticSeasonTask = StaticIniDataMgr.getStaticSeasonTaskMap().get(task.getTaskId());
            if (Objects.nonNull(staticSeasonTask) && staticSeasonTask.getTaskType() == eTask.getTaskType() && task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
                boolean b = taskService.checkTaskCondition(player, task, staticSeasonTask.getTaskCond(), eTask, params);
                if (b) {
                    builder.addTask(PbHelper.buildTask(task.getTaskId(), task.getSchedule(), task.getStatus()));
                }
            }
        });
        if (builder.getTaskCount() > 0) {
            BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncSeasonTaskRs.EXT_FIELD_NUMBER, GamePb4.SyncSeasonTaskRs.ext, builder.build()).build();
            playerService.syncMsgToPlayer(msg, player);
        }
    }

    /**
     * 检测商品在当前赛季是否可售卖
     *
     * @param seasonCfg [[1],[2,5]]
     * @throws MwException
     */
    public void checkSeasonItem(List<List<Integer>> seasonCfg) throws MwException {
        if (!CheckNull.isEmpty(seasonCfg)) {
            boolean notSeasonItem = seasonCfg.size() == 1 && seasonCfg.get(0).get(0) == 0;
            //赛季商品必须要在指定赛季开启期间才能购买
            if (!notSeasonItem) {
                if (getSeasonState() != SeasonConst.STATE_OPEN || !ListUtils.isInList(getCurrSeason(), seasonCfg)) {
                    throw new MwException(GameError.SEASON_NON_OPEN.getCode());
                }
            }
        }
    }

    /**
     * 获得赛季积分
     * 1、在endTime时发放未领取的旅程奖励里面有积分，但此刻添加积分已不在开放期；
     * 原本要求只能在开放期内才能获得积分，但为了兼容此逻辑则改为展示期也能获得积分
     *
     * @param player
     * @param count
     * @param awardFrom
     * @param param
     */
    public void updateSeasonScore(Player player, int count, AwardFrom awardFrom, Object... param) {
        int state = this.getSeasonState();
        if (state != SeasonConst.STATE_OPEN && state != SeasonConst.STATE_DISPLAY) {
            return;
        }
        PlayerSeasonData playerSeasonData = player.getPlayerSeasonData();
        playerSeasonData.setSeasonScore(playerSeasonData.getSeasonScore() + count);
        int now = TimeHelper.getCurrentSecond();
        playerSeasonData.setSeasonScoreTime(now);

        this.updatePlayerRank(player, now);

        //更新所属阵营积分
        this.updateCampRank(player, count);
        LogLordHelper.activityScore("seasonScore", awardFrom, player, playerSeasonData.getSeasonScore(), count, null);
    }

    private void updateCampRank(Player player, int count) {
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        CampRankData campRankdata = globalSeasonData.getCampRank().stream().filter(tmp -> tmp.camp == player.getCamp()).findFirst().orElse(null);
        if (campRankdata == null) {
            campRankdata = new CampRankData(player.getCamp(), count, TimeHelper.getCurrentSecond(), 0);
            globalSeasonData.getCampRank().add(campRankdata);
        } else {
            campRankdata.value = campRankdata.value + count;
            campRankdata.time = TimeHelper.getCurrentSecond();
        }
//        globalSeasonData.getCampRank().sort(COMPARATOR_CAMP_RANK);
    }

    private CampRankData getCampRankData(int camp, List<CampRankData> sortedList) {
        int i = 1;
        for (CampRankData campRankData : sortedList) {
            campRankData.rank = i;
            if (camp == campRankData.camp) {
                return campRankData;
            }
            i++;
        }
        return null;
    }

    private static final Comparator<CampRankData> COMPARATOR_CAMP_RANK = (o1, o2) -> {
        if (o1.value < o2.value) {
            return 1;
        } else if (o1.value > o2.value) {
            return -1;
        } else {
            if (o1.time > o2.time) {
                return 1;
            } else if (o1.time < o2.time) {
                return -1;
            }
        }
        return 0;
    };

    public int getSeasonScore(Player player) {
        return player.getPlayerSeasonData().getSeasonScore();
    }

    public void updatePlayerRank(Player player, int now) {
        int newValue = player.getPlayerSeasonData().getSeasonScore();
        if (newValue <= 0) {
            return;
        }
        boolean isSort = false;
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        LinkedList<ActRank> ranks = globalSeasonData.getRanks();
        if (ranks.isEmpty()) {
            ranks.add(new ActRank(player.roleId, 0, newValue, now));
            return;
        } else {
            ActRank myRank = null;
            for (ActRank rank : ranks) {
                if (rank.getLordId() == player.roleId) {
                    myRank = rank;
                    break;
                }
            }
            if (Objects.isNull(myRank)) {
                ranks.add(new ActRank(player.roleId, 0, newValue, now));
                isSort = true;
            } else {
                if (myRank.getRankValue() != newValue) {
                    myRank.setRankValue(newValue);
                    myRank.setRankTime(now);
                    isSort = true;
                }
            }
        }
        if (isSort) {
            ranks.sort(RANK1);
        }
        if (ranks.size() > MAX_RANK_NUM) {
            ranks.removeLast();
        }
    }

    private static final int MAX_RANK_NUM = 1000000;
    private static final Comparator<ActRank> RANK1 = (o1, o2) -> {
        if (o1.getRankValue() < o2.getRankValue()) {
            return 1;
        } else if (o1.getRankValue() > o2.getRankValue()) {
            return -1;
        } else {
            // 数值相等的情况下，不再比较id，比较上榜时间，先上榜排在前面
            if (o1.getRankTime() > o2.getRankTime()) {
                return 1;
            } else if (o1.getRankTime() < o2.getRankTime()) {
                return -1;
            }
        }
        return 0;
    };

    public GamePb4.SeasonGetRankRs getSeasonRank(long roleId, int page) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int currSeason = this.getCurrSeason();
        if (currSeason == 0) {
            throw new MwException(GameError.SEASON_NON_OPEN.getCode(), GameError.SEASON_NON_OPEN.errMsg(roleId));
        }
        GamePb4.SeasonGetRankRs.Builder resp = GamePb4.SeasonGetRankRs.newBuilder();
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        ActRank myRank = globalSeasonData.getRank(player.roleId);
        if (Objects.nonNull(myRank)) {
            resp.setMyRank(PbHelper.createActRank(myRank, player.lord.getNick(), player.lord.getCamp(), player.lord.getPortrait(), player.getDressUp().getCurPortraitFrame()));
        }
        int pageSize = 10;
        int maxSize = StaticIniDataMgr.getSeasonRankMax().getOrDefault(currSeason, 100);
        int size_ = globalSeasonData.getRanks().size();
        size_ = size_ > maxSize ? maxSize : size_;
        int a = size_ % pageSize;
        int b = size_ / pageSize;
        int totalPage_ = a == 0 ? b : b + 1;
        if (page > totalPage_) {

        } else {
            int startIdx = (page - 1) * pageSize;
            int endIdx = pageSize * page;

            for (int i = 0; i < size_; i++) {
                if (i < startIdx) {
                    continue;
                }
                if (i >= endIdx) {
                    break;
                }
                ActRank actRank = globalSeasonData.getRanks().get(i);
                actRank.setRank(i + 1);
                Player p = playerDataManager.getPlayer(actRank.getLordId());
                resp.addRanks(PbHelper.createActRank(actRank, p.lord.getNick(), p.lord.getCamp(), p.lord.getPortrait(), player.getDressUp().getCurPortraitFrame()));
            }
        }

        int j = 1;
        globalSeasonData.getCampRank().sort(COMPARATOR_CAMP_RANK);
        for (CampRankData campRankData : globalSeasonData.getCampRank()) {
            campRankData.rank = j;
            resp.addCampRank(globalSeasonData.buildCampRankInfo(campRankData));
            j++;
        }
        return resp.build();
    }

    public void loadRankDataOnStartup() {
        playerDataManager.getPlayers().values().forEach(player -> {
            if (player.getPlayerSeasonData().getSeasonScore() > 0) {
                this.updatePlayerRank(player, player.getPlayerSeasonData().getSeasonScoreTime());
                // 更新所属阵营积分
                this.updateCampRank(player, player.getPlayerSeasonData().getSeasonScore());
            }
        });
    }

    public boolean checkPropUse(StaticProp staticProp) {
        if (staticProp.getSeason() > 0) {
            GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
            StaticSeasonPlan staticSeasonPlan = StaticIniDataMgr.getStaticSeasonPlanById(globalSeasonData.getLastSeasonId());
            int lastSeason = Objects.isNull(staticSeasonPlan) ? 0 : staticSeasonPlan.getSeason();
            if (lastSeason < staticProp.getSeason()) {
                LogUtil.error("使用道具的赛季小于开过的赛季, 开过的赛季=" + globalSeasonData.getLastSeasonId());
                return false;
            }
        }
        return true;
    }

    public void gm_setSeason() {
        Date now = new Date();
        int serverId = serverSetting.getServerID();
        StaticSeasonPlan staticSeasonPlan = StaticIniDataMgr.getStaticSeasonPlanList().stream().filter(tmp ->
                checkServerId(tmp.getServerId(), serverId) && now.after(tmp.getPreviewTime()) && now.before(tmp.getDisplayTime())).findFirst().orElse(null);
        if (Objects.nonNull(staticSeasonPlan)) {
            this.execJob4Pre(staticSeasonPlan.getId());
            this.addSeasonJob(ScheduleManager.getInstance().getSched(), staticSeasonPlan, now);
        }
    }

    public void gm_reload() {
        this.initSchedule(ScheduleManager.getInstance().getSched());
        DataResource.getBean(SeasonTalentService.class).initSchedule(ScheduleManager.getInstance().getSched());
        syncSeasonInfo();
    }

    public void gm_seasonOver() {
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        int currSeasonId = globalSeasonData.getCurrSeasonId();
        this.execJob4Over(globalSeasonData.getCurrSeasonId());
        if (currSeasonId != 0) {
            QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(), SeasonJob.NAME_PRE + currSeasonId, SeasonJob.GROUP_SEASON);
            QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(), SeasonJob.NAME_BEGIN + currSeasonId, SeasonJob.GROUP_SEASON);
            QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(), SeasonJob.NAME_END + currSeasonId, SeasonJob.GROUP_SEASON);
            QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(), SeasonJob.NAME_OVER + currSeasonId, SeasonJob.GROUP_SEASON);
        }
    }

    public void gm_seasonEnd() {
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        int currSeasonId = globalSeasonData.getCurrSeasonId();
        this.execJob4End(currSeasonId);
    }

    public void gm_clearSeason() {
        GlobalSeasonData globalSeasonData = globalDataManager.getGameGlobal().getGlobalSeasonData();
        globalSeasonData.clear();
        syncSeasonInfo();
    }

    // <editor-fold desc="自己测试用的方法" defaultstate="collapsed">
    public void test_protocol(Player player, String... params) throws Exception {
        if (params[1].equalsIgnoreCase("fixCampRank")) {
            int state = this.getSeasonState();
            if (state != SeasonConst.STATE_NON) {
                LinkedList<CampRankData> campRankDatas = globalDataManager.getGameGlobal().getGlobalSeasonData().getCampRank();
                Map<Integer, Integer> fixMap = new HashMap<>();
                for (Player target : playerDataManager.getPlayers().values()) {
                    if (target.getPlayerSeasonData().getSeasonScore() > 0) {
                        if (fixMap.containsKey(target.getCamp())) {
                            fixMap.put(target.getCamp(), fixMap.get(target.getCamp()) + target.getPlayerSeasonData().getSeasonScore());
                        } else {
                            fixMap.put(target.getCamp(), target.getPlayerSeasonData().getSeasonScore());
                        }
                    }
                }
                LogUtil.error("修复赛季阵营排行数据重新计算" + JSON.toJSONString(fixMap));
                for (Map.Entry<Integer, Integer> entry : fixMap.entrySet()) {
                    CampRankData currData = null;
                    for (CampRankData campRankData : campRankDatas) {
                        if (campRankData.camp == entry.getKey().intValue()) {
                            currData = campRankData;
                            break;
                        }
                    }
                    boolean isFix = false;
                    if (Objects.isNull(currData)) {
                        currData = new CampRankData(entry.getKey(), entry.getValue(), TimeHelper.getCurrentSecond(), 0);
                        campRankDatas.add(currData);
                        isFix = true;
                    } else {
                        if (currData.value != entry.getValue().intValue()) {
                            currData.value = entry.getValue();
                            isFix = true;
                        }
                    }
                    if (isFix) {
                        LogUtil.error("修复赛季阵营排行数据CampRankData=" + JSON.toJSONString(currData));
                    }
                }
                LogUtil.error("修复赛季阵营排行数据完成, " + JSON.toJSONString(globalDataManager.getGameGlobal().getGlobalSeasonData().getCampRank()));
            }
            LogUtil.error("修复赛季阵营排行数据赛季状态=" + state);
        }
        if (params[1].equalsIgnoreCase("clearCampRank")) {
            globalDataManager.getGameGlobal().getGlobalSeasonData().getCampRank().clear();
            LogUtil.error("测试用清除阵营排行数据, " + JSON.toJSONString(globalDataManager.getGameGlobal().getGlobalSeasonData().getCampRank()));
        }
        if (params[1].equalsIgnoreCase("setSeason")) {
            this.gm_setSeason();
        }
        if (params[1].equalsIgnoreCase("seasonEnd")) {
            this.gm_seasonEnd();
        }
        if (params[1].equalsIgnoreCase("seasonOver")) {
            this.gm_seasonOver();
        }
        if (params[1].equals("getSeasonInfo")) {
            LogUtil.c2sMessage(this.getSeasonInfo(player.roleId), player.roleId);
        }
        if (params[1].equals("getTreasuryInfo")) {
            LogUtil.c2sMessage(this.getTreasuryInfo(player.roleId), player.roleId);
        }
        if (params[1].equals("getSeasonTaskInfo")) {
            LogUtil.c2sMessage(this.getSeasonTaskInfo(player.roleId), player.roleId);
        }
        if (params[1].equals("task")) {
            int taskId = Integer.parseInt(params[2]);
        }
        if (params[1].equalsIgnoreCase("clear")) {
            this.gm_clearSeason();
        }
        if (params[1].equalsIgnoreCase("addscore")) {
            this.updateSeasonScore(player, Integer.parseInt(params[2]), AwardFrom.GM_SEND);
        }
        if (params[1].equalsIgnoreCase("allscore")) {
            playerDataManager.getPlayers().values().forEach(player1 -> this.updateSeasonScore(player, RandomUtil.randomIntIncludeEnd(1, 5000), null));
        }
    }
// </editor-fold>

}
