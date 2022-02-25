package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import org.quartz.JobExecutionContext;

public class CrossGamePlanJob extends AbsMainLogicThreadJob {

    public static final String GROUP = "CROSS_GAME_PLAN";
    public static final String NAME_END = "CROSS_GAME_PLAN_END_";
    public static final String NAME_START = "CROSS_GAME_PLAN_START_";
    public static final String NAME_DISPLAY = "CROSS_GAME_PLAN_DISPLAY_DISPLAY_";
    public static final String NAME_DISPLAY_BEGIN = "CROSS_GAME_PLAN_DISPLAY_BEGIN_";

    @Override
    protected void executeInMain(JobExecutionContext context) {
        try {
            String jobName = context.getJobDetail().getKey().getName();
            int lastIdx = jobName.lastIndexOf("_");
            int planId = Integer.parseInt(jobName.substring(lastIdx + 1));

            String nameFunctionStr = jobName.substring(0, lastIdx);
            int nameLastIndex = nameFunctionStr.lastIndexOf("_");
            String name = nameFunctionStr.substring(0, nameLastIndex + 1);
            int functionId = Integer.parseInt(nameFunctionStr.substring(nameLastIndex + 1));

            LogUtil.error("执行跨服功能定时器, jobName = " + jobName);
            DataResource.ac.getBean(CrossGamePlayService.class).executeGamePlan(functionId, planId, name);
        } catch (Exception e) {
            LogUtil.error("执行跨服功能定时器, e: ", e);
        }
    }
}
