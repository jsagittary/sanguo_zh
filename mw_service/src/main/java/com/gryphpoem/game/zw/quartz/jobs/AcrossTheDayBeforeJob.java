package com.gryphpoem.game.zw.quartz.jobs;

import org.quartz.JobExecutionContext;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName AcrossTheDayBeforeJob.java
 * @Description
 * @author QiuKun
 * @date 2018年1月9日
 */
public class AcrossTheDayBeforeJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        LogUtil.debug("------------转点前的处理start-------------");
        ActivityService activityService = DataResource.ac.getBean(ActivityService.class);
        String keyName = context.getJobDetail().getKey().getName();
        activityService.sendUnrewardedMailForCleanDay(keyName);
        LogUtil.debug("------------转点前的处理end-------------");
    }
}
