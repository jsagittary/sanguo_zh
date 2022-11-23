package com.gryphpoem.game.zw.service.dominate.state;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideCity;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.WorldMapPlay;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.abs.TimeLimitDominateMap;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.StateDominateWorldMap;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.dominate.abs.AbsDominateWorldMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description: 州郡雄踞一方
 * Author: zhangpeng
 * createTime: 2022-11-22 21:26
 */
@Component
public class StateDominateWorldMapService extends AbsDominateWorldMapService {

    @Autowired
    private FightService fightService;

    @Autowired
    private ChatDataManager chatDataManager;

    @Autowired
    private WarService warService;


    @Override
    public void marchEnd(Player player, Army army, int nowSec) {
        try {
            long roleId = player.roleId;
            int tarPos = army.getTarget();
            //活动未开启
            StateDominateWorldMap worldMap = StateDominateWorldMap.getInstance();
            if (worldMap.state() != WorldPb.WorldFunctionStateDefine.IN_PROGRESS_VALUE) {
                // 州郡雄踞一方活动未开启
                LogUtil.error(String.format("state dominate not open!!! roleId :%d, armyKeyId :%d, target pos :%d, march end time :%d",
                        roleId, army.getKeyId(), tarPos, army.getEndTime()));
                retreatArmy(player, army, null, nowSec, false);
                return;
            }

            // 城池不存在
            WorldMapPlay worldMapPlay = getWorldMapPlay(getWorldMapFunction());
            TimeLimitDominateMap timeLimitDominateMap = (TimeLimitDominateMap) worldMapPlay;
            List<DominateSideCity> sideCityList = timeLimitDominateMap.getCurOpenCityList().get(timeLimitDominateMap.getCurTimes());
            DominateSideCity sideCity;
            if (CheckNull.isEmpty(sideCityList) || Objects.isNull(sideCity = sideCityList.stream().filter(city ->
                    city.getCityId() == army.getTargetId()).findFirst().orElse(null))) {
                LogUtil.error(String.format("state dominate city not found, army: ", armyHeroString(army)));
                return;
            }
            //遗迹已经被我方占领. 加入驻防队列
            if (sideCity.getHoldCamp() == player.getCamp()) {
                joinProbing(sideCity, player, army, TimeHelper.getCurrentSecond());
                return;
            }
            //没有防守部队 直接占领
            LinkedList<Turple<Long, Integer>> defendList = sideCity.getDefendList();
            if (CheckNull.isEmpty(defendList)) {
                handAttackDominateSuccess(sideCity, player, army, nowSec);
                return;
            }
            //开始战斗
            doFight(sideCity, player, army, nowSec);
        } catch (Exception e) {
            LogUtil.error(String.format("roleId :%d, armyKeyId :%d, heroList :%s", player.roleId, army.getKeyId(), armyHeroString(army)), e);
            //行军失败!!! 返回部队
            worldService.retreatArmy(player, army, nowSec);
        }
    }

    /**
     * 没有防守将领, 直接占领
     *
     * @param sideCity
     * @param attackPlayer
     * @param army
     * @param nowSec
     */
    private void handAttackDominateSuccess(DominateSideCity sideCity, Player attackPlayer, Army army, int nowSec) {
        //更换占领阵营，先计算原阵营的占领时间
        sideCity.changeCampHolder(attackPlayer.getCamp(), nowSec);

        List<Integer> posList = new ArrayList<>();
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(sideCity.getCityId());
        if (Objects.nonNull(staticCity)) {
            posList.add(staticCity.getCityPos());
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
        }

        //玩家加入探索
        joinProbing(sideCity, attackPlayer, army, nowSec);
    }

    /**
     * 加入探索
     *
     * @param sideCity
     * @param player
     * @param army
     * @param now
     */
    private void joinProbing(DominateSideCity sideCity, Player player, Army army, int now) {
        army.setState(ArmyConstant.ARMY_STATE_STATE_DOMINATE_HOLDER);
        // TODO 修改主副武将将领状态


        sideCity.joinDefendList(new Turple<>(player.roleId, army.getKeyId()));
        sideCity.joinHoldArmy(player.roleId, army.getKeyId(), System.currentTimeMillis());
        LinkedList<Turple<Long, Integer>> defendList = sideCity.getDefendList();
        LogUtil.debug(String.format("state dominate player join ,cityId :%d, roleId :%d, army keyId :%d, join defend list current defend list size :%d",
                sideCity.getCityId(), player.roleId, army.getKeyId(), defendList.size()));
        if (defendList.size() > Constant.MAXIMUM_NUMBER_OF_DOMINATE_DEFENSE_QUEUE) {
            LogUtil.warn(String.format("state dominate player count,cityId :%d, roleId :%d, army keyId :%d, current defend list size :%d > %d",
                    sideCity.getCityId(), player.roleId, army.getKeyId(), defendList.size(), Constant.MAXIMUM_NUMBER_OF_DOMINATE_DEFENSE_QUEUE));
        }
    }

