package com.gryphpoem.game.zw.robot.conditon;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticBtreeNode;
import com.gryphpoem.game.zw.robot.ConditionUtil;
import com.gryphpoem.game.zw.robot.base.AbstractConditionNode;

/**
 * @Description 机器人角色等级条件
 * @author TanDonghai
 * @date 创建时间：2017年9月18日 下午3:22:20
 *
 */
public class RobotRoleLvCondition extends AbstractConditionNode {
    private int roleLv;// 条件配置的角色等级

    public RobotRoleLvCondition(StaticBtreeNode config) throws MwException {
        super(config);
    }

    @Override
    public void parseCondition() {
        roleLv = parseIntParam();
    }

    @Override
    public boolean checkCondition() {
        return ConditionUtil.greaterThanCondition(robot.lord.getLevel(), roleLv);
    }

    @Override
    public boolean checkCondition(Object... params) {
        if (null == params[0]) return false;

        int compareLv = 0;
        if (params[0] instanceof Player) {
            Player player = (Player) params[0];
            compareLv = player.lord.getLevel();
        } else if (params[0] instanceof Integer) {
            compareLv = (int) params[0];
        }
        return ConditionUtil.greaterThanCondition(compareLv, roleLv);
    }

}
