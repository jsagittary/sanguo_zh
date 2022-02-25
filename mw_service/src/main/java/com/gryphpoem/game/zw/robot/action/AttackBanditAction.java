package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 攻打流寇
 * @author TanDonghai
 * @date 创建时间：2017年10月23日 下午8:19:13
 *
 */
public class AttackBanditAction extends AbstractActionNode {

    public AttackBanditAction() {
        super(ActionNodeType.ATTACK_BANDIT);
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
                robotService.autoAttackBandit((Player) param);
                return true;
            }
        }
        return false;
    }

}
