package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.RptHero;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb4.AttackSuperMineRq;
import com.gryphpoem.game.zw.pb.GamePb4.AttackSuperMineRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetSuperMineRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetSuperMineRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticActBandit;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticSuperMine;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.pojo.world.SuperGuard;
import com.gryphpoem.game.zw.resource.pojo.world.SuperMine;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName SuperMineService.java
 * @Description ????????????
 * @author QiuKun
 * @date 2018???7???14???
 */
@Component
public class SuperMineService {
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private MineService mineService;
    @Autowired
    private HeroService heroService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WarDataManager warDataManager;
    @Autowired
    private WarService warService;
    @Autowired
    private FightService fightService;
    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private ArmyService armyService;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private SeasonTalentService seasonTalentService;
    @Autowired
    private ActivityDataManager activityDataManager;

    /**
     * ???????????????????????????
     * 
     * @param block
     * @return
     */
    public List<SuperMine> getSuperMineByBlock(int block) {
        int area = MapHelper.blockToArea(block);
        if (area != WorldConstant.AREA_TYPE_13) {
            return Collections.emptyList();
        }
        return worldDataManager.getSuperMineMap().values().stream()
                .filter(sm -> block == MapHelper.block(sm.getPos()) && sm.isMapShow()).collect(Collectors.toList());
    }

