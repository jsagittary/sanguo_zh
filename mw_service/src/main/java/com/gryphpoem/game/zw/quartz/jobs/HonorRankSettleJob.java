package com.gryphpoem.game.zw.quartz.jobs;

import org.quartz.JobExecutionContext;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.service.CampService;

/**
 * @ClassName HonorRankSettleJob.java
 * @Description
 * @author QiuKun
 * @date 2017年8月17日
 */
public class HonorRankSettleJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        CampService service = DataResource.ac.getBean(CampService.class);
        service.honorRankSettleAll();

    }

}
