package com.gryphpoem.game.zw.quartz.jobs.function;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.quartz.jobs.AbsMainLogicThreadJob;
import com.gryphpoem.game.zw.service.plan.DrawCardPlanTemplateService;
import org.quartz.JobExecutionContext;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-16 16:05
 */
public class FunctionEndJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        String jobKeyName = context.getJobDetail().getKey().getName();
        DrawCardPlanTemplateService service = DataResource.ac.getBean(DrawCardPlanTemplateService.class);
        try {
            String[] strArr = jobKeyName.split("_");
            service.execActivityEnd(Integer.parseInt(strArr[0]), Integer.parseInt(strArr[1]));
            LogUtil.error(String.format("功能EndTime执行job, jobKeyName=%s", jobKeyName));
        } catch (Exception e) {
            LogUtil.error("FUNCTION END JOB, ", e);
        }
    }
}
