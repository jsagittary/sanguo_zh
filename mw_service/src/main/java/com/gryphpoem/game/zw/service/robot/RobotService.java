package com.gryphpoem.game.zw.service.robot;

import com.gryphpoem.game.zw.core.executor.NonOrderedQueuePoolExecutor;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb.RobotDataRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Mill;
import com.gryphpoem.game.zw.resource.domain.p.Robot;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingInit;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.BuildingState;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.robot.work.RobotWorker;
import com.gryphpoem.game.zw.service.AcquisitionService;
import com.gryphpoem.game.zw.service.BuildingService;
import com.gryphpoem.game.zw.service.MineService;
import com.gryphpoem.game.zw.service.TechService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @Description 游戏机器人相关
 * @date 创建时间：2017年9月16日 下午3:13:46
 */
@Service
public class RobotService {

    @Autowired
    private TaskDataManager taskDataManager;

    @Autowired
    private RobotDataManager robotDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private BuildingDataManager buildingDataManager;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private RobotBuildingService robotBuildingService;

    @Autowired
    private RobotEquipService robotEquipService;

    @Autowired
    private RobotHeroService robotHeroService;

    @Autowired
    private RobotArmyService robotArmyService;

    @Autowired
    private RobotCombatService robotCombatService;

    @Autowired
    private RobotTaskService robotTaskService;

    @Autowired
    private RobotWarService robotWarService;

    @Autowired
    private AcquisitionService acquisitionService;

    @Autowired
    private MineService mineService;

    @Autowired
    private TechService techService;

    @Autowired
    private WorldDataManager worldDataManager;

    /**
     * 非有序线程池
     */
    private static NonOrderedQueuePoolExecutor robotExcutor = new NonOrderedQueuePoolExecutor(50);


    /**
     * 添加建筑自动升级操作
     *
     * @param robot
     */
    public void addBuildingAutoBuild(Player robot) {
        if (null == robot || buildQueIsFull(robot)) {
            return;
        }

        robotBuildingService.addBuildingAutoBuild(robot);
    }

    /**
     * 判断玩家的建造队列是否已满
     *
     * @param robot
     * @return
     */
    public boolean buildQueIsFull(Player robot) {
        return buildingService.buildQueIsFull(robot);
    }

    /**
     * 添加机器人自动升级科技操作
     *
     * @param robot
     */
    public void addTechAutoUp(Player robot) {
        if (robotLogSwitch()) {
            LogUtil.robot("机器人升级科技, robot:", robot.roleId);
        }
        techService.addAutoTechUp(robot);
    }


    /**
     * 将领招募
     *
     * @param robot
     */
    public void heroRecruit(Player robot) {
        if (null != robot && robot.robotRecord.isNeedRecruitHero(TimeHelper.getCurrentSecond())) {
            if (robotLogSwitch()) {
                LogUtil.robot("机器人将领招募, robot:", robot.roleId);
            }
            robotHeroService.autoRecruitHero(robot);
        }
    }

    /**
     * 士兵招募
     *
     * @param robot
     */
    public void armRecruit(Player robot) {
        if (null != robot && robot.robotRecord.isNeedRecruitArm(TimeHelper.getCurrentSecond())) {
            if (robotLogSwitch()) {
                LogUtil.robot("机器人士兵招募, robot:", robot.roleId);
            }
            robotArmyService.autoRecruitArmy(robot);
        }
    }

    /**
     * 装备自动打造、收取、穿戴
     *
     * @param robot
     */
    public void addAutoEquipLogic(Player robot) {
        if (robotLogSwitch()) {
            LogUtil.robot("机器人装备自动打造, robot:", robot.roleId);
        }
        // 自动穿戴装备
        if (robot.robotRecord.isNeedDressEquip()) {
            robotEquipService.autoDressEquip(robot);
        }

        // 自动收取打造完的装备
        robotEquipService.autoGainEquip(robot);

        // 自动打造装备
        robotEquipService.autoEquipForge(robot);
    }

