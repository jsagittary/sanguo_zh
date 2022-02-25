package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.service.session.SeasonService;
import org.quartz.JobExecutionContext;

/**
 * 赛季定时器
 * @author xwind
 * @date 2021/4/16
 */
public class SeasonJob extends AbsMainLogicThreadJob {

    public static final String NAME_PRE = "NAME_PRE_";
    public static final String NAME_BEGIN = "NAME_BEGIN_";
    public static final String NAME_END = "NAME_END_";
    public static final String NAME_OVER = "NAME_OVER_";
    public static final String NAME_TREASURY_RESET = "NAME_TREASURY_RESET_";
    public static final String NAME_TREASURY_AWARD = "NAME_TREASURY_AWARD_";
    public static final String GROUP_SEASON = "GROUP_SEASON";

    @Override
    protected void executeInMain(JobExecutionContext context) {
        try {
            String jobName = context.getJobDetail().getKey().getName();
            int lastIdx = jobName.lastIndexOf("_");
            String name = jobName.substring(0, lastIdx + 1);
            int planId = Integer.parseInt(jobName.substring(lastIdx + 1));
            LogUtil.error("执行赛季定时器, jobName=" + jobName);
            if (name.equals(NAME_PRE)) {
                DataResource.ac.getBean(SeasonService.class).execJob4Pre(planId);
            } else if (name.equals(NAME_BEGIN)) {
                DataResource.ac.getBean(SeasonService.class).execJob4Begin(planId);
            } else if (name.equals(NAME_END)) {
                DataResource.ac.getBean(SeasonService.class).execJob4End(planId);
            } else if (name.equals(NAME_OVER)) {
                DataResource.ac.getBean(SeasonService.class).execJob4Over(planId);
            } else if (name.equals(NAME_TREASURY_RESET)) {
                DataResource.ac.getBean(SeasonService.class).execJob4AwardTime(planId);
            } else if (name.equals(NAME_TREASURY_AWARD)){
                DataResource.ac.getBean(SeasonService.class).execJob4ResetTime(planId);
            }
        }catch (Exception e) {
            LogUtil.error("执行赛季job错误, ",e);
        }
    }
}
