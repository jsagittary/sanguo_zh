package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticMentorDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.MentorConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Mentor;
import com.gryphpoem.game.zw.resource.domain.p.MentorEquip;
import com.gryphpoem.game.zw.resource.domain.p.MentorInfo;
import com.gryphpoem.game.zw.resource.domain.p.MentorSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticMentor;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-29 16:33
 * @description: 教官
 * @modified By:
 */
@Component public class MentorDataManager {

    /**
     * 是否获得了更好的装备
     *
     * @param player 角色
     * @param equip  新装备
     */
    public void checkBetterEquip(Player player, MentorEquip equip) {
        if (CheckNull.isNull(player) || CheckNull.isNull(equip)) {
            return;
        }
        MentorInfo info = player.getMentorInfo();
        int mentorType = equip.getMentorType(); // 教官类型
        int type = equip.getType(); // 装备类型, 也用作穿戴装备位置
        Map<Integer, Integer> battleEquips = info.getBetterEquip().get(mentorType);
        if (CheckNull.isNull(battleEquips)) {
            battleEquips = new HashMap<>();
            info.getBetterEquip().put(mentorType, battleEquips);
        }
        int keyId = battleEquips.getOrDefault(type, 0); // 之前更好的装备
        if (checkEquipBetter(keyId, equip.getKeyId(), info, mentorType)) { // 更好的装备
            battleEquips.put(type, equip.getKeyId());
        }
    }

    /**
     * 检测装备是否更好
     *
     * @param key1 装备1的key
     * @param key2 装备2的key
     * @param info
     * @param type 教官的类型
     * @return
     */
    private boolean checkEquipBetter(int key1, int key2, MentorInfo info, int type) {
        boolean better = false;
        Mentor mentor = info.getMentors().get(type);
        if (CheckNull.isNull(mentor)) { // 如果没有教官, 就在获取的时候检测一次
            return better;
        }
        int lv = mentor.getLv();
        MentorEquip oldEq = info.getEquipMap().get(key1);
        MentorEquip newEq = info.getEquipMap().get(key2);
        if (lv >= newEq.getLv()) { // 装备2等级小于等于教官等级
            if (key1 == 0 || CheckNull.isNull(oldEq)) { // 找不到装备1
                better = true;
            } else if (newEq.getFight() > oldEq.getFight()) { // 装备2的评分高与装备1
                better = true;
            }
        }
        return better;
    }

    /**
     * 检测所有的装备
     *
     * @param player
     * @param mentor
     */
    public void checkAllBetterEquip(Player player, Mentor mentor) {
        int lv = mentor.getLv();
        MentorInfo info = player.getMentorInfo();
        // 检测之前清除掉这个map, 因为强制贩卖绕过了之前的规则
        Map<Integer, Integer> map = info.getBetterEquip().get(mentor.getType());
        if (!CheckNull.isNull(map)) {
            map.clear();
        }
        info.getEquipMap().values().stream()
                .filter(equip -> equip.getLv() <= lv && equip.getMentorType() == mentor.getType()).collect(Collectors
                .groupingBy(MentorEquip::getType, Collectors
                        .collectingAndThen(Collectors.maxBy(Comparator.comparingInt(MentorEquip::getFight)),
                                Optional::get))).values().forEach(equip -> checkBetterEquip(player, equip));

    }

