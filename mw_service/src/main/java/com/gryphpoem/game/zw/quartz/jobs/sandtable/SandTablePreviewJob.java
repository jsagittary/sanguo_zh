package com.gryphpoem.game.zw.quartz.jobs.sandtable;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.quartz.jobs.AbsMainLogicThreadJob;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.pojo.global.GlobalSchedule;
import com.gryphpoem.game.zw.resource.pojo.sandtable.SandTableContest;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;
import org.quartz.CronExpression;
import org.quartz.JobExecutionContext;

import java.util.Date;

public class SandTablePreviewJob extends AbsMainLogicThreadJob implements SandTableJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
//        String name = context.getJobDetail().getKey().getName();
        try {
            GlobalDataManager globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
            GlobalSchedule globalSchedule = globalDataManager.getGameGlobal().getGlobalSchedule();
            if(globalSchedule.getCurrentScheduleId() < 7){
                LogUtil.error("执行沙盘演武定时器[SandTablePreviewJob], 世界进程 <7 不开放");
                return;
            }
            SandTableContestService sandTableContestService = DataResource.ac.getBean(SandTableContestService.class);
            if(!sandTableContestService.checkOpenByServerId()){
                LogUtil.error("执行沙盘演武定时器[SandTablePreviewJob], 当前区服不开放, 配表1042=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
                return;
            }

            //clear player buy times
            PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
            playerDataManager.getPlayers().values().forEach(o -> o.getSandTableBought().clear());

            SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
            //clear data
            sandTableContest.clearData();
            //setting time
            sandTableContest.setPreviewBeginDate(context.getFireTime());
            CronExpression cronExpression = new CronExpression(Constant.SAND_TABLE_OPEN_END.get(0));
            sandTableContest.setOpenBeginDate(cronExpression.getNextValidTimeAfter(sandTableContest.getPreviewBeginDate()));
            cronExpression = new CronExpression(Constant.SAND_TABLE_OPEN_END.get(1));
            sandTableContest.setOpenEndDate(cronExpression.getNextValidTimeAfter(sandTableContest.getOpenBeginDate()));
            int exchangeEndStamp = (int) (sandTableContest.getOpenEndDate().getTime()/1000 + Constant.SAND_TABLE_1055);
            sandTableContest.setExchangeEndDate(new Date(exchangeEndStamp * 1000L));
            //setting match Date
            sandTableContest.setMatchDate(TimeHelper.getDay(sandTableContest.getOpenBeginDate()));
            //grouping
            sandTableContestService.matchGroup();

            //sync to client
            sandTableContestService.syncSandTablePreview(sandTableContest);

            LogUtil.error("执行沙盘演武定时器[SandTablePreviewJob]完成, SandTableContest=" + JSON.toJSONString(sandTableContest));
        } catch (Exception e) {
            LogUtil.error("执行沙盘演武定时器[SandTablePreviewJob]错误，", e);
        }
    }

}
