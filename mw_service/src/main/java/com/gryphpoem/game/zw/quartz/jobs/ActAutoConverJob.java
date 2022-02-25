package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import org.quartz.JobExecutionContext;

import java.util.Date;

public class ActAutoConverJob extends AbsMainLogicThreadJob {
    @Override
    protected void executeInMain(JobExecutionContext context) {
        String name = context.getJobDetail().getKey().getName();
        String actTypeStr = name.split("_")[0];
        Integer actType = Integer.valueOf(actTypeStr);
        LogUtil.debug("-----act name", name, ", now", DateHelper.formatDateMiniTime(new Date()));
        ActivityService service = DataResource.ac.getBean(ActivityService.class);
        try {
            service.autoConverActItems(actType,name);
        } catch (Exception e) {
            LogUtil.error("活动结束后，自动将活动道具转换，发生错误， name=" + name, e);
        }

    }
}
