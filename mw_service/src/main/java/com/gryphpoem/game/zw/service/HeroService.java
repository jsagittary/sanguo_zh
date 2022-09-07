package com.gryphpoem.game.zw.service;

import com.gryphpoem.cross.activity.CrossRechargeActivityService;
import com.gryphpoem.cross.constants.PlayerUploadTypeDefine;
import com.gryphpoem.cross.player.RpcPlayerService;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.pb.GamePb1.*;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.WarPlane;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import com.gryphpoem.game.zw.service.fish.FishingService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName HeroService.java
 * @Description 将领相关
 * @date 创建时间：2017年3月25日 下午6:11:32
 */
@Service
public class HeroService implements GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private EquipService equipService;

    @Autowired
    private TechDataManager techDataManager;

    @Autowired
    private TaskDataManager taskDataManager;

    @Autowired
    private ActivityTriggerService activityTriggerService;

    @Autowired
    private WallService wallService;

    @Autowired
    private ArmyService armyService;

    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private ChatDataManager chatDataManager;

    @Autowired
    private WarPlaneDataManager warPlaneDataManager;

    @Autowired
    private BattlePassDataManager battlePassDataManager;

    @Autowired
    private RoyalArenaService royalArenaService;

    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private SeasonTalentService seasonTalentService;
    @Autowired
    private FishingService fishingService;

    @Autowired
    private CrossRechargeActivityService crossRechargeActivityService;
    @Autowired
    private RpcPlayerService crossPlayerService;

    /**
     * 获取所有将领
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb1.GetHerosRs getHeros(long roleId) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);

        // 检查角色是否存在
        AccountHelper.checkPlayerIsExist(player, roleId);

        // 计算自动补兵的兵数
        try {
            wallService.processAutoAddArmy(player);
        } catch (MwException e) {
            LogUtil.error("roleId:", roleId, " 自动补兵错误:", e.toString());
        }

        GamePb1.GetHerosRs.Builder builder = GamePb1.GetHerosRs.newBuilder();
        playerDataManager.refreshDaily(player);
        playerDataManager.autoAddArmy(player);
        if (!checkHeroPosIsInit(player.heroDef)) {
            player.heroDef = Arrays.copyOf(player.heroBattle, player.heroBattle.length);
            for (int heroId : player.heroDef) {
                Hero hero = player.heros.get(heroId);
                if (hero != null && heroId == hero.getHeroId() && hero.getDefPos() == 0) {
                    hero.onDef(hero.getPos());
                }
            }
        }
        for (Hero hero : player.heros.values()) {
            builder.addHero(PbHelper.createHeroPb(hero, player, getGuardAttr(hero, player)));
        }

        // 返回所有将领信息的协议
        builder.setFree(0);
        return builder.build();
    }

    /**
     * 获取部分将领
     *
     * @param roleId
     * @param heroIds
     * @return
     * @throws MwException
     */
    public GamePb1.GetHeroByIdsRs getHeroByIds(long roleId, List<Integer> heroIds) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);
        try {
            // 检查角色是否存在
            AccountHelper.checkPlayerIsExist(player, roleId);
        } catch (MwException e) {
            LogUtil.error("roleId:", roleId, " 自动补兵错误:", e.toString());
        }

        // 计算自动补兵的兵数
        wallService.processAutoAddArmy(player);

        GamePb1.GetHeroByIdsRs.Builder builder = GamePb1.GetHeroByIdsRs.newBuilder();
        playerDataManager.refreshDaily(player);
        playerDataManager.autoAddArmy(player);
        Hero hero;
        for (int heroId : heroIds) {
            hero = player.heros.get(heroId);
            if (hero == null) {
                continue;
            }

            builder.addHero(PbHelper.createHeroPb(hero, player, getGuardAttr(hero, player)));
        }
        return builder.build();
    }

    /**
     * 驻防英雄添加赛季天赋驻防属性加成，不参与战斗力加成
     *
     * @param hero
     * @param player
     */
    private List<TwoInt> getGuardAttr(Hero hero, Player player) {
        List<TwoInt> result = null;
        if (hero.isOnWall()) {
            //禁卫军赛季天赋加成
            List<TwoInt> janitorAttr = seasonTalentService.getSeasonTalentEffectTwoInt(player, hero, SeasonConst.TALENT_EFFECT_619);
            if (!ObjectUtils.isEmpty(janitorAttr)) {
                result = new ArrayList<>(janitorAttr);
            }
            //宝具加成禁卫军
            treasureWareBuff(player, hero, result);
        }

        //驻防军赛季天赋加成
        if (hero.getState() == ArmyConstant.ARMY_STATE_GUARD) {
            for (Army army : player.armys.values()) {
                if (!army.isGuard()) {
                    continue;
                }
                TwoInt twoInt = Optional.ofNullable(army.getHero().stream().
                        filter(ar -> ar.getV1() == hero.getHeroId()).findFirst()).get().orElse(null);
                if (CheckNull.isNull(twoInt)) {
                    continue;
                }
                if (ObjectUtils.isEmpty(army.getSeasonTalentAttr())) {
                    continue;
                }
                if (CheckNull.isNull(result)) {
                    result = new ArrayList<>();
                }
                result.addAll(army.getSeasonTalentAttr());
            }
        }

        return result;
    }

    /**
     * 宝具加成禁卫军
     *
     * @param player
     * @param hero
     * @param result
     */
    private void treasureWareBuff(Player player, Hero hero, List<TwoInt> result) {
        Object buff = DataResource.getBean(TreasureWareService.class).
                getTreasureWareBuff(player, hero, TreasureWareConst.SpecialType.JANITOR_TYPE, 0);
        if (ObjectUtils.isEmpty(buff) || !(buff instanceof List)) {
            return;
        }

        if (CheckNull.isNull(result)) {
            result = new ArrayList<>();
        }
        List<List<Integer>> buffEffect = (List<List<Integer>>) buff;
        for (List<Integer> list : buffEffect) {
            result.add(PbHelper.createTwoIntPb(list.get(0), list.get(1)));
        }
    }

    /**
     * 将领上阵
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb1.HeroBattleRs heroBattle(long roleId, GamePb1.HeroBattleRq req) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);

        // 检查角色是否存在
        AccountHelper.checkPlayerIsExist(player, roleId);

        int heroId = req.getHeroId();
        int pos = req.getPos();
        boolean swap = false;
        if (req.hasSwap()) {
            swap = req.getSwap();
        }
        boolean swapPlane = false;
        if (req.hasSwapPlane()) {
            swapPlane = req.getSwapPlane();
        }

        // 检查pos位是否正常
        if (pos < HeroConstant.HERO_BATTLE_1 || pos > HeroConstant.HERO_BATTLE_4) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId, ", pos:",
                    pos);
        }

        // 检查科技开启位置
        if (pos == HeroConstant.HERO_BATTLE_3) {
            if (!techDataManager.isOpen(player, TechConstant.TYPE_10)) {
                throw new MwException(GameError.HERO_BATTLE_POS_NEED_TECH.getCode(), "未开启,请提升科技等级 roleId:", roleId,
                        ", pos:", pos, "lv:", techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_10));
            }
        } else if (pos == HeroConstant.HERO_BATTLE_4) {
            if (!techDataManager.isOpen(player, TechConstant.TYPE_20)) {
                throw new MwException(GameError.HERO_BATTLE_POS_NEED_TECH.getCode(), "未开启,请提升科技等级, roleId:", roleId,
                        ", pos:", pos, "lv:", techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_20));
            }
        }

        // 检查玩家是否有该将领
        Hero hero = checkHeroIsExist(player, heroId);
        // 判断该将领是否已经上阵
        if (player.isOnBattleHero(heroId) || player.isOnWallHero(heroId) || player.isOnAcqHero(heroId) || player.isOnCommandoHero(heroId)) {
            throw new MwException(GameError.HERO_BATTLE_REPEAT.getCode(), String.format("roleId :%d, heroId :%d, status :%d, pos :%d, wallPos :%d, acqPos :%d",
                    roleId, heroId, hero.getStatus(), hero.getPos(), hero.getWallPos(), hero.getAcqPos()));
        }

        GamePb1.HeroBattleRs.Builder builder = GamePb1.HeroBattleRs.newBuilder();
        Hero battleHero = player.getBattleHeroByPos(pos);
        // Hero defendHero = null;
        int defPos = 0;
        if (battleHero != null) {
            // defendHero = player.getDefendHeroByPos(battleHero.getHeroId());
            defPos = battleHero.getDefPos();
        }
        ChangeInfo change = ChangeInfo.newIns();
        if (null != battleHero) {// 位置上已有其他将领存在，现将该将领下阵
            if (!battleHero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作");
            }
            if (swap) {// 如果需要交换装备，执行交换装备的逻辑
                // rewardDataManager.checkBagCnt(player);
                swapHeroEquip(player, hero, battleHero);
            }
            if (swapPlane) {// 如果需要交换战机, 执行交换战机的逻辑
                swapHeroPlanes(player, hero, battleHero, pos);
            } else {
                downHeroAllPlane(player, battleHero); // 下阵将领, 把战机也下阵
            }
            battleHero.onBattle(0);// 将领下阵，pos设置为0
            battleHero.onDef(0);// 防守将领下阵, pos设置为0

            // 士兵回营
            int sub = battleHero.getCount();
            battleHero.setCount(0);
            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(battleHero.getHeroId());
//            if (Objects.nonNull(staticHero)) {
//                int armType = staticHero.getType();// 获取将领对应类型的兵力
                // LogLordHelper.heroArm(AwardFrom.HERO_DOWN, player.account, player.lord, heroId, hero.getCount(), -sub, staticHero.getType(),
                //         Constant.ACTION_ADD);

                // 上报玩家兵力变化
//                LogLordHelper.playerArm(
//                        AwardFrom.HERO_DOWN,
//                        player, armType,
//                        Constant.ACTION_ADD,
//                        -sub,
//                        playerDataManager.getArmCount(player.resource, armType)
//                );
//            }

            rewardDataManager.modifyArmyResource(player, staticHero.getType(), sub, 0, AwardFrom.HERO_DOWN);

            // 重新计算并更新将领属性
            CalculateUtil.processAttr(player, battleHero);
            change.addChangeType(AwardType.ARMY, staticHero.getType());
            change.addChangeType(AwardType.HERO_ARM, battleHero.getHeroId());
            builder.setDownHero(PbHelper.createHeroPb(battleHero, player));
        }

        // 将领上阵
        hero.onBattle(pos);

        if (!CheckNull.isEmpty(hero.getWarPlanes())) {
            for (int planeId : hero.getWarPlanes()) {
                WarPlane plane = player.checkWarPlaneIsExist(planeId);
                if (!CheckNull.isNull(plane)) {
                    plane.setPos(pos);
                }
            }
        }
        player.heroBattle[pos] = heroId;// 更新已上阵将领队列信息
        if (defPos != 0) { // 更换将领
            hero.onDef(defPos);
            player.heroDef[defPos] = heroId;// 更新已防守将领队列信息
        } else { // 上阵防守将领
            hero.onDef(pos);
            player.heroDef[pos] = heroId;// 更新已防守将领队列信息
        }

        // 重新计算并更新将领属性
        CalculateUtil.processAttr(player, hero);
        if (techDataManager.isOpen(player, TechConstant.TYPE_19) && player.common.getAutoArmy() == 0) {
            // 研究自动补兵,并且关闭了自动补兵:不进行补兵
        } else {
            // 上阵将领自动补兵
            armyService.autoAddArmySingle(player, hero);
        }
        change.addChangeType(AwardType.ARMY, hero.getType());
        change.addChangeType(AwardType.HERO_ARM, hero.getHeroId());
        // 通知客户端玩家资源改变
        rewardDataManager.syncRoleResChanged(player, change);

        // 返回将领上阵协议
        builder.setUpHero(PbHelper.createHeroPb(hero, player));
        taskDataManager.updTask(player, TaskType.COND_HERO_UP, 1, hero.getHeroId());
        taskDataManager.updTask(player, TaskType.COND_509, 1, hero.getQuality());
        CalculateUtil.reCalcFight(player);
        // rankDataManager.setFight(player.lord);
        taskDataManager.updTask(player, TaskType.COND_28, 1, hero.getType());
        checkHeroQueueStatus(player);
        return builder.build();
    }

    /**
     * 下阵将领所有战机
     *
     * @param player
     * @param battleHero
     * @throws MwException
     */
    public void downHeroAllPlane(Player player, Hero battleHero) throws MwException {
        List<Integer> planes = battleHero.getWarPlanes();
        if (!CheckNull.isEmpty(planes)) {
            Iterator<Integer> it = planes.iterator();
            while (it.hasNext()) {
                int planeId = it.next();
                WarPlane plane = player.checkWarPlaneIsExist(planeId);
                if (!CheckNull.isNull(plane)) {
                    it.remove();
                    plane.downBattle(battleHero);
                }
            }
        }
    }

    /**
     * 将领换位置
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb1.HeroPosSetRs heroPosSet(long roleId, GamePb1.HeroPosSetRq req) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);
        Set<Integer> check = new HashSet<>();
        int posType = req.getPosType();
        if (posType < HeroConstant.CHANGE_POS_TYPE || posType > HeroConstant.CHANGE_TREASURE_WARE_POS_TYPE) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId :", roleId);
        }
        for (TwoInt kv : req.getHerosList()) {
            int pos = kv.getV1();

            if (check.contains(kv.getV1()) || check.contains(kv.getV2())) {
                throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId,
                        ", pos:", pos);
            }
            check.add(kv.getV1());
            check.add(kv.getV2());

            if (pos < HeroConstant.HERO_BATTLE_1 || pos > HeroConstant.HERO_BATTLE_4) {
                throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId, ", pos:", pos);
            }

            if (pos == HeroConstant.HERO_BATTLE_3) {
                if (!techDataManager.isOpen(player, TechConstant.TYPE_10)) {
                    throw new MwException(GameError.HERO_BATTLE_POS_NEED_TECH.getCode(), "未开启,请提升科技等级 roleId:", roleId, ", pos:", pos);
                }
            } else if (pos == HeroConstant.HERO_BATTLE_4) {
                if (!techDataManager.isOpen(player, TechConstant.TYPE_20)) {
                    throw new MwException(GameError.HERO_BATTLE_POS_NEED_TECH.getCode(), "未开启,请提升科技等级, roleId:", roleId, ", pos:", pos);
                }
            }
            Hero hero = checkHeroIsExist(player, kv.getV2());
            if (hero.getPos() <= 0) {
                throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId, ", pos:", pos);
            }
        }

        if (posType == HeroConstant.CHANGE_POS_TYPE ||
                posType == HeroConstant.CHANGE_DEFEND_POS_TYPE ||
                posType == HeroConstant.CHANGE_TREASURE_WARE_POS_TYPE) {
            int[] heroArray = posType == HeroConstant.CHANGE_POS_TYPE ||
                    posType == HeroConstant.CHANGE_TREASURE_WARE_POS_TYPE ?
                    player.heroBattle : player.heroDef;
            Set<Integer> set = Arrays.stream(heroArray).filter(heroId -> heroId > 0).boxed().collect(Collectors.toSet());
            if (req.getHerosCount() != set.size()) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, hero arrays :%s", roleId, Arrays.toString(heroArray)));
            }
            for (TwoInt twoInt : req.getHerosList()) {
                if (!set.contains(twoInt.getV2())) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, heroId :%d, not In arrays,  hero arrays :%s",
                            roleId, twoInt.getV2(), Arrays.toString(heroArray)));
                }
            }
        }


        GamePb1.HeroPosSetRs.Builder builder = GamePb1.HeroPosSetRs.newBuilder();
        List<Integer> posList = player.heroBattlePos.get(posType);
        if (posType == HeroConstant.CHANGE_COMBAT_POS_TYPE ||
                posType == HeroConstant.CHANGE_BATTLE_POS_TYPE ||
                posType == HeroConstant.CHANGE_TREASURE_WARE_POS_TYPE) {
            if (posList == null) {
                posList = new ArrayList<>();
                player.heroBattlePos.put(posType, posList);
            }
            posList.clear();
        }
        for (TwoInt kv : req.getHerosList()) {
            Hero hero = checkHeroIsExist(player, kv.getV2());
            if (posType == HeroConstant.CHANGE_POS_TYPE) {
                hero.onBattle(kv.getV1());
                player.heroBattle[kv.getV1()] = kv.getV2();// 更新已上阵将领队列信息
            } else if (posType == HeroConstant.CHANGE_DEFEND_POS_TYPE) {
                hero.onDef(kv.getV1());
                player.heroDef[kv.getV1()] = kv.getV2();// 更新已上阵防守将领队列信息
            } else {
                posList.add(kv.getV1());
            }
            builder.addHeros(PbHelper.createHeroPb(hero, player));
        }
        checkHeroQueueStatus(player);
        return builder.build();
    }

    /**
     * 检查将领是否存在
     *
     * @param player
     * @param heroId
     * @return
     * @throws MwException
     */
    public Hero checkHeroIsExist(Player player, int heroId) throws MwException {
        Hero hero = player.heros.get(heroId);
        if (null == hero) {
            throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "玩家没有这个将领, roleId:", player.roleId, ", heroId:",
                    heroId);
        }
        return hero;
    }

    /**
     * 判断数组是否初始化
     *
     * @param heroPos
     * @return
     */
    public boolean checkHeroPosIsInit(int[] heroPos) {
        // ArrayList<Integer> list = new ArrayList<>();
        for (int heroId : heroPos) {
            if (heroId != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 交换两个将领的战机(h2的所有战机上到h1)
     *
     * @param player
     * @param h1
     * @param h2
     * @param pos    上阵位置
     */
    public void swapHeroPlanes(Player player, Hero h1, Hero h2, int pos) throws MwException {
        downHeroAllPlane(player, h1);
        List<Integer> h2Planes = h2.getWarPlanes();
        if (!CheckNull.isEmpty(h2Planes)) {
            Iterator<Integer> it = h2Planes.iterator();
            while (it.hasNext()) {
                int planeId = it.next();
                WarPlane plane = player.checkWarPlaneIsExist(planeId);
                if (!CheckNull.isNull(plane)) {
                    it.remove();
                    if (h1.upPlane(planeId, warPlaneDataManager.planeOpenSize(player))) {
                        plane.upBattle(h1, pos, plane.getBattlePos());
                    }
                }
            }
        }
    }

    /**
     * 交换两个将领的装备
     *
     * @param player
     * @param h1
     * @param h2
     */
    public void swapHeroEquip(Player player, Hero h1, Hero h2) {
        int[] equip1 = Arrays.copyOf(h1.getEquip(), h1.getEquip().length);
        int[] equip2 = Arrays.copyOf(h2.getEquip(), h2.getEquip().length);
        int equipKeyId;
        // 卸载原来的装备
        for (int i = 0; i < equip1.length; i++) {
            equipKeyId = equip1[i];
            if (equipKeyId > 0) {
                equipService.downEquip(player, h1, equipKeyId);
            }
            equipKeyId = equip2[i];
            if (equipKeyId > 0) {
                equipService.downEquip(player, h2, equipKeyId);
            }
        }
        // 戴上新的装备
        for (int i = 0; i < equip2.length; i++) {
            equipKeyId = equip2[i];
            if (equipKeyId > 0) {
                equipService.heroOnEquip(player, h1, i, equipKeyId);
            }
            equipKeyId = equip1[i];
            if (equipKeyId > 0) {
                equipService.heroOnEquip(player, h2, i, equipKeyId);
            }
        }
    }

    /**
     * 将领快速升级
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public HeroQuickUpRs heroQuickUp(Long roleId, HeroQuickUpRq req) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);

        // 检查角色是否存在
        AccountHelper.checkPlayerIsExist(player, roleId);

        int heroId = req.getHeroId();
        int type = req.getType();
        boolean useGold = false;
        if (req.hasUseGold()) {
            useGold = req.getUseGold();
        }

        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        if (null == staticHero) {
            throw new MwException(GameError.HERO_NO_CONFIG.getCode(), "将领未配置, roleId:", player.roleId, ", heroId:",
                    heroId);
        }
        int propId = getPropByHeroUpType(type);
        StaticProp prop = StaticPropDataMgr.getPropMap(propId);
        if (null == prop) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "将领升级道具未配置, roleId:", player.roleId, ", heroId:",
                    heroId, ", type:", type, ", propId:", propId);
        }

        List<Integer> heroExpList;
        if (CheckNull.isEmpty(prop.getRewardList())) {
            throw new MwException(GameError.HERO_UP_PROP_ERR.getCode(), "奖励快速升级道具奖励未配置, roleId:", player.roleId,
                    ", heroId:", heroId, ", type:", type, ", propId:", propId);
        } else {
            heroExpList = prop.getRewardList().get(0);
            if (CheckNull.isEmpty(heroExpList) || heroExpList.size() < 3 || heroExpList.get(0) != AwardType.SPECIAL
                    || heroExpList.get(1) != AwardType.Special.HERO_EXP) {
                throw new MwException(GameError.HERO_UP_PROP_ERR.getCode(), "奖励快速升级道具配置错误, roleId:", player.roleId,
                        ", heroId:", heroId, ", type:", type, ", propId:", propId);
            }
        }

        // 检查玩家是否有该将领
        Hero hero = checkHeroIsExist(player, heroId);

        if (!hero.isIdle()) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos，将领不在空闲中, roleId:", roleId, ", heroId:",
                    heroId, ", state:", hero.getState());
        }

        if (hero.getLevel() >= player.lord.getLevel()) {
            throw new MwException(GameError.HERO_UP_MAX.getCode(), "将领快速升级，等级已达上限, roleId:", player.roleId, ", heroId:",
                    heroId, ", heroLv:", hero.getLevel(), ", roleLv:", player.lord.getLevel());
        }

        if (useGold) {
            int needGold = prop.getPrice();
            // 检查玩家金币是否足够
            rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, needGold, "将领金币升级");

            // 扣除相关金币
            rewardDataManager.subGold(player, needGold, AwardFrom.HERO_QUICK_UP);
        } else {
            // 检查玩家是否有相关道具，一次只使用一个
            rewardDataManager.checkPropIsEnough(player, propId, 1, "将领快速升级");

            // 扣除道具
            rewardDataManager.subProp(player, propId, 1, AwardFrom.HERO_QUICK_UP);
        }

        // 将领增加经验
        int addExp = prop.getRewardList().get(0).get(2);
        addExp = addHeroExp(hero, addExp, player.lord.getLevel(), player);

        // 返回协议
        GamePb1.HeroQuickUpRs.Builder builder = GamePb1.HeroQuickUpRs.newBuilder();
        builder.setHeroId(heroId);
        builder.setLv(hero.getLevel());
        builder.setExp(hero.getExp());
        if (useGold) {
            builder.setGold(player.lord.getGold());
        }
        for (int i = HeroConstant.ATTR_ATTACK; i <= HeroConstant.ATTR_LEAD; i++) {
            builder.addAttr(PbHelper.createTwoIntPb(i, hero.getAttr()[i]));// 将领属性
        }
        return builder.build();
    }

    /**
     * 将领直升一级
     *
     * @param
     * @return
     */
    public GamePb4.HeroQuickUpLvRs heroQuickUpLv(Long roleId, GamePb4.HeroQuickUpLvRq req) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);

        // 检查角色是否存在
        AccountHelper.checkPlayerIsExist(player, roleId);
        int heroId = req.getHeroId();
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        if (null == staticHero) {
            throw new MwException(GameError.HERO_NO_CONFIG.getCode(), "将领未配置, roleId:", player.roleId, ", heroId:",
                    heroId);
        }
        // 检查玩家是否有该将领
        Hero hero = checkHeroIsExist(player, heroId);

        if (!hero.isIdle()) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos，将领不在空闲中, roleId:", roleId, ", heroId:",
                    heroId, ", state:", hero.getState());
        }

        if (hero.getLevel() >= player.lord.getLevel()) {
            throw new MwException(GameError.HERO_UP_MAX.getCode(), "将领快速升级，等级已达上限, roleId:", player.roleId, ", heroId:",
                    heroId, ", heroLv:", hero.getLevel(), ", roleLv:", player.lord.getLevel());
        }
        boolean useAll = false;
        if (req.hasUseAll()) {
            useAll = req.getUseAll();
        }
        int lv = hero.getLevel() + 1;
        //是否自动升级，经验书全部使用，到所能升的最高等级停止。
        if (useAll) {
            lv = player.lord.getLevel();
        }
        //经验需要
        int need = heroUpLvNeedExp(staticHero.getQuality(), hero, hero.getLevel(), lv);

        // 将领增加经验
        int addExp = 0;
        //各种经验书ID
        int lowPropId = getPropByHeroUpType(HeroConstant.QUICK_UP_TYPE_LOW);
        int middlePropId = getPropByHeroUpType(HeroConstant.QUICK_UP_TYPE_MIDDLE);
        int highPropId = getPropByHeroUpType(HeroConstant.QUICK_UP_TYPE_HIGH);
        int topPropId = getPropByHeroUpType(HeroConstant.QUICK_UP_TYPE_TOP);
        //各种经验书拥有的数量
        int playerLowProp = (int) rewardDataManager.getRoleResByType(player, AwardType.PROP, lowPropId);
        int playerMiddleProp = (int) rewardDataManager.getRoleResByType(player, AwardType.PROP, middlePropId);
        int playerHighProp = (int) rewardDataManager.getRoleResByType(player, AwardType.PROP, highPropId);
        int playerTopProp = (int) rewardDataManager.getRoleResByType(player, AwardType.PROP, topPropId);
        //消耗各种经验书数量
        int lowPropNum = 0;
        int middlePropNum = 0;
        int highPropNum = 0;
        int topPropNum = 0;
        while (addExp + hero.getExp() < need) {
            int propId = 0;
            // 检查玩家是否有相关道具，由低到高一次使用 至 等级提升
            if (playerLowProp > lowPropNum) {
                propId = lowPropId;
                lowPropNum += 1;
            } else if (playerMiddleProp > middlePropNum) {
                propId = middlePropId;
                middlePropNum += 1;
            } else if (playerHighProp > highPropNum) {
                propId = highPropId;
                highPropNum += 1;
            } else if (playerTopProp > topPropNum) {
                propId = topPropId;
                topPropNum += 1;
            } else {
                break;
            }
            StaticProp prop = StaticPropDataMgr.getPropMap(propId);
            if (null == prop) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "将领升级道具未配置, roleId:", player.roleId, ", heroId:",
                        heroId, ", propId:", propId);
            }

            List<Integer> heroExpList;
            if (CheckNull.isEmpty(prop.getRewardList())) {
                throw new MwException(GameError.HERO_UP_PROP_ERR.getCode(), "奖励快速升级道具奖励未配置, roleId:", player.roleId,
                        ", heroId:", heroId, ", propId:", propId);
            } else {
                heroExpList = prop.getRewardList().get(0);
                if (CheckNull.isEmpty(heroExpList) || heroExpList.size() < 3 || heroExpList.get(0) != AwardType.SPECIAL
                        || heroExpList.get(1) != AwardType.Special.HERO_EXP) {
                    throw new MwException(GameError.HERO_UP_PROP_ERR.getCode(), "奖励快速升级道具配置错误, roleId:", player.roleId,
                            ", heroId:", heroId, ", propId:", propId);
                }
            }
            addExp += prop.getRewardList().get(0).get(2);
        }
        // 返回协议
        GamePb4.HeroQuickUpLvRs.Builder builder = GamePb4.HeroQuickUpLvRs.newBuilder();
        // 扣除道具
        if (lowPropNum > 0) {
            rewardDataManager.subProp(player, lowPropId, lowPropNum, AwardFrom.HERO_QUICK_UP);
            builder.addProps(PbHelper.createTwoIntPb(lowPropId, lowPropNum));
        }
        if (middlePropNum > 0) {
            rewardDataManager.subProp(player, middlePropId, middlePropNum, AwardFrom.HERO_QUICK_UP);
            builder.addProps(PbHelper.createTwoIntPb(middlePropId, middlePropNum));
        }
        if (highPropNum > 0) {
            rewardDataManager.subProp(player, highPropId, highPropNum, AwardFrom.HERO_QUICK_UP);
            builder.addProps(PbHelper.createTwoIntPb(highPropId, highPropNum));
        }
        if (topPropNum > 0) {
            rewardDataManager.subProp(player, topPropId, topPropNum, AwardFrom.HERO_QUICK_UP);
            builder.addProps(PbHelper.createTwoIntPb(topPropId, topPropNum));
        }
        // 将领增加经验
        addExp = addHeroExp(hero, addExp, player.lord.getLevel(), player);

        builder.setHeroId(heroId);
        builder.setLv(hero.getLevel());
        builder.setExp(hero.getExp());

        for (int i = HeroConstant.ATTR_ATTACK; i <= HeroConstant.ATTR_LEAD; i++) {
            builder.addAttr(PbHelper.createTwoIntPb(i, hero.getAttr()[i]));// 将领属性
        }
        return builder.build();

    }

    private int getPropByHeroUpType(int type) {
        switch (type) {
            case HeroConstant.QUICK_UP_TYPE_LOW:
                return PropConstant.PROP_ID_LOW_HERO_EXP;
            case HeroConstant.QUICK_UP_TYPE_MIDDLE:
                return PropConstant.PROP_ID_MIDDLE_HERO_EXP;
            case HeroConstant.QUICK_UP_TYPE_HIGH:
                return PropConstant.PROP_ID_HIGH_HERO_EXP;
            case HeroConstant.QUICK_UP_TYPE_TOP:
                return PropConstant.PROP_ID_TOP_HERO_EXP;
        }
        return 0;
    }

    /**
     * 给将领加经验
     *
     * @param quality
     * @param hero
     * @param addExp
     * @param maxLv   最高等级
     * @return 返回实际增加经验
     */
    public int addHeroExp(int quality, Hero hero, int addExp, int maxLv) {
        int add = 0;
        while (addExp > 0 && hero.getLevel() < maxLv) {
            int need = StaticHeroDataMgr.getExperByQuality(quality, hero.getLevel() + 1);
            if (need > 0) {
                if (hero.getExp() + addExp >= need) {
                    add += need - hero.getExp();
                    addExp -= need - hero.getExp();
                    hero.levelUp();
                } else {
                    add += addExp;
                    hero.setExp(hero.getExp() + addExp);
                    addExp = 0;
                }
            } else {
                // 设置经验为本级经验上限
                int max = StaticHeroDataMgr.getExperByQuality(quality, hero.getLevel());
                add += max - hero.getExp();
                hero.setExp(max);
                break;
            }
        }
        return add;
    }

    /**
     * 将领升到指定等级所需经验
     */
    public int heroUpLvNeedExp(int quality, Hero hero, int curLv, int maxLv) {
        int add = 0;
        while (curLv < maxLv) {
            curLv += 1;
            int need = StaticHeroDataMgr.getExperByQuality(quality, curLv);
            if (need > 0) {
                add += need;
            }
        }
        return add;
    }

    /**
     * 给将领加经验
     *
     * @param hero
     * @param addExp
     * @param maxLv  最高等级
     * @return 返回实际增加经验
     */
    public int addHeroExp(Hero hero, int addExp, int maxLv, Player player) {
        if (null == hero && addExp <= 0) {
            return 0;
        }
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (null == staticHero) {
            LogUtil.error("将领加经验，heroId为配置, heroId:", hero.getHeroId());
            return 0;
        }
        int preLv = hero.getLevel();
        int addHeroExp = addHeroExp(staticHero.getQuality(), hero, addExp, maxLv);
        if (preLv != hero.getLevel()) {
            // 更新任务进度
            taskDataManager.updTask(player, TaskType.COND_DESIGNATED_HERO_ID_UPGRADE, 1, staticHero.getHeroId());
            taskDataManager.updTask(player, TaskType.COND_DESIGNATED_HERO_QUALITY_UPGRADE, hero.getLevel(), staticHero.getQuality());
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_HERO_QUALITY_UPGRADE_CNT);
            taskDataManager.updTask(player, TaskType.COND_991, hero.getLevel(), staticHero.getQuality());
            taskDataManager.updTask(player, TaskType.COND_514, 1, hero.getLevel());
            //任务 - xx英雄升到xx级
            TaskService.handleTask(player, ETask.HERO_LEVELUP);
            ActivityDiaoChanService.completeTask(player, ETask.HERO_LEVELUP);
            TaskService.processTask(player, ETask.HERO_LEVELUP);
            // 更新将领属性
            CalculateUtil.processAttr(player, hero);
            //打印升级日志
            LogLordHelper.heroLvUp(player, hero.getHeroId(), addExp, preLv, hero.getLevel());
        }

        if (hero.isOnBattle() || hero.isOnWall()) {
            List<Long> rolesId = new ArrayList<>();
            EventBus.getDefault().post(
                    new Events.CrossPlayerChangeEvent(PlayerUploadTypeDefine.UPLOAD_TYPE_HERO,
                            0, CrossFunction.CROSS_WAR_FIRE, rolesId));
        }

        return addHeroExp;
    }

    // ===========================将领洗髓 begin ==================================

    /**
     * 将领洗髓
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
//    public HeroWashRs heroWash(long roleId, HeroWashRq req) throws MwException {
//        // 检查角色是否存在
//        Player player = playerDataManager.checkPlayerIsExist(roleId);
//
//        int heroId = req.getHeroId();
//        boolean useGold = false;
//        if (req.hasUseGold()) {
//            useGold = req.getUseGold();
//        }
//
//        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
//        if (null == staticHero) {
//            throw new MwException(GameError.HERO_NO_CONFIG.getCode(), "将领未配置, roleId:", player.roleId, ", heroId:",
//                    heroId);
//        }
//        if (staticHero.getQuality() <= 1) {
//            throw new MwException(GameError.HERO_NOT_WASH.getCode(), "将领不能洗髓, roleId:", player.roleId, ", heroId:",
//                    heroId);
//        }
//
//        // 检查玩家是否有该将领
//        Hero hero = checkHeroIsExist(player, heroId);
//
//        // if (!hero.isIdle()) {
//        // throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos，将领不在空闲中, roleId:", roleId, ", heroId:",
//        // heroId, ", state:", hero.getState());
//        // }
//
//        // 获取总属性洗髓配置
//        int[] preWash = hero.getWash();
//        int totalMax = staticHero.getTotalMax();// 将领总资质上限
//        int preTotal = preWash[1] + preWash[2] + preWash[3];
//        int total = preTotal;// 当前总资质
//        // int limit = totalMax - total;// 计算距离满属性差值
//        StaticResetTotal resetTotal = StaticHeroDataMgr.getResetTotalByLm(total); // 总洗髓配置表
//        if (null == resetTotal) {
//            throw new MwException(GameError.HERO_TOTAL_RESET_NO_CONFIG.getCode(), "总属性洗髓,配置未找到, roleId:", roleId,
//                    " , heroId:", heroId, ", total:", total, ", totalMax", totalMax);
//        }
//        List<List<Integer>> totalProbability = useGold ? resetTotal.getPay() : resetTotal.getFree(); // 总属性 洗髓的概率配置
//        Integer totalAddWash = RandomUtil.getRandomByWeight(totalProbability);// 权重随机
//
//        if (null == totalAddWash) {
//            throw new MwException(GameError.HERO_TOTAL_RESET_CONFIG_ERR.getCode(), "总属性洗髓配置格式错误, roleId:", roleId,
//                    " , heroId:", heroId, ", resetTotalId", resetTotal.getId());
//        }
//        LogUtil.debug("将领洗髓随机出来总属性增量  roleId:", roleId, ", heroId:", heroId, ", useGold:", useGold, ", totalAddWash:",
//                totalAddWash);
//        // 总属性 洗髓逻辑
//        if (total < totalMax) {// 非满级才对总属性进行调整
//            if (totalAddWash == 0) {
//                // 保底策略启动
//                int curCount = hero.getWashTotalFloorCount();
//                if (curCount + 1 >= Constant.WASH_TOTAL_FLOOR_COUNT) {
//                    total += Constant.ADD_WASH_TOTAL;
//                    hero.setWashTotalFloorCount(0);
//                } else {
//                    // 保底值加 +1
//                    hero.setWashTotalFloorCount(curCount + 1);
//                }
//            } else {
//                // 未随机到0的情况,保底值清零
//                hero.setWashTotalFloorCount(0);
//                total += totalAddWash;
//            }
//            // 最后值是否大于最大值
//            if (total > totalMax) {
//                total = totalMax;
//            }
//        }
//        LogUtil.debug("将领洗髓随得到的总属性  roleId:", roleId, ", heroId:", heroId, ", useGold:", useGold, ", total:", total,
//                ", totalMax:", totalMax);
//        // 单属性 洗髓逻辑
//        int[] curWash = new int[3];
//        int[] washMax = {staticHero.getAttackMax(), staticHero.getDefendMax(), staticHero.getLeadMax()};
//        // 1. 计算基准值
//        // 攻或防 基准=（当前总资质/最大总资质）*（当前品质对应 攻或防 资上限-10） 向上取整
//        double totalRatio = (total * 1.0) / (totalMax * 1.0); // （当前总资质/最大总资质）
//        int attackBase = (int) Math.ceil(totalRatio * (washMax[0] * 1.0 - 10.0));
//        int defendBase = (int) Math.ceil(totalRatio * (washMax[1] * 1.0 - 10.0));
//        // 2. 浮动值
//        do {
//            Integer attackFct = RandomUtil
//                    .getRandomByWeight(useGold ? Constant.WASH_PAY_FLUCTUATE : Constant.WASH_FREE_FLUCTUATE);
//            Integer defendFct = RandomUtil
//                    .getRandomByWeight(useGold ? Constant.WASH_PAY_FLUCTUATE : Constant.WASH_FREE_FLUCTUATE);
//            if (null == attackFct || null == defendFct) {
//                throw new MwException(GameError.HERO_TOTAL_RESET_CONFIG_ERR.getCode(), "洗髓单属性配置格式错误, roleId:", roleId,
//                        " , heroId:", heroId);
//            }
//            curWash[0] = attackBase + attackFct;
//            curWash[1] = defendBase + defendFct;
//            curWash[2] = total - curWash[0] - curWash[1];
//        } while (hero.getWash()[HeroConstant.ATTR_ATTACK] == curWash[0]
//                && hero.getWash()[HeroConstant.ATTR_DEFEND] == curWash[1]
//                && hero.getWash()[HeroConstant.ATTR_LEAD] == curWash[2]); // 保证和上次不一致
//
//        int[] washMin = {staticHero.getAttackMin(), staticHero.getDefendMin(), staticHero.getLeadMin()};
//        if (!checkWashInScope(washMax, washMin, curWash)) {
//            throw new MwException(GameError.HERO_TOTAL_RESET_CONFIG_ERR.getCode(), "洗髓上下限配置错误roleId:", player.roleId,
//                    ", washMax:", Arrays.toString(washMax), ", washMin:", Arrays.toString(washMin), ", curWash:",
//                    Arrays.toString(curWash));
//        }
//        int now = TimeHelper.getCurrentSecond();
//
//        // 扣钱扣次数操作
//        if (!useGold) {
//            // 检查是否有洗髓次数
//            if (player.common.getWashCount() <= 0) {
//                throw new MwException(GameError.NO_FREE_WASH.getCode(), "没有免费洗髓次数, roleId:", player.roleId, ", heroId:",
//                        heroId);
//            }
//            // 扣除洗髓次数
//            player.common.setWashCount(player.common.getWashCount() - 1);
//            int washTime = player.common.getWashTime();
//            if (washTime <= 0 && !player.washCountFull()) {// 剩余次数不满，且未开启刷新定时，开启
//                player.common.beginWashTime(now);
//            }
///*            if (player.common.getWashCount() < WorldConstant.HERO_WASH_FREE_MAX) {
//                player.removePushRecord(PushConstant.WASH_HERO_IS_FULL); // 移除推送
//            }*/
//            // 免费洗髓次数减为0
//            if (player.common.getWashCount() == 0) {
//                activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_HERO_WASH, player);
//            }
//
//            //貂蝉任务-初级特训次数
//            ActivityDiaoChanService.completeTask(player, ETask.TRAINING_LOW);
//            TaskService.processTask(player, ETask.TRAINING_LOW);
//        } else {
//            int need = WorldConstant.HERO_WASH_GOLD;
//            // 检查并扣除相应金币数
//            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, need,
//                    AwardFrom.HERO_GOLD_WASH, heroId);
//            // rewardDataManager.checkMoneyIsEnough(player.lord, AwardType.Money.GOLD, need, "将领至尊洗髓");
//            // rewardDataManager.subMoney(player, AwardType.Money.GOLD, need, AwardFrom.HERO_GOLD_WASH, "将领至尊洗髓");
//
//            //貂蝉任务-初级特训次数
//            ActivityDiaoChanService.completeTask(player, ETask.TRAINING_HIGH);
//            TaskService.processTask(player, ETask.TRAINING_HIGH);
//        }
//        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_HERO_WASH_CNT, useGold ? 1 : 0);
//
//        //貂蝉任务-英雄特训
//        ActivityDiaoChanService.completeTask(player, ETask.HERO_TRAINING);
//        //喜悦金秋-日出而作- 英雄特训x次（包含高级特训）
//        TaskService.processTask(player, ETask.HERO_TRAINING);
//
//        //任务处理
//        taskDataManager.updTask(player, TaskType.COND_993, 1, hero.getQuality());
//
//        // 洗属性
//        hero.getWash()[HeroConstant.ATTR_ATTACK] = curWash[0];
//        hero.getWash()[HeroConstant.ATTR_DEFEND] = curWash[1];
//        hero.getWash()[HeroConstant.ATTR_LEAD] = curWash[2];
//
//        // 原公式算法
//        /*if (total < totalMax) {
//            boolean result = RandomHelper.randomBool(); 
//            if (result) {
//                total++;
//            }
//        }
//        int offset = total - staticHero.getAttack() - staticHero.getDefend() - staticHero.getLead();// 记录可用于洗髓的总点数
//        int atkOffset = staticHero.getAttackMax() - staticHero.getAttack();// 攻击最大可增加数
//        int defOffset = staticHero.getDefendMax() - staticHero.getDefend();// 防御最大可增加数
//        int leadOffset = staticHero.getLeadMax() - staticHero.getLead();// 兵力最大可增加数
//        int minOffset = offset - defOffset - leadOffset;// 记录单个属性洗髓最小增加量，避免前面增加太少，导致后面用不完空闲点数
//        minOffset = minOffset < 0 ? 0 : minOffset;
//        int attack = staticHero.getAttack() + RandomHelper.randomInArea(minOffset, min(atkOffset, offset));// 最大可增加数与总剩余点数取最小值，防止溢出
//        offset -= (attack - staticHero.getAttack());
//        minOffset = offset - leadOffset;
//        minOffset = minOffset < 0 ? 0 : minOffset;
//        int defend = staticHero.getDefend() + RandomHelper.randomInArea(minOffset, min(defOffset, offset));
//        offset -= (defend - staticHero.getDefend());
//        int lead = staticHero.getLead() + offset;
//        
//        // 保存洗髓结果
//        hero.getWash()[1] = attack;
//        hero.getWash()[2] = defend;
//        hero.getWash()[3] = lead;
//        */
//
//        // 重新计算并更新将领属性
//        CalculateUtil.processAttr(player, hero);
//        // 计算士兵回营
//        CalculateUtil.returnArmy(player, hero);
//        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_HERO_WASH);
//        taskDataManager.updTask(player, TaskType.COND_WASH_HERO_42, 1);
//        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_WASH_HERO_42, 1);
//        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_WASH_HERO_42, 1);
//        taskDataManager.updTask(player, TaskType.COND_511, 1, Arrays.stream(curWash).sum());
//        taskDataManager.updTask(player, TaskType.COND_512, 1, curWash[0]);
//        HeroWashRs.Builder builder = HeroWashRs.newBuilder();
//        builder.setHeroId(hero.getHeroId());
//        for (int i = 1; i < hero.getWash().length; i++) {
//            builder.addWash(PbHelper.createTwoIntPb(i, hero.getWash()[i]));
//        }
//        for (int i = HeroConstant.ATTR_ATTACK; i <= HeroConstant.ATTR_LEAD; i++) {
//            builder.addAttr(PbHelper.createTwoIntPb(i, hero.getAttr()[i]));// 将领属性
//        }
//        builder.setGold(player.lord.getGold());
//        builder.setFree(player.common.getWashCount());
//        if (!checkWashTime(player, now)) {
//            builder.setWashTime(player.common.getWashTime());
//        }
//        return builder.build();
//    }

    /**
     * 检测洗髓时间
     *
     * @return
     */
    private boolean checkWashTime(Player player, int now) {
        return false;
//        return now + WorldConstant.HERO_WASH_FREE_MAX == player.common.getWashTime();
    }

    /**
     * 检测洗髓结果是否在范围内
     *
     * @param max
     * @param min
     * @param wash
     * @return true 满足条件
     */
    private boolean checkWashInScope(int[] max, int[] min, int[] wash) {
        for (int i = 0; i < max.length; i++) {
            if (wash[i] > max[i] || wash[i] < min[i]) return false;
        }
        return true;
    }

    /**
     * 获取两个值中小的那个
     *
     * @param a
     * @param b
     * @return
     */
    // private static int min(int a, int b) {
    // return a > b ? b : a;
    // }

    // ===========================将领洗髓 end ==================================

    /**
     * 保存将领洗髓（暂时废弃）
     *
     * @param roleId
     * @param req
     * @return
     */
    public SaveHeroWashRs saveHeroWash(long roleId, SaveHeroWashRq req) {
        return null;
    }

