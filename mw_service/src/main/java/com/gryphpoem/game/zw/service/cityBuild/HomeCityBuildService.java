package com.gryphpoem.game.zw.service.cityBuild;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticDataMgr;
import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayQueue;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityCell;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityFoundation;
import com.gryphpoem.game.zw.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 主城建设(包括：探索迷雾、开垦地基、摆放建筑)
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/5 10:20
 */
@Service
public class HomeCityBuildService implements DelayInvokeEnvironment {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private PlayerService playerService;

    /**
     * 探索迷雾
     */
    public GamePb1.ExploreFogRs exploreFog(long roleId, GamePb1.ExploreFogRq rq) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int cellId = rq.getCellId();
        int scoutIndex = rq.getScoutIndex();
        // 获取目标迷雾格子配置
        StaticHomeCityCell staticHomeCityCell = StaticDataMgr.getStaticHomeCityCellById(cellId);
        if (staticHomeCityCell == null) {
            throw new MwException(GameError.NO_CONFIG, String.format("未找到主城地图格配置, roleId:%s, cellId:%s", roleId, cellId));
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

        // 新增定时任务开始探索
        // 任务开始到结束期间，向客户端同步倒计时
        // 任务结束后，向客户端同步解锁的格子，是否推送

        GamePb1.ExploreFogRs.Builder builder = GamePb1.ExploreFogRs.newBuilder();
        return builder.build();
    }

    /**
     * 开垦地基
     */
    public GamePb1.DigUpFoundationRs reclaimFoundation(long roleId, GamePb1.DigUpFoundationRq rq) {
        // 获取目标地基配置

        // 校验地基所占格子(迷雾)是否已全部解锁

        // 校验开垦消耗(如果有)

        // 加入定时任务开始开垦(向客户端同步开垦状态)

        GamePb1.DigUpFoundationRs.Builder builder = GamePb1.DigUpFoundationRs.newBuilder();
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

    private DelayQueue<ExploreFogDelayRun> DELAY_QUEUE = new DelayQueue<>(this);

    @Override
    public DelayQueue getDelayQueue() {
        return DELAY_QUEUE;
    }

    /**
     * 迷雾探索结束时要做的事
     *
     * @param startTime
     * @param staticHomeCityCell
     * @param player
     */
    public void doAtExploreFogEnd(int startTime, StaticHomeCityCell staticHomeCityCell, Player player) {

    }

    /**
     * 地基开垦结束时要做的事
     *
     * @param startTime
     * @param staticHomeCityFoundation
     * @param player
     */
    public void doAtReclaimFoundationEnd(int startTime, StaticHomeCityFoundation staticHomeCityFoundation, Player player) {

    }


}
