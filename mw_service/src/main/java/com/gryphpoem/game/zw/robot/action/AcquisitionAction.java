package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 个人资源点采集
 * @author TanDonghai
 * @date 创建时间：2017年10月21日 下午2:58:20
 *
 */
public class AcquisitionAction extends AbstractActionNode {

    public AcquisitionAction() {
        super(ActionNodeType.ACQUISITION);
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
                robotService.autoAcauisition((Player) param);
                return true;
            }
        }
        return false;
    }

}
