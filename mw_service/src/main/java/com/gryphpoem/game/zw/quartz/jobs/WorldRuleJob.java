package com.gryphpoem.game.zw.quartz.jobs;

import org.quartz.JobExecutionContext;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.WorldDataManager;

/**
 * @ClassName WorldRuleJob.java
 * @Description 流寇或矿点刷新规则
 * @author QiuKun
 * @date 2018年2月6日
 */
public class WorldRuleJob extends AbsMainLogicThreadJob {

    private void refreshWorld() {
        String name = getContext().getJobDetail().getKey().getName();
        WorldDataManager dataManager = DataResource.ac.getBean(WorldDataManager.class);
        try {
            String typeStr = name.split("_")[0];
            String ruleStr = name.split("_")[1];
            Integer type = Integer.valueOf(typeStr); // 1为流寇 2为矿点
            Integer rule = Integer.valueOf(ruleStr);
            String desc = type == 1 ? "流寇" : "矿点";
            LogUtil.world("-----------开始执行 世界地图", desc, "点刷新 ,规则:", rule, "-----------");
            if (type == 1) {// 流寇
                dataManager.refreshAllBandit(rule);
            } else {// 矿点
                dataManager.refreshAllMine(rule);
            }
            LogUtil.world("-----------结束执行 世界地图", desc, "点刷新 ,规则:", rule, "-----------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void executeInMain(JobExecutionContext context) {
        refreshWorld();
    }

}
