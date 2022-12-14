package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.service.TreasureWareService;
import org.quartz.JobExecutionContext;

public class DelTreasureWareJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        LogUtil.debug("------------DelTreasureWareJob整点处理start-------------");
        DataResource.ac.getBean(TreasureWareService.class).timedClearDecomposeTreasureWare();
        LogUtil.debug("------------DelTreasureWareJob整点处理end-------------");
    }

}
