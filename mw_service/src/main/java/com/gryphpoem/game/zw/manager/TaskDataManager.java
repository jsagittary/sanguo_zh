package com.gryphpoem.game.zw.manager;

import com.google.common.collect.Lists;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMentorDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTaskDataMgr;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb3.SyncDailyTaskRs;
import com.gryphpoem.game.zw.pb.GamePb3.SyncPartyFinishRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.task.CommandMultType;
import com.gryphpoem.game.zw.resource.constant.task.TaskCone517Type;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.*;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.gryphpoem.game.zw.service.FactoryService;
import com.gryphpoem.game.zw.service.HeroService;
import com.gryphpoem.game.zw.service.StoneService;
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
    @Autowired
    private HeroService heroService;
    @Autowired
    private StoneService stoneService;
    @Autowired
    private ChapterTaskDataManager chapterTaskDataManager;
    @Autowired
    private FactoryService factoryService;


    /**
     * 更新玩家任务
     *
     * @param player
     * @param cond     参见TaskType类
     * @param schedule 增加进度值
     * @param param
     */
    public void updTask(Player player, int cond, int schedule, int... param) {
        if (null == player || cond <= 0 || schedule <= 0) {
            return;
        }
        try {
            //章节任务
            chapterTaskDataManager.updTask(player, cond, schedule, param);
        } catch (Exception e) {
            LogUtil.error("章节任务更新错误", e.toString());
        }

        // 日常任务
        List<Integer> chgDailyTaskId = null;
        for (StaticTask stask : StaticTaskDataMgr.getDayiyMap().values()) {
            if (stask.getCond() == cond) {
                Task task = player.getDailyTask().computeIfAbsent(stask.getTaskId(), x -> new Task(stask.getTaskId()));
                if (task.getStatus() != TaskType.TYPE_STATUS_REWARD
                        && modifyTaskSchedule(player, task, stask.getCond(), stask.getCondId(), stask.getSchedule(), schedule, param)) {
                    if (chgDailyTaskId == null) {
                        chgDailyTaskId = new ArrayList<>();
                    }
                    chgDailyTaskId.add(stask.getTaskId());
                }
            }
        }
        checkchgDailyTaskAndSync(player, chgDailyTaskId);

        // 军团任务
        if (!TimeHelper.isFriday()) {// 周五无军团任务
            int partyLv = campDataManager.getCampMember(player.lord.getLordId()).getTaskLv();
            for (StaticPartyTask next : StaticTaskDataMgr.getPartyTaskMap().values()) {
                if (partyLv != next.getLv() || next.getCond() != cond) {
                    continue;
                }
                Task task = player.partyTask.computeIfAbsent(next.getId(), x -> new Task(next.getId()));
                modifyTaskSchedule(player, task, next.getCond(), next.getCondId(), next.getSchedule(), schedule, param);
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
                    return task.getStatus() != TaskType.TYPE_STATUS_REWARD && modifyTaskSchedule(player, task, sTask.getCond(), sTask.getCondId(), sTask.getSchedule(), schedule, param);
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

    public boolean modifyTaskSchedule(Player player, Task task, int sCond, int sCondId, int sSchedule, int schedule,
                                      int... param) {
        if (task == null) {
            return false;
        }
        if (task.getSchedule() >= sSchedule) {
            return false;
        }
        long oldSchedule = task.getSchedule();
        switch (sCond) {
            case TaskType.COND_18: {// 开始升级某个建筑,到多少级
                int paramId = param.length > 0 ? param[0] : 0;
                if (sCondId == 0 || sCondId == paramId) {
                    // 获取该建筑当前等级
                    int lv = BuildingDataManager.getBuildingTopLv(player, paramId);
                    task.setSchedule(lv + schedule);
                }
                break;
            }
            // 单个条件
            case TaskType.COND_OTHER_TASK_CNT:
            case TaskType.COND_19:
            case TaskType.COND_BATTLE_CITY_LV_CNT:
            case TaskType.COND_BATTLE_STATE_LV_CNT:
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
            case TaskType.COND_APPOINTMENT_AGENT:
            case TaskType.COND_INTERACTION_AGENT:
            case TaskType.COND_PRESENT_GIFT_AGENT:
            case TaskType.COND_DESIGNATED_HERO_ID_UPGRADE:
            case TaskType.COND_FISHING_MASTER://钓到一个鱼
            case TaskType.COND_TREASURE_WARE_MAKE_COUNT://打造宝具
            case TaskType.COND_GET_TREASURE_WARE_HOOK_AWARD://领取挂机奖励
            case TaskType.COND_990:
            case TaskType.COND_994:
            case TaskType.COND_995:
            case TaskType.COND_506:
            case TaskType.COND_513:
            case TaskType.COND_520:
            case TaskType.COND_521:
            case TaskType.COND_522:
            case TaskType.COND_525:
            case TaskType.COND_527:
            case TaskType.COND_528:
            case TaskType.COND_529:
            case TaskType.COND_530:
            case TaskType.COND_531:
            case TaskType.COND_532:
            case TaskType.COND_533:
            case TaskType.COND_997:
            case TaskType.COND_998:
                int paramId = param.length > 0 ? param[0] : 0;
                if (sCondId == 0 || sCondId == paramId) {
                    task.setSchedule(task.getSchedule() + schedule);
                }
                break;
            case TaskType.COND_SUPER_EQUIP:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId == paramId) {
                    Optional.ofNullable(player.supEquips.get(sCondId)).ifPresent(superEquip -> {
                        task.setSchedule(superEquip.getLv());
                    });
                }
                break;
            case TaskType.COND_UNLOCK_AGENT:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId == paramId) {
                    if (Objects.nonNull(player.getCia())) {
                        FemaleAgent femaleAgent = player.getCia().getFemaleAngets().get(paramId);
                        if (Objects.nonNull(femaleAgent) && CiaConstant.AGENT_UNLOCK_STATUS_2 == femaleAgent.getStatus()) {
                            task.setSchedule(sSchedule);
                        }
                    }
                }
                break;
            case TaskType.COND_BUILDING_TYPE_LV:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId == paramId) {
                    schedule = BuildingDataManager.getBuildingTopLv(player, sCondId);
                    task.setSchedule(schedule);
                }
                break;
            case TaskType.COND_RES_FOOD_CNT:
                long count = buildingDataManager.getBuilding4LvCnt(player, BuildingType.RES_FOOD, sCondId);
                task.setSchedule(count);
                break;
            case TaskType.COND_RES_OIL_CNT:
                count = buildingDataManager.getBuilding4LvCnt(player, BuildingType.RES_OIL, sCondId);
                task.setSchedule(count);
                break;
            case TaskType.COND_RES_ELE_CNT:
                count = buildingDataManager.getBuilding4LvCnt(player, BuildingType.RES_ELE, sCondId);
                task.setSchedule(count);
                break;
            case TaskType.COND_RES_ORE_CNT:
                count = buildingDataManager.getBuilding4LvCnt(player, BuildingType.RES_ORE, sCondId);
                task.setSchedule(count);
                break;
            case TaskType.COND_32: {
                paramId = param.length > 0 ? param[0] : 0;
                if (paramId == sCondId) {
                    count = player.heros.values().stream().filter(hero -> hero.getEquip().length >= paramId && hero.getEquip()[paramId] > 0).count();
                    task.setSchedule(count);
                }
                break;
            }
            case TaskType.COND_LORD_LV:
                count = player.lord.getLevel();
                task.setSchedule(count);
                break;
            // 单个条件 非累加
            case TaskType.COND_500:
            case TaskType.COND_507:
                paramId = param.length > 0 ? param[0] : 0;
                if ((sCondId == 0 || sCondId == paramId) && schedule > task.getSchedule()) {
                    task.setSchedule(schedule);
                }
                break;
            case TaskType.COND_DESIGNATED_HERO_QUALITY_UPGRADE:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId == 0 || sCondId <= paramId) {
                    task.setSchedule(schedule);
                }
                break;
            // 大于或等于所需条件
            case TaskType.COND_ATTCK_PLAYER_CNT:
            case TaskType.COND_30:
            case TaskType.COND_FACTORY_RECRUIT:
            case TaskType.COND_COMMAND_ADD:
            case TaskType.COND_535:
            case TaskType.COND_536:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId == 0 || sCondId <= paramId) {
                    task.setSchedule(task.getSchedule() + schedule);
                }
                break;
            case TaskType.COND_EQUIP: // 指定将领的指定部位穿装备(双重条件)
                paramId = param.length > 0 ? param[0] : 0;
                int param2 = param.length > 1 ? param[1] : 0;
                if ((sCondId == 0 || sCondId == paramId) && param2 == sSchedule) {
                    task.setSchedule(sSchedule);
                }
                break;
            case TaskType.COND_MONTH_CARD_STATE_45:// 检测是否在月卡状态
                //检测功能卡任意存在，就完成每日任务
                int today = TimeHelper.getCurrentDay();
                for (FunCard fc : player.funCards.values()) {
                    if (today == fc.getLastTime() && task.getSchedule() < 1) {
                        task.setSchedule(1);
                    }
                }
                break;
            case TaskType.COND_LOGIN_36: // 每日登陆
                if (task.getSchedule() < 1) {
                    task.setSchedule(1);
                }
                break;
            case TaskType.COND_STONE_HOLE_49:// 装备x个x级以上饰品
                int cnt = stoneService.getInlaidStoneNumByStoneLv(player, sCondId);
                task.setSchedule(cnt);
                break;
            case TaskType.COND_991:
                int progress = heroService.getTaskSchedule1(player, sCondId);
                task.setSchedule(progress);
                break;
            case TaskType.COND_992:
            case TaskType.COND_993:
                if (param[0] >= sCondId) {
                    task.setSchedule(task.getSchedule() + schedule);
                }
                break;
            case TaskType.COND_996:
                int v = player.getMixtureDataById(PlayerConstant.CAMP_FIGHT_TOTAL_COUNT);
                if (v != task.getSchedule()) {
                    task.setSchedule(v);
                }
                break;
            case TaskType.COND_501:
            case TaskType.COND_519:
                int quality = sCond == TaskType.COND_501 ? Constant.Quality.green : Constant.Quality.blue;
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId == paramId) {
                    count = chapterTaskDataManager.getHeroQualityEquipCount(player, sCondId, quality);
                    task.setSchedule(count);
                }
                break;
            case TaskType.COND_502:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId <= paramId) {
                    count = chapterTaskDataManager.getHeroEquipReformAttack(player, sCondId);
                    task.setSchedule(count);
                }
                break;
            case TaskType.COND_503:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId <= paramId) {
                    count = chapterTaskDataManager.getHeroEquipReform(player, sCondId);
                    task.setSchedule(count);
                }
                break;
            case TaskType.COND_504:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId <= paramId) {
                    count = chapterTaskDataManager.battleHeroWearingEquipment(player, sCondId);
                    task.setSchedule(count);
                }
                break;
            case TaskType.COND_505:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId <= paramId) {
                    count = chapterTaskDataManager.arbitrarilyHeroWearingEquipment(player, sCondId, sSchedule);
                    task.setSchedule(count);
                }
                break;
            case TaskType.COND_508:
                int armNum = factoryService.getAddNumByCondId(player, sCondId);
                task.setSchedule(armNum);
                break;
            case TaskType.COND_509:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId <= paramId) {
                    count = player.getAllOnBattleHeros().stream().filter(hero -> hero.getQuality() >= sCondId).count();
                    task.setSchedule(count);
                }
                break;
            case TaskType.COND_510:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId <= paramId) {
                    count = Arrays.stream(player.heroAcq).filter(heroId -> {
                        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                        if (Objects.nonNull(staticHero)) {
                            return staticHero.getQuality() >= sCondId;
                        }
                        return false;
                    }).count();
                    task.setSchedule(count);
                }
                break;
            case TaskType.COND_511:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId <= paramId) {
                    count = player.heros.values().stream().filter(hero -> Objects.nonNull(hero) && Arrays.stream(Arrays.copyOfRange(hero.getWash(), 1, hero.getWash().length)).sum() >= sCondId).count();
                    task.setSchedule(count);
                }
                break;
            case TaskType.COND_512:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId <= paramId) {
                    count = player.heros.values().stream().filter(hero -> Objects.nonNull(hero) && hero.getWash()[HeroConstant.ATTR_ATTACK] >= sCondId).count();
                    task.setSchedule(count);
                }
                break;
            case TaskType.COND_514:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId <= paramId) {
                    count = player.heros.values().stream().filter(hero -> Objects.nonNull(hero) && hero.getLevel() >= sCondId).count();
                    task.setSchedule(count);
                }
                break;
            case TaskType.COND_515:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId <= paramId && Objects.nonNull(player.getCia())) {
                    count = player.getCia().getFemaleAngets().values().stream().filter(cia -> Objects.nonNull(cia) && cia.getExp() >= sCondId).count();
                    task.setSchedule(count);
                }
                break;
            case TaskType.COND_516:
                paramId = param.length > 0 ? param[0] : 0;
                if (sCondId <= paramId) {
                    count = player.heros.values().stream().filter(e -> e.getFightVal() >= sCondId).count();
                    task.setSchedule(count);
                }
                break;
            case TaskType.COND_517:
