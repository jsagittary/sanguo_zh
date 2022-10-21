package com.gryphpoem.game.zw.service.session;

import com.gryphpoem.cross.constants.PlayerUploadTypeDefine;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.quartz.jobs.SeasonTalentJob;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.domain.s.StaticSeasonTalent;
import com.gryphpoem.game.zw.resource.domain.s.StaticSeasonTalentPlan;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.pojo.fight.AttrData;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.season.GlobalSeasonData;
import com.gryphpoem.game.zw.resource.pojo.season.SeasonTalent;
import com.gryphpoem.game.zw.resource.util.*;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * 赛季天赋
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-06-02 14:52
 */
@Service
public class SeasonTalentService {

    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private SeasonService seasonService;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private ServerSetting serverSetting;

    /**
     * 获取赛季天赋
     *
     * @param player
     * @throws MwException
     */
    public GamePb4.GetSeasonTalentRs getSeasonTalent(Player player) throws MwException {
        int curSeasonPlanId = seasonService.getCurSeasonPlanId();
        checkOpen(player, curSeasonPlanId);

        int curSeason = seasonService.getCurrSeason();
        SeasonTalent talent = player.getPlayerSeasonData().getSeasonTalent();
        if (curSeason != talent.getSeasonId()) {
            talent.reset(curSeason, curSeasonPlanId);
        }
        int resetCountWeek = getResetClassifierCountWeek(talent);
        GamePb4.GetSeasonTalentRs.Builder rsb = GamePb4.GetSeasonTalentRs.newBuilder();
        rsb.setTalent(PbHelper.createSeasonTalent(talent, resetCountWeek, getCurSeasonTalentPlanId()));
        return rsb.build();
    }

    /**
     * 选择天赋分类
     *
     * @param player
     * @throws MwException
     */
    public GamePb4.ChooseClassifierRs chooseClassifier(Player player, GamePb4.ChooseClassifierRq req) throws MwException {
        int curSeasonPlanId = seasonService.getCurSeasonPlanId();
        checkOpen(player, curSeasonPlanId);

        int classifier = req.getClassifier();//1-攻击, 2-防守, 3-种田
        long lordId = player.getLordId();
        if (classifier < SeasonConst.TALENT_CLASSIFIER_1 || classifier > SeasonConst.TALENT_CLASSIFIER_3) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId :", lordId);
        }

