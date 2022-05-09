package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.dataMgr.cross.StaticNewCrossDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.NewCrossConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.GlobalWarFire;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.Constant.AttrId;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.*;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.dressup.BaseDressUpEntity;
import com.gryphpoem.game.zw.resource.pojo.dressup.CastleSkinEntity;
import com.gryphpoem.game.zw.resource.pojo.dressup.TitleEntity;
import com.gryphpoem.game.zw.resource.pojo.hero.AwakenData;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.pojo.medal.RedMedal;
import com.gryphpoem.game.zw.resource.pojo.season.SeasonTalent;
import com.gryphpoem.game.zw.resource.pojo.totem.Totem;
import com.gryphpoem.game.zw.rpc.DubboRpcService;
import com.gryphpoem.game.zw.service.TaskService;
import com.gryphpoem.game.zw.service.TitleService;
import com.gryphpoem.game.zw.service.TreasureWareService;
import com.gryphpoem.game.zw.service.WorldScheduleService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author TanDonghai
 * @ClassName CalculateUtil.java
 * @Description 数值计算工具类
 * @date 创建时间：2017年3月29日 下午2:58:49
 */
public class CalculateUtil {

    /**
     * 计算返回将领的属性值
     *
     * @param staticHero 将领配置信息
     * @param attrId     要计算的属性id
     * @param wash       当前资质
     * @param lv         将领等级
     * @param lineAdd    将领兵力排数加成
     * @return
     */
    public static int calcHeroAttrById(StaticHero staticHero, int attrId, int wash, int lv, int lineAdd) {
        if (null == staticHero) {
            return 0;
        }
        // 攻击=攻击资质*（武将等级-1）*攻击系数+基础攻击
        // 攻击=基础值 + 洗髓后的资质 * （品质影响基数 + 资质影响系数 * 将领等级） + 属性成长值 * 将领等级
        double value = 0;
        value += staticHero.getBaseAttrById(attrId);
        value += wash * (staticHero.getAttrRadixById(attrId) + staticHero.getAttrRatioById(attrId) * lv)
                / Constant.TEN_THROUSAND;
        value += staticHero.getAttrGrowthById(attrId) * lv;

        if (attrId == Constant.AttrId.LEAD) {
            // 总兵力=兵力*排数
            value *= (staticHero.getLine() + lineAdd);
        }

        return (int) value;
    }

    public static int calcHeroAttrById(StaticHero staticHero, int attrId, int wash, int lv) {
        if (null == staticHero) {
            return 0;
        }
        float ratio = 0;// 系数
        if (attrId == Constant.AttrId.ATTACK) {
            ratio = WorldConstant.ATK_RATIO;
        } else if (attrId == Constant.AttrId.DEFEND) {
            ratio = WorldConstant.DEF_RATIO;
        } else if (attrId == Constant.AttrId.LEAD) {
            ratio = WorldConstant.ARMY_RATIO;
        }
        // 攻击=攻击资质*（武将等级）*系数+基础攻击
        double value = wash * lv * ratio + staticHero.getBaseAttrById(attrId);
        LogUtil.calculate("属性计算attrId=" + attrId + ",wash=" + wash + "系数=" + ratio + ",attr="
                + staticHero.getBaseAttrById(attrId) + ",value=" + value);
        return (int) value;
    }

    /**
     * 重新计算玩家所有将领属性
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
     * 重新计算上阵将领属性
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
     * 重新计算将领属性(只存将领基础属性，其他装备等属性参与战斗计算)
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
      LogUtil.calculate("重新计算将领属性，id为配置，跳过, heroId:", hero.getHeroId());
      return;
      }

      // 计算将领本身属性
      hero.getAttr()[HeroConstant.ATTR_ATTACK] = calcHeroAttrById(staticHero, Constant.AttrId.ATTACK,
      hero.getWash()[HeroConstant.ATTR_ATTACK], hero.getLevel());
      hero.getAttr()[HeroConstant.ATTR_DEFEND] = calcHeroAttrById(staticHero, Constant.AttrId.DEFEND,
      hero.getWash()[HeroConstant.ATTR_DEFEND], hero.getLevel());
      hero.getAttr()[HeroConstant.ATTR_LEAD] = calcHeroAttrById(staticHero, Constant.AttrId.LEAD,
      hero.getWash()[HeroConstant.ATTR_LEAD], hero.getLevel());

      // 计算装备加成属性
      Equip equip;
      StaticEquip staticEquip;
      for (int equipKeyId : hero.getEquip()) {
      if (equipKeyId > 0) {
      equip = player.equips.get(equipKeyId);
      if (null == equip) {
      LogUtil.calculate("重新计算玩家将领属性，未找到装备, equipKeyId:", equipKeyId);
      continue;
      }

      // 获取装备配置信息
      staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
      if (null == staticEquip) {
      LogUtil.calculate("重新计算玩家将领属性，未找到装备id配置信息, equipId:", equip.getEquipId());
      continue;
      }

      // 增加装备基础属性加成
      for (Entry<Integer, Integer> entry : staticEquip.getAttr().entrySet()) {
      hero.getAttr()[entry.getKey()] += entry.getValue();
      }

      // 技能属性 洗练
      for (int i = 0; i < equip.getAttrAndLv().size(); i++) {// 当前洗炼属性
      int attrId = equip.getAttrAndLv().get(i).getA();
      int level = equip.getAttrAndLv().get(i).getB();
      if (Arrays.binarySearch(Constant.BASE_ATTRS, attrId) >= 0) {
      StaticEquipExtra equipExtra = StaticPropDataMgr.getEuqipExtraByIdAndLv(attrId, level);
      if (equipExtra != null) {
      hero.getAttr()[attrId] += equipExtra.getAttrValue();
      }
      }
      }

      // for (int attrId : equip.getAttrId()) {// 当前洗炼属性
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

      // 计算军阶加成
      StaticPartyRanks ranks = StaticPartyDataMgr.getPartyRanks(player.lord.getRanks());
      if (null != ranks) {
      for (Entry<Integer, Integer> entry : ranks.getAttr().entrySet()) {
      hero.getAttr()[entry.getKey()] += entry.getValue();
      }
      }
      reCalcFight(player);
      }*/

