package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import org.quartz.JobExecutionContext;

import java.util.Date;

/**
 * @ClassName ActMailJob.java
 * @Description 将未领取奖励的活动通过邮件进发送
 * @author QiuKun
 * @date 2017年11月23日
 */
public class ActMailJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        String jobKeyName = context.getJobDetail().getKey().getName();
        LogUtil.debug("-----act name", jobKeyName, ", now", DateHelper.formatDateMiniTime(new Date()));
        ActivityService service = DataResource.ac.getBean(ActivityService.class);
        try {
            String actTypeStr = jobKeyName.split("_")[0];
            Integer actType = Integer.valueOf(actTypeStr);
            service.autoExchangUnrewardeMail(actType);
            service.sendUnrewardedMail(actType, jobKeyName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
