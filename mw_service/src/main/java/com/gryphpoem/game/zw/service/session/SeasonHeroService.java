package com.gryphpoem.game.zw.service.session;

import com.gryphpoem.cross.constants.PlayerUploadTypeDefine;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroClergy;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSeason;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSeasonSkill;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.season.GlobalSeasonData;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.TaskService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 赛季英雄
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-04-13 11:46
 */
@Service
public class SeasonHeroService {

    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private SeasonService seasonService;

    /**
     * 合成赛季英雄
     */
    public GamePb4.SynthSeasonHeroRs synthSessionHero(Player player, GamePb4.SynthSeasonHeroRq req) throws MwException {
        long lordId = player.getLordId();
        int heroId = req.getHeroId();//合成的英雄
        StaticHeroSeason seasonHero = StaticHeroDataMgr.getSeasonHero(heroId);
        if (Objects.isNull(seasonHero)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("lordId :%d, heroId :%d, not found!!!", lordId, heroId));
        }
        Hero hero = player.heros.get(heroId);
        if (Objects.nonNull(hero)) {
            throw new MwException(GameError.HERO_EXISTS.getCode(), String.format("lordId :%d, 已经拥有该英雄, heroId :%d", lordId, heroId));
        }

        if (player.lord.getLevel() < seasonHero.getSynthLv()) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), String.format("lordId :%d, 合成英雄 heroId :%d, 等级不足 :[%d]", lordId, heroId, player.lord.getLevel()));
        }

        //扣除指定碎片数量
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, seasonHero.getPieceId(), seasonHero.getPieceCount(),
                AwardFrom.SEASON_HERO_SYNTH, true, heroId, seasonService.getCurrSeason());
        //获得英雄
        hero = rewardDataManager.addHero(player, heroId, AwardFrom.SEASON_HERO_SYNTH);
        GamePb4.SynthSeasonHeroRs.Builder rsb = GamePb4.SynthSeasonHeroRs.newBuilder();
        rsb.setHero(PbHelper.createHeroPb(hero, player));
        return rsb.build();
    }

    public GamePb4.UpgradeHeroSkillRs upgradeHeroSkill(Player player, GamePb4.UpgradeHeroSkillRq req) throws MwException {
        long lordId = player.getLordId();
        int heroId = req.getHeroId();
        Hero hero = player.heros.get(heroId);
        if (Objects.isNull(hero)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("lordId :%d, 将领ID :%d, 不存在", lordId, heroId));
        }

        StaticHeroSeason staticHeroSeason = StaticHeroDataMgr.getSeasonHero(heroId);
        if (Objects.isNull(staticHeroSeason)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("lordId :%d, heroId :%d, not found!!!", lordId, heroId));
        }


        //技能不存在
        int skillId = req.getSkillId();
        TreeMap<Integer, StaticHeroSeasonSkill> lvMap = StaticHeroDataMgr.getHeroSkillMap(heroId, skillId);
        if (Objects.isNull(lvMap) || !hero.getSkillLevels().containsKey(skillId)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("lordId :%d, heroId :%d, skillId :%d, not found!!!", lordId, heroId, skillId));
        }

        int lv = hero.getSkillLevels().get(skillId);
        StaticHeroSeasonSkill heroSkill = lvMap.get(lv + 1);
        if (Objects.isNull(heroSkill) || CheckNull.isEmpty(heroSkill.getUpgradeCost())) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("lordId :%d, heroId :%d, skill :%d, level :%d not found!!!", lordId, heroId, skillId, lv + 1));
        }

        //升级技能所需英雄等级
        if (hero.getLevel() < heroSkill.getNeedHeroLv()) {
            throw new MwException(GameError.HERO_LEVEL_NOT_ENOUGH.getCode(), String.format("lordId :%d, heroId :%d level not enough", lordId, heroId));
        }

        rewardDataManager.checkAndSubPlayerRes(player, heroSkill.getUpgradeCost(), true, AwardFrom.SEASON_UPGRADE_HERO_SKILL, heroId, skillId, lv);
        hero.getSkillLevels().merge(skillId, 1, Integer::sum);
        //重新计算战力
        CalculateUtil.processAttr(player, hero);

        //任务 - 升级技能
        TaskService.handleTask(player, ETask.HERO_UPSKILL);
        ActivityDiaoChanService.completeTask(player,ETask.HERO_UPSKILL);
        TaskService.processTask(player,ETask.HERO_UPSKILL);

        if (hero.isOnBattle() || hero.isOnWall()) {
            List<Long> rolesId = new ArrayList<>();
            EventBus.getDefault().post(
                    new Events.CrossPlayerChangeEvent(PlayerUploadTypeDefine.UPLOAD_TYPE_HERO,
                            0, CrossFunction.CROSS_WAR_FIRE, rolesId));
        }

        GamePb4.UpgradeHeroSkillRs.Builder rsb = GamePb4.UpgradeHeroSkillRs.newBuilder();
        rsb.setHero(PbHelper.createHeroPb(hero, player));
        return rsb.build();
    }

    /**
     * 神职升级
     *
     * @param player 玩家
     * @throws MwException
     */
    public GamePb4.UpgradeHeroCgyRs upgradeCgy(Player player, GamePb4.UpgradeHeroCgyRq req) throws MwException {
        int heroId = req.getHeroId();
        //英雄不存在, 或者不是赛季英雄
        long lordId = player.getLordId();
        StaticHeroSeason sHero = StaticHeroDataMgr.getSeasonHero(heroId);
        if (Objects.isNull(sHero)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "将领找不到配置, roleId:", lordId, ", heroId:", heroId);
        }

        Hero hero = player.heros.get(heroId);
        if (Objects.isNull(hero)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("玩家 roleId :%d, 赛季英雄 :%d, 不存在!!!", player.getLordId(), heroId));
        }

        //英雄神职配置不存在
        int stage = req.getStage();
        int lv = req.getLv();
        TreeMap<Integer, TreeMap<Integer, StaticHeroClergy>> cgyMap = StaticHeroDataMgr.getHeroClergyMap(heroId);
        TreeMap<Integer, StaticHeroClergy> lvMap = cgyMap != null ? cgyMap.get(stage) : null;
        StaticHeroClergy s_cgy = lvMap != null ? lvMap.get(lv) : null;
        if (Objects.isNull(s_cgy) || CheckNull.isEmpty(s_cgy.getCost())) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId :%d, heroId :%d, lv :%d, step :%d 没有找到神职配置, 或者升级消耗为空!!!", lordId, heroId, stage, lv));
        }

        boolean isUpStage = false;
        if (hero.getCgyStage() == stage) {
            //升级
            if (hero.getCgyLv() + 1 != lv) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, 升阶数量错误(%d, %d) -> (%d, %d)", lordId, hero.getCgyStage(), hero.getCgyLv(), stage, lv));
            }
        } else {
            //进阶
            if (hero.getCgyStage() + 1 != stage || lv != 0) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, 升阶数量错误(%d, %d) -> (%d, %d)", lordId, hero.getCgyStage(), hero.getCgyLv(), stage, lv));
            }
            if (lvMap.lastKey() != hero.getCgyLv()) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, 升阶数量错误(%d, %d) -> (%d, %d)", lordId, hero.getCgyStage(), hero.getCgyLv(), stage, lv));
            }
            isUpStage = true;
        }

        rewardDataManager.checkAndSubPlayerRes(player, s_cgy.getCost(), true, AwardFrom.SEASON_HERO_CLERGY_UPGRADE, heroId, stage, lv);

        hero.setCgyStage(stage);
        hero.setCgyLv(lv);
        // 更新将领属性
        CalculateUtil.processAttr(player, hero);

        //如果进阶, 并且需要广播的话
        if (isUpStage) {
            chatDataManager.sendSysChat(ChatConst.SEASON_HERO_UPGRADE_STAGE, player.lord.getArea(), 0,
                    player.getCamp(), player.lord.getNick(), heroId, stage);

        }
        //任务 - xx英雄升到x军职
        TaskService.handleTask(player, ETask.HERO_UPSTAR);
        ActivityDiaoChanService.completeTask(player,ETask.HERO_UPSTAR);
        TaskService.processTask(player,ETask.HERO_UPSTAR);
        GamePb4.UpgradeHeroCgyRs.Builder rsb = GamePb4.UpgradeHeroCgyRs.newBuilder();
        rsb.setHero(PbHelper.createHeroPb(hero, player));
        return rsb.build();
    }
}
