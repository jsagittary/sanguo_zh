package com.gryphpoem.game.zw.gameplay.local.world.battle;

import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.service.newyork.NewYorkWarService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyAttackTaskService;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.CityMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.newyork.NewYorkWar;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Report;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.FightRecord;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.pojo.world.CityHero;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.WorldService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by pengshuo on 2019/5/14 14:27
 * <br>Description: 纽约争霸战斗
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class NewYorkWarBattle extends AbsCommonBattle {

    public NewYorkWarBattle(Battle battle) {
        super(battle);
    }

    /**
     * 战斗逻辑
     */
    @Override
    public void doFight(int now, MapWarData mapWarData) {
        int pos = getBattle().getPos();
        CrossWorldMap cmap = mapWarData.getCrossWorldMap();
        BaseWorldEntity baseWorldEntity = cmap.getAllMap().get(pos);
        if (baseWorldEntity == null || baseWorldEntity.getType() != WorldEntityType.CITY) {
            // 取消战斗
            cancelBattleAndReturnArmy(mapWarData, CancelBattleType.UNKNOW);
            return;
        }
        CityMapEntity cityMapEntity = (CityMapEntity) baseWorldEntity;
        City city = cityMapEntity.getCity();
        if (city.getProtectTime() > now) {
            // 取消战斗
            cancelBattleAndReturnArmy(mapWarData, CancelBattleType.UNKNOW);
            return;
        }
        CrossWorldMapDataManager crossWorldMapDataManager = DataResource.ac.getBean(CrossWorldMapDataManager.class);
        NewYorkWar newYorkWar = crossWorldMapDataManager.getNewYorkWar();
        if (newYorkWar == null) {
            LogUtil.error("纽约争夺战异常结束");
            cancelBattleAndReturnArmy(mapWarData, CancelBattleType.UNKNOW);
            return;
        }
        WarService warService = DataResource.ac.getBean(WarService.class);
        FightService fightService = DataResource.ac.getBean(FightService.class);
        WarDataManager warDataManager = DataResource.ac.getBean(WarDataManager.class);
        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);
        WorldWarSeasonDailyAttackTaskService dailyAttackTaskService = DataResource.ac.getBean(WorldWarSeasonDailyAttackTaskService.class);
        NewYorkWarService newYorkWarService = DataResource.ac.getBean(NewYorkWarService.class);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);
        // Fighter
        Fighter attacker = fightService.createMultiPlayerFighter(battle, null);
        Fighter defender = fightService.createNewYorkWarBattleDefender(battle, city.getFormList());
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        // 战斗逻辑处理方法
        fightLogic.start();

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

        // 军功显赫 <roleId, <heroId, exploit>>
        HashMap<Long, Map<Integer, Integer>> exploitAwardMap = new HashMap<>(5);
        // 执行勋章特技-军功显赫 逻辑
        if (battle.getType() == WorldConstant.BATTLE_TYPE_CAMP) {
            medalDataManager.militaryMeritIsProminent(attacker, defender, exploitAwardMap);
        }
        // 结果处理
        boolean atkSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS;
        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>(4);
        // 发起攻击方损兵处理
        if (attacker.lost > 0) {
            warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.CAMP_BATTLE_ATTACK);
            // 计算玩家每日杀敌数量
            dailyAttackTaskService.addPlayerDailyAttackOther(attacker.forces);
            newYorkWarService.addPlayerNewYorkWarAttackVal(attacker.forces);
        }
        // 防守方损兵处理
        if (defender.lost > 0) {
            // 先扣除npc的兵力
            warService.subBattleNpcArm(defender.forces, city);
            warService.subBattleHeroArm(defender.forces, changeMap, AwardFrom.CAMP_BATTLE_DEFEND);
            // 计算玩家每日杀敌数量
            dailyAttackTaskService.addPlayerDailyAttackOther(defender.forces);
            newYorkWarService.addPlayerNewYorkWarAttackVal(defender.forces);
        }
        // 兵力恢复
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>(4);
        // 执行勋章白衣天使特技逻辑
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
        //执行赛季天赋技能---伤病恢复
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
        seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);

        // 执行勋章-以战养战特技逻辑
        medalDataManager.sustainTheWarByMeansOfWar(attacker, defender, recoverArmyAwardMap, atkSuccess);
        // 战报生成
        SolarTermsDataManager solarTermsDataManager = DataResource.ac.getBean(SolarTermsDataManager.class);
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        // 节气
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(atkSuccess);
        // 记录双方汇总信息
        int cityId = city.getCityId();
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, -1, null, 0, 0));
        rpt.setDefCity(PbHelper.createRptCityPb(cityId, pos));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, battle.getDefCamp(), null, 0, 0));
        Turple<Integer, Integer> atkPos = new Turple<>(0, 0);
        long aliveForceLordId = 0;
        if (atkSuccess) {
            aliveForceLordId = attacker.getAliveForce().ownerId;
            Player killMan = playerDataManager.getPlayer(aliveForceLordId);
            atkPos = cmap.posToTurple(killMan.lord.getPos());
            int killCamp = attacker.getAliveForce().getCamp();
            // 阵营战，攻方胜利后设置城池归属
            city.setCamp(killCamp);
            // 回复兵力
            StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
            armRepair(city, staticCity);

            newYorkWar.setFinalOccupyCamp(killCamp);
            if (newYorkWar.getCurrentRound() + 1 < newYorkWar.getTotalRound()) {
                city.setStatus(WorldConstant.CITY_STATUS_CALM);
            } else {
                city.setStatus(WorldConstant.CITY_STATUS_FREE);
                city.setProtectTime(now + WorldConstant.CITY_PROTECT_TIME_NEW_MAP * TimeHelper.MINUTE);
            }
            // 取消改点的其他战斗
            mapWarData.getBattlePosCache().get(pos).stream()
                    .filter(battleId -> battleId.intValue() != getBattleId())
                    .collect(Collectors.toList())
                    .stream().map(battleId -> mapWarData.getAllBattles().get(battleId))
                    .filter(b -> b != null).forEach(b ->
                            b.cancelBattleAndReturnArmy(mapWarData, CancelBattleType.UNKNOW)
                    );
        } else {
            // 修改城池状态
            List<BaseMapBattle> cityBattleList = mapWarData.getBattlesByPos(pos);
            if (!CheckNull.isEmpty(cityBattleList)) {
                if (cityBattleList.size() > 1) {
                    // 如果后面还有其他阵营战的队列，更新进攻阵营
                    BaseMapBattle nextBattle = cityBattleList.get(1);
                    city.setAttackCamp(nextBattle.getBattle().getAtkCamp());
                } else if (cityBattleList.size() == 1) {
                    // 当前城池只有一场战斗,才会改成空闲状态
                    city.setStatus(WorldConstant.CITY_STATUS_CALM);
                }
            } else {
                // 这个else 只是为了容错的修改城池状态
                city.setStatus(WorldConstant.CITY_STATUS_CALM);
            }
        }
        // 发送广播
        newYorkWarService.sendSysChat(atkSuccess);
        // 增加将领经验
        warService.addBattleHeroExp(attacker.forces, AwardFrom.NEWYORK_WAR_JOIN_AWARD, rpt, true, false,
                battle.isCityBattle(), changeMap, true, exploitAwardMap);
        warService.addBattleHeroExp(defender.forces, AwardFrom.NEWYORK_WAR_JOIN_AWARD, rpt, false, false,
                battle.isCityBattle(), changeMap, true, exploitAwardMap);
        // report
        Report report = DataResource.ac.getBean(FightRecordDataManager.class).generateReport(rpt.build(), fightLogic, now);
        // recordMap
        Map<Long, FightRecord> recordMap = new HashMap<>(8);
        recordRoleFight(recordMap, attacker.forces, true);
        recordRoleFight(recordMap, defender.forces, false);
        // 纽约争霸奖励
        Map<Long, List<CommonPb.Award>> dropMap = sendResourceReward(recordMap, changeMap);
        // 发送战报
        sendNewYorkWarBattleMail(battle, atkSuccess, report, recordMap, dropMap, now, recoverArmyAwardMap, atkPos, aliveForceLordId);
        // 推送改点的信息
        mapWarData.getCrossWorldMap().publishMapEvent(MapEvent.mapEntity(battle.getPos(), MapCurdEvent.UPDATE));
        // 通知客户端玩家资源变化
        warService.sendRoleResChange(changeMap);
        // 战斗打日志
        warService.logBattle(battle, fightLogic.getWinState(), attacker, defender, rpt.getAtkHeroList(), rpt.getDefHeroList());
    }

    /**
     * 加入战斗
     */
    @Override
    public void joinBattle(AttackParamDto param) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        Player player = param.getInvokePlayer();
        int camp = player.lord.getCamp();
        long roleId = player.roleId;
        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        Battle battle = getBattle();
        if (WorldConstant.ATTACK_STATE_NEED_LV > player.lord.getLevel()) {
            throw new MwException(GameError.ATTACK_STATE_NEED_LV.getCode(), "指挥官先磨砺至45级 ，再发动阵营战吧, roleId:", roleId);
        }
        // 补给检测
        checkAndSubFood(param);
        BaseArmy baseArmy = createBaseArmy(param, now, ArmyConstant.ARMY_TYPE_NEW_YORK_WAR);
        if (camp == battle.getDefCamp()) {
            battle.getDefRoles().add(roleId);
            int count = battle.getDefArm() + param.getArmCount();
            if (count < 0) {
                battle.setDefArm(0);
            } else {
                battle.setDefArm(count);
            }
        } else {
            WorldService worldService = DataResource.ac.getBean(WorldService.class);
            worldService.removeProTect(player, AwardFrom.NEW_YORK_WAR, battle.getPos());
            battle.getAtkRoles().add(roleId);
            int count = battle.getAtkArm() + param.getArmCount();
            if (count < 0) {
                battle.setAtkArm(0);
            } else {
                battle.setAtkArm(count);
            }
        }
        // 事件通知
        crossWorldMap.publishMapEvent(baseArmy.createMapEvent(MapCurdEvent.CREATE),
                MapEvent.mapEntity(player.lord.getPos(), MapCurdEvent.UPDATE));
        // 填充返回值
        param.setArmy(PbHelper.createArmyPb(baseArmy.getArmy(), false));
        param.setBattle(PbHelper.createBattlePb(battle));
    }

    @Override
    protected void onCancelBattleAfter(MapWarData mapWarData, CancelBattleType cancelType) {
        super.onCancelBattleAfter(mapWarData, cancelType);
    }

    /**
     * 兵力回复
     *
     * @param city
     * @param staticCity
     */
    private void armRepair(City city, StaticCity staticCity) {
        for (CityHero hero : city.getFormList()) {
            StaticNpc sNpc = StaticNpcDataMgr.getNpcMap().get(hero.getNpcId());
            if (sNpc != null) {
                hero.setCurArm(sNpc.getTotalArm());
            }
        }
    }

    /**
     * 发送纽约争霸战报
     */
    private void sendNewYorkWarBattleMail(Battle battle, boolean atkSuccess, Report report
            , Map<Long, FightRecord> recordMap, Map<Long, List<CommonPb.Award>> dropMap, int now
            , Map<Long, List<CommonPb.Award>> recoverArmyAwardMap, Turple<Integer, Integer> atkPos, long aliveForceLordId) {
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        int defineCamp = battle.getDefCamp();
        int a = Optional.ofNullable(WorldConstant.NEWYORK_WAR_LOST_EXP).map(s -> s.get(0)).orElse(1);
        int b = Optional.ofNullable(WorldConstant.NEWYORK_WAR_LOST_EXP).map(s -> s.get(1)).orElse(1);
        // 攻打成功
        if (atkSuccess) {
            // 攻击方发送攻击成功战报
            String alivePlayerNick =
                    Optional.ofNullable(playerDataManager.getPlayer(aliveForceLordId)).map(p -> p.lord.getNick()).orElse("");
            battle.getAtkRoles().forEach(lordId -> {
                Player player = playerDataManager.getPlayer(lordId);
                int lost = Optional.ofNullable(recordMap.get(lordId)).map(r -> r.getLost()).orElse(0);
                Optional.ofNullable(player).ifPresent(p ->
                        mailDataManager.sendReportMail(p, report, MailConstant.MOLD_NEWYORK_WAR_ROUND_ATTACK_SUCCESS,
                                dropMap.get(lordId), now, recoverArmyAwardMap, defineCamp, defineCamp, alivePlayerNick,
                                atkPos.getA(), atkPos.getB(), lost, lost * b / a)
                );
            });
            // 防守方发送防守失败战报
            battle.getDefRoles().forEach(lordId -> {
                Player player = playerDataManager.getPlayer(lordId);
                int lost = Optional.ofNullable(recordMap.get(lordId)).map(r -> r.getLost()).orElse(0);
                Optional.ofNullable(player).ifPresent(p ->
                        mailDataManager.sendReportMail(p, report, MailConstant.MOLD_NEWYORK_WAR_ROUND_DEFINE_FAIL,
                                dropMap.get(lordId), now, recoverArmyAwardMap, defineCamp, defineCamp, lost, lost * b / a)
                );
            });
        }
        // 攻打失败
        else {
            // 攻击方发送攻击失败战报
            battle.getAtkRoles().forEach(lordId -> {
                Player player = playerDataManager.getPlayer(lordId);
                int lost = Optional.ofNullable(recordMap.get(lordId)).map(r -> r.getLost()).orElse(0);
                Optional.ofNullable(player).ifPresent(p ->
                        mailDataManager.sendReportMail(p, report, MailConstant.MOLD_NEWYORK_WAR_ROUND_ATTACK_FAIL,
                                dropMap.get(lordId), now, recoverArmyAwardMap, defineCamp, defineCamp, lost, lost * b / a)
                );
            });
            // 防守方发送防守成功战报
            battle.getDefRoles().forEach(lordId -> {
                Player player = playerDataManager.getPlayer(lordId);
                int lost = Optional.ofNullable(recordMap.get(lordId)).map(r -> r.getLost()).orElse(0);
                Optional.ofNullable(player).ifPresent(p ->
                        mailDataManager.sendReportMail(player, report, MailConstant.MOLD_NEWYORK_WAR_ROUND_DEFINE_SUCCESS,
                                dropMap.get(lordId), now, recoverArmyAwardMap, defineCamp, defineCamp, lost, lost * b / a)
                );
            });
        }
    }

    /**
     * 奖励生成
     */
    private Map<Long, List<CommonPb.Award>> sendResourceReward(Map<Long, FightRecord> recordMap, Map<Long, ChangeInfo> changeMap) {
        Map<Long, List<CommonPb.Award>> campDropMap = new HashMap<>(10);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        int a = Optional.ofNullable(WorldConstant.NEWYORK_WAR_LOST_EXP).map(s -> s.get(0)).orElse(1);
        int b = Optional.ofNullable(WorldConstant.NEWYORK_WAR_LOST_EXP).map(s -> s.get(1)).orElse(1);
        for (FightRecord record : recordMap.values()) {
            Player player = playerDataManager.getPlayer(record.getRoleId());
            if (player == null) {
                continue;
            }
            ChangeInfo info = changeMap.get(record.getRoleId());
            if (null == info) {
                info = ChangeInfo.newIns();
                changeMap.put(record.getRoleId(), info);
            }
            if (record.getLost() > 0) {
                // 损兵增加 玩家经验*5
                int exp = record.getLost() * b / a;
                List<CommonPb.Award> dropList = new ArrayList<>(2);
                campDropMap.put(record.getRoleId(), dropList);
                rewardDataManager.addAward(player, AwardType.MONEY, AwardType.Money.EXP, exp, AwardFrom.NEWYORK_WAR_JOIN_AWARD);
                dropList.add(PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.EXP, exp));
                info.addChangeType(AwardType.MONEY, AwardType.Money.EXP);
            }
        }
        return campDropMap;
    }

    /**
     * 统计战斗记录
     */
    private void recordRoleFight(Map<Long, FightRecord> recordMap, List<Force> forces, boolean attacker) {
        Optional.ofNullable(forces).ifPresent(fs ->
                fs.stream().filter(f -> f.roleType == Constant.Role.PLAYER && f.ownerId != 0).forEach(f -> {
                    long roleId = f.ownerId;
                    FightRecord record = recordMap.get(roleId);
                    if (null == record) {
                        record = new FightRecord(roleId);
                        recordMap.put(roleId, record);
                    }
                    record.addLost(f.totalLost);
                    record.addKilled(f.killed);
                    record.setAttacker(attacker);
                })
        );
    }
}
