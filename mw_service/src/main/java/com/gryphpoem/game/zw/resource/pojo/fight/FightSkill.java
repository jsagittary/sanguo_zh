package com.gryphpoem.game.zw.resource.pojo.fight;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.s.StaticFightSkill;
import com.gryphpoem.game.zw.resource.util.RandomHelper;

import java.util.HashMap;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-28 11:17
 * @description: 战斗技能
 * @modified By:
 */
public abstract class FightSkill {

    protected int skillId;             // 技能id
    protected int skillType;           // 技能类型, 1 将领技能, 2 战机入场技能, 3 战机专业技能
    protected boolean release;         // 是否释放技能
    protected boolean roll;            // 是否有触发
    protected int order;               // 释放排序
    protected int probability;         // 触发概率
    public HashMap<Integer, Integer> param = new HashMap<>(); // 临时变量的存储

    public FightSkill() {
    }

    public FightSkill(StaticFightSkill sFightSkill) {
        this();
        this.release = false;
        this.skillId = sFightSkill.getSkillId();
        this.skillType = sFightSkill.getType();
        this.order = sFightSkill.getOrder();
        this.probability = sFightSkill.getProbability();
    }

    /**
     * 触发技能
     *
     * @param force
     * @param target
     * @param isAtk
     */
    public abstract void releaseSkill(Force force, Force target, FightLogic logic, boolean isAtk);

    /**
     * 技能效果
     *
     * @param force      进攻方
     * @param target     防守方
     * @param actionData Action
     * @param hurt       伤害
     * @return
     */
    public CommonPb.FightSkill.Builder createFightSkillPb(Force force, Force target, CommonPb.Action.Builder actionData,
            int hurt, FightLogic logic) {
        hurt = logic.hurt(target, null, hurt);
        force.killed += hurt;// 记录攻击方击杀数
        force.fighter.hurt += hurt;// 记录总击杀数
        target.fighter.lost += hurt;// 记录总伤兵数
        actionData.setHurt(hurt);
        actionData.setCount(target.getSurplusCount());
        actionData.setDeadLine(target.getDeadLine());
        CommonPb.FightSkill.Builder builder = CommonPb.FightSkill.newBuilder();
        builder.setType(skillType);
        return builder;
    }

    /**
     * 根据触发概率计算是否可以触发
     *
     * @return
     */
    public boolean canRelease() {
        if (!roll && notReleaseSkill()) { // 没有Roll过技能, 有技能未释放
            roll = true;
            return RandomHelper.isHitRangeIn10000(this.probability); // Roll一次
        } else if (roll && !notReleaseSkill() && notEndSkillEffect()) { //  Roll过技能, 技能未释放完
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否还未释放技能
     *
     * @return
     */
    public boolean notReleaseSkill() {
        return !release;
    }

    /**
     * 是否还未Roll技能
     * @return
     */
    public boolean notRollSkill() {
        return !roll;
    }

    /**
     * 是否技能效果未结束
     *
     * @return
     */
    public boolean notEndSkillEffect() {
        return false;
    }

    /**
     * 创建Action对象
     *
     * @return
     */
    protected void createAction(Force force, Force target, FightLogic logic) {
        logic.actionDataA = CommonPb.Action.newBuilder();
        logic.actionDataA.setTarget(target.id);
        logic.actionDataA.setTargetRoleId(target.ownerId);
        logic.actionDataB = CommonPb.Action.newBuilder();
        logic.actionDataB.setTarget(force.id);
        logic.actionDataB.setTargetRoleId(force.ownerId);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
