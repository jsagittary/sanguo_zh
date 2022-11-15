package com.gryphpoem.game.zw.gameplay.local.world.army;

import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.WFCityMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.GlobalWarFire;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.PlayerWarFire;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFire;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 战火燎原打城池部队
 *
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2021-01-28 15:01
 */
public class AttackWFCityArmy extends AttackCityArmy {

    public AttackWFCityArmy(Army army) {
        super(army);
    }

    @Override
    public CommonPb.Army toArmyPb() {
        return super.toArmyPb();
    }

    public CommonPb.Army toArmyPb(int cityCamp) {
        Army army = getArmy();
        CommonPb.Army.Builder armyPb_ = PbHelper.createArmyPb(army, false).toBuilder();
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.getPlayer(army.getLordId());
        if (Objects.nonNull(player)) {
            CommonPb.WarFireArmyExt.Builder extBuilder = CommonPb.WarFireArmyExt.newBuilder();
            int camp = player.lord.getCamp();
            extBuilder.setStatus(cityCamp == camp ? 2 : 1);
            extBuilder.setCamp(camp);
            extBuilder.setNick(player.lord.getNick());
            armyPb_.setExt(extBuilder.build());
        }
        return armyPb_.build();
    }

    /**
     * 战火燎原部队行军到达
     *
     * @param mapMarchArmy 地图信息
     * @param now
     */
    @Override
    protected void marchEnd(MapMarch mapMarchArmy, int now) {
        // 到达的部队的玩家对象
        Player armyPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
        if (armyPlayer == null) {
            return;
        }
        CrossWorldMap cMap = mapMarchArmy.getCrossWorldMap();
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);

        int targetPos = army.getTarget();
        BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(targetPos);

        Turple<Integer, Integer> xy = cMap.posToTurple(targetPos);
        int marchTime = cMap.marchTime(cMap, armyPlayer, armyPlayer.lord.getPos(), targetPos);
        // 未找到城池, 就返回
        if (Objects.isNull(baseWorldEntity) || baseWorldEntity.getType() != WorldEntityType.CITY) {
            Player tarPlayer = playerDataManager.getPlayer(army.getTarLordId());
            if (tarPlayer != null) {
                mailDataManager.sendReportMail(armyPlayer, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now, xy.getA(), xy.getB(), xy.getA(), xy.getB());
            }
            retreatArmy(mapMarchArmy, marchTime, marchTime);
        }
        // 设置部队状态
        army.setState(ArmyConstant.ARMY_STATE_WAR_FIRE_CITY);
        // 设置玩家将领状态
        setArmyPlayerHeroState(mapMarchArmy, ArmyConstant.ARMY_STATE_WAR_FIRE_CITY);
        // 战火燎原城池扩展数据
        WFCityMapEntity wfCityMapEntity = (WFCityMapEntity) baseWorldEntity;
        // 获取城池
        City city = wfCityMapEntity.getCity();
        // 未找到战火燎原的配置
        StaticWarFire sWarFire = StaticCrossWorldDataMgr.getStaticWarFireMap().get(city.getCityId());
        if (Objects.isNull(sWarFire)) {
            Player tarPlayer = playerDataManager.getPlayer(army.getTarLordId());
            if (tarPlayer != null) {
                mailDataManager.sendReportMail(armyPlayer, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now, xy.getA(), xy.getB(), xy.getA(), xy.getB());
            }
            retreatArmy(mapMarchArmy, marchTime, marchTime);
        }
        // 当前所属阵营
        int cityCamp = city.getCamp();
        // 进攻阵营
        int attackCamp = armyPlayer.lord.getCamp();
        if (attackCamp == cityCamp) {
            // TODO: 2021/1/5 驻防到达处理
            return;
        }
        // 当前阵营驻防部队
        List<AttackWFCityArmy> defWFArmy = wfCityMapEntity.getArmyByCamp(Collections.singletonList(cityCamp)).stream().filter(wfArmy -> now >= wfArmy.getArmy().getEndTime() && wfArmy.getArmy().getState() == ArmyConstant.ARMY_STATE_WAR_FIRE_CITY).collect(Collectors.toList());
        if (CheckNull.isEmpty(defWFArmy)) {
            wfCityMapEntity.changeOccupy(attackCamp, cMap);
            return;
        }
        List<Army> allArmy = defWFArmy.stream().map(BaseArmy::getArmy).collect(Collectors.toList());

