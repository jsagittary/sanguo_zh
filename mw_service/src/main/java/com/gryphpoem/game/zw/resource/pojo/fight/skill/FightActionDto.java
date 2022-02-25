package com.gryphpoem.game.zw.resource.pojo.fight.skill;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-04-16 16:14
 */
public class FightActionDto {
    private Direction direction;
    private FightLogic fightLogic;
    private Force source;
    private List<Force> targets;
    private CommonPb.Action.Builder actionBuilder;

    /**
     * 初始化行动对象
     *
     * @param fightLogic 战斗逻辑
     * @param source     本次行动部队
     * @param targets    本次行动目标部队
     * @param direction  进攻方向
     */
    public FightActionDto(FightLogic fightLogic, Force source, List<Force> targets, Direction direction) {
        this.fightLogic = fightLogic;
        this.source = source;
        this.targets = targets;
        this.direction = direction;
        this.actionBuilder = CommonPb.Action.newBuilder();
    }

    public FightActionDto(FightLogic fightLogic, Force source, Force target, Direction direction) {
        this.fightLogic = fightLogic;
        this.source = source;
        this.targets = new ArrayList<>();
        this.targets.add(target);
        this.direction = direction;
        fightLogic.actionDataA = CommonPb.Action.newBuilder();
        fightLogic.actionDataA.setTarget(target.id);
        fightLogic.actionDataA.setTargetRoleId(target.ownerId);
        fightLogic.actionDataB = CommonPb.Action.newBuilder();
        fightLogic.actionDataB.setTarget(source.id);
        fightLogic.actionDataB.setTargetRoleId(source.ownerId);
        this.actionBuilder = direction == Direction.ACTION_ATK ? fightLogic.actionDataA : fightLogic.actionDataB;
    }

    public enum Direction {
        ACTION_ATK,//进攻方行动
        ACTION_DEF,//防守方行动
    }

    public Direction getDirection() {
        return direction;
    }

    public FightLogic getFightLogic() {
        return fightLogic;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setFightLogic(FightLogic fightLogic) {
        this.fightLogic = fightLogic;
    }

    public Force getSource() {
        return source;
    }

    public void setSource(Force source) {
        this.source = source;
    }

    public List<Force> getTargets() {
        return targets;
    }

    public void setTargets(List<Force> targets) {
        this.targets = targets;
    }

    public CommonPb.Action.Builder getActionBuilder() {
        return actionBuilder;
    }

    public void setActionBuilder(CommonPb.Action.Builder actionBuilder) {
        this.actionBuilder = actionBuilder;
    }

}
