package com.gryphpoem.game.zw.resource.pojo.medal;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * 红色勋章, 普通勋章的变种, 因为前期设计未考虑到扩展, 为了减少代码变动的折中方案
 * @author: ZhouJie
 * @date: Create in 2019-04-26 16:27
 * @modified By:
 * @see RedMedal#auraUnLock         需要对应普通勋章光环技能才能激活
 * @see RedMedal#specialSkillUnLock 需要对应普通勋章特殊技能才能激活
 */
public class RedMedal extends Medal {

    /**
     * 光环激活
     */
    private boolean auraUnLock;

    /**
     * 特技激活
     */
    private boolean specialSkillUnLock;

    public RedMedal() {
    }

    public RedMedal(CommonPb.Medal medal) {
        super(medal);
        CommonPb.MedalExt ext = medal.getExt();
        this.auraUnLock = ext.getAuraUnLock();
        this.specialSkillUnLock = ext.getSpecialSkillUnLock();
    }

    public boolean isAuraUnLock() {
        return auraUnLock;
    }

    public void setAuraUnLock(boolean auraUnLock) {
        this.auraUnLock = auraUnLock;
    }

    public boolean isSpecialSkillUnLock() {
        return specialSkillUnLock;
    }

    public void setSpecialSkillUnLock(boolean specialSkillUnLock) {
        this.specialSkillUnLock = specialSkillUnLock;
    }
}
