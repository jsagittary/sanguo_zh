package com.gryphpoem.game.zw.quartz.jobs.worldwar;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonMomentOverService;
import com.gryphpoem.game.zw.quartz.jobs.AbsMainLogicThreadJob;
import org.quartz.JobExecutionContext;

/**
 * Created by pengshuo on 2019/4/4 10:39
 * <br>Description: 世界争霸-每日结束-发放玩家未领取奖励
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class WorldWarDailyJob extends AbsMainLogicThreadJob{

    /**
     * 世界争霸-每日结束-发放玩家未领取奖励
     * @param context
     */
    @Override
    protected void executeInMain(JobExecutionContext context) {
        try {
            WorldWarSeasonMomentOverService worldWarSeasonMomentOverService
                    = DataResource.ac.getBean(WorldWarSeasonMomentOverService.class);
            worldWarSeasonMomentOverService.dailyOver();
        }catch (Exception e){
            LogUtil.error("世界争霸-每日结束-发放玩家未领取奖励 error",e.getMessage());
        }
    }

}