    /**
     * ??????????????????
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetSuperMineRs getSuperMine(long roleId, GetSuperMineRq req) throws MwException {
        // Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetSuperMineRs.Builder builder = GetSuperMineRs.newBuilder();
        int camp = req.getCamp();
        List<SuperMine> smList = worldDataManager.getSuperMineCampMap().get(camp);
        if (!CheckNull.isEmpty(smList)) {
            int seqId = req.getSeqId();
            SuperMine sm = smList.stream().filter(s -> s.getSeqId() == seqId).findFirst().orElse(null);
            if (sm != null) {
                int now = TimeHelper.getCurrentSecond();
                builder.setSuperMine(PbHelper.createSuperMineDetailPb(sm, now, playerDataManager));
            }
        }
        return builder.build();
    }

    /**
     * ???????????? ??????,??????,??????
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AttackSuperMineRs attackSuperMine(long roleId, AttackSuperMineRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int pos = req.getPos();
        List<Integer> heroIdList = req.getHeroIdList(); // ??????????????????
        int processType = req.getProcessType(); // ???????????? 1 ??????, 2 ??????, 3??????

        SuperMine superMine = worldDataManager.getSuperMineMap().get(pos);
        if (superMine == null) {
            throw new MwException(GameError.SUPER_MINE_NOT_EXIST.getCode(), "?????????????????????, roleId:", roleId, ", pos:", pos);
        }

        if (processType == 1) {// 1 ??????
            return collectSuperMine(player, superMine, heroIdList);
        } else if (processType == 2) { // 2 ??????
            return helpSuperMine(player, superMine, heroIdList);
        } else if (processType == 3) {// 3 ??????
            return atkSuperMine(player, superMine, heroIdList);
        } else {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "???????????????, roleId:", roleId, ", pos:", pos,
                    ", processType:", processType);
        }

    }

    /**
     * ??????????????????
     * 
     * @param player
     * @param heroIdList
     * @param isCollect
     * @throws MwException
     */
    private void checkHeroState(Player player, List<Integer> heroIdList, int pos, boolean isCollect)
            throws MwException {
        long roleId = player.roleId;
        playerDataManager.autoAddArmy(player);
        // ??????????????????????????????
        if (isCollect) {
            if (heroIdList.size() != 1) {
                // ?????????????????????,????????????????????????
                throw new MwException(GameError.COLLECT_WORK_ONLYONE.getCode(), "??????????????????????????????, roleId:", roleId,
                        ", heroIdList.size:", heroIdList.size());
            }
            int stateAcqCount = 0; // ??????????????????????????????
            for (int heroId : player.heroAcq) {
                Hero h = player.heros.get(heroId);
                if (null != h && h.getState() == HeroConstant.HERO_STATE_COLLECT) {
                    stateAcqCount++;
                }
            }
            for (int heroId : player.heroBattle) {
                Hero h = player.heros.get(heroId);
                if (null != h && h.getState() == HeroConstant.HERO_STATE_COLLECT) {
                    stateAcqCount++;
                }
            }
            if (stateAcqCount >= 4) {
                throw new MwException(GameError.COLLECT_HERO_OVER_MAX.getCode(), "?????????????????????????????????, roleId:", roleId,
                        ", stateAcqCount:", stateAcqCount);
            }

            int cnt = 0;
            for (Army army : player.armys.values()) {
                if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT
                        || army.getType() == ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE) {
                    cnt++;
                    if (army.getTarget() == pos && army.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
                        throw new MwException(GameError.ALREADY_COLLECT_HERO.getCode(), "???????????????????????????????????????, roleId:",
                                roleId);
                    }
                }
            }
            if (cnt >= WorldConstant.MINE_MAX_CNT) {
                throw new MwException(GameError.MINE_MAX_NUM.getCode(), "??????????????????????????????, roleId:", roleId, ", pos:", pos);
            }

        }
        for (Integer heroId : heroIdList) {
            Hero hero = player.heros.get(heroId);
            if (null == hero) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "????????????????????????, roleId:", roleId, ", heroId:",
                        heroId);
            }

            if (!player.isOnBattleHero(heroId) && !player.isOnAcqHero(heroId)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "???????????????,?????????????????????????????? roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "?????????????????????, roleId:", roleId, ", heroId:",
                        heroId, ", state:", hero.getState());
            }

            if (hero.getCount() <= 0) {
                throw new MwException(GameError.HERO_NO_ARM.getCode(), "??????????????????, roleId:", roleId, ", heroId:", heroId,
                        ", count:", hero.getCount());
            }
            if (!isCollect) {
                // ???????????????????????????,????????????????????????
                if (hero.isOnAcq()) {
                    throw new MwException(GameError.ACQ_HERO_NOT_ATTACK.getCode(), "??????????????????????????????, roleId:", roleId);
                }
            } else {
                // ???????????????????????????, ?????????????????????
                if (hero.isCommando()) {
                    throw new MwException(GameError.COMMANDO_HERO_NOT_ATK.getCode(), "??????????????????????????????, roleId:",
                            roleId);
                }
            }
        }
    }

    /**
     * ??????????????????
     * 
     * @param player
     * @param superMine
     * @param heroIdList
     * @return
     * @throws MwException
     */
    private AttackSuperMineRs collectSuperMine(Player player, SuperMine superMine, List<Integer> heroIdList)
            throws MwException {
        int pos = superMine.getPos();
        long roleId = player.roleId;
        if (superMine.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.SUPER_MINE_CAMP_ERR.getCode(), "???????????????????????????????????????, roleId:", roleId);
        }
        if (!superMine.isProduceState()) {
            throw new MwException(GameError.SUPER_MINE_STATE_ERR.getCode(), "???????????????????????????, roleId:", roleId, " state:",
                    superMine.getState());

        }
        worldService.checkSameArea(player, pos);
        // ????????????????????????
        checkHeroState(player, heroIdList, pos, true);
        int now = TimeHelper.getCurrentSecond();
        // ???????????????????????????????????????
        Army army = armyService.checkAndcreateArmy(player, superMine.getPos(), heroIdList, now,
                ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE);
        army.setTargetId(superMine.getConfigId());
        // ???????????????
        armyService.addMarchAndChangeHeroState(player, army, pos);

        AttackSuperMineRs.Builder builder = AttackSuperMineRs.newBuilder();

        builder.addArmy(PbHelper.createArmyPb(army, false));
        return builder.build();
    }

    /**
     * ??????????????????
     * 
     * @param player
     * @param superMine
     * @param heroIdList
     * @return
     * @throws MwException
     */
    private AttackSuperMineRs atkSuperMine(Player player, SuperMine superMine, List<Integer> heroIdList)
            throws MwException {
        int pos = superMine.getPos();
        long roleId = player.roleId;
        if (superMine.getCamp() == player.lord.getCamp()) {
            throw new MwException(GameError.SAME_CAMP.getCode(), "??????????????????????????????, roleId:", roleId);
        }
        worldService.checkSameArea(player, pos);
        // ????????????????????????
        checkHeroState(player, heroIdList, pos, false);
        // ??????????????????,????????????????????????
        for (Army army : player.armys.values()) {
            if (army.getTarget() == pos && army.getState() == ArmyConstant.ARMY_STATE_MARCH) {
                throw new MwException(GameError.SUPER_MINE_ALREADY_ATK.getCode(), "????????????????????????????????????, roleId:", roleId);
            }
        }
        int now = TimeHelper.getCurrentSecond();
        Army army = armyService.checkAndcreateArmy(player, superMine.getPos(), heroIdList, now,
                ArmyConstant.ARMY_TYPE_ATK_SUPERMINE);
        // ??????battle
        Battle battle = new Battle();
        battle.setType(WorldConstant.BATTLE_TYPE_SUPER_MINE);
        battle.setBattleTime(army.getEndTime() - 1); // -1????????? ?????????????????????????????????
        battle.setBeginTime(now);
        battle.setPos(pos);
        battle.setSponsor(player);
        battle.setSponsorId(player.roleId);
        battle.setAtkCamp(player.lord.getCamp());
        battle.setDefCamp(superMine.getCamp());
        int armCount = 0;
        for (Integer heroId : heroIdList) {
            Hero hero = player.heros.get(heroId);
            armCount += hero.getCount();
        }
        battle.setAtkArm(armCount); // ???????????????
        battle.setDefArm(superMine.defArmyCnt());// ???????????????
        army.setBattleId(battle.getBattleId());
        player.armys.put(army.getKeyId(), army);
        warDataManager.addBattle(player, battle); // ????????????
        //?????????????????????????????????
//        syncAttackRoleStatus(superMine, player, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_1);

        // ?????????
        worldService.removeProTect(player,AwardFrom.SUPER_MINE_WAR,battle.getPos());
        // ???????????????
        armyService.addMarchAndChangeHeroState(player, army, pos);
        superMine.getBattleIds().add(battle.getBattleId());

        AttackSuperMineRs.Builder builder = AttackSuperMineRs.newBuilder();

        builder.addArmy(PbHelper.createArmyPb(army, false));
        builder.setBattle(PbHelper.createBattlePb(battle));
        // // ????????????
        // superMine.getCollectArmy().forEach(sg -> {
        //     Player p = playerDataManager.getPlayer(sg.getArmy().getLordId());
        //     if (p != null) PushMessageUtil.pushMessage(p.account, PushConstant.SUPER_MINE_ATTCK);
        // });
        return builder.build();
    }

    /**
     * ??????????????????
     * 
     * @param player
     * @param superMine
     * @param heroIdList
     * @return
     * @throws MwException
     */
    private AttackSuperMineRs helpSuperMine(Player player, SuperMine superMine, List<Integer> heroIdList)
            throws MwException {
        int pos = superMine.getPos();
        long roleId = player.roleId;
        if (superMine.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "???????????????????????????????????????, roleId:", roleId);
        }
        worldService.checkSameArea(player, pos);
        // ????????????????????????
        checkHeroState(player, heroIdList, pos, false);
        // Set<Long> roleList = superMine.getCollectArmy().stream().map(sg -> sg.getArmy().getLordId())
        // .collect(Collectors.toSet());
        // if (roleList.contains(roleId)) {
        // throw new MwException(GameError.SUPER_MINE_COLLECT_NOT_HELP.getCode(), "???????????????????????????????????????, roleId:", roleId);
        // }
        if (superMine.getHelpArmy().size() >= Constant.WALL_HELP_MAX_NUM) {
            throw new MwException(GameError.WALL_HELP_FULL.getCode(), "??????????????????????????????????????????, roleId:", roleId);
        }

        int armCount = 0;
        for (Integer heroId : heroIdList) {
            Hero hero = player.heros.get(heroId);
            armCount += hero.getCount();
        }
        int marchTime = worldService.marchTime(player, pos);
        int needFood = worldService.checkMarchFood(player, marchTime, armCount); // ????????????
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);
        AttackSuperMineRs.Builder builder = AttackSuperMineRs.newBuilder();
        int now = TimeHelper.getCurrentSecond();
        for (Integer heroId : heroIdList) {
            List<TwoInt> form = new ArrayList<>();
            Hero hero = player.heros.get(heroId);
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
            form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));
            Army army = new Army(player.maxKey(), ArmyConstant.ARMY_TYPE_HELP_SUPERMINE, pos,
                    ArmyConstant.ARMY_STATE_MARCH, form, marchTime, now + marchTime, player.getDressUp());
            army.setLordId(roleId);
            army.setOriginPos(player.lord.getPos());
            Optional.ofNullable(medalDataManager.getHeroMedalByHeroIdAndIndex(player, hero.getHeroId(), MedalConst.HERO_MEDAL_INDEX_0))
                    .ifPresent(medal -> {
                        army.setHeroMedals(Collections.singletonList(PbHelper.createMedalPb(medal)));
                    });

            player.armys.put(army.getKeyId(), army);
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
            // ??????????????????
            March march = new March(player, army);
            worldDataManager.addMarch(march);

            builder.addArmy(PbHelper.createArmyPb(army, false));
        }
        // ??????????????????
        List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(pos, player.lord.getPos()));
        posList.add(pos);
        posList.add(player.lord.getPos());
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        return builder.build();
    }

    /*---------------------------------?????????????????? start------------------------------------*/
    /**
     * ???????????????????????????????????????
     * 
     * @param player
     * @param army
     */
    public void retreatCollectArmy(Player player, Army army, int type) {
        int now = TimeHelper.getCurrentSecond();
        int pos = army.getTarget();
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm == null) {
            // ??????????????? ,??????????????????
            worldService.retreatArmy(player, army, now, type);
            return;
        }
        if (army.getState() == ArmyConstant.ARMY_STATE_MARCH) {
            // ??????????????????
            worldService.retreatArmy(player, army, now, type);
            // ??????????????????
            // Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            // mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_SUPER_MINE_COLLECT_MIDWAY_RETURN, null,
            // now,
            // sm.getConfigId(), xy.getA(), xy.getB());

        } else {
            SuperGuard sg = sm.getCollectArmy().stream().filter(sperGuard -> sperGuard.isSameArmy(army)).findFirst()
                    .orElse(null);
            if (sg != null) {
                // ????????????
                finishCollect(sg, now, false);
                sm.removeCollectArmy(sg.getArmy().getLordId(), sg.getArmy().getKeyId());
                boolean isReCalc = sm.reCalcAllCollectArmyTime(now);// ????????????????????????
                if (isReCalc && sm.isProduceState()) { // ?????????????????????????????????,????????????
                    syncCollectArmyState(sm, now);
                }
            } else {
                // ????????????????????? ,??????????????????
                LogUtil.debug("not found superGuard, return army, roleId: ", player.getLordId(), ", army: ", army);
                worldService.retreatArmy(player, army, now, type);
            }
        }

        //??????????????????
