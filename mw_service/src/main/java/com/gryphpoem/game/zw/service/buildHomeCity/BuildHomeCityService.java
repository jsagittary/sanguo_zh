package com.gryphpoem.game.zw.service.buildHomeCity;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticDataMgr;
import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayQueue;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityCell;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityFoundation;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.ExploreQue;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.ReclaimQue;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 主城建设(包括：探索迷雾、开垦地基、摆放建筑)
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/5 10:20
 */
@Service
public class BuildHomeCityService implements DelayInvokeEnvironment {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private PlayerService playerService;

    /**
     * 探索迷雾
     */
    public GamePb1.ExploreRs exploreFog(long roleId, GamePb1.ExploreRq rq) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int cellId = rq.getCellId();
        int scoutIndex = rq.getScoutIndex();
        // 获取目标迷雾格子配置
        StaticHomeCityCell staticHomeCityCell = StaticDataMgr.getStaticHomeCityCellById(cellId);
        if (staticHomeCityCell == null) {
            throw new MwException(GameError.NO_CONFIG, String.format("探索主城迷雾区时, 未找到主城地图格配置, roleId:%s, cellId:%s", roleId, cellId));
        }
        // 校验探索条件
        Integer needLordLevel = staticHomeCityCell.getLevel();
        if (needLordLevel != null && player.lord.getLevel() < needLordLevel) {
            throw new MwException(GameError.INSUFFICIENT_LORD_LEVEL, String.format("探索主城迷雾区时, 未达到领主等级要求, roleId:%s, cellId:%s", roleId, cellId));
        }
        Integer scoutState = player.getScoutMap().get(scoutIndex);
        if (scoutState == 1) {
            throw new MwException(GameError.SCOUT_NOT_IDLE, String.format("探索主城迷雾区时, 侦察兵非空闲状态, roleId:%s, scoutIndex:%s", roleId, scoutIndex));
        }
        if (player.getMapCellData().containsKey(cellId)) {
            throw new MwException(GameError.MAP_CELL_ALREADY_EXPLORED, String.format("探索主城迷雾区时, 该迷雾区已被探索, roleId:%s, cellId:%s", roleId, cellId));
        }

        // 新增定时任务开始探索
        int now = TimeHelper.getCurrentSecond();
        int endTime = staticHomeCityCell.getExploreTime() + now;
        ExploreQue exploreQue = new ExploreQue(player.maxKey(), scoutIndex, cellId, staticHomeCityCell.getExploreTime(), endTime);
        player.exploreQue.put(scoutIndex, exploreQue); // 更新玩家探索队列
        player.getScoutMap().put(scoutIndex, 1); // 更新侦察兵状态
        // 添加探索结束后的延时任务
        DELAY_QUEUE.add(new BuildHomeCityDelayRun(1, endTime, cellId, scoutIndex, 0, player));

