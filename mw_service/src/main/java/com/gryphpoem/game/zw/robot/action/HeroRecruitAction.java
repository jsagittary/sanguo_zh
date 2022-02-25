package com.gryphpoem.game.zw.robot.action;

import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description 将领招募
 * @author TanDonghai
 * @date 创建时间：2017年10月17日 下午7:39:40
 *
 */
public class HeroRecruitAction extends AbstractActionNode {

    public HeroRecruitAction() {
        super(ActionNodeType.HERO_RECRUIT);
    }

    @Override
    public boolean action() {
        robotService.heroRecruit(robot);
        return true;
    }

    @Override
    public boolean action(Object... params) {
        if (!CheckNull.isEmpty(params)) {
            Object param = params[0];
            if (param instanceof Player) {
                robotService.heroRecruit((Player) param);
                return true;
            }
        }
        return false;
    }

}
