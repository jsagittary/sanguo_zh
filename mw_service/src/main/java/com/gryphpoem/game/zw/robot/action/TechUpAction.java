package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 研究科技
 * @author TanDonghai
 * @date 创建时间：2017年10月12日 下午5:43:03
 *
 */
public class TechUpAction extends AbstractActionNode {

    public TechUpAction() {
        super(ActionNodeType.TECH_UP);
    }

    @Override
    public boolean action() {
        robotService.addTechAutoUp(robot);
        return true;
    }

    @Override
    public boolean action(Object... params) {
        if (!CheckNull.isEmpty(params)) {
            Object param = params[0];
            if (param instanceof Player) {
                Player player = (Player) param;
                robotService.addTechAutoUp(player);
                return true;
            }
        }
        return false;
    }

}
