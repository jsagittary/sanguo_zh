package com.gryphpoem.game.zw.mgr.timer;

import org.quartz.JobExecutionContext;

import com.gryphpoem.game.zw.core.util.QuartzHelper.QuartzJob;
import com.gryphpoem.game.zw.core.util.QuartzHelper.QuartzJobRun;

/**
 * @ClassName DefultLogicMainJob.java
 * @Description
 * @author QiuKun
 * @date 2019年5月23日
 */
public class DefultLogicMainJob extends LogicMainJob implements QuartzJob {
    public final static String RUN = "run";
    public final static String DEFULT_GROUP = "defult_group";

    public DefultLogicMainJob() {
    }

    public DefultLogicMainJob(String name) {
        this.group = DEFULT_GROUP;
        this.name = name;
    }

    public static DefultLogicMainJob newInstance(String name) {
        return new DefultLogicMainJob(name);
    }

    @Override
    protected void executeInMain(JobExecutionContext context) {
        Object o = getContext().getJobDetail().getJobDataMap().get(RUN);
        if (o != null && o instanceof QuartzJobRun) {
            QuartzJobRun r = (QuartzJobRun) o;
            r.run(getContext());
        }
    }

    @Override
    public String getRunKey() {
        return RUN;
    }

}
