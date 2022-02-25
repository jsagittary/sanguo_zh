package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.service.activity.ActivityTemplateService;
import org.quartz.JobExecutionContext;

/**
 * end time
 */
public class ActEndJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        String jobKeyName = context.getJobDetail().getKey().getName();
        ActivityTemplateService service = DataResource.ac.getBean(ActivityTemplateService.class);
        try {
            String[] strArr = jobKeyName.split("_");
            service.execActivityEnd(Integer.parseInt(strArr[0]),Integer.parseInt(strArr[1]),Integer.parseInt(strArr[2]));
            LogUtil.error(String.format("活动EndTime执行job, jobKeyName=%s",jobKeyName));
        } catch (Exception e) {
            LogUtil.error("ACTIVITY END JOB, ",e);
        }

    }

}
