package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroEvolve;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroUpgrade;
import com.gryphpoem.game.zw.resource.pojo.hero.AwakenData;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
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
        StaticHeroUpgrade staticData = StaticHeroDataMgr.getNextLvHeroUpgrade(heroId, hero.getGradeKeyId());
        if (CheckNull.isNull(staticData) || staticData.getKeyId() == hero.getGradeKeyId()) {
            throw new MwException(GameError.NO_CONFIG, String.format("player:%d, no next level config, heroId:%d, keyId:%d", roleId, heroId, hero.getGradeKeyId()));
        }
        checkCondition(player, staticData.getCondition(), staticData.getKeyId(), hero);
        checkConsume(player, staticData.getConsume(), staticData.getKeyId());
        hero.setGradeKeyId(staticData.getKeyId());
        CalculateUtil.processAttr(player, hero);

        StaticHeroUpgrade nextStaticData = StaticHeroDataMgr.getNextLvHeroUpgrade(heroId, hero.getGradeKeyId());
        // 若武将升级到满阶, 发送跑马灯
        if (Objects.nonNull(nextStaticData) && nextStaticData.getKeyId() == hero.getGradeKeyId()) {
            chatDataManager.sendSysChat(ChatConst.CHAT_HERO_FULL_GRADE, player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), heroId);
        }
        GamePb5.UpgradeHeroRs.Builder builder = GamePb5.UpgradeHeroRs.newBuilder();
        builder.setHero(PbHelper.createHeroPb(hero, player));
        return builder.build();
    }

    /**
     * 学习武将天赋
     *
     * @param roleId 角色id
     * @param heroId 将领id
     * @param type   进行的行为, 激活 1, 进化 2, 重组 3
     * @return 操作后的将领数据
     * @throws MwException 自定义异常
     */
    public GamePb5.StudyHeroTalentRs studyHeroTalent(long roleId, int heroId, int type, int index) throws MwException {
        // 角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 将领条件检测------------------
        Hero hero = heroService.checkHeroIsExist(player, heroId);
        if (!hero.isIdle()) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不在空闲中, roleId:", roleId, ", heroId:", heroId,
                    ", state:", hero.getState());
        }
        StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (sHero == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "将领找不到配置, roleId:", player.roleId, ", heroId:",
                    heroId);
        }
        if (CheckNull.isEmpty(sHero.getEvolveGroup()) || sHero.getEvolveGroup().size() < index) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "英雄天赋组未找到, roleId:", player.roleId, ", heroId:",
                    heroId);
        }

        if (type == HeroConstant.AWAKEN_HERO_TYPE_1 || type == HeroConstant.AWAKEN_HERO_TYPE_2) {
            // 进化与激活时, 校验武将觉醒层数
            if (hero.getDecorated() < index) {
                throw new MwException(GameError.HERO_INSUFFICIENT_AWAKENING_CONDITIONS.getCode(), "武将觉醒条件不足, roleId:", player.roleId, ", heroId:",
                        heroId);
            }
            // 激活或进化当前页签, 需要确认之前的天赋页签是否全部学习完毕
            if (index > 1) {
                int count = index;
                while (--count >= 1) {
                    AwakenData awakenData = hero.getAwaken().get(count);
                    if (CheckNull.isNull(awakenData) || !awakenData.isActivate() || awakenData.curPart() != 0) {
                        throw new MwException(GameError.PRE_TALENT_NOT_UNLOCKED.getCode(), "前置天赋未解锁, roleId:", player.roleId, ", heroId:",
                                heroId);
                    }
                }
            }
        } else {
            // 重组时, 武将没有激活或学习过的天赋
            if (CheckNull.isEmpty(hero.getAwaken()) || CheckNull.isNull(hero.getAwaken().values().stream().
                    filter(awakenData -> awakenData.lastPart() != 0).findFirst().orElse(null))) {
                throw new MwException(GameError.AWAKEN_HERO_REGROUP_ERROR.getCode(), "已经没部位可以重组了, roleId:", player.roleId, ", heroId:", heroId);
            }
        }

        // 获取学习天赋数据
        AwakenData awaken = hero.getAwaken().get(index);
        if (type == HeroConstant.AWAKEN_HERO_TYPE_1) {
            // 若为非激活状态且数据为空, 则初始化数据
            if (CheckNull.isNull(awaken)) {
                awaken = new AwakenData(index);
                hero.getAwaken().put(index, awaken);
            }
        }
        // 进化校验武将是否有当前天赋页签数据
        if (type == HeroConstant.AWAKEN_HERO_TYPE_2) {
            if (CheckNull.isNull(awaken)) {
                throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "进化前需要先激活, roleId:", player.roleId, ", heroId:", heroId);
            }
        }

        // 当前的部位
        int curPart = 0;
        // 激活状态
        boolean activate = true;
        if (type != HeroConstant.AWAKEN_HERO_TYPE_3) {
            activate = awaken.isActivate();
            // 当前的部位
            curPart = awaken.curPart();
        }

        List<StaticHeroEvolve> heroEvolve = StaticHeroDataMgr.getHeroEvolve(sHero.getEvolveGroup().get(index - 1));
        if (CheckNull.isEmpty(heroEvolve)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "将领找不到配置, roleId:", player.roleId, ", heroId:",
                    heroId);
        }

        // 激活 1, 进化 2, 重组 3
        if (type == HeroConstant.AWAKEN_HERO_TYPE_1) {
            if (activate) {
                throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "将领已经激活过了, roleId:", player.roleId, ", heroId:", heroId);
            }
            // 扣除激活的消耗, 将将领标识为已激活
            rewardDataManager.checkAndSubPlayerRes(player, sHero.getActivateConsume(), AwardFrom.AWAKEN_HERO_ACTIVE_CONSUME, heroId);
            // 初始化
            AwakenData finalAwaken = awaken;
            Stream.iterate(HeroConstant.AWAKEN_PART_MIN, part -> ++part).limit(HeroConstant.AWAKEN_PART_MAX).forEach(part -> finalAwaken.getEvolutionGene().put(part, 0));
            awaken.setStatus(1);
        } else if (type == HeroConstant.AWAKEN_HERO_TYPE_2) {
            int finalCurPart = curPart;
            StaticHeroEvolve sHeroEvolve = heroEvolve.stream().filter(he -> he.getPart() == finalCurPart).findFirst().orElse(null);
            if (CheckNull.isNull(sHeroEvolve)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "将领找不到配置, roleId:", player.roleId, ", heroId:",
                        heroId);
            }
            if (!activate) {
                throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "进化前需要先激活, roleId:", player.roleId, ", heroId:", heroId);
            }
            if (curPart == 0) {
                throw new MwException(GameError.AWAKEN_HERO_ERROR.getCode(), "已经没部位可以进化了, roleId:", player.roleId, ", heroId:", heroId);
            }
            // 扣除进化的消耗, 将部位状态标识为1
            rewardDataManager.checkAndSubPlayerRes(player, sHeroEvolve.getConsume(), AwardFrom.AWAKEN_HERO_ACTIVE_CONSUME, heroId, curPart);
            awaken.getEvolutionGene().put(curPart, 1);
        } else if (type == HeroConstant.AWAKEN_HERO_TYPE_3) {
            // 重置则重置所有天赋页
            hero.getAwaken().values().forEach(awakenData -> {
                if (CheckNull.isNull(awakenData))
                    return;
                // 扣除重组的消耗, 将部位状态清空
                int lastPart_ = awakenData.lastPart();
                rewardDataManager.checkAndSubPlayerRes(player, sHero.getRecombination(), AwardFrom.AWAKEN_HERO_REGROUP_CONSUME, heroId, lastPart_);
                // 初始化
                Stream.iterate(HeroConstant.AWAKEN_PART_MIN, part -> ++part).limit(HeroConstant.AWAKEN_PART_MAX).forEach(part -> awakenData.getEvolutionGene().put(part, 0));
                List<List<Integer>> awards = new ArrayList<>();
                heroEvolve.stream().filter(he -> he.getPart() >= HeroConstant.AWAKEN_PART_MIN && he.getPart() <= lastPart_).forEach(she -> {
                    List<List<Integer>> consume = she.getConsume();
                    awards.addAll(consume);
                });
                if (!CheckNull.isEmpty(awards)) {
                    List<List<Integer>> mergeAward = RewardDataManager.mergeAward(awards);
                    mergeAward = Objects.requireNonNull(mergeAward).stream().peek(award -> award.set(2, (int) (award.get(2) * (HeroConstant.HERO_REGROUP_AWARD_NUM / Constant.TEN_THROUSAND)))).collect(Collectors.toList());
                    // 发送重组的奖励
                    rewardDataManager.sendReward(player, mergeAward, AwardFrom.AWAKEN_HERO_REGROUP_CONSUME, heroId, lastPart_);
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
        rewardDataManager.checkPlayerResIsEnough(player, configList, 1, String.valueOf(gradeKeyId));
    }

    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {

    }
}
