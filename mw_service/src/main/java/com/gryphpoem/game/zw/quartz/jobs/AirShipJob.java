package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.service.AirshipService;
import org.quartz.JobExecutionContext;

/**
 * 飞艇定时器
 * @program: empire_en
 * @description:
 * @author: zhou jie
 * @create: 2020-07-20 18:17
 */
public class AirShipJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        DataResource.ac.getBean(AirshipService.class).runSecTimer();
    }
}