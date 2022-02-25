package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.constant.PushConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PushMessageUtil;
import org.quartz.JobExecutionContext;

/**
 * @program: zombie_push
 * @description:
 * @author: zhou jie
 * @create: 2019-09-25 16:46
 */
public class PushTestJob extends AbsMainLogicThreadJob {

    @Override
    protected void executeInMain(JobExecutionContext context) {
        PlayerDataManager dataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = dataManager.getPlayers().values().stream().findFirst().orElse(null);
        if (!CheckNull.isNull(player)) {
            PushMessageUtil.pushMessage(player.account, PushConstant.ACT_POWER_TWELVE);
        }
    }
}