//    public HeroBreakRs heroBreak(long roleId, HeroBreakRq req) throws MwException {
//        Player player = playerDataManager.checkPlayerIsExist(roleId);
//        int heroId = req.getHeroId();
//        // 检查能否突破,哪些状态下不能突破
//        Hero hero = checkHeroIsExist(player, heroId);
//        if (hero.getState() != ArmyConstant.ARMY_STATE_IDLE) {
//            throw new MwException(GameError.HERO_BREAK_BATLLE.getCode(), "将领不在空闲状态, roleId:", player.roleId,
//                    ", heroId:", heroId + ",quality:" + hero.getQuality());
//        }
//        if (!hero.isIdle()) {
//            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos，将领不在空闲中, roleId:", roleId, ", heroId:",
//                    heroId, ", state:", hero.getState());
//        }
//        StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
//        StaticHeroBreak heroBreak = StaticHeroDataMgr.getHeroBreak(sHero.getQuality());
//        if (heroBreak == null) {
//            throw new MwException(GameError.HERO_BREAK_NO.getCode(), "突破找不到配置, roleId:", player.roleId, ", heroId:",
//                    heroId + ",quality:" + sHero.getQuality());
//        }
//        StaticHero newHero = StaticHeroDataMgr.getHeroByHeroIdAndQuality(sHero.getHeroType(), heroBreak.getToQuality());
//        if (newHero == null) {
//            throw new MwException(GameError.NO_CONFIG.getCode(), "突破找不到配置, roleId:", player.roleId, ", heroId:",
//                    heroId + ",quality:" + sHero.getQuality());
//        }
//
//        // 每日清理突破经验
//        playerDataManager.refreshDaily(player);
//
//        HeroBreakRs.Builder builder = HeroBreakRs.newBuilder();
//        // 扣道具
//        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.HERO_TOKEN,
//                heroBreak.getItemNum(), AwardFrom.HERO_BREAK);
//
//        // 判断是否有科技加成
//        List<Integer> techList = techDataManager.getTechEffect(player, TechConstant.TYPE_25);
//        int techEff = 0;
//        if (techList != null && techList.size() > 0) {
//            techEff = techList.get(0);
//        }
//
//        // 概率直接升级
//        boolean up = false;
//        if (heroBreak.getRatio() != null && !heroBreak.getRatio().isEmpty()) {
//            int cnt = hero.getBreakExp() / heroBreak.getStep();
//            for (List<Integer> pro : heroBreak.getRatio()) {
//                if (pro.size() >= 3) {
//                    if (pro.get(0) <= cnt && cnt <= pro.get(1)) {
//                        if (RandomHelper.isHitRangeIn10000(pro.get(2) + techEff)) {
//                            up = true;
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//        // 加经验
//        int preQuality = hero.getQuality();
//        if (up || hero.getBreakExp() + heroBreak.getStep() >= heroBreak.getExp()) {
//            int oldHeroId = hero.getHeroId();
//            hero.setQuality(heroBreak.getToQuality());
//            hero.setBreakExp(0);
//            // 升级变更heroId
//            processHeroWash(hero, newHero);
//            hero.setHeroId(newHero.getHeroId());
//
//            //处理采集鱼饵队列
//            fishingService.updateBaitTeamHeroId(player, heroId, hero.getHeroId());
//
//            // 替换上阵的旧数据
//            if (hero.getPos() > 0) {
//                player.heroBattle[hero.getPos()] = newHero.getHeroId();
//            }
//            // 替换城墙上面的旧的数据
//            if (hero.getWallPos() > 0) {
//                player.heroWall[hero.getWallPos()] = newHero.getHeroId();
//            }
//            // 替换防守上面的旧的数据
//            if (hero.getDefPos() > 0) {
//                player.heroDef[hero.getDefPos()] = newHero.getHeroId();
//            }
//            // 采集替换
//            if (hero.getAcqPos() > 0) {
//                player.heroAcq[hero.getAcqPos()] = newHero.getHeroId();
//            }
//            // 特攻替换
//            if (hero.getCommandoPos() > 0) {
//                player.heroCommando[hero.getCommandoPos()] = newHero.getHeroId();
//            }
//            // 替换副本将
//            if (player.combatHeroForm.contains(oldHeroId)) {
//                int index = player.combatHeroForm.indexOf(oldHeroId);
//                if (index != -1) {
//                    player.combatHeroForm.add(index, newHero.getHeroId());
//                    player.combatHeroForm.remove(index + 1);
//                }
//                LogUtil.debug("突破替换副本将领: index:", index, ", oldHeroId:", oldHeroId, ", newHeroId:",
//                        newHero.getHeroId());
//            }
//            // 装备换id
//            Equip equip;
//            if (hero.getEquip() != null) {
//                for (int equipKeyId : hero.getEquip()) {
//                    if (equipKeyId > 0) {
//                        equip = player.equips.get(equipKeyId);
//                        if (null != equip) {
//                            equip.onEquip(newHero.getHeroId());
//                        }
//                    }
//                }
//            }
//
//            // 勋章替换
//            if (!CheckNull.isEmpty(hero.getMedalKeys())) {
//                for (int key : hero.getMedalKeys()) {
//                    Medal medal = player.medals.get(key);
//                    if (null != medal) {
//                        medal.onMedal(newHero.getHeroId());
//                    }
//                }
//            }
//
//            // 战机替换
//            if (!CheckNull.isEmpty(hero.getWarPlanes())) {
//                for (Integer planeId : hero.getWarPlanes()) {
//                    WarPlane plane = player.checkWarPlaneIsExist(planeId);
//                    if (!CheckNull.isNull(plane)) {
//                        plane.setHeroId(newHero.getHeroId());
//                    }
//                }
//            }
//
//            //宝具替换
//            if (hero.getTreasureWare() != null) {
//                TreasureWare treasureWare = player.treasureWares.get(hero.getTreasureWare());
//                if (Objects.nonNull(treasureWare)) {
//                    treasureWare.onEquip(newHero.getHeroId());
//                }
//            }
//
//            player.heros.put(newHero.getHeroId(), hero);
//            // 更新将领属性
//            CalculateUtil.processAttr(player, hero);
//
//            // 将领列表删除旧数据
//            player.heros.remove(heroId);
//            LogUtil.debug("删除就数据heroId=" + heroId + ",obj=" + player.heros.get(heroId) + ",new="
//                    + player.heros.get(newHero.getHeroId()));
//
//            builder.setHeroId(heroId);
//            if (heroBreak.getToQuality() >= 6) {
//                chatDataManager.sendSysChat(ChatConst.CHAT_HERO_BREAK, player.lord.getCamp(), 0, player.lord.getNick(),
//                        newHero.getHeroId());
//            }
//
//            //处理任务
//            taskDataManager.updTask(player, TaskType.COND_992, 1, heroBreak.getQuality());
//            taskDataManager.updTask(player, TaskType.COND_27, 1, hero.getQuality());
//            taskDataManager.updTask(player, TaskType.COND_517, 1, TaskCone517Type.getCondId(hero.getQuality(), 1/*hero.getStage()*/));
//
//            LogLordHelper.hero(AwardFrom.HERO_BREAK, player.account, player.lord, oldHeroId, Constant.ACTION_SUB);
//            LogLordHelper.hero(AwardFrom.HERO_BREAK, player.account, player.lord, newHero.getHeroId(), Constant.ACTION_ADD);
//        } else {
//            hero.setBreakExp(hero.getBreakExp() + heroBreak.getStep());
//        }
//
//        StaticHeroBreak newHeroBreak = StaticHeroDataMgr.getHeroBreak(newHero.getQuality());
//        LogLordHelper.gameLog(LogParamConstant.HERO_BREAK, player, AwardFrom.HERO_BREAK, preQuality == hero.getQuality() ? LogParamConstant.HERO_BREAK_IN_SAME_QUALITY
//                : LogParamConstant.HERO_BREAK_IN_DIFFERENT_QUALITY, hero.getBreakExp(), CheckNull.isNull(newHeroBreak) ? newHero.getQuality() : newHeroBreak.getToQuality(), CheckNull.isNull(newHeroBreak) ? 0 : newHeroBreak.getExp());
//
//        builder.setHero(PbHelper.createHeroPb(hero, player));
//        builder.setHeroToken(player.lord.getHeroToken());
//        if (hero.isOnBattle() || hero.isOnWall()) {
//            List<Long> rolesId = new ArrayList<>();
//            EventBus.getDefault().post(
//                    new Events.CrossPlayerChangeEvent(PlayerUploadTypeDefine.UPLOAD_TYPE_HERO,
//                            0, CrossFunction.CROSS_WAR_FIRE, rolesId));
//        }
//
//        return builder.build();
//    }

    private void processHeroWash(Hero hero, StaticHero newHero) {
//        hero.getWash()[HeroConstant.ATTR_ATTACK] += newHero.getAttack();
//        hero.getWash()[HeroConstant.ATTR_DEFEND] += newHero.getDefend();
//        hero.getWash()[HeroConstant.ATTR_LEAD] += newHero.getLead();
    }

    /**
     * 获取将领洗髓次数等信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
//    public GetHeroWashInfoRs getHeroWashInfo(long roleId) throws MwException {
//        // 检查角色是否存在
//        Player player = playerDataManager.checkPlayerIsExist(roleId);
//
//        Common common = player.common;
//
//        GetHeroWashInfoRs.Builder builder = GetHeroWashInfoRs.newBuilder();
//        builder.setWashCount(common.getWashCount());
//        builder.setWashTime(common.getWashTime());
//        return builder.build();
//    }

    /**
     * 获取良将寻访信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
//    public GetHeroSearchRs getHeroSearch(long roleId) throws MwException {
//        // 检查角色是否存在
//        Player player = playerDataManager.checkPlayerIsExist(roleId);
//
//        GetHeroSearchRs.Builder builder = GetHeroSearchRs.newBuilder();
//        if (player.lord.getLevel() >= HeroConstant.HERO_SEARCH_ROLE_LV) {
//            builder.setOpen(true);
//
//            Common common = player.common;
//            refreshHeroSearchData(common);// 刷新将领寻访数据
//
//            builder.setCdTime(common.getHeroCdTime());
//            builder.setNormalNum(HeroConstant.NORMAL_SPECIL_NUM - common.getNormalHero());
//            builder.setSuperProcess(common.getSuperProcess());
//            builder.setSuperNum(showSuperSearchCnt(common.getSuperHero()));
//            builder.setSuperOpenNum(common.getSuperOpenNum());
//            builder.setSuperTime(common.getSuperTime());
//            builder.setSuperFreeNum(common.getSuperFreeNum());
//            if (CheckNull.nonEmpty(player.getRecruitReward())) {
//                player.getRecruitReward().forEach((key, value) -> builder.addRecruitReward(PbHelper.createTwoIntPb(key, value)));
//            }
//            builder.setRecruitRecord(player.getMixtureDataById(PlayerConstant.NORMAL_HERO_SEARCH_COUNT));
//            builder.setWishHero(PbHelper.createTwoIntPb(player.getMixtureDataById(PlayerConstant.WISH_HERO), player.getMixtureDataById(PlayerConstant.WISH_HERO_SEARCH_COUNT)));
//        } else {
//            builder.setOpen(false);
//        }
//        return builder.build();
//    }

    /**
     * 刷新将领寻访数据
     *
     * @param common
     */
