package com.gryphpoem.game.zw.quartz.jobs.worldwar;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonAttackCityService;
import com.gryphpoem.game.zw.quartz.jobs.AbsMainLogicThreadJob;
import org.quartz.JobExecutionContext;

/**
 * Created by pengshuo on 2019/4/4 10:39
 * <br>Description: 每天中午12点和晚上24点，根据当前阵营占领的城市，结算一次阵营的军威值，
 * 当军威值达到要求数值时，可为阵营增加赛季积分
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class WorldWarDailyAttackCityCampIntegralJob extends AbsMainLogicThreadJob{

    /**
     * 世界争霸-每天中午12点和晚上24点，根据当前阵营占领的城市，结算一次阵营的军威
     * @param context
     */
    @Override
    protected void executeInMain(JobExecutionContext context) {
        try {
            WorldWarSeasonAttackCityService worldWarSeasonAttackCityService
                    = DataResource.ac.getBean(WorldWarSeasonAttackCityService.class);
            worldWarSeasonAttackCityService.addAttackCityCampIntegral();
        }catch (Exception e){
            LogUtil.error("世界争霸-每日12,24-统计玩家军威值 error",e.getMessage());
        }
    }

}
