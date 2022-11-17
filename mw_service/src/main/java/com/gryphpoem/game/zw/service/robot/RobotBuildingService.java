package com.gryphpoem.game.zw.service.robot;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.BuildQue;
import com.gryphpoem.game.zw.resource.domain.p.Common;
import com.gryphpoem.game.zw.resource.domain.p.Factory;
import com.gryphpoem.game.zw.resource.domain.p.Mill;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingInit;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingLv;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Description 机器人建筑相关服务类
 * @author TanDonghai
 * @date 创建时间：2017年11月16日 下午4:16:49
 *
 */
@Service
public class RobotBuildingService {

    @Autowired
    private BuildingDataManager buildingDataManager;

    @Autowired
    private TaskDataManager taskDataManager;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private TechDataManager techDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    /**
     * 添加建筑自动升级操作
     * 
     * @param robot
     */
    public void addBuildingAutoBuild(Player robot) {
        if (buildingService.buildQueIsFull(robot)) {
            LogUtil.robot("建筑队列已满， robot:" + robot.roleId);
            return;
        }

        // 检查是否可以功能建筑，如果有功能建筑可以升级，优先升级
        int buildingId = getCanUpFunctionBuildingId(robot);
        if (buildingId > 0) {
            try {
                LogUtil.robot("机器人建造功能建筑, robot:", robot.roleId + ", buildingId:" + buildingId);
                buildingService.upgradeBuilding(robot.roleId, buildingId,false, 0);
            } catch (MwException e) {
                LogUtil.robot(e, "机器人升级司令部等级出错, robot:", robot.roleId);
            }
        }

        Common common = robot.common;
        if (common.getAutoBuildOnOff() == 0) {
            // 开启自动建造
            common.setAutoBuildOnOff(1);
        }

        // 自动建造次数已用完，购买自动建造次数
        if (common.getAutoBuildCnt() <= 0) {
            buildingDataManager.resetAutoBuildCnt(robot);
        }
        LogUtil.robot("机器人添加自动建造, robot:", robot.roleId, ", autoBuild:", common.getAutoBuildOnOff(), ":",
                common.getAutoBuildCnt());

        // 检查更新资源矿点解锁信息
        updateMillUnlock(robot);

        // 刷新资源矿点解锁状态
        buildingDataManager.refreshSourceData(robot);
        buildingService.addAtuoBuild(robot);

        // 如果有免费加速，立即使用
        for (BuildQue que : robot.buildQue.values()) {
            if (que.haveFreeSpeed()) {
                que.useFreeSpeed();
                taskDataManager.updTask(robot, TaskType.COND_FREE_CD, 1, que.getBuildingType());
            }
        }
    }

    /**
     * 记录资源建筑的buildingId
     */
    private List<StaticBuildingInit> resourceBuildingList;

    private void updateMillUnlock(Player player) {
        if (null == resourceBuildingList) {
            resourceBuildingList = new CopyOnWriteArrayList<>();
            for (StaticBuildingInit sbi : StaticBuildingDataMgr.getBuildingInitMap().values()) {
                if (isResourceBuildingType(sbi.getBuildingType())) {
                    resourceBuildingList.add(sbi);
                }
            }
        }

        Mill mill;
        int buildingId;
        for (StaticBuildingInit sbi : resourceBuildingList) {
            buildingId = sbi.getBuildingId();
            if (buildingDataManager.checkBuildingLock(player, buildingId)) {
                mill = player.mills.get(buildingId);
                if (mill == null) {
                    mill = new Mill(buildingId, sbi.getBuildingType(), sbi.getInitLv(), 0);
                    mill.setUnlock(true);
                    player.mills.put(buildingId, mill);
                }

                // 对于没有达到1级的资源建筑，机器人默认为1级（即默认为已建造）
                if (mill.getLv() < 1) {
                    mill.setLv(1);
                }
            }
        }
    }

