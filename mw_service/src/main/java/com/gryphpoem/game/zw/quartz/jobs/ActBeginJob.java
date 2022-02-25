package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import com.gryphpoem.game.zw.service.activity.ActivityTemplateService;
import org.quartz.JobExecutionContext;

import java.util.Date;

/**
 * @ClassName ActBeginJob.java
 * @Description 新活动开启的推送
 * @author QiuKun
 * @date 2018年4月3日
 */
public class ActBeginJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        String jobKeyName = context.getJobDetail().getKey().getName();
        LogUtil.debug("-----活动开启推送 act name", jobKeyName, ", now", DateHelper.formatDateMiniTime(new Date()));
        ActivityService service = DataResource.ac.getBean(ActivityService.class);
        try {
            service.syncActListChg();
            String[] strArr = jobKeyName.split("_");
            int actType = Integer.parseInt(strArr[0]);
            service.onActBegin(actType);
            ActivityTemplateService templateService = DataResource.ac.getBean(ActivityTemplateService.class);
            templateService.execActivityBegin(Integer.parseInt(strArr[0]),Integer.parseInt(strArr[1]),Integer.parseInt(strArr[2]));
            // service.initLightningWarBoss(actType);
            LogUtil.error(String.format("活动BeginTime执行job, jobKeyName=%s",jobKeyName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
