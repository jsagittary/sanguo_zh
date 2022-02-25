package com.gryphpoem.game.zw.quartz.jobs.visitaltar;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.quartz.jobs.AbsMainLogicThreadJob;
import com.gryphpoem.game.zw.service.activity.RamadanVisitAltarService;
import org.quartz.JobExecutionContext;

/**
 * 拜访圣坛刷新定时器任务
 * @description:
 * @author: zhou jie
 * @time: 2021/3/30 0:08
 */
public class VisitAltarRefreshJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        try {
            DataResource.ac.getBean(RamadanVisitAltarService.class).refreshAndRemoveLogic(context);
        }catch (Exception e){
            LogUtil.error("拜访圣坛刷新定时器任务 error ", e);
        }
    }

}