    /**
     * 装备自动改造（洗炼）
     *
     * @param robot
     */
    public void equipRefit(Player robot) {
        int now = TimeHelper.getCurrentSecond();
        if (robot.robotRecord.isNeedEquipRefit(now)) {
            if (robotLogSwitch()) {
                LogUtil.robot("机器人装备改造, robot:", robot.roleId);
            }
            robotEquipService.autoEquipRefit(robot, now);
            robot.robotRecord.setNextEquipRefit(now + RobotConstant.EQUIP_REFIT_DELAY);
        }
    }

    /**
     * 增加机器人VIP经验
     *
     * @param robot
     * @param addVipExp
     */
    public void adRobotVipExp(Player robot, int addVipExp) {
        rewardDataManager.addAward(robot, AwardType.MONEY, AwardType.Money.VIP_EXP, addVipExp, AwardFrom.ROBOT);
        if (robotLogSwitch()) {
            LogUtil.robot("机器人增加VIP经验, robot:", robot.roleId, ", add:", addVipExp, ", have:", robot.lord.getVipExp());
        }
    }

    /**
     * 增加机器人金币
     *
     * @param robot
     * @param addGold
     */
    public void addRobotGold(Player robot, int addGold) {
        rewardDataManager.addAward(robot, AwardType.MONEY, AwardType.Money.GOLD, addGold, AwardFrom.ROBOT);
        if (robotLogSwitch()) {
            LogUtil.robot("机器人增加金币, robot:", robot.roleId, ", add:", addGold, ", have:", robot.lord.getGold());
        }
    }

    /**
     * 自动攻打副本
     *
     * @param robot
     */
    public void autoDoCombat(Player robot) {
        int now = TimeHelper.getCurrentSecond();
        if (robot.robotRecord.isNeedDoCombat(now)) {
            if (robotLogSwitch()) {
                LogUtil.robot("机器人攻打副本, robot:", robot.roleId);
            }
            if (robotCombatService.autoDoCombat(robot)) {
                // 如果本次攻打副本成功，将下次启动攻打副本逻辑的时间间隔缩短，防止限时类副本超过时间没有攻打
                robot.robotRecord.setNextDoCombat(now + RobotConstant.ROBOT_SLEEP);
            } else {
                robot.robotRecord.setNextDoCombat(now + RobotConstant.DO_COMBAT_DELAY);
            }
        }
    }

    /**
     * 自动采矿
     *
     * @param player
     */
    public void autoCollectMine(Player player) {
        Robot robot = robotDataManager.getRobot(player.roleId);
        if (!robotIsActive(robot)) {
            return;
        }
        int now = TimeHelper.getCurrentSecond();
        if (player.robotRecord.isNeedCollectMine(now)) {
            if (robotLogSwitch()) {
                LogUtil.robot("机器人采矿, robot:", player.roleId);
            }
            mineService.autoCollectMine(player, now);
            player.robotRecord.setNextCollectMine(now + RobotConstant.DO_COMBAT_DELAY);
        }
    }

    /**
     * 自动采集个人资源点
     *
     * @param player
     */
    public void autoAcauisition(Player player) {
        Robot robot = robotDataManager.getRobot(player.roleId);
        if (!robotIsActive(robot)) {
            return;
        }
        if (robotLogSwitch()) {
            LogUtil.robot("机器人个人资源点采集, robot:", player.roleId);
        }
        acquisitionService.autoAcquisition(player);
    }

    /**
     * 自动攻击流寇
     *
     * @param player
     */
    public void autoAttackBandit(Player player) {
        Robot robot = robotDataManager.getRobot(player.roleId);
        if (!robotIsActive(robot)) {
            return;
        }
        if (player.lord.getLevel() >= RobotConstant.ATTACK_BANDIT_MIN_LV) {
            int now = TimeHelper.getCurrentSecond();
            if (player.robotRecord.isNeedAttackBandit(now)) {
                if (robotLogSwitch()) {
                    LogUtil.robot("机器人攻打流寇, robot:", player.roleId);
                }
                robotWarService.autoAttackBandit(player);
                player.robotRecord.setNextAttackBandit(now + RobotConstant.ROBOT_SLEEP);
            }
        }
    }

    /**
     * 自动领取任务奖励
     *
     * @param robot
     */
    public void autoTaskReward(Player robot) {
        if (robotLogSwitch()) {
            LogUtil.robot("机器人领取任务奖励, robot:", robot.roleId);
        }
        robotTaskService.autoTaskReward(robot);
    }

