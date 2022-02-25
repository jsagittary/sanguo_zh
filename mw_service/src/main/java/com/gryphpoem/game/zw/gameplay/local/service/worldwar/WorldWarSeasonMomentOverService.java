package com.gryphpoem.game.zw.gameplay.local.service.worldwar;

import com.gryphpoem.game.zw.core.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by pengshuo on 2019/4/3 15:02
 * <br>Description: 世界争霸 阶段结束所做处理(包含：赛季城市征战、赛季积分排行、
 * 赛季任务周奖励、赛季任务每日任务、赛季任务计时任务)
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Service
public class WorldWarSeasonMomentOverService {

    @Autowired
    private WorldWarSeasonIntegralRankService worldWarSeasonIntegralRankService;

    @Autowired
    private WorldWarSeasonAttackCityService worldWarSeasonAttackCityService;

    @Autowired
    private WorldWarSeasonWeekIntegralService worldWarSeasonWeekIntegralService;

    @Autowired
    private WorldWarSeasonDailyAttackTaskService worldWarSeasonDailyAttackTaskService;

    @Autowired
    private WorldWarSeasonDailyRestrictTaskService worldWarSeasonDailyRestrictTaskService;

    /**
     * 赛季-每日结束
     */
    public void dailyOver() {
        LogUtil.common("赛季任务-每日杀敌任务结束（发放玩家当天未领取奖励）");
        worldWarSeasonDailyAttackTaskService.dailyOverGiveAward();
        LogUtil.common("赛季任务-每日限定任务结束（发放玩家当天未领取奖励）");
        worldWarSeasonDailyRestrictTaskService.dailyOverGiveAward();
    }

    /**
     * 赛季-每周结束(每周日00:00结束)
     */
    public void weekOver() {
        LogUtil.common("赛季任务-当前周结束（发放玩家当前周未领取奖励）");
        worldWarSeasonWeekIntegralService.weekOverGiveAward();
    }

    /**
     * 赛季结束（世界争霸结束时间触发）
     */
    public void seasonOver() {
        // 赛季结束（发放世界阵营积分排行奖励）
        LogUtil.common("赛季结束（发放世界争霸积分（阵营排行、个人排行）排行奖励）");
        worldWarSeasonIntegralRankService.seasonOverGiveAward();
    }

    /**
     * 赛季结束（活动显示时间结束触发）
     */
    public void seasonOverClearIntegral() {
        // 赛季结束（发放世界阵营城市征战未领取奖励）
        LogUtil.common("赛季结束（发放玩家世界阵营城市征战未领取奖励）");
        worldWarSeasonAttackCityService.seasonOverGiveAward();
        // 赛季展示结束（清除玩家赛季数据和阵营赛季积分、军威值）
        LogUtil.common("赛季展示结束 清除玩家赛季数据和阵营赛季积分、军威值");
        worldWarSeasonIntegralRankService.seasonOverClearIntegral();
    }

}
