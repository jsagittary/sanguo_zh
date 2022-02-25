package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.service.activity.Year2022FireworkService;
import org.quartz.JobExecutionContext;

/**
 * 放有烟花job
 * @author xwind
 * @date 2021/12/20
 */
public class FireworksJob extends AbsMainLogicThreadJob {
    @Override
    protected void executeInMain(JobExecutionContext context) {
        String jobKeyName = context.getJobDetail().getKey().getName();
        if(jobKeyName.contains(ActJob.NAME_FWPRE)){
            String[] strArr = jobKeyName.split("_");
            //推送跑马灯
            DataResource.getBean(Year2022FireworkService.class).sendChat(Integer.parseInt(strArr[1]));
            LogUtil.error("执行放烟花job,预告跑马灯" + jobKeyName);
        }else if(jobKeyName.contains(ActJob.NAME_FWLETOFF)){
            String[] strArr = jobKeyName.split("_");
            DataResource.getBean(Year2022FireworkService.class).systemLetoff(Integer.parseInt(strArr[1]),Integer.parseInt(strArr[2]));
            LogUtil.error("执行放烟花job,系统燃放" + jobKeyName);
        }
    }
}