//    public void refreshHeroSearchData(Common common) {
//        int now = TimeHelper.getCurrentSecond();
//        if (common.getHeroCdTime() > 0 && now >= common.getHeroCdTime()) {
//            common.setHeroCdTime(0);// 良将寻访免费次数过了CD，清空CD时间
//        }
//
//        if (common.getSuperTime() > 0 && now >= common.getSuperTime()) {// 神将激活后过了时间，清空相关数据
//            common.setSuperTime(0);
//            // common.setSuperHero(0);
//            common.setSuperProcess(0);
//            common.setSuperFreeNum(0);
//        }
//    }

    /**
     * 获取上阵将领在其他地方位置的映射
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetHeroBattlePosRs getHeroBattlePos(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetHeroBattlePosRs.Builder bulider = GetHeroBattlePosRs.newBuilder();
        List<Integer> combatPos = player.heroBattlePos.get(HeroConstant.CHANGE_COMBAT_POS_TYPE);
        if (CheckNull.isEmpty(combatPos)) {// 为空就是默认
            for (int i = 1; i < player.heroBattle.length; i++) {
                if (player.heroBattle[i] > 0) {
                    bulider.addCombatPos(i);
                }
            }
        } else {
            bulider.addAllCombatPos(combatPos);
        }

        List<Integer> battlePos = player.heroBattlePos.get(HeroConstant.CHANGE_BATTLE_POS_TYPE);
        if (CheckNull.isEmpty(battlePos)) {
            for (int i = 1; i < player.heroBattle.length; i++) {
                if (player.heroBattle[i] > 0) {
                    bulider.addBattlePos(i);
                }
            }
        } else {
            bulider.addAllBattlePos(battlePos);
        }

        return bulider.build();
    }

    /**
     * 良将寻访
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
//    public SearchHeroRs searchHero(long roleId, SearchHeroRq req) throws MwException {
//        // 检查角色是否存在
//        Player player = playerDataManager.checkPlayerIsExist(roleId);
//
//        if (player.lord.getLevel() < HeroConstant.HERO_SEARCH_ROLE_LV) {
//            throw new MwException(GameError.HERO_SEARCH_NOT_OPEN.getCode(), "玩家将领寻访未开启, roleId:", player.roleId);
//        }
//
//        int searchType = req.getSearchType();
//        int countType = req.getCountType();
//        int costType = req.getCostType();
//        // 检查参数是否正确
//        if (!realSearchType(searchType) || !realCountType(countType) || !realSearchCostType(costType)) {
//            throw new MwException(GameError.PARAM_ERROR.getCode(), "将领寻访，参数错误, roleId:", player.roleId, ", searchType:",
//                    searchType, ", countType:", countType, ", costType:", costType);
//        }
//
//        if (searchType == HeroConstant.SEARCH_TYPE_SUPER && countType == HeroConstant.COUNT_TYPE_ONE
//                && costType == HeroConstant.SEARCH_COST_FREE && player.common.getSuperFreeNum() > 0) {
//            throw new MwException(GameError.PARAM_ERROR.getCode(), "将领寻访，神将无免费次数, roleId:", player.roleId,
//                    ", searchType:", searchType, ", countType:", countType, ", costType:", costType);
//        }
//
//        Common common = player.common;
//        refreshHeroSearchData(common);// 刷新将领寻访数据
//
//        int gold = 0;// 如果金币寻访，记录需要消耗的金币数
//        if (costType == HeroConstant.SEARCH_COST_FREE) {// 如果是使用免费次数良将寻访，检查相关数据
//            if (countType != HeroConstant.COUNT_TYPE_ONE) {
//                throw new MwException(GameError.PARAM_ERROR.getCode(), "将领寻访，免费寻访只能单次寻访, roleId:", player.roleId,
//                        ", searchType:", searchType, ", countType:", countType, ", costType:", costType);
//            }
//        } else if (costType == HeroConstant.SEARCH_COST_GOLD) {
//            gold = HeroConstant.getHeroSearchGoldByType(searchType, countType);
//            if (gold < 0) {
//                throw new MwException(GameError.NO_CONFIG.getCode(), "将领寻访金币消耗未配置, roleId:", player.roleId,
//                        ", searchType:", searchType, ", countType:", countType);
//            }
//        }
//
//        // 如果是神将寻访，检查是否开启
//        if (searchType == HeroConstant.SEARCH_TYPE_SUPER) {
//            if (common.getSuperProcess() < Constant.INT_HUNDRED) {
//                throw new MwException(GameError.HERO_SUPER_SEARCH_NOT_OPEN.getCode(), "神将寻访未开启, roleId:", player.roleId,
//                        ", superProcess:", common.getSuperProcess());
//            }
//        }
//
//        int count = 0;// 记录本次准备寻访次数
//        if (countType == HeroConstant.COUNT_TYPE_ONE) {// 寻访1次
//            count = 1;
//        } else if (countType == HeroConstant.COUNT_TYPE_TEN) {// 寻访10次
//            count = 10;
//        }
//
//        int costCount = 0;
//        if (costType == HeroConstant.SEARCH_COST_PROP) costCount = count;
//        if (costType == HeroConstant.SEARCH_COST_GOLD) costCount = gold;
//
//        // gold *= count; 已经算好了
//        ChangeInfo change = ChangeInfo.newIns();// 记录玩家资源变更类型
//
//        // 检查并扣除相关消耗
//        if (searchType == HeroConstant.SEARCH_TYPE_NORMAL) {
//            if (costType == HeroConstant.SEARCH_COST_FREE) {
//                int now = TimeHelper.getCurrentSecond();
//                if (common.getHeroCdTime() > now) {
//                    throw new MwException(GameError.HERO_SEARCH_CD.getCode(), "良将寻访免费次数CD中, roleId:", player.roleId,
//                            ", cdTime:", common.getHeroCdTime(), ", now:", now);
//                }
//
//                // 扣除免费次数
//                common.setHeroCdTime(now + HeroConstant.NORMAL_SEARCH_CD);
//            } else if (costType == HeroConstant.SEARCH_COST_PROP) {
//                rewardDataManager.checkPropIsEnough(player, HeroConstant.NORMAL_HERO_ID, count, "良将道具寻访");
//                rewardDataManager.subProp(player, HeroConstant.NORMAL_HERO_ID, count, AwardFrom.HERO_NORMAL_SEARCH);// "良将道具寻访"
//                change.addChangeType(AwardType.PROP, HeroConstant.NORMAL_HERO_ID);
//            } else if (costType == HeroConstant.SEARCH_COST_GOLD) {
//                rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, gold, "良将金币寻访");
//                rewardDataManager.subGold(player, gold, AwardFrom.HERO_NORMAL_SEARCH);
//                change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
//            }
//        } else if (searchType == HeroConstant.SEARCH_TYPE_SUPER) {
//            if (costType == HeroConstant.SEARCH_COST_PROP) {
//                rewardDataManager.checkPropIsEnough(player, HeroConstant.SUPER_HERO_ID, count, "神将道具寻访");
//                rewardDataManager.subProp(player, HeroConstant.SUPER_HERO_ID, count, AwardFrom.HERO_SUPER_SEARCH);// "神将道具寻访"
//                change.addChangeType(AwardType.PROP, HeroConstant.SUPER_HERO_ID);
//            } else if (costType == HeroConstant.SEARCH_COST_GOLD) {
//                rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, gold, "神将金币寻访");
//                rewardDataManager.subGold(player, gold, AwardFrom.HERO_SUPER_SEARCH);
//                change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
//            } else if (costType == HeroConstant.SEARCH_COST_FREE) {
//                player.common.setSuperFreeNum(1);
//            }
//        }
//        // 更新战令任务进度
//        battlePassDataManager.updTaskSchedule(roleId, TaskType.COND_SEARCH_HERO_CNT, count);
//        // 通知玩家消耗的资源类型
//        rewardDataManager.syncRoleResChanged(player, change);
//        // 检测触发将领寻访礼包
//        activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_HERO_SEARCH, player);
//
//        SearchHeroRs.Builder builder = SearchHeroRs.newBuilder();
//        for (int i = 0; i < count; i++) {// 执行将领寻访逻辑
//            SearchHero sh = doHeroSearch(player, searchType, costCount);
//            if (null != sh) {
//                builder.addHero(sh);
//            }
//        }
//        if (searchType == HeroConstant.SEARCH_TYPE_NORMAL) {// 良将寻访
//            builder.setCount(HeroConstant.NORMAL_SPECIL_NUM - common.getNormalHero());
//            builder.setCdTime(common.getHeroCdTime());
//            builder.setSuperProcess(common.getSuperProcess());
//            if (common.getSuperProcess() >= Constant.INT_HUNDRED) {
//                builder.setSuperTime(common.getSuperTime());
//            }
//        } else if (searchType == HeroConstant.SEARCH_TYPE_SUPER) {
//            builder.setCount(showSuperSearchCnt(common.getSuperHero()));
//        }
//        builder.setRecruitRecord(player.getMixtureDataById(PlayerConstant.NORMAL_HERO_SEARCH_COUNT));
//        builder.setWishHero(PbHelper.createTwoIntPb(player.getMixtureDataById(PlayerConstant.WISH_HERO), player.getMixtureDataById(PlayerConstant.WISH_HERO_SEARCH_COUNT)));
//        builder.setSuperFreeNum(common.getSuperFreeNum());
//        builder.setSuperOpenNum(common.getSuperOpenNum());
//        return builder.build();
//    }

    /**
     * 计算升降寻访的次数
     *
     * @param searchCnt
     * @return
     */
    private static int superSearchCnt(int searchCnt) {
        if (searchCnt >= HeroConstant.SUPER_FIRST_SPECIL_NUM) {
            int sc = searchCnt - HeroConstant.SUPER_FIRST_SPECIL_NUM;
            return sc % HeroConstant.SUPER_SPECIL_NUM;
        } else {
            return searchCnt % HeroConstant.SUPER_FIRST_SPECIL_NUM;
        }

    }

    /**
     * 客户度神将的显示剩余次数
     *
     * @param searchCnt
     * @return
     */
    private static int showSuperSearchCnt(int searchCnt) {
        int cnt = superSearchCnt(searchCnt);
        if (searchCnt >= HeroConstant.SUPER_FIRST_SPECIL_NUM) {
            return HeroConstant.SUPER_SPECIL_NUM - cnt;
        } else {
            int c = HeroConstant.SUPER_FIRST_SPECIL_NUM - cnt;
            // return c <= 0 ? HeroConstant.SUPER_SPECIL_NUM : c;
            return c;
        }
    }

    /**
     * 单次将令寻访逻辑
     *
     * @param
     * @param searchType
     * @return
     * @throws MwException
     */
