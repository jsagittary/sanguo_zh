package com.gryphpoem.game.zw.gameplay.local.world.battle;

import com.gryphpoem.cross.fight.report.CrossFightReport;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyAttackTaskService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticActBandit;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.gryphpoem.game.zw.rpc.comsumer.RpcFighterConsumer;
import com.gryphpoem.game.zw.service.*;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName AttackPlayerBattle.java
 * @Description 打玩家的battle
 * @author QiuKun
 * @date 2019年3月23日
 */
public class AttackPlayerBattle extends AbsCommonBattle {

    public AttackPlayerBattle(Battle battle) {
        super(battle);
    }

    @Override
    public void doFight(int now, MapWarData mapWarData) {
        int pos = getBattle().getPos();
        // 不是玩家
        BaseWorldEntity baseWorldEntity = mapWarData.getCrossWorldMap().getAllMap().get(pos);
        if (baseWorldEntity == null || baseWorldEntity.getType() != WorldEntityType.PLAYER) {
            // 取消战斗
            cancelBattleAndReturnArmy(mapWarData, CancelBattleType.UNKNOW);
            return;
        }
        // 保护罩检测
        Player defPlayer = battle.getDefencer();
        if (defPlayer == null) return;
        Effect effect = battle.getDefencer().getEffect().get(EffectConstant.PROTECT);
        if (effect != null && effect.getEndTime() > now) {
            // 取消战斗
            cancelBattleAndReturnArmy(mapWarData, CancelBattleType.UNKNOW);
            return;
        }

        FightService fightService = DataResource.ac.getBean(FightService.class);
        WarDataManager warDataManager = DataResource.ac.getBean(WarDataManager.class);
        WorldWarSeasonDailyAttackTaskService dailyAttackTaskService = DataResource.ac
                .getBean(WorldWarSeasonDailyAttackTaskService.class);
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);
        addDefendRoleHeros(mapWarData);
        Fighter attacker = fightService.createMultiPlayerFighter(battle, null);
        Fighter defender = fightService.createCampBattleDefencer(battle, null);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.fight();// 战斗逻辑处理方法

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker,true,true);
        ActivityDiaoChanService.killedAndDeathTask0(defender,true,true);

        // 结果处理
        boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // 损兵处理
        WarService warService = DataResource.ac.getBean(WarService.class);
        if (attacker.lost > 0) {
            warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.CITY_BATTLE_ATTACK);
            // 计算玩家每日杀敌数量
            dailyAttackTaskService.addPlayerDailyAttackOther(attacker.forces);
        }
        if (defender.lost > 0) {
            warService.subBattleHeroArm(defender.forces, changeMap, AwardFrom.CITY_BATTLE_DEFEND);
            // 计算玩家每日杀敌数量
            dailyAttackTaskService.addPlayerDailyAttackOther(defender.forces);
        }

        // 兵力恢复
        Map<Long, List<Award>> recoverArmyAwardMap = new HashMap<>();
        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);
        // 执行勋章白衣天使特技逻辑
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
        //执行赛季天赋技能---伤病恢复
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
        seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);

        // 执行勋章-以战养战特技逻辑
        medalDataManager.sustainTheWarByMeansOfWar(attacker, defender, recoverArmyAwardMap, atkSuccess);
        // buff回血,只对防守者有效
        mapWarData.getCrossWorldMap().rebelBuffRecoverArmy(attacker, defender, recoverArmyAwardMap);

        // 战报生成
        SolarTermsDataManager solarTermsDataManager = DataResource.ac.getBean(SolarTermsDataManager.class);
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        CommonPb.Record record = fightLogic.generateRecord();
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null); // 节气
        rpt.setResult(atkSuccess);
        rpt.setRecord(record);

        Player atkPlayer = battle.getSponsor();
        Lord atkLord = atkPlayer.lord;
        rpt.setAttack(PbHelper.createRptMan(atkLord.getPos(), atkLord.getNick(), atkLord.getVip(), atkLord.getLevel()));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkLord.getCamp(), atkLord.getNick(),
                atkLord.getPortrait(), atkPlayer.getDressUp().getCurPortraitFrame()));

        Lord defLord = defPlayer.lord;
        rpt.setDefMan(PbHelper.createRptMan(defLord.getPos(), defLord.getNick(), defLord.getVip(), defLord.getLevel()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defLord.getCamp(), defLord.getNick(),
                defLord.getPortrait(), defPlayer.getDressUp().getCurPortraitFrame()));

        List<CommonPb.Award> loseList = new ArrayList<>();
        List<CommonPb.Award> dropList = new ArrayList<>(); // 掉落

        BuildingDataManager buildingDataManager = DataResource.ac.getBean(BuildingDataManager.class);
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        TaskDataManager taskDataManager = DataResource.ac.getBean(TaskDataManager.class);
        BattlePassDataManager battlePassDataManager = DataResource.ac.getBean(BattlePassDataManager.class);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        ActivityDataManager activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);
        HonorDailyService honorDailyService = DataResource.ac.getBean(HonorDailyService.class);
        CampDataManager campDataManager = DataResource.ac.getBean(CampDataManager.class);
        CampService campService = DataResource.ac.getBean(CampService.class);
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        WorldWarSeasonDailyRestrictTaskService restrictTaskService = DataResource.ac
                .getBean(WorldWarSeasonDailyRestrictTaskService.class);
        MapMarch mapMarchArmy = mapWarData.getCrossWorldMap().getMapMarchArmy();
        if (atkSuccess) {
            try {
                // 不发放保护罩
                dropList.addAll(buildingDataManager.dropList4War(atkPlayer, battle.getDefencer(), loseList, false));
                // 击飞获得战机碎片奖励
                Award award = warService.checkPlaneChipAward(atkPlayer);
                if (!CheckNull.isNull(award)) {
                    dropList.add(award);
                }
                // 击杀玩家掉落活动
                List<Award> actHitDrop = activityDataManager.getActHitDrop(atkPlayer, 0,
                        StaticActBandit.ACT_HIT_DROP_TYPE_1);
                if (!CheckNull.isEmpty(actHitDrop)) {
                    dropList.addAll(actHitDrop);
                }
            } catch (MwException e) {
                LogUtil.error(e);
            }
            // 执行勋章-维和部队 特技逻辑
            medalDataManager.peacekeepingForces(defender, defPlayer);
            // 固定给胜利方玩家角色加10点经验
            dropList.add(Award.newBuilder().setType(AwardType.MONEY).setId(AwardType.Money.EXP).setCount(10).build());
            // 发送奖励给发起攻击的玩家
            rewardDataManager.sendRewardByAwardList(atkPlayer, dropList, AwardFrom.CITY_BATTLE_ATTACK);
            // 记录攻方战斗次数 成功才记录
            warService.recordPartyBattle(battle, battle.getType(), battle.getAtkCamp(), true);

            // 击飞玩家,玩家迁城
            playerHitFly(mapWarData.getCrossWorldMap(), defPlayer, battle.getType(), atkPlayer);
            mailDataManager.sendNormalMail(battle.getDefencer(), MailConstant.MOLD_HIT_FLY,
                    TimeHelper.getCurrentSecond(), atkPlayer.lord.getNick(), atkPlayer.lord.getNick());
            // 驻防部队的撤回
            mapMarchArmy.retreatGuardArmy(defLord.getPos(), true);

            // 取消改点的其他战斗
            List<Integer> cancelBattleIds = mapWarData.getBattlePosCache().get(pos).stream()
                    .filter(battlId -> battlId.intValue() != getBattleId()).collect(Collectors.toList());
            if (!CheckNull.isEmpty(cancelBattleIds)) {
                cancelBattleIds.stream().map(battleId -> mapWarData.getAllBattles().get(battleId))
                        .filter(b -> b != null).forEach(b -> {
                            b.cancelBattleAndReturnArmy(mapWarData, CancelBattleType.DEF_HITFLY);
                        });
            }
            taskDataManager.updTask(atkPlayer, TaskType.COND_BATTLE_CITY_LV_CNT, 1);
            activityDataManager.updDay7ActSchedule(atkPlayer, ActivityConst.ACT_TASK_ATK,
                    battle.getDefencer().building.getCommand());

            //貂蝉任务-击飞玩家主城
            ActivityDiaoChanService.completeTask(atkPlayer, ETask.HITFLY_PLAYER);
            TaskService.processTask(atkPlayer, ETask.HITFLY_PLAYER);

            // 参与击飞
            for (long roles : battle.getAtkRoles()) {
                Player actPlayer = playerDataManager.getPlayer(roles);
                if (actPlayer != null) {
                    activityDataManager.updDay7ActSchedule(atkPlayer, ActivityConst.ACT_TASK_JOIN_OR_ATK,
                            battle.getDefencer().building.getCommand());
                    activityDataManager.updAtkCityActSchedule(atkPlayer, ActivityConst.ACT_TASK_JOIN_OR_ATK,
                            battle.getDefencer().building.getCommand());
                }
            }
            // 杀敌数++
            atkPlayer.common.incrKillNum();
            // 活动排行榜
            activityDataManager.updRankActivity(atkPlayer, ActivityConst.ACT_CITY_BATTLE_RANK, 1);
            for (Long atkRole : getBattle().getAtkRoles()) {
                // 记录玩家被击飞
                restrictTaskService.updatePlayerDailyRestrictTask(playerDataManager.getPlayer(atkRole),
                        TaskType.COND_BATTLE_CITY_LV_CNT, 1);
            }
        } else {
            // 驻防部队的撤回
            mapMarchArmy.retreatGuardArmy(defLord.getPos(), false);
        }

        // 给将领加经验=（杀敌数+损兵数）/2，并记录双方将领信息, 和军工奖励
        HashMap<Long, Map<Integer, Integer>> exploitAwardMap = new HashMap<>();
        warService.addBattleHeroExp(attacker.forces, AwardFrom.CITY_BATTLE_ATTACK, rpt, true, true,
                battle.isCityBattle(), changeMap, true, exploitAwardMap);
        warService.addBattleHeroExp(defender.forces, AwardFrom.CITY_BATTLE_DEFEND, rpt, false, true,
                battle.isCityBattle(), changeMap, true, exploitAwardMap);

        CommonPb.Report.Builder report = CommonPb.Report.newBuilder();
        report.setTime(now);
        report.setRptPlayer(rpt);

        taskDataManager.updTask(atkPlayer, TaskType.COND_ATTCK_PLAYER_CNT, 1, defLord.getLevel());
        // 检测并添加荣耀日报
        honorDailyService.addAndCheckHonorReports(atkPlayer, battle.getDefencer(), atkSuccess, battle.getType());
        battle.getAtkList().stream().mapToLong(role -> role.getRoleId()).distinct().forEach(roleId -> {
            Player actPlayer = playerDataManager.getPlayer(roleId);
            activityDataManager.updAtkCityActSchedule(actPlayer, ActivityConst.ACT_TASK_ATK_AND_JOIN, // 参与攻打玩家
                    battle.getDefencer().building.getCommand());
        });
        battle.getDefList().stream().mapToLong(role -> role.getRoleId()).distinct().forEach(roleId -> {
            Player defPalyer = playerDataManager.getPlayer(roleId);
            if (!battle.getDefencer().roleId.equals(defPalyer.roleId)) {
                activityDataManager.updAtkCityActSchedule(defPalyer, ActivityConst.ACT_TASK_JOIN_OR_DEF, // 参与防守玩家
                        battle.getDefencer().building.getCommand());
            }
        });
        // 军团礼包消息推送
        Optional.of(campDataManager.getParty(battle.getAtkCamp()))
                .ifPresent((party) -> campService.checkHonorRewardAndSendSysChat(party));
        CrossWorldMap cmap = mapWarData.getCrossWorldMap();
        int cityId = 0;
        Turple<Integer, Integer> atkPos = cmap.posToTurple(atkLord.getPos());
        Turple<Integer, Integer> defPos = cmap.posToTurple(battle.getPos());
        warService.sendCityBattleMail(battle, cityId, atkLord, defLord, atkPos, defPos, atkSuccess, report, dropList,
                loseList, now, recoverArmyAwardMap);
        // 防守方触发自动补兵
        warService.autoFillArmy(battle.getDefencer());
        // 通知客户端玩家资源变化
        warService.sendRoleResChange(changeMap);
        // 战斗打日志
