package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 增加角色经验
 * @author TanDonghai
 * @date 创建时间：2017年10月30日 上午10:15:40
 *
 */
public class RoleLvUpAction extends AbstractActionNode {

    public RoleLvUpAction() {
        super(ActionNodeType.ROLE_EXP);
    }

    // 第一个数表示增加经验的时间周期，单位：秒，第二个数表示单次增加量，第三个数表示增加经验到玩家等级上限
    int[] expAddArr = null;

    @Override
    public boolean action() {
        return action(robot);
    }

    @Override
    public boolean action(Object... params) {
        if (!CheckNull.isEmpty(params)) {
            Object param = params[0];
            if (param instanceof Player && haveConfigParam()) {
                addRobotExp((Player) param);
                return true;
            }
        }
        return false;
    }

    private void addRobotExp(Player player) {
        int now = TimeHelper.getCurrentSecond();
        if (player.robotRecord.isNeedAddRoleExp(now)) {
            if (null == expAddArr) {
                parseNodeParam();
            }

            // 第一个数表示增加经验的时间周期，单位：秒，第二个数表示单次增加量，第三个数表示增加经验到玩家等级上限
            if (expAddArr.length >= 3) {
                int time = expAddArr[0];
                int addExp = expAddArr[1];
                int maxLv = expAddArr[2];
                if (player.lord.getLevel() < maxLv) {
                    robotService.addRobotExp(player, addExp);
                    player.robotRecord.setNextAddRoleExp(now + time);
                }
            }
        }
    }

    @Override
    protected void parseNodeParam() {
        expAddArr = parseListIntParam();
    }
}
