package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.constant.MentorConstant;
import com.gryphpoem.game.zw.resource.constant.PlaneConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticMentor;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-26 16:21
 * @description: 教官信息
 * @modified By:
 */
public class Mentor {

    private int id;                                                     // id, 随着等级变化
    private int type;                                                   // 类型, 不可变
    private int lv;                                                     // 等级
    private int exp;                                                    // 经验
    private int fight;                                                  // 战斗力
    private Map<Integer, Integer> extAttr = new HashMap<>();            // 附加属性，v1为攻击防御兵力等属性，v2为数值
    private int[] equips = new int[HeroConstant.HERO_BATTLE_LEN + 1];   // 装备, 长度限制HERO_BATTLE_LEN, equipId
    private int[] skills = new int[PlaneConstant.PLANE_SKILL_LEN + 1];  // 技能, 长度限制PLANE_SKILL_LEN, skillType
    private Map<Integer, Boolean> upAward = new HashMap<>();            // 升级奖励领取状态, key: id, val: 是否领取

    public Mentor() {
    }

    public Mentor(StaticMentor sMentor) {
        this();
        if (!CheckNull.isNull(sMentor)) {
            this.id = sMentor.getId();
            this.type = sMentor.getType();
            this.lv = sMentor.getLv();
            this.exp = 0;
            this.fight = 0;
        }
    }

    /**
     * 反序列化
     * @param mentor
     */
    public Mentor(CommonPb.Mentor mentor) {
        this();
        this.id = mentor.getId();
        this.type = mentor.getType();
        this.lv = mentor.getLv();
        this.exp = mentor.getExp();
        this.fight = mentor.getFight();
        for (CommonPb.TwoInt twoInt : mentor.getExtAttrList()) {
            this.extAttr.put(twoInt.getV1(), twoInt.getV2());
        }
        int pos;
        for (CommonPb.TwoInt twoInt : mentor.getEquipList()) {
            pos = twoInt.getV1();
            if (pos > 0) {
                this.equips[pos] = twoInt.getV2();
            }
        }
        for (CommonPb.MentorSkill skill : mentor.getSkillsList()) {
            pos = skill.getPos();
            if (pos > 0 ) {
                this.skills[pos] = skill.getType();
            }
        }
        for (CommonPb.TwoInt twoInt : mentor.getUpAwardList()) {
            int id = twoInt.getV1();
            boolean can = twoInt.getV2() == MentorConstant.MENTOR_UPAWARD_HAS_GAIN;
            this.upAward.put(id, can);
        }
    }

    /**
     * 教官升级
     * @param sMentor
     */
    public void levelUp(StaticMentor sMentor) {
        this.id = sMentor.getId();
        this.lv = sMentor.getLv();
        this.exp = 0;
    }

    /**
     * 激活技能
     * @param pos       技能栏位置
     * @param type      技能id
     */
    public void onSkill(int pos, int type) {
        if (type <= 0 || type >= Integer.MAX_VALUE) {
            return;
        }

        if (pos <= 0 || pos > PlaneConstant.PLANE_SKILL_LEN) {
            return;
        }

        this.skills[pos] = type;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public int getLv() {
        return lv;
    }

    public int getExp() {
        return exp;
    }

    public int getFight() {
        return fight;
    }

    public Map<Integer, Integer> getExtAttr() {
        return extAttr;
    }

    public int[] getEquips() {
        return equips;
    }

    public int[] getSkills() {
        return skills;
    }

    public Map<Integer, Boolean> getUpAward() {
        return upAward;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setFight(int fight) {
        this.fight = fight;
    }
}
