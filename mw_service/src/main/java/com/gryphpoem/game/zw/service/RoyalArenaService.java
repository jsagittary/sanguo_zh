package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticRoyalArenaAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticRoyalArenaTask;
import com.gryphpoem.game.zw.resource.pojo.CampRoyalArena;
import com.gryphpoem.game.zw.resource.pojo.GlobalRoyalArena;
import com.gryphpoem.game.zw.resource.pojo.PersonRoyalArena;
import com.gryphpoem.game.zw.resource.pojo.RoyalArenaTask;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User:        zhoujie
 * Date:        2020/4/2 12:49
 * Description:
 */
@Service
public class RoyalArenaService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private ChatDataManager chatDataManager;

    /**
     * 活动阵营对拼活动的数据
     *
     * @param roleId 玩家id
     * @return 返回协议
     * @throws MwException 自定义异常
     */
    public GamePb4.GetRoyalArenaRs getRoyalArena(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GlobalRoyalArena globalRoyalArena = (GlobalRoyalArena) activityDataManager.getGlobalActivity(ActivityConst.ACT_ROYAL_ARENA);

        if (globalRoyalArena == null || !StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_ROYAL_ARENA)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "阵营比拼活动未开放, roleId:", roleId);
        }

        GamePb4.GetRoyalArenaRs.Builder builder = GamePb4.GetRoyalArenaRs.newBuilder();
        builder.setActivityId(globalRoyalArena.getActivityId());

        PersonRoyalArena personInfo = globalRoyalArena.getPersonInfoById(roleId);
        if (personInfo == null) {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "阵营对拼活动报错, roleId:", roleId);
        }
        builder.setPersonInfo(personInfo.ser());
        globalRoyalArena.getCampCampRoyalArena().values().forEach(en -> builder.addCampInfo(en.ser()));

        return builder.build();
    }


    /**
     * 领取阵营对拼活动的奖励
     *
     * @param roleId 玩家id
     * @param type   行为
     * @param id     id
     * @return 返回协议
     * @throws MwException 自定义异常
     */
    public GamePb4.RoyalArenaAwardRs royalArenaAward(long roleId, int type, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GlobalRoyalArena globalRoyalArena = (GlobalRoyalArena) activityDataManager.getGlobalActivity(ActivityConst.ACT_ROYAL_ARENA);

        // function_open解锁判断
        if (globalRoyalArena == null || !StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_ROYAL_ARENA)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "阵营比拼活动未开放, roleId:", roleId);
        }

        PersonRoyalArena personInfo = globalRoyalArena.getPersonInfoById(roleId);
        if (personInfo == null) {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "阵营对拼活动报错, roleId:", roleId);
        }
        // 活动id
        int activityId = globalRoyalArena.getActivityId();

        GamePb4.RoyalArenaAwardRs.Builder builder = GamePb4.RoyalArenaAwardRs.newBuilder();
        if (type == 1) {
            // 任务奖励
            RoyalArenaTask task = personInfo.getTask();
            if (task.getAgree() == RoyalArenaTask.TASK_AGREE_0) {
                // 还没有接受
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未接受任务, roleId:,", roleId);
            }
            StaticRoyalArenaTask sTask = StaticActivityDataMgr.getRoyalArenaTaskByActIdAndTaskId(activityId, id);
            if (CheckNull.isNull(sTask)) {
                throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "获取阵营对拼活动的任务奖励， 未找到奖励配置, activityId:", activityId, ", taskId:", id, ",roleId:,", roleId);
            }
            if (task.getStatus() != TaskType.TYPE_STATUS_FINISH && task.getSchedule() < sTask.getCond()) {
                // 任务状态不对
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动任务状态错误, roleId:,", roleId);
            }
            if (task.getTaskId() != id) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动任务id不对, roleId:,", roleId);
            }
            // 添加贡献
            addPst(globalRoyalArena, personInfo, player, sTask);
            // 设置任务的领取状态
            task.setStatus(TaskType.TYPE_STATUS_REWARD);
            // 刷新任务
            globalRoyalArena.refreshTask(personInfo);
            // 埋点日志
            LogLordHelper.commonLog("royalArena", AwardFrom.ROYAL_ARENA_TASK_AWARD, player, sTask.getId(), sTask.getStar());
        } else if (type == 2) {
            // 贡献度奖励
            StaticRoyalArenaAward sAward = StaticActivityDataMgr.getRoyalArenaAwardByActIdAndAwardId(activityId, id);
            if (CheckNull.isNull(sAward)) {
                throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "获取阵营对拼活动的贡献度奖励， 未找到奖励配置, activityId:", activityId, ", awardId:", id, ",roleId:,", roleId);
            }
            Map<Integer, Integer> awardStatus = personInfo.getAwardStatus();
            int status = awardStatus.getOrDefault(id, 0);
            if (status == 1) {
                throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "已领取奖励, roleId:,", roleId, ", activityId:", activityId, ", awardId:", id);
            }
            if (personInfo.getContribution() < sAward.getGloryPts()) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动贡献值不够, roleId:,", roleId);
            }
            // 设置awardId为已领取
            awardStatus.put(id, 1);
            // 领取奖励
            List<List<Integer>> reward = sAward.getReward();
            builder.addAllAward(rewardDataManager.sendReward(player, reward, AwardFrom.ROYAL_ARENA_PST_AWARD));
        }
        builder.setPersonInfo(personInfo.ser());
        builder.setCampInfo(globalRoyalArena.getCampCampRoyalArenaByCamp(player.lord.getCamp()).ser());
        return builder.build();
    }

    /**
     * 添加贡献度
     *
     * @param globalRoyalArena 公共阵营对拼数据
     * @param personInfo       个人对拼数据
     * @param player           玩家对象
     * @param sTask            任务配置
     */
    private void addPst(GlobalRoyalArena globalRoyalArena, PersonRoyalArena personInfo, Player player, StaticRoyalArenaTask sTask) {
        if (globalRoyalArena == null || personInfo == null || player == null || sTask == null) {
            return;
        }
        CampRoyalArena campRoyalArena = globalRoyalArena.getCampCampRoyalArenaByCamp(player.lord.getCamp());
        // 大国风范系数
        int radio = campRoyalArena.getCountryStyleRadio();
        // 加成的上限
        radio = radio > (int) Constant.TEN_THROUSAND ? (int) Constant.TEN_THROUSAND : radio;
        // 配置的贡献值
        int pts = sTask.getPts();
        if (radio > 0) {
            // 加成后的贡献值
            pts = (int) Math.ceil(pts * (1 + radio / Constant.TEN_THROUSAND));
        }
        addPst(globalRoyalArena, personInfo, player, pts);
    }

    /**
     * 添加玩家的贡献值
     *
     * @param globalRoyalArena 阵营对拼的活动数据
     * @param personInfo       玩家活动数据
     * @param player           玩家数据
     * @param add              添加的贡献值
     */
    public void addPst(GlobalRoyalArena globalRoyalArena, PersonRoyalArena personInfo, Player player, int add) {
        if (globalRoyalArena == null || personInfo == null || player == null || add == 0) {
            return;
        }
        CampRoyalArena campRoyalArena = globalRoyalArena.getCampCampRoyalArenaByCamp(player.lord.getCamp());
        // 给阵营加
        campRoyalArena.addPst(add);
        // 给玩家加
        personInfo.addPst(add);
        // 更新排行榜
        activityDataManager.updRankActivity(player, ActivityConst.ACT_ROYAL_ARENA, add);
    }


    /**
     * 任务相关协议
     *
     * @param roleId  玩家id
     * @param type    任务的行为
     * @param useGold 是否使用金币
     * @return 返回协议
     * @throws MwException 自定义异常
     */
    public GamePb4.RoyalArenaTaskRs royalArenaTask(long roleId, int type, int useGold) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GlobalRoyalArena globalRoyalArena = (GlobalRoyalArena) activityDataManager.getGlobalActivity(ActivityConst.ACT_ROYAL_ARENA);

        // function_open解锁判断
        if (globalRoyalArena == null || !StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_ROYAL_ARENA)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "阵营比拼活动未开放, roleId:", roleId);
        }

        PersonRoyalArena personInfo = globalRoyalArena.getPersonInfoById(roleId);
        if (personInfo == null) {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "阵营对拼活动报错, roleId:", roleId);
        }
        // 活动id
        int activityId = globalRoyalArena.getActivityId();
        // 1 同意任务
        RoyalArenaTask task = personInfo.getTask();
        if (type == 1) {
            if (task.getAgree() == RoyalArenaTask.TASK_AGREE_1) {
                throw new MwException(GameError.ROYAL_ARENA_TASK_ALREAY_HAVE.getCode(), "阵营对拼活动任务已经领取了, roleId:", roleId);
            }
            boolean agree = false;
            // 判断任务的完成次数
            if (personInfo.getFulfilTask() < ActParamConstant.ROYAL_ARENA_TASK_CNT.get(0).get(0) + personInfo.getExtraTask()) {
                // 增加次数
                personInfo.incrementTaskcnt();
                agree = true;
            }
            if (agree) {
                // 领取任务
                task.setAgree(RoyalArenaTask.TASK_AGREE_1);
                // 星级
                int star = task.getStar();
                // 任务结束时间
                int now = TimeHelper.getCurrentSecond();
                int refreshTime = now + ActParamConstant.ROYAL_ARENA_TASK_ENDTIME.get(0).get(0) + ((star - 1) * ActParamConstant.ROYAL_ARENA_TASK_ENDTIME.get(1).get(0));
                if (refreshTime > now) {
                    // 设置任务的结束时间
                    task.setEndTime(refreshTime);
                }
            }
        } else if (type == 2) {
            // 2 放弃任务(放弃任务需要先领取)
            if (task.getAgree() == RoyalArenaTask.TASK_AGREE_0) {
                throw new MwException(GameError.ROYAL_ARENA_TASK_ABANDON_NEED_AGREE.getCode(), "阵营对拼活动放弃任务需要先领取任务， roleId:", roleId);
            }
            // 刷新任务
            globalRoyalArena.refreshTask(personInfo);
        } else if (type == 3) {
            // 3 刷新任务星级
            boolean refresh = false;
            if (useGold == 0 && task.getFreeRefreshCnt() < ActParamConstant.ROYAL_ARENA_TASK_FREE_REFRESH_CNT) {
                task.incrementFreeCnt();
                refresh = true;
            } else if (useGold == 1) {
                rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, ActParamConstant.ROYAL_ARENA_TASK_REFRESH_PRICE, AwardFrom.ROYAL_ARENA_BUY_TASK_CNT, true);
                refresh = true;
            }
            if (refresh) {
                StaticRoyalArenaTask sTask = StaticActivityDataMgr.getRoyalArenaTaskByActIdAndTaskId(activityId, task.getTaskId());
                List<StaticRoyalArenaTask> taskList = StaticActivityDataMgr.getRoyalArenaTaskList(activityId);
                if (CheckNull.isEmpty(taskList)) {
                    throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "获取阵营对拼刷新星级， 未找到奖励配置, activityId:", activityId, ",roleId:,", roleId);
                }
                // 过滤同类型的任务
                List<StaticRoyalArenaTask> sameTypeTasks = taskList.stream().filter(s -> s.getTaskType() == sTask.getTaskType() && s.getParam() == sTask.getParam()).collect(Collectors.toList());
                if (CheckNull.isEmpty(sameTypeTasks)) {
                    throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "获取阵营对拼刷新星级， 未找到奖励配置, activityId:", activityId, ",roleId:,", roleId);
                }
                // 根据权重刷新星级
                StaticRoyalArenaTask refreshTask = RandomUtil.getWeightByList(sameTypeTasks, StaticRoyalArenaTask::getRandomStr);
                // 刷新星级
                task.refreshStart(refreshTask);
            }
        } else if (type == 4) {
            // 购买任务次数
            if (personInfo.getExtraTask() < ActParamConstant.ROYAL_ARENA_TASK_CNT.get(1).get(0)) {
                int buyCnt = personInfo.getExtraTask();
                int firstPrice = ActParamConstant.ROYAL_ARENA_TASK_PRICE.get(0).get(0);
                int price = firstPrice + (buyCnt * ActParamConstant.ROYAL_ARENA_TASK_PRICE.get(1).get(0));
                rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, price, AwardFrom.ROYAL_ARENA_BUY_TASK_CNT, true);
                // 增加购买次数
                personInfo.incrementExtraTaskcnt();
            }
        }
        GamePb4.RoyalArenaTaskRs.Builder builder = GamePb4.RoyalArenaTaskRs.newBuilder();
        builder.setPersonInfo(personInfo.ser());
        return builder.build();
    }

    /**
     * 阵营对拼活动技能相关
     *
     * @param roleId 玩家id
     * @param type   类型
     * @return
     * @throws MwException
     */
    public GamePb4.RoyalArenaSkillRs royalArenaSkill(long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GlobalRoyalArena globalRoyalArena = (GlobalRoyalArena) activityDataManager.getGlobalActivity(ActivityConst.ACT_ROYAL_ARENA);

        // function_open解锁判断
        if (globalRoyalArena == null || !StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_ROYAL_ARENA)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "阵营比拼活动未开放, roleId:", roleId);
        }

        PersonRoyalArena personInfo = globalRoyalArena.getPersonInfoById(roleId);
        if (personInfo == null) {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "阵营对拼活动报错, roleId:", roleId);
        }

        int pCamp = player.lord.getCamp();
        CampRoyalArena campRoyalArena = globalRoyalArena.getCampCampRoyalArenaByCamp(pCamp);
        if (campRoyalArena == null) {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "阵营对拼活动报错, roleId:", roleId);
        }
        GamePb4.RoyalArenaSkillRs.Builder builder = GamePb4.RoyalArenaSkillRs.newBuilder();
        if (type == 1) {
            // 购买大国风范
            if (personInfo.getCountryStyleCnt() + 1 > ActParamConstant.ROYAL_ARENA_COUNTRY_STYLE_CNT.get(0).get(0)) {
                throw new MwException(GameError.ROYAL_ARENA_COUNTRY_STYLE_CNT_ERR.getCode(), "阵营对拼购买大国风范个人次数上限了, roleId:", roleId, ", cnt:", personInfo.getCountryStyleCnt());
            }
            if (campRoyalArena.getCountryStyleCnt() + 1 > ActParamConstant.ROYAL_ARENA_COUNTRY_STYLE_CNT.get(1).get(0)) {
                throw new MwException(GameError.ROYAL_ARENA_COUNTRY_STYLE_CNT_ERR.getCode(), "阵营对拼购买大国风范阵营次数上限了, roleId:", roleId, ", cnt:", campRoyalArena.getCountryStyleCnt());
            }
            // 扣除资源
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, ActParamConstant.ROYAL_ARENA_COUNTRY_STYLE_CONSUME, AwardFrom.ROYAL_ARENA_COUNTRY_CNT, true);
            // 跑马灯
            chatDataManager.sendSysChat(ChatConst.CHAT_ROYAL_ARENA_COUNTRY_BUY, player.lord.getCamp(), 0,
                    player.lord.getNick(), player.lord.getCamp());
            // 自增购买次数
            personInfo.incrementCountryStyleCnt();
            campRoyalArena.incrementCountryStyleCnt();
            // 增加贡献值
            addPst(globalRoyalArena, personInfo, player, ActParamConstant.ROYAL_ARENA_COUNTRY_STYLE.get(1).get(0));
        } else if (type == 2) {
            // 购买刺探
            if (personInfo.getDetectCnt() + 1 > ActParamConstant.ROYAL_ARENA_DETECT_STYLE_CNT.get(0).get(0)) {
                throw new MwException(GameError.ROYAL_ARENA_COUNTRY_STYLE_CNT_ERR.getCode(), "阵营对拼购买刺探个人次数上限了, roleId:", roleId, ", cnt:", personInfo.getDetectCnt());
            }
            if (campRoyalArena.getDetectCnt() + 1 > ActParamConstant.ROYAL_ARENA_DETECT_STYLE_CNT.get(1).get(0)) {
                throw new MwException(GameError.ROYAL_ARENA_COUNTRY_STYLE_CNT_ERR.getCode(), "阵营对拼购买刺探阵营次数上限了, roleId:", roleId, ", cnt:", campRoyalArena.getDetectCnt());
            }
            // 扣除资源
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, ActParamConstant.ROYAL_ARENA_DETECT_STYLE_CONSUME, AwardFrom.ROYAL_ARENA_DETECT_CNT, true);
            // 自增购买次数
            personInfo.incrementDetectStyleCnt();
            campRoyalArena.incrementDetectStyleCnt();
            // 增加贡献值
            addPst(globalRoyalArena, personInfo, player, ActParamConstant.ROYAL_ARENA_DETECT_STYLE.get(1).get(0));
            List<Integer> camps = Arrays.stream(Constant.Camp.camps).filter(camp -> camp != pCamp).boxed().collect(Collectors.toList());
            // 被刺探的阵营
            int randomCamp = RandomUtil.getListRandom(camps, 1).get(0);
            CampRoyalArena randomCampData = globalRoyalArena.getCampCampRoyalArenaByCamp(randomCamp);
            if (CheckNull.isNull(randomCampData)) {
                throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "阵营对拼活动报错, roleId:", roleId);
            }
            // 当前阵营被刺探减少贡献值
            int contribution = randomCampData.getContribution();
            contribution = contribution - ActParamConstant.ROYAL_ARENA_DETECT_STYLE.get(0).get(0);
            // 跑马灯
            chatDataManager.sendSysChat(ChatConst.CHAT_ROYAL_ARENA_DETECT_BUY, player.lord.getCamp(), 0,
                    randomCamp);
            randomCampData.setContribution(contribution);
            builder.addCampInfo(randomCampData.ser());
        }
        builder.setPersonInfo(personInfo.ser());
        builder.addCampInfo(campRoyalArena.ser());
        return builder.build();
    }


    /**
     * 更新任务进度
     *
     * @param roleId   玩家id
     * @param cond     任务类型
     * @param schedule 进度
     * @param param    可变参数
     */
    public void updTaskSchedule(long roleId, int cond, int schedule, int... param) {
        Player player = playerDataManager.getPlayer(roleId);
        // 解锁判断
        if (CheckNull.isNull(player) || !StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_ROYAL_ARENA)) {
            return;
        }
        GlobalRoyalArena globalRoyalArena = (GlobalRoyalArena) activityDataManager.getGlobalActivity(ActivityConst.ACT_ROYAL_ARENA);
        if (CheckNull.isNull(globalRoyalArena)) {
            return;
        }
        updTask(globalRoyalArena.getPersonInfoById(roleId), globalRoyalArena.getActivityId(), roleId, cond, schedule, param);
    }

    /**
     * 更新任务
     *
     * @param personInfo 玩家数据
     * @param actId      活动id
     * @param roleId     玩家id
     * @param cond       任务类型
     * @param schedule   进度
     * @param param      可变参数
     */
    private void updTask(PersonRoyalArena personInfo, int actId, long roleId, int cond, int schedule, int[] param) {
        if (CheckNull.isNull(personInfo)) {
            return;
        }
        RoyalArenaTask task = personInfo.getTask();
        // 现在的时间
        int now = TimeHelper.getCurrentSecond();
        // 没有任务或者任务还未接受, 过了任务的结束时间
        if (CheckNull.isNull(task) || task.getAgree() == RoyalArenaTask.TASK_AGREE_0 || now > task.getEndTime()) {
            return;
        }
        // 任务配置
        StaticRoyalArenaTask sTask = StaticActivityDataMgr.getRoyalArenaTaskByActIdAndTaskId(actId, task.getTaskId());
        if (CheckNull.isNull(sTask)) {
            return;
        }
        // 不是当前类型的任务
        if (cond != sTask.getTaskType()) {
            return;
        }
        // 63类型需要特殊处理
        if (cond == TaskType.COND_RESOURCE_CNT && sTask.getParam() != param[0]) {
            return;
        }
        // 更新任务的进度
        modifyTaskSchedule(sTask, task, schedule, param);
        // 同步任务
        syncTaskSchedule(personInfo, roleId);
    }

    /**
     * 同步任务的变动
     *
     * @param personInfo 玩家数据
     * @param roleId     玩家id
     */
    private void syncTaskSchedule(PersonRoyalArena personInfo, long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        if (!CheckNull.isNull(player) && !CheckNull.isNull(personInfo)) {
            GamePb4.SyncRoyalArenaTaskRs.Builder builder = GamePb4.SyncRoyalArenaTaskRs.newBuilder();
            builder.setPersonInfo(personInfo.ser());
            BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb4.SyncRoyalArenaTaskRs.EXT_FIELD_NUMBER, GamePb4.SyncRoyalArenaTaskRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 修改任务进度
     *
     * @param sTask    任务配置
     * @param task     任务
     * @param schedule 进度
     * @param param    可变参数
     */
    private void modifyTaskSchedule(StaticRoyalArenaTask sTask, RoyalArenaTask task, int schedule, int[] param) {
        // 已经达成条件
        if (task.getSchedule() >= sTask.getCond()) {
            return;
        }
        int sCondId = sTask.getParam();
        int paramId = param.length > 0 ? param[0] : 0;
        switch (sTask.getTaskType()) {
            case TaskType.COND_RECRUIT_ARMS_CNT:
            case TaskType.COND_COMBAT_37:
            case TaskType.COND_JOIN_CAMP_BATTLE_41:
            case TaskType.COND_ATTCK_PLAYER_CNT:
            case TaskType.COND_WASH_HERO_42:
            case TaskType.COND_STONE_COMBAT_47:
            case TaskType.COND_STONE_COMPOUND_COMBAT_48:
            case TaskType.COND_RESOURCE_CNT:
            case TaskType.COND_SUB_GOLD_CNT:
            case TaskType.COND_GOLD_CNT:
            case TaskType.COND_EXPLOIT_CNT:
            case TaskType.COND_EQUIP_BAPTIZE:
            case TaskType.COND_USE_PROP:
            case TaskType.COND_ONLINE_AWARD_CNT:
                // 等于配置的条件, 就将进度+1
                if (sCondId == 0 || sCondId == paramId) {
                    task.setSchedule(task.getSchedule() + schedule, sTask);
                }
                break;
            case TaskType.COND_BANDIT_LV_CNT:
                // 大于装备的品质
                if (sCondId == 0 || paramId >= sCondId) {
                    task.setSchedule(task.getSchedule() + schedule, sTask);
                }
                break;
        }
    }

    /**
     * 转点清除玩家任务完成次数、每日购买次数、技能的购买次数
     */
    public void clearTaskAndData() {
        GlobalRoyalArena globalRoyalArena = (GlobalRoyalArena) activityDataManager.getGlobalActivity(ActivityConst.ACT_ROYAL_ARENA);

        if (globalRoyalArena == null) {
            return;
        }
        globalRoyalArena.clearTaskAndData();
    }

    /**
     * 阵营对拼活动的定时器, 主要用于清除过期的活动(过期的活动, 让玩家自己放弃)
     */
    @Deprecated
    public void personTaskLogic() {
        GlobalRoyalArena globalRoyalArena = (GlobalRoyalArena) activityDataManager.getGlobalActivity(ActivityConst.ACT_ROYAL_ARENA);
        if (globalRoyalArena == null) {
            return;
        }
        int now = TimeHelper.getCurrentSecond();
        globalRoyalArena.getPersonRoyalArena().values().stream()
                // 过滤有任务
                .filter(p -> !CheckNull.isNull(p.getTask()))
                // 过滤任务已过期
                .filter(p -> now > p.getTask().getEndTime() && p.getTask().getEndTime() > 0)
                .forEach(p -> {
                    // 刷新任务
                    globalRoyalArena.refreshTask(p);
                    // 同步任务的变动
                    syncTaskSchedule(p, p.getRoleId());
                });
    }
}
