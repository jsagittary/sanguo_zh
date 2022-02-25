package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.constant.MentorConstant;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticMentor;
import com.gryphpoem.game.zw.resource.domain.s.StaticMentorEquip;
import com.gryphpoem.game.zw.resource.domain.s.StaticMentorSkill;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-26 17:59
 * @description: 教官的相关静态配置
 * @modified By:
 */
public class StaticMentorDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // 教官配置, key: id, val: StaticMentor
    private static Map<Integer, StaticMentor> sMentorIdMap = new HashMap<>();
    // 教官配置, key: type, val: StaticMentors
    private static Map<Integer, List<StaticMentor>> sMentorTypeMap = new HashMap<>();
    // 教官技能配置, key: id, val: StaticMentorSkill
    private static Map<Integer, StaticMentorSkill> sMentorSkillIdMap = new HashMap<>();
    // 教官技能配置, key: type, val: StaticMentorSkills
    private static Map<Integer, List<StaticMentorSkill>> sMentorSkillTypeMap = new HashMap<>();
    // 教官装备配置, key: id, val: StaticMentorEquip
    private static Map<Integer, StaticMentorEquip> sMentorEquipIdMap = new HashMap<>();
    // 教官装备配置, key: gearOrder, val: StaticMentorEquips
    private static Map<Integer, List<StaticMentorEquip>> sMentorEquipTypeMap = new HashMap<>();

    public static void init() {
        StaticMentorDataMgr.sMentorIdMap = staticDataDao.selectMentorIdMap();
        MentorConstant.MENTOR_MAX_LV.clear();
        MentorConstant.MENTOR_SKILL_MAX_LV.clear();
        StaticMentorDataMgr.sMentorIdMap.values().stream().collect(Collectors.groupingBy(StaticMentor::getType,
                Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparingInt(StaticMentor::getLv)),
                        Optional::get))).entrySet()
                .forEach(en -> MentorConstant.MENTOR_MAX_LV.put(en.getKey(), en.getValue().getLv()));
        Map<Integer, List<StaticMentor>> sMentorTypeMap = new HashMap<>();
        StaticMentorDataMgr.sMentorIdMap.values().forEach(staticMentor -> {
            int type = staticMentor.getType();
            List<StaticMentor> sMentors = sMentorTypeMap.get(type);
            if (CheckNull.isNull(sMentors)) {
                sMentors = new ArrayList<>();
                sMentorTypeMap.put(type, sMentors);
            }
            sMentors.add(staticMentor);
            if (!CheckNull.isEmpty(staticMentor.getUnsealSkill()) && staticMentor.getLv() > 0) { // 处理教官技能的解锁条件
                Map<Integer, List<List<Integer>>> unSealMap = MentorConstant.MENTOR_SKILL_UNSEAL.get(type);
                if (CheckNull.isNull(unSealMap)) {
                    unSealMap = new HashMap<>();
                    MentorConstant.MENTOR_SKILL_UNSEAL.put(type, unSealMap);
                }
                unSealMap.put(staticMentor.getLv(), staticMentor.getUnsealSkill());
            }
        });
        StaticMentorDataMgr.sMentorTypeMap = sMentorTypeMap;

        StaticMentorDataMgr.sMentorSkillIdMap = staticDataDao.selectMentorSkillIdMap();
        StaticMentorDataMgr.sMentorSkillIdMap.values().stream().collect(Collectors
                .groupingBy(StaticMentorSkill::getType, Collectors
                        .collectingAndThen(Collectors.maxBy(Comparator.comparingInt(StaticMentorSkill::getLv)),
                                Optional::get))).entrySet().forEach(en -> MentorConstant.MENTOR_SKILL_MAX_LV.put(en.getKey(), en.getValue().getLv()));
        Map<Integer, List<StaticMentorSkill>> sMentorSkillTypeMap = new HashMap<>();
        StaticMentorDataMgr.sMentorSkillIdMap.values().forEach(staticMentorSkill -> {
            int type = staticMentorSkill.getType();
            List<StaticMentorSkill> skills = sMentorSkillTypeMap.get(type);
            if (CheckNull.isNull(skills)) {
                skills = new ArrayList<>();
                sMentorSkillTypeMap.put(type, skills);
            }
            skills.add(staticMentorSkill);
        });
        StaticMentorDataMgr.sMentorSkillTypeMap = sMentorSkillTypeMap;

        StaticMentorDataMgr.sMentorEquipIdMap = staticDataDao.selectMentorEquipIdMap();
        Map<Integer, List<StaticMentorEquip>> sMentorEquipTypeMap = new HashMap<>();
        StaticMentorDataMgr.sMentorEquipIdMap.values().forEach(staticMentorEquip -> {
            int gearOrder = staticMentorEquip.getGearOrder();
            List<StaticMentorEquip> equipList = sMentorEquipTypeMap.get(gearOrder);
            if (CheckNull.isNull(equipList)) {
                equipList = new ArrayList<>();
                sMentorEquipTypeMap.put(gearOrder, equipList);
            }
            equipList.add(staticMentorEquip);
        });
        StaticMentorDataMgr.sMentorEquipTypeMap = sMentorEquipTypeMap;
    }

    /**
     * 根据mentorId查询配置
     *
     * @param id
     * @return
     */
    public static StaticMentor getsMentorIdMap(int id) {
        return sMentorIdMap.get(id);
    }

    /**
     * 根据mentorType查询配置
     *
     * @param type
     * @return
     */
    public static List<StaticMentor> getsMentorTypeMap(int type) {
        return sMentorTypeMap.get(type);
    }

    /**
     * 根据mentorType和lv查询配置
     *
     * @param type
     * @param lv
     * @return
     */
    public static StaticMentor getsMentorByTypeAndLv(int type, int lv) {
        List<StaticMentor> mentors = getsMentorTypeMap(type);
        StaticMentor sMentor = null;
        if (!CheckNull.isEmpty(mentors)) {
            sMentor = mentors.stream().filter(m -> m.getLv() == lv).findFirst().orElse(null);
        }
        return sMentor;
    }

    /**
     * 根据技能id查询教官技能配置
     *
     * @param skillId
     * @return
     */
    public static StaticMentorSkill getsMentorSkillIdMap(int skillId) {
        return sMentorSkillIdMap.get(skillId);
    }

    /**
     * 根据skillType查询技能配置
     *
     * @param type
     * @return
     */
    public static List<StaticMentorSkill> getsMentorSkillTypeMap(int type) {
        return sMentorSkillTypeMap.get(type);
    }

    /**
     * 根据skillType和lv查询技能配置
     *
     * @param type
     * @param lv
     * @return
     */
    public static StaticMentorSkill getMentorSkillByTypeAndLv(int type, int lv) {
        StaticMentorSkill sSkill = null;
        List<StaticMentorSkill> skillList = getsMentorSkillTypeMap(type);
        if (!CheckNull.isEmpty(skillList)) {
            sSkill = skillList.stream().filter(s -> s.getLv() == lv).findFirst().orElse(null);
        }
        return sSkill;
    }

    /**
     * 根据id查询装备配置
     *
     * @param id
     * @return
     */
    public static StaticMentorEquip getsMentorEquipIdMap(int id) {
        return sMentorEquipIdMap.get(id);
    }

    /**
     * 根据gearOrder查询装备配置
     *
     * @param gearOrder
     * @return
     */
    public static List<StaticMentorEquip> getsMentorEquipTypeMap(int gearOrder) {
        return sMentorEquipTypeMap.get(gearOrder);
    }
}
