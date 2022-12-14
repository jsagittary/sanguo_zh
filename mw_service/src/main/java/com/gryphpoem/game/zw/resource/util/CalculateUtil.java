package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticCiaDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticCrossDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWarFireDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFightDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMedalDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMentorDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticNightRaidMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPartyDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTotemDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWarPlaneDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.dataMgr.cross.StaticNewCrossDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.NewCrossConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.GlobalWarFire;
import com.gryphpoem.game.zw.manager.DressUpDataManager;
import com.gryphpoem.game.zw.manager.MedalDataManager;
import com.gryphpoem.game.zw.manager.RankDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.Constant.AttrId;
import com.gryphpoem.game.zw.resource.constant.EffectConstant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.constant.LogParamConstant;
import com.gryphpoem.game.zw.resource.constant.MedalConst;
import com.gryphpoem.game.zw.resource.constant.MentorConstant;
import com.gryphpoem.game.zw.resource.constant.PlaneConstant;
import com.gryphpoem.game.zw.resource.constant.ScheduleConstant;
import com.gryphpoem.game.zw.resource.constant.SeasonConst;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.constant.TechConstant;
import com.gryphpoem.game.zw.resource.constant.TreasureWareConst;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Cia;
import com.gryphpoem.game.zw.resource.domain.p.CrossBuff;
import com.gryphpoem.game.zw.resource.domain.p.CrossFunctionData;
import com.gryphpoem.game.zw.resource.domain.p.CrossPersonalData;
import com.gryphpoem.game.zw.resource.domain.p.CrossWarFireLocalData;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.p.FemaleAgent;
import com.gryphpoem.game.zw.resource.domain.p.Mentor;
import com.gryphpoem.game.zw.resource.domain.p.MentorEquip;
import com.gryphpoem.game.zw.resource.domain.p.MentorInfo;
import com.gryphpoem.game.zw.resource.domain.p.MentorSkill;
import com.gryphpoem.game.zw.resource.domain.p.PlayerRebellion;
import com.gryphpoem.game.zw.resource.domain.p.RebelBuff;
import com.gryphpoem.game.zw.resource.domain.p.Tech;
import com.gryphpoem.game.zw.resource.domain.p.TechLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticCabinetPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkin;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkinStar;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticEquip;
import com.gryphpoem.game.zw.resource.domain.s.StaticEquipExtra;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroClergy;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroDecorated;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroEvolve;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSeasonSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticJewel;
import com.gryphpoem.game.zw.resource.domain.s.StaticMarchLine;
import com.gryphpoem.game.zw.resource.domain.s.StaticMedalAuraSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticMedalGeneralSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticMedalSpecialSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticMentor;
import com.gryphpoem.game.zw.resource.domain.s.StaticMentorSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticPartyRanks;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneInit;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneUpgrade;
import com.gryphpoem.game.zw.resource.domain.s.StaticRebelBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticRingStrengthen;
import com.gryphpoem.game.zw.resource.domain.s.StaticSeasonTalent;
import com.gryphpoem.game.zw.resource.domain.s.StaticSkillAction;
import com.gryphpoem.game.zw.resource.domain.s.StaticStone;
import com.gryphpoem.game.zw.resource.domain.s.StaticStoneImprove;
import com.gryphpoem.game.zw.resource.domain.s.StaticSuperEquipLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticTechLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticTotem;
import com.gryphpoem.game.zw.resource.domain.s.StaticTotemLink;
import com.gryphpoem.game.zw.resource.domain.s.StaticTotemUp;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFire;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFireBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFireBuffCross;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.Ring;
import com.gryphpoem.game.zw.resource.pojo.StoneHole;
import com.gryphpoem.game.zw.resource.pojo.StoneImprove;
import com.gryphpoem.game.zw.resource.pojo.SuperEquip;
import com.gryphpoem.game.zw.resource.pojo.WarPlane;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.dressup.BaseDressUpEntity;
import com.gryphpoem.game.zw.resource.pojo.dressup.CastleSkinEntity;
import com.gryphpoem.game.zw.resource.pojo.dressup.TitleEntity;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.TalentData;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.pojo.medal.RedMedal;
import com.gryphpoem.game.zw.resource.pojo.season.SeasonTalent;
import com.gryphpoem.game.zw.resource.pojo.totem.Totem;
import com.gryphpoem.game.zw.rpc.DubboRpcService;
import com.gryphpoem.game.zw.service.HeroUpgradeService;
import com.gryphpoem.game.zw.service.TaskService;
import com.gryphpoem.game.zw.service.TitleService;
import com.gryphpoem.game.zw.service.TreasureWareService;
import com.gryphpoem.game.zw.service.WorldScheduleService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.hero.HeroBiographyService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/**
 * @author TanDonghai
 * @ClassName CalculateUtil.java
 * @Description ?????????????????????
 * @date ???????????????2017???3???29??? ??????2:58:49
 */
public class CalculateUtil {
    /**
     * ??????????????????????????????
     *
     * @param staticHero ??????????????????
     * @param attrId     ??????????????????id
     * @param wash       ????????????
     * @param lv         ????????????
     * @param lineAdd    ????????????????????????
     * @return
     */
    public static int calcHeroAttrById(StaticHero staticHero, int attrId, int wash, int lv, int lineAdd) {
        if (null == staticHero) {
            return 0;
        }
        // ??????=????????????*???????????????-1???*????????????+????????????
        // ??????=????????? + ?????????????????? * ????????????????????? + ?????????????????? * ??????????????? + ??????????????? * ????????????
        double value = 0;
        value += staticHero.getBaseAttrById(attrId);
        value += wash * (staticHero.getAttrRadixById(attrId) + staticHero.getAttrRatioById(attrId) * lv)
                / Constant.TEN_THROUSAND;
        value += staticHero.getAttrGrowthById(attrId) * lv;

        if (attrId == Constant.AttrId.LEAD) {
            // ?????????=??????*??????
            value *= (staticHero.getLine() + lineAdd);
        }

        return (int) value;
    }