        int curSeason = seasonService.getCurrSeason();
        SeasonTalent talent = player.getPlayerSeasonData().getSeasonTalent();
        if (talent.getSeasonId() != curSeason) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "赛季ID 错误, roleId :", lordId);
        }
        if (talent.getClassifier() == classifier) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "已经是此天赋分类, roleId :", lordId);
        }
        if (!talent.isOpenTalent()) {
            throw new MwException(GameError.TALENT_NON_OPEN.getCode(), "赛季天赋未开启, roleId :", lordId);
        }

        //如果当前已经消耗了天赋石则扣除指定钻石并返还一定比率已经消耗的天赋石
        if (!talent.getLearns().isEmpty()) {
            //优先消耗道具重置
            List<Integer> propCost = Constant.SEASON_TALENT_RESET_COST_PROP;
            if (propCost == null || propCost.size() != 3) {
                throw new MwException(GameError.NO_CONFIG.getCode());
            }
            int resetClassifierCountWeek = getResetClassifierCountWeek(talent);
            Prop prop = player.props.get(propCost.get(1));
            int haveCount = Objects.nonNull(prop) ? prop.getCount() : 0;
            if (haveCount >= propCost.get(2)) {
                rewardDataManager.checkAndSubPlayerRes4List(player, Constant.SEASON_TALENT_RESET_COST_PROP, AwardFrom.SEASON_TALENT_RESET, curSeason);
            } else {
                LogUtil.debug(String.format("roleId :%d, 道具ID :%d, haveCount :%d, needCount :%d", lordId, propCost.get(1), haveCount, propCost.get(2)));
                List<Integer> diamondCost = Constant.SEASON_TALENT_RESET_COST_DIAMOND;
                if (ObjectUtils.isEmpty(diamondCost)) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId :%d, 配置文件错误", lordId));
                }
                //修改获取消耗钻石方式
                int costDiamond = 0;
                if (resetClassifierCountWeek >= diamondCost.size()) {
                    costDiamond = diamondCost.get(diamondCost.size() - 1);
                } else {
                    costDiamond = diamondCost.get(resetClassifierCountWeek);
                }

                if (costDiamond <= 0) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId :%d, 配置文件错误", lordId));
                }
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, costDiamond, AwardFrom.SEASON_TALENT_RESET, curSeason, resetClassifierCountWeek);
            }

            //返还天赋石
            int givePoint = (int) (talent.getCostStone() * (Constant.SEASON_TALENT_RESET_RETURN_RATE / Constant.TEN_THROUSAND));
            rewardDataManager.addAward(player, AwardType.SPECIAL, AwardType.Special.SEASON_TALENT_STONE, givePoint,
                    AwardFrom.SEASON_TALENT_RESET, curSeason, talent.getTotalStone(), talent.getCostStone());
            talent.setLastResetClassifierTime(System.currentTimeMillis());
            talent.setResetClassifierCountWeek(resetClassifierCountWeek + 1);
            talent.getLearns().clear();
            talent.setCostStone(0);
            CalculateUtil.reCalcAllHeroAttr(player);
        }
        GamePb4.ChooseClassifierRs.Builder rsb = GamePb4.ChooseClassifierRs.newBuilder();
        talent.setClassifier(classifier);
        rsb.setClassifier(classifier);
        rsb.setRemainStone(talent.getRemainStone());
        rsb.setResetClassifierCountWeek(talent.getResetClassifierCountWeek());
        return rsb.build();
    }

    private int getResetClassifierCountWeek(SeasonTalent talent) {
        long lastResetTime = talent.getLastResetClassifierTime();
        boolean isSameWeek = TimeHelper.isSameWeek(new Date(lastResetTime), new Date());
        return isSameWeek ? talent.getResetClassifierCountWeek() : 0;//本周重置的次数
    }

    /**
     * 获取天赋祝福概率
     *
     * @param seasonTalent
     * @return
     */
    private int getOpenTalentProbability(SeasonTalent seasonTalent) {
        Optional<List<Integer>> result = Constant.OPEN_TALENT_BLESS_PROBABILITY.stream().
                filter(list -> seasonTalent.getRemainStone() <= list.get(0) &&
                        seasonTalent.getRemainStone() >= list.get(1)).findFirst();

        return result.isPresent() ? result.get().get(2) : 0;
    }

    /**
     * 开启天赋
     *
     * @param player
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb4.OpenTalentRs openTalent(Player player, GamePb4.OpenTalentRq req) throws MwException {
        int curSeasonPlanId = seasonService.getCurSeasonPlanId();
        checkOpen(player, curSeasonPlanId);

        int curSeason = seasonService.getCurrSeason();
        long lordId = player.getLordId();
        SeasonTalent seasonTalent = player.getPlayerSeasonData().getSeasonTalent();
        if (seasonTalent.getSeasonId() != curSeason) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "赛季ID 错误, roleId :", lordId);
        }
        if (seasonTalent.isOpenTalent()) {
            throw new MwException(GameError.SEASON_TALENT_ALREADY_OPEN.getCode(), "天赋已开启, roleId :", lordId);
        }

        //首次开启不消耗天赋石，扣除消耗的天赋石
        if (seasonTalent.getOpenTalentProgress() > 0) {
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.SPECIAL, AwardType.Special.SEASON_TALENT_STONE, Constant.OPEN_TALENT_SINGLE_BLESS_CONSUME,
                    AwardFrom.SEASON_TALENT_STUDY, true, 1);
//            seasonTalent.setCostStone(seasonTalent.getCostStone() + Constant.OPEN_TALENT_SINGLE_BLESS_CONSUME);
        }

        boolean openTalent;
        if (seasonTalent.getOpenTalentProgress() >= Constant.OPEN_TALENT_BLESS_LIMIT) {
            openTalent = true;
        } else {
            openTalent = RandomHelper.isHitRangeIn10000(getOpenTalentProbability(seasonTalent));
        }

        seasonTalent.setOpenTalent(openTalent);
        seasonTalent.addOpenTalentProgress(Constant.FAIL_OPEN_TALENT_BLESS_FIXED_VALUE);

        GamePb4.OpenTalentRs.Builder builder = GamePb4.OpenTalentRs.newBuilder();
        builder.setOpen(seasonTalent.isOpenTalent());
        builder.setOpenTalentProgress(seasonTalent.getOpenTalentProgress());
        builder.setRemainStone(seasonTalent.getRemainStone());
        return builder.build();
    }

    /**
     * 天赋学习
     *
     * @param player
     * @throws MwException
     */
    public GamePb4.StudyTalentRs studyTalent(Player player, GamePb4.StudyTalentRq req) throws MwException {
        int curSeasonPlanId = seasonService.getCurSeasonPlanId();
        checkOpen(player, curSeasonPlanId);

        int uid = req.getTalentId();
        int curSeason = seasonService.getCurrSeason();
        long lordId = player.getLordId();
        SeasonTalent seasonTalent = player.getPlayerSeasonData().getSeasonTalent();
        if (seasonTalent.getSeasonId() != curSeason) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "赛季ID 错误, roleId :", lordId);
        }
        if (!seasonTalent.isOpenTalent()) {
            throw new MwException(GameError.TALENT_NON_OPEN.getCode(), "天赋未开启, roleId :", lordId);
        }

        Map<Integer, StaticSeasonTalent> sTalentMap = StaticIniDataMgr.getSeasonTalentMap();
        StaticSeasonTalent sTalent = sTalentMap != null ? sTalentMap.get(uid) : null;

        if (Objects.isNull(sTalent)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId :", lordId);
        }
        if (sTalent.getCost() < 0) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId :", lordId);
        }

        //已经习得的天赋
        if (seasonTalent.getLearns().contains(uid)) {
            throw new MwException(GameError.SEASON_TALENT_HAS_STUDIED.getCode(), "已经学习过该天赋 roleId :", lordId);
        }
        //只能学习选中分类的天赋
        if (sTalent.getClassifier() != seasonTalent.getClassifier()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId :", lordId);
        }
        //上一级未解锁
        if (CheckNull.nonEmpty(sTalent.getNeedTalent())) {
            for (Integer tid : sTalent.getNeedTalent()) {
                boolean unlock = false;
                StaticSeasonTalent sst0 = sTalentMap.get(tid);
                for (Integer learnId : seasonTalent.getLearns()) {
                    StaticSeasonTalent sst = sTalentMap.get(learnId);
                    if (sst.getTalentId() == sst0.getTalentId() && sst.getLv() >= sst0.getLv()) {
                        unlock = true;
                    }
                }
                if (!unlock) {
                    throw new MwException(GameError.SEASON_TALENT_UNLOCK_ERROR.getCode(), String.format("roleId :%d, talentId :%d, unlock !!!", lordId, tid));
                }
            }
        }

        if (sTalent.getTalentType() == SeasonConst.TALENT_TYPE_2) {
            //特殊技能具有排他性
            for (Integer learnId : seasonTalent.getLearns()) {
                StaticSeasonTalent sst = sTalentMap.get(learnId);
                if (sst.getTalentType() == SeasonConst.TALENT_TYPE_2 && sTalent.getTalentId() != sst.getTalentId()) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, 已经习得特殊技能 :%d, 特殊天赋技能同一时间只能学习一个!!! ", lordId, learnId));
                }
            }
        }

        //扣除消耗的天赋石
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.SPECIAL, AwardType.Special.SEASON_TALENT_STONE, sTalent.getCost(),
                AwardFrom.SEASON_TALENT_STUDY, true, uid);
        seasonTalent.setCostStone(seasonTalent.getCostStone() + sTalent.getCost());
        clearTalent(sTalentMap, seasonTalent, sTalent.getTalentId());
        seasonTalent.getLearns().add(uid);
        CalculateUtil.reCalcAllHeroAttr(player);
        GamePb4.StudyTalentRs.Builder rsb = GamePb4.StudyTalentRs.newBuilder();
        rsb.setTalentId(uid);
        rsb.setRemainStone(seasonTalent.getRemainStone());
        rsb.setCostStone(seasonTalent.getCostStone());

        List<Long> rolesId = new ArrayList<>();
        EventBus.getDefault().post(
                new Events.CrossPlayerChangeEvent(PlayerUploadTypeDefine.UPLOAD_TYPE_TALENT,
                        0, CrossFunction.CROSS_WAR_FIRE, rolesId));
        return rsb.build();
    }

    /**
     * 更换天赋技能
     *
     * @param player
     * @param req
     * @throws MwException
     */
    public GamePb4.ChangeTalentSkillRs changeTalentSkill(Player player, GamePb4.ChangeTalentSkillRq req) throws MwException {
        int curSeasonPlanId = seasonService.getCurSeasonPlanId();
        checkOpen(player, curSeasonPlanId);

        int curId = req.getCurId();
        int changeId = req.getChangeId();
        long lordId = player.getLordId();
        if (curId <= 0 || changeId <= 0 || curId == changeId) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId :", lordId);
        }

        int curSeason = seasonService.getCurrSeason();
        SeasonTalent seasonTalent = player.getPlayerSeasonData().getSeasonTalent();
        if (!seasonTalent.isOpenTalent()) {
            throw new MwException(GameError.TALENT_NON_OPEN.getCode(), "天赋未开启, roleId :", lordId);
        }
        if (seasonTalent.getSeasonId() != curSeason) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "赛季ID 错误, roleId :", lordId);
        }
        if (!seasonTalent.getLearns().contains(curId)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, 当前天赋技能ID :%d, 未习得", lordId, curId));
        }
        Map<Integer, StaticSeasonTalent> sTalentMap = StaticIniDataMgr.getSeasonTalentMap();
        StaticSeasonTalent sTalent = sTalentMap != null ? sTalentMap.get(changeId) : null;
        if (Objects.isNull(sTalent) || sTalent.getTalentType() != SeasonConst.TALENT_TYPE_2) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId :", lordId);
        }
        StaticSeasonTalent sCurTalent = sTalentMap.get(curId);
        if (Objects.isNull(sCurTalent) || sCurTalent.getTalentType() != SeasonConst.TALENT_TYPE_2) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId :", lordId);
        }

        //只能更换同等级的技能
        if (sCurTalent.getLv() != sTalent.getLv()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId :", lordId);
        }

        if (sCurTalent.getClassifier() != sTalent.getClassifier()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId :", lordId);
        }

        if (sTalent.getChangeCost() < 0) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId :", lordId);
        }

        //扣除消耗的天赋石
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.SPECIAL, AwardType.Special.SEASON_TALENT_STONE, sTalent.getChangeCost(),
                AwardFrom.SEASON_TALENT_SKILL_CHANGE, true, curId, changeId);

        clearTalent(sTalentMap, seasonTalent, sCurTalent.getTalentId());
        seasonTalent.getLearns().add(changeId);
        CalculateUtil.reCalcAllHeroAttr(player);
        GamePb4.ChangeTalentSkillRs.Builder rsb = GamePb4.ChangeTalentSkillRs.newBuilder();
        rsb.setRemainStone(seasonTalent.getRemainStone());

        List<Long> rolesId = new ArrayList<>();
        EventBus.getDefault().post(
                new Events.CrossPlayerChangeEvent(PlayerUploadTypeDefine.UPLOAD_TYPE_TALENT,
                        0, CrossFunction.CROSS_WAR_FIRE, rolesId));
        return rsb.build();
    }

    /**
     * 清除玩家习得的技能
     *
     * @param sTalentMap
     * @param seasonTalent
     * @param talentId
     */
    private void clearTalent(Map<Integer, StaticSeasonTalent> sTalentMap, SeasonTalent seasonTalent, int talentId) {
        //key1:天赋ID, KEY2:天赋等级
        Map<Integer, TreeMap<Integer, StaticSeasonTalent>> groupMap = sTalentMap.values().stream()
                .collect(Java8Utils.groupByMapTreeMap(StaticSeasonTalent::getTalentId, StaticSeasonTalent::getLv));
        TreeMap<Integer, StaticSeasonTalent> lvMap = groupMap.get(talentId);
        if (CheckNull.nonEmpty(lvMap) && !seasonTalent.getLearns().isEmpty()) {//清除该天赋ID下其它等级
            lvMap.forEach((k, v) -> seasonTalent.getLearns().remove(v.getId()));
        }
    }


    /**
     * 行军加速 天赋
     *
     * @param player
     * @param effect
     * @return
     */
    public int getSeasonTalentEffectValue(Player player, int effect) {
        if (!checkTalentBuffOpen(player)) {
            return 0;
        }

        SeasonTalent seasonTalent = player.getPlayerSeasonData().getSeasonTalent();
        Map<Integer, StaticSeasonTalent> sTalentMap = StaticIniDataMgr.getSeasonTalentMap();
        int effectValue = 0;
        for (Integer learnId : seasonTalent.getLearns()) {
            StaticSeasonTalent sTalent = sTalentMap.get(learnId);
            if (Objects.nonNull(sTalent) && sTalent.getEffect() == effect) {
                effectValue += sTalent.getEffectParam().get(0).get(0);
            }
        }

        return effectValue;
    }

    /**
     * 获取习得天赋id字符串
     *
     * @param player
     * @param effect
     * @return
     */
    public String getSeasonTalentIdStr(Player player, int effect) {
        List<Integer> talents = getSeasonTalentId(player, effect);
        if (CheckNull.isNull(talents)) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        talents.forEach(id -> {
            stringBuilder.append(id);
            if (talents.lastIndexOf(id) == talents.size() - 1) {
                return;
            }

            stringBuilder.append(",");
        });

        return stringBuilder.toString();
    }

    /**
     * 获取效果的天赋id
     *
     * @param player
     * @param effect
     * @return
     */
    public List<Integer> getSeasonTalentId(Player player, int effect) {
        if (!checkTalentBuffOpen(player)) {
            return null;
        }

        List<Integer> talents = null;
        SeasonTalent seasonTalent = player.getPlayerSeasonData().getSeasonTalent();
        Map<Integer, StaticSeasonTalent> sTalentMap = StaticIniDataMgr.getSeasonTalentMap();
        for (Integer learnId : seasonTalent.getLearns()) {
            StaticSeasonTalent sTalent = sTalentMap.get(learnId);
            if (Objects.nonNull(sTalent) && sTalent.getEffect() == effect) {
                talents = Optional.ofNullable(talents).orElse(new ArrayList<>());
                talents.add(learnId);
            }
        }

        return talents;
    }

    /**
     * 添加属性,
     *
     * @param player
     * @param effect
     * @param buffType 不同buff类型
     * @param type     不同功能类型
     * @return
     */
    public int getSeasonTalentEffectValueByFunc(Player player, int effect, int buffType, int type) {
        if (!checkTalentBuffOpen(player)) {
            return 0;
        }

        SeasonTalent seasonTalent = player.getPlayerSeasonData().getSeasonTalent();
        Map<Integer, StaticSeasonTalent> sTalentMap = StaticIniDataMgr.getSeasonTalentMap();
        int effectValue = 0;
        for (Integer learnId : seasonTalent.getLearns()) {
            StaticSeasonTalent sTalent = sTalentMap.get(learnId);
            if (Objects.isNull(sTalent)) {
                continue;
            }
            if (sTalent.getEffect() != effect) {
                continue;
            }
            for (List<Integer> list : sTalent.getEffectParam()) {
                if (type == list.get(0) && buffType == list.get(1)) {
                    effectValue += list.get(2);
                }
            }
        }
        return effectValue;
    }

    /**
     * 根据类型获得数值
     *
     * @param player
     * @param effect
     * @param type
     * @return
     */
    public int getSeasonTalentEffectValueByType(Player player, int effect, int type) {
        if (!checkTalentBuffOpen(player)) {
            return 0;
        }

        SeasonTalent seasonTalent = player.getPlayerSeasonData().getSeasonTalent();
        Map<Integer, StaticSeasonTalent> sTalentMap = StaticIniDataMgr.getSeasonTalentMap();
        int effectValue = 0;
        for (Integer learnId : seasonTalent.getLearns()) {
            StaticSeasonTalent sTalent = sTalentMap.get(learnId);
            if (Objects.isNull(sTalent)) {
                continue;
            }
            if (sTalent.getEffect() != effect) {
                continue;
            }
            for (List<Integer> list : sTalent.getEffectParam()) {
                if (type == list.get(0)) {
                    effectValue += list.get(1);
                }
            }
        }

        return effectValue;
    }

    /**
     * 获取当前驻守英雄的属性值加成
     *
     * @param player
     * @param effect
     * @return
     */
    public List<CommonPb.TwoInt> getSeasonTalentEffectTwoInt(Player player, Hero hero, int effect) {
        if (!checkTalentBuffOpen(player) || CheckNull.isNull(hero)) {
            return null;
        }

        List<CommonPb.TwoInt> result = null;
        Map<Integer, Integer> attrMap = null;
        SeasonTalent seasonTalent = player.getPlayerSeasonData().getSeasonTalent();
        Map<Integer, StaticSeasonTalent> sTalentMap = StaticIniDataMgr.getSeasonTalentMap();
        for (Integer learnId : seasonTalent.getLearns()) {
            StaticSeasonTalent sTalent = sTalentMap.get(learnId);
            if (Objects.isNull(sTalent)) {
                continue;
            }
            if (sTalent.getEffect() != effect) {
                continue;
            }
            for (List<Integer> list : sTalent.getEffectParam()) {
                if (result == null) {
                    result = new ArrayList<>();
                }

                CommonPb.TwoInt twoInt = calAttrValue(hero.getAttr(), list);
                if (!CheckNull.isNull(twoInt)) {
                    result.add(twoInt);
                }
            }
        }

        return result;
    }

    /**
     * 根据战斗属性添加赛季天赋属性
     *
     * @param player
     * @param attrData
     * @param effect
     * @return
     */
    public void getSeasonTalentAttrDataEffect(Player player, Player defender, AttrData attrData, int effect) {
        if (!checkTalentBuffOpen(player)) {
            return;
        }
        if (CheckNull.isNull(defender) || player.getLordId() != defender.getLordId()) {
            return;
        }

        SeasonTalent seasonTalent = player.getPlayerSeasonData().getSeasonTalent();
        Map<Integer, StaticSeasonTalent> sTalentMap = StaticIniDataMgr.getSeasonTalentMap();
        for (Integer learnId : seasonTalent.getLearns()) {
            StaticSeasonTalent sTalent = sTalentMap.get(learnId);
            if (Objects.isNull(sTalent)) {
                continue;
            }
            if (sTalent.getEffect() != effect) {
                continue;
            }
            for (List<Integer> list : sTalent.getEffectParam()) {
                calAttrValue(attrData, list);
            }
        }
    }

    /**
     * 计算战斗角色的当前属性加成
     *
     * @param attrData
     * @param list     [属性类型，百分比/固定值(指定类型)，加成数值]
     * @return
     */
    private void calAttrValue(AttrData attrData, List<Integer> list) {
        if (ObjectUtils.isEmpty(list) || ObjectUtils.isEmpty(attrData)) {
            return;
        }

        switch (list.get(0)) {
            case Constant.AttrId.ATK_MUT:
                if (attrData.attack == 0) {
                    return;
                }

                attrData.addValue(Constant.AttrId.ATTACK, (int) (attrData.attack * (1 + (list.get(2) / Constant.TEN_THROUSAND))));
                break;
            case Constant.AttrId.ATTACK:
                if (attrData.attack == 0) {
                    return;
                }
                switch (list.get(1)) {
                    case SeasonConst.PERCENTAGE:
                        attrData.addValue(Constant.AttrId.ATTACK, (int) (attrData.attack * (1 + (list.get(2) / Constant.TEN_THROUSAND))));
                        break;
                    case SeasonConst.FIXED_VALUE:
                        attrData.addValue(Constant.AttrId.ATTACK, list.get(2));
                        break;
                    default:
                        break;
                }
                break;
            case Constant.AttrId.DEFEND:
                if (attrData.defend == 0) {
                    return;
                }
                switch (list.get(1)) {
                    case SeasonConst.PERCENTAGE:
                        attrData.addValue(Constant.AttrId.DEFEND, (int) (attrData.defend * (1 + (list.get(2) / Constant.TEN_THROUSAND))));
                        break;
                    case SeasonConst.FIXED_VALUE:
                        attrData.addValue(Constant.AttrId.DEFEND, list.get(2));
                        break;
                    default:
                        break;
                }
                break;
            default:
                return;
        }
    }

    /**
     * 攻击百分比换算攻击力
     *
     * @param attrMap
     * @param list    [属性类型，百分比/固定值类型，数值]
     * @return
     */
    private CommonPb.TwoInt calAttrValue(int[] attrMap, List<Integer> list) {
        if (ObjectUtils.isEmpty(list) || ObjectUtils.isEmpty(attrMap)) {
            return null;
        }

        CommonPb.TwoInt twoInt = null;
//        switch (list.get(0)) {
//            case Constant.AttrId.ATTACK:
//            case Constant.AttrId.DEFEND:
//            case Constant.AttrId.LEAD:

        switch (list.get(1)) {
            case SeasonConst.PERCENTAGE:
                twoInt = PbHelper.createTwoIntPb(list.get(0),
                        (int) (attrMap[list.get(0)] * (list.get(2) / Constant.TEN_THROUSAND)));
                break;
            case SeasonConst.FIXED_VALUE:
                twoInt = PbHelper.createTwoIntPb(list.get(0), list.get(2));
                break;
            default:
                break;
        }
//                break;
//        }

        return twoInt;
    }

    /**
     * 和平主义: 每天前两次使用战争保护持续时间变为12小时
     *
     * @param player
     * @param sProp
     * @return
     */
    public void execSeasonTalentEffect501(Player player, StaticProp sProp, Effect effect) {
        if (!checkTalentBuffOpen(player)) {
            return;
        }

        SeasonTalent seasonTalent = player.getPlayerSeasonData().getSeasonTalent();
        //学习的是 种田天赋
        if (seasonTalent.getClassifier() == SeasonConst.TALENT_CLASSIFIER_3) {//有学习技能
            Map<Integer, StaticSeasonTalent> sTalentMap = StaticIniDataMgr.getSeasonTalentMap();
            for (Integer learnId : seasonTalent.getLearns()) {
                StaticSeasonTalent sTalent = sTalentMap.get(learnId);
                if (Objects.nonNull(sTalent)//not null
                        && sTalent.getTalentType() == SeasonConst.TALENT_TYPE_2 //特殊技能
                        && sTalent.getEffect() == SeasonConst.TALENT_EFFECT_501) {//和平主义效果
                    List<Integer> effectParams = sTalent.getEffectParam().get(0);
                    if (effectParams.get(0) == sProp.getPropId()) {//战争保护的道具ID判断
                        int effect501Count = getEffect501Count(seasonTalent);//今日已经生效的次数
                        if (effect501Count < effectParams.get(1)) {
                            effect.setEndTime(effect.getEndTime() + effectParams.get(2));//有且只有一个特殊技能, 并返回增加的保护时间
                            seasonTalent.setEffect501Count(effect501Count + 1);
                            seasonTalent.setLastEffect501Time(System.currentTimeMillis());
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取市场扩容效果
     *
     * @param player
     * @return v0:单次兑换减少的CD时间, v1:兑换累计时间增加的上限
     */
    public int[] getSeasonTalentEffect503Value(Player player) {
        if (!checkTalentBuffOpen(player)) {
            return new int[]{0, 0};
        }

        SeasonTalent seasonTalent = player.getPlayerSeasonData().getSeasonTalent();
        //学习的是 种田天赋
        if (seasonTalent.getClassifier() == SeasonConst.TALENT_CLASSIFIER_3) {//有学习技能
            Map<Integer, StaticSeasonTalent> sTalentMap = StaticIniDataMgr.getSeasonTalentMap();
            for (Integer learnId : seasonTalent.getLearns()) {
                StaticSeasonTalent sTalent = sTalentMap.get(learnId);
                if (Objects.nonNull(sTalent)//not null
                        && sTalent.getTalentType() == SeasonConst.TALENT_TYPE_2 //特殊技能
                        && sTalent.getEffect() == SeasonConst.TALENT_EFFECT_503) {//市场优化
                    List<Integer> effectParam1 = sTalent.getEffectParam().get(0);//减少单次兑换时间
                    List<Integer> effectParam2 = sTalent.getEffectParam().get(1);//增加最大兑换时间
                    return new int[]{effectParam1.get(0), effectParam2.get(0)};
                }
            }
        }

        return new int[]{0, 0};
    }

    /**
     * 赛季天赋伤病恢复
     */
    public void execSeasonTalentEffect303(Fighter fighter, Map<Long, List<CommonPb.Award>> recoverArmyAwardMap) {
        int seasonId = seasonService.getCurrSeason();
        if (seasonId <= 0) return;//赛季未开启
        if (fighter.roleType != Constant.Role.PLAYER) return;//判断是否是玩家
        //是否是城战
        if (!isCityBattle(fighter)) {
            return;
        }

        Map<Integer, StaticSeasonTalent> sTalentMap = StaticIniDataMgr.getSeasonTalentMap();
        for (Force force : fighter.forces) {
            if (force.totalLost <= 0) continue;//没有战斗损兵
            Player player = force.ownerId > 0 ? playerDataManager.getPlayer(force.ownerId) : null;
            //打小号,合服后清除跳出
            if (!checkTalentBuffOpen(player)) return;  //天赋校验未通过
            for (Integer learnId : player.getPlayerSeasonData().getSeasonTalent().getLearns()) {
                StaticSeasonTalent sTalent = sTalentMap.get(learnId);
                if (sTalent.getEffect() == SeasonConst.TALENT_EFFECT_303) {
                    int recovery = (int) (force.totalLost * (sTalent.getEffectParam().get(0).get(0) / Constant.TEN_THROUSAND));
                    if (recovery <= 0) continue;
                    List<CommonPb.Award> awards = recoverArmyAwardMap.computeIfAbsent(force.ownerId, t -> new ArrayList<>());
                    Hero hero = player.heros.get(force.id);
                    if (CheckNull.isNull(hero)) {
                        //NPC参与战斗
                        continue;
                    }

                    StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                    if (!CheckNull.isNull(staticHero)) {
                        int armyType = staticHero.getType();
                        //判断是否超过最大兵力
                        int max = hero.getAttr()[HeroConstant.ATTR_LEAD];//最大兵力
                        recovery = Math.min(recovery, max - hero.getCount());
                        hero.addArm(recovery);//返还兵力
                        awards.add(PbHelper.createAwardPb(AwardType.ARMY, staticHero.getType(), recovery));

                        // 上报玩家兵力变化
                        LogLordHelper.playerArm(
                                AwardFrom.SEASON_TALENT_ACTION,
                                player,
                                armyType,
                                Constant.ACTION_ADD,
                                recovery
                        );
                    }
                }
            }
        }

    }

    /**
     * 判断是否是城战
     *
     * @param fighter
     * @return
     */
    private boolean isCityBattle(Fighter fighter) {
        if (CheckNull.isNull(fighter)) {
            return false;
        }
        if (CheckNull.isNull(fighter.fightLogic)) {
            return false;
        }

        return fighter.fightLogic.getBattleType() == WorldConstant.BATTLE_TYPE_CITY;
    }

    /**
     * 今日和平主义生效次数
     *
     * @param talent
     * @return
     */
    private int getEffect501Count(SeasonTalent talent) {
        boolean isToday = DateHelper.dayiy(new Date(talent.getLastEffect501Time()), new Date()) == 1;
        return isToday ? talent.getEffect501Count() : 0;
    }


    public void checkSeasonTalentConfig() throws MwException {
        Map<Integer, StaticSeasonTalent> talentMap = StaticIniDataMgr.getSeasonTalentMap();
        for (Map.Entry<Integer, StaticSeasonTalent> entry : talentMap.entrySet()) {
            int uid = entry.getKey();
            StaticSeasonTalent sTalent = entry.getValue();
            if (sTalent.getLv() == 0) continue;
            List<List<Integer>> effectParam = sTalent.getEffectParam();
            if (CheckNull.isEmpty(effectParam)) {
                throw new MwException(GameError.NO_CONFIG.getCode(),
                        String.format("Season Talent uid :%d, config error", uid));
            }
            if (sTalent.getEffect() == SeasonConst.TALENT_EFFECT_101) {//属性加成
                for (List<Integer> param : effectParam) {
                    if (CheckNull.isEmpty(param) || param.size() != 2) {
                        throw new MwException(GameError.NO_CONFIG.getCode(),
                                String.format("Season Talent uid :%d, config error", uid));
                    }
                }
            } else if (sTalent.getEffect() == SeasonConst.TALENT_EFFECT_501) {
                if (effectParam.get(0).size() != 3) {//和平主义
                    throw new MwException(GameError.NO_CONFIG.getCode(),
                            String.format("Season Talent uid :%d, config error", uid));
                }
            } else if (sTalent.getEffect() == SeasonConst.TALENT_EFFECT_503) {
                if (effectParam.size() != 2 || effectParam.get(0).size() != 1 || effectParam.get(1).size() != 1) {//市场扩容
                    throw new MwException(GameError.NO_CONFIG.getCode(),
                            String.format("Season Talent uid :%d, config error", uid));
                }
            }

            switch (sTalent.getEffect()) {
                //配置校验
                case SeasonConst.TALENT_EFFECT_606:
                case SeasonConst.TALENT_EFFECT_619:
                case SeasonConst.TALENT_EFFECT_612:
                case SeasonConst.TALENT_EFFECT_604:
                    for (List<Integer> param : effectParam) {
                        if (CheckNull.isEmpty(param) || param.size() != 3) {
                            throw new MwException(GameError.NO_CONFIG.getCode(),
                                    String.format("Season Talent uid :%d, config error", uid));
                        }
                    }
                    break;
                case SeasonConst.TALENT_EFFECT_603:
                    for (List<Integer> param : effectParam) {
                        if (CheckNull.isEmpty(param) || param.size() != 2) {
                            throw new MwException(GameError.NO_CONFIG.getCode(),
                                    String.format("Season Talent uid :%d, config error", uid));
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 初始化定时器
     *
     * @param scheduler
     */
    public void initSchedule(Scheduler scheduler) {
        Date now = new Date();
        int serverId = serverSetting.getServerID();
        StaticIniDataMgr.getStaticSeasonTalentPlanList().stream().filter(tmp ->
                StaticSeasonTalentPlan.checkServerId(tmp.getServerId(), serverId)).forEach(tmp -> {
            this.addSeasonTalentJob(scheduler, tmp, now);
        });

        initOnStartUp(scheduler);
    }

    /**
     * 服务器开启，以免错过定时器时间
     */
    private void initOnStartUp(Scheduler scheduler) {
        //设置赛季天赋
        GlobalSeasonData globalSeasonData = getGlobalSeasonData();
        if (CheckNull.isNull(globalSeasonData)) {
            return;
        }

        Date now = new Date();
        int currSeasonTalentId = globalSeasonData.getCurrSeasonTalentId();
        //当前没有开放的赛季天赋，则根据配置设置当前赛季天赋
        if (currSeasonTalentId == 0) {
            StaticSeasonTalentPlan staticSeasonTalentPlan = StaticIniDataMgr.getOpenStaticSeasonTalentPlan(globalSeasonData.getCurrSeasonId());
            if (Objects.nonNull(staticSeasonTalentPlan))
                this.executeSeasonTalentBegin(staticSeasonTalentPlan.getId());
        } else {
            StaticSeasonTalentPlan staticSeasonTalentPlan = StaticIniDataMgr.getStaticSeasonTalentPlan(currSeasonTalentId);
            if (Objects.isNull(staticSeasonTalentPlan)) {
                LogUtil.error("当前赛季天赋ID找不到配置, currSeasonTalentId:", currSeasonTalentId, ", 当前ID对应定时器开始时间:",
                        QuartzHelper.getStartTime(scheduler, SeasonTalentJob.NAME_BEGIN + currSeasonTalentId, SeasonTalentJob.GROUP_SEASON),
                        ", 当前ID对应定时器结束时间:", QuartzHelper.getStartTime(scheduler, SeasonTalentJob.NAME_END + currSeasonTalentId, SeasonTalentJob.GROUP_SEASON));
                return;
            }

            if (now.after(staticSeasonTalentPlan.getEndTime())) {
                this.executeSeasonTalentEnd(staticSeasonTalentPlan.getId());
                staticSeasonTalentPlan = StaticIniDataMgr.getOpenStaticSeasonTalentPlan(globalSeasonData.getCurrSeasonId());
                if (Objects.nonNull(staticSeasonTalentPlan))
                    this.executeSeasonTalentBegin(staticSeasonTalentPlan.getId());
            }
        }
    }

    /**
     * 添加赛季天赋定时任务
     *
     * @param scheduler
     * @param tmp
     * @param now
     */
    private void addSeasonTalentJob(Scheduler scheduler, StaticSeasonTalentPlan tmp, Date now) {
        if (now.before(tmp.getBeginTime())) {
            QuartzHelper.removeJob(scheduler, SeasonTalentJob.NAME_BEGIN + tmp.getId(), SeasonTalentJob.GROUP_SEASON);
            QuartzHelper.addJob(scheduler, SeasonTalentJob.NAME_BEGIN + tmp.getId(), SeasonTalentJob.GROUP_SEASON, SeasonTalentJob.class, tmp.getBeginTime());
        }
        if (now.before(tmp.getEndTime())) {
            QuartzHelper.removeJob(scheduler, SeasonTalentJob.NAME_END + tmp.getId(), SeasonTalentJob.GROUP_SEASON);
            QuartzHelper.addJob(scheduler, SeasonTalentJob.NAME_END + tmp.getId(), SeasonTalentJob.GROUP_SEASON, SeasonTalentJob.class, tmp.getEndTime());
        }
    }

    /**
     * 执行赛季天赋开始
     *
     * @param seasonTalentId
     */
    public void executeSeasonTalentBegin(int seasonTalentId) {
        LogUtil.debug("========赛季天赋开始，不同season结算玩家邮件, 相同赛季直接开启");
        int curSeason = seasonService.getCurrSeason();
        if (curSeason == 0) {
            LogUtil.error("赛季旅程未开启, 赛季天赋无法开始!, seasonTalentId:", seasonTalentId);
            return;
        }

        //设置赛季天赋
        GlobalSeasonData globalSeasonData = getGlobalSeasonData();
        if (Objects.nonNull(globalSeasonData)) {
            int currSeasonTalentId = globalSeasonData.getCurrSeasonTalentId();
            if (currSeasonTalentId != 0) {
                LogUtil.error("赛季天赋当前赛季天赋id已开启过, currSeasonTalentId:", currSeasonTalentId);
            }
        }

        setCurSeasonTalentId(seasonTalentId);
        //活动开始时结算邮件
        int now = TimeHelper.getCurrentSecond();
        for (Map.Entry<String, Player> entry : playerDataManager.getAllPlayer().entrySet()) {
            Player player = entry.getValue();
            SeasonTalent talent = player.getPlayerSeasonData().getSeasonTalent();
            if (talent.getSeasonId() != curSeason && !talent.getLearns().isEmpty()) {
                // 邮件通知
                CommonPb.Award awardPb = PbHelper.createAwardPb(AwardType.SPECIAL, AwardType.Special.SEASON_TALENT_STONE, talent.getCostStone());
                List<CommonPb.Award> finalAwards = new ArrayList<>(1);
                finalAwards.add(awardPb);
                mailDataManager.sendAttachMail(player, finalAwards, MailConstant.RESET_END_SEASON_TALENT,
                        AwardFrom.RESET_END_SEASON_TALENT, now, talent.getCostStone());

                LogUtil.debug("赛季天赋开始，结算玩家邮件  roleId:", player.roleId, ", talent.getCostStone():", talent.getCostStone());
            }

            //重置玩家赛季id
            int curSeasonPlanId = seasonService.getCurSeasonPlanId();
            if (talent.getSeasonId() != curSeason) {
                //不同season，重置玩家数据
                talent.reset(curSeason, curSeasonPlanId);
                LogUtil.debug("赛季天赋开始，重置玩家数据  roleId:", player.roleId, ", curSeason:", curSeason, ", curSeasonPlanId:", curSeasonPlanId);
            } else {
                //重新计算玩家战斗力
                if (!talent.getLearns().isEmpty()) {
                    CalculateUtil.reCalcBattleHeroAttr(player);
                }
            }
        }

        seasonService.syncSeasonInfo();
        LogUtil.debug("========赛季天赋开始, id=" + seasonTalentId);
    }

    private GlobalSeasonData getGlobalSeasonData() {
        return DataResource.getBean(GlobalDataManager.class).getGameGlobal().getGlobalSeasonData();
    }

    private void setCurSeasonTalentId(int seasonTalentId) {
        getGlobalSeasonData().setCurrSeasonTalentId(seasonTalentId);
    }

    public int getCurSeasonTalentPlanId() {
        GlobalSeasonData globalSeasonData = getGlobalSeasonData();
        if (Objects.nonNull(globalSeasonData)) {
            return globalSeasonData.getCurrSeasonTalentId();
        }

        return 0;
    }

    /**
     * 赛季活动结束时重新计算玩家战力
     */
    public void executeSeasonTalentEnd(int talentPlanId) {
        try {
            LogUtil.debug("========赛季天赋结束=======, 结算当前talentPlanId:", talentPlanId);
            GlobalSeasonData globalSeasonData = getGlobalSeasonData();
            if (Objects.nonNull(globalSeasonData)) {
                int currSeasonTalentId = globalSeasonData.getCurrSeasonTalentId();
                if (currSeasonTalentId == 0) {
                    LogUtil.error("当前seasonTalentPlan已结算, talentPlanId:", talentPlanId);
                    return;
                }
            }

            int currSeason = seasonService.getCurrSeason();
            if (currSeason == 0) {
                LogUtil.error("当前赛季旅程已结束!, talentPlanId:", talentPlanId);
            }

            //重新计算玩家战斗力
            for (Map.Entry<String, Player> entry : playerDataManager.getAllPlayer().entrySet()) {
                Player player = entry.getValue();
                SeasonTalent talent = player.getPlayerSeasonData().getSeasonTalent();
                if (!talent.getLearns().isEmpty()) {
                    CalculateUtil.reCalcBattleHeroAttr(player);
                }
            }

            //设置赛季天赋
            setCurSeasonTalentId(0);
            seasonService.syncSeasonInfo();
            LogUtil.debug("========赛季天赋结束=======, 结算当前talentPlanId:", talentPlanId, " 完毕！");
        } catch (Exception e) {
            LogUtil.error("========赛季天赋结算当前talentPlanId:", talentPlanId, ", 报错, e:", e);
        }
    }

    /**
     * 校验天赋功能是否开启
     *
     * @param player
     * @param curSeasonPlanId
     * @throws MwException
     */
    private void checkOpen(Player player, int curSeasonPlanId) throws MwException {
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_SEASON_TALENT)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "赛季天赋未开放, roleId :", player.getLordId());
        }

        if (seasonService.getCurrSeason() <= 0) {
            throw new MwException(GameError.SEASON_NON_OPEN.getCode(), "赛季未开启, roleId :", player.getLordId());
        }

        StaticSeasonTalentPlan staticSeasonTalentPlan = StaticIniDataMgr.getOpenStaticSeasonTalentPlan(curSeasonPlanId);
        if (Objects.isNull(staticSeasonTalentPlan)) {
            throw new MwException(GameError.SEASON_TALENT_PLAN_NON_OPEN.getCode(), "赛季天赋没有配置, roleId :", player.getLordId());
        }
//        if (!staticSeasonTalentPlan.isOpen()) {
//            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "赛季天赋未开放, roleId :", player.getLordId());
//        }
    }

    public Set<Integer> getSeasonTalentLearns(Player player) {
        if (!checkTalentBuffOpen(player))
            return null;

        return player.getPlayerSeasonData().getSeasonTalent().getLearns();
    }

    /**
     * 检验增益是否添加
     *
     * @param player
     * @return
     */
    public boolean checkTalentBuffOpen(Player player) {
        if (CheckNull.isNull(player)) {
            return false;
        }
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_SEASON_TALENT)) {
            return false;
        }

        int curSeasonPlanId = seasonService.getCurSeasonPlanId();
        if (curSeasonPlanId <= 0) {
            return false;
        }
        StaticSeasonTalentPlan staticSeasonTalentPlan = StaticIniDataMgr.getOpenStaticSeasonTalentPlan(curSeasonPlanId);
        if (Objects.isNull(staticSeasonTalentPlan)) {
            return false;
        }
        SeasonTalent seasonTalent = player.getPlayerSeasonData().getSeasonTalent();
        if (!seasonTalent.isOpenTalent() || seasonTalent.getSeasonId() != seasonService.getCurrSeason()) {
            return false;
        }
        if (CheckNull.isEmpty(seasonTalent.getLearns())) {
            return false;
        }
        return true;
    }

}
