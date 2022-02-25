package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticAcquisitionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTaskDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb4.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticAcquisition;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticTask;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.pojo.world.Area;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * @ClassName AcquisitionService.java
 * @Description 个人资源点相关
 * @author TanDonghai
 * @date 创建时间：2017年5月9日 下午4:42:31
 *
 */
@Service
public class AcquisitionService {
    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private FactoryService factoryService;

    @Autowired
    private TaskDataManager taskDataManager;

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private WorldService worldService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityTriggerService activityTriggerService;

    /**
     * 获取玩家个人资源点数据
     * 
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetAcquisitionRs getAcquisition(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检查玩家是否可以刷新个人资源点
        checkRoleHaveAcquisition(player.lord);
        refreshRoleAcquisition(player);

        GetAcquisitionRs.Builder builder = GetAcquisitionRs.newBuilder();
        for (Entry<Integer, Integer> entry : player.acquisiteReward.entrySet()) {
            builder.addReward(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue()));
        }
        builder.addAllCollect(player.acquisiteQue);
        return builder.build();
    }

    /**
     * 检查玩家是否能开启个人资源点，只有在最低级分区时可以
     * 
     * @param lord
     * @throws MwException
     */
    public void checkRoleHaveAcquisition(Lord lord) throws MwException {
        if (!roleHaveAcquisition(lord.getPos())) {
            throw new MwException(GameError.ACQUISITE_MAX_QUE.getCode(), "个人资源点未开启, roleId:", lord.getLordId(),
                    ", pos:", lord.getPos());
        }
    }

    /**
     * 角色是否可以刷新个人资源点
     * 
     * @param pos
     * @return
     */
    public boolean roleHaveAcquisition(int pos) {
        int area = MapHelper.getAreaIdByPos(pos);
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(area);
        if (null == staticArea) {
            return false;
        }
        Area globalArea = worldDataManager.getAreaByAreaId(area);
        if (globalArea == null) {
            return false;
        }
        LogUtil.debug("roleAcquisition,pos=" + pos + ",area=" + area + ",status=" + staticArea.getOpenOrder()
                + ",status=" + globalArea.getStatus());
        // return staticArea.getOpenOrder() == WorldConstant.AREA_ORDER_1;
        return globalArea.getStatus() != WorldConstant.AREA_STATUS_CLOSE;
    }

    /**
     * 刷新玩家的个人资源点数据
     * 
     * @param player
     */
    private void refreshRoleAcquisition(Player player) {
        int today = TimeHelper.getCurrentDay();
        if (today != player.acquisiteDate && player.acquisiteQue.isEmpty()) {
            player.acquisiteReward.clear();
            player.acquisiteQue.clear();
            player.acquisiteDate = today;
        }
    }

