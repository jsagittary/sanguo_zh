package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 装备打造
 * @author TanDonghai
 * @date 创建时间：2017年10月16日 下午6:36:21
 *
 */
public class EquipForgeAction extends AbstractActionNode {

    public EquipForgeAction() {
        super(ActionNodeType.EQUIP_FORGE);
    }

    @Override
    public boolean action() {
        robotService.addAutoEquipLogic(robot);
        return false;
    }

    @Override
    public boolean action(Object... params) {
        if (!CheckNull.isEmpty(params)) {
            Object param = params[0];
            if (param instanceof Player) {
                robotService.addAutoEquipLogic((Player) param);
                return true;
            }
        }
        return false;
    }

}
