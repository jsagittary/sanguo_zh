package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.p.MentorSkill;
import com.gryphpoem.game.zw.resource.domain.p.WallNpc;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.SuperEquip;
import com.gryphpoem.game.zw.resource.pojo.WarPlane;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.fight.*;
import com.gryphpoem.game.zw.resource.pojo.fight.skill.FightSkillAction;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.pojo.medal.RedMedal;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinForce;
import com.gryphpoem.game.zw.resource.pojo.world.CityHero;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName FightService.java
 * @Description 战斗相关
 * @date 创建时间：2017年3月31日 下午5:05:46
 */
@Service
public class FightService {

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private TechDataManager techDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private SolarTermsDataManager solarTermsDataManager;

    @Autowired
    private MedalDataManager medalDataManager;

    @Autowired
    private GlobalDataManager globalDataManager;

    @Autowired
    private WarService warService;

    private Fighter createFighter() {
        Fighter fighter = new Fighter();
        return fighter;
    }

    public Fighter createFighter(Player player, int armyKeyId, List<TwoInt> form) {
        if (CheckNull.isEmpty(form)) {
            throw new IllegalArgumentException(String.format("roleId :%d, armyKeyId :%d, heroMap isEmpty", player.roleId, armyKeyId));
        }
        Fighter fighter = createFighter();
        Force force;
        for (TwoInt twoInt : form) {
            Hero hero = player.heros.get(twoInt.getV1());
            int hpCount = Objects.nonNull(hero) ? hero.getCount() : 0;
            if (hpCount <= 0) {//死亡的将领不进入战斗
                LogUtil.debug(String.format("roleId :%d, armyKeyId :%d, hero count :%d", player.roleId, armyKeyId, hpCount));
                continue;
            }
            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(twoInt.getV1());
            force = createForce(player, staticHero, twoInt.getV1(), hpCount);
            force.roleType = Constant.Role.PLAYER;
            force.ownerId = player.roleId;
            force.camp = player.lord.getCamp();
            force.skillId = staticHero.getSkillId();
            fighter.addForce(force);
            // 加入光环技能
            addMedalAuraSkill(fighter, hero, player);
        }
        fighter.roleType = Constant.Role.PLAYER;
        return fighter;
    }

    public Fighter createFighter(Player player, List<TwoInt> form) {
        if (null == form) {
            return null;
        }
        Fighter fighter = createFighter();
        StaticHero staticHero = null;
        Force force;
        for (TwoInt twoInt : form) {
            staticHero = StaticHeroDataMgr.getHeroMap().get(twoInt.getV1());
            if (null == staticHero) {
                LogUtil.error("创建Fighter，heroId未配置, heroId:", twoInt.getV1());
                continue;
            }
            Hero hero = player.heros.get(twoInt.getV1());
            if (hero == null) {
                LogUtil.error("创建Fighter将领为找到, heroId:", twoInt.getV1(), " roleId:", player.roleId);
                continue;
            }
            force = createForce(player, staticHero, twoInt.getV1(), hero.getCount());
            force.roleType = Constant.Role.PLAYER;
            force.ownerId = player.roleId;
            force.camp = player.lord.getCamp();
            force.skillId = staticHero.getSkillId();
            fighter.addForce(force);
            // 加入光环技能
            addMedalAuraSkill(fighter, hero, player);
            // fighter.addForce(createForce(player, staticHero, twoInt.getV1(), twoInt.getV2()));
        }
        fighter.roleType = Constant.Role.PLAYER;
        return fighter;
    }

    /**
     * 根据npc的阵型创建Figher
     *
     * @param npcForces
     * @return
     */
    public Fighter createFighter(List<NpcForce> npcForces) {
        Fighter fighter = createFighter();
        Force force;
        for (NpcForce npcForce : npcForces) {
            if (!npcForce.alive()) continue;
            force = createCityNpcForce(npcForce.getNpcId(), npcForce.getHp());
            force.roleType = Constant.Role.CITY;
            fighter.addRealForce(force);
        }
        return fighter;
    }

    private Force createNpcForce(NpcForce npcForce) {
        int npcId = npcForce.getNpcId();
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        AttrData attrData = new AttrData(npc.getAttr());
        Force force = new Force(attrData, npc.getArmType(), npc.getLine(), npcId);
        force.hp = npcForce.getHp();
        force.count = npcForce.getHp();
        force.curLine = npcForce.getCurLine();
        return force;
    }

    public Fighter createBossFighter(List<NpcForce> npcForces) {
        if (null == npcForces) {
            return null;
        }

        Fighter fighter = new Fighter();
        for (NpcForce npcForce : npcForces) {
            Force force = createBossNpcForce(npcForce.getNpcId(), npcForce.getHp());
            force.roleType = Constant.Role.CITY;
            fighter.addForce(force);
        }

        fighter.roleType = Constant.Role.BANDIT;
        int allHp = 0; // boss的真实血量
        for (Force f : fighter.forces) {
            allHp += f.hp;
        }
        fighter.lost = fighter.total - allHp;// 总损兵
        return fighter;
    }


