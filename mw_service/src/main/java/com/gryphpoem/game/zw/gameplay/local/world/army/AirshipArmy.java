package com.gryphpoem.game.zw.gameplay.local.world.army;

import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.RetreatArmyParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.map.AirshipMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.MedalDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.pojo.p.NpcForce;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticAirship;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.world.AirshipWorldData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.MarchService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.WorldService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName AirshipArmy.java
 * @Description 攻打飞艇部队
 * @date 2019年4月23日
 */
public class AirshipArmy extends BaseArmy {

    public AirshipArmy(Army army) {
        super(army);
    }

    @Override
    protected void marchEnd(MapMarch mapMarchArmy, int now) {
        int pos = getTargetPos();
        CrossWorldMap cMap = mapMarchArmy.getCrossWorldMap();
        BaseWorldEntity baseEntity = cMap.getAllMap().get(pos);
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        Player player = checkAndGetAmryHasPlayer(mapMarchArmy);
        if (baseEntity == null || baseEntity.getType() != WorldEntityType.AIRSHIP) {
            Turple<Integer, Integer> xy = cMap.posToTurple(pos);
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, now, xy.getA(), xy.getB(),
                    xy.getA(), xy.getB());
            normalRetreatArmy(mapMarchArmy);
            mapMarchArmy.getCrossWorldMap().publishMapEvent(createMapEvent(MapCurdEvent.UPDATE));
            return;
        }
        AirshipMapEntity airshipMapEntity = (AirshipMapEntity) baseEntity;
        AirshipWorldData airship = airshipMapEntity.getAirshipWorldData();
        List<BattleRole> battleRoles = airship.getJoinRoles().computeIfAbsent(player.lord.getCamp(),
                k -> new ArrayList<>());
        // 加入玩家的信息
        battleRoles.add(CommonPb.BattleRole.newBuilder().setKeyId(army.getKeyId()).setRoleId(player.roleId)
                .addAllPartnerHeroId(getArmy().getHero()).build());
        if (battleRoles.stream().mapToLong(role -> role.getRoleId()).distinct()
                .count() >= Constant.AIRSHIP_JOIN_MEMBER_CNT) { // 达成条件, 开启战斗
            fightAirShip(cMap, airshipMapEntity, battleRoles, player.lord.getCamp());
            return;
        }
        WorldService worldService = DataResource.ac.getBean(WorldService.class);
        setArmyPlayerHeroState(mapMarchArmy, ArmyConstant.ARMY_STATE_ATTACK_AIRSHIP_WAIT);
        getArmy().setState(ArmyConstant.ARMY_STATE_ATTACK_AIRSHIP_WAIT);
        worldService.synRetreatArmy(player, army, now);
    }

    @Override
    public void retreat(RetreatArmyParamDto param) {

        if (army.getState() == ArmyConstant.ARMY_STATE_ATTACK_AIRSHIP_WAIT) {
            CrossWorldMap cMap = param.getCrossWorldMap();
            BaseWorldEntity baseEntity = cMap.getAllMap().get(getTargetPos());
            if (baseEntity != null && baseEntity.getType() == WorldEntityType.AIRSHIP) {
                Player player = checkAndGetAmryHasPlayer(cMap.getMapMarchArmy());
                AirshipMapEntity airshipMapEntity = (AirshipMapEntity) baseEntity;
                List<BattleRole> roleList = airshipMapEntity.getAirshipWorldData().getJoinRoles()
                        .get(player.lord.getCamp());
                if (!CheckNull.isEmpty(roleList)) {
                    for (Iterator<BattleRole> it = roleList.iterator(); it.hasNext(); ) {
                        BattleRole rb = it.next();
                        if (rb.getRoleId() == player.roleId && rb.getKeyId() == army.getKeyId()) {
                            it.remove();
                        }
                    }
                }
            }
        }
        super.retreat(param);
    }

    private void fightAirShip(CrossWorldMap cMap, AirshipMapEntity airshipMapEntity, List<BattleRole> battleRoles,
                              int camp) {
        int now = TimeHelper.getCurrentSecond();
        AirshipWorldData airShip = airshipMapEntity.getAirshipWorldData();
        int airShipId = airShip.getId();
        int airShipPos = airShip.getPos();
        int areaId = airShip.getAreaId();
        Turple<Integer, Integer> defPos = cMap.posToTurple(airShipPos);

        FightService fightService = DataResource.ac.getBean(FightService.class);
        WarService warService = DataResource.ac.getBean(WarService.class);
        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        MarchService marchService = DataResource.ac.getBean(MarchService.class);
        WorldService worldService = DataResource.ac.getBean(WorldService.class);
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);

        // 战斗计算
        Fighter attacker = fightService.createFighterByBattleRole(battleRoles);
        Fighter defender = fightService.createFighter(airShip.getNpc());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.start();

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker, false, true);

        boolean atkSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS;
        // 需要返回的玩家
        Set<Long> retreatPlayers;
        // 兵力恢复
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        Map<Long, List<CommonPb.Award>> dropMap = new HashMap<>();

        // 攻击方损兵处理
        if (attacker.lost > 0) {
            warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.AIR_SHIP_BATTLE);
        }

        // 执行勋章白衣天使特技逻辑
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        //执行赛季天赋技能---伤病恢复
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
        // 执行勋章-以战养战特技逻辑
        medalDataManager.sustainTheWarByMeansOfWar(attacker, defender, recoverArmyAwardMap, atkSuccess);

        // Report战报
        Player firstAttackPlayer = playerDataManager.getPlayer(battleRoles.get(0).getRoleId());
        CommonPb.RptAtkPlayer.Builder rpt = marchService.createAirShipRptBuilder(camp, attacker, defender, fightLogic,
                atkSuccess, firstAttackPlayer, airShipId, airShipPos);
        CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);

        StaticAirship sAirShip = StaticWorldDataMgr.getAirshipMap().get(airShipId);

        if (atkSuccess) { // 飞艇被击杀
            // 协助者奖励
            battleRoles.stream().filter(role -> role.getRoleId() != airShip.getBelongRoleId()) // 归属者不能获取协助者奖励
                    .mapToLong(role -> role.getRoleId()).distinct()
                    .mapToObj(roleId -> playerDataManager.getPlayer(roleId)).filter(p -> !CheckNull.isNull(p))
                    .forEach(p -> {
                        if (p.getAndCreateAirshipPersonData().getAttendAwardCnt() > 0) {
                            p.getAndCreateAirshipPersonData().subAttendAwardCnt(1);
                            List<CommonPb.Award> awards = rewardDataManager.sendReward(p, sAirShip.getAwardRegular(),
                                    AwardFrom.AIR_SHIP_BATTLE_AWARD);
                            if (CheckNull.isEmpty(awards)) return;
                            List<CommonPb.Award> drops = dropMap.get(p.roleId);
                            if (CheckNull.isNull(drops)) {
                                drops = new ArrayList<>();
                                dropMap.put(p.roleId, drops);
                            }
                            drops.addAll(awards);
                        } else {
                            mailDataManager.sendNormalMail(p, MailConstant.MOLD_AIR_SHIP_HELP_AWARD_MAX, now);
                        }
                    });

            // 飞艇击飞所有玩家都返回
            retreatPlayers = airShip.getJoinRoles().values().stream().flatMap(roles -> roles.stream())
                    .map(role -> role.getRoleId()).collect(Collectors.toSet());
            // 地图上移除飞艇
            cMap.getMapEntityGenerator().removeAirshipFromMap(airShip, now, AirshipWorldData.STATUS_DEAD_REFRESH);
        } else {
            // 防守方损兵处理
            if (defender.lost > 0) {
                airShip.getNpc().clear();
                for (Force force : defender.forces) {
                    if (force.alive()) {
                        airShip.getNpc().add(new NpcForce(force.id, force.hp, force.curLine));
                    }
                }
            }
            // 移除当前阵营
            retreatPlayers = airShip.getJoinRoles().remove(camp).stream().map(role -> role.getRoleId())
                    .collect(Collectors.toSet());
        }
        // 需要返回的Player
        retreatPlayers.forEach(roleId -> {
            Player p = playerDataManager.getPlayer(roleId);
            if (CheckNull.isNull(p)) {
                return;
            }
            PlayerArmy playerArmy = cMap.getMapMarchArmy().getPlayerArmyMap().get(roleId);
            if (playerArmy == null) {
                return;
            }
            for (BaseArmy baseArmy : playerArmy.getArmy().values()) {
                if (baseArmy.getArmy().getType() == ArmyConstant.ARMY_TYPE_ATTACK_AIRSHIP
                        && baseArmy.getArmy().getTarget() == airShipPos
                        && baseArmy.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
                    baseArmy.normalRetreatArmy(cMap.getMapMarchArmy());
                    cMap.publishMapEvent(baseArmy.createMapEvent(MapCurdEvent.UPDATE));
                }
            }
            // 如果是参战人员, 发送战报
            if (battleRoles.stream().filter(battleRole -> battleRole.getRoleId() == p.roleId).findFirst().isPresent()) {
                Turple<Integer, Integer> atkPos = cMap.posToTurple(p.lord.getPos());
                mailDataManager.sendReportMail(p, report,
                        atkSuccess ? MailConstant.MOLD_AIR_SHIP_BATTLE_SUC : MailConstant.MOLD_AIR_SHIP_BATTLE_FAIL,
                        dropMap.get(p.roleId), now, recoverArmyAwardMap, p.lord.getNick(), airShipId, p.lord.getNick(),
                        atkPos.getA(), atkPos.getB(), airShipId, defPos.getA(), defPos.getB());
            } else {
                if (atkSuccess) { // 发送飞艇被击毁
                    mailDataManager.sendNormalMail(p, MailConstant.MOLD_AIR_SHIP_DEAD, now, airShipId, airShipId);
                }
            }
        });
        marchService.logAirShipBattle(areaId, battleRoles, atkSuccess, airShip.getKeyId() + "_" + airShipId, airShipPos, attacker, firstAttackPlayer, rpt.getAtkHeroList(), String.valueOf(airShipId));
    }

}
