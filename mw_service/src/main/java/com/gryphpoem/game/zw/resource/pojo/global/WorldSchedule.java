package com.gryphpoem.game.zw.resource.pojo.global;

import com.alibaba.fastjson.JSONArray;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.GlobalConstant;
import com.gryphpoem.game.zw.resource.constant.PlayerConstant;
import com.gryphpoem.game.zw.resource.constant.ScheduleConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticSchedule;
import com.gryphpoem.game.zw.resource.domain.s.StaticScheduleGoal;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.WorldScheduleService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author QiuKun
 * @ClassName WorldSchedule.java
 * @Description 世界进度
 * @date 2019年2月21日
 */
public class WorldSchedule {

    /**
     * 进度id
     */
    private int id;

    /**
     * 开始时间,秒值
     */
    private int startTime;

    /**
     * 预计结束时间,没有预计结束时间-1
     */
    private int finishTime;

    /**
     * 状态 {@link com.gryphpoem.game.zw.resource.constant.ScheduleConstant SCHEDULE_STATUS进度状态定义}
     */
    private int status;

    /**
     * 目标的进度值 key= goalId, val= scheduleGoal 限时目标进度
     */
    private Map<Integer, ScheduleGoal> goal;

    /**
     * 条件进度, key=condId, val = schedule目标
     */
    private Map<Integer, Integer> statusCnt = new HashMap<>(10);

    /**
     * 排行数据
     */
    private ScheduleRank rank;

    /**
     * 世界进度的Boss
     */
    private ScheduleBoss boss;

    /**
     * 可攻打城池type 格式: [cityType]
     */
    private List<Integer> attackCity;

    public WorldSchedule() {
    }

    /**
     * 序列化
     *
     * @return
     */
    public CommonPb.WorldSchedule ser(Player player, boolean isSer) {
        CommonPb.WorldSchedule.Builder builder = CommonPb.WorldSchedule.newBuilder();
        builder.setId(this.id);
        builder.setStartTime(this.startTime);
        builder.setFinishTime(this.finishTime);
        builder.setStatus(this.status);
        if (!CheckNull.isEmpty(this.goal)) {
            for (ScheduleGoal scheduleGoal : this.goal.values()) {
                builder.addGoal(scheduleGoal.ser(player));
            }
        }
        // 限时目标进度, 有公共进度, 也有个人进度
        StaticSchedule staticSchedule = StaticWorldDataMgr.getScheduleById(this.id);
        if (!CheckNull.isNull(staticSchedule)) {
            for (int goalId : staticSchedule.getGoal()) {
                StaticScheduleGoal sScheduleGoal = StaticWorldDataMgr.getScheduleGoalById(goalId);
                if (CheckNull.isNull(sScheduleGoal)) {
                    continue;
                }
                int condId = sScheduleGoal.getCond();
                int val = CheckNull.isNull(player) ? this.statusCnt.getOrDefault(condId, 0) : getCondSchedule(player, condId);
                builder.addStatusCnt(PbHelper.createTwoIntPb(condId, val));
            }
        }
        // 序列化排行榜
        if (this.rank != null) {
            builder.setRank(this.rank.ser(isSer));
        }
        if (!CheckNull.isNull(this.boss)) {
            builder.setBoss(this.boss.ser());
        }
        return builder.build();
    }

    /**
     * 反序列化
     *
     * @param schedule
     */
    public WorldSchedule(CommonPb.WorldSchedule schedule) {
        this();
        this.id = schedule.getId();
        this.startTime = schedule.getStartTime();
        this.finishTime = schedule.getFinishTime();
        this.status = schedule.getStatus();
        List<CommonPb.ScheduleGoal> goalList = schedule.getGoalList();
        this.goal = new HashMap<>();
        if (!CheckNull.isEmpty(goalList)) {
            for (CommonPb.ScheduleGoal scheduleGoal : goalList) {
                this.goal.put(scheduleGoal.getId(), new ScheduleGoal(scheduleGoal));
            }
        }
        List<CommonPb.TwoInt> statusCntList = schedule.getStatusCntList();
        if (!CheckNull.isEmpty(statusCntList)) {
            for (CommonPb.TwoInt twoInt : statusCntList) {
                this.statusCnt.put(twoInt.getV1(), twoInt.getV2());
            }
        }
        // 反序列化排行榜
        this.rank = new ScheduleRank(schedule.getRank());

        CommonPb.ScheduleBoss boss = schedule.getBoss();
        if (!CheckNull.isNull(boss)) {
            this.boss = new ScheduleBoss(boss);
        }

    }

