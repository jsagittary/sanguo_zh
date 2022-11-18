package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMentorDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Mentor;
import com.gryphpoem.game.zw.resource.domain.p.MentorEquip;
import com.gryphpoem.game.zw.resource.domain.p.MentorInfo;
import com.gryphpoem.game.zw.resource.domain.p.MentorSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticMentor;
import com.gryphpoem.game.zw.resource.domain.s.StaticMentorEquip;
import com.gryphpoem.game.zw.resource.domain.s.StaticMentorSkill;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.HeroUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-27 11:38
 * @description: 教官相关功能
 * @modified By:
 */
@Service
public class MentorService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private MentorDataManager mentorDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private WorldScheduleService worldScheduleService;

    /**
     * 获取教官信息
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb4.GetMentorsRs getMentors(long roleId, GamePb4.GetMentorsRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检测并解锁教官
        checkAndOpenMentor(player);

        List<Integer> types = req.getTypeList();
        MentorInfo mentorInfo = player.getMentorInfo();

        Collection<Mentor> mentors;
        if (CheckNull.isEmpty(types)) {
            mentors = mentorInfo.getMentors().values();
        } else {
            mentors = mentorInfo.getMentors().values().stream().filter(m -> types.contains(m.getType()))
                    .collect(Collectors.toList());
        }

        GamePb4.GetMentorsRs.Builder builder = GamePb4.GetMentorsRs.newBuilder();
        for (Mentor mentor : mentors) {
            checkFunctionIsOpen(player, mentor.getType());
            builder.addMentor(mentorDataManager.createMentorPb(mentor, mentorInfo));
        }
        builder.setBill(player.getMixtureDataById(PlayerConstant.MENTOR_BILL));
        return builder.build();
    }

    /**
     * 获取装备信息
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb4.GetSpecialEquipsRs getSpecialEquips(long roleId, GamePb4.GetSpecialEquipsRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检测并解锁教官
        checkAndOpenMentor(player);

        MentorInfo mentorInfo = player.getMentorInfo();

        List<Integer> ids = req.getIdsList();

        Collection<MentorEquip> equips;
        if (CheckNull.isEmpty(ids)) {
            equips = mentorInfo.getEquipMap().values();
        } else {
            equips = mentorInfo.getEquipMap().values().stream().filter(e -> ids.contains(e.getKeyId()))
                    .collect(Collectors.toList());
        }

        GamePb4.GetSpecialEquipsRs.Builder builder = GamePb4.GetSpecialEquipsRs.newBuilder();
        for (MentorEquip equip : equips) {
            builder.addEquips(equip.createEquipPb());
        }

        return builder.build();
    }

    /**
     * 教官升级
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb4.MentorsQuickUpRs mentorsQuickUp(long roleId, GamePb4.MentorsQuickUpRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int type = req.getType();
        boolean useBill = req.getBill();

        // 检测教官功能解锁
        checkFunctionIsOpen(player, type);

        MentorInfo mentorInfo = player.getMentorInfo();

        Mentor mentor = mentorInfo.getMentors().get(type);
        if (CheckNull.isNull(mentor)) {
            throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "升级教官时, 未找到指定教官, roleId:", roleId,
                    ", mentorType:", type);
        }

        int lv = mentor.getLv();
        int maxLv = MentorConstant.MENTOR_MAX_LV.get(type);
        if (lv >= maxLv) {
            throw new MwException(GameError.MENTOR_IS_UNLOCK.getCode(), "升级教官时, 教官已达到最大等级, roleId:", roleId,
                    ", mentorType:", type);
        }

        List<Integer> conf = MentorConstant.upConfByType(type);
        if (CheckNull.isEmpty(conf)) {
            throw new MwException(GameError.MENTOR_CONFI_ERROR.getCode(), "升级教官时, 教官配置未找到, roleId:", roleId,
                    ", mentorType:", type);
        }

        List<Integer> propConf = MentorConstant.mentorUpPropConf(type);
        if (CheckNull.isEmpty(propConf)) {
            throw new MwException(GameError.MENTOR_CONFI_ERROR.getCode(), "升级教官时, 教官升级道具未定义, roleId:", roleId,
                    ", mentorType:", type);
        }

        int exp; // 获得的经验值
        if (useBill) { // 使用钞票
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.MENTOR_BILL, conf.get(2),
                    AwardFrom.MENTOR_UP_COST, true);
            exp = conf.get(3);
        } else { // 使用升级手册
            rewardDataManager.checkPropIsEnough(player, propConf.get(0), conf.get(0));
            rewardDataManager.subProp(player, propConf.get(0), conf.get(0), AwardFrom.MENTOR_UP_COST);
            exp = conf.get(1);
        }

        // 增加教官经验
        addMentorExp(mentor, exp, player);

        GamePb4.MentorsQuickUpRs.Builder builder = GamePb4.MentorsQuickUpRs.newBuilder();
        builder.setMentor(mentorDataManager.createMentorPb(mentor, mentorInfo));
        if (useBill) {
            builder.setBill(player.getMixtureDataById(PlayerConstant.MENTOR_BILL));
        }
        taskDataManager.updTask(player, TaskType.COND_MENTOR_UPLV_CNT, 1, mentor.getType());
        return builder.build();
    }

    /**
     * 教官技能升级
     *
     * @param roleId
     * @param type   教官技能type
     * @return
     * @throws MwException
     */
    public GamePb4.MentorsSkillUpRs mentorsSkillUp(long roleId, int type) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        MentorInfo mentorInfo = player.getMentorInfo();
        MentorSkill skill = mentorInfo.getSkillMap().get(type);
        if (CheckNull.isNull(skill)) {
            throw new MwException(GameError.MENTOR_SKILL_IS_UNLOCK.getCode(), "升级教官技能时, 技能未解锁, roleId:", roleId,
                    ", skillType:", type);
        }

        int lv = skill.getLv();
        int maxLv = MentorConstant.MENTOR_SKILL_MAX_LV.get(type);
        if (lv >= maxLv) {
            throw new MwException(GameError.MENTOR_MAX_LV.getCode(), "升级教官技能时, 已达到最大等级, roleId:", roleId,
                    ", skillType:", type);
        }

        StaticMentorSkill sMentorSkill = StaticMentorDataMgr.getMentorSkillByTypeAndLv(type, lv + 1);
        if (CheckNull.isNull(sMentorSkill)) {
            throw new MwException(GameError.MENTOR_CONFI_ERROR.getCode(), "升级教官技能时, 找不到配置, roleId:", roleId,
                    ", skillType:", type, ", lv", lv);
        }

        Mentor mentor = mentorInfo.getMentors().get(sMentorSkill.getMentorType());
        if (CheckNull.isNull(mentor)) {
            throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "升级教官技能时, 未找到指定教官, roleId:", roleId,
                    ", mentorType:", type);
        }

        // 检测教官功能解锁
        checkFunctionIsOpen(player, sMentorSkill.getMentorType());

        List<List<Integer>> cost = sMentorSkill.getCost();
        if (CheckNull.isEmpty(cost)) {
            throw new MwException(GameError.MENTOR_CONFI_ERROR.getCode(), "升级教官技能时, 教官升级道具未定义, roleId:", roleId,
                    ", mentorType:", sMentorSkill.getMentorType());
        }

        // 消耗对应的资源道具
        rewardDataManager.checkAndSubPlayerRes(player, cost, false, AwardFrom.MENTOR_SKILL_UP_COST);

        // 技能升级
        skill.upLevel(sMentorSkill);

        // 重新计算附加属性
        CalculateUtil.calcMentorExtAttr(mentor, player);

        // 空军教官和装甲师
        if (mentor.getType() == MentorConstant.MENTOR_TYPE_2 || mentor.getType() == MentorConstant.MENTOR_TYPE_3) {
            CalculateUtil.reCalcBattleHeroAttr(player);
        } else {
            // 重新计算该将领的属性值
            PartnerHero partnerHero = player.getBattleHeroByPos(skill.getPos());
            if (!HeroUtil.isEmptyPartner(partnerHero)) {
                CalculateUtil.processAttr(player, partnerHero.getPrincipalHero());
            }
        }

        GamePb4.MentorsSkillUpRs.Builder builder = GamePb4.MentorsSkillUpRs.newBuilder();
        builder.setSkill(mentorDataManager.createMentorSkillPb(skill.getPos(), type, mentorInfo));
        return builder.build();
    }

    /**
     * 领取教官奖励
     *
     * @param roleId
     * @param req
     * @throws MwException
     */
    public GamePb4.GetMentorAwardRs getMentorAward(long roleId, GamePb4.GetMentorAwardRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);


        int id = req.getId();
        StaticMentor sMentor = StaticMentorDataMgr.getsMentorIdMap(id);
        if (CheckNull.isNull(sMentor)) {
            throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "领取教官奖励时, 未找到指定教官, roleId:", roleId,
                    ", MentorId:", id);
        }

        int type = sMentor.getType();
        // 检测教官功能解锁
        checkFunctionIsOpen(player, type);

        MentorInfo mentorInfo = player.getMentorInfo();
        Mentor mentor = mentorInfo.getMentors().get(type);
        if (CheckNull.isNull(mentor)) {
            throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "领取教官奖励时, 未找到指定教官, roleId:", roleId,
                    ", mentorType:", type);
        }

        boolean status = mentor.getUpAward().getOrDefault(id, false);
        if (status) { // 已经领取了
            throw new MwException(GameError.MENTOR_AWARD_HAD_GOT.getCode(), "领取教官奖励时, 奖励已经领取了, roleId:", roleId,
                    ", mentorType:", type, ", mentorId:", id);
        }
        mentor.getUpAward().put(id, true);// 领取

        GamePb4.GetMentorAwardRs.Builder builder = GamePb4.GetMentorAwardRs.newBuilder();

        if (!CheckNull.isEmpty(sMentor.getAward())) {
            for (List<Integer> awrad : sMentor.getAward()) {
                builder.addAwards(rewardDataManager.addAwardSignle(player, awrad, AwardFrom.MENTOR_UP_AWARD));
            }
        }

        return builder.build();
    }

    /**
     * 贩卖教官装备
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb4.SellMentorEquipRs sellMentorEquip(long roleId, GamePb4.SellMentorEquipRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        List<Integer> ids = req.getIdsList().stream().distinct().collect(Collectors.toList());
        int type = req.getType(); // 要贩卖的数量, 1: 当前选中, 2: 默认50个

        if (type != MentorConstant.MENTOR_SELL_EQUIP_TYPE_3) { // 强制卖出全部, 不检测装备id
            mentorDataManager.checkSellEquipIds(ids, player); // 检测要贩卖的装备id
        }

        MentorInfo info = player.getMentorInfo();

        if (type == MentorConstant.MENTOR_SELL_EQUIP_TYPE_2) { // 贩卖50个
            int need = MentorConstant.MENTOR_SELL_EQUIP_CNT - ids.size();

            Map<String, MentorEquip> equipMap = info.getMentors().values().stream()
                    .flatMap( // 所有教官穿戴的装备(未穿戴位置不能贩卖)
                            mentor -> Arrays.stream(mentor.getEquips())
                                    .mapToObj(keyId -> info.getEquipMap().getOrDefault(keyId, null)))
                    .filter(equip -> !CheckNull.isNull(equip)).distinct()
                    .collect(Collectors.toMap((e) -> e.getMentorType() + "_" + e.getType(), e -> e));

            List<Integer> addKeys = info.getEquipMap().values().stream()
                    .filter(equip -> !ids.contains(equip.getKeyId())).filter(e -> { // 战力力低于穿戴装备的战斗力
                        MentorEquip equip = equipMap.get(e.getMentorType() + "_" + e.getType());
                        if (!CheckNull.isNull(equip) && e.getFight() <= equip.getFight()
                                && e.getKeyId() != equip.getKeyId()) {
                            return true;
                        }
                        return false;
                    }).sorted((e1, e2) -> e1.getFight() - e2.getFight()).limit(need).map(MentorEquip::getKeyId)
                    .collect(Collectors.toList());
            if (!CheckNull.isEmpty(addKeys)) {
                ids.addAll(addKeys);
            }

        } else if (type == MentorConstant.MENTOR_SELL_EQUIP_TYPE_3) { // 强制卖出全部,
            // 未穿戴的装备
            List<Integer> addKeys = info.getEquipMap().values().stream()
                    .filter(equip -> !ids.contains(equip.getKeyId())).filter(equip -> equip.getMentorId() == 0)
                    .map(MentorEquip::getKeyId).collect(Collectors.toList());
            if (!CheckNull.isEmpty(addKeys)) {
                ids.addAll(addKeys);
            }
        }
        List<CommonPb.Award> showAward = new ArrayList<>();
        for (int key : ids) {
            MentorEquip equip = info.getEquipMap().remove(key);
            if (!CheckNull.isNull(equip)) {
                StaticMentorEquip sEquip = StaticMentorDataMgr.getsMentorEquipIdMap(equip.getEquipId());
                if (!CheckNull.isNull(sEquip)) {
                    List<Integer> vendorPrice = sEquip.getVendorPrice();
                    if (!CheckNull.isEmpty(vendorPrice)) {
                        showAward.add(rewardDataManager.addAwardSignle(player, vendorPrice,
                                AwardFrom.MENTOR_SELL_EQUIP_AWARD));

                        LogUtil.debug("贩卖装备成功: roleId:", roleId, ", 贩卖类型type:", type, ", equip:", equip,
                                ", 获得奖励AwardType:", vendorPrice.get(0), ", awardId:", vendorPrice.get(1), ", count:",
                                vendorPrice.get(2));
                    }
                }
            }
        }

        // 检测所有教官的所有装备
        info.getMentors().forEach((k, v) -> {
            // 检测所有的装备有没有更好的
            mentorDataManager.checkAllBetterEquip(player, v);
        });

        GamePb4.SellMentorEquipRs.Builder builder = GamePb4.SellMentorEquipRs.newBuilder();
        builder.addAllAward(showAward);
        builder.addAllIds(ids);
        return builder.build();
    }

    /**
     * 自动穿戴装备
     *
     * @param roleId
     * @param type   教官类型
     * @return
     */
    public GamePb4.AutoWearEquipRs autoWearEquip(long roleId, int type) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        checkFunctionIsOpen(player, type);

        MentorInfo mentorInfo = player.getMentorInfo();
        Mentor mentor = mentorInfo.getMentors().get(type);
        if (CheckNull.isNull(mentor)) {
            throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "自动穿戴装备时, 未找到指定教官, roleId:", roleId,
                    ", mentorType:", type);
        }

        // 检测所有的装备有没有更好的
        mentorDataManager.checkAllBetterEquip(player, mentor);

        ArrayList<Integer> downList = new ArrayList<>();

        if (mentorDataManager.checkEquipCanUp(mentor, mentorInfo)) { // 如果有装备可以替换
            Map<Integer, Integer> betterEquip = mentorInfo.getBetterEquip().get(type);
            if (!CheckNull.isEmpty(betterEquip)) {
                for (Map.Entry<Integer, Integer> en : betterEquip.entrySet()) {
                    int pos = en.getKey();
                    int newKey = en.getValue();
                    if (newKey <= 0 || newKey >= Integer.MAX_VALUE || pos <= 0 || pos >= Integer.MAX_VALUE) { // 参数校验
                        continue;
                    }
                    int oldKey = mentor.getEquips()[pos];
                    if (oldKey != newKey) { // 穿戴新装备
                        MentorEquip oldEq = mentorInfo.getEquipMap().get(oldKey);
                        MentorEquip newEq = mentorInfo.getEquipMap().get(newKey);
                        if (CheckNull.isNull(newEq)) {
                            continue;
                        }
                        if (!CheckNull.isNull(oldEq)) { // 当前位置上有装备
                            oldEq.setMentorId(0);
                            downList.add(oldKey);
                        }
                        newEq.setMentorId(mentor.getId());
                        mentor.getEquips()[pos] = newKey;
                        LogUtil.debug("自动穿戴装备时, roleId:", roleId, ", newEq:", newEq, ", oldEq:", oldEq);
                    }
                }
            }
        }

        // 更新将领属性
        CalculateUtil.reCalcBattleHeroAttr(player);

        GamePb4.AutoWearEquipRs.Builder builder = GamePb4.AutoWearEquipRs.newBuilder();
        builder.setMentor(mentorDataManager.createMentorPb(mentor, mentorInfo));
        builder.addAllDownKey(downList);
        return builder.build();
    }

    /**
     * 教官专业技能激活
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.MentorActivateRs planeActivate(long roleId, int type) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        checkAndOpenMentor(player); // 检测并解锁教官

        MentorInfo info = player.getMentorInfo();

        MentorInfo mentorInfo = player.getMentorInfo();
        MentorSkill skill = mentorInfo.getSkillMap().get(type);
        if (CheckNull.isNull(skill)) {
            throw new MwException(GameError.MENTOR_SKILL_IS_UNLOCK.getCode(), "教官专业技能激活时, 技能未解锁, roleId:", roleId,
                    ", skillType:", type);
        }

        if (skill.isActivate()) {
            throw new MwException(GameError.MENTOR_SKILL_IS_ACTIVE.getCode(), "教官技能已经激活了, roleId:", roleId,
                    ", skillType:", type);
        }

        StaticMentorSkill sMentorSkill = StaticMentorDataMgr.getMentorSkillByTypeAndLv(type, 1);
        if (CheckNull.isNull(sMentorSkill)) {
            throw new MwException(GameError.MENTOR_CONFI_ERROR.getCode(), "教官专业技能激活时, 找不到配置, roleId:", roleId,
                    ", skillType:", type, ", lv", 1);
        }

        Mentor mentor = mentorInfo.getMentors().get(sMentorSkill.getMentorType());
        if (CheckNull.isNull(mentor)) {
            throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "教官专业技能激活时, 未找到指定教官, roleId:", roleId,
                    ", mentorType:", type);
        }

        rewardDataManager.checkAndSubPlayerRes(player, sMentorSkill.getActiveItem(), AwardFrom.MENTOR_SKILL_ACTIVATE);

        skill.setActivate(true);
        mentorInfo.getSkillMap().put(type, skill);

        // 重新计算附加属性
        CalculateUtil.calcMentorExtAttr(mentor, player);

        // 空军教官做特殊处理
        if (mentor.getType() == MentorConstant.MENTOR_TYPE_2) {
            CalculateUtil.reCalcBattleHeroAttr(player);
        } else {
            // 重新计算该将领的属性值
            PartnerHero partnerHero = player.getBattleHeroByPos(skill.getPos());
            if (!HeroUtil.isEmptyPartner(partnerHero)) {
                CalculateUtil.processAttr(player, partnerHero.getPrincipalHero());
            }
        }

        GamePb4.MentorActivateRs.Builder builder = GamePb4.MentorActivateRs.newBuilder();
        return builder.build();
    }

    /**
     * 增加教官经验
     *
     * @param mentor
     * @param addExp
     * @param player
     */
    private void addMentorExp(Mentor mentor, int addExp, Player player) {
        if (CheckNull.isNull(mentor) && addExp <= 0) {
            return;
        }
        StaticMentor sMentor = StaticMentorDataMgr.getsMentorIdMap(mentor.getId());
        if (CheckNull.isNull(sMentor)) {
            LogUtil.error("教官加经验，mentorId未配置, mentorId:", mentor.getId());
            return;
        }
        int preLv = mentor.getLv();
        int maxLv = MentorConstant.MENTOR_MAX_LV.get(sMentor.getType());
        addMentorExp(mentor, addExp, maxLv, player);
        if (preLv != mentor.getLv()) { // 升级后的处理
            // 检测并解锁教官技能
            checkMentorSkill(player.getMentorInfo().getSkillMap(), mentor);
            // 检测所有的装备有没有更好的
            mentorDataManager.checkAllBetterEquip(player, mentor);
        }
        CalculateUtil.reCalcBattleHeroAttr(player); // 更新将领属性
    }

    /**
     * 增加教官经验
     *
     * @param mentor
     * @param addExp
     * @param maxLv
     * @param player
     */
    private void addMentorExp(Mentor mentor, int addExp, int maxLv, Player player) {
        int type = mentor.getType();
        while (addExp > 0 && mentor.getLv() < maxLv) {
            StaticMentor sMentor = StaticMentorDataMgr.getsMentorByTypeAndLv(type, mentor.getLv() + 1);
            if (!CheckNull.isNull(sMentor)) {
                int need = sMentor.getExp();
                if (mentor.getExp() + addExp >= need) { // 升级
                    addExp -= need - mentor.getExp();
                    mentor.levelUp(sMentor);
                } else { // 不够升级
                    mentor.setExp(mentor.getExp() + addExp);
                    addExp = 0;
                }
            } else { // 已经升级到了最大等级了
                sMentor = StaticMentorDataMgr.getsMentorByTypeAndLv(type, mentor.getLv());
                if (!CheckNull.isNull(sMentor)) {
                    int max = sMentor.getExp();
                    mentor.setExp(max);
                    break;
                }
            }
        }
        // 重新计算附加属性
        CalculateUtil.calcMentorExtAttr(mentor, player);
    }

    /**
     * 检测并解锁教官
     *
     * @param player
     */
    private void checkAndOpenMentor(Player player) {
        if (!CheckNull.isEmpty(MentorConstant.UNSEAL_MENTOR_ID)) {
            MentorInfo mentorInfo = player.getMentorInfo();
            Map<Integer, Mentor> mentors = mentorInfo.getMentors();
            Map<Integer, MentorSkill> skillMap = mentorInfo.getSkillMap();
            for (int id : MentorConstant.UNSEAL_MENTOR_ID) {
                StaticMentor sMentor = StaticMentorDataMgr.getsMentorIdMap(id);
                if (!CheckNull.isNull(sMentor)) {
                    int type = sMentor.getType();
                    try {
                        checkFunctionIsOpen(player, type);
                    } catch (MwException e) { // 功能未解锁
                        continue;
                    }
                    Mentor mentor = mentors.get(type);
                    if (CheckNull.isNull(mentor)) {
                        mentor = new Mentor(sMentor);
                        CalculateUtil.calcMentorExtAttr(mentor, player); // 计算教官的战斗力
                        checkMentorSkill(skillMap, mentor); // 检测并解锁教官技能
                        mentors.put(type, mentor);
                        CalculateUtil.reCalcBattleHeroAttr(player); // 更新将领属性
                    }
                    // 检测所有的装备有没有更好的
                    mentorDataManager.checkAllBetterEquip(player, mentor);
                }
            }
        }
    }

    /**
     * 检测并解锁教官技能
     *
     * @param skillMap 技能容器
     * @param mentor   教官
     */
    public void checkMentorSkill(Map<Integer, MentorSkill> skillMap, Mentor mentor) {
        Map<Integer, List<List<Integer>>> unsealLv = MentorConstant.MENTOR_SKILL_UNSEAL.get(mentor.getType());
        int lv = mentor.getLv();
        if (!CheckNull.isEmpty(unsealLv) && unsealLv.containsKey(lv)) { // 当前等级教官有技能初始化
            List<List<Integer>> lists = unsealLv.get(lv);
            if (!CheckNull.isEmpty(lists)) {
                for (List<Integer> unseal : lists) {
                    int pos = unseal.get(0); // 技能位置
                    int id = unseal.get(1); // 技能id
                    StaticMentorSkill sMentorSkill = StaticMentorDataMgr.getsMentorSkillIdMap(id);
                    if (!CheckNull.isNull(sMentorSkill) && mentor.getSkills()[pos] != sMentorSkill.getType()) {
                        MentorSkill skill = new MentorSkill(sMentorSkill, pos);
                        int type = sMentorSkill.getType();
                        skillMap.put(type, skill);
                        mentor.onSkill(pos, type); // 教官激活技能
                    }
                }
            }
        }
    }

    /**
     * 检测教官功能解锁
     *
     * @param player
     * @param type
     * @throws MwException
     */
    public void checkFunctionIsOpen(Player player, int type) throws MwException {
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, MentorConstant.functionIdByType(type))) {
            throw new MwException(GameError.MENTOR_IS_UNLOCK.getCode(), "教官功能未解锁, roleId:", player.roleId,
                    ", mentorType:", type);
        } else {
            int worldTaskId = worldScheduleService.getCurrentSchduleId();
            if (type == MentorConstant.MENTOR_TYPE_3 && worldTaskId < 6) {
                throw new MwException(GameError.MENTOR_IS_UNLOCK.getCode(), "教官功能未解锁, roleId:", player.roleId,
                        ", mentorType:", type, ", WorldTaskId:", worldTaskId);
            }
        }
    }

}
