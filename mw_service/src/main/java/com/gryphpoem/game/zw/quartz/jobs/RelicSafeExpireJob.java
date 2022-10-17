package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.service.relic.RelicService;
import org.quartz.JobExecutionContext;

/**
 * @Description:
 * @Author: DuanShQ
 * @CreateTime: 2022-08-25  18:28
 */
public class RelicSafeExpireJob extends AbsMainLogicThreadJob {
    @Override
    protected void executeInMain(JobExecutionContext context) {
        RelicService relicService = DataResource.getBean(RelicService.class);
        if (relicService.checkFunctionOpen()) {
            relicService.broadcastRelicSafeExpire();
        }
    }
}
