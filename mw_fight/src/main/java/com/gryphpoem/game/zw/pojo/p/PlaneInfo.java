package com.gryphpoem.game.zw.pojo.p;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-22 11:56
 * @description: 战机详情
 * @modified By:
 */
public class PlaneInfo {

    private int planeId;                // 战机Id
    private int skillId;                // 技能Id
    private boolean useSkill;           // 是否使用技能

    public int getPlaneId() {
        return planeId;
    }

    public void setPlaneId(int planeId) {
        this.planeId = planeId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public boolean isUseSkill() {
        return useSkill;
    }

    public void setUseSkill(boolean useSkill) {
        this.useSkill = useSkill;
    }

    /**
     * 是否有技能
     *
     * @return
     */
    public boolean hasSkill() {
        return this.skillId > 0 && !this.useSkill;
    }

    /**
     * 使用技能
     */
    public void useSkill() {
        this.useSkill = true;
    }

    @Override
    public String toString() {
        return "PlaneInfo{" + "planeId=" + planeId + ", skillId=" + skillId + ", useSkill=" + useSkill + '}';
    }
}