//        syncRetreatStatus(sm, player);
    }

    /**
     * ??????????????????????????????
     * 
     * @param player
     * @param army
     */
    public void retreatAtkArmy(Player player, Army army, int type) {
        int now = TimeHelper.getCurrentSecond();
        int pos = army.getTarget();
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm == null) {
            // ??????????????? ,??????????????????
            worldService.retreatArmy(player, army, now, type);
            return;
        }
        // ????????????
//        Integer battleId = army.getBattleId() == null ? 0 : army.getBattleId();
//        Battle battle;
//        if (battleId.intValue() == 0) {
//            battle = null;
//        } else {
//            battle = warDataManager.removeBattleById(army.getBattleId());
//        }
        if (!CheckNull.isNull(army.getBattleId()) && army.getBattleId() > 0)
            warDataManager.removeSuperMineBattleById(army.getBattleId());
        //??????????????????
//        syncAttackRoleStatus(sm, player, CheckNull.isNull(battle) ? army.getEndTime() - 1 : battle.getBattleTime(), WorldConstant.ATTACK_ROLE_0);
        checkAndRemoveBattle(sm);
        // ??????????????????????????????
        worldService.retreatArmy(player, army, now, type);
    }

    private void checkAndRemoveBattle(SuperMine sm) {
        Iterator<Integer> it = sm.getBattleIds().iterator();
        while (it.hasNext()) {
            Integer bId = it.next();
            if (!warDataManager.getBattleMap().containsKey(bId)) {
                it.remove();
            }
        }
    }

    /**
     * ??????????????????????????????
     * 
     * @param player
     * @param army
     */
    public void retreatHelpArmy(Player player, Army army, int type) {
        int now = TimeHelper.getCurrentSecond();
        int pos = army.getTarget();
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm == null) {
            // ??????????????? ,??????????????????
            worldService.retreatArmy(player, army, now, type);
            return;
        }
        sm.removeHelpArmy(army.getLordId(), army.getKeyId());// ??????
        worldService.retreatArmy(player, army, now, type);
        //??????????????????
