package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.manager.WarDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.constant.HeroUpgradeConstant;
import com.gryphpoem.game.zw.resource.constant.LogParamConstant;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroEvolve;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroUpgrade;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.TalentData;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.SuperMine;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.hero.HeroBiographyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description: ?????????????????????
 * Author: zhangpeng
 * createTime: 2022-06-17 13:56
 */
@Component
public class HeroUpgradeService implements GmCmdService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private HeroService heroService;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private WorldDataManager worldDataManager;

    /**
     * ??????????????????
     *
     * @param roleId
     * @param heroId
     * @return
     */
    public GamePb5.UpgradeHeroRs upgradeHero(long roleId, int heroId) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Hero hero = player.heros.get(heroId);
        if (CheckNull.isNull(hero)) {
            throw new MwException(GameError.HERO_NOT_FOUND, String.format("player:%d, not own this hero, heroId:%d", roleId, heroId));
        }
        // ???????????????
        if (!hero.isIdle()) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "heroUpgrade????????????????????????, roleId:", roleId, ", heroId:",
                    heroId, ", state:", hero.getState());
        }

        StaticHeroUpgrade preStaticData = StaticHeroDataMgr.getStaticHeroUpgrade(hero.getGradeKeyId());
        if (CheckNull.isNull(preStaticData) || (HeroConstant.ALL_HERO_GRADE_CAPS.get(0) <= preStaticData.getGrade() && HeroConstant.ALL_HERO_GRADE_CAPS.get(1) <= preStaticData.getLevel()))
            throw new MwException(GameError.MAX_UPGRADE_CONFIG, String.format("player:%d, has access max config, keyId:%d", hero.getGradeKeyId()));
        if (CheckNull.isEmpty(preStaticData.getConsume()))
            throw new MwException(GameError.CONFIG_FORMAT_ERROR, String.format("player:%d upgrade hero, consume list is empty, curKeyId:%d", roleId, preStaticData.getKeyId()));
        StaticHeroUpgrade staticData = StaticHeroDataMgr.getNextLvHeroUpgrade(heroId, hero.getGradeKeyId());
        if (CheckNull.isNull(staticData) || staticData.getKeyId() == hero.getGradeKeyId()) {
            throw new MwException(GameError.NO_CONFIG, String.format("player:%d, no next level config, heroId:%d, keyId:%d", roleId, heroId, hero.getGradeKeyId()));
        }
        checkCondition(player, preStaticData.getCondition(), staticData.getKeyId(), hero);
        checkConsume(player, preStaticData.getConsume(), staticData.getKeyId());
        hero.setGradeKeyId(staticData.getKeyId());
        CalculateUtil.processAttr(player, hero);

        // ????????????????????????, ???????????????
        if (HeroConstant.ALL_HERO_GRADE_CAPS.get(0) <= staticData.getGrade() && HeroConstant.ALL_HERO_GRADE_CAPS.get(1) <= staticData.getLevel()) {
            chatDataManager.sendSysChat(ChatConst.CHAT_HERO_FULL_GRADE, player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), heroId);
        }
        taskDataManager.updTask(player, TaskType.COND_998, 1, hero.getGradeKeyId());
        GamePb5.UpgradeHeroRs.Builder builder = GamePb5.UpgradeHeroRs.newBuilder();
        builder.setHero(PbHelper.createHeroPb(hero, player));

        // ??????????????????????????????
        LogUtil.getLogThread().addCommand(() -> LogLordHelper.gameLog(LogParamConstant.HERO_UPGRADE, player,
                AwardFrom.UPGRADE_HERO, heroId, preStaticData.getGrade(), preStaticData.getLevel(), staticData.getGrade(), staticData.getLevel()));
        return builder.build();
    }

    // /**
    //  * ??????????????????
    //  *
    //  * @param roleId ??????id
    //  * @param heroId ??????id
    //  * @param type   ???????????????, ?????? 1, ?????? 2, ?????? 3
    //  * @return ????????????????????????
    //  * @throws MwException ???????????????
    //  */
    // public GamePb5.StudyHeroTalentRs studyHeroTalent(long roleId, int heroId, int type, int index) throws MwException {
    //     // ??????????????????
    //     Player player = playerDataManager.checkPlayerIsExist(roleId);
    //
    //     // ??????????????????------------------
    //     Hero hero = heroService.checkHeroIsExist(player, heroId);
    //     if (!hero.isIdle()) {
    //         throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "?????????????????????, roleId:", roleId, ", heroId:", heroId,
    //                 ", state:", hero.getState());
    //     }
    //     StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
    //     if (sHero == null) {
    //         throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????, roleId:", player.roleId, ", heroId:",
    //                 heroId);
    //     }
    //     if (CheckNull.isEmpty(sHero.getEvolveGroup()) || sHero.getEvolveGroup().size() < index) {
    //         throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????, roleId:", player.roleId, ", heroId:",
    //                 heroId);
    //     }
    //
    //     if (type == HeroConstant.AWAKEN_HERO_TYPE_1 || type == HeroConstant.AWAKEN_HERO_TYPE_2) {
    //         // ??????????????????, ????????????????????????
    //         if (hero.getDecorated() < index) {
    //             throw new MwException(GameError.HERO_INSUFFICIENT_AWAKENING_CONDITIONS.getCode(), "????????????????????????, roleId:", player.roleId, ", heroId:",
    //                     heroId);
    //         }
    //         // ???????????????????????????, ?????????????????????????????????????????????????????????
    //         if (index > 1) {
    //             int count = index;
    //             while (--count >= 1) {
    //                 AwakenData awakenData = hero.getAwaken().get(count);
    //                 if (CheckNull.isNull(awakenData) || !awakenData.isActivate() || awakenData.curPart() != 0) {
    //                     throw new MwException(GameError.PRE_TALENT_NOT_UNLOCKED.getCode(), "?????????????????????, roleId:", player.roleId, ", heroId:",
    //                             heroId);
    //                 }
    //             }
    //         }
    //     } else {
    //         // ?????????, ???????????????????????????????????????
    //         if (CheckNull.isEmpty(hero.getAwaken()) || CheckNull.isNull(hero.getAwaken().values().stream().
    //                 filter(awakenData -> awakenData.lastPart() != 0).findFirst().orElse(null))) {
    //             throw new MwException(GameError.AWAKEN_HERO_REGROUP_ERROR.getCode(), "??????????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
    //         }
    //     }
    //
    //     // ????????????????????????
    //     AwakenData awaken = hero.getAwaken().get(index);
    //     if (type == HeroConstant.AWAKEN_HERO_TYPE_1) {
    //         // ????????????????????????????????????, ??????????????????
    //         if (CheckNull.isNull(awaken)) {
    //             awaken = new AwakenData(index);
    //             hero.getAwaken().put(index, awaken);
    //         }
    //     }
    //     // ???????????????????????????????????????????????????
    //     if (type == HeroConstant.AWAKEN_HERO_TYPE_2) {
    //         if (CheckNull.isNull(awaken)) {
    //             throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
    //         }
    //     }
    //
    //     // ???????????????
    //     int curPart = 0;
    //     // ????????????
    //     boolean activate = true;
    //     if (type != HeroConstant.AWAKEN_HERO_TYPE_3) {
    //         activate = awaken.isActivate();
    //         // ???????????????
    //         curPart = awaken.curPart();
    //     }
    //
    //     List<StaticHeroEvolve> heroEvolve = StaticHeroDataMgr.getHeroEvolve(sHero.getEvolveGroup().get(index - 1));
    //     if (CheckNull.isEmpty(heroEvolve)) {
    //         throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????, roleId:", player.roleId, ", heroId:",
    //                 heroId);
    //     }
    //
    //     // ?????? 1, ?????? 2, ?????? 3
    //     if (type == HeroConstant.AWAKEN_HERO_TYPE_1) {
    //         if (activate) {
    //             throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
    //         }
    //         // ?????????????????????, ???????????????????????????
    //         rewardDataManager.checkAndSubPlayerRes(player, sHero.getActivateConsume(), AwardFrom.AWAKEN_HERO_ACTIVE_CONSUME, heroId);
    //         // ?????????
    //         AwakenData finalAwaken = awaken;
    //         Stream.iterate(HeroConstant.AWAKEN_PART_MIN, part -> ++part).limit(HeroConstant.AWAKEN_PART_MAX).forEach(part -> finalAwaken.getEvolutionGene().put(part, 0));
    //         awaken.setStatus(1);
    //     } else if (type == HeroConstant.AWAKEN_HERO_TYPE_2) {
    //         int finalCurPart = curPart;
    //         StaticHeroEvolve sHeroEvolve = heroEvolve.stream().filter(he -> he.getPart() == finalCurPart).findFirst().orElse(null);
    //         if (CheckNull.isNull(sHeroEvolve)) {
    //             throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????, roleId:", player.roleId, ", heroId:",
    //                     heroId);
    //         }
    //         if (!activate) {
    //             throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
    //         }
    //         if (curPart == 0) {
    //             throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "??????????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
    //         }
    //         // ?????????????????????, ????????????????????????1
    //         rewardDataManager.checkAndSubPlayerRes(player, sHeroEvolve.getConsume(), AwardFrom.AWAKEN_HERO_ACTIVE_CONSUME, heroId, curPart);
    //         awaken.getEvolutionGene().put(curPart, 1);
    //     } else if (type == HeroConstant.AWAKEN_HERO_TYPE_3) {
    //         // ??????????????????????????????
    //         hero.getAwaken().values().forEach(awakenData -> {
    //             if (CheckNull.isNull(awakenData))
    //                 return;
    //             // ?????????????????????, ?????????????????????
    //             int lastPart_ = awakenData.lastPart();
    //             rewardDataManager.checkAndSubPlayerRes(player, sHero.getRecombination(), AwardFrom.AWAKEN_HERO_REGROUP_CONSUME, heroId, lastPart_);
    //             // ?????????
    //             Stream.iterate(HeroConstant.AWAKEN_PART_MIN, part -> ++part).limit(HeroConstant.AWAKEN_PART_MAX).forEach(part -> awakenData.getEvolutionGene().put(part, 0));
    //             List<List<Integer>> awards = new ArrayList<>();
    //             heroEvolve.stream().filter(he -> he.getPart() >= HeroConstant.AWAKEN_PART_MIN && he.getPart() <= lastPart_).forEach(she -> {
    //                 List<List<Integer>> consume = she.getConsume();
    //                 awards.addAll(consume);
    //             });
    //             if (!CheckNull.isEmpty(awards)) {
    //                 List<List<Integer>> mergeAward = RewardDataManager.mergeAward(awards);
    //                 mergeAward = Objects.requireNonNull(mergeAward).stream().peek(award -> award.set(2, (int) (award.get(2) * (HeroConstant.HERO_REGROUP_AWARD_NUM / Constant.TEN_THROUSAND)))).collect(Collectors.toList());
    //                 // ?????????????????????
    //                 rewardDataManager.sendReward(player, mergeAward, AwardFrom.AWAKEN_HERO_REGROUP_CONSUME, heroId, lastPart_);
    //             }
    //         });
    //     }
    //     // ??????????????????
    //     CalculateUtil.processAttr(player, hero);
    //     GamePb5.StudyHeroTalentRs.Builder builder = GamePb5.StudyHeroTalentRs.newBuilder();
    //     builder.setHero(PbHelper.createHeroPb(hero, player));
    //     return builder.build();
    // }

    /**
     * ???????????????????????????
     *
     * @param roleId ??????id
     * @param heroId ??????id
     * @param type   ???????????????, ?????? 1, ?????? 3
     * @return ????????????????????????
     * @throws MwException ???????????????
     */
    public GamePb5.StudyHeroTalentRs activateOrClearHeroTalent(long roleId, int heroId, int type, int index) throws MwException {
        // ??????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // ??????????????????------------------
        Hero hero = heroService.checkHeroIsExist(player, heroId);
        if (!hero.isIdle()) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "?????????????????????, roleId:", roleId, ", heroId:", heroId, ", state:", hero.getState());
        }
        StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (sHero == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????, roleId:", player.roleId, ", heroId:", heroId);
        }
        if (CheckNull.isEmpty(sHero.getEvolveGroup()) || sHero.getEvolveGroup().size() < index) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
        }

        // ?????????????????????????????????
        TalentData talentDataMap = hero.getTalent().get(index);

        // ?????????????????????????????????????????????
        List<StaticHeroEvolve> sHeroEvolveList = StaticHeroDataMgr.getHeroEvolve(sHero.getEvolveGroup().get(index - 1));
        if (CheckNull.isEmpty(sHeroEvolveList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????, roleId:", player.roleId, ", heroId:", heroId);
        }

        if (type == HeroConstant.TALENT_HERO_TYPE_1) {
            // ?????????, ????????????????????????
            if (hero.getDecorated() < index) {
                throw new MwException(GameError.HERO_INSUFFICIENT_AWAKENING_CONDITIONS.getCode(), "????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
            }
            // ??????????????????, ?????????????????????????????????????????????????????????
            if (index > 1) {
                int count = index;
                while (--count >= 1) {
                    TalentData preTalentDataMap = hero.getTalent().get(count);
                    if (CheckNull.isNull(preTalentDataMap) || !preTalentDataMap.isActivate() || !preTalentDataMap.isAllPartActivated()) {
                        // TODO ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????1????????????????????????????????????
                        throw new MwException(GameError.PRE_TALENT_NOT_UNLOCKED.getCode(), "?????????????????????, roleId:", player.roleId, ", heroId:", heroId);
                    }
                }
            }

            // ????????????????????????????????????, ??????????????????
            if (CheckNull.isNull(talentDataMap) || !talentDataMap.isActivate()) {
                int maxPart;
                switch (hero.getQuality()) {
                    case HeroConstant.QUALITY_PURPLE_HERO:
                        maxPart = HeroConstant.TALENT_PART_MAX_OF_PURPLE_HERO;
                        break;
                    case HeroConstant.QUALITY_ORANGE_HERO:
                        maxPart = HeroConstant.TALENT_PART_MAX_OF_ORANGE_HERO;
                        break;
                    default:
                        throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
                }
                talentDataMap = new TalentData(1, index, maxPart);
                hero.getTalent().put(index, talentDataMap);
            } else {
                if (talentDataMap.isActivate()) {
                    throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
                }
            }

            // ?????????????????????, ???????????????????????????
            rewardDataManager.checkAndSubPlayerRes(player, sHero.getActivateConsume(), AwardFrom.AWAKEN_HERO_ACTIVE_CONSUME, heroId);
        }

        if (type == HeroConstant.TALENT_HERO_TYPE_3) {
            // ?????????
            if (CheckNull.isEmpty(hero.getTalent())) {
                throw new MwException(GameError.AWAKEN_HERO_REGROUP_ERROR.getCode(), "??????????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
            }

            // ??????????????????????????????
            hero.getTalent().values().forEach(talentData_ -> {
                // ?????????????????????????????????
                if (CheckNull.isNull(talentData_) || !talentData_.isActivate()) {
                    return;
                }
                // ?????????????????????????????????????????????
                if (talentData_.getTalentArr().entrySet().stream().filter(talentPart ->talentPart.getValue() > 0).count() < 1) {
                    return;
                }
                if (talentData_.getMaxPart() <= 0) {
                    int maxPart;
                    switch (hero.getQuality()) {
                        case HeroConstant.QUALITY_PURPLE_HERO:
                            maxPart = HeroConstant.TALENT_PART_MAX_OF_PURPLE_HERO;
                            break;
                        case HeroConstant.QUALITY_ORANGE_HERO:
                            maxPart = HeroConstant.TALENT_PART_MAX_OF_ORANGE_HERO;
                            break;
                        default:
                            throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
                    }
                    talentData_.setMaxPart(maxPart);
                }
                // ?????????????????????, ?????????????????????
                int indexTemp = talentData_.getIndex();
                rewardDataManager.checkAndSubPlayerRes(player, sHero.getRecombination(), AwardFrom.AWAKEN_HERO_REGROUP_CONSUME, heroId, indexTemp);
                // ????????????????????????????????????
                List<List<Integer>> hadConsumeList = new ArrayList<>();
                talentData_.getTalentArr().forEach((part_, lv_) -> {
                    if (lv_ > 0) {
                        sHeroEvolveList.stream()
                                .filter(she -> she.getPart() == part_ && (she.getLv() >= 0 && she.getLv() < lv_))
                                .forEach(she -> {
                                    List<List<Integer>> consume = she.getConsume();
                                    hadConsumeList.addAll(consume);
                                });
                    }
                });
                // ???????????????????????????
                Stream.iterate(HeroConstant.TALENT_PART_MIN, part -> ++part).limit(talentData_.getMaxPart()).forEach(part -> talentData_.getTalentArr().put(part, 0));
                hero.getTalent().put(indexTemp, talentData_);
                // ???????????????????????????
                if (!CheckNull.isEmpty(hadConsumeList)) {
                    List<List<Integer>> mergeAward = RewardDataManager.mergeAward(hadConsumeList);
                    mergeAward = Objects.requireNonNull(mergeAward).stream().peek(award -> award.set(2, (int) (award.get(2) * (HeroConstant.HERO_REGROUP_AWARD_NUM / Constant.TEN_THROUSAND)))).collect(Collectors.toList());
                    rewardDataManager.sendReward(player, mergeAward, AwardFrom.AWAKEN_HERO_REGROUP_CONSUME, heroId, indexTemp);
                }
            });
        }

        // ??????????????????
        CalculateUtil.processAttr(player, hero);
        GamePb5.StudyHeroTalentRs.Builder builder = GamePb5.StudyHeroTalentRs.newBuilder();
        builder.setHero(PbHelper.createHeroPb(hero, player));
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param roleId    ??????id
     * @param heroId    ??????id
     * @param pageIndex ??????????????????s_hero_evolve???group???
     * @param part      ??????????????????????????????????????????????????????1???
     * @return
     * @throws MwException
     */
    public GamePb5.UpgradeHeroTalentRs upgradeHeroTalent(long roleId, int heroId, int pageIndex, int part) throws MwException {
        // ??????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // ??????????????????
        Hero hero = heroService.checkHeroIsExist(player, heroId);
        if (!hero.isIdle()) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "?????????????????????, roleId:", roleId, ", heroId:", heroId, ", state:", hero.getState());
        }
        StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (sHero == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????, roleId:", player.roleId, ", heroId:", heroId);
        }
        if (CheckNull.isEmpty(sHero.getEvolveGroup()) || sHero.getEvolveGroup().size() < pageIndex) { // s_hero???evolveGroup??????1???2???3...??????????????????
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
        }

        Integer heroTalentGroup = sHero.getEvolveGroup().get(pageIndex - 1);
        if ((hero.getQuality() == HeroConstant.QUALITY_PURPLE_HERO && heroTalentGroup >= 100) || (hero.getQuality() == HeroConstant.QUALITY_ORANGE_HERO && heroTalentGroup < 100)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????, roleId:", player.roleId, ", heroId:", heroId);
        }

        // ???????????????, ????????????????????????
        if (hero.getDecorated() < pageIndex) { // ??????????????????????????????
            throw new MwException(GameError.HERO_INSUFFICIENT_AWAKENING_CONDITIONS.getCode(), "????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
        }

        TalentData talentData = hero.getTalent().get(pageIndex);
        if (CheckNull.isNull(talentData) || !talentData.isActivate()) {
            throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "????????????????????????, roleId:", player.roleId, ", heroId:", heroId);
        }

        // ???????????????????????????????????????????????????????????????????????????????????????
        List<StaticHeroEvolve> staticHeroEvolveList = StaticHeroDataMgr.getHeroEvolve(heroTalentGroup).stream()
                .filter(staticHeroEvolve -> staticHeroEvolve.getPart() == part)
                .collect(Collectors.toList());
        // ???????????????????????????????????????
        Integer maxTargetTalentLv = staticHeroEvolveList.stream().max(Comparator.comparingInt(StaticHeroEvolve::getLv)).map(StaticHeroEvolve::getLv).orElse(null);
        if (maxTargetTalentLv == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????, roleId:", player.roleId, ", heroId:", heroId);
        }
        if (talentData.getTalentArr().get(part) >= maxTargetTalentLv) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "?????????????????????, roleId:", player.roleId, ", heroId:", heroId);
        }
        // ??????????????????????????????????????????
        Integer curLv = talentData.getTalentArr().get(part);
        StaticHeroEvolve curLvStaticHeroEvolve = staticHeroEvolveList.stream().filter(she -> she.getLv() == curLv).findFirst().orElse(null);
        if (curLvStaticHeroEvolve == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????????????????, roleId:", player.roleId, ", heroId:", heroId, "talentGroup:", pageIndex);
        }
        List<List<Integer>> consume = curLvStaticHeroEvolve.getConsume();
        // ???????????????????????????????????????1
        rewardDataManager.checkAndSubPlayerRes(player, consume, AwardFrom.AWAKEN_HERO_EVOLVE_CONSUME, heroId, part);
        talentData.upgradeTalent(part);

        // ??????????????????????????????????????????????????????????????????
        int size = (int) talentData.getTalentArr().entrySet().stream().filter(talent -> {
            Integer partTemp = talent.getKey();
            Integer lvTemp = talent.getValue();
            Integer maxLvOfPart = StaticHeroDataMgr.getHeroEvolve(heroTalentGroup).stream()
                    .filter(she -> she.getPart() == partTemp)
                    .max(Comparator.comparingInt(StaticHeroEvolve::getLv))
                    .map(StaticHeroEvolve::getLv).orElse(null);
            if (maxLvOfPart == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????, roleId:", player.roleId, ", heroId:", heroId);
            }

            return lvTemp.compareTo(maxLvOfPart) < 0;
        }).count();
        if (size == 0) {
            talentData.setAllPartActivated(1);
        }

        // ??????????????????????????????
        LogUtil.getLogThread().addCommand(() -> LogLordHelper.gameLog(LogParamConstant.UPGRADE_HERO_TALENT, player,
                AwardFrom.UPGRADE_HERO_TALENT, heroId, hero.getDecorated(), pageIndex, part, talentData.getTalentArr().get(part)));

        // ??????????????????
        CalculateUtil.processAttr(player, hero);
        GamePb5.UpgradeHeroTalentRs.Builder builder = GamePb5.UpgradeHeroTalentRs.newBuilder();
        builder.setHero(PbHelper.createHeroPb(hero, player));
        return builder.build();
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param configList
     * @param gradeKeyId
     * @param hero
     * @throws MwException
     */
    private void checkCondition(Player player, List<List<Integer>> configList, int gradeKeyId, Hero hero) throws MwException {
        if (CheckNull.isEmpty(configList)) {
            throw new MwException(GameError.NO_CONFIG, String.format("player:%d, no next level condition config, gradeKeyId:%d",
                    player.roleId, gradeKeyId));
        }
        for (List<Integer> configTmp : configList) {
            if (CheckNull.isEmpty(configTmp))
                continue;
            HeroUpgradeConstant.Condition condition = HeroUpgradeConstant.Condition.convertTo(configTmp.get(0));
            if (CheckNull.isNull(condition))
                throw new MwException(GameError.CONFIG_FORMAT_ERROR, String.format("hero upgrade config error, roleId:%d, keyId:%d", player.roleId, gradeKeyId));
            switch (condition) {
                case LEVEL:
                    if (hero.getLevel() < configTmp.get(1)) {
                        throw new MwException(GameError.HERO_LEVEL_NOT_ENOUGH, String.format("hero level not enough, roleId:%d, level:%d, condition:%d, keyId:%d",
                                player.roleId, hero.getLevel(), configTmp.get(1), gradeKeyId));
                    }
                    break;
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param hero
     * @param attrId
     * @return
     */
    public int getGradeAttrValue(Hero hero, int attrId) {
        StaticHeroUpgrade staticData = StaticHeroDataMgr.getStaticHeroUpgrade(hero.getGradeKeyId());
        if (CheckNull.isNull(staticData) || CheckNull.isEmpty(staticData.getAttr()))
            return 0;
        List<Integer> attrs = staticData.getAttr().stream().filter(attrList -> attrList.get(0) == attrId).findFirst().orElse(null);
        if (CheckNull.isEmpty(attrs))
            return 0;
        return attrs.get(1);
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param configList
     * @param gradeKeyId
     * @throws MwException
     */
    public void checkConsume(Player player, List<List<Integer>> configList, int gradeKeyId) throws MwException {
        rewardDataManager.checkAndSubPlayerRes(player, configList, 1, AwardFrom.UPGRADE_HERO, String.valueOf(gradeKeyId));
    }

    @GmCmd("upgradeHero")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        if ("resetAllPurple".equalsIgnoreCase(params[0])) {
            Java8Utils.syncMethodInvoke(() -> {
                long startTime = System.nanoTime();
                HeroBiographyService service = DataResource.ac.getBean(HeroBiographyService.class);
                playerDataManager.getAllPlayer().values().forEach(p -> {
                    if (resetOneHeroGrade(p)) {
                        service.recalculateHeroBiography(p);
                        CalculateUtil.reCalcAllHeroAttr(p);
                    }
                });
                DataResource.ac.getBean(WarDataManager.class).clearAllBattle();
                LogUtil.debug("??????????????????????????????: ", (System.nanoTime() - startTime) / 100000);
            });
        }
        if ("resetOnePurple".equalsIgnoreCase(params[0])) {
            Java8Utils.syncMethodInvoke(() -> {
                HeroBiographyService service = DataResource.ac.getBean(HeroBiographyService.class);
                if (resetOneHeroGrade(player)) {
                    service.recalculateHeroBiography(player);
                    CalculateUtil.reCalcAllHeroAttr(player);
                }
            });
        }
    }

    /**
     * ????????????????????????
     *
     * @param p
     */
    private boolean resetOneHeroGrade(Player p) {
        if (CheckNull.isNull(p)) return false;
        if (CheckNull.isEmpty(p.heros)) return false;

        List<Hero> heroList = p.heros.values().stream().filter(hero ->
                hero.getQuality() == HeroConstant.QUALITY_PURPLE_HERO).collect(Collectors.toList());
        if (CheckNull.isEmpty(heroList)) return false;

        LogUtil.debug("--------------???????????????????????? ??????  roleId:", p.roleId);
        // ????????????????????????id
        int now = TimeHelper.getCurrentSecond();
        WarDataManager warDataManager = DataResource.ac.getBean(WarDataManager.class);
        WarService warService = DataResource.ac.getBean(WarService.class);
        WorldService worldService = DataResource.ac.getBean(WorldService.class);
        long roleId = p.roleId;
        for (Army army : p.armys.values()) {
            if (CheckNull.isNull(army)) continue;
            int armyState = army.getState();
            if (armyState == ArmyConstant.ARMY_STATE_COLLECT) {
                // ??????????????????????????????
                worldService.retreatSettleCollect(army, 0, p, now, roleId);
            }

            worldService.retreatArmy(p, army, now, ArmyConstant.MOVE_BACK_TYPE_2);
            LogUtil.debug("--------------??????????????????: ", army);
            int keyId = army.getKeyId();
            try {
                Integer battleId = army.getBattleId();
                if (null != battleId && battleId > 0) {
                    Battle battle = warDataManager.getBattleMap().get(battleId);
                    if (null != battle) {
                        int camp = p.lord.getCamp();
                        int armCount = army.getArmCount();
                        battle.updateArm(camp, -armCount);
                        if (battle.getType() == WorldConstant.BATTLE_TYPE_CITY) {
                            // ?????? ?????????
                            if (battle.getSponsor() != null && battle.getSponsor().roleId == roleId) {
                                // ????????????????????????
                                // ????????????????????????????????????????????????????????????
                                warService.cancelCityBattle(army.getTarget(), true, battle, true);
                            } else {
                                // ???????????????,??????battle?????????
                                worldService.removeBattleArmy(battle, roleId, keyId, battle.getAtkCamp() == camp);
                            }
                        } else if (battle.getType() == WorldConstant.BATTLE_TYPE_MINE_GUARD) {
                            //??????????????????, ???????????????????????????????????????
                            warDataManager.removeBattleByIdNoSync(battleId);
                        } else {// ?????????
                            worldService.removeBattleArmy(battle, roleId, keyId, battle.getAtkCamp() == camp);
                        }
                        HashSet<Integer> battleIds = p.battleMap.get(battle.getPos());
                        if (battleIds != null) {
                            battleIds.remove(battleId);
                        }
                    }
                } else {
                    if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT) {
                        worldDataManager.removeMineGuard(army.getTarget());
                    } else if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE) {
                        SuperMine sm = worldDataManager.getSuperMineMap().get(army.getTarget());
                        if (Objects.nonNull(sm)) {
                            sm.removeCollectArmy(p.roleId, keyId);
                        }
                    }
                }

                //??????????????????????????????????????????, ???????????????????????????
                if (armyState == ArmyConstant.ARMY_STATE_COLLECT) {
                    //???????????????????????????
                    worldService.cancelMineBattle(army.getTarget(), now, p);
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }

        for (int i = 1; i < p.heroBattle.length; i++) {
            int heroId = p.heroBattle[i];
            Hero hero = p.heros.get(heroId);
            if (hero != null) {
                hero.setState(HeroConstant.HERO_STATE_IDLE);
            }
            LogUtil.debug("--------------??????????????????????????? heroId: ", heroId);
        }
        for (int i = 1; i < p.heroAcq.length; i++) {
            int heroId = p.heroAcq[i];
            Hero hero = p.heros.get(heroId);
            if (hero != null) {
                hero.setState(HeroConstant.HERO_STATE_IDLE);
            }
            LogUtil.debug("--------------??????????????????????????? heroId: ", heroId);
        }
        LogUtil.debug("--------------???????????????????????? ??????  roleId:", p.roleId);

        // ??????????????????
        Map<Integer, Integer> heroFragment = new HashMap<>();
        heroList.forEach(hero -> {
            if (hero.getGradeKeyId() == 0) return;
            StaticHeroUpgrade staticHeroUpgrade = StaticHeroDataMgr.getInitHeroUpgrade(hero.getHeroId());
            if (CheckNull.isNull(staticHeroUpgrade)) {
                hero.setGradeKeyId(0);
                return;
            }
            Integer costNum = StaticHeroDataMgr.heroUpgradeCostFragment(hero.getHeroId(), hero.getGradeKeyId());
            if (Objects.nonNull(costNum) && costNum > 0) {
                heroFragment.put(hero.getHeroId(), costNum);
            }
            hero.setGradeKeyId(staticHeroUpgrade.getKeyId());
        });

        if (CheckNull.nonEmpty(heroFragment)) {
            heroFragment.forEach((heroId, count) -> rewardDataManager.addAwardSignle(p, AwardType.HERO_FRAGMENT, heroId, count, AwardFrom.DO_SOME));
            return true;
        }

        return false;
    }
}