    /**
     * 根据配置创建WorldSchedule对象
     *
     * @param sSch   配置
     * @param status {@link com.gryphpoem.game.zw.resource.constant.ScheduleConstant SCHEDULE_STATUS进度状态定义}
     * @return
     */
    public static WorldSchedule create(StaticSchedule sSch, int status) {
        WorldSchedule worldSchedule = new WorldSchedule();
        worldSchedule.id = sSch.getId();

        // 没有排行榜
        if (sSch.getRankType() != 0) {
            worldSchedule.rank = new ScheduleRank(sSch);
        }
        // 可攻击cityType
        worldSchedule.attackCity = sSch.getAttckCity();
        // boss血量 , 如果世界任务过了boss进度, 也要初始化0血量的boss占位置
        if (!CheckNull.isEmpty(sSch.getBossForm()) && CheckNull.isNull(worldSchedule.boss)) {
            worldSchedule.boss = ScheduleBoss.createBoss(sSch.getId(), status != ScheduleConstant.SCHEDULE_STATUS_FINISH || status != ScheduleConstant.SCHEDULE_STATUS_NOT_YET_BEGIN ? sSch.getBossForm() : null);
        }
        // 状态记录
        worldSchedule.status = status;
        // 正在进行的任务, 初始化
        if (status == ScheduleConstant.SCHEDULE_STATUS_PROGRESS) {
            worldSchedule.progressSchedule(sSch);
        }

        // 初始化限时目标
        if (!CheckNull.isEmpty(sSch.getGoal()) && CheckNull.isEmpty(worldSchedule.goal)) {
            worldSchedule.goal = new HashMap<>(10);
            WorldScheduleService service = DataResource.ac.getBean(WorldScheduleService.class);
            for (int goalId : sSch.getGoal()) {
                StaticScheduleGoal sScheduleGoal = StaticWorldDataMgr.getScheduleGoalById(goalId);
                if (!CheckNull.isNull(sScheduleGoal)) {
                    int condId = sScheduleGoal.getCond();
                    // 初始化默认的目标值
                    worldSchedule.goal.put(goalId, new ScheduleGoal(goalId));
                    // 更新限时目标的完成
                    worldSchedule.updateCondStatus(condId, service.achieveGoal(sScheduleGoal, null));
                }
            }
        }
        return worldSchedule;
    }

    /**
     * 正在进行的任务, 初始化时间等信息
     *
     * @param sSch
     */
    public void progressSchedule(StaticSchedule sSch) {
        // 设置正在进行中的状态
        setStatus(ScheduleConstant.SCHEDULE_STATUS_PROGRESS);
        // 正在进行的世界任务
        if (!CheckNull.isNull(sSch)) {
            Date openTime = DateHelper.parseDate(DataResource.ac.getBean(ServerSetting.class).getOpenTime());
            // 第一个阶段, 以开服时间来计算开启时间
            int now = sSch.getId() == 1 ? TimeHelper.dateToSecond(openTime) : TimeHelper.getCurrentSecond();
            setStartTime(now);
            // 持续时间
            int durationTime = sSch.getDurationTime();
            if (durationTime > 0) {
                setFinishTime(TimeHelper.getSomeDayAfter(TimeHelper.secondToDate(this.startTime), durationTime - 1, 23, 59, 59));
            } else {
                setFinishTime(durationTime);
            }
            // 设置排行榜参数
            setRank(new ScheduleRank(sSch));
            // 初始化限时目标
            initGoal(sSch);
            //setFinishTime(durationTime > 0 ? now + (TimeHelper.DAY_S * durationTime) : durationTime);
        }
    }

    /**
     * 初始化限时目标
     * @param staticSchedule 世界进程配置
     */
    public void initGoal(StaticSchedule staticSchedule) {
        // 初始化限时目标
        if (!CheckNull.isEmpty(staticSchedule.getGoal())) {
            this.goal = new HashMap<>(10);
            WorldScheduleService service = DataResource.ac.getBean(WorldScheduleService.class);
            for (int goalId : staticSchedule.getGoal()) {
                StaticScheduleGoal sScheduleGoal = StaticWorldDataMgr.getScheduleGoalById(goalId);
                if (!CheckNull.isNull(sScheduleGoal)) {
                    int condId = sScheduleGoal.getCond();
                    // 初始化默认的目标值
                    this.goal.put(goalId, new ScheduleGoal(goalId));
                    // 更新限时目标的完成
                    this.updateCondStatus(condId, service.achieveGoal(sScheduleGoal, null));
                }
            }
        }
    }

    /**
     * 可以领取限时目标
     *
     * @return
     */
    public boolean canReward(Player player, StaticScheduleGoal ssg) {
        boolean canReward = false;
        if (!CheckNull.isNull(this.goal)) {
            ScheduleGoal scheduleGoal = this.goal.get(ssg.getId());
            if (!CheckNull.isNull(scheduleGoal)) {
                // 进度达到, 没有领奖
                if (enoughSchedule(player, ssg) && !alreadyReward(player, scheduleGoal)) {
                    canReward = true;
                }
            }
        }
        return canReward;
    }


