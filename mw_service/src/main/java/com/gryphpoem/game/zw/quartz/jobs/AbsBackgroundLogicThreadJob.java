package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.util.LogUtil;
import org.quartz.JobExecutionContext;

/**
 * @description:  后台线程定时器逻辑
 * @author: GeYuanpeng
 * @time: 2022/8/29
 */
public abstract class AbsBackgroundLogicThreadJob extends AbsGameJob{

    @Override
    protected void process() {
        DataResource.logicServer.addCommandByType(() -> {
            long start = System.nanoTime();
            executeInMain(context);
            long cost = (System.nanoTime() - start) / 1000000;
            if (cost > 500) {
                LogUtil.warn(String.format("Job :%s, haust :%d", context.getJobInstance().getClass().getSimpleName(), cost));
            }
        }, DealType.BACKGROUND);
    }

    protected abstract void executeInMain(JobExecutionContext context);

}
