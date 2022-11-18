package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.RptHero;
import com.gryphpoem.game.zw.pb.GamePb4.AttackSuperMineRq;
import com.gryphpoem.game.zw.pb.GamePb4.AttackSuperMineRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetSuperMineRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetSuperMineRs;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticSuperMine;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
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
 * @author QiuKun
 * @ClassName SuperMineService.java
 * @Description 超级矿点
 * @date 2018年7月14日
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

    /**
     * 根据块获取超级矿点
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
     * 获取矿点信息
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
     * 超级矿点 采集,驻防,攻打
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AttackSuperMineRs attackSuperMine(long roleId, AttackSuperMineRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int pos = req.getPos();
        List<Integer> heroIdList = req.getHeroIdList(); // 派出去的将领
        int processType = req.getProcessType(); // 操作类型 1 采集, 2 驻防, 3攻击

        SuperMine superMine = worldDataManager.getSuperMineMap().get(pos);
        if (superMine == null) {
            throw new MwException(GameError.SUPER_MINE_NOT_EXIST.getCode(), "超级矿点不存在, roleId:", roleId, ", pos:", pos);
        }

        if (processType == 1) {// 1 采集
            return collectSuperMine(player, superMine, heroIdList);
        } else if (processType == 2) { // 2 驻防
            return helpSuperMine(player, superMine, heroIdList);
        } else if (processType == 3) {// 3 攻击
            return atkSuperMine(player, superMine, heroIdList);
        } else {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "操作不存在, roleId:", roleId, ", pos:", pos,
                    ", processType:", processType);
        }

    }

    /**
     * 检测将领状态
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
        // 如果目标是采集的时候
        if (isCollect) {
            if (heroIdList.size() != 1) {
                // 目标为采集的时,只能排出一个将领
                throw new MwException(GameError.COLLECT_WORK_ONLYONE.getCode(), "将领采集时只能有一个, roleId:", roleId,
                        ", heroIdList.size:", heroIdList.size());
            }
            int stateAcqCount = 0; // 有多少个将领正在采集
            for (PartnerHero partnerHero : player.getPlayerFormation().getHeroAcq()) {
                if (HeroUtil.isEmptyPartner(partnerHero)) continue;
                Hero h = partnerHero.getPrincipalHero();
                if (null != h && h.getState() == HeroConstant.HERO_STATE_COLLECT) {
                    stateAcqCount++;
                }
            }
            for (PartnerHero partnerHero : player.getPlayerFormation().getHeroBattle()) {
                if (HeroUtil.isEmptyPartner(partnerHero)) continue;
                Hero h = partnerHero.getPrincipalHero();
                if (null != h && h.getState() == HeroConstant.HERO_STATE_COLLECT) {
                    stateAcqCount++;
                }
            }
            if (stateAcqCount >= 4) {
                throw new MwException(GameError.COLLECT_HERO_OVER_MAX.getCode(), "当前采集将领已超过上限, roleId:", roleId,
                        ", stateAcqCount:", stateAcqCount);
            }

            int cnt = 0;
            for (Army army : player.armys.values()) {
                if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT
                        || army.getType() == ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE) {
                    cnt++;
                    if (army.getTarget() == pos && army.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
                        throw new MwException(GameError.ALREADY_COLLECT_HERO.getCode(), "该矿点已经派出将领进行采集, roleId:",
                                roleId);
                    }
                }
            }
            if (cnt >= WorldConstant.MINE_MAX_CNT) {
                throw new MwException(GameError.MINE_MAX_NUM.getCode(), "已达将领达到采集上限, roleId:", roleId, ", pos:", pos);
            }

        }
        for (Integer heroId : heroIdList) {
            Hero hero = player.heros.get(heroId);
            if (null == hero) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "玩家没有这个将领, roleId:", roleId, ", heroId:",
                        heroId);
            }

            if (!player.isOnBattleHero(heroId) && !player.isOnAcqHero(heroId)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "将领未上阵,又未在采集队列中上阵 roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不在空闲中, roleId:", roleId, ", heroId:",
                        heroId, ", state:", hero.getState());
            }

            if (hero.getCount() <= 0) {
                throw new MwException(GameError.HERO_NO_ARM.getCode(), "将领没有带兵, roleId:", roleId, ", heroId:", heroId,
                        ", count:", hero.getCount());
            }
            if (!isCollect) {
                // 如果是一个非采集点,但派出了采集将领
                if (hero.isOnAcq()) {
                    throw new MwException(GameError.ACQ_HERO_NOT_ATTACK.getCode(), "采集将领只能进行采集, roleId:", roleId);
                }
            } else {
                // 如果是一个特攻将领, 但是派出来采集
                if (hero.isCommando()) {
                    throw new MwException(GameError.COMMANDO_HERO_NOT_ATK.getCode(), "特攻将领不能进行采集, roleId:",
                            roleId);
                }
            }
        }
    }

    /**
     * 超级矿点采集
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
            throw new MwException(GameError.SUPER_MINE_CAMP_ERR.getCode(), "超级矿点非同一阵营不能采集, roleId:", roleId);
        }
        if (!superMine.isProduceState()) {
            throw new MwException(GameError.SUPER_MINE_STATE_ERR.getCode(), "超级矿点状态不正确, roleId:", roleId, " state:",
                    superMine.getState());

        }
        worldService.checkSameArea(player, pos);
        // 检测自己派出将领
        checkHeroState(player, heroIdList, pos, true);
        int now = TimeHelper.getCurrentSecond();
        // 检测补给是否满足并创建部队
        Army army = armyService.checkAndcreateArmy(player, superMine.getPos(), heroIdList, now,
                ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE);
        army.setTargetId(superMine.getConfigId());
        // 添加行军线
        armyService.addMarchAndChangeHeroState(player, army, pos);

        AttackSuperMineRs.Builder builder = AttackSuperMineRs.newBuilder();

        builder.addArmy(PbHelper.createArmyPb(army, false));
        return builder.build();
    }

    /**
     * 攻击超级矿点
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
            throw new MwException(GameError.SAME_CAMP.getCode(), "同阵营矿点不能被攻击, roleId:", roleId);
        }
        worldService.checkSameArea(player, pos);
        // 检测自己派出将领
        checkHeroState(player, heroIdList, pos, false);
        // 检测自己部队,是否已经派去部队
        for (Army army : player.armys.values()) {
            if (army.getTarget() == pos && army.getState() == ArmyConstant.ARMY_STATE_MARCH) {
                throw new MwException(GameError.SUPER_MINE_ALREADY_ATK.getCode(), "已经派出部队攻击超级矿点, roleId:", roleId);
            }
        }
        int now = TimeHelper.getCurrentSecond();
        Army army = armyService.checkAndcreateArmy(player, superMine.getPos(), heroIdList, now,
                ArmyConstant.ARMY_TYPE_ATK_SUPERMINE);
        // 创建battle
        Battle battle = new Battle();
        battle.setType(WorldConstant.BATTLE_TYPE_SUPER_MINE);
        battle.setBattleTime(army.getEndTime() - 1); // -1秒原因 ，让战斗状态先进行移除
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
        battle.setAtkArm(armCount); // 进攻方兵力
        battle.setDefArm(superMine.defArmyCnt());// 防守方兵力
        army.setBattleId(battle.getBattleId());
        player.armys.put(army.getKeyId(), army);
        warDataManager.addBattle(player, battle); // 添加战斗
        //添加防守方所有人的报警
//        syncAttackRoleStatus(superMine, player, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_1);

        // 破罩子
        worldService.removeProTect(player, AwardFrom.SUPER_MINE_WAR, battle.getPos());
        // 添加行军线
        armyService.addMarchAndChangeHeroState(player, army, pos);
        superMine.getBattleIds().add(battle.getBattleId());

        AttackSuperMineRs.Builder builder = AttackSuperMineRs.newBuilder();

        builder.addArmy(PbHelper.createArmyPb(army, false));
        builder.setBattle(PbHelper.createBattlePb(battle));
        // // 手机推送
        // superMine.getCollectArmy().forEach(sg -> {
        //     Player p = playerDataManager.getPlayer(sg.getArmy().getLordId());
        //     if (p != null) PushMessageUtil.pushMessage(p.account, PushConstant.SUPER_MINE_ATTCK);
        // });
        return builder.build();
    }

    /**
     * 驻防超级矿点
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
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "超级矿点非同一阵营不能驻防, roleId:", roleId);
        }
        worldService.checkSameArea(player, pos);
        // 检测自己派出将领
        checkHeroState(player, heroIdList, pos, false);
        // Set<Long> roleList = superMine.getCollectArmy().stream().map(sg -> sg.getArmy().getLordId())
        // .collect(Collectors.toSet());
        // if (roleList.contains(roleId)) {
        // throw new MwException(GameError.SUPER_MINE_COLLECT_NOT_HELP.getCode(), "有部队在该矿点采集不能驻防, roleId:", roleId);
        // }
        if (superMine.getHelpArmy().size() >= Constant.WALL_HELP_MAX_NUM) {
            throw new MwException(GameError.WALL_HELP_FULL.getCode(), "该矿点坐标驻防满了，不能驻防, roleId:", roleId);
        }

        int armCount = 0;
        for (Integer heroId : heroIdList) {
            Hero hero = player.heros.get(heroId);
            armCount += hero.getCount();
        }
        int marchTime = worldService.marchTime(player, pos);
        int needFood = worldService.checkMarchFood(player, marchTime, armCount); // 检查补给
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);
        AttackSuperMineRs.Builder builder = AttackSuperMineRs.newBuilder();
        int now = TimeHelper.getCurrentSecond();
        for (Integer heroId : heroIdList) {
            PartnerHero partnerHero = player.getPlayerFormation().getPartnerHero(heroId);
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;

            List<CommonPb.PartnerHeroIdPb> form = new ArrayList<>();
            Hero hero = player.heros.get(heroId);
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
            form.add(partnerHero.convertTo());
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
            // 添加行军路线
            March march = new March(player, army);
            worldDataManager.addMarch(march);

            builder.addArmy(PbHelper.createArmyPb(army, false));
        }
        // 区域变化推送
        List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(pos, player.lord.getPos()));
        posList.add(pos);
        posList.add(player.lord.getPos());
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        return builder.build();
    }

    /*---------------------------------部队返回处理 start------------------------------------*/

    /**
     * 玩家主动返回正在采集的部队
     *
     * @param player
     * @param army
     */
    public void retreatCollectArmy(Player player, Army army, int type) {
        int now = TimeHelper.getCurrentSecond();
        int pos = army.getTarget();
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm == null) {
            // 没找到矿点 ,还是让他返回
            worldService.retreatArmy(player, army, now, type);
            return;
        }
        if (army.getState() == ArmyConstant.ARMY_STATE_MARCH) {
            // 中途返回邮件
            worldService.retreatArmy(player, army, now, type);
            // 发送邮件通知
            // Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            // mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_SUPER_MINE_COLLECT_MIDWAY_RETURN, null,
            // now,
            // sm.getConfigId(), xy.getA(), xy.getB());

        } else {
            SuperGuard sg = sm.getCollectArmy().stream().filter(sperGuard -> sperGuard.isSameArmy(army)).findFirst()
                    .orElse(null);
            if (sg != null) {
                // 移除部队
                finishCollect(sg, now, false);
                sm.removeCollectArmy(sg.getArmy().getLordId(), sg.getArmy().getKeyId());
                boolean isReCalc = sm.reCalcAllCollectArmyTime(now);// 重新计算分布时间
                if (isReCalc && sm.isProduceState()) { // 如果重新计算了部队时间,需要推送
                    syncCollectArmyState(sm, now);
                }
            } else {
                // 没找到采集信息 ,还是让他返回
                LogUtil.debug("not found superGuard, return army, roleId: ", player.getLordId(), ", army: ", army);
                worldService.retreatArmy(player, army, now, type);
            }
        }

        //同步取消报警
