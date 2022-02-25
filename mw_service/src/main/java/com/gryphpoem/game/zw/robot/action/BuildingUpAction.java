package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 建筑升级
 * @author TanDonghai
 * @date 创建时间：2017年9月19日 下午6:29:26
 *
 */
public class BuildingUpAction extends AbstractActionNode {
    public BuildingUpAction() {
        super(ActionNodeType.BUILDING_UP);
    }

    @Override
    public boolean action() {
        robotService.addBuildingAutoBuild(robot);
        return true;
    }

    @Override
    public boolean action(Object... params) {
        if (!CheckNull.isEmpty(params)) {
            Object param = params[0];
            if (param instanceof Player) {
                robotService.addBuildingAutoBuild((Player) param);
                return true;
            }
        }
        return false;
    }

}
