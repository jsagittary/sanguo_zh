package com.gryphpoem.game.zw.service.cityBuild;

import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.domain.Player;
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
public class MainCityBuildService {

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
        int gridId = rq.getGridId();
        int scoutIndex = rq.getScoutIndex();
        // 获取目标迷雾格子配置

        // 校验探索条件

        // 校验探索消耗(如果有)

        // 加入定时任务开始探索(向客户端同步探索状态)

        GamePb1.ExploreFogRs.Builder builder = GamePb1.ExploreFogRs.newBuilder();
        return builder.build();
    }

    /**
     * 开垦地基
     */
    public GamePb1.DigUpFoundationRs digUpFoundation(long roleId, GamePb1.DigUpFoundationRq rq) {
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

}
