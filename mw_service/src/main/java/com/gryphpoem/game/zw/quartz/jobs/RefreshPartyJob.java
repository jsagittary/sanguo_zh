package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.resource.domain.Player;
import org.quartz.JobExecutionContext;

/**
 * @author: ZhouJie
 * @date: Create in 2019-01-08 15:44
 * @description: 刷新阵营任务
 * @modified By:
 */
public class RefreshPartyJob extends AbsMainLogicThreadJob {

    @Override protected void executeInMain(JobExecutionContext context) {
        LogUtil.debug("------------刷新阵营任务start-------------");
        TaskDataManager taskDataManager = DataResource.ac.getBean(TaskDataManager.class);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        for (Player p : playerDataManager.getPlayers().values()) {
            Java8Utils.invokeNoExceptionICommand(() -> taskDataManager.refreshPartyTask(p));// 刷新军团任务
        }
        LogUtil.debug("------------刷新阵营任务end-------------");
    }
}
