package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.rank.RankItem;
import com.gryphpoem.game.zw.core.rank.SimpleRank4SkipSet;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMusicFestivalMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.ActivityPb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.ActivityTask;
import com.gryphpoem.game.zw.resource.domain.s.StaticCreativeOffice;
import com.gryphpoem.game.zw.resource.domain.s.StaticCreativeOfficeAward;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityCreativeOfficeData;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.TaskFinishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 音乐节创作
 *
 * @Description
 * @Author zhangdh 玩家
 * @Date 2021-10-27 11:18
 */
@Service
public class MusicFestivalCreativeService extends AbsSimpleActivityService implements TaskFinishService, GmCmdService {
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ChatDataManager chatDataManager;


    //玩家当前创作的歌曲任务清单
    private static final int CREATE_TASK_LIST_IDX = 0;
    //当前的歌曲创作难度
    private static final int SAVE_IDX_CRT_DIFFICULT_MODEL = -1;
    //活动期间个人总完成次数
    private static final int SAVE_IDX_CRT_CNT_TOTAL = -2;
    //玩家个人创作积分, 阵营积分-101, -102, -103 参见: getCampScoreIndex()
    private static final int SAVE_IDX_SCORE = -3;

    //今日免费创作的次数
    private static final int STATUS_IDX_CRT_CNT_FREE = -1;
    //今日花费钻石创作的次数
    private static final int STATUS_IDX_CRT_CNT_NOT_FREE = -2;

    private static final int CREATIVE_OFFICE_AWARD_TYPE_1 = 1;//个人积分进度奖励
    private static final int CREATIVE_OFFICE_AWARD_TYPE_2 = 2;//个人排名奖励
    private static final int CREATIVE_OFFICE_AWARD_TYPE_3 = 3;//阵营排名奖励
    private static final int CREATIVE_OFFICE_AWARD_TYPE_4 = 4;//阵营进度奖励


    private ActivityBase getActivityBase(Date now, int activityType) throws MwException {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (activityBase == null
                || activityBase.getPlan() == null
                || now.before(activityBase.getBeginTime())
                || now.after(activityBase.getEndTime())) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启");
        }
        return activityBase;
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws MwException {
        ActivityPb.MusicCreativeOfficeData.Builder data = ActivityPb.MusicCreativeOfficeData.newBuilder();
        Map<Integer, Integer> statusMap = activity.getStatusMap();
        Map<Integer, Integer> saveMap = activity.getSaveMap();
        int freeCnt = statusMap.getOrDefault(STATUS_IDX_CRT_CNT_FREE, 0);
        data.setFreeCnt(freeCnt);//免费次数
        int notFreeCnt = statusMap.getOrDefault(STATUS_IDX_CRT_CNT_NOT_FREE, 0);
        data.setNotFreeCnt(notFreeCnt);//收费次数
        int difficultModel = saveMap.getOrDefault(SAVE_IDX_CRT_DIFFICULT_MODEL, 0);
        List<ActivityTask> taskList = activity.getDayTasks().get(CREATE_TASK_LIST_IDX);
        if (CheckNull.nonEmpty(taskList)) {
            for (ActivityTask task : taskList) {
                data.addTask(createActTaskPb(task));
            }
        } else {
            //防止选择了难度, 但任务列表为空的情况
            if (difficultModel > 0) {
                saveMap.put(SAVE_IDX_CRT_DIFFICULT_MODEL, difficultModel = 0);
            }
        }
        data.setDifficultModel(difficultModel);//难度模式
        //玩家积分
        int score = saveMap.getOrDefault(SAVE_IDX_SCORE, 0);
        data.setTotalScore(score);

        //已经领取过的奖励列表
        for (Map.Entry<Integer, Integer> entry : saveMap.entrySet()) {
            if (entry.getKey() > 0) {
                data.addDrawUid(entry.getKey());
            }
        }
        GlobalActivityCreativeOfficeData globalActivity = (GlobalActivityCreativeOfficeData) globalActivityData;
        int campFinishCnt = (int) globalActivity.getCampValByCamp(player.getCamp());
        data.setCampFinishCnt(campFinishCnt);
        return GamePb4.GetActivityDataInfoRs.newBuilder().setMusicCrtData(data);
    }

    /**
     * 选择创作难度
     *
     * @param player 玩家
     * @param req    请求信息
     * @return 返回信息
     * @throws MwException 异常
     */
    public GamePb4.ChooseDifficultRs chooseCreativeDifficultyModel(Player player, GamePb4.ChooseDifficultRq req) throws MwException {
        int actType = req.getActType();
        validateActivityType(actType);
        //活动配置缺失
        Map<Integer, Integer> taskCntMap = ActParamConstant.MUSIC_CRT_OFFICE_TASK_COUNT_BY_DIFFICULT;
        if (CheckNull.isEmpty(taskCntMap)) {
            throw new MwException(GameError.NO_CONFIG.getCode());
        }
        //难度检测, 1-普通模式, 2-困难模式, 3-大师模式
        int difficultModel = req.getDifficultModel();
        if (!taskCntMap.containsKey(difficultModel)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, difficult: %d not found!!!", player.getLordId(), difficultModel));
        }
        //任务列表
        Date now = new Date();
        ActivityBase base = getActivityBase(now, actType);
        List<StaticCreativeOffice> staticTaskList = StaticMusicFestivalMgr.getCreativeOfficeTaskListByType(base.getActivityId(), difficultModel);
        if (CheckNull.isEmpty(staticTaskList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId: %d, actType: %d, activityId: %d, difficultModel: %d, not found taskLib",
                    player.getLordId(), actType, base.getActivityId(), difficultModel));
        }

