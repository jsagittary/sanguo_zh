package com.gryphpoem.game.zw.manager;

import com.hundredcent.game.ai.btree.HaiBehaviorTree;
import com.hundredcent.game.ai.btree.HaiBtreeManager;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticRobotDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.LoginConstant;
import com.gryphpoem.game.zw.resource.constant.RobotConstant;
import com.gryphpoem.game.zw.resource.dao.impl.p.AccountDao;
import com.gryphpoem.game.zw.resource.dao.impl.p.RobotDao;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Account;
import com.gryphpoem.game.zw.resource.domain.p.Robot;
import com.gryphpoem.game.zw.resource.domain.s.StaticBehaviorTree;
import com.gryphpoem.game.zw.resource.domain.s.StaticBtreeNode;
import com.gryphpoem.game.zw.resource.domain.s.StaticIniLord;
import com.gryphpoem.game.zw.resource.pojo.robot.RobotRecord;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @Description 机器人数据管理类
 * @date 创建时间：2017年9月20日 下午12:02:07
 */
@Component
public class RobotDataManager {

    @Autowired
    private RobotDao robotDao;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    private Map<Long, Robot> robotMap;

    private Map<Long, Robot> runtimeAddRobot;

    private AtomicInteger robotCount = new AtomicInteger(0);

    // 即将分配区域的机器人队列
    private LinkedBlockingQueue<Robot> areaRobotsQue;

    // key: areaId value: 需要加入该区域的机器人
    //  LinkedList<Robot> areaRobot;

    // key: areaId value: 该区域的机器人
    private Map<Integer, LinkedList<Robot>> areaRobotCache;

    public void load() {
        robotMap = robotDao.selectRobotMap();
    }

    public void init() {
        // 如果没有机器人配置信息，检查是否需要创建相关机器人数据
        if (CheckNull.isEmpty(robotMap)) {
            createRobots(RobotConstant.ROBOT_INIT);
        }
        if (CheckNull.isEmpty(areaRobotCache)) {
            areaRobotCache = new HashMap<>();
        }
        LinkedList<Robot> robots;
        for (Robot robot : robotMap.values()) {
            int posArea = robot.getPosArea();
            if (posArea == RobotConstant.ROBOT_NOT_HAVE_POS) continue;
            robots = areaRobotCache.get(posArea);
            if (robots == null) {
                robots = new LinkedList<>();
                areaRobotCache.put(posArea, robots);
            }
            robots.add(robot);
        }
        // 初始化所有机器人的行为树（暂不考虑使用共享树）
        initRobotTree(robotMap);
    }

    /**
     * 创建机器人角色数据
     *
     * @param robotInitList 需要创建的机器人数量和对应的行为树id
     */
    private void createRobots(List<List<Integer>> robotInitList) {
        if (!CheckNull.isEmpty(robotInitList)) {
            LogUtil.robot("开始创建机器人数据, robotInitList:" + robotInitList);
            for (List<Integer> list : robotInitList) {
                // list第一个数值表示行为树id，第二个数值表示对应的机器人数量
                if (list.size() >= 2) {
                    for (int i = 0; i < list.get(1); i++) {
                        createRobot(list.get(0));
                    }
                }
            }
        }
    }

    /**
     * 运行时添加机器人
     *
     * @param count
     * @param treeId
     * @param areaId
     */
    public void runtimeCreateRobots(int count, int treeId, int areaId) {
        if (CheckNull.isEmpty(runtimeAddRobot)) {
            runtimeAddRobot = new HashMap<>();
        } else {
            runtimeAddRobot.clear();
        }
        for (int i = 0; i < count; i++) {
            Player newPlayer = craeteRobotAccount();
            newPlayer.isRobot = true;
            // 创建角色并返回角色id
            Long roleId = createRobotRole(newPlayer);
            if (null != roleId && roleId > 0) {
                Robot robot = new Robot();
                robot.setRoleId(roleId);
                robot.setTreeId(treeId);
                robot.setRobotState(1);
                robot.setPosArea(areaId);
                robotDao.insertRobot(robot);
                runtimeAddRobot.put(roleId, robot);
            }
        }
        initRobotTree(runtimeAddRobot); // 新加入的机器人初始化行为树
        LinkedList robots = (LinkedList) runtimeAddRobot.values();
        areaRobotsQue.addAll(robots);
    }

    /**
     * 创建机器人帐号和角色
     *
     * @return
     */
    private void createRobot(int treeId) {
        // 创建帐号
        Player newPlayer = craeteRobotAccount();
        newPlayer.isRobot = true;
        // 创建角色并返回角色id
        Long roleId = createRobotRole(newPlayer);
        if (null != roleId && roleId > 0) {
            Robot robot = new Robot();
            robot.setRoleId(roleId);
            robot.setTreeId(treeId);
            robot.setRobotState(RobotConstant.ROBOT_STATE_NORMAL);
            robotDao.insertRobot(robot);
            robotMap.put(roleId, robot);
        }
    }

