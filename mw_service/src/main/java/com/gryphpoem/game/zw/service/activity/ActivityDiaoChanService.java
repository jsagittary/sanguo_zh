package com.gryphpoem.game.zw.service.activity;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.quartz.jobs.ActEndJob;
import com.gryphpoem.game.zw.quartz.jobs.ActOverJob;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.ActivityTask;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.*;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.PlayerService;
import com.gryphpoem.game.zw.service.TaskService;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xwind
 * @date 2021/3/1
 */
@Service
public class ActivityDiaoChanService {

    private static final int TOTAL_SCORE = -2;
    private static final int BIYUE_SCORE = -3;

    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private MailDataManager mailDataManager;

    private void checkReqParams(Player player,int activityType) throws MwException {
        if(activityType != ActivityConst.ACT_DIAOCHAN && activityType != ActivityConst.ACT_SEASON_HERO){
            throw new MwException(GameError.PARAM_ERROR.getCode(), "参数错误, roleId:,", player.lord.getLordId(),", activityType:",activityType);
        }
    }

    public GamePb4.DiaoChanGetInfoRs getInfo(long roleId,int activityType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        this.checkReqParams(player,activityType);
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (Objects.isNull(activityBase)) {
            throw new MwException(GameError.DIAOCHAN_ACTIVITY_NOT_OPEN.getCode(), "貂蝉活动未开启(ActivityBase=null), roleId:,", player.lord.getLordId());
        }
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (Objects.isNull(activity)) {
            throw new MwException(GameError.DIAOCHAN_ACTIVITY_NOT_OPEN.getCode(), "貂蝉活动未开启(Activity=null), roleId:,", player.lord.getLordId());
        }
        int step = activityBase.getStep0();
        if(step == ActivityConst.OPEN_CLOSE){
            throw new MwException(GameError.DIAOCHAN_ACTIVITY_NOT_OPEN.getCode(), "貂蝉活动未开启, roleId:,", player.lord.getLordId(),", step=" + activityBase.getStep());
        }

        int day ;
        if(step == ActivityConst.DISPLAY_OPEN){
            day = 1;
        }else if(step == ActivityConst.OPEN_STEP){
            day = this.getDay(activityBase);
        }else{
            day = StaticIniDataMgr.getDiaoChanMaxDay(activityBase.getActivityId());
        }

        List<StaticDiaoChanDayTask> taskList = StaticIniDataMgr.getStaticDiaoChanDayTaskByDay(activityBase.getActivityId(),day);
        if (ListUtils.isBlank(taskList)) {
            throw new MwException(GameError.DIAOCHAN_NO_CONFIG.getCode(), String.format("貂蝉活动任务配置找不到, roleId:%s, 活动id=%s, day=%s",player.lord.getLordId(),activityBase.getActivityId(),day));
        }

        GamePb4.DiaoChanGetInfoRs.Builder resp = GamePb4.DiaoChanGetInfoRs.newBuilder();
        resp.setDay(day);
        taskList.forEach(o -> {
            CommonPb.DiaoChanTaskInfo.Builder diaoChanTaskInfo = CommonPb.DiaoChanTaskInfo.newBuilder();
            diaoChanTaskInfo.setTaskId(o.getId());
            diaoChanTaskInfo.setProgress(getTaskProgress(activity, o.getTaskid()));
            resp.addTaskInfo(diaoChanTaskInfo);
        });
        activity.getStatusMap().entrySet().forEach(o -> {
            if (o.getKey() > 0) {
                resp.addAwardState(PbHelper.createTwoIntPb(o.getKey(), o.getValue()));
            }
        });
        resp.setTodayScore(getRankDayScore(activity, day));
        resp.setTotalScore(getTotalScore(activity));
        resp.setBiyueScore(getBiyueScore(activity));
        activity.getStatusMap().entrySet().forEach(o -> {
            if (o.getKey() < 0) {
                resp.addBiyueAwardState(PbHelper.createTwoIntPb(Math.abs(o.getKey()), o.getValue()));
            }
        });
        resp.setDayOverStamp(TimeHelper.getDayEndStamp());
        resp.setActivityType(activityType);
        return resp.build();
    }