    /**
     * 更新指定目标的进度, 注意: 已经达成目标不更新进度
     *
     * @param player
     * @param ssg
     * @param param
     * @return sync 同步目标达成
     */
    public boolean updateScheduleGoal(Player player, StaticScheduleGoal ssg, int param) {
        boolean sync = false;
        if (CheckNull.isNull(this.goal)) {
            LogUtil.error("更新目标进度, goalMap目标bean未初始化, worldScheduleId:", this.id);
            return sync;
        }
        int goalId = ssg.getId();
        ScheduleGoal goal = this.goal.get(goalId);
        if (CheckNull.isNull(goal)) {
            LogUtil.error("更新目标进度, goal目标bean未初始化, worldScheduleId:", this.id, ", goalId:", goalId);
            return sync;
        }
        // 进度足够了
        if (enoughSchedule(player, ssg)) {
            return sync;
        }
        // 已经领取的目标不再更新
        if (alreadyReward(player, goal)) {
            return sync;
        }
        // 可以更新目标进度
        if (canUpdateSched(ssg, param)) {
            // 更新目标的进度
            updateSchedule(player, ssg, param);
            // 进度足够了
            if (enoughSchedule(player, ssg)) {
                sync = true;
            }
        }
        return sync;
    }

    /**
     * 目标进度是否达成
     *
     * @param ssg
     * @return
     */
    public boolean enoughSchedule(Player player, StaticScheduleGoal ssg) {
        return getCondSchedule(player, ssg.getCond()) >= ssg.getSchedule();
    }

    /**
     * 获取目标的进度
     *
     * @param player
     * @param condId
     * @return
     */
    private int getCondSchedule(Player player, int condId) {
        int schedule;
        if (condId == ScheduleConstant.GOAL_COND_FIGHT) {
            schedule = player.getMixtureDataById(PlayerConstant.WORLD_SCHEDULE_MAX_FIGHT);
        } else if (condId == ScheduleConstant.GOAL_COND_ATTACK_BOSS) {
            schedule = CheckNull.isNull(boss) ? 0 : boss.getFightCnt().get();
        } else {
            schedule = this.statusCnt.getOrDefault(condId, 0);
        }
        return schedule;
    }

    /**
     * 更新目标进度
     *
     * @param player
     * @param sScheduleGoal
     * @param param
     */
    public void updateSchedule(Player player, StaticScheduleGoal sScheduleGoal, int param) {
        int condId = sScheduleGoal.getCond();
        int schedule = getCondSchedule(player, condId);
        if (condId == ScheduleConstant.GOAL_COND_FIGHT) {
            if (param > schedule) {
                player.setMixtureData(PlayerConstant.WORLD_SCHEDULE_MAX_FIGHT, param);
            }
        } else if (condId == ScheduleConstant.GOAL_COND_ATTACK_BOSS) {
            if (!CheckNull.isNull(this.boss)) {
                this.boss.getFightCnt().incrementAndGet();
            }
        } else if (condId == ScheduleConstant.GOAL_COND_CONQUER_CITY) {
            // 更新被占领的城池
            GlobalDataManager globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
            GameGlobal gameGlobal = globalDataManager.getGameGlobal();
            Map<Integer, Integer> cityMap = gameGlobal.getMixtureDataById(GlobalConstant.WORLD_SCHEDULE_GOAL_COND_CONQUER_CITY);
            cityMap.put(param, 1);
            gameGlobal.setMixtureData(GlobalConstant.WORLD_SCHEDULE_GOAL_COND_CONQUER_CITY, cityMap);
            // 更新限时目标的进度
            this.statusCnt.put(condId, schedule + 1);
        } else {
            this.statusCnt.put(condId, schedule + 1);
        }
    }