    public static int calcHeroAttrById(StaticHero staticHero, int attrId, int wash, int lv) {
        if (null == staticHero) {
            return 0;
        }
        float ratio = 0;// ??????
        if (attrId == Constant.AttrId.ATTACK) {
            ratio = WorldConstant.ATK_RATIO;
        } else if (attrId == Constant.AttrId.DEFEND) {
            ratio = WorldConstant.DEF_RATIO;
        } else if (attrId == Constant.AttrId.LEAD) {
            ratio = WorldConstant.ARMY_RATIO;
        }
        // ??????=????????????*??????????????????*??????+????????????
        double value = wash * lv * ratio + staticHero.getBaseAttrById(attrId);
        LogUtil.calculate("????????????attrId=" + attrId + ",wash=" + wash + "??????=" + ratio + ",attr="
                + staticHero.getBaseAttrById(attrId) + ",value=" + value);
        return (int) value;
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     */
    public static void reCalcAllHeroAttr(Player player) {
        if (null == player) {
            return;
        }
        for (Hero hero : player.heros.values()) {
            processAttr(player, hero, false);
        }
        reCalcFight(player);
    }

    /**
     * ??????????????????????????????
     *
     * @param player
     */
    public static void reCalcBattleHeroAttr(Player player) {
        if (null == player) {
            return;
        }
        int[] heroIds = new int[player.heroBattle.length + player.heroCommando.length];
        System.arraycopy(player.heroBattle, 0, heroIds, 0, player.heroBattle.length);
        System.arraycopy(player.heroCommando, 0, heroIds, player.heroBattle.length, player.heroCommando.length);
        for (int i = 1; i < heroIds.length; i++) {
            int heroId = heroIds[i];
            Hero hero = player.heros.get(heroId);
            if (hero != null) {
                processAttr(player, hero, false);
            }
        }
        reCalcFight(player);
    }

    /**
     * ????????????????????????(??????????????????????????????????????????????????????????????????)
     *
     * @param player
     * @param hero
     *//*
      private static void reCalcHeroAttr(Player player, Hero hero) {
      if (null == player || null == hero) {
      return;
      }

      StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
      if (null == staticHero) {
      LogUtil.calculate("???????????????????????????id??????????????????, heroId:", hero.getHeroId());
      return;
      }

      // ????????????????????????
      hero.getAttr()[HeroConstant.ATTR_ATTACK] = calcHeroAttrById(staticHero, Constant.AttrId.ATTACK,
      hero.getWash()[HeroConstant.ATTR_ATTACK], hero.getLevel());
      hero.getAttr()[HeroConstant.ATTR_DEFEND] = calcHeroAttrById(staticHero, Constant.AttrId.DEFEND,
      hero.getWash()[HeroConstant.ATTR_DEFEND], hero.getLevel());
      hero.getAttr()[HeroConstant.ATTR_LEAD] = calcHeroAttrById(staticHero, Constant.AttrId.LEAD,
      hero.getWash()[HeroConstant.ATTR_LEAD], hero.getLevel());

      // ????????????????????????
      Equip equip;
      StaticEquip staticEquip;
      for (int equipKeyId : hero.getEquip()) {
      if (equipKeyId > 0) {
      equip = player.equips.get(equipKeyId);
      if (null == equip) {
      LogUtil.calculate("????????????????????????????????????????????????, equipKeyId:", equipKeyId);
      continue;
      }

      // ????????????????????????
      staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
      if (null == staticEquip) {
      LogUtil.calculate("????????????????????????????????????????????????id????????????, equipId:", equip.getEquipId());
      continue;
      }

      // ??????????????????????????????
      for (Entry<Integer, Integer> entry : staticEquip.getAttr().entrySet()) {
      hero.getAttr()[entry.getKey()] += entry.getValue();
      }

      // ???????????? ??????
      for (int i = 0; i < equip.getAttrAndLv().size(); i++) {// ??????????????????
      int attrId = equip.getAttrAndLv().get(i).getA();
      int level = equip.getAttrAndLv().get(i).getB();
      if (Arrays.binarySearch(Constant.BASE_ATTRS, attrId) >= 0) {
      StaticEquipExtra equipExtra = StaticPropDataMgr.getEuqipExtraByIdAndLv(attrId, level);
      if (equipExtra != null) {
      hero.getAttr()[attrId] += equipExtra.getAttrValue();
      }
      }
      }

      // for (int attrId : equip.getAttrId()) {// ??????????????????
      // if (Arrays.binarySearch(Constant.BASE_ATTRS, attrId) >= 0) {
      // StaticEquipExtra equipExtra = StaticPropDataMgr.getEuqipExtraByIdAndLv(attrId,
      // equip.getAttrLv());
      // if (equipExtra != null) {
      // hero.getAttr()[attrId] += equipExtra.getAttrValue();
      // }
      // }
      // }
      }
      }

      // ??????????????????
      StaticPartyRanks ranks = StaticPartyDataMgr.getPartyRanks(player.lord.getRanks());
      if (null != ranks) {
      for (Entry<Integer, Integer> entry : ranks.getAttr().entrySet()) {
      hero.getAttr()[entry.getKey()] += entry.getValue();
      }
      }
      reCalcFight(player);
      }*/

    /**
     * ?????? 1??? 2????????????
     */
    private static Map<Integer, Integer> FIGHT_K1;
    /**
     * ?????????????????????
     */
    private static Map<Integer, Integer> FIGHT_K2;

    static {
        FIGHT_K1 = new HashMap<>();
        FIGHT_K1.put(AttrId.ATTACK, 3);
        FIGHT_K1.put(AttrId.DEFEND, 3);
        FIGHT_K1.put(AttrId.LEAD, 3);
        FIGHT_K1.put(AttrId.ATTACK_TOWN, 3);
        FIGHT_K1.put(AttrId.DEFEND_TOWN, 3);
        FIGHT_K1.put(AttrId.ATTACK_EXT, 3);
        FIGHT_K1.put(AttrId.DEFEND_EXT, 3);
        FIGHT_K1.put(AttrId.FIGHT, 1);

        FIGHT_K2 = new HashMap<>();
        FIGHT_K2.put(AttrId.ATTACK, 3);
        FIGHT_K2.put(AttrId.DEFEND, 4);
        FIGHT_K2.put(AttrId.LEAD, 1);
        FIGHT_K2.put(AttrId.ATTACK_TOWN, 4);
        FIGHT_K2.put(AttrId.DEFEND_TOWN, 4);
        FIGHT_K2.put(AttrId.ATTACK_EXT, 4);
        FIGHT_K2.put(AttrId.DEFEND_EXT, 4);
        FIGHT_K2.put(AttrId.FIGHT, 1);
    }

    /**
     * ???????????????
     *
     * @param player
     */
    public static void reCalcFight(Player player) {
        // ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);
        // String environment = serverSetting.getEnvironment();
        // int serverID = serverSetting.getServerID();
        // Map<Integer, Integer> keyMap = null;
        // if ((serverID == 1 || serverID == 2) && "release".equals(environment)) {
        // // FIGHT_K1 ?????????
        // keyMap = FIGHT_K1;
        // } else {
        // keyMap = FIGHT_K2;
        // }
        Map<Integer, Integer> keyMap = FIGHT_K2;

        Hero hero = null;
        int fight = 0;
        int[] heroIds = new int[player.heroBattle.length + player.heroCommando.length];
        System.arraycopy(player.heroBattle, 0, heroIds, 0, player.heroBattle.length);
        System.arraycopy(player.heroCommando, 0, heroIds, player.heroBattle.length, player.heroCommando.length);
        for (int heroId : heroIds) {
            hero = player.heros.get(heroId);
            if (hero != null) {
                if (hero.getAttr() != null) {
                    for (int i = 1; i < hero.getAttr().length; i++) { // ???????????????
                        int attrVal = hero.getAttr()[i];
                        // if (i == HeroConstant.ATTR_ATTACK) {
                        // fight += attrVal * 3;
                        // } else if (i == HeroConstant.ATTR_DEFEND) {
                        // fight += attrVal * 3;
                        // } else if (i == HeroConstant.ATTR_LEAD) {
                        // fight += attrVal * 3;
                        // }
                        Integer k = keyMap.get(i);
                        k = k == null ? k = 0 : k;
                        fight += attrVal * k;
                    }
                }
                if (hero.getExtAttrs() != null) {
                    for (Entry<Integer, Integer> kv : hero.getExtAttrs().entrySet()) {
                        Integer i = kv.getKey();
                        i = i == null ? 0 : i;
                        Integer attrVal = kv.getValue();
                        attrVal = attrVal == null ? 0 : attrVal;
                        // if (i == AttrId.ATTACK_TOWN) {
                        // fight += attrVal * 3;
                        // } else if (i == AttrId.DEFEND_TOWN) {
                        // fight += attrVal * 3;
                        // } else if (i == AttrId.ATTACK_EXT) {
                        // fight += attrVal * 3;
                        // } else if (i == AttrId.DEFEND_EXT) {
                        // fight += attrVal * 3;
                        // }
                        Integer k = keyMap.get(i);
                        k = k == null ? k = 0 : k;
                        fight += attrVal * k;
                    }
                }
                //??????????????????
                fight += reCalcHeroSkillFight(hero);
                fight += reCalcHeroTalentFight(hero);
            }
        }
        long preFight = player.lord.getFight();
        player.lord.setFight(fight);
        if (preFight != player.lord.getFight()) {
            WorldScheduleService service = DataResource.ac.getBean(WorldScheduleService.class);
            service.updateScheduleGoal(player, ScheduleConstant.GOAL_COND_FIGHT, fight);
            // ?????????????????????
            EventBus.getDefault().post(new Events.FightChangeEvent(player));

            //????????????-????????????
            ActivityDiaoChanService.completeTask(player, ETask.PLAYER_POWER);
            TaskService.processTask(player, ETask.PLAYER_POWER);
            LogLordHelper.recodePower(LogParamConstant.FIGHTING_CHANGE, player, preFight, fight, fight - preFight);

            DubboRpcService dubboRpcService = DataResource.ac.getBean(DubboRpcService.class);
            dubboRpcService.updatePlayerLord2CrossPlayerServer(player);
        }
    }

    /**
     * ?????????????????????
     *
     * @param player
     * @param hero
     * @param reCalcFight ???????????????????????????
     * @return
     */
    private static Map<Integer, Integer> processAttr(Player player, Hero hero, boolean reCalcFight) {
        if (null == player || null == hero) {
            return null;
        }
        // ???????????????
        int oldArmyCnt = hero.getCount();

        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (null == staticHero) {
            LogUtil.error("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "???????????????????????????id??????????????????, heroId:",
                    hero.getHeroId());
            return null;
        }

        // ????????????
        Map<Integer, Integer> attrMap = calcHeroAttr(player, hero, staticHero);

        // ?????????????????????
        if (reCalcFight && (hero.getPos() > 0 || hero.getCommandoPos() > 0)) {
            CalculateUtil.reCalcFight(player);
        }
        // ?????????????????????????????????
        if (hero.getAttr()[Constant.AttrId.LEAD] < oldArmyCnt) {
            // ???????????????????????????,????????????
            returnArmy(player, hero);
        }
        return attrMap;
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????Hero??????????????????????????????Map
     *
     * @param player
     * @param hero
     * @return
     */
    public static Map<Integer, Integer> processHeroAttr(Player player, Hero hero) {
        if (Objects.isNull(player) || Objects.isNull(hero)) {
            LogUtil.error("????????????????????????, Player or Hero is NULL");
            return new HashMap<>();
        }
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (Objects.isNull(staticHero)) {
            LogUtil.error("????????????????????????, StaticHero is NULL");
            return new HashMap<>();
        }
        Map<Integer, Integer> attrMap = calcHeroAttr(player, hero, staticHero);
        //hero fight value
        int fightVal = reCalcFight(attrMap);
        // ??????????????????
        fightVal += reCalcHeroSkillFight(hero);
        // ????????????????????????
        fightVal += reCalcHeroTalentFight(hero);
        hero.setFightVal(fightVal);

        return attrMap;
    }

    private static void addTotemEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        HashMap<Integer, Integer> tempMap = new HashMap<>();
        //??????????????????
        for (int totemKey : hero.getTotem()) {
            if (totemKey <= 0) continue;
            Totem totem = player.getTotemData().getTotem(totemKey);
            if (Objects.isNull(totem)) {
                LogUtil.error("????????????????????????,???????????????,totemKey=", totemKey);
                continue;
            }
            StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totem.getTotemId());
            StaticTotemUp staticTotemUp1 = StaticTotemDataMgr.getStaticTotemUp(1, staticTotem.getQuality(), totem.getStrengthen());
            StaticTotemUp staticTotemUp2 = StaticTotemDataMgr.getStaticTotemUp(2, staticTotem.getQuality(), totem.getResonate());
            if (Objects.isNull(staticTotem) || Objects.isNull(staticTotemUp1) || Objects.isNull(staticTotemUp2)) {
                LogUtil.error("????????????????????????,???????????????", totem, staticTotem, staticTotemUp1, staticTotemUp2);
                continue;
            }
            Optional.ofNullable(staticTotemUp1.getAttrByIdx(staticTotem.getPlace())).ifPresent(map -> map.entrySet().forEach(entry -> tempMap.merge(entry.getKey(), entry.getValue(), Integer::sum)));
            Optional.ofNullable(staticTotemUp2.getAttrByIdx(staticTotem.getPlace())).ifPresent(map -> map.entrySet().forEach(entry -> tempMap.merge(entry.getKey(), entry.getValue(), Integer::sum)));
        }
        reCalcAttrFight(player, Constant.ShowFightId.TOTEM, hero, attrMap, tempMap);
    }

