package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.quartz.JobExecutionContext;

public class SeasonTalentJob extends AbsMainLogicThreadJob {

    public static final String NAME_BEGIN = "SEASON_TALENT_BEGIN_";
    public static final String NAME_END = "SEASON_TALENT_END_";
    public static final String GROUP_SEASON = "GROUP_SEASON_TALENT";

    @Override
    protected void executeInMain(JobExecutionContext context) {
        try {
            String jobName = context.getJobDetail().getKey().getName();
            int lastIdx = jobName.lastIndexOf("_");
            String name = jobName.substring(0, lastIdx + 1);
            int planId = Integer.parseInt(jobName.substring(lastIdx + 1));
            LogUtil.error("执行赛季天赋定时器, jobName=" + jobName);
            if (name.equals(NAME_BEGIN)) {
                DataResource.ac.getBean(SeasonTalentService.class).executeSeasonTalentBegin(planId);
            } else if (name.equals(NAME_END)) {
                DataResource.ac.getBean(SeasonTalentService.class).executeSeasonTalentEnd(planId);
            }
        } catch (Exception e) {
            LogUtil.error("执行赛季天赋job错误, ",e);
        }
    }
}
