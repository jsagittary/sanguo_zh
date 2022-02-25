package com.gryphpoem.game.zw.quartz.jobs;

import org.quartz.JobExecutionContext;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.service.CityService;

/**
 * @ClassName CityLordRewardJob.java
 * @Description 城主奖励9点发放
 * @author QiuKun
 * @date 2017年9月29日
 */
public class CityLordRewardJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        CityService service = DataResource.ac.getBean(CityService.class);
        service.cityLordRewardTimeJob();

    }

}