    /**
     * 教官PB
     * @param mentor
     * @param mentorInfo
     * @return
     */
    public CommonPb.Mentor createMentorPb(Mentor mentor, MentorInfo mentorInfo) {
        CommonPb.Mentor.Builder builder = CommonPb.Mentor.newBuilder();
        int type = mentor.getType();
        builder.setId(mentor.getId());
        builder.setType(type);
        builder.setLv(mentor.getLv());
        builder.setExp(mentor.getExp());
        builder.setFight(mentor.getFight());
        StaticMentor sMentor = StaticMentorDataMgr.getsMentorIdMap(mentor.getId());
        if (!CheckNull.isNull(sMentor)) {
            for (Map.Entry<Integer, Integer> en : sMentor.getAttrUp().entrySet()) {
                builder.addAttr(PbHelper.createTwoIntPb(en.getKey(), en.getValue()));
            }
        }
        for (Map.Entry<Integer, Integer> en : mentor.getExtAttr().entrySet()) {
            builder.addExtAttr(PbHelper.createTwoIntPb(en.getKey(), en.getValue()));
        }
        int equipId;
        for (int pos = 0; pos < mentor.getEquips().length; pos++) {
            equipId = mentor.getEquips()[pos];
            if (equipId > 0) {
                builder.addEquip(PbHelper.createTwoIntPb(pos, equipId));
            }
        }
        List<StaticMentor> sMentors = StaticMentorDataMgr.getsMentorTypeMap(type);
        if (!CheckNull.isEmpty(sMentors)) {
            List<StaticMentor> upAwardList = sMentors.stream()
                    .filter(staticMentor -> !CheckNull.isEmpty(staticMentor.getAward())).distinct()
                    .collect(Collectors.toList());
            Map<Integer, Boolean> upAward = mentor.getUpAward();
            for (StaticMentor staticMentor : upAwardList) {
                int id = staticMentor.getId();
                int state = upAward.containsKey(id) && upAward.get(id) ?
                        MentorConstant.MENTOR_UPAWARD_HAS_GAIN :
                        MentorConstant.MENTOR_UPAWARD_NOT_HAS_GAIN;
                builder.addUpAward(PbHelper.createTwoIntPb(id, state));
            }
        }
        int skillType;
        for (int pos = 0; pos < mentor.getSkills().length; pos++) {
            skillType = mentor.getSkills()[pos];
            builder.addSkills(createMentorSkillPb(pos, skillType, mentorInfo));
        }
        builder.setBetterEquip(checkEquipCanUp(mentor, mentorInfo));
        return builder.build();
    }

    /**
     * 教官技能PBment
     *
     * @param pos
     * @param type
     * @param mentorInfo
     * @return
     */
    public CommonPb.MentorSkill createMentorSkillPb(int pos, int type, MentorInfo mentorInfo) {
        CommonPb.MentorSkill.Builder builder = CommonPb.MentorSkill.newBuilder();
        builder.setPos(pos);
        if (type > 0) {
            MentorSkill skill = mentorInfo.getSkillMap().get(type);
            if (!CheckNull.isNull(skill)) {
                builder.setSkillId(skill.getId());
                builder.setType(skill.getType());
                builder.setLv(skill.getLv());
                builder.setIsActivate(skill.isActivate());
            }
        }
        return builder.build();
    }

    /**
     * 检测装备可以替换
     * @param mentor        教官
     * @param mentorInfo
     * @return
     */
    public boolean checkEquipCanUp(Mentor mentor, MentorInfo mentorInfo) {
        int type = mentor.getType();
        Map<Integer, Integer> betterEquips = mentorInfo.getBetterEquip().get(type);
        if (!CheckNull.isEmpty(betterEquips)) {
            for (int i = 1; i < mentor.getEquips().length; i++) {
                int oldKey = mentor.getEquips()[i];
                int newKey = betterEquips.getOrDefault(i, 0);
                if (oldKey != newKey) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检测贩卖装备id
     * @param ids
     * @param player
     * @return
     */
    public void checkSellEquipIds(List<Integer> ids, Player player) throws MwException {
        if (CheckNull.isEmpty(ids)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "贩卖教官装备时, 参数错误ids为NULL, roleId:", player.roleId);
        }
        MentorInfo info = player.getMentorInfo();
        for (int key : ids) {
            MentorEquip equip = info.getEquipMap().get(key);
            if (CheckNull.isNull(equip)) {
                throw new MwException(GameError.MENTOR_SELL_EQUIP_ERROR.getCode(), "贩卖装备Id错误, roleId:", player.roleId);
            }
            if (equip.getMentorId() > 0) {
                throw new MwException(GameError.MENTOR_SELL_EQUIP_ERROR.getCode(), "贩卖装备时, 装备被穿戴, roleId:", player.roleId);
            }
            int mentorType = equip.getMentorType();
            Mentor mentor = info.getMentors().get(mentorType);
            if (CheckNull.isNull(mentor)) {
                throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "贩卖装备时, 未找到指定教官, roleId:", player.roleId,
                        ", mentorType:", mentorType);
            }

            int pos = equip.getType();
            int oldKey = mentor.getEquips()[pos];
            if (mentor.getLv() < equip.getLv() || checkEquipBetter(oldKey, key, info, mentorType)) {
                throw new MwException(GameError.MENTOR_SELL_EQUIP_ERROR.getCode(), "贩卖装备时, 贩卖的装备比穿戴装备更好, roleId:", player.roleId,
                        ", mentorType:", mentorType);
            }
        }
    }
}
