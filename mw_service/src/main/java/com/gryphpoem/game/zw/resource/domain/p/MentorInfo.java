package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.manager.MentorDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-26 16:19
 * @description: 教官相关
 * @modified By:
 */
public class MentorInfo {

    private Map<Integer, Mentor> mentors = new HashMap<>();           // 教官信息, key: type, val: Mentor
    private Map<Integer, MentorEquip> equipMap = new HashMap<>();     // 教官装备, <keyId, MentorEquip>
    private Map<Integer, MentorSkill> skillMap = new HashMap<>();   // 教官技能, <skillType, MentorSkill>
    private Map<Integer, Map<Integer, Integer>> betterEquip = new HashMap<>(); // 可穿戴高战力装备, key: mentorType, val: pos, keyId

    public MentorInfo() {
    }

    public Map<Integer, Mentor> getMentors() {
        return mentors;
    }

    public Map<Integer, MentorSkill> getSkillMap() {
        return skillMap;
    }

    public Map<Integer, MentorEquip> getEquipMap() {
        return equipMap;
    }

    public Map<Integer, Map<Integer, Integer>> getBetterEquip() {
        return betterEquip;
    }

    /**
     * 序列化
     * @return
     */
    public SerializePb.SerMentorInfo ser() {
        SerializePb.SerMentorInfo.Builder builder = SerializePb.SerMentorInfo.newBuilder();
        MentorDataManager mentorDataManager = DataResource.ac.getBean(MentorDataManager.class);
        for (Mentor mentor : this.mentors.values()) {
            builder.addMentors(mentorDataManager.createMentorPb(mentor, this));
        }
        for (MentorEquip equip : this.equipMap.values()) {
            builder.addEquips(equip.createEquipPb());
        }
        for (MentorSkill skill : this.skillMap.values()) {
            builder.addSkills(skill.createSkillPb());
        }
        for (Map.Entry<Integer, Map<Integer, Integer>> en : this.betterEquip.entrySet()) {
            int mentorType = en.getKey();
            SerializePb.MentorBetterEquip.Builder equipBuilder = SerializePb.MentorBetterEquip.newBuilder();
            equipBuilder.setMentorType(mentorType);
            for (Map.Entry<Integer, Integer> entry : en.getValue().entrySet()) {
                equipBuilder.addEquipInfo(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue()));
            }
            builder.addBetters(equipBuilder.build());
        }
        return builder.build();
    }

    /**
     * 反序列化
     * @param mentorInfo
     */
    public MentorInfo(SerializePb.SerMentorInfo mentorInfo) {
        this();
        for (CommonPb.Mentor mentor : mentorInfo.getMentorsList()) {
            this.mentors.put(mentor.getType(), new Mentor(mentor));
        }
        for (CommonPb.MentorEquip equip : mentorInfo.getEquipsList()) {
            this.equipMap.put(equip.getKeyId(), new MentorEquip(equip));
        }
        for (CommonPb.MentorSkill skill : mentorInfo.getSkillsList()) {
            this.skillMap.put(skill.getType(), new MentorSkill(skill));
        }
        for (SerializePb.MentorBetterEquip equip : mentorInfo.getBettersList()) {
            int mentorType = equip.getMentorType();
            Map<Integer, Integer> map = this.betterEquip.get(mentorType);
            if (CheckNull.isNull(map)) {
                map = new HashMap<>();
                this.betterEquip.put(mentorType, map);
            }
            for (CommonPb.TwoInt twoInt : equip.getEquipInfoList()) {
                map.put(twoInt.getV1(), twoInt.getV2());
            }
        }
    }
}