//        syncRetreatStatus(sm, player);
    }

    /*---------------------------------?????????????????? end------------------------------------*/

    /**
     * ??????????????????
     * 
     * @param sg
     * @param now
     */
    private void finishCollect(SuperGuard sg, int now) {
        finishCollect(sg, now, true);
    }

    /**
     * ??????????????????
     * 
     * @param sg
     * @param now
     * @param syncMarch ?????????????????????
     */
    private void finishCollect(SuperGuard sg, int now, boolean syncMarch) {
        long roleId = sg.getArmy().getLordId();
        Player player = playerDataManager.getPlayer(roleId);
        try {
            StaticSuperMine sSm = StaticWorldDataMgr.getSuperMineById(sg.getSuperMine().getConfigId());
            int collectTime = sg.calcCollectedTime(now); // ????????????
            // ???????????????????????????????????????
            Date startDate = new Date(sg.getArmyArriveTime() * 1000L);
            boolean effect = mineService.hasCollectEffect(player, sSm.getMineType(), startDate, sg.getArmy()); // ?????????????????????
            int gainRes = (int) Math.floor((collectTime * 1.0 / Constant.HOUR) * sSm.getSpeed()); // ??????????????????
            sg.getSuperMine().addConvertRes(gainRes);// ????????????????????????
            gainRes = mineService.calcGainCollectEffectCnt(player, gainRes, sSm.getMineType(), startDate, sg.getArmy());
            List<CommonPb.Award> grab = new ArrayList<>(); // ??????
            grab.add(PbHelper.createAwardPb(sSm.getReward().get(0).get(0), sSm.getReward().get(0).get(1), gainRes));
            sg.getArmy().setCollectTime(collectTime); // ????????????????????????
            // ????????????????????????
            List<CommonPb.Award> collectDrop = activityDataManager.getCollectDrop(player, sSm.getMineId(), collectTime, StaticActBandit.ACT_HIT_DROP_TYPE_5);
            grab.addAll(collectDrop);
            sg.getArmy().setGrab(grab);
            // ??????????????????
            Hero hero = player.heros.get(sg.getArmy().getHero().get(0).getV1());
            int addExp = (int) Math.ceil(collectTime * 1.0 / Constant.MINUTE) * 20;// ??????????????????
            addExp = heroService.adaptHeroAddExp(player, addExp);
            addExp = heroService.addHeroExp(hero, addExp, player.lord.getLevel(), player);

            CommonPb.MailCollect collect = PbHelper.createMailCollectPb(collectTime, hero, addExp, grab, effect);
            // ????????????
            Turple<Integer, Integer> xy = MapHelper.reducePos(sg.getSuperMine().getPos());
            mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_SUPER_MINE_COLLECT, collect, now,
                    sg.getSuperMine().getConfigId(), gainRes, sg.getSuperMine().getConfigId(), xy.getA(), xy.getB());

            //????????????-???????????? ?????????
            ActivityDiaoChanService.completeTask(player, ETask.COLLECT_RES,grab.get(0).getId(),grab.get(0).getCount());
            TaskService.processTask(player, ETask.COLLECT_RES,grab.get(0).getId(),grab.get(0).getCount());
        } catch (Exception e) {
            LogUtil.error("???????????????????????? ?????? roleId:", player.roleId, ", army:", sg.getArmy());
        } finally {
            // ????????????
            if (syncMarch) {
                worldService.retreatArmyByDistance(player, sg.getArmy(), now);
            } else {
                worldService.retreatArmy(player, sg.getArmy(), now);
            }
            worldService.synRetreatArmy(player, sg.getArmy(), now);
            //??????????????????
//            syncRetreatStatus(sg.getSuperMine(), player);
        }
    }

    /*----------------------------------????????????????????? start------------------------------*/
    /**
     * ?????????????????????
     */
    public void superMineStateChangeTimer() {
        int now = TimeHelper.getCurrentSecond();
        worldDataManager.getSuperMineCampMap().values().stream().flatMap(smList -> smList.stream())
                .forEach(sm -> dispatchProcessSuperMine(sm, now));
    }

    /**
     * ????????????
     * 
     * @param sm
     * @param now
     */
    private void dispatchProcessSuperMine(SuperMine sm, int now) {
        try {
            if (sm.getState() == SuperMine.STATE_PRODUCED) {// ?????????
                processProducedState(sm, now);
            } else if (sm.getState() == SuperMine.STATE_STOP) {// ?????????
                processStopState(sm, now);
            } else if (sm.getState() == SuperMine.STATE_RESET) { // ?????????
                processResetState(sm, now);
            }
        } catch (Exception e) {
            LogUtil.error(e, "????????????????????? sm:", sm);
        }
    }

    /**
     * ????????????????????????
     * 
     * @param sm
     * @param now
     */
    private void processProducedState(SuperMine sm, int now) {
        // ??????????????????
        int remaining = sm.calcCollectRemaining(now);
        if (remaining <= 0) { // ???????????????????????????
            resetStateSuperMine(sm, now);
        } else { // ??????????????????????????????
            List<SuperGuard> rmSgList = sm.getCollectArmy().stream()
                    .filter(sg -> sg.getArmy().getEndTime() < now && sg.getArmy().getEndTime() != -1)
                    .collect(Collectors.toList());
            if (!CheckNull.isEmpty(rmSgList)) {
                for (SuperGuard sg : rmSgList) {
                    finishCollect(sg, now);// ????????????
                    // ????????????
                    sm.removeCollectArmy(sg.getArmy().getLordId(), sg.getArmy().getKeyId());
                    sm.reCalcAllCollectArmyTime(now);// ????????????????????????
                }
            }
            // ??????????????????
            checkHelpArmyReturn(sm, now);
        }
    }

    /**
     * ??????????????????
     * 
     * @param sm
     * @param now
     */
    private void processResetState(SuperMine sm, int now) {
        if (sm.getNextTime() < now) {
            worldDataManager.refreshSuperMine(sm, now); // ???????????????????????????
        }
    }

    /**
     * ??????????????????
     * 
     * @param sm
     * @param now
     */
    private void processStopState(SuperMine sm, int now) {
        if (sm.getNextTime() < now) {
            resetStateSuperMine(sm, now);
        } else {
            checkHelpArmyReturn(sm, now);
        }
    }

    /**
     * ??????????????????
     * 
     * @param sm
     * @param now
     */
    public void resetStateSuperMine(SuperMine sm, int now) {
        // ??????????????????,??????????????????
        List<Integer> posList = returnAllArmy(sm, now, false);
        worldDataManager.getSuperMineMap().remove(sm.getPos());
        if (Objects.nonNull(sm)) {
            //??????????????????, ??????????????????
            for (Integer battleId : sm.getBattleIds()) {
                if (CheckNull.isNull(battleId))
                    continue;
                warDataManager.removeSuperMineBattleById(battleId);
            }
        }

        sm.setResetState(now);
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
    }

    /**
     * ???????????????????????????????????????????????????
     * 
     * @param sm
     * @param now
     * @param isSyncMap ??????????????????
     */
    private List<Integer> returnAllArmy(SuperMine sm, int now, boolean isSyncMap) {
        List<Integer> posList = new ArrayList<>(); // ????????????
        posList.add(sm.getPos());
        for (SuperGuard sg : sm.getCollectArmy()) {
            finishCollect(sg, now, false);
            Player player = playerDataManager.getPlayer(sg.getArmy().getLordId());
            if (player != null) {
                posList.add(player.lord.getPos());
            }
        }
        for (Army army : sm.getHelpArmy()) {// ?????????????????????
            Player player = playerDataManager.getPlayer(army.getLordId());
            if (player != null) {
                worldService.retreatArmy(player, army, now);
                worldService.synRetreatArmy(player, army, now);
                posList.add(player.lord.getPos());
            }
        }

        // ????????????
        sm.getCollectArmy().clear();
        sm.getHelpArmy().clear();
        if (isSyncMap) {
            // ????????????
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        }
        return posList;
    }

    /**
     * ????????????????????????????????????
     * 
     * @param sm
     * @param now
     */
    private void checkHelpArmyReturn(SuperMine sm, int now) {
        Iterator<Army> it = sm.getHelpArmy().iterator();
        while (it.hasNext()) {
            Army helpArmy = it.next();
            if (helpArmy.getEndTime() < now) {
                it.remove();
                Player player = playerDataManager.getPlayer(helpArmy.getLordId());
                if (player != null) {
                    Turple<Integer, Integer> xy = MapHelper.reducePos(sm.getPos());
                    worldService.retreatArmyByDistance(player, helpArmy, now);
//                    syncRetreatStatus(sm, player);
                    mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_SUPER_MINE_HELP_RETURN, null, now,
                            sm.getConfigId(), xy.getA(), xy.getB(), helpArmy.getHero().get(0).getV1(), sm.getConfigId(),
                            xy.getA(), xy.getB(), helpArmy.getHero().get(0).getV1());

                }
            }
        }

    }

    /*----------------------------------????????????????????? end------------------------------*/

    /*----------------------------------?????????????????? start------------------------------*/
    /**
     * ??????????????????????????????????????????
     * 
     * @param player
     * @param army
     * @param now
     */
    public void marchEndcollectSuperMineLogic(Player player, Army army, int now) {
        int pos = army.getTarget();
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm == null) {
            // ????????????
            worldService.noMineRetreat(player, army, now);
        } else {
            if (sm.isResetState()) { // ??????????????????
                worldService.noMineRetreat(player, army, now);
                return;
            }
            List<SuperGuard> collectArmyList = sm.getCollectArmy();
            if (collectArmyList.size() >= SuperMine.MAX_COLLECT_SIZE) {
                // ????????????????????? ,?????????????????????
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_SUPER_MINE_COLLECT_FILL, null, now,
                        sm.getConfigId(), xy.getA(), xy.getB());
                worldService.retreatArmyByDistance(player, army, now);
                return;
            }
            int remaining = sm.calcCollectRemaining(now);// ????????????
            if (remaining <= 0) { // ???????????????
                worldService.retreatArmyByDistance(player, army, now);
                return;
            }
            // ????????????
            sm.joinCollect(player, army, now);
            // ????????????
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUPER_MINE_CNT, 1);
            //????????????????????????
