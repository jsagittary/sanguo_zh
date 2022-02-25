package com.gryphpoem.game.zw.quartz.jobs;

import org.quartz.JobExecutionContext;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName SyncActPowerGiveJob.java
 * @Description
 * @author QiuKun
 * @date 2017年9月11日
 */
public class SyncActPowerGiveJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        ActivityService service = DataResource.ac.getBean(ActivityService.class);
        service.syncAllPlayerActPower();

    }

}