    /**
     * 创建机器人角色
     *
     * @param newPlayer
     * @return
     */
    private Long createRobotRole(Player newPlayer) {
        // 随机阵营
        int camp = playerDataManager.getSmallCamp();
        // 随机一个姓名
        String nick = playerDataManager.getFreeManName();
        if (playerDataManager.takeNick(nick)) {
            StaticIniLord ini = StaticIniDataMgr.getLordIniData();
            newPlayer.account.setCreated(LoginConstant.ROLE_CREATED);
            newPlayer.account.setCreateDate(new Date());
            newPlayer.lord.setPortrait(ini.getPortrait());
            newPlayer.lord.setSex(ini.getSex());
            newPlayer.lord.setNick(nick);
            newPlayer.lord.setCamp(camp);
            newPlayer.lord.setPower(ini.getPower());
            newPlayer.lord.setRanks(ini.getRanks());

            if (playerDataManager.createFullPlayer(newPlayer)) {
                Player player = playerDataManager.getNewPlayer(newPlayer.roleId);
                if (player == null) {
                    LogUtil.robot("changeNewPlayer {" + newPlayer.roleId + "} error");
                    return null;
                }

                playerDataManager.removeNewPlayer(newPlayer.roleId);
                playerDataManager.addPlayer(newPlayer);
                Account account = newPlayer.account;
                LogLordHelper.logRegister(account);

                // 发送注册初始奖励
                rewardDataManager.sendReward(player, Constant.REGISTER_REWARD, AwardFrom.REGISTER_REWARD);
                int now = TimeHelper.getCurrentSecond();
                if (!Constant.MAIL_FOR_CREATE_ROLE.isEmpty()) {
                    for (List<Integer> param : Constant.MAIL_FOR_CREATE_ROLE) {
                        if (param.size() > 1) {
                            // 附件内容
                            List<CommonPb.Award> awardList = new ArrayList<CommonPb.Award>();
                            Integer item_type = param.get(1);
                            Integer item_id = param.get(2);
                            Integer item_num = param.get(3);
                            CommonPb.Award en = PbHelper.createAwardPb(item_type, item_id, item_num);
                            awardList.add(en);
                            mailDataManager.sendAttachMail(player, awardList, param.get(0), AwardFrom.CREATE_ROLE_AWARD, now);
                        } else {
                            mailDataManager.sendNormalMail(player, param.get(0), now);
                        }
                    }
                }

                // 记录阵营人数
                playerDataManager.campRoleNumArr[camp]++;
            } else {
                newPlayer.account.setCreated(LoginConstant.ROLE_NOT_CREATE);
                LogUtil.robot("createFullPlayer {" + newPlayer.roleId + "} error");
                return null;
            }
        }
        return newPlayer.roleId;
    }

    /**
     * 创建并返回机器人帐号
     *
     * @return
     */
    private Player craeteRobotAccount() {
        int serverId = DataResource.ac.getBean(ServerSetting.class).getServerID();
        Long minAccountKey = accountDao.selectMinAccountKey();
        // 机器人的accountKey使用负值，避免与玩家的帐号服id重复
        long nextRobotAccountKey = (minAccountKey == null || minAccountKey > 0) ? -1 : minAccountKey - 1;
        Account account = new Account();
        account.setServerId(serverId);
        account.setAccountKey(nextRobotAccountKey);
        account.setPlatNo(Constant.SELF_PLAT_NO);
        account.setPlatId(RobotConstant.DEFAULT_PLAT_ID);
        account.setDeviceNo(RobotConstant.DEFAULT_DEVICE_NO);
        account.setLoginDays(1);
        account.setCreateDate(new Date());
        account.setLoginDate(new Date());

        Player robot = playerDataManager.createPlayer(account);
        LogLordHelper.logRegister(account);
        return robot;
    }