//                paramId = param.length > 0 ? param[0] : 0;
//                if (sCondId == paramId) {
//                    Turple<Integer, Integer> turple = TaskCone517Type.getQualityAndStageByCondId(sCondId);
//                    if (Objects.nonNull(turple)) {
//                        long count = player.heros.values().stream().filter(hero -> {
//                            if (hero.getQuality() >= turple.getA()) {
//                                return true;
//                            } else return hero.getQuality() == turple.getA() && hero.getStage() >= turple.getB();
//                        }).count();
//                        task.setSchedule(count);
//                        return true;
//                    }
//                }
                break;
            case TaskType.COND_534:
                count = player.getAllOnBattleHeros().stream().filter(e -> {
                    Integer treasureWare = e.getTreasureWare();
                    return Objects.nonNull(treasureWare) && treasureWare > 0;
                }).count();
                task.setSchedule(count);
                break;
            default:
        }
        return oldSchedule != task.getSchedule();
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
        if (Objects.isNull(task) || task.getStatus() == TaskType.TYPE_STATUS_FINISH) {
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
            return null;
        }
        if (task.getStatus() != TaskType.TYPE_STATUS_UNFINISH) {
            return task;
        }
        long schedule = 0;
        switch (cond) {
            case TaskType.COND_LORD_LV:
                schedule = player.lord.getLevel();
                break;
            case TaskType.COND_500:
            case TaskType.COND_TECH_LV: {
                schedule = techDataManager.getTechLv(player, condId);
                break;
            }
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
                schedule = stoneService.getInlaidStoneNumByStoneLv(player, condId);
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
            case TaskType.COND_991:
                schedule = heroService.getTaskSchedule1(player, condId);
                break;
            case TaskType.COND_27:
                schedule = player.heros.values().stream().filter(e -> e.getType() == condId).count();
                break;
            case TaskType.COND_996:
                int v = player.getMixtureDataById(PlayerConstant.CAMP_FIGHT_TOTAL_COUNT);
                task.setSchedule(v);
                break;
            case TaskType.COND_501:
            case TaskType.COND_519:
                schedule = chapterTaskDataManager.getHeroQualityEquipCount(player, condId, cond == TaskType.COND_501 ? Constant.Quality.green : Constant.Quality.blue);
                break;
            case TaskType.COND_502:
                schedule = chapterTaskDataManager.getHeroEquipReformAttack(player, condId);
                break;
            case TaskType.COND_503:
                schedule = chapterTaskDataManager.getHeroEquipReform(player, condId);
                break;
            case TaskType.COND_504:
                schedule = chapterTaskDataManager.battleHeroWearingEquipment(player, condId);
                break;
            case TaskType.COND_505:
                schedule = chapterTaskDataManager.arbitrarilyHeroWearingEquipment(player, condId, sSchedule);
                break;
            case TaskType.COND_506:
                Prop prop = player.props.get(condId);
                if (Objects.nonNull(prop)) {
                    schedule = prop.getCount();
                }
                break;
            case TaskType.COND_508:
                schedule = factoryService.getAddNumByCondId(player, condId);
                break;
            case TaskType.COND_509:
                schedule = player.getAllOnBattleHeros().stream().filter(hero -> hero.getQuality() >= condId).count();
                break;
            case TaskType.COND_510:
                schedule = Arrays.stream(player.heroAcq).filter(heroId -> {
                    StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                    if (Objects.nonNull(staticHero)) {
                        return staticHero.getQuality() >= condId;
                    }
                    return false;
                }).count();
                break;
            case TaskType.COND_511:
                schedule = player.heros.values().stream().filter(hero -> Objects.nonNull(hero) && Arrays.stream(Arrays.copyOfRange(hero.getWash(), 1, hero.getWash().length)).sum() >= condId).count();
                break;
            case TaskType.COND_512:
                schedule = player.heros.values().stream().filter(hero -> Objects.nonNull(hero) && hero.getWash()[HeroConstant.ATTR_ATTACK] >= condId).count();
                break;
            case TaskType.COND_514:
                schedule = player.heros.values().stream().filter(hero -> Objects.nonNull(hero) && hero.getLevel() >= condId).count();
                break;
            case TaskType.COND_515:
                if (Objects.nonNull(player.getCia())) {
                    schedule = player.getCia().getFemaleAngets().values().stream().filter(cia -> Objects.nonNull(cia) && cia.getExp() >= condId).count();
                }
                break;
            case TaskType.COND_516:
                schedule = player.heros.values().stream().filter(e -> e.getShowFight().values().stream().mapToInt(Integer::intValue).sum() >= condId).count();
                break;
            case TaskType.COND_517:
//                Turple<Integer, Integer> turple = TaskCone517Type.getQualityAndStageByCondId(condId);
//                if (Objects.nonNull(turple)) {
//                    schedule = player.heros.values().stream().filter(hero -> {
//                        if (hero.getQuality() >= turple.getA()) {
//                            return true;
//                        } else return hero.getQuality() == turple.getA() && hero.getStage() >= turple.getB();
//                    }).count();
//                }
                break;
            case TaskType.COND_534:
                schedule = player.getAllOnBattleHeros().stream().filter(e -> {
                    Integer treasureWare = e.getTreasureWare();
                    return Objects.nonNull(treasureWare) && treasureWare > 0;
                }).count();
                break;
            case TaskType.COND_990:
                schedule = player.getMixtureDataById(PlayerConstant.HERO_SEARCH_NEW_TOTAL_COUNT);
                break;
            case TaskType.COND_30:
            case TaskType.COND_FACTORY_RECRUIT:
            case TaskType.COND_COMMAND_ADD:
                List<History> histories = player.typeInfo.get(Constant.TypeInfo.TYPE_1);
                if (CheckNull.nonEmpty(histories)) {
                    schedule = histories.stream().mapToInt(e -> {
                        if (Objects.isNull(e)) return 0;
                        StaticCommandMult staticCommandMult = StaticBuildingDataMgr.getCommandMult(e.getId());
                        if (Objects.nonNull(staticCommandMult) && staticCommandMult.getType() == CommandMultType.getCommandMultType(cond)) {
                            return staticCommandMult.getLv();
                        }
                        return 0;
                    }).max().orElse(0);
                }
                break;
            case TaskType.COND_HERO_EQUIPID:
                schedule = player.equips.values().stream().filter(e -> Objects.nonNull(e) && e.getEquipId() == condId && e.getHeroId() > 0).count();
                break;
            case TaskType.COND_28:
                schedule = player.getAllOnBattleHeros().stream().filter(e -> e.getType() == condId).count();
                break;
            case TaskType.COND_UNLOCK_AGENT:
                if (Objects.nonNull(player.getCia())) {
                    FemaleAgent femaleAgent = player.getCia().getFemaleAngets().get(condId);
                    if (Objects.nonNull(femaleAgent) && CiaConstant.AGENT_UNLOCK_STATUS_2 == femaleAgent.getStatus()) {
                        schedule = sSchedule;
                    }
                }
                break;
            default:
                break;
        }
        boolean push = false;
        if (schedule > task.getSchedule()) {
            task.setSchedule(schedule);
            push = true;
        }
        if (schedule >= sSchedule && task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {// 只能从未完成->完成
            task.setStatus(TaskType.TYPE_STATUS_FINISH);
            push = true;
        }
        if (push) chapterTaskDataManager.pushMajorTask(player, task);
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
     * 是否开启了世界任务
     *
     * @param player
     * @return true 开启
     */
    public boolean isOpenWorldTask(Player player) {
        if (player.worldTasks.size() >= 2) { // 兼容用gm命令开启的世界任务,
            return true;
        }
        Task task = player.chapterTask.getOpenTasks().get(WorldConstant.WORLD_TASK_OPEN_TASK_ID);
        return task != null && task.getStatus() == TaskType.TYPE_STATUS_REWARD;
    }
}
