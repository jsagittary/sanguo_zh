package com.gryphpoem.game.zw.resource.pojo.fight;

import com.gryphpoem.game.zw.dataMgr.StaticFightDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.PlaneConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticFightBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticFightSkill;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-28 13:50
 * @description: 战机战斗技能
 * @modified By:
 */
public class PlaneFightSkill extends FightSkill {

    private int planeId;

    public int getPlaneId() {
        return planeId;
    }

    public void setPlaneId(int planeId) {
        this.planeId = planeId;
    }

    public PlaneFightSkill() {
    }

    public PlaneFightSkill(StaticFightSkill staticFightSkill) {
        super(staticFightSkill);
    }

    @Override public void releaseSkill(Force force, Force target, FightLogic logic, boolean isAtk) {
        CommonPb.Action.Builder actionData;
        if (isAtk) {
            createAction(force, target, logic);
            actionData = logic.actionDataA;
        } else {
            createAction(target, force, logic);
            actionData = logic.actionDataB;
        }
        StaticFightSkill sFightSkill = StaticFightDataMgr.getFightSkillMapById(this.skillId);
        if (!CheckNull.isNull(sFightSkill)) {
            if (this.notReleaseSkill()) { // 未释放技能
                this.release = true;    // 更新技能释放标识
                int buffId = sFightSkill.getBuffId();
                if (buffId > 0) {
                    StaticFightBuff sFightBuff = StaticFightDataMgr.getFightBuffMapById(buffId);
                    if (!CheckNull.isNull(sFightBuff)) {
                        FightBuff buff = new FightBuff(sFightBuff, force.id, isAtk);
                        buff.releaseBuff(force, target, logic); // 释放buff
                    }
                } else if (this.notEndSkillEffect()) { // 释放技能
                    releaseSkill(force, target, actionData, sFightSkill, logic);
                }
            } else if (this.notEndSkillEffect()) { // 释放技能
                releaseSkill(force, target, actionData, sFightSkill, logic);
            }
        }
    }

    @Override public CommonPb.FightSkill.Builder createFightSkillPb(Force force, Force target,
            CommonPb.Action.Builder actionData, int hurt, FightLogic logic) {
        CommonPb.FightSkill.Builder builder = super.createFightSkillPb(force, target, actionData, hurt, logic);
        actionData.setPlaneId(getPlaneId()); // 设置战机id
        builder.setCount(getReleaseCnt());
        return builder;
    }

    /**
     * 重写父类技能效果未结束检测
     *
     * @return
     */
    @Override public boolean notEndSkillEffect() {
        return getReleaseCnt() > 0;
    }

    /**
     * 释放技能
     *  @param force
     * @param target
     * @param actionData
     * @param sFightSkill
     * @param logic
     */
    private void releaseSkill(Force force, Force target, CommonPb.Action.Builder actionData,
            StaticFightSkill sFightSkill, FightLogic logic) {
        int lastHurt = this.param.getOrDefault(PlaneConstant.SkillParam.LAST_HURT_NUM, 0);
        int releaseCnt = this.param.getOrDefault(PlaneConstant.SkillParam.RELEASE_CNT, 0); // 释放次数
        int fade = sFightSkill.getFade();
        int hurt = releaseCnt == 0 && lastHurt == 0 ?                                                   // 如果是首次释放就是配置的伤害值
                sFightSkill.getValue() :                                                                // 之后就是衰减万分比
                (int) Math.ceil(lastHurt * ((1 - (fade / Constant.TEN_THROUSAND))));                    // 技能计算公式
        this.param.put(PlaneConstant.SkillParam.LAST_HURT_NUM, hurt);                                   // 更新上次伤害值
        this.param.put(PlaneConstant.SkillParam.RELEASE_CNT, releaseCnt + 1);                           // 更新释放次数
        CommonPb.FightSkill.Builder builder = createFightSkillPb(force, target, actionData, hurt, logic);
        actionData.setSkill(builder.build());
    }

    /**
     * 获取技能剩余触发次数
     *
     * @return
     */
    private int getReleaseCnt() {
        return this.param.getOrDefault(PlaneConstant.SkillParam.MAX_RELEASE_CNT, 0) - this.param
                .getOrDefault(PlaneConstant.SkillParam.RELEASE_CNT, 0);
    }

}