    public GamePb4.DiaoChanGetAwardRs getAward(long roleId, int type, int awardId,int activityType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        this.checkReqParams(player,activityType);
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (Objects.isNull(activityBase)) {
            throw new MwException(GameError.DIAOCHAN_ACTIVITY_NOT_OPEN.getCode(), "貂蝉活动领取奖励(ActivityBase=null), roleId:,", player.lord.getLordId(), " type:", type);
        }
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (Objects.isNull(activity)) {
            throw new MwException(GameError.DIAOCHAN_ACTIVITY_NOT_OPEN.getCode(), "貂蝉活动领取奖励(Activity=null), roleId:,", player.lord.getLordId(), " type:", type);
        }
        if(activityBase.getStep() == ActivityConst.OPEN_CLOSE){
            throw new MwException(GameError.DIAOCHAN_ACTIVITY_NOT_OPEN.getCode(), "貂蝉活动已结束, roleId:,", player.lord.getLordId(),", step=" + activityBase.getStep());
        }

        GamePb4.DiaoChanGetAwardRs.Builder resp = GamePb4.DiaoChanGetAwardRs.newBuilder();
        List<CommonPb.Award> awardList;
        if (type == 1) {//积分奖励
            StaticDiaoChanDay staticDiaoChanDay = StaticIniDataMgr.getStaticDiaoChanDay(awardId);
            if (Objects.isNull(staticDiaoChanDay)) {
                throw new MwException(GameError.DIAOCHAN_NO_CONFIG.getCode(), "貂蝉活动领取奖励配置找不到, roleId:,", player.lord.getLordId(), " type:", type);
            }
            int day = this.getDay(activityBase);
            if(day > 7){
                day = 7;
            }
            int todayScore = this.getDayScore(activity, day);
            if (todayScore < staticDiaoChanDay.getIntegral()) {
                throw new MwException(GameError.DIAOCHAN_DAYAWARD_SCORE_NOENOUGHT.getCode(), "貂蝉活动领取奖励不满足条件, roleId:,", player.lord.getLordId(), " type:", type, " awardId:", awardId);
            }
            if (getDayAwardState(activity, awardId) != 0) {
                throw new MwException(GameError.DIAOCHAN_DAYAWARD_GOT.getCode(), "貂蝉活动今日积分奖励已领取, roleId:,", player.lord.getLordId(), " type:", type);
            }
            awardList = rewardDataManager.sendReward(player, staticDiaoChanDay.getAward(), AwardFrom.ACTIVITY_DIAOCHAN_DAYSCORE_AWARD);

            activity.getStatusMap().put(awardId, 1);

            activity.getStatusMap().entrySet().forEach(o -> {
                if (o.getKey() > 0) {
                    resp.addAwardState(PbHelper.createTwoIntPb(o.getKey(), o.getValue()));
                }
            });
            resp.addAllAward(awardList);
        } else if (type == 2) {//闭月奖励
            if(activityType == ActivityConst.ACT_SEASON_HERO){
                throw new MwException(GameError.PARAM_ERROR.getCode(), "参数错误 赛季英雄活动没有闭月奖励, roleId:,", player.lord.getLordId(), " type:", type);
            }
            StaticDiaoChanAward staticDiaoChanAward = StaticIniDataMgr.getStaticDiaoChanAward(awardId);
            if (Objects.isNull(staticDiaoChanAward)) {
                throw new MwException(GameError.DIAOCHAN_NO_CONFIG.getCode(), "貂蝉活动领取奖励配置找不到, roleId:,", player.lord.getLordId(), " type:", type);
            }
            int count = getBiyueScore(activity);
            if (count < staticDiaoChanAward.getCondition()) {
                throw new MwException(GameError.DIAOCHAN_DAYAWARD_SCORE_NOENOUGHT.getCode(), "貂蝉活动领取奖励不满足条件, roleId:,", player.lord.getLordId(), " type:", type, " awardId:", awardId);
            }
            if (getKeepsakeAwardState(activity, awardId) != 0) {
                throw new MwException(GameError.DIAOCHAN_DAYAWARD_GOT.getCode(), "貂蝉活动信物奖励已领取, roleId:,", player.lord.getLordId(), " type:", type);
            }
            awardList = rewardDataManager.sendReward(player, staticDiaoChanAward.getAward(), AwardFrom.ACTIVITY_DIAOCHAN_BIYUE_AWARD);

            activity.getStatusMap().put(awardId * -1, 1);

            activity.getStatusMap().entrySet().forEach(o -> {
                if (o.getKey() < 0) {
                    resp.addAwardState(PbHelper.createTwoIntPb(Math.abs(o.getKey()), o.getValue()));
                }
            });
            resp.addAllAward(awardList);
        } else {
            throw new MwException(GameError.DIAOCHAN_DAYAWARD_GOT.getCode(), "貂蝉活动领取奖励类型错误, roleId:,", player.lord.getLordId(), " type:", type);
        }
        resp.setActivityType(activityType);
        return resp.build();
    }

    private int getKeepsakeAwardState(Activity activity, int awardId) {
        return activity.getStatusMap().getOrDefault(awardId * -1, 0);
    }

    private int getDayAwardState(Activity activity, int awardId) {
        return activity.getStatusMap().getOrDefault(awardId, 0);
    }

    public int getDayRankKey(int type,int sub){
        return 100000000 + type * 100 + sub;
    }

    public int getActivityTypeByDayRankKey(int key){
        return (key - 100000000) / 100;
    }

    public GamePb4.DiaoChanGetRankInfoRs getRankInfo(long roleId, int type, int day,int activityType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        this.checkReqParams(player,activityType);
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (Objects.isNull(activityBase)) {
            throw new MwException(GameError.DIAOCHAN_ACTIVITY_NOT_OPEN.getCode(), "貂蝉活动获取排行榜数据(ActivityBase=null), roleId:,", player.lord.getLordId());
        }
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (Objects.isNull(activity)) {
            throw new MwException(GameError.DIAOCHAN_ACTIVITY_NOT_OPEN.getCode(), "貂蝉活动获取排行榜数据(Activity=null), roleId:,", player.lord.getLordId());
        }

        GlobalActivityData gActDate = activityDataManager.getGlobalActivity(activityType);
        if (gActDate == null) {
            throw new MwException(GameError.DIAOCHAN_RANK_NOGLOBAL.getCode(), "貂蝉活动获取排行榜数据(GlobalActivityData=null), roleId:", player.roleId);
        }

        GamePb4.DiaoChanGetRankInfoRs.Builder resp = GamePb4.DiaoChanGetRankInfoRs.newBuilder();
        int showSize = 10;
        int i = 0;
        LinkedList<ActRank> rankList;
        ActRank myRank;
        if (type == 1) {
            if(day < 1){
                day = 1;
            }
            if(day > 7){
                day = 7;
            }
            int type_ = getDayRankKey(activityType,day);
            rankList = gActDate.getPlayerRanks(null, type_);
            myRank = gActDate.getPlayerRank(player, type_, player.roleId);
            resp.setMyRank(PbHelper.createTwoIntPb(myRank == null ? 0 : myRank.getRank(), getDayScore(activity,day)));
        } else if (type == 2) {
            rankList = gActDate.getPlayerRanks(null, activityType);
            myRank = gActDate.getPlayerRank(player, activityType, player.roleId);
            resp.setMyRank(PbHelper.createTwoIntPb(myRank == null ? 0 : myRank.getRank(), getTotalScore(activity)));
        } else {
            rankList = null;
        }
        if(rankList != null){
            for (ActRank actRank : rankList) {
                if (i >= showSize) {
                    break;
                }
                actRank.setRank(i + 1);
                Player p = playerDataManager.getPlayer(actRank.getLordId());
                actRank.setParam(actRank.getRankValue() + "");
                resp.addRanks(PbHelper.createActRank(actRank, p.lord.getNick(), p.lord.getCamp(), p.lord.getPortrait(), p.getDressUp().getCurPortraitFrame()));
                i++;
            }
        }
        resp.setActivityType(activityType);
        return resp.build();
    }

