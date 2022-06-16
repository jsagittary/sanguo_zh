package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.SeasonConst;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Combat;
import com.gryphpoem.game.zw.resource.domain.p.CombatFb;
import com.gryphpoem.game.zw.resource.domain.p.StoneCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingInit;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunctionCondition;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunctionOpen;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunctionUnlock;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.pojo.function.AbstractCondition;
import com.gryphpoem.game.zw.resource.pojo.global.GlobalSchedule;
import com.gryphpoem.game.zw.resource.pojo.season.GlobalSeasonData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.FunctionConditionUtil;
import com.gryphpoem.game.zw.service.session.SeasonService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 功能解锁、开关配置相关数据
 * @author TanDonghai
 * @date 创建时间：2017年6月7日 下午7:10:56
 *
 */
public class StaticFunctionDataMgr {
    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    private static Map<Integer, StaticFunctionUnlock> unlockMap;

    private static Map<Integer, StaticFunctionCondition> conditionMap;

    private static Map<Integer, AbstractCondition> conditions;

    // key 建筑ID或功能ID
    private static Map<Integer, StaticFunctionOpen> openMap;

    public static void init() {
        Map<Integer, StaticFunctionUnlock> unlockMap = staticDataDao.selectFunctionUnlockMap();
        StaticFunctionDataMgr.unlockMap = unlockMap;

        Map<Integer, StaticFunctionCondition> conditionMap = staticDataDao.selectFunctionConditionMap();
        StaticFunctionDataMgr.conditionMap = conditionMap;

        Map<Integer, StaticFunctionOpen> openMap = staticDataDao.selectStaticFunctionOpen();
        StaticFunctionDataMgr.openMap = openMap;

        // 初始化功能解锁条件检查对象
        initFunctionUnlockConditions();
    }

    private static void initFunctionUnlockConditions() {
        AbstractCondition condition;
        Map<Integer, AbstractCondition> conditions = new HashMap<>();
        for (StaticFunctionCondition sfc : conditionMap.values()) {
            try {
                condition = FunctionConditionUtil.getCondition(sfc);
                if (null != condition) {
                    conditions.put(sfc.getConditionId(), condition);
                }
            } catch (MwException e) {
                LogUtil.error(e);
            }
        }
        StaticFunctionDataMgr.conditions = conditions;
    }

    public static Map<Integer, StaticFunctionUnlock> getUnlockMap() {
        return unlockMap;
    }

    public static Map<Integer, StaticFunctionCondition> getConditionMap() {
        return conditionMap;
    }

    public static AbstractCondition getConditionById(int conditionId) {
        return conditions.get(conditionId);
    }

    public static StaticFunctionOpen getOpenById(int typeId) {
        return openMap.get(typeId);
    }

    /**
     * 检测功能是否解锁
     * 
     * @param player
     * @param typeId 功能id
     * @return true 已解锁
     */
    public static boolean funcitonIsOpen(Player player, int typeId) {
        StaticFunctionOpen sOpen = StaticFunctionDataMgr.getOpenById(typeId);
        if (sOpen == null) {
            LogUtil.debug("未找到建筑解锁配置   buildingId:", typeId);
            return false;
        }
        // 等级条件
        int lv = sOpen.getLv();
        if (lv > 0 && player.lord.getLevel() < lv) {
            return false;
        }
        // 其他建筑等级要求
        List<Integer> needBuilding = sOpen.getNeedBuilding();
        if (!CheckNull.isEmpty(needBuilding) && needBuilding.size() == 3) {
            int buildId = needBuilding.get(0);// 建筑ID
            // int needCount = needBuilding.get(1); // 建筑的个数
            int needLv = needBuilding.get(2);// 建筑等级
            StaticBuildingInit sBuilding = StaticBuildingDataMgr.getBuildingInitMapById(buildId);
            if (sBuilding == null) {
                return false;
            }
            int buildingLv = BuildingDataManager.getBuildingLv(buildId, player);
            if (buildingLv < needLv) {
                return false;
            }
        }
        // 任务要求
        int taskId = sOpen.getTaskId();
        if (taskId > 0) {
            Task task = player.chapterTask.getOpenTasks().get(taskId);
            if (task == null || task.getStatus() < TaskType.TYPE_STATUS_REWARD) {
                return false;
            }
        }
        // 副本要求
        int combatId = sOpen.getCombatId();
        if (combatId > 0) {
            CombatFb combatFb = player.combatFb.get(combatId);// 高级副本
            Combat combat = player.combats.get(combatId);// 普通副本关卡
            if ((combat == null || combat.getStar() < 1) && (combatFb == null || combatFb.getStatus() == 0)) {
                return false;
            }
        }
        // 宝石副本要求
        int stoneCombatId = sOpen.getStoneCombatid();
        if (stoneCombatId > 0) {
            StoneCombat stoneCombat = player.stoneCombats.get(stoneCombatId);
            if (stoneCombat == null || stoneCombat.getPassCnt() <= 0) {
                return false;
            }
        }
        // 服务器Id判断
        List<List<Integer>> serverIdList = sOpen.getServerId();
        if (!CheckNull.isEmpty(serverIdList)) {
            final int selfServerId = DataResource.ac.getBean(ServerSetting.class).getServerID();
            return serverIdList.stream()
                    .filter(l -> !CheckNull.isEmpty(l) && l.get(0) <= selfServerId && selfServerId <= l.get(1))
                    .findFirst().orElse(null) != null;
        }
        // 世界进度判断
        int scheduleId = sOpen.getScheduleId();
        if (scheduleId > 0) {
            GlobalSchedule globalSchedule = DataResource.ac.getBean(GlobalDataManager.class).getGameGlobal().getGlobalSchedule();
            if (CheckNull.isNull(globalSchedule) || globalSchedule.getCurrentScheduleId() < scheduleId) {
                return false;
            }
        }
        //是否开放过赛季
        if(typeId == BuildingType.SEASON_TREASURY){
            GlobalSeasonData globalSeasonData = DataResource.ac.getBean(GlobalDataManager.class).getGameGlobal().getGlobalSeasonData();
            if(globalSeasonData.getLastSeasonId() <= 0){
                int seasonState = DataResource.ac.getBean(SeasonService.class).getSeasonState();
                if(seasonState == SeasonConst.STATE_OPEN){
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    /**
     * 检测功能是否解锁
     * 到该等级只触发一次
     *
     * @param player
     * @param typeId 功能id
     * @return true 已解锁
     */
    public static boolean funcitonIsOneOpen(Player player, int typeId) {
        StaticFunctionOpen sOpen = StaticFunctionDataMgr.getOpenById(typeId);
        if (sOpen == null) {
            LogUtil.debug("未找到建筑解锁配置   buildingId:", typeId);
            return false;
        }
        // 等级条件
        int lv = sOpen.getLv();
        if (lv > 0 && player.lord.getLevel() != lv) {
            return false;
        }
        return true;
    }
}
