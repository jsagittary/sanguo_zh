package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;

/**
 * 任务策略接口
 */
public interface TaskFinishService {

    /**
     * 处理任务
     * @param player 玩家信息
     * @param eTask 任务类型
     * @param params 任务参数
     */
    void process(Player player, ETask eTask, int...params);
}
