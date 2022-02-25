package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.service.WorldScheduleRankService;
import org.quartz.JobExecutionContext;

/**
 * Created by pengshuo on 2019/3/7 10:39
 * <br>Description: 世界进程 （2、3、4） （6、7、8） 阶段每天12 、21 点统计数据
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class WorldScheduleRankJob extends AbsMainLogicThreadJob{

    /**
     * 世界进程攻占数据统计
     * @param context
     */
    @Override
    protected void executeInMain(JobExecutionContext context) {
        try {
            WorldScheduleRankService worldScheduleRankService = DataResource.ac.getBean(WorldScheduleRankService.class);
            worldScheduleRankService.addCityWorldScheduleRankData();
        }catch (Exception e){
            LogUtil.error("世界进程（2、3、4） （6、7、8）阶段数据统计error",e.getMessage());
        }
    }

}