//        syncRetreatStatus(sm, player);
    }

    /**
     * 超级矿点攻击主动返回
     *
     * @param player
     * @param army
     */
    public void retreatAtkArmy(Player player, Army army, int type) {
        int now = TimeHelper.getCurrentSecond();
        int pos = army.getTarget();
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm == null) {
            // 没找到矿点 ,还是让他返回
            worldService.retreatArmy(player, army, now, type);
            return;
        }
        // 取消战斗
//        Integer battleId = army.getBattleId() == null ? 0 : army.getBattleId();
//        Battle battle;
//        if (battleId.intValue() == 0) {
//            battle = null;
//        } else {
//            battle = warDataManager.removeBattleById(army.getBattleId());
//        }
        if (!CheckNull.isNull(army.getBattleId()) && army.getBattleId() > 0)
            warDataManager.removeSuperMineBattleById(army.getBattleId());
        //取消战斗报警
//        syncAttackRoleStatus(sm, player, CheckNull.isNull(battle) ? army.getEndTime() - 1 : battle.getBattleTime(), WorldConstant.ATTACK_ROLE_0);
        checkAndRemoveBattle(sm);
        // 检测战斗是否还有空的
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
     * 超级矿点驻防主动返回
     *
     * @param player
     * @param army
     */
    public void retreatHelpArmy(Player player, Army army, int type) {
        int now = TimeHelper.getCurrentSecond();
        int pos = army.getTarget();
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm == null) {
            // 没找到矿点 ,还是让他返回
            worldService.retreatArmy(player, army, now, type);
            return;
        }
        sm.removeHelpArmy(army.getLordId(), army.getKeyId());// 移除
        worldService.retreatArmy(player, army, now, type);
        //同步取消报警