//            syncCollectStatus(sm, player);

            // ?????? ????????????
            if (sm.isStopState()) {
                Turple<Integer, Integer> xy = MapHelper.reducePos(sm.getPos());
                City city = worldDataManager.getCityById(sm.getCityId());
                StaticCity sCity = StaticWorldDataMgr.getCityMap().get(sm.getCityId());
                Turple<Integer, Integer> cityXy = MapHelper.reducePos(sCity.getCityPos());
                mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_SUPER_MINE_STOP, null, now,
                        sm.getConfigId(), xy.getA(), xy.getB(), city.getCamp(), sm.getCityId(), cityXy.getA(),
                        cityXy.getB(), sm.getConfigId(), xy.getA(), xy.getB());
            }
        }
    }

    /**
     * ????????????????????????????????????
     * @param superMine
     * @param player
     */
    private void syncCollectStatus(SuperMine superMine, Player player) {
        if (ObjectUtils.isEmpty(superMine.getBattleIds()) || !player.isLogin) {
            return;
        }

        Battle battle;
        for (Integer battleId : superMine.getBattleIds()) {
            if (CheckNull.isNull(battleId))
                continue;

            battle = warDataManager.getBattleMap().get(battleId);
            if (CheckNull.isNull(battle))
                continue;
            worldService.syncAttackRole(player, battle.getSponsor().lord, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_1);
        }
    }

    /**
     * ?????????????????????????????????
     * @param superMine
     * @param player
     */
    private void syncRetreatStatus(SuperMine superMine, Player player) {
        if (ObjectUtils.isEmpty(superMine.getBattleIds()) || !player.isLogin) {
            return;
        }

        Battle battle;
        for (Integer battleId : superMine.getBattleIds()) {
            if (CheckNull.isNull(battleId))
                continue;

            battle = warDataManager.getBattleMap().get(battleId);
            if (CheckNull.isNull(battle))
                continue;
            worldService.syncAttackRole(player, battle.getSponsor().lord, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_0);
        }
    }

    /**
     * ??????????????????
     * 
     * @param player
     * @param army
     * @param now
     */
    public void marchEndAtkSuperMineLogic(Player player, Army army, int now) {
        int pos = army.getTarget();
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm == null) {
            worldService.noTargetRetreat(player, army, now); // ??????????????????
        } else {
            try {
                if (sm.isResetState()) { // ??????????????????
                    worldService.noTargetRetreat(player, army, now);
                    return;
                }
                // ????????????
                LogUtil.debug("????????????????????????.... pos:", army.getTarget());
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                List<Army> collect = sm.getCollectArmy().stream().map(SuperGuard::getArmy).collect(Collectors.toList());
                List<Army> allArmy = new ArrayList<>();
                // ????????????
                Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();

                allArmy.addAll(sm.getHelpArmy()); // ???????????????
                allArmy.addAll(collect);
                if (allArmy.isEmpty()) { // ??????????????? ??????
                    // ????????????
                    worldService.retreatArmyByDistance(player, army, now);
                    mailDataManager.sendReportMail(player, null, MailConstant.MOLD_SUPER_MINE_ATK_RUN_AWAY, null, now,
                            recoverArmyAwardMap, sm.getConfigId(), xy.getA(), xy.getB(), sm.getConfigId(), xy.getA(),
                            xy.getB());
                    return;
                }
                Fighter attacker = fightService.createFighter(player, army.getHero());
                Fighter defender = fightService.createFighterByArmy(allArmy);
                FightLogic fightLogic = new FightLogic(attacker, defender, true);
                fightLogic.fight();

                //????????????-??????????????????
                ActivityDiaoChanService.killedAndDeathTask0(attacker,true,true);
                ActivityDiaoChanService.killedAndDeathTask0(defender,true,true);

                boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS; // ????????????

                Map<Long, ChangeInfo> changeMap = new HashMap<>(); // ????????????????????????
                // ????????????
                if (attacker.lost > 0) warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.SUPER_MINE_BATTLE);
                if (defender.lost > 0) warService.subBattleHeroArm(defender.forces, changeMap, AwardFrom.SUPER_MINE_BATTLE);

                // ????????????????????????????????????
                medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
                medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
                //????????????????????????---????????????
                seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
                seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);

                // ????????????
                CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
                CommonPb.Record record = fightLogic.generateRecord();
                rpt.setResult(atkSuccess);
                rpt.setAttack(PbHelper.createRptMan(player.lord.getPos(), player.lord.getNick(), player.lord.getVip(),
                        player.lord.getLevel()));// ??????????????????
                rpt.setDefRptOther(PbHelper.createRptOtherPb(1, sm.getConfigId(), sm.getPos(), sm.getCamp(), null));
                rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, player.lord.getCamp(),
                        player.lord.getNick(), player.lord.getPortrait(), player.getDressUp().getCurPortraitFrame()));
                rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, sm.getCamp(), null, 0, 0));
                // ????????????
                addHeroExploit(attacker.forces, rpt, AwardFrom.SUPER_MINE_BATTLE, true, changeMap);
                addHeroExploit(defender.forces, rpt, AwardFrom.SUPER_MINE_BATTLE, false, changeMap);
                // ????????????
                rpt.setRecord(record);

                CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now); // ??????

                Turple<Integer, Integer> atkXy = MapHelper.reducePos(player.lord.getPos());

                List<Player> defPlayers = allArmy.stream().map(am -> am.getLordId()).distinct()
                        .map(roleId -> playerDataManager.getPlayer(roleId)).collect(Collectors.toList());
                // ????????????
                if (atkSuccess) { // ???????????????
                    mailDataManager.sendReportMail(player, report, MailConstant.MOLD_SUPER_ATK_SUCCESS, null, now,
                            recoverArmyAwardMap, player.lord.getNick(), sm.getCamp(), sm.getConfigId(),
                            player.lord.getNick(), atkXy.getA(), atkXy.getB(), sm.getCamp(), sm.getConfigId(), xy.getA(),
                            xy.getB());
                    defPlayers.forEach(p -> {
                        mailDataManager.sendReportMail(p, report, MailConstant.MOLD_SUPER_DEF_FAIL, null, now,
                                recoverArmyAwardMap, sm.getCamp(), sm.getConfigId(), player.lord.getNick(), sm.getCamp(),
                                sm.getConfigId(), xy.getA(), xy.getB(), player.lord.getNick(), atkXy.getA(), atkXy.getB());
                    });
                } else {
                    mailDataManager.sendReportMail(player, report, MailConstant.MOLD_SUPER_ATK_FAIL, null, now,
                            recoverArmyAwardMap, player.lord.getNick(), sm.getCamp(), sm.getConfigId(),
                            player.lord.getNick(), atkXy.getA(), atkXy.getB(), sm.getCamp(), sm.getConfigId(), xy.getA(),
                            xy.getB());
                    defPlayers.forEach(p -> {
                        mailDataManager.sendReportMail(p, report, MailConstant.MOLD_SUPER_DEF_SUCCESS, null, now,
                                recoverArmyAwardMap, sm.getCamp(), sm.getConfigId(), player.lord.getNick(), sm.getCamp(),
                                sm.getConfigId(), xy.getA(), xy.getB(), player.lord.getNick(), atkXy.getA(), atkXy.getB());
                    });
                }

                // ??????????????????
                worldService.retreatArmy(player, army, now);
                // ????????????????????????,??????????????????
                checkHeroCntAndReturn(sm, now);

                // ????????????
                List<Integer> posList = MapHelper
                        .getAreaStartPos(MapHelper.getLineAcorss(army.getTarget(), player.lord.getPos()));
                posList.add(army.getTarget());
                posList.add(player.lord.getPos());
                EventBus.getDefault()
                        .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));

                //????????????(?????????)
                EventDataUp.battle(player.account, player.lord,attacker,"atk", CheckNull.isNull(army.getBattleId()) ? "" : String.valueOf(army.getBattleId()), String.valueOf(WorldConstant.BATTLE_TYPE_SUPER_MINE),
                        String.valueOf(fightLogic.getWinState()),player.roleId, rpt.getAtkHeroList());
                //????????????(?????????)
                defPlayers.forEach(p -> {
                    EventDataUp.battle(p.account, p.lord,defender,"def", CheckNull.isNull(army.getBattleId()) ? "" : String.valueOf(army.getBattleId()), String.valueOf(WorldConstant.BATTLE_TYPE_SUPER_MINE),
                            String.valueOf(fightLogic.getWinState()),player.roleId, rpt.getDefHeroList());
                });
            } finally {
                //????????????????????????
//                syncAttackRoleStatus(sm, player, army.getEndTime() - 1, WorldConstant.ATTACK_ROLE_0);
            }
        }
    }

    /**
     * ?????????????????????
     * @param superMine
     * @param player
     * @param battleTime
     */
    private void syncAttackRoleStatus(SuperMine superMine, Player player, int battleTime, int attackRoleStatus) {
        //??????????????????
        Player defencePlayer;
        if (!ObjectUtils.isEmpty(superMine.getCollectArmy())) {
            for (SuperGuard superGuard : superMine.getCollectArmy()) {
                if (ObjectUtils.isEmpty(superGuard) || ObjectUtils.isEmpty(superGuard.getArmy()))
                    continue;

                defencePlayer = playerDataManager.getPlayer(superGuard.getArmy().getLordId());
                if (CheckNull.isNull(defencePlayer))
                    continue;

                if (defencePlayer.isLogin) {
                    worldService.syncAttackRole(defencePlayer, player.lord, battleTime, attackRoleStatus);
                }
            }
        }
        if (!ObjectUtils.isEmpty(superMine.getHelpArmy())) {
            for (Army army : superMine.getHelpArmy()) {
                if (CheckNull.isNull(army))
                    continue;

                defencePlayer = playerDataManager.getPlayer(army.getLordId());
                if (CheckNull.isNull(defencePlayer))
                    continue;

                if (defencePlayer.isLogin) {
                    worldService.syncAttackRole(defencePlayer, player.lord, battleTime, attackRoleStatus);
                }
            }
        }
    }

    /**
     * ????????????
     * 
     * @param sm
     * @param now
     */
    private void checkHeroCntAndReturn(SuperMine sm, int now) {
        Iterator<Army> helpIt = sm.getHelpArmy().iterator();
        while (helpIt.hasNext()) {
            Army army = helpIt.next();
            Player player = playerDataManager.getPlayer(army.getLordId());
            if (player == null) {
                helpIt.remove();
                continue;
            }
            Hero hero = player.heros.get(army.getHero().get(0).getV1());
            if (hero.getCount() <= 0) { // ???????????????
                helpIt.remove(); // ??????
                worldService.retreatArmy(player, army, now);
                worldService.synRetreatArmy(player, army, now);
                // ???????????????
                Turple<Integer, Integer> xy = MapHelper.reducePos(sm.getPos());
                mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_SUPER_MINE_HELP_KILL, null, now,
                        sm.getConfigId(), xy.getA(), xy.getB(), hero.getHeroId(), sm.getConfigId(), xy.getA(),
                        xy.getB(), hero.getHeroId());
            } else {// ????????????
                List<TwoInt> heroList = new ArrayList<>();
                heroList.add(PbHelper.createTwoIntPb(hero.getHeroId(), hero.getCount()));
                army.setHero(heroList);
            }
        }

        List<SuperGuard> finishCollect = new ArrayList<>(); // ?????????????????????

        Iterator<SuperGuard> collectIt = sm.getCollectArmy().iterator();
        while (collectIt.hasNext()) {
            SuperGuard sg = collectIt.next();
            Army army = sg.getArmy();
            Player player = playerDataManager.getPlayer(sg.getArmy().getLordId());
            if (player == null) {
                collectIt.remove();
                continue;
            }
            Hero hero = player.heros.get(army.getHero().get(0).getV1());
            if (hero.getCount() <= 0) { // ???????????????
                finishCollect.add(sg);
            } else {// ????????????
                List<TwoInt> heroList = new ArrayList<>();
                heroList.add(PbHelper.createTwoIntPb(hero.getHeroId(), hero.getCount()));
                army.setHero(heroList);
            }
        }

        for (SuperGuard sg : finishCollect) {
            finishCollect(sg, now, false);
            // ??????????????????
            Player defPlayer = playerDataManager.getPlayer(sg.getArmy().getLordId());
            worldService.synRetreatArmy(defPlayer, sg.getArmy(), now);
            // ????????????
            sm.removeCollectArmy(sg.getArmy().getLordId(), sg.getArmy().getKeyId());
            sm.reCalcAllCollectArmyTime(now);// ????????????????????????
        }
    }

    /**
     * ????????????
     * 
     * @param pos
     * @param battleId
     */
    public void removeBattle(int pos, int battleId) {
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm != null) {
            Iterator<Integer> it = sm.getBattleIds().iterator();
            while (it.hasNext()) {
                Integer bId = it.next();
                if (bId.intValue() == battleId) {
                    it.remove();
                }
            }
        }
    }

    private void addHeroExploit(List<Force> forces, CommonPb.RptAtkPlayer.Builder rpt, AwardFrom from,
            boolean isAttacker, Map<Long, ChangeInfo> changeMap) {
        for (Force force : forces) {
            // ????????????
            ChangeInfo info = changeMap.get(force.ownerId);
            if (null == info) {
                info = ChangeInfo.newIns();
                changeMap.put(force.ownerId, info);
            }
            int type = force.roleType;
            int kill = force.killed;
            int heroId = force.id;
            int exploit = force.totalLost;// ?????? = ?????????
            if (force.roleType == Constant.Role.PLAYER) {
                Player player = playerDataManager.getPlayer(force.ownerId);
                if (player == null) continue;
                Hero hero = player.heros.get(force.id);
                if (hero == null) continue;
                warService.addExploit(player, exploit, info, from);// ?????????
                RptHero rptHero = PbHelper.createRptHero(type, kill, exploit, heroId, player.lord.getNick(),
                        hero.getLevel(), 0, force.lost, hero);
                if (isAttacker) {
                    rpt.addAtkHero(rptHero);
                } else {
                    rpt.addDefHero(rptHero);
                }
            }
        }
    }

    /**
     * ??????????????????
     * 
     * @param player
     * @param army
     * @param now
     */
    public void marchEndHelpSuperMineLogic(Player player, Army army, int now) {
        int pos = army.getTarget();
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm == null) {
            worldService.noMineRetreat(player, army, now); // ????????????
        } else {
            if (sm.isResetState()) { // ??????????????????
                worldService.retreatArmyByDistance(player, army, now);
                return;
            }
            if (sm.getHelpArmy().size() >= Constant.WALL_HELP_MAX_NUM) {
                // ??????????????????
                worldService.retreatArmyByDistance(player, army, now);// ??????
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                int heroId = army.getHero().get(0).getV1();
                mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_SUPER_MINE_HELP_FILL, null, now,
                        sm.getConfigId(), xy.getA(), xy.getB(), heroId, sm.getConfigId(), xy.getA(), xy.getB(), heroId);
                return;
            }
            // ??????????????????????????????
            int maxTime = Constant.ARMY_STATE_GUARD_TIME * TimeHelper.HOUR_S;// ??????????????????????????????
            army.setDuration(maxTime);
            army.setEndTime(now + maxTime);
            army.setState(ArmyConstant.ARMY_STATE_GUARD);
            for (TwoInt twoInt : army.getHero()) {
                Hero hero = player.heros.get(twoInt.getV1());
                hero.setState(ArmyConstant.ARMY_STATE_GUARD);
            }
            sm.getHelpArmy().add(army); // ????????????????????????
        }
        //?????????????????????
