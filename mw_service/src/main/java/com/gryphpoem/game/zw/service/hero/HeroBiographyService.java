package com.gryphpoem.game.zw.service.hero;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.eventbus.Subscribe;
import com.gryphpoem.game.zw.core.eventbus.ThreadMode;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticHeroBiographyDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.PlayerHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroBiographyAttr;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroBiographyShow;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroUpgrade;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.EventRegisterService;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-12 17:05
 */
@Component
public class HeroBiographyService implements GmCmdService, EventRegisterService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private StaticHeroBiographyDataMgr staticHeroBiographyDataMgr;

    /**
     * ????????????????????????
     *
     * @param roleId
     * @return
     */
    public GamePb5.GetHeroBiographyInfoRs getHeroBiographyInfo(long roleId) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        PlayerHero playerHero = player.playerHero;
        GamePb5.GetHeroBiographyInfoRs.Builder builder = GamePb5.GetHeroBiographyInfoRs.newBuilder();
        builder.setInfo(playerHero.getBiography().buildClientData());
        return builder.build();
    }

    /**
     * ???????????????????????????
     *
     * @param roleId
     * @param type
     * @return
     */
    public GamePb5.UpgradeHeroBiographyRs upgradeHeroBiography(long roleId, int type) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        PlayerHero playerHero = player.playerHero;
        StaticHeroBiographyShow biographyShow = staticHeroBiographyDataMgr.getBiographyShow(type);
        if (CheckNull.isNull(biographyShow)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("roleId:%d, hero biography type:%d, is not found", roleId, type));
        }
        if (CheckNull.isEmpty(biographyShow.getHeroId())) {
            throw new MwException(GameError.CONFIG_FORMAT_ERROR, String.format("roleId:%d, hero biography type:%d, biographyShow heroList is empty", roleId, type));
        }

        Integer curLevel = playerHero.getBiography().getLevelMap().get(type);
        StaticHeroBiographyAttr biographyAttr = Objects.nonNull(curLevel) ? staticHeroBiographyDataMgr.nextBiographyAttr(type, curLevel) : staticHeroBiographyDataMgr.initBiographyAttr(type);
        if (CheckNull.isNull(biographyAttr) || biographyAttr.getLevel() == (Objects.nonNull(curLevel) ? curLevel : -1)) {
            throw new MwException(GameError.NO_CONFIG, String.format("roleId:%d, hero biography type:%d, next biographyAttr config not found, curLv:%d", roleId, type, curLevel));
        }

        int activeGradeCount = 0;
        for (Integer heroId : biographyShow.getHeroId()) {
            Hero hero = player.heros.get(heroId);
            if (CheckNull.isNull(hero)) {
                continue;
            }
            StaticHeroUpgrade staticHeroUpgrade = StaticHeroDataMgr.getStaticHeroUpgrade(hero.getGradeKeyId());
            if (CheckNull.isNull(staticHeroUpgrade))
                continue;
            if (staticHeroUpgrade.getGrade() >= biographyAttr.getActiveGrade())
                activeGradeCount++;
        }
        if (activeGradeCount < biographyAttr.getActiveNum()) {
            throw new MwException(GameError.UPGRADE_HERO_BIOGRAPHY_INSUFFICIENT_CONDITIONS, String.format("roleId:%d, hero biography type:%d, upgrade hero biography insufficient conditions, lv:%d", roleId, type, biographyAttr.getLevel()));
        }

        playerHero.getBiography().getLevelMap().put(type, biographyAttr.getLevel());
        GamePb5.UpgradeHeroBiographyRs.Builder builder = GamePb5.UpgradeHeroBiographyRs.newBuilder();
        builder.setType(type);
        builder.setLevel(biographyAttr.getLevel());
        CalculateUtil.reCalcAllHeroAttr(player);
        // ???????????????????????????????????????
        EventBus.getDefault().post(new Events.SyncHeroAttrChangeEvent(player.heroBattle, player));
        return builder.build();
    }

    /**
     * ?????????????????????
     *
     * @param player
     * @param hero
     * @return
     */
    public List<List<Integer>> getFightAttr(Player player, Hero hero) {
        if (CheckNull.isNull(player) || CheckNull.isNull(hero))
            return null;
        if (!hero.isOnBattle()) return null;

        PlayerHero playerHero = player.playerHero;
        if (CheckNull.isEmpty(playerHero.getBiography().getLevelMap()))
            return null;
        List<List<Integer>> attrList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> attrId : playerHero.getBiography().getLevelMap().entrySet()) {
            StaticHeroBiographyAttr staticData = staticHeroBiographyDataMgr.getStaticHeroBiographyAttr(attrId.getKey(), attrId.getValue());
            if (CheckNull.isNull(staticData)) continue;
            attrList.addAll(staticData.getAttr());
        }
        return attrList;
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param player
     */
    public void recalculateHeroBiography(Player player) {
        if (CheckNull.isNull(player) || CheckNull.isNull(player.playerHero) ||
                CheckNull.isNull(player.playerHero.getBiography()) || CheckNull.isEmpty(player.playerHero.getBiography().getLevelMap()))
            return;
        Map<Integer, Integer> lvMap = new HashMap<>(player.playerHero.getBiography().getLevelMap());
        lvMap.forEach((type, lv) -> {
            StaticHeroBiographyShow biographyShow = staticHeroBiographyDataMgr.getBiographyShow(type);
            if (CheckNull.isNull(biographyShow)) return;
            TreeMap<Integer, StaticHeroBiographyAttr> treeMap = staticHeroBiographyDataMgr.getStaticHeroBiographyAttrMap(type);
            if (CheckNull.isEmpty(treeMap)) return;

            int maxLv = 0;
            int activeGradeCount = 0;
            for (Map.Entry<Integer, StaticHeroBiographyAttr> entry : treeMap.entrySet()) {
                for (Integer heroId : biographyShow.getHeroId()) {
                    Hero hero = player.heros.get(heroId);
                    if (CheckNull.isNull(hero)) {
                        continue;
                    }
                    StaticHeroUpgrade staticHeroUpgrade = StaticHeroDataMgr.getStaticHeroUpgrade(hero.getGradeKeyId());
                    if (CheckNull.isNull(staticHeroUpgrade))
                        continue;
                    if (staticHeroUpgrade.getGrade() >= entry.getValue().getActiveGrade())
                        activeGradeCount++;
                }
                if (activeGradeCount < entry.getValue().getActiveNum()) {
                    break;
                }
                if (maxLv < entry.getKey())
                    maxLv = entry.getKey();
                activeGradeCount = 0;
            }

            if (maxLv == 0)
                player.playerHero.getBiography().getLevelMap().remove(type);
            else
                player.playerHero.getBiography().getLevelMap().put(type, maxLv);
        });
    }

    /**
     * ????????????????????????
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void syncHeroAttr(Events.SyncHeroAttrChangeEvent event) {
        if (ObjectUtils.isEmpty(event) || ObjectUtils.isEmpty(event.heroIds) || ObjectUtils.isEmpty(event.player) || !event.player.isLogin)
            return;

        GamePb5.SyncHeroOnBattleAttrChangeRs.Builder builder = GamePb5.SyncHeroOnBattleAttrChangeRs.newBuilder();
        for (int heroId : event.heroIds) {
            if (heroId == 0)
                continue;
            Hero hero = event.player.heros.get(heroId);
            if (CheckNull.isNull(hero)) continue;
            builder.addHero(PbHelper.createHeroPb(hero, event.player));
        }
        BasePb.Base base = PbHelper.createRsBase(GamePb5.SyncHeroOnBattleAttrChangeRs.EXT_FIELD_NUMBER, GamePb5.SyncHeroOnBattleAttrChangeRs.ext, builder.build()).build();
        DataResource.ac.getBean(PlayerService.class).syncMsgToPlayer(base, event.player);
    }

    /**
     * ???????????????????????????
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void changeMilitaryHero(Events.SyncHeroMilitaryChangeEvent event) {
        if (ObjectUtils.isEmpty(event) || event.heroId <= 0 || CheckNull.isNull(event.player))
            return;

//        GamePb5.SyncHeroOnBattleAttrChangeRs.Builder builder = GamePb5.SyncHeroOnBattleAttrChangeRs.newBuilder();
//        if (event.curCount > 0)
//            event.player.playerHero.getMilitaryHero().add(event.heroId);
//        else
//            event.player.playerHero.getMilitaryHero().remove(event.heroId);
    }

    @GmCmd("biography")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        if ("addAward".equalsIgnoreCase(params[0])) {
            String[] award_ = params[1].split(",");
            DataResource.ac.getBean(RewardDataManager.class).sendRewardSignle(player, Integer.parseInt(award_[0]),
                    Integer.parseInt(award_[1]), Integer.parseInt(award_[2]), AwardFrom.COMMON);
        }
    }
}
