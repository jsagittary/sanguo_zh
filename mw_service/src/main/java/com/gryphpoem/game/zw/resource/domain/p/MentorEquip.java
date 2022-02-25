package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-26 16:45
 * @description: 教官装备, 用来提升对应位置上阵将领的属性, 创建的
 * @modified By:
 */
public class MentorEquip {

    private int keyId;                                          // 私有id
    private int equipId;                                        // equipId
    private int type;                                           // 装备类型, 同时代表可穿戴的装备位置
    private int lv;                                             // 等级
    private int starLv;                                         // 星级
    private Map<Integer, Integer> attr = new HashMap<>();       // 基本属性，v1为攻击防御兵力等属性，v2为数值
    private Map<Integer, Integer> extAttr = new HashMap<>();    // 附加属性，v1为攻击防御兵力等属性，v2为数值
    private int mentorId;                                       // 教官id
    private int fight;                                          // 服务器自用
    private int mentorType;                                     // 教官类型

    public MentorEquip() {
    }

    /**
     * 反序列化
     * @param equip
     */
    public MentorEquip(CommonPb.MentorEquip equip) {
        this();
        this.keyId = equip.getKeyId();
        this.equipId = equip.getEquipId();
        this.type = equip.getType();
        this.lv = equip.getLv();
        this.starLv = equip.getStarLv();
        this.mentorId = equip.getMentorId();
        this.fight = equip.getFight();
        this.mentorType = equip.getMentorType();
        for (CommonPb.TwoInt twoInt : equip.getAttrList()) {
            this.attr.put(twoInt.getV1(), twoInt.getV2());
        }
        for (CommonPb.TwoInt twoInt : equip.getExtAttrList()) {
            this.extAttr.put(twoInt.getV1(), twoInt.getV2());
        }
    }

    /**
     * 序列化
     * @return
     */
    public CommonPb.MentorEquip createEquipPb() {
        CommonPb.MentorEquip.Builder builder = CommonPb.MentorEquip.newBuilder();
        builder.setKeyId(this.keyId);
        builder.setEquipId(this.equipId);
        builder.setType(this.type);
        builder.setLv(this.lv);
        builder.setStarLv(this.starLv);
        for (Map.Entry<Integer, Integer> en : attr.entrySet()) {
            builder.addAttr(PbHelper.createTwoIntPb(en.getKey(), en.getValue()));
        }
        for (Map.Entry<Integer, Integer> en : extAttr.entrySet()) {
            builder.addExtAttr(PbHelper.createTwoIntPb(en.getKey(), en.getValue()));
        }
        builder.setMentorId(this.mentorId);
        builder.setFight(this.fight);
        builder.setMentorType(this.mentorType);
        return builder.build();
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public void setStarLv(int starLv) {
        this.starLv = starLv;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }

    public void setExtAttr(Map<Integer, Integer> extAttr) {
        this.extAttr = extAttr;
    }

    public void setMentorId(int mentorId) {
        this.mentorId = mentorId;
    }

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public Map<Integer, Integer> getExtAttr() {
        return extAttr;
    }

    public int getFight() {
        return fight;
    }

    public void setFight(int fight) {
        this.fight = fight;
    }

    public int getMentorType() {
        return mentorType;
    }

    public void setMentorType(int mentorType) {
        this.mentorType = mentorType;
    }

    public int getType() {
        return type;
    }

    public int getLv() {
        return lv;
    }

    public int getEquipId() {
        return equipId;
    }

    public int getMentorId() {
        return mentorId;
    }

    @Override public String toString() {
        return "MentorEquip{" + "keyId=" + keyId + ", equipId=" + equipId + ", type=" + type + ", lv=" + lv
                + ", starLv=" + starLv + ", attr=" + attr + ", extAttr=" + extAttr + ", mentorId=" + mentorId
                + ", fight=" + fight + ", mentorType=" + mentorType + '}';
    }
}