//    public SearchHero doHeroSearch(Player player, int searchType, int costCount) throws MwException {
//        int specialNum = 0;// 需要特殊处理的次数，如良将寻访达到10次必出良将
//        int searchCount = 0;// 记录已寻访次数
//        boolean wishHero = false;// 心愿将领
//        StaticHeroSearch shs = null;// 记录随机到的奖励信息
//        Common common = player.common;
//        if (searchType == HeroConstant.SEARCH_TYPE_NORMAL) {
//            // 记录寻访次数
//            searchCount = common.getNormalHero() + 1;
//            specialNum = HeroConstant.NORMAL_SPECIL_NUM;
//
//            if (searchCount >= specialNum) {// 如果达到条件，必出良将
//                shs = HeroSearchRandom.randomHeroBySearchType(searchType);
//                // 清空寻访次数
//                common.setNormalHero(0);
//            } else {
//                shs = HeroSearchRandom.randomRewardBySearchType(searchType);
//                common.setNormalHero(searchCount);
//            }
//            // 记录神将激活进度
//            if (common.getSuperProcess() < Constant.INT_HUNDRED) {
//                int process = HeroConstant.TOKEN_ADD_PROCESS;
//                if (null != shs && shs.getRewardType() == HeroConstant.SEARCH_RESULT_HERO) {
//                    process = HeroConstant.HERO_ADD_PROCESS;// 出良将加的进度与出将令加的进度不同
//                }
//                common.addHeroSearchSuperProcess(process);
//            }
//            // 心愿武将功能开启后，再记录玩家武将抽取次数
//            if (StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_WISH_HERO)) {
//                player.setMixtureData(PlayerConstant.NORMAL_HERO_SEARCH_COUNT, player.getMixtureDataById(PlayerConstant.NORMAL_HERO_SEARCH_COUNT) + 1);
//            }
//        } else if (searchType == HeroConstant.SEARCH_TYPE_SUPER) {
//            // 记录寻访次数
//            searchCount = common.getSuperHero() + 1;
//            // if (common.getSuperOpenNum() == 1) {// 首次激活，特殊处理
//            // specialNum = HeroConstant.SUPER_FIRST_SPECIL_NUM;
//            // } else {
//            // specialNum = HeroConstant.SUPER_SPECIL_NUM;
//            // }
//
//            int wishHeroId = player.getMixtureDataById(PlayerConstant.WISH_HERO);
//
//            if (wishHeroId != 0) {
//                int specialCount = player.getMixtureDataById(PlayerConstant.WISH_HERO_SEARCH_COUNT);
//                if (specialCount > 0) {
//                    player.setMixtureData(PlayerConstant.WISH_HERO_SEARCH_COUNT, --specialCount);
//                    if (specialCount == 0) {
//                        wishHero = true;
//                    }
//                }
//            }
//
//            if (wishHero) {
//                shs = StaticHeroDataMgr.getHeroSearchMap().get(wishHeroId);
//            } else if (HeroConstant.SEARCH_SUPER_HERO_SPECIAL.get(searchCount) != null) {// 优先判断,是否特殊次数
//                // 获取的将领
//                Integer autoId = HeroConstant.SEARCH_SUPER_HERO_SPECIAL.get(searchCount);
//                shs = StaticHeroDataMgr.getHeroSearchMap().get(autoId);
//                common.setSuperOpenNum(common.getSuperOpenNum() + 1);
//            } else { // 无特殊次数
//                if (searchCount == HeroConstant.SUPER_FIRST_SPECIL_NUM) { // 首次激活，特殊处理
//                    Integer autoId = RandomUtil.getRandomByWeight(HeroConstant.SEARCH_SUPER_HERO_FOR_FIVE);
//                    shs = StaticHeroDataMgr.getHeroSearchMap().get(autoId);
//                    common.setSuperOpenNum(common.getSuperOpenNum() + 1);
//                } else if (superSearchCnt(searchCount) == 0) {// 如果达到条件，必出神将
//                    shs = HeroSearchRandom.randomHeroBySearchType(searchType);
//                    // common.setSuperHero(0);// 清空寻访次数 (修改不继续清0操作)
//                    common.setSuperOpenNum(common.getSuperOpenNum() + 1);
//                } else {
//                    shs = HeroSearchRandom.randomRewardBySearchType(searchType);
//                }
//            }
//            common.setSuperHero(searchCount);
//        }
//        if (CheckNull.isNull(shs)) {
//            throw new MwException(GameError.NO_CONFIG.getCode(), "寻访配置错误   searchType:", searchType, ", specialNum:",
//                    specialNum, ", searchCount:", searchCount, ", shs:", shs);
//        }
//        SearchHero.Builder builder = SearchHero.newBuilder();
//        int heroTokenCount = 0;
//        if (null != shs) {
//            builder.setSearchId(shs.getAutoId());
//            builder.setWish(wishHero);
//            if (shs.getRewardType() == HeroConstant.SEARCH_RESULT_HERO) {
//                // 检查玩家是否已拥有该将领
//                int heroId = shs.getRewardList().get(0).get(1);
//                StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
//                if (null == staticHero) {
//                    LogUtil.error("将领寻访，寻访到的将领未找到配置信息, heroId:", heroId);
//                    return null;
//                } else {
//                    boolean containHero = false;
//                    StaticHero staticHeroOld = null;
//                    for (Hero v : player.heros.values()) {
//                        staticHeroOld = StaticHeroDataMgr.getHeroMap().get(v.getHeroId());
//                        if (staticHeroOld.getHeroType() == staticHero.getHeroType()) {
//                            containHero = true;
//                        }
//                    }
//                    if (player.heros.containsKey(staticHero.getHeroId()) || containHero) {// 如果玩家已有该将领，奖励转化为将令
//                        if (searchType == HeroConstant.SEARCH_TYPE_NORMAL) {
//                            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.HERO_TOKEN,
//                                    HeroConstant.NORMAL_HERO_TOKEN, AwardFrom.HERO_NORMAL_SEARCH);
//                            heroTokenCount += HeroConstant.NORMAL_HERO_TOKEN;
//                        } else if (searchType == HeroConstant.SEARCH_TYPE_SUPER) {
//                            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.HERO_TOKEN,
//                                    HeroConstant.SUPER_HERO_TOKEN, AwardFrom.HERO_SUPER_SEARCH);
//                            heroTokenCount += HeroConstant.SUPER_HERO_TOKEN;
//                        }
//
//                        LogUtil.debug("寻访获得将领，转化为将令=" + staticHero.getHeroId());
//                    } else {// 发送奖励
//                        if (searchType == HeroConstant.SEARCH_TYPE_NORMAL) {
//                            rewardDataManager.sendReward(player, shs.getRewardList(), AwardFrom.HERO_NORMAL_SEARCH);
//                        } else if (searchType == HeroConstant.SEARCH_TYPE_SUPER) {
//                            rewardDataManager.sendReward(player, shs.getRewardList(), AwardFrom.HERO_SUPER_SEARCH);
//                            // 获得神将发消息
//                            int goldHeroId = shs.getRewardList().get(0).get(1);
//                            LogUtil.debug("获取神将 roleId:", player.roleId, ", goldHeroId:", goldHeroId);
//                            chatDataManager.sendSysChat(ChatConst.CHAT_RECRUIT_HERO, player.lord.getCamp(), 0,
//                                    player.lord.getNick(), goldHeroId);
//                        }
//                        Hero hero = player.heros.get(staticHero.getHeroId());
//                        LogUtil.debug("寻访获得将领=" + staticHero.getHeroId() + ",hero=" + hero);
//                        builder.setHero(PbHelper.createHeroPb(hero, player));// 返回新得到的将领信息
//                    }
//                }
//            } else { // 发送将令奖励
//                if (searchType == HeroConstant.SEARCH_TYPE_NORMAL) {
//                    rewardDataManager.sendReward(player, shs.getRewardList(), AwardFrom.HERO_NORMAL_SEARCH);
//                } else if (searchType == HeroConstant.SEARCH_TYPE_SUPER) {
//                    rewardDataManager.sendReward(player, shs.getRewardList(), AwardFrom.HERO_SUPER_SEARCH);
//                }
//                if (CheckNull.nonEmpty(shs.getRewardList())) {
//                    heroTokenCount = shs.getRewardList().stream().filter(reward -> CheckNull.nonEmpty(reward) && reward.size() >= 3
//                            && reward.get(0) == AwardType.MONEY && reward.get(1) == AwardType.Money.HERO_TOKEN).mapToInt(reward -> reward.get(2)).sum();
//                }
//            }
//        }
//
//        LogLordHelper.gameLog(LogParamConstant.HERO_SEARCH_METHOD, player, AwardFrom.LOG_HERO_SEARCH, searchType, CheckNull.isNull(builder.getHero()) ? 0 :
//                builder.getHero().getHeroId(), heroTokenCount, costCount, searchType == HeroConstant.SEARCH_TYPE_NORMAL ? common.getNormalHero() : common.getSuperHero());
//        return builder.build();
//    }

    private boolean realSearchType(int searchType) {
        return searchType == HeroConstant.SEARCH_TYPE_NORMAL || searchType == HeroConstant.SEARCH_TYPE_SUPER;
    }

    private boolean realCountType(int countType) {
        return countType == HeroConstant.COUNT_TYPE_ONE || countType == HeroConstant.COUNT_TYPE_TEN;
    }

    private boolean realSearchCostType(int costType) {
        return costType == HeroConstant.SEARCH_COST_FREE || costType == HeroConstant.SEARCH_COST_PROP
                || costType == HeroConstant.SEARCH_COST_GOLD;
    }

    /**
     * 将领相关定时任务
     */
