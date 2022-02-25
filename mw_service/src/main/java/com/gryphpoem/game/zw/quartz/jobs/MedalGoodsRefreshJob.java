package com.gryphpoem.game.zw.quartz.jobs;

import org.quartz.JobExecutionContext;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.service.MedalService;
/**
 * 
* @ClassName: MedalGoodsRefreshJob
* @Description: 勋章商品刷新定时器
* @author chenqi
* @date 2018年9月17日
*
 */
public class MedalGoodsRefreshJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        MedalService service = DataResource.ac.getBean(MedalService.class);
        service.refreshAllPlayerMedalGoods();
    }

}
