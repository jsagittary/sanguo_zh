package com.gryphpoem.game.zw.gameplay.cross.util;

import com.gryphpoem.cross.constants.FightCommonConstant;
import com.gryphpoem.cross.gameplay.common.Game2CrossRequest;
import com.gryphpoem.cross.gameplay.player.common.*;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.dataMgr.cross.StaticNewCrossDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.manager.DressUpDataManager;
import com.gryphpoem.game.zw.manager.MedalDataManager;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.p.WallNpc;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.dressup.BaseDressUpEntity;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/11/30 15:18
 */
public class CrossEntity2Dto {

    private static CrossPlayerDress createCrossPlayerDress(Player player) {
        CrossPlayerDress crossPlayerDress = new CrossPlayerDress();
        crossPlayerDress.setNamePlate(player.getDressUp().getCurNamePlate());
        crossPlayerDress.setCastleSkin(player.getCurCastleSkin());
        crossPlayerDress.setCastleSkinStar(player.getCastleSkinStarById(crossPlayerDress.getCastleSkin()));
        crossPlayerDress.setMarchLine(player.getDressUp().getCurMarchEffect());
        crossPlayerDress.setPortrait(player.lord.getPortrait());
        crossPlayerDress.setPortraitFrame(player.getDressUp().getCurPortraitFrame());
        return crossPlayerDress;
    }

    private static CrossLord createCrossLord(Player player, int functionId, int serverId) {
        CrossLord crossLord = new CrossLord();
        crossLord.setLordId(player.lord.getLordId());
        crossLord.setNick(player.lord.getNick());
        crossLord.setCamp(getForce(player, functionId, serverId));
        crossLord.setLevel(player.lord.getLevel());
        crossLord.setCommandLv(player.building.getCommand());
        crossLord.setServerId(serverId);
        crossLord.setOriginalServerId(player.account.getServerId());
        crossLord.setMapId(player.lord.getArea());
        crossLord.setPos(player.lord.getPos());
        crossLord.setScoutCdTime(player.common.getScoutCdTime());
        return crossLord;
    }

