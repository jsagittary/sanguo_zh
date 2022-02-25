package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 增加VIP经验
 * @author TanDonghai
 * @date 创建时间：2017年10月20日 上午10:58:33
 *
 */
public class VipUpAction extends AbstractActionNode {

    public VipUpAction() {
        super(ActionNodeType.VIP_UP);
    }

    // 第一个数表示增加经验的时间周期，单位：秒，第二个数表示单次增加量，第三个数表述最大增加量
    int[] vipExpAddArr = null;

    @Override
    public boolean action() {
        return action(robot);
    }

    @Override
    public boolean action(Object... params) {
        if (!CheckNull.isEmpty(params)) {
            Object param = params[0];
            if (param instanceof Player && haveConfigParam()) {
                addRobotVipExp((Player) param);
                return true;
            }
        }
        return false;
    }

    /**
     * 给机器人增加VIP经验
     * 
     * @param player
     */
    private void addRobotVipExp(Player player) {
        int now = TimeHelper.getCurrentSecond();
        if (player.robotRecord.isNeedAddVipExp(now)) {
            if (null == vipExpAddArr) {
                parseNodeParam();
            }

            // 第一个数表示增加经验的时间周期，单位：秒，第二个数表示单次增加量，第三个数表示最大增加量
            if (vipExpAddArr.length >= 3) {
                int time = vipExpAddArr[0];
                int addExp = vipExpAddArr[1];
                int maxExp = vipExpAddArr[2];
                if (player.lord.getVipExp() < maxExp) {
                    robotService.adRobotVipExp(player, addExp);
                    player.robotRecord.setNextAddVipExp(now + time);
                }
            }
        }
    }

    @Override
    protected void parseNodeParam() {
        vipExpAddArr = parseListIntParam();
    }

}