    /**
     * 判断是否可以更新目标进度
     *
     * @param ssg
     * @param param
     * @return
     */
    private boolean canUpdateSched(StaticScheduleGoal ssg, int param) {
        boolean canUpdate = false;
        // 正在进行的目标 或者 已经结束的目标, 并且结束后是否可以继续增加进度
        if (this.status == ScheduleConstant.SCHEDULE_STATUS_PROGRESS || (this.status == ScheduleConstant.SCHEDULE_STATUS_FINISH
                && ssg.canFinishUpdate())) {
            switch (ssg.getCond()) {
                // 指挥官基地提升至N级
                case ScheduleConstant.GOAL_COND_COMMAND_LV:
                // 全服有N个N次觉醒英雄, 这里授勋次数判断等于
                case ScheduleConstant.GOAL_COND_HERO_DECORATED:
                    int lv = ssg.getCondId();
                    if (param == lv) {
                        canUpdate = true;
                    }
                    break;
                // N个指定type城池被攻克
                case ScheduleConstant.GOAL_COND_CONQUER_CITY:
                    StaticCity sCity = StaticWorldDataMgr.getCityMap().get(param);
                    if (!CheckNull.isNull(sCity)) {
                        int type = sCity.getType();
                        int cityType = ssg.getCondId();
                        if (cityType == type) {
                            GlobalDataManager globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
                            GameGlobal gameGlobal = globalDataManager.getGameGlobal();
                            Map<Integer, Integer> cityMap = gameGlobal.getMixtureDataById(GlobalConstant.WORLD_SCHEDULE_GOAL_COND_CONQUER_CITY);
                            // 之前没有占领过这个城池
                            if (cityMap.getOrDefault(param, 0) == 0) {
                                canUpdate = true;
                            }
                        }
                    }
                    break;
                // 攻击boss指定次数
                case ScheduleConstant.GOAL_COND_ATTACK_BOSS:
                    int scheduleId = ssg.getScheduleId();
                    if (param == scheduleId) {
                        canUpdate = true;
                    }
                    break;
                case ScheduleConstant.GOAL_COND_FIGHT:
                case ScheduleConstant.GOAL_COND_ATK_GESTAPO_BANDIT:
                    canUpdate = true;
                    break;
                default:
                    break;
            }
        }
        return canUpdate;
    }

    /**
     * 判断这个玩家的目标是否有领奖
     *
     * @param player
     * @param goal
     * @return
     */
    public boolean alreadyReward(Player player, ScheduleGoal goal) {
        if (goal.getStatusMap().containsKey(player.roleId) && goal.getStatusMap().get(player.roleId) == ScheduleGoal.ALREADY_RECEIVED) {
            return true;
        }
        return false;
    }


    /**
     * 更新目标进度
     *
     * @param condId
     * @param schedule
     */
    public void updateCondStatus(int condId, int schedule) {
        if (condId > 0 && schedule > 0) {
            int oldSched = statusCnt.getOrDefault(condId, 0);
            if (schedule > oldSched) {
                this.statusCnt.put(condId, schedule);
            }
        }
    }

    /**
     * 设置世界进度状态
     */
    public void finishSchedule() {
        setStatus(ScheduleConstant.SCHEDULE_STATUS_FINISH);
        setFinishTime(TimeHelper.getCurrentSecond());
    }

    /**
     * 是否所有的限时目标都已完成
     *
     * @return
     */
    public boolean isAllGoalFinish() {
        if (CheckNull.isEmpty(goal)) {
            return false;
        }

        for (Map.Entry<Integer, ScheduleGoal> entry : goal.entrySet()) {
            StaticScheduleGoal staticScheduleGoal = StaticWorldDataMgr.getScheduleGoalById(entry.getKey());
            if (staticScheduleGoal == null) {
                LogUtil.error("判断目标进度, goal不存在, worldScheduleId:", this.id, ", goalId:", entry.getKey());
                return false;
            }

            int schedule = this.statusCnt.getOrDefault(staticScheduleGoal.getCond(), 0);
            if (schedule < staticScheduleGoal.getSchedule()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取goal的字符串
     *
     * @return
     */
    public String getGoalString() {
        StaticSchedule staticSchedule = StaticWorldDataMgr.getScheduleById(id);
        JSONArray result = new JSONArray();

        if (CheckNull.isNull(staticSchedule)) {
            return result.toString();
        }

        List<Integer> goalIdList = staticSchedule.getGoal();

        if (goalIdList == null || goalIdList.size() == 0) {
            return result.toString();
        }

        for (Integer goalId : goalIdList) {
            StaticScheduleGoal staticScheduleGoal = StaticWorldDataMgr.getScheduleGoalById(goalId);
            if (staticScheduleGoal == null) {
                continue;
            }

            JSONArray goalObj = new JSONArray();
            goalObj.add(goalId);
            goalObj.add(this.statusCnt.getOrDefault(staticScheduleGoal.getCond(), 0));

            result.add(goalObj);
        }

        return result.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<Integer, ScheduleGoal> getGoal() {
        return goal;
    }

    public void setGoal(Map<Integer, ScheduleGoal> goal) {
        this.goal = goal;
    }

    public ScheduleRank getRank() {
        return rank;
    }

    public void setRank(ScheduleRank rank) {
        this.rank = rank;
    }

    public void setBoss(ScheduleBoss boss) {
        this.boss = boss;
    }

    public List<Integer> getAttackCity() {
        return attackCity;
    }

    public void setAttackCity(List<Integer> attackCity) {
        this.attackCity = attackCity;
    }


    public Map<Integer, Integer> getStatusCnt() {
        return statusCnt;
    }

    public ScheduleBoss getBoss() {
        return boss;
    }
}