    /**
     * 自动领取资源（征收资源）
     *
     * @param robot
     */
    public void autoGainResource(Player robot) {
        if (robotLogSwitch()) {
            LogUtil.robot("机器人征收资源, robot:", robot.roleId);
        }
        int now = TimeHelper.getCurrentSecond();
        Iterator<Mill> it2 = robot.mills.values().iterator();
        while (it2.hasNext()) {
            Mill mill = it2.next();
            // 检测资源是否解锁
            if (!buildingDataManager.checkMillCanGain(robot, mill)) {
                continue;
            }
            int resCnt = mill.getResCnt();
            if (resCnt > 0) {
                mill.setResCnt(0);
                mill.setResTime(now);
                BuildingState buildingState = robot.getBuildingData().get(mill.getPos());
                if (buildingState == null) {
                    StaticBuildingInit sBuildingInit = StaticBuildingDataMgr.getBuildingInitMapById(mill.getPos());
                    buildingState = new BuildingState(sBuildingInit.getBuildingId(), mill.getType());
                    buildingState.setBuildingLv(sBuildingInit.getInitLv());
                    robot.getBuildingData().put(sBuildingInit.getBuildingId(), buildingState);
                }
                buildingService.gainResource(robot, mill.getType(), mill.getLv(), resCnt, buildingState.getResidentCnt(), buildingState.getLandType());
                if (robotLogSwitch()) {
                    LogUtil.robot("征收资源, type:" + mill.getType() + ", count:" + resCnt);
                }
            }
        }
        taskDataManager.updTask(robot, TaskType.COND_RES_AWARD, 1);
    }

    /**
     * 增加机器人角色经验
     *
     * @param robot
     * @param addExp
     */
    public void addRobotExp(Player robot, int addExp) {
        rewardDataManager.addAward(robot, AwardType.MONEY, AwardType.Money.EXP, addExp, AwardFrom.ROBOT);
        if (!robotLogSwitch()) {
            LogUtil.robot("机器人增加角色经验, robot:", robot.roleId, ", add:", addExp, ", lv:", robot.lord.getLevel(), ", exp:",
                    robot.lord.getExp());
        }
    }

    /**
     * 机器人定时任务逻辑
     */
    public void robotTimerLogic() {
        if (robotTimerLogicSwitch()) {
            return;
        }
        Player player;
        int tickCount = 0;// 记录已执行机器人数量
        int now = TimeHelper.getCurrentSecond();
        long startTime = System.currentTimeMillis();// 记录本次定时任务开始时间
        for (Robot robot : robotDataManager.getRobotMap().values()) {
            if (!robot.isValid()) {
                continue;
            }

            // 未到响应时间，将会跳过处理；每次只处理一定数量的机器人，避免占用太多主线程时间；如果处理耗时太长，将结束本次定时任务，等待下次处理
            long executeTime = System.currentTimeMillis() - startTime;
            if (tickCount < RobotConstant.ROBOT_AWAKE_LIMIT && executeTime < RobotConstant.ROBOT_EXECUTE_LIMIT
                    && robot.getNextTick() <= now) {
                player = playerDataManager.getPlayer(robot.getRoleId());
                // 如果机器人帐号有人登录，暂停自动执行任务，由人操作
                if (null == player || player.isLogin) {
                    continue;
                }

                try {
                    // 设置下次响应时间
                    robot.setNextTick(now + RobotConstant.ROBOT_SLEEP);

                    // 遍历行为树，执行相关逻辑
                    // tick(robot);
                    parallelTick(robot);

                    tickCount++;
                } catch (Exception e) {
                    LogUtil.error(e, "机器人逻辑执行出错, robot:", robot.getRoleId());
                }
            }
        }
    }

    /**
     * 多线程同步执行机器人逻辑
     *
     * @param robot
     */
    private void parallelTick(Robot robot) {
        robotExcutor.execute(new RobotWorker(robot));
    }