        GamePb1.ExploreRs.Builder builder = GamePb1.ExploreRs.newBuilder();
        builder.setExploreQue(exploreQue.creatExplorePb());
        return builder.build();
    }

    /**
     * 开垦地基<br>
     * 一个格子一个格子开垦, 地基所占的格子全部开垦完后, 则解锁地基
     */
    public GamePb1.ReclaimFoundationRs reclaimFoundation(long roleId, GamePb1.ReclaimFoundationRq rq) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int cellId = rq.getCellId();
        int farmerCount = rq.getFarmerCount();
        // 获取目标格子配置
        StaticHomeCityCell staticHomeCityCell = StaticDataMgr.getStaticHomeCityCellById(cellId);
        if (staticHomeCityCell == null) {
            throw new MwException(GameError.NO_CONFIG, String.format("开垦地基时, 未找到主城地图格配置, roleId:%s, cellId:%s", roleId, cellId));
        }
        // 开垦格子前, 需要先探索
        if (!player.getMapCellData().containsKey(cellId)) {
            throw new MwException(GameError.INSUFFICIENT_LORD_LEVEL, String.format("开垦地基时, 该格子还未解锁, roleId:%s, cellId:%s", roleId, cellId));
        }
        if (player.getMapCellData().get(cellId) == 1) {
            throw new MwException(GameError.INSUFFICIENT_LORD_LEVEL, String.format("开垦地基时, 该格子已被开垦, roleId:%s, cellId:%s", roleId, cellId));
        }
        // 校验是否有空闲农民
        if (player.getIdleFarmerCount() < farmerCount) {
            throw new MwException(GameError.INSUFFICIENT_LORD_LEVEL, String.format("开垦地基时, 没有足够空闲的农民, roleId:%s, cellId:%s", roleId, cellId));
        }

        // 新增定时任务开始开垦
        int now = TimeHelper.getCurrentSecond();
        int endTime = staticHomeCityCell.getExploreTime() + now;
        int reclaimIndex = player.reclaimQue.keySet().stream().max(Integer::compareTo).orElse(0);
        ReclaimQue reclaimQue = new ReclaimQue(player.maxKey(), reclaimIndex + 1, farmerCount, cellId, staticHomeCityCell.getReclaimTime(), endTime);
        player.reclaimQue.put(reclaimIndex + 1, reclaimQue); // 更新玩家开垦队列
        player.subIdleFarmerCount(farmerCount); // 更新空闲农民数量
        // 添加探索结束后的延时任务
        DELAY_QUEUE.add(new BuildHomeCityDelayRun(1, endTime, cellId, 0, farmerCount, player));

        GamePb1.ReclaimFoundationRs.Builder builder = GamePb1.ReclaimFoundationRs.newBuilder();
        builder.setReclaimQue(reclaimQue.creatExplorePb());
        return builder.build();
    }

    /**
     * 建筑摆放(交换建筑位置)
     */
    public GamePb1.SwapBuildingLocationRs swapBuildingLocation(long roleId, GamePb1.SwapBuildingLocationRq rq) {
        // 校验功能解锁条件(如果有)

        // 校验建筑

        // 校验地基

        GamePb1.SwapBuildingLocationRs.Builder builder = GamePb1.SwapBuildingLocationRs.newBuilder();
        return builder.build();
    }

    private DelayQueue<BuildHomeCityDelayRun> DELAY_QUEUE = new DelayQueue<>(this);

    @Override
    public DelayQueue getDelayQueue() {
        return DELAY_QUEUE;
    }

    /**
     * 迷雾探索结束时要做的事
     *
     * @param cellId
     * @param player
     * @param scoutIndex
     */
    public void doAtExploreEnd(Integer cellId, int scoutIndex, Player player) {
        // 玩家新增解锁的地图格子
        player.getMapCellData().put(cellId, 0);
        // 恢复探索的侦察兵状态为空闲, 并向客户端同步
        player.getScoutMap().put(scoutIndex, 0);
        playerDataManager.syncRoleInfo(player);
        // 向客户端同步探索完成的格子
        GamePb1.SynExploreOrReclaimRs.Builder builder = GamePb1.SynExploreOrReclaimRs.newBuilder();
        builder.setCellId(cellId);
        BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SynExploreOrReclaimRs.EXT_FIELD_NUMBER, GamePb1.SynExploreOrReclaimRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    /**
     * 地基开垦结束时要做的事
     *
     * @param cellId
     * @param farmerCnt
     * @param player
     */
    public void doAtReclaimFoundationEnd(Integer cellId, int farmerCnt, Player player) {
        // 获取玩家新增解锁的地基id, 解锁地基需要该地基所占的格子全部被开垦完成
        List<Integer> foundationIdList = StaticDataMgr.getFoundationIdListByCellId(cellId);
        List<Integer> unlockFoundationIdList = new ArrayList<>();
        List<Integer> reclaimedCellIdList = (List<Integer>) player.getMapCellData().entrySet().stream().filter(entry -> entry.getValue() == 1).map(Map.Entry::getKey);
        for (Integer foundationId : foundationIdList) {
            StaticHomeCityFoundation staticFoundation = StaticDataMgr.getStaticHomeCityFoundationById(foundationId);
            if (reclaimedCellIdList.containsAll(staticFoundation.getCellList())) {
                unlockFoundationIdList.add(foundationId);
            }
        }
        // 玩家新增解锁的地基
        player.getFoundationData().addAll(foundationIdList);
        // 释放开垦的农民, 并向客户端同步
        player.addIdleFarmerCount(farmerCnt);
        playerDataManager.syncRoleInfo(player);
        // 向客户端同步开垦出的地基
        GamePb1.SynExploreOrReclaimRs.Builder builder = GamePb1.SynExploreOrReclaimRs.newBuilder();
        builder.setCellId(cellId);
        builder.addAllFoundationId(unlockFoundationIdList);
        BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SynExploreOrReclaimRs.EXT_FIELD_NUMBER, GamePb1.SynExploreOrReclaimRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }


}