//        syncRetreatStatus(sm, player);
    }

    /*---------------------------------部队返回处理 end------------------------------------*/

    /**
     * 结束采集处理
     *
     * @param sg
     * @param now
     */
    private void finishCollect(SuperGuard sg, int now) {
        finishCollect(sg, now, true);
    }

    /**
     * 结束采集处理
     *
     * @param sg
     * @param now
     * @param syncMarch 是否需要同步线
     */
    private void finishCollect(SuperGuard sg, int now, boolean syncMarch) {
        long roleId = sg.getArmy().getLordId();
        Player player = playerDataManager.getPlayer(roleId);
        try {
            StaticSuperMine sSm = StaticWorldDataMgr.getSuperMineById(sg.getSuperMine().getConfigId());
            int collectTime = sg.calcCollectedTime(now); // 采集时间
            // 获取季节加成和活动加成比例
            Date startDate = new Date(sg.getArmyArriveTime() * 1000L);
            boolean effect = mineService.hasCollectEffect(player, sSm.getMineType(), startDate, sg.getArmy()); // 是否有采集加成
            int gainRes = (int) Math.floor((collectTime * 1.0 / Constant.HOUR) * sSm.getSpeed()); // 计算采集数量
            sg.getSuperMine().addConvertRes(gainRes);// 结算采集到的资源
            gainRes = mineService.calcGainCollectEffectCnt(player, gainRes, sSm.getMineType(), startDate, sg.getArmy());
            List<CommonPb.Award> grab = new ArrayList<>(); // 奖励
            grab.add(PbHelper.createAwardPb(sSm.getReward().get(0).get(0), sSm.getReward().get(0).get(1), gainRes));
            sg.getArmy().setCollectTime(collectTime); // 设置部队采集时间
            sg.getArmy().setGrab(grab);
            // 给将领加经验
            Hero hero = player.heros.get(sg.getArmy().getHero().get(0).getPrincipleHeroId());
            int addExp = (int) Math.ceil(collectTime * 1.0 / Constant.MINUTE) * 20;// 将领采集经验
            addExp = heroService.adaptHeroAddExp(player, addExp);
//            addExp = worldService.addDeputyHeroExp(hero, addExp, sg.getArmy().getHero().get(0), player);

            CommonPb.MailCollect collect = PbHelper.createMailCollectPb(collectTime, hero, addExp, grab, effect);
            // 采集邮件
            Turple<Integer, Integer> xy = MapHelper.reducePos(sg.getSuperMine().getPos());
            mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_SUPER_MINE_COLLECT, collect, now,
                    sg.getSuperMine().getConfigId(), gainRes, sg.getSuperMine().getConfigId(), xy.getA(), xy.getB());

            //貂蝉任务-采集资源 超级矿
            ActivityDiaoChanService.completeTask(player, ETask.COLLECT_RES, grab.get(0).getId(), grab.get(0).getCount());
            TaskService.processTask(player, ETask.COLLECT_RES, grab.get(0).getId(), grab.get(0).getCount());
        } catch (Exception e) {
            LogUtil.error("超级矿点采集完成 出错 roleId:", player.roleId, ", army:", sg.getArmy());
        } finally {
            // 返回部队
            if (syncMarch) {
                worldService.retreatArmyByDistance(player, sg.getArmy(), now);
            } else {
                worldService.retreatArmy(player, sg.getArmy(), now);
            }
            worldService.synRetreatArmy(player, sg.getArmy(), now);
            //同步取消报警
//            syncRetreatStatus(sg.getSuperMine(), player);
        }
    }

    /*----------------------------------定时器状态处理 start------------------------------*/

    /**
     * 读秒定时器执行
     */
    public void superMineStateChangeTimer() {
        int now = TimeHelper.getCurrentSecond();
        worldDataManager.getSuperMineCampMap().values().stream().flatMap(smList -> smList.stream())
                .forEach(sm -> dispatchProcessSuperMine(sm, now));
    }

    /**
     * 处理矿点
     *
     * @param sm
     * @param now
     */
    private void dispatchProcessSuperMine(SuperMine sm, int now) {
        try {
            if (sm.getState() == SuperMine.STATE_PRODUCED) {// 生产中
                processProducedState(sm, now);
            } else if (sm.getState() == SuperMine.STATE_STOP) {// 停产中
                processStopState(sm, now);
            } else if (sm.getState() == SuperMine.STATE_RESET) { // 重置中
                processResetState(sm, now);
            }
        } catch (Exception e) {
            LogUtil.error(e, "超级采集点出错 sm:", sm);
        }
    }

    /**
     * 处理生产状态矿点
     *
     * @param sm
     * @param now
     */
    private void processProducedState(SuperMine sm, int now) {
        // 计算矿点资源
        int remaining = sm.calcCollectRemaining(now);
        if (remaining <= 0) { // 矿点没矿了都回家吧
            resetStateSuperMine(sm, now);
        } else { // 检测矿点每个驻军时间
            List<SuperGuard> rmSgList = sm.getCollectArmy().stream()
                    .filter(sg -> sg.getArmy().getEndTime() < now && sg.getArmy().getEndTime() != -1)
                    .collect(Collectors.toList());
            if (!CheckNull.isEmpty(rmSgList)) {
                for (SuperGuard sg : rmSgList) {
                    finishCollect(sg, now);// 结束采集
                    // 移除部队
                    sm.removeCollectArmy(sg.getArmy().getLordId(), sg.getArmy().getKeyId());
                    sm.reCalcAllCollectArmyTime(now);// 重新计算分布时间
                }
            }
            // 驻防部队检测
            checkHelpArmyReturn(sm, now);
        }
    }

    /**
     * 重置状态处理
     *
     * @param sm
     * @param now
     */
    private void processResetState(SuperMine sm, int now) {
        if (sm.getNextTime() < now) {
            worldDataManager.refreshSuperMine(sm, now); // 找位置进行随机刷新
        }
    }

    /**
     * 停产状态处理
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
     * 充值超级矿点
     *
     * @param sm
     * @param now
     */
    public void resetStateSuperMine(SuperMine sm, int now) {
        // 撤回所有部队,包括驻防部队
        List<Integer> posList = returnAllArmy(sm, now, false);
        worldDataManager.getSuperMineMap().remove(sm.getPos());
        if (Objects.nonNull(sm)) {
            //超级矿点重置, 移除矿点战斗
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
     * 返回该矿点的所有采集部队和驻防部队
     *
     * @param sm
     * @param now
     * @param isSyncMap 是否同步地图
     */
    private List<Integer> returnAllArmy(SuperMine sm, int now, boolean isSyncMap) {
        List<Integer> posList = new ArrayList<>(); // 推送的点
        posList.add(sm.getPos());
        for (SuperGuard sg : sm.getCollectArmy()) {
            finishCollect(sg, now, false);
            Player player = playerDataManager.getPlayer(sg.getArmy().getLordId());
            if (player != null) {
                posList.add(player.lord.getPos());
            }
        }
        for (Army army : sm.getHelpArmy()) {// 驻防部队也返回
            Player player = playerDataManager.getPlayer(army.getLordId());
            if (player != null) {
                worldService.retreatArmy(player, army, now);
                worldService.synRetreatArmy(player, army, now);
                posList.add(player.lord.getPos());
            }
        }

        // 清空部队
        sm.getCollectArmy().clear();
        sm.getHelpArmy().clear();
        if (isSyncMap) {
            // 同一通知
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        }
        return posList;
    }

    /**
     * 检测驻防部队是否可以返回
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
                            sm.getConfigId(), xy.getA(), xy.getB(), helpArmy.getHero().get(0).getPrincipleHeroId(), sm.getConfigId(),
                            xy.getA(), xy.getB(), helpArmy.getHero().get(0).getPrincipleHeroId());

                }
            }
        }

    }

    /*----------------------------------定时器状态处理 end------------------------------*/

    /*----------------------------------行军结束处理 start------------------------------*/

    /**
     * 采集部队到超级矿点的逻辑处理
     *
     * @param player
     * @param army
     * @param now
     */
    public void marchEndcollectSuperMineLogic(Player player, Army army, int now) {
        int pos = army.getTarget();
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm == null) {
            // 矿点丢失
            worldService.noMineRetreat(player, army, now);
        } else {
            if (sm.isResetState()) { // 状态不对返回
                worldService.noMineRetreat(player, army, now);
                return;
            }
            List<SuperGuard> collectArmyList = sm.getCollectArmy();
            if (collectArmyList.size() >= SuperMine.MAX_COLLECT_SIZE) {
                // 人满了返回部队 ,可能需要发邮件
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_SUPER_MINE_COLLECT_FILL, null, now,
                        sm.getConfigId(), xy.getA(), xy.getB());
                worldService.retreatArmyByDistance(player, army, now);
                return;
            }
            int remaining = sm.calcCollectRemaining(now);// 剩余资源
            if (remaining <= 0) { // 没有资源了
                worldService.retreatArmyByDistance(player, army, now);
                return;
            }
            // 加入采集
            sm.joinCollect(player, army, now);
            // 战令任务
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUPER_MINE_CNT, 1);
            //同步有战斗即报警
//            syncCollectStatus(sm, player);

            // 停产 邮件发送
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
     * 同步刚到采集点的部队报警
     *
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
     * 同步撤回部队被攻击报警
     *
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
     * 攻击超级矿点
     *
     * @param player
     * @param army
     * @param now
     */
    public void marchEndAtkSuperMineLogic(Player player, Army army, int now) {
        int pos = army.getTarget();
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm == null) {
            worldService.noTargetRetreat(player, army, now); // 进攻目标丢失
        } else {
            try {
                if (sm.isResetState()) { // 状态不对返回
                    worldService.noTargetRetreat(player, army, now);
                    return;
                }
                // 战斗逻辑
                LogUtil.debug("矿点战斗逻辑开始.... pos:", army.getTarget());
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                List<Army> collect = sm.getCollectArmy().stream().map(SuperGuard::getArmy).collect(Collectors.toList());
                List<Army> allArmy = new ArrayList<>();
                // 兵力恢复
                Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();

                allArmy.addAll(sm.getHelpArmy()); // 驻防在前面
                allArmy.addAll(collect);
                if (allArmy.isEmpty()) { // 发现没驻兵 返回
                    // 部队返回
                    worldService.retreatArmyByDistance(player, army, now);
                    mailDataManager.sendReportMail(player, null, MailConstant.MOLD_SUPER_MINE_ATK_RUN_AWAY, null, now,
                            recoverArmyAwardMap, sm.getConfigId(), xy.getA(), xy.getB(), sm.getConfigId(), xy.getA(),
                            xy.getB());
                    return;
                }
                Fighter attacker = fightService.createFighter(player, army.getHero());
                Fighter defender = fightService.createFighterByArmy(allArmy);
                FightLogic fightLogic = new FightLogic(attacker, defender, true);
                fightLogic.start();

                //貂蝉任务-杀敌阵亡数量
                ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
                ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

                boolean atkSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS; // 是否胜利

                Map<Long, ChangeInfo> changeMap = new HashMap<>(); // 记录需要推送的值
                // 损兵处理
                if (attacker.lost > 0)
                    warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.SUPER_MINE_BATTLE);
                if (defender.lost > 0)
                    warService.subBattleHeroArm(defender.forces, changeMap, AwardFrom.SUPER_MINE_BATTLE);

                // 执行勋章白衣天使特技逻辑
                medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
                medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
                //执行赛季天赋技能---伤病恢复
                seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
                seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);

                // 战斗记录
                CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
                BattlePb.BattleRoundPb record = fightLogic.generateRecord();
                rpt.setResult(atkSuccess);
                rpt.setAttack(PbHelper.createRptMan(player.lord.getPos(), player.lord.getNick(), player.lord.getVip(),
                        player.lord.getLevel()));// 进攻方的信息
                rpt.setDefRptOther(PbHelper.createRptOtherPb(1, sm.getConfigId(), sm.getPos(), sm.getCamp(), null));
                rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, player.lord.getCamp(),
                        player.lord.getNick(), player.lord.getPortrait(), player.getDressUp().getCurPortraitFrame()));
                rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, sm.getCamp(), null, 0, 0));
                // 计算军工
                addHeroExploit(attacker.forces, rpt, AwardFrom.SUPER_MINE_BATTLE, true, changeMap);
                addHeroExploit(defender.forces, rpt, AwardFrom.SUPER_MINE_BATTLE, false, changeMap);
                // 回合战报
                rpt.setRecord(record);

                CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now); // 战报

                Turple<Integer, Integer> atkXy = MapHelper.reducePos(player.lord.getPos());

                List<Player> defPlayers = allArmy.stream().map(am -> am.getLordId()).distinct()
                        .map(roleId -> playerDataManager.getPlayer(roleId)).collect(Collectors.toList());
                // 发送战报
                if (atkSuccess) { // 进攻方胜利
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

                // 进攻部队返回
                worldService.retreatArmy(player, army, now);
                // 重新计算所有兵力,该回家的回家
                checkHeroCntAndReturn(sm, now);

                // 地图刷新
                List<Integer> posList = MapHelper
                        .getAreaStartPos(MapHelper.getLineAcorss(army.getTarget(), player.lord.getPos()));
                posList.add(army.getTarget());
                posList.add(player.lord.getPos());
                EventBus.getDefault()
                        .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));

                //上报数数(攻击方)
                EventDataUp.battle(player.account, player.lord, attacker, "atk", CheckNull.isNull(army.getBattleId()) ? "" : String.valueOf(army.getBattleId()), String.valueOf(WorldConstant.BATTLE_TYPE_SUPER_MINE),
                        String.valueOf(fightLogic.getWinState()), player.roleId, rpt.getAtkHeroList());
                //上报数数(防守方)
                defPlayers.forEach(p -> {
                    EventDataUp.battle(p.account, p.lord, defender, "def", CheckNull.isNull(army.getBattleId()) ? "" : String.valueOf(army.getBattleId()), String.valueOf(WorldConstant.BATTLE_TYPE_SUPER_MINE),
                            String.valueOf(fightLogic.getWinState()), player.roleId, rpt.getDefHeroList());
                });
            } finally {
                //取消被攻击人报警
//                syncAttackRoleStatus(sm, player, army.getEndTime() - 1, WorldConstant.ATTACK_ROLE_0);
            }
        }
    }

    /**
     * 添加被攻击报警
     *
     * @param superMine
     * @param player
     * @param battleTime
     */
    private void syncAttackRoleStatus(SuperMine superMine, Player player, int battleTime, int attackRoleStatus) {
        //添加战斗报警
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
     * 更新兵力
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
            Hero hero = player.heros.get(army.getHero().get(0).getPrincipleHeroId());
            if (hero.getCount() <= 0) { // 没有兵回家
                helpIt.remove(); // 移除
                worldService.retreatArmy(player, army, now);
                worldService.synRetreatArmy(player, army, now);
                // 驻防被击杀
                Turple<Integer, Integer> xy = MapHelper.reducePos(sm.getPos());
                mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_SUPER_MINE_HELP_KILL, null, now,
                        sm.getConfigId(), xy.getA(), xy.getB(), hero.getHeroId(), sm.getConfigId(), xy.getA(),
                        xy.getB(), hero.getHeroId());
            } else {// 更新兵力
                List<CommonPb.PartnerHeroIdPb> heroList = new ArrayList<>();
                CommonPb.PartnerHeroIdPb.Builder builder = CommonPb.PartnerHeroIdPb.newBuilder();
                builder.setPrincipleHeroId(hero.getHeroId());
                if (CheckNull.nonEmpty(army.getHero().get(0).getDeputyHeroIdList())) {
                    builder.addAllDeputyHeroId(army.getHero().get(0).getDeputyHeroIdList());
                }
                builder.setCount(hero.getCount());
                heroList.add(builder.build());
                army.setHero(heroList);
            }
        }

        List<SuperGuard> finishCollect = new ArrayList<>(); // 结束采集的部队

        Iterator<SuperGuard> collectIt = sm.getCollectArmy().iterator();
        while (collectIt.hasNext()) {
            SuperGuard sg = collectIt.next();
            Army army = sg.getArmy();
            Player player = playerDataManager.getPlayer(sg.getArmy().getLordId());
            if (player == null) {
                collectIt.remove();
                continue;
            }
            Hero hero = player.heros.get(army.getHero().get(0).getPrincipleHeroId());
            if (hero.getCount() <= 0) { // 没有兵回家
                finishCollect.add(sg);
            } else {// 更新兵力
                List<CommonPb.PartnerHeroIdPb> heroList = new ArrayList<>();
                CommonPb.PartnerHeroIdPb.Builder builder = CommonPb.PartnerHeroIdPb.newBuilder();
                builder.setPrincipleHeroId(hero.getHeroId());
                if (CheckNull.nonEmpty(army.getHero().get(0).getDeputyHeroIdList())) {
                    builder.addAllDeputyHeroId(army.getHero().get(0).getDeputyHeroIdList());
                }
                builder.setCount(hero.getCount());
                heroList.add(builder.build());
                army.setHero(heroList);
            }
        }

        for (SuperGuard sg : finishCollect) {
            finishCollect(sg, now, false);
            // 部队状态推送
            Player defPlayer = playerDataManager.getPlayer(sg.getArmy().getLordId());
            worldService.synRetreatArmy(defPlayer, sg.getArmy(), now);
            // 移除部队
            sm.removeCollectArmy(sg.getArmy().getLordId(), sg.getArmy().getKeyId());
            sm.reCalcAllCollectArmyTime(now);// 重新计算分布时间
        }
    }

    /**
     * 移除战斗
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
            // 奖励记录
            ChangeInfo info = changeMap.get(force.ownerId);
            if (null == info) {
                info = ChangeInfo.newIns();
                changeMap.put(force.ownerId, info);
            }
            int type = force.roleType;
            int kill = force.killed;
            int heroId = force.id;
            int exploit = force.totalLost;// 军工 = 杀敌数
            if (force.roleType == Constant.Role.PLAYER) {
                Player player = playerDataManager.getPlayer(force.ownerId);
                if (player == null) continue;
                Hero hero = player.heros.get(force.id);
                if (hero == null) continue;
                warService.addExploit(player, exploit, info, from);// 加军工
                RptHero rptHero = PbHelper.createRptHero(type, kill, exploit, force, player.lord.getNick(),
                        hero.getLevel(), 0, force.lost);
                if (isAttacker) {
                    rpt.addAtkHero(rptHero);
                } else {
                    rpt.addDefHero(rptHero);
                }
            }
        }
    }

    /**
     * 超级矿点驻防
     *
     * @param player
     * @param army
     * @param now
     */
    public void marchEndHelpSuperMineLogic(Player player, Army army, int now) {
        int pos = army.getTarget();
        SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
        if (sm == null) {
            worldService.noMineRetreat(player, army, now); // 矿点丢失
        } else {
            if (sm.isResetState()) { // 状态不对返回
                worldService.retreatArmyByDistance(player, army, now);
                return;
            }
            if (sm.getHelpArmy().size() >= Constant.WALL_HELP_MAX_NUM) {
                // 驻防部队满了
                worldService.retreatArmyByDistance(player, army, now);// 返回
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                int heroId = army.getHero().get(0).getPrincipleHeroId();
                mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_SUPER_MINE_HELP_FILL, null, now,
                        sm.getConfigId(), xy.getA(), xy.getB(), heroId, sm.getConfigId(), xy.getA(), xy.getB(), heroId);
                return;
            }
            // 计算能驻防的最大时间
            int maxTime = Constant.ARMY_STATE_GUARD_TIME * TimeHelper.HOUR_S;// 计算能驻防的最大时间
            army.setDuration(maxTime);
            army.setEndTime(now + maxTime);
            army.setState(ArmyConstant.ARMY_STATE_GUARD);
            army.setHeroState(player, ArmyConstant.ARMY_STATE_GUARD);
            sm.getHelpArmy().add(army); // 驻防部队加入进去
        }
        //添加被攻击报警