//        syncCollectStatus(sm, player);
    }

    /*----------------------------------?????????????????? end------------------------------*/

    /**
     * ?????????????????????????????????
     */
    public void gmRefreshSuperMine() {
        int now = TimeHelper.getCurrentSecond();
        worldDataManager.getSuperMineCampMap().values().stream().flatMap(list -> list.stream()).forEach(sm -> {
            // ??????????????????,??????????????????
            resetStateSuperMine(sm, now);
            // ????????????
            worldDataManager.refreshSuperMine(sm, now); // ???????????????????????????
        });
    }

    public void gmClearAndRefreshSuperMineByCamp(int camp) {
        int now = TimeHelper.getCurrentSecond();
        List<SuperMine> listSm = worldDataManager.getSuperMineCampMap().get(camp);
        if (!CheckNull.isEmpty(listSm)) {
            // ??????????????????,??????????????????
            listSm.forEach(sm -> resetStateSuperMine(sm, now));
            worldDataManager.getSuperMineCampMap().remove(camp);
            worldDataManager.addCampSuperMine(camp); // ????????????
        }
    }

    /**
     * Gm???????????????????????????????????????
     */
    public void gmClearAndRefreshSuperMine() {
        int now = TimeHelper.getCurrentSecond();
        worldDataManager.getSuperMineCampMap().values().stream().flatMap(list -> list.stream()).forEach(sm -> {
            // ??????????????????,??????????????????
            resetStateSuperMine(sm, now);
        });
        // ??????
        worldDataManager.getSuperMineCampMap().clear();
        worldDataManager.getSuperMineMap().clear();
        SuperMine.SEQ_ID = 0; // seqid??????
        // ????????????
        worldDataManager.addCampSuperMine(1);
        worldDataManager.addCampSuperMine(2);
        worldDataManager.addCampSuperMine(3);
    }

    /**
     * ????????????????????????
     * 
     * @param city
     */
    public void changeSuperState(City city) {
        int now = TimeHelper.getCurrentSecond();
        StaticCity sCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
        if (sCity != null && sCity.getType() == WorldConstant.CITY_TYPE_8) {
            Turple<Integer, Integer> cityXy = MapHelper.reducePos(sCity.getCityPos());
            worldDataManager.getSuperMineCampMap().values().stream().flatMap(list -> list.stream())
                    .filter(sm -> sm.getCityId() == city.getCityId()).forEach(sm -> {
                        if (sm.getCamp() == city.getCamp()) {
                            // ????????????
                            sm.setStopToProducedState(now);
                            // army????????????
                            syncCollectArmyState(sm, now);
                        } else {
                            // ????????????
                            sm.setStopState(now);
                            // army????????????
                            syncCollectArmyState(sm, now);
                            // ???????????????
                            Turple<Integer, Integer> xy = MapHelper.reducePos(sm.getPos());
                            List<SuperGuard> sgList = sm.getCollectArmy();
                            for (SuperGuard sg : sgList) {
                                Player recvPlayer = playerDataManager.getPlayer(sg.getArmy().getLordId());
                                mailDataManager.sendCollectMail(recvPlayer, null, MailConstant.MOLD_SUPER_MINE_STOP,
                                        null, now, sm.getConfigId(), xy.getA(), xy.getB(), city.getCamp(),
                                        sCity.getCityId(), cityXy.getA(), cityXy.getB(), sm.getConfigId(), xy.getA(),
                                        xy.getB());
                            }
                        }
                    });
        }
    }

    /**
     * ????????????????????????
     * 
     * @param sm
     * @param now
     */
    private void syncCollectArmyState(SuperMine sm, int now) {
        for (SuperGuard sg : sm.getCollectArmy()) {
            // ??????????????????
            Player defPlayer = playerDataManager.getPlayer(sg.getArmy().getLordId());
            worldService.synRetreatArmy(defPlayer, sg.getArmy(), now);
        }
    }

}
