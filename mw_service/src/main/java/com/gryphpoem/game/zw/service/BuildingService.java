package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.BuildingBase;
import com.gryphpoem.game.zw.pb.CommonPb.OffLineBuild;
import com.gryphpoem.game.zw.pb.GamePb1.*;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.OnOffAutoBuildRs;
import com.gryphpoem.game.zw.pb.GamePb4.UptBuildingRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.Constant.TypeInfo;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.season.SeasonTalent;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.ActivityRobinHoodService;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * 建筑
 *
 * @author tyler
 */
@Service
public class BuildingService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private TechDataManager techDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private CiaService ciaService;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private ActivityTriggerService activityTriggerService;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private ActivityRobinHoodService activityRobinHoodService;
    @Autowired
    private SeasonTalentService seasonTalentService;

    /**
     * 玩家的0级建筑修改成1级
     */
    public void gmBuildZeroToOne(Player player) {
        LogUtil.debug("------------玩家的0级建筑修改成1级 start-----------", player.roleId);
        Building building = player.building;
        if (building != null) {
            Class<?> buildClazz = building.getClass();
            Field[] fieds = buildClazz.getDeclaredFields();
            for (Field fied : fieds) {
                try {
                    Class<?> ft = fied.getType();
                    if (ft == int.class || ft == Integer.class) {
                        fied.setAccessible(true);
                        int value = fied.getInt(building);
                        if (value == 0) {
                            fied.setInt(building, 1);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        player.mills.forEach((k, mill) -> {
            if (mill.getLv() == 0) {
                mill.setLv(1);
            }
        });
        LogUtil.debug("------------玩家的0级建筑修改成1级 end-----------", player.roleId);
    }

    /**
     * 客户端请求玩家建筑数据
     *
     * @param roleId
     * @return
     */
    public GetBuildingRs getBuilding(long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        if (player != null) {
            Building building = player.building;
            if (building == null) {
                buildingDataManager.createBuilding(player);
                building = player.building;
            }
            // 先更新一下所有建筑的解锁状态
            buildingDataManager.updateBuildingLockState(player);
            buildingDataManager.refreshSourceData(player);

            GetBuildingRs.Builder builder = GetBuildingRs.newBuilder();
            BuildingBase.Builder buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.COMMAND).setType(BuildingType.COMMAND).setLv(building.getCommand())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.COMMAND));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.TECH).setType(BuildingType.TECH).setLv(building.getTech())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.TECH));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.WAR_FACTORY).setType(BuildingType.WAR_FACTORY).setLv(building.getWar())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.WAR_FACTORY));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.STOREHOUSE).setType(BuildingType.STOREHOUSE).setLv(building.getWare())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.STOREHOUSE));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.WAR_COLLEGE).setType(BuildingType.WAR_COLLEGE).setLv(building.getCollege())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.WAR_COLLEGE));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.REMAKE).setType(BuildingType.REMAKE).setLv(building.getRefit())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.REMAKE));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.ORDNANCE_FACTORY).setType(BuildingType.ORDNANCE_FACTORY)
                    .setLv(building.getMunition())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.ORDNANCE_FACTORY));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.CHEMICAL_PLANT).setType(BuildingType.CHEMICAL_PLANT)
                    .setLv(building.getChemical())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.CHEMICAL_PLANT));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.WALL).setType(BuildingType.WALL).setLv(building.getWall())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.WALL));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.TRADE_CENTRE).setType(BuildingType.TRADE_CENTRE).setLv(building.getTrade())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.TRADE_CENTRE));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.CLUB).setType(BuildingType.CLUB).setLv(building.getClub())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.CLUB));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.CIA).setType(BuildingType.CIA).setLv(1)
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.CIA));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.NUCLEAR_BOMB).setType(BuildingType.NUCLEAR_BOMB).setLv(1)
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.NUCLEAR_BOMB));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.FACTORY_1).setType(BuildingType.FACTORY_1).setLv(building.getFactory1())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.FACTORY_1));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.FACTORY_2).setType(BuildingType.FACTORY_2).setLv(building.getFactory2())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.FACTORY_2));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.FACTORY_3).setType(BuildingType.FACTORY_3).setLv(building.getFactory3())
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.FACTORY_3));
            builder.addBuildingBase(buildingBase);

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.SEASON_TREASURY).setType(BuildingType.SEASON_TREASURY).setLv(1)
                    .setUnlock(buildingDataManager.checkBuildingLock(player,BuildingType.SEASON_TREASURY));
            builder.addBuildingBase(buildingBase);

            //码头
            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.WHARF).setType(BuildingType.WHARF).setLv(1)
                    .setUnlock(buildingDataManager.checkBuildingLock(player,BuildingType.WHARF));
            builder.addBuildingBase(buildingBase);

            BuildingExt train = player.buildingExts.get(BuildingType.TRAIN_FACTORY_1);
            if (!CheckNull.isNull(train)) {
                buildingBase = BuildingBase.newBuilder();
                buildingBase.setId(train.getId()).setType(train.getType()).setLv(building.getTrain())
                        .setUnlock(buildingDataManager.checkBuildingLock(player, train.getId()));
                builder.addBuildingBase(buildingBase);
            }

            BuildingExt train2 = player.buildingExts.get(BuildingType.TRAIN_FACTORY_2);
            if (building.getTrain2() <= 0) {
                StaticBuildingInit init = StaticBuildingDataMgr.getBuildingInitMap()
                        .get(BuildingType.TRAIN_FACTORY_2);
                if (init != null) {
                    building.setTrain2(init.getInitLv());
                }
            }
            if (!CheckNull.isNull(train2)) {
                buildingBase = BuildingBase.newBuilder();
                buildingBase.setId(train2.getId()).setType(train2.getType()).setLv(building.getTrain2())
                        .setUnlock(buildingDataManager.checkBuildingLock(player, train2.getId()));
                builder.addBuildingBase(buildingBase);
            }

            buildingBase = BuildingBase.newBuilder();
            buildingBase.setId(BuildingType.AIR_BASE).setType(BuildingType.AIR_BASE).setLv(1)
                    .setUnlock(buildingDataManager.checkBuildingLock(player, BuildingType.AIR_BASE));
            builder.addBuildingBase(buildingBase);

            Iterator<BuildQue> it1 = player.buildQue.values().iterator();
            while (it1.hasNext()) {
                builder.addQueue(PbHelper.createBuildQuePb(it1.next()));
            }

            Iterator<Mill> it2 = player.mills.values().iterator();
            while (it2.hasNext()) {
                builder.addMill(PbHelper.createMillPb(it2.next()));
            }

            builder.setResCnt(player.common.getResCnt());
            builder.setResTime(player.common.getResTime());

            // Iterator<Map<Integer, Mill>> it = player.mills.values().iterator();
            // while (it.hasNext()) {
            // Iterator<Mill> it2 = it.next().values().iterator();
            // while (it2.hasNext()) {
            // builder.addMill(createMillPb(it2.next()));
            // }
            // }
            return builder.build();
        }
        return null;
    }

    /**
     * 客户端请求建筑升级
     *
     * @param roleId     建筑类型
     * @param buildingId 建筑位置
     * @param immediate  是否立即完成,true表示立即完成
     * @return 升级后的建筑信息
     */
    public UpBuildingRs upgradeBuilding(long roleId, int buildingId, boolean immediate) throws MwException {
        StaticBuildingInit sBuildingInit = StaticBuildingDataMgr.getBuildingInitMapById(buildingId);
        if (sBuildingInit == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "升级建筑配置未找到, roleId:" + roleId + ",buildingId:" + buildingId);
        }
        if (sBuildingInit.getCanUp() != BuildingType.BUILD_CAN_UP_STATUS) {
            throw new MwException(GameError.INVALID_PARAM.getCode(),
                    "升级建筑时，建筑不允许升级, roleId:" + roleId + ",buildingId:" + buildingId);
        }

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        //获取建筑类型  从玩家建筑信息中获取 
        int buildingType = getBuildingType(player, buildingId);
        if (buildingType == 0) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(),
                    "升级建筑时，建筑未建造, roleId:" + roleId + ",buildingId:" + buildingId);
        }

        if (BuildingType.TRAIN_FACTORY_1 == buildingId || BuildingType.TRAIN_FACTORY_2 == buildingId) {
            BuildingExt buildingExt = player.buildingExts.get(buildingId);
            if (!CheckNull.isNull(buildingExt)) {
                buildingType = buildingExt.getType();
            }
            if (buildingType == 0) {
                throw new MwException(GameError.FUNCTION_LOCK.getCode(),
                        "升级建筑时，建筑未建造, roleId:" + roleId + ",buildingId:" + buildingId);
            }
        }
        Lord lord = player.lord;
        Map<Integer, BuildQue> buildQue = player.buildQue;
        int queCnt = getBuildQueCount(player);
        // 不是立即升级建筑的时候
        if (!immediate && buildQueIsFull(player)) {
            throw new MwException(GameError.MAX_BUILD_QUE.getCode(),
                    "升级建筑时，队列满了, roleId:" + roleId + ",buildingPos:" + buildingId);
        }

        // if (type == BuildingType.RES_ORE && type == BuildingType.RES_FOOD && type == BuildingType.RES_ELE
        // && type == BuildingType.RES_OIL) {
        // // 非资源建筑
        // id = type;
        // }

        for (BuildQue build : buildQue.values()) {
            if (build.getPos() == buildingId) {
                throw new MwException(GameError.ALREADY_BUILD.getCode(),
                        "升级建筑时，建筑升级中, roleId:" + roleId + ",buildingPos:" + buildingId);
            }
        }

        // 解锁判断
        if (!buildingDataManager.checkBuildingLock(player, buildingId)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(),
                    "升级建筑时，建筑未解锁, roleId:" + roleId + ",buildingPos:" + buildingId);
        }

        int buildLevel = BuildingDataManager.getBuildingLv(buildingId, player);
        if (buildLevel >= sBuildingInit.getMaxLv()) {
            throw new MwException(GameError.MAX_LV.getCode(),
                    "升级建筑时，已满级 roleId:" + roleId + ",buildingId:" + buildingId);
        }

        StaticBuildingLv staticBuildingLevel = StaticBuildingDataMgr
                .getStaticBuildingLevel(buildingType, buildLevel + 1);
        if (staticBuildingLevel == null) {
            throw new MwException(GameError.INVALID_PARAM.getCode(),
                    "升级建筑时，找不到升级配置,或已满级 roleId:" + roleId + ",buildingType:" + buildingType);
        }

        // 司令部特出要求，司令部开启等级上限
        // if (buildingType == BuildingType.COMMAND && buildLevel >= Constant.MAX_COMMAND_LV) {
        // throw new MwException(GameError.MAX_LV.getCode(), "升级建筑时，已满级 roleId:" + roleId + ",buildingType:" +
        // buildingType);
        // }

        if (lord.getLevel() < staticBuildingLevel.getRoleLv()) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(),
                    "升级建筑时，等级不足, roleId:" + roleId + ",buildingType:" + buildingType);
        }

        if (buildingDataManager.checkBuildingLv(player, staticBuildingLevel.getUpNeedBuilding())) {
            throw new MwException(GameError.COMMAND_LV_NOT_ENOUGH.getCode(),
                    "升级建筑时，建筑等级不足, roleId:" + roleId + ",buildingType:" + buildingType);
        }

        // ===========================正在生产或造兵的建筑不能升级===========================

        // 兵营在造兵时不能升级
        if (buildingId == BuildingType.FACTORY_1 || buildingId == BuildingType.FACTORY_2
                || buildingId == BuildingType.FACTORY_3 || buildingId == BuildingType.TRAIN_FACTORY_1
                || buildingId == BuildingType.TRAIN_FACTORY_2) { // 训练中心
            Factory factory = player.factory.get(buildingId);
            if (factory != null && !CheckNull.isEmpty(factory.getAddList())) {
                throw new MwException(GameError.BUILD_IS_WORKING.getCode(),
                        "建筑正在生产,不能升级  roleId:" + roleId + ", buildingId:" + buildingId);
            }
        }
        // 检测化工厂是否正在生产
        if (buildingId == BuildingType.CHEMICAL_PLANT) {
            if (player.chemical != null && !CheckNull.isEmpty(player.chemical.getPosQue())) {
                throw new MwException(GameError.BUILD_IS_WORKING.getCode(),
                        "建筑正在生产,不能升级  roleId:" + roleId + ", buildingId:" + buildingId);
            }
        }

        // 检测科研所是否在研究
        if (buildingId == BuildingType.TECH) {
            if (player.tech != null && player.tech.getQue() != null && player.tech.getQue().getId() > 0) {// 正在升级
                if (player.shop == null || !player.shop.getVipId().contains(Constant.TECH_QUICK_VIP_BAG)) {
                    throw new MwException(GameError.BUILD_NOT_TECH_QUICK_VIP_BAG.getCode(), "roleId:", roleId, " 没有购买科技快研礼包");
                }
                if (!techDataManager.isAdvanceTechGain(player)) {
                    throw new MwException(GameError.NOT_ADVANCE_TECH_GAIN.getCode(), "roleId:", roleId, " 没有雇佣高级研究院");
                }
                /*// 检测购买过vip礼包5
                if (player.shop == null) {// shop为null
                    throw new MwException(GameError.BUILD_NOT_TECH_QUICK_VIP_BAG.getCode(),
                            "建筑正在生产,不能升级  roleId:" + roleId + ", buildingId:" + buildingId);
                } else {
                    // 购买vip5礼包,同时雇佣了高级研究院
                    if (!(player.shop.getVipId().contains(Constant.TECH_QUICK_VIP_BAG) && techDataManager
                            .isAdvanceTechGain(player))) {
                        throw new MwException(GameError.BUILD_NOT_TECH_QUICK_VIP_BAG.getCode(),
                                "建筑正在生产,不能升级  roleId:" + roleId + ", buildingId:" + buildingId);
                    }
                }*/
            }
        }
        // ===========================正在生产或造兵的建筑不能升级===========================

        // int coinCost = staticBuildingLevel.getCoinCost();
        // if (coinCost > 0) {
        // if (lord.getGold() < coinCost) {
        // throw new MwException(GameError.GOLD_NOT_ENOUGH.getCode(),
        // "升级建筑时，等级不足, roleId:" + roleId + ",buildingType:" + buildingType);
        // }
        // rewardDataManager.subGold(player, coinCost, AwardFrom.UP_BUILD);
        // }

        float factor = 0;
        // 效果影响资源消耗
        // Effect effect = player.effects.get(EffectType.ACTIVITY_BUILD_SUB_COST);
        // if (effect != null) {
        // factor += (-50 / 100.0f);
        // }
        int haust = calcBuildTime(player, staticBuildingLevel.getUpTime()); // 耗时

        if (immediate) { // 立即完成
            int freeTime = vipDataManager.getNum(player.lord.getVip(), VipConstant.FREE_BUILD_TIME, player);
            int second = haust - freeTime; //
            int needGold = (int) Math.ceil(second * 1.00 / 60);
            List<List<Integer>> costList = new ArrayList<>(staticBuildingLevel.getUpNeedResource());
            if (needGold > 0) {
                List<Integer> goldCost = new ArrayList<>(); // 金币的消耗
                goldCost.add(AwardType.MONEY);
                goldCost.add(AwardType.Money.GOLD);
                goldCost.add(needGold * 2);
                costList.add(goldCost);
            }
            rewardDataManager.checkAndSubPlayerRes(player, costList, AwardFrom.BUILDING_SPEED, buildingId);

        } else {
            if (!rewardDataManager.checkPlayerResourceIsEnough(player, staticBuildingLevel.getUpNeedResource())) {
                // 升级建筑资源不足时触发
                activityTriggerService.buildLevelUpNoResourceTriggerGift(player, buildingType, buildLevel);
            }
            // 升级建筑时
            rewardDataManager.checkPlayerResIsEnough(player, staticBuildingLevel.getUpNeedResource());
            rewardDataManager
                    .modifyResource(player, staticBuildingLevel.getUpNeedResource(), factor, AwardFrom.UP_BUILD);
            // 建筑升级触发礼包(点击免费加速后触发，故这里注释掉 2019-12-11shi.pei)
            //            activityTriggerService.buildLevelUpTriggerGift(player, buildingType, buildLevel);
        }

        if (BuildingDataManager.isResType(buildingType)) {
            Mill mill = player.mills.get(buildingId);
            if (mill == null) {
                mill = new Mill(buildingId, buildingType, sBuildingInit.getInitLv(), 0);
                // mill.setUnlock(buildingDataManager.checkBuildingLock(player, buildingId));
                player.mills.put(buildingId, mill);
            }
            // Map<Integer, Mill> millMap = player.mills.get(pos);
            // if (millMap == null) {
            // millMap = new HashMap<>();
            // millMap.put(type, new Mill(pos, type, 0));
            // }
            // player.mills.put(pos, millMap);
        }
        // 判断建造还是升级
        if (BuildingDataManager.getBuildingLv(buildingId, player) < 1) {
            // 0->1是建造
            LogUtil.debug("建造了建筑  buildingId:", buildingId, ", roleId:", roleId);
            // 新手指引的记录
            updateBuidingGuide(player, buildingId, true);
        } else {
            // 1->+是升级
        }

        UpBuildingRs.Builder builder = UpBuildingRs.newBuilder();
        Resource resource = player.resource;
        builder.setFood(resource.getFood());
        builder.setOil(resource.getOil());
        builder.setOre(resource.getOre());
        builder.setEle(resource.getElec());
        int now = TimeHelper.getCurrentSecond();

