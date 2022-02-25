package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 参加活动
 * @author TanDonghai
 * @date 创建时间：2017年10月20日 上午11:00:31
 *
 */
public class ActivityRewardAction extends AbstractActionNode {

    public ActivityRewardAction() {
        super(ActionNodeType.ACTIVITY);
    }

    @Override
    public boolean action() {
        return false;
    }

    @Override
    public boolean action(Object... params) {
        return false;
    }

}