    /**
     * 机器人执行一次行为树逻辑
     *
     * @param robot
     */
    private void tick(Robot robot) {
        if (null == robot || null == robot.getDefaultTree()) {
            return;
        }

        LogUtil.robot("开始执行机器人逻辑, robot:" + robot.getRoleId());
        long startNano = System.nanoTime();
        robot.getDefaultTree().tick();
        long exeTime = System.nanoTime() - startNano;
        if (exeTime > RobotConstant.ROBOT_EXECUTE_THRESHOLD) {
            LogUtil.robot("机器人逻辑执行时间cost(微秒):", (exeTime / 1000), ", robot:", robot.getRoleId());
        }
    }


    /**
     * 判断机器人日志是否开启
     *
     * @return
     */
    public boolean robotLogSwitch() {
        return RobotConstant.ROBOT_LOG_STATE == RobotConstant.ROBOT_STATE_NORMAL;
    }

    /**
     * 关闭机器人定时任务逻辑
     */
    public void gmCloseRobotTimerLogic() {
        RobotConstant.ROBOT_STATE_SWITCH = RobotConstant.ROBOT_STATE_INVALID;
    }

    /**
     * 判断机器人功能是否开启
     *
     * @return
     */
    public boolean robotTimerLogicSwitch() {
        return RobotConstant.ROBOT_STATE_SWITCH == RobotConstant.ROBOT_STATE_INVALID;
    }

    /**
     * 判断机器人是否活跃(可以在地图上看到机器人,但是看不到机器人的采集,攻击等行为)
     *
     * @param robot
     * @return
     */
    public boolean robotIsActive(Robot robot) {
        return robot.getActionType() == RobotConstant.ROBOT_EXTERNAL_BEHAVIOR;
    }


    /**
     * 添加机器人到指定的区域
     *
     * @param serverId   区域ID
     * @param robotCount 需要加入的机器人数量
     */
    /*public void modCityLogic(int serverId, int robotCount) {
        if (serverId <= WorldConstant.AREA_MIN_ID || serverId > WorldConstant.AREA_MAX_ID) {
            LogUtil.error("modCityLogic error areaId Invalid");
            return;
        }
        if (robotCount <= 0 || robotCount > RobotConstant.ROBOT_AREA_COUNT.get(0)) {
            LogUtil.error("modCityLogic error robotCount Invalid");
            return;
        }
        LinkedList areaRobotsCache = robotDataManager.getAreaRobotsCache(serverId);
        int areaRobotsCount = areaRobotsCache.size();
        if ((areaRobotsCount + robotCount) > RobotConstant.ROBOT_AREA_COUNT.get(1)) {
            LogUtil.error("modCityLogic error moveCity robot Too Many");
            return;
        }
        // 获取当前未分配区域的所有机器人
        List<Robot> nonePosRobotList = robotDataManager.getNonePosRobotList();
        if (nonePosRobotList.size() < robotCount) { // 如果初始化的机器人不够,运行时添加机器人,并初始化行为树
            int needCreateRobotCount = robotCount - nonePosRobotList.size();
            robotDataManager.runtimeCreateRobots(needCreateRobotCount, RobotConstant.ROBOT_DEF_TREE_ID, serverId);
            robotCount -= needCreateRobotCount;
        }
        LinkedBlockingQueue areaRobotsQue = robotDataManager.getAreaRobotsQue();
        List<Robot> robots = nonePosRobotList.subList(0, robotCount);
        for (int i = 0; i < robotCount; i++) {
            Robot robot = robots.get(i);
            if (!areaRobotsQue.contains(robot)) {
                areaRobotsQue.add(robot);
                robot.setPosArea(serverId);
            }
        }
    }*/


    /**
     * 给机器人分配坐标
     */
    public void robotAllotAreaLogic() {
        LinkedBlockingQueue areaRobotsQue = robotDataManager.getAreaRobotsQue();
        if (!CheckNull.isEmpty(areaRobotsQue)) {
            Robot robot = (Robot) areaRobotsQue.poll();
            if (!CheckNull.isNull(robot)) {
                worldDataManager.addNewRobot(robot, false);
                robot.setActionType(RobotConstant.ROBOT_EXTERNAL_BEHAVIOR);
                robotDataManager.getAreaRobotsCache(robot.getPosArea()).add(robot);
                LogUtil.robot("机器人分配坐标 robot :" + robot.getRoleId() + "区域 area :" + robot.getPosArea());
            }
        }
    }

