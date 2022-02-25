package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 领取任务奖励
 * @author TanDonghai
 * @date 创建时间：2017年10月23日 下午6:06:22
 *
 */
public class TaskRewardAction extends AbstractActionNode {

    public TaskRewardAction() {
        super(ActionNodeType.TASK_REWARD);
    }

    @Override
    public boolean action() {
        return action(robot);
    }

    @Override
    public boolean action(Object... params) {
        if (!CheckNull.isEmpty(params)) {
            Object param = params[0];
            if (param instanceof Player) {
                robotService.autoTaskReward((Player) param);
                return true;
            }
        }
        return false;
    }

}
