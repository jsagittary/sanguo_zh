package com.gryphpoem.game.zw.resource.pojo.fight.skill;

import com.gryphpoem.game.zw.resource.constant.FightConstant;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;

import java.util.List;

/**
 * todo: 本类目前只做未来扩展用
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-04-17 14:03
 */
public final class SkillTargetUtils {

    /**
     * @param skillAction
     * @param dto
     * @return
     */
    public static Force selectSingleTarget(FightSkillAction skillAction, FightActionDto dto) {
        switch (skillAction.getSkillConfig().getTarget()) {
            case FightConstant.TargetSelect.TARGET_SELECT_0:
                return selectSingleTarget0(skillAction, dto);
        }
        return null;
    }


    /**
     * 目前技能机制不支持多目标技能
     * @param skillAction
     * @param dto
     * @return
     */
    public static List<Force> selectMultTarget(FightSkillAction skillAction, FightActionDto dto) {
        switch (skillAction.getSkillConfig().getTarget()) {
            case FightConstant.TargetSelect.TARGET_SELECT_0:
                return dto.getTargets();
        }
        return null;
    }

    private static Force selectSingleTarget0(FightSkillAction skillAction, FightActionDto dto){
        FightLogic flogic = dto.getFightLogic();
        Fighter target = dto.getDirection() == FightActionDto.Direction.ACTION_ATK ? flogic.getAttacker() : flogic.getDefender();
        return null;
    }


}
