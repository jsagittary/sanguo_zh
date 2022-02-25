package com.gryphpoem.game.zw.resource.pojo.fight.skill;

import com.gryphpoem.game.zw.resource.domain.s.StaticSkillAction;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.FightSkill;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-04-16 15:38
 */
public class FightSkillAction extends FightSkill {

    //技能激活后才能释放
    private boolean isActive;
    //释放次数
    private int releaseCount;
    //触发次数
    private int triggerCount;
    //触发成功次数
    private int triggerSuccCount;
    //触发失败次数
    private int triggerFailCount;
    //技能配置
    private StaticSkillAction s_skill;
    //增加的技能释放概率
    private int addProbability;

    public FightSkillAction(StaticSkillAction s_skill) {
        super();
        this.s_skill = s_skill;
    }

    @Override
    public void releaseSkill(Force source, Force target, FightLogic logic, boolean isAtk) {

    }

    public int getReleaseCount() {
        return releaseCount;
    }

    public StaticSkillAction getSkillConfig() {
        return s_skill;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setReleaseCount(int releaseCount) {
        this.releaseCount = releaseCount;
    }

    public int getTriggerSuccCount() {
        return triggerSuccCount;
    }

    public void setTriggerSuccCount(int triggerSuccCount) {
        this.triggerSuccCount = triggerSuccCount;
    }

    public int getTriggerFailCount() {
        return triggerFailCount;
    }

    public void setTriggerFailCount(int triggerFailCount) {
        this.triggerFailCount = triggerFailCount;
    }

    public int getTriggerCount() {
        return triggerCount;
    }

    public void setTriggerCount(int triggerCount) {
        this.triggerCount = triggerCount;
    }

    public int getAddProbability() {
        return addProbability;
    }

    public void setAddProbability(int addProbability) {
        this.addProbability = addProbability;
    }
}