    public Fighter createFighterByBattleRole(List<BattleRole> battleRoles) {
        Fighter fighter = createFighter();
        long roleId;
        List<Integer> heroIdList;
        StaticHero staticHero;
        Hero hero;
        Force force;
        for (BattleRole battleRole : battleRoles) {
            roleId = battleRole.getRoleId();
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("创建阵营战守方Fighter对象,player is null=" + roleId);
                continue;
            }
            heroIdList = battleRole.getHeroIdList();
            if (!CheckNull.isEmpty(heroIdList)) {
                for (Integer heroId : heroIdList) {
                    staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                    if (null == staticHero) {
                        LogUtil.error("创建Fighter，heroId未配置, heroId:", heroId, ", roleId:", player.roleId);
                        continue;
                    }

                    hero = player.heros.get(heroId);
                    if (null == hero) {
                        LogUtil.error("玩家没有这个将领，不能创建Force, heroId:", heroId, ", roleId:", player.roleId);
                        continue;
                    }

                    force = createForce(player, staticHero, heroId, hero.getCount());
                    force.roleType = Constant.Role.PLAYER;
                    force.ownerId = player.roleId;
                    force.camp = player.lord.getCamp();
                    force.skillId = staticHero.getSkillId();
                    fighter.addForce(force);
                    // 加入光环技能
                    addMedalAuraSkill(fighter, hero, player);
                }
            }
        }
        return fighter;
    }

    /**
     * 根据部队创建fighter
     *
     * @param armyList
     * @return
     */
    public Fighter createFighterByArmy(List<Army> armyList) {
        Fighter fighter = createFighter();
        StaticHero staticHero = null;
        for (Army army : armyList) {
            Player player = playerDataManager.getPlayer(army.getLordId());
            if (player == null) {
                LogUtil.error("根据army 创建Fighter时 玩家不存在 ", army.getLordId());
                continue;
            }
            for (TwoInt twoInt : army.getHero()) {
                staticHero = StaticHeroDataMgr.getHeroMap().get(twoInt.getV1());
                if (null == staticHero) {
                    LogUtil.error("创建Fighter，heroId未配置, heroId:", twoInt.getV1());
                    continue;
                }
                Hero hero = player.heros.get(twoInt.getV1());
                if (hero == null) {
                    LogUtil.error("创建Fighter将领为找到, heroId:", twoInt.getV1(), " roleId:", player.roleId);
                    continue;
                }
                Force force = createForce(player, staticHero, twoInt.getV1(), hero.getCount());
                force.roleType = Constant.Role.PLAYER;
                force.ownerId = player.roleId;
                force.camp = player.lord.getCamp();
                force.skillId = staticHero.getSkillId();
                fighter.addForce(force);
                // 加入光环技能
                addMedalAuraSkill(fighter, hero, player);
            }
        }
        fighter.roleType = Constant.Role.PLAYER;
        return fighter;

    }

    /**
     * 创建攻方Force
     *
     * @param battle
     * @param npcForm 都城的NPC
     * @return
     */
    public Fighter createMultiPlayerFighter(Battle battle, List<CityHero> npcForm) {
        Fighter fighter = createFighter();
        fighter.roleType = Constant.Role.PLAYER;

        Hero hero;
        Force force;
        List<Integer> heroIdList;
        StaticHero staticHero = null;

        // NPC阵容
        if (!CheckNull.isEmpty(npcForm)) {
            for (CityHero cityHero : npcForm) {
                force = createCityNpcForce(cityHero.getNpcId(), cityHero.getCurArm());
                force.roleType = Constant.Role.CITY;
                fighter.addForce(force);
            }
        }

        // 参与攻击的玩家
        long roleId;
        for (BattleRole battleRole : battle.getAtkList()) {
            roleId = battleRole.getRoleId();
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("创建阵营战守方Fighter对象,player is null=" + roleId);
                continue;
            }
            heroIdList = battleRole.getHeroIdList();
            if (!CheckNull.isEmpty(heroIdList)) {
                for (Integer heroId : heroIdList) {
                    staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                    if (null == staticHero) {
                        LogUtil.error("创建Fighter，heroId未配置, heroId:", heroId, ", roleId:", player.roleId);
                        continue;
                    }

                    hero = player.heros.get(heroId);
                    if (null == hero) {
                        LogUtil.error("玩家没有这个将领，不能创建Force, heroId:", heroId, ", roleId:", player.roleId);
                        continue;
                    }

                    force = createForce(player, staticHero, heroId, hero.getCount());
                    force.roleType = Constant.Role.PLAYER;
                    force.ownerId = player.roleId;
                    force.camp = player.lord.getCamp();
                    force.skillId = staticHero.getSkillId();
//                    force.player = player;
                    fighter.addForce(force);
                    // 加入光环技能
                    addMedalAuraSkill(fighter, hero, player);
                }
            }
        }
        return fighter;
    }

    /**
     * 加入光环技能
     *
     * @param fighter
     * @param hero
     * @param player
     */
    public void addMedalAuraSkill(Fighter fighter, Hero hero, Player player) {
        int heroId = hero.getHeroId();
        if (player.isOnWallHero(heroId)) { // 城防将领光环不生效
            return;
        }
        Map<Integer, Integer> auraSkill = fighter.getAuraSkill(player.roleId);
        if (CheckNull.isNull(auraSkill)) {
            auraSkill = new HashMap<>();
        }
        List<AuraInfo> auraInfos = fighter.getAuraInfo(player.roleId);
        if (CheckNull.isNull(auraInfos)) {
            auraInfos = new ArrayList<>();
        }
        Medal medal = medalDataManager.getHeroMedalByHeroIdAndIndex(player, hero.getHeroId(), MedalConst.HERO_MEDAL_INDEX_0);
        Medal red = medalDataManager.getHeroMedalByHeroIdAndIndex(player, hero.getHeroId(), MedalConst.HERO_MEDAL_INDEX_1);
        // 光环技能
        List<Integer> skillIds = new ArrayList<>();
        if (medal != null && medal.hasAuraSkill()) {
            // 将橙色勋章的光环加入
            skillIds.add(medal.getAuraSkillId());
            // 如果有红色勋章, 并且红色勋章光环被激活了
            if (red != null && ((RedMedal) red).isAuraUnLock()) {
                // 获取橙色勋章配置
                StaticMedalAuraSkill sOrangeSkill = StaticMedalDataMgr.getAuraSkillById(medal.getAuraSkillId());
                if (sOrangeSkill != null) {
                    // 将橙色勋章激活的红色勋章光环加入
                    skillIds.add(sOrangeSkill.getActiveSkill());
                }
            }
        }
        // 加入光环技能
        for (int skillId : skillIds) {
            StaticMedalAuraSkill sMedalAuraSkill = StaticMedalDataMgr.getAuraSkillById(skillId);
            // 获取将领对应类型的兵力
            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
            if (CheckNull.isNull(staticHero)) {
                return;
            }
            // 作用于兵种的类型
            int armType = sMedalAuraSkill.getArmType();
            if (skillId > 0 && armType > 0 && armType == staticHero.getType()) {
                int val = auraSkill.getOrDefault(skillId, 0);
                auraSkill.put(skillId, val + 1);
                auraInfos.add(new AuraInfo(heroId, skillId, player.lord.getNick()));
            }
        }
        fighter.getAuraInfos().put(player.roleId, auraInfos);
        fighter.getMedalAura().put(player.roleId, auraSkill);
    }

    /**
     * 创建盖世太保守方Fighter对象
     *
     * @param battle
     * @param npcForm
     * @return
     */
    public Fighter createGestapoBattleDefencer(Battle battle, List<CityHero> npcForm) {
        Fighter fighter = createFighter();

        Force force;
        List<Integer> heroIdList;
        // 防守方夜间属性加成
        Map<Integer, Integer> nightEffect = solarTermsDataManager.getNightEffect();
        // 城池NPC守军
        if (!CheckNull.isEmpty(npcForm)) {
            for (CityHero cityHero : npcForm) {
                if (cityHero.getCurArm() <= 0) continue;
                force = createCityNpcForce(cityHero.getNpcId(), cityHero.getCurArm());
                force.roleType = Constant.Role.GESTAPO;
                force.attrData.addValue(nightEffect);
                fighter.addForce(force);
            }
        }
        return fighter;
    }

    /**
     * 创建纽约争霸防守方fighter，纽约城hero，在最后出战
     *
     * @param battle
     * @param npcForm
     * @return
     */
    public Fighter createNewYorkWarBattleDefender(Battle battle, List<CityHero> npcForm) {
        Fighter fighter = createFighter();
        fighter.roleType = Constant.Role.PLAYER;
        Hero hero;
        Force force;
        List<Integer> heroIdList;
        // 防守方夜间属性加成
        Map<Integer, Integer> nightEffect = solarTermsDataManager.getNightEffect();
        long roleId;
        for (BattleRole map : battle.getDefList()) {
            roleId = map.getRoleId();
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("创建阵营战守方Fighter对象, player is null=" + roleId);
                continue;
            }
            if (map.getKeyId() == WorldConstant.ARMY_TYPE_WALL_NPC) {
                // 城防NPC
                WallNpc wallNpc = player.wallNpc.get(map.getHeroId(0));
                StaticWallHeroLv staticWallHeroLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());
                force = createWallNpcForce(wallNpc, staticWallHeroLv);
                force.roleType = Constant.Role.WALL;
                force.camp = player.lord.getCamp();
                force.ownerId = player.roleId;
                force.attrData.addValue(nightEffect);
//                //赛季天赋优化 (城墙npc属性加成)
//                DataResource.getBean(SeasonTalentService.class).
//                        getSeasonTalentAttrDataEffect(player, battle.getDefencer(), force.attrData, SeasonConst.TALENT_EFFECT_619);
                fighter.addForce(force);
            } else {
                // 正常的hero
                heroIdList = map.getHeroIdList();
                if (!CheckNull.isEmpty(heroIdList)) {
                    StaticHero staticHero = null;
                    for (Integer heroId : heroIdList) {
                        staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                        if (null == staticHero) {
                            LogUtil.error("创建Fighter，heroId未配置, heroId:", heroId, ", roleId:", player.roleId);
                            continue;
                        }

                        hero = player.heros.get(heroId);
                        if (null == hero) {
                            LogUtil.error("玩家没有这个将领， 不能创建Force, heroId:", heroId, ", roleId:", player.roleId);
                            continue;
                        }

                        if (hero.getCount() <= 0) {
                            LogUtil.error("玩家将领兵力不足，不能创建Force, heroId:", heroId, ", roleId:", player.roleId);
                            continue;
                        }

                        force = createForce(player, staticHero, heroId, hero.getCount());
                        force.roleType = Constant.Role.PLAYER;
                        force.ownerId = player.roleId;
                        force.camp = player.lord.getCamp();
                        force.attrData.addValue(nightEffect);
                        force.skillId = staticHero.getSkillId();
                        //战斗添加额外属性
                        DataResource.getBean(WarService.class).
                                fightForceBuff(battle, force, hero, player, map);
                        fighter.addForce(force);
                        addMedalAuraSkill(fighter, hero, player);
                    }
                }
            }
        }
        // 城池NPC守军
        if (!CheckNull.isEmpty(npcForm)) {
            for (CityHero cityHero : npcForm) {
                if (cityHero.getCurArm() <= 0) {
                    continue;
                }
                force = createCityNpcForce(cityHero.getNpcId(), cityHero.getCurArm());
                force.roleType = Constant.Role.CITY;
                force.attrData.addValue(nightEffect);
                fighter.addForce(force);
            }
        }

        return fighter;
    }

    /**
     * 创建阵营战守方Fighter对象 防守出战顺序:友军支援-自己上阵将领-驻防NPC-友军驻防
     *
     * @return
     */
    public Fighter createCrossWarCampBattleDef(Battle battle, List<CityHero> npcForm) {
        Fighter fighter = createFighter();
        fighter.roleType = Constant.Role.PLAYER;
        Hero hero;
        Force force;
        List<Integer> heroIdList;
        // 防守方夜间属性加成
        Map<Integer, Integer> nightEffect = solarTermsDataManager.getNightEffect();
        // 城池NPC守军
        if (!CheckNull.isEmpty(npcForm)) {
            for (CityHero cityHero : npcForm) {
                if (cityHero.getCurArm() <= 0) continue;
                force = createCrossWarCityNpcForce(cityHero.getNpcId(), cityHero.getCurArm());
                force.roleType = Constant.Role.CITY;
                force.attrData.addValue(nightEffect);
                fighter.addForce(force);
            }
        }

        long roleId;
        for (BattleRole map : battle.getDefList()) {
            roleId = map.getRoleId();
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("创建阵营战守方Fighter对象, player is null=" + roleId);
                continue;
            }
            if (map.getKeyId() == WorldConstant.ARMY_TYPE_WALL_NPC) {
                // 城防NPC
                WallNpc wallNpc = player.wallNpc.get(map.getHeroId(0));
                StaticWallHeroLv staticWallHeroLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(),
                        wallNpc.getLevel());
                force = createWallNpcForce(wallNpc, staticWallHeroLv);
                force.roleType = Constant.Role.WALL;
                force.ownerId = player.roleId;
                force.camp = player.lord.getCamp();
                force.attrData.addValue(nightEffect);
//                //赛季天赋优化 (城墙npc属性加成)
//                DataResource.getBean(SeasonTalentService.class).
//                        getSeasonTalentAttrDataEffect(player, battle.getDefencer(), force.attrData, SeasonConst.TALENT_EFFECT_619);
                fighter.addForce(force);
            } else {
                // 正常的hero
                heroIdList = map.getHeroIdList();
                if (!CheckNull.isEmpty(heroIdList)) {
                    StaticHero staticHero = null;
                    for (Integer heroId : heroIdList) {
                        staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                        if (null == staticHero) {
                            LogUtil.error("创建Fighter，heroId未配置,  heroId:", heroId, ", roleId:", player.roleId);
                            continue;
                        }

                        hero = player.heros.get(heroId);
                        if (null == hero) {
                            LogUtil.error("玩家没有这个将领，不能创建Force,  heroId:", heroId, ", roleId:", player.roleId);
                            continue;
                        }

                        if (hero.getCount() <= 0) {
                            LogUtil.error("玩家将领兵力不足，不能创建Force, heroId:", heroId, ", roleId:", player.roleId);
                            continue;
                        }

                        force = createForce(player, staticHero, heroId, hero.getCount());
                        force.roleType = Constant.Role.PLAYER;
                        force.ownerId = player.roleId;
                        force.camp = player.lord.getCamp();
                        force.attrData.addValue(nightEffect);
                        force.skillId = staticHero.getSkillId();
                        //战斗添加额外属性
                        DataResource.getBean(WarService.class).
                                fightForceBuff(battle, force, hero, player, map);
                        fighter.addForce(force);
                        addMedalAuraSkill(fighter, hero, player);
                    }
                }
            }
        }
        return fighter;
    }


    /**
     * 创建阵营战守方Fighter对象 防守出战顺序:友军支援-自己上阵将领-驻防NPC-友军驻防
     *
     * @param battle
     * @param npcForm
     * @return
     */
    public Fighter createCampBattleDefencer(Battle battle, List<CityHero> npcForm) {
        // List<Player> playerList, Map<Long, List<Integer>> heroMap,
        Fighter fighter = createFighter();
        fighter.roleType = Constant.Role.PLAYER;

        Hero hero;
        Force force;
        List<Integer> heroIdList;
        // 防守方夜间属性加成
        Map<Integer, Integer> nightEffect = solarTermsDataManager.getNightEffect();
        // 城池NPC守军(地图据点NPC守城-配表)
        if (!CheckNull.isEmpty(npcForm)) {
            for (CityHero cityHero : npcForm) {
                if (cityHero.getCurArm() <= 0) continue;
                force = createCityNpcForce(cityHero.getNpcId(), cityHero.getCurArm());
                force.roleType = Constant.Role.CITY;
                force.attrData.addValue(nightEffect);
                fighter.addForce(force);
            }
        }

        long roleId;
        for (BattleRole map : battle.getDefList()) {
            roleId = map.getRoleId();
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("创建阵营战守方Fighter对象,player is null=" + roleId);
                continue;
            }
            if (map.getKeyId() == WorldConstant.ARMY_TYPE_WALL_NPC) {
                // 城防NPC
                WallNpc wallNpc = player.wallNpc.get(map.getHeroId(0));
                StaticWallHeroLv staticWallHeroLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(),
                        wallNpc.getLevel());
                force = createWallNpcForce(wallNpc, staticWallHeroLv);
                force.roleType = Constant.Role.WALL;
                force.ownerId = player.roleId;
                force.camp = player.lord.getCamp();
                force.attrData.addValue(nightEffect);
                //赛季天赋优化 (城墙npc属性加成)
//                DataResource.getBean(SeasonTalentService.class).
//                        getSeasonTalentAttrDataEffect(player, battle.getDefencer(), force.attrData, SeasonConst.TALENT_EFFECT_619);

                fighter.addForce(force);
            } else {
                // 正常的hero
                heroIdList = map.getHeroIdList();
                if (!CheckNull.isEmpty(heroIdList)) {
                    StaticHero staticHero = null;
                    for (Integer heroId : heroIdList) {
                        staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                        if (null == staticHero) {
                            LogUtil.error("创建Fighter，heroId未配置, heroId:", heroId, ", roleId:", player.roleId);
                            continue;
                        }

                        hero = player.heros.get(heroId);
                        if (null == hero) {
                            LogUtil.error("玩家没有这个将领，不能创建Force, heroId:", heroId, ", roleId:", player.roleId);
                            continue;
                        }

                        if (hero.getCount() <= 0) {
                            LogUtil.error("玩家将领兵力不足，不能创建Force, heroId:", heroId, ", roleId:", player.roleId);
                            continue;
                        }

                        force = createForce(player, staticHero, heroId, hero.getCount());
                        force.roleType = Constant.Role.PLAYER;
                        force.ownerId = player.roleId;
                        force.camp = player.lord.getCamp();
                        force.attrData.addValue(nightEffect);
                        force.skillId = staticHero.getSkillId();
                        //战斗添加额外属性
                        DataResource.getBean(WarService.class).
                                fightForceBuff(battle, force, hero, player, map);
                        fighter.addForce(force);
                        addMedalAuraSkill(fighter, hero, player);
                    }
                }
            }
        }
        return fighter;
    }

    /**
     * 创建玩家副本战斗对象
     *
     * @param player  player
     * @param heroIds 选择的将领
     * @return
     */
    public Fighter createCombatPlayerFighter(Player player, List<Integer> heroIds) {
        if (null == player.heroBattle) {
            return null;
        }

        Fighter fighter = createFighter();
        StaticHero staticHero = null;
        Force force;
        for (int heroId : heroIds) {
            Hero hero = player.heros.get(heroId);
            if (hero == null) {
                continue;
            }
            staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            if (null == staticHero) {
                LogUtil.error("创建Fighter，heroId未配置, heroId:", hero.getHeroId());
                continue;
            }
            force = createForce(player, staticHero, hero.getHeroId(), hero.getAttr()[HeroConstant.ATTR_LEAD]);
            force.roleType = Constant.Role.PLAYER;
            force.ownerId = player.roleId;
            force.camp = player.lord.getCamp();
            fighter.addForce(force);
            force.skillId = staticHero.getSkillId();
            // fighter.addForce(createForce(player, staticHero, hero.getHeroId(),
            // hero.getAttr()[HeroConstant.ATTR_LEAD]));
            addMedalAuraSkill(fighter, hero, player);
        }
        fighter.roleType = Constant.Role.PLAYER;
        return fighter;
    }

    /**
     * 创建玩家宝具副本战斗对象
     *
     * @param player 玩家
     * @param ids    参战的将领
     * @return Fighter
     */
    public Fighter createTreasureCombatFighter(Player player, List<Integer> ids) {
        Fighter fighter = createFighter();
        Map<Integer, StaticHero> heroConfList = ids.stream().filter(id -> id > 0 && Objects.nonNull(player.heros.get(id)))
                .map(id -> StaticHeroDataMgr.getHeroMap().get(id))
                .collect(Collectors.toMap(StaticHero::getHeroId, Function.identity()));

        Map<Integer, Long> armyCount = heroConfList.values().stream()
                .map(StaticHero::getType)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 宝具副本增益
        Map<Integer, Integer> extAttr = new HashMap<>();
        if (CheckNull.nonEmpty(armyCount)) {
            List<Map<Integer, Integer>> confList = armyCount.entrySet().stream()
                    .map(en -> StaticTreasureWareDataMgr.getTreasureCombatBuff(en.getKey(), en.getValue()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            for (Map<Integer, Integer> map : confList) {
                map.forEach((k, v) -> extAttr.merge(k, v, Integer::sum));
            }
        }
        StaticHero staticHero;
        Force force;
        for (int heroId : ids) {
            if (heroId == 0)
                continue;
            Hero hero = player.heros.get(heroId);
            staticHero = heroConfList.get(hero.getHeroId());
            force = createForce0(player, staticHero, hero.getHeroId(), hero.getAttr()[HeroConstant.ATTR_LEAD]);
            AttrData attrData = force.getAttrData();
            if (CheckNull.nonEmpty(extAttr)) {
                // 添加额外增益, 只用于计算, 不上面板
                attrData.addValue(extAttr);
            }
            force.roleType = Constant.Role.PLAYER;
            force.ownerId = player.roleId;
            force.camp = player.lord.getCamp();
            //重新计算兵力
            force.maxHp = attrData.lead;
            force.hp = force.maxHp;
            force.lead = (int) Math.ceil(attrData.lead * 1.0 / force.maxLine);
            force.count = force.maxHp % force.lead;
            if (force.count == 0) {
                force.count = force.lead;
            }
            fighter.addForce(force);
            force.skillId = staticHero.getSkillId();
            addMedalAuraSkill(fighter, hero, player);
        }
        fighter.roleType = Constant.Role.PLAYER;
        return fighter;
    }

    public Fighter createSandTableFighter(Player player, List<Integer> heroIds) {
        Fighter fighter = createFighter();
        StaticHero staticHero;
        Force force;
        for (int heroId : heroIds) {
            Hero hero = player.heros.get(heroId);
            if (hero == null) continue;
            staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            if (staticHero == null) continue;
            force = createForce0(player, staticHero, hero.getHeroId(), hero.getAttr()[HeroConstant.ATTR_LEAD]);
            force.roleType = Constant.Role.PLAYER;
            force.ownerId = player.roleId;
            force.camp = player.lord.getCamp();
            fighter.addForce(force);
            force.skillId = staticHero.getSkillId();
            addMedalAuraSkill(fighter, hero, player);
        }
        fighter.roleType = Constant.Role.PLAYER;
        return fighter;
    }

    /**
     * 多人副本创建
     *
     * @param players
     * @return
     */
    public Fighter createCombatPlayerFighter(List<Player> players) {
        Fighter fighter = createFighter();

        for (Player p : players) {
            if (p.heroBattle != null) {
                for (int i = 1; i < p.heroBattle.length; i++) {
                    int heroId = p.heroBattle[i];
                    Hero hero = p.heros.get(heroId);
                    if (hero == null) {
                        continue;
                    }
                    StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                    if (null == staticHero) {
                        LogUtil.error("创建Fighter，heroId未配置, heroId:", hero.getHeroId());
                        continue;
                    }
                    Force force = createForce(p, staticHero, hero.getHeroId(), hero.getAttr()[HeroConstant.ATTR_LEAD]);
                    force.roleType = Constant.Role.PLAYER;
                    force.ownerId = p.roleId;
                    force.camp = p.lord.getCamp();
                    force.skillId = staticHero.getSkillId();
                    fighter.addForce(force);
                    addMedalAuraSkill(fighter, hero, p);
                }
            }
        }
        fighter.roleType = Constant.Role.PLAYER;
        return fighter;
    }

    /**
     * 创建bossNpc的势力对象(去除已阵亡的将领)
     *
     * @param boss
     * @return
     */
    public Fighter createBossNpcForce(Fighter boss) {
        int coef = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.FORM_COEF_OF_DIFFICULTY)
                .getOrDefault(GlobalConstant.CoefDifficulty.COUNTER_ATTACK, 100);
        LogUtil.error("反攻德意志当前难度系数, coef:", coef);
        Fighter fighter = new Fighter();
        fighter.roleType = Constant.Role.BANDIT;

        for (Force force : boss.getForces()) {
            if (force.alive()) {
                Force npcForce = createBossNpcForce(force.id, force.hp, coef);
                npcForce.roleType = Constant.Role.CITY;
                fighter.addForce(npcForce);
            }
        }
        int allHp = 0; // boss的真实血量
        for (Force f : fighter.forces) {
            allHp += f.hp;
        }
        fighter.lost = fighter.total - allHp;// 总损兵
        return fighter;
    }

    /**
     * 创建bossNpc的势力
     *
     * @param npcId npcId
     * @param curHp 当前血量
     * @param coef  难度系数
     * @return
     */
    private Force createBossNpcForce(int npcId, int curHp, int coef) {
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        AttrData attrData = new AttrData(npc.getAttr(), coef);
        Force force = new Force(attrData, npc.getArmType(), npc.getLine(), npcId);
        force.hp = curHp;
        force.totalLost = force.maxHp - force.hp; // 总损兵
        int tmpTotalLost = force.totalLost;
        // 计算当前是第几排,从0开始
        int curLine = 0;// 当前第几排
        while (tmpTotalLost >= force.lead) {
            curLine++;
            tmpTotalLost -= force.lead;
        }
        force.count = force.lead - tmpTotalLost;// 本排兵剩余数量
        force.curLine = curLine;
        return force;
    }

    /**
     * 创建玩家副本战斗对象
     *
     * @param player
     * @return
     */
    public Fighter createWorldBossPlayerFighter(Player player) {
        if (null == player.heroBattle) {
            return null;
        }

        Fighter fighter = createFighter();
        StaticHero staticHero = null;
        Force force;
        for (int i = 0; i < player.heroBattle.length; i++) {
            Hero hero = player.getBattleHeroByPos(i);
            if (hero == null) {
                continue;
            }
            staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            if (null == staticHero) {
                LogUtil.error("创建Fighter，heroId未配置, heroId:", hero.getHeroId());
                continue;
            }
            force = createForce(player, staticHero, hero.getHeroId(), hero.getAttr()[HeroConstant.ATTR_LEAD]);
            force.roleType = Constant.Role.PLAYER;
            force.ownerId = player.roleId;
            force.camp = player.lord.getCamp();
            fighter.addForce(force);
            force.skillId = staticHero.getSkillId();
            // fighter.addForce(createForce(player, staticHero, hero.getHeroId(),
            // hero.getAttr()[HeroConstant.ATTR_LEAD]));
            addMedalAuraSkill(fighter, hero, player);
        }
        fighter.roleType = Constant.Role.PLAYER;
        return fighter;
    }

    /**
     * 创建流寇战斗对象
     *
     * @param banditId
     * @return
     */
    public Fighter createBanditFighter(int banditId) {
        StaticBandit staticBandit = StaticBanditDataMgr.getBanditMap().get(banditId);
        if (null == staticBandit) {
            LogUtil.error("流寇id未配置, banditId:", banditId);
            return null;
        }
        List<Integer> npcIdList = staticBandit.getForm();
        if (null == npcIdList) {
            return null;
        }

        Fighter fighter = createFighter();
        for (Integer npcId : npcIdList) {
            fighter.addForce(createNpcForce(npcId));
        }
        fighter.roleType = Constant.Role.BANDIT;
        return fighter;
    }

    /**
     * 创建阵营战NPC方的Fighter对象
     *
     * @param npcIdList
     * @return
     */
    public Fighter createNpcFighter(List<Integer> npcIdList) {
        if (null == npcIdList) {
            return null;
        }
        Fighter fighter = createFighter();
        for (Integer npcId : npcIdList) {
            fighter.addForce(createNpcForce(npcId));
        }
        fighter.roleType = Constant.Role.BANDIT;
        return fighter;
    }

    /**
     * 创建Force对象
     *
     * @param npcId
     * @return
     */
    public Force createNpcForce(int npcId) {
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        AttrData attrData = new AttrData(npc.getAttr());
        return new Force(attrData, npc.getArmType(), npc.getLine(), npcId);
    }

    /**
     * 创建Force对象
     *
     * @param npcId
     * @param count
     * @return
     */
    public Force createCityNpcForce(int npcId, int count) {
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        AttrData attrData = new AttrData(npc.getAttr());
        return new Force(attrData, npc.getArmType(), count, attrData.lead, npcId, attrData.lead, npc.getLine());
    }

    /**
     * 创建Force对象(可调整系数，针对npc攻击和穿甲调整)
     *
     * @param npcId
     * @param count
     * @return
     */
    public Force createCrossWarCityNpcForce(int npcId, int count) {
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        Map<Integer, Integer> attr = npc.getAttr();
        AttrData attrData = new AttrData(attr);
        int crossCityNpcChgAttrCnt = globalDataManager.getGameGlobal().getCrossCityNpcChgAttrCnt();
        // npc 新属性 = 原属性 * （1 + 调整次数*调整系数（万分比））
        if (crossCityNpcChgAttrCnt > 0 && attr.containsKey(Constant.AttrId.ATTACK)) {
            int attack = Optional.ofNullable(attr.get(Constant.AttrId.ATTACK)).orElse(0);
            int add = (int) (attack * crossCityNpcChgAttrCnt * (WorldConstant.WORLD_WAR_CITY_EFFECT / Constant.TEN_THROUSAND));
            attrData.addValue(Constant.AttrId.ATTACK, add);
            LogUtil.common("调整世界争霸city npc 攻击属性 npcId: ", npcId, " cur_attack: ", attack, " add_attack: ", add);
        }
        if (crossCityNpcChgAttrCnt > 0 && attr.containsKey(Constant.AttrId.ATTACK_EXT)) {
            int attackExt = Optional.ofNullable(attr.get(Constant.AttrId.ATTACK_EXT)).orElse(0);
            int add = (int) (attackExt * crossCityNpcChgAttrCnt * (WorldConstant.WORLD_WAR_CITY_EFFECT / Constant.TEN_THROUSAND));
            attrData.addValue(Constant.AttrId.ATTACK_EXT, add);
            LogUtil.common("调整世界争霸city npc 穿甲属性 npcId: ", npcId, " cur_attackExt: ", attackExt, " add_attackExt: ", add);
        }
        return new Force(attrData, npc.getArmType(), count, attrData.lead, npcId, attrData.lead, npc.getLine());
    }

    /**
     * 创建Force对象
     *
     * @param npcId
     * @param count
     * @param coef  难度系数(百分比)
     * @return
     */
    public Force createCityNpcForce(int npcId, int count, int coef) {
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        AttrData attrData = new AttrData(npc.getAttr(), coef);
        return new Force(attrData, npc.getArmType(), count, attrData.lead, npcId, attrData.lead, npc.getLine());
    }

    public Force createWallNpcForce(WallNpc wallNpc, StaticWallHeroLv staticWallHeroLv) {
        AttrData attrData = new AttrData(staticWallHeroLv.getAttr());
        return new Force(attrData, staticWallHeroLv.getType(), wallNpc.getCount(), attrData.lead, wallNpc.getId(),
                attrData.lead, staticWallHeroLv.getLine());
    }

    /**
     * @param player
     * @param staticHero
     * @param heroId     将领id
     * @param count
     * @return Force
     * @Title: createForce
     * @Description: 创建玩家 force
     */
    public Force createForce(Player player, StaticHero staticHero, int heroId, int count) {
        Hero hero = player.heros.get(heroId);
        Map<Integer, Integer> attrMap = CalculateUtil.processAttr(player, hero);
        AttrData attrData = new AttrData(attrMap);
        int line = calcHeroLine(player, hero, staticHero.getLine());
        int lead = (int) Math.ceil(hero.getAttr()[HeroConstant.ATTR_LEAD] * 1.0 / line);// 当兵力不能被整除时，向上取整
        int heroLv = techDataManager.getIntensifyLv4HeroType(player, staticHero.getType());// 等级
        int restrain = techDataManager.getIntensifyRestrain4HeroType(player, staticHero.getType());// 克制值
        Force force = new Force(attrData, staticHero.getType(), count, lead, heroId, player.roleId);
        // 添加战机详情
        addPlaneInfo(player, hero, force);
        //设置英雄战斗技能
        loadHeroSkill(force, hero);
        force.setIntensifyLv(heroLv);
        force.setEffect(restrain);
        return force;
    }

    public Force createForce0(Player player, StaticHero staticHero, int heroId, int count) {
        Hero hero = player.heros.get(heroId);
        Map<Integer, Integer> attrMap = CalculateUtil.processHeroAttr(player, hero);
        AttrData attrData = new AttrData(attrMap);
        int line = calcHeroLine(player, hero, staticHero.getLine());
        int lead = (int) Math.ceil(hero.getAttr()[HeroConstant.ATTR_LEAD] * 1.0 / line);// 当兵力不能被整除时，向上取整
        int heroLv = techDataManager.getIntensifyLv4HeroType(player, staticHero.getType());// 等级
        int restrain = techDataManager.getIntensifyRestrain4HeroType(player, staticHero.getType());// 克制值
        Force force = new Force(attrData, staticHero.getType(), count, lead, heroId, player.roleId);
        // 添加战机详情
        addPlaneInfo(player, hero, force);
        //设置英雄战斗技能
        loadHeroSkill(force, hero);
        force.setIntensifyLv(heroLv);
        force.setEffect(restrain);
        return force;
    }

    /**
     * 设置英雄战斗技能
     *
     * @param force
     * @param hero
     */
    private void loadHeroSkill(Force force, Hero hero) {
        for (Entry<Integer, Integer> entry : hero.getSkillLevels().entrySet()) {
            StaticHeroSeasonSkill heroSkill = StaticHeroDataMgr.getHeroSkill(hero.getHeroId(), entry.getKey(), entry.getValue());
            if (Objects.nonNull(heroSkill)) {
                StaticSkillAction ska = StaticFightDataMgr.getSkillAction(heroSkill.getSkillActionId());
                if (Objects.nonNull(ska)) {
                    FightSkillAction fightSkillAction = new FightSkillAction(ska);
                    Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(force.ownerId);
                    //宝具增加的释放技能概率
                    fightSkillAction.setAddProbability(DataResource.getBean(TreasureWareService.class).addSkillBuff(player, hero,
                            TreasureWareConst.SpecialType.SEASON_HERO, TreasureWareConst.SpecialType.SeasonHero.SKILL_RELEASE_PROBABILITY,
                            ska.getBaseSkill()));
                    force.fightSkills.add(fightSkillAction);
                }
            }
        }
    }

    /**
     * Force添加战机详情
     *
     * @param player
     * @param hero
     * @param force
     */
    private void addPlaneInfo(Player player, Hero hero, Force force) {
        List<Integer> warPlanes = hero.getWarPlanes();
        if (CheckNull.isNull(player) || CheckNull.isEmpty(warPlanes)) {
            return;
        }
        // 战机列表
        for (int planeId : warPlanes) {
            StaticPlaneUpgrade sPlaneUpgrade = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
            if (CheckNull.isNull(sPlaneUpgrade)) {
                return;
            }
            WarPlane warPlane = player.warPlanes.get(sPlaneUpgrade.getPlaneType());
            if (CheckNull.isNull(warPlane)) {
                return;
            }
            int planeType = warPlane.getType();
            StaticPlaneInit sPlaneInit = StaticWarPlaneDataMgr.getPlaneInitByType(planeType);
            int skillId = sPlaneUpgrade.getSkillId();
            // 拥有技能, 创建Force的战机详情
            PlaneInfo info = new PlaneInfo();
            if (skillId > 0) {
                info.setPlaneId(planeId);
                info.setSkillId(skillId);
                info.setUseSkill(false);
            }
            int quality = sPlaneInit.getQuality();
            int skillType = sPlaneInit.getSkillType();
            MentorSkill skill = player.getMentorInfo().getSkillMap().get(skillType);
            // 拥有专业技能, 并且已经激活
            if (!CheckNull.isNull(skill) && skill.isActivate()) {
                StaticMentorSkill sMentorSkill = StaticMentorDataMgr.getMentorSkillByTypeAndLv(skillType,
                        skill.getLv());
                if (!CheckNull.isNull(sMentorSkill) && !CheckNull.isEmpty(sMentorSkill.getSkill())) {
                    if (!sMentorSkill.getSkill().containsKey(quality)) {
                        LogUtil.error("战机专业技能配置错误, roleId:", player.roleId, ", planeType", planeType, ", fightSkill:",
                                sMentorSkill.getSkill());
                    } else {
                        StaticFightSkill sFightSkill = StaticFightDataMgr
                                .getFightSkillMapById(sMentorSkill.getSkill().getOrDefault(quality, 0));
                        int battlePos = warPlane.getBattlePos(); // 战机在将领上的上阵位置
                        List<FightSkill> skills = force.fightSkill.get(battlePos);
                        if (CheckNull.isNull(skills)) {
                            skills = new ArrayList<>();
                            force.fightSkill.put(battlePos, skills);
                        }
                        if (!CheckNull.isNull(sFightSkill)) {
                            PlaneFightSkill fightSkill = new PlaneFightSkill(sFightSkill);
                            fightSkill.setPlaneId(planeId); // 记录战机id
                            if (!CheckNull.isEmpty(sFightSkill.getAttackTime())) {
                                List<Integer> randomByWeight = RandomUtil.getRandomByWeight(sFightSkill.getAttackTime(),
                                        1, false);
                                if (!CheckNull.isEmpty(randomByWeight)) { // 计算最大释放次数
                                    int maxReleaseCnt = randomByWeight.get(0);
                                    fightSkill.param.put(PlaneConstant.SkillParam.MAX_RELEASE_CNT, maxReleaseCnt);
                                }
                            }
                            skills.add(fightSkill);
                        }

                    }
                }
            }
            force.planeInfos.put(warPlane.getBattlePos(), info);
        }
    }

    /**
     * @param player
     * @param staticHero
     * @param heroId
     * @param count
     * @param atkOrDef
     * @param addMode
     * @param addTime
     * @param camp
     * @return BerlinForce
     * @Title: createBerlinForce
     * @Description: 柏林会战 创建 玩家force
     */
    public BerlinForce createBerlinForce(Player player, StaticHero staticHero, int heroId, int count, int atkOrDef,
                                         int addMode, long addTime, int camp) {
        Hero hero = player.heros.get(heroId);
        Map<Integer, Integer> attrMap = CalculateUtil.processAttr(player, hero);
        int line = calcHeroLine(player, hero, staticHero.getLine());
        int lead = (int) Math.ceil(hero.getAttr()[HeroConstant.ATTR_LEAD] * 1.0 / line);// 当兵力不能被整除时，向上取整
        AttrData attrData = new AttrData(attrMap);
        // 查询玩家兵种等级 和 强化科技 额外加成的克制
        int heroLv = techDataManager.getIntensifyLv4HeroType(player, staticHero.getType());// 等级
        int restrain = techDataManager.getIntensifyRestrain4HeroType(player, staticHero.getType());// 克制值

        BerlinForce force = new BerlinForce(attrData, staticHero.getType(), count, lead, heroId, atkOrDef, addMode,
                addTime, camp, player.roleId, heroLv, restrain, 0);
        force.roleType = Constant.Role.PLAYER;
        // 添加战机详情
        addPlaneInfo(player, hero, force);
        //设置英雄战斗技能
        loadHeroSkill(force, hero);
        return force;
    }

    /**
     * 国器加成
     *
     * @param player
     * @param attrMap
     */
    private void addSuperEquipEffect(Player player, Map<Integer, Integer> attrMap) {
        StaticSuperEquipLv staticSuperEquipLv = null;
        for (Entry<Integer, SuperEquip> kv : player.supEquips.entrySet()) {
            staticSuperEquipLv = StaticPropDataMgr.getSuperEquipLv(kv.getValue().getType(), kv.getValue().getLv());
            if (staticSuperEquipLv != null && staticSuperEquipLv.getAttrs() != null
                    && staticSuperEquipLv.getAttrs().size() > 1) {
                for (List<Integer> attr : staticSuperEquipLv.getAttrs()) {
                    addAttrValue(attrMap, attr.get(0), attr.get(1));
                }
            }
        }
    }

    /**
     * 科技加成
     *
     * @param player
     * @param attrMap
     * @param heroType
     */
    private void addTechEffect(Player player, Map<Integer, Integer> attrMap, int heroType) {
        if (heroType == 1) {
            addAttrValue(attrMap, HeroConstant.ATTR_ATTACK,
                    techDataManager.getEffect4BuildingType(player, BuildingType.FACTORY_1));
        } else if (heroType == 2) {
            addAttrValue(attrMap, HeroConstant.ATTR_ATTACK,
                    techDataManager.getEffect4BuildingType(player, BuildingType.FACTORY_2));
        } else if (heroType == 3) {
            addAttrValue(attrMap, HeroConstant.ATTR_ATTACK,
                    techDataManager.getEffect4BuildingType(player, BuildingType.FACTORY_3));
        }
    }

    /**
     * 基础属性*加成万分比
     *
     * @param attrMutMap
     */
    private void processFinalAttr(Map<Integer, Integer> attrMap, Map<Integer, Integer> attrMutMap) {
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
                default:
                    break;
            }
        }
    }

    /**
     * 装备属性加成
     *
     * @param player
     * @param hero
     * @param attrMap
     */
    public void addEquipEffect(Player player, Hero hero, Map<Integer, Integer> attrMap) {
        Equip equip;
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

                // 装备基础属性加成
                for (Entry<Integer, Integer> entry : staticEquip.getAttr().entrySet()) {
                    addAttrValue(attrMap, entry.getKey(), entry.getValue());
                }

                // 装备洗炼属性加成
                List<Turple<Integer, Integer>> attrLvs = equip.getAttrAndLv();
                for (int j = 0; j < attrLvs.size(); j++) {
                    // 当前洗炼属性
                    Turple<Integer, Integer> al = attrLvs.get(j);
                    StaticEquipExtra equipExtra = StaticPropDataMgr.getEuqipExtraByIdAndLv(al.getA(), al.getB(), staticEquip.getEquipPart());
                    if (equipExtra != null && Arrays.binarySearch(Constant.BASE_ATTRS, al.getA()) < 0) {
                        addAttrValue(attrMap, al.getA(), equipExtra.getAttrValue());
                    }
                }
            }
        }
    }

    /**
     * buff加成
     *
     * @param player
     * @param attrMutMap
     */
    private void addEffectVal(Player player, Map<Integer, Integer> attrMutMap) {
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

    public void addAttrValue(Map<Integer, Integer> attrMap, int attrId, int value) {
        Integer v = attrMap.get(attrId);
        if (null == v) {
            attrMap.put(attrId, value);
        } else {
            attrMap.put(attrId, v + value);
        }
    }

    /**
     * 计算将领当前兵力排数
     *
     * @param player
     * @param hero
     * @param baseLine
     * @return
     */
    public int calcHeroLine(Player player, Hero hero, int baseLine) {
        // 点兵将领加成
        int leadLine = 0;
        if (player.cabinet != null) {
            int planId = player.cabinet.getEffectPlanId();
            StaticCabinetPlan cabinetPlan = StaticBuildingDataMgr.getCabinetPlanById(planId);
            if (cabinetPlan != null && !CheckNull.isEmpty(cabinetPlan.getEffect())
                    && cabinetPlan.getEffect().size() == 2) {
                if ((hero.isOnAcq() || hero.isCommando()) && cabinetPlan.getEffect().get(1) > 0) {// 采集
                    leadLine += cabinetPlan.getEffect().get(1);
                } else if (hero.isOnBattle() && cabinetPlan.getEffect().get(0) > 0) {// 上阵主将
                    leadLine += cabinetPlan.getEffect().get(0);
                }
            }
        }

        //城防将领兵排数科技
        if (hero.isOnWall()) {
            //高级禁卫军加成
            if (player.tech.getTechLv().containsKey(TechConstant.TYPE_32)) {
                leadLine += techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_32);
            } else {//中级禁卫军加成
                leadLine += techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_31);
            }
        }

        // 科技加成
        int line = techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_16);
        if (line > 0) {
            return baseLine + line + player.common.getLineAdd() + leadLine;
        }
        line = techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_7);
        if (line > 0) {
            return baseLine + line + player.common.getLineAdd() + leadLine;
        }
        LogUtil.debug("baseLine=" + baseLine + ",line=" + line + ",leadLine=" + leadLine);
        return baseLine + player.common.getLineAdd() + leadLine;
    }

    public CommonPb.RptAtkBandit.Builder createRptBuilderPb(int roundId, Fighter attacker, Fighter defender,
                                                            FightLogic fightLogic, boolean defSucce, Player defPlayer) {
        // 战斗记录
        CommonPb.RptAtkBandit.Builder rpt = CommonPb.RptAtkBandit.newBuilder();
        rpt.setResult(defSucce);
        // 注意此处,是进攻防双方信息是交换的
        rpt.setAttack(PbHelper.createRptMan(defPlayer.lord.getPos(), defPlayer.lord.getNick(), defPlayer.lord.getVip(),
                defPlayer.lord.getLevel())); // 注意啦！~~~~其实是防守方
        rpt.setDefend(PbHelper.createRptBandit(roundId, defPlayer.lord.getPos())); // 注意啦！~~~~ 其实匪军是进攻方
        rpt.setAtkSum(PbHelper.createRptSummary(defender.total, defender.lost, defPlayer.lord.getCamp(),
                defPlayer.lord.getNick(), defPlayer.lord.getPortrait(), defPlayer.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(attacker.total, attacker.lost, 0, null, -1, -1));
        for (Force force : defender.forces) {
            CommonPb.RptHero rptHero = forceToRptHeroNoExp(force);
            if (rptHero != null) {
                rpt.addAtkHero(rptHero);
            }
        }
        DataResource.ac.getBean(WorldService.class).buildRptHeroData(attacker, rpt, false);
        // 回合信息
        CommonPb.Record record = fightLogic.generateRecord();
        rpt.setRecord(record);

        return rpt;
    }

    public CommonPb.RptHero forceToRptHeroNoExp(Force force) {
        Hero hero = null;
        long roleId = force.ownerId;
        int type = force.roleType;
        int kill = force.killed;
        int heroId = force.id;
        String owner = playerDataManager.getNickByLordId(roleId);
        int lv = 0;
        int lost = force.totalLost;
        int heroDecorated = 0;
        if (force.roleType == Constant.Role.CITY) {

        } else if (force.roleType == Constant.Role.WALL) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) return null;
            heroId = player.wallNpc.get(heroId).getWallHeroLvId();
        } else if (force.roleType == Constant.Role.PLAYER) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) return null;
            hero = player.heros.get(heroId);
            if (hero == null) return null;
            lv = hero.getLevel();
            heroDecorated = hero.getDecorated();
        }
        CommonPb.RptHero rptHero = PbHelper.createRptHero(type, kill, 0, heroId, owner, lv, 0, lost, hero);
        return rptHero;
    }

    /**
     * 创建bossNpc的势力
     *
     * @param npcId
     * @param curHp 当前血量
     * @return
     */
    public static Force createBossNpcForce(int npcId, int curHp) {
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        AttrData attrData = new AttrData(npc.getAttr());
        Force force = new Force(attrData, npc.getArmType(), npc.getLine(), npcId);
        force.hp = curHp;
        force.totalLost = force.maxHp - force.hp; // 总损兵
        int tmpTotalLost = force.totalLost;
        // 计算当前是第几排,从0开始
        int curLine = 0;// 当前第几排
        int cnt = 4;// 防止死循环
        while (tmpTotalLost >= force.lead && cnt-- > 0) {
            curLine++;
            tmpTotalLost -= force.lead;
        }
        force.count = force.lead - tmpTotalLost;// 本排兵剩余数量
        force.curLine = curLine;
        return force;
    }

    /**
     * 添加战功并创建RptHero
     *
     * @param force
     * @param changeMap
     * @param awardFrom
     * @return
     */
    public CommonPb.RptHero addExploitAndBuildRptHero(Force force, Map<Long, ChangeInfo> changeMap, AwardFrom awardFrom) {
        //计算战功
        ChangeInfo changeInfo = changeMap.computeIfAbsent(force.ownerId, v -> ChangeInfo.newIns());
        int exploit = (int) (force.totalLost * 0.1f);
        Player player = playerDataManager.getPlayer(force.ownerId);
        warService.addExploit(player, exploit, changeInfo, awardFrom);
        return PbHelper.createRptHero(force.roleType, force.killed, exploit, force.id, player.lord.getNick(), player.lord.getLevel(), 0, force.totalLost, player.heros.get(force.id));
    }
}