    public static void completeTask(Player player, ETask eTask, int... params) {
        try {
            DataResource.ac.getBean(ActivityDiaoChanService.class).completeTask0(player, eTask, ActivityConst.ACT_DIAOCHAN, params);
            DataResource.ac.getBean(ActivityDiaoChanService.class).completeTask0(player, eTask, ActivityConst.ACT_SEASON_HERO, params);
        }catch (Exception e) {
            LogUtil.error("执行貂蝉任务发生错误, ",e);
        }
    }

    private void completeTask0(Player player, ETask eTask, int activityType, int... params) {
        if(!functionIsOpen(player,activityType)){
            return;
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (Objects.isNull(activityBase) || activityBase.getStep() != ActivityConst.OPEN_STEP) {
            return;
        }
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (Objects.isNull(activity)) {
            return;
        }

        int day = this.getDay(activityBase);
        List<StaticDiaoChanDayTask> taskList = StaticIniDataMgr.getStaticDiaoChanDayTaskByDay(activityBase.getActivityId(),day);
        if (ListUtils.isBlank(taskList)) {
            return;
        }
        List<ActivityTask> changeList = new ArrayList<>();
        taskList.forEach(ctask -> {
            if (ctask.getTaskid() == eTask.getTaskType()) {
                ActivityTask activityTask = this.getActivityTask(activity, day, ctask.getId());
                if (ctask.getNum() > 0 && activityTask.getCount() >= ctask.getNum()) {
                    return;
                }
                if (checkTaskCondition(player, activityTask, eTask, ctask, params)) {
                    int addScore = checkTaskFinished(player,activityTask,eTask,ctask);
                    if (addScore > 0) {
                        this.updateScore(player, activity, addScore, day);
                    }
                    changeList.add(activityTask);
                }
            }
        });
        if (!ListUtils.isBlank(changeList)) {
            this.syncTaskInfo(player, activity, changeList, day,activityType);
        }
    }

    private boolean checkTaskCondition(Player player, ActivityTask activityTask, ETask eTask, StaticDiaoChanDayTask config, int... params) {
        boolean b = false;
        Hero hero;
        switch (eTask) {
            case FIGHT_REBEL:
            case FIGHT_ELITE_REBEL:
                if (params[1] >= config.getParam().get(0) && params[1] <= config.getParam().get(1)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + 1);
                }
                break;
            case JOIN_CITY_WAR:
            case JOIN_ACTIVITY:
            case FINISHED_TASK:
                if (params[0] == config.getParam().get(0)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + 1);
                }
                break;
            case PASS_BARRIER:
            case PASS_EXPEDITION:
            case CONSUME_DIAMOND:
            case RECHARGE_DIAMOND:
            case DEATH_NUMBER:
            case KILLED_NUMBER:
            case ARMY_MAK_LOST:
                b = true;
                activityTask.setProgress(activityTask.getProgress() + params[0]);
                break;
            case CONSUME_ITEM:
            case COLLECT_RES:
                if (params[0] == config.getParam().get(1)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + params[1]);
                }
                break;
            case BUILD_UP:
                int buildLv = BuildingDataManager.getBuildingLv(config.getParam().get(0), player);
                if(buildLv > activityTask.getProgress()){
                    b = true;
                    activityTask.setProgress(buildLv);
                }
                break;
            case TECHNOLOGY_UP:
                int technologyLv = player.tech.getTechLvById(config.getParam().get(0));
                if(technologyLv > activityTask.getProgress()){
                    b = true;
                    activityTask.setProgress(technologyLv);
                }
                break;
            case MAKE_EQUIP:
            case GET_TASKAWARD:
                if (params[1] == config.getParam().get(0)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + 1);
                }
                break;
            case REFORM_EQUIP:
            case HERO_TRAINING:
            case DAILY_LOGIN:
            case CITY_FIRSTKILLED:
            case APPOINTMENT:
            case TRAINING_HIGH:
            case TRAINING_LOW:
            case FINISHED_DAILYTASK:
            case BUILD_CAMP:
            case HITFLY_PLAYER:
                b = true;
                activityTask.setProgress(activityTask.getProgress() + 1);
                break;
            case GET_ITEM:
                if (params[0] == config.getParam().get(0)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + params[2]);
                }
                break;
            case ARTIFACT_UP:
                SuperEquip superEquip = player.supEquips.get(config.getParam().get(0));
                if(superEquip != null && superEquip.getLv() > activityTask.getProgress()){
                    b = true;
                    activityTask.setProgress(superEquip.getLv());
                }
                break;
            case BEAUTY_GIFT:
                if ((config.getParam().get(0) == 0 && (config.getParam().get(1) == 0 || params[1] == config.getParam().get(1))) ||
                        (params[0] == config.getParam().get(0) && (config.getParam().get(1) == 0 || params[1] == config.getParam().get(1)))) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + params[2]);
                }
                break;
            case MAKE_ARMY:
                if (config.getParam().get(0) == 0 || params[0] == config.getParam().get(0)) {
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + params[1]);
                }
                break;
            case BEAUTY_INTIMACY:
                AtomicInteger femaleAgentExp = new AtomicInteger(0);
                Optional.ofNullable(player.getCia()).ifPresent(cia ->
                        Optional.ofNullable(cia.getFemaleAngets().get(config.getParam().get(0))).ifPresent(femaleAgent ->
                                femaleAgentExp.set(femaleAgent.getExp())));
                if(femaleAgentExp.get() > activityTask.getProgress()){
                    b = true;
                    activityTask.setProgress(femaleAgentExp.get());
                }
                break;
            case ORNAMENT_COUNT:
                int stoneCount = 0;
                for (Stone stone : player.getStoneInfo().getStones().values()) {
                    StaticStone sStone = StaticPropDataMgr.getStoneMapById(stone.getStoneId());
                    if (sStone.getLv() >= config.getParam().get(0)) {
                        stoneCount += stone.getCnt();
                    }
                }
                for (StoneHole stoneHole : player.getStoneInfo().getStoneHoles().values()) {
                    StaticStone sStone = StaticPropDataMgr.getStoneMapById(stoneHole.getStoneId());
                    if (sStone.getLv() >= config.getParam().get(0)) {
                        stoneCount += 1;
                    }
                }
                if (activityTask.getProgress() < stoneCount) {
                    b = true;
                    activityTask.setProgress(stoneCount);
                }
                break;
            case TITLE_LV:
                if(player.lord.getRanks() > activityTask.getProgress()){
                    b = true;
                    activityTask.setProgress(player.lord.getRanks());
                }
                break;
            case OWN_BEAUTY:
                long count = player.getCia().getFemaleAngets().values().stream().filter(o -> o.getStatus() == 2 && o.getStar() >= config.getParam().get(1)).count();
                if (count > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress((int) count);
                }
                break;
            case OWN_HERO:
                int heroCount = (int) player.heros.values().stream().map(tmp -> StaticHeroDataMgr.getHeroMap().get(tmp.getHeroId())).filter(staticHero -> staticHero.getQuality() == config.getParam().get(1)).count();
                if (heroCount > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress(heroCount);
                }
                break;
            case PLAYER_LV:
                if (player.lord.getLevel() > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress(player.lord.getLevel());
                }
                break;
            case PLAYER_POWER:
                if (player.lord.getFight() > activityTask.getProgress()) {
                    b = true;
                    activityTask.setProgress((int) player.lord.getFight());
                }
                break;
            case TRADE_TIMES:
                if (params[0] == config.getParam().get(1)) {
                    activityTask.setProgress(activityTask.getProgress() + 1);
                }
                break;
            case GET_HERO:
                if(Objects.nonNull(player.heros.get(config.getParam().get(0)))){
                    b = true;
                    activityTask.setProgress(activityTask.getProgress() + 1);
                }
                break;
            case HERO_UPSTAR:
                hero = player.heros.get(config.getParam().get(0));
                if (Objects.nonNull(hero)) {
                    b = true;
                    activityTask.setProgress(hero.getCgyStage());
                }
                break;
            case HERO_UPSKILL:
                hero = player.heros.get(config.getParam().get(0));
                if (Objects.nonNull(hero)) {
                    b = true;
                    activityTask.setProgress(hero.getSkillLevels().getOrDefault(config.getParam().get(1), 0));
                }
                break;
            case HERO_LEVELUP:
                hero = player.heros.get(config.getParam().get(0));
                if (Objects.nonNull(hero)) {
                    b = true;
                    activityTask.setProgress(hero.getLevel());
                }
                break;
            default:
        }
        return b;
    }

    private int checkTaskFinished(Player player,ActivityTask activityTask, ETask eTask, StaticDiaoChanDayTask config) {
        int addScore = 0;
        int count;
        int remainder;
        Hero hero;
        switch (eTask) {
            case FIGHT_REBEL:
            case FIGHT_ELITE_REBEL:
            case COLLECT_RES:
                count = activityTask.getProgress() / config.getParam().get(2);
                if (count > 0) {
                    int a = count + activityTask.getCount();
                    count = a > config.getNum() ? config.getNum() - activityTask.getCount() : count;
                    activityTask.setCount(activityTask.getCount() + count);
                    remainder = activityTask.getProgress() % config.getParam().get(2);
                    activityTask.setProgress(remainder);
                    addScore = count * config.getIntegral();
                }
                break;
            case JOIN_CITY_WAR:
            case MAKE_EQUIP:
            case JOIN_ACTIVITY:
            case FINISHED_TASK:
            case GET_TASKAWARD:
                count = activityTask.getProgress() / config.getParam().get(1);
                if (count > 0) {
                    activityTask.setCount(activityTask.getCount() + count);
                    remainder = activityTask.getProgress() % config.getParam().get(1);
                    activityTask.setProgress(remainder);
                    addScore = count * config.getIntegral();
                }
                break;
            case PASS_BARRIER:
            case PASS_EXPEDITION:
            case REFORM_EQUIP:
            case CONSUME_DIAMOND:
            case TRAINING_HIGH:
            case TRAINING_LOW:
            case KILLED_NUMBER:
            case DEATH_NUMBER:
            case ARMY_MAK_LOST:
                count = activityTask.getProgress() / config.getParam().get(0);
                if (count > 0) {
                    activityTask.setCount(activityTask.getCount() + count);
                    remainder = activityTask.getProgress() % config.getParam().get(0);
                    activityTask.setProgress(remainder);
                    addScore = count * config.getIntegral();
                }
                break;
            case CONSUME_ITEM:
            case BUILD_UP:
            case TECHNOLOGY_UP:
            case HERO_TRAINING:
            case GET_ITEM:
            case ARTIFACT_UP:
            case BEAUTY_GIFT:
            case MAKE_ARMY:
            case BEAUTY_INTIMACY:
            case DAILY_LOGIN:
            case CITY_FIRSTKILLED:
            case ORNAMENT_COUNT:
            case APPOINTMENT:
            case RECHARGE_DIAMOND:
            case TITLE_LV:
            case FINISHED_DAILYTASK:
            case OWN_BEAUTY:
            case OWN_HERO:
            case BUILD_CAMP:
            case HITFLY_PLAYER:
            case PLAYER_LV:
            case PLAYER_POWER:
            case TRADE_TIMES:
                break;
            case GET_HERO:
                if (activityTask.getProgress() >= 1) {
                    activityTask.setCount(activityTask.getCount() + 1);
                    activityTask.setProgress(0);
                    addScore = config.getIntegral();
                }
                break;
            case HERO_UPSTAR:
                hero = player.heros.get(config.getParam().get(0));
                if (Objects.nonNull(hero)) {
                    if(hero.getCgyStage() > config.getParam().get(1) || (hero.getCgyStage() == config.getParam().get(1) && hero.getCgyLv() >= config.getParam().get(2))){
                        activityTask.setCount(activityTask.getCount() + 1);
                        activityTask.setProgress(0);
                        addScore = config.getIntegral();
                    }
                }
                break;
            case HERO_LEVELUP:
                if(activityTask.getProgress() >= config.getParam().get(1)){
                    activityTask.setCount(activityTask.getCount() + 1);
                    activityTask.setProgress(0);
                    addScore = config.getIntegral();
                }
                break;
            case HERO_UPSKILL:
                if(activityTask.getProgress() >= config.getParam().get(2)){
                    activityTask.setCount(activityTask.getCount() + 1);
                    activityTask.setProgress(0);
                    addScore = config.getIntegral();
                }
                break;
            default:
        }
        return addScore;
    }

    private ActivityTask getActivityTask(Activity activity, int day, int taskId) {
        List<ActivityTask> tasks = activity.getDayTasks().get(day);
        if (tasks == null) {
            tasks = new ArrayList<>();
            activity.getDayTasks().put(day, tasks);
        }
        ActivityTask activityTask = null;
        for (ActivityTask task : tasks) {
            if (task.getTaskId() == taskId) {
                activityTask = task;
                break;
            }
        }
        if (activityTask == null) {
            activityTask = new ActivityTask(taskId, 0, 0);
            tasks.add(activityTask);
        }
        return activityTask;
    }

    private void syncTaskInfo(Player player, Activity activity, List<ActivityTask> changes, int day,int activityType) {
        GamePb4.SyncDiaoChanTaskInfoRs.Builder builder = GamePb4.SyncDiaoChanTaskInfoRs.newBuilder();
        Optional.ofNullable(changes).ifPresent(tmps -> tmps.forEach(o -> {
            CommonPb.DiaoChanTaskInfo.Builder diaoChanTaskInfo = CommonPb.DiaoChanTaskInfo.newBuilder();
            diaoChanTaskInfo.setTaskId(o.getTaskId());
            diaoChanTaskInfo.setProgress(o.getCount());
            builder.addTaskInfo(diaoChanTaskInfo);
        }));
        builder.setDayScore(this.getDayScore(activity, day));
        builder.setActivityType(activityType);
        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncDiaoChanTaskInfoRs.EXT_FIELD_NUMBER, GamePb4.SyncDiaoChanTaskInfoRs.ext, builder.build()).build();
        playerService.syncMsgToPlayer(msg, player.roleId);
    }

    private void updateScore(Player player, Activity activity, int addScore, int day) {
        int totalScore = activity.getSaveMap().getOrDefault(TOTAL_SCORE, 0);
        activity.getSaveMap().put(TOTAL_SCORE, totalScore + addScore);
        int dayScore = this.getDayScore(activity, day);
        activity.getPropMap().put(day, dayScore + addScore);

        activityDataManager.updRankActivity(player, activity.getActivityType(), addScore);
        activityDataManager.updateDayRank(player, activity.getActivityType(), day, addScore);

        LogLordHelper.activityScore("diaochanScore",AwardFrom.ACTIVITY_DIAOCHAN_GET_SCORE,player,getTotalScore(activity),addScore,activity.getActivityType());
    }

    public int getDayScore(Activity activity, int day) {
        return activity.getPropMap().getOrDefault(day, 0);
    }

    public int getRankDayScore(Activity activity, int day) {
        Turple<Integer, Integer> turple = activity.getDayScore().get(day);
        if (turple == null) {
            turple = new Turple<>(0,0);
            activity.getDayScore().put(day, turple);
        }
        return turple.getA() == null ? 0 : turple.getA();
    }

    public int getTotalScore(Activity activity) {
        return activity.getSaveMap().getOrDefault(TOTAL_SCORE, 0);
    }

    private int getTaskProgress(Activity activity, int taskId) {
        return activity.getSaveMap().getOrDefault(taskId, 0);
    }

    private int getDay(ActivityBase activityBase) {
        int day = 0;
        if(activityBase.getOpenRule() == 0x1){
            int openDay = serverSetting.getOpenServerDay(new Date());
            day = openDay - activityBase.getPlan().getOpenBegin() + 1;
        }else {
            Date now = new Date();
//            if(now.after(activityBase.getBeginTime()) && now.before(activityBase.getEndTime())){
                day = (int) ((now.getTime() - activityBase.getBeginTime().getTime())/TimeHelper.DAY_MS + 1);
//            }
        }
        return day;
    }

