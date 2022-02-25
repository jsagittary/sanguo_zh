package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAutumnDayTask;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.TaskFinishService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 喜悦金秋活动 - 日出而作
 *
 * @author liuyc
 * @date 2021/9/11
 */
@Service
public class GoldenAutumnSunriseService extends GoldenAutumnService implements TaskFinishService {

    /**
     * 构建日出而作数据信息
     */
    @Override
    public GeneratedMessage buildGoldenAutumnInfo(Player player, Activity activity) {
        CommonPb.GoldenAutumnSunrise.Builder goldenAutumnSunrise = CommonPb.GoldenAutumnSunrise.newBuilder();
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (!super.isOpenStage(activityBase)) {
            return goldenAutumnSunrise.build();
        }
        goldenAutumnSunrise.setBookmark(2);
        goldenAutumnSunrise.setTreChestIntegral(activity.getStatusMap().getOrDefault(2, 0));//statusMap的key=2用于存储日出而作的宝箱积分
        //获取所有积分奖励配置
        List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        if (null != actAwardList && !actAwardList.isEmpty())
        {
            for (StaticActAward staticActAward : actAwardList)
            {
                CommonPb.GoldenAutumnSunriseTreChest.Builder chest = CommonPb.GoldenAutumnSunriseTreChest.newBuilder();
                chest.setKeyId(staticActAward.getKeyId());
                chest.setAwardStatus(false);
                int awardStatus = activity.getStatusMap().getOrDefault(staticActAward.getKeyId(), 0);//statusMap的key=宝箱配置id 用于存储宝箱领取状态 0-未领取, 1-已领取
                if (awardStatus != 0)
                {
                    chest.setAwardStatus(true);
                }
                goldenAutumnSunrise.addTreChest(chest.build());
            }
        }
        int day = activityBase.getDayiyBegin();//获取当前金秋活动已进行到第几天
        //获取喜悦金秋活动对应天数的任务列表
        List<StaticActAutumnDayTask> autumnDayTaskList = StaticActivityDataMgr.getStaticActAutumnDayTaskMap().values().stream()
                .filter(e -> e.getActivityType() == ActivityConst.ACT_GOLDEN_AUTUMN_FARM
                        && e.getActivityId() == activityBase.getActivityId()
                        && day >= e.getDay())
                .collect(Collectors.toList());
        if (!autumnDayTaskList.isEmpty())
        {
            for (StaticActAutumnDayTask staticActAutumnDayTask : autumnDayTaskList)
            {
                int taskSchedule = activity.getSaveMap().getOrDefault(staticActAutumnDayTask.getId(), 0);//获取任务进度
                long awardStatus = activity.getStatusCnt().getOrDefault(staticActAutumnDayTask.getId(), 0L);//获取任务奖励领取状态
                CommonPb.GoldenAutumnSunriseTask.Builder sunriseTask = CommonPb.GoldenAutumnSunriseTask.newBuilder();
                sunriseTask.setDay(staticActAutumnDayTask.getDay());
                sunriseTask.setTaskId(staticActAutumnDayTask.getId());
                sunriseTask.setTaskSchedule(taskSchedule);
                if (awardStatus > 0)
                {
                    sunriseTask.setAwardStatus(true);
                }
                else
                {
                    sunriseTask.setAwardStatus(false);
                }
                goldenAutumnSunrise.addTask(sunriseTask);
            }
        }
        goldenAutumnSunrise.setDay(day);
        return goldenAutumnSunrise.build();
    }

