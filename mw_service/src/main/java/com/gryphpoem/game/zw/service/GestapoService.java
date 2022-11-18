package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.NumUtils;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticGestapoPlan;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.Gestapo;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-04-13 17:12
 * @Description: 盖世太保相关, 主要有召唤盖世太保, 盖世太保发起进攻, 通知客户端太保信息, 加入盖世太保战役, 盖世太保相关定时器
 * @Modified By:
 */
@Service
public class GestapoService {

    @Autowired
    private WorldService worldService;

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private WarDataManager warDataManager;

    @Autowired
    private MedalDataManager medalDataManager;

    // ============================盖世太保相关start============================

    /**
     * 获取盖世太保击杀排行
     *
     * @param roleId
     * @throws MwException
     */
    public GestapoKillCampRankRs getGestapoKillRank(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activityInfo = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ATK_GESTAPO);
        if (CheckNull.isNull(activityInfo)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "盖世太保活动未开启 roleId:", roleId);
        }
        GlobalActivityData activityData = activityDataManager.getGlobalActivity(ActivityConst.ACT_ATK_GESTAPO);
        if (CheckNull.isNull(activityData)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "盖世太保击杀排行活动未开启 roleId:", roleId);
        }
        GestapoKillCampRankRs.Builder builder = GestapoKillCampRankRs.newBuilder();
        int[] aTwoInt = NumUtils.separateLong2int(activityData.getTopupa().get());// 高位:时间;低位:数据值
        int[] bTwoInt = NumUtils.separateLong2int(activityData.getTopupb().get());
        int[] cTwoInt = NumUtils.separateLong2int(activityData.getTopupc().get());
        builder.addCampRank(PbHelper.createGestapoCampRankPb(Constant.Camp.EMPIRE, aTwoInt[0], aTwoInt[1]));
        builder.addCampRank(PbHelper.createGestapoCampRankPb(Constant.Camp.ALLIED, bTwoInt[0], bTwoInt[1]));
        builder.addCampRank(PbHelper.createGestapoCampRankPb(Constant.Camp.UNION, cTwoInt[0], cTwoInt[1]));
        return builder.build();
    }

    /**
     * @Author: ZhouJie
     * @Date: 2018-03-28 19:49
     * @Description: 召唤盖世太保
     */
    public SummonGestapoRs summonGestapo(Long lordId, SummonGestapoRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        Activity activityInfo = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ATK_GESTAPO);
        if (CheckNull.isNull(activityInfo)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "盖世太保活动未开启 roleId:", lordId);
        }
        int gestapoId = req.getGestapoId();
        StaticGestapoPlan staticGestapoPlan = StaticWorldDataMgr.getGestapoPlanById(gestapoId);
        if (CheckNull.isNull(staticGestapoPlan)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "盖世太保未配置, roleId:,", lordId, ", staticGestapoPlanId:",
                    gestapoId);
        }

        int pos = req.getPos();
        if (!worldDataManager.isEmptyPos(pos)) {
            throw new MwException(GameError.NOT_EMPTY_POS.getCode(), "目前坐标不为空坐标, pos:", pos);
        }
        boolean hasItem = false;
        List<List<Integer>> costPropList = staticGestapoPlan.getCostProp();
        if (!CheckNull.isEmpty(costPropList)) {
            for (List<Integer> costProp : costPropList) {
                rewardDataManager.checkAndSubPlayerRes(player, costPropList, AwardFrom.SUMMON_GESTAPO_COST);
                // rewardDataManager.checkPropIsEnough(player, costProp.get(1), costProp.get(2), "召唤盖世太保");
                // rewardDataManager.subProp(player, costProp.get(1), costProp.get(2), AwardFrom.SUMMON_GESTAPO_COST,
                // "召唤盖世太保");
                hasItem = true;
                break;
            }
        }
        if (!hasItem) {
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), "召唤盖世太保资源不足, roleId:", lordId, ",gestapoId:",
                    gestapoId);
        }
        SummonGestapoRs.Builder builder = SummonGestapoRs.newBuilder();
        worldDataManager.addGestapo(pos, staticGestapoPlan, player);
        // 通知其他玩家地图数据改变
        List<Integer> posList = new ArrayList<>();
        posList.add(pos);
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        return builder.build();
    }

    /**
     * GM批量添加盖世太保
     *
     * @param player
     * @param areaId
     * @param count
     */
    public void summonManyGestapos(Player player, Integer areaId, Integer count) {
        Activity activityInfo = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ATK_GESTAPO);
        if (CheckNull.isNull(activityInfo)) return;
        List<StaticGestapoPlan> gestapoList = StaticWorldDataMgr.getGestapoList();
        if (CheckNull.isEmpty(gestapoList)) return;
        StaticGestapoPlan staticGestapoPlan = gestapoList.get(0);
        if (CheckNull.isNull(staticGestapoPlan)) return;
        // 通知其他玩家地图数据改变
        List<Integer> posList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int pos = worldDataManager.randomEmptyPosInArea(areaId);
            worldDataManager.addGestapo(pos, staticGestapoPlan, player);
            posList.add(pos);
        }
        SummonGestapoRs.Builder builder = SummonGestapoRs.newBuilder();
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
    }

    /**
     * @Author: ZhouJie
     * @Date: 2018-03-29 11:59
     * @Description: 盖世太保发起进攻
     */
    public AttackGestapoRs AttackGestapo(Long lordId, AttackGestapoRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        int now = TimeHelper.getCurrentSecond();
        Activity activityInfo = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ATK_GESTAPO);
        if (CheckNull.isNull(activityInfo)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "盖世太保活动未开启 roleId:", lordId);
        }
        int pos = req.getPos();
        if (!worldDataManager.isGestapoPos(pos)) {
            throw new MwException(GameError.GESTAPO_POS_IS_EMPTY.getCode(), "进攻太保,目标坐标为空 roleId:", lordId, ", pos:",
                    pos);
        }
        Gestapo gestapo = worldDataManager.getGestapoByPos(pos);
        if (gestapo.getEndTime() < now) {
            throw new MwException(GameError.GESTAPO_EXCEED_TIME.getCode(), "进攻太保,太保过了显示时间 roleId:", lordId, ", pos:",
                    pos);
        }
        LinkedList<Battle> battleList = warDataManager.getBattlePosMap().get(pos);
        int camp = player.lord.getCamp();
        if (!CheckNull.isEmpty(battleList)) {
            for (Battle battle : battleList) {
                if (battle.getAtkCamp() == camp) {
                    throw new MwException(GameError.GESTAPO_BATTLE_HAS_EXISTS.getCode(),
                            "发起太保战，已有该阵营玩家对该太保发起战斗, roleId:", lordId, ", pos:", pos);
                }
            }
        }

        StaticGestapoPlan staticGestapoPlan = StaticWorldDataMgr.getGestapoPlanById(gestapo.getGestapoId());
        if (CheckNull.isNull(staticGestapoPlan)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "盖世太保未配置, roleId:,", lordId, ", staticGestapoPlanId:",
                    gestapo.getGestapoId());
        }

        Battle battle = new Battle();
        battle.setType(WorldConstant.BATTLE_TYPE_GESTAPO);
        int battleTime = now + staticGestapoPlan.getCountdown() <= gestapo.getEndTime()
                ? now + staticGestapoPlan.getCountdown() : gestapo.getEndTime();
        battle.setBattleTime(battleTime);
        battle.setBeginTime(now);
        battle.setPos(pos);
        battle.setSponsor(player);
        battle.setDefCamp(Constant.Camp.NPC);
        battle.addDefArm(staticGestapoPlan.getTotalArm());
        battle.getAtkRoles().add(lordId);
        battle.setAtkCamp(player.lord.getCamp());
        battle.setBattleType(gestapo.getGestapoId());

        // 通知进攻方阵营战消息
        // chatService.sendSysChat(ChatConst.CHAT_CITY_ATK, player.lord.getCamp(), 0, player.lord.getNick(),
        // Constant.Camp.NPC, pos);

        // 添加战斗记录
        warDataManager.addBattle(player, battle);

        // 通知太保信息
        syncAttackGestapo(battle, gestapo);

        List<Integer> posList = new ArrayList<>();
        posList.add(pos);
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, 0L, 1));

        AttackGestapoRs.Builder builder = AttackGestapoRs.newBuilder();
        return builder.build();
    }

    /**
     * 通知客户端太保信息
     *
     * @param battle
     * @param gestapo
     */
    private void syncAttackGestapo(Battle battle, Gestapo gestapo) {
        int atkCamp = battle.getAtkCamp();
        int defCamp = battle.getDefCamp();
        SyncGestapoBattleRs.Builder builder = SyncGestapoBattleRs.newBuilder();
        builder.setBattle(PbHelper.createGestapoBattlePb(battle, gestapo));
        Base.Builder msg = PbHelper.createSynBase(SyncGestapoBattleRs.EXT_FIELD_NUMBER, SyncGestapoBattleRs.ext,
                builder.build());

        int areaId = MapHelper.getAreaIdByPos(battle.getPos());
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(areaId);
        List<Integer> areaIdList = staticArea.getUnlockArea();// 通知所有与本区已开通关联的区域内，相关联的玩家

        ConcurrentHashMap<Long, Player> playerMap = playerDataManager.getPlayerByAreaList(areaIdList);
        for (Player player : playerMap.values()) {
            if (player.isLogin && (player.lord.getCamp() == atkCamp || player.lord.getCamp() == defCamp)) {
                MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
            }
        }
    }

    /**
     * 加入盖世太保战役
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public JoinGestapoBattleRs joinGestapoBattle(long roleId, JoinGestapoBattleRq req) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int camp = player.lord.getCamp();
        int battleId = req.getBattleId();
        Battle battle = warDataManager.getBattleMap().get(battleId);
        if (null == battle) {
            throw new MwException(GameError.BATTLE_NOT_FOUND.getCode(), "战争信息未找到, roleId:", roleId, ", battleId:",
                    battleId);
        }
        StaticArea targetSArea = StaticWorldDataMgr.getAreaMap().get(MapHelper.getAreaIdByPos(battle.getPos()));
        StaticArea mySArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());

        // 城战区域检测
        worldService.checkArea(player, targetSArea, mySArea);

        if (ActParamConstant.ACT_GESTAPO_LEVEL.get(0) > player.lord.getLevel()) {
            throw new MwException(GameError.JOIN_GESTAPO_BATTLE_NEED_LV.getCode(), "指挥官先磨砺至30级，再发动阵营战吧, roleId:",
                    roleId);
        }

        if (battle.getAtkCamp() != camp) {
            throw new MwException(GameError.CAN_NOT_JOIN_BATTLE.getCode(), "不是本阵营的战斗，不能参加, roleId:", roleId,
                    ", battleId:", battleId, ", roleCamp:", camp);
        }

        int pos = battle.getPos();
        // 检查出征将领信息
        List<Integer> heroIdList = req.getHeroIdList();
        worldService.checkFormHero(player, heroIdList);

        int armCount = 0;
        Hero hero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            armCount += hero.getCount();
        }

        int now = TimeHelper.getCurrentSecond();
        int marchTime = worldService.marchTime(player, pos);

        Gestapo gestapo = worldDataManager.getGestapoByPos(pos);
        if (CheckNull.isNull(gestapo)) {
            throw new MwException(GameError.GESTAPO_POS_IS_EMPTY.getCode(), "进攻太保,目标坐标为空 roleId:", roleId, ", pos:",
                    pos);
        }
        StaticGestapoPlan staticGestapoPlan = StaticWorldDataMgr.getGestapoPlanById(gestapo.getGestapoId());
        if (CheckNull.isNull(staticGestapoPlan)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "盖世太保未配置, roleId:,", roleId, ", staticGestapoPlanId:",
                    gestapo.getGestapoId());
        }
        // 行军加速,正常行军时间/8,向上取整
        int lead = Integer.valueOf(staticGestapoPlan.getParam());
        marchTime = (int) Math.ceil(marchTime * 1.0 / lead);

        // 计算时间是否赶得上
        if (now + marchTime > battle.getBattleTime()) {
            throw new MwException(GameError.BATTLE_CD_TIME.getCode(), "加入城战,赶不上时间, roleId:", roleId, ", pos:",
                    pos + ",行军时间=" + (now + marchTime) + ",城战倒计时=" + battle.getBattleTime());
        }

        // 计算补给
        int needFood = worldService.checkMarchFood(player, marchTime, armCount);
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);

        List<CommonPb.PartnerHeroIdPb> form = new ArrayList<>();
        for (Integer heroId : heroIdList) {
            PartnerHero partnerHero = player.getPlayerFormation().getPartnerHero(heroId);
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;
            partnerHero.setState(ArmyConstant.ARMY_STATE_MARCH);
            form.add(partnerHero.convertTo());
        }

        Army army = new Army(player.maxKey(), ArmyConstant.ARMY_TYPE_ATK_GESTAPO, pos, ArmyConstant.ARMY_STATE_MARCH,
                form, marchTime, now + marchTime, player.getDressUp());
        army.setBattleId(battleId);
        army.setLordId(roleId);
        army.setTarLordId(staticGestapoPlan.getGestapoId());
        army.setBattleTime(battle != null ? battle.getBattleTime() : 0);
        army.setHeroMedals(heroIdList.stream()
                .map(heroId -> medalDataManager.getHeroMedalByHeroIdAndIndex(player, heroId, MedalConst.HERO_MEDAL_INDEX_0))
                .filter(Objects::nonNull)
                .map(PbHelper::createMedalPb)
                .collect(Collectors.toList()));

        player.armys.put(army.getKeyId(), army);
        HashSet<Integer> set = player.battleMap.get(pos);
        if (set == null) {
            set = new HashSet<>();
            player.battleMap.put(pos, set);
        }
        set.add(battle.getBattleId());
        // player.battleMap.put(pos, battle.getBattleId());

        // 添加行军路线
        March march = new March(player, army);
        worldDataManager.addMarch(march);

        //  攻击方
        if (camp == battle.getAtkCamp()) {
            // worldService.removeProTect(player);
            battle.getAtkRoles().add(roleId);
        }

        // 推送区数据改变
        List<Integer> posList = new ArrayList<>();
        posList.add(pos);
        posList.add(player.lord.getPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, roleId, 5));
        // 返回协议
        JoinGestapoBattleRs.Builder builder = JoinGestapoBattleRs.newBuilder();
        // 更新battle兵力
        battle.updateArm(camp, armCount);

        builder.setArmy(PbHelper.createArmyPb(army, false));
        builder.setBattle(PbHelper.createBattlePb(battle));
        return builder.build();
    }

    /**
     * 盖世太保加速行军
     *
     * @param army
     * @param marchTime
     * @return
     */
    public int getGestapoMarchTime(Army army, int marchTime) {
        if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_GESTAPO) {
            Gestapo gestapo = worldDataManager.getGestapoByPos(army.getTarget());
            int gestapoId = !CheckNull.isNull(gestapo) ? gestapo.getGestapoId() : (int) army.getTarLordId();
            StaticGestapoPlan staticGestapoPlan = StaticWorldDataMgr.getGestapoPlanById(gestapoId);
            if (!CheckNull.isNull(staticGestapoPlan)) {
                // 行军加速,正常行军时间/8,向上取整
                int lead = Integer.valueOf(staticGestapoPlan.getParam());
                marchTime = (int) Math.ceil(marchTime * 1.0 / lead);
            }
        }
        return marchTime;
    }

    // ============================盖世太保相关end============================

    /**
     * 盖世太保相关定时器
     */
    public void gestapoTimerLogic() {
        Iterator<Gestapo> iterator = worldDataManager.getGestapoMap().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        List<Integer> posList = null;
        while (iterator.hasNext()) {
            Gestapo gestapo = iterator.next();
            int pos = gestapo.getPos();
            try {
                if (now > gestapo.getEndTime()) {
                    // 移除这个盖世太保
                    worldDataManager.removeBandit(pos, 2);
                    if (CheckNull.isEmpty(posList)) {
                        posList = new ArrayList<>();
                    }
                    // 通知周围玩家
                    posList.add(gestapo.getPos());
                }
            } catch (Exception e) {
                LogUtil.error(e, "执行盖世太保定时任务出现错误, pos:", pos);
            }
        }
        if (!CheckNull.isEmpty(posList)) {
            // 通知其他玩家数据改变
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        }
    }

}