    /**
     * 个人资源点开始采集
     * 
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public BeginAcquisiteRs beginAcquisite(long roleId, BeginAcquisiteRq req) throws MwException {
        int id = req.getId();
        int pos = req.getPos();
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticAcquisition sa = StaticAcquisitionDataMgr.getAcquisitionById(id);
        if (null == sa) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "个人采集点开始采集，未找到配置, roleId:", roleId, ", id:", id);
        }
        // 返回消息
        BeginAcquisiteRs.Builder builder = BeginAcquisiteRs.newBuilder();
        // 流寇任务
        if (sa.getType() == 6) {
            int taskId = req.getTaskId();
            Task task = player.majorTasks.get(taskId);
            if (task == null) {
                LogUtil.debug("打任务流寇,任务为null taskId:", taskId + " ,roleId:", roleId);
                return builder.build();
            }

            // 任务是打流寇
            StaticTask staticTask = StaticTaskDataMgr.getTaskById(taskId);
            if (staticTask == null || staticTask.getCond() != TaskType.COND_BANDIT_LV_CNT
                    || staticTask.getCondId() == 0) {
                LogUtil.debug("打任务流寇,任务配置为null taskId:", taskId + " ,roleId:", roleId);
                return builder.build();
            }
            Task preTask = player.majorTasks.get(staticTask.getTriggerId());
            if (task.getStatus() == TaskType.TYPE_STATUS_UNFINISH && (staticTask.getTriggerId() == 0
                    || (preTask != null && preTask.getStatus() > TaskType.TYPE_STATUS_UNFINISH))) {
                worldService.attackPos4Task(roleId, pos, staticTask.getCondId(), req.getHeroIdList());
            }
            return builder.build();
        }

        // 检查玩家是否可以刷新个人资源点
        checkRoleHaveAcquisition(player.lord);
        refreshRoleAcquisition(player);

        // 检查采集队列
        List<TwoInt> collectQue = player.acquisiteQue;
        if (collectQue.size() >= WorldConstant.ACQUISITION_MAX_QUE) {
            throw new MwException(GameError.ACQUISITE_MAX_QUE.getCode(), "没有空闲的采集队列, roleId:", roleId, ", queue:",
                    collectQue.size(), ", max:", WorldConstant.ACQUISITION_MAX_QUE);
        }

        // 获取采集次数
        Map<Integer, Integer> rewardMap = player.acquisiteReward;
        Integer rewardNum = rewardMap.get(id);
        if (null == rewardNum) {
            rewardNum = 0;
        }
        // 检查采集次数是否达到上限
        if (rewardNum >= sa.getMaxNum()) {
            throw new MwException(GameError.ACQUISITE_MAX_REWARD.getCode(), "该资源已采集达到上限, roleId:", roleId,
                    ", rewardNum:", rewardNum, ", max:", sa.getMaxNum());
        }

        // 检查并扣除消耗
        rewardDataManager.checkAndSubPlayerRes(player, sa.getCost(), AwardFrom.ACQUISITE);

        // 开启并记录采集队列
        int endTime = TimeHelper.getCurrentSecond() + sa.getCollectTime();
        collectQue.add(PbHelper.createTwoIntPb(id, endTime));

        builder.setEndTime(endTime);
        return builder.build();
    }

    /**
     * 领取个人资源点奖励
     * 
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public AcquisiteRewradRs acquisiteRewrad(long roleId, int id) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检查玩家是否可以刷新个人资源点
        checkRoleHaveAcquisition(player.lord);
        refreshRoleAcquisition(player);

        StaticAcquisition sa = StaticAcquisitionDataMgr.getAcquisitionById(id);
        if (null == sa) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "个人采集点开始采集，未找到配置, roleId:", roleId, ", id:", id);
        }

        // 检查采集队列中是否有该资源
        List<TwoInt> collectQue = player.acquisiteQue;
        TwoInt que = null;
        for (TwoInt twoInt : collectQue) {
            if (twoInt.getV1() == id) {
                que = twoInt;
                break;
            }
        }
        if (que == null) {
            throw new MwException(GameError.ACQUISITE_NOT_BEGIN.getCode(), "采集队列不存在, roleId:", roleId, ", id:", id);
        }

        // 检查采集是否结束
        int now = TimeHelper.getCurrentSecond();
        if (now < que.getV2()) {
            throw new MwException(GameError.ACQUISITE_NOT_END.getCode(), "采集队列未结束, roleId:", roleId, ", id:", id,
                    ", endTime:", que.getV2(), ", now:", now);
        }

        // 记录采集次数
        Map<Integer, Integer> rewardMap = player.acquisiteReward;
        Integer rewardNum = rewardMap.get(id);
        if (null == rewardNum) {
            rewardNum = 1;
        } else {
            rewardNum++;
        }
        rewardMap.put(id, rewardNum);

        // 清除采集队列
        collectQue.remove(que);

        // 发送奖励
        rewardDataManager.sendReward(player, sa.getReward(), AwardFrom.ACQUISITE_REWARD);

        // 返回消息
        AcquisiteRewradRs.Builder builder = AcquisiteRewradRs.newBuilder();
        builder.setReward(rewardNum);
        return builder.build();
    }

    /**
     * 使用免费加速
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public UseFreeSpeedRs useFreeSpeed(long roleId, UseFreeSpeedRq req) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int keyId = req.getKeyId();
        int freeType = req.getFreeType();
        BuildQue buildQue = player.buildQue.get(keyId);
        UseFreeSpeedRs.Builder builder = UseFreeSpeedRs.newBuilder();
        if (null == buildQue) {
            ArmQue armQue = getArmQueById(player, keyId);
            if (null == armQue) {
                throw new MwException(GameError.FREE_SPEED_NOT_FOUND.getCode(), "队列免费加速，队列未找到, roleId:", roleId,
                        ", keyId:", keyId);
            }

            if (!armQue.haveFreeSpeed()) {
                throw new MwException(GameError.QUEUE_NO_FREE_SPEED.getCode(), "队列免费加速，该队列没有免费加速, roleId:", roleId,
                        ", keyId:", keyId, ", queFreeType:", armQue.getFree());
            }

            if (armQue.getFree() != freeType) {
                throw new MwException(GameError.QUEUE_FREE_TYPE_ERR.getCode(), "队列免费加速，免费加速加速类型错误, roleId:", roleId,
                        ", keyId:", keyId, ", queFreeType:", armQue.getFree(), ", freeType:", freeType);
            }

            // 更新队列结束时间
            factoryService.processArmyQue(player, keyId, armQue.getParam());

            Factory factory = player.factory.get(keyId);
            for (ArmQue que : factory.getAddList()) {
                builder.addArmQue(PbHelper.createArmQuePb(que));
            }
        } else {
            if (!buildQue.haveFreeSpeed()) {
                throw new MwException(GameError.QUEUE_NO_FREE_SPEED.getCode(), "队列免费加速，该队列没有免费加速, roleId:", roleId,
                        ", keyId:", keyId, ", queFreeType:", buildQue.getFree());
            }

            if (buildQue.getFree() != freeType) {
                throw new MwException(GameError.QUEUE_FREE_TYPE_ERR.getCode(), "队列免费加速，免费加速加速类型错误, roleId:", roleId,
                        ", keyId:", keyId, ", queFreeType:", buildQue.getFree(), ", freeType:", freeType);
            }

            // 更新队列结束时间
            buildQue.useFreeSpeed();

            int buildingId = buildQue.getPos();
            int buildingType = buildQue.getBuildingType();

            int buildLevel = BuildingDataManager.getBuildingLv(buildingId, player);
            // 使用免费加速, 建筑升级满足触发条件
//            int giftId = activityDataManager.checkBuildTrigger(player, buildingType, buildLevel);
//            if (giftId != 0) {
//                try {
//                    activityService.checkTriggerGiftSyncByGiftId(giftId, player);
//                } catch (MwException e) {
//                    LogUtil.error(e);
//                }
//            }
            // 建筑升级触发礼包
            activityTriggerService.buildLevelUpTriggerGift(player, buildingType, buildLevel);

            builder.setBuildQue(PbHelper.createBuildQuePb(buildQue));
            taskDataManager.updTask(player, TaskType.COND_FREE_CD, 1, buildingType);
        }

        return builder.build();
    }

    /**
     * 根据建筑id获取募兵队列
     * 
     * @param player
     * @param id
     * @return
     */
    private ArmQue getArmQueById(Player player, int id) {
        Factory factory = player.factory.get(id);
        if (factory == null || factory.getAddList().isEmpty()) {
            return null;
        }
        return factory.getAddList().get(0);
    }

