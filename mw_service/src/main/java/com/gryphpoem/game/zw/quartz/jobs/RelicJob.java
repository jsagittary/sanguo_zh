package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.service.relic.RelicService;
import org.quartz.JobExecutionContext;

/**
 * @author xwind
 * @date 2022/8/2
 */
public class RelicJob extends AbsMainLogicThreadJob {
    @Override
    protected void executeInMain(JobExecutionContext context) {
        RelicService relicService = DataResource.getBean(RelicService.class);
        if(relicService.checkFunctionOpen()){
            relicService.refreshRelic();
        }
    }
}
