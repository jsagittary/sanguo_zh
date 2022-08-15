package com.gryphpoem.game.zw.service.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticHeroBiographyDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.PlayerHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroBiographyAttr;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroBiographyShow;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroUpgrade;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-12 17:05
 */
@Component
public class HeroBiographyService implements GmCmdService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private StaticHeroBiographyDataMgr staticHeroBiographyDataMgr;

    /**
     * 获取武将列传信息
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
     * 升级或激活武将列传
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
        if (CheckNull.isNull(biographyAttr) || biographyAttr.getLevel() == (Objects.nonNull(curLevel) ? curLevel : 0)) {
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
        return builder.build();
    }

    /**
     * 获取战斗力属性
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

    @GmCmd("biography")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {

    }
}