    /**
     * 初始化机器人相关数据
     */
    private void initRobotTree(Map<Long, Robot> robotMap) {
        if (!CheckNull.isEmpty(robotMap)) {
            Player player;
            HaiBehaviorTree tree;
            StaticBehaviorTree sbt;
            List<StaticBtreeNode> nodeConfigList;
            int now = TimeHelper.getCurrentSecond();
            for (Robot robot : robotMap.values()) {
                sbt = StaticRobotDataMgr.getTreeMap().get(robot.getTreeId());
                if (!sbt.isValid()) {
                    LogUtil.robot("机器人对应的行为树无效， robot:" + robot);
                    continue;
                }

                player = playerDataManager.getPlayer(robot.getRoleId());
                if (null == player) {
                    LogUtil.robot("机器人对应的角色id不存在, roleId:" + robot.getRoleId());
                } else {
                    nodeConfigList = StaticRobotDataMgr.getTreeNodeMap().get(sbt.getTreeId());
                    tree = HaiBtreeManager.createBehaviorTree(sbt, nodeConfigList);
                    tree.setRobotRoleId(robot.getRoleId());
                    robot.setDefaultTree(tree);
                    robot.setNextTick(++now);// 设置机器人下一次遍历行为树的时间，分开遍历，避免一次处理太多机器人逻辑，导致占用过多资源

                    player.isRobot = true;
                    player.robotRecord = new RobotRecord();
                    player.robotRecord.initData(now);// 初始化一些机器人记录量
                }
            }
        }

    }

    /**
     * 保存所有机器人数据
     */
    public void saveAllRobot() {
        for (Robot robot : robotMap.values()) {
            try {
                robotDao.updateRobot(robot);
            } catch (Exception e) {
                LogUtil.robot(e, "保存机器人数据出错, robot:", robot);
            }
        }
    }

    /**
     * 减少制定类型的有效机器人的数量
     *
     * @param treeId
     * @param reduceCount
     */
    public void reduceValidRobot(int treeId, int reduceCount) {
        if (reduceCount > 0) {
            int temp = 0;
            Iterator<Robot> its = robotMap.values().iterator();
            while (its.hasNext()) {
                Robot robot = its.next();
                if (robot.getTreeId() == treeId) {
                    temp++;
                    robot.setRobotState(RobotConstant.ROBOT_STATE_INVALID);
                }
                if (temp >= reduceCount) {
                    break;
                }
            }
        }
    }

    /**
     * 修改机器人状态
     *
     * @param roleId
     * @param state
     */
    public void modifyRobotState(long roleId, int state) {
        Robot robot = robotMap.get(roleId);
        if (null != robot) {
            robot.setRobotState(state);
        }
    }

    /**
     * 获取未分配坐标的机器人
     *
     * @return
     */
    public List<Robot> getNonePosRobotList() {
        List<Robot> nonePosRobot = new LinkedList<>();
        for (Robot robot : getRobotMap().values()) {
            if (!robot.hasPos()) {
                nonePosRobot.add(robot);
            }
        }
        return nonePosRobot;
    }

    public Map<Long, Robot> getRobotMap() {
        return robotMap;
    }

    public boolean isRobot(long lordId) {
        return robotMap.containsKey(lordId);
    }

    public Map<Integer, LinkedList<Robot>> getAreaRobotCache() {
        return areaRobotCache;
    }

    public Robot getRobot(Long roleId) {
        return robotMap.get(roleId);
    }

    /**
     * 获取指定区域内所有的机器人
     *
     * @param serverId
     * @return
     */
    public LinkedList getAreaRobotsCache(int serverId) {
        if (CheckNull.isEmpty(areaRobotCache)) {
            areaRobotCache = new HashMap<>();
        }
        LinkedList<Robot> robots = areaRobotCache.get(serverId);
        if (CheckNull.isEmpty(robots)) {
            robots = new LinkedList<>();
            areaRobotCache.put(serverId,robots);
        }
        return robots;
    }

    /**
     * 获取即将分配坐标的机器人队列
     *
     * @param serverId
     * @return
     */
    public LinkedBlockingQueue getAreaRobotsQue() {
        if (CheckNull.isEmpty(areaRobotsQue)) {
            areaRobotsQue = new LinkedBlockingQueue<Robot>();
        }
        return areaRobotsQue;
    }

    /**
     * 获取指定区域内拥有外在行为的机器人
     *
     * @param areaId
     * @return
     */
    public List getAreaRobotsHaveExternalBehavior(int areaId) {
        LinkedList<Robot> areaRobotsCache = getAreaRobotsCache(areaId);
        return areaRobotsCache
                .stream()
                .filter(robot -> robot.getActionType() == RobotConstant.ROBOT_EXTERNAL_BEHAVIOR)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定区域内拥有内在行为的机器人
     *
     * @param areaId
     * @return
     */
    public List getAreaRobotsHaveInnerBehavior(int areaId) {
        LinkedList<Robot> areaRobotsCache = getAreaRobotsCache(areaId);
        return areaRobotsCache
                .stream()
                .filter(robot -> robot.getActionType() == RobotConstant.ROBOT_INNER_BEHAVIOR)
                .collect(Collectors.toList());
    }
}