    /**
     * 添加机器人到指定的区域
     *
     * @param areaId   区域ID
     * @param robotCount 需要加入的机器人数量
     */
    public void gmOpenRobotsExternalBehavior(int areaId, int robotCount) {
        if (areaId <= WorldConstant.AREA_MIN_ID || areaId > WorldConstant.AREA_MAX_ID) {
            LogUtil.robot("gmModCityLogic error : areaId Invalid");
            return;
        }
        if (robotCount <= 0 || robotCount > RobotConstant.ROBOT_AREA_COUNT.get(0)) {
            LogUtil.robot("gmModCityLogic error : robotCount Invalid");
            return;
        }
        LinkedList areaRobotsCache = robotDataManager.getAreaRobotsCache(areaId);
        int areaRobotsCount = areaRobotsCache.size();
        // 获取当前区域没有外在行为的机器人
        List<Robot> innerBehaviorRobots = robotDataManager.getAreaRobotsHaveInnerBehavior(areaId);
        if ((areaRobotsCount + robotCount - innerBehaviorRobots.size()) > RobotConstant.ROBOT_AREA_COUNT.get(1)) {
            LogUtil.robot("gmModCityLogic error : robot Too Many");
            return;
        }
        // 获取当前未分配区域的所有机器人
        List<Robot> nonePosRobotList = robotDataManager.getNonePosRobotList();
        if ((nonePosRobotList.size() + innerBehaviorRobots.size()) < robotCount) {
            LogUtil.robot("gmModCityLogic error : not so many robots available");
            return;
        }
        if (innerBehaviorRobots.size() > 0) {
            innerBehaviorRobots.forEach(robot -> robot.setActionType(RobotConstant.ROBOT_EXTERNAL_BEHAVIOR));
            robotCount -= innerBehaviorRobots.size();
        }
        LinkedBlockingQueue areaRobotsQue = robotDataManager.getAreaRobotsQue();
        List<Robot> robots = nonePosRobotList.subList(0, robotCount);
        for (int i = 0; i < robotCount; i++) {
            Robot robot = robots.get(i);
            if (!areaRobotsQue.contains(robot)) {
                areaRobotsQue.add(robot);
                robot.setPosArea(areaId);
            }
        }
    }

    /**
     * Gm让指定城池的机器人失去外在行为
     *
     * @param areaId     区域ID
     * @param robotCount 需要移除外在行为 的机器人数量
     */
    public void gmCloseRobotsExternalBehavior(Integer areaId, Integer robotCount) {
        if (areaId <= WorldConstant.AREA_MIN_ID || areaId > WorldConstant.AREA_MAX_ID) {
            LogUtil.robot("gmCloseRbotsBehavior error : areaId Invalid");
            return;
        }
        List<Robot> haveExternalBehaviorRobots = robotDataManager.getAreaRobotsHaveExternalBehavior(areaId);
        if (robotCount <= 0 || robotCount > RobotConstant.ROBOT_AREA_COUNT.get(0) || robotCount > haveExternalBehaviorRobots.size()) {
            LogUtil.robot("gmCloseRbotsBehavior error : robotCount Invalid");
            return;
        }
        haveExternalBehaviorRobots.stream().limit(robotCount).forEach(robot -> robot.setActionType(RobotConstant.ROBOT_INNER_BEHAVIOR));
    }

    /**
     * 获取机器人数据
     * @param areaId
     * @return
     */
    public RobotDataRs getRobotCountByArea(int areaId) {
        RobotDataRs.Builder builder = RobotDataRs.newBuilder();
        builder.setAreaCapacity(RobotConstant.ROBOT_AREA_COUNT.get(1));
        builder.setAreaRobotCount(robotDataManager.getAreaRobotsCache(areaId).size());
        builder.setExternalCount(robotDataManager.getAreaRobotsHaveExternalBehavior(areaId).size());
        LinkedBlockingQueue<Robot> areaRobotsQue = robotDataManager.getAreaRobotsQue();
        builder.setRuntimeCreateCount(areaRobotsQue.stream().filter(robot -> robot.getPosArea() == areaId).collect(Collectors.toList()).size());
        return builder.build();
    }

}
