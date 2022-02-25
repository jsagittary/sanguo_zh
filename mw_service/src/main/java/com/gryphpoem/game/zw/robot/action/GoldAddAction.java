package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 增加金币
 * @author TanDonghai
 * @date 创建时间：2017年10月20日 上午11:01:38
 *
 */
public class GoldAddAction extends AbstractActionNode {

    public GoldAddAction() {
        super(ActionNodeType.GOLD_ADD);
    }

    // 第一个数表示增加金币的时间周期，单位：秒，第二个数表示单次增加量，第三个数表述最大增加量
    int[] goldAddArr = null;

    @Override
    public boolean action() {
        return action(robot);
    }

    @Override
    public boolean action(Object... params) {
        if (!CheckNull.isEmpty(params)) {
            Object param = params[0];
            if (param instanceof Player && haveConfigParam()) {
                addRobotGold((Player) param);
                return true;
            }
        }
        return false;
    }

    /**
     * 给机器人加金币逻辑
     * 
     * @param player
     */
    private void addRobotGold(Player player) {
        int now = TimeHelper.getCurrentSecond();
        if (player.robotRecord.isNeedAddGold(now)) {
            if (null == goldAddArr) {
                parseNodeParam();
            }

            // 第一个数表示增加金币的时间周期，单位：秒，第二个数表示单次增加量，第三个数表述最大增加量
            if (goldAddArr.length >= 3) {
                int time = goldAddArr[0];
                int addGold = goldAddArr[1];
                int maxGold = goldAddArr[2];
                if (player.lord.getGoldGive() < maxGold) {
                    robotService.addRobotGold(player, addGold);
                    player.robotRecord.setNextAddGold(now + time);
                }
            }
        }
    }

    @Override
    protected void parseNodeParam() {
        goldAddArr = parseListIntParam();
    }
}