    private boolean isResourceBuildingType(int buildingType) {
        for (Integer type : BuildingType.RES_ARRAY) {
            if (type == buildingType) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取可升级的功能建筑
     * 
     * @param player
     * @return
     */
    private int getCanUpFunctionBuildingId(Player player) {
        for (int buildingType : BuildingType.FUNCTION_BUILDING) {
            if (functionBuildingCanUp(player, buildingType)) {
                return buildingType;
            }
        }
        return -1;
    }

    /**
     * 判断功能建筑是否可建造或升级
     * 
     * @param player
     * @param buildingType
     * @return
     */
    private boolean functionBuildingCanUp(Player player, int buildingType) {
        StaticBuildingInit sBuilding = StaticBuildingDataMgr.getBuildingInitMapById(buildingType);
        if (null == sBuilding || sBuilding.getCanUp() != BuildingType.BUILD_CAN_UP_STATUS) {
            return false;
        }

        long roleId = player.roleId;
        // 判断是否在已经在升级
        for (BuildQue build : player.buildQue.values()) {
            LogUtil.robot("========== build.getPos：", build.getPos());
            if (build.getPos() == buildingType) {
                LogUtil.robot("升级建筑时，建筑升级中, roleId:" + roleId + "type: " + buildingType);
                return false;
            }
        }

        if (!buildingDataManager.checkBuildingLock(player, buildingType)) {
            LogUtil.robot("升级建筑时，建筑未解锁, roleId:" + roleId + ", buildingType:" + buildingType);
            return false;
        }

        int buildLevel = buildingDataManager.getBuildingLv(buildingType, player);
        StaticBuildingLv staticBuildingLevel = StaticBuildingDataMgr.getStaticBuildingLevel(buildingType,
                buildLevel + 1);
        if (staticBuildingLevel == null) {
            // 升级建筑时，找不到升级配置,或已满级
            LogUtil.robot("升级建筑时，找不到升级配置,或已满级 roleId:" + roleId + ",  buildingType:" + buildingType);
            return false;
        }

        // 司令部特出要求，司令部开启等级上限
        if (buildingType == BuildingType.COMMAND && buildLevel >= Constant.MAX_COMMAND_LV) {
            LogUtil.robot(" roleId:" + roleId + ", buildingType:" + buildingType);
            return false;
        }

        if (player.lord.getLevel() < staticBuildingLevel.getRoleLv()) {
            LogUtil.robot("升级建筑时，等级不足, roleId:" + roleId + ", buildingType:" + buildingType + ", conf:"
                    + staticBuildingLevel);
            return false;
        }
        if (buildingDataManager.checkBuildingLv(player, staticBuildingLevel.getUpNeedBuilding())) {
            LogUtil.robot("升级建筑时，建筑等级不足, roleId:" + roleId + ",  buildingType:" + buildingType);
            return false;
        }

        // 兵营在造兵时不能升级
        if (buildingType == BuildingType.FACTORY_1 || buildingType == BuildingType.FACTORY_2
                || buildingType == BuildingType.FACTORY_3) {
            Factory factory = player.factory.get(buildingType);
            if (factory != null && !CheckNull.isEmpty(factory.getAddList())) {
                LogUtil.robot("兵营正在造兵,不能升级  roleId:" + roleId + ", buildingType:" + buildingType);
                return false;
            }
        }

        // 检测化工厂是否正在生产
        if (buildingType == BuildingType.CHEMICAL_PLANT && player.chemical != null
                && !CheckNull.isEmpty(player.chemical.getPosQue())) {
            LogUtil.robot("化工厂是否正在生产,不能升级  roleId:" + roleId + ", buildingType:" + buildingType);
            return false;
        }
        // 检测科研所是否在研究
        if (buildingType == BuildingType.TECH && player.tech != null && player.tech.getQue() != null
                && player.tech.getQue().getId() > 0) {
            // 检测购买过vip礼包5
            if (player.shop == null) {// shop为null
                LogUtil.robot("科研所正在研究,不能升级  roleId:" + roleId);
                return false;
            } else {
                // 购买vip5礼包,同时雇佣了高级研究院
//                if (!(
////                        player.shop.getVipId().contains(Constant.TECH_QUICK_VIP_BAG)
////                        &&
//                                techDataManager.isAdvanceTechGain(player))) {
//                    LogUtil.robot("科研所正在研究,不能升级  roleId:" + roleId);
//                    return false;
//                }
            }
        }

        // 检查资源条件是否满足
        try {
            rewardDataManager.checkPlayerResIsEnough(player, staticBuildingLevel.getUpNeedResource(), "升级建筑时");
        } catch (MwException e) {
            return false;
        }
        return true;
    }

}