//    private int getDay(ActivityBase activityBase,Date date){
//        int openDay = serverSetting.getOpenServerDay(date);
//        return openDay - activityBase.getPlan().getOpenBegin() + 1;
//    }

    private int getBiyueScore(Activity activity) {
        return activity.getSaveMap().getOrDefault(BIYUE_SCORE, 0);
    }

    public void updateBiyueScore(Player player, int count,AwardFrom awardFrom,Object... param) {
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_DIAOCHAN);
        if(activity != null){
            int old = getBiyueScore(activity);
            activity.getSaveMap().put(BIYUE_SCORE,old + count);

            LogLordHelper.activityScore("biyueScore",awardFrom,player,getBiyueScore(activity),count,ActivityConst.ACT_DIAOCHAN);
        }
    }

    public int getBiyueScore(Player player){
        Activity activity = activityDataManager.getActivityInfo(player,ActivityConst.ACT_DIAOCHAN);
        if(activity != null){
            return getBiyueScore(activity);
        }
        return 0;
    }

    /**
     * 跨天执行0:0:1
     *  1.处理昨天未领取的每日积分奖励
     *  2.处理昨天排行榜奖励
     *  3.处理今天的任务
     * @param player
     */
    public void handleAcrossDay(Player player) {
        this.handleAcrossDay0(player,ActivityConst.ACT_DIAOCHAN);
        this.handleAcrossDay0(player,ActivityConst.ACT_SEASON_HERO);
    }

    private void handleAcrossDay0(Player player,int activityType) {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (Objects.isNull(activityBase)) {
            return;
        }
        this.handleUnreward4AcrossDay(player,activityType);
        this.handleDayRankAward4AcrossDay(player,activityType);
        if (activityBase.getStep() == ActivityConst.OPEN_STEP) {
            this.handleTodayTask(player,activityType);
        }
    }

    /**
     * 活动 end time , 23:59:59
     * .处理总榜奖励
     * .处理闭月未领取的奖励
     * @param player
     */
    protected void handleEnd(Player player,int activityType,int activityId,int keyId){
//        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
//        if(Objects.isNull(activityBase)){
//            return;
//        }
        this.handleGeneralRankAward4Over(player,activityType,activityId);
        this.handleUnreward4Over(player,activityType,activityId,keyId);
    }

    /**
     * 活动 display time , 23:59:59
     *  1、自动兑换貂蝉活动的物品
     * @param player
     */
    protected void handleOver(Player player,int activityType,int activityId,int keyId){
//        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType0(ActivityConst.ACT_DIAOCHAN);
//        if(Objects.isNull(activityBase)){
//            return;
//        }

        this.handleConvert4Over(player,activityType,activityId,keyId);
    }

    private void  handleConvert4Over(Player player,int activityType,int activityId,int keyId){
        try {
            if (Objects.isNull(player)) {
                return;
            }
            StaticActVoucher sActVoucher = StaticActivityDataMgr.getActVoucherByActId(activityId);
            if (sActVoucher == null) {
                return;
            }
            int now = TimeHelper.getCurrentSecond();
//            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_DIAOCHAN);
//            if (activityBase == null) {
//                return;
//            }

            int chipNum = Math.toIntExact(rewardDataManager.getRoleResByType(player, sActVoucher.getConsume().get(0), sActVoucher.getConsume().get(1)));
            if (chipNum > 0) {
                rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, sActVoucher.getConsume().get(1), chipNum, AwardFrom.ACTIVITY_DIAOCHAN_OVER_AUTO_EXCHANGE, true, activityType, activityId);
                List<CommonPb.Award> awards = Collections.singletonList(PbHelper.createAwardPb(sActVoucher.getAwardList().get(0), sActVoucher.getAwardList().get(1), sActVoucher.getAwardList().get(2) * chipNum));
                mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_BANDIT_CONVERT_AWARD, AwardFrom.ACTIVITY_DIAOCHAN_OVER_AUTO_EXCHANGE, now, activityType, activityId);
                LogUtil.activity("貂蝉活动结束自动转换香囊道具, roleId=" + player.roleId + ", count=" + chipNum);
            }
        }catch (Exception e) {
            LogUtil.error("貂蝉活动结束自动转换香囊道具，发生错误", e);
        }
    }

    private void handleGeneralRankAward4Over(Player player,int activityType,int activityId){
        try {
            if (Objects.isNull(player)) {
                return;
            }
//            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
//            if (activityBase == null) {
//                return;
//            }
            GlobalActivityData globalActivityData = activityDataManager.getActivityMap().get(activityType);
            if (globalActivityData == null) {
                return;
            }
            ActRank myRank = globalActivityData.getPlayerRank(player,activityType,player.roleId);
            if(myRank != null){
                List<StaticDiaoChanRank> staticDiaoChanRankList = StaticIniDataMgr.getStaticDiaoChanRankListByDay(activityId,2,0);
                if(staticDiaoChanRankList != null){
                    StaticDiaoChanRank staticDiaoChanRank_ = staticDiaoChanRankList.stream().filter(o -> o.getRank().get(0) <= myRank.getRank() && o.getRank().get(1) >= myRank.getRank() && myRank.getRankValue() >= o.getLimit()).findFirst().orElse(null);
                    if(staticDiaoChanRank_ != null){
                        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player,staticDiaoChanRank_.getAward(),AwardFrom.ACTIVITY_DIAOCHAN_TOTAL_RANK_AWARD);
                        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ACT_DIAOCHAN_RANK_AWARD, awardList, TimeHelper.getCurrentSecond(),activityType,activityId,activityType,activityId, 0, myRank.getRank());
                        LogUtil.activity("貂蝉活动结束处理总榜奖励, roleId=" + player.roleId + ", award=" + JSON.toJSONString(staticDiaoChanRank_.getAward()));
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("貂蝉活动结束处理总榜奖励，发生错误", e);
        }
    }

    private void handleUnreward4Over(Player player,int activityType,int activityId,int keyId){
        try {
//            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType0(ActivityConst.ACT_DIAOCHAN);
//            if (activityBase == null) {
//                return;
//            }
            if(activityType != ActivityConst.ACT_DIAOCHAN){
                return;
            }
            List<StaticDiaoChanAward> staticDiaoChanAwardList = StaticIniDataMgr.getStaticDiaoChanAwardList(activityId);
            Activity activity = player.activitys.get(activityType);
            if(Objects.isNull(activity)){
                return;
            }
            int count = getBiyueScore(activity);
            List<CommonPb.Award> unrewardList = new ArrayList<>();
            staticDiaoChanAwardList.stream().filter(o -> count >= o.getCondition() && getKeepsakeAwardState(activity,o.getId()) == 0).forEach(o1 -> {
                unrewardList.addAll(PbHelper.createAwardsPb(o1.getAward()));
                activity.getStatusMap().put(o1.getId() * -1, 1);
            });
            if(ListUtils.isNotBlank(unrewardList)){
                rewardDataManager.sendRewardByAwardList(player,unrewardList,AwardFrom.ACTIVITY_DIAOCHAN_BIYUE_AWARD);
                mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ACT_UNREWARDED_REWARD, unrewardList, TimeHelper.getCurrentSecond(),activity.getActivityType(), activity.getActivityId(),activity.getActivityType(), activity.getActivityId());
                LogUtil.activity("貂蝉活动结束处理未领取奖励, roleId=" + player.roleId + ", size=" + unrewardList.size());
            }
        } catch (Exception e) {
            LogUtil.error("貂蝉活动结束处理未领取闭月奖励，发生错误", e);
        }
    }

    private void handleUnreward4AcrossDay(Player player,int activityType){
        try {
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
            if(Objects.isNull(activityBase)){
                return;
            }
            Activity activity = player.activitys.get(activityType);
            if(Objects.isNull(activity)){
                return;
            }
            int yesterday = this.getDay(activityBase) - 1;
            if(yesterday > StaticIniDataMgr.getDiaoChanMaxDay(activityBase.getActivityId()) || yesterday < 1){
                return;
            }
            List<StaticDiaoChanDay> staticDiaoChanDayList = StaticIniDataMgr.getStaticDiaoChanDayList(activityBase.getActivityId(),yesterday);
            if(!ListUtils.isBlank(staticDiaoChanDayList)){
                List<CommonPb.Award> notAwards = new ArrayList<>();
                List<List<Integer>> unrewards = new ArrayList<>();
                staticDiaoChanDayList.forEach(o -> {
                    if(checkDayScoreAwardGot(o,yesterday,activity,player)){
                        notAwards.addAll(PbHelper.createAwardsPb(o.getAward()));
                        unrewards.addAll(o.getAward());
                        activity.getStatusMap().put(o.getId(), 1);
                    }
                });
                if(!ListUtils.isBlank(notAwards)){
                    rewardDataManager.sendRewardByAwardList(player,notAwards,AwardFrom.ACTIVITY_DIAOCHAN_DAYSCORE_AWARD);
                    mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ACT_UNREWARDED_REWARD, notAwards, TimeHelper.getCurrentSecond(),activity.getActivityType(), activity.getActivityId(),activity.getActivityType(), activity.getActivityId());
                    LogUtil.activity("貂蝉活动跨天处理未领取的积分奖励, roleId=" + player.roleId + ", notAwards=" + JSON.toJSONString(unrewards));
                }
            }
        } catch (Exception e) {
            LogUtil.error("貂蝉活动跨天处理未领取的积分奖励发生错误, ", e);
        }
    }

    private void handleDayRankAward4AcrossDay(Player player,int activityType){
        try {
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
            if(Objects.isNull(activityBase)){
                return;
            }
            Activity activity = player.activitys.get(activityType);
            if(Objects.isNull(activity)){
                return;
            }
            int yesterday = this.getDay(activityBase) - 1;
            if(yesterday > StaticIniDataMgr.getDiaoChanMaxDay(activityBase.getActivityId()) || yesterday < 1){
                return;
            }
            GlobalActivityData globalActivityData = activityDataManager.getActivityMap().get(activityType);
            if (globalActivityData == null) {
                return;
            }
            int type_ = getDayRankKey(activityType,yesterday);
            ActRank myRank = globalActivityData.getPlayerRank(player, type_, player.roleId);
            Optional.ofNullable(myRank).ifPresent(myRank_ ->
                    Optional.ofNullable(StaticIniDataMgr.getStaticDiaoChanRankListByDay(activityBase.getActivityId(),1, yesterday)).ifPresent(tmps ->
                    Optional.ofNullable(tmps.stream().filter(o -> o.getRank().get(0) <= myRank.getRank() && o.getRank().get(1) >= myRank.getRank() && myRank.getRankValue() >= o.getLimit()).findFirst().orElse(null))
                            .ifPresent(o1 -> {
                                List<CommonPb.Award> awardList = rewardDataManager.sendReward(player,o1.getAward(),AwardFrom.ACTIVITY_DIAOCHAN_DAY_RANK_AWARD);
                                mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ACT_DIAOCHAN_RANK_AWARD, awardList, TimeHelper.getCurrentSecond(),activity.getActivityType(),activity.getActivityId(),activity.getActivityType(),activity.getActivityId(), yesterday, myRank.getRank());
                                LogUtil.activity("貂蝉活动处理昨天排名奖励, roleId=" + player.roleId + ", award=" + JSON.toJSONString(o1.getAward()));
                            })));
        } catch (Exception e) {
            LogUtil.error("貂蝉活动跨天处理日排名奖励发生错误, ", e);
        }
    }

    private boolean checkDayScoreAwardGot(StaticDiaoChanDay staticDiaoChanDay,int day,Activity activity,Player player){
        int score = this.getDayScore(activity,day);
        if(score >= staticDiaoChanDay.getIntegral() && getDayAwardState(activity, staticDiaoChanDay.getId()) == 0){
            return true;
        }
        return false;
    }

    public void handleTodayTask(Player player){
        this.handleTodayTask(player,ActivityConst.ACT_DIAOCHAN);
        this.handleTodayTask(player,ActivityConst.ACT_SEASON_HERO);
    }

    private void handleTodayTask(Player player,int activityType){
        try {
            if(!functionIsOpen(player,activityType)){
                return;
            }
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
            if(Objects.isNull(activityBase)){
                return;
            }
            Activity activity = activityDataManager.getActivityInfo(player, activityType);
            if(Objects.isNull(activity)){
                return;
            }
            for (ETask value : ETask.values()) {
                if(value.isHandle()){
                    ActivityDiaoChanService.completeTask(player, value);
                    TaskService.processTask(player, value);
                }
            }
        } catch (Exception e) {
            LogUtil.error("貂蝉活动跨天处理任务发生错误, ", e);
        }
    }

    public boolean functionIsOpen(Player player,int activityType){
        if(activityType == ActivityConst.ACT_DIAOCHAN){
            return StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ACTIVITY_DIAOCHAN);
        }else if(activityType == ActivityConst.ACT_SEASON_HERO){
            return StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ACTIVITY_SEASON_HERO);
        }
        return false;
    }

    private void killedAndDeathTask(Fighter fighter,boolean killed,boolean lost){
        Map<Player,Integer> playerKilledMap = new HashMap<>();
        Map<Player,Integer> playerLostMap = new HashMap<>();
        if(fighter.roleType == Constant.Role.PLAYER){
            fighter.getForces().forEach(force -> {
                Player p = playerDataManager.getPlayer(force.ownerId);
                if(p != null){
                    if(killed)
                        playerKilledMap.merge(p,force.killed,Integer::sum);
                    if(lost)
                        playerLostMap.merge(p,force.totalLost,Integer::sum);
                }
            });
        }
        playerKilledMap.entrySet().forEach(entry -> {
            ActivityDiaoChanService.completeTask(entry.getKey(), ETask.KILLED_NUMBER, entry.getValue());
            TaskService.processTask(entry.getKey(), ETask.KILLED_NUMBER, entry.getValue());
        });
        playerLostMap.entrySet().forEach(entry -> {
            ActivityDiaoChanService.completeTask(entry.getKey(),ETask.DEATH_NUMBER,entry.getValue());
            TaskService.processTask(entry.getKey(),ETask.DEATH_NUMBER,entry.getValue());
            TaskService.handleTask(entry.getKey(),ETask.ARMY_MAK_LOST,entry.getValue());
            ActivityDiaoChanService.completeTask(entry.getKey(),ETask.ARMY_MAK_LOST,entry.getValue());
            TaskService.processTask(entry.getKey(),ETask.ARMY_MAK_LOST,entry.getValue());
        });
    }

    public static void killedAndDeathTask0(Fighter fighter,boolean killed,boolean lost) {
        try {
            DataResource.ac.getBean(ActivityDiaoChanService.class).killedAndDeathTask(fighter, killed, lost);
        }catch (Exception e) {
            LogUtil.error("执行貂蝉杀敌阵亡任务发生错误, ",e);
        }
    }



    public int getDayRankLimit(int rankKey){
        int activityType = this.getActivityTypeByDayRankKey(rankKey);
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if(activityBase == null){
            return 2000;
        }
        int day = this.getDay(activityBase);
        if(day < 1){
            day = 1;
        }
        if(day > 7){
            day = 7;
        }
        List<StaticDiaoChanRank> list = StaticIniDataMgr.getStaticDiaoChanRankListByDay(activityBase.getActivityId(),1,day);
        if(ListUtils.isBlank(list)){
            return StaticIniDataMgr.getStaticDiaoChanRankListByDay(activityBase.getActivityId(),1,1).get(0).getLimit();
        }else {
            return list.get(0).getLimit();
        }
    }

    public int getRankLimit(int activityType){
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if(activityBase == null){
            return 5000;
        }
        List<StaticDiaoChanRank> list = StaticIniDataMgr.getStaticDiaoChanRankListByDay(activityBase.getActivityId(),2,0);
        if(ListUtils.isBlank(list)){
            return StaticIniDataMgr.getStaticDiaoChanRankListByDay(activityBase.getActivityId(),2,0).get(0).getLimit();
        }else {
            return list.get(0).getLimit();
        }
    }

    public static void addScheduleJob(ActivityBase activityBase, Date now, Scheduler sched){
        String jobName = activityBase.getActivityType() + "_" + activityBase.getActivityId() + "_" + activityBase.getPlan().getKeyId();
        if(activityBase.getEndTime().getTime() > now.getTime()){
            QuartzHelper.removeJob(sched,jobName,"ACT_END");
            long millis = activityBase.getEndTime().getTime() + 4000;
            QuartzHelper.addJob(sched,jobName,"ACT_END", ActEndJob.class,new Date(millis));
        }
        if(Objects.nonNull(activityBase.getDisplayTime()) && activityBase.getDisplayTime().getTime() > now.getTime()){
            QuartzHelper.removeJob(sched,jobName,"ACT_OVER");
            QuartzHelper.addJob(sched,jobName,"ACT_OVER", ActOverJob.class,activityBase.getDisplayTime());
        }
    }

