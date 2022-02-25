package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.gameplay.local.service.newyork.NewYorkWarService;
import org.quartz.JobExecutionContext;

/**
 * Created by pengshuo on 2019/5/9 15:38
 * <br>Description: NewYorkWar job
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class NewYorkWarJob extends AbsMainLogicThreadJob{

    @Override
    protected void executeInMain(JobExecutionContext context) {
        try {
            // 初始化 纽约争霸
            DataResource.ac.getBean(NewYorkWarService.class).initNewYorkWar();
        }catch (Exception e){
            LogUtil.error("纽约争霸-初始化 error ",e.getMessage());
        }
    }

    /** 执行预显示开启纽约争霸活动 */
    public static void initPreNewYorkWarJob(){
        QuartzHelper.addJob(ScheduleManager.getInstance().getSched(),"preNewYorkWarJob",
                "NewYorkWar",NewYorkWarJob.class, WorldConstant.NEWYORK_WAR_PRE_TIME);
    }
}