        FightService fightService = DataResource.ac.getBean(FightService.class);
        WarDataManager warDataManager = DataResource.ac.getBean(WarDataManager.class);
        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);

        // 战斗相关
        Fighter attacker = fightService.createFighter(armyPlayer, army.getHero());
        Fighter defender = fightService.createFighterByArmy(allArmy);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, WorldConstant.BATTLE_TYPE_CAMP);
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.start();

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

        // 结果处理
        boolean atkSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS;
        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // 损兵处理
        WarService warService = DataResource.ac.getBean(WarService.class);

        // 发起攻击方损兵处理
        if (attacker.lost > 0) {
            warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.CAMP_BATTLE_ATTACK);
        }
        // 防守方损兵处理
        if (defender.lost > 0) {
            // 先扣除npc的兵力
            warService.subBattleHeroArm(defender.forces, changeMap, AwardFrom.CAMP_BATTLE_DEFEND);
        }

        // 记录双方杀敌, 城池战斗, 需要根据击杀数量获取积分
        recordWar(cMap, wfCityMapEntity, sWarFire, attacker, defender);

        // TODO: 2021/1/28 战火燎原城池战斗, 伤兵恢复是没有发送邮件的
        // 兵力恢复
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        // 执行勋章白衣天使特技逻辑
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
        //执行赛季天赋技能---伤病恢复
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
        seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);


        // 执行勋章-以战养战特技逻辑
        medalDataManager.sustainTheWarByMeansOfWar(attacker, defender, recoverArmyAwardMap, atkSuccess);

        // buff回血,只对防守者有效
        cMap.rebelBuffRecoverArmy(attacker, defender, recoverArmyAwardMap);

        // 战报生成
        SolarTermsDataManager solarTermsDataManager = DataResource.ac.getBean(SolarTermsDataManager.class);
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        BattlePb.BattleRoundPb record = fightLogic.generateRecord();
        // 节气
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(atkSuccess);
        rpt.setRecord(record);

        Lord atkLord = armyPlayer.lord;
        // 记录发起进攻和防守方的信息
        String atkNick = atkLord.getNick();
        rpt.setAttack(PbHelper.createRptMan(atkLord.getPos(), atkNick, atkLord.getVip(), atkLord.getLevel()));
        // 记录双方汇总信息
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkLord.getCamp(), atkNick,
                atkLord.getPortrait(), armyPlayer.getDressUp().getCurPortraitFrame()));

        int cityId = city.getCityId();
        rpt.setDefCity(PbHelper.createRptCityPb(cityId, targetPos));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, cityCamp, null, 0, 0));

        if (atkSuccess) {
            // 历史的阵营, 可以用于发邮件和跑马灯
            // 攻方胜利后设置城池归属
            wfCityMapEntity.changeOccupy(attackCamp, cMap);
        } else {
            // 防方胜利
        }

        // 已阵亡的部队自动返回
        if (!attacker.forces.get(0).alive()) {
            retreatArmy(mapMarchArmy, marchTime, marchTime);
        }
        for (Force force : defender.forces) {
            if (!force.alive()) {
                // 驻防玩家id
                long ownerId = force.ownerId;
                // 参与防守并阵亡的将领
                int heroId = force.id;
                Optional.ofNullable(playerDataManager.getPlayer(ownerId))
                        .ifPresent(defP -> {
                            // 防守玩家返回的行军时间
                            int retreatMarch = cMap.marchTime(cMap, defP, defP.lord.getPos(), targetPos);
                            defWFArmy
                                    .stream()
                                    .filter(wfArmy -> wfArmy.getLordId() == ownerId && wfArmy.getArmy().getHero().get(0).getV1() == heroId)
                                    .forEach(wfArmy -> {
                                        // 部队返回
                                        wfArmy.retreatArmy(mapMarchArmy, retreatMarch, retreatMarch);
                                    });
                        });
            }
        }

        // 事件通知
        cMap.publishMapEvent(createMapEvent(MapCurdEvent.UPDATE),
                MapEvent.mapEntity(getTargetPos(), MapCurdEvent.UPDATE));

        // 通知客户端玩家资源变化
        warService.sendRoleResChange(changeMap);

    }

    /**
     * 记录战斗双方杀敌
     *
     * @param cMap     新地图数据
     * @param attacker 进攻方
     * @param defender 防守方
     */
    private void recordWar(CrossWorldMap cMap, WFCityMapEntity wfCity, StaticWarFire staticWarFire, Fighter attacker, Fighter defender) {
        GlobalWarFire globalWarFire = cMap.getGlobalWarFire();
        if (globalWarFire.getStage() == GlobalWarFire.STAGE_RUNNING) {
            List<Force> forces = Stream.of(attacker.forces, defender.forces)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            if (!CheckNull.isEmpty(forces)) {
                forces.stream()
                        .filter(force -> force.killed > 0 || force.totalLost > 0)
                        .forEach(force -> {
                            long roleId = force.ownerId;
                            PlayerWarFire pwf = globalWarFire.getPlayerWarFire(roleId);
                            globalWarFire.addKillCnt(pwf, force.killed);
                        });
                //战火地图中进攻方每次只能出征一个将领
                Force atkForce = attacker.forces.get(0);
                for (Force defForce : defender.forces) {
                    if (defForce.killed == 0 && defForce.totalLost == 0) continue;
                    globalWarFire.logWarFireFightEvent(atkForce, defForce, wfCity);
                }
            }
        }
    }

    /**
     * 返回部队
     *
     * @param mapMarchArmy 地图部队
     * @param duration     行军时间
     * @param marchTime    行军时间
     */
    @Override
    public void retreatArmy(MapMarch mapMarchArmy, int duration, int marchTime) {
        super.retreatArmy(mapMarchArmy, duration, marchTime);
        BaseWorldEntity baseWorldEntity = mapMarchArmy.getCrossWorldMap().getAllMap().get(this.getTargetPos());
        if (Objects.nonNull(baseWorldEntity) && baseWorldEntity.getType() == WorldEntityType.CITY) {
            WFCityMapEntity wfCityMapEntity = (WFCityMapEntity) baseWorldEntity;
            wfCityMapEntity.getQueue().remove(this);
        }
    }
}