    public static int getPlayerForce(StaticCrossGamePlayPlan gamePlayPlan, int serverId, int camp, long lordId) {
        int groupId = gamePlayPlan.getGroup();
        StaticCrossGroup staticCrossGroup = StaticNewCrossDataMgr.getStaticCrossGroup(groupId);
        if (Objects.isNull(staticCrossGroup)) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    String.format("serverId: %d, camp: %d, lordId: %d", serverId, camp, lordId));
        }
        return staticCrossGroup.getForceBySidAndCamp(serverId, camp);
    }

    public static int getForce(Player player, int functionId, int serverId) {
        StaticCrossGroup staticCrossGroup = StaticNewCrossDataMgr.getStaticCrossGroup(
                StaticNewCrossDataMgr.getOpenPlan(player, functionId).getGroup());
        return staticCrossGroup.getForceBySidAndCamp(serverId, player.lord.getCamp());
    }

    public static CrossPlayer uploadCrossPlayer(Player player, int functionId, boolean enter) {
        int serverId = DataResource.ac.getBean(ServerSetting.class).getServerID();
        CrossPlayer crossPlayer = new CrossPlayer();
        crossPlayer.setLord(createCrossLord(player, functionId, serverId));
        crossPlayer.setTalent(createCrossSeasonTalent(player));
        crossPlayer.setHeroMap(createCrossHero(player));

        crossPlayer.setWallNpcMap(createCrossWallNpcMap(player, TimeHelper.getCurrentSecond()));
        crossPlayer.setCrossMarchRatio(createCrossMarchRatio(player));
        crossPlayer.setPlayerDress(createCrossPlayerDress(player));
        crossPlayer.setTechMap(createCrossTech(player));
        return crossPlayer;
    }

    public static Map<Integer, CrossTech> createCrossTech(Player player) {
        if (CheckNull.isNull(player.tech) || ObjectUtils.isEmpty(player.tech.getTechLv()))
            return null;

        Map<Integer, CrossTech> crossTechMap = new HashMap<>();
        player.tech.getTechLv().forEach((techId, techInfo) -> {
            crossTechMap.put(techId, new CrossTech(techInfo.getId(), techInfo.getLv()));
        });

        return crossTechMap;
    }

    public static Map<Integer, CrossHero> createCrossHero(Player player) {
        Map<Integer, CrossHero> map = new HashMap<>();
        Optional.ofNullable(player.heroBattle).ifPresent(heroes -> {
            int index = 0;
            for (Integer heroId : heroes) {
                Hero hero = player.heros.get(heroId);
                if (CheckNull.isNull(hero))
                    continue;
                map.put(heroId, createCrossHero(player, hero, index++, true));
            }
        });

        Optional.ofNullable(player.heroWall).ifPresent(heroes -> {
            int index = 0;
            for (Integer heroId : heroes) {
                Hero hero = player.heros.get(heroId);
                if (CheckNull.isNull(hero))
                    continue;
                map.put(heroId, createCrossHero(player, hero, index++, true));
            }
        });

        Optional.ofNullable(player.heroAcq).ifPresent(heroes -> {
            int index = 0;
            for (Integer heroId : heroes) {
                Hero hero = player.heros.get(heroId);
                if (CheckNull.isNull(hero))
                    continue;
                map.put(heroId, createCrossHero(player, hero, index++, true));
            }
        });
        return map;
    }

    private static CrossSeasonTalent createCrossSeasonTalent(Player player) {
        CrossSeasonTalent crossSeasonTalent = new CrossSeasonTalent();
        Optional.ofNullable(DataResource.getBean(SeasonTalentService.class)
                .getSeasonTalentLearns(player)).ifPresent(set -> crossSeasonTalent.setTidSet(set));
        return crossSeasonTalent;
    }

    public static CrossPlayer defenceBattle2dto(Player player, int functionId, boolean details) {
        int serverId = DataResource.ac.getBean(ServerSetting.class).getServerID();
        CrossPlayer crossPlayer = new CrossPlayer();
        crossPlayer.setLord(createCrossLord(player, functionId, serverId));
        if (details) {
            crossPlayer.setTalent(createCrossSeasonTalent(player));
            crossPlayer.setCrossMarchRatio(createCrossMarchRatio(player));
            crossPlayer.setPlayerDress(createCrossPlayerDress(player));
            crossPlayer.setTechMap(createCrossTech(player));
        }

//        if (!ObjectUtils.isEmpty(player.getDefendHeros())) {
//            int index = 0;
//            crossPlayer.setHeroMap(new HashMap<>());
//            for (Hero hero : player.getDefendHeros()) {
//                crossPlayer.getHeroMap().put(hero.getHeroId(), createCrossHero(player, hero, index++, details));
//            }
//        }
        crossPlayer.setHeroMap(createCrossHero(player));

        crossPlayer.setWallNpcMap(createCrossWallNpcMap(player, TimeHelper.getCurrentSecond()));
        return crossPlayer;
    }


    public static CrossHero createCrossHero(Player player, Hero hero, int index, boolean details) {
        CrossHero crossHero = new CrossHero();
        crossHero.setLordId(player.getLordId());
        if (!details) {
            crossHero.setRemainHp(hero.getCount());
        } else {
            crossHero.setRemainHp(hero.getCount());
            crossHero.setHeroId(hero.getHeroId());
            crossHero.setLevel(hero.getLevel());
            crossHero.setDecorated(hero.getDecorated());
            crossHero.setGradeKeyId(hero.getGradeKeyId());
            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            crossHero.setMaxLine(DataResource.getBean(FightService.class).calcHeroLine(player, hero, staticHero.getLine()));
            crossHero.setLead((int) Math.ceil(hero.getAttr()[HeroConstant.ATTR_LEAD] * 1.0 / crossHero.getMaxLine()));// 当兵力不能被整除时，向上取整
            crossHero.setIntensifyLv(DataResource.getBean(TechDataManager.class).getIntensifyLv4HeroType(player, staticHero.getType()));// 兵种等级

            List<Medal> medals = DataResource.getBean(MedalDataManager.class).getHeroMedalByHeroId(player, hero.getHeroId());
            if (CheckNull.nonEmpty(medals)) {
                int auraSkillId = CheckNull.isNull(medals.get(0).getAuraSkillId()) ? 0 : medals.get(0).getAuraSkillId();
                int specialSkillId = CheckNull.isNull(medals.get(0).getSpecialSkillId()) ? 0 : medals.get(0).getSpecialSkillId();
                CrossMedal crossMedal = new CrossMedal(auraSkillId, specialSkillId);
                crossHero.setMedal(crossMedal);
            }

            Map<Integer, Integer> attrMap = CalculateUtil.processAttr(player, hero);
            crossHero.setAttrMap(attrMap);

//            if (!ObjectUtils.isEmpty(hero.getSkillLevels())) {
//                crossHero.setSkillAction(new ArrayList<>());
//                for (Map.Entry<Integer, Integer> entry : hero.getSkillLevels().entrySet()) {
//                    StaticHeroSeasonSkill heroSkill = StaticHeroDataMgr.getHeroSkill(hero.getHeroId(), entry.getKey(), entry.getValue());
//                    if (Objects.nonNull(heroSkill)) {
//                        StaticSkillAction ska = StaticFightDataMgr.getSkillAction(heroSkill.getSkillActionId());
//                        if (Objects.nonNull(ska))
//                            crossHero.getSkillAction().add(ska.getId());
//                    }
//                }
//            }

            crossHero.setState(hero.getState());
        }
        crossHero.setIndex(index);
        crossHero.setQueue(hero.getStatus());
        return crossHero;
    }

    private static Map<Integer, CrossWallNpc> createCrossWallNpcMap(Player player, int now) {
        WallNpc wallNpc;
        Map<Integer, CrossWallNpc> wallNpcMap = null;
        if (!player.wallNpc.isEmpty()) {
            wallNpcMap = new HashMap<>();
            for (Map.Entry<Integer, WallNpc> ks : player.wallNpc.entrySet()) {
                wallNpc = ks.getValue();
                StaticWallHeroLv staticSuperEquipLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(),
                        wallNpc.getLevel());
                int maxArmy = staticSuperEquipLv.getAttr().get(FightCommonConstant.AttrId.LEAD);
                if (wallNpc.getCount() < maxArmy) {
                    continue;
                }
                wallNpc.setAddTime(now); // 刷新一下bu兵的时间

                CrossWallNpc crossWallNpc = new CrossWallNpc(wallNpc.getHeroNpcId(), wallNpc.getLevel(), wallNpc.getCount());
                wallNpcMap.put(wallNpc.getId(), crossWallNpc);
            }
        }

        return wallNpcMap;
    }

    public static CrossAttackPos createCrossAttackPos(int pos, int type, List<Integer> heroIds, Player player) {
        Map<Integer, Integer> attackHero = null;
        if (!ObjectUtils.isEmpty(heroIds)) {
            Hero hero;
            attackHero = new LinkedHashMap<>();
            for (Integer heroId : heroIds) {
                hero = player.heros.get(heroId);
                if (CheckNull.isNull(hero)) continue;
                attackHero.put(heroId, hero.getCount());
            }
        }

        return new CrossAttackPos(pos, type, attackHero);
    }

    private static CrossMarchRatio createCrossMarchRatio(Player player) {
        CrossMarchRatio crossMarchRatio = new CrossMarchRatio();
        // 科技加成
        TechDataManager techDataManager = DataResource.ac.getBean(TechDataManager.class);
        double addRatio = techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_6);
        // 柏林官员
        double berlinJobEffect = BerlinWar.getBerlinBuffVal(player.roleId, BerlinWarConstant.BUFF_TYPE_MARCH_TIME);
        // 赛季天赋:行军加速
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);
        double seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_301);
        crossMarchRatio.setAddRatio(addRatio + berlinJobEffect + seasonTalentEffect);

        //buff加成
        Effect effect = player.getEffect().get(EffectConstant.WALK_SPEED);
        double addRatio1 = effect != null ? effect.getEffectVal() : 0;
        crossMarchRatio.setAddRatio1(addRatio1);

        //督战官
        effect = player.getEffect().get(EffectConstant.WALK_SPEED_HIGHT);

        // 军曹官
        double addRatio2 = effect != null ? effect.getEffectVal() : 0;
        crossMarchRatio.setAddRatio2(addRatio2);

        // 柏林战前buff
        double addRatio3 = effect != null ? effect.getEffectVal() : 0;
        crossMarchRatio.setAddRatio3(addRatio3);

        // 战火燎原城池加成
        double addRatio4 = 0d;
        try {
            CrossWorldMap cMap = DataResource.getBean(CrossWorldMapService.class).checkCrossWorldMap(player, CrossWorldMapConstant.CROSS_MAP_ID);
            Map<Integer, Integer> cityBuff = cMap.getCityBuff(player);
            addRatio4 = !CheckNull.isEmpty(cityBuff) ? cityBuff.getOrDefault(StaticWarFire.BUFF_TYPE_5, 0) : 0;
        } catch (MwException e) {
        }
        crossMarchRatio.setAddRatio4(addRatio4);

        //皮肤加成
        int skinAdd = 0;
        Map<Integer, BaseDressUpEntity> castleSkinMap = DataResource.getBean(DressUpDataManager.class).getDressUpByType(player, AwardType.CASTLE_SKIN);
        if (!CheckNull.isEmpty(castleSkinMap)) {
            List<StaticCastleSkin> staticCastleSkinList = castleSkinMap.values().stream().map(entity -> StaticLordDataMgr.getCastleSkinMapById(entity.getId())).filter(staticCastleSkin -> staticCastleSkin.getEffectType() == 4).collect(Collectors.toList());
            for (StaticCastleSkin o : staticCastleSkinList) {
                int star = player.getCastleSkinStarById(o.getId());
                StaticCastleSkinStar staticCastleSkinStar = StaticLordDataMgr.getCastleSkinStarById(o.getId() * 100 + star);
                skinAdd += staticCastleSkinStar.getEffectVal();
            }
        }

        int addRatio5 = skinAdd;
        // 行军特效加成
        Map<Integer, BaseDressUpEntity> marchLineMap = DataResource.getBean(DressUpDataManager.class).getDressUpByType(player, AwardType.MARCH_SPECIAL_EFFECTS);
        if (!CheckNull.isEmpty(marchLineMap)) {
            addRatio5 += marchLineMap
                    .keySet()
                    .stream()
                    .map(StaticLordDataMgr::getMarchLine)
                    .filter(Objects::nonNull)
                    .filter(conf -> conf.getEffectType() == StaticMarchLine.EFFECT_TYPE_WALK_SPEED)
                    .mapToInt(StaticMarchLine::getEffectVal)
                    .sum();
        }
        crossMarchRatio.setAddRatio5(addRatio5);
        return crossMarchRatio;
    }

    public static Game2CrossRequest createGame2CrossRequest(Player player, int gamePlanKey) {
        return new Game2CrossRequest(DataResource.getBean(ServerSetting.class).
                getServerID(), player.getCamp(), player.getLordId(), gamePlanKey);
    }
}
