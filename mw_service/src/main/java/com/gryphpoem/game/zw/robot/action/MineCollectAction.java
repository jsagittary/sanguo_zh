package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 采集矿点
 * @author TanDonghai
 * @date 创建时间：2017年10月20日 下午3:35:11
 *
 */
public class MineCollectAction extends AbstractActionNode {

    public MineCollectAction() {
        super(ActionNodeType.MINE_COLLECT);
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
                robotService.autoCollectMine((Player) param);
                return true;
            }
        }
        return false;
    }

}
