package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.quartz.QuartzCallBack;

/**
 * @ClassName GameJob.java
 * @Description
 * @author QiuKun
 * @date 2018年5月17日
 */
public class DefultJob extends AbsGameJob {
    public final static String RUN = "run";
    public final static String DEFULT_GROUP = "defult_group";

    public DefultJob() {
    }

    public DefultJob(String name, String group) {
        super(name, group);
    }

    public DefultJob(String name) {
        super(name, DEFULT_GROUP);
    }

    public static DefultJob createDefult(String name) {
        return new DefultJob(name, DEFULT_GROUP);
    }

    @Override
    protected void process() {
        DataResource.logicServer.addCommandByMainType(() -> {
            QuartzCallBack callBack = (QuartzCallBack) super.context.getJobDetail().getJobDataMap().get(RUN);
            callBack.run(DefultJob.this);
        });
    }

}
