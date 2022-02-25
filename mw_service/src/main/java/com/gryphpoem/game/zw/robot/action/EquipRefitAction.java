package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 装备改造
 * @author TanDonghai
 * @date 创建时间：2017年10月20日 上午10:50:31
 *
 */
public class EquipRefitAction extends AbstractActionNode {

    public EquipRefitAction() {
        super(ActionNodeType.EQUIP_REFIT);
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
                robotService.equipRefit((Player) param);
                return true;
            }
        }
        return false;
    }

}
