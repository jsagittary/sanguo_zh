package com.gryphpoem.game.zw.quartz.jobs.sandtable;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.quartz.jobs.AbsMainLogicThreadJob;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;
import org.quartz.JobExecutionContext;

public class SandTableOpenEndRoundJob extends AbsMainLogicThreadJob implements SandTableJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        String name = context.getJobDetail().getKey().getName();

        SandTableContestService sandTableContestService = DataResource.ac.getBean(SandTableContestService.class);
        if(!sandTableContestService.checkOpenByServerId()){
            LogUtil.error("执行沙盘演武定时器[SandTableOpenEndRoundJob,"+name+"], 当前区服不开放, 配表1042=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
            return;
        }
        try {
            if (name.equalsIgnoreCase(name_open)) {
                //send enroll mail
                sandTableContestService.openBegin();
            } else if (name.equalsIgnoreCase(name_end)) {
                //send camp rank email
            } else if (name.indexOf(name_round) >= 0) {
                //execute fight
                String round = name.substring(name_round.length());
                sandTableContestService.fightLines(Integer.parseInt(round));
            }
            LogUtil.error("执行沙盘演武定时器[SandTableOpenEndRoundJob,"+name+"]完成");
        }catch (Exception e) {
            LogUtil.error("执行沙盘演武定时器[SandTableOpenEndRoundJob,"+name+"]错误，", e);
        }
    }

}
