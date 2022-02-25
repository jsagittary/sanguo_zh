package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import org.quartz.JobExecutionContext;

/**
 * 在主线程执行定时器逻辑
 * 
 * @ClassName AbsJob.java
 * @Description
 * @author QiuKun
 * @date 2018年3月26日
 */
public abstract class AbsMainLogicThreadJob extends AbsGameJob {

    @Override
    protected void process() {
        DataResource.logicServer.addCommandByMainType(() -> {
            long start = System.nanoTime();
            executeInMain(context);
            long cost = (System.nanoTime() - start) / 1000000;
            if (cost > 500) {
                LogUtil.warn(String.format("Job :%s, haust :%d", context.getJobInstance().getClass().getSimpleName(), cost));
            }
        });
    }

    protected abstract void executeInMain(JobExecutionContext context);

}