//        syncCollectStatus(sm, player);
    }

    /*----------------------------------行军结束处理 end------------------------------*/

    /**
     * 重新重新把矿点位置生成
     */
    public void gmRefreshSuperMine() {
        int now = TimeHelper.getCurrentSecond();
        worldDataManager.getSuperMineCampMap().values().stream().flatMap(list -> list.stream()).forEach(sm -> {
            // 撤回所有部队,包括驻防部队
            resetStateSuperMine(sm, now);
            // 重新生成
            worldDataManager.refreshSuperMine(sm, now); // 找位置进行随机刷新
        });
    }

    public void gmClearAndRefreshSuperMineByCamp(int camp) {
        int now = TimeHelper.getCurrentSecond();
        List<SuperMine> listSm = worldDataManager.getSuperMineCampMap().get(camp);
        if (!CheckNull.isEmpty(listSm)) {
            // 撤回所有部队,包括驻防部队
            listSm.forEach(sm -> resetStateSuperMine(sm, now));
            worldDataManager.getSuperMineCampMap().remove(camp);
            worldDataManager.addCampSuperMine(camp); // 重新生成
        }
    }

    /**
     * Gm清除所有的超级矿点重新生成
     */
    public void gmClearAndRefreshSuperMine() {
        int now = TimeHelper.getCurrentSecond();
        worldDataManager.getSuperMineCampMap().values().stream().flatMap(list -> list.stream()).forEach(sm -> {
            // 撤回所有部队,包括驻防部队
            resetStateSuperMine(sm, now);
        });
        // 清掉
        worldDataManager.getSuperMineCampMap().clear();
        worldDataManager.getSuperMineMap().clear();
        SuperMine.SEQ_ID = 0; // seqid清除
        // 重新生成
        worldDataManager.addCampSuperMine(1);
        worldDataManager.addCampSuperMine(2);
        worldDataManager.addCampSuperMine(3);
    }

    /**
     * 城池争夺状态修改
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
                            // 打回来了
                            sm.setStopToProducedState(now);
                            // army状态推送
                            syncCollectArmyState(sm, now);
                        } else {
                            // 停产设置
                            sm.setStopState(now);
                            // army状态推送
                            syncCollectArmyState(sm, now);
                            // 发停产邮件
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
     * 推送采集部队状态
     *
     * @param sm
     * @param now
     */
    private void syncCollectArmyState(SuperMine sm, int now) {
        for (SuperGuard sg : sm.getCollectArmy()) {
            // 部队状态推送
            Player defPlayer = playerDataManager.getPlayer(sg.getArmy().getLordId());
            worldService.synRetreatArmy(defPlayer, sg.getArmy(), now);
        }
    }

}