//        int heroid = report.getRptPlayer().getAtkHero(0).getHeroId();
        warService.logBattle(battle, fightLogic.getWinState(),attacker,defender, rpt.getAtkHeroList(), rpt.getDefHeroList());
        // 推送改点的信息
        mapWarData.getCrossWorldMap().publishMapEvent(MapEvent.mapEntity(battle.getPos(), MapCurdEvent.UPDATE));
        //战火燎原进攻玩家主城的战斗结束
        cmap.getGlobalWarFire().attackPlayerBattleFinish(attacker, defender, battle, baseWorldEntity);
    }

    /**
     * 玩家被击飞
     *  @param cMap
     * @param player
     * @param battleType
     * @param atkPlayer
     */
    private void playerHitFly(CrossWorldMap cMap, Player player, int battleType, Player atkPlayer) {
        if (player.lord.getArea() != cMap.getMapId()) {
            return;
        }
        int prePos = player.lord.getPos();
        int newPos = cMap.getRandomOpenEmptyPosSafeArea(player.lord.getCamp());
        BaseWorldEntity playerEntity = cMap.removeWorldEntity(prePos);
        playerEntity.setPos(newPos);
        cMap.addWorldEntity(playerEntity);
        player.lord.setPos(newPos);

        // 城墙建起之后才会有失火状态
        if (battleType != WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE
                && BuildingDataManager.getBuildingLv(BuildingType.WALL, player) > 0) {
            player.setFireState(true);
        }

        ActivityDataManager activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);
        WarService warService = DataResource.ac.getBean(WarService.class);
        BuildingDataManager buildingDataManager = DataResource.ac.getBean(BuildingDataManager.class);
        // 更新触发式礼包进度
        activityDataManager.updateTriggerStatus(ActivityConst.TRIGGER_GIFT_REBUILD, player, 1);
        // 通知玩家被击飞迁城
        warService.syncRoleMove(player, newPos);
        // 重建家园
        buildingDataManager.SyncRebuild(player, atkPlayer);
        // 地图推送
        cMap.publishMapEvent(MapEvent.mapEntity(prePos, MapCurdEvent.DELETE),
                MapEvent.mapEntity(newPos, MapCurdEvent.CREATE));
    }

    @Override
    protected void onCancelBattleAfter(MapWarData mapWarData, CancelBattleType cancelType) {
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);

        String atkNick = battle.getSponsor() != null ? battle.getSponsor().lord.getNick() : "";
        int defCamp = battle.getDefCamp();
        String defNick = battle.getDefencer() != null ? battle.getDefencer().lord.getNick() : "";
        CrossWorldMap crossWorldMap = mapWarData.getCrossWorldMap();
        int[] xy = crossWorldMap.posToXy(getBattle().getPos());
        int x = xy != null ? xy[0] : 0;
        int y = xy != null ? xy[1] : 0;
        int now = TimeHelper.getCurrentSecond();
        if (cancelType == CancelBattleType.ATKCANCEL) {
            for (Player player : getPlayerByRoleId(getBattle().getAtkRoles())) {
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_ATK_RETREAT_ATK, now, atkNick, defCamp,
                        defNick, x, y, atkNick, defCamp, defNick, x, y);
            }
            for (Player player : getPlayerByRoleId(getBattle().getDefRoles())) {
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_ATK_RETREAT_DEF, now, atkNick, defCamp,
                        defNick, x, y, atkNick, defCamp, defNick, x, y);
            }
            if (battle.getDefencer() != null) {// 通知防守方
                mailDataManager.sendNormalMail(battle.getDefencer(), MailConstant.MOLD_CITY_ATK_RETREAT_DEF, now,
                        atkNick, defCamp, defNick, x, y, atkNick, defCamp, defNick, x, y);
            }
        } else if (cancelType == CancelBattleType.DEFMOVECITY) {// 被攻击玩家迁城
            for (Player player : getPlayerByRoleId(getBattle().getAtkRoles())) {
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_DEF_FLEE_ATK, now, defNick, defNick);
            }
            for (Player player : getPlayerByRoleId(getBattle().getDefRoles())) {
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_DEF_FLEE_DEF, now, defNick, defNick);
            }
        } else if (cancelType == CancelBattleType.DEF_HITFLY) { // 玩家被击飞
            for (Player player : getPlayerByRoleId(getBattle().getAtkRoles())) {
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_ATTACK_TARGET_FLY, now, defNick, defNick);
            }
        }
    }

    private static List<Player> getPlayerByRoleId(Collection<Long> roleIds) {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        return roleIds.stream().map(roleId -> playerDataManager.getPlayer(roleId)).filter(p -> p != null)
                .collect(Collectors.toList());
    }

    @Override
    public void joinBattle(AttackParamDto param) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        Player player = param.getInvokePlayer();
        long roleId = player.roleId;
        int battleId = param.getBattleId();
        if (battle.getDefencer().roleId == roleId) {
            throw new MwException(GameError.HAS_JOIN_BATTLE.getCode(), "城战被攻击方不用再加入战斗, roleId:", roleId, ", battleId:",
                    battleId);
        }
        // 是闪电战进攻方不让加入,防守方可以加入
        int camp = player.lord.getCamp();
        if (battle.getBattleType() == WorldConstant.CITY_BATTLE_BLITZ && camp == battle.getAtkCamp()) {
            throw new MwException(GameError.QUICKLY_BATTLE_NOT_JOIN.getCode(), "闪电战不让加入战斗, roleId:", roleId,
                    ", battleId:", battleId);
        }
        // 补给检测
        checkAndSubFood(param);
        BaseArmy baseArmy = createBaseArmy(param, now, ArmyConstant.ARMY_TYPE_ATK_PLAYER);
        addBattleRole(param,AwardFrom.ATTACK_PLAYER_BATTLE);
        // 事件通知
        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        crossWorldMap.publishMapEvent(baseArmy.createMapEvent(MapCurdEvent.CREATE),
                MapEvent.mapEntity(player.lord.getPos(), MapCurdEvent.UPDATE));
        // 填充返回值
        param.setArmy(PbHelper.createArmyPb(baseArmy.getArmy(), false));
        param.setBattle(PbHelper.createBattlePb(battle));
    }

}
