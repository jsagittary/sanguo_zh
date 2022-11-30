package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.service.buildHomeCity.BuildHomeCityService;
import org.quartz.JobExecutionContext;

/**
 * 土匪刷新定时任务
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/30 17:05
 */
public class RefreshBanditJob extends AbsMainLogicThreadJob{

    @Override
    protected void executeInMain(JobExecutionContext context) {
        LogUtil.debug("------------RefreshBanditJob每日指定时间刷新处理start-------------");
        DataResource.ac.getBean(BuildHomeCityService.class).refreshBanditJob();
        LogUtil.debug("------------RefreshBanditJob每日指定时间刷新处理end-------------");
    }

}