    /**
     * 喜悦金秋活动-日出而作-领取单个任务奖励
     */
    public GamePb4.GoldenAutumnSunriseGetTaskAwardRs receiveSingleTaskAward(long roleId, GamePb4.GoldenAutumnSunriseGetTaskAwardRq rq) throws MwException {
        if (Objects.isNull(rq) || rq.getActType() == 0 || rq.getTaskId() == 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "参数错误"));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = super.checkAndGetActivity(player, rq.getActType());
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(rq.getActType());
        if (null == activity || !super.isOpenStage(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(), GameError.err(roleId, "活动未开放", rq.getActType(), activityBase.getStep0()));
        }
        //根据任务id拿到任务配置
        StaticActAutumnDayTask staticActAutumnDayTask = StaticActivityDataMgr.getStaticActAutumnDayTaskMap().get(rq.getTaskId());
        if (Objects.isNull(staticActAutumnDayTask))
        {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "金秋活动-日出而作, 该任务未配置, roleId:" + roleId + ", taskId:" + rq.getTaskId());
        }
        //判断任务进度是否满足
        int schedule = activity.getSaveMap().getOrDefault(rq.getTaskId(), 0);//saveMap保持任务进度key-id value-当前任务的进度
        if (schedule < this.getTaskMaxSchedule(staticActAutumnDayTask))
        {
            throw new MwException(GameError.TASK_SCHEDULE_UNDONE.getCode(), "金秋活动-日出而作, 任务进度未达成, roleId:" + roleId + ", taskId:" + rq.getTaskId());
        }

        Long taskAwardStatus = activity.getStatusCnt().get(rq.getTaskId());
        if (Objects.nonNull(taskAwardStatus) && taskAwardStatus >= 1L) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, 已经领取过 taskId: %d, 的积分奖励", roleId, rq.getTaskId()));
        }

        //更新宝箱积分
        int treChestIntegral = activity.getStatusMap().getOrDefault(2, 0);//statusMap的key=2用于存储日出而作的宝箱积分
        activity.getStatusMap().put(2, treChestIntegral + staticActAutumnDayTask.getIntegral());
        //更新单个任务奖励领取状态
        activity.getStatusCnt().put(rq.getTaskId(), 1L);//statusCnt用于存储每个任务得奖励领取状态, 0-未领取, 1-已领取

        //同步兑换奖励
        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player, staticActAutumnDayTask.getAwardList(), AwardFrom.SUNRISE_TASK_REWARD);
        //组装协议
        GamePb4.GoldenAutumnSunriseGetTaskAwardRs.Builder rs = GamePb4.GoldenAutumnSunriseGetTaskAwardRs.newBuilder();
        rs.setActType(rq.getActType());
        rs.setTaskId(rq.getTaskId());
        rs.setAwardStatus(true);
        rs.setTreChestIntegral(activity.getStatusMap().get(2));
        Optional.ofNullable(awardList).ifPresent(tmpList -> tmpList.forEach(rs::addGetAward));
        return rs.build();
    }

    /**
     * 获取当前任务完成条件
     */
    private int getTaskMaxSchedule(StaticActAutumnDayTask staticActAutumnDayTask)
    {
        try {

            if (staticActAutumnDayTask.getTaskid() == ETask.FIGHT_REBEL.getTaskType()
                    || staticActAutumnDayTask.getTaskid() == ETask.GOLDEN_AUTUMN_GET_RESOURCE.getTaskType()
                    || staticActAutumnDayTask.getTaskid() == ETask.MAKE_EQUIP.getTaskType())
            {
                return staticActAutumnDayTask.getParam().get(1);
            }
            else
            {
                return staticActAutumnDayTask.getParam().get(0);
            }
        }
        catch (Exception e)
        {
            LogUtil.error("喜悦金秋-日出而作活动, 解析任务参数异常 param:" + staticActAutumnDayTask.getParam(),e);
        }
        return 0;
    }

    /**
     * 喜悦金秋活动-日出而作-开宝箱
     */
    public GamePb4.GoldenAutumnSunriseOpenTreasureChestRs openTreasureChest(long roleId, GamePb4.GoldenAutumnSunriseOpenTreasureChestRq rq) throws MwException {
        if (Objects.isNull(rq) || rq.getActType() == 0 || rq.getKeyId() == 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "参数错误"));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = super.checkAndGetActivity(player, rq.getActType());
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(rq.getActType());
        if (null == activity || !super.isOpenStage(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(), GameError.err(roleId, "活动未开放", rq.getActType(), activityBase.getStep0()));
        }
        //获取所有积分奖励配置
        List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activityBase.getActivityId());
        if (null == actAwardList || actAwardList.isEmpty())
        {
            throw new MwException(GameError.NO_CONFIG.getCode(), "金秋活动-日出而作, 未匹配到积分奖励配置, roleId:" + roleId, ",ActivityId:" + activityBase.getActivityId());
        }
        //判断传入的宝箱配置id是否跟活动配置的相匹配
        StaticActAward staticActAward = actAwardList.stream().filter(e -> e.getKeyId() == rq.getKeyId()).findFirst().orElse(null);
        if (Objects.isNull(staticActAward))
        {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "金秋活动-日出而作, 传入的宝箱配置id与配置表不一致, roleId:" + roleId, ",keyId:" + rq.getKeyId());
        }
        //判断积分是否满足领取奖励要求
        int treChestIntegral = activity.getStatusMap().getOrDefault(2, 0);//statusMap的key=2用于存储日出而作的宝箱积分
        if (treChestIntegral < staticActAward.getParam().get(0))
        {
            throw new MwException(GameError.TREASURE_CHEST_INTEGRAL_INSUFFICIENT.getCode(), "金秋活动-日出而作, 宝箱积分不足, roleId:" + roleId, ",treChestIntegral:" + treChestIntegral);
        }
        //判断奖励无法重复领取
        if (activity.getStatusMap().containsKey(rq.getKeyId()) && activity.getStatusMap().get(rq.getKeyId()) == 1)
        {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "金秋活动-日出而作, 宝箱奖励已领取, roleId:" + roleId);
        }
        //同步兑换奖励
        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player, staticActAward.getAwardList(), AwardFrom.SUNRISE_TREASURE_CHEST_REWARD);
        //更新宝箱领取状态
        activity.getStatusMap().put(rq.getKeyId(), 1);//statusMap的key=宝箱配置id 用于存储宝箱领取状态 0-未领取, 1-已领取
        GamePb4.GoldenAutumnSunriseOpenTreasureChestRs.Builder rs = GamePb4.GoldenAutumnSunriseOpenTreasureChestRs.newBuilder();
        rs.setActType(rq.getActType());
        rs.setKeyId(rq.getKeyId());
        rs.setAwardStatus(true);
        Optional.ofNullable(awardList).ifPresent(tmpList -> tmpList.forEach(rs::addGetAward));
        return rs.build();
    }

    /**
     * 处理任务
     * @param player 玩家信息
     * @param eTask 任务类型
     * @param params 任务参数
     */
    @Override
    public void process(Player player, ETask eTask, int... params) {
        try {
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_GOLDEN_AUTUMN_FARM);
            if (!isOpenStage(activityBase)) return;
            Activity activity = super.checkAndGetActivity(player, ActivityConst.ACT_GOLDEN_AUTUMN_FARM);
            int day = activityBase.getDayiyBegin();//获取当前金秋活动已进行到第几天
            //获取喜悦金秋活动对应任务类型、天数的任务列表
            List<StaticActAutumnDayTask> autumnDayTaskList = StaticActivityDataMgr.getStaticActAutumnDayTaskMap().values().stream()
                    .filter(e -> e.getActivityType() == ActivityConst.ACT_GOLDEN_AUTUMN_FARM && e.getActivityId() == activityBase.getActivityId()
                            && e.getTaskid() == eTask.getTaskType() && day >= e.getDay()).collect(Collectors.toList());
            if (!autumnDayTaskList.isEmpty()) {
                this.processTask(player, activity, autumnDayTaskList, params);
            }
        }
        catch (Exception e)
        {
            LogUtil.error("喜悦金秋-日出而作活动任务处理异常! roleId:" + player.roleId + ", taskType:" + eTask.getTaskType() + ", error:" + e.getLocalizedMessage());
        }
    }

    /**
     * 处理喜悦金秋任务
     * @param autumnDayTaskList 喜悦金秋活动任务列表
     * @param params 参数
     */
    private void processTask(Player player, Activity activity, List<StaticActAutumnDayTask> autumnDayTaskList, int... params)
    {
        for (StaticActAutumnDayTask staticTask : autumnDayTaskList)
        {
            int param = params[0];
            /*
             *  攻打叛军[6,30]
             */
            if (staticTask.getTaskid() == ETask.FIGHT_REBEL.getTaskType() && param >= staticTask.getParam().get(0))
            {
                this.updTaskSchedule(player, activity, staticTask.getId(), staticTask.getParam().get(1), params[1]);
            }
            /*
             *  打造绿色装备[2,3]
             *  打造蓝色装备[3,3]
             */
            else if (staticTask.getTaskid() == ETask.MAKE_EQUIP.getTaskType() && param == staticTask.getParam().get(0))
            {
                this.updTaskSchedule(player, activity, staticTask.getId(), staticTask.getParam().get(1), params[1]);
            }
            /*  攻打精英叛军
             *  通关战役xx次（包含扫荡）
             *  通关帝国远征xx次（包含扫荡）
             *  在码头钓鱼
             *  佳人送礼
             *  训练xx任意等级士兵
             *  装备改造xx次（包含高级改造，不包含终极改造）
             *  合成x个任意等级配饰
             *  阵营建设xx次
             *  英雄特训x次（包含高级特训）
             *  在码头钓到鱼
             */
            else if (staticTask.getTaskid() == ETask.FIGHT_ELITE_REBEL.getTaskType() || staticTask.getTaskid() == ETask.PASS_BARRIER.getTaskType()
                    || staticTask.getTaskid() == ETask.PASS_EXPEDITION.getTaskType() || staticTask.getTaskid() == ETask.GOLDEN_AUTUMN_FISHING.getTaskType()
                    || staticTask.getTaskid() == ETask.BEAUTY_GIFT.getTaskType() || staticTask.getTaskid() == ETask.MAKE_ARMY.getTaskType()
                    || staticTask.getTaskid() == ETask.REFORM_EQUIP.getTaskType() || staticTask.getTaskid() == ETask.ORNAMENT_COUNT.getTaskType()
                    || staticTask.getTaskid() == ETask.BUILD_CAMP.getTaskType() || staticTask.getTaskid() == ETask.HERO_TRAINING.getTaskType()
                    || staticTask.getTaskid() == ETask.GOLDEN_AUTUMN_CATCH_FISH.getTaskType())
            {
                this.updTaskSchedule(player, activity, staticTask.getId(), staticTask.getParam().get(0), param);
            }
            //获得资源(param中0(任意资源)、1(黄金)、2(木材)、3(粮食)、4(矿石))
            else if (staticTask.getTaskid() == ETask.GOLDEN_AUTUMN_GET_RESOURCE.getTaskType())
            {
                if (param == staticTask.getParam().get(0) || staticTask.getParam().get(0) == 0)
                {
                    this.updTaskSchedule(player, activity, staticTask.getId(), staticTask.getParam().get(1), params[1]);
                }
            }
        }
    }

    /**
     * 更新进度
     */
    private void updTaskSchedule(Player player, Activity activity, int taskId, int configMax, int updSchedule)
    {
        //判断如果当前任务已经领取奖励则不需要处理
        if(activity.getStatusCnt().containsKey(taskId) && activity.getStatusCnt().get(taskId) == 1)
        {
            return;
        }
        int schedule = activity.getSaveMap().getOrDefault(taskId, 0);
        if (schedule >= configMax)
        {
            return;
        }
        schedule = schedule + updSchedule;//更新进度
        if (schedule >= configMax)
        {
            activity.getSaveMap().put(taskId, configMax);
            GamePb3.SyncActChangeRs.Builder actBuild = GamePb3.SyncActChangeRs.newBuilder();
            actBuild.addAct(PbHelper.createTwoIntPb(activity.getActivityType(), 1));
            BasePb.Base.Builder builder = PbHelper.createSynBase(GamePb3.SyncActChangeRs.EXT_FIELD_NUMBER, GamePb3.SyncActChangeRs.ext,
                    actBuild.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, builder.build(), player.roleId));
        }
        else
        {
            activity.getSaveMap().put(taskId, schedule);
        }
    }

    // <editor-fold desc="GM命令" defaultstate="collapsed">
    public void test_receiveSingleTaskAward(Player player,int actType, int taskId) throws MwException {
        GamePb4.GoldenAutumnSunriseGetTaskAwardRq.Builder rq = GamePb4.GoldenAutumnSunriseGetTaskAwardRq.newBuilder();
        rq.setActType(actType);
        rq.setTaskId(taskId);
        this.receiveSingleTaskAward(player.roleId, rq.build());
    }

    public void test_openTreasureChest(Player player,int actType, int keyId) throws MwException {
        GamePb4.GoldenAutumnSunriseOpenTreasureChestRq.Builder rq = GamePb4.GoldenAutumnSunriseOpenTreasureChestRq.newBuilder();
        rq.setActType(actType);
        rq.setKeyId(keyId);
        this.openTreasureChest(player.roleId, rq.build());
    }

    public void test_buildGoldenAutumnInfo(Player player,int actType) throws MwException {
        this.buildGoldenAutumnInfo(player, super.checkAndGetActivity(player, actType));
    }

// </editor-fold>
}