    /**
     * 战斗
     *
     * @param sideCity
     * @param player
     * @param army
     * @param nowSec
     */
    private void doFight(DominateSideCity sideCity, Player player, Army army, int nowSec) {
        LinkedList<Turple<Long, Integer>> defendList = sideCity.getDefendList();
        int fightCount = 0;
        do {
            Turple<Long, Integer> tpl = defendList.peekFirst();
            if (Objects.isNull(tpl)) {
                break;//防守队列被打光了
            }
            //进攻部队被打光了
            if (army.getHeroLeadCount() <= 0) {
                LogUtil.common(String.format("cityId :%d, roleId :%d, armyId :%d, army hero all dead...", sideCity.getCityId(), player.roleId, army.getKeyId()));
                break;
            }

            long defendRoleId = tpl.getA();
            int defendArmyKeyId = tpl.getB();
            Player defendPlayer = playerDataManager.getPlayer(defendRoleId);
            Army defendArmy = Objects.nonNull(defendPlayer) ? defendPlayer.armys.get(defendArmyKeyId) : null;
            //防守玩家的部队不存在
            if (Objects.isNull(defendArmy) || defendArmy.getHeroLeadCount() <= 0 || defendArmy.getType() != ArmyConstant.ARMY_STATE_STATE_DOMINATE_HOLDER) {
                LogUtil.error(String.format("cityId :%d, defend roleId :%d, defend army keyId :%d, not found!!!", sideCity.getCityId(), defendRoleId, defendArmyKeyId));
                defendList.removeFirst();
                continue;
            }
            FightLogic fightLogic = fightLogic(sideCity, player, army, defendPlayer, defendArmy, StateDominateWorldMap.getInstance(), nowSec, tpl);
            if (Objects.isNull(fightLogic)) {
                LogUtil.error(String.format("state dominate fight occur error,cityId :%d, roleId :%d, army keyId :%d, def roleId :%d, def army keyId :%d, fight is error...",
                        sideCity.getCityId(), player.roleId, army.getKeyId(), defendRoleId, defendArmyKeyId));
                break;
            }
        } while (++fightCount < Constant.DOMINATE_ARMY_FIGHT_MAX);

        //记录一下战斗超过10场的玩家与出战部队信息
        if (fightCount > 10) {
            LogUtil.common(String.format("cityId :%d, roleId :%d, army keyId :%d, hero list :%s, fight count so much !!!",
                    sideCity.getCityId(), player.roleId, army.getKeyId(), armyHeroString(army)));
        }

        //攻打遗迹战斗结束[进攻胜利, 防守胜利, 战斗异常]
        if (army.getHeroLeadCount() > 0) {
            if (defendList.isEmpty()) {
                handAttackDominateSuccess(sideCity, player, army, nowSec);
            } else {//达到战斗次数上限还没打下来 就返回
                //never got here except fight error!!!
                LogUtil.error(String.format("cityId :%d, roleId :%d army keyId :%d, survival hero list :%s. defend remain size :%d",
                        sideCity.getCityId(), player.roleId, army.getKeyId(), armyHeroString(army), defendList.size()));
                retreatArmy(player, army, null, nowSec, false);
            }
        }
    }

