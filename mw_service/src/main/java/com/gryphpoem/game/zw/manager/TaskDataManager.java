package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMentorDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTaskDataMgr;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb3.SyncDailyTaskRs;
import com.gryphpoem.game.zw.pb.GamePb3.SyncPartyFinishRs;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticPartyTask;
import com.gryphpoem.game.zw.resource.domain.s.StaticSectiontask;
import com.gryphpoem.game.zw.resource.domain.s.StaticTask;
import com.gryphpoem.game.zw.resource.pojo.FunCard;
import com.gryphpoem.game.zw.resource.pojo.SuperEquip;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.pojo.WarPlane;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务
 *
 * @author tyler
 */
@Component
public class TaskDataManager {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private TaskService taskService;
    @Autowired
    private CampDataManager campDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private CombatDataManager combatDataManager;
    @Autowired
    private TechDataManager techDataManager;

    @Autowired
    @Qualifier("rebelService")
    private BaseAwkwardDataManager rebelService;

    /**
     * 更新玩家进度,通过lordId
     *
     * @param lordId
     * @param cond
     * @param schedule
     * @param param
     */
    public void updTask(long lordId, int cond, int schedule, int... param) {
        Player player = playerDataManager.getPlayer(lordId);
        if (player != null) {
            updTask(player, cond, schedule, param);
        }
    }

    /**
     * 更新玩家任务
     *
     * @param player
     * @param cond     参见TaskType类
     * @param schedule 增加进度值
     * @param param
     */
    public void updTask(Player player, int cond, int schedule, int... param) {
        if (null == player) {
            return;
        }
        // activityDataManager.activityTaskUpdata(player, cond, schedule); //
        // 活动影响

        // 主线任务
        Iterator<Task> it1 = player.majorTasks.values().iterator();
        while (it1.hasNext()) {
            Task next = it1.next();
            StaticTask stask = StaticTaskDataMgr.getTaskById(next.getTaskId());
            if (stask == null || stask.getCond() != cond) {
                continue;
            }
            modifyTaskSchedule(player, next, stask, schedule, param);
        }

        // 日常任务
        List<Integer> chgDailyTaskId = null;
        for (StaticTask stask : StaticTaskDataMgr.getDayiyMap().values()) {
            if (stask.getCond() == cond) {
                Task task = player.getDailyTask().get(stask.getTaskId());
                if (task == null) {
                    task = new Task(stask.getTaskId());
                    player.getDailyTask().put(stask.getTaskId(), task);
                }
                if (task.getStatus() != TaskType.TYPE_STATUS_REWARD
                        && modifyTaskSchedule(player, task, stask, schedule, param)) {
                    if (chgDailyTaskId == null) {
                        chgDailyTaskId = new ArrayList<>();
                    }
                    chgDailyTaskId.add(stask.getTaskId());
                    // if (task.getSchedule() >= stask.getSchedule()) {
                    // task.setStatus(TaskType.TYPE_STATUS_REWARD);// 自动领奖
                    // int sch = stask.getAwardList().get(0).get(0);
                    // player.setDailyTaskLivenss(player.getDailyTaskLivenss() + sch); // 加上活跃度
                    // LogUtil.debug("roleId:", player.roleId, " 日常任务活跃度增加: taskId:", stask.getTaskId(), ",sch:", sch,
                    // ",Livenss", player.getDailyTaskLivenss());
                    // }
                }
            }
        }
        checkchgDailyTaskAndSync(player, chgDailyTaskId);

        // 军团任务
        if (!TimeHelper.isFriday()) {// 周五无军团任务
            int partyLv = campDataManager.getCampMember(player.lord.getLordId()).getTaskLv();
            Iterator<StaticPartyTask> it4 = StaticTaskDataMgr.getPartyTaskMap().values().iterator();
            while (it4.hasNext()) {
                StaticPartyTask next = it4.next();
                if (partyLv != next.getLv() || next.getCond() != cond) {
                    continue;
                }
                modifyTaskSchedule(player, next, schedule, param);
            }
            checkPartyTaskFinishAndNotice(player);
        }

        // 个人目标任务
        List<Task> syncAdvTasks = StaticTaskDataMgr.getAdvanceMap().values().stream()
                .filter(sTask -> sTask.getCond() == cond)
                .map(sTask -> player.getAdvanceTask().computeIfAbsent(sTask.getTaskId(), (t) -> new Task(sTask.getTaskId())))
                .filter(task -> {
                    StaticTask sTask = StaticTaskDataMgr.getTaskById(task.getTaskId());
                    if (Objects.isNull(sTask)) {
                        return false;
                    }
                    return task.getStatus() != TaskType.TYPE_STATUS_REWARD && modifyTaskSchedule(player, task, sTask, schedule, param);
                })
                .collect(Collectors.toList());
        syncAdvanceTask(player, syncAdvTasks);
    }

