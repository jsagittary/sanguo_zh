package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.service.GameJobService;
import org.quartz.JobExecutionContext;

import com.gryphpoem.game.zw.core.util.LogUtil;

/**
 * @author QiuKun
 * @ClassName AcrossTheDayJob.java
 * @Description 跨天处理
 * @date 2017年12月7日
 */
public class AcrossTheDayJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        LogUtil.debug("------------转处理玩家状态start-------------");
        LogUtil.error("quartz执行转点任务------JobExecutionContext: " + context);
        DataResource.ac.getBean(GameJobService.class).execAcrossTheDayJob();
        LogUtil.debug("------------转处理玩家状态end-------------");
    }

}