    /**
     * 获取成就
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetStatusRs getStatus(Long roleId, GetStatusRq req) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int index = req.getIndex();
        GetStatusRs.Builder builder = GetStatusRs.newBuilder();
        if (index == 1) {
            Integer cnt = player.trophy.get(TrophyConstant.TROPHY_1);
            cnt = cnt != null ? cnt : 0;
            builder.setStatus(cnt);
        }

        return builder.build();
    }

    /**
     * 获取vip特权次数
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetVipCntRs getVipCnt(Long roleId, GetVipCntRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetVipCntRs.Builder builder = GetVipCntRs.newBuilder();
        Common common = player.common;
        builder.setActCnt(common.getBuyAct());
        builder.setRetreatCnt(common.getRetreat());
        return builder.build();
    }

    public void autoAcquisition(Player player) {
        int now = TimeHelper.getCurrentSecond();

        // 如果有采集队列已完成，收取
        gainAcquisiton(player, now);

        // 刷新采集相关数据
        refreshRoleAcquisition(player);

        // 检查是否有空闲的采集队列
        List<TwoInt> collectQue = player.acquisiteQue;
        if (collectQue.size() >= WorldConstant.ACQUISITION_MAX_QUE) {
            return;
        }

        // 获取有效的可采集物品
        StaticAcquisition sa = getValidAcquisition(player);
        if (null == sa) {
            return;
        }

        // 检查并扣除消耗
        try {
            rewardDataManager.checkAndSubPlayerRes(player, sa.getCost(), AwardFrom.ACQUISITE);
        } catch (MwException e) {
            // 不应该走到这里
            LogUtil.error(e, "自动开始个人资源点采集出错， roleId:", player.roleId, ", StaticAcquisition:", sa);
        }

        // 开启并记录采集队列
        int endTime = TimeHelper.getCurrentSecond() + sa.getCollectTime();
        collectQue.add(PbHelper.createTwoIntPb(sa.getId(), endTime));
    }

    /**
     * 收取已完成的采集点资源，并发送奖励
     * 
     * @param player
     * @param now
     */
    private void gainAcquisiton(Player player, int now) {
        StaticAcquisition sa;
        Iterator<TwoInt> its = player.acquisiteQue.iterator();
        while (its.hasNext()) {
            TwoInt twoInt = its.next();
            if (twoInt.getV2() <= now) {
                // 记录采集次数
                incrementAcquisitonRewardCount(player, twoInt.getV1());

                // 清除采集队列
                its.remove();

                // 发送奖励
                sa = StaticAcquisitionDataMgr.getAcquisitionById(twoInt.getV1());
                rewardDataManager.sendReward(player, sa.getReward(), AwardFrom.ACQUISITE_REWARD);

                //  如果奖励加速类，给机器人发送对应时间，这个作为后期的一个优化方向
            }
        }
    }

