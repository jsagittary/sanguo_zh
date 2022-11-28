package com.gryphpoem.game.zw.service;

import com.gryphpoem.cross.constants.FightCommonConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.pojo.p.*;
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
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.pojo.medal.RedMedal;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinForce;
import com.gryphpoem.game.zw.resource.pojo.world.CityHero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.HeroUtil;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

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

    /**
     * 创建王朝遗迹战斗信息
     *
     * @param player
     * @param armyKeyId
     * @param form
     * @param holdTime
     * @return
     */
    public Fighter createRelicFighter(Player player, int armyKeyId, List<CommonPb.PartnerHeroIdPb> form, long holdTime) {
        if (CheckNull.isEmpty(form)) {
            throw new IllegalArgumentException(String.format("roleId :%d, armyKeyId :%d, heroMap isEmpty", player.roleId, armyKeyId));
        }
        Fighter fighter = createFighter();
        Force force;
        for (CommonPb.PartnerHeroIdPb partnerHero : form) {
            Hero hero = player.heros.get(partnerHero.getPrincipleHeroId());
            if (CheckNull.isNull(hero)) continue;
            force = createRelicForce(player, partnerHero, holdTime);
            force.roleType = Constant.Role.PLAYER;
            force.ownerId = player.roleId;
            force.camp = player.lord.getCamp();
            fighter.addForce(force);
            // 加入光环技能
            addMedalAuraSkill(fighter, hero, player);
        }
        fighter.roleType = Constant.Role.PLAYER;
        return fighter;
    }

    public Fighter createFighter(Player player, List<CommonPb.PartnerHeroIdPb> form) {
        if (null == form) {
            return null;
        }
        Fighter fighter = createFighter();
        StaticHero staticHero = null;
        Force force;
        for (CommonPb.PartnerHeroIdPb partnerHero : form) {
            if (CheckNull.isNull(partnerHero)) continue;
            staticHero = StaticHeroDataMgr.getHeroMap().get(partnerHero.getPrincipleHeroId());
            if (null == staticHero) {
                LogUtil.error("创建Fighter，heroId未配置, heroId:", partnerHero.getPrincipleHeroId());
                continue;
            }
            Hero hero = player.heros.get(partnerHero.getPrincipleHeroId());
            if (hero == null) {
                LogUtil.error("创建Fighter将领为找到, heroId:", partnerHero.getPrincipleHeroId(), " roleId:", player.roleId);
                continue;
            }
            force = createForce(player, partnerHero);
            force.roleType = Constant.Role.PLAYER;
            force.ownerId = player.roleId;
            force.camp = player.lord.getCamp();
            fighter.addForce(force);
            // 加入光环技能
            addMedalAuraSkill(fighter, hero, player);
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
        List<CommonPb.PartnerHeroIdPb> heroList;
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
            heroList = battleRole.getPartnerHeroIdList();
            if (!CheckNull.isEmpty(heroList)) {
                for (CommonPb.PartnerHeroIdPb partnerHeroIdPb : heroList) {
                    staticHero = StaticHeroDataMgr.getHeroMap().get(partnerHeroIdPb.getPrincipleHeroId());
                    if (null == staticHero) {
                        LogUtil.error("创建Fighter，heroId未配置, heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                        continue;
                    }

                    hero = player.heros.get(partnerHeroIdPb.getPrincipleHeroId());
                    if (null == hero) {
                        LogUtil.error("玩家没有这个将领，不能创建Force, heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                        continue;
                    }

                    force = createForce(player, partnerHeroIdPb);
                    force.roleType = Constant.Role.PLAYER;
                    force.ownerId = player.roleId;
                    force.camp = player.lord.getCamp();
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
            for (CommonPb.PartnerHeroIdPb partnerHeroIdPb : army.getHero()) {
                staticHero = StaticHeroDataMgr.getHeroMap().get(partnerHeroIdPb.getPrincipleHeroId());
                if (null == staticHero) {
                    LogUtil.error("创建Fighter，heroId未配置, heroId:", partnerHeroIdPb.getPrincipleHeroId());
                    continue;
                }
                Hero hero = player.heros.get(partnerHeroIdPb.getPrincipleHeroId());
                if (hero == null) {
                    LogUtil.error("创建Fighter将领为找到, heroId:", partnerHeroIdPb.getPrincipleHeroId(), " roleId:", player.roleId);
                    continue;
                }
                Force force = createForce(player, partnerHeroIdPb);
                force.roleType = Constant.Role.PLAYER;
                force.ownerId = player.roleId;
                force.camp = player.lord.getCamp();
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
        List<CommonPb.PartnerHeroIdPb> heroIdList;
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
            heroIdList = battleRole.getPartnerHeroIdList();
            if (!CheckNull.isEmpty(heroIdList)) {
                for (CommonPb.PartnerHeroIdPb partnerHeroIdPb : heroIdList) {
                    staticHero = StaticHeroDataMgr.getHeroMap().get(partnerHeroIdPb.getPrincipleHeroId());
                    if (null == staticHero) {
                        LogUtil.error("创建Fighter，heroId未配置, heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                        continue;
                    }

                    hero = player.heros.get(partnerHeroIdPb.getPrincipleHeroId());
                    if (null == hero) {
                        LogUtil.error("玩家没有这个将领，不能创建Force, heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                        continue;
                    }

                    force = createForce(player, partnerHeroIdPb);
                    force.roleType = Constant.Role.PLAYER;
                    force.ownerId = player.roleId;
                    force.camp = player.lord.getCamp();
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
        List<CommonPb.PartnerHeroIdPb> heroIdList;
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
                WallNpc wallNpc = player.wallNpc.get(map.getPartnerHeroId(0).getPrincipleHeroId());
                StaticWallHeroLv staticWallHeroLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());
                force = createWallNpcForce(wallNpc, staticWallHeroLv);
                force.roleType = Constant.Role.WALL;
                force.camp = player.lord.getCamp();
                force.ownerId = player.roleId;
                force.attrData.addValue(nightEffect);
                fighter.addForce(force);
            } else {
                // 正常的hero
                heroIdList = map.getPartnerHeroIdList();
                if (!CheckNull.isEmpty(heroIdList)) {
                    StaticHero staticHero = null;
                    for (CommonPb.PartnerHeroIdPb partnerHeroIdPb : heroIdList) {
                        staticHero = StaticHeroDataMgr.getHeroMap().get(partnerHeroIdPb.getPrincipleHeroId());
                        if (null == staticHero) {
                            LogUtil.error("创建Fighter，heroId未配置, heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                            continue;
                        }

                        hero = player.heros.get(partnerHeroIdPb.getPrincipleHeroId());
                        if (null == hero) {
                            LogUtil.error("玩家没有这个将领， 不能创建Force, heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                            continue;
                        }

                        if (hero.getCount() <= 0) {
                            LogUtil.error("玩家将领兵力不足，不能创建Force, heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                            continue;
                        }

                        force = createForce(player, partnerHeroIdPb);
                        force.roleType = Constant.Role.PLAYER;
                        force.ownerId = player.roleId;
                        force.camp = player.lord.getCamp();
                        force.attrData.addValue(nightEffect);
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
        List<CommonPb.PartnerHeroIdPb> heroIdList;
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
                WallNpc wallNpc = player.wallNpc.get(map.getPartnerHeroId(0).getPrincipleHeroId());
                StaticWallHeroLv staticWallHeroLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(),
                        wallNpc.getLevel());
                force = createWallNpcForce(wallNpc, staticWallHeroLv);
                force.roleType = Constant.Role.WALL;
                force.ownerId = player.roleId;
                force.camp = player.lord.getCamp();
                force.attrData.addValue(nightEffect);
                fighter.addForce(force);
            } else {
                // 正常的hero
                heroIdList = map.getPartnerHeroIdList();
                if (!CheckNull.isEmpty(heroIdList)) {
                    StaticHero staticHero = null;
                    for (CommonPb.PartnerHeroIdPb partnerHeroIdPb : heroIdList) {
                        staticHero = StaticHeroDataMgr.getHeroMap().get(partnerHeroIdPb.getPrincipleHeroId());
                        if (null == staticHero) {
                            LogUtil.error("创建Fighter，heroId未配置,  heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                            continue;
                        }

                        hero = player.heros.get(partnerHeroIdPb.getPrincipleHeroId());
                        if (null == hero) {
                            LogUtil.error("玩家没有这个将领，不能创建Force,  heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                            continue;
                        }

                        if (hero.getCount() <= 0) {
                            LogUtil.error("玩家将领兵力不足，不能创建Force, heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                            continue;
                        }

                        force = createForce(player, partnerHeroIdPb);
                        force.roleType = Constant.Role.PLAYER;
                        force.ownerId = player.roleId;
                        force.camp = player.lord.getCamp();
                        force.attrData.addValue(nightEffect);
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
        Fighter fighter = createFighter();
        fighter.roleType = Constant.Role.PLAYER;

        Hero hero;
        Force force;
        List<CommonPb.PartnerHeroIdPb> heroIdList;
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
                WallNpc wallNpc = player.wallNpc.get(map.getPartnerHeroId(0).getPrincipleHeroId());
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
                heroIdList = map.getPartnerHeroIdList();
                if (!CheckNull.isEmpty(heroIdList)) {
                    StaticHero staticHero = null;
                    for (CommonPb.PartnerHeroIdPb partnerHeroIdPb : heroIdList) {
                        staticHero = StaticHeroDataMgr.getHeroMap().get(partnerHeroIdPb.getPrincipleHeroId());
                        if (null == staticHero) {
                            LogUtil.error("创建Fighter，heroId未配置, heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                            continue;
                        }

                        hero = player.heros.get(partnerHeroIdPb.getPrincipleHeroId());
                        if (null == hero) {
                            LogUtil.error("玩家没有这个将领，不能创建Force, heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                            continue;
                        }

                        if (hero.getCount() <= 0) {
                            LogUtil.error("玩家将领兵力不足，不能创建Force, heroId:", partnerHeroIdPb.getPrincipleHeroId(), ", roleId:", player.roleId);
                            continue;
                        }

                        force = createForce(player, partnerHeroIdPb);
                        force.roleType = Constant.Role.PLAYER;
                        force.ownerId = player.roleId;
                        force.camp = player.lord.getCamp();
                        force.attrData.addValue(nightEffect);
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
            PartnerHero partnerHero = player.getBattleHeroByHeroId(heroId);
            if (CheckNull.isNull(partnerHero)) continue;
            force = createPartnerForce(player, partnerHero, staticHero);
            force.roleType = Constant.Role.PLAYER;
            force.ownerId = player.roleId;
            force.camp = player.lord.getCamp();
            fighter.addForce(force);
            addMedalAuraSkill(fighter, hero, player);
        }
        fighter.roleType = Constant.Role.PLAYER;
        return fighter;
    }

    public Fighter createCombatPlayerFighterByPartnerHero(Player player, List<PartnerHero> partnerHeroList) {
        Fighter fighter = createFighter();
        StaticHero staticHero;
        Force force;
        for (PartnerHero partnerHero : partnerHeroList) {
            if (HeroUtil.isEmptyPartner(partnerHero)) {
                continue;
            }
            staticHero = StaticHeroDataMgr.getHeroMap().get(partnerHero.getPrincipalHero().getHeroId());
            if (null == staticHero) {
                LogUtil.error("创建Fighter，heroId未配置, heroId:", partnerHero.getPrincipalHero().getHeroId());
                continue;
            }
            force = createPartnerForce(player, partnerHero, staticHero);
            force.roleType = Constant.Role.PLAYER;
            force.ownerId = player.roleId;
            force.camp = player.lord.getCamp();
            fighter.addForce(force);
            addMedalAuraSkill(fighter, partnerHero.getPrincipalHero(), player);
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
            PartnerHero[] heroBattle = p.getPlayerFormation().getHeroBattle();
            for (int i = 1; i < heroBattle.length; i++) {
                PartnerHero partnerHero = heroBattle[i];
                if (HeroUtil.isEmptyPartner(partnerHero)) {
                    continue;
                }
                Hero hero = partnerHero.getPrincipalHero();
                StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(partnerHero.getPrincipalHero().getHeroId());
                if (CheckNull.isNull(staticHero)) {
                    LogUtil.error("创建Fighter，heroId未配置, heroId:", hero.getHeroId());
                    continue;
                }

                Force force = createPartnerForce(p, partnerHero, staticHero);
                if (CheckNull.isNull(force)) continue;

                force.roleType = Constant.Role.PLAYER;
                force.ownerId = p.roleId;
                force.camp = p.lord.getCamp();
                fighter.addForce(force);
                addMedalAuraSkill(fighter, hero, p);
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
        Fighter fighter = createFighter();
        StaticHero staticHero = null;
        Force force;
        for (int i = 0; i < player.getPlayerFormation().getHeroBattle().length; i++) {
            PartnerHero partnerHero = player.getBattleHeroByPos(i);
            if (HeroUtil.isEmptyPartner(partnerHero)) {
                continue;
            }

            Hero hero = partnerHero.getPrincipalHero();
            staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            if (null == staticHero) {
                LogUtil.error("创建Fighter，heroId未配置, heroId:", hero.getHeroId());
                continue;
            }
            force = createPartnerForce(player, partnerHero, staticHero);
            force.roleType = Constant.Role.PLAYER;
            force.ownerId = player.roleId;
            force.camp = player.lord.getCamp();
            fighter.addForce(force);
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
        List<List<Integer>> npcIdListList = staticBandit.getForm();
        if (null == npcIdListList) {
            return null;
        }

        Fighter fighter = createFighter();
        for (List<Integer> npcIdList : npcIdListList) {
            if (CheckNull.isEmpty(npcIdList)) continue;
            fighter.addForce(createNpcForce(npcIdList));
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
    public Fighter createNpcFighter(List<List<Integer>> npcIdList) {
        if (null == npcIdList) {
            return null;
        }
        Fighter fighter = createFighter();
        for (List<Integer> npcId : npcIdList) {
            fighter.addForce(createNpcForce(npcId));
        }
        fighter.roleType = Constant.Role.BANDIT;
        return fighter;
    }

    /**
     * 创建Force对象
     *
     * @param npcIdList
     * @return
     */
    public Force createNpcForce(List<Integer> npcIdList) {
        if (CheckNull.isEmpty(npcIdList)) return null;

        StaticNpc npc;
        AttrData attrData;
        Force force = null;
        for (int i = 0; i < npcIdList.size(); i++) {
            Integer npcId = npcIdList.get(0);
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            if (CheckNull.isNull(npc)) continue;
            attrData = new AttrData(npc.getAttr());
            List<SimpleHeroSkill> skillList = createFightSkillList(npc.getActiveSkills(),
                    npc.getOnStageSkills(), npc.getSkillLv());
            if (i == 0) {
                force = new Force(attrData, npc.getArmType(), npc.getLine(), npcId);
                if (CheckNull.nonEmpty(skillList))
                    force.skillList = skillList;
            } else {
                FightAssistantHero assistantHero = new FightAssistantHero(force, npcId, force.attrData.copy(), skillList);
                force.assistantHeroList.add(assistantHero);
                assistantHero.getAttrData().speed = npc.getSpeed();
            }
        }

        return force;
    }

    /**
     * 创建技能列表
     *
     * @param activeSkills
     * @param onStageSkills
     * @param skillLv
     * @return
     */
    public List<SimpleHeroSkill> createFightSkillList(List<Integer> activeSkills, List<Integer> onStageSkills, int skillLv) {
        List<SimpleHeroSkill> skillList = null;
        if (CheckNull.nonEmpty(activeSkills)) {
            skillList = new ArrayList<>();
            for (Integer skillId : activeSkills) {
                StaticHeroSkill staticConfig = StaticFightManager.getHeroSkill(skillId, skillLv);
                if (Objects.nonNull(staticConfig)) {
                    SimpleHeroSkill skill = new SimpleHeroSkill(staticConfig, false);
                    skillList.add(skill);
                }
            }
        }
        if (CheckNull.nonEmpty(onStageSkills)) {
            if (CheckNull.isNull(skillList)) skillList = new ArrayList<>();
            for (Integer skillId : onStageSkills) {
                StaticHeroSkill staticConfig = StaticFightManager.getHeroSkill(skillId, skillLv);
                if (Objects.nonNull(staticConfig)) {
                    SimpleHeroSkill skill = new SimpleHeroSkill(staticConfig, true);
                    skillList.add(skill);
                }
            }
        }

        return skillList;
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
        if (crossCityNpcChgAttrCnt > 0 && attr.containsKey(FightCommonConstant.AttrId.ATTACK)) {
            int attack = Optional.ofNullable(attr.get(FightCommonConstant.AttrId.ATTACK)).orElse(0);
            int add = (int) (attack * crossCityNpcChgAttrCnt * (WorldConstant.WORLD_WAR_CITY_EFFECT / Constant.TEN_THROUSAND));
            attrData.addValue(FightCommonConstant.AttrId.ATTACK, add);
            LogUtil.common("调整世界争霸city npc 攻击属性 npcId: ", npcId, " cur_attack: ", attack, " add_attack: ", add);
        }
        if (crossCityNpcChgAttrCnt > 0 && attr.containsKey(FightCommonConstant.AttrId.ATTACK_EXT)) {
            int attackExt = Optional.ofNullable(attr.get(FightCommonConstant.AttrId.ATTACK_EXT)).orElse(0);
            int add = (int) (attackExt * crossCityNpcChgAttrCnt * (WorldConstant.WORLD_WAR_CITY_EFFECT / Constant.TEN_THROUSAND));
            attrData.addValue(FightCommonConstant.AttrId.ATTACK_EXT, add);
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
     * 创建王朝遗迹战斗对象
     *
     * @param player
     * @param holdTime
     * @return
     */
    public Force createRelicForce(Player player, CommonPb.PartnerHeroIdPb partnerHero, long holdTime) {
        if (CheckNull.isNull(partnerHero))
            return null;

        Hero hero = player.heros.get(partnerHero.getPrincipleHeroId());
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (null == staticHero) {
            LogUtil.error("创建Fighter，heroId未配置, heroId:", partnerHero.getPrincipleHeroId());
            return null;
        }

        Map<Integer, Integer> attrMap = CalculateUtil.processAttr(player, hero);
        AttrData attrData = new AttrData(attrMap);
        // 王朝遗迹防守玩家deBuff
        if (holdTime > 0) {
            long nowMills = System.currentTimeMillis();
            long intervalTime = (nowMills - holdTime) / 1000l;
            int attrId, ratio;
            if (intervalTime > 0) {
                for (List<Integer> config : ActParamConstant.FATIGUE_DE_BUFF_PARAMETER) {
                    if (CheckNull.isEmpty(config)) continue;
                    ratio = 0;
                    attrId = config.get(0);
                    if (intervalTime < config.get(1))
                        continue;
                    ratio += config.get(3);
                    ratio += (intervalTime - config.get(1)) / config.get(2) * config.get(3);
                    ratio = Math.min(ratio, config.get(4));
                    attrData.addRatioValue(attrId, ratio * -1);
                }
            }
        }


        int line = calcHeroLine(player, hero, staticHero.getLine());
        int lead = (int) Math.ceil(hero.getAttr()[HeroConstant.ATTR_LEAD] * 1.0 / line);// 当兵力不能被整除时，向上取整
        int heroLv = techDataManager.getIntensifyLv4HeroType(player, staticHero.getType());// 等级
        int restrain = techDataManager.getIntensifyRestrain4HeroType(player, staticHero.getType());// 克制值
        Force force = new Force(attrData, staticHero.getType(), hero.getCount(), lead, hero.getHeroId(), player.roleId);

        // 添加战机详情
        addPlaneInfo(player, hero, force);

        //设置英雄战斗技能
        loadHeroSkill(force, hero);
        force.setIntensifyLv(heroLv);
        force.setEffect(restrain);

        if (CheckNull.nonEmpty(partnerHero.getDeputyHeroIdList())) {
            // 添加所有副将
            partnerHero.getDeputyHeroIdList().forEach(heroId -> {
                Hero hero_ = player.heros.get(heroId);
                if (CheckNull.isNull(hero_)) return;
                FightAssistantHero fightAssistantHero = new FightAssistantHero(force, hero_.getHeroId(), new AttrData(attrMap), getHeroSkill(hero_));
                Map<Integer, Integer> attrMap_ = CalculateUtil.processAttr(player, hero_);
                fightAssistantHero.getAttrData().speed = attrMap_.getOrDefault(FightCommonConstant.AttrId.SPEED, 0);
                force.assistantHeroList.add(fightAssistantHero);
            });
        }
        return force;
    }

    /**
     * 创建带有副将的force
     *
     * @param player
     * @param partnerHero
     * @return
     */
    public Force createPartnerForce(Player player, PartnerHero partnerHero, StaticHero staticHero) {
        Hero hero = partnerHero.getPrincipalHero();
        Map<Integer, Integer> attrMap = CalculateUtil.processAttr(player, hero);
        AttrData attrData = new AttrData(attrMap);
        int line = calcHeroLine(player, hero, staticHero.getLine());
        int lead = (int) Math.ceil(hero.getAttr()[HeroConstant.ATTR_LEAD] * 1.0 / line);// 当兵力不能被整除时，向上取整
        int heroLv = techDataManager.getIntensifyLv4HeroType(player, staticHero.getType());// 等级
        int restrain = techDataManager.getIntensifyRestrain4HeroType(player, staticHero.getType());// 克制值
        Force force = new Force(attrData, staticHero.getType(), hero.getAttr()[HeroConstant.ATTR_LEAD], lead,
                staticHero.getHeroId(), player.roleId);
        // 添加战机详情
        addPlaneInfo(player, hero, force);
        //设置英雄战斗技能
        loadHeroSkill(force, hero);
        force.setIntensifyLv(heroLv);
        force.setEffect(restrain);

        if (CheckNull.nonEmpty(partnerHero.getDeputyHeroList())) {
            // 添加所有副将
            partnerHero.getDeputyHeroList().forEach(hero_ -> {
                if (CheckNull.isNull(hero_)) return;
                FightAssistantHero fightAssistantHero = new FightAssistantHero(force, hero_.getHeroId(), new AttrData(attrMap), getHeroSkill(hero_));
                Map<Integer, Integer> attrMap_ = CalculateUtil.processAttr(player, hero_);
                fightAssistantHero.getAttrData().speed = attrMap_.getOrDefault(FightCommonConstant.AttrId.SPEED, 0);
                force.assistantHeroList.add(fightAssistantHero);
            });
        }

        return force;
    }


    /**
     * @param player
     * @return Force
     * @Title: createForce
     * @Description: 创建玩家 force
     */
    public Force createForce(Player player, CommonPb.PartnerHeroIdPb partnerHero) {
        if (CheckNull.isNull(partnerHero))
            return null;

        Hero hero = player.heros.get(partnerHero.getPrincipleHeroId());
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (null == staticHero) {
            LogUtil.error("创建Fighter，heroId未配置, heroId:", partnerHero.getPrincipleHeroId());
            return null;
        }

        Map<Integer, Integer> attrMap = CalculateUtil.processAttr(player, hero);
        AttrData attrData = new AttrData(attrMap);

        int line = calcHeroLine(player, hero, staticHero.getLine());
        int lead = (int) Math.ceil(hero.getAttr()[HeroConstant.ATTR_LEAD] * 1.0 / line);// 当兵力不能被整除时，向上取整
        int heroLv = techDataManager.getIntensifyLv4HeroType(player, staticHero.getType());// 等级
        int restrain = techDataManager.getIntensifyRestrain4HeroType(player, staticHero.getType());// 克制值
        Force force = new Force(attrData, staticHero.getType(), hero.getCount(), lead, hero.getHeroId(), player.roleId);

        // 添加战机详情
        addPlaneInfo(player, hero, force);

        //设置英雄战斗技能
        loadHeroSkill(force, hero);
        force.setIntensifyLv(heroLv);
        force.setEffect(restrain);

        if (CheckNull.nonEmpty(partnerHero.getDeputyHeroIdList())) {
            // 添加所有副将
            partnerHero.getDeputyHeroIdList().forEach(heroId -> {
                Hero hero_ = player.heros.get(heroId);
                if (CheckNull.isNull(hero_)) return;
                FightAssistantHero fightAssistantHero = new FightAssistantHero(force, hero_.getHeroId(), new AttrData(attrMap), getHeroSkill(hero_));
                Map<Integer, Integer> attrMap_ = CalculateUtil.processAttr(player, hero_);
                fightAssistantHero.getAttrData().speed = attrMap_.getOrDefault(FightCommonConstant.AttrId.SPEED, 0);
                force.assistantHeroList.add(fightAssistantHero);
            });
        }
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
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (CheckNull.isNull(staticHero))
            return;
        if (CheckNull.isEmpty(staticHero.getOnStageSkills()) && CheckNull.isEmpty(staticHero.getActiveSkills()))
            return;
        for (Integer skillGroupId : staticHero.getOnStageSkills()) {
            StaticHeroSkill staticHeroSkill = StaticFightManager.getHeroSkill(skillGroupId, 1);
            if (CheckNull.isNull(staticHeroSkill))
                continue;
            SimpleHeroSkill simpleHeroSkill = new SimpleHeroSkill(staticHeroSkill, true);
            force.getSkillList(hero.getHeroId()).add(simpleHeroSkill);
        }
        for (Integer skillGroupId : staticHero.getActiveSkills()) {
            StaticHeroUpgrade staticHeroUpgrade = StaticHeroDataMgr.getStaticHeroUpgrade(hero.getGradeKeyId());
            if (CheckNull.isNull(staticHeroUpgrade)) continue;
            StaticHeroSkill staticHeroSkill = StaticFightManager.getHeroSkill(skillGroupId, staticHeroUpgrade.getSkillLv());
            if (CheckNull.isNull(staticHeroSkill))
                continue;
            SimpleHeroSkill simpleHeroSkill = new SimpleHeroSkill(staticHeroSkill, false);
            force.getSkillList(hero.getHeroId()).add(simpleHeroSkill);
        }
    }

    /**
     * 获取当前武将所有技能
     *
     * @param hero
     * @return
     */
    private List<SimpleHeroSkill> getHeroSkill(Hero hero) {
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (CheckNull.isNull(staticHero))
            return null;
        if (CheckNull.isEmpty(staticHero.getOnStageSkills()) && CheckNull.isEmpty(staticHero.getActiveSkills()))
            return null;

        List<SimpleHeroSkill> skillList = new ArrayList<>(staticHero.getTotalSkillNum());
        for (Integer skillGroupId : staticHero.getOnStageSkills()) {
            StaticHeroSkill staticHeroSkill = StaticFightManager.getHeroSkill(skillGroupId, 1);
            if (CheckNull.isNull(staticHeroSkill))
                continue;
            SimpleHeroSkill simpleHeroSkill = new SimpleHeroSkill(staticHeroSkill, true);
            skillList.add(simpleHeroSkill);
        }
        for (Integer skillGroupId : staticHero.getActiveSkills()) {
            StaticHeroUpgrade staticHeroUpgrade = StaticHeroDataMgr.getStaticHeroUpgrade(hero.getGradeKeyId());
            if (CheckNull.isNull(staticHeroUpgrade)) continue;
            StaticHeroSkill staticHeroSkill = StaticFightManager.getHeroSkill(skillGroupId, staticHeroUpgrade.getSkillLv());
            if (CheckNull.isNull(staticHeroSkill))
                continue;
            SimpleHeroSkill simpleHeroSkill = new SimpleHeroSkill(staticHeroSkill, false);
            skillList.add(simpleHeroSkill);
        }

        return skillList;
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
//                        StaticFightSkill sFightSkill = StaticFightDataMgr
//                                .getFightSkillMapById(sMentorSkill.getSkill().getOrDefault(quality, 0));
//                        int battlePos = warPlane.getBattlePos(); // 战机在将领上的上阵位置
//                        List<FightSkill> skills = force.fightSkill.get(battlePos);
//                        if (CheckNull.isNull(skills)) {
//                            skills = new ArrayList<>();
//                            force.fightSkill.put(battlePos, skills);
//                        }
//                        if (!CheckNull.isNull(sFightSkill)) {
//                            PlaneFightSkill fightSkill = new PlaneFightSkill(sFightSkill);
//                            fightSkill.setPlaneId(planeId); // 记录战机id
//                            if (!CheckNull.isEmpty(sFightSkill.getAttackTime())) {
//                                List<Integer> randomByWeight = RandomUtil.getRandomByWeight(sFightSkill.getAttackTime(),
//                                        1, false);
//                                if (!CheckNull.isEmpty(randomByWeight)) { // 计算最大释放次数
//                                    int maxReleaseCnt = randomByWeight.get(0);
//                                    fightSkill.param.put(PlaneConstant.SkillParam.MAX_RELEASE_CNT, maxReleaseCnt);
//                                }
//                            }
//                            skills.add(fightSkill);
//                        }

                    }
                }
            }
//            force.planeInfos.put(warPlane.getBattlePos(), info);
        }
    }

    /**
     * @param player
     * @param staticHero
     * @param atkOrDef
     * @param addMode
     * @param addTime
     * @param camp
     * @return BerlinForce
     * @Title: createBerlinForce
     * @Description: 柏林会战 创建 玩家force
     */
    public BerlinForce createBerlinForce(Player player, StaticHero staticHero, CommonPb.PartnerHeroIdPb partnerHeroIdPb, int atkOrDef,
                                         int addMode, long addTime, int camp) {
        Hero hero = player.heros.get(partnerHeroIdPb.getPrincipleHeroId());
        Map<Integer, Integer> attrMap = CalculateUtil.processAttr(player, hero);
        int line = calcHeroLine(player, hero, staticHero.getLine());
        int lead = (int) Math.ceil(hero.getAttr()[HeroConstant.ATTR_LEAD] * 1.0 / line);// 当兵力不能被整除时，向上取整
        AttrData attrData = new AttrData(attrMap);
        // 查询玩家兵种等级 和 强化科技 额外加成的克制
        int heroLv = techDataManager.getIntensifyLv4HeroType(player, staticHero.getType());// 等级
        int restrain = techDataManager.getIntensifyRestrain4HeroType(player, staticHero.getType());// 克制值

        BerlinForce force = new BerlinForce(attrData, staticHero.getType(), partnerHeroIdPb.getCount(), lead, hero.getHeroId(), atkOrDef, addMode,
                addTime, camp, player.roleId, heroLv, restrain, 0);
        force.roleType = Constant.Role.PLAYER;
        // 添加战机详情
        addPlaneInfo(player, hero, force);
        //设置英雄战斗技能
        loadHeroSkill(force, hero);

        if (CheckNull.nonEmpty(partnerHeroIdPb.getDeputyHeroIdList())) {
            // 添加所有副将
            partnerHeroIdPb.getDeputyHeroIdList().forEach(heroId -> {
                Hero hero_ = player.heros.get(heroId);
                if (CheckNull.isNull(hero_)) return;
                FightAssistantHero fightAssistantHero = new FightAssistantHero(force, hero_.getHeroId(), new AttrData(attrMap), getHeroSkill(hero_));
                Map<Integer, Integer> attrMap_ = CalculateUtil.processAttr(player, hero_);
                fightAssistantHero.getAttrData().speed = attrMap_.getOrDefault(FightCommonConstant.AttrId.SPEED, 0);
                force.assistantHeroList.add(fightAssistantHero);
            });
        }
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
                case FightCommonConstant.AttrId.ATK_MUT:
                    Integer v = attrMap.get(FightCommonConstant.AttrId.ATTACK);
                    if (v == null) {
                        continue;
                    }
                    addAttrValue(attrMap, FightCommonConstant.AttrId.ATTACK, (int) (v * (kv.getValue() / Constant.TEN_THROUSAND)));
                    break;
                case FightCommonConstant.AttrId.DEF_MUT:
                    v = attrMap.get(FightCommonConstant.AttrId.DEFEND);
                    if (v == null) {
                        continue;
                    }
                    addAttrValue(attrMap, FightCommonConstant.AttrId.DEFEND, (int) (v * (kv.getValue() / Constant.TEN_THROUSAND)));
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
                    if (equipExtra != null && Arrays.binarySearch(FightCommonConstant.BASE_ATTRS, al.getA()) < 0) {
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
                        addAttrValue(attrMutMap, FightCommonConstant.AttrId.ATK_MUT, effect.getEffectVal());
                        break;
                    case EffectConstant.DEF_MUT:
                        addAttrValue(attrMutMap, FightCommonConstant.AttrId.DEF_MUT, effect.getEffectVal());
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
        BattlePb.BattleRoundPb record = fightLogic.generateRecord();
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
        CommonPb.RptHero rptHero = PbHelper.createRptHero(type, kill, 0, force, owner, lv, 0, lost);
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
        return PbHelper.createRptHero(force.roleType, force.killed, exploit, force, player.lord.getNick(), player.lord.getLevel(), 0, force.totalLost);
    }

    public NpcForce createCacheNpcForce(List<Integer> npcIdList) {
        if (CheckNull.isEmpty(npcIdList)) return null;
        StaticNpc staticNpc = StaticNpcDataMgr.getNpcMap().get(npcIdList.get(0));
        if (staticNpc != null) {
            int hp = staticNpc.getAttr().getOrDefault(FightCommonConstant.AttrId.LEAD, 1);
            return new NpcForce(staticNpc.getNpcId(), hp, 0, npcIdList.size() > 1 ? npcIdList.subList(1, npcIdList.size()) : null);
        }
        return null;
    }

    public CityHero createCityHero(List<Integer> npcIdList) {
        if (CheckNull.isEmpty(npcIdList)) return null;
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcIdList.get(0));
        if (CheckNull.isNull(npc)) return null;
        return new CityHero(npcIdList.get(0), npc.getTotalArm(), npcIdList.size() > 1 ? npcIdList.subList(1, npcIdList.size()) : null);
    }
}