//    public void heroTimeLogic() {
//        int washTime;
//        int now = TimeHelper.getCurrentSecond();
//        for (Player player : playerDataManager.getPlayers().values()) {
//            if (player.common != null && !player.washCountFull()) {
//                washTime = player.common.getWashTime();
//                if (washTime <= 0) {// 剩余次数不满，且未开启刷新定时，开启
//                    player.common.beginWashTime(now);
//                } else if (washTime <= now) {// 刷新时间到，更新洗髓次数
//                    player.common.washTimeEnd(now);
//                    if (player.common.getWashCount() >= WorldConstant.HERO_WASH_FREE_MAX) {
//                        pushWashHeroMsg(player);
//                    }
//                }
//            }
//        }
//
//    }

    private void pushWashHeroMsg(Player p) {
        /*if (!p.hasPushRecord(String.valueOf(PushConstant.WASH_HERO_IS_FULL))) {
            PushMessageUtil.pushMessage(p.account, PushConstant.WASH_HERO_IS_FULL);
            p.putPushRecord(PushConstant.WASH_HERO_IS_FULL, PushConstant.PUSH_HAS_PUSHED);
        }*/
    }

    /**
     * 将领授勋 将领觉醒
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public HeroDecoratedRs heroDecorated(long roleId, HeroDecoratedRq req) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        List<Integer> equipKeyIdList = req.getEquipKeyIdList(); // 装备
        int heroId = req.getHeroId();

        // 将领条件检测------------------
        Hero hero = checkHeroIsExist(player, heroId);
        if (!hero.isIdle()) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "授勋将领不在空闲中, roleId:", roleId, ", heroId:", heroId,
                    ", state:", hero.getState());
        }
        StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (sHero == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "授勋将领找不到配置, roleId:", player.roleId, ", heroId:",
                    heroId);
        }
        int cnt = hero.getDecorated() + 1;
        StaticHeroDecorated sHeroDecorated = StaticHeroDataMgr.getHeroDecoratedMap().get(cnt);
        if (sHeroDecorated == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "授勋将领等级已满,或找不到配置, roleId:", player.roleId, ", heroId:",
                    heroId, ", cnt:", cnt);
        }
        // 获取英雄升阶配置信息
        StaticHeroUpgrade staticHeroUpgrade = StaticHeroDataMgr.getStaticHeroUpgrade(hero.getGradeKeyId());
        if (CheckNull.isNull(staticHeroUpgrade) || staticHeroUpgrade.getGrade() < sHeroDecorated.getHeroGrade()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "授勋将领品阶不足, roleId:", player.roleId, ", heroId:",
                    heroId);
        }

        // 检测装备是否满足------------------
        if (equipKeyIdList.size() != sHeroDecorated.getNeedEquip().size() * sHeroDecorated.getSetsOf()) {
            throw new MwException(GameError.HERO_DECORATED_EQUIP_NOT_ENOUGH.getCode(), "将领授勋装备条件不满足(个数有错误), roleId:",
                    player.roleId, ", heroId:", heroId);
        }
        Map<Integer, Integer> equipParCnt = new HashMap<>(); // key:装备位置,value:数量
        for (int equipKeyId : equipKeyIdList) {
            Equip equip = player.equips.get(equipKeyId);
            if (null == equip) {
                throw new MwException(GameError.EQUIP_NOT_FOUND.getCode(), "玩家没有这个装备, roleId:", roleId, ", equipKeyId:",
                        equipKeyId);
            }
            // 检查装备是否已经被穿戴
            if (equip.isOnEquip()) {
                throw new MwException(GameError.EQUIP_HAS_ON_HERO.getCode(), "将领授勋已经有将领穿戴了这个装备, roleId:", roleId,
                        ", equipKeyId:", equipKeyId, ", 穿戴该装备的将领id:", equip.getHeroId());
            }
            StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
            if (null == staticEquip) {
                throw new MwException(GameError.EQUIP_NO_CONFIG.getCode(), "装备未配置, roleId:", roleId, ", equipKeyId:",
                        equipKeyId);
            }
            Integer attrType = sHeroDecorated.findAttrByEquipId(equip.getEquipId()); // 需要的装备的属性
            if (attrType == null) {
                throw new MwException(GameError.HERO_DECORATED_EQUIP_NOT_ENOUGH.getCode(), "将领授勋装备条件不满足, roleId:",
                        player.roleId, ", heroId:", heroId, ", equipId:", equip.getEquipId());
            }
            // 检查装备是否触发秘籍
            if (!equipService.checkEquipIsEquip4Skill(equip)) {
                throw new MwException(GameError.HERO_DECORATED_EQUIP_NOT_ENOUGH.getCode(),
                        "将领授勋装备条件不满足(秘籍条件没达到), roleId:", player.roleId, ", heroId:", heroId, ", equipId:",
                        equip.getEquipId());
            }
            if (attrType.intValue() != equip.getAttrAndLv().get(0).getA()) { // 属性是否满足
                throw new MwException(GameError.HERO_DECORATED_EQUIP_NOT_ENOUGH.getCode(),
                        "将领授勋装备条件不满足(技能属性符合), roleId:", player.roleId, ", heroId:", heroId, ", equipId:",
                        equip.getEquipId());
            }
            int equipPart = staticEquip.getEquipPart();
            int eCnt = equipParCnt.getOrDefault(equipPart, 0);
            equipParCnt.put(equipPart, eCnt + 1);
        }
        if (equipParCnt.size() != sHeroDecorated.getNeedEquip().size()) {
            throw new MwException(GameError.HERO_DECORATED_EQUIP_NOT_ENOUGH.getCode(), "将领授勋装备条件不满足(个数的id错误), roleId:",
                    player.roleId, ", heroId:", heroId);
        }
        for (Entry<Integer, Integer> kv : equipParCnt.entrySet()) {
            int par = kv.getKey();
            if (kv.getValue() < sHeroDecorated.getSetsOf()) {
                throw new MwException(GameError.HERO_DECORATED_EQUIP_NOT_ENOUGH.getCode(),
                        "将领授勋装备条件不满足(部位数量不足), roleId:", player.roleId, ", heroId:", heroId, ",par:", par);
            }
        }
        // 扣装备
        for (int equipKeyId : equipKeyIdList) {
            rewardDataManager.subEquip(player, equipKeyId, AwardFrom.HERO_DECORATED_COST);
        }
//        // 将领觉醒 触发式礼包
//        AwakenData awaken = hero.getAwaken();
//        activityTriggerService.heroDecoratedTriggerGift(player, sHero.getType(), awaken.isActivate());
        // 授勋
        hero.setDecorated(hero.getDecorated() + 1);
        // 更新战令任务的进度
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_HERO_DECORATED_HAVE_CNT, 1, hero.getDecorated());
        // 更新世界目标进度: 全服有N个N次觉醒英雄
        worldScheduleService.updateScheduleGoal(player, ScheduleConstant.GOAL_COND_HERO_DECORATED, hero.getDecorated());
        // 更新将领属性
        CalculateUtil.processAttr(player, hero);
        LogLordHelper.commonLog("heroDecorated", AwardFrom.HERO_DECORATED_COST, player, hero.getHeroId());
        // 发公告
        chatDataManager.sendSysChat(ChatConst.CHAT_HERO_DECORATED, 0, 0, player.lord.getCamp(), player.lord.getNick(),
                hero.getDecorated(), heroId);
        HeroDecoratedRs.Builder builder = HeroDecoratedRs.newBuilder();
        builder.setHero(PbHelper.createHeroPb(hero, player));

        if (hero.isOnBattle() || hero.isOnWall()) {
            List<Long> rolesId = new ArrayList<>();
            EventBus.getDefault().post(
                    new Events.CrossPlayerChangeEvent(PlayerUploadTypeDefine.UPLOAD_TYPE_HERO,
                            0, CrossFunction.CROSS_WAR_FIRE, rolesId));
        }

        return builder.build();
    }

    public int adaptHeroAddExp(Player player, int addExp) {
        int seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_404);
        addExp = (int) (addExp * (1 + seasonTalentEffect / Constant.TEN_THROUSAND));
        return addExp;
    }

    /**
     * 检测并修复玩家上阵将领信息
     *
     * @param player   玩家
     * @param bRepaire true 开启修复
     */
    public void checkAndRepaireHero(Player player, boolean bRepaire) {
        checkHeroQueueStatus(player);
        if (bRepaire) {
            if (player.isLogin && player.account.getIsGm() == 0 && player.ctx != null) {
                player.ctx.close();
            }
            repaire(player);
        }
    }

    /**
     * 使用出战阵形重置防守阵形
     *
     * @param player
     */
    public void repaire(Player player) {
        //修复出战队列
        int[] heroBattle = player.heroBattle;
        StringBuilder logSb = new StringBuilder();
        logSb.append(String.format("roleId :%d 修复前的出战队列 :%s", player.roleId, Arrays.toString(heroBattle))).append("\n");
        for (int i = 1; i < heroBattle.length; i++) {
            if (heroBattle[i] == 0) {
                for (int j = i + 1; j < heroBattle.length; j++) {
                    if (heroBattle[j] != 0) {
                        heroBattle[i] = heroBattle[j];
                        heroBattle[j] = 0;
                        Hero hero = player.heros.get(heroBattle[i]);
                        hero.onBattle(i);
                    }
                }
            }
            Hero hero = player.heros.get(heroBattle[i]);
            if (Objects.nonNull(hero) && hero.isFaineant()) {
                hero.onBattle(i);
            }
        }
        logSb.append(String.format("roleId :%d 修复后的出战队列 :%s", player.roleId, Arrays.toString(heroBattle))).append("\n");
        logSb.append(String.format("roleId :%d 修复前的防守队列 :%s", player.roleId, Arrays.toString(player.heroDef))).append("\n");
        //将出战队列顺序复制到防守队列顺序
        player.heroDef = Arrays.copyOf(heroBattle, heroBattle.length);
        for (int heroId : heroBattle) {
            if (heroId > 0) {
                Hero hero = player.heros.get(heroId);
                hero.onDef(hero.getPos());
            }
        }
        logSb.append(String.format("roleId :%d 修复后的防守队列 :%s", player.roleId, Arrays.toString(player.heroDef)));
        LogUtil.error(logSb);
    }

    /**
     * 检查英雄出战队列的正确性
     * 1.检测时候出战队列中有空缺的英雄 eg:[0, 1335, 0, 1854, 1294]
     * 2.检测出战队列中英雄status!=HeroConstant.HERO_STATUS_BATTLE
     * 3.检测出战队列或者防守队列中时候有重复英雄
     * 4.检测出战队列中的英雄是否与防守队列中一样
     *
     * @param player
     * @return true:玩家英雄状态无异常
     */
    public boolean checkHeroQueueStatus(Player player) {
        try {
            boolean isOk = true;
            int[] heroBattle = player.heroBattle;
            for (int i = 1; i < heroBattle.length; i++) {
                int heroId = heroBattle[i];
                if (heroId != 0) {
                    Hero hero = player.heros.get(heroId);
                    if (Objects.isNull(hero)) {//英雄不存在
                        LogUtil.error(String.format("roleId :%d, heroId :%d, idx :%d, not found!!! %s", player.getLordId(), heroId, i, Arrays.toString(heroBattle)));
                        break;
                    } else {
                        if (hero.getStatus() != HeroConstant.HERO_STATUS_BATTLE) {//英雄状态不正确
                            LogUtil.error(String.format("roleId :%d, heroId :%d, idx :%d, status error !!! %s", player.getLordId(), heroId, i, Arrays.toString(heroBattle)));
                            isOk = false;
                        } else {
                            for (int j = i + 1; j < heroBattle.length; j++) {
                                if (heroBattle[j] == heroId) {//出战队列中有重复英雄
                                    LogUtil.error(String.format("roleId :%d, heroId :%d, idx :%d, has repeat !!! %s", player.getLordId(), heroId, i, Arrays.toString(heroBattle)));
                                    isOk = false;
                                }
                            }
                        }
                    }
                } else {
                    for (int j = i + 1; j < heroBattle.length; j++) {
                        if (heroBattle[j] != 0) {//出战队列中间缺失了英雄 eg:[0, 1335, 0, 1854, 1294]
                            LogUtil.error(String.format("roleId :%d, heroId :%d, idx :%d, empty pos !!! %s", player.getLordId(), heroId, i, Arrays.toString(heroBattle)));
                            isOk = false;
                        }
                    }
                }
            }

            //检查出战队列英雄列表是否与防守英雄列表相同
            String battleString = Arrays.stream(player.heroBattle).sorted().mapToObj(String::valueOf).collect(Collectors.joining(",", "{", "}"));
            String defString = Arrays.stream(player.heroDef).sorted().mapToObj(String::valueOf).collect(Collectors.joining(",", "{", "}"));
            if (!battleString.equals(defString)) {
                LogUtil.error(String.format("roleId :%d, 出战英雄列表 :%s, 防守英雄列表 :%s, 2个队列英雄ID不一致", player.roleId, battleString, defString));
                isOk = false;
            }
            if (!isOk) LogUtil.sentry(String.format("roleId :%d 英雄状态异常, 请复查 error log !!!", player.getLordId()));
            return isOk;
        } catch (Exception e) {
            LogUtil.error("roleId : " + player.getLordId(), e);
        }
        return false;
    }

    public boolean checkHeroStatus(Player player, Hero hero) {
        if (hero.getStatus() == HeroConstant.HERO_STATUS_BATTLE) {//出战
            int pos = hero.getPos();
            if (pos > 0 && pos <= 4) {
                if (player.heroBattle[pos] != hero.getHeroId()) {
                    LogUtil.error(String.format("Hero Battle Status Error!!! roleId :%d, heroId :%d, pos :%d, ---> %s", player.getLordId(), hero.getHeroId(), pos,
                            Arrays.toString(player.heroBattle)));
                    return false;
                }
            } else {
                LogUtil.error(String.format("Hero Battle Pos Error!!! roleId :%d, heroId :%d, status battle pos :%d ",
                        player.getLordId(), hero.getHeroId(), pos));
                return false;
            }
        } else if (hero.getStatus() == HeroConstant.HERO_STATUS_COLLECT) {//采集
            int pos = hero.getAcqPos();
            if (pos > 0 && pos <= 4) {
                if (player.heroAcq[pos] != hero.getHeroId()) {
                    LogUtil.error(String.format("Hero Acq Status Error!!! roleId :%d, heroId :%d, pos :%d, ---> %s", player.getLordId(), hero.getHeroId(), pos,
                            Arrays.toString(player.heroAcq)));
                    return false;
                }
            } else {
                LogUtil.error(String.format("Hero Acq Pos Error!!! roleId :%d, heroId :%d, status battle pos :%d ",
                        player.getLordId(), hero.getHeroId(), pos));
                return false;
            }
        } else if (hero.getStatus() == HeroConstant.HERO_STATUS_WALL_BATTLE) {//禁卫军
            int pos = hero.getWallPos();
            if (pos > 0 && pos <= 4) {
                if (player.heroWall[pos] != hero.getHeroId()) {
                    LogUtil.error(String.format("Hero Wall Status Error!!! roleId :%d, heroId :%d, pos :%d, ---> %s", player.getLordId(), hero.getHeroId(), pos,
                            Arrays.toString(player.heroWall)));
                    return false;
                }
            } else {
                LogUtil.error(String.format("Hero Wall Pos Error!!! roleId :%d, heroId :%d, status battle pos :%d ",
                        player.getLordId(), hero.getHeroId(), pos));
                return false;
            }
        }
        return true;
    }

    /**
     * 选择心愿武将
     *
     * @param roleId
     * @param searchId
     * @return
     * @throws MwException
     */
    public ChooseWishHeroRs chooseWishHero(long roleId, int searchId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_WISH_HERO)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "心愿武将功能未开启, roleId:", player.roleId, ", lv:", player.lord.getLevel());
        }

        Map<Integer, StaticHeroSearch> heroSearchMap = StaticHeroDataMgr.getHeroSearchMap();
        StaticHeroSearch sHeroSearch = heroSearchMap.get(searchId);

        if (Objects.isNull(sHeroSearch)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "选择心愿将领 , 未找到配置 , roleId:" + roleId + " searchId:" + searchId);
        }

        if (sHeroSearch.getRewardType() != HeroConstant.SEARCH_RESULT_HERO) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "选择心愿将领 , 配置奖励不是将领 , roleId:" + roleId + " searchId:" + searchId);
        }

        int wishHeroRecord = player.getMixtureDataById(PlayerConstant.WISH_HERO);
        if (wishHeroRecord == 0) {
            // 首次选择心愿将领, 初始化必得次数
            player.setMixtureData(PlayerConstant.WISH_HERO_SEARCH_COUNT, HeroConstant.WISH_HERO_COUNT);
        } else if (player.getMixtureDataById(PlayerConstant.WISH_HERO_SEARCH_COUNT) == 0) {
            throw new MwException(GameError.WISH_HERO_FUNCTION_OVER.getCode(), "选择心愿将领 , 心愿将领功能已结束 , roleId:" + roleId + " searchId:" + searchId);
        }

        player.setMixtureData(PlayerConstant.WISH_HERO, searchId);
        return ChooseWishHeroRs.newBuilder().setWishHero(PbHelper.createTwoIntPb(player.getMixtureDataById(PlayerConstant.WISH_HERO), player.getMixtureDataById(PlayerConstant.WISH_HERO_SEARCH_COUNT))).build();
    }

    /**
     * 领取抽取武将累计奖励
     *
     * @param roleId
     * @param index
     * @return
     * @throws MwException
     */
    public ReceiveRecruitRewardRs receiveRecruitReward(long roleId, int index) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_WISH_HERO)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "心愿武将功能未开启, roleId:", player.roleId, ", lv:", player.lord.getLevel());
        }

        StaticHeroSearchExtAward sHeroExtAward = StaticHeroDataMgr.getSearchHeroExtAwardById(index);

        if (Objects.isNull(sHeroExtAward)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "领取招募奖励 ,读取配置错误 , roleId:" + roleId + " index:" + index);
        }

        Map<Integer, Integer> recruitReward = player.getRecruitReward();
        if (recruitReward.containsKey(index)) {
            throw new MwException(GameError.SEARCH_HERO_EXT_AWARD_ALREADY_RECEIVE.getCode(), "领取招募奖励 ,奖励已领取 , roleId:" + roleId + " index:" + index);
        }

        ReceiveRecruitRewardRs.Builder builder = ReceiveRecruitRewardRs.newBuilder();

        if (sHeroExtAward.getSearchType() == HeroConstant.SEARCH_TYPE_NORMAL) {
            if (player.getMixtureDataById(PlayerConstant.NORMAL_HERO_SEARCH_COUNT) < sHeroExtAward.getSearchCount()) {
                throw new MwException(GameError.SEARCH_HERO_EXT_AWARD_CONDITION_NOT_MET.getCode(), "领取招募奖励 , 条件不满足 , roleId:" + roleId + ", index:" + index + ", search_count: " + player.getMixtureDataById(PlayerConstant.NORMAL_HERO_SEARCH_COUNT));
            }
        }

        builder.addAllAward(rewardDataManager.addAwardDelaySync(player, sHeroExtAward.getSearchAward(), null, AwardFrom.SEARCH_HERO_EXT_AWARD, index));
        recruitReward.put(index, 1);

        if (CheckNull.nonEmpty(player.getRecruitReward())) {
            player.getRecruitReward().forEach((key, value) -> builder.addRecruitReward(PbHelper.createTwoIntPb(key, value)));
        }
        return builder.build();

    }

    /**
     * 检测当前玩家是否拥有此英雄
     *
     * @param player
     * @param heroId
     * @param staticHero
     * @return
     */
    public Hero hasOwnedHero(Player player, int heroId, StaticHero staticHero) {
        if (CheckNull.isNull(staticHero) || CheckNull.isNull(player))
            return null;

        // 查找玩家是否拥有此英雄
        return player.heros.get(heroId);
//        if (CheckNull.isNull(hero_)) {
//            hero_ = player.heros.values().stream().filter(v -> {
//                StaticHero staticData = StaticHeroDataMgr.getHeroMap().get(v.getHeroId());
//                if (CheckNull.isNull(staticData))
//                    return false;
//                return staticData.getHeroType() == staticHero.getHeroType();
//            }).findFirst().orElse(null);
//        }
    }

    /**
     * 指定品质英雄到指定等级
     * @param player
     * @param quality
     * @return
     */
    public int getTaskSchedule1(Player player, int quality) {
        int maxLevel = 0;
        for (Hero value : player.heros.values()) {
            if (value.getQuality() >= quality && value.getLevel() > maxLevel) {
                maxLevel = value.getLevel();
            }
        }
        return maxLevel;
    }

    @GmCmd("hero")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        switch (params[0]) {
            case "manji2jue":
                int heroId = Integer.parseInt(params[1]);
                Hero hero = player.heros.get(heroId);
                if (Objects.nonNull(hero)) {
                    //满级
                    addHeroExp(hero, Integer.MAX_VALUE, player.lord.getLevel(), player);
                    //觉醒
                    hero.setDecorated(hero.getDecorated() + 2);
                    battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_HERO_DECORATED_HAVE_CNT, 1, hero.getDecorated());
                    worldScheduleService.updateScheduleGoal(player, ScheduleConstant.GOAL_COND_HERO_DECORATED, hero.getDecorated());
                    CalculateUtil.processAttr(player, hero);
                    LogLordHelper.commonLog("heroDecorated", AwardFrom.DO_SOME, player, hero.getHeroId());
                    chatDataManager.sendSysChat(ChatConst.CHAT_HERO_DECORATED, 0, 0, player.lord.getCamp(), player.lord.getNick(),
                            hero.getDecorated(), heroId);
                }
                break;
            default:
        }
    }
}
