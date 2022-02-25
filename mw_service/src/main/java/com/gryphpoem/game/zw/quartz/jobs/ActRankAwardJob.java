package com.gryphpoem.game.zw.quartz.jobs;

import java.util.Date;

import org.quartz.JobExecutionContext;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName ActRankAwardJob.java
 * @Description 活动领取奖励
 * @author QiuKun
 * @date 2018年3月21日
 */
public class ActRankAwardJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        String name = context.getJobDetail().getKey().getName();
        LogUtil.debug("-----ActAwardJob ----- act name", name, ", now", DateHelper.formatDateMiniTime(new Date()));
        ActivityService service = DataResource.ac.getBean(ActivityService.class);
        try {
            String actTypeStr = name.split("_")[0];
            Integer actType = Integer.valueOf(actTypeStr);
            service.actAwardTimeProcess(actType);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