    private static void addTotemLinkEffect(Player player, Hero hero, Map<Integer, Integer> attrMutMap) {
        //??????????????????
        StaticTotemLink staticTotemLink = null;
        for (StaticTotemLink link : StaticTotemDataMgr.getStaticTotemLinkList()) {
            boolean isOut = false, isFit = true;

            for (int i = 1; i < hero.getTotem().length; i++) {
                Totem totem = player.getTotemData().getTotem(hero.getTotemKey(i));
                if (Objects.isNull(totem)) {
                    isOut = true;
                    break;
                }
                StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totem.getTotemId());
                if (Objects.isNull(staticTotem)) {
                    isOut = true;
                    break;
                }
                StaticTotemUp staticTotemUp = StaticTotemDataMgr.getStaticTotemUp(1, staticTotem.getQuality(), totem.getStrengthen());
                if (Objects.isNull(staticTotemUp)) {
                    isOut = true;
                    break;
                }
                if (link.getQuality() > staticTotem.getQuality() || totem.getStrengthen() < link.getLv()) {
                    isFit = false;
                    break;
                }
            }
            if (isOut) {
                break;
            }
            if (isFit) {
                staticTotemLink = link;
                break;
            }
        }
        if (Objects.nonNull(staticTotemLink)) {
//            staticTotemLink.getAttr().entrySet().forEach(entry -> tempMap.merge(entry.getKey(),entry.getValue(),Integer::sum));
            staticTotemLink.getAttr().entrySet().forEach(entry -> addAttrValue(attrMutMap, entry.getKey(), entry.getValue()));
        }
    }

    public static Map<Integer, Integer> calcHeroAttr(Player player, Hero hero, StaticHero staticHero) {
        Map<Integer, Integer> attrMap = new HashMap<>();
        // ??????????????????
        addHeroEffect(player, hero, staticHero, attrMap);//???????????????????????????????????????????????????????????? ???????????????????????????
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Hero attrMap=" + attrMap);
        // ??????????????????
        addEquipEffect(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Equip attrMap=" + attrMap);
        //????????????????????????
        addTotemEffect(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "?????????????????? attrMap=" + attrMap);
        // ????????????
        addSuperEquipEffect(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "???????????? attrMap=" + attrMap);
        // ??????????????????
        addPartyRankEffect(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "???????????? attrMap=" + attrMap);
        // ???????????? ??????
        addFemaleAgentEffect(player, hero, attrMap);//???????????????????????????????????????
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "femaleAgent attrMap=" + attrMap);
        // ????????????
        addStoneEffect(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "stone attrMap=" + attrMap);
        // ??????????????????
        addMedalEffect(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Medal attrMap=" + attrMap);
        // ????????????
        addPlaneEffect(player, hero, attrMap);//?????????????????????????????????????????????????????????????????????
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "plane attrMap=" + attrMap);
        // ????????????????????? hero??????
        addCastleSkinEffect(player, hero, attrMap);//???????????????????????????????????????
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "CastleSkin attrMap=" + attrMap);

        // ??????????????????, ??????: ????????????
        addOtherEffect(player, hero, attrMap, staticHero);//??????????????????????????????????????????
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "otherEffect attrMap=" + attrMap);
        // ????????????
        addTreasureWare(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "treasureWare attrMap=" + attrMap);
        // ????????????
        addSeasonTalent(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "seasonTalent attrMap=" + attrMap);
        // ????????????????????????
        addHeroBiographyAttr(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "heroBiography attrMap=" + attrMap);

        // ???????????????????????????
        if (player.isTester) {
            attrMap.put(Constant.AttrId.ATTACK, 9999999);
            attrMap.put(Constant.AttrId.DEFEND, 9999999);
            attrMap.put(Constant.AttrId.LEAD, 9999999);
        }

        hero.getAttr()[Constant.AttrId.ATTACK] = attrMap.get(Constant.AttrId.ATTACK);
        hero.getAttr()[Constant.AttrId.DEFEND] = attrMap.get(Constant.AttrId.DEFEND);
        hero.getAttr()[Constant.AttrId.LEAD] = attrMap.get(Constant.AttrId.LEAD);
        attrMap.put(Constant.AttrId.LEAD,
                getFinalLead(player, hero, staticHero.getLine(), hero.getAttr()[Constant.AttrId.LEAD]));
        hero.getAttr()[Constant.AttrId.LEAD] = attrMap.get(Constant.AttrId.LEAD);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "????????? attrMap=" + attrMap);

        // ??????????????????
        hero.getExtAttrs().clear();
        for (int attr : Constant.EXT_ATTRS) {
            Integer attrVal = attrMap.get(attr);
            if (attrVal == null) {
                hero.getExtAttrs().put(attr, 0);
            } else {
                hero.getExtAttrs().put(attr, attrVal);
            }
        }
        DataResource.ac.getBean(TaskDataManager.class).updTask(player, TaskType.COND_516, 1, hero.getShowFight().values().stream().mapToInt(Integer::intValue).sum());
        return attrMap;
    }

    public static int calcHeroesFightVal(Player player, List<Integer> heroIds) {
        int val = 0;
        LogUtil.calculate("????????????, ???????????????????????? roleId=" + player.roleId);
        for (int heroId : heroIds) {
            Hero hero = player.heros.get(heroId);
            if (hero == null) continue;
            Map<Integer, Integer> attrMap = processHeroAttr(player, hero);
            val += hero.getFightVal();
        }
        LogUtil.calculate("????????????, ????????????????????????, roleId=" + player.roleId);
        return val;
    }

    /**
     * ??????????????????, ??????: ????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     * @param staticHero ????????????
     */
    private static void addOtherEffect(Player player, Hero hero, Map<Integer, Integer> attrMap, StaticHero staticHero) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        int type = staticHero.getType();
        Map<Integer, Integer> tempMap = new HashMap<>();
        // ??????/??????/??????/ ??????????????????
        addTechEffect(player, tempMap, type);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Tech tempMap=" + tempMap);
        // ??????/??????/??????/ ??????????????????
        addIntensifyTechEffect(player, tempMap, type);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "intensify Tech tempMap=" + tempMap);
        // ??????buff
        addCrossBuffEffect(player, hero, tempMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), " ??????buff tmpMap=" + tempMap);
        // ???????????????????????????
        reCalcAttrFight(player, Constant.ShowFightId.OTHER, hero, attrMap, tempMap);
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    private static void addPartyRankEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        Map<Integer, Integer> tempMap = new HashMap<>();
        StaticPartyRanks ranks = StaticPartyDataMgr.getPartyRanks(player.lord.getRanks());
        if (null != ranks) {
            for (Entry<Integer, Integer> entry : ranks.getAttr().entrySet()) {
                addAttrValue(tempMap, entry.getKey(), entry.getValue());
            }
        }
        reCalcAttrFight(player, Constant.ShowFightId.PARTY_RANK, hero, attrMap, tempMap);
    }

    /**
     * ??????????????????, ????????????????????????????????????
     *
     * @param player
     * @param hero
     * @param staticHero
     * @param attrMap
     */
    private static void addHeroEffect(Player player, Hero hero, StaticHero staticHero, Map<Integer, Integer> attrMap) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        Map<Integer, Integer> tempMap = new HashMap<>();// ????????????
        Map<Integer, Integer> attrMutMap = new HashMap<>();// ???????????????
        // ????????????
        tempMap.put(HeroConstant.ATTR_ATTACK, CalculateUtil.calcHeroAttrById(staticHero, Constant.AttrId.ATTACK,
                DataResource.ac.getBean(HeroUpgradeService.class).getGradeAttrValue(hero, Constant.AttrId.ATTACK), hero.getLevel()));
        tempMap.put(HeroConstant.ATTR_DEFEND, CalculateUtil.calcHeroAttrById(staticHero, Constant.AttrId.DEFEND,
                DataResource.ac.getBean(HeroUpgradeService.class).getGradeAttrValue(hero, Constant.AttrId.DEFEND), hero.getLevel()));
        tempMap.put(HeroConstant.ATTR_LEAD, CalculateUtil.calcHeroAttrById(staticHero, Constant.AttrId.LEAD,
                DataResource.ac.getBean(HeroUpgradeService.class).getGradeAttrValue(hero, Constant.AttrId.LEAD), hero.getLevel()));


        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "base tempMap=" + tempMap);

        // ??????????????????
        addDecoratedEffect(player, hero, tempMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "decorated attrMap=" + tempMap);
        // ??????????????????
        addAwakenEffect(staticHero, hero, tempMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "awaken attrMap=" + tempMap);
        // buff?????? ?????????, ??????????????????, attrMap???attrMutMap????????????
        addEffectSpecificVal(player, hero, tempMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "buff??????????????? tempMap=" + tempMap);
        if (hero.getStatus() != HeroConstant.HERO_STATUS_COMMANDO) {
            // ??????????????????
            addMentorEffect(player, hero, tempMap);
            LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Mentor tempMap=" + tempMap);
        }
        // ????????????????????????
        addHeroCgy(hero, tempMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Hero Clergy attrMap=" + tempMap);
        // buff?????? ?????????, ????????????????????????
        addEffectVal(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "??????buff(????????????) attrMutMap=" + attrMutMap);
        // ????????????buff
        addRebelBuffEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(),
                "????????????buff(????????????) attrMutMap=" + attrMutMap);
        // ???????????????????????????
        addStoneMultEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "?????????????????????????????? attrMutMap=" + attrMutMap);
        // ??????????????????????????????
        addHeroFemaleAgentEffect(player, hero, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "???????????????????????? attrMutMap=" + attrMutMap);
        // ???????????????????????????
        addWarFireBuffEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), ",??????????????????????????? attrMutMap=" + attrMutMap);
        //?????????????????????
        addTotemLinkEffect(player, hero, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), ",????????????????????????????????? attrMutMap=" + attrMutMap);
        // ?????????????????????????????????
        addCrossWarFireBuffEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), ",????????????????????????????????? attrMutMap=" + attrMutMap);

        // ????????? = ?????? * ?????????
        processFinalAttr(tempMap, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "buff?????????????????? tempMap=" + tempMap);

        // ???????????????????????????
        reCalcAttrFight(player, Constant.ShowFightId.HERO, hero, attrMap, tempMap);
    }


    /**
     * ??????????????????????????????, ??????????????????
     *
     * @param player     ??????
     * @param attrMutMap ???????????????
     */
    private static void addWarFireBuffEffect(Player player, Map<Integer, Integer> attrMutMap) {
        if (player.lord.getArea() != CrossWorldMapConstant.CROSS_MAP_ID) {
            return;
        }
        int camp = player.lord.getCamp();
        Optional.ofNullable(DataResource.ac.getBean(CrossWorldMapDataManager.class))
                .ifPresent(bean -> {
                    Optional.ofNullable(bean.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID))
                            .ifPresent(cMap -> {
                                GlobalWarFire globalWarFire = cMap.getGlobalWarFire();
                                // ???????????????, ????????????????????????????????????, ?????????????????????????????????????????????
                                if (cMap.getPlayerMap().containsKey(player.roleId)) {
                                    // ????????????????????????buff??????
                                    Map<Integer, Integer> cityBuffs = cMap.getCityBuff(player);
                                    if (!CheckNull.isEmpty(cityBuffs)) {
                                        for (Entry<Integer, Integer> buff : cityBuffs.entrySet()) {
                                            int buffType = buff.getKey();
                                            int buffVal = buff.getValue();
                                            switch (buffType) {
                                                case StaticWarFire.BUFF_TYPE_3:
                                                    addAttrValue(attrMutMap, Constant.AttrId.ATK_MUT, buffVal);
                                                    break;
                                                case StaticWarFire.BUFF_TYPE_4:
                                                    addAttrValue(attrMutMap, Constant.AttrId.DEF_MUT, buffVal);
                                                    break;
                                                default:
                                                    break;
                                            }
                                        }
                                    }
                                    // ?????????buff??????
                                    Optional.ofNullable(globalWarFire.getPlayerWarFire(player.roleId))
                                            .ifPresent(pwf -> {
                                                pwf.getBuffs().values()
                                                        .stream()
                                                        .filter(buff -> buff.isInTime() && buff.getType() != StaticWarFireBuff.BUFF_TYPE_RECOVER_ARMY)
                                                        .map(buff -> StaticCrossWorldDataMgr.getWarFireBuffByTypeLv(buff.getType(), buff.getLv()))
                                                        .filter(Objects::nonNull)
                                                        .forEach(sBuff -> {
                                                            int seasonTalentBuff = DataResource.getBean(SeasonTalentService.class).
                                                                    getSeasonTalentEffectValueByFunc(player, SeasonConst.TALENT_EFFECT_604, sBuff.getType(), SeasonConst.TALENT_WAR_FIRE_ATTR);
                                                            int seasonBuff = (int) (sBuff.getBuffVal() * (1 + seasonTalentBuff / Constant.TEN_THROUSAND));
                                                            switch (sBuff.getType()) {
                                                                //????????????
                                                                case StaticWarFireBuff.BUFF_TYPE_ATTK:
                                                                    addAttrValue(attrMutMap, Constant.AttrId.ATK_MUT, seasonBuff);
                                                                    break;
                                                                case StaticWarFireBuff.BUFF_TYPE_DEF:
                                                                    addAttrValue(attrMutMap, Constant.AttrId.DEF_MUT, seasonBuff);
                                                                    break;
                                                                default:
                                                                    break;
                                                            }
                                                        });
                                            });
                                }
                            });
                });
    }

    private static void addCrossWarFireBuffEffect(Player player, Map<Integer, Integer> attrMutMap) {
        StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getOpenPlan(player, CrossFunction.CROSS_WAR_FIRE.getFunctionId());
        if (CheckNull.isNull(plan))
            return;

        CrossFunctionData crossFunctionData = player.crossPlayerLocalData.getCrossFunctionData(CrossFunction.CROSS_WAR_FIRE, plan.getKeyId(), false);
        if (!crossFunctionData.isInCross())
            return;

        CrossWarFireLocalData crossWarFireLocalData = (CrossWarFireLocalData) crossFunctionData;
        Optional.ofNullable(crossWarFireLocalData.getCityBuff()).ifPresent(buffs -> {
            buffs.forEach((buffType, buffVal) -> {
                switch (buffType) {
                    case NewCrossConstant.CrossWarFire.BUFF_TYPE_3:
                        addAttrValue(attrMutMap, Constant.AttrId.ATK_MUT, buffVal);
                        break;
                    case NewCrossConstant.CrossWarFire.BUFF_TYPE_4:
                        addAttrValue(attrMutMap, Constant.AttrId.DEF_MUT, buffVal);
                        break;
                    default:
                        break;
                }
            });
        });

        //??????????????????buff??????
        Optional.ofNullable(crossWarFireLocalData.getBuffs()).ifPresent(buffs -> {
            buffs.forEach((buffType, buffLv) -> {
                if (buffType == NewCrossConstant.CrossWarFire.BUFF_TYPE_RECOVER_ARMY)
                    return;

                StaticWarFireBuffCross sBuff = StaticCrossWarFireDataMgr.getWarFireBuffByTypeLv(buffType, buffLv);
                if (CheckNull.isNull(sBuff))
                    return;

                int seasonTalentBuff = DataResource.getBean(SeasonTalentService.class).
                        getSeasonTalentEffectValueByFunc(player, SeasonConst.TALENT_EFFECT_604, sBuff.getType(), SeasonConst.TALENT_WAR_FIRE_ATTR);
                int seasonBuff = (int) (sBuff.getBuffVal() * (1 + seasonTalentBuff / Constant.TEN_THROUSAND));
                switch (sBuff.getType()) {
                    //????????????
                    case NewCrossConstant.CrossWarFire.BUFF_TYPE_ATTACK:
                        addAttrValue(attrMutMap, Constant.AttrId.ATK_MUT, seasonBuff);
                        break;
                    case NewCrossConstant.CrossWarFire.BUFF_TYPE_DEF:
                        addAttrValue(attrMutMap, Constant.AttrId.DEF_MUT, seasonBuff);
                        break;
                    default:
                        break;
                }
            });
        });
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    private static void addHeroBiographyAttr(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        List<List<Integer>> attrList = DataResource.ac.getBean(HeroBiographyService.class).getFightAttr(player, hero);
        if (CheckNull.isEmpty(attrList)) return;

        Map<Integer, Integer> tempMap = new HashMap<>();
        Map<Integer, Integer> attrMutMap = new HashMap<>();// ???????????????
        for (List<Integer> attrList_ : attrList) {
            if (CheckNull.isEmpty(attrList_))
                continue;
            switch (attrList_.get(0)) {
                case Constant.AttrId.ATK_MUT:
                case Constant.AttrId.DEF_MUT:// ?????????????????????
                case Constant.AttrId.LEAD_MUT:// ?????????????????????
                    attrMutMap.merge(attrList_.get(0), attrList_.get(1), Integer::sum);
                    break;
                default:
                    tempMap.merge(attrList_.get(0), attrList_.get(1), Integer::sum);
                    break;
            }
        }

        // ????????? = ?????? * ?????????
        processFinalAttr(tempMap, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "???????????? tempMap=" + tempMap);
        //????????????showFight
        reCalcAttrFight(player, Constant.ShowFightId.HERO_BIOGRAPHY, hero, attrMap, tempMap);
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    private static void addSeasonTalent(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (hero.getStatus() != HeroConstant.HERO_STATUS_BATTLE) return;//????????????????????????????????????????????????
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);
        if (!seasonTalentService.checkTalentBuffOpen(player)) {
            return;
        }

        SeasonTalent talent = player.getPlayerSeasonData().getSeasonTalent();
        Map<Integer, Integer> tempMap = null;
        Map<Integer, StaticSeasonTalent> talentMap = StaticIniDataMgr.getSeasonTalentMap();
        for (Integer tid : talent.getLearns()) {
            StaticSeasonTalent sTalent = talentMap.get(tid);
            if (Objects.nonNull(sTalent)) {
                tempMap = Optional.ofNullable(tempMap).orElse(new HashMap<>());
                if (sTalent.getEffect() == SeasonConst.TALENT_EFFECT_101) {//????????????
                    for (List<Integer> params : sTalent.getEffectParam()) {
                        tempMap.merge(params.get(0), params.get(1), Integer::sum);
                    }
                }

                if (sTalent.getEffect() == SeasonConst.TALENT_EFFECT_201//??????
                        || sTalent.getEffect() == SeasonConst.TALENT_EFFECT_202) {//??????
                    tempMap.merge(AttrId.DMG_INC, sTalent.getEffectParam().get(0).get(0), Integer::sum);
                }

                //??????????????????
                //?????????????????????????????????????????? list [????????????????????????, ??????????????????????????????, ???????????????]
                if (sTalent.getEffect() == SeasonConst.TALENT_EFFECT_606) {
                    for (List<Integer> params : sTalent.getEffectParam()) {
                        if (Objects.nonNull(attrMap.get(params.get(1)))) {
                            tempMap.merge(params.get(0), (int) (attrMap.get(params.get(1))
                                    * (params.get(2) / Constant.TEN_THROUSAND)), Integer::sum);
                        }
                    }
                }
            }
        }

        //????????????showFight
        reCalcFinalAttrFight(player, Constant.ShowFightId.HERO, hero, attrMap, tempMap);
    }

    /**
     * ?????????????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    private static void addTreasureWare(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        Map<Integer, Integer> tempMap = new HashMap<>();
        try {
            if (CheckNull.isNull(attrMap)) {
                return;
            }
            // ?????????????????????????????????
            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            if (CheckNull.isNull(staticHero)) {
                return;
            }

            List<Turple<Integer, Integer>> basicAttr = DataResource.getBean(TreasureWareService.class).getBasicAttr(player, hero);
            if (ObjectUtils.isEmpty(basicAttr)) {
                return;
            }

            //????????????
            for (Turple<Integer, Integer> attr : basicAttr) {
                tempMap = Optional.ofNullable(tempMap).orElse(new HashMap<>());
                tempMap.merge(attr.getA(), attr.getB(), Integer::sum);
            }

            //????????????
            Object specialAttr = DataResource.getBean(TreasureWareService.class).getTreasureWareBuff(player, hero, TreasureWareConst.SpecialType.ADD_ATTR, 0);
            if (!ObjectUtils.isEmpty(specialAttr) && specialAttr instanceof List) {
                List<List<Integer>> buffEffect = (List<List<Integer>>) specialAttr;
                for (List<Integer> list : buffEffect) {
                    tempMap = Optional.ofNullable(tempMap).orElse(new HashMap<>());
                    tempMap.merge(list.get(0), list.get(1), Integer::sum);
                }
            }
        } finally {
            // ???????????????????????????
            reCalcAttrFight(player, Constant.ShowFightId.TREASURE_WARE, hero, attrMap, tempMap);
        }
    }

    /**
     * ????????????????????????
     *
     * @param hero
     * @param attrMap
     */
    private static void addHeroCgy(Hero hero, Map<Integer, Integer> attrMap) {
        StaticHeroClergy heroCgy = StaticHeroDataMgr.getHeroClergy(hero.getHeroId(), hero.getCgyStage(), hero.getCgyLv());
        if (Objects.nonNull(heroCgy)) {
            for (Entry<Integer, Integer> entry : heroCgy.getAttr().entrySet()) {
                addAttrValue(attrMap, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * ????????????
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    private static void addCastleSkinEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        Map<Integer, Integer> tempMap = new HashMap<>();// ????????????
        int pos = hero.getPos();
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (null == staticHero) {
            LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "???????????????????????????id??????????????????, heroId:",
                    hero.getHeroId());
            return;
        }
        if (hero.getStatus() != HeroConstant.HERO_STATUS_BATTLE) {//?????????????????????????????????
            return;
        }
        // ???????????? 1-4 ?????? ??????????????????????????????
        Optional.ofNullable(DataResource.ac.getBean(DressUpDataManager.class))
                .ifPresent(dressUpDataManager -> {
                    Map<Integer, BaseDressUpEntity> castleSkinMap = dressUpDataManager.getDressUpByType(player, AwardType.CASTLE_SKIN);
                    if (!CheckNull.isEmpty(castleSkinMap)) {
                        castleSkinMap
                                .values()
                                .stream()
                                .map(entity -> StaticLordDataMgr.getCastleSkinMapById(entity.getId()))
                                .filter(castleSkinCfg -> castleSkinCfg != null && castleSkinCfg.getEffectType() == StaticCastleSkin.EFFECT_TYPE_HERO_ATTR)
                                .forEach(currentSkin -> {
                                    int armyType = currentSkin.getArmyType();
                                    // ?????????????????????
                                    if (armyType != 0 && armyType != staticHero.getType()) {
                                        return;
                                    }
                                    if (currentSkin.getEffectType() == StaticCastleSkin.EFFECT_TYPE_HERO_ATTR) {
                                        Optional.ofNullable(currentSkin.getEffectParam())
                                                .ifPresent(keyIds -> keyIds.forEach(keyId -> {
                                                    CastleSkinEntity castleSkinEntity = (CastleSkinEntity) castleSkinMap.get(currentSkin.getId());
                                                    int currStar = castleSkinEntity.getStar();
                                                    currStar = currStar == 0 ? currentSkin.getStar() : currStar;
                                                    StaticCastleSkinStar staticCastleSkinStar = StaticLordDataMgr.getCastleSkinStarById(currentSkin.getId() * 100 + currStar);
                                                    if (Objects.nonNull(staticCastleSkinStar)) {
                                                        addAttrValue(tempMap, keyId, staticCastleSkinStar.getEffectVal());
                                                    }
                                                }));
                                    }
                                });
                    }
                    Map<Integer, BaseDressUpEntity> marchLineMap = dressUpDataManager.getDressUpByType(player, AwardType.MARCH_SPECIAL_EFFECTS);
                    if (!CheckNull.isEmpty(marchLineMap)) {
                        marchLineMap
                                .keySet()
                                .stream()
                                .map(StaticLordDataMgr::getMarchLine)
                                .filter(Objects::nonNull)
                                .filter(conf -> conf.getEffectType() == StaticMarchLine.EFFECT_TYPE_HERO_ATTR)
                                .forEach(conf -> {
                                    int armyType = conf.getArmyType();
                                    // ?????????????????????
                                    if (armyType != 0 && armyType != staticHero.getType()) {
                                        return;
                                    }
                                    if (conf.getEffectType() == StaticCastleSkin.EFFECT_TYPE_HERO_ATTR) {
                                        Optional.ofNullable(conf.getEffectParam())
                                                .ifPresent(keyIds -> keyIds.forEach(keyId -> {
                                                    addAttrValue(tempMap, keyId, conf.getEffectVal());
                                                }));
                                    }
                                });
                    }
                });
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "CastleSkin into title attrMap=" + attrMap);
        addTitleEffect(player, hero, attrMap, tempMap);
        // ???????????????????????????
        reCalcAttrFight(player, Constant.ShowFightId.CASTLE_SKIN, hero, attrMap, tempMap);
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     * @param tempMap
     */
    private static void addTitleEffect(Player player, Hero hero, Map<Integer, Integer> attrMap, Map<Integer, Integer> tempMap) {
        DressUpDataManager dressUpDataManager = DataResource.ac.getBean(DressUpDataManager.class);
        TitleService titleService = DataResource.ac.getBean(TitleService.class);
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        if (hero.getStatus() != HeroConstant.HERO_STATUS_BATTLE) {//??????????????????????????????
            return;
        }
        try {
            StaticLordDataMgr.getTitleMap().values().forEach(title -> {
                Map<Integer, BaseDressUpEntity> dressUpByType = dressUpDataManager.getDressUpByType(player, AwardType.TITLE);
                if (null == dressUpByType) {
                    return;
                }
                //?????????????????????????????????????????????????????????????????????
                if (null == dressUpByType.get(title.getId())) {
                    TitleEntity titleEntity = new TitleEntity(title.getId(), false);
                    dressUpByType.put(title.getId(), titleEntity);
                }
                TitleEntity titleEntity = (TitleEntity) dressUpByType.get(title.getId());
                if (null != title.getTaskId() && title.getTaskId() > 0) {
                    if (titleService.checkFinishTaskUnlock(player, titleEntity, ETask.getByType(title.getTaskId()), title)) {
                        if (null != title.getAttr() && !title.getAttr().isEmpty()) {
                            title.getAttr().entrySet().forEach(add -> {
                                addAttrValue(tempMap, add.getKey(), add.getValue());
                            });
                        }
                    }
                } else {
                    //????????????,???????????????
                    if (titleEntity.isPermanentHas() || titleEntity.getDuration() > 0) {
                        if (!title.getAttr().isEmpty()) {
                            title.getAttr().entrySet().forEach(add -> {
                                addAttrValue(tempMap, add.getKey(), add.getValue());
                            });
                        }
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.error(e, "????????????????????????????????????,id:" + player.getLordId() + ",hero:", hero);
        }
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param hero
     * @param tempMap
     */
    private static void addMentorEffect(Player player, Hero hero, Map<Integer, Integer> tempMap) {
        MentorInfo mentorInfo = player.getMentorInfo();
        int pos = hero.getPos();
        if (hero.getStatus() == HeroConstant.HERO_STATUS_BATTLE && pos >= HeroConstant.HERO_BATTLE_1
                && pos <= HeroConstant.HERO_BATTLE_4) {
            mentorInfo.getMentors().values().forEach(mentor -> {
                int id = mentor.getId();
                StaticMentor sMentor = StaticMentorDataMgr.getsMentorIdMap(id);
                if (!CheckNull.isNull(sMentor)) {
                    for (Entry<Integer, Integer> en : sMentor.getAttrUp().entrySet()) { // ??????????????????
                        addAttrValue(tempMap, en.getKey(), en.getValue());
                    }
                }
                LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Mentor Base tempMap=" + tempMap);
                for (Entry<Integer, Integer> en : mentor.getExtAttr().entrySet()) { // ?????????????????????
                    addAttrValue(tempMap, en.getKey(), en.getValue());
                }
                LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Mentor Ext tempMap=" + tempMap);
                int equipId = mentor.getEquips()[pos];
                if (equipId > 0) {
                    MentorEquip equip = mentorInfo.getEquipMap().get(equipId);
                    if (!CheckNull.isNull(equip)) {
                        for (Entry<Integer, Integer> en : equip.getAttr().entrySet()) { // ?????????????????????
                            addAttrValue(tempMap, en.getKey(), en.getValue());
                        }
                        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(),
                                "Mentor Equip Base tempMap=" + tempMap);
                        for (Entry<Integer, Integer> en : equip.getExtAttr().entrySet()) { // ?????????????????????
                            addAttrValue(tempMap, en.getKey(), en.getValue());
                        }
                        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(),
                                "Mentor Equip Ext tempMap=" + tempMap);
                    }
                }
                if (mentor.getType() == MentorConstant.MENTOR_TYPE_2) { // ????????????????????????
                } else if (mentor.getType() == MentorConstant.MENTOR_TYPE_3) { // ?????????
                    for (int i = 0; i < mentor.getSkills().length; i++) {
                        int skillType = mentor.getSkills()[i];
                        MentorSkill skill = mentorInfo.getSkillMap().get(skillType);
                        if (!CheckNull.isNull(skill)) {
                            StaticMentorSkill sSkill = StaticMentorDataMgr.getsMentorSkillIdMap(skill.getId());
                            if (!CheckNull.isNull(sSkill) && sSkill.getArmyType() != 0
                                    && sSkill.getArmyType() == hero.getType()) {
                                for (Entry<Integer, Integer> en : sSkill.getAttr().entrySet()) {
                                    addAttrValue(tempMap, en.getKey(), en.getValue());
                                }
                                LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), ",sSkillId:",
                                        skillType, ",Mentor Skill Base tempMap=" + tempMap);
                            }
                        }
                    }
                } else {
                    int skillType = mentor.getSkills()[pos];
                    if (skillType > 0) {
                        MentorSkill skill = mentorInfo.getSkillMap().get(skillType);
                        if (!CheckNull.isNull(skill)) {
                            StaticMentorSkill sSkill = StaticMentorDataMgr.getsMentorSkillIdMap(skill.getId());
                            if (!CheckNull.isNull(sSkill)) {
                                for (Entry<Integer, Integer> en : sSkill.getAttr().entrySet()) {
                                    addAttrValue(tempMap, en.getKey(), en.getValue());
                                }
                                LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), ",sSkillId:",
                                        skillType, ",Mentor Skill Base tempMap=" + tempMap);
                            }
                        }
                    }
                }
            });
        }

    }


    /**
     * ???????????????????????????
     *
     * @param player
     * @param fightId ????????????id
     * @param hero    ??????
     * @param attrMap
     * @param tempMap
     */
    private static void reCalcAttrFight(Player player, int fightId, Hero hero, Map<Integer, Integer> attrMap,
                                        Map<Integer, Integer> tempMap) {
        if (!CheckNull.isNull(tempMap)) {
            for (Entry<Integer, Integer> entry : tempMap.entrySet()) {
                addAttrValue(attrMap, entry.getKey(), entry.getValue());
            }
            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            if (null == staticHero) {
                LogUtil.error("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "???????????????????????????id??????????????????, heroId:",
                        hero.getHeroId());
                return;
            }
            // ?????????????????????
            tempMap.put(Constant.AttrId.LEAD,
                    getFinalLead(player, hero, staticHero.getLine(), tempMap.getOrDefault(Constant.AttrId.LEAD, 0)));
            if (hero.getPos() > 0 || hero.getCommandoPos() > 0) {
                int fight = reCalcFight(tempMap);
                Map<Integer, Integer> showFight = hero.getShowFight();
                int oldFight = showFight.getOrDefault(fightId, 0);
                if (oldFight != fight) {
                    showFight.put(fightId, fight);
                }
            }
        }
    }

    /**
     * ?????????????????????????????????showFight
     *
     * @param player
     * @param fightId
     * @param hero
     * @param attrMap
     * @param tempMap
     */
    private static void reCalcFinalAttrFight(Player player, int fightId, Hero hero, Map<Integer, Integer> attrMap,
                                             Map<Integer, Integer> tempMap) {
        if (!ObjectUtils.isEmpty(tempMap)) {
            for (Entry<Integer, Integer> entry : tempMap.entrySet()) {
                addAttrValue(attrMap, entry.getKey(), entry.getValue());
            }
            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            if (null == staticHero) {
                LogUtil.error("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "???????????????????????????id??????????????????, heroId:",
                        hero.getHeroId());
                return;
            }
            // ?????????????????????
            tempMap.put(Constant.AttrId.LEAD,
                    getFinalLead(player, hero, staticHero.getLine(), tempMap.getOrDefault(Constant.AttrId.LEAD, 0)));
            if (!CheckNull.isNull(hero) && (hero.getPos() > 0 || hero.getCommandoPos() > 0)) {
                Optional.ofNullable(hero.getShowFight()).
                        ifPresent(showFight -> showFight.merge(fightId, reCalcFight(tempMap), Integer::sum));
            }
        }
    }

    /**
     * ???????????????
     *
     * @param attrMap ?????????
     * @return ?????????
     */
    public static int reCalcFight(Map<Integer, Integer> attrMap) {
        ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);
        String environment = serverSetting.getEnvironment();
        int serverID = serverSetting.getServerID();
        Map<Integer, Integer> keyMap = FIGHT_K2;

        int fight = 0;
        if (!CheckNull.isNull(attrMap)) {
            for (Entry<Integer, Integer> entry : attrMap.entrySet()) {
                Integer key = entry.getKey();
                key = CheckNull.isNull(key) ? 0 : key;
                Integer val = entry.getValue();
                val = CheckNull.isNull(val) ? 0 : val;
                Integer radio = keyMap.get(key);
                radio = CheckNull.isNull(radio) ? 0 : radio;
                fight += radio * val;
            }
        }
        return fight;
    }

    /**
     * ??????????????????????????????
     *
     * @param hero
     * @return
     */
    public static int reCalcHeroTalentFight(Hero hero) {
        StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        Map<Integer, TalentData> talentDataMap = hero.getTalent();
        int talentFight = 0;
        for (TalentData talentData : talentDataMap.values()) {
            boolean activate = talentData.isActivate();
            if (activate) {
                int index = talentData.getIndex();
                Map<Integer, Integer> talentArr = talentData.getTalentArr();
                List<Integer> activateFight = sHero.getActivateFight();
                int fightFromActivateTalent = activateFight.get(index - 1);
                talentFight += fightFromActivateTalent;
                for (Entry<Integer, Integer> entry : talentArr.entrySet()) {
                    Integer part = entry.getKey();
                    Integer lv = entry.getValue();
                    StaticHeroEvolve sHeroEvolve = StaticHeroDataMgr.getHeroEvolve(sHero.getEvolveGroup().get(index - 1), index, part, lv);
                    if (sHeroEvolve != null && sHeroEvolve.getFight() > 0) {
                        talentFight += sHeroEvolve.getFight();
                    }
                }
            } else {
                talentFight += 0;
            }
        }

        hero.getShowFight().merge(Constant.ShowFightId.HERO, talentFight, Integer::sum);
        return talentFight;
    }

    /**
     * ????????????????????????
     *
     * @param hero
     * @return
     */
    public static int reCalcHeroSkillFight(Hero hero) {
        int skillFightVal = 0;
        for (Entry<Integer, Integer> entry : hero.getSkillLevels().entrySet()) {
            StaticHeroSeasonSkill heroSkill = StaticHeroDataMgr.getHeroSkill(hero.getHeroId(), entry.getKey(), entry.getValue());
            if (Objects.nonNull(heroSkill)) {
                StaticSkillAction ska = StaticFightDataMgr.getSkillAction(heroSkill.getSkillActionId());
                return ska != null ? ska.getFightVal() : 0;
            }
        }
        //?????????????????????????????????Constant.ShowFightId.HERO
        hero.getShowFight().merge(Constant.ShowFightId.HERO, skillFightVal, Integer::sum);
        return skillFightVal;
    }

    /**
     * ?????????,????????????,???????????? ???????????????,????????????????????????
     *
     * @param player
     * @param hero
     */
    public static void returnArmy(Player player, Hero hero) {
        if (player != null && hero != null && hero.getAttr()[Constant.AttrId.LEAD] < hero.getCount()) {
            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            int subArmy = hero.getCount() - hero.getAttr()[Constant.AttrId.LEAD];
            hero.setCount(hero.getAttr()[Constant.AttrId.LEAD]);
            RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
            // ???????????????????????????
            if (hero.isOnAcq() || hero.isOnBattle() || hero.isCommando()) {
                // ????????????
                rewardDataManager.modifyArmyResource(player, staticHero.getType(), subArmy, 0, AwardFrom.HERO_DOWN);
                LogUtil.calculate("roleId:", player.roleId,
                        " ????????????=" + subArmy + ",heroCnt=" + hero.getCount() + ",hero=" + hero);
                ChangeInfo change = ChangeInfo.newIns();
                change.addChangeType(AwardType.ARMY, staticHero.getType());
                // ????????????????????????????????????
                rewardDataManager.syncRoleResChanged(player, change);

//                int armyType = staticHero.getType();
//                AwardFrom from = AwardFrom.CALCULATE_CHANGE_FIGHT_ACTION;
                //??????????????????????????????
                // LogLordHelper.filterHeroArm(from, player.account, player.lord, hero.getHeroId(), hero.getCount(), -subArmy, Constant.ACTION_SUB, armyType, hero.getQuality());

                // ??????????????????????????????
//                LogLordHelper.playerArm(
//                        from,
//                        player,
//                        armyType,
//                        Constant.ACTION_SUB,
//                        -subArmy,
//                        DataResource.ac.getBean(PlayerDataManager.class).getArmCount(player.resource, armyType)
//                );
            }
        }
    }

    /**
     * ???????????????????????????ID??????
     *
     * @param player
     * @param hero
     * @return
     */
    public static Map<Integer, Integer> processAttr(Player player, Hero hero) {
        return processAttr(player, hero, true);
    }

    /**
     * ???????????????
     *
     * @param player
     * @param hero
     * @param baseLine
     */
    public static int getFinalLead(Player player, Hero hero, int baseLine, int lead) {
        // ????????????
        LogUtil.calculate("roleId:", player.roleId, ", heroId:", hero.getHeroId(), ", getFinalLead lead=" + lead);
        List<Integer> list = getTechEffect(player, TechConstant.TYPE_16);
        if (list != null && list.size() > 0) {
            baseLine += list.get(0);
        } else {
            list = getTechEffect(player, TechConstant.TYPE_7);
            if (list != null && list.size() > 0) {
                baseLine += list.get(0);
            }
        }
        lead *= baseLine * 1.00f / 4;
        LogUtil.calculate("roleId:", player.roleId, ", heroId:", hero.getHeroId(),
                ", getFinalLead lead=" + lead + ", baseLine=" + baseLine);
        // ??????????????????
        int leadLine = 0;
        if (player.cabinet != null) {
            int planId = player.cabinet.getEffectPlanId();
            StaticCabinetPlan cabinetPlan = StaticBuildingDataMgr.getCabinetPlanById(planId);
            if (cabinetPlan != null && !CheckNull.isEmpty(cabinetPlan.getEffect())
                    && cabinetPlan.getEffect().size() == 2) {
                if (hero.isOnAcq() && cabinetPlan.getEffect().get(1) > 0) {// ??????
                    leadLine += cabinetPlan.getEffect().get(1);
                } else if (hero.isOnBattle() && cabinetPlan.getEffect().get(0) > 0) {// ????????????
                    leadLine += cabinetPlan.getEffect().get(0);
                }
            }
        }

        //???????????????????????????
        if (hero.isOnWall()) {
            //?????????????????????
            TechDataManager techDataManager = DataResource.ac.getBean(TechDataManager.class);
            if (player.tech.getTechLv().containsKey(TechConstant.TYPE_32)) {
                leadLine += techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_32);
            } else {//?????????????????????
                leadLine += techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_31);
            }
        }

        lead *= (1 + leadLine * 1.0f / 4);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(),
                ",getFinalLead lead=" + lead + ",leadLine=" + leadLine);
        return lead;
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    private static void addSuperEquipEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        Map<Integer, Integer> tempMap = new HashMap<>();
        StaticSuperEquipLv staticSuperEquipLv = null;
        for (Entry<Integer, SuperEquip> kv : player.supEquips.entrySet()) {
            staticSuperEquipLv = StaticPropDataMgr.getSuperEquipLv(kv.getValue().getType(), kv.getValue().getLv());
            if (staticSuperEquipLv != null && staticSuperEquipLv.getAttrs() != null
                    && !staticSuperEquipLv.getAttrs().isEmpty()) {
                for (List<Integer> attr : staticSuperEquipLv.getAttrs()) {
                    addAttrValue(tempMap, attr.get(0), attr.get(1));
                }
            }
        }
        // ???????????????????????????
        reCalcAttrFight(player, Constant.ShowFightId.SUPER_EQUIP, hero, attrMap, tempMap);
    }

    /**
     * ??????????????????(?????????)
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    private static void addStoneEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        Map<Integer, Integer> tempMap = new HashMap<>();
        Map<Integer, StoneHole> stoneHoles = player.getStoneInfo().getStoneHoles();
        for (StoneHole hole : stoneHoles.values()) {
            if (hole.getType() == StoneHole.TYPE_STONE) {
                int stoneId = hole.getStoneId();
                StaticStone sStone = StaticPropDataMgr.getStoneMapById(stoneId);
                if (sStone != null) {
                    for (List<Integer> attr : sStone.getAttr()) {
                        addAttrValue(tempMap, attr.get(0), attr.get(1));
                    }
                }
            } else if (hole.getType() == StoneHole.TYPE_STONE_IMPROVE) {
                int keyId = hole.getStoneId();
                StoneImprove stoneImprove = player.getStoneInfo().getStoneImproves().get(keyId);
                if (stoneImprove != null) {
                    StaticStoneImprove sSi = StaticPropDataMgr.getStoneImproveById(stoneImprove.getStoneImproveId());
                    if (sSi != null) {
                        for (List<Integer> attr : sSi.getAttr()) {
                            addAttrValue(tempMap, attr.get(0), attr.get(1));
                        }
                    }
                }

            }
        }
        reCalcAttrFight(player, Constant.ShowFightId.STONE, hero, attrMap, tempMap);
    }

    /**
     * ?????????????????????(?????????)
     *
     * @param player
     * @param attrMutMap
     */
    private static void addStoneMultEffect(Player player, Map<Integer, Integer> attrMutMap) {
        if (CheckNull.isNull(attrMutMap)) {
            return;
        }
        Map<Integer, StoneHole> stoneHoles = player.getStoneInfo().getStoneHoles();
        for (StoneHole hole : stoneHoles.values()) {
            if (hole.getType() == StoneHole.TYPE_STONE_IMPROVE) {
                int keyId = hole.getStoneId();
                StoneImprove stoneImprove = player.getStoneInfo().getStoneImproves().get(keyId);
                if (stoneImprove != null) {
                    StaticStoneImprove sSi = StaticPropDataMgr.getStoneImproveById(stoneImprove.getStoneImproveId());
                    if (sSi != null) {
                        for (List<Integer> attr : sSi.getAttrMult()) {
                            addAttrValue(attrMutMap, attr.get(0), attr.get(1));
                        }
                    }
                }

            }
        }

    }


    /**
     * ?????????????????????????????????, ??????????????????
     *
     * @param player     ????????????
     * @param hero       ??????
     * @param attrMutMap ???????????????
     */
    private static void addHeroFemaleAgentEffect(Player player, Hero hero, Map<Integer, Integer> attrMutMap) {
        if (CheckNull.isNull(attrMutMap)) {
            return;
        }
        Cia cia = player.getCia();
        if (cia != null && (hero.getPos() > 0 || hero.getCommandoPos() > 0)) {// ?????????????????????
            for (FemaleAgent fa : cia.getFemaleAngets().values()) {
                // ???????????????
                Optional.ofNullable(StaticCiaDataMgr.getAgentConfByAgent(fa))
                        .ifPresent(sAgent -> {
                            int armyType = sAgent.getArmyType();
                            if (armyType != 4 && armyType != hero.getType()) {
                                return;
                            }
                            addAttrValue(attrMutMap, sAgent.getAttributeId(), sAgent.getAttributeVal());
                        });
            }
        }
    }

    /**
     * ????????????buff??????
     *
     * @param player
     * @param attrMutMap
     */
    private static void addRebelBuffEffect(Player player, Map<Integer, Integer> attrMutMap) {
        PlayerRebellion playerRebellion = player.getPlayerRebellion();
        int now = TimeHelper.getCurrentSecond();
        if (playerRebellion != null) {
            for (RebelBuff buff : playerRebellion.getBuffs().values()) {
                if (now >= buff.getStartTime() && now < buff.getEndTime()
                        && buff.getType() != StaticRebelBuff.BUFF_TYPE_RECOVER_ARMY) {
                    StaticRebelBuff sBuff = StaticWorldDataMgr.getRebelBuffByTypeLv(buff.getType(), buff.getLv());
                    if (sBuff != null) {
                        switch (sBuff.getType()) {
                            case StaticRebelBuff.BUFF_TYPE_ATTK:
                                addAttrValue(attrMutMap, Constant.AttrId.ATK_MUT, sBuff.getBuffVal());
                                break;
                            case StaticRebelBuff.BUFF_TYPE_DEF:
                                addAttrValue(attrMutMap, Constant.AttrId.DEF_MUT, sBuff.getBuffVal());
                                break;
                            default:
                                break;
                        }
                    } else {
                        LogUtil.error("??????????????????,???????????????buff??????????????? roleId:", player.roleId, ", buffType:", buff.getType(),
                                ", buffLv:", buff.getLv());
                    }
                }
            }
        }
    }

    /**
     * ??????buff??????
     *
     * @param player
     * @param hero
     * @param tempMap
     */
    private static void addCrossBuffEffect(Player player, Hero hero, Map<Integer, Integer> tempMap) {
        if (hero.getStatus() == HeroConstant.HERO_STATUS_BATTLE) { // ??????????????????????????????
            CrossPersonalData crossPersonalData = player.getAndCreateCrossPersonalData();
            int now = TimeHelper.getCurrentSecond();
            for (CrossBuff buff : crossPersonalData.getBuffs().values()) {
                if (now >= buff.getStartTime() && now < buff.getEndTime()) {
                    StaticCrossBuff sBuff = StaticCrossDataMgr.getBuffByTypeLv(buff.getType(), buff.getLv());
                    if (sBuff != null) {
                        addAttrValue(tempMap, sBuff.getType(), sBuff.getBuffVal());
                    } else {
                        LogUtil.error("??????????????????,??????buff??????????????? roleId:", player.roleId, ", buffType:", buff.getType(),
                                ", buffLv:", buff.getLv());
                    }
                }
            }
        }
    }

    /**
     * ??????Buff??????
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    @Deprecated
    private static void addCityBuffEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        // ??????????????????
        if (!CheckNull.isNull(hero) && hero.getPos() > 0) {
            WorldDataManager worldDataManager = DataResource.ac.getBean(WorldDataManager.class);
            CommonPb.TwoInt cityStatus = worldDataManager.checkCityBuffer(player.lord.getPos());
            if (!CheckNull.isNull(cityStatus)) {
                // int atkBuff = (int) worldDataManager.getCityBuffer(cityStatus, WorldConstant.CityBuffer.ATK_BUFFER,
                // player.roleId);
                // if (atkBuff > 0) {
                // addAttrValue(attrMap, HeroConstant.ATTR_ATTACK, atkBuff);
                // }
                int defBuff = (int) worldDataManager.getCityBuffer(cityStatus, WorldConstant.CityBuffer.DEF_BUFFER,
                        player.roleId);
                if (defBuff > 0) {
                    addAttrValue(attrMap, HeroConstant.ATTR_DEFEND, defBuff);
                }
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param sPlaneInit
     * @param sPlaneUp
     * @param attrId
     * @param lv
     * @return
     */
    public static int calcPlaneAttrById(StaticPlaneInit sPlaneInit, StaticPlaneUpgrade sPlaneUp, int attrId, int lv) {
        if (CheckNull.isNull(sPlaneInit) || CheckNull.isNull(sPlaneUp)) {
            return 0;
        }
        float ratio = 0;
        // ????????????
        int planeRadio = PlaneConstant.getPlaneRadioByAttrId(attrId);
        if (planeRadio >= 0) {
            ratio = (float) (planeRadio / Constant.TEN_THROUSAND);
        }
        // ????????????
        int baseRatio = sPlaneUp.getBaseRatioById(attrId);
        // ????????????
        int baseAttr = sPlaneInit.getBaseAttrById(attrId);
        // ??????= ?????? *??????????????? - 1???* ?????? + ????????????
        double value = baseRatio * (lv - 1) * ratio + baseAttr;
        LogUtil.calculate("????????????, attrId=" + attrId + ", ????????????=" + baseRatio + ", ??????=" + lv + ", ??????=" + ratio + ", ????????????="
                + baseAttr + ", value=" + value);
        return (int) value;
    }

    /**
     * ????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    public static void addPlaneEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        Map<Integer, Integer> tempMap = new HashMap<>();
        List<Integer> warPlanes = hero.getWarPlanes();
        if (!CheckNull.isEmpty(warPlanes)) {
            warPlanes.forEach(planeId -> {
                try {
                    WarPlane plane = player.checkWarPlaneIsExist(planeId);
                    StaticPlaneUpgrade sPlaneUpgrade = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
                    StaticPlaneInit sPlaneInit = StaticWarPlaneDataMgr.getPlaneInitByType(sPlaneUpgrade.getPlaneType());
                    if (CheckNull.isNull(sPlaneInit) || CheckNull.isNull(plane)) {
                        throw new MwException(GameError.PLANE_CONFIG_NOT_FOUND.getCode(),
                                "?????????????????????, ???????????????????????????, planeId:", planeId);
                    }
                    // ????????????
                    for (int attr : Constant.ATTRS) {
                        addAttrValue(tempMap, attr,
                                calcPlaneAttrById(sPlaneInit, sPlaneUpgrade, attr, plane.getLevel()));
                    }
                } catch (MwException e) {
                    LogUtil.error("", e);
                }
            });
        }
        // ????????????????????????????????????????????????
        MentorInfo mentorInfo = player.getMentorInfo();
        Mentor mentor = mentorInfo.getMentors().values().stream()
                .filter(m -> m.getType() == MentorConstant.MENTOR_TYPE_2).findFirst().orElse(null);
        if (!CheckNull.isNull(mentor)) {
            // ???????????????????????????
            for (int i = 0; i < mentor.getSkills().length; i++) {
                int skillType = mentor.getSkills()[i];
                MentorSkill skill = mentorInfo.getSkillMap().get(skillType);
                if (!CheckNull.isNull(skill) && skill.isActivate() && hero.getPos() == HeroConstant.HERO_BATTLE_1) {
                    StaticMentorSkill sSkill = StaticMentorDataMgr.getsMentorSkillIdMap(skill.getId());
                    if (!CheckNull.isNull(sSkill)) {
                        for (Entry<Integer, Integer> en : sSkill.getAttr().entrySet()) {
                            addAttrValue(tempMap, en.getKey(), en.getValue());
                        }
                        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), ",sSkillId:", skillType,
                                ",Mentor2 Skill Base tempMap=" + tempMap);
                    }
                }
            }
        }
        // ???????????????????????????
        reCalcAttrFight(player, Constant.ShowFightId.PLANE, hero, attrMap, tempMap);
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    private static void addDecoratedEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (hero.getDecorated() > 0) {
            StaticHeroDecorated sHeroDecorated = StaticHeroDataMgr.getHeroDecoratedMap().get(hero.getDecorated());
            if (sHeroDecorated != null && sHeroDecorated.getAddAttr() != null) {
                for (Entry<Integer, Integer> attrVal : sHeroDecorated.getAddAttr().entrySet()) {
                    addAttrValue(attrMap, attrVal.getKey(), attrVal.getValue());
                }
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param sHero   ????????????
     * @param hero    ??????
     * @param attrMap ??????
     */
    private static void addAwakenEffect(StaticHero sHero, Hero hero, Map<Integer, Integer> attrMap) {
        if (hero.getDecorated() > 0) {
            Map<Integer, TalentData> talentMap = hero.getTalent();
            if (CheckNull.nonEmpty(talentMap)) {
                talentMap.values().forEach(talentData -> {
                    if (CheckNull.isNull(talentData)) {
                        return;
                    }
                    if (talentData.isActivate()) {
                        // ?????????
                        if (CheckNull.isEmpty(sHero.getEvolveGroup()) || talentData.getIndex() > sHero.getEvolveGroup().size()) {
                            return;
                        }
                        // ?????????????????????????????????
                        for (Entry<Integer, Integer> en : sHero.getActivateAttr().entrySet()) {
                            addAttrValue(attrMap, en.getKey(), en.getValue());
                        }
                        // ???????????????????????????
                        List<StaticHeroEvolve> staticHeroEvolveList = StaticHeroDataMgr.getHeroEvolve(sHero.getEvolveGroup().get(talentData.getIndex() - 1));
                        if (!CheckNull.isEmpty(staticHeroEvolveList) && !CheckNull.isEmpty(talentData.getTalentArr())) {
                            // ????????????????????????????????????????????????????????????????????????????????????0?????????attr????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                            talentData.getTalentArr().forEach((part, lv) -> {
                                StaticHeroEvolve staticHeroEvolve = staticHeroEvolveList.stream()
                                        .filter(she -> she.getPart() == part && she.getLv() == lv && she.getAttr() != null)
                                        .findFirst().orElse(null);
                                if (staticHeroEvolve != null) {
                                    for (Entry<Integer, Integer> en : staticHeroEvolve.getAttr().entrySet()) {
                                        addAttrValue(attrMap, en.getKey(), en.getValue());
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    private static void addFemaleAgentEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        Map<Integer, Integer> tempMap = new HashMap<>();// ????????????
        Cia cia = player.getCia();
        if (cia != null && (hero.getPos() > 0 || hero.getCommandoPos() > 0)) {// ?????????????????????
            for (FemaleAgent fa : cia.getFemaleAngets().values()) {
                // ????????????
                Optional.ofNullable(StaticCiaDataMgr.getStaticAgentStar(fa))
                        .ifPresent(sAgentStar -> {
                            int armyType = sAgentStar.getArmyType();
                            if (armyType != 4 && armyType != hero.getType()) {
                                return;
                            }
                            addAttrValue(tempMap, sAgentStar.getAttributeId(), sAgentStar.getAttributeVal());
                        });
            }
        }
        reCalcAttrFight(player, Constant.ShowFightId.FEMALE_AGENT, hero, attrMap, tempMap);
    }

    /**
     * ??????/??????/?????? ??????????????????
     *
     * @param player
     * @param attrMap
     * @param heroType
     */
    private static void addTechEffect(Player player, Map<Integer, Integer> attrMap, int heroType) {
        int techType = 0;
        if (heroType == 1) {
            techType = TechConstant.TYPE_8;
        } else if (heroType == 2) {
            techType = TechConstant.TYPE_9;
        } else if (heroType == 3) {
            techType = TechConstant.TYPE_15;
        }
        List<Integer> list = getTechEffect(player, techType);
        if (list != null && list.size() > 0) {
            addAttrValue(attrMap, HeroConstant.ATTR_ATTACK, list.get(0));
        }
    }

    /**
     * ??????/??????/?????? ??????????????????
     *
     * @param player
     * @param attrMap
     * @param heroType
     */
    private static void addIntensifyTechEffect(Player player, Map<Integer, Integer> attrMap, int heroType) {
        int techType = 0;
        if (heroType == 1) {
            techType = TechConstant.TYPE_28;
        } else if (heroType == 2) {
            techType = TechConstant.TYPE_29;
        } else if (heroType == 3) {
            techType = TechConstant.TYPE_30;
        }
        List<Integer> list = getTechEffect(player, techType);
        if (list != null && list.size() >= 3) {
            addAttrValue(attrMap, HeroConstant.ATTR_ATTACK, list.get(1));// ?????????
            addAttrValue(attrMap, HeroConstant.ATTR_DEFEND, list.get(2));// ?????????
        }
    }

    public static List<Integer> getTechEffect(Player player, int techType) {
        Tech tech = player.tech;
        if (tech == null) {
            return null;
        }
        TechLv techLv = tech.getTechLv().get(techType);
        if (techLv == null) {
            return null;
        }
        StaticTechLv staticTechLv = StaticBuildingDataMgr.getTechLvMap(techLv.getId(), techLv.getLv());
        if (staticTechLv == null) {
            return null;
        }
        return staticTechLv.getEffect();
    }

    /**
     * ????????????*???????????????
     *
     * @param attrMap
     * @param attrMutMap
     */
    public static void processFinalAttr(Map<Integer, Integer> attrMap, Map<Integer, Integer> attrMutMap) {
        for (Entry<Integer, Integer> kv : attrMutMap.entrySet()) {
            switch (kv.getKey()) {
                case Constant.AttrId.ATK_MUT:
                    Integer v = attrMap.get(Constant.AttrId.ATTACK);
                    if (v == null) {
                        continue;
                    }
                    addAttrValue(attrMap, Constant.AttrId.ATTACK, (int) (v * (kv.getValue() / Constant.TEN_THROUSAND)));
                    break;
                case Constant.AttrId.DEF_MUT:
                    v = attrMap.get(Constant.AttrId.DEFEND);
                    if (v == null) {
                        continue;
                    }
                    addAttrValue(attrMap, Constant.AttrId.DEFEND, (int) (v * (kv.getValue() / Constant.TEN_THROUSAND)));
                    break;
                case Constant.AttrId.LEAD_MUT:// ??????????????? ,??????????????????????????????
                    v = attrMap.get(Constant.AttrId.LEAD);
                    if (v == null) {
                        continue;
                    }
                    addAttrValue(attrMap, Constant.AttrId.LEAD, (int) (v * (kv.getValue() / Constant.TEN_THROUSAND)));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param player ??????
     * @param hero   ??????
     * @return ???????????????
     */
    public static Map<Integer, Integer> getAttrMutMap(Player player, Hero hero) {
        Map<Integer, Integer> attrMutMap = new HashMap<>();// ???????????????
        // buff?????? ?????????, ????????????????????????
        addEffectVal(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "??????buff(????????????) attrMutMap=" + attrMutMap);
        // ????????????buff
        addRebelBuffEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "????????????buff(????????????) attrMutMap=" + attrMutMap);
        // ???????????????????????????
        addStoneMultEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "?????????????????????????????? attrMutMap=" + attrMutMap);
        // ??????????????????????????????
        addHeroFemaleAgentEffect(player, hero, attrMutMap);//???????????????????????????
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "???????????????????????? attrMutMap=" + attrMutMap);
        // ???????????????????????????
        addWarFireBuffEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), ",??????????????????????????? attrMutMap=" + attrMutMap);
        return attrMutMap;
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    public static void addEquipEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        HashMap<Integer, Integer> tempMap = new HashMap<>();
        Equip equip;
        Ring ring = null;
        StaticEquip staticEquip;
        for (int equipKeyId : hero.getEquip()) {
            if (equipKeyId > 0) {
                equip = player.equips.get(equipKeyId);
                if (null == equip) {
                    LogUtil.error("???????????????????????????????????????, equipKeyId:", equipKeyId);
                    continue;
                }

                staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
                if (null == staticEquip) {
                    LogUtil.error("?????????????????????id?????????, equipId:", equip.getEquipId());
                    continue;
                }

                // ????????????????????????????????????
                int val = 0;
                if (equip instanceof Ring) {
                    ring = (Ring) equip;
                    List<Integer> jewels = ring.getJewels();
                    if (!CheckNull.isEmpty(jewels)) {
                        for (int jewel : jewels) {
                            StaticJewel jewelConf = StaticPropDataMgr.getJewelByLv(jewel);
                            if (!CheckNull.isNull(jewelConf)) {
                                val += jewelConf.getValue();
                            }
                        }
                    }
                }

                // ???????????????
                if (staticEquip.getArmType() != 0 && staticEquip.getArmType() != hero.getType()) {
                    continue;
                }

                // ????????????????????????
                for (Entry<Integer, Integer> entry : staticEquip.getAttr().entrySet()) {
                    addAttrValue(tempMap, entry.getKey(), entry.getValue());
                }

                // ????????????????????????
                List<Turple<Integer, Integer>> attrLvs = equip.getAttrAndLv();
                for (int j = 0; j < attrLvs.size(); j++) {
                    Turple<Integer, Integer> al = attrLvs.get(j);
                    StaticEquipExtra equipExtra = StaticPropDataMgr.getEuqipExtraByIdAndLv(al.getA(), al.getB(),
                            staticEquip.getEquipPart());
                    if (equipExtra != null) {
                        // ??????????????????????????????
                        int attrValue = val > 0
                                ? (int) (equipExtra.getAttrValue() * (1 + (val / Constant.TEN_THROUSAND)))
                                : equipExtra.getAttrValue();
                        addAttrValue(tempMap, al.getA(), attrValue);
                    }
                }
                // ??????????????????
                if (equip instanceof Ring) {
                    StaticRingStrengthen ringConf = StaticPropDataMgr.getRingConfByLv(staticEquip.getEquipId(),
                            ring.getLv());
                    if (ringConf == null) {
                        LogUtil.error("?????????????????????id?????????, equipId:", equip.getEquipId(), ", lv:", ring.getLv());
                        continue;
                    }
                    // ?????????????????????
                    int count = ring.getCount();
                    // ????????????
                    List<List<Integer>> baseAttr = ringConf.getAttr();
                    if (!CheckNull.isEmpty(baseAttr)) {
                        for (List<Integer> list : baseAttr) {
                            addAttrValue(tempMap, list.get(0), list.get(1));
                        }
                    }
                    // ???????????? = ???????????? / ????????????????????? * ???????????????
                    List<List<Integer>> exAttr = ringConf.getExAttr();
                    if (!CheckNull.isEmpty(exAttr)) {
                        for (List<Integer> list : exAttr) {
                            int baseRadio = list.get(0);
                            int cnt = count / baseRadio;
                            cnt = cnt >= 1 ? cnt : 1;
                            addAttrValue(tempMap, list.get(1), list.get(2) * cnt);
                        }
                    }
                    // 2019-04-11 LYJ???????????????????????? ????????????(?????????????????????, ????????????????????????, ?????????????????????) = ???????????? / ????????????????????? * ???????????????
                    List<List<Integer>> upAttr = ringConf.getUpAttr();
                    if (!CheckNull.isEmpty(upAttr)) {
                        for (List<Integer> list : upAttr) {
                            addAttrValue(tempMap, list.get(0), list.get(1));
                        }
                    }
                }
            }
        }
        // ???????????????????????????
        reCalcAttrFight(player, Constant.ShowFightId.EQUIP, hero, attrMap, tempMap);
    }

    /**
     * ??????-????????????
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    public static void addMedalEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        // ?????????????????????????????????
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (CheckNull.isNull(staticHero)) {
            return;
        }
        Map<Integer, Integer> tempMap = new HashMap<>();

        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);

        // ????????????id ????????????????????? ??????????????????null
        List<Medal> medals = medalDataManager.getHeroMedalByHeroId(player, hero.getHeroId());
        if (!CheckNull.isEmpty(medals)) {
            for (Medal medal : medals) {
                StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                if (CheckNull.isNull(sHero)) {
                    return;
                }

                // ????????????????????????
                addAttrValue(tempMap, medal.getMedalAttr().getA(), medal.getMedalAttr().getB());

                RedMedal redMedal = null;
                if (medal instanceof RedMedal) {
                    redMedal = (RedMedal) medal;
                }

                // ??????????????????????????????
                if (medal.hasAuraSkill()) {
                    // ?????????????????????????????????
                    boolean canUseSkill = redMedal == null || redMedal.isAuraUnLock();
                    if (canUseSkill) {
                        StaticMedalAuraSkill staticMedalAuraSkill = StaticMedalDataMgr.getAuraSkillById(medal.getAuraSkillId());
                        // TODO: 2020/9/23 ???????????????????????????????????????????????????????????????
                        if (staticMedalAuraSkill != null && staticMedalAuraSkill.getArmType() == staticHero.getType()) {
                            List<List<Integer>> attrs = staticMedalAuraSkill.getAttrEffect();
                            if (!CheckNull.isEmpty(attrs)) {
                                for (List<Integer> list : attrs) {
                                    if (list.size() > 1) {
                                        addAttrValue(tempMap, list.get(0), list.get(1));
                                    }
                                }
                            }
                        }
                    }
                }

                // ??????????????????????????????
                if (medal.getSpecialSkillId() != null && medal.getSpecialSkillId() > 0) {
                    StaticMedalSpecialSkill staticMedalSpecialSkill = StaticMedalDataMgr
                            .getSpecialSkillById(medal.getSpecialSkillId());
                    if (staticMedalSpecialSkill != null) {
                        addAttrValue(tempMap, staticMedalSpecialSkill.getAttrId(),
                                staticMedalSpecialSkill.getAttrEffect());
                    }
                    // ????????????????????????
                    // TODO: 2019/11/5 ??????????????????????????????  MedalConst.BLITZ_CURIOUS_SOLDIER MedalConst.IRON_BASTIONS
                    /*if (medal.getSpecialSkillId() == MedalConst.BLITZ_CURIOUS_SOLDIER) {
                        int defTown = attrMap.getOrDefault(AttrId.DEFEND_TOWN, 0);
                        if (defTown > 0) {
                            addAttrValue(tempMap, AttrId.ATTACK_TOWN, (int) (defTown / 2.00));
                        }
                    } else if (medal.getSpecialSkillId() == MedalConst.IRON_BASTIONS) {
                        int atkTownn = attrMap.getOrDefault(AttrId.ATTACK_TOWN, 0);
                        if (atkTownn > 0) {
                            addAttrValue(tempMap, AttrId.DEFEND_TOWN, (int) (atkTownn / 2.00));
                        }
                    }*/
                }

                // ???????????????????????????????????????
                if (!CheckNull.isEmpty(medal.getGeneralSkillId())) {
                    for (int genaralSkillId : medal.getGeneralSkillId()) {
                        StaticMedalGeneralSkill staticMedalGeneralSkill = StaticMedalDataMgr
                                .getGeneralSkillById(genaralSkillId);
                        // ???????????????
                        if (staticMedalGeneralSkill.getIntimateLv() > 0) {
                            int countLv = RankDataManager.calcAgentAllLv(player);// ?????????????????????
                            if (countLv < staticMedalGeneralSkill.getIntimateLv()) {
                                continue;
                            }
                        }
                        // ???????????????????????????????????????
                        if (!CheckNull.isEmpty(staticMedalGeneralSkill.getSkillEffect())) {
                            if (staticMedalGeneralSkill.getSkillEffect().size() > 1) {// ?????????????????????
                                if (staticMedalGeneralSkill.getSkillEffect().get(0) != sHero.getType()) {
                                    continue;
                                }
                                // ????????????
                                addAttrValue(tempMap, staticMedalGeneralSkill.getAttrId(),
                                        staticMedalGeneralSkill.getSkillEffect().get(1));
                            } else {
                                // ????????????
                                addAttrValue(tempMap, staticMedalGeneralSkill.getAttrId(),
                                        staticMedalGeneralSkill.getSkillEffect().get(0));
                            }
                        }
                    }
                }
            }
            // ????????????????????????
            int specialSkill = medalDataManager.getHeroSpecialSkill(player, hero.getHeroId(),
                    MedalConst.HERO_MEDAL_INDEX_1);
            if (specialSkill != 0) {
                StaticMedalSpecialSkill sSpecialSkill = StaticMedalDataMgr.getSpecialSkillById(specialSkill);
                if (sSpecialSkill != null) {
                    List<Integer> skillEffect = sSpecialSkill.getSkillEffect();
                    if (specialSkill == MedalConst.INTENSIFY_MILITARY_MERIT_IS_PROMINENT) {
                        if (!CheckNull.isEmpty(skillEffect)) {
                            addAttrValue(tempMap, AttrId.DEFEND_EXT, skillEffect.get(0));
                        }
                    } else if (specialSkill == MedalConst.INTENSIFY_ANGEL_IN_WHITE) {
                        int atkTownn = attrMap.getOrDefault(AttrId.ATTACK_TOWN, 0);
                        if (atkTownn > 0) {
                            addAttrValue(tempMap, AttrId.DEFEND_TOWN, (int) (atkTownn / 2.00));
                        }
                    } else if (specialSkill == MedalConst.INTENSIFY_SUSTAIN_THE_WAR_BY_MEANS_OF_WAR) {
                        int defTown = attrMap.getOrDefault(AttrId.DEFEND_TOWN, 0);
                        if (defTown > 0) {
                            addAttrValue(tempMap, AttrId.ATTACK_TOWN, (int) (defTown / 2.00));
                        }
                    }
                }
            }
        }

        // ???????????????????????????
        reCalcAttrFight(player, Constant.ShowFightId.MEDAL, hero, attrMap, tempMap);
    }

    /**
     * buff???????????????
     *
     * @param player
     * @param attrMutMap
     */
    public static void addEffectSpecificVal(Player player, Hero hero, Map<Integer, Integer> attrMutMap) {
        // ??????????????????
        if (hero.getStatus() != HeroConstant.HERO_STATUS_BATTLE) {
            return;
        }
        for (Entry<Integer, Effect> kv : player.getEffect().entrySet()) {
            Effect effect = kv.getValue();
            if (effect != null) {
                //????????????
                double seasonTalentBuff = DataResource.getBean(SeasonTalentService.class).
                        getSeasonTalentEffectValueByFunc(player, SeasonConst.TALENT_EFFECT_604, effect.getEffectType(), SeasonConst.TALENT_BERLIN_ATTR) / Constant.TEN_THROUSAND;
                switch (effect.getEffectType()) {
                    case EffectConstant.PREWAR_ATK:
                        addAttrValue(attrMutMap, Constant.AttrId.ATTACK, (int) (effect.getEffectVal() * (1 + seasonTalentBuff)));
                        break;
                    case EffectConstant.PREWAR_DEF:
                        addAttrValue(attrMutMap, Constant.AttrId.DEFEND, (int) (effect.getEffectVal() * (1 + seasonTalentBuff)));
                        break;
                    case EffectConstant.PREWAR_LEAD:
                        addAttrValue(attrMutMap, AttrId.LEAD, (int) (effect.getEffectVal() * (1 + seasonTalentBuff)));
                        break;
                    case EffectConstant.PREWAR_ATTACK_EXT:
//                        addAttrValue(attrMutMap, Constant.AttrId.FIGHT, FIGHT_K2.get(Constant.AttrId.ATTACK_EXT) * effect.getEffectVal());
                        addAttrValue(attrMutMap, Constant.AttrId.ATTACK_EXT, (int) (effect.getEffectVal() * (1 + seasonTalentBuff)));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * buff??????(?????????)
     *
     * @param player
     * @param attrMutMap
     */
    private static void addEffectVal(Player player, Map<Integer, Integer> attrMutMap) {
        for (Entry<Integer, Effect> kv : player.getEffect().entrySet()) {
            Effect effect = kv.getValue();
            if (effect != null) {
                switch (effect.getEffectType()) {
                    case EffectConstant.ATK_MUT:
                        addAttrValue(attrMutMap, Constant.AttrId.ATK_MUT, effect.getEffectVal());
                        break;
                    case EffectConstant.DEF_MUT:
                        addAttrValue(attrMutMap, Constant.AttrId.DEF_MUT, effect.getEffectVal());
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * ????????????
     *
     * @param attrMap ???????????????
     * @param attrId  ?????????key?????? {@link AttrId}
     * @param value   ?????????(oldVal + NewVal)
     */
    public static void addAttrValue(Map<Integer, Integer> attrMap, int attrId, int value) {
        if (value == 0) {
            return;
        }
        // JDK1.8?????????????????????, ?????????????????????????????????, ???????????????Function??????
        int oldValue = attrMap.getOrDefault(attrId, 0);
        attrMap.put(attrId, oldValue + value);
    }

    /**
     * ??????????????????buff?????????
     *
     * @param player
     * @param now
     * @param banditLv
     * @return
     */
    public static double getRecoverArmyEffect(Player player, int now, int banditLv) {
        double nightRaidRecArmyEffect = StaticNightRaidMgr.getNightRaidRecArmyEffect(player, now, banditLv);// ????????????
        double medicalEffect = 0.0;// ???????????????
        return nightRaidRecArmyEffect + medicalEffect;
    }

    /**
     * ?????????????????????
     * <p>
     * ?????? ??????=??????*????????? ????????????
     *
     * @param player
     * @param now
     * @param banditLv
     * @param lostArmy
     * @return
     */
    public static Map<Integer, Integer> calcRecoverArmyCntByNight(Player player, int now, int banditLv,
                                                                  Map<Integer, Integer> lostArmy) {
        final Map<Integer, Integer> cntMap = new HashMap<>();
        final double recoverArmyEffect = getRecoverArmyEffect(player, now, banditLv);
        lostArmy.forEach((armyType, lostCnt) -> {
            int cnt = (int) (lostCnt * recoverArmyEffect);// ????????????
            if (cnt > 0) {
                cntMap.put(armyType, cnt);
            }
        });
        return cntMap;
    }

    /**
     * ????????????????????????
     *
     * @param mentor
     * @param player
     */
    public static void calcMentorExtAttr(Mentor mentor, Player player) {
        mentor.getExtAttr().clear(); // ??????????????????
        MentorInfo mentorInfo = player.getMentorInfo();
        StaticMentor sMentor = StaticMentorDataMgr.getsMentorIdMap(mentor.getId()); // ??????????????????
        int exp = mentor.getExp();
        int count = exp / Constant.INT_HUNDRED;
        if (!CheckNull.isNull(sMentor) && count > 0) {
            StaticMentor nextMentor = StaticMentorDataMgr.getsMentorByTypeAndLv(mentor.getType(), mentor.getLv() + 1); // ??????????????????
            if (!CheckNull.isNull(nextMentor)) {
                for (Entry<Integer, Integer> en : nextMentor.getAttr().entrySet()) {
                    mentor.getExtAttr().put(en.getKey(), en.getValue() * count);
                }
            }

        }

        // ????????????????????????
        HashMap<Integer, Integer> sumAttr = new HashMap<>();
        for (Entry<Integer, Integer> en : sMentor.getAttrUp().entrySet()) {
            addAttrValue(sumAttr, en.getKey(), en.getValue());
        }
        for (Entry<Integer, Integer> en : mentor.getExtAttr().entrySet()) {
            addAttrValue(sumAttr, en.getKey(), en.getValue());
        }

        int fight = reCalcFight(sumAttr);
        if (fight != mentor.getFight()) {
            mentor.setFight(fight);
        }
    }
}