        //当前创作任务还未完成
        Activity activity = checkAndGetActivity(player, actType);
        Map<Integer, Integer> saveMap = activity.getSaveMap();
        int difficult = saveMap.getOrDefault(SAVE_IDX_CRT_DIFFICULT_MODEL, 0);
        if (difficult > 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(),
                    String.format("roleId: %d, planId: %d, 已经选择了一个创作难度", player.getLordId(), base.getPlanKeyId()));
        }
        //[免费次数, 收费次数, 收费金额]
        List<Integer> params = ActParamConstant.MUSIC_CRT_OFFICE_PARAMS;
        if (CheckNull.isEmpty(params) || params.size() != 3) {
            throw new MwException(GameError.NO_CONFIG.getCode());
        }
        Map<Integer, Integer> statusMap = activity.getStatusMap();
        if (req.getFree() == 1) {
            //免费创作
            int freeCnt = statusMap.getOrDefault(STATUS_IDX_CRT_CNT_FREE, 0);
            if (freeCnt >= params.get(0)) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, 免费创作次数已经用完[%d - %d]", player.getLordId(), freeCnt, params.get(0)));
            }
            statusMap.merge(STATUS_IDX_CRT_CNT_FREE, 1, Integer::sum);
        } else {
            //收费创作
            int notFreeCnt = statusMap.getOrDefault(STATUS_IDX_CRT_CNT_NOT_FREE, 0);
            if (notFreeCnt >= params.get(1)) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, 收费创作次数已经用完[%d - %d]", player.getLordId(), notFreeCnt, params.get(1)));
            }
            //扣除钻石
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, params.get(2),
                    AwardFrom.ACT_CREATIVE_OFFICE_COMPOSE_MUSIC_NOT_FREE, notFreeCnt);
            statusMap.merge(STATUS_IDX_CRT_CNT_NOT_FREE, 1, Integer::sum);
        }


        //设置谱曲难度
        activity.getSaveMap().put(SAVE_IDX_CRT_DIFFICULT_MODEL, difficultModel);

        //初始化任务列表
        GamePb4.ChooseDifficultRs.Builder rsp = GamePb4.ChooseDifficultRs.newBuilder();
        int taskCount = taskCntMap.get(difficultModel);
        List<Integer> taskLibs = staticTaskList.stream()
                .filter(t -> t.getType() == difficultModel)
                .map(StaticCreativeOffice::getId).collect(Collectors.toList());
        List<Integer> randomTaskList = new ArrayList<>();
        if (taskLibs.size() <= taskCount) {
            randomTaskList.addAll(taskLibs);
        } else {
            for (int i = 0; i < taskCount; i++) {
                int taskId = taskLibs.remove(RandomUtil.randomIntExcludeEnd(0, taskLibs.size()));
                randomTaskList.add(taskId);
            }
        }
        List<ActivityTask> taskList = activity.getDayTasks().computeIfAbsent(CREATE_TASK_LIST_IDX, t -> new ArrayList<>());
        if (!taskList.isEmpty()) taskList.clear();
        for (Integer tid : randomTaskList) {
            StaticCreativeOffice staticCrtOffice = StaticMusicFestivalMgr.getStaticCreativeOfficeById(tid);
            ActivityTask task = new ActivityTask();
            task.setUid(staticCrtOffice.getId());
            task.setTaskId(staticCrtOffice.getTaskId());
            taskList.add(task);
            rsp.addCrtTask(createActTaskPb(task));
        }
        int freeCnt = statusMap.getOrDefault(STATUS_IDX_CRT_CNT_FREE, 0);
        rsp.setFreeCnt(freeCnt);
        int notFreeCnt = statusMap.getOrDefault(STATUS_IDX_CRT_CNT_NOT_FREE, 0);
        rsp.setNotFreeCnt(notFreeCnt);
        return rsp.build();
    }

    private CommonPb.ActTask createActTaskPb(ActivityTask task) {
        CommonPb.ActTask.Builder builder = CommonPb.ActTask.newBuilder();
        builder.setUid(task.getUid());
        builder.setProgress(task.getProgress());
        builder.setFinishCnt(task.getCount());
        builder.setDrawCnt(task.getDrawCount());
        return builder.build();
    }

    /**
     * 谱曲, 完成任务
     *
     * @param player 玩家
     * @param req    请求信息
     * @return 返回信息
     * @throws MwException 异常
     */
    public GamePb4.FinishCrtTaskRs finishTask(Player player, GamePb4.FinishCrtTaskRq req) throws MwException {
        int actType = req.getActType();
        validateActivityType(actType);
        int finishUid = req.getTaskUid();
        long lordId = player.getLordId();
        StaticCreativeOffice staticCreativeOffice = StaticMusicFestivalMgr.getStaticCreativeOfficeById(finishUid);
        if (Objects.isNull(staticCreativeOffice)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId: %d, actType: %d, finishUid: %d, not found!!!", lordId, actType, finishUid));
        }
        Activity activity = checkAndGetActivity(player, actType);
        List<ActivityTask> taskList = activity.getDayTasks().get(CREATE_TASK_LIST_IDX);
        if (CheckNull.isEmpty(taskList)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, actType: %d, 任务列表尚未初始化", lordId, actType));
        }
        Map<Integer, ActivityTask> taskMap = taskList.stream().collect(Collectors.toMap(ActivityTask::getUid, t -> t));
        ActivityTask task = taskMap.get(finishUid);
        if (Objects.isNull(task)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, 任务uid: %d, 不存在", player.getLordId(), finishUid));
        }
        //已经领取过奖励
        if (task.getDrawCount() > 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, 任务uid: %d, 已经领取过奖励", player.getLordId(), finishUid));
        }

        //任务未完成
        if (task.getCount() <= 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, 任务uid: %d, 未完成不能领取奖励", player.getLordId(), finishUid));
        }

        if (CheckNull.isEmpty(staticCreativeOffice.getAwardList())) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId: %d, uid: %d, awardList isEmpty", lordId, finishUid));
        }
        task.setDrawCount(task.getDrawCount() + 1);
        List<CommonPb.Award> awardPb = rewardDataManager.sendReward(player, staticCreativeOffice.getAwardList(), AwardFrom.ACT_CREATIVE_OFFICE_FINISH_TASK, activity.getActivityId(), finishUid);
        GamePb4.FinishCrtTaskRs.Builder rsp = GamePb4.FinishCrtTaskRs.newBuilder();
        rsp.addAllAward(awardPb);
        rsp.setTask(createActTaskPb(task));
        return rsp.build();
    }

    /**
     * 完成创作(谱曲)
     *
     * @param player 玩家
     * @param req    请求信息
     * @return 返回信息
     * @throws MwException 异常
     */
    public GamePb4.FinishCrtMusicRs finishCreateMusic(Player player, GamePb4.FinishCrtMusicRq req) throws MwException {
        int actType = req.getActType();
        validateActivityType(actType);
        long lordId = player.getLordId();
        Date now = new Date();
        ActivityBase base = getActivityBase(now, actType);
        Activity activity = activityDataManager.getActivityInfo(player, actType);
        if (Objects.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启");
        }
        int difficultModel = activity.getSaveMap().getOrDefault(SAVE_IDX_CRT_DIFFICULT_MODEL, 0);
        if (difficultModel < 1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, actType: %d, 当前没有选择创作难度", lordId, actType));
        }

        List<ActivityTask> taskList = activity.getDayTasks().get(CREATE_TASK_LIST_IDX);
        if (CheckNull.isEmpty(taskList)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, actType: %d, 任务列表尚未初始化", lordId, actType));
        }
        for (ActivityTask task : taskList) {
            if (task.getCount() <= 0) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, 未完成任务 uid: %d, ", lordId, task.getTaskId()));
            }
        }

        GlobalActivityCreativeOfficeData globalActivity = (GlobalActivityCreativeOfficeData) activityDataManager.getGlobalActivity(actType);
        if (Objects.isNull(globalActivity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), String.format("roleId: %d, planId: %d 活动已经结束", player.getLordId(), base.getPlanKeyId()));
        }

        //清空当前任务列表与难度标识
        activity.getSaveMap().merge(SAVE_IDX_CRT_CNT_TOTAL, 1, Integer::sum);
        activity.getSaveMap().remove(SAVE_IDX_CRT_DIFFICULT_MODEL);
        activity.getDayTasks().remove(CREATE_TASK_LIST_IDX);

        //增加阵营创作完成次数
        incCampFinishMusicCount(base, player, globalActivity);
        return GamePb4.FinishCrtMusicRs.newBuilder().build();
    }

    /**
     * 增加阵营完成作曲数量
     *
     * @param base           b
     * @param player         p
     * @param globalActivity g
     */
    private void incCampFinishMusicCount(ActivityBase base, Player player, GlobalActivityCreativeOfficeData globalActivity) {
        try {
            globalActivity.addCampValByCamp(player.getCamp(), 1);
            int campConcertIndex = getCampConcertIndex(player.getCamp());
            int state = globalActivity.getSaveMap().getOrDefault(campConcertIndex, 0);
            if (state == 0) {
                long campCrtCount = globalActivity.getCampValByCamp(player.getCamp());
                Map<Integer, StaticCreativeOfficeAward> awardMap = StaticMusicFestivalMgr.getAllCreativeOfficeAwards();
                if (CheckNull.nonEmpty(awardMap)) {
                    StaticCreativeOfficeAward award = awardMap.values().stream()
                            .filter(t -> t.getActivityId() == base.getActivityId())
                            .filter(t -> t.getType() == CREATIVE_OFFICE_AWARD_TYPE_4)
                            .findFirst().orElse(null);
                    if (Objects.nonNull(award) && campCrtCount >= award.getParams().get(0)) {
                        globalActivity.getSaveMap().put(campConcertIndex, 1);
                        int sysChatId = ChatConst.CAMP_OPEN_MUSIC_SUCCESS;
                        chatDataManager.sendSysChat(sysChatId, 0, 0, player.getCamp());
                        sendCampMusicConductSuccess(base, player.getCamp(), award);
                    }
                } else {
                    LogUtil.error(String.format("activityPlanId: %d, activityId: %d, 没有找到阵营谱曲完成数量奖励", base.getPlanKeyId(), base.getActivityId()));
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    /**
     * 放弃当前的创作
     *
     * @param player 玩家
     * @param req    请求信息
     * @return 返回信息
     * @throws MwException 异常
     */
    public GamePb4.GiveUpCrtMusicRs giveUpComposeMusic(Player player, GamePb4.GiveUpCrtMusicRq req) throws MwException {
        int actType = req.getActType();
        validateActivityType(actType);
        Activity activity = checkAndGetActivity(player, actType);
        int difficult = activity.getSaveMap().getOrDefault(SAVE_IDX_CRT_DIFFICULT_MODEL, 0);
        if (difficult < 1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(),
                    String.format("roleId: %d, activityId: %d 没有创作列表", player.getLordId(), activity.getActivityId()));
        }
        activity.getSaveMap().remove(SAVE_IDX_CRT_DIFFICULT_MODEL);
        activity.getDayTasks().remove(CREATE_TASK_LIST_IDX);
        return GamePb4.GiveUpCrtMusicRs.newBuilder().build();
    }

    public int getMusicCrtScore(Player player) {
        for (int actType : getActivityType()) {
            ActivityBase base = StaticActivityDataMgr.getActivityByType(actType);
            if (Objects.nonNull(base)) {
                Activity activity = getActivity(player, actType);
                if (Objects.nonNull(activity)) {
                    return activity.getSaveMap().getOrDefault(SAVE_IDX_SCORE, 0);
                }
            }
        }
        return 0;
    }

    /**
     * 增加创作积分
     */
    public void updateCreativeScore(Player player, int updateScore, AwardFrom from) {
        try {
            long curMill = System.currentTimeMillis();
            for (int actType : getActivityType()) {
                Activity activity = getActivity(player, actType);
                ActivityBase base = StaticActivityDataMgr.getActivityByType(actType);
                if (Objects.nonNull(base)) {
                    activity.getSaveMap().merge(SAVE_IDX_SCORE, updateScore, Integer::sum);
                    int total = activity.getSaveMap().get(SAVE_IDX_SCORE);
                    LogLordHelper.activityScore("activityMusicCrtScore", from, player, total, updateScore, activity);
                    GlobalActivityCreativeOfficeData globalActivity = (GlobalActivityCreativeOfficeData) activityDataManager.getGlobalActivity(actType);
                    if (Objects.nonNull(globalActivity)) {
                        int camp = player.getCamp();
                        globalActivity.updatePlayerRank(camp, player.getLordId(), total, curMill);
                        int campScoreIdx = getCampScoreIndex(player.getCamp());
                        int campScore = globalActivity.getSaveMap().merge(campScoreIdx, updateScore, Integer::sum);
                        globalActivity.updateCampRank(camp, campScore, curMill);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(String.format("roleId: %d, updateScore: %d error", player.getLordId(), updateScore), e);
        }
    }

    private int getCampScoreIndex(int camp) {
        return -(camp + Constant.INT_HUNDRED);
    }

    /**
     * 阵营是否成功举办过演唱会记录
     *
     * @param camp 阵营
     * @return save map index
     */
    private int getCampConcertIndex(int camp) {
        return -(camp + NumberUtil.THOUSAND);
    }

    /**
     * 获取阵营排名
     *
     * @param player 玩家
     * @param req    请求信息
     * @return 返回信息
     * @throws MwException 异常
     */
    public GamePb4.CrtMusicCampRankRs getCampRank(Player player, GamePb4.CrtMusicCampRankRq req) throws MwException {
        int actType = req.getActType();
        validateActivityType(actType);
        GlobalActivityCreativeOfficeData globalActivity = (GlobalActivityCreativeOfficeData) checkAndGetGlobalActivity(player, actType);
        int campFinishCnt = (int) globalActivity.getCampValByCamp(player.getCamp());
        GamePb4.CrtMusicCampRankRs.Builder rsp = GamePb4.CrtMusicCampRankRs.newBuilder();
        rsp.setFinishCnt(campFinishCnt);
        SimpleRank4SkipSet<Long> campRank = globalActivity.getCampRank();
        List<RankItem<Long>> rankList = campRank.getRankList(0, Constant.Camp.camps.length);
        int rank = 1;
        for (RankItem<Long> item : rankList) {
            CommonPb.RankItem.Builder itemBuilder = CommonPb.RankItem.newBuilder();
            itemBuilder.setRank(rank++);
            itemBuilder.setRankValue(item.getRankValue());
            itemBuilder.setLordId(item.getLordId());
            rsp.addCampRank(itemBuilder);
        }
        return rsp.build();
    }


    /**
     * 获取个人排名
     *
     * @param player 玩家
     * @param req    请求信息
     * @return 返回信息
     * @throws MwException 异常
     */
    public GamePb4.CrtMusicPersonRankRs getPersonRank(Player player, GamePb4.CrtMusicPersonRankRq req) throws MwException {
        int actType = req.getActType();
        validateActivityType(actType);
        int page = req.getPage();
        int pageSize = req.getPageSize();
        if (page < 1 || pageSize > 50) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, page: %d, pageSize: %d", player.getLordId(), page, pageSize));
        }

        int fromIndex = (page - 1) * pageSize;//include
        int toIndex = fromIndex + pageSize;//exclude
        GlobalActivityCreativeOfficeData globalActivity = (GlobalActivityCreativeOfficeData) checkAndGetGlobalActivity(player, actType);
        SimpleRank4SkipSet<Integer> simpleRank = globalActivity.getPersonRank(player.lord.getCamp());
        List<RankItem<Integer>> rankList = simpleRank.getRankList(fromIndex, toIndex);
        GamePb4.CrtMusicPersonRankRs.Builder rsp = GamePb4.CrtMusicPersonRankRs.newBuilder();
        int rank = fromIndex + 1;
        for (RankItem<Integer> item : rankList) {
            CommonPb.RankItem.Builder itemBuilder = CommonPb.RankItem.newBuilder();
            Player rankPlayer = playerDataManager.getPlayer(item.getLordId());
            itemBuilder.setRank(rank++);
            itemBuilder.setNick(rankPlayer.lord.getNick());
            itemBuilder.setRankValue(item.getRankValue());
            Activity activity = getActivity(rankPlayer, actType);
            int totalCnt = activity.getSaveMap().getOrDefault(SAVE_IDX_CRT_CNT_TOTAL, 0);
            itemBuilder.addParam(totalCnt);
            rsp.addItem(itemBuilder);
        }

        int myRank = simpleRank.getRank(player.getLordId());
        CommonPb.RankItem.Builder myRankBuilder = CommonPb.RankItem.newBuilder();
        myRankBuilder.setRank(myRank);
        if (myRank > 0) {
            Activity activity = checkAndGetActivity(player, actType);
            Map<Integer, Integer> saveMap = activity.getSaveMap();
            myRankBuilder.setRankValue(saveMap.getOrDefault(SAVE_IDX_SCORE, 0));
            myRankBuilder.addParam(saveMap.getOrDefault(SAVE_IDX_CRT_CNT_TOTAL, 0));
            myRankBuilder.setNick(player.lord.getNick());
        }
        rsp.setMyRank(myRankBuilder);

        return rsp.build();
    }

    /**
     * 领取个人积分奖励
     *
     * @param player 玩家
     * @param req    请求协议
     * @return 返回协议
     * @throws MwException 异常
     */
    public GamePb4.DrawProgressRs drawPersonScoreAward(Player player, GamePb4.DrawProgressRq req) throws MwException {
        int actType = req.getActType();
        validateActivityType(actType);
        int uid = req.getUid();
        long lordId = player.getLordId();
        //奖励不存在
        StaticCreativeOfficeAward sAward = StaticMusicFestivalMgr.getCreativeOfficeAward(uid);
        if (Objects.isNull(sAward)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, actType: %d, uid: %d, not found config", lordId, actType, uid));
        }
        //奖励配置的活动ID错误
        ActivityBase base = checkAndGetActivityBase(player, actType);
        if (sAward.getActivityId() != base.getActivityId()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(),
                    String.format("roleId: %d, uid: %d base.activityId: %d, award.activityId: %d", lordId, uid, base.getActivityId(), sAward.getActivityId()));
        }
        //奖励配置缺失
        List<Integer> params = sAward.getParams();
        List<List<Integer>> awards = sAward.getAwards();
        if (CheckNull.isEmpty(params) || CheckNull.isEmpty(awards)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId: %d, uid: %d, config is empty", lordId, uid));
        }

        //奖励类型错误
        if (sAward.getType() != CREATIVE_OFFICE_AWARD_TYPE_1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, uid: %d, 奖励类型错误", lordId, uid));
        }

        //已经领取过奖励
        Activity activity = checkAndGetActivity(player, actType);
        Map<Integer, Integer> saveMap = activity.getSaveMap();
        int drawCnt = saveMap.getOrDefault(uid, 0);
        if (drawCnt > 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, uid: %d, 已经领取过该奖励", lordId, uid));
        }

        if (sAward.getType() == CREATIVE_OFFICE_AWARD_TYPE_1) {
            int score = saveMap.getOrDefault(SAVE_IDX_SCORE, 0);
            int needScore = sAward.getParams().get(0);
            if (score < needScore) {
                throw new MwException(GameError.PARAM_ERROR.getCode(),
                        String.format("roleId: %d, uid: %d, score: %d need score: %d, not enough", lordId, uid, score, needScore));
            }
        }

        activity.getSaveMap().put(uid, 1);
        //奖励发放
        List<CommonPb.Award> awardPb = rewardDataManager.sendReward(player, awards, AwardFrom.ACT_DRAW_CREATIVE_OFFICE_PERSON_SCORE, base.getPlanKeyId(), uid);
        GamePb4.DrawProgressRs.Builder rsp = GamePb4.DrawProgressRs.newBuilder();
        rsp.addAllAward(awardPb);
        return rsp.build();
    }

    public GamePb4.MusicSpecialThanksRs getSpecialThanks(Player player, GamePb4.MusicSpecialThanksRq req) throws MwException {
        int actType = req.getActType();
        validateActivityType(actType);
        GlobalActivityCreativeOfficeData globalActivity = (GlobalActivityCreativeOfficeData) checkAndGetGlobalActivity(player, actType);
        SimpleRank4SkipSet<Integer> simpleRank = globalActivity.getPersonRank(player.getCamp());
        List<RankItem<Integer>> rankList = simpleRank.getRankList(0, Constant.INT_HUNDRED / 2);
        GamePb4.MusicSpecialThanksRs.Builder rsp = GamePb4.MusicSpecialThanksRs.newBuilder();
        for (RankItem<Integer> item : rankList) {
            Player rankPlayer = playerDataManager.getPlayer(item.getLordId());
            if (Objects.nonNull(rankPlayer)) {
                rsp.addNick(rankPlayer.lord.getNick());
            }
        }
        return rsp.build();
    }

    @Override
    protected int[] getActivityType() {
        return new int[]{ActivityConst.ACT_MUSIC_FESTIVAL_CREATIVE_OFFICE};
    }

    @Override
    public void process(Player player, ETask eTask, int... params) {
        try {
            int[] types = getActivityType();
            for (int actType : types) {
                ActivityBase base = StaticActivityDataMgr.getActivityByType(actType);
                if (Objects.isNull(base) || base.getStep0() != ActivityConst.OPEN_STEP) continue;
                Activity activity = activityDataManager.getActivityInfo(player, actType);
                List<ActivityTask> taskList = activity.getDayTasks().get(CREATE_TASK_LIST_IDX);
                if (CheckNull.isEmpty(taskList)) continue;
                //当前任务ID且未完成的任务列表
                List<ActivityTask> unFinishTaskList = taskList.stream()
                        .filter(t -> t.getTaskId() == eTask.getTaskType())
                        .filter(t -> t.getCount() == 0)
                        .collect(Collectors.toList());
                if (CheckNull.isEmpty(unFinishTaskList)) continue;
                for (ActivityTask actTask : unFinishTaskList) {
                    StaticCreativeOffice staticCreativeOffice = StaticMusicFestivalMgr.getStaticCreativeOfficeById(actTask.getUid());
                    if (Objects.isNull(staticCreativeOffice) || CheckNull.isEmpty(staticCreativeOffice.getParams())) {
                        continue;
                    }
                    List<Integer> cfgParams = staticCreativeOffice.getParams();
                    int needCount = CheckNull.nonEmpty(cfgParams) ? cfgParams.get(cfgParams.size() - 1) : 0;
                    boolean isChange = ActTaskUtil.updTaskSchedule(player, actTask, eTask, cfgParams, params);
                    if (isChange) {
                        LogUtil.debug(String.format("roleId: %d, 创作活动任务ID: %d, 任务类型: %d, 进度发生变化当前进度: (%d/%d)", player.getLordId(), actTask.getUid(), actTask.getTaskId(), actTask.getProgress(), needCount));
                        if (ActTaskUtil.checkTaskFinished(player, actTask, eTask, staticCreativeOffice.getParams())) {
                            actTask.setCount(1);
                            //如果任务已经完成通知客户端
                            activityDataManager.syncActChange(player, actType, 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(String.format("roleId: %d, taskTy: %d, params: %s", player.getLordId(),
                    eTask.getTaskType(), Arrays.toString(params)), e);
        }
    }


    /**
     * 阵营成功举办音乐节
     *
     * @param base 活动
     * @param camp 阵营
     */
    private void sendCampMusicConductSuccess(ActivityBase base, int camp, StaticCreativeOfficeAward award) {
        int nowSec = TimeHelper.getCurrentSecond();
        for (Player player : playerDataManager.getAllPlayer().values()) {
            if (player.getCamp() != camp) continue;
            Activity activity = getActivity(player, base.getActivityType());
            if (Objects.isNull(activity) || activity.getActivityId() != base.getActivityId()) continue;
            int crtCnt = activity.getSaveMap().getOrDefault(SAVE_IDX_CRT_CNT_TOTAL, 0);
            if (crtCnt <= 0) continue;
            // 尊敬的领主，您创作了%s乐谱，为%s阵营音乐节做出了贡献，获得如下奖励，请查收。
            List<CommonPb.Award> pbAwards = PbHelper.createAwardsPb(award.getAwards());
            mailDataManager.sendAttachMail(player, pbAwards, MailConstant.MUSIC_CAMP_CONDUCT_SUCCESS,
                    AwardFrom.ACT_CREATIVE_OFFICE_CONDUCT_SUCCESS, nowSec, crtCnt, camp);
        }
    }


    /**
     * 发放活动结束奖励
     *
     * @param activityType ty:65
     * @param activityId   活动ID
     * @param planKeyId    planKeyId
     */
    public void handleOnEndTime(int activityType, int activityId, int planKeyId) {
        LogUtil.common("活动结束结算: " + planKeyId);
        ActivityBase base = ActivityUtil.getActivityBase(planKeyId);
        Map<Integer, StaticCreativeOfficeAward> awardMap = StaticMusicFestivalMgr.getAllCreativeOfficeAwards();
        //KEY:ActivityId
        Map<Integer, List<StaticCreativeOfficeAward>> actAwardMap = awardMap.values().stream().collect(Collectors.groupingBy(StaticCreativeOfficeAward::getActivityId));
        if (Objects.nonNull(base)) {
            List<StaticCreativeOfficeAward> awardList = actAwardMap.get(activityId);
            if (CheckNull.isEmpty(awardList)) {
                LogUtil.error(String.format("planKeyId: %s, activity: %d, not found awards config !!!", planKeyId, activityId));
                return;
            }

            //KEY: 奖励类型
            Map<Integer, List<StaticCreativeOfficeAward>> typeAwardMap = awardList.stream().collect(Collectors.groupingBy(StaticCreativeOfficeAward::getType));

            //发放个人排名奖励
            List<StaticCreativeOfficeAward> personRankAwardList = typeAwardMap.get(CREATIVE_OFFICE_AWARD_TYPE_2);
            if (CheckNull.isEmpty(personRankAwardList)) {
                LogUtil.error(String.format("planKeyId: %d, activity: %d, type: %d, not found awards config !!!", planKeyId, activityId, CREATIVE_OFFICE_AWARD_TYPE_2));
                return;
            }
            sendPersonRankAward(base, personRankAwardList);
            //发放阵营排名奖励
            List<StaticCreativeOfficeAward> campRankAwardList = typeAwardMap.get(CREATIVE_OFFICE_AWARD_TYPE_3);
            if (CheckNull.isEmpty(campRankAwardList)) {
                LogUtil.error(String.format("planKeyId: %d, activity: %d, type: %d, not found awards config !!!", planKeyId, activityId, CREATIVE_OFFICE_AWARD_TYPE_3));
                return;
            }
            sendCampRankAward(base, campRankAwardList);

            //发放个人未领取的积分奖励
            List<StaticCreativeOfficeAward> personScoreAwardList = typeAwardMap.get(CREATIVE_OFFICE_AWARD_TYPE_1);
            if (CheckNull.isEmpty(personScoreAwardList)) {
                LogUtil.error(String.format("planKeyId: %d, activity: %d, type: %d, not found awards config !!!", planKeyId, activityId, CREATIVE_OFFICE_AWARD_TYPE_1));
                return;
            }
            sendUnRewardMail(base, personScoreAwardList);
        }
    }

    private void sendPersonRankAward(ActivityBase base, List<StaticCreativeOfficeAward> awards) {
        try {
            GlobalActivityCreativeOfficeData globalActivityData = (GlobalActivityCreativeOfficeData) activityDataManager.getActivityMap().get(base.getActivityType());
            StaticCreativeOfficeAward lastAward = awards.get(awards.size() - 1);
            int maxRank = lastAward.getParams().get(1);
            for (int camp : Constant.Camp.camps) {
                SimpleRank4SkipSet<Integer> simpleRank = globalActivityData.getPersonRank(camp);
                List<RankItem<Integer>> rankList = simpleRank.getRankList(0, maxRank);
                int rank = 1;
                for (RankItem<Integer> item : rankList) {
                    long lordId = item.getLordId();
                    Player player = playerDataManager.getPlayer(lordId);
                    sendPersonRankAward(base, player, rank, item, awards);
                    rank++;
                }
            }
        } catch (Exception e) {
            LogUtil.error(String.format("activityPlanId: %d, person awards send fail!!!", base.getPlanKeyId()), e);
        }
    }

    private void sendPersonRankAward(ActivityBase base, Player player, int rank, RankItem<Integer> rankItem, List<StaticCreativeOfficeAward> awards) {
        try {
            for (StaticCreativeOfficeAward award : awards) {
                List<Integer> params = award.getParams();
                if (rank >= params.get(0) && rank <= params.get(1)) {
                    List<CommonPb.Award> pbAwards = PbHelper.createAwardsPb(award.getAwards());
                    mailDataManager.sendAttachMail(player, pbAwards, MailConstant.MUSIC_PLAYER_RANK_AWARD, AwardFrom.ACT_CREATIVE_OFFICE_PLAYER_RANK_AWARD, TimeHelper.getCurrentSecond(),
                            base.getActivityType(), rank);
                    return;
                }
            }
        } catch (Exception e) {
            LogUtil.error(String.format("roleId: %d, rank: %d, rankValue: %d, 奖励发放失败!!!", player.getLordId(), rank, rankItem.getRankValue()), e);
        }
    }


    private void sendCampRankAward(ActivityBase base, List<StaticCreativeOfficeAward> awards) {
        GlobalActivityCreativeOfficeData globalActivity = (GlobalActivityCreativeOfficeData) activityDataManager.getActivityMap().get(base.getActivityType());
        SimpleRank4SkipSet<Long> simpleRank = globalActivity.getCampRank();
        int rank = 1;
        for (RankItem<Long> rankItem : simpleRank.getAll()) {
            int camp = (int) rankItem.getLordId();
            for (StaticCreativeOfficeAward award : awards) {
                if (rank == award.getParams().get(0)) {//发放阵营奖励
                    LogUtil.common(String.format("activityPlanId: %d, 阵营: %d, 积分: %d, 排名第 %d ", base.getPlanKeyId(), camp, rankItem.getRankValue(), rank));
                    sendCampRankAward(base, camp, rank, rankItem, award);
                    break;
                }
            }
            rank++;
        }
    }

    private void sendCampRankAward(ActivityBase base, int camp, int rank, RankItem<Long> item, StaticCreativeOfficeAward award) {
        try {
            List<CommonPb.Award> pbAwards = PbHelper.createAwardsPb(award.getAwards());
            int nowSec = TimeHelper.getCurrentSecond();
            for (Map.Entry<String, Player> entry : playerDataManager.getAllPlayer().entrySet()) {
                Player player = entry.getValue();
                if (player.getCamp() == camp) {
                    Activity activity = player.activitys.get(base.getActivityType());
                    if (Objects.isNull(activity) || activity.getActivityId() != base.getActivityId()) continue;
                    int crtCnt = activity.getSaveMap().getOrDefault(SAVE_IDX_CRT_CNT_TOTAL, 0);
                    if (crtCnt <= 0) continue;
                    mailDataManager.sendAttachMail(player, pbAwards, MailConstant.MUSIC_CAMP_RANK_AWARD,
                            AwardFrom.ACT_CREATIVE_OFFICE_CAMP_RANK_AWARD, nowSec,
                            camp, base.getActivityType(), rank);
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    private void sendUnRewardMail(ActivityBase base, List<StaticCreativeOfficeAward> awards) {
        int nowSec = TimeHelper.getCurrentSecond();
        for (Player player : playerDataManager.getAllPlayer().values()) {
            Activity activity = player.activitys.get(base.getActivityType());
            if (Objects.isNull(activity)) continue;
            Map<Integer, Integer> saveMap = activity.getSaveMap();
            int score = saveMap.getOrDefault(SAVE_IDX_SCORE, 0);
            List<List<Integer>> totalAwardList = new ArrayList<>();
            for (StaticCreativeOfficeAward award : awards) {
                if (saveMap.containsKey(award.getId())) continue;//已经领取过
                int needScore = award.getParams().get(0);
                if (score < needScore) continue;//积分不达标
                totalAwardList.addAll(award.getAwards());
            }
            if (totalAwardList.isEmpty()) continue;
            List<List<Integer>> mergeAwardList = RewardDataManager.mergeAward(totalAwardList);
            List<CommonPb.Award> pbAwards = PbHelper.createAwardsPb(mergeAwardList);
            mailDataManager.sendAttachMail(player, pbAwards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,
                    AwardFrom.ACT_UNREWARDED_RETURN, nowSec, base.getActivityType(), base.getActivityId(), base.getActivityType(), base.getActivityId());
        }
    }

    @Override
    public void handleOnConfigReload() {
        loadRankOnStartup();
    }

    @Override
    protected void loadRankOnStartup() {
        long nowTime = System.currentTimeMillis();
        for (int actType : getActivityType()) {
            ActivityBase base = StaticActivityDataMgr.getActivityByType(actType);
            if (Objects.isNull(base) || base.getStep0() == ActivityConst.OPEN_CLOSE) {
                continue;
            }
            GlobalActivityCreativeOfficeData globalActivity = (GlobalActivityCreativeOfficeData) activityDataManager.getGlobalActivity(actType);
            if (Objects.isNull(globalActivity)) continue;
            //更新阵营玩家排名
            for (Map.Entry<String, Player> entry : playerDataManager.getAllPlayer().entrySet()) {
                Player player = entry.getValue();
                Activity activity = activityDataManager.getActivityInfo(player, actType);
                int score = activity.getSaveMap().getOrDefault(SAVE_IDX_SCORE, 0);
                if (score <= 0) continue;
                globalActivity.updatePlayerRank(player.getCamp(), player.getLordId(), score, nowTime);
            }
            //更新阵营排名
            for (int camp : Constant.Camp.camps) {
                long campValue = globalActivity.getSaveMap().getOrDefault(getCampScoreIndex(camp), 0);
                globalActivity.updateCampRank(camp, campValue, nowTime);
            }
        }
    }

    /**
     * 该功能放弃, 但不保证以后不会用
     * 如果该阵营曾经开启过演唱会则在主城中一直展示该行为
     *
     * @return true-开启演唱会
     */
    @Deprecated
    public int showConcert(Player player) {
        try {
            for (int actType : getActivityType()) {
                //检查base 来获取数据
                GlobalActivityData gData = activityDataManager.getGlobalActivity(actType);
                //直接获取数据
                if (Objects.isNull(gData)) {
                    gData = activityDataManager.getActivityMap().get(actType);
                }
                if (Objects.nonNull(gData)) {
                    GlobalActivityCreativeOfficeData globalActivity = (GlobalActivityCreativeOfficeData) gData;
                    int campConcertIndex = getCampConcertIndex(player.getCamp());
                    return globalActivity.getSaveMap().getOrDefault(campConcertIndex, 0);
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
        return 0;
    }

    @GmCmd(GmCmdConst.musicCrt)
    @Override
    public void handleGmCmd(Player player, String... params) {
        String gmMethod = params[0];
        if ("finishTask".equals(gmMethod)) {
            int taskUid = Integer.parseInt(params[1]);
            finishTask(player, taskUid);
        } else if ("testRank".equals(gmMethod)) {
            testRank();
        }
    }

    private void testRank() {
        for (Map.Entry<String, Player> entry : playerDataManager.getAllPlayer().entrySet()) {
            Player player = entry.getValue();
            updateCreativeScore(player, RandomUtil.randomIntIncludeEnd(100, 10000), AwardFrom.DO_SOME);
            for (int actType : getActivityType()) {
                ActivityBase base = StaticActivityDataMgr.getActivityByType(actType);
                if (Objects.nonNull(base) && base.getStep0() == ActivityConst.OPEN_STEP) {
                    GlobalActivityCreativeOfficeData globalActivity = (GlobalActivityCreativeOfficeData) activityDataManager.getGlobalActivity(actType);
                    if (Objects.nonNull(globalActivity)) {
                        incCampFinishMusicCount(base, player, globalActivity);
                    }
                }
            }
        }
    }

    private void finishTask(Player player, int taskUid) {
        try {
            for (int actType : getActivityType()) {
                Activity activity = getActivity(player, actType);
                if (Objects.nonNull(activity)) {
                    List<ActivityTask> taskList = activity.getDayTasks().get(CREATE_TASK_LIST_IDX);
                    if (CheckNull.nonEmpty(taskList)) {
                        for (ActivityTask task : taskList) {
                            if (taskUid == 0 || taskUid == task.getUid()) {
                                StaticCreativeOffice sData = StaticMusicFestivalMgr.getStaticCreativeOfficeById(task.getUid());
                                if (Objects.nonNull(sData) && CheckNull.nonEmpty(sData.getParams())) {
                                    task.setProgress(sData.getParams().get(sData.getParams().size() - 1));
                                    task.setCount(1);
                                    activityDataManager.syncActChange(player, actType, 1);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

}
