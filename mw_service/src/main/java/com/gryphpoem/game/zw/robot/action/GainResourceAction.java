package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 征收资源
 * @author TanDonghai
 * @date 创建时间：2017年10月27日 上午9:29:04
 *
 */
public class GainResourceAction extends AbstractActionNode {

    public GainResourceAction() {
        super(ActionNodeType.GAIN_RESOURCE);
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
                robotService.autoGainResource((Player) param);
                return true;
            }
        }
        return false;
    }

}
