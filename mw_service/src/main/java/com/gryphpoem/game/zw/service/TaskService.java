package com.gryphpoem.game.zw.service;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTaskDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb.Mill;
import com.gryphpoem.game.zw.pb.GamePb1.SynGainResRs;
import com.gryphpoem.game.zw.pb.GamePb3.*;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.task.TaskCategory;
import com.gryphpoem.game.zw.resource.constant.task.TaskCone513Type;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.CampMember;
import com.gryphpoem.game.zw.resource.domain.s.StaticDailyTaskAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticPartyTask;
import com.gryphpoem.game.zw.resource.domain.s.StaticTask;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务：包括主线支线任务，世界任务，军团任务，攻打世界boss
 */
@Service
public class TaskService {
    @Autowired
    private SeasonService seasonService;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private CampDataManager campDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private PropService propService;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private TitleService titleService;

    /**
     * 获取军团任务
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetPartyTaskRs getPartyTask(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 将主线任务添加到玩家身上
        Map<Integer, Task> taskMap = player.partyTask;

        GetPartyTaskRs.Builder builder = GetPartyTaskRs.newBuilder();
        Iterator<Task> it = taskMap.values().iterator();
        CampMember p = campDataManager.getCampMember(player.lord.getLordId());
        if (p.getTaskLv() == 0) {
            Camp camp = campDataManager.getParty(player.lord.getCamp());
            p.setTaskLv(camp.getPartyLv());
            LogUtil.debug(roleId + ",军团任务等级重置为" + camp.getPartyLv());
        }
        while (it.hasNext()) {
            Task task = it.next();
            int taskId = task.getTaskId();
            StaticPartyTask stask = StaticTaskDataMgr.getPartyTask(taskId);
            if (stask == null) {
                continue;
            }
            if (stask.getLv() != p.getTaskLv()) {
                continue;
            }
            taskDataManager.currentTask(player, task, stask.getCond(), stask.getCondId(), stask.getSchedule());
            if (task.getSchedule() >= stask.getSchedule() && task.getStatus() == 0) {
                task.setStatus(TaskType.TYPE_STATUS_FINISH);
            }
            if (task.getSchedule() < stask.getSchedule()) {
                task.setStatus(TaskType.TYPE_STATUS_UNFINISH);
            }
            builder.addTask(PbHelper.createTaskPb(task));
        }
        int nowHour = TimeHelper.getCurrentHour();
        int difHour = nowHour % PartyConstant.PARTY_TASK_REFRESH_PER_HOUR;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, PartyConstant.PARTY_TASK_REFRESH_PER_HOUR - difHour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        builder.setEndTime((int) (cal.getTimeInMillis() / 1000));
        builder.setTaskLv(p.getTaskLv());
        return builder.build();
    }

    /**
     * 领取军团任务奖励
     *
     * @param roleId
     * @param taskId
     * @return
     * @throws MwException
     */
    public PartyTaskAwardRs partyTaskAward(Long roleId, int taskId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 星期五无活动
        if (TimeHelper.isFriday()) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(), "周五无活动, roleId:" + roleId + ",taskId=" + taskId);
        }
        Task task = player.partyTask.get(taskId);
        if (task == null) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(),
                    "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
        }
        StaticPartyTask stask = StaticTaskDataMgr.getPartyTask(taskId);
        if (stask == null) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(),
                    "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
        }
        if (task.getStatus() == TaskType.TYPE_STATUS_REWARD) {
            throw new MwException(GameError.TASK_REWARD.getCode(), "军团奖励已领取, roleId:" + roleId + ",taskId=" + taskId);
        }
        CampMember p = campDataManager.getCampMember(player.lord.getLordId());
        if (stask.getLv() != p.getTaskLv()) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(),
                    "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
        }
        taskDataManager.currentTask(player, task, stask.getCond(), stask.getCondId(), stask.getSchedule());
        int finishValue = stask.getSchedule();
        if (task.getSchedule() < finishValue) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(),
                    "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
        }

        task.setStatus(TaskType.TYPE_STATUS_REWARD);

        PartyTaskAwardRs.Builder builder = PartyTaskAwardRs.newBuilder();
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_CAMP_TASK_FINSH_CNT, 1);
        taskDataManager.updTask(player, TaskType.COND_513, 1, TaskCone513Type.CAMP_TASK);

        // 给予奖励
        builder.addAllAward(
                rewardDataManager.addAwardDelaySync(player, stask.getAward(), null, AwardFrom.PARTY_TASK_REWARD));
        if (PartyConstant.PARTY_TASK_EXT_REWARD_CNT >= p.getTaskAwardCnt()) {
            List<List<Integer>> awardList = stask.getExtReward();
            if (awardList != null) {
                if (RandomHelper.isHitRangeIn10000(stask.getExtRatio())) {
                    for (List<Integer> ee : awardList) {
                        int type = ee.get(0);
                        int id = ee.get(1);
                        int count = ee.get(2);
                        int keyId = rewardDataManager.addAward(player, type, id, count, AwardFrom.TASK_DAYIY_AWARD);
                        builder.addAward(PbHelper.createAwardPb(type, id, count, keyId));
                    }
                    p.setTaskAwardCnt(p.getTaskAwardCnt() + 1);
                    // 额外奖励发军团聊天消息
                    chatDataManager.sendSysChat(ChatConst.CHAT_EXTRA_TASK_REWARD, player.lord.getCamp(), 0,
                            player.lord.getNick());
                }
            }
        }
        builder.setTask(PbHelper.createTaskPb(task));
        return builder.build();
    }


    /*-------------------------------日常活动相关start----------------------------------*/

    /**
     * 获取日常任务
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetDailyTaskRs getDailyTask(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetDailyTaskRs.Builder builder = GetDailyTaskRs.newBuilder();
        player.getDailyTask().values().forEach(t -> builder.addDailyTask(PbHelper.createTaskPb(t)));
        builder.addAllDailyIsGet(player.getDailyIsGet());
        builder.setDailyTaskLivenss(player.getDailyTaskLivenss());
        return builder.build();
    }

    /**
     * 日常活跃度领奖
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public DailyAwardRs dailyAward(long roleId, DailyAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int awardId = req.getId();
        Set<Integer> dailyIsGet = player.getDailyIsGet();
        if (dailyIsGet.contains(awardId)) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "奖励已经被领取, roleId:", player.roleId);
        }
        StaticDailyTaskAward sAward = StaticTaskDataMgr.getDailyTaskAwardMapById(awardId);
        if (sAward == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "日常任务奖励配置没找到, roleId:", player.roleId, " id:",
                    awardId);
        }
        //等级检查
        if (player.lord.getLevel() < sAward.getLevel().get(0) || player.lord.getLevel() > sAward.getLevel().get(1)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "领取日常任务活跃奖励, 等级错误", player.lord.getLevel(), JSON.toJSONString(sAward)));
        }
        if (player.getDailyTaskLivenss() < sAward.getValue()) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(), "日常任务活跃度不够, roleId:", player.roleId, " id:",
                    awardId, ", hasLivenss:", player.getDailyTaskLivenss());
        }

        dailyIsGet.add(awardId);
        DailyAwardRs.Builder builder = DailyAwardRs.newBuilder();
        builder.addAllAward(
                rewardDataManager.addAwardDelaySync(player, sAward.getAward(), null, AwardFrom.DAILY_TASK_AWARD));
        builder.addAllDailyIsGet(dailyIsGet);
        taskDataManager.updTask(player, TaskType.COND_513, 1, TaskCone513Type.DAILY_TASKS);
        return builder.build();
    }

    private boolean duringPeriod(StaticDailyTaskAward award) {
        if (CheckNull.isNull(award.getSeasonBeginTime()) || CheckNull.isNull(award.getSeasonEndTime())) {
            return false;
        }

        Date now = new Date();
        return now.after(award.getSeasonBeginTime()) && now.before(award.getSeasonEndTime());
    }

    /**
     * 日常任务活跃度领取
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public LivenssAwardRs livenssAward(long roleId, LivenssAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int taskId = req.getTaskId();
        StaticTask sTask = StaticTaskDataMgr.getDayiyMap().get(taskId);
        if (sTask == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "日常活动领取活跃未找到配置, roleId:", player.roleId, ", taskId:",
                    taskId);
        }
        Task task = player.getDailyTask().get(taskId);
        if (task == null || task.getSchedule() < sTask.getSchedule()) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(), "任务未完成 roleId:", roleId, ", taskId:", taskId);
        }
        if (task.getStatus() == TaskType.TYPE_STATUS_REWARD) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "奖励已经被领取, roleId:", player.roleId, ", taskId:",
                    taskId);
        }
        task.setStatus(TaskType.TYPE_STATUS_REWARD);
        int sch = sTask.getAwardList().get(0).get(0);
        player.setDailyTaskLivenss(player.getDailyTaskLivenss() + sch); // 加上活跃度
        //上报数数
        EventDataUp.credits(player.account, player.lord, player.getDailyTaskLivenss(), sch, CreditsConstant.DAILY_TASK, AwardFrom.DAILY_TASK_AWARD);

        activityDataManager.updateLuckyPoolLive(player, player.getDailyTaskLivenss());
        // 更新七日活动每日任务领取的次数
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_DAILY_TASK_CNT, 1);

        //貂蝉任务-完成每日任务
        ActivityDiaoChanService.completeTask(player, ETask.FINISHED_DAILYTASK);
        TaskService.processTask(player, ETask.FINISHED_DAILYTASK);
        titleService.processTask(player, ETask.FINISHED_DAILYTASK);

        LogUtil.debug("roleId:", player.roleId, " 日常任务活跃度增加: taskId:", taskId, ",sch:", sch, ",Livenss",
                player.getDailyTaskLivenss());
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_DAILY_LIVENSS, sch);
        LivenssAwardRs.Builder builder = LivenssAwardRs.newBuilder();
        builder.setDailyTask(PbHelper.createTaskPb(task));
        builder.setDailyTaskLivenss(player.getDailyTaskLivenss());
        return builder.build();
    }

    /**
     * 蝎王激活
     *
     * @param roleId 玩家id
     * @param req    请求协议
     * @return 返回协议
     * @throws MwException 自定义异常
     */
    public GamePb4.ScorpionActivateRs scorpionActivate(Long roleId, GamePb4.ScorpionActivateRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GamePb4.ScorpionActivateRs.Builder builder = GamePb4.ScorpionActivateRs.newBuilder();
        if (req.hasType()) {
            int type = req.getType();
            // 使用道具
            if (type == 1) {
                int propId = PropConstant.SCORPION_ACTIVATE_PROP;

                Prop prop = player.props.get(propId);
                if (prop == null || prop.getCount() == 0) {
                    throw new MwException(GameError.NO_PROP.getCode(), "道具使用,无此道具, roleId:" + roleId);
                }
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, propId, 1, AwardFrom.USE_PROP);
                // 更新使用蝎王解锁时间
                int now = TimeHelper.getCurrentSecond();
                player.setMixtureData(PlayerConstant.SCORPION_ACTIVATE_END_TIME, StaticPropDataMgr.SCORPION_ACTIVATE_TIME.get(0) + now);
                taskDataManager.updTask(player, TaskType.COND_SCORPION_ACTIVATE_PREPOSITION, 1);
                builder.setTime(StaticPropDataMgr.SCORPION_ACTIVATE_TIME.get(0) + now);
            } else if (type == 2) {
                // 召唤将领
                int activateTime = player.getMixtureDataById(PlayerConstant.SCORPION_ACTIVATE_END_TIME);
                int now = TimeHelper.getCurrentSecond();
                if (activateTime == 0 || activateTime > now) {
                    throw new MwException(GameError.SCORPION_ACTIVATE_ERROR.getCode(), "激活蝎王, 前置条件未完成, 或者时间未达到, activateTime:", activateTime);
                }

                Hero hero = rewardDataManager.addHero(player, StaticPropDataMgr.SCORPION_ACTIVATE_TIME.get(1), AwardFrom.SCORPION_ACTIVATE_AWARD);
                taskDataManager.updTask(player, TaskType.COND_SCORPION_ACTIVATE, 1);
                builder.setHero(PbHelper.createHeroPb(hero, player));
            } else if (type == 3) {
                // 救援爱丽丝
                int aliceAwardTime = player.getMixtureDataById(PlayerConstant.ALICE_AWARD_TIME);
                int now = TimeHelper.getCurrentSecond();
                if (aliceAwardTime == 0 || aliceAwardTime > now) {
                    throw new MwException(GameError.ALICE_AWARD_ERROR.getCode(), "救援爱丽丝, 前置条件未完成, 或者时间未达到, aliceAwardTime:", aliceAwardTime);
                }
                Hero hero = rewardDataManager.sendHeroAward(player, WorldConstant.GUAN_PING_RESCUE_REWARD.get(0),
                        WorldConstant.GUAN_PING_RESCUE_REWARD.get(1), WorldConstant.GUAN_PING_RESCUE_REWARD.get(2), AwardFrom.ALICE_AWARD);
                if (hero == null) {
                    throw new MwException(GameError.ALICE_AWARD_RECEIVED.getCode(), "救援爱丽丝, 奖励已经领取");
                }
                builder.setHero(PbHelper.createHeroPb(hero, player));
            }
        }

        return builder.build();
    }

    /**
     * 个人目标任务
     *
     * @param roleId 玩家id
     * @param ids    目标任务id
     * @return 响应
     * @throws MwException 自定义异常
     */
    public GetAdvanceTaskRs getAdvanceTask(long roleId, List<Integer> ids) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 检测并修改个人目标进度
        checkAndModTask(player);

        List<Task> showTask;
        if (CheckNull.isEmpty(ids)) {
            showTask = new ArrayList<>(player.getAdvanceTask().values());
        } else {
            showTask = player.getAdvanceTask().values().stream().filter(task -> ids.contains(task.getTaskId())).collect(Collectors.toList());
        }
        return GetAdvanceTaskRs.newBuilder().addAllAdvanceTask(showTask.stream().map(PbHelper::createTaskPb).collect(Collectors.toList())).build();
    }

    /**
     * 个人目标领取
     *
     * @param roleId 玩家id
     * @param req    请求
     * @return 响应
     * @throws MwException 自定义异常
     */
    public AdvanceAwardRs advanceAward(long roleId, AdvanceAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int taskId = req.getTaskId();
        StaticTask staticTask = StaticTaskDataMgr.getTaskById(taskId);
        if (staticTask == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "领取任务奖励时,配置找不到, roleId:" + roleId + ",taskId=" + taskId);
        }

        Task task = player.getAdvanceTask().get(taskId);
        if (task == null) {
            throw new MwException(GameError.NO_TASK.getCode(), "领取任务奖励时,无此任务, roleId:" + roleId + ",taskId=" + taskId);
        }
        if (task.getStatus() == TaskType.TYPE_STATUS_REWARD) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "领取任务奖励时,重复领取, roleId:" + roleId);
        }

        // 检测并修改个人目标进度
        checkAndModTask(player);
        if (task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(), "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
        }

        // task增加日志记录 (基本信息、战力、关卡类型、关卡id)
        LogLordHelper.commonLog("advanceTask", AwardFrom.COMMON, player, player.lord.getFight(), staticTask.getType(), taskId);

        task.setStatus(TaskType.TYPE_STATUS_REWARD);

        List<List<Integer>> awardList = staticTask.getAwardList();
        if (!CheckNull.isEmpty(awardList)) {
            // 奖励同步给客户端
            rewardDataManager.sendReward(player, awardList, AwardFrom.ADVANCE_AWARD, taskId);
        }

        return AdvanceAwardRs.newBuilder().setAdvanceTask(PbHelper.createTaskPb(task)).build();
    }


    /**
     * 检测并修改个人目标进度
     *
     * @param player 玩家对象
     */
    private void checkAndModTask(Player player) {
        if (CheckNull.isNull(player)) {
            return;
        }
        Map<Integer, Task> advanceTask = player.getAdvanceTask();
        StaticTaskDataMgr.getAdvanceMap()
                .forEach((taskId, sTask) -> {
                    Task task = advanceTask.computeIfAbsent(taskId, (t) -> new Task(sTask.getTaskId()));
                    if (task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
                        // 如果任务状态是未完成, 每次获取的时候, 主动检测任务进度
                        taskDataManager.currentTask(player, task, sTask.getCond(), sTask.getCondId(), sTask.getSchedule());
                    }
                    if (task.getSchedule() >= sTask.getSchedule() && task.getStatus() == 0) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                    }
                    if (task.getSchedule() < sTask.getSchedule()) {
                        task.setStatus(TaskType.TYPE_STATUS_UNFINISH);
                    }
                });
    }

    /**
     * @param player
     * @param eTask
     * @param params
     */
    public static void handleTask(Player player, ETask eTask, int... params) {
        try {
            SeasonService seasonService = DataResource.ac.getBean(SeasonService.class);
            seasonService.handleTreasuryTask0(player, eTask, params);
            seasonService.handleJourneyTask0(player, eTask, params);
        } catch (Exception e) {
            LogUtil.error("处理任务发生错误, ", e);
        }
    }

    /**
     * 处理任务
     *
     * @param player 玩家信息
     * @param eTask  任务类型
     * @param params 任务参数
     */
    public static void processTask(Player player, ETask eTask, int... params) {
        Map<String, TaskFinishService> resultMap = DataResource.ac.getBeansOfType(TaskFinishService.class);
        if (CheckNull.nonEmpty(resultMap)) {
            for (Map.Entry<String, TaskFinishService> entry : resultMap.entrySet()) {
                try {
                    entry.getValue().process(player, eTask, params);
                } catch (Exception e) {
                    LogUtil.error(String.format("service: %s, roleId: %d, process task type: %d, params: %s",
                            entry.getKey(), player.getLordId(), eTask.getTaskType(), Arrays.toString(params)), e);
                }
            }
        }
    }

    /**
     * 检查任务条件并处理任务进度和状态
     *
     * @param task
     * @param condList
     * @param eTask
     * @param params
     * @return
     */
    public boolean checkTaskCondition(Player player, Task task, List<Integer> condList, ETask eTask, int... params) {
        boolean b = false;
        Hero hero;
        int count;
        switch (eTask) {
            case FIGHT_REBEL:
                if (params[1] >= condList.get(0) && params[1] <= condList.get(1)) {
                    b = true;
                    task.setSchedule(task.getSchedule() + 1);
                    if (task.getSchedule() >= condList.get(2)) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        task.setSchedule(condList.get(2));
                    }
                }
                break;
            case MAKE_ARMY:
                if (condList.get(0) == 0 || condList.get(0) == params[0]) {
                    b = true;
                    task.setSchedule(task.getSchedule() + params[1]);
                    if (task.getSchedule() >= condList.get(1)) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        task.setSchedule(condList.get(1));
                    }
                }
                break;
            case FINISHED_TASK:
                if (params[0] == condList.get(0)) {
                    if (params[0] == TaskCategory.TREASURY.getCategory()) {//宝库任务 累计的完成个数
                        count = player.getPlayerSeasonData().getFinishedCount();
                        if (count != task.getSchedule()) {
                            b = true;
                            task.setSchedule(count);
                        }
                        if (task.getSchedule() >= condList.get(1)) {
                            task.setStatus(TaskType.TYPE_STATUS_FINISH);
                            task.setSchedule(condList.get(1));
                        }
                    }
                }
                break;
            case GET_TASKAWARD:
                if (params[0] == condList.get(0)) {
                    if (params[0] == TaskCategory.TREASURY.getCategory()) {
                        count = player.getPlayerSeasonData().getGetAwardCount();
                        if (count != task.getSchedule()) {
                            b = true;
                            task.setSchedule(count);
                        }
                        if (task.getSchedule() >= condList.get(1)) {
                            task.setStatus(TaskType.TYPE_STATUS_FINISH);
                            task.setSchedule(condList.get(1));
                        }
                    }
                }
                break;
            case JOIN_ACTIVITY:
                if (params[0] == condList.get(0)) {
                    b = true;
                    task.setSchedule(task.getSchedule() + 1);
                    if (task.getSchedule() >= condList.get(1)) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        task.setSchedule(condList.get(1));
                    }
                }
                break;
            case CONSUME_DIAMOND:
            case ARMY_MAK_LOST:
                b = true;
                task.setSchedule(task.getSchedule() + params[0]);
                if (task.getSchedule() >= condList.get(0)) {
                    task.setStatus(TaskType.TYPE_STATUS_FINISH);
                    task.setSchedule(condList.get(0));
                }
                break;
            case GET_HERO:
                if (Objects.nonNull(player.heros.get(condList.get(0)))) {
                    b = true;
                    task.setSchedule(task.getSchedule() + 1);
                    task.setStatus(TaskType.TYPE_STATUS_FINISH);
                }
                break;
            case HERO_UPSTAR:
                hero = player.heros.get(condList.get(0));
                if (Objects.nonNull(hero)) {
                    if (hero.getCgyStage() > condList.get(1) || (hero.getCgyStage() == condList.get(1) && hero.getCgyLv() >= condList.get(2))) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        task.setSchedule(1);
                    }
                    b = true;
                }
                break;
            case HERO_UPSKILL:
                hero = player.heros.get(condList.get(0));
                if (Objects.nonNull(hero)) {
                    task.setSchedule(hero.getSkillLevels().getOrDefault(condList.get(1), 0));
                    if (task.getSchedule() >= condList.get(2)) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        task.setSchedule(condList.get(2));
                    }
                    b = true;
                }
                break;
            case HERO_LEVELUP:
                hero = player.heros.get(condList.get(0));
                if (Objects.nonNull(hero)) {
                    task.setSchedule(hero.getLevel());
                    if (task.getSchedule() >= condList.get(1)) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        task.setSchedule(condList.get(1));
                    }
                    b = true;
                }
                break;
            default:
        }
        return b;
    }

    /**
     * 给N次征收
     *
     * @param player
     * @param type
     * @param count
     */
    private void addGainResCnt(Player player, int type, int count) {
        List<Mill> millPbList = player.mills.values().stream().filter(mill -> mill.isUnlock() && mill.getType() == type)
                .map(mill -> {
                    mill.setResCnt(mill.getResCnt() + count);
                    if (mill.getResCnt() > Constant.RES_GAIN_MAX) { // 防止爆掉了
                        mill.setResCnt(Constant.RES_GAIN_MAX);
                    }
                    return PbHelper.createMillPb(mill);
                }).collect(Collectors.toList());

        SynGainResRs.Builder builder = SynGainResRs.newBuilder();
        builder.addAllMills(millPbList);
        Base.Builder msg = PbHelper.createSynBase(SynGainResRs.EXT_FIELD_NUMBER, SynGainResRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));

    }

    /**
     * 给资源+1次征收
     *
     * @param player
     * @param taskId
     */
    public void processTask(Player player, int taskId) {
        if (!CheckNull.isEmpty(Constant.TASK_GIVE_GAINRES)) {
            List<Integer> config = Constant.TASK_GIVE_GAINRES.stream()
                    .filter(l -> !CheckNull.isEmpty(l) && l.get(0) == taskId).findFirst().orElse(null);
            if (config != null && config.size() >= 3) {
                addGainResCnt(player, config.get(1), config.get(2));
            }
        }
    }
}
