package com.gryphpoem.game.zw.rpc.comsumer;

import com.gryphpoem.cross.fight.common.fighter.CrossFighter;
import com.gryphpoem.cross.fight.common.fighter.FighterPlayer;
import com.gryphpoem.cross.fight.common.force.HeroForce;
import com.gryphpoem.cross.fight.common.force.WallNpcForce;
import com.gryphpoem.cross.fight.report.CrossFightRecord;
import com.gryphpoem.cross.fight.report.CrossFightReport;
import com.gryphpoem.cross.fight.service.RpcFightService;
import com.gryphpoem.cross.gameplay.player.common.CrossHero;
import com.gryphpoem.cross.gameplay.player.common.CrossSeasonTalent;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.WallNpc;
import com.gryphpoem.game.zw.resource.domain.s.StaticWallHeroLv;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.util.DtoParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-11-17 17:20
 */
@Service
public class RpcFighterConsumer {
    @Autowired
    private RpcFightService rpcFightService;
    @Autowired
    private PlayerDataManager playerDataManager;

    public BattlePb.BattleRoundPb parserPbRecord(CrossFightRecord record) {
        return null;
    }


    public CrossFightReport doCrossWarFireFight(Player attacker, Army atkArmy, List<Army> defArmyList) {
        try {
            CrossFighter attackFighter = createFighter(attacker, atkArmy.getHero());
            CrossFighter defenceFighter = createFighter(defArmyList);
            CompletableFuture<CrossFightReport> future = rpcFightService.doFight(attackFighter, defenceFighter, WorldConstant.BATTLE_TYPE_CAMP);
            return future.get();
        } catch (Exception e) {
            LogUtil.error("", e);
        }
        return null;
    }

    /**
     * 打人
     *
     * @param battle
     * @return
     */
    public CrossFightReport doCrossWarFireFight(Battle battle) {
        try {
            //进攻方
            CrossFighter attacker = CrossFighter(battle.getAtkList());
            //防守方
            CrossFighter defence = CrossFighter(battle.getDefList());
            CompletableFuture<CrossFightReport> future = rpcFightService.doFight(attacker, defence, WorldConstant.BATTLE_TYPE_CITY);
            return future.get();
        } catch (Exception e) {
            LogUtil.error("", e);
        }
        return null;
    }

    private CrossFighter CrossFighter(List<CommonPb.BattleRole> battleRoleList) {
        CrossFighter fighter = new CrossFighter();
        Map<Long, Object> talentMap = new HashMap<>();
        for (CommonPb.BattleRole battleRole : battleRoleList) {
            if (battleRole.getKeyId() == WorldConstant.ARMY_TYPE_WALL_NPC) {
                WallNpcForce wallNpcForce = createWallNpcForce(battleRole);
                fighter.getForces().add(wallNpcForce);
            } else {
                List<HeroForce> heroForceList = createHeroForceList(battleRole);
                fighter.getForces().addAll(heroForceList);
            }
            long lordId = battleRole.getRoleId();
            Player player = playerDataManager.getPlayer(lordId);
            FighterPlayer fighterPlayer = fighter.getPlayerMap().computeIfAbsent(lordId, k -> new FighterPlayer());
            fighterPlayer.setCamp(player.lord.getCamp());
            fighterPlayer.setNick(player.lord.getNick());
            if (!talentMap.containsKey(lordId)) {
                CrossSeasonTalent talent = DtoParser.buildCrossFightSeasonTalent(player);
                if (Objects.nonNull(talent)) {
                    fighterPlayer.setTalent(talent);
                }
                talentMap.put(lordId, new Object());
            }
        }
        fighter.setRoleType(Constant.Role.PLAYER);
        return fighter;
    }

    private List<HeroForce> createHeroForceList(CommonPb.BattleRole battleRole) {
        long lordId = battleRole.getRoleId();
        Player player = playerDataManager.getPlayer(lordId);
        List<HeroForce> forceList = new ArrayList<>();
        for (Integer heroId : battleRole.getHeroIdList()) {
            Hero hero = player.heros.get(heroId);
            TwoInt.Builder twoInt = TwoInt.newBuilder();
            twoInt.setV1(heroId);
            twoInt.setV2(hero.getCount());
            HeroForce heroForce = createCrossFightForce(player, twoInt.build());
            forceList.add(heroForce);
        }
        return forceList;
    }

    private WallNpcForce createWallNpcForce(CommonPb.BattleRole battleRole) {
        long lordId = battleRole.getRoleId();
        Player player = playerDataManager.getPlayer(lordId);
        WallNpc wallNpc = player.wallNpc.get(battleRole.getPartnerHeroId(0).getPrincipleHeroId());
        WallNpcForce force = new WallNpcForce();
        force.setForceType(Constant.Role.WALL);
        StaticWallHeroLv wallHeroLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());
        force.setForceId(wallHeroLv.getId());
        force.setHp(wallNpc.getCount());
        force.setPos(wallNpc.getId());
        return force;
    }

    public CrossFighter createFighter(Player player, List<TwoInt> form) {
        CrossFighter fighter = new CrossFighter();
        for (TwoInt twoInt : form) {
            HeroForce heroForce = createCrossFightForce(player, twoInt);
            fighter.getForces().add(heroForce);
        }

        FighterPlayer fighterPlayer = new FighterPlayer();
        fighterPlayer.setCamp(player.lord.getCamp());
        fighterPlayer.setNick(player.lord.getNick());
        CrossSeasonTalent talent = DtoParser.buildCrossFightSeasonTalent(player);
        if (Objects.nonNull(talent)) {
            fighterPlayer.setTalent(talent);
        }
        fighter.getPlayerMap().put(player.getLordId(), fighterPlayer);
        fighter.setRoleType(Constant.Role.PLAYER);
        return fighter;
    }


    private HeroForce createCrossFightForce(Player player, TwoInt twoInt) {
        Hero hero = player.heros.get(twoInt.getV1());
        CrossHero crossHero = DtoParser.buildCrossFightHero(player, hero);
        HeroForce force = new HeroForce();
        force.setLordId(crossHero.getLordId());
        force.setForceId(crossHero.getHeroId());
        force.setAttrMap(crossHero.getAttrMap());
        force.setIntensifyLv(crossHero.getIntensifyLv());
        force.setHp(twoInt.getV2());
        force.setLead(crossHero.getLead());
        force.setMaxLine(crossHero.getMaxLine());
        force.setMedal(crossHero.getMedal());
        force.setSkillAction(crossHero.getSkillAction());
        force.setForceType(Constant.Role.PLAYER);
        return force;
    }


    /**
     * 根据部队创建fighter
     *
     * @param armyList
     * @return
     */
    public CrossFighter createFighter(List<Army> armyList) {
        CrossFighter fighter = new CrossFighter();
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Map<Long, FighterPlayer> playerMap = fighter.getPlayerMap();
        for (Army army : armyList) {
            Player player = playerDataManager.getPlayer(army.getLordId());
            for (TwoInt twoInt : army.getHero()) {
                HeroForce heroForce = createCrossFightForce(player, twoInt);
                fighter.getForces().add(heroForce);
                FighterPlayer fighterPlayer = playerMap.computeIfAbsent(player.getLordId(), t -> new FighterPlayer());
                fighterPlayer.setCamp(player.lord.getCamp());
                fighterPlayer.setNick(player.lord.getNick());
                if (Objects.isNull(fighterPlayer.getTalent())) {
                    fighterPlayer.setTalent(DtoParser.buildCrossFightSeasonTalent(player));
                }
            }
        }
        fighter.setRoleType(Constant.Role.PLAYER);
        return fighter;
    }
}