// <editor-fold desc="自己测试用的方法" defaultstate="collapsed">
    public void test_protocol(Player player,String...params) throws Exception{
        int activityType = Integer.parseInt(params[1]);
        String cmd = params[2];
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        GlobalActivityData gActDate = activityDataManager.getGlobalActivity(activityType);
        if(activity == null || activityBase == null){
            return;
        }
        if(cmd.equalsIgnoreCase("getinfo")){
            LogUtil.c2sMessage(this.getInfo(player.roleId,activityType),player.roleId);
        }
        if(cmd.equalsIgnoreCase("getrank")){
            LogUtil.c2sMessage(this.getRankInfo(player.roleId,Integer.parseInt(params[2]),Integer.parseInt(params[3]),activityType),player.roleId);
        }
        if(cmd.equalsIgnoreCase("addscore")){
            this.updateScore(player,activity,Integer.parseInt(params[3]),this.getDay(activityBase));
            syncTaskInfo(player,activity,null,this.getDay(activityBase),activityType);
        }
        if(cmd.equalsIgnoreCase("sendmail")){
            if(params[3].equalsIgnoreCase("day")){
                this.handleAcrossDay(player);
            }
            if(params[3].equalsIgnoreCase("end")){
//                this.handleOver(player);
            }
        }
        if(cmd.equalsIgnoreCase("clear")){
            activity.getDayTasks().clear();
            activity.getDayScore().clear();
            activity.getStatusMap().clear();
            activity.getSaveMap().clear();
            activity.getPropMap().clear();
            activity.getStatusCnt().clear();
            gActDate.getRanks().clear();
        }
    }
// </editor-fold>

}
