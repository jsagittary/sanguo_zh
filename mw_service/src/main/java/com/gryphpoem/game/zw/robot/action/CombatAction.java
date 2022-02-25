package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 挑战副本
 * @author TanDonghai
 * @date 创建时间：2017年10月20日 下午12:05:24
 *
 */
public class CombatAction extends AbstractActionNode {

    public CombatAction() {
        super(ActionNodeType.DO_COMBAT);
    }

    @Override
    public boolean action() {
        action(robot);
        return false;
    }

    @Override
    public boolean action(Object... params) {
        if (!CheckNull.isEmpty(params)) {
            Object param = params[0];
            if (param instanceof Player) {
                robotService.autoDoCombat((Player) param);
                return true;
            }
        }
        return false;
    }

}