/*        for (int i = 1; i <= queCnt; i++) {
            if (!buildQue.containsKey(i)) {
                BuildQue que = createQue(player, i, buildingType, buildingId, haust, now + haust);
                buildQue.put(i, que);
                if (immediate) { // 立即完成
                    que.setEndTime(now);
                    que.clearFree(); // 清除免费加速
                    taskDataManager.updTask(player, TaskType.COND_FREE_CD, 1, que.getBuildingType()); // 任务进度
                }
                builder.setQueue(PbHelper.createBuildQuePb(que));
                break;
            }
        }*/
        builder.setGold(lord.getGold());
        builder.setType(buildingType);
        builder.setId(buildingId);
        builder.setLv(buildLevel);

        /*if (!immediate) { // 立即完成不触发礼包
            // 建筑升级满足触发条件
            int giftId = activityDataManager.checkBuildTrigger(player, buildingType, buildLevel);
            if (giftId != 0) {
                try {
                    activityService.checkTriggerGiftSyncByGiftId(giftId, player);
                } catch (MwException e) {
                    LogUtil.error(e);
                }
            }
        }*/
        taskDataManager.updTask(player, TaskType.COND_18, 1, buildingType);
        taskDataManager.updTask(player, TaskType.COND_BUILDING_UP_43, 1);
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_BUILDING_UP_43, 1);

        // 立即完成不添加进入队列
        if (immediate) {
            // 这里手动设置索引999, 并不存入队列
            BuildQue que = createQue(player, 999, buildingType, buildingId, haust, now + haust);
            // 设置结束时间
            que.setEndTime(now);
            // 清除免费加速
            que.clearFree();
            taskDataManager.updTask(player, TaskType.COND_FREE_CD, 1, que.getBuildingType()); // 任务进度
            dealOneQue(player, que);
        } else {
            for (int i = 1; i <= queCnt; i++) {
                if (!buildQue.containsKey(i)) {
                    BuildQue que = createQue(player, i, buildingType, buildingId, haust, now + haust);
                    buildQue.put(i, que);
                    builder.setQueue(PbHelper.createBuildQuePb(que));
                    break;
                }
            }
        }
        return builder.build();
    }

    /**
     * @param roleId
     * @param buildingId
     * @param newType
     * @param keyId
     * @param immediate
     * @return
     * @throws MwException 参数
     *                     UptBuildingRs    返回类型
     * @throws
     * @Title: uptBuilding
     * @Description: 建筑改建
     */
    public UptBuildingRs uptBuilding(long roleId, int buildingId, int newType, int keyId, boolean immediate)
            throws MwException {
        //获取改建配置
        StaticUptBuild staticUptBuild = StaticBuildingDataMgr.getUptBuildConfig(keyId);
        //获取建筑配置
        StaticBuildingInit sBuildingInit = StaticBuildingDataMgr.getBuildingInitMapById(buildingId);
        //获取玩家信息
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        //判断科技
        if (!techDataManager.isOpen(player, TechConstant.TYPE_27)) {
            throw new MwException(GameError.SCIENCE_LOCKED.getCode(), "科技未解锁, roleId:" + roleId);
        }

        //获取玩家非资源建筑信息
        BuildingExt buildingExt = player.buildingExts.get(buildingId);
        //获取玩家资源建筑信息
        Mill mill = player.mills.get(buildingId);

        //判断配置
        if (staticUptBuild == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "建筑改建配置未找到, roleId:" + roleId + ",keyId:" + keyId);
        }
        if (sBuildingInit == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "建筑配置未找到, roleId:" + roleId + ",buildingId:" + buildingId);
        }
        if (buildingExt == null && mill == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "该玩家无该建筑信息, roleId:" + roleId + ",buildingId:" + buildingId);
        }

        //获取建筑类型  从玩家建筑信息中获取
        int buildingType = mill != null ? mill.getType() : buildingExt.getType();

        //判断建筑类型是否支持改建
        if (buildingType != BuildingType.RES_OIL && buildingType != BuildingType.RES_ELE
                && buildingType != BuildingType.RES_FOOD && buildingType != BuildingType.RES_ORE
                && buildingType != BuildingType.TRAIN_FACTORY_1 && buildingType != BuildingType.TRAIN_FACTORY_2
                && buildingType != BuildingType.TRAIN_FACTORY_3) {
            throw new MwException(GameError.PARAM_ERROR.getCode(),
                    "建筑类型错误, roleId:" + roleId + ",buildingType:" + buildingType);
        }

        //判断改建后的类型
        if (newType == buildingType) {
            throw new MwException(GameError.INVALID_PARAM.getCode(),
                    "改建前后类型相同, buildingType:" + buildingType + ",newType:" + newType);
        }

        if (sBuildingInit.getCanUp() != BuildingType.BUILD_CAN_UP_STATUS) {
            throw new MwException(GameError.INVALID_PARAM.getCode(),
                    "升级或改建建筑时，建筑不允许改建, roleId:" + roleId + ",buildingId:" + buildingId);
        }

        Lord lord = player.lord;
        Map<Integer, BuildQue> buildQue = player.buildQue;
        int queCnt = getBuildQueCount(player);
        if (buildQueIsFull(player)) {
            throw new MwException(GameError.MAX_BUILD_QUE.getCode(),
                    "建筑改建时，队列满了, roleId:" + roleId + ",buildingPos:" + buildingId);
        }

        for (BuildQue build : buildQue.values()) {
            if (build.getPos() == buildingId) {
                throw new MwException(GameError.ALREADY_BUILD.getCode(),
                        "改建建筑时，建筑升级或改建中, roleId:" + roleId + ",buildingPos:" + buildingId);
            }
        }

        // 解锁判断
        if (!buildingDataManager.checkBuildingLock(player, buildingId)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(),
                    "改建建筑时，建筑未解锁, roleId:" + roleId + ",buildingPos:" + buildingId);
        }

        //获取建筑等级
        int buildLevel = BuildingDataManager.getBuildingLv(buildingId, player);
        int haust = staticUptBuild.getUptTime(); // 耗时

        //判断改建后的资源是否解锁
        StaticBuildingLv staticBuildingLevelNew = StaticBuildingDataMgr.getStaticBuildingLevel(newType, buildLevel);
        if (player.building.getCommand() < staticBuildingLevelNew.getUpNeedBuilding().get(2)) {
            throw new MwException(GameError.BUILD_IS_UPPING.getCode(),
                    "改建目标资源未解锁, need:" + staticBuildingLevelNew.getUpNeedBuilding().get(2) + ",command:"
                            + player.building.getCommand());
        }

        //判断是-------资源建筑 改建 还是 超级兵营改建---------
        if (buildingType == BuildingType.RES_OIL || buildingType == BuildingType.RES_ELE
                || buildingType == BuildingType.RES_FOOD || buildingType == BuildingType.RES_ORE) {//资源建筑改建

            if (staticUptBuild.getBuildingType() != 1 || staticUptBuild.getLevel() != buildLevel) {//不是资源田的改建配置 或 等级对不上
                throw new MwException(GameError.PARAM_ERROR.getCode(),
                        "改建配置id错误, buildingType:" + buildingType + ",keyId:" + keyId);
            }
            //判断改建类型
            if (newType != BuildingType.RES_OIL && newType != BuildingType.RES_ELE && newType != BuildingType.RES_FOOD
                    && newType != BuildingType.RES_ORE) {
                throw new MwException(GameError.PARAM_ERROR.getCode(),
                        "改建类型错误, buildingType:" + buildingType + ",newType:" + newType);
            }

        } else {//非资源建筑改建
            //判断改建类型
            if (newType != BuildingType.TRAIN_FACTORY_1 && newType != BuildingType.TRAIN_FACTORY_2
                    && newType != BuildingType.TRAIN_FACTORY_3) {
                throw new MwException(GameError.PARAM_ERROR.getCode(),
                        "改建类型错误, buildingType:" + buildingType + ",newType:" + newType);
            }
            //判断是否是超级工厂的改建配置  或 等级对不上
            if (staticUptBuild.getBuildingType() != 2 || staticUptBuild.getLevel() != buildLevel) {
                throw new MwException(GameError.PARAM_ERROR.getCode(),
                        "改建配置id错误, buildingType:" + buildingType + ",keyId:" + keyId);
            }

            if (BuildingType.TRAIN_FACTORY_1 == buildingId || BuildingType.TRAIN_FACTORY_2 == buildingId
                    || BuildingType.TRAIN_FACTORY_3 == buildingId) {
                buildingExt = player.buildingExts.get(buildingId);
                if (!CheckNull.isNull(buildingExt)) {
                    buildingType = buildingExt.getType();
                }
                if (buildingType == 0) {
                    throw new MwException(GameError.FUNCTION_LOCK.getCode(),
                            "升级建筑时，建筑未建造, roleId:" + roleId + ",buildingId:" + buildingId);
                }
                int otherBuildId = buildingId == BuildingType.TRAIN_FACTORY_1 ?
                        BuildingType.TRAIN_FACTORY_2 :
                        BuildingType.TRAIN_FACTORY_1;
                BuildingExt otherBuildExt = player.buildingExts.get(otherBuildId);
                if (otherBuildExt != null && otherBuildExt.getType() != 0 && otherBuildExt.getType() == newType) {
                    throw new MwException(GameError.TRAIN_BUILD_ERROR.getCode(), "不同的超级工厂, 不能建造同类型的兵种, roleId:", roleId,
                            ",buildingId:", buildingId, ",otherBuildType:", newType);
                }
                for (BuildQue que : buildQue.values()) {
                    if (que.getPos() == otherBuildId && que.getNewType() == newType) {
                        throw new MwException(GameError.TRAIN_BUILD_ERROR.getCode(), "不同的超级工厂, 不能建造同类型的兵种, roleId:", roleId,
                                ",buildingId:", buildingId, ",otherBuildType:", newType);
                    }
                }

            }

            // 兵营在造兵时不能升级
            if (buildingId == BuildingType.TRAIN_FACTORY_3 || buildingId == BuildingType.TRAIN_FACTORY_2
                    || buildingId == BuildingType.TRAIN_FACTORY_1) { // 训练中心
                Factory factory = player.factory.get(buildingId);
                if (factory != null && !CheckNull.isEmpty(factory.getAddList())) {
                    throw new MwException(GameError.BUILD_IS_WORKING.getCode(),
                            "建筑正在生产,不能改建  roleId:" + roleId + ", buildingId:" + buildingId);
                }
            }

        }

        float factor = 0;

        if (immediate) { // 立即完成

            //判断是-------资源建筑 改建 还是 超级兵营改建---------
            if (buildingType == BuildingType.RES_OIL || buildingType == BuildingType.RES_ELE
                    || buildingType == BuildingType.RES_FOOD || buildingType == BuildingType.RES_ORE) {//资源建筑改建
                //修改资源田类型
                mill.setType(newType);
                ;
                player.mills.put(buildingId, mill);
            } else {
                //修改超级兵营的类型
                buildingExt.setType(newType);
                player.buildingExts.put(buildingId, buildingExt);
            }

            int freeTime = vipDataManager.getNum(player.lord.getVip(), VipConstant.FREE_BUILD_TIME, player);
            int second = haust - freeTime; //
            int needGold = (int) Math.ceil(second * 1.00 / 60);
            List<List<Integer>> costList = staticUptBuild.getResourceCost().stream().collect(Collectors.toList());
            if (needGold > 0) {
                List<Integer> goldCost = new ArrayList<>(); // 金币的消耗
                goldCost.add(AwardType.MONEY);
                goldCost.add(AwardType.Money.GOLD);
                goldCost.add(needGold * 2);
                costList.add(goldCost);
            }
            rewardDataManager.checkAndSubPlayerRes(player, costList, AwardFrom.BUILDING_SPEED, buildingId);
        } else {
            rewardDataManager.checkPlayerResIsEnough(player, staticUptBuild.getResourceCost());// 升级建筑时
            rewardDataManager.modifyResource(player, staticUptBuild.getResourceCost(), factor, AwardFrom.UP_BUILD);
        }

        if (BuildingDataManager.isResType(buildingType)) {
            mill = player.mills.get(buildingId);
            if (mill == null) {
                mill = new Mill(buildingId, buildingType, sBuildingInit.getInitLv(), 0);
                player.mills.put(buildingId, mill);
            }
        }

        UptBuildingRs.Builder builder = UptBuildingRs.newBuilder();
        Resource resource = player.resource;
        builder.setFood(resource.getFood());
        builder.setOil(resource.getOil());
        builder.setOre(resource.getOre());
        builder.setEle(resource.getElec());
        int now = TimeHelper.getCurrentSecond();

        for (int i = 1; i <= queCnt; i++) {
            if (!buildQue.containsKey(i)) {
                BuildQue que = createQue(player, i, buildingType, buildingId, haust, now + haust);
                que.setFromType(2);
                que.setNewType(newType);
                buildQue.put(i, que);
                if (immediate) { // 立即完成
                    que.setEndTime(now);
                    que.clearFree(); // 清除免费加速
                    //taskDataManager.updTask(player, TaskType.COND_FREE_CD, 0, que.getBuildingType()); // 任务进度
                }
                builder.setQueue(PbHelper.createBuildQuePb(que));
                break;
            }
        }
        builder.setGold(lord.getGold());
        builder.setType(newType);
        builder.setId(buildingId);
        builder.setLv(buildLevel);
        return builder.build();
    }

    /**
     * 获取司令部信息
     *
     * @param roleId
     * @return
     */
    public GetCommandRs getCommandRs(long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        if (player != null) {
            GetCommandRs.Builder builder = GetCommandRs.newBuilder();
            Gains commandMult = player.gains.get(BuildingType.COMMAND);
            if (commandMult != null) {
                if (TimeHelper.getCurrentSecond() > commandMult.getEndTime()) {
                    player.gains.remove(BuildingType.COMMAND);
                } else {
                    builder.setId(commandMult.getId());
                    builder.setEndTime(commandMult.getEndTime());
                }
            }
            // 重新计算资源
            buildingDataManager.refreshSourceData(player);
            builder.addAllRoleOpt(player.opts);
            builder.addAllResAdd(buildingDataManager.listResAdd(player));
            return builder.build();
        }
        return null;
    }

    /**
     * 内政官招募
     *
     * @param roleId
     * @return
     */
    public CommandAddRs getCommandAddRs(long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCommandMult staticCommandMult = StaticBuildingDataMgr.getCommandMult(id);
        if (staticCommandMult == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "内政官招募时，找不到配置, roleId:" + roleId);
        }
        if (staticCommandMult.getLordLv() > player.lord.getLevel()) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "内政官招募时，等级不满足, roleId:" + roleId);
        }
        if (staticCommandMult.getNeedBuildingType() > 0) {
            int lv = BuildingDataManager.getBuildingTopLv(player, staticCommandMult.getNeedBuildingType());
            if (staticCommandMult.getNeedBuildingLv() > lv) {
                throw new MwException(GameError.COMMAND_LV_NOT_ENOUGH.getCode(), "内政官招募时，司令部等级不满足, roleId:" + roleId);
            }
        }
        rewardDataManager.checkAndSubPlayerRes(player, staticCommandMult.getCost(), AwardFrom.COMMAND_ADD);

        CommandAddRs.Builder builder = CommandAddRs.newBuilder();
        Gains commandMult = new Gains(staticCommandMult.getType());
        int now = TimeHelper.getCurrentSecond();

        commandMult.setId(id);
        commandMult.setEndTime((int) (now + staticCommandMult.getAddTime()));
        player.gains.put(staticCommandMult.getType(), commandMult);
        builder.setId(id);
        builder.setEndTime(commandMult.getEndTime());
        builder.addAllResAdd(buildingDataManager.listResAdd(player));

        taskDataManager.updTask(player, TaskType.COND_COMMAND_ADD, 1, staticCommandMult.getLv());
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_RECRUIT, staticCommandMult.getType(),
                staticCommandMult.getQuality());
        return builder.build();
    }

    /**
     * 兵工厂信息
     *
     * @param roleId
     * @return
     */
    public GetEquipFactoryRs getEquipFactoryRs(long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        if (player != null) {
            GetEquipFactoryRs.Builder builder = GetEquipFactoryRs.newBuilder();
            Gains gains = player.gains.get(BuildingType.ORDNANCE_FACTORY);
            if (gains != null) {
                if (TimeHelper.getCurrentSecond() > gains.getEndTime()) {
                    player.gains.remove(BuildingType.ORDNANCE_FACTORY);
                } else {
                    builder.setId(gains.getId());
                    builder.setEndTime(gains.getEndTime());
                }
            }
            for (EquipQue equipQue : player.equipQue) {
                builder.setEquipQue(PbHelper.createEquipQuePb(equipQue));
            }
            List<History> list = player.typeInfo.get(TypeInfo.TYPE_1);
            if (list != null && list.size() > 0) {
                for (History history : list) {
                    builder.addFreeId(history.getId());
                }
            }
            return builder.build();
        }
        return null;
    }

    /**
     * 兵工厂雇佣
     *
     * @param roleId
     * @return
     */
    public EquipFactoryRecruitRs doEquipFactoryRecruit(long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCommandMult staticCommandMult = StaticBuildingDataMgr.getCommandMult(id);
        if (staticCommandMult == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "兵工厂雇佣时，找不到配置, roleId:" + roleId);
        }
        if (staticCommandMult.getLordLv() > player.lord.getLevel()) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "兵工厂雇佣时，等级不满足, roleId:" + roleId);
        }
        if (staticCommandMult.getNeedBuildingType() > 0) {
            int lv = BuildingDataManager.getBuildingTopLv(player, staticCommandMult.getNeedBuildingType());
            if (staticCommandMult.getNeedBuildingLv() > lv) {
                throw new MwException(GameError.COMMAND_LV_NOT_ENOUGH.getCode(), "兵工厂雇佣时，司令部等级不满足, roleId:" + roleId);
            }
        }

        EquipFactoryRecruitRs.Builder builder = EquipFactoryRecruitRs.newBuilder();
        Gains oldGains = player.gains.get(staticCommandMult.getType());
        Gains commandMult = new Gains(staticCommandMult.getType());
        int now = TimeHelper.getCurrentSecond();

        commandMult.setId(id);
        commandMult.setEndTime(now + staticCommandMult.getAddTime());
        if (staticCommandMult.isFirstFree()) {
            // 记录首次免费雇佣
            List<History> list = player.typeInfo.get(TypeInfo.TYPE_1);
            if (list == null) {
                list = new ArrayList<>();
                player.typeInfo.put(TypeInfo.TYPE_1, list);
            }
            // 查询是否已经使用免费
            boolean free = true;
            for (History history : list) {
                if (history.getId() == id) {
                    free = false;
                    rewardDataManager
                            .checkAndSubPlayerRes(player, staticCommandMult.getCost(), AwardFrom.FACTORY_RECRUIT, id);
                }
            }
            if (free) {
                list.add(new History(id, 1));
                commandMult.setEndTime((int) (now + staticCommandMult.getFreeTime()));
            }
        } else {
            rewardDataManager.checkAndSubPlayerRes(player, staticCommandMult.getCost(), AwardFrom.FACTORY_RECRUIT);
        }

        if (oldGains != null && now < oldGains.getEndTime()) {
            commandMult.setEndTime(commandMult.getEndTime() + oldGains.getEndTime() - now);
        }

        if (!player.equipQue.isEmpty()) {
            EquipQue que = player.equipQue.get(0);
            LogUtil.debug("装备打造队列que=" + que);
            if (que != null) {
                // 已经加速过，算差值
                if (que.getFreeCnt() > 0) {
                    if (staticCommandMult.getSpeedTime(player) - que.getFreeTime() > 0) {
                        que.setFreeTime(staticCommandMult.getSpeedTime(player) - que.getFreeTime());
                    }
                } else {
                    que.setFreeTime(staticCommandMult.getSpeedTime(player));
                }
                que.setFreeCnt(0);
                // builder.setQue(PbHelper.c);
            }
        }

        player.gains.put(staticCommandMult.getType(), commandMult);
        builder.setId(id);
        builder.setEndTime(commandMult.getEndTime());
        builder.setResource(PbHelper.createResourcePb(player.resource));
        builder.setGold(player.lord.getGold());
        taskDataManager.updTask(player, TaskType.COND_FACTORY_RECRUIT, 1, staticCommandMult.getLv());
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_RECRUIT, staticCommandMult.getType(),
                staticCommandMult.getQuality());
        return builder.build();
    }

    /**
     * 征收资源
     *
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public GainResRs gainResRs(long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Mill mill = player.mills.get(id);
        if (mill == null) {
            throw new MwException(GameError.GAINRES_NOT_ENOUGH.getCode(), "领取资源时，次数不足, roleId:" + roleId);
        }
        int type = mill.getType();
        Iterator<Mill> it2 = player.mills.values().iterator();
        GainResRs.Builder builder = GainResRs.newBuilder();
        while (it2.hasNext()) {
            mill = it2.next();
            if (type != mill.getType()) {
                continue;
            }
            // 检测资源是否能征收
            if (!buildingDataManager.checkMillCanGain(player, mill)) {
                continue;
            }
            int resCnt = mill.getResCnt();
            if (resCnt <= 0) {
                continue;
            }
            mill.setResCnt(0);
            int now = TimeHelper.getCurrentSecond();
            mill.setResTime(now);
            CommonPb.Award award = gainResource(player, mill.getType(), mill.getLv(), resCnt);
            if (award != null) {
                builder.addAward(award);
            }
            builder.addMills(PbHelper.createMillPb(mill));
        }

        // 重置资源满仓推送消息的状态
        // player.removePushRecord(PushConstant.ID_RESOURCE_FULL);

        builder.setResource(PbHelper.createCombatPb(player.resource));
        taskDataManager.updTask(player, TaskType.COND_RES_AWARD, 1, type);
        battlePassDataManager.updTaskSchedule(roleId, TaskType.COND_RES_AWARD, 1);
        return builder.build();
    }

    // /**
    // * 征收资源
    // *
    // * @param roleId
    // * @param buildingId
    // * @return
    // * @throws MwException
    // */
    // public GainResRs gainResRsOld(long roleId, int buildingId) throws MwException {
    // Player player = playerDataManager.checkPlayerIsExist(roleId);
    // Mill mill = player.mills.get(buildingId);
    // if (mill == null || mill.getResCnt() <= 0) {
    // throw new MwException(GameError.GAINRES_NOT_ENOUGH.getCode(), "领取资源时，次数不足, roleId:" + roleId);
    // }
    // StaticBuildingLv staticBuildingLv = StaticBuildingDataMgr.getStaticBuildingLevel(mill.getType(), mill.getLv());
    // if (staticBuildingLv == null) {
    // mill.setResCnt(0);
    // throw new MwException(GameError.NO_CONFIG.getCode(), "领取资源时，建筑配置找不到, roleId:", roleId);
    // }
    // // 检测资源是否解锁
    // if (!buildingDataManager.checkMillUnlock(player, mill)) {
    // throw new MwException(GameError.FUNCTION_LOCK.getCode(), "功能未解锁, roleId:", roleId, ", buildingId:", buildingId);
    // }
    // int resCnt = mill.getResCnt();
    // mill.setResCnt(0);
    // int now = TimeHelper.getCurrentSecond();
    // mill.setResTime(now);
    // CommonPb.Award award = gainResource(player, mill.getType(), mill.getLv(), resCnt);
    // GainResRs.Builder builder = GainResRs.newBuilder();
    // builder.setId(buildingId);
    // if (award != null) {
    // builder.setAward(award);
    // }
    // builder.setResource(PbHelper.createCombatPb(player.resource));
    // taskDataManager.updTask(roleId, TaskType.COND_RES_AWARD, 1);
    // return builder.build();
    // }

    public CommonPb.Award gainResource(Player player, int buildingType, int buildingLv, int resCnt) {
        if (buildingLv <= 0) {
            return null;
        }
        StaticBuildingLv staticBuildingLevel = StaticBuildingDataMgr.getStaticBuildingLevel(buildingType, buildingLv);
        if (staticBuildingLevel == null) {
            LogUtil.error("BuildingLv config error,type=" + buildingType + ",lv=" + buildingLv);
            return null;
        }

        List<Integer> resOuts = staticBuildingLevel.getResourceOut();
        if (resOuts == null || resOuts.size() < 3) {
            return null;
        }

        int gain = buildingDataManager.getResourceMult(player, buildingType, resOuts.get(2));

        LogUtil.debug(player.roleId + ",收获资源=" + gain + ",建筑类型=" + buildingType + ",领取次数=" + resCnt + ",基础产量=" + resOuts
                .get(2));

        int id = 0;
        switch (buildingType) {
            case BuildingType.RES_ELE:
                id = AwardType.Resource.ELE;
                break;
            case BuildingType.RES_FOOD:
                id = AwardType.Resource.FOOD;
                break;
            case BuildingType.RES_OIL:
                id = AwardType.Resource.OIL;
                break;
            case BuildingType.RES_ORE:
                id = AwardType.Resource.ORE;
                break;
        }
        return rewardDataManager.addAwardSignle(player, AwardType.RESOURCE, id, gain * resCnt, AwardFrom.GAIN_RES);
    }

    private BuildQue createQue(Player player, int index, int buildingType, int pos, int period, int endTime) {
        if (buildingType == BuildingType.RES_ORE && buildingType == BuildingType.RES_FOOD
                && buildingType == BuildingType.RES_ELE && buildingType == BuildingType.RES_OIL) {
            // 非资源建筑
            pos = buildingType;
        }
        BuildQue buildQue = new BuildQue(player.maxKey(), index, buildingType, pos, period, endTime);
        buildQue.setFree(WorldConstant.SPEED_TYPE_VIP);
        buildQue.setParam(vipDataManager.getNum(player.lord.getVip(), VipConstant.FREE_BUILD_TIME, player));
        return buildQue;
    }

    /**
     * 建造队列是否已满
     *
     * @param player
     * @return 返回true 队列为已满 ,false表示未满
     */
    public boolean buildQueIsFull(Player player) {
        int queCnt = getBuildQueCount(player);
        Map<Integer, BuildQue> buildQue = player.buildQue;
        if (!CheckNull.isEmpty(buildQue) && buildQue.size() >= queCnt) {
            for (BuildQue b : buildQue.values()) {
                if (b.getIndex() == 1) { // 位置为1的就是免费建造队列
                    return true;
                }
            }
        }
        return false;
    }

    private int getBuildQueCount(Player player) {
        int count = 1 + player.lord.getBuildCount();
        // 以后有活动buff加成放如这里
        Effect ef = player.getEffect().get(EffectConstant.BUILD_CNT);
        if (ef != null && ef.getEndTime() > TimeHelper.getCurrentSecond()) {
            count += 1;
        }
        return count;
    }

    /**
     * 计算建筑时间
     *
     * @param player
     * @param baseTime
     * @return
     */
    private int calcBuildTime(Player player, int baseTime) {
        // 建筑设计科技
        int add = techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_13);
        double factor = add / Constant.TEN_THROUSAND;
        // 特工
        double agentEffect =
                ciaService.getAgentSkillVal(player, CiaConstant.SKILL_BUILDING_ACC) / Constant.TEN_THROUSAND;
        // 柏林官员
        double berlinJobEffect = BerlinWar.getBerlinBuffVal(player.roleId, BerlinWarConstant.BUFF_TYPE_BUILDING_TIME)
                / Constant.TEN_THROUSAND;
        //赛季天赋
        double seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_401) / Constant.TEN_THROUSAND;
        int buildTime = (int) Math.ceil(baseTime * (1 - factor) * (1 - agentEffect - berlinJobEffect - seasonTalentEffect));
        return buildTime;
    }

    public void buildQueTimerLogic() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player.isActive()) {
                try {
                    // LogUtil.channel("======pp"+player.roleId+"==isactive="+player.isActive());
                    if (!player.buildQue.isEmpty()) {
                        dealBuildQue(player, now);
                    }

                    // if (player.lord.getOnBuild() == 1) {
                    // dealAutoBuild(player);
                    // }
                } catch (Exception e) {
                    LogUtil.error("建筑队列定时器报错, lordId:" + player.lord.getLordId(), e);
                }
            }
        }
    }

    private void dealBuildQue(Player player, int now) throws MwException {
        Collection<BuildQue> list = player.buildQue.values();
        Iterator<BuildQue> it = list.iterator();
        boolean update = false;
        int pros = 0;
        while (it.hasNext()) {
            BuildQue buildQue = it.next();
            if (now >= buildQue.getEndTime()) {
                //执行离线建筑升级保存事件
                if (buildQue.getFromType() != 2) {
                    saveOffLineBuild(player, buildQue);
                }
                pros += dealOneQue(player, buildQue);
                update = true;
                it.remove();
                // 加入自动建造
                addAtuoBuild(player);
                continue;
            }
        }
    }

    private int dealOneQue(Player player, BuildQue buildQue) {
        int pros = 0;
        int buildingId = buildQue.getPos();
        int buildingType = buildQue.getBuildingType();
        int newType = buildQue.getNewType();
        int preBuildingLv = BuildingDataManager.getBuildingLv(buildingId, player);
        int buildingLv;
        if (BuildingDataManager.isResType(buildQue.getBuildingType())) {//资源建筑
            buildingLv = upMillLv(player, buildQue);
        } else {
            buildingLv = upBuildingLv(buildingId, player.building, buildQue);
            if (buildQue.getFromType() == 2) {//改建
                //获取玩家非资源建筑信息
                BuildingExt buildingExt = player.buildingExts.get(buildingId);
                if (buildingExt != null) {
                    //修改超级兵营的类型
                    buildingExt.setType(buildQue.getNewType());
                    player.buildingExts.put(buildingId, buildingExt);
                }
            }
            if (buildingId == BuildingType.STOREHOUSE) {
                // 重建次数+1
                if (buildingLv > 1) {// 建造不加家园重建次数
                    player.common.setReBuild(player.common.getReBuild() + 1);
                }
            } else if (buildingId == BuildingType.COMMAND) {
                activityDataManager.syncActChange(player, ActivityConst.ACT_COMMAND_LV);
                activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_BUILDING, buildingLv);
                worldScheduleService.updateScheduleGoal(player, ScheduleConstant.GOAL_COND_COMMAND_LV, buildingLv);
                activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_BUILDING, 1, buildingLv);
            } else if (buildingId == BuildingType.WALL) {
                /*Integer status = player.getPushRecord(PushConstant.WALL_RECRUIT);
                if (null == status || status == PushConstant.PUSH_NOT_PUSHED && wallService
                        .wallLevelInNeed(buildingLv)) {
                    player.putPushRecord(PushConstant.WALL_RECRUIT, PushConstant.ACT_IS_FULL);
                    // 发送应用外推送消息，建筑队列建造完成机会销毁，所以不用记录是否推送的状态
                    // PushMessageUtil.pushMessage(player.account, PushConstant.WALL_RECRUIT);
                }*/
            } else if (buildingId == BuildingType.TECH) {
                activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_UNIVERSITY_LV, 1, buildingLv);
            }
            playerDataManager.createRoleOpt(player, Constant.OptId.id_1, String.valueOf(buildingLv),
                    String.valueOf(buildQue.getBuildingType()));
            // playerDataManager.updTask(player, TaskType.COND_BUILDING_LV_UP, 1, null);
            // switch (buildingId) {
            // case BuildingId.COMMAND:
            // playerDataManager.updDay7ActSchedule(player, 1, buildingLv);
            // break;
            // case BuildingId.FACTORY_1:
            // case BuildingId.FACTORY_2:
            // playerDataManager.updDay7ActSchedule(player, 3, buildingLv);
            // break;
            // case BuildingId.TECH:
            // playerDataManager.updDay7ActSchedule(player, 5, buildingLv);
            // break;
            // }
        }
        // 触发检测功能解锁
        buildingDataManager.refreshSourceData(player);
        buildingDataManager.synBuildToPlayer(player, buildQue, 2);

        taskDataManager.updTask(player, TaskType.COND_BUILDING_ID_LV, buildingLv, buildingId);

        //貂蝉任务-升级建筑
        ActivityDiaoChanService.completeTask(player, ETask.BUILD_UP);
        TaskService.processTask(player, ETask.BUILD_UP);

        // 新手指引更新
        if (preBuildingLv < 1) {
            // 0->1建造
            // 更新建筑部分的
            updateBuidingGuide(player, buildingId, false);
            StaticGuideBuild sGuide = StaticBuildingDataMgr.getGuideBuildMapById(buildingId);
            if (sGuide != null && BuildingDataManager.isResType(buildingType)) {
                // 给一次征收
                giveOnceGains(player, buildingId);
            }
        }
        // 发送应用外推送消息，建筑队列建造完成机会销毁，所以不用记录是否推送的状态
        pushBuildFinish(player.account, buildingLv, newType == 0 ? buildQue.getBuildingType() : newType,
                buildQue.getPos());
        LogLordHelper.build(AwardFrom.BUILD_UP_FINISH, player.account, player.lord, buildingId, buildingLv);
        EventDataUp.buildingSuccess(player, buildingId, 0, buildingLv);
        taskDataManager.updTask(player, TaskType.COND_BUILDING_TYPE_LV, 1, buildingType);
        return pros;
    }

    /**
     * 送一次征收
     *
     * @param player
     * @param buildingId
     */
    private void giveOnceGains(Player player, int buildingId) {
        Mill mill = player.mills.get(buildingId);
        if (mill != null) {
            mill.setResCnt(mill.getResCnt() + 1);
            buildingDataManager.synGainResRs(player, mill);
            LogUtil.debug("送一次征收给 buildingId:", buildingId, ", roleId:", player.roleId);
        }
    }

    /**
     * 建筑部分的新手指引更新
     *
     * @param player
     * @param buildingId
     * @param isBuildingUp true为建筑建造,false为建造完成
     */
    public void updateBuidingGuide(Player player, int buildingId, boolean isBuildingUp) {
        StaticGuideBuild sGuide = StaticBuildingDataMgr.getGuideBuildMapById(buildingId);
        if (sGuide != null) {
            int oldIndex = player.lord.getNewState();
            int newIndex = 0;
            if (isBuildingUp) {
                newIndex = sGuide.getBuildUp();
            } else {
                newIndex = sGuide.getBuildFinlish();
            }
            if (newIndex > oldIndex) {// 保存最大的那个值
                player.lord.setNewState(newIndex);
                LogUtil.debug("建造完成建筑重新设置新手指引状态 roleId: ", player.roleId, ", buildingId:", buildingId, ", newIndex:",
                        newIndex, ", oldIndex:", oldIndex);
            }
        }
    }

    /**
     * 向玩家推送建筑升级完成消息
     *
     * @param account
     * @param buildingLv
     * @param buildingType
     * @param pos
     */
    private void pushBuildFinish(Account account, int buildingLv, int buildingType, int pos) {
        String id = "s_building_init_" + buildingType;
        String name = StaticIniDataMgr.getTextName(id);
        if (null == name) {
            LogUtil.error("建筑类型名称未找到，跳过消息推送, roleId:", account.getLordId(), ", buildingId:", id);
            return;
        }
        // 建筑物的等级和类型的判断
        if (BuildingType.WAR_FACTORY != buildingType && BuildingType.CHEMICAL_PLANT != buildingType && buildingLv < 8) {
            return;
        }
        // 推送 消息
        PushMessageUtil.pushMessage(account, PushConstant.ID_UP_BUILD_FINISH, name, buildingLv);
    }

    private int upBuildingLv(int buildingId, Building building, BuildQue buildQue) {
        int lv = 0;
        switch (buildingId) {
            case BuildingType.COMMAND:
                lv = building.getCommand() + 1;
                building.setCommand(lv);
                break;
            case BuildingType.TECH:
                lv = building.getTech() + 1;
                building.setTech(lv);
                break;
            case BuildingType.STOREHOUSE:
                lv = building.getWare() + 1;
                building.setWare(lv);
                break;
            case BuildingType.WALL:
                lv = building.getWall() + 1;
                building.setWall(lv);
                break;
            case BuildingType.WAR_COLLEGE:
                lv = building.getCollege() + 1;
                building.setCollege(lv);
                break;
            case BuildingType.WAR_FACTORY:
                lv = building.getWar() + 1;
                building.setWar(lv);
                break;
            case BuildingType.REMAKE:
                lv = building.getRefit() + 1;
                building.setRefit(lv);
                break;
            case BuildingType.ORDNANCE_FACTORY:
                lv = building.getMunition() + 1;
                building.setMunition(lv);
                break;
            case BuildingType.CHEMICAL_PLANT:
                lv = building.getChemical() + 1;
                building.setChemical(lv);
                break;
            case BuildingType.FACTORY_1:
                lv = building.getFactory1() + 1;
                building.setFactory1(lv);
                break;
            case BuildingType.FACTORY_2:
                lv = building.getFactory2() + 1;
                building.setFactory2(lv);
                break;
            case BuildingType.FACTORY_3:
                lv = building.getFactory3() + 1;
                building.setFactory3(lv);
                break;
            case BuildingType.TRAIN_FACTORY_1:
                lv = buildQue.getFromType() != 2 ? building.getTrain() + 1 : building.getTrain();
                building.setTrain(lv);
                break;
            case BuildingType.TRAIN_FACTORY_2:
                lv = buildQue.getFromType() != 2 ? building.getTrain2() + 1 : building.getTrain2();
                building.setTrain2(lv);
                break;
            case BuildingType.TRADE_CENTRE:
                lv = building.getTrade() + 1;
                building.setTrade(lv);
                break;
            case BuildingType.CLUB:
                lv = building.getClub() + 1;
                building.setClub(lv);
                break;
            case BuildingType.AIR_BASE:
                lv = building.getAir() + 1;
                building.setAir(lv);
                break;
        }
        return lv;
    }

    private int upMillLv(Player player, BuildQue buildQue) {
        // Map<Integer, Mill> map = player.mills.get(buildQue.getPos());
        // if(map==null){
        // map = new HashMap<>();
        // player.mills.put(buildQue.getPos(), map);
        // }
        // Mill mill = map.get(buildQue.getBuildingType());
        // if (mill == null) {
        // mill = new Mill(buildQue.getPos(), buildQue.getBuildingType(), 1);
        // map.put(buildQue.getBuildingType(), mill);
        // } else {
        // mill.setLv(mill.getLv() + 1);
        // }
        // return mill.getLv();
        Mill mill = player.mills.get(buildQue.getPos());
        if (mill.getLv() == 0) {
            // 从0-1
            mill.setResTime(TimeHelper.getCurrentSecond());
        }
        mill.setLv(buildQue.getFromType() != 2 ? mill.getLv() + 1 : mill.getLv());

        //修改资源田类型
        if (buildQue.getFromType() == 2) {//改建
            mill.setType(buildQue.getNewType());
            ;
            player.mills.put(buildQue.getPos(), mill);
        }

        return mill.getLv();
    }

    /**
     * Method: resourceTimerLogic
     *
     * @Description: 资源生产逻辑 @return void @throws
     */
    public void resourceTimerLogic() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            try {
                // if (player.account.getCreated() == 1) {
                dealResource(player, now);
                // }
            } catch (Exception e) {
                LogUtil.error("资源生产逻辑定时器报错, lordId:" + player.lord.getLordId(), e);
            }
        }
    }

    private void dealResource(Player player, int now) {
        if (!player.isActive()) { // 如果不在线 并且离线时间超过指定时间 不自增资源
            // if (TimeHelper.getCurrentSecond() - player.lord.getOffTime() > BuildConst.RESOURCE_STOP_ADD_OFFTIME) {
            return;
            // }
        }
        if (player.resource == null) {
            return;
        }
        // 资源领取次数每隔多少秒涨一次, 全局控制
        dealResourceHourOld(player, now);
        // if (now - player.resource.getStoreTime() >= Constant.RES_ADD_TIME) {
        // player.resource.setStoreTime(now);
        // }
    }

    /**
     * 老功能
     *
     * @param player
     * @param now
     */
    private void dealResourceHourOld(Player player, int now) {
        SynGainResRs.Builder builder = null;
        boolean allFull = true;// 记录是否所有资源都已经涨满
        Mill mill = null;
        for (Entry<Integer, Mill> kv : player.mills.entrySet()) {
            mill = kv.getValue();
            if (mill != null && mill.getResCnt() >= Constant.RES_GAIN_MAX) {
                continue;
            }

            if (mill.getLv() == 0) {
                continue;
            }
            // 检测是否能征收
            // if (!buildingDataManager.checkMillCanGain(player, mill)) {
            // continue;
            // }
            if (!mill.isUnlock()) { // 去掉原来检测解锁部分,对于跑秒定时器执行太耗时,平均20毫秒
                continue;
            }

            allFull = false;
            // 资源领取次数每隔多少秒涨一次,单个控制增长
            if (now - mill.getResTime() < Constant.RES_ADD_TIME) {
                continue;
            }
            mill.setResTime(now);
            mill.setResCnt(mill.getResCnt() + 1);
            if (builder == null) {
                builder = SynGainResRs.newBuilder();
            }
            builder.addMills(PbHelper.createMillPb(mill));
        }
        player.resource.setStoreTime(now);
        if (player.ctx != null && player.isLogin && builder != null) {
            Base.Builder msg = PbHelper.createSynBase(SynGainResRs.EXT_FIELD_NUMBER, SynGainResRs.ext, builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }

        if (allFull) {
/*            if (!player.hasPushRecord(String.valueOf(PushConstant.ID_RESOURCE_FULL))) {
                player.putPushRecord(PushConstant.ID_RESOURCE_FULL, PushConstant.PUSH_HAS_PUSHED);
                pushResourceFull(player.account);
            }*/
        }
    }

    /**
     * 新版 等待客户端一起改
     *
     * @param player
     * @param now
     */
    private void dealResourceHour(Player player, int now) {
        SynGainResRs.Builder builder = null;
        Mill mill = null;
        Common common = player.common;
        if (common == null) {
            return;
        }
        if (common.getResCnt() >= Constant.RES_GAIN_MAX) {
            return;
        }
        if (now - common.getResTime() < Constant.RES_ADD_TIME) {
            return;
        }
        common.setResCnt(common.getResCnt() + 1);
        for (Entry<Integer, Mill> kv : player.mills.entrySet()) {
            mill = kv.getValue();
            if (mill != null && mill.getResCnt() >= Constant.RES_GAIN_MAX) {
                continue;
            }
            if (mill.getLv() == 0) {
                continue;
            }
            // 资源领取次数每隔多少秒涨一次,单个控制增长
            if (now - mill.getResTime() < Constant.RES_ADD_TIME) {
                continue;
            }
            // mill.setResCnt(mill.getResCnt() + 1);
            mill.setResCnt(common.getResCnt());
            if (builder == null) {
                builder = SynGainResRs.newBuilder();
            }
            builder.addMills(PbHelper.createMillPb(mill));
        }
        if (player.isLogin && builder != null) {
            Base.Builder msg = PbHelper.createSynBase(SynGainResRs.EXT_FIELD_NUMBER, SynGainResRs.ext, builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 向玩家推送资源满仓消息
     *
     * @param account
     */
    private void pushResourceFull(Account account) {
        // PushMessageUtil.pushMessage(account, PushConstant.ID_RESOURCE_FULL);
    }

    /**
     * 建筑加速
     *
     * @param roleId
     * @return
     */
    public SpeedBuildingRs speedBuilding(long roleId, int id, int itemId, boolean isGoldSpeed, int itemNum) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);
        if (itemId != PropConstant.ITEM_ID_REDUCE_TIME_1 && itemId != PropConstant.ITEM_ID_REDUCE_TIME_2
                && itemId != PropConstant.ITEM_ID_REDUCE_TIME_3 && itemId != -1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "建筑加速时，无队列, roleId:" + roleId);
        }
        Map<Integer, BuildQue> buildQue = player.buildQue;
        if (buildQue == null || buildQue.size() <= 0 || player.buildQue.get(id) == null) {
            throw new MwException(GameError.SPEED_NO_TIME.getCode(), "建筑加速时，无队列, roleId:" + roleId);
        }

        int now = TimeHelper.getCurrentSecond();
        BuildQue que = player.buildQue.get(id);
        int second = que.getEndTime() - now;
        if (second <= 0) {
            throw new MwException(GameError.SPEED_NO_TIME.getCode(), "建筑加速时，时间结束, roleId:" + roleId);
        }
        itemNum = itemNum <= 0 ? 1 : itemNum;
        SpeedBuildingRs.Builder builder = SpeedBuildingRs.newBuilder();
        if (itemId == -1) {// 金币加速
            int needGold = (int) Math.ceil(second * 1.00 / 60);
            // TODO: 2021/11/8 注意！！！ 这里的消耗系数是服务器写死的. 海外版本是 * 8, 国内版本是 * 2
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold * 2,
                    AwardFrom.BUILDING_SPEED, que.getPos());
        } else {
            StaticProp prop = StaticPropDataMgr.getPropMap(itemId);
            if(Objects.isNull(prop))
                throw new MwException(GameError.NO_CONFIG.getCode(),"招募加速，使用道具加速错误，道具不存在, itemId=" + itemId);
            //计算实际需要几个道具
//            int a_ = prop.getDuration() / second;
//            int b_ = prop.getDuration() % second;
//            itemNum = a_ + (b_>0?1:0);
            if (isGoldSpeed) {
                if (prop.getPrice() <= 0) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "建筑加速时，价格错, roleId:" + roleId);
                }
                rewardDataManager
                        .checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, prop.getPrice() * itemNum,
                                AwardFrom.BUILDING_SPEED, que.getPos());
            } else {
                rewardDataManager
                        .checkAndSubPlayerResHasSync(player, AwardType.PROP, itemId, itemNum, AwardFrom.BUILDING_SPEED,
                                que.getPos());
            }
            second = Math.min(prop.getDuration() * itemNum, second); // 道具具体加速
            builder.setItemId(player.props.get(itemId) != null ? player.props.get(itemId).getCount() : 0);
        }

        que.setEndTime(que.getEndTime() - second);
        // 潮哥 因为客户端显示误差,所以已经结束的队列,再减5秒
        if (now >= que.getEndTime()) {
            LogUtil.debug("建筑加速多减5秒");
            que.setEndTime(que.getEndTime() - 5);
        }
        builder.setQueue(PbHelper.createBuildQuePb(que));
        builder.setGold(player.lord.getGold());

        return builder.build();
    }

    /**
     * 获取重建家园奖励
     *
     * @param roleId
     * @return
     */
    public RebuildRewardRs rebuildReward(Long roleId) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);
        List<Award> awards = player.awards.get(Constant.AwardType.TYPE_1);
        if (awards == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "获取重建家园奖励时，无奖励, roleId:" + roleId);
        }
        // 离线时被击飞,上线时才触发礼包
        if (player.isFirstHitFly() && player.isOffOnlineHitFly()) {
            // 首次被击飞
            activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_FIRST_BY_HIT_FLY, player);
            player.setHitFlyCount(player.getHitFlyCount() + 1);
            player.setOffOnlineHitFly(false);
        }
        // 重建家园
        activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_REBUILD, player);
        List<Award> awardClone = new ArrayList<>();
        awardClone.addAll(awards);
        player.awards.remove(Constant.AwardType.TYPE_1);
        player.setAdvanceAward(false);
        for (Award award : awardClone) {
            rewardDataManager.addAward(player, award.getType(), award.getId(), award.getCount(), AwardFrom.REBUILD);
        }
        RebuildRewardRs.Builder builder = RebuildRewardRs.newBuilder();
        builder.addAllAward(awards);
        return builder.build();
    }

    /**
     * 获取重建家园信息
     *
     * @param roleId
     * @return
     */
    public SyncRoleRebuildRs getRoleRebuild(Long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        List<Award> awards = player.awards.get(Constant.AwardType.TYPE_1);
        SyncRoleRebuildRs.Builder builder = SyncRoleRebuildRs.newBuilder();
        builder.setRebuild(player.common.getReBuild());
        if (awards != null) {
            builder.addAllAward(awards);
            builder.setIsAdvance(player.isAdvanceAward());
            Mail mail = player.mails.values().stream()
                    .filter(m -> m.getMoldId() == MailConstant.MOLD_DEF_CITY_FAIL && player.getMailReport(m) != null)
                    .max(Comparator.comparingInt(Mail::getTime))
                    .orElse(null);
            if (mail != null) {
                // 最近的一次防守失败
                int battleTime = mail.getTime();
                CommonPb.Report report = player.getMailReport(mail);
                builder.setAtkCamp(report.getRptPlayer().getAtkSum().getCamp());
                builder.setAtkName(report.getRptPlayer().getAttack().getName());
                builder.setQuickBuyArmyCnt(player.getMixtureDataById(PlayerConstant.DAILY_QUICK_BUY_ARMY));
                builder.setBattleTime(battleTime);
            }
        }
        return builder.build();
    }

    /**
     * 建造建筑
     *
     * @param roleId
     * @param buildingType
     * @param buildingId
     * @throws MwException
     */
    public GamePb4.TrainingBuildingRs trainingBuilding(long roleId, int buildingId, int buildingType)
            throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!buildingDataManager.checkBuildingLock(player, buildingId)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(),
                    "建造建筑时，建筑未解锁, roleId:" + roleId + ",buildingId:" + buildingId);
        }
        BuildingExt buildingExt = player.buildingExts.get(buildingId);
        if (!CheckNull.isNull(buildingExt)) {
            if (buildingExt.getType() != 0) {
                throw new MwException(GameError.TRAIN_NOT_DEMOLITION.getCode(), "建筑未拆除, roleId:", roleId,
                        ",buildingId:", buildingId);
            }
            int otherBuildId = buildingId == BuildingType.TRAIN_FACTORY_1 ?
                    BuildingType.TRAIN_FACTORY_2 :
                    BuildingType.TRAIN_FACTORY_1;
            BuildingExt otherBuildExt = player.buildingExts.get(otherBuildId);
            if (otherBuildExt != null && otherBuildExt.getType() != 0 && otherBuildExt.getType() == buildingType) {
                throw new MwException(GameError.TRAIN_BUILD_ERROR.getCode(), "不同的超级工厂, 不能建造同类型的兵种, roleId:", roleId,
                        ",buildingId:", buildingId, ",otherBuildType:", buildingType);
            }
            for (BuildQue que : player.buildQue.values()) {
                if (que.getPos() == otherBuildId && que.getNewType() == buildingType) {
                    throw new MwException(GameError.TRAIN_BUILD_ERROR.getCode(), "不同的超级工厂, 不能建造同类型的兵种, roleId:", roleId,
                            ",buildingId:", buildingId, ",otherBuildType:", buildingType);
                }
            }

            buildingExt.setType(buildingType);
        }
        Building building = player.building;
        if (building == null) {
            buildingDataManager.createBuilding(player);
            building = player.building;
        }
        BuildingBase.Builder buildingBase = BuildingBase.newBuilder();
        buildingBase.setId(buildingExt.getId()).setType(buildingExt.getType())
                .setLv(buildingId == BuildingType.TRAIN_FACTORY_1 ? building.getTrain() : building.getTrain2())
                .setUnlock(buildingDataManager.checkBuildingLock(player, buildingExt.getId()));
        GamePb4.TrainingBuildingRs.Builder builder = GamePb4.TrainingBuildingRs.newBuilder();
        builder.setBuild(buildingBase);
        return builder.build();
    }

    /**
     * 建筑拆除
     *
     * @param buildingId
     * @param roleId
     * @return
     */
    public DesBuildingRs desBuilding(int buildingId, Long roleId) throws MwException {
        StaticBuildingInit buildingInit = StaticBuildingDataMgr.getBuildingInitMapById(buildingId);
        if (!CheckNull.isNull(buildingInit) && buildingInit.getCanDestroy() != BuildingType.BUILD_CAN_DESTORY_STATUS) {
            throw new MwException(GameError.INVALID_PARAM.getCode(),
                    "建筑不允许拆除, roleId:" + roleId + ",buildingPos:" + buildingId);
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!techDataManager.isOpen(player, TechConstant.TYPE_27)) { // 开启所需等级
            throw new MwException(GameError.REBUILD_NEED_TECH_LV.getCode(),
                    "建筑不允许拆除,未开启功能 roleId:" + roleId + ",buildingPos:" + buildingId);
        }
        BuildingExt buildingExt = player.buildingExts.get(buildingId);
        if (CheckNull.isNull(buildingExt)) {
            throw new MwException(GameError.BUILDEXT_NOT_FOUND.getCode(), "建筑扩展未找到, roleId:", player.roleId,
                    ", buildingId:", buildingId);
        }
        int type = buildingExt.getType();
        if (type == 0) {
            throw new MwException(GameError.BUILDING_NOT_CREATE.getCode(), "建筑还未建造, roleId:", player.roleId,
                    ", buildingId", buildingId);
        }
        if (BuildingDataManager.isResType(type)) {
            player.mills.remove(buildingId);
        } else if (BuildingDataManager.isTrainType(type)) {
            Factory factory = player.factory.get(buildingId);
            if (!CheckNull.isNull(factory) && !CheckNull.isEmpty(factory.getAddList())) {
                throw new MwException(GameError.BUILD_IS_WORKING.getCode(),
                        "建筑正在生产,不能升级  roleId:" + roleId + ", buildingId:" + buildingId);
            }
            for (BuildQue build : player.buildQue.values()) {
                if (build.getPos() == buildingId) {
                    throw new MwException(GameError.ALREADY_BUILD.getCode(),
                            "升级建筑时，建筑升级中, roleId:" + roleId + ",buildingPos:" + buildingId);
                }
            }
            factory.setFctLv(Constant.FACTORY_TIME_NINT_LEVL);// 清除募兵加时
            buildingExt.setType(0);// 清除兵营类型
            buildingDataManager.ReInitBuilding(player, buildingId);// 重置建筑等级
        }
        DesBuildingRs.Builder builder = DesBuildingRs.newBuilder();
        builder.setStatus(1);
        return builder.build();
    }

    /**
     * 建筑重建
     *
     * @param id
     * @param type
     * @param roleId
     * @return
     * @throws MwException
     */
    public ReBuildRs reBuild(int id, int type, Long roleId) throws MwException {
        if (StaticBuildingDataMgr.getBuildingInitMapById(id).getCanDestroy() != BuildingType.BUILD_CAN_DESTORY_STATUS) {
            throw new MwException(GameError.INVALID_PARAM.getCode(),
                    "建筑不允许重建, roleId:" + roleId + ",buildingPos:" + id);
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Mill mill = player.mills.get(id);
        if (mill != null && mill.getLv() > 0) {
            throw new MwException(GameError.INVALID_PARAM.getCode(),
                    "建筑重建,目标资源已存在 roleId:" + roleId + ",buildingPos:" + id);
        }
        mill = new Mill(id, type, 1, 0);
        player.mills.put(id, mill);
        ReBuildRs.Builder builder = ReBuildRs.newBuilder();
        builder.setMill(PbHelper.createMillPb(mill));
        return builder.build();
    }

    // ==============================================自动建造相关===================================================

    /**
     * 开启或关闭自动建造
     *
     * @param roleId
     * @param onOff
     * @return
     * @throws MwException
     */
    public OnOffAutoBuildRs onOffAutoBuild(long roleId, int onOff) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int cnt = player.common.getAutoBuildCnt();
        // 如果有自动建造次数
        if (cnt > 0) {
            player.common.setAutoBuildOnOff(onOff);
            if (onOff == 1) {
                int queCnt = getBuildQueCount(player);
                for (int i = 0; i < queCnt; i++) {
                    if (addAtuoBuild(player) == -1) {
                        throw new MwException(GameError.AUTO_BUILD_NOT_FOUND.getCode(), "暂时没有建筑满足升级条件  roleId:",
                                roleId);
                    }
                }
            }
        } else {
            // 次数不够自动关闭
            player.common.setAutoBuildOnOff(0);
        }
        OnOffAutoBuildRs.Builder builder = OnOffAutoBuildRs.newBuilder();
        builder.setAutoBuildOnOff(player.common.getAutoBuildOnOff());
        return builder.build();
    }

    /**
     * 加入自动建造
     *
     * @param player
     */
    /**
     * @param player
     * @return 0:默认返回 ;-1:没有建筑满足升级条件
     */
    public int addAtuoBuild(Player player) {
        if (player.common == null) {
            return 0;
        }
        // 开关控制和次数的检查
        if (player.common.getAutoBuildCnt() <= 0 || player.common.getAutoBuildOnOff() == 0) {
            player.common.setAutoBuildOnOff(0);
            return 0;
        }
        // LogUtil.debug("roleId:", player.roleId, "==============进入自动建造:===============");
        Map<Integer, BuildQue> buildQueMap = player.buildQue;
        int queCnt = getBuildQueCount(player);
        if (buildQueIsFull(player)) {
            // 队列满了
            return 0;
        }
        int pos = getAutoBuildId(player);
        LogUtil.debug("roleId:", player.roleId, ",==========自动建造========= 建筑id:", pos);
        if (pos < 0) {
            // 找不到可升级建筑
            player.common.setAutoBuildOnOff(0);// 修改状态
            buildingDataManager.syncAutoBuildInfo(player);// 推送
            return -1;
        }

        StaticBuildingInit staticBuilding = StaticBuildingDataMgr.getBuildingInitMapById(pos);
        //获取建筑类型  从玩家建筑信息中获取
        int type = getBuildingType(player, pos);
        if (type == 0) {
            return 0;
        }
        ;

        int buildLevel = BuildingDataManager.getBuildingLv(pos, player);
        StaticBuildingLv staticBuildingLevel = StaticBuildingDataMgr.getStaticBuildingLevel(type, buildLevel + 1);
        try {
            rewardDataManager.checkAndSubPlayerRes(player, staticBuildingLevel.getUpNeedResource(), AwardFrom.UP_BUILD);
        } catch (MwException e) {
            return 0;
        }

        if (BuildingDataManager.isResType(type)) {
            Mill mill = player.mills.get(pos);
            if (mill == null) {
                mill = new Mill(pos, type, staticBuilding.getInitLv(), 0);
                // mill.setUnlock(buildingDataManager.checkBuildingLockCalc(player, staticBuilding.getBuildingId()));
                player.mills.put(pos, mill);
            }
        }
        int now = TimeHelper.getCurrentSecond();
        int haust = calcBuildTime(player, staticBuildingLevel.getUpTime());
        for (int i = 1; i <= queCnt; i++) {
            if (!buildQueMap.containsKey(i)) {
                // 减掉次数
                player.common.decAutoBuildCnt();
                if (player.common.getAutoBuildCnt() <= 0) {// 为0就关闭
                    player.common.setAutoBuildOnOff(0);
                }
                BuildQue que = createQue(player, i, type, pos, haust, now + haust);
                que.setFromType(1);
                buildQueMap.put(i, que);
                // 推送建造队列和次数
                buildingDataManager.synBuildToPlayer(player, que, 1);
                buildingDataManager.syncAutoBuildInfo(player);
                break;
            }
        }

        // 建筑升级满足触发条件
        /*int giftId = activityDataManager.checkBuildTrigger(player, type, buildLevel);
        if (giftId != 0) {
            try {
                activityService.checkTriggerGiftSyncByGiftId(giftId, player);
            } catch (MwException e) {
                LogUtil.error(e);
            }
        }*/
        taskDataManager.updTask(player, TaskType.COND_18, 1, type);
        taskDataManager.updTask(player, TaskType.COND_BUILDING_UP_43, 1);
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_BUILDING_UP_43, 1);

        return 0;
    }

    /**
     * 获取新手指引中的自动建造
     *
     * @param player
     * @return
     */
    private int getAutoBuildIdByGuide(Player player) {
        // 看新手指引中是否有自动建造
        StaticGuidAward staticGuidAward = StaticLordDataMgr.getStaticGuidAward(player.lord.getNewState());
        if (staticGuidAward != null && !CheckNull.isEmpty(staticGuidAward.getBuildIds())) {
            for (List<Integer> builingIdAndLv : staticGuidAward.getBuildIds()) {
                int builingId = builingIdAndLv.get(0);
                StaticBuildingInit sBuildInit = StaticBuildingDataMgr.getBuildingInitMapById(builingId);
                if (sBuildInit == null) {
                    continue;
                }
                int buildLevel = BuildingDataManager.getBuildingLv(builingId, player);
                if (buildLevel >= builingIdAndLv.get(1)) { // 超过了设定等级
                    continue;
                }
                if (isBuildUpLv(player, sBuildInit.getBuildingType(), builingId)) {
                    return builingId;
                }
            }
        }
        return -1;
    }

    /**
     * 按规则找到当前可用的建筑
     *
     * @param player
     * @return -1 表示找到合适建筑可用
     */
    private int getAutoBuildId(Player player) {

        int buildingId = getAutoBuildIdByGuide(player);
        if (buildingId != -1) {
            return buildingId;
        }
        // 获取当前任务id
        // List<Integer> curTaskIds = taskDataManager.getCurTask(player);
        List<StaticTask> buildTask =  player.chapterTask.getOpenTasks().keySet().stream().map(t -> StaticTaskDataMgr.getTaskById(t))
                .filter(t -> t.getCond() == TaskType.COND_BUILDING_TYPE_LV || t.getCond() == TaskType.COND_RES_FOOD_CNT
                        || t.getCond() == TaskType.COND_RES_OIL_CNT || t.getCond() == TaskType.COND_RES_ELE_CNT
                        || t.getCond() == TaskType.COND_RES_ORE_CNT)
                // 过滤未完成的任务
                .filter(t -> {
                    int taskId = t.getTaskId();
                    Task task = player.chapterTask.getOpenTasks().get(taskId);
                    if (CheckNull.isNull(task)) {
                        return false;
                    }
                    StaticTask sTask = StaticTaskDataMgr.getTaskById(taskId);
                    if (CheckNull.isNull(sTask)) {
                        return false;
                    }
                    // 拿最新的任务状态
                    Task currentTask = taskDataManager.currentMajorTask(player, task, sTask);
                    return currentTask != null && currentTask.getStatus() == TaskType.TYPE_STATUS_UNFINISH  && currentTask.getSchedule() < sTask.getSchedule();
                })
                .collect(Collectors.toList());
        if (CheckNull.isEmpty(buildTask)) {// 任务中没有建筑升级
            return getNonTaskAutoBulidId(player);
        } else {
            // 任务中的见着优先
            int buildId = getTaskAutoBuildId(player, buildTask);
            if (buildId == -1) {// 任务中的建筑不满足
                buildId = getNonTaskAutoBulidId(player);
            }
            return buildId;
        }
    }

    /**
     * 获取非任务中的自动建造
     *
     * @param player
     * @return 建筑id
     */
    private int getNonTaskAutoBulidId(Player player) {
        for (int type : BuildingType.AUTO_BUILD_RULE) {
            if (type == BuildingType.RES_ORE || type == BuildingType.RES_FOOD || type == BuildingType.RES_ELE
                    || type == BuildingType.RES_OIL) {
                List<Mill> mills = new ArrayList<>();
                mills.addAll(player.mills.values());
                final int fType = type;
                // 筛选出对应可用的资源建筑,并按照等级从小到大排序
                mills = mills.stream().filter(m -> m.getType() == fType && m.isUnlock() && m.getLv() > 0)
                        .sorted((m1, m2) -> m1.getLv() - m2.getLv()).collect(Collectors.toList());
                if (!CheckNull.isEmpty(mills)) {
                    for (Mill m : mills) {
                        if (isBuildUpLv(player, m.getType(), m.getPos())) {
                            return m.getPos();
                        }
                    }
                }
            }
            if (isBuildUpLv(player, type, type)) {
                return type;
            }
        }
        return -1;
    }


    /**
     * 获取任务中的自动建造
     *
     * @param player
     * @param buildTask
     * @return 建筑id
     */
    private int getTaskAutoBuildId(Player player, List<StaticTask> buildTask) {
        // 过滤不能自动建造的建筑
        buildTask = buildTask.stream().filter(st ->  getBuildType(st) != null)
                .filter(st -> {
                    Turple turple = getBuildType(st);
                    int buildType = (int) turple.getB();
                    if (BuildingDataManager.isResType(buildType)) {
                        List<Mill> mills = new ArrayList<>();
                        mills.addAll(player.mills.values());
                        final int fType = buildType;
                        // 筛选出对应可用的资源建筑,并按照等级从小到大排序
                        mills = mills.stream().filter(m -> m.getType() == fType && m.isUnlock() && m.getLv() > 0)
                                .sorted(Comparator.comparingInt(Mill::getLv)).collect(Collectors.toList());
                        if (!CheckNull.isEmpty(mills)) {
                            for (Mill m : mills) {
                                if (isBuildUpLv(player, m.getType(), m.getPos())) {
                                    return true;
                                }
                            }
                        }
                    }
                    if (isBuildUpLv(player, buildType, buildType)) {
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());
        // 优先自动选择主线任务的建筑
        StaticTask mainBuildTask = buildTask.stream().filter(st -> st.getType() == TaskType.TYPE_MAIN).findFirst().orElse(null);
        if (!CheckNull.isNull(mainBuildTask)) {
            Turple turple = getBuildType(mainBuildTask);
            int buildType = (int) turple.getB();
            if (BuildingDataManager.isResType(buildType)) {
                List<Mill> mills = new ArrayList<>();
                mills.addAll(player.mills.values());
                final int fType = buildType;
                // 筛选出对应可用的资源建筑,并按照等级从小到大排序
                mills = mills.stream().filter(m -> m.getType() == fType && m.isUnlock() && m.getLv() > 0)
                        .sorted(Comparator.comparingInt(Mill::getLv)).collect(Collectors.toList());
                if (!CheckNull.isEmpty(mills)) {
                    for (Mill m : mills) {
                        if (isBuildUpLv(player, m.getType(), m.getPos())) {
                            return m.getPos();
                        }
                    }
                }
            }
            if (isBuildUpLv(player, buildType, buildType)) {
                return buildType;
            }
        } else {
            StaticTask subBuildTask = buildTask.stream()
                    .sorted(Comparator.comparingInt(StaticTask::getBuildTaskLv).thenComparingInt((st) -> (int) getBuildType(st).getA())).findFirst().orElse(null);
            if (!CheckNull.isNull(subBuildTask)) {
                Turple turple = getBuildType(subBuildTask);
                int buildType = (int) turple.getB();
                if (BuildingDataManager.isResType(buildType)) {
                    List<Mill> mills = new ArrayList<>();
                    mills.addAll(player.mills.values());
                    final int fType = buildType;
                    // 筛选出对应可用的资源建筑,并按照等级从小到大排序
                    mills = mills.stream().filter(m -> m.getType() == fType && m.isUnlock() && m.getLv() > 0)
                            .sorted(Comparator.comparingInt(Mill::getLv)).collect(Collectors.toList());
                    if (!CheckNull.isEmpty(mills)) {
                        for (Mill m : mills) {
                            if (isBuildUpLv(player, m.getType(), m.getPos())) {
                                return m.getPos();
                            }
                        }
                    }
                }
                if (isBuildUpLv(player, buildType, buildType)) {
                    return buildType;
                }
            }
        }

        List<Turple<Integer, Integer>> types = new ArrayList<>();
        for (StaticTask t : buildTask) {
            if (t.getCond() == TaskType.COND_BUILDING_TYPE_LV) {
                int index = getIndex(t.getCondId(), BuildingType.AUTO_BUILD_RULE);
                if (index < 0) {
                    continue;
                }
                types.add(new Turple<Integer, Integer>(index, t.getCondId()));// 获取建筑id
            } else if (t.getCond() == TaskType.COND_RES_FOOD_CNT) {
                int index = getIndex(BuildingType.RES_FOOD, BuildingType.AUTO_BUILD_RULE);
                if (index < 0) {
                    continue;
                }
                types.add(new Turple<Integer, Integer>(index, BuildingType.RES_FOOD));// 获取建筑id
            } else if (t.getCond() == TaskType.COND_RES_OIL_CNT) {
                int index = getIndex(BuildingType.RES_OIL, BuildingType.AUTO_BUILD_RULE);
                if (index < 0) {
                    continue;
                }
                types.add(new Turple<Integer, Integer>(index, BuildingType.RES_OIL));
            } else if (t.getCond() == TaskType.COND_RES_ELE_CNT) {
                int index = getIndex(BuildingType.RES_ELE, BuildingType.AUTO_BUILD_RULE);
                if (index < 0) {
                    continue;
                }
                types.add(new Turple<Integer, Integer>(index, BuildingType.RES_ELE));
            } else if (t.getCond() == TaskType.COND_RES_ORE_CNT) {
                int index = getIndex(BuildingType.RES_ORE, BuildingType.AUTO_BUILD_RULE);
                if (index < 0) {
                    continue;
                }
                types.add(new Turple<Integer, Integer>(index, BuildingType.RES_ORE));
            }
        }
        types.sort((t1, t2) -> t1.getA() - t2.getA()); // 排序
        for (Turple<Integer, Integer> t : types) {
            int type = t.getB();
            if (BuildingDataManager.isResType(type)) {
                List<Mill> mills = new ArrayList<>();
                mills.addAll(player.mills.values());
                final int fType = type;
                // 筛选出对应可用的资源建筑,并按照等级从小到大排序
                mills = mills.stream().filter(m -> m.getType() == fType && m.isUnlock() && m.getLv() > 0)
                        .sorted((m1, m2) -> m1.getLv() - m2.getLv()).collect(Collectors.toList());
                if (!CheckNull.isEmpty(mills)) {
                    for (Mill m : mills) {
                        if (isBuildUpLv(player, m.getType(), m.getPos())) {
                            return m.getPos();
                        }
                    }
                }
            }
            if (isBuildUpLv(player, type, type)) {
                return type;
            }
        }
        return -1;
    }

    private Turple getBuildType(StaticTask t) {
        int index;
        Turple turple = null;
        if (t.getCond() == TaskType.COND_BUILDING_TYPE_LV) {
            index = getIndex(t.getCondId(), BuildingType.AUTO_BUILD_RULE);
            if (index >= 0) {
                turple = new Turple<>(index, t.getCondId());
            }
        } else if (t.getCond() == TaskType.COND_RES_FOOD_CNT) {
            index = getIndex(BuildingType.RES_FOOD, BuildingType.AUTO_BUILD_RULE);
            if (index >= 0) {
                turple = new Turple<>(index, BuildingType.RES_FOOD);
            }
        } else if (t.getCond() == TaskType.COND_RES_OIL_CNT) {
            index = getIndex(BuildingType.RES_OIL, BuildingType.AUTO_BUILD_RULE);
            if (index >= 0) {
                turple = new Turple<>(index, BuildingType.RES_OIL);
            }
        } else if (t.getCond() == TaskType.COND_RES_ELE_CNT) {
            index = getIndex(BuildingType.RES_ELE, BuildingType.AUTO_BUILD_RULE);
            if (index >= 0) {
                turple = new Turple<>(index, BuildingType.RES_ELE);
            }
        } else if (t.getCond() == TaskType.COND_RES_ORE_CNT) {
            index = getIndex(BuildingType.RES_ORE, BuildingType.AUTO_BUILD_RULE);
            if (index >= 0) {
                turple = new Turple<>(index, BuildingType.RES_ORE);
            }
        }
        return turple;
    }

    private static int getIndex(int x, int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == x) {
                return i;
            }
        }
        return -1; // 若数组中没有则返回-1
    }

    // 判断建筑是否满足升级条件,自动建造
    public boolean isBuildUpLv(Player player, int type, int pos) {
        int buildLevel = BuildingDataManager.getBuildingLv(pos, player);
        long roleId = player.roleId;
        // 判断是否在已经在升级
        for (BuildQue build : player.buildQue.values()) {
            LogUtil.debug("========== build.getPos：", build.getPos());
            if (build.getPos() == pos) {
                LogUtil.debug("升级建筑时，建筑升级中, roleId:" + roleId + ",buildingPos:" + pos + "type: " + type);
                return false;
            }
        }

        int buildingId = pos != 0 ? pos : type;
        if (!buildingDataManager.checkBuildingLock(player, buildingId)) {
            LogUtil.debug("升级建筑时，建筑未解锁, roleId:" + roleId + ", buildingPos:" + pos);
            return false;
        }
        if (buildLevel < 1) {
            // 升级建筑时，建筑不允许升级
            LogUtil.debug("升级建筑时，建筑还未建造, roleId:" + roleId + ", buildingPos:" + pos);
            return false;
        }
        StaticBuildingLv staticBuildingLevel = StaticBuildingDataMgr.getStaticBuildingLevel(type, buildLevel + 1);
        if (staticBuildingLevel == null) {
            // 升级建筑时，找不到升级配置,或已满级
            LogUtil.debug("升级建筑时，找不到升级配置,或已满级 roleId:" + roleId + ",  buildingPos:" + pos);
            return false;
        }
        // 司令部特出要求，司令部开启等级上限
        if (type == BuildingType.COMMAND && buildLevel >= Constant.MAX_COMMAND_LV) {
            LogUtil.debug(" roleId:" + roleId + ", buildingType:" + type);
            return false;
        }
        if (player.lord.getLevel() < staticBuildingLevel.getRoleLv()) {
            LogUtil.debug("升级建筑时，等级不足, roleId:" + roleId + ", buildingPos:" + pos + ", conf:" + staticBuildingLevel);
            return false;
        }
        if (buildingDataManager.checkBuildingLv(player, staticBuildingLevel.getUpNeedBuilding())) {
            LogUtil.debug("升级建筑时，建筑等级不足, roleId:" + roleId + ",  buildingPos:" + pos);
            return false;
        }

        // 兵营在造兵时不能升级
        if (pos == BuildingType.FACTORY_1 || pos == BuildingType.FACTORY_2 || pos == BuildingType.FACTORY_3) {
            Factory factory = player.factory.get(pos);
            if (factory != null && !CheckNull.isEmpty(factory.getAddList())) {
                LogUtil.debug("兵营正在造兵,不能升级  roleId:" + roleId + ", buildingId:" + pos);
                return false;
            }
        }
        // 检测化工厂是否正在生产
        if (pos == BuildingType.CHEMICAL_PLANT) {
            if (player.chemical != null && !CheckNull.isEmpty(player.chemical.getPosQue())) {
                LogUtil.debug("化工厂是否正在生产,不能升级  roleId:" + roleId + ", buildingId:" + pos);
                return false;
            }
        }
        // 检测科研所是否在研究
        if (pos == BuildingType.TECH) {
            if (player.tech != null && player.tech.getQue() != null && player.tech.getQue().getId() > 0) {
                // 检测购买过vip礼包5
                if (player.shop == null) {// shop为null
                    LogUtil.debug("科研所正在研究,不能升级  roleId:" + roleId);
                    return false;
                } else {
                    // 购买vip5礼包,同时雇佣了高级研究院
                    if (!(player.shop.getVipId().contains(Constant.TECH_QUICK_VIP_BAG) && techDataManager
                            .isAdvanceTechGain(player))) {
                        LogUtil.debug("科研所正在研究,不能升级  roleId:" + roleId);
                        return false;
                    }
                }
            }
        }

        // 检查资源条件是否满足
        try {
            rewardDataManager.checkPlayerResIsEnough(player, staticBuildingLevel.getUpNeedResource(), "升级建筑时");
        } catch (MwException e) {
            return false;
        }
        return true;
    }

    // ==============================================自动建造相关===================================================

    /**
     * @param player
     * @param buildingId
     * @return 参数
     * int    返回类型
     * @throws
     * @Title: getBuildingType
     * @Description: 根据建筑id获取玩家对应的建筑类型
     */
    public int getBuildingType(Player player, int buildingId) {
        //获取玩家非资源建筑信息
        BuildingExt buildingExt = player.buildingExts.get(buildingId);
        //获取玩家资源建筑信息
        Mill mill = player.mills.get(buildingId);
        if (buildingExt == null && mill == null) {
            return 0;
        }
        return mill != null ? mill.getType() : buildingExt.getType();
    }

    /**
     * @param player
     * @param buildQue 参数
     *                 void    返回类型
     * @throws
     * @Title: saveOffLineBuild
     * @Description: 建筑完成升级 且 玩家是离线状态  则保存离线升级的建筑信息
     */
    public void saveOffLineBuild(Player player, BuildQue buildQue) {
        if (!player.isLogin) {
            if (player.offLineBuilds == null) {
                player.offLineBuilds = new HashMap<Integer, OffLineBuild>();
            }
            OffLineBuild.Builder offLineBuild = OffLineBuild.newBuilder();
            if (player.offLineBuilds.get(buildQue.getPos()) == null) {//第一次升级
                int buildLevel = BuildingDataManager.getBuildingLv(buildQue.getPos(), player);
                offLineBuild.setId(buildQue.getPos());
                offLineBuild.setType(buildQue.getBuildingType());
                offLineBuild.setLv(buildLevel);
                offLineBuild.setNewLv(buildLevel + 1);
                player.offLineBuilds.put(buildQue.getPos(), offLineBuild.build());
            } else {
                offLineBuild = player.offLineBuilds.get(buildQue.getPos()).toBuilder();
                offLineBuild.setNewLv(offLineBuild.getNewLv() + 1);
                player.offLineBuilds.put(buildQue.getPos(), offLineBuild.build());
            }
        }
    }

    /**
     * 使用道具触发立即升级(针对司令部)
     */
    public void buildingLvUpImmediately(Player player, int level) {
        int buildingId = BuildingType.COMMAND;
        StaticBuildingInit sBuildingInit = StaticBuildingDataMgr.getBuildingInitMapById(buildingId);
        long roleId = player.lord.getLordId();
        if (sBuildingInit == null) {
            LogUtil.error("升级建筑配置未找到, roleId:" + roleId + ",buildingId:" + buildingId);
            return;
        }
        //获取建筑类型  从玩家建筑信息中获取
        int buildingType = getBuildingType(player, buildingId);
        if (buildingType == 0) {
            LogUtil.error("升级建筑时， 建筑未建造, roleId:" + roleId + ",buildingId:" + buildingId);
            return;
        }
        // 解锁判断
        if (!buildingDataManager.checkBuildingLock(player, buildingId)) {
            LogUtil.error("升级建筑时， 建筑未解锁, roleId:" + roleId + ",buildingPos:" + buildingId);
            return;
        }
        int buildLevel = BuildingDataManager.getBuildingLv(buildingId, player);
        if (buildLevel >= sBuildingInit.getMaxLv()) {
            LogUtil.error("升级建筑时， 已满级 roleId:" + roleId + ",buildingId:" + buildingId);
            return;
        }
        // 建筑等级大于等于直升等级
        if (buildLevel >= level) {
            LogUtil.error("升级建筑时, 建筑已到当前等级, roleId:" + roleId + ",buildingPos:" + buildingId);
            return;
        } else if (buildLevel + 1 == level) {
            int now = TimeHelper.getCurrentSecond();
            Map<Integer, BuildQue> buildQue = player.buildQue;
            // 如果存在升级，则升级
            for (BuildQue build : buildQue.values()) {
                if (build.getPos() == buildingId) {
                    build.setEndTime(now);
                    // 清除免费加速
                    build.clearFree();
                    // 任务进度
                    taskDataManager.updTask(player, TaskType.COND_FREE_CD, 1, build.getBuildingType());
                    break;
                }
            }
        }
    }
}
