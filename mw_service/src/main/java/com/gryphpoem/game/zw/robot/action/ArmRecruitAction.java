package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 士兵招募
 * @author TanDonghai
 * @date 创建时间：2017年10月18日 上午11:07:13
 *
 */
public class ArmRecruitAction extends AbstractActionNode {

    public ArmRecruitAction() {
        super(ActionNodeType.ARM_RECRUIT);
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
                robotService.armRecruit((Player) param);
                return true;
            }
        }
        return false;
    }

}