    /**
     * 战斗逻辑
     *
     * @param rlc
     * @param attackPlayer
     * @param atkArmy
     * @param defendPlayer
     * @param defArmy
     * @param nowSec
     * @param o2
     * @return
     */
    private FightLogic fightLogic(DominateSideCity rlc, Player attackPlayer, Army atkArmy, Player defendPlayer, Army defArmy, StateDominateWorldMap worldMap, int nowSec, Turple<Long, Integer> o2) {
        long fightId = 0;
        try {
            Fighter attacker = fightService.createFighterWithFatigueDeBuff(attackPlayer, atkArmy.getKeyId(), atkArmy.getHero(), null, 0);
            Fighter defender = fightService.createFighterWithFatigueDeBuff(defendPlayer, defArmy.getKeyId(), defArmy.getHero(),
                    Constant.DOMINATE_FATIGUE_DE_BUFF_PARAMETER, rlc.holdTime(defendPlayer.roleId, defArmy.getKeyId()));
            FightLogic fightLogic = new FightLogic(attacker, defender, true, WorldConstant.BATTLE_TYPE_HIS_REMAIN);
            fightLogic.packForm();
            fightLogic.fight();
            fightId = fightLogic.fightId;
            LogUtil.debug(String.format("his cityId :%d, fightId :%d, result :%s, attack roleId :%d, atk army keyId :%d, defend roleId :%d, army keyId :%d",
                    rlc.getCityId(), fightId, fightLogic.getWinState(), attackPlayer.roleId, atkArmy.getKeyId(), defendPlayer.roleId, defArmy.getKeyId()));

            Map<Long, ChangeInfo> changeMap = new HashMap<>(); // 记录需要推送的值
            Map<Long, Integer> recoverMap = new HashMap<>();
            // 损兵与伤病恢复
            if (attacker.lost > 0) {
                subAndRetreatDeadHeroInArmy(attackPlayer, attacker, atkArmy, changeMap, recoverMap, nowSec);
                //进攻方兵力=0 则返回部队
                if (atkArmy.getHeroLeadCount() <= 0) {
                    retreatArmy(attackPlayer, atkArmy, changeMap, nowSec, false);
                }
            }
            if (defender.lost > 0) {
                subAndRetreatDeadHeroInArmy(defendPlayer, defender, defArmy, changeMap, recoverMap, nowSec);
                if (defArmy.getHeroLeadCount() <= 0) {
                    rlc.getDefendList().remove(o2);
                    retreatArmy(defendPlayer, defArmy, changeMap, nowSec, true);
                }
            }

            //创建战报
            CommonPb.Report.Builder report = createFightReport(fightLogic, attackPlayer, defendPlayer, recoverMap, nowSec, changeMap);
            // 通知客户端玩家兵力/战功等资源变化
            warService.sendRoleResChange(changeMap);
            boolean attackSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
            //发送战斗邮件
            if (attackSuccess) {
                atkArmy.setState(ArmyConstant.ARMY_STATE_STATE_DOMINATE_HOLDER);
                // 连杀广播
                worldMap.incContinuousKillCnt(atkArmy.getLordId(), rlc.getCityId());
                continuousKillBroadcast(attackPlayer);
            } else {
                worldMap.clearContinuousKillCnt(atkArmy.getLordId(), rlc.getCityId());
            }
            // 地图刷新
            List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(atkArmy.getTarget(), attackPlayer.lord.getPos()));
            posList.add(atkArmy.getTarget());
            posList.add(attackPlayer.lord.getPos());
            // 新增后台埋点日志
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
            LogLordHelper.otherLog("battle", attackPlayer.account.getServerId(), attackPlayer.roleId, "atk", 0, WorldConstant.BATTLE_TYPE_HIS_REMAIN,
                    fightLogic.getWinState(), atkArmy.getTarget(), attackPlayer.lord.getLordId(), defendPlayer.lord.getLordId(), attackPlayer.lord.getCamp());
            LogLordHelper.otherLog("battle", defendPlayer.account.getServerId(), defendPlayer.roleId, "def", 0, WorldConstant.BATTLE_TYPE_HIS_REMAIN,
                    fightLogic.getWinState(), atkArmy.getTarget(), attackPlayer.lord.getLordId(), defendPlayer.lord.getLordId(), defendPlayer.lord.getCamp());

            //上报数数(攻击方)
            EventDataUp.battle(attackPlayer.account, attackPlayer.lord, attacker, "atk", "fightStateDominate",
                    String.valueOf(WorldConstant.BATTLE_TYPE_HIS_REMAIN), String.valueOf(fightLogic.getWinState()),
                    attackPlayer.roleId, report.getRptPlayer().getAtkHeroList(), rlc.getCityId());
            //上报数数(防守方)
            EventDataUp.battle(defendPlayer.account, defendPlayer.lord, defender, "def", "fightStateDominate",
                    String.valueOf(WorldConstant.BATTLE_TYPE_HIS_REMAIN), String.valueOf(fightLogic.getWinState()),
                    attackPlayer.roleId, report.getRptPlayer().getDefHeroList(), rlc.getCityId());
            return fightLogic;
        } catch (Exception e) {
            LogUtil.error(String.format("cityId :%d, attack roleId :%d, army keyId :%d, defend roleId :%d, army keyId :%d, fightId :%d, fight error!!!",
                    rlc.getCityId(), attackPlayer.roleId, atkArmy.getKeyId(), defendPlayer.roleId, defArmy.getKeyId(), fightId), e);
        }
        return null;
    }

    /**
     * 创建战报
     *
     * @param fightLogic
     * @param attackPlayer
     * @param defendPlayer
     * @param recoverMap
     * @param nowSec
     * @param changeMap
     * @return
     */
    private CommonPb.Report.Builder createFightReport(FightLogic fightLogic, Player attackPlayer, Player defendPlayer, Map<Long, Integer> recoverMap, int nowSec, Map<Long, ChangeInfo> changeMap) {
        // 战斗记录
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        CommonPb.Record record = fightLogic.generateRecord();
        rpt.setResult(fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS);
        Lord atkLord = attackPlayer.lord;
        Lord defLord = defendPlayer.lord;
        Fighter attacker = fightLogic.getAttacker();
        Fighter defender = fightLogic.getDefender();
        //战斗双方信息
        rpt.setAttack(PbHelper.createRptMan(atkLord.getPos(), atkLord.getNick(), atkLord.getVip(), atkLord.getLevel()));
        rpt.setDefMan(PbHelper.createRptMan(defLord.getPos(), defLord.getNick(), defLord.getVip(), defLord.getLevel()));
        //战斗摘要
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkLord.getCamp(), atkLord.getNick(),
                atkLord.getPortrait(), attackPlayer.getDressUp().getCurPortraitFrame(), recoverMap.getOrDefault(attackPlayer.roleId, 0)));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defLord.getCamp(), defLord.getNick(),
                defLord.getPortrait(), defendPlayer.getDressUp().getCurPortraitFrame(), recoverMap.getOrDefault(defendPlayer.roleId, 0)));
        //攻击、防守的将领
        for (Force force : attacker.forces) {
            CommonPb.RptHero rptHero = fightService.addExploitAndBuildRptHero(force, changeMap, AwardFrom.STATE_DOMINATE_FIGHT);
            if (rptHero != null) {
                rpt.addAtkHero(rptHero);
            }
        }
        for (Force force : defender.forces) {
            CommonPb.RptHero rptHero = fightService.addExploitAndBuildRptHero(force, changeMap, AwardFrom.STATE_DOMINATE_FIGHT);
            if (rptHero != null) {
                rpt.addDefHero(rptHero);
            }
        }
        // 回合战报
        rpt.setRecord(record);
        return worldService.createAtkPlayerReport(rpt.build(), nowSec); // 战报
    }


    private void subAndRetreatDeadHeroInArmy(Player player, Fighter fighter, Army army, Map<Long, ChangeInfo> changeMap, Map<Long, Integer> recoverMap, int nowSec) {
        // 通用损兵处理
        warService.subBattleHeroArm(fighter.forces, changeMap, AwardFrom.STATE_DOMINATE_FIGHT);
        // 记录将领累计损兵恢复
        Map<Integer, Integer> lostHpMap = army.getAndCreateIfAbsentTotalLostHpMap();
        Map<Integer, Integer> recoverHpMap = army.getAndCreateIfAbsentRecoverMap();
        fighter.forces.stream().filter(force -> force.totalLost > 0).forEach(force -> {
            int totalLost = force.totalLost + lostHpMap.getOrDefault(force.id, 0);
            lostHpMap.put(force.id, totalLost);
            int recoveryHp = (int) (Constant.STATE_DOMINATE_WORLD_MAP_RETURNING_SOLDIERS_RATIO / NumberUtil.TEN_THOUSAND_DOUBLE * totalLost);
            recoverHpMap.put(force.id, recoveryHp);
        });
        //分离死亡部队和存活部队. 死亡将领回家, 存活将领继续战斗.
        List<CommonPb.TwoInt> deadHero = null, survivorHero = null;
        for (CommonPb.TwoInt twoInt : army.getHero()) {
            Hero hero = player.heros.get(twoInt.getV1());
            if (Objects.isNull(hero)) return;
            if (hero.getCount() <= 0) {
                if (Objects.isNull(deadHero)) deadHero = new ArrayList<>();
                deadHero.add(PbHelper.createTwoIntPb(twoInt.getV1(), 0));
            } else {
                if (Objects.isNull(survivorHero)) survivorHero = new ArrayList<>();
                survivorHero.add(PbHelper.createTwoIntPb(twoInt.getV1(), hero.getCount()));
            }
        }
        if (CheckNull.nonEmpty(survivorHero)) {
            //重新设置剩余存活将领
            army.setHero(survivorHero);
            //死亡的将领回家
            if (Objects.nonNull(deadHero)) {
                Army deadArmy = simpleArmyCopy(player.maxKey(), army);
                player.armys.put(deadArmy.getKeyId(), deadArmy);
                deadArmy.setHero(deadHero);
                Map<Integer, Integer> deadArmyRecoverMap = deadArmy.getAndCreateIfAbsentRecoverMap();
                for (CommonPb.TwoInt twoInt : deadHero) {
                    int recoverHp = recoverHpMap.getOrDefault(twoInt.getV1(), 0);
                    if (recoverHp > 0) deadArmyRecoverMap.put(twoInt.getV1(), recoverHp);
                    recoverMap.merge(player.roleId, recoverHp, Integer::sum);
                }
                //返回死亡将领
                retreatArmy(player, deadArmy, changeMap, nowSec, false);
            }
        } else {
            //整个部队全部死亡
            army.setHero(deadHero);
            if (CheckNull.nonEmpty(deadHero)) {
                for (CommonPb.TwoInt twoInt : deadHero) {
                    int recoverHp = recoverHpMap.getOrDefault(twoInt.getV1(), 0);
                    recoverMap.merge(player.roleId, recoverHp, Integer::sum);
                }
            }
        }
    }

    private Army simpleArmyCopy(int armyKeyId, Army army) {
        Army copyArmy = new Army();
        copyArmy.setKeyId(armyKeyId);
        copyArmy.setLordId(army.getLordId());
        copyArmy.setType(army.getType());
        copyArmy.setSubType(army.getSubType());
        copyArmy.setTarget(army.getTarget());
        copyArmy.setTargetId(army.getTargetId());
        copyArmy.setTarLordId(army.getTarLordId());
        copyArmy.setBattleId(army.getBattleId());
        copyArmy.setBattleTime(army.getBeginTime());
        copyArmy.setState(army.getState());
        copyArmy.setDuration(army.getDuration());
        copyArmy.setEndTime(army.getEndTime());
        copyArmy.setOriginCity(army.getOriginCity());
        copyArmy.setOriginPos(army.getOriginPos());
        return copyArmy;
    }

    public void retreatArmy(Player player, Army army, Map<Long, ChangeInfo> changeMap, int nowSec, boolean b) {
        try {
            Map<Integer, Integer> recoverMap = army.getRecoverMap();
            if (CheckNull.nonEmpty(recoverMap)) doRecoverArmy(player, army, recoverMap, changeMap);
            worldService.retreatArmy(player, army, nowSec);
            worldService.synRetreatArmy(player, army, nowSec);
        } catch (Exception e) {
            LogUtil.error(String.format("retreat army occur exception,roleId=%s,army=%s", player.roleId, army), e);
        }
    }

    private void doRecoverArmy(Player player, Army retreatArmy, Map<Integer, Integer> recoverMap, Map<Long, ChangeInfo> changeMap) {
        for (CommonPb.TwoInt twoInt : retreatArmy.getHero()) {
            Hero hero = player.heros.get(twoInt.getV1());
            Integer recoverHp = recoverMap.remove(hero.getHeroId());
            if (Objects.isNull(recoverHp) || recoverHp <= 0) continue;
            hero.addArm(recoverHp);
            if (Objects.nonNull(changeMap)) {
                ChangeInfo info = changeMap.computeIfAbsent(player.roleId, k -> ChangeInfo.newIns());
                info.addChangeType(AwardType.HERO_ARM, hero.getHeroId());
            }
        }
    }

    private String armyHeroString(Army army) {
        if (CheckNull.isEmpty(army.getHero())) return "";
        return army.getHero().stream()
                .map(twoInt -> new StringBuilder().append(twoInt.getV1()).append("_").append(twoInt.getV2()))
                .collect(Collectors.joining(","));
    }

    /**
     * 连杀广播
     *
     * @param attackPlayer 进攻玩家
     */
    private void continuousKillBroadcast(Player attackPlayer) {
        int continuousKillCnt = attackPlayer.getPlayerRelic().getContinuousKillCnt();
        Integer chatId = Constant.DOMINATE_CHAT_KILL_BROADCAST_MAP.get(continuousKillCnt);
        if (Objects.nonNull(chatId)) {
            int camp = attackPlayer.getCamp();
            chatDataManager.sendSysChat(chatId, camp, 0, camp, attackPlayer.lord.getNick());
        }
    }

    @Override
    public int getWorldMapFunction() {
        return WorldPb.WorldFunctionDefine.STATES_AND_COUNTIES_DOMINATE_VALUE;
    }
}
