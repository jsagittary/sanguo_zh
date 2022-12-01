package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroEvolve;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroGradeInterior;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroUpgrade;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
import com.gryphpoem.game.zw.resource.pojo.hero.TalentData;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.SuperMine;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.hero.HeroBiographyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description: 英雄品阶与天赋
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
     * 升级英雄品阶
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
        // 非空闲状态
        if (!hero.isIdle()) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "heroUpgrade，将领不在空闲中, roleId:", roleId, ", heroId:",
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
        // 英雄升阶, 更新属性内政
        Map<Integer, Integer> interiorAttr = hero.getInteriorAttr();
        StaticHeroGradeInterior sHeroGradeInterior = StaticHeroDataMgr.getStaticHeroGradeInterior(staticData.getGrade(), staticData.getLevel());
        if (sHeroGradeInterior != null && CheckNull.nonEmpty(sHeroGradeInterior.getAttr())) {
            for (List<Integer> attr : sHeroGradeInterior.getAttr()) {
                if (CheckNull.isEmpty(attr) || attr.size() < 3) {
                    continue;
                }
                Map.Entry<Integer, Integer> entry = interiorAttr.entrySet().stream()
                        .filter(tmp -> tmp.getKey().intValue() == attr.get(0).intValue())
                        .findFirst()
                        .orElse(null);
                if (entry == null) {
                    interiorAttr.put(attr.get(0), attr.get(2));
                } else {
                    int newAttrValue = entry.getValue() + attr.get(2);
                    entry.setValue(newAttrValue);
                    interiorAttr.put(entry.getKey(), entry.getValue());
                }
            }
        }
        hero.setInteriorAttr(interiorAttr);
        CalculateUtil.processAttr(player, hero);

        // 若武将升级到满阶, 发送跑马灯
        if (HeroConstant.ALL_HERO_GRADE_CAPS.get(0) <= staticData.getGrade() && HeroConstant.ALL_HERO_GRADE_CAPS.get(1) <= staticData.getLevel()) {
            chatDataManager.sendSysChat(ChatConst.CHAT_HERO_FULL_GRADE, player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), heroId);
        }
        taskDataManager.updTask(player, TaskType.COND_998, 1, hero.getGradeKeyId());
        GamePb5.UpgradeHeroRs.Builder builder = GamePb5.UpgradeHeroRs.newBuilder();
        builder.setHero(PbHelper.createHeroPb(hero, player));

        // 添加升阶日志埋点打印
        LogUtil.getLogThread().addCommand(() -> LogLordHelper.gameLog(LogParamConstant.HERO_UPGRADE, player,
                AwardFrom.UPGRADE_HERO, heroId, preStaticData.getGrade(), preStaticData.getLevel(), staticData.getGrade(), staticData.getLevel()));
        return builder.build();
    }

    // /**
    //  * 学习武将天赋
    //  *
    //  * @param roleId 角色id
    //  * @param heroId 将领id
    //  * @param type   进行的行为, 激活 1, 进化 2, 重组 3
    //  * @return 操作后的将领数据
    //  * @throws MwException 自定义异常
    //  */
    // public GamePb5.StudyHeroTalentRs studyHeroTalent(long roleId, int heroId, int type, int index) throws MwException {
    //     // 角色是否存在
    //     Player player = playerDataManager.checkPlayerIsExist(roleId);
    //
    //     // 将领条件检测------------------
    //     Hero hero = heroService.checkHeroIsExist(player, heroId);
    //     if (!hero.isIdle()) {
    //         throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不在空闲中, roleId:", roleId, ", heroId:", heroId,
    //                 ", state:", hero.getState());
    //     }
    //     StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
    //     if (sHero == null) {
    //         throw new MwException(GameError.NO_CONFIG.getCode(), "将领找不到配置, roleId:", player.roleId, ", heroId:",
    //                 heroId);
    //     }
    //     if (CheckNull.isEmpty(sHero.getEvolveGroup()) || sHero.getEvolveGroup().size() < index) {
    //         throw new MwException(GameError.NO_CONFIG.getCode(), "英雄天赋组未找到, roleId:", player.roleId, ", heroId:",
    //                 heroId);
    //     }
    //
    //     if (type == HeroConstant.AWAKEN_HERO_TYPE_1 || type == HeroConstant.AWAKEN_HERO_TYPE_2) {
    //         // 进化与激活时, 校验武将觉醒层数
    //         if (hero.getDecorated() < index) {
    //             throw new MwException(GameError.HERO_INSUFFICIENT_AWAKENING_CONDITIONS.getCode(), "武将觉醒条件不足, roleId:", player.roleId, ", heroId:",
    //                     heroId);
    //         }
    //         // 激活或进化当前页签, 需要确认之前的天赋页签是否全部学习完毕
    //         if (index > 1) {
    //             int count = index;
    //             while (--count >= 1) {
    //                 AwakenData awakenData = hero.getAwaken().get(count);
    //                 if (CheckNull.isNull(awakenData) || !awakenData.isActivate() || awakenData.curPart() != 0) {
    //                     throw new MwException(GameError.PRE_TALENT_NOT_UNLOCKED.getCode(), "前置天赋未解锁, roleId:", player.roleId, ", heroId:",
    //                             heroId);
    //                 }
    //             }
    //         }
    //     } else {
    //         // 重组时, 武将没有激活或学习过的天赋
    //         if (CheckNull.isEmpty(hero.getAwaken()) || CheckNull.isNull(hero.getAwaken().values().stream().
    //                 filter(awakenData -> awakenData.lastPart() != 0).findFirst().orElse(null))) {
    //             throw new MwException(GameError.AWAKEN_HERO_REGROUP_ERROR.getCode(), "已经没部位可以重组了, roleId:", player.roleId, ", heroId:", heroId);
    //         }
    //     }
    //
    //     // 获取学习天赋数据
    //     AwakenData awaken = hero.getAwaken().get(index);
    //     if (type == HeroConstant.AWAKEN_HERO_TYPE_1) {
    //         // 若为非激活状态且数据为空, 则初始化数据
    //         if (CheckNull.isNull(awaken)) {
    //             awaken = new AwakenData(index);
    //             hero.getAwaken().put(index, awaken);
    //         }
    //     }
    //     // 进化校验武将是否有当前天赋页签数据
    //     if (type == HeroConstant.AWAKEN_HERO_TYPE_2) {
    //         if (CheckNull.isNull(awaken)) {
    //             throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "进化前需要先激活, roleId:", player.roleId, ", heroId:", heroId);
    //         }
    //     }
    //
    //     // 当前的部位
    //     int curPart = 0;
    //     // 激活状态
    //     boolean activate = true;
    //     if (type != HeroConstant.AWAKEN_HERO_TYPE_3) {
    //         activate = awaken.isActivate();
    //         // 当前的部位
    //         curPart = awaken.curPart();
    //     }
    //
    //     List<StaticHeroEvolve> heroEvolve = StaticHeroDataMgr.getHeroEvolve(sHero.getEvolveGroup().get(index - 1));
    //     if (CheckNull.isEmpty(heroEvolve)) {
    //         throw new MwException(GameError.NO_CONFIG.getCode(), "将领找不到配置, roleId:", player.roleId, ", heroId:",
    //                 heroId);
    //     }
    //
    //     // 激活 1, 进化 2, 重组 3
    //     if (type == HeroConstant.AWAKEN_HERO_TYPE_1) {
    //         if (activate) {
    //             throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "将领已经激活过了, roleId:", player.roleId, ", heroId:", heroId);
    //         }
    //         // 扣除激活的消耗, 将将领标识为已激活
    //         rewardDataManager.checkAndSubPlayerRes(player, sHero.getActivateConsume(), AwardFrom.AWAKEN_HERO_ACTIVE_CONSUME, heroId);
    //         // 初始化
    //         AwakenData finalAwaken = awaken;
    //         Stream.iterate(HeroConstant.AWAKEN_PART_MIN, part -> ++part).limit(HeroConstant.AWAKEN_PART_MAX).forEach(part -> finalAwaken.getEvolutionGene().put(part, 0));
    //         awaken.setStatus(1);
    //     } else if (type == HeroConstant.AWAKEN_HERO_TYPE_2) {
    //         int finalCurPart = curPart;
    //         StaticHeroEvolve sHeroEvolve = heroEvolve.stream().filter(he -> he.getPart() == finalCurPart).findFirst().orElse(null);
    //         if (CheckNull.isNull(sHeroEvolve)) {
    //             throw new MwException(GameError.NO_CONFIG.getCode(), "将领找不到配置, roleId:", player.roleId, ", heroId:",
    //                     heroId);
    //         }
    //         if (!activate) {
    //             throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "进化前需要先激活, roleId:", player.roleId, ", heroId:", heroId);
    //         }
    //         if (curPart == 0) {
    //             throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "已经没部位可以进化了, roleId:", player.roleId, ", heroId:", heroId);
    //         }
    //         // 扣除进化的消耗, 将部位状态标识为1
    //         rewardDataManager.checkAndSubPlayerRes(player, sHeroEvolve.getConsume(), AwardFrom.AWAKEN_HERO_ACTIVE_CONSUME, heroId, curPart);
    //         awaken.getEvolutionGene().put(curPart, 1);
    //     } else if (type == HeroConstant.AWAKEN_HERO_TYPE_3) {
    //         // 重置则重置所有天赋页
    //         hero.getAwaken().values().forEach(awakenData -> {
    //             if (CheckNull.isNull(awakenData))
    //                 return;
    //             // 扣除重组的消耗, 将部位状态清空
    //             int lastPart_ = awakenData.lastPart();
    //             rewardDataManager.checkAndSubPlayerRes(player, sHero.getRecombination(), AwardFrom.AWAKEN_HERO_REGROUP_CONSUME, heroId, lastPart_);
    //             // 初始化
    //             Stream.iterate(HeroConstant.AWAKEN_PART_MIN, part -> ++part).limit(HeroConstant.AWAKEN_PART_MAX).forEach(part -> awakenData.getEvolutionGene().put(part, 0));
    //             List<List<Integer>> awards = new ArrayList<>();
    //             heroEvolve.stream().filter(he -> he.getPart() >= HeroConstant.AWAKEN_PART_MIN && he.getPart() <= lastPart_).forEach(she -> {
    //                 List<List<Integer>> consume = she.getConsume();
    //                 awards.addAll(consume);
    //             });
    //             if (!CheckNull.isEmpty(awards)) {
    //                 List<List<Integer>> mergeAward = RewardDataManager.mergeAward(awards);
    //                 mergeAward = Objects.requireNonNull(mergeAward).stream().peek(award -> award.set(2, (int) (award.get(2) * (HeroConstant.HERO_REGROUP_AWARD_NUM / Constant.TEN_THROUSAND)))).collect(Collectors.toList());
    //                 // 发送重组的奖励
    //                 rewardDataManager.sendReward(player, mergeAward, AwardFrom.AWAKEN_HERO_REGROUP_CONSUME, heroId, lastPart_);
    //             }
    //         });
    //     }
    //     // 更新将领属性
    //     CalculateUtil.processAttr(player, hero);
    //     GamePb5.StudyHeroTalentRs.Builder builder = GamePb5.StudyHeroTalentRs.newBuilder();
    //     builder.setHero(PbHelper.createHeroPb(hero, player));
    //     return builder.build();
    // }

    /**
     * 激活或重置武将天赋
     *
     * @param roleId 角色id
     * @param heroId 将领id
     * @param type   进行的行为, 激活 1, 重置 3
     * @return 操作后的将领数据
     * @throws MwException 自定义异常
     */
    public GamePb5.StudyHeroTalentRs activateOrClearHeroTalent(long roleId, int heroId, int type, int pageIndex) throws MwException {
        // 角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 将领条件检测------------------
        Hero hero = heroService.checkHeroIsExist(player, heroId);
        if (!hero.isIdle()) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不在空闲中, roleId:", roleId, ", heroId:", heroId, ", state:", hero.getState());
        }
        StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (sHero == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "将领找不到配置, roleId:", player.roleId, ", heroId:", heroId);
        }
        if (CheckNull.isEmpty(sHero.getEvolveGroup()) || sHero.getEvolveGroup().size() < pageIndex) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "英雄天赋组未找到, roleId:", player.roleId, ", heroId:", heroId);
        }
        Integer heroTalentGroup = sHero.getEvolveGroup().get(pageIndex - 1);
        if ((hero.getQuality() == HeroConstant.QUALITY_PURPLE_HERO && heroTalentGroup >= 100) || (hero.getQuality() == HeroConstant.QUALITY_ORANGE_HERO && heroTalentGroup < 100)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "将领天赋组错误, roleId:", player.roleId, ", heroId:", heroId);
        }
        // 获取武将对应天赋页的天赋组配置
        List<StaticHeroEvolve> sHeroEvolveList = StaticHeroDataMgr.getHeroEvolve(heroTalentGroup);
        if (CheckNull.isEmpty(sHeroEvolveList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "将领找不到配置, roleId:", player.roleId, ", heroId:", heroId);
        }

        // 获取武将对应天赋页数据
        TalentData talentDataMap = hero.getTalent().get(pageIndex);

        // 激活
        if (type == HeroConstant.TALENT_HERO_TYPE_1) {
            // 校验校验武将等级
            if (hero.getLevel() < Constant.HERO_LEVEL_OF_OPEN_TALENT) {
                throw new MwException(GameError.INSUFFICIENT_CONDITIONS_FOR_HERO_TALENT.getCode(), String.format("激活天赋时, 武将等级不够, roleId:%s, heroId:%s", roleId, heroId));
            }
            if (sHero.getActivate() != 1) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("武将不可激活天赋, 因为未配置可觉醒, roleId:%s, heroId:%s", roleId, heroId));
            }
            // 激活当前页签, 需要确认之前的天赋页签是否全部学习完毕
            if (pageIndex > 1) {
                int count = pageIndex;
                while (--count >= 1) {
                    TalentData preTalentDataMap = hero.getTalent().get(count);
                    if (CheckNull.isNull(preTalentDataMap) || !preTalentDataMap.isActivate() || !preTalentDataMap.isAllPartActivated()) {
                        // TODO 后续如果有多个天赋页，需明确下一页天赋激活的条件是前一天赋页的天赋全部升满，还是全部都大于1级（即天赋球点亮了）即可
                        throw new MwException(GameError.PRE_TALENT_NOT_UNLOCKED.getCode(), "前置天赋未解锁, roleId:", player.roleId, ", heroId:", heroId);
                    }
                }
            }

            // 若为非激活状态且数据为空, 则初始化数据
            if (CheckNull.isNull(talentDataMap) || !talentDataMap.isActivate()) {
                // 初始化武将天赋
                Integer maxPart = StaticHeroDataMgr.getHeroEvolve(sHero.getEvolveGroup().get(pageIndex - 1)).stream()
                        .map(StaticHeroEvolve::getPart)
                        .distinct()
                        .max(Integer::compareTo)
                        .orElse(null);
                if (maxPart == null) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "武将天赋球个数配置错误, heroId: ", heroId);
                }
                talentDataMap = new TalentData(1, pageIndex, maxPart);
                hero.getTalent().put(pageIndex, talentDataMap);
            } else {
                if (talentDataMap.isActivate()) {
                    throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "将领已经激活过了, roleId:", player.roleId, ", heroId:", heroId);
                }
            }

            // 扣除激活的消耗, 将将领标识为已激活
            rewardDataManager.checkAndSubPlayerRes(player, sHero.getActivateConsume(), AwardFrom.AWAKEN_HERO_ACTIVE_CONSUME, heroId);
        }

        // 重置
        if (type == HeroConstant.TALENT_HERO_TYPE_3) {
            if (CheckNull.isEmpty(hero.getTalent())) {
                throw new MwException(GameError.AWAKEN_HERO_REGROUP_ERROR.getCode(), "已经没部位可以重置了, roleId:", player.roleId, ", heroId:", heroId);
            }

            // 重置则重置所有天赋页
            hero.getTalent().values().forEach(talentData_ -> {
                // 天赋页未激活，不用重置
                if (CheckNull.isNull(talentData_) || !talentData_.isActivate()) {
                    return;
                }
                // 天赋页天赋球均未升级，不用重置
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
                            throw new MwException(GameError.NO_CONFIG.getCode(), "武将天赋球个数配置错误, roleId:", player.roleId, ", heroId:", heroId);
                    }
                    talentData_.setMaxPart(maxPart);
                }
                // 扣除重置的消耗, 将部位状态清空
                int indexTemp = talentData_.getIndex();
                rewardDataManager.checkAndSubPlayerRes(player, sHero.getRecombination(), AwardFrom.AWAKEN_HERO_REGROUP_CONSUME, heroId, indexTemp);
                // 获取已升级天赋的总计消耗
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
                // 初始化所有部位天赋
                Stream.iterate(HeroConstant.TALENT_PART_MIN, part -> ++part).limit(talentData_.getMaxPart()).forEach(part -> talentData_.getTalentArr().put(part, 0));
                hero.getTalent().put(indexTemp, talentData_);
                // 返还重置的部分消耗
                if (!CheckNull.isEmpty(hadConsumeList)) {
                    List<List<Integer>> mergeAward = RewardDataManager.mergeAward(hadConsumeList);
                    mergeAward = Objects.requireNonNull(mergeAward).stream().peek(award -> award.set(2, (int) (award.get(2) * (HeroConstant.HERO_REGROUP_AWARD_NUM / Constant.TEN_THROUSAND)))).collect(Collectors.toList());
                    rewardDataManager.sendReward(player, mergeAward, AwardFrom.AWAKEN_HERO_REGROUP_CONSUME, heroId, indexTemp);
                }
            });
        }

        // 更新将领属性
        CalculateUtil.processAttr(player, hero);
        GamePb5.StudyHeroTalentRs.Builder builder = GamePb5.StudyHeroTalentRs.newBuilder();
        builder.setHero(PbHelper.createHeroPb(hero, player));
        return builder.build();
    }

    /**
     * 升级武将天赋
     *
     * @param roleId    角色id
     * @param heroId    武将id
     * @param pageIndex 天赋页（对应s_hero_evolve的group）
     * @param part      要升级的天赋索引位置（中心天赋位置为1）
     * @return
     * @throws MwException
     */
    public GamePb5.UpgradeHeroTalentRs upgradeHeroTalent(long roleId, int heroId, int pageIndex, int part) throws MwException {
        // 角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 将领条件检测
        Hero hero = heroService.checkHeroIsExist(player, heroId);
        if (!hero.isIdle()) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不在空闲中, roleId:", roleId, ", heroId:", heroId, ", state:", hero.getState());
        }
        StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (sHero == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "将领找不到配置, roleId:", player.roleId, ", heroId:", heroId);
        }
        if (CheckNull.isEmpty(sHero.getEvolveGroup()) || sHero.getEvolveGroup().size() < pageIndex) { // s_hero的evolveGroup对应1、2、3...天赋页的天赋
            throw new MwException(GameError.NO_CONFIG.getCode(), "英雄天赋组未找到, roleId:", player.roleId, ", heroId:", heroId);
        }

        Integer heroTalentGroup = sHero.getEvolveGroup().get(pageIndex - 1);
        if ((hero.getQuality() == HeroConstant.QUALITY_PURPLE_HERO && heroTalentGroup >= 100) || (hero.getQuality() == HeroConstant.QUALITY_ORANGE_HERO && heroTalentGroup < 100)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "将领天赋组错误, roleId:", player.roleId, ", heroId:", heroId);
        }

        // 升级天赋时, 校验武将等级
        if (hero.getLevel() < Constant.HERO_LEVEL_OF_OPEN_TALENT) {
            throw new MwException(GameError.INSUFFICIENT_CONDITIONS_FOR_HERO_TALENT.getCode(), "升级天赋时, 武将等级不够, roleId:", player.roleId, ", heroId:", heroId);
        }

        TalentData talentData = hero.getTalent().get(pageIndex);
        if (CheckNull.isNull(talentData) || !talentData.isActivate()) {
            throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "升级前需要先激活, roleId:", player.roleId, ", heroId:", heroId);
        }

        // 目标部位天赋的配置（同一天赋按等级区分，表里分为多条记录）
        List<StaticHeroEvolve> staticHeroEvolveList = StaticHeroDataMgr.getHeroEvolve(heroTalentGroup).stream()
                .filter(staticHeroEvolve -> staticHeroEvolve.getPart() == part)
                .collect(Collectors.toList());
        // 目标位置天赋的最大等级配置
        Integer maxTargetTalentLv = staticHeroEvolveList.stream().max(Comparator.comparingInt(StaticHeroEvolve::getLv)).map(StaticHeroEvolve::getLv).orElse(null);
        if (maxTargetTalentLv == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "天赋等级未配置, roleId:", player.roleId, ", heroId:", heroId);
        }
        if (talentData.getTalentArr().get(part) >= maxTargetTalentLv) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "该天赋已经满级, roleId:", player.roleId, ", heroId:", heroId);
        }
        // 获取升到下一级所需的资源数量
        Integer curLv = talentData.getTalentArr().get(part);
        StaticHeroEvolve curLvStaticHeroEvolve = staticHeroEvolveList.stream().filter(she -> she.getLv() == curLv).findFirst().orElse(null);
        if (curLvStaticHeroEvolve == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "天赋下一等级未配置, roleId:", player.roleId, ", heroId:", heroId, "talentGroup:", pageIndex);
        }
        List<List<Integer>> consume = curLvStaticHeroEvolve.getConsume();
        // 扣除升级的消耗，天赋等级加1
        rewardDataManager.checkAndSubPlayerRes(player, consume, AwardFrom.AWAKEN_HERO_EVOLVE_CONSUME, heroId, part);
        talentData.upgradeTalent(part);

        // 如果所有部位天赋都已达到最大等级，更新天赋页
        int size = (int) talentData.getTalentArr().entrySet().stream().filter(talent -> {
            Integer partTemp = talent.getKey();
            Integer lvTemp = talent.getValue();
            Integer maxLvOfPart = StaticHeroDataMgr.getHeroEvolve(heroTalentGroup).stream()
                    .filter(she -> she.getPart() == partTemp)
                    .max(Comparator.comparingInt(StaticHeroEvolve::getLv))
                    .map(StaticHeroEvolve::getLv).orElse(null);
            if (maxLvOfPart == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "天赋等级未配置, roleId:", player.roleId, ", heroId:", heroId);
            }

            return lvTemp.compareTo(maxLvOfPart) < 0;
        }).count();
        if (size == 0) {
            talentData.setAllPartActivated(1);
        }

        // 添加天赋升级日志埋点
        LogUtil.getLogThread().addCommand(() -> LogLordHelper.gameLog(LogParamConstant.UPGRADE_HERO_TALENT, player,
                AwardFrom.UPGRADE_HERO_TALENT, heroId, hero.getDecorated(), pageIndex, part, talentData.getTalentArr().get(part)));

        // 更新将领属性
        CalculateUtil.processAttr(player, hero);
        GamePb5.UpgradeHeroTalentRs.Builder builder = GamePb5.UpgradeHeroTalentRs.newBuilder();
        builder.setHero(PbHelper.createHeroPb(hero, player));
        return builder.build();
    }

    /**
     * 校验升阶前提条件
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
     * 获取品阶属性
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
     * 校验升阶消耗
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
                LogUtil.debug("重置玩家紫将品阶耗时: ", (System.nanoTime() - startTime) / 100000);
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
     * 重置单个将领品阶
     *
     * @param p
     */
    private boolean resetOneHeroGrade(Player p) {
        if (CheckNull.isNull(p)) return false;
        if (CheckNull.isEmpty(p.heros)) return false;

        List<Hero> heroList = p.heros.values().stream().filter(hero ->
                hero.getQuality() == HeroConstant.QUALITY_PURPLE_HERO).collect(Collectors.toList());
        if (CheckNull.isEmpty(heroList)) return false;

        LogUtil.debug("--------------修复玩家部队状态 开始  roleId:", p.roleId);
        // 未返回的部队将领id
        int now = TimeHelper.getCurrentSecond();
        WarDataManager warDataManager = DataResource.ac.getBean(WarDataManager.class);
        WarService warService = DataResource.ac.getBean(WarService.class);
        WorldService worldService = DataResource.ac.getBean(WorldService.class);
        long roleId = p.roleId;
        for (Army army : p.armys.values()) {
            if (CheckNull.isNull(army)) continue;
            int armyState = army.getState();
            if (armyState == ArmyConstant.ARMY_STATE_COLLECT) {
                // 部队采集中，结算采集
                worldService.retreatSettleCollect(army, 0, p, now, roleId);
            }

            worldService.retreatArmy(p, army, now, ArmyConstant.MOVE_BACK_TYPE_2);
            LogUtil.debug("--------------返回部队成功: ", army);
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
                            // 城战 打玩家
                            if (battle.getSponsor() != null && battle.getSponsor().roleId == roleId) {
                                // 如果是发起者撤退
                                // 玩家发起的城战，发起人撤回部队，城战取消
                                warService.cancelCityBattle(army.getTarget(), true, battle, true);
                            } else {
                                // 不是发起者,移除battle的兵力
                                worldService.removeBattleArmy(battle, roleId, keyId, battle.getAtkCamp() == camp);
                            }
                        } else if (battle.getType() == WorldConstant.BATTLE_TYPE_MINE_GUARD) {
                            //采集驻守撤回, 半路撤回部队，取消战斗提示
                            warDataManager.removeBattleByIdNoSync(battleId);
                        } else {// 阵营战
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

                //不管采矿当前部队是否带有战斗, 一律撤回被攻击提示
                if (armyState == ArmyConstant.ARMY_STATE_COLLECT) {
                    //取消采矿被攻击提示
                    worldService.cancelMineBattle(army.getTarget(), now, p);
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }

        Optional.ofNullable(p.getPlayerFormation().getHeroBattle()).ifPresent(heroes -> {
            for (PartnerHero partnerHero : heroes) {
                if (HeroUtil.isEmptyPartner(partnerHero))
                    continue;

                partnerHero.setState(HeroConstant.HERO_STATE_IDLE);
            }
        });

        Optional.ofNullable(p.getPlayerFormation().getHeroAcq()).ifPresent(heroes -> {
            for (PartnerHero partnerHero : heroes) {
                if (HeroUtil.isEmptyPartner(partnerHero))
                    continue;

                partnerHero.setState(HeroConstant.HERO_STATE_IDLE);
            }
        });
        LogUtil.debug("--------------修复玩家部队状态 结束  roleId:", p.roleId);

        // 计算返还碎片
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