    /**
     * 增加个人资源点采集次数
     * 
     * @param player
     * @param acquisitionId
     */
    private void incrementAcquisitonRewardCount(Player player, int acquisitionId) {
        Map<Integer, Integer> rewardMap = player.acquisiteReward;
        Integer rewardNum = rewardMap.get(acquisitionId);
        if (null == rewardNum) {
            rewardNum = 1;
        } else {
            rewardNum++;
        }
        rewardMap.put(acquisitionId, rewardNum);
    }

    /**
     * 获取有效的可采集物品
     * 
     * @param player
     * @return
     */
    private StaticAcquisition getValidAcquisition(Player player) {
        Optional<StaticAcquisition> acquisition = StaticAcquisitionDataMgr.getAcquisitionMap().values().stream()
                .filter(sa -> acquisitionCanDo(player, sa)).min((sa1, sa2) -> (sa1.getId() - sa2.getId()));
        return acquisition.isPresent() ? acquisition.get() : null;
    }

    /**
     * 采集物是否还可以采集
     * 
     * @param player
     * @param acquisition
     * @return
     */
    private boolean acquisitionCanDo(Player player, StaticAcquisition acquisition) {
        Integer rewardNum = player.acquisiteReward.get(acquisition.getId());
        if (null != rewardNum && rewardNum >= acquisition.getMaxNum()) {
            return false;
        }

        try {
            rewardDataManager.checkPlayerResIsEnough(player, acquisition.getCost());
            return true;
        } catch (MwException e) {
            return false;
        }
    }

}