    /**
     * 同步个人目标任务
     *
     * @param player       玩家对象
     * @param syncAdvTasks 需要同步变动的个人目标进度
     */
    private void syncAdvanceTask(Player player, List<Task> syncAdvTasks) {
        if (!CheckNull.isNull(player) && !CheckNull.isEmpty(syncAdvTasks)) {
            GamePb3.SyncAdvanceTaskRs.Builder builder = GamePb3.SyncAdvanceTaskRs.newBuilder().addAllAdvanceTask(syncAdvTasks.stream().map(PbHelper::createTaskPb).collect(Collectors.toList()));
            Base.Builder msg = PbHelper.createSynBase(GamePb3.SyncAdvanceTaskRs.EXT_FIELD_NUMBER, GamePb3.SyncAdvanceTaskRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 检测日常任务进度是否发生改变,并推送
     *
     * @param chgDailyTaskId
     */
    private void checkchgDailyTaskAndSync(Player player, List<Integer> chgDailyTaskId) {
        if (!CheckNull.isEmpty(chgDailyTaskId) && player.isLogin && player.ctx != null) {
            SyncDailyTaskRs.Builder builder = SyncDailyTaskRs.newBuilder();
            for (int taskId : chgDailyTaskId) {
                Task task = player.getDailyTask().get(taskId);
                if (task != null) {
                    builder.addDailyTask(PbHelper.createTaskPb(task));
                }
            }
            builder.setDailyTaskLivenss(player.getDailyTaskLivenss());
            Base.Builder msg = PbHelper.createSynBase(SyncDailyTaskRs.EXT_FIELD_NUMBER, SyncDailyTaskRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 检测军任务是否完成
     *
     * @param player
     */
    private void checkPartyTaskFinishAndNotice(Player player) {
        List<Integer> taskIds = new ArrayList<>();
        for (Task task : player.partyTask.values()) {
            StaticPartyTask stask = StaticTaskDataMgr.getPartyTask(task.getTaskId());
            if (stask != null) {
                currentTask(player, task, stask.getCond(), stask.getCond(), stask.getSchedule());
                if (task.getSchedule() >= stask.getSchedule() && task.getStatus() == 0) {
                    task.setStatus(TaskType.TYPE_STATUS_FINISH);
                }
                if (task.getStatus() == TaskType.TYPE_STATUS_FINISH) {// 完成发送通知
                    taskIds.add(stask.getId());
                }
            }
        }
        if (!CheckNull.isEmpty(taskIds)) {
            syncPartyFinish(player, taskIds);
        }
    }

    /**
     * 推送军团任务领取奖励
     *
     * @param taskIds
     */
    public void syncPartyFinish(Player player, Collection<Integer> taskIds) {
        if (player != null && player.ctx != null && player.isLogin) {
            SyncPartyFinishRs.Builder builder = SyncPartyFinishRs.newBuilder();
            builder.addAllTaskId(taskIds);
            Base.Builder msg = PbHelper.createSynBase(SyncPartyFinishRs.EXT_FIELD_NUMBER, SyncPartyFinishRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 任务按类型单独处理,便于后期修改维护
     *
     * @param task
     * @param stask
     * @param schedule
     * @param param
     * @return
     */
    public boolean modifyTaskSchedule(Player player, Task task, StaticTask stask, int schedule, int... param) {
        return modifyTaskSchedule(player, task, stask.getCond(), stask.getCondId(), stask.getSchedule(), schedule,
                param);
    }

    /**
     * 军团任务的处理
     *
     * @param player
     * @param stask
     * @param schedule
     * @param param
     * @return
     */
    private boolean modifyTaskSchedule(Player player, StaticPartyTask stask, int schedule, int... param) {
        Task task = player.partyTask.get(stask.getId());
        if (task == null) {
            task = new Task(stask.getId());
            player.partyTask.put(stask.getId(), task);
        }
        return modifyTaskSchedule(player, task, stask.getCond(), stask.getCondId(), stask.getSchedule(), schedule,
                param);
    }

    public boolean modifyTaskSchedule(Player player, Task task, int sCond, int sCondId, int sSchedule, int schedule,
                                      int... param) {
        if (task == null) {
            return false;
        }
        if (task.getSchedule() >= sSchedule) {
            return false;
        }
        switch (sCond) {
            case TaskType.COND_18: {// 开始升级某个建筑,到多少级
                int paramId = param.length > 0 ? param[0] : 0;
                if (sCondId == 0 || sCondId == paramId) {
                    // 获取该建筑当前等级
                    int lv = BuildingDataManager.getBuildingTopLv(player, paramId);
                    task.setSchedule(lv + schedule);
                    return true;
                }
                break;
            }
            // 单个条件
            case TaskType.COND_OTHER_TASK_CNT:
            case TaskType.COND_19:
            case TaskType.COND_BATTLE_CITY_LV_CNT:
            case TaskType.COND_BATTLE_STATE_LV_CNT:
            case TaskType.COND_BUILDING_TYPE_LV:
            case TaskType.COND_COMBAT_ID_WIN:
            case TaskType.COND_BANDIT_LV_CNT:
            case TaskType.COND_ARM_TYPE_CNT:
            case TaskType.COND_FREE_CD:
            case TaskType.COND_RES_AWARD:
            case TaskType.COND_EQUIP_BUILD:
            case TaskType.COND_HERO_UP:
            case TaskType.COND_EQUIP_SPEED:
            case TaskType.COND_EQUIP_BAPTIZE:
            case TaskType.COND_CHEMICAL:
            case TaskType.COND_26:
            case TaskType.COND_27:
            case TaskType.COND_28:
            case TaskType.COND_29:
            case TaskType.COND_22:
            case TaskType.COND_31:
            case TaskType.COND_33:
            case TaskType.COND_ENTER_COMBAT_34:
            case TaskType.COND_BUILDING_ID_LV:
            case TaskType.COND_ARM_CNT:
            case TaskType.COND_TREASURE:
            case TaskType.COND_COMBAT_37:
            case TaskType.COND_CAMP_BUILD_38:
            case TaskType.COND_BUY_ACT_40:
            case TaskType.COND_JOIN_CAMP_BATTLE_41:
            case TaskType.COND_WASH_HERO_42:
            case TaskType.COND_BUILDING_UP_43:
            case TaskType.COND_TECH_UP_44:
            case TaskType.COND_PAY_46:
            case TaskType.COND_STONE_COMBAT_47:
            case TaskType.COND_STONE_COMPOUND_COMBAT_48:
            case TaskType.COND_HERO_EQUIPID:
            case TaskType.COND_PITCHCOMBAT_PASS_CNT:
            case TaskType.COND_SEARCH_PLANE_HIGH:
            case TaskType.COND_SEARCH_PLANE_LOW:
            case TaskType.COND_MENTOR_UPLV_CNT:
            case TaskType.COND_SCORPION_ACTIVATE_PREPOSITION:
            case TaskType.COND_SCORPION_ACTIVATE:
            case TaskType.COND_UNLOCK_AGENT:
            case TaskType.COND_APPOINTMENT_AGENT:
            case TaskType.COND_INTERACTION_AGENT:
            case TaskType.COND_PRESENT_GIFT_AGENT:
            case TaskType.COND_DESIGNATED_HERO_ID_UPGRADE:
            case TaskType.COND_FISHING_MASTER://钓到一个鱼
            case TaskType.COND_TREASURE_WARE_MAKE_COUNT://打造宝具
            case TaskType.COND_GET_TREASURE_WARE_HOOK_AWARD://领取挂机奖励
                int paramId = param.length > 0 ? param[0] : 0;
                if (sCondId == 0 || sCondId == paramId) {
                    task.setSchedule(task.getSchedule() + schedule);
                    return true;
                }
                break;
            case TaskType.COND_DESIGNATED_HERO_QUALITY_UPGRADE:
                paramId = param.length > 0 ? param[0] : 0;
                if ((sCondId == 0 || sCondId <= paramId) && schedule > task.getSchedule()) {
                    task.setSchedule(schedule);
                    return true;
                }
                break;
            case TaskType.COND_ATTCK_PLAYER_CNT:
            case TaskType.COND_30:
            case TaskType.COND_FACTORY_RECRUIT:
            case TaskType.COND_COMMAND_ADD:// 大于或等于所需条件
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId == 0 || sCondId <= paramId) {
                    task.setSchedule(task.getSchedule() + schedule);
                    return true;
                }
                break;
            case TaskType.COND_EQUIP: // 指定将领的指定部位穿装备(双重条件)
                paramId = param.length > 0 ? param[0] : 0;
                int param2 = param.length > 1 ? param[1] : 0;
                if ((sCondId == 0 || sCondId == paramId) && param2 == sSchedule) {
                    task.setSchedule(sSchedule);
                    return true;
                }
                break;
            case TaskType.COND_MONTH_CARD_STATE_45:// 检测是否在月卡状态
                //检测功能卡任意存在，就完成每日任务
                int today = TimeHelper.getCurrentDay();
                for (FunCard fc : player.funCards.values()) {
                    if (today == fc.getLastTime() && task.getSchedule() < 1) {
                        task.setSchedule(1);
                        return true;
                    }
                }

                break;
            case TaskType.COND_LOGIN_36: // 每日登陆
                if (task.getSchedule() < 1) {
                    task.setSchedule(1);
                    return true;
                }
                break;
            case TaskType.COND_STONE_HOLE_49:// 宝石镶嵌个数
                int cnt = player.getStoneInfo().getStoneHoles().size();
                if (task.getSchedule() < cnt) {
                    task.setSchedule(cnt);
                    return true;
                }
                break;
            // case TaskType.COND_SUPER_EQUIP:// 超级武器等级
            // paramId = param.length > 0 ? param[0] : 0;
            // if (sCondId == 0 || sCondId == paramId) {
            // task.setSchedule(schedule > task.getSchedule() ? schedule : task.getSchedule());
            // return true;
            // }
            // break;
            default:
                break;
        }
        return false;
    }

    /**
     * 主线任务进度检测并且更新
     *
     * @param player
     * @param task
     * @param stask
     * @return
     */
    public Task currentMajorTask(Player player, Task task, StaticTask stask) {
        if (task.getStatus() == TaskType.TYPE_STATUS_FINISH) {
            return task;
        }
        return currentTask(player, task, stask.getCond(), stask.getCondId(), stask.getSchedule());
    }

    /**
     * 任务的进度检测和状态修改
     *
     * @param player
     * @param task
     * @param cond
     * @param condId
     * @param sSchedule
     * @return
     */
    public Task currentTask(Player player, Task task, int cond, int condId, int sSchedule) {
        if (task == null) {
            return task;
        }
        if (task.getStatus() == TaskType.TYPE_STATUS_FINISH) {
            return task;
        }
        long schedule = 0;
        switch (cond) {
            case TaskType.COND_LORD_LV:
                schedule = player.lord.getLevel();
                break;
            // case TaskType.COND_CLICK:
            // schedule = 1;
            // break;
            // case TaskType.COND_20:
            // case TaskType.COND_21:
            // case TaskType.COND_24:
            // case TaskType.COND_25:
            // case TaskType.COND_FIGHT_SHOW:// 直接完成
            // schedule = 1;
            // break;
            case TaskType.COND_TECH_LV: {
                schedule = techDataManager.getTechLv(player, condId);
                break;
            }
            // case TaskType.COND_OTHER_TASK_CNT: {
            //     Iterator<Task> it1 = player.majorTasks.values().iterator();
            //     while (it1.hasNext()) {
            //         Task next = it1.next();
            //         StaticTask stask = StaticTaskDataMgr.getTaskById(next.getTaskId());
            //         if (stask != null && stask.getType() == TaskType.TYPE_SUB
            //                 && next.getStatus() == TaskType.TYPE_STATUS_REWARD) {
            //             schedule++;
            //         }
            //     }
            //     break;
            // }
            case TaskType.COND_18: {
                schedule = BuildingDataManager.getBuildingTopLv(player, condId);
                // 判断该建筑是否在升级
                Map<Integer, BuildQue> buildQueMap = player.buildQue;
                for (BuildQue bq : buildQueMap.values()) {
                    if (bq.getBuildingType() == condId) {
                        schedule += 1;
                        break;
                    }
                }
                break;
            }
            case TaskType.COND_BUILDING_TYPE_LV: {
                schedule = BuildingDataManager.getBuildingTopLv(player, condId);
                break;
            }
            case TaskType.COND_BUILDING_ID_LV: {
                schedule = BuildingDataManager.getBuildingLv(condId, player);
                break;
            }
            case TaskType.COND_COMBAT_ID_WIN: {
                schedule = combatDataManager.getCombatId(player, condId);
                break;
            }
            case TaskType.COND_RES_FOOD_CNT: {
                schedule = buildingDataManager.getBuilding4LvCnt(player, BuildingType.RES_FOOD, condId);
                break;
            }
            case TaskType.COND_RES_OIL_CNT: {
                schedule = buildingDataManager.getBuilding4LvCnt(player, BuildingType.RES_OIL, condId);
                break;
            }
            case TaskType.COND_RES_ELE_CNT: {
                schedule = buildingDataManager.getBuilding4LvCnt(player, BuildingType.RES_ELE, condId);
                break;
            }
            case TaskType.COND_RES_ORE_CNT: {
                schedule = buildingDataManager.getBuilding4LvCnt(player, BuildingType.RES_ORE, condId);
                break;
            }
            case TaskType.COND_26: {
                schedule = player.heros.containsKey(condId) ? 1 : 0;
                break;
            }
            // case TaskType.COND_ARM_TYPE_CNT: {
            // schedule = armyService.getArmCount(player.resource, condId);
            // break;
            // }
            case TaskType.COND_32: {
                Iterator<Hero> it1 = player.heros.values().iterator();
                while (it1.hasNext()) {
                    Hero next = it1.next();
                    if (next.getEquip().length >= condId && next.getEquip()[condId] > 0) {
                        schedule++;
                    }
                }
                break;
            }
            case TaskType.COND_SUPER_EQUIP:
                SuperEquip superEquip = player.supEquips.get(condId);
                if (superEquip != null) {
                    schedule = superEquip.getLv();
                }
                break;
            case TaskType.COND_STONE_COMBAT_47: // 宝石副本挑战
                StoneCombat sc = player.stoneCombats.get(condId);
                if (sc != null) schedule = sc.getPassCnt();
                break;
            case TaskType.COND_STONE_HOLE_49:// 宝石镶嵌个数
                schedule = player.getStoneInfo().getStoneHoles().size();
                break;
            case TaskType.COND_PITCHCOMBAT_PASS: {// 通关指定ID的荣耀演习场关卡,过关了也要算
                PitchCombat pitchCombat = player.getPitchCombat(condId);
                if (pitchCombat != null) {
                    int highestCombatId = pitchCombat.getHighestCombatId();
                    task.setSchedule(highestCombatId);
                    if (highestCombatId >= sSchedule) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);// 直接设置已完成
                    }
                    return task;
                }
                break;
            }
            case TaskType.COND_MENTOR_EQUIP_GEARORDER: {// 装备指定gearorder的教官装备,已装备也算
                MentorInfo mentorInfo = player.getMentorInfo();
                MentorEquip me = mentorInfo.getEquipMap().values().stream()
                        .filter(e -> e.getType() == condId && e.getMentorId() > 0
                                && StaticMentorDataMgr.getsMentorEquipIdMap(e.getEquipId()).getGearOrder() == sSchedule)
                        .findFirst().orElse(null);
                if (me != null) {
                    task.setSchedule(sSchedule);
                    task.setStatus(TaskType.TYPE_STATUS_FINISH);
                    return task;
                }
                break;
            }
            case TaskType.COND_MENTOR_SKILL_UPLV: {// 升级指定教官技能至XX级,已经升级也算
                MentorInfo mentorInfo = player.getMentorInfo();
                MentorSkill mentorSkill = mentorInfo.getSkillMap().get(condId);
                schedule = mentorSkill == null ? 0 : mentorSkill.getLv();
                break;
            }
            case TaskType.COND_MENTOR_UPLV: {// 升级教官等级至XX级
                MentorInfo mentorInfo = player.getMentorInfo();
                Mentor mentor = mentorInfo.getMentors().get(condId);
                schedule = mentor == null ? 0 : mentor.getLv();
                break;
            }
            case TaskType.COND_FITTING_PLANE_ID: {
                for (WarPlane wp : player.warPlanes.values()) {
                    if (wp.getHeroId() > 0 && condId == wp.getType()) {
                        schedule++;
                    }
                }
                break;
            }
            case TaskType.COND_DESIGNATED_HERO_ID_UPGRADE: {
                Hero h = player.heros.values().stream().filter(hero -> hero.getHeroId() == condId).findFirst().orElse(null);
                if (Objects.nonNull(h)) {
                    schedule = h.getLevel();
                }
                break;
            }
            case TaskType.COND_DESIGNATED_HERO_QUALITY_UPGRADE: {
                schedule = player.heros.values()
                        .stream()
                        .filter(hero -> {
                            StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                            return Objects.nonNull(sHero) && sHero.getQuality() >= condId;
                        })
                        .mapToInt(Hero::getLevel)
                        .max()
                        .orElse(0);
                break;
            }
            default:
                break;
        }
        if (schedule > task.getSchedule()) {
            task.setSchedule(schedule);
        }
        if (schedule >= sSchedule && task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {// 只能从未完成->完成
            task.setStatus(TaskType.TYPE_STATUS_FINISH);

        }
        return task;
    }

    /**
     * 重置日常任务
     *
     * @param player
     * @return
     */
    public void refreshDailyTask(Player player) {
        player.getDailyIsGet().clear();
        player.getDailyTask().clear();
        player.setDailyTaskLivenss(0);
    }

    /*
    public void refreshTask(Player player, int type) {
        if (type == 1) {// 重置日常任务和活跃任务
            List<Integer> staskList = StaticTaskDataMgr.getRadomDayiyTask();
            List<Task> dayiyList = player.dailyTask;
            if (dayiyList.size() == 0) {
                for (Integer taskId : staskList) {
                    Task task = new Task(taskId);
                    dayiyList.add(task);
                }
            } else {
                int i = 0;
                Iterator<Task> it = dayiyList.iterator();
                while (it.hasNext()) {
                    Task next = it.next();
                    int taskId = staskList.get(i++);
                    next.setTaskId(taskId);
                    next.setSchedule(0);
                    next.setAccept(0);
                    next.setStatus(0);
                }
            }
        } else if (type == 2) {// 日常活跃任务
            Map<Integer, Task> liveTaskMap = player.partyTask;
            if (liveTaskMap.size() == 0) {
                List<StaticTask> liveList = StaticTaskDataMgr.getLiveList();
                for (StaticTask ee : liveList) {
                    Task task = new Task(ee.getTaskId());
                    liveTaskMap.put(ee.getTaskId(), task);
                }
            } else {
                Iterator<Task> it = liveTaskMap.values().iterator();
                while (it.hasNext()) {
                    Task task = it.next();
                    task.setSchedule(0);
                }
            }
        }
    }
    */

    /**
     * 累计世界任务中个人的流寇部分
     *
     * @param lordId
     * @param taskType
     * @param schedule
     * @param param
     */
    public void updWorldTaskSelf(long lordId, int taskType, int schedule, int... param) {
        Player player = playerDataManager.getPlayer(lordId);
        if (player != null) {
            Task task = player.worldTasks.get(taskType);
            if (task == null) {
                task = new Task(1, 0, TaskType.TYPE_STATUS_UNFINISH, 1);
                player.worldTasks.put(1, task);
            }
            task.setSchedule(task.getSchedule() + schedule);
        }
    }

    /**
     * 刷新军团任务
     *
     * @param player
     */
    public void refreshPartyTask(Player player) {
        CampMember p = campDataManager.getCampMember(player.lord.getLordId());
        Camp camp = campDataManager.getParty(player.lord.getCamp());
        clearPartyTask(player, p, camp);
    }

    private void clearPartyTask(Player player, CampMember p, Camp camp) {
        player.partyTask.clear();
        p.setTaskTime(TimeHelper.getCurrentSecond());
        p.setTaskAwardCnt(0);
        p.setTaskLv(camp.getPartyLv());
        // LogUtil.debug(player.lord.getLordId() + ",刷新军团任务等级重置为" + camp.getPartyLv());
    }

    /**
     * 更新世界任务完成进度
     *
     * @param taskType
     * @param schedule
     * @param param
     */
    // @Deprecated
    // public void updWorldTask(int taskType, int schedule, int... param) {
    // WorldTask wTask = worldDataManager.getWorldTask();
    // int curWorldTaskId = wTask.getWorldTaskId().get();
    // StaticWorldTask sCurWorldTask = StaticWorldDataMgr.getWorldTask(curWorldTaskId);
    // // 检查世界任务是否完成(如果当前完成的是打城市和boss，则进入下个任务)
    // LogUtil.debug("updWorldTask城战推送type=" + curWorldTaskId + ",任务id=" + wTask.getWorldTaskId().get() + ",schedule="
    // + schedule + ",taskType=" + taskType + ",statiType=" + sCurWorldTask.getTaskType() + ",param="
    // + param.length);
    // // if (sCurWorldTask.getTaskType() != taskType) {
    // // return;
    // // }
    // if (wTask.getWorldTaskMap().isEmpty()) {
    // LogUtil.debug("updWorldTask isEmpty");
    // wTask.getWorldTaskMap().put(WorldConstant.INIT_WORLD_TASKID,
    // CommonPb.WorldTask.newBuilder().setTaskId(WorldConstant.INIT_WORLD_TASKID).build());
    // }
    // boolean push = false;
    // CommonPb.WorldTask pushTask = null;
    //
    // // 如果是打城池，则开启下个世界任务进度
    // if (taskType == TaskType.WORLD_TASK_TYPE_CITY) {
    // if (param.length < 2) {
    // return;
    // }
    // int cityType = param[0];
    // int camp = param[1];
    // StaticWorldTask swt = StaticWorldDataMgr.getWorldTaskMap().values()
    // .stream().filter(wt -> wt.getTaskType() == TaskType.WORLD_TASK_TYPE_CITY
    // && !CheckNull.isEmpty(wt.getParam()) && wt.getParam().get(0) == cityType)
    // .findFirst().orElse(null);
    // if (swt == null) {
    // return;
    // }
    // if (swt.getTaskId() < curWorldTaskId) { // 已经完成的任务
    // return;
    // } else if (swt.getTaskId() > curWorldTaskId) { // 未来的任务
    // /* ------------------记录完成未开启的世界任务------------------*/
    // if (swt != null && swt.getTaskId() > curWorldTaskId
    // && !wTask.getWorldTaskMap().containsKey(swt.getTaskId())) { // 完成未来的世界任务
    // pushTask = CommonPb.WorldTask.newBuilder().setTaskId(swt.getTaskId()).setTaskCnt(1).setCamp(camp)
    // .build();
    // wTask.getWorldTaskMap().put(swt.getTaskId(), pushTask);
    // curWorldTaskId = swt.getTaskId();
    // push = true;
    // }
    //
    // } else {// 当前的任务
    // /* ------------------是当前任务完成的是当前任务------------------*/
    // CommonPb.WorldTask worldTask = wTask.getWorldTaskMap().get(curWorldTaskId);
    // if (worldTask == null || worldTask.getTaskCnt() <= 0) {
    // pushTask = CommonPb.WorldTask.newBuilder().setTaskId(curWorldTaskId).setTaskCnt(1).setCamp(camp)
    // .build();
    // wTask.getWorldTaskMap().put(curWorldTaskId, pushTask);
    // push = true;
    // }
    // onWroldTaskComplete(wTask.getWorldTaskId().get());
    // wTask.getWorldTaskId().incrementAndGet();// 进度+1
    // }
    // StaticWorldTask nextWorldTask = StaticWorldDataMgr.getWorldTask(curWorldTaskId + 1);
    // if (nextWorldTask != null && !wTask.getWorldTaskMap().containsKey(nextWorldTask.getTaskId())) {
    // if (nextWorldTask.getTaskType() == TaskType.WORLD_TASK_TYPE_BOSS) {
    // wTask.getWorldTaskMap().put(nextWorldTask.getTaskId(), CommonPb.WorldTask.newBuilder()
    // .setTaskId(nextWorldTask.getTaskId()).setHp(nextWorldTask.getHp()).build());
    // wTask.setDefender(null);
    // } else {
    // wTask.getWorldTaskMap().put(nextWorldTask.getTaskId(),
    // CommonPb.WorldTask.newBuilder().setTaskId(nextWorldTask.getTaskId()).setTaskCnt(0).build());
    // }
    // }
    // } else if (taskType == TaskType.WORLD_TASK_TYPE_BOSS) {
    // // 如果是打世界boss，没打死则扣血，打死则进入下个世界任务进度
    // if (schedule <= 0) {
    // return;
    // }
    // // 扣血
    // CommonPb.WorldTask worldTask = wTask.getWorldTaskMap().get(curWorldTaskId);
    // pushTask = CommonPb.WorldTask.newBuilder().setTaskId(curWorldTaskId)
    // .setTaskCnt(worldTask.getHp() <= schedule ? 1 : 0).setHp(worldTask.getHp() - schedule).build();
    // push = true;
    // LogUtil.debug("updWorldTask世界boss当前血=" + worldTask.getHp() + ",扣血=" + schedule);
    // // 打死了,开启下个任务
    // if (worldTask.getHp() <= schedule) {
    // StaticWorldTask nextWorldTask = StaticWorldDataMgr.getWorldTask(curWorldTaskId + 1);
    // if (nextWorldTask != null) {
    // // 新开启
    // onWroldTaskComplete(wTask.getWorldTaskId().get());
    // int newWorldTaskId = wTask.getWorldTaskId().incrementAndGet();
    // wTask.getWorldTaskMap().put(newWorldTaskId, CommonPb.WorldTask.newBuilder()
    // .setTaskId(newWorldTaskId).setHp(nextWorldTask.getHp()).build());
    // }
    // schedule = 1;
    // }
    // // 更新世界任务内存值
    // wTask.getWorldTaskMap().put(curWorldTaskId, pushTask);
    // LogUtil.debug("---updWorldTask世界boss当前血=" + worldTask.getHp() + ",扣血=" + schedule);
    // }
    //
    // /*------------------------------进度更新推送-----------------------------*/
    // LogUtil.debug("updWorldTaspush=" + push);
    // if (push) {
    // SynWorldTaskRs.Builder syncwtBuilder = SynWorldTaskRs.newBuilder();
    // CommonPb.WorldTask.Builder wtBuilder = CommonPb.WorldTask.newBuilder();
    // wtBuilder.setTaskId(curWorldTaskId);
    // wtBuilder.setTaskCnt(schedule);
    // wtBuilder.setCamp(pushTask.getCamp());
    // wtBuilder.setHp(pushTask.getHp());
    // syncwtBuilder.setWorldTask(wtBuilder);
    //
    // Base.Builder msg = PbHelper.createSynBase(SynWorldTaskRs.EXT_FIELD_NUMBER, SynWorldTaskRs.ext,
    // syncwtBuilder.build());
    // Player player;
    // Iterator<Player> it = playerDataManager.getAllOnlinePlayer().values().iterator();
    // while (it.hasNext()) {
    // player = it.next();
    // if (player.ctx != null) {
    // MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    // }
    // }
    // }
    // }

    /**
     * 世界任务完成条件触发
     *
     * @param worldTaskId
     */
    public void onWroldTaskComplete(int worldTaskId) {
        if (worldTaskId == Constant.REBEL_WORLD_TASKID_COND) {
            // 匪军叛乱的触发条件
            rebelService.initRebellion();
        }
    }

    /**
     * 触发剧情
     *
     * @param player
     */
    public void triggerSectiontask(Player player) {
        if (player.sectiontask.isEmpty()) {// 没有章节才会触发
            StaticSectiontask firstSSection = StaticTaskDataMgr.getSectiontaskList().get(0);
            Sectiontask fSection = new Sectiontask(firstSSection.getSectionId(), TaskType.TYPE_STATUS_UNFINISH);
            player.sectiontask.add(fSection);
            // 任务
            List<Integer> newTasks = firstSSection.getSectionTask();
            // 触发章节任务
            for (Integer taskId : newTasks) {
                if (StaticTaskDataMgr.getTaskById(taskId) != null) {
                    if (!player.majorTasks.containsKey(taskId)) {
                        // 给资源+1次征收
                        taskService.processTask(player, taskId);
                        Task task = new Task(taskId);
                        player.majorTasks.put(taskId, task);
                    }
                }
            }
            LogUtil.debug("触发了首个剧情章节  roleId:", player.roleId, " firstSectionId:", firstSSection.getSectionId());
        }
    }


    /**
     * 触发第十一章剧情任务
     *
     * @param player 玩家对象
     */
    public void triggerSectiontask2(Player player) {
        // 查最后一个剧情任务是不是第九章
        Sectiontask sectiontask = player.sectiontask.stream().sorted(Comparator.comparingInt(Sectiontask::getSectionId).reversed()).findFirst().orElse(null);
        final int sectionId = Objects.requireNonNull(sectiontask).getSectionId();
        // 如果剧情任务id为9
        if (sectionId == Constant.SECTION_ID_9) {
            StaticSectiontask sectionTask = StaticTaskDataMgr.getSectiontaskById(Constant.SECTION_ID_11);
            Sectiontask section = new Sectiontask(sectionTask.getSectionId(), TaskType.TYPE_STATUS_UNFINISH);
            player.sectiontask.add(section);
            // 任务
            List<Integer> newTasks = sectionTask.getSectionTask();
            // 触发章节任务
            for (Integer taskId : newTasks) {
                if (StaticTaskDataMgr.getTaskById(taskId) != null) {
                    if (!player.majorTasks.containsKey(taskId)) {
                        // 给资源+1次征收
                        taskService.processTask(player, taskId);
                        Task task = new Task(taskId);
                        player.majorTasks.put(taskId, task);
                    }
                }
            }
            LogUtil.debug("触发了第二个剧情章节  roleId:", player.roleId, " firstSectionId:", sectionTask.getSectionId());
        }
    }


    /**
     * 是否开启了世界任务
     *
     * @param player
     * @return true 开启
     */
    public boolean isOpenWorldTask(Player player) {
        if (player.worldTasks.size() >= 2) { // 兼容用gm命令开启的世界任务,
            return true;
        }
        Task task = player.majorTasks.get(WorldConstant.WORLD_TASK_OPEN_TASK_ID);
        return task != null && task.getStatus() == TaskType.TYPE_STATUS_REWARD;
    }

    // =============================计算当前需要显示的任务==========================
    // 获取当前任务的列表
    public List<Integer> getCurTask(Player player) {
        List<Integer> curTask = new ArrayList<>();

        // 1. 找到当前正在进行主线任务
        Map<Integer, Task> majorTasks = player.majorTasks;
        List<Task> majorTaskList = new ArrayList<>();
        majorTaskList.addAll(majorTasks.values());
        // 获取排序最小的未完成的主线任务
        StaticTask curCfgMaintask = majorTaskList.stream().filter(t -> t.getStatus() != TaskType.TYPE_STATUS_REWARD)// 获取未领取的主线任务
                .map(t -> StaticTaskDataMgr.getMajorMap().get(t.getTaskId()))
                .filter(staticTask -> staticTask != null && staticTask.getType() == TaskType.TYPE_MAIN)
                .min(Comparator.comparing(StaticTask::getMainSort)).orElse(null); // 找到主线任务排序最小

        int curMainTaskId = 0;// 当前主线任务的id
        if (curCfgMaintask == null) {// 没有主线任务说明,可能所有的主线任务已经完成
            // 找到自己当前完成的最后一个主线任务
            StaticTask myLastMainTask = majorTaskList.stream().filter(t -> t.getStatus() == TaskType.TYPE_STATUS_REWARD)// 已经完成
                    .map(t -> StaticTaskDataMgr.getMajorMap().get(t.getTaskId()))
                    .filter(staticTask -> staticTask != null && staticTask.getType() == TaskType.TYPE_MAIN) // 主线任务
                    .max(Comparator.comparing(StaticTask::getMainSort)).orElse(null);
            if (myLastMainTask != null) {
                curMainTaskId = myLastMainTask.getTaskId();
            }
            // 目前任务配置表中最后一个任务id
            StaticTask staticTask = StaticTaskDataMgr.getLastMainTask();

            if (myLastMainTask != null && staticTask != null) {
                if (staticTask.getTaskId() == myLastMainTask.getTaskId()) {
                    LogUtil.debug("roleId:", player.roleId, ", taskId: 主线任务全部完成");
                } else {
                    // 尝试触发配表新增的主线任务
                    List<StaticTask> newTaskList = StaticTaskDataMgr.getTriggerTask(myLastMainTask.getTaskId());
                    if (!CheckNull.isEmpty(newTaskList)) {
                        for (StaticTask st : newTaskList) {
                            if (st.getType() == TaskType.TYPE_MAIN && !player.majorTasks.containsKey(st.getTaskId())) {
                                // 触发新的主线
                                Task t = new Task(st.getTaskId());
                                player.majorTasks.put(t.getTaskId(), t);
                                curMainTaskId = t.getTaskId();
                                curTask.add(curMainTaskId);// 添加主线列表中
                                LogUtil.debug("roleId:", player.roleId, ", 触发新的主线任务:", ", taskId:", st.getTaskId());
                            }
                        }
                    }
                }
            }
        } else {
            LogUtil.debug("roleId: ", player.roleId, ", curMaintaskId: ", curCfgMaintask.getTaskId());
            curMainTaskId = curCfgMaintask.getTaskId();
            curTask.add(curMainTaskId);// 添加主线
        }
        // 2. 找当前正在进行支线任务
        final int mainTaskId = curMainTaskId;
        // 获取已经完成主线任务
        List<StaticTask> mainSTasks = StaticTaskDataMgr.getAllTask().stream()
                .filter(st -> st.getType() == TaskType.TYPE_MAIN && st.getTaskId() <= mainTaskId)
                .sorted((t1, t2) -> t1.getTaskId() - t2.getTaskId()) // 从小到大排序
                .collect(Collectors.toList());

        List<StaticTask> curTriggerTasks = new ArrayList<>(); // 已经触发的任务
        for (StaticTask st : mainSTasks) {// 找每个主线 已经触发的支线任务
            if (getTaskStatusByPlayer(player, st) == TaskType.TYPE_STATUS_REWARD) {// 主线领取才能触发
                curTriggerTasks.addAll(findSubTask(player, st));
            }
        }
        // 每种类型只能有一种
        Set<Integer> subTypes = new HashSet<>();
        for (StaticTask cst : curTriggerTasks) {
            int type = cst.getSubType();
            if (!subTypes.contains(type)) {
                subTypes.add(type);
                curTask.add(cst.getTaskId());
            }
        }
        // 打印
        curTask.forEach(
                t -> LogUtil.debug("roleId:", player.roleId, " ,", StaticTaskDataMgr.getMajorMap().get(t).getTaskId()
                        + " " + StaticTaskDataMgr.getMajorMap().get(t).getDesc()));

        return curTask;
    }

    /**
     * 更具主线获取支线
     */
    private List<StaticTask> findSubTask(Player player, StaticTask staticTask) {
        List<StaticTask> subTask = new ArrayList<>();
        List<StaticTask> triggerList = StaticTaskDataMgr.getTriggerTask(staticTask.getTaskId());
        if (!CheckNull.isEmpty(triggerList)) {
            // 有可触发的任务
            for (StaticTask t : triggerList) {
                if (t.getType() == TaskType.TYPE_SUB) {
                    if (getTaskStatusByPlayer(player, t) == TaskType.TYPE_STATUS_REWARD) {
                        subTask.addAll(findSubTask(player, t));
                    } else {
                        subTask.add(t);
                    }
                }
            }
        }
        return subTask;
    }

    /**
     * 获取某个任务的状态
     */
    public int getTaskStatusByPlayer(Player player, StaticTask t) {
        Task task = player.majorTasks.get(t.getTaskId());
        if (task != null) {
            return task.getStatus();
        } else {
            return TaskType.TYPE_STATUS_UNFINISH;
        }
    }

    // =============================计算当前需要显示的任务==========================
}
