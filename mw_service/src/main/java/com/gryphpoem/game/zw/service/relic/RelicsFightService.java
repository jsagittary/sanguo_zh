package com.gryphpoem.game.zw.service.relic;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticDataMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.relic.GlobalRelic;
import com.gryphpoem.game.zw.resource.pojo.relic.RelicCons;
import com.gryphpoem.game.zw.resource.pojo.relic.RelicEntity;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.WorldScheduleService;
import com.gryphpoem.game.zw.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.gryphpoem.game.zw.resource.constant.ActParamConstant.RELIC_ARMY_FIGHT_MAX;

/**
 * dynastic relics
 * 王朝遗迹战斗服务
 * Description:
 * Author: zhangdh
 * createTime: 2022-08-02 13:55
 */
@Service
public class RelicsFightService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private FightService fightService;
    @Autowired
    private WarService warService;
    @Autowired
    private WorldService worldService;
    @Autowired
    private RelicService relicService;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private WorldScheduleService worldScheduleService;


    /**
     * 王朝遗迹行军结束...
     *
     * @param player p
     * @param army   army
     * @param nowSec sec
     */
    public void marchEnd(Player player, Army army, int nowSec) {
        try {
            long roleId = player.roleId;
            int tarPos = army.getTarget();
            //活动未开启
            GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
            if (globalRelic.state() != RelicCons.OPEN) {//遗迹活动未开启
                LogUtil.error(String.format("relics not open!!! roleId :%d, armyKeyId :%d, target pos :%d, march end time :%d", roleId, army.getKeyId(), tarPos, army.getEndTime()));
                retreatArmy(player, army, null, nowSec, false);
                return;
            }
            //遗迹不存在
            RelicEntity relic = globalRelic.getRelicEntityMap().get(tarPos);
            if (Objects.isNull(relic)) {
                LogUtil.error(String.format("roleId :%d, armyKeyId :%d, target pos :%d, not found historical remains, march end time :%d", roleId, army.getKeyId(), tarPos, army.getEndTime()));
                retreatArmy(player, army, null, nowSec, false);
                return;
            }
            //遗迹已经被我方占领. 加入驻防队列
            if (relic.getHoldCamp() == player.getCamp()) {
                joinProbing(relic, player, army, TimeHelper.getCurrentSecond());
                return;
            }
            //没有防守部队 直接占领
            LinkedList<Turple<Long, Integer>> defendList = relic.getDefendList();
            if (CheckNull.isEmpty(defendList)) {
                handAttackRelicSuccess(relic, player, army, nowSec);
                return;
            }
            //开始战斗
            doFight(relic, player, army, nowSec);
        } catch (Exception e) {
            LogUtil.error(String.format("roleId :%d, armyKeyId :%d, heroList :%s", player.roleId, army.getKeyId(), armyHeroString(army)), e);
            //行军失败!!! 返回部队
            worldService.retreatArmy(player, army, nowSec);
        }
    }

    private void joinProbing(RelicEntity relicEntity, Player player, Army army, int now) {
        army.setState(ArmyConstant.ARMY_STATE_RELIC_PROBING);
        //若已经有部队正在探索中则不更新这个值
        if (!relicEntity.isHavingProbe(player.roleId)) {
            player.getPlayerRelic().setStartProbe(now);
        }
        relicEntity.joinDefendList(new Turple<>(player.roleId, army.getKeyId()));
        LinkedList<Turple<Long, Integer>> defendList = relicEntity.getDefendList();
        LogUtil.debug(String.format("relic player join ,cityId :%d, roleId :%d, army keyId :%d, join defend list current defend list size :%d", relicEntity.getCfgId(), player.roleId, army.getKeyId(), defendList.size()));
        if (defendList.size() > Constant.RELIC_DEFEND_ARMY_MAX_COUNT) {
            LogUtil.warn(String.format("relic player count,cityId :%d, roleId :%d, army keyId :%d, current defend list size :%d > %d", relicEntity.getCfgId(), player.roleId, army.getKeyId(), defendList.size(), Constant.RELIC_DEFEND_ARMY_MAX_COUNT));
        }
    }

    private void doFight(RelicEntity rlc, Player player, Army army, int nowSec) {
        LinkedList<Turple<Long, Integer>> defendList = rlc.getDefendList();
        int fightCount = 0;
        do {
            Turple<Long, Integer> tpl = defendList.peekFirst();
            if (Objects.isNull(tpl)) {
                break;//防守队列被打光了
            }
            //进攻部队被打光了
            if (army.getHeroLeadCount() <= 0) {
                LogUtil.common(String.format("cityId :%d, roleId :%d, armyId :%d, army hero all dead...", rlc.getCfgId(), player.roleId, army.getKeyId()));
                break;
            }

            long defendRoleId = tpl.getA();
            int defendArmyKeyId = tpl.getB();
            Player defendPlayer = playerDataManager.getPlayer(defendRoleId);
            Army defendArmy = Objects.nonNull(defendPlayer) ? defendPlayer.armys.get(defendArmyKeyId) : null;
            //防守玩家的部队不存在
            if (Objects.isNull(defendArmy) || defendArmy.getHeroLeadCount() <= 0 || defendArmy.getType() != ArmyConstant.ARMY_TYPE_RELIC_BATTLE) {
                LogUtil.error(String.format("cityId :%d, defend roleId :%d, defend army keyId :%d, not found!!!", rlc.getCfgId(), defendRoleId, defendArmyKeyId));
                defendList.removeFirst();
                continue;
            }
            FightLogic fightLogic = fightLogic(rlc, player, army, defendPlayer, defendArmy, nowSec, rlc, tpl);
            if (Objects.nonNull(fightLogic)) {
//                if (defendArmy.getHeroLeadCount() <= 0) {
//                    //当前防守部队将领全部死亡.
//                    defendList.remove(tpl);
//                }
            } else {
                LogUtil.error(String.format("relic fight occur error,cityId :%d, roleId :%d, army keyId :%d, def roleId :%d, def army keyId :%d, fight is error...",
                        rlc.getCfgId(), player.roleId, army.getKeyId(), defendRoleId, defendArmyKeyId));
                break;
            }
        } while (++fightCount < RELIC_ARMY_FIGHT_MAX);

        //记录一下战斗超过10场的玩家与出战部队信息
        if (fightCount > 10) {
            LogUtil.common(String.format("cityId :%d, roleId :%d, army keyId :%d, hero list :%s, fight count so much !!!",
                    rlc.getCfgId(), player.roleId, army.getKeyId(), armyHeroString(army)));
        }

        //攻打遗迹战斗结束[进攻胜利, 防守胜利, 战斗异常]
        if (army.getHeroLeadCount() > 0) {
            if (defendList.isEmpty()) {
                handAttackRelicSuccess(rlc, player, army, nowSec);
            } else {//达到战斗次数上限还没打下来 就返回
                //never got here except fight error!!!
                LogUtil.error(String.format("cityId :%d, roleId :%d army keyId :%d, survival hero list :%s. defend remain size :%d",
                        rlc.getCfgId(), player.roleId, army.getKeyId(), armyHeroString(army), defendList.size()));
                retreatArmy(player, army, null, nowSec, false);
            }
        } else {
            //防守胜利
            handAttackRelicFailure(rlc, player, army, nowSec);
        }
    }

    private FightLogic fightLogic(RelicEntity rlc, Player attackPlayer, Army atkArmy, Player defendPlayer, Army defArmy, int nowSec, RelicEntity o1, Turple<Long, Integer> o2) {
        long fightId = incrAndGetFightId();
        try {
            Fighter attacker = fightService.createFighter(attackPlayer, atkArmy.getKeyId(), atkArmy.getHero());
            Fighter defender = fightService.createFighter(defendPlayer, defArmy.getKeyId(), defArmy.getHero());
            FightLogic fightLogic = new FightLogic(attacker, defender, true, WorldConstant.BATTLE_TYPE_HIS_REMAIN);
            fightLogic.packForm();
            fightLogic.fight();
            fightLogic.fightId = fightId;
            LogUtil.debug(String.format("his cityId :%d, fightId :%d, result :%s, attack roleId :%d, atk army keyId :%d, defend roleId :%d, army keyId :%d",
                    rlc.getCfgId(), fightId, fightLogic.getWinState(), attackPlayer.roleId, atkArmy.getKeyId(), defendPlayer.roleId, defArmy.getKeyId()));

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
                    o1.getDefendList().remove(o2);
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
                attackFightSuccess(fightLogic, rlc, report, attackPlayer, atkArmy, defendPlayer, nowSec);

                // 连杀广播
                attackPlayer.getPlayerRelic().incContinuousKillCnt();
                continuousKillBroadcast(attackPlayer);
            } else {
                attackFightFailure(fightLogic, rlc, report, attackPlayer, atkArmy, defendPlayer, nowSec);

                attackPlayer.getPlayerRelic().clearContinuousKillCnt();
            }
            // 地图刷新
            List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(atkArmy.getTarget(), attackPlayer.lord.getPos()));
            posList.add(atkArmy.getTarget());
            posList.add(attackPlayer.lord.getPos());
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
            //上报数数(攻击方)
            EventDataUp.battle(attackPlayer.account, attackPlayer.lord, attacker, "atk", "fightSuperMine",
                    String.valueOf(WorldConstant.BATTLE_TYPE_HIS_REMAIN), String.valueOf(fightLogic.getWinState()), attackPlayer.roleId, report.getRptPlayer().getAtkHeroList());
            //上报数数(防守方)
            EventDataUp.battle(defendPlayer.account, defendPlayer.lord, defender, "def", "fightSuperMine",
                    String.valueOf(WorldConstant.BATTLE_TYPE_HIS_REMAIN), String.valueOf(fightLogic.getWinState()), attackPlayer.roleId, report.getRptPlayer().getDefHeroList());
            return fightLogic;
        } catch (Exception e) {
            LogUtil.error(String.format("cityId :%d, attack roleId :%d, army keyId :%d, defend roleId :%d, army keyId :%d, fightId :%d, fight error!!!",
                    rlc.getCfgId(), attackPlayer.roleId, atkArmy.getKeyId(), defendPlayer.roleId, defArmy.getKeyId(), fightId), e);
        }
        return null;
    }

    /**
     * 连杀广播
     *
     * @param attackPlayer 进攻玩家
     */
    private void continuousKillBroadcast(Player attackPlayer) {
        int continuousKillCnt = attackPlayer.getPlayerRelic().getContinuousKillCnt();
        Integer chatId = ActParamConstant.RELIC_CHAT_KILL_BROADCAST_MAP.get(continuousKillCnt);
        if (Objects.nonNull(chatId)) {
            int camp = attackPlayer.getCamp();
            chatDataManager.sendSysChat(chatId, camp, 0, camp, attackPlayer.lord.getNick());
        }
    }

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
            CommonPb.RptHero rptHero = fightService.addExploitAndBuildRptHero(force, changeMap, AwardFrom.RELIC_FIGHT);
            if (rptHero != null) {
                rpt.addAtkHero(rptHero);
            }
        }
        for (Force force : defender.forces) {
            CommonPb.RptHero rptHero = fightService.addExploitAndBuildRptHero(force, changeMap, AwardFrom.RELIC_FIGHT);
            if (rptHero != null) {
                rpt.addDefHero(rptHero);
            }
        }
        // 回合战报
        rpt.setRecord(record);
        return worldService.createAtkPlayerReport(rpt.build(), nowSec); // 战报
    }


    private void attackFightSuccess(FightLogic fightLogic, RelicEntity rlc, CommonPb.Report.Builder report, Player attackPlayer, Army army,
                                    Player defendPlayer, int nowSec) {
        try {
            army.setState(ArmyConstant.ARMY_STATE_RELIC_BATTLE);
            if (Objects.isNull(fightLogic)) {
                LogUtil.common(String.format("cityId :%d, attack roleId :%d, army keyId :%d  occupy success with no fight...",
                        rlc.getCfgId(), attackPlayer.roleId, army.getKeyId()));
            } else {
                String atkNick = attackPlayer.lord.getNick();
                int attackKillScore = calcScoreByKillCount(fightLogic.getDefender().lost);//进攻杀敌积分
                relicService.addPlayerScoreHandle(attackPlayer, attackKillScore);
                mailDataManager.sendReportMail(attackPlayer, report, MailConstant.MOLD_HIS_REMAINS_ATTACK_SUCCESS, null, nowSec,
                        atkNick, defendPlayer.getCamp(), defendPlayer.lord.getLevel(), defendPlayer.lord.getNick(),//标题参数
                        rlc.getPos(), defendPlayer.lord.getNick(), defendPlayer.lord.getPos(), attackKillScore);//内容参数
                int defendKillScore = calcScoreByKillCount(fightLogic.getAttacker().lost);//防守杀敌积分
                relicService.addPlayerScoreHandle(defendPlayer, defendKillScore);
                mailDataManager.sendReportMail(defendPlayer, report, MailConstant.MOLD_HIS_REMAINS_DEFEND_FAILURE, null, nowSec,
                        defendPlayer.lord.getNick(), attackPlayer.getCamp(), attackPlayer.lord.getLevel(), attackPlayer.lord.getNick(),
                        rlc.getPos(), attackPlayer.lord.getNick(), attackPlayer.lord.getPos(), defendKillScore);
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    private void attackFightFailure(FightLogic fightLogic, RelicEntity rlc, CommonPb.Report.Builder report, Player attackPlayer, Army army,
                                    Player defendPlayer, int nowSec) {
        try {
            Lord atkLord = attackPlayer.lord;
            Lord defLord = defendPlayer.lord;
            //进攻战斗失败
            int atkKillScore = calcScoreByKillCount(fightLogic.getDefender().lost);
            relicService.addPlayerScoreHandle(attackPlayer, atkKillScore);
            mailDataManager.sendReportMail(attackPlayer, report, MailConstant.MOLD_HIS_REMAINS_ATTACK_FAILURE, null, nowSec,
                    attackPlayer.lord.getNick(), defendPlayer.getCamp(), defLord.getLevel(), defLord.getNick(),
                    rlc.getPos(), defLord.getNick(), defLord.getPos(), atkKillScore);
            //防守战斗成功
            int defKillScore = calcScoreByKillCount(fightLogic.getAttacker().lost);
            relicService.addPlayerScoreHandle(defendPlayer, defKillScore);
            mailDataManager.sendReportMail(defendPlayer, report, MailConstant.MOLD_HIS_REMAINS_DEFEND_SUCCESS, null, nowSec,
                    defLord.getNick(), atkLord.getCamp(), atkLord.getLevel(), atkLord.getNick(),
                    rlc.getPos(), atkLord.getNick(), atkLord.getPos(), defKillScore);
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    private void handAttackRelicSuccess(RelicEntity rlc, Player attackPlayer, Army army, int nowSec) {
        // 首次被占领时广播
        int camp = attackPlayer.getCamp();
        if (rlc.getHoldCamp() == 0) {
            chatDataManager.sendSysChat(ChatConst.CHAT_FIND_RELICS, camp, 0, camp, attackPlayer.lord.getNick(), rlc.getArea(), rlc.getPos());
        } else {
            // 所属权发生改变时广播
//            chatDataManager.sendSysChat(ChatConst.CHAT_RELICS_OCCUPY, camp, 0, camp, attackPlayer.lord.getNick(), camp);
        }

        //更换占领阵营，先计算原阵营的占领时间
        rlc.updCampHoldValue(nowSec);
//        int now = TimeHelper.getCurrentSecond();
        rlc.setHoldCamp(attackPlayer.getCamp());
        rlc.setStartHold(nowSec);
        List<Integer> posList = new ArrayList<>();
        posList.add(rlc.getPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));

        //玩家加入探索
        joinProbing(rlc, attackPlayer, army, nowSec);

        // 占领遗迹后重置连杀次数
        attackPlayer.getPlayerRelic().clearContinuousKillCnt();
    }

    private void handAttackRelicFailure(RelicEntity rlc, Player attackPlayer, Army army, int nowSec) {
        //进攻方行军返回
    }

    private int calcScoreByKillCount(int killCount) {
        return killCount / StaticDataMgr.getStaticRelicFraction(worldScheduleService.getCurrentSchduleId()).getKill();
    }

    private void subAndRetreatDeadHeroInArmy(Player player, Fighter fighter, Army army, Map<Long, ChangeInfo> changeMap, Map<Long, Integer> recoverMap, int nowSec) {
        // 通用损兵处理
        warService.subBattleHeroArm(fighter.forces, changeMap, AwardFrom.RELIC_FIGHT);
        // 记录将领累计损兵恢复
        Map<Integer, Integer> lostHpMap = army.getAndCreateIfAbsentTotalLostHpMap();
        Map<Integer, Integer> recoverHpMap = army.getAndCreateIfAbsentRecoverMap();
        fighter.forces.stream().filter(force -> force.totalLost > 0).forEach(force -> {
            int totalLost = force.totalLost + lostHpMap.getOrDefault(force.id, 0);
            lostHpMap.put(force.id, totalLost);
            int recoveryHp = (int) (ActParamConstant.RELIC_RECOVERY_RATIO / NumberUtil.TEN_THOUSAND_DOUBLE * totalLost);
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

    /**
     * 返回已经死亡的部队,返回部队时处理伤病恢复
     * 战斗中返回死亡的将领
     *
     * @param player p
     * @param army   a
     * @param nowSec s
     */
    public void retreatArmy(Player player, Army army, Map<Long, ChangeInfo> changeMap, int nowSec, boolean b) {
        try {
            Map<Integer, Integer> recoverMap = army.getRecoverMap();
            if (CheckNull.nonEmpty(recoverMap)) doRecoverArmy(player, army, recoverMap, changeMap);
            worldService.retreatArmy(player, army, nowSec);
            worldService.synRetreatArmy(player, army, nowSec);

            //check has army in probing
            if (b) {
                relicService.retreatCheckProbing(player, army);
            }
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


    private long incrAndGetFightId() {
        return globalDataManager.getGameGlobal().getGlobalRelic().incrAndGetFightId();
    }

    private String armyHeroString(Army army) {
        if (CheckNull.isEmpty(army.getHero())) return "";
        return army.getHero().stream()
                .map(twoInt -> new StringBuilder().append(twoInt.getV1()).append("_").append(twoInt.getV2()))
                .collect(Collectors.joining(","));
    }
}
