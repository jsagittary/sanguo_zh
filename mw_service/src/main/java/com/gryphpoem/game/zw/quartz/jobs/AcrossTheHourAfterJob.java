package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.service.ActivityTriggerService;
import com.gryphpoem.game.zw.service.TreasureWareService;
import org.quartz.JobExecutionContext;

/**
 * 整点处理, 有五秒的偏移, [21:00:5, 22:00:5, 23:00:5, 00:00:5]
 * @program:
 * @description:
 * @author: zhou jie
 * @create: 2021-06-01 16:32
 */
public class AcrossTheHourAfterJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        LogUtil.debug("------------AcrossTheHourAfterJob整点处理start-------------");
        Java8Utils.invokeNoExceptionICommand(() -> {
            // 转点执行时间触发活动事件
            ActivityTriggerService activityTriggerService = DataResource.ac.getBean(ActivityTriggerService.class);
            activityTriggerService.checkTimeTriggerActivity();
        });
        // 宝具清除定时器
        Java8Utils.invokeNoExceptionICommand(() -> DataResource.ac.getBean(TreasureWareService.class).timedClearDecomposeTreasureWare());
        LogUtil.debug("------------AcrossTheHourAfterJob整点处理end-------------");
    }

}