    /**
     * 线上 1服 2服的系数
     */
    private static Map<Integer, Integer> FIGHT_K1;
    /**
     * 新的战斗力系数
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
     * 计算战斗力
     *
     * @param player
     */
    public static void reCalcFight(Player player) {
        // ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);
        // String environment = serverSetting.getEnvironment();
        // int serverID = serverSetting.getServerID();
        // Map<Integer, Integer> keyMap = null;
        // if ((serverID == 1 || serverID == 2) && "release".equals(environment)) {
        // // FIGHT_K1 的公式
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
                    for (int i = 1; i < hero.getAttr().length; i++) { // 基本攻防兵
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
                //英雄技能战力
                fight += reCalcHeroSkillFight(hero);
            }
        }
        long preFight = player.lord.getFight();
        player.lord.setFight(fight);
        if (preFight != player.lord.getFight()) {
            WorldScheduleService service = DataResource.ac.getBean(WorldScheduleService.class);
            service.updateScheduleGoal(player, ScheduleConstant.GOAL_COND_FIGHT, fight);
            // 战斗力发生改变
            EventBus.getDefault().post(new Events.FightChangeEvent(player));

            //貂蝉任务-玩家战力
            ActivityDiaoChanService.completeTask(player, ETask.PLAYER_POWER);
            TaskService.processTask(player, ETask.PLAYER_POWER);

            DubboRpcService dubboRpcService = DataResource.ac.getBean(DubboRpcService.class);
            dubboRpcService.updatePlayerLord2CrossPlayerServer(player);
        }
    }

    /**
     * 计算将领的属性
     *
     * @param player
     * @param hero
     * @param reCalcFight 是否重新计算战斗力
     * @return
     */
    private static Map<Integer, Integer> processAttr(Player player, Hero hero, boolean reCalcFight) {
        if (null == player || null == hero) {
            return null;
        }
        // 之前的兵力
        int oldArmyCnt = hero.getCount();

        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (null == staticHero) {
            LogUtil.error("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "重新计算将领属性，id为配置，跳过, heroId:",
                    hero.getHeroId());
            return null;
        }

        // 基础属性
        Map<Integer, Integer> attrMap = calcHeroAttr(player, hero, staticHero);

        // 重新计算战斗力
        if (reCalcFight && (hero.getPos() > 0 || hero.getCommandoPos() > 0)) {
            CalculateUtil.reCalcFight(player);
        }
        // 兵力属性改变时返还兵力
        if (hero.getAttr()[Constant.AttrId.LEAD] < oldArmyCnt) {
            // 兵属下降时才会执行,士兵回营
            returnArmy(player, hero);
        }
        return attrMap;
    }

    /**
     * 计算英雄属性，按照上阵英雄的方式计算属性；此方法不修改Hero的属性值，只返回属性Map
     *
     * @param player
     * @param hero
     * @return
     */
    public static Map<Integer, Integer> processHeroAttr(Player player, Hero hero) {
        if (Objects.isNull(player) || Objects.isNull(hero)) {
            LogUtil.error("计算英雄属性失败, Player or Hero is NULL");
            return new HashMap<>();
        }
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (Objects.isNull(staticHero)) {
            LogUtil.error("计算英雄属性失败, StaticHero is NULL");
            return new HashMap<>();
        }
        Map<Integer, Integer> attrMap = calcHeroAttr(player, hero, staticHero);
        //hero fight value
        int fightVal = reCalcFight(attrMap);
        //英雄技能战力
        fightVal += reCalcHeroSkillFight(hero);
        hero.setFightVal(fightVal);

        return attrMap;
    }

    private static void addTotemEffect(Player player,Hero hero,Map<Integer, Integer> attrMap){
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        HashMap<Integer, Integer> tempMap = new HashMap<>();
        //图腾基础属性
        for (int totemKey : hero.getTotem()) {
            if(totemKey <= 0) continue;
            Totem totem = player.getTotemData().getTotem(totemKey);
            if(Objects.isNull(totem)) {
                LogUtil.error("计算将领图腾属性,图腾不存在,totemKey=",totemKey);
                continue;
            }
            StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totem.getTotemId());
            StaticTotemUp staticTotemUp1 = StaticTotemDataMgr.getStaticTotemUp(1,staticTotem.getQuality(),totem.getStrengthen());
            StaticTotemUp staticTotemUp2 = StaticTotemDataMgr.getStaticTotemUp(2,staticTotem.getQuality(),totem.getResonate());
            if(Objects.isNull(staticTotem) || Objects.isNull(staticTotemUp1) || Objects.isNull(staticTotemUp2)){
                LogUtil.error("计算将领图腾属性,配置不存在",totem,staticTotem,staticTotemUp1,staticTotemUp2);
                continue;
            }
            Optional.ofNullable(staticTotemUp1.getAttrByIdx(staticTotem.getPlace())).ifPresent(map -> map.entrySet().forEach(entry -> tempMap.merge(entry.getKey(),entry.getValue(),Integer::sum)));
            Optional.ofNullable(staticTotemUp2.getAttrByIdx(staticTotem.getPlace())).ifPresent(map -> map.entrySet().forEach(entry -> tempMap.merge(entry.getKey(),entry.getValue(),Integer::sum)));
        }
        reCalcAttrFight(player,Constant.ShowFightId.TOTEM,hero,attrMap,tempMap);
    }

    private static void addTotemLinkEffect(Player player,Hero hero, Map<Integer, Integer> attrMutMap){
        //图腾套装属性
        StaticTotemLink staticTotemLink = null;
        for (StaticTotemLink link : StaticTotemDataMgr.getStaticTotemLinkList()) {
            boolean isOut = false,isFit = true;

            for(int i=1;i<hero.getTotem().length;i++){
                Totem totem = player.getTotemData().getTotem(hero.getTotemKey(i));
                if(Objects.isNull(totem)){
                    isOut = true;
                    break;
                }
                StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totem.getTotemId());
                if(Objects.isNull(staticTotem)){
                    isOut = true;
                    break;
                }
                StaticTotemUp staticTotemUp = StaticTotemDataMgr.getStaticTotemUp(1,staticTotem.getQuality(),totem.getStrengthen());
                if(Objects.isNull(staticTotemUp)){
                    isOut = true;
                    break;
                }
                if(link.getQuality() > staticTotem.getQuality() || totem.getStrengthen() < link.getLv()){
                    isFit = false;
                    break;
                }
            }
            if(isOut){
                break;
            }
            if(isFit){
                staticTotemLink = link;
                break;
            }
        }
        if(Objects.nonNull(staticTotemLink)){
//            staticTotemLink.getAttr().entrySet().forEach(entry -> tempMap.merge(entry.getKey(),entry.getValue(),Integer::sum));
            staticTotemLink.getAttr().entrySet().forEach(entry -> addAttrValue(attrMutMap, entry.getKey(), entry.getValue()));
        }
    }

    public static Map<Integer, Integer> calcHeroAttr(Player player, Hero hero, StaticHero staticHero) {
        Map<Integer, Integer> attrMap = new HashMap<>();
        // 将领属性加成
        addHeroEffect(player, hero, staticHero, attrMap);//处理出战沙盘的，出战沙盘的不处理教官加成 处理特工好感度加成
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Hero attrMap=" + attrMap);
        // 装备属性加成
        addEquipEffect(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Equip attrMap=" + attrMap);
        //阵法图腾属性加成
        addTotemEffect(player,hero,attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "阵法图腾加成 attrMap=" + attrMap);
        // 国器加成
        addSuperEquipEffect(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "国器加成 attrMap=" + attrMap);
        // 计算军阶加成
        addPartyRankEffect(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "军阶加成 attrMap=" + attrMap);
        // 特工加成 行宫
        addFemaleAgentEffect(player, hero, attrMap);//出战沙盘的武将处理特工加成
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "femaleAgent attrMap=" + attrMap);
        // 宝石加成
        addStoneEffect(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "stone attrMap=" + attrMap);
        // 勋章属性加成
        addMedalEffect(player, hero, attrMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Medal attrMap=" + attrMap);
        // 战机加成
        addPlaneEffect(player, hero, attrMap);//这里有教官的加成，出战沙盘的武将不处理教官加成
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "plane attrMap=" + attrMap);
        // 玩家皮肤对上阵 hero加成
        addCastleSkinEffect(player, hero, attrMap);//出战沙盘的武将处理皮肤加成
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "CastleSkin attrMap=" + attrMap);

        // 其他属性加成, 例如: 科技加成
        addOtherEffect(player, hero, attrMap, staticHero);//出战沙盘的武将不处理跨服加成

        //宝具加成
        addTreasureWare(player, hero, attrMap);
        // 赛季天赋
        addSeasonTalent(player, hero, attrMap);

        // 方便一个人打一个城
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
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "最终值 attrMap=" + attrMap);

        // 额外属性计算
        hero.getExtAttrs().clear();
        for (int attr : Constant.EXT_ATTRS) {
            Integer attrVal = attrMap.get(attr);
            if (attrVal == null) {
                hero.getExtAttrs().put(attr, 0);
            } else {
                hero.getExtAttrs().put(attr, attrVal);
            }
        }
        return attrMap;
    }

    public static int calcHeroesFightVal(Player player, List<Integer> heroIds) {
        int val = 0;
        LogUtil.calculate("沙盘演武, 计算英雄属性开始 roleId=" + player.roleId);
        for (int heroId : heroIds) {
            Hero hero = player.heros.get(heroId);
            if (hero == null) continue;
            Map<Integer, Integer> attrMap = processHeroAttr(player, hero);
            val += hero.getFightVal();
        }
        LogUtil.calculate("沙盘演武, 计算英雄属性结束, roleId=" + player.roleId);
        return val;
    }

    /**
     * 其他属性加成, 例如: 科技加成
     *
     * @param player
     * @param hero
     * @param attrMap
     * @param staticHero 将领配置
     */
    private static void addOtherEffect(Player player, Hero hero, Map<Integer, Integer> attrMap, StaticHero staticHero) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        int type = staticHero.getType();
        Map<Integer, Integer> tempMap = new HashMap<>();
        // 战车/坦克/火箭/ 攻击科技加成
        addTechEffect(player, tempMap, type);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Tech tempMap=" + tempMap);
        // 战车/坦克/火箭/ 强化科技加成
        addIntensifyTechEffect(player, tempMap, type);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "intensify Tech tempMap=" + tempMap);
        // 跨服buff
        addCrossBuffEffect(player, hero, tempMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), " 跨服buff tmpMap=" + tempMap);
        // 重新计算模块战斗力
        reCalcAttrFight(player, Constant.ShowFightId.OTHER, hero, attrMap, tempMap);
    }

    /**
     * 军衔属性加成
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
     * 将领属性加成, 万分比直接加在将领三围上
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
        Map<Integer, Integer> tempMap = new HashMap<>();// 基础属性
        Map<Integer, Integer> attrMutMap = new HashMap<>();// 万分比属性
        // 基础属性
        tempMap.put(HeroConstant.ATTR_ATTACK, CalculateUtil.calcHeroAttrById(staticHero, Constant.AttrId.ATTACK,
                hero.getWash()[HeroConstant.ATTR_ATTACK], hero.getLevel()));
        tempMap.put(HeroConstant.ATTR_DEFEND, CalculateUtil.calcHeroAttrById(staticHero, Constant.AttrId.DEFEND,
                hero.getWash()[HeroConstant.ATTR_DEFEND], hero.getLevel()));
        tempMap.put(HeroConstant.ATTR_LEAD, CalculateUtil.calcHeroAttrById(staticHero, Constant.AttrId.LEAD,
                hero.getWash()[HeroConstant.ATTR_LEAD], hero.getLevel()));

        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "base tempMap=" + tempMap);

        // 将领授勋加成
        addDecoratedEffect(player, hero, tempMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "decorated attrMap=" + tempMap);
        // 将领觉醒加成
        addAwakenEffect(staticHero, hero, tempMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "awaken attrMap=" + tempMap);
        // buff加成 具体值, 注意是具体值, attrMap和attrMutMap是不同的
        addEffectSpecificVal(player, hero, tempMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "buff具体值加成 tempMap=" + tempMap);
        if (hero.getStatus() != HeroConstant.HERO_STATUS_COMMANDO) {
            // 教官功能加成
            addMentorEffect(player, hero, tempMap);
            LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Mentor tempMap=" + tempMap);
        }
        // 英雄神职属性加成
        addHeroCgy(hero, tempMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Hero Clergy attrMap=" + tempMap);
        // buff加成 万分比, 注意这里是加万分
        addEffectVal(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "普通buff(万分比值) attrMutMap=" + attrMutMap);
        // 匪军叛乱buff
        addRebelBuffEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(),
                "匪军叛乱buff(万分比值) attrMutMap=" + attrMutMap);
        // 进阶宝石万分比加成
        addStoneMultEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "进阶的宝石万分比加成 attrMutMap=" + attrMutMap);
        // 特工好感度万分比加成
        addHeroFemaleAgentEffect(player, hero, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "特工的好感度加成 attrMutMap=" + attrMutMap);
        // 战火燎原万分比加成
        addWarFireBuffEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), ",战火燎原万分比加成 attrMutMap=" + attrMutMap);
        //图腾万分比加成
        addTotemLinkEffect(player,hero,attrMutMap);
        LogUtil.calculate("roleId:",player.roleId,",heroId:",hero.getHeroId(),",图腾套装属性万分比加成 attrMutMap=" + attrMutMap);
        // 跨服战火燎原万分比加成
        addCrossWarFireBuffEffect(player, attrMutMap);
        LogUtil.calculate("roleId:",player.roleId,",heroId:",hero.getHeroId(),",跨服战火燎原万分比加成 attrMutMap=" + attrMutMap);

        // 最终值 = 基础 * 万分比
        processFinalAttr(tempMap, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "buff万分比最终值 tempMap=" + tempMap);

        // 重新计算模块战斗力
        reCalcAttrFight(player, Constant.ShowFightId.HERO, hero, attrMap, tempMap);
    }


    /**
     * 战火燎原给将领加属性, 这里是万分比
     *
     * @param player     玩家
     * @param attrMutMap 万分比属性
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
                                // 活动结束后, 会将玩家迁回到原来的地图, 所以这里不用判断功能的开启状态
                                if (cMap.getPlayerMap().containsKey(player.roleId)) {
                                    // 在新地图上的城池buff加成
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
                                    // 购买的buff加成
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
                                                                //赛季天赋
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

        //活动开启期间buff生效
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
                    //赛季天赋
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
     * 天赋优化处理
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    private static void addSeasonTalent(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (hero.getStatus() != HeroConstant.HERO_STATUS_BATTLE) return;//只有出战英雄才有赛季天赋属性加成
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);
        if (!seasonTalentService.checkTalentBuffOpen(player)) {
            return;
        }
//         //赛季未开启
//        if (seasonService.getSeasonState() == SeasonConst.STATE_OPEN) return;
//        int seasonPlanId = seasonService.getCurSeasonPlanId();
//        //天赋未开启
//        StaticSeasonTalentPlan sTalentPlan = StaticIniDataMgr.getOpenStaticSeasonTalentPlan(seasonPlanId);
//        if (Objects.isNull(sTalentPlan)) return;
//        //赛季ID异常
//        int curSeasonId = seasonService.getCurrSeason();
//        if (curSeasonId < 1) return;
//        //玩家天赋未开启, 赛季ID异常, 未学习天赋
//        if (!talent.isOpenTalent() || talent.getSeasonId() != curSeasonId || CheckNull.isEmpty(talent.getLearns())) {
//            return;
//        }
        SeasonTalent talent = player.getPlayerSeasonData().getSeasonTalent();
        Map<Integer, Integer> tempMap = null;
        Map<Integer, StaticSeasonTalent> talentMap = StaticIniDataMgr.getSeasonTalentMap();
        for (Integer tid : talent.getLearns()) {
            StaticSeasonTalent sTalent = talentMap.get(tid);
            if (Objects.nonNull(sTalent)) {
                tempMap = Optional.ofNullable(tempMap).orElse(new HashMap<>());
                if (sTalent.getEffect() == SeasonConst.TALENT_EFFECT_101) {//属性加成
                    for (List<Integer> params : sTalent.getEffectParam()) {
                        tempMap.merge(params.get(0), params.get(1), Integer::sum);
                    }
                }

                if (sTalent.getEffect() == SeasonConst.TALENT_EFFECT_201//增伤
                        || sTalent.getEffect() == SeasonConst.TALENT_EFFECT_202) {//减伤
                    tempMap.merge(AttrId.DMG_INC, sTalent.getEffectParam().get(0).get(0), Integer::sum);
                }

                //赛季天赋优化
                //某属性的百分比加成到某属性上 list [被加成的属性类型, 从该属性类型加成数值, 百分比数值]
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

        //重新计算showFight
        reCalcFinalAttrFight(player, Constant.ShowFightId.HERO, hero, attrMap, tempMap);
    }

    /**
     * 宝具战斗力加成
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
            // 获取将领对应类型的兵力
            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            if (CheckNull.isNull(staticHero)) {
                return;
            }

            List<Turple<Integer, Integer>> basicAttr = DataResource.getBean(TreasureWareService.class).getBasicAttr(player, hero);
            if (ObjectUtils.isEmpty(basicAttr)) {
                return;
            }

            //基本属性
            for (Turple<Integer, Integer> attr : basicAttr) {
                tempMap = Optional.ofNullable(tempMap).orElse(new HashMap<>());
                tempMap.merge(attr.getA(), attr.getB(), Integer::sum);
            }

            //专属属性
            Object specialAttr = DataResource.getBean(TreasureWareService.class).getTreasureWareBuff(player, hero, TreasureWareConst.SpecialType.ADD_ATTR, 0);
            if (!ObjectUtils.isEmpty(specialAttr) && specialAttr instanceof List) {
                List<List<Integer>> buffEffect = (List<List<Integer>>) specialAttr;
                for (List<Integer> list : buffEffect) {
                    tempMap = Optional.ofNullable(tempMap).orElse(new HashMap<>());
                    tempMap.merge(list.get(0), list.get(1), Integer::sum);
                }
            }
        } finally {
            // 重新计算模块战斗力
            reCalcAttrFight(player, Constant.ShowFightId.TREASURE_WARE, hero, attrMap, tempMap);
        }
    }

    /**
     * 英雄神职属性加成
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
     * 皮肤加成
     * 目前是皮肤获得时就加属性，重新计算属性就是计算所有皮肤的加成，不是计算使用中的皮肤加成
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    private static void addCastleSkinEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        Map<Integer, Integer> tempMap = new HashMap<>();// 基础属性
        int pos = hero.getPos();
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (null == staticHero) {
            LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "重新计算将领属性，id为配置，跳过, heroId:",
                    hero.getHeroId());
            return;
        }
        if (hero.getStatus() != HeroConstant.HERO_STATUS_BATTLE) {//出战沙盘时计算皮肤加成
            return;
        }
        // 将领上阵 1-4 号位 增加将领皮肤属性加成
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
                                    // 兵种类型不符合
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
                                    // 兵种类型不符合
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
        addTitleEffect(player,hero,attrMap,tempMap);
        // 重新计算模块战斗力
        reCalcAttrFight(player, Constant.ShowFightId.CASTLE_SKIN, hero, attrMap, tempMap);
    }

    /**
     * 计算称号属性加成
     * @param player
     * @param hero
     * @param attrMap
     * @param tempMap
     */
    private static void addTitleEffect(Player player, Hero hero, Map<Integer, Integer> attrMap,Map<Integer, Integer> tempMap) {
        DressUpDataManager dressUpDataManager = DataResource.ac.getBean(DressUpDataManager.class);
        TitleService titleService = DataResource.ac.getBean(TitleService.class);
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        if (hero.getStatus() != HeroConstant.HERO_STATUS_BATTLE) {//只在上阵英雄增加属性
            return;
        }
        try {
            StaticLordDataMgr.getTitleMap().values().forEach(title -> {
                Map<Integer, BaseDressUpEntity> dressUpByType = dressUpDataManager.getDressUpByType(player, AwardType.TITLE);
                if (null == dressUpByType) {
                    return;
                }
                //如果玩家装扮上没有，加入进去一个未解锁的称号。
                if(null==dressUpByType.get(title.getId())){
                    TitleEntity titleEntity=new TitleEntity(title.getId(),false);
                    dressUpByType.put(title.getId(),titleEntity);
                }
                TitleEntity titleEntity = (TitleEntity) dressUpByType.get(title.getId());
                if (null != title.getTaskId() && title.getTaskId() > 0) {
                    if (titleService.checkFinishTaskUnlock(player, titleEntity, ETask.getByType(title.getTaskId()), title)) {
                        if(null!=title.getAttr()&&!title.getAttr().isEmpty()){
                            title.getAttr().entrySet().forEach(add -> {
                                addAttrValue(tempMap, add.getKey(), add.getValue());
                            });
                        }
                    }
                } else {
                    //没有任务,需要解锁过
                    if (titleEntity.isPermanentHas() || titleEntity.getDuration() > 0) {
                        if(!title.getAttr().isEmpty()){
                            title.getAttr().entrySet().forEach(add -> {
                                addAttrValue(tempMap, add.getKey(), add.getValue());
                            });
                        }
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.error(e, "计算称号加成属性出错玩家,id:" + player.getLordId() + ",hero:", hero);
        }
    }

    /**
     * 教官功能加成
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
                    for (Entry<Integer, Integer> en : sMentor.getAttrUp().entrySet()) { // 教官基础属性
                        addAttrValue(tempMap, en.getKey(), en.getValue());
                    }
                }
                LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Mentor Base tempMap=" + tempMap);
                for (Entry<Integer, Integer> en : mentor.getExtAttr().entrySet()) { // 教官的附加属性
                    addAttrValue(tempMap, en.getKey(), en.getValue());
                }
                LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "Mentor Ext tempMap=" + tempMap);
                int equipId = mentor.getEquips()[pos];
                if (equipId > 0) {
                    MentorEquip equip = mentorInfo.getEquipMap().get(equipId);
                    if (!CheckNull.isNull(equip)) {
                        for (Entry<Integer, Integer> en : equip.getAttr().entrySet()) { // 装备的基础属性
                            addAttrValue(tempMap, en.getKey(), en.getValue());
                        }
                        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(),
                                "Mentor Equip Base tempMap=" + tempMap);
                        for (Entry<Integer, Integer> en : equip.getExtAttr().entrySet()) { // 装备的扩展属性
                            addAttrValue(tempMap, en.getKey(), en.getValue());
                        }
                        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(),
                                "Mentor Equip Ext tempMap=" + tempMap);
                    }
                }
                if (mentor.getType() == MentorConstant.MENTOR_TYPE_2) { // 空军教官不做处理
                } else if (mentor.getType() == MentorConstant.MENTOR_TYPE_3) { // 装甲师
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
     * 重新计算模块战斗力
     *
     * @param player
     * @param fightId 显示战力id
     * @param hero    将领
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
                LogUtil.error("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "重新计算将领属性，id为配置，跳过, heroId:",
                        hero.getHeroId());
                return;
            }
            // 重新计算总兵力
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
     * 计算最终面板属性，更新showFight
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
                LogUtil.error("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "重新计算将领属性，id为配置，跳过, heroId:",
                        hero.getHeroId());
                return;
            }
            // 重新计算总兵力
            tempMap.put(Constant.AttrId.LEAD,
                    getFinalLead(player, hero, staticHero.getLine(), tempMap.getOrDefault(Constant.AttrId.LEAD, 0)));
            if (!CheckNull.isNull(hero) && (hero.getPos() > 0 || hero.getCommandoPos() > 0)) {
                Optional.ofNullable(hero.getShowFight()).
                        ifPresent(showFight -> showFight.merge(fightId, reCalcFight(tempMap), Integer::sum));
            }
        }
    }

    /**
     * 计算战斗力
     *
     * @param attrMap 属性值
     * @return 战斗力
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
     * 计算英雄技能战力
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
        //英雄的战力加上技能战力Constant.ShowFightId.HERO
        hero.getShowFight().merge(Constant.ShowFightId.HERO, skillFightVal, Integer::sum);
        return skillFightVal;
    }

    /**
     * 脱装备,装备洗练,将领洗髓 掉兵属性时,进行士兵回营操作
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
            // 武将和采集将领才会
            if (hero.isOnAcq() || hero.isOnBattle() || hero.isCommando()) {
                // 士兵回营
                rewardDataManager.modifyArmyResource(player, staticHero.getType(), subArmy, 0, AwardFrom.HERO_DOWN);
                LogUtil.calculate("roleId:", player.roleId,
                        " 士兵回营=" + subArmy + ",heroCnt=" + hero.getCount() + ",hero=" + hero);
                ChangeInfo change = ChangeInfo.newIns();
                change.addChangeType(AwardType.ARMY, staticHero.getType());
                // 向客户端同步玩家资源数据
                rewardDataManager.syncRoleResChanged(player, change);

                //记录玩家兵力变化信息
                LogLordHelper.filterHeroArm(AwardFrom.CALCULATE_CHANGE_FIGHT_ACTION, player.account, player.lord, hero.getHeroId(), hero.getCount(), -subArmy,
                        Constant.ACTION_SUB, staticHero.getType(), hero.getQuality());
            }
        }
    }

    /**
     * 计算属性，返回属性ID，值
     *
     * @param player
     * @param hero
     * @return
     */
    public static Map<Integer, Integer> processAttr(Player player, Hero hero) {
        return processAttr(player, hero, true);
    }

    /**
     * 计算总兵力
     *
     * @param player
     * @param hero
     * @param baseLine
     */
    public static int getFinalLead(Player player, Hero hero, int baseLine, int lead) {
        // 科技加成
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
        // 点兵将领加成
        int leadLine = 0;
        if (player.cabinet != null) {
            int planId = player.cabinet.getEffectPlanId();
            StaticCabinetPlan cabinetPlan = StaticBuildingDataMgr.getCabinetPlanById(planId);
            if (cabinetPlan != null && !CheckNull.isEmpty(cabinetPlan.getEffect())
                    && cabinetPlan.getEffect().size() == 2) {
                if (hero.isOnAcq() && cabinetPlan.getEffect().get(1) > 0) {// 采集
                    leadLine += cabinetPlan.getEffect().get(1);
                } else if (hero.isOnBattle() && cabinetPlan.getEffect().get(0) > 0) {// 上阵主将
                    leadLine += cabinetPlan.getEffect().get(0);
                }
            }
        }

        //城防将领兵排数科技
        if (hero.isOnWall()) {
            //高级禁卫军加成
            TechDataManager techDataManager = DataResource.ac.getBean(TechDataManager.class);
            if (player.tech.getTechLv().containsKey(TechConstant.TYPE_32)) {
                leadLine += techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_32);
            } else {//中级禁卫军加成
                leadLine += techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_31);
            }
        }

        lead *= (1 + leadLine * 1.0f / 4);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(),
                ",getFinalLead lead=" + lead + ",leadLine=" + leadLine);
        return lead;
    }

    /**
     * 国器属性加成
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
        // 重新计算模块战斗力
        reCalcAttrFight(player, Constant.ShowFightId.SUPER_EQUIP, hero, attrMap, tempMap);
    }

    /**
     * 宝石属性加成(具体值)
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
     * 宝石万分比加成(万分比)
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
     * 特工好感度给将领加属性, 这里是万分比
     *
     * @param player     玩家对象
     * @param hero       将领
     * @param attrMutMap 万分比属性
     */
    private static void addHeroFemaleAgentEffect(Player player, Hero hero, Map<Integer, Integer> attrMutMap) {
        if (CheckNull.isNull(attrMutMap)) {
            return;
        }
        Cia cia = player.getCia();
        if (cia != null && (hero.getPos() > 0 || hero.getCommandoPos() > 0)) {// 上阵将领起作用
            for (FemaleAgent fa : cia.getFemaleAngets().values()) {
                // 好感度加成
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
     * 匪军叛乱buff加成
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
                        LogUtil.error("计算战斗力时,匪军叛乱的buff配置未找到 roleId:", player.roleId, ", buffType:", buff.getType(),
                                ", buffLv:", buff.getLv());
                    }
                }
            }
        }
    }

    /**
     * 跨服buff加成
     *
     * @param player
     * @param hero
     * @param tempMap
     */
    private static void addCrossBuffEffect(Player player, Hero hero, Map<Integer, Integer> tempMap) {
        if (hero.getStatus() == HeroConstant.HERO_STATUS_BATTLE) { // 上阵将领才对跨服有效
            CrossPersonalData crossPersonalData = player.getAndCreateCrossPersonalData();
            int now = TimeHelper.getCurrentSecond();
            for (CrossBuff buff : crossPersonalData.getBuffs().values()) {
                if (now >= buff.getStartTime() && now < buff.getEndTime()) {
                    StaticCrossBuff sBuff = StaticCrossDataMgr.getBuffByTypeLv(buff.getType(), buff.getLv());
                    if (sBuff != null) {
                        addAttrValue(tempMap, sBuff.getType(), sBuff.getBuffVal());
                    } else {
                        LogUtil.error("计算战斗力时,跨服buff配置未找到 roleId:", player.roleId, ", buffType:", buff.getType(),
                                ", buffLv:", buff.getLv());
                    }
                }
            }
        }
    }

    /**
     * 名城Buff加成
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    @Deprecated
    private static void addCityBuffEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        // 上阵将领加成
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
     * 计算战机属性值
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
        // 属性系数
        int planeRadio = PlaneConstant.getPlaneRadioByAttrId(attrId);
        if (planeRadio >= 0) {
            ratio = (float) (planeRadio / Constant.TEN_THROUSAND);
        }
        // 基础资质
        int baseRatio = sPlaneUp.getBaseRatioById(attrId);
        // 基础攻击
        int baseAttr = sPlaneInit.getBaseAttrById(attrId);
        // 攻击= 资质 *（战机等级 - 1）* 系数 + 基础攻击
        double value = baseRatio * (lv - 1) * ratio + baseAttr;
        LogUtil.calculate("属性计算, attrId=" + attrId + ", 基础资质=" + baseRatio + ", 等级=" + lv + ", 系数=" + ratio + ", 基础属性="
                + baseAttr + ", value=" + value);
        return (int) value;
    }

    /**
     * 战机加成
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
                                "战机替换的时候, 没有找到战机的配置, planeId:", planeId);
                    }
                    // 基础属性
                    for (int attr : Constant.ATTRS) {
                        addAttrValue(tempMap, attr,
                                calcPlaneAttrById(sPlaneInit, sPlaneUpgrade, attr, plane.getLevel()));
                    }
                } catch (MwException e) {
                    LogUtil.error("", e);
                }
            });
        }
        // 空军教官的技能战斗力加到战机模块
        MentorInfo mentorInfo = player.getMentorInfo();
        Mentor mentor = mentorInfo.getMentors().values().stream()
                .filter(m -> m.getType() == MentorConstant.MENTOR_TYPE_2).findFirst().orElse(null);
        if (!CheckNull.isNull(mentor)) {
            // 计算教官技能战斗力
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
        // 重新计算模块战斗力
        reCalcAttrFight(player, Constant.ShowFightId.PLANE, hero, attrMap, tempMap);
    }

    /**
     * 将领授勋加成
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
     * 将领觉醒加成
     *
     * @param sHero   将领配置
     * @param hero    将领
     * @param attrMap 加成
     */
    private static void addAwakenEffect(StaticHero sHero, Hero hero, Map<Integer, Integer> attrMap) {
        if (hero.getDecorated() > 0) {
            AwakenData awaken = hero.getAwaken();
            // 激活了
            if (awaken.isActivate()) {
                for (Entry<Integer, Integer> en : sHero.getActivateAttr().entrySet()) {
                    addAttrValue(attrMap, en.getKey(), en.getValue());
                }
                List<StaticHeroEvolve> heroEvolve = StaticHeroDataMgr.getHeroEvolve(sHero.getEvolveGroup());
                if (!CheckNull.isEmpty(heroEvolve) && !CheckNull.isEmpty(awaken.getEvolutionGene())) {
                    // 目前进化到哪个部位
                    int lastPart = awaken.lastPart();
                    if (lastPart == 0) {
                        return;
                    }
                    // 哪些部位
                    heroEvolve.stream().filter(he -> he.getPart() >= HeroConstant.AWAKEN_PART_MIN && he.getPart() <= lastPart).forEach(she -> {
                        for (Entry<Integer, Integer> en : she.getAttr().entrySet()) {
                            addAttrValue(attrMap, en.getKey(), en.getValue());
                        }
                    });
                }
            }
        }
    }

    /**
     * 特工加成
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    private static void addFemaleAgentEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        Map<Integer, Integer> tempMap = new HashMap<>();// 基础属性
        Cia cia = player.getCia();
        if (cia != null && (hero.getPos() > 0 || hero.getCommandoPos() > 0)) {// 上阵将领起作用
            for (FemaleAgent fa : cia.getFemaleAngets().values()) {
                // 星级加成
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
     * 战车/坦克/火箭 攻击科技加成
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
     * 战车/坦克/火箭 强化科技加成
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
            addAttrValue(attrMap, HeroConstant.ATTR_ATTACK, list.get(1));// 加攻击
            addAttrValue(attrMap, HeroConstant.ATTR_DEFEND, list.get(2));// 加防御
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
     * 基础属性*加成万分比
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
                case Constant.AttrId.LEAD_MUT:// 兵力百分比 ,在优先于兵排之前计算
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
     * 获取万分比加成
     *
     * @param player 玩家
     * @param hero   将领
     * @return 万分比加成
     */
    public static Map<Integer, Integer> getAttrMutMap(Player player, Hero hero) {
        Map<Integer, Integer> attrMutMap = new HashMap<>();// 万分比属性
        // buff加成 万分比, 注意这里是加万分
        addEffectVal(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "普通buff(万分比值) attrMutMap=" + attrMutMap);
        // 匪军叛乱buff
        addRebelBuffEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "匪军叛乱buff(万分比值) attrMutMap=" + attrMutMap);
        // 进阶宝石万分比加成
        addStoneMultEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "进阶的宝石万分比加成 attrMutMap=" + attrMutMap);
        // 特工好感度万分比加成
        addHeroFemaleAgentEffect(player, hero, attrMutMap);//出战沙盘的计算加成
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), "特工的好感度加成 attrMutMap=" + attrMutMap);
        // 战火燎原万分比加成
        addWarFireBuffEffect(player, attrMutMap);
        LogUtil.calculate("roleId:", player.roleId, ",heroId:", hero.getHeroId(), ",战火燎原万分比加成 attrMutMap=" + attrMutMap);
        return attrMutMap;
    }

    /**
     * 装备属性加成
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
                    LogUtil.error("战斗逻辑，未找到玩家的装备, equipKeyId:", equipKeyId);
                    continue;
                }

                staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
                if (null == staticEquip) {
                    LogUtil.error("战斗逻辑，装备id未配置, equipId:", equip.getEquipId());
                    continue;
                }

                // 戒指镶嵌的宝石的加成基数
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

                // 兵种不对应
                if (staticEquip.getArmType() != 0 && staticEquip.getArmType() != hero.getType()) {
                    continue;
                }

                // 装备基础属性加成
                for (Entry<Integer, Integer> entry : staticEquip.getAttr().entrySet()) {
                    addAttrValue(tempMap, entry.getKey(), entry.getValue());
                }

                // 装备洗炼属性加成
                List<Turple<Integer, Integer>> attrLvs = equip.getAttrAndLv();
                for (int j = 0; j < attrLvs.size(); j++) {
                    Turple<Integer, Integer> al = attrLvs.get(j);
                    StaticEquipExtra equipExtra = StaticPropDataMgr.getEuqipExtraByIdAndLv(al.getA(), al.getB(),
                            staticEquip.getEquipPart());
                    if (equipExtra != null) {
                        // 宝石对洗练属性的加成
                        int attrValue = val > 0
                                ? (int) (equipExtra.getAttrValue() * (1 + (val / Constant.TEN_THROUSAND)))
                                : equipExtra.getAttrValue();
                        addAttrValue(tempMap, al.getA(), attrValue);
                    }
                }
                // 戒指强化属性
                if (equip instanceof Ring) {
                    StaticRingStrengthen ringConf = StaticPropDataMgr.getRingConfByLv(staticEquip.getEquipId(),
                            ring.getLv());
                    if (ringConf == null) {
                        LogUtil.error("装备属性，装备id未配置, equipId:", equip.getEquipId(), ", lv:", ring.getLv());
                        continue;
                    }
                    // 戒指的强化次数
                    int count = ring.getCount();
                    // 基础加成
                    List<List<Integer>> baseAttr = ringConf.getAttr();
                    if (!CheckNull.isEmpty(baseAttr)) {
                        for (List<Integer> list : baseAttr) {
                            addAttrValue(tempMap, list.get(0), list.get(1));
                        }
                    }
                    // 额外加成 = 强化次数 / 需要强化的次数 * 加成的属性
                    List<List<Integer>> exAttr = ringConf.getExAttr();
                    if (!CheckNull.isEmpty(exAttr)) {
                        for (List<Integer> list : exAttr) {
                            int baseRadio = list.get(0);
                            int cnt = count / baseRadio;
                            cnt = cnt >= 1 ? cnt : 1;
                            addAttrValue(tempMap, list.get(1), list.get(2) * cnt);
                        }
                    }
                    // 2019-04-11 LYJ说不计算强化次数 资质加成(方便服务器计算, 直接给的具体属性, 客户端用的三围) = 强化次数 / 需要强化的次数 * 加成的属性
                    List<List<Integer>> upAttr = ringConf.getUpAttr();
                    if (!CheckNull.isEmpty(upAttr)) {
                        for (List<Integer> list : upAttr) {
                            addAttrValue(tempMap, list.get(0), list.get(1));
                        }
                    }
                }
            }
        }
        // 重新计算模块战斗力
        reCalcAttrFight(player, Constant.ShowFightId.EQUIP, hero, attrMap, tempMap);
    }

    /**
     * 勋章-属性加成
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    public static void addMedalEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        if (CheckNull.isNull(attrMap)) {
            return;
        }
        // 获取将领对应类型的兵力
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (CheckNull.isNull(staticHero)) {
            return;
        }
        Map<Integer, Integer> tempMap = new HashMap<>();

        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);

        // 根据将领id 获取穿戴的勋章 没有穿戴返回null
        List<Medal> medals = medalDataManager.getHeroMedalByHeroId(player, hero.getHeroId());
        if (!CheckNull.isEmpty(medals)) {
            for (Medal medal : medals) {
                StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                if (CheckNull.isNull(sHero)) {
                    return;
                }

                // 勋章基础属性加成
                addAttrValue(tempMap, medal.getMedalAttr().getA(), medal.getMedalAttr().getB());

                RedMedal redMedal = null;
                if (medal instanceof RedMedal) {
                    redMedal = (RedMedal) medal;
                }

                // 判断勋章是否激活光环
                if (medal.hasAuraSkill()) {
                    // 红色勋章不需要判断激活
                    boolean canUseSkill = redMedal == null || redMedal.isAuraUnLock();
                    if (canUseSkill) {
                        StaticMedalAuraSkill staticMedalAuraSkill = StaticMedalDataMgr.getAuraSkillById(medal.getAuraSkillId());
                        // TODO: 2020/9/23 唐寅说光环的基础属性也需要加上兵种类型判断
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

                // 判断勋章是否激活特技
                if (medal.getSpecialSkillId() != null && medal.getSpecialSkillId() > 0) {
                    StaticMedalSpecialSkill staticMedalSpecialSkill = StaticMedalDataMgr
                            .getSpecialSkillById(medal.getSpecialSkillId());
                    if (staticMedalSpecialSkill != null) {
                        addAttrValue(tempMap, staticMedalSpecialSkill.getAttrId(),
                                staticMedalSpecialSkill.getAttrEffect());
                    }
                    // 勋章特技增加属性
                    // TODO: 2019/11/5 这里总是频繁改动需求  MedalConst.BLITZ_CURIOUS_SOLDIER MedalConst.IRON_BASTIONS
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

                // 判断勋章是否激活了普通技能
                if (!CheckNull.isEmpty(medal.getGeneralSkillId())) {
                    for (int genaralSkillId : medal.getGeneralSkillId()) {
                        StaticMedalGeneralSkill staticMedalGeneralSkill = StaticMedalDataMgr
                                .getGeneralSkillById(genaralSkillId);
                        // 判断亲密度
                        if (staticMedalGeneralSkill.getIntimateLv() > 0) {
                            int countLv = RankDataManager.calcAgentAllLv(player);// 获取特工总等级
                            if (countLv < staticMedalGeneralSkill.getIntimateLv()) {
                                continue;
                            }
                        }
                        // 判断是否有将领兵种类型限制
                        if (!CheckNull.isEmpty(staticMedalGeneralSkill.getSkillEffect())) {
                            if (staticMedalGeneralSkill.getSkillEffect().size() > 1) {// 有兵种类型限制
                                if (staticMedalGeneralSkill.getSkillEffect().get(0) != sHero.getType()) {
                                    continue;
                                }
                                // 增加属性
                                addAttrValue(tempMap, staticMedalGeneralSkill.getAttrId(),
                                        staticMedalGeneralSkill.getSkillEffect().get(1));
                            } else {
                                // 增加属性
                                addAttrValue(tempMap, staticMedalGeneralSkill.getAttrId(),
                                        staticMedalGeneralSkill.getSkillEffect().get(0));
                            }
                        }
                    }
                }
            }
            // 红色勋章增加属性
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

        // 重新计算模块战斗力
        reCalcAttrFight(player, Constant.ShowFightId.MEDAL, hero, attrMap, tempMap);
    }

    /**
     * buff加成具体值
     *
     * @param player
     * @param attrMutMap
     */
    public static void addEffectSpecificVal(Player player, Hero hero, Map<Integer, Integer> attrMutMap) {
        // 上阵将领才加
        if (hero.getStatus() != HeroConstant.HERO_STATUS_BATTLE) {
            return;
        }
        for (Entry<Integer, Effect> kv : player.getEffect().entrySet()) {
            Effect effect = kv.getValue();
            if (effect != null) {
                //赛季天赋
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
     * buff加成(万分比)
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
     * 添加属性
     *
     * @param attrMap 属性的集合
     * @param attrId  属性的key定义 {@link AttrId}
     * @param value   属性值(oldVal + NewVal)
     */
    public static void addAttrValue(Map<Integer, Integer> attrMap, int attrId, int value) {
        if (value == 0) {
            return;
        }
        // JDK1.8的提供的新方法, 省去了手动判断是否为空, 实参可以传Function函数
        int oldValue = attrMap.getOrDefault(attrId, 0);
        attrMap.put(attrId, oldValue + value);
    }

    /**
     * 获取兵力恢复buff加成值
     *
     * @param player
     * @param now
     * @param banditLv
     * @return
     */
    public static double getRecoverArmyEffect(Player player, int now, int banditLv) {
        double nightRaidRecArmyEffect = StaticNightRaidMgr.getNightRaidRecArmyEffect(player, now, banditLv);// 夜袭加成
        double medicalEffect = 0.0;// 医疗箱加成
        return nightRaidRecArmyEffect + medicalEffect;
    }

    /**
     * 计算恢复的兵力
     * <p>
     * 公式 恢复=损兵*加成值 向下取整
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
            int cnt = (int) (lostCnt * recoverArmyEffect);// 向下取整
            if (cnt > 0) {
                cntMap.put(armyType, cnt);
            }
        });
        return cntMap;
    }

    /**
     * 重新计算附加属性
     *
     * @param mentor
     * @param player
     */
    public static void calcMentorExtAttr(Mentor mentor, Player player) {
        mentor.getExtAttr().clear(); // 清除附加属性
        MentorInfo mentorInfo = player.getMentorInfo();
        StaticMentor sMentor = StaticMentorDataMgr.getsMentorIdMap(mentor.getId()); // 当前等级配置
        int exp = mentor.getExp();
        int count = exp / Constant.INT_HUNDRED;
        if (!CheckNull.isNull(sMentor) && count > 0) {
            StaticMentor nextMentor = StaticMentorDataMgr.getsMentorByTypeAndLv(mentor.getType(), mentor.getLv() + 1); // 下一等级配置
            if (!CheckNull.isNull(nextMentor)) {
                for (Entry<Integer, Integer> en : nextMentor.getAttr().entrySet()) {
                    mentor.getExtAttr().put(en.getKey(), en.getValue() * count);
                }
            }

        }

        // 计算教官总战斗力
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
