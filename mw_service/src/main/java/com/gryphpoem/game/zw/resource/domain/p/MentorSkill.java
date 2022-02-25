package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.s.StaticMentorSkill;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-26 16:47
 * @description: 教官技能, 用来提升对应位置上阵将领的属性
 * @modified By:
 */
public class MentorSkill {

    private int id;                 // id
    private int type;               // 类型
    private int lv;                 // 等级
    private int pos;                // 作用的将领位置
    private boolean isActivate;     // 是否激活专业技能, 用作与空军教官

    /**
     * 技能升级
     * @param sMentorSkill
     */
    public void upLevel(StaticMentorSkill sMentorSkill) {
        this.id = sMentorSkill.getId();
        this.lv = sMentorSkill.getLv();
    }

    public MentorSkill() {
    }

    /**
     * 反序列化
     * @param skill
     */
    public MentorSkill(CommonPb.MentorSkill skill) {
        this();
        this.id = skill.getSkillId();
        this.type = skill.getType();
        this.lv = skill.getLv();
        this.pos = skill.getPos();
        this.isActivate = skill.getIsActivate();
    }

    public MentorSkill(StaticMentorSkill sMentorSkill, int pos) {
        this();
        this.id = sMentorSkill.getId();
        this.type = sMentorSkill.getType();
        this.lv = sMentorSkill.getLv();
        this.pos = pos;
        this.isActivate = !CheckNull.isEmpty(sMentorSkill.getActiveItem()) ? false : true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getPos() {
        return pos;
    }

    public boolean isActivate() {
        return isActivate;
    }

    public void setActivate(boolean activate) {
        this.isActivate = activate;
    }

    public CommonPb.MentorSkill createSkillPb() {
        CommonPb.MentorSkill.Builder builder = CommonPb.MentorSkill.newBuilder();
        builder.setPos(this.pos);
        builder.setSkillId(this.id);
        builder.setType(this.type);
        builder.setLv(this.lv);
        builder.setIsActivate(this.isActivate);
        return builder.build();
    }
}
