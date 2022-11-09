package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.eventbus.Subscribe;
import com.gryphpoem.game.zw.core.eventbus.ThreadMode;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb2.SyncWorldChgRs;
import com.gryphpoem.game.zw.pb.GamePb4.SyncWorldEventRs;
import com.gryphpoem.game.zw.pojo.p.AttrData;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.p.MineData;
import com.gryphpoem.game.zw.resource.domain.p.Robot;
import com.gryphpoem.game.zw.resource.domain.p.WorldEvent;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.relic.RelicEntity;
import com.gryphpoem.game.zw.resource.pojo.world.*;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.random.MineLvRandom;
import com.gryphpoem.game.zw.service.WorldScheduleService;
import com.gryphpoem.game.zw.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Component
public class WorldDataManager {

    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private TechDataManager techDataManager;
    @Autowired
    private HonorDailyDataManager honorDailyDataManager;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;

    // =========================== pos 相关的 start=============================
    // 地图上所有已经用掉的位置
    // private Set<Integer> allAlreadyPosSet = new CopyOnWriteArraySet<>();

    // 世界地图上的玩家 key: pos
    private Map<Integer, Player> posMap = new ConcurrentHashMap<>();
    // 流寇位置信息, key:pos, value:banditId
    private Map<Integer, Integer> banditMap;
    // 点兵统领 key:pos
    private Map<Integer, CabinetLead> cabinetLeadMap;
    // 矿点位置信息, key:pos, value:mineId
    private Map<Integer, MineData> mineMap;
    // 矿点剩余资源数, key:pos, value:剩余数量
    private Map<Integer, Integer> mineResourceMap;
    // 全地图矿点驻军数据, key:pos
    private Map<Integer, Guard> guardMap = new ConcurrentHashMap<>();
    // 玩家城防驻军数据, key:pos
    private Map<Integer, List<Army>> armyMap = new ConcurrentHashMap<>();
    // 城池信息 key:pos
    private Map<Integer, City> cityMap;
    // 盖世太保的信息, key:pos, value:gestapoId
    private Map<Integer, Gestapo> gestapoMap;
    // 超级矿点会在地图上显示的, key:pos
    private Map<Integer, SuperMine> superMineMap = new ConcurrentHashMap<>();
    // 超级矿点, key:camp,val: ArrayList
    private Map<Integer, List<SuperMine>> superMineCampMap;
    // 地图上的飞艇 <pos,AirshipWorldData>
    private Map<Integer, AirshipWorldData> airshipWorldDataMap = new ConcurrentHashMap<>();
    // 所有的飞艇 ,包含刷新中的飞艇
    private List<AirshipWorldData> allAirshipWorldData;
    // 闪电战的信息, key:areaId, value:LightningWarBoss
    private Map<Integer, LightningWarBoss> lightningWarBossMap;
    // 闪电战消息推送, key:areaId(战斗开启) Or 0 (活动开启), val: cnt
    private Map<Integer, Integer> sendChatCnt;
    // 圣坛记录, key: pos
    private Map<Integer, Altar> altarMap;
    //遗迹
    private Map<Integer, RelicEntity> relicEntityMap;
    // =========================== pos 相关的 end=============================

    // =========================== block 相关 start=============================
    // 世界地图分块上的玩家，每个分块的范围为10*10, key:block
    private Map<Integer, Map<Integer, Player>> blockMap = new ConcurrentHashMap<>();
    // 按10*10分块记录矿点, key1:block, key2:pos, value:矿点id
    private Map<Integer, Map<Integer, Integer>> mineBlockMap = new ConcurrentHashMap<>();
    // 按10*10分块记录流寇, key1:block, key2:pos, value:流寇id
    private Map<Integer, Map<Integer, Integer>> banditBlockMap = new ConcurrentHashMap<>();
    // 按 10*10分块记录点兵统领
    private Map<Integer, Map<Integer, CabinetLead>> cabinetLeadBlockMap = new ConcurrentHashMap<>();
    // 按10*10分块记录盖世太保, key1:block, key2:pos, value:太保id
    private Map<Integer, Map<Integer, Gestapo>> gestapoBlockMap = new ConcurrentHashMap<>();

    // =========================== block 相关的 end=============================

    // private Map<Integer, List<City>> cityBlockMap;

    // =========================== area 相关的 start=============================
    // 行政分区信息
    private Map<Integer, Area> areaMap;
    // key: areaID_lv, 数量
    private Map<String, Integer> areaBanditMap = new ConcurrentHashMap<>();
    // 全地图行军数据，行军路线, Map<areaId, Map<armyKeyId, March>>
    private Map<Integer, Map<Integer, March>> marchMap = new ConcurrentHashMap<>();

    // =========================== area 相关的 end=============================

    // =============================玩家焦点相关 start===============================
    // 玩家对应地图区域的焦点 key:roleId val:area
    private Map<Long, Integer> focusAreaByPlayer = new ConcurrentHashMap<>();
    // 区域对应的焦点位置 key:areaId val:roleIdSet
    private Map<Integer, Set<Long>> focusByArea = new ConcurrentHashMap<>();
    // =============================玩家焦点相关 end=================================

    // 空余的位置信息
    private List<Integer> freePostList = new CopyOnWriteArrayList<>();
    // 世界任务 废弃掉了
    // private WorldTask worldTask;

    // 每个阵营玩家在皇城打流寇的数量,key:阵营, val 在要塞打的流寇的数量
    private Map<Integer, Integer> campCapitalBanditCnt = new ConcurrentHashMap<>();
    // 每个阵营玩家在皇城采集的时间, key:阵营 , val 采集的时间
    private Map<Integer, Long> campCollectTime = new ConcurrentHashMap<>();

    public void init(boolean load) {
        // 注册EventBus
        EventBus.getDefault().register(this);

        if (load) {
            for (int i = 1; i <= 2500; i++) {
                blockMap.put(i, new HashMap<Integer, Player>());
            }
        }
        this.mineBlockMap.clear();
        this.banditBlockMap.clear();
        this.areaBanditMap.clear();
        this.cabinetLeadBlockMap.clear();
        this.gestapoBlockMap.clear();

        areaMap = globalDataManager.getGameGlobal().getAreaMap();
        cityMap = globalDataManager.getGameGlobal().getCityMap();
        mineMap = globalDataManager.getGameGlobal().getMineMap();
        banditMap = globalDataManager.getGameGlobal().getBanditMap();
        mineResourceMap = globalDataManager.getGameGlobal().getMineResourceMap();
        // worldTask = globalDataManager.getGameGlobal().getWorldTask();
        cabinetLeadMap = globalDataManager.getGameGlobal().getCabinetLeadMap();
        gestapoMap = globalDataManager.getGameGlobal().getGestapoMap();
        lightningWarBossMap = globalDataManager.getGameGlobal().getLightningWarBossMap();
        sendChatCnt = globalDataManager.getGameGlobal().getSendChatCnt();
        superMineCampMap = globalDataManager.getGameGlobal().getSuperMineCampMap();
        allAirshipWorldData = globalDataManager.getGameGlobal().getAllAirshipWorldData();
        altarMap = globalDataManager.getGameGlobal().getAltarMap();
        relicEntityMap = globalDataManager.getGameGlobal().getGlobalRelic().getRelicEntityMap();
        /*-------------------------------加载一些地图block数据 start----------------------------------*/
        int block;
        Map<Integer, Integer> mineIdMap;
        for (Entry<Integer, MineData> entry : mineMap.entrySet()) {
            block = block(entry.getKey());
            mineIdMap = mineBlockMap.get(block);
            if (null == mineIdMap) {
                mineIdMap = new ConcurrentHashMap<>();
                mineBlockMap.put(block, mineIdMap);
            }
            mineIdMap.put(entry.getKey(), entry.getValue().getMineId());
        }

        Map<Integer, Integer> banditIdMap;
        StaticBandit staticBandit = null;
        String key = null;
        for (Entry<Integer, Integer> entry : banditMap.entrySet()) {
            block = block(entry.getKey());
            banditIdMap = banditBlockMap.get(block);
            if (null == banditIdMap) {
                banditIdMap = new ConcurrentHashMap<>();
                banditBlockMap.put(block, banditIdMap);
            }
            banditIdMap.put(entry.getKey(), entry.getValue());
            staticBandit = StaticBanditDataMgr.getBanditMap().get(entry.getValue());
            if (staticBandit != null) {
                int area = MapHelper.getAreaIdByPos(entry.getKey());
                key = area + "_" + staticBandit.getLv();
                Integer cnt = areaBanditMap.get(key);
                cnt = cnt != null ? cnt : 0;
                areaBanditMap.put(key, cnt + 1);
            }
        }

        LogUtil.debug("加载每个区域每个等级的流寇数=" + areaBanditMap);

        Map<Integer, CabinetLead> leadMap;
        for (Entry<Integer, CabinetLead> entry : cabinetLeadMap.entrySet()) {
            block = block(entry.getKey());
            leadMap = cabinetLeadBlockMap.get(block);
            if (null == leadMap) {
                leadMap = new ConcurrentHashMap<>();
                cabinetLeadBlockMap.put(block, leadMap);
            }
            leadMap.put(entry.getKey(), entry.getValue());
        }

        Map<Integer, Gestapo> gestapoMapTemp;
        for (Entry<Integer, Gestapo> entry : gestapoMap.entrySet()) {
            block = block(entry.getKey());
            gestapoMapTemp = gestapoBlockMap.get(block);
            if (null == gestapoMapTemp) {
                gestapoMapTemp = new ConcurrentHashMap<>();
                gestapoBlockMap.put(block, gestapoMapTemp);
            }
            gestapoMapTemp.put(entry.getKey(), entry.getValue());
        }
        /*-------------------------------加载一些地图block数据 end----------------------------------*/
//        refreshAllBandit(WorldConstant.REFRESH_TYPE_BANDIT_2);
        LogUtil.debug("init mineMap=" + mineMap);
        LogUtil.debug("init banditMap=" + banditMap);
        // 加载超级矿点
        loadSuperMine();
        loadAirship();// 初始化飞艇
    }

    public void clearRelicMap() {
        Set<Integer> tmp = new HashSet<>(relicEntityMap.size());
        relicEntityMap.values().forEach(entity -> tmp.add(entity.getPos()));
        freePostList.addAll(tmp);
        relicEntityMap.clear();
    }

    /**
     * 初始化飞艇
     */
    private void loadAirship() {
        for (AirshipWorldData aswd : this.allAirshipWorldData) {
            if (aswd.getPos() > 0) {
                this.airshipWorldDataMap.put(aswd.getPos(), aswd);
            }
        }
    }

    /**
     * 加载超级矿点
     */
    private void loadSuperMine() {
        for (Entry<Integer, List<SuperMine>> kv : superMineCampMap.entrySet()) {
            List<SuperMine> mineList = kv.getValue();
            if (!CheckNull.isEmpty(mineList)) {
                for (SuperMine sm : mineList) {
                    SuperMine.SEQ_ID++;
                    if (sm.isMapShow()) {
                        superMineMap.put(sm.getPos(), sm);
                    }
                }
            }
        }
    }

    /**
     * 初始化闪电战boss
     *
     * @param lightningWar
     */
    public void initLightningWarBoss(StaticLightningWar lightningWar) {
        for (StaticCity staticCity : StaticWorldDataMgr.getMaxTypeCityByOpenArea().values()) {
            if (CheckNull.isNull(staticCity)) {
                return;
            }
            int area = staticCity.getArea();
            GameGlobal gameGlobal = globalDataManager.getGameGlobal();
            LightningWarBoss boss;
            if (CheckNull.isNull(lightningWar)) {
                LogUtil.error("闪电战未配置");
            } else {
                if (CheckNull.isNull(staticCity)) {
                    LogUtil.error("有分区的城池数据为配置, area:", area);
                } else {
                    boss = gameGlobal.getLightningWarBossMap().get(area);
                    if (CheckNull.isNull(boss)) {
                        LogUtil.error("开始生成闪电战Boss, area:", area);
                        boss = new LightningWarBoss();
                        boss.setId(staticCity.getCityId());
                        boss.setPos(staticCity.getCityPos());
                        boss.setStatus(WorldConstant.BOSS_STATUS_CALM);
                        // boss没有初始化,首次创建Fighter对象
                        if (CheckNull.isNull(boss.getFighter())) {
                            boss.setFighter(createLightningWarBossDefencer(lightningWar.getFormList()));
                        }
                        gameGlobal.getLightningWarBossMap().put(area, boss);
                    }
                }
            }
        }
        lightningWarBossMap = globalDataManager.getGameGlobal().getLightningWarBossMap();
    }

    /**
     * 初始化闪电战Boss
     *
     * @param npcForm
     * @return
     */
    public Fighter createLightningWarBossDefencer(List<CityHero> npcForm) {
        Fighter fighter = new Fighter();
        Force force;
        // 城池NPC守军
        if (!CheckNull.isEmpty(npcForm)) {
            for (CityHero cityHero : npcForm) {
                if (cityHero.getCurArm() <= 0)
                    continue;
                force = createCityNpcForce(cityHero.getNpcId(), cityHero.getCurArm());
                force.roleType = Constant.Role.CITY;
                fighter.addForce(force);
            }
        }
        return fighter;
    }

    public Force createCityNpcForce(int npcId, int count) {
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        AttrData attrData = new AttrData(npc.getAttr());
        return new Force(attrData, npc.getArmType(), count, attrData.lead, npcId, attrData.lead, npc.getLine());
    }

    /**
     * 开服后新开的区域
     */
    public void initNewWorldData() {
        globalDataManager.initNewWorldData();
        int block;
        Map<Integer, Integer> mineIdMap;
        for (Entry<Integer, MineData> entry : mineMap.entrySet()) {
            block = block(entry.getKey());
            mineIdMap = mineBlockMap.get(block);
            if (null == mineIdMap) {
                mineIdMap = new ConcurrentHashMap<>();
                mineBlockMap.put(block, mineIdMap);
            }
            mineIdMap.put(entry.getKey(), entry.getValue().getMineId());
        }

        Map<Integer, Integer> banditIdMap;
        StaticBandit staticBandit = null;
        String key = null;
        for (Entry<Integer, Integer> entry : banditMap.entrySet()) {
            block = block(entry.getKey());
            banditIdMap = banditBlockMap.get(block);
            if (null == banditIdMap) {
                banditIdMap = new ConcurrentHashMap<>();
                banditBlockMap.put(block, banditIdMap);
            }
            banditIdMap.put(entry.getKey(), entry.getValue());
            staticBandit = StaticBanditDataMgr.getBanditMap().get(entry.getValue());
            if (staticBandit != null) {
                int area = MapHelper.getAreaIdByPos(entry.getKey());
                key = area + "_" + staticBandit.getLv();
                Integer cnt = areaBanditMap.get(key);
                cnt = cnt != null ? cnt : 0;
                areaBanditMap.put(key, cnt + 1);
            }
        }
//        LogUtil.debug("new mineMap=" + mineMap);
//        LogUtil.debug("new banditMap=" + banditMap);
//        LogUtil.debug("new areaBanditMap=" + areaBanditMap);
        LogUtil.debug("new area=" + globalDataManager.getGameGlobal().getAreaMap());
    }

    /*--------------------------------------------流寇刷新规则 start----------------------------------------------------*/

    /**
     * 类型1刷新玩家周围流寇：根据配置取出玩家周围需要多少等级的流寇， 然后计算玩家周围的9宫格坐标 依次判断坐标是否是空，并生成流寇；
     * 类型2刷新世界流寇：根据配置取出每个区域需要多少等级的流寇， 然后再此区域随即并生成流寇；
     * 类型3清理并刷新世界流寇：先清理地图流寇，然后执行1，再执行2。
     *
     * @param type
     */
    public void refreshAllBandit(int type) {
        long startTime = System.currentTimeMillis();
        if (type == WorldConstant.REFRESH_TYPE_BANDIT_1) {
            LogUtil.world("------------------刷新玩家周围寇完成 start------------------");
            refreshBanditByPlayer();
            LogUtil.world("------------------刷新玩家周围寇完成 end-------------------用时:",
                    System.currentTimeMillis() - startTime);
        } else if (type == WorldConstant.REFRESH_TYPE_BANDIT_2) {
            // checkAreaBandit();
            LogUtil.world("------------------刷新世界流寇寇完成 start------------------");
            replenishBandit();
            // 新地图的流寇补刷
            // crossWorldMapDataManager.execCrossMap(cMap->{
            //     cMap.getMapEntityGenerator().initAndRefreshBandit();
            //     cMap.publishMapEvent(MapEvent.mapReload());
            // });
            LogUtil.world("------------------刷新世界流寇寇完成 end-------------------用时:",
                    System.currentTimeMillis() - startTime);
            LogUtil.world("---------每个区域每个等级的流寇数:", areaBanditMap);
        } else if (type == WorldConstant.REFRESH_TYPE_BANDIT_3) {
            LogUtil.world("------------------清除刷流寇_清除阶段  start------------------");
            // Java8Utils.syncMethodInvoke(() -> clearAllBandit());
            clearAllBandit();
            LogUtil.world("------------------清除刷流寇_清除阶段 end------------------用时:",
                    System.currentTimeMillis() - startTime);

            startTime = System.currentTimeMillis();
            LogUtil.world("------------------清除刷流寇_刷新玩家周围阶段 start------------------");
            refreshBanditByPlayer();
            LogUtil.world("------------------清除刷流寇_刷新玩家周围阶段 end------------------用时:",
                    System.currentTimeMillis() - startTime);

            startTime = System.currentTimeMillis();
            LogUtil.world("------------------清除刷流寇_补充阶段 start------------------");
            // checkAreaBandit();
            replenishBandit();
            LogUtil.world("------------------清除刷流寇_补充阶段 end-------------------用时:",
                    System.currentTimeMillis() - startTime);
            LogUtil.world("---------每个区域每个等级的流寇数", areaBanditMap);
        }
        synCleanMapRefreshChg();// 通知客户端刷新流寇
    }

    /**
     * 在玩家周围刷流寇 规则4
     *
     * @param player
     */
    public void processBanditByPlayer(Player player) {
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
        if (staticArea == null) {
            return;
        }
//        StaticBanditArea staticBanditArea = StaticBanditDataMgr.getBanditAreaMap().get(staticArea.getOpenOrder());
        StaticBanditArea staticBanditArea = this.getStaticBanditAreaByWorldProgress(staticArea.getOpenOrder());
        if (staticBanditArea == null) {
            return;
        }
        List<Integer> listPos = MapHelper.getRoundPos(player.lord.getPos(), 1);
        // 检查玩家周围空闲坐标和流寇数量
        List<Integer> emptyPos = new ArrayList<>();
        Map<Integer, Integer> lvNumBandit = new HashMap<>(); // 玩家周围流寇等级数量
        for (int newPos : listPos) {
            if (isEmptyPos(newPos)) {
                emptyPos.add(newPos);
            } else {
                Integer bandit = banditMap.get(newPos);
                if (bandit != null) {
                    Integer num = lvNumBandit.get(bandit);
                    lvNumBandit.put(bandit, (num != null ? num : 0) + 1);
                }
            }
        }
        // 没有空位置，返回
        if (emptyPos.isEmpty()) {
            return;
        }
        List<Integer> newBanditPos = new ArrayList<>();
        out:
        for (Entry<Integer, Integer> entry : staticBanditArea.getRoleBandis().entrySet()) {
            // 没有空位置，返回
            int lv = entry.getKey();// 流寇等级
            int count = entry.getValue();// 该等级流寇刷新数量
            Integer hasNum = lvNumBandit.get(lv);
            hasNum = hasNum != null ? hasNum : 0;
            // LogUtil.debug(player.lord.getLordId() + ",玩家周围流寇已有=" + hasNum + ",流寇等级=" + lv + ",需要=" + count);
            count -= hasNum;
            if (count <= 0) {
                continue;
            }
            for (int i = 0; i < count; i++) {
                List<StaticBandit> banditList = StaticBanditDataMgr.getBanditByLv(lv);
                if (CheckNull.isEmpty(banditList)) {
                    LogUtil.error("该等级没有配置流寇, lv:" + lv);
                    break;
                }
                // 在同等级中随机一个流寇
                Integer newPos = emptyPos.get(RandomHelper.randomInSize(emptyPos.size()));
                emptyPos.remove(newPos);
                if (!isEmptyPos(newPos)) {
                    continue;
                }
                StaticBandit sBandit = banditList.get(RandomHelper.randomInSize(banditList.size()));
                // 地图添加流寇，移除空位
                newBanditPos.add(newPos);
                Java8Utils.syncMethodInvoke(() -> addBandit(newPos, sBandit));
                LogUtil.debug("roleId:", player.lord.getLordId(), ", 玩家周围生成流寇pos:" + MapHelper.reducePos(newPos),
                        ",流寇等级:" + sBandit.getBanditId());
                // 没有空位置，返回
                if (emptyPos.isEmpty()) {
                    break out;
                }
            }
        }
        // 同步地图数据给客户端
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(newBanditPos, Events.AreaChangeNoticeEvent.CLEAR_CACHE_TYPE));
    }

    public StaticBanditArea getStaticBanditAreaByWorldProgress(int areaOrder) {
        int currWorldSchedule = worldScheduleService.getCurrentSchduleId();
        List<StaticBanditArea> staticBanditAreaList = StaticBanditDataMgr.getStaticBanditAreaByAreaOrder(areaOrder);
        if (!CheckNull.isEmpty(staticBanditAreaList)) {
            return staticBanditAreaList.stream()
                    .filter(o -> o.getSchedule().get(0) <= currWorldSchedule && o.getSchedule().get(1) >= currWorldSchedule)
                    .findFirst().orElse(null);
        }
        return null;
    }

    /**
     * 在玩家点位maxRadius范围内创建一个叛军
     *
     * @param maxRadius
     * @param banditLv
     * @param player
     * @return
     */
    public int refreshOneBanditByPlayer(int maxRadius, int banditLv, Player player) {
        if (maxRadius < 1) return -1;

        List<StaticBandit> banditList = StaticBanditDataMgr.getBanditByLv(banditLv);
        if (CheckNull.isEmpty(banditList)) return -1;
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
        if (staticArea == null) return -1;
        StaticBanditArea staticBanditArea = this.getStaticBanditAreaByWorldProgress(staticArea.getOpenOrder());
        if (CheckNull.isNull(staticBanditArea)) return -1;

        int randomPos = shufflePosInRadius(maxRadius, player.lord.getPos());
        if (randomPos == -1) return randomPos;

        StaticBandit sBandit = banditList.get(RandomHelper.randomInSize(banditList.size()));
        addBandit(randomPos, sBandit);
        return randomPos;
    }

    /**
     * 在一定范围内随机点位
     *
     * @param maxRadius
     * @param playerPos
     * @return
     */
    public int shufflePosInRadius(int maxRadius, int playerPos) {
        List<Integer> emptyPos = null;
        // 先在一半区域里找
        List<Integer> posList = MapHelper.getRoundPos(playerPos, maxRadius / 2);
        if (CheckNull.nonEmpty(posList)) {
            emptyPos = posList.stream().filter(pos -> isEmptyPos(pos)).collect(Collectors.toList());
            if (CheckNull.isEmpty(emptyPos)) {
                // 一半区域里没位置则在一半以外的区域找
                posList = MapHelper.getExcludedRoundPos(playerPos, maxRadius, maxRadius / 2);
                emptyPos = posList.stream().filter(pos -> isEmptyPos(pos)).collect(Collectors.toList());
            }
        } else {
            // 一半区域没有pos, 在一半以外里找
            posList = MapHelper.getExcludedRoundPos(playerPos, maxRadius, maxRadius / 2);
            if (CheckNull.nonEmpty(posList))
                emptyPos = posList.stream().filter(pos -> isEmptyPos(pos)).collect(Collectors.toList());
        }
        if (CheckNull.isEmpty(emptyPos)) return -1;
        if (emptyPos.size() == 1) return emptyPos.get(0);
        return emptyPos.get(RandomHelper.randomInSize(emptyPos.size()));
    }

    /**
     * 点位离玩家点位最近的pos
     *
     * @param posList
     * @param playerPos
     * @return
     */
    public int nearestPos(List<Integer> posList, int playerPos) {
        if (CheckNull.isEmpty(posList)) return -1;
        if (posList.size() == 1) return posList.get(0);

        int nearestPos = -1;
        int minDistance = Integer.MAX_VALUE;
        for (int pos : posList) {
            int distance = MapHelper.calcDistance(playerPos, pos);
            if (minDistance > distance) {
                minDistance = distance;
                nearestPos = pos;
            }
        }

        return nearestPos;
    }

    /**
     * 刷新玩家周围的流寇
     */
    public void refreshBanditByPlayer() {
        Map<Integer, StaticBandit> banditMapTmp = new HashMap<>();
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        Player player;
        // 在每个玩家基地坐标±1范围内（9格），按配置补充刷新。位置不足，补充部分。
        // int pos;
        int lv;
        int count;
        List<Integer> emptyPos; // 玩家周围空闲坐标
        Map<Integer, Integer> lvNumBandit; // 玩家周围流寇等级数量
        List<StaticBandit> banditList;
        while (iterator.hasNext()) {
            player = iterator.next();
            if (player.lord.getPos() < 0 || player.lord.getArea() < 0 || !player.isLogin) {
                // 新号不处理
                continue;
            }
            StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
            if (staticArea == null) {
                continue;
            }

            //根据当前世界进程，获取StaticBanditArea
//            int currWorldSchedule =worldScheduleService.getCurrentSchduleId();
//            List<StaticBanditArea> staticBanditAreaList = StaticBanditDataMgr.getStaticBanditAreaByAreaOrder(staticArea.getOpenOrder());
//            StaticBanditArea staticBanditArea = staticBanditAreaList.stream().filter(o -> o.getSchedule().get(0) <= currWorldSchedule && o.getSchedule().get(1) >= currWorldSchedule).findFirst().get();
            StaticBanditArea staticBanditArea = this.getStaticBanditAreaByWorldProgress(staticArea.getOpenOrder());
//            StaticBanditArea staticBanditArea = StaticBanditDataMgr.getBanditAreaMap().get(staticArea.getOpenOrder());
            if (staticBanditArea == null) {
                continue;
            }
            List<Integer> listPos = MapHelper.getRoundPos(player.lord.getPos(), 1);
            // 检查玩家周围空闲坐标和流寇数量
            emptyPos = new ArrayList<>();
            lvNumBandit = new HashMap<>();
            for (int newPos : listPos) {
                if (isEmptyPos(newPos)) {
                    emptyPos.add(newPos);
                } else {
                    Integer bandit = banditMap.get(newPos);
                    if (bandit != null) {
                        Integer num = lvNumBandit.get(bandit);
                        lvNumBandit.put(bandit, (num != null ? num : 0) + 1);
                        LogUtil.world(player.lord.getLordId(), ",玩家周围发现流寇pos=", MapHelper.reducePos(newPos), ",流寇等级=",
                                bandit);
                    }
                }
            }

            // 没有空位置，返回
            if (emptyPos.isEmpty()) {
                continue;
            }
            out:
            for (Entry<Integer, Integer> entry : staticBanditArea.getRoleBandis().entrySet()) {
                // 没有空位置，返回
                if (emptyPos.isEmpty()) {
                    break;
                }
                lv = entry.getKey();// 流寇等级
                count = entry.getValue();// 该等级流寇刷新数量
                Integer hasNum = lvNumBandit.get(lv);
                hasNum = hasNum != null ? hasNum : 0;
                LogUtil.world(player.lord.getLordId(), ",玩家周围流寇已有=", hasNum, ",流寇等级=", lv, ",需要=", count);
                count -= hasNum;
                if (count <= 0) {
                    continue;
                }
                for (int i = 0; i < count; i++) {
                    banditList = StaticBanditDataMgr.getBanditByLv(lv);
                    if (CheckNull.isEmpty(banditList)) {
                        LogUtil.error("该等级没有配置流寇, lv:" + lv);
                        break;
                    }
                    // 在同等级中随机一个流寇
                    Integer newPos = emptyPos.get(RandomHelper.randomInSize(emptyPos.size()));
                    emptyPos.remove(newPos);
                    if (!isEmptyPos(newPos)) {
                        continue;
                    }
                    StaticBandit sBandit = banditList.get(RandomHelper.randomInSize(banditList.size()));
                    // 地图添加流寇
                    banditMapTmp.put(newPos, sBandit);
                    // 没有空位置，返回
                    if (emptyPos.isEmpty()) {
                        break out;
                    }
                }
            }
        }
        // 统一添加
        Java8Utils.syncMethodInvoke(() -> addAllBandit(banditMapTmp));
    }

    /**
     * 补充刷新流寇
     */
    private void replenishBandit() {
        Map<Integer, StaticBandit> banditMapTmp = new HashMap<>();
        StaticWorldDataMgr.getAreaMap().values().stream().filter(sa -> sa.isOpen()).forEach(sa -> {
            final int area = sa.getArea();
//            StaticBanditArea sba = StaticBanditDataMgr.getBanditAreaMap().get(sa.getOpenOrder());
            StaticBanditArea sba = this.getStaticBanditAreaByWorldProgress(sa.getOpenOrder());
            if (sba == null) return;
            List<Integer> blocks = sba.getBlock();
            int blockSize = blocks.size();
            Map<Integer, Integer> banditsLv = sba.getBandits();
            // 计算每个小区块需配置的数量
            Map<Integer, Integer> sLvAndCnt = new HashMap<>();
            for (Entry<Integer, Integer> kv : banditsLv.entrySet()) {
                int lv = kv.getKey();
                int cnt = kv.getValue();
                int cntByBlock = cnt / blockSize;
                sLvAndCnt.put(lv, cntByBlock);
            }
            // 每个块的逻辑处理
            for (Integer b : blocks) {
                int mapBlock = MapHelper.areaBlockToMapBlock(area, b);
                Map<Integer, Integer> mb = banditBlockMap.get(mapBlock);// pos:BanditId

                // 统计某块区域的数据
                Map<Integer, Integer> lvAndCnt = new HashMap<>();
                if (!CheckNull.isEmpty(mb)) {
                    mb.values().forEach(banditId -> {
                        StaticBandit sBandit = StaticBanditDataMgr.getBanditById(banditId);
                        if (sBandit != null) {
                            int lv = sBandit.getLv();
                            Integer cnt = lvAndCnt.get(lv);
                            cnt = cnt == null ? 0 : cnt;
                            lvAndCnt.put(lv, cnt + 1);
                        }
                    });
                }
                // 计算差值
                for (Entry<Integer, Integer> pm : sLvAndCnt.entrySet()) {
                    int lv = pm.getKey();
                    int cnt = pm.getValue();// 配置数据
                    Integer realCnt = lvAndCnt.get(lv);
                    realCnt = realCnt == null ? 0 : realCnt; // 地图上数据
                    int needCnt = cnt - realCnt;
//                    LogUtil.world("----------刷流寇  区域:", area, ", 大地图的块:", mapBlock, ", 本区域块:", b, ", 流寇等级:", lv,
//                            ", 配置数量:", cnt, ", 地图上的数量:", realCnt, ", 需要刷的数量:", needCnt, "----------");
                    if (needCnt > 0) {
                        List<Integer> emptyPos = randomEmptyByAreaBlock(area, b, needCnt);
                        emptyPos.forEach(pos -> {
                            List<StaticBandit> banditList = StaticBanditDataMgr.getBanditByLv(lv);
                            if (!CheckNull.isEmpty(banditList)) {
                                // 在同等级中随机一个流寇
                                StaticBandit sBandit = banditList.get(RandomHelper.randomInSize(banditList.size()));
                                banditMapTmp.put(pos, sBandit);
                                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
//                                LogUtil.world("---------- 新增流寇 位置:", pos, " [x=", xy.getA(), ",y=", xy.getB(), "] lv:",
//                                        sBandit.getLv(), "----------");
                            }
                        });
                    }
                }
            }
        });
        Java8Utils.syncMethodInvoke(() -> addAllBandit(banditMapTmp));
    }

    /**
     * 清理所有的流寇
     */
    public void clearAllBandit() {
        freePostList.addAll(banditMap.keySet());
        banditBlockMap.clear();
        banditMap.clear();
        areaBanditMap.clear();
    }

    private void addAllBandit(Map<Integer, StaticBandit> banditMap) {
        banditMap.forEach((pos, sBandit) -> addBandit(pos, sBandit));
    }

    /**
     * 刷流寇是添加流寇
     *
     * @param pos
     * @param sBandit
     */
    private void addBandit(int pos, StaticBandit sBandit) {
        if (sBandit != null && isEmptyPos(pos)) {
            int banditId = sBandit.getBanditId();
            putBandit4Block(pos, banditId);
            int area = MapHelper.getAreaIdByPos(pos);
            int lv = sBandit.getLv();
            String key = area + "_" + lv;
            Integer cnt = areaBanditMap.get(key);
            cnt = cnt != null ? cnt : 0;
            areaBanditMap.put(key, cnt + 1);
        }
    }

    /*--------------------------------------------流寇刷新规则 start----------------------------------------------------*/

    /*--------------------------------------地图刷矿点start--------------------------------------*/

    public void refreshAllMine(int type) {
        if (type == WorldConstant.REFRESH_TYPE_MINE_1) {// 规则1 补充刷新
            LogUtil.world("----------------补充刷新矿点  start -------------------");
            replenishMine();
            // crossWorldMapDataManager.execCrossMap(cMap->{
            //     cMap.getMapEntityGenerator().initAndRefreshMine();
            //     cMap.publishMapEvent(MapEvent.mapReload());
            // });
            LogUtil.world("----------------补充刷新矿点 end-------------------");
            synCleanMapRefreshChg();
        } else if (type == WorldConstant.REFRESH_TYPE_MINE_2) {
            LogUtil.world("----------------清除刷新矿点 start -------------------");
            // 此处还是交给主线程算吧,移除时还需要判断
            Java8Utils.syncMethodInvoke(() -> {
                clearAllMine();
                replenishMine();
            });
            LogUtil.world("----------------清除刷新矿点  end-------------------");
            synCleanMapRefreshChg();
        }
    }

    /**
     * 补充矿点
     */
    private void replenishMine() {
        Map<Integer, StaticMine> mineMapTmp = new HashMap<>();
        // 缺省补齐
        StaticWorldDataMgr.getAreaMap().values().stream().filter(sa -> sa.isOpen()).forEach(sa -> {
            final int area = sa.getArea();
            List<StaticAreaRange> rangeList = StaticWorldDataMgr.getBlockRangeMap().get(sa.getOpenOrder());
            if (!CheckNull.isEmpty(rangeList)) {
                rangeList.forEach(sar -> {
                    List<Integer> blocks = sar.getBlock();
                    int blockSize = blocks.size();
                    Map<Integer, Integer> mineLv = sar.getMineLv();
                    // 计算每个小区块需配置的数量
                    Map<Integer, Integer> sLvAndCnt = new HashMap<>();
                    for (Entry<Integer, Integer> kv : mineLv.entrySet()) {
                        int lv = kv.getKey();
                        int cnt = kv.getValue();
                        int cntByBlock = cnt / blockSize;
                        sLvAndCnt.put(lv, cntByBlock);
                    }
                    // 每个块的逻辑处理
                    for (Integer b : blocks) {
                        int mapBlock = MapHelper.areaBlockToMapBlock(area, b);
                        Map<Integer, Integer> mb = mineBlockMap.get(mapBlock);// pos:mineId

                        // 统计某块区域的数据
                        Map<Integer, Integer> lvAndCnt = new HashMap<>();
                        if (!CheckNull.isEmpty(mb)) {
                            mb.values().forEach(mineId -> {
                                StaticMine sMine = StaticWorldDataMgr.getMineMap().get(mineId);
                                if (sMine != null) {
                                    int lv = sMine.getLv();
                                    Integer cnt = lvAndCnt.get(lv);
                                    cnt = cnt == null ? 0 : cnt;
                                    lvAndCnt.put(lv, cnt + 1);
                                }
                            });
                        }
                        // 计算差值
                        for (Entry<Integer, Integer> pm : sLvAndCnt.entrySet()) {
                            int lv = pm.getKey();
                            int cnt = pm.getValue();// 配置数据
                            Integer realCnt = lvAndCnt.get(lv);
                            realCnt = realCnt == null ? 0 : realCnt; // 地图上数据
                            int needCnt = cnt - realCnt;
                            LogUtil.world("----------刷矿点  区域:", area, ", 大地图的块:", mapBlock, ", 本区域块:", b, ", 矿点等级:", lv,
                                    ", 配置数量:", cnt, ", 地图上的数量:", realCnt, ", 需要刷的数量:", needCnt, "----------");
                            if (needCnt > 0) {
                                List<Integer> emptyPos = randomEmptyByAreaBlock(area, b, needCnt);
                                emptyPos.forEach(pos -> {
                                    StaticMine mine = MineLvRandom.randomMineByLv(lv);
                                    if (mine != null) {
                                        mineMapTmp.put(pos, mine);
                                        Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                                        LogUtil.world("---------- 新增矿点 位置:", pos, " [x=", xy.getA(), ",y=", xy.getB(),
                                                "] lv:", mine.getLv(), ", mineType:", mine.getMineType(), "----------");
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
        Java8Utils.syncMethodInvoke(() -> addAllMine(mineMapTmp)); // 在主线程去更新
    }

    /**
     * 获取某个块空余随机的点
     *
     * @param area
     * @param block 这里的分块不是大地图中的分块id，而是相对本分区从1开始计算的分块，范围1-100
     * @param cnt   预计需要的空点
     * @return
     */
    public List<Integer> randomEmptyByAreaBlock(int area, int block, int cnt) {
        List<Integer> emptyPosLs = new ArrayList<>();
        List<Integer> posList = MapHelper.getPosListByAreaBlock(area, block);
        posList = posList.stream().filter(this::isEmptyPos).collect(Collectors.toList()); // 排除已用点
        if (!CheckNull.isEmpty(posList)) {
            Collections.shuffle(posList);
            int size = Math.min(cnt, posList.size());
            for (Integer pos : posList) {
                if (size-- == 0)
                    break;
                emptyPosLs.add(pos);
            }
        }
        return emptyPosLs;
    }

    /**
     * 获取某个城池的空点位置
     *
     * @param sCity
     * @param cnt   预计需要的空点
     * @return
     */
    public List<Integer> randomEmptyByKingCityPos(StaticCity sCity, int cnt) {
        if (sCity.getArea() == WorldConstant.AREA_TYPE_13) {
            List<Integer> emptyPos = MapHelper.posInKingCityList(sCity.getCityPos()).stream().filter(this::isEmptyPos)
                    .distinct().collect(Collectors.toList());
            Collections.shuffle(emptyPos);
            return emptyPos.stream().limit(cnt).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private void addAllMine(Map<Integer, StaticMine> mineMapTmp) {
        mineMapTmp.forEach((pos, sMine) -> {
            addMine(pos, sMine);
        });
        LogUtil.world("-----------所有矿点生产完成 ----------");
    }

    /**
     * 添加矿点
     *
     * @param pos
     * @param mine
     */
    private void addMine(int pos, StaticMine mine) {
        if (mine != null && isEmptyPos(pos)) {
            mineMap.put(pos, new MineData(mine.getMineId())); // 资源
            mineResourceMap.put(pos, mine.getResource());
            int block = block(pos);
            Map<Integer, Integer> blockMine = mineBlockMap.get(block);
            if (blockMine == null) {
                blockMine = new ConcurrentHashMap<>();
                mineBlockMap.put(block, blockMine);
            }
            blockMine.put(pos, mine.getMineId());
            freePostList.remove(Integer.valueOf(pos));// 空闲列表中移除空点
        }
    }

    public void gmClearAllMine() {
        freePostList.addAll(mineMap.keySet());
        mineResourceMap.clear();
        mineBlockMap.clear();
        mineMap.clear();
    }

    /**
     * 移除全部矿点没有被采集的矿点
     */
    public void clearAllMine() {
        Set<Integer> keySet = mineMap.keySet();
        Set<Integer> posSet = new HashSet<>();
        posSet.addAll(keySet);
        posSet.forEach(pos -> {
            checkGuardAndRemoveMine(pos);
        });
        LogUtil.world("----------所有矿点移除完成 ----------");
    }

    /**
     * 检测被移除的矿点是否正在被采集
     *
     * @param pos
     * @return true 表示移除成功 false表示移除失败,改矿点正在被采集
     */
    private boolean checkGuardAndRemoveMine(int pos) {
        if (!guardMap.containsKey(pos)) { // 如果正在被采集就不删除
            removeMine(pos);
            return true;
        }
        return false;
    }

    /**
     * 直接删除矿点
     *
     * @param pos
     */
    public void removeMine(int pos) {
        //移除矿点所有战斗
        DataResource.getBean(WorldService.class).cancelMineBattle(pos, TimeHelper.getCurrentSecond(), null);
        mineMap.remove(pos);
        mineResourceMap.remove(pos);
        int block = block(pos);
        Map<Integer, Integer> mineBlock = mineBlockMap.get(block);
        if (mineBlockMap != null) {
            mineBlock.remove(pos);
        }
        freePostList.add(pos);
    }

    /*--------------------------------------地图刷矿点end----------------------------------------*/

    /*-------------------------------------- 超级矿点相关  start--------------------------------------*/

    /**
     * 新增超级矿点,都城升级才会有
     *
     * @param camp
     */
    public void addCampSuperMine(int camp) {
        City capitalCity = checkHasHome(camp);
        if (capitalCity == null)
            return;
        StaticCityDev sCityDev = StaticWorldDataMgr.getCityDev(capitalCity.getCityLv());
        if (sCityDev == null)
            return;
        List<SuperMine> myCampSuperMineList = superMineCampMap.get(camp);// 阵营的超级矿点
        if (myCampSuperMineList == null) {
            myCampSuperMineList = new ArrayList<>();
            superMineCampMap.put(camp, myCampSuperMineList);
        }
        int curSize = myCampSuperMineList.size();
        int needAdd = sCityDev.getSupermineNum() - curSize;
        if (needAdd < 1)
            return;

        Map<Integer, Integer> emptyPos = calcSuperMinePos(needAdd, camp, capitalCity, sCityDev, myCampSuperMineList);
        for (Entry<Integer, Integer> kv : emptyPos.entrySet()) {
            int cityId = kv.getValue();
            int pos = kv.getKey();
            StaticSuperMine sSm = StaticWorldDataMgr.getSuperMineRandom();
            SuperMine sm = SuperMine.newSuperMine(camp, cityId, pos, sSm);
            superMineMap.put(pos, sm);// 添加到地图
            myCampSuperMineList.add(sm); // 添加到阵营
            LogUtil.common("----------新增超级矿点 矿点信息:", sm.toString());
        }
        // 通知玩家
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(new ArrayList<>(emptyPos.keySet()),
                Events.AreaChangeNoticeEvent.MAP_TYPE));
    }

    /**
     * 刷新超级矿点 , 从重置状态 -> 生产状态
     *
     * @param sm
     * @param now
     */
    public void refreshSuperMine(SuperMine sm, int now) {
        if (sm.getState() != SuperMine.STATE_RESET)
            return;
        City capitalCity = checkHasHome(sm.getCamp());
        if (capitalCity == null)
            return;
        StaticCityDev sCityDev = StaticWorldDataMgr.getCityDev(capitalCity.getCityLv());
        int camp = sm.getCamp();
        List<SuperMine> myCampSuperMineList = superMineCampMap.get(camp);// 阵营的超级矿点
        if (myCampSuperMineList == null)
            return;
        Map<Integer, Integer> emptyPos = calcSuperMinePos(1, camp, capitalCity, sCityDev, myCampSuperMineList);
        for (Entry<Integer, Integer> kv : emptyPos.entrySet()) {
            int cityId = kv.getValue();
            int pos = kv.getKey();
            StaticSuperMine sSm = StaticWorldDataMgr.getSuperMineRandom();
            sm.setResetToProducedState(now, pos, sSm, cityId); // 修改状态
            superMineMap.put(pos, sm);// 添加到地图
            LogUtil.common("----------刷新超级矿点 矿点信息:", sm.toString());
        }
        // 通知玩家
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(new ArrayList<>(emptyPos.keySet()),
                Events.AreaChangeNoticeEvent.MAP_TYPE));
    }

    /**
     * 计算超级矿点位置,如果不够会移除流寇
     *
     * @param needAdd             需要刷的数量
     * @param camp
     * @param capitalCity         都城
     * @param sCityDev            都城开发配置
     * @param myCampSuperMineList
     * @return
     */
    private Map<Integer, Integer> calcSuperMinePos(int needAdd, int camp, City capitalCity, StaticCityDev sCityDev,
                                                   List<SuperMine> myCampSuperMineList) {
        // 自阵营占据的据点
        List<City> myCampCityList = getCityInArea(WorldConstant.AREA_TYPE_13).stream().filter(city -> {
            StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
            return city.getCamp() == camp && staticCity.getType() == WorldConstant.CITY_TYPE_8;
        }).collect(Collectors.toList());
        myCampCityList.add(capitalCity); // 加上自己的都城
        Collections.shuffle(myCampCityList);// 打乱顺序
        // 每个据点周围最多的数量
        final int maxCellCnt = (int) Math.ceil(sCityDev.getSupermineNum() * 1.0 / myCampCityList.size());

        Map<Integer, Integer> emptyPosMap = new HashMap<>(); // key:pos,
        // val:cityId
        List<StaticCity> needRmBanditCity = new ArrayList<>(); // 需要被移除的块区域
        out:
        for (City c : myCampCityList) {
            // 该据点已有的超级矿点个数
            int hasCnt = (int) myCampSuperMineList.stream().filter(sm -> sm.getCityId() == c.getCityId()).count();
            int cityNeed = maxCellCnt - hasCnt; // 需要的数量
            if (cityNeed < 1)
                continue;// 数量已经足够
            StaticCity sCity = StaticWorldDataMgr.getCityMap().get(c.getCityId());
            List<Integer> emptyList = randomEmptyByKingCityPos(sCity, cityNeed);
            if (!CheckNull.isEmpty(emptyList)) {
                for (int pos : emptyList) {
                    emptyPosMap.put(pos, c.getCityId());
                    if (--needAdd <= 0) {
                        break out; // 达到需要的值
                    }
                }
            } else {
                needRmBanditCity.add(sCity);
            }
        }
        if (needAdd > 0) { // 极端情况没有空点,移除流寇
            out:
            for (StaticCity sCity : needRmBanditCity) {
                for (int pos : MapHelper.posInKingCityList(sCity.getCityPos())) {
                    if (isBanditPos(pos)) {
                        removeBandit(pos, 0);
                        emptyPosMap.put(pos, sCity.getCityId());
                        LogUtil.world("------刷新超级矿点位置不够移除流寇  pos:", pos, ",cityId:", sCity.getCityId(),
                                "--------------");
                        if (--needAdd <= 0) {
                            break out; // 达到需要的值
                        }
                    }
                }
            }
        }
        return emptyPosMap;
    }

    /*--------------------------------------超级矿点相关 end--------------------------------------*/

    /**
     * 获取名城Buff
     *
     * @return
     */
    public double getCityBuffer(CommonPb.TwoInt cityStatus, int buffType, long roleId) {
        double buff = 0.0;
        if (CheckNull.isNull(cityStatus)) {
            return buff;
        }
        int cityId = cityStatus.getV1();
        int cityCamp = cityStatus.getV2();
        Player player = playerDataManager.getPlayer(roleId);
        if (!CheckNull.isNull(player) && cityCamp != Constant.Camp.NPC) {
            boolean isUp = player.lord.getCamp() == cityCamp;
            StaticCity sCity = StaticWorldDataMgr.getCityMap().get(cityId);
            if (!CheckNull.isNull(sCity) && isUp || buffType == WorldConstant.CityBuffer.MILITARY_RESTRICTED_ZONES) {
                // TODO: 2018-08-29 目前是没有debuff状态,后期有需要,这一块要改
                List<Integer> cityBuff = sCity.getBuff().get(0);
                if (!CheckNull.isEmpty(cityBuff)) {
                    int indexOf = cityBuff.indexOf(buffType);
                    if (indexOf >= 0) {
                        Integer buffId = cityBuff.get(indexOf);
                        final StaticCityBuffer sCityBuff = StaticWorldDataMgr.getCityBuffById(buffId);
                        if (!CheckNull.isNull(sCityBuff)) {
                            buff = sCityBuff.getBuff();
                        }
                    }
                }
            }
        }
        return buff;
    }

    /**
     * 检测玩家在皇城的哪座名城范围内
     *
     * @param pos
     * @return
     */
    public int getKingCityId(int pos) {
        int cityId = 0;
        List<StaticCity> cityList = StaticWorldDataMgr.getCityByArea(WorldConstant.AREA_TYPE_13);
        for (StaticCity sCity : cityList) {
            if (MapHelper.isInKingCityAreaPos(sCity.getCityPos(), pos)) {
                return sCity.getCityId();
            }
        }
        return cityId;
    }

    /**
     * 行军前记录名城city和所属
     *
     * @param originPos
     */
    public CommonPb.TwoInt checkCityBuffer(int originPos) {
        CommonPb.TwoInt cityStatus = null;
        if (MapHelper.getAreaIdByPos(originPos) == WorldConstant.AREA_TYPE_13) {
            int cityId = getKingCityId(originPos);
            City city = getCityById(cityId);
            StaticCity sCity = StaticWorldDataMgr.getCityMap().get(cityId);
            if (!CheckNull.isNull(city) && !CheckNull.isNull(sCity) && !CheckNull.isEmpty(sCity.getBuff())) {
                cityStatus = PbHelper.createTwoIntPb(cityId, city.getCamp());
            }
        }
        return cityStatus;
    }

    /**
     * 地图添加流寇
     *
     * @param pos
     * @param banditId
     */
    public void putBandit4Block(int pos, int banditId) {
        int block = block(pos);
        Map<Integer, Integer> banditIdMap = banditBlockMap.get(block);
        if (null == banditIdMap) {
            banditIdMap = new ConcurrentHashMap<>();
            banditBlockMap.put(block, banditIdMap);
        }
        banditIdMap.put(pos, banditId);
        freePostList.remove(Integer.valueOf(pos));
        banditMap.put(pos, banditId);
    }

    public boolean isCityPos(int pos) {
        return StaticWorldDataMgr.isCityPos(pos);
    }

    public boolean isBossPos(int pos) {
        return StaticWorldDataMgr.getBossPosSet().contains(pos);
    }

    public boolean isValidPos(int pos) {
        if (pos > 249999 || pos < 0) {
            return false;
        }
        if (isCityPos(pos)) {
            return false;
        }
        if (isBossPos(pos)) {
            return false;
        }
        return true;
    }

    /**
     * 遍历所有有效坐标，计算空余位置list后并混乱
     */
    public void calcFreePostList() {
        this.freePostList.clear();
        Map<Integer, StaticArea> worldAreaMap = StaticWorldDataMgr.getAreaMap();
        int pos = 0;
        int xBegin = 0;
        int yBegin = 0;
        Turple<Integer, Integer> xy = null;
        for (StaticArea area : worldAreaMap.values()) {
            if (!area.isOpen())
                continue;

            xy = MapHelper.reduceXYInArea(area.getArea());
            xBegin = xy.getA();
            yBegin = xy.getB();

            for (int x = 1; x <= 100; x++) {
                for (int y = 0; y < 100; y++) {
                    pos = (xBegin + x) + (yBegin + y) * 500;
                    if (!isEmptyPos(pos)) {
                        continue;
                    } else {
                        freePostList.add(pos);
                    }
                }
            }
        }

        Collections.shuffle(freePostList);
    }

    /**
     * 获取出生地的区域
     *
     * @return
     */
    public int birthArea() {
        List<StaticArea> sAreaList = StaticWorldDataMgr.getBirthArea();
        for (StaticArea sa : sAreaList) {
            Area area = areaMap.get(sa.getArea());
            if (area == null) {
                continue;
            }
            if (area.getRealPlayerNum() >= Constant.AREA_REAL_PLAER_CAPACITY) {
                continue;
            }
            return sa.getArea();
        }
        // 都满了 随机一个区域
        int size = sAreaList.size();
        return sAreaList.get(RandomHelper.randomInSize(size)).getArea();
    }

    /**
     * 给新角色分配坐标
     *
     * @param player
     * @param protect 是否分配防护罩 ,true表示分配
     */
    public void addNewPlayer(Player player, boolean protect) {
        int pos;
        int times = 0;
        int areaId = birthArea();
        LogUtil.debug("出生地区域  lordId:", player.roleId, ", areaId:", areaId);
        Turple<Integer, Integer> xy = MapHelper.reduceXYInArea(areaId);
        int xBegin = xy.getA();
        int yBegin = xy.getB();
        while (true) {
            pos = MapHelper.randomPosInArea(areaId, xBegin, yBegin);
            LogUtil.debug(player.lord.getLordId() + ",初始坐标=" + pos + ",areaId=" + areaId);
            if (!isEmptyPos(pos)) {
                times++;

                if (times >= 100) {
                    pos = freePostList.get(0);

                    if (!isEmptyPos(pos)) {
                        pos = -1;
                        break;
                    }
                    if (freePostList.size() < 3000) {
                        LogUtil.warn("空闲位置不够了,请注意, 剩余:" + freePostList.size() + ", 已分配:" + posMap.size());
                        LogUtil.error("空闲位置不够了,请注意, 剩余:" + freePostList.size() + ", 已分配:" + posMap.size());
                    }
                    break;
                }
                continue;
            }
            break;
        }
        freePostList.remove(Integer.valueOf(pos));
        if (getAreaIdByPos(pos) != areaId) {
            LogUtil.debug(player.lord.getLordId() + ",!!!错误最终初始坐标=" + pos + ",areaId=" + getAreaIdByPos(pos));
        }
        player.lord.setPos(pos);
        player.lord.setArea(getAreaIdByPos(pos));
        if (protect) {
            player.getEffect().put(EffectConstant.PROTECT, new Effect(EffectConstant.PROTECT, 0,
                    TimeHelper.getCurrentSecond() + WorldConstant.NEW_LORD_PROTECT_TIME));
        }
        LogLordHelper.commonLog("birthAreaPos", AwardFrom.COMMON, player, areaId, pos);
        putPlayer(player);
    }

    /**
     * 给机器人分配坐标
     *
     * @param robot
     * @param protect 是否分配防护罩 ,true表示分配
     */
    public void addNewRobot(Robot robot, boolean protect) {
        Player player = playerDataManager.getPlayer(robot.getRoleId());
        int posInArea = randomEmptyPosInArea(robot.getPosArea());

        if (getAreaIdByPos(posInArea) != robot.getPosArea()) {
            LogUtil.debug(
                    player.lord.getLordId() + ",!!!错误最终初始坐标=" + posInArea + ",areaId=" + getAreaIdByPos(posInArea));
        }
        player.lord.setPos(posInArea);
        player.lord.setArea(getAreaIdByPos(posInArea));
        if (protect) {
            player.getEffect().put(EffectConstant.PROTECT, new Effect(EffectConstant.PROTECT, 0,
                    TimeHelper.getCurrentSecond() + WorldConstant.NEW_LORD_PROTECT_TIME));
        }
        putPlayer(player);
    }

    /**
     * true表示可以使用,false表示坐标已被占用
     *
     * @param pos
     * @return
     */
    public boolean isEmptyPos(int pos) {
        if (!isValidPos(pos) || isPlayerPos(pos) || isMinePos(pos) || isBanditPos(pos) || isCabinetLeadPos(pos)
                || isGestapoPos(pos) || isSuperMinePos(pos) || isAirshipWorldData(pos) || isAltarPos(pos) || isRelicPos(pos)) {
            return false;
        }
        return true;
    }

    public int getSmallArea() {
        int areaId = 1;
        int min = Integer.MAX_VALUE;
        for (Area area : areaMap.values()) {
            if (area.isOpen() && area.getPlayerNum() < min) {
                min = area.getPlayerNum();
                areaId = area.getArea();
            }
        }
        return areaId;
    }

    public Map<Integer, Area> getAreaMap() {
        return areaMap;
    }

    /**
     * 获取所有开放区域
     *
     * @return 开放的区域
     */
    public List<Area> getOpenArea() {
        return areaMap.values().stream()
                .filter(area -> {
                    StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(area.getArea());
                    if (staticArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) {
                        return area.isOpen();
                    } else {
                        return area.canPass();
                    }
                }).collect(Collectors.toList());
    }

    /**
     * 随机迁城
     *
     * @param playerPos
     * @param camp
     * @return
     */
    public int randomPlayerPos(Player player, int playerPos, int camp) {
        int areaId = ramdomPlayerAreaByPos(player, playerPos);
        if (areaId == WorldConstant.AREA_TYPE_13) {
            return randomKingAreaPos(camp);
        } else {
            return randomEmptyPosInArea(areaId);
        }
    }

    /**
     * 根据玩家当前坐标，在已开分区中随机返回一个
     *
     * @param playerPos
     * @return
     */
    private int ramdomPlayerAreaByPos(Player player, int playerPos) {
        int areaId = getAreaIdByPos(playerPos);
        Area area = areaMap.get(areaId);
        if (null == area) {
            LogUtil.error("有分区的数据没有初始化, area:", area);
            return areaId;
        }

        if (RandomHelper.isHitRangeIn100(Constant.LOW_MOVE_CITY_PROBABILITY)) {
            return areaId;// 暂时先做成90%还会随机在原来的分区中
        }

        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(areaId);
        // if (!area.canPass()) {// 如果是一级分区，且没有开通下一级分区，玩家只能在当前分区
        // if (staticArea.getOpenOrder() <= WorldConstant.AREA_ORDER_1) {
        // return areaId;
        // }
        // }
        int bossDeadState = worldScheduleService.getBossDeadState();
        if (staticArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) {
            if (bossDeadState == ScheduleConstant.BOSS_NO_DEAD) {// 第一个世界boss没打完,郡城只能在本区域
                return areaId;
            }
        }
        // 第二个世界BOSS的状态
        boolean boss2IsDead = bossDeadState == ScheduleConstant.BOSS_2_DEAD; // 第二个世界BOSS是否死亡,true表示死亡

        // 该分区开通下一级后，本分区玩家可以去的所有分区
        List<Integer> unlockAreaIdList = staticArea.getUnlockArea();
        int totalWeight = staticArea.getUnlockTotalWeight();// 所有可以去的分区的总权重
        int random = RandomHelper.randomInSize(totalWeight);
        int temp = 0;
        for (Integer id : unlockAreaIdList) {
            if (!boss2IsDead && id.intValue() == WorldConstant.AREA_TYPE_13) { // 第二个世界boss没死调过13号 区域
                continue;
            }
            if (id.intValue() == WorldConstant.AREA_TYPE_13
                    && !StaticFunctionDataMgr.funcitonIsOpen(player, BuildingType.ENTER_AREA_13_COND)) { // 等级未达到也不让随机飞到13号区域
                continue;
            }
            staticArea = StaticWorldDataMgr.getAreaMap().get(id);
            temp += staticArea.getLowWeight();
            if (temp >= random && staticArea.isOpen()) {
                return staticArea.getArea();
            }
        }
        return areaId;
    }

    /**
     * 皇城区域随机迁城的逻辑
     *
     * @param camp
     * @return
     */
    public int randomKingAreaPos(int camp) {
        // 检查皇城区域有哪些城池是属于本阵营
        List<City> cityList = getCityInArea(WorldConstant.AREA_TYPE_13);
        List<City> myCampCity = cityList.stream().filter(c -> c.getCamp() == camp).collect(Collectors.toList());
        if (CheckNull.isEmpty(myCampCity)) {
            // 如果没有,就到中立区
            List<City> npcCity = cityList.stream().filter(c -> c.getCamp() == 0).collect(Collectors.toList());
            return randomKingCityListPos(npcCity);
        } else {
            return randomKingCityListPos(myCampCity);
        }
    }

    public int randomEmptyPosInRadius(StaticCity staticCity, int radius) {
        int centrePos = staticCity.getCityPos();
        List<Integer> cityPosList = StaticWorldDataMgr.getCityByArea(staticCity.getArea()).stream().map(StaticCity::getCityPos).collect(Collectors.toList());
        if (staticCity.getArea() == 13) {
            List<StaticBerlinWar> staticBerlinWarList = StaticBerlinWarDataMgr.getBerlinBattlefront();
            staticBerlinWarList.forEach(o -> cityPosList.add(o.getCityPos()));
        }
        int i = 0;
        for (; ; ) {
            i++;
            int rdmPos = MapHelper.randomPosByCentre(centrePos, radius);
            if (cityPosList.contains(rdmPos))
                continue;
            if (isEmptyPos(rdmPos) && !hasCityInRadius(rdmPos, 8, cityPosList)) {
                return rdmPos;
            }
            if (i >= 100) {
                LogUtil.warn("random empty position failure");
                return -1;
            }
        }
    }

    public boolean hasCityInRadius(int pos, int radius, List<Integer> cityPosList) {
        List<Integer> posList = MapHelper.getRoundPos0(pos, radius);
        for (Integer integer : posList) {
            if (cityPosList.contains(integer)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在指定分区内随机获取一个空闲坐标
     *
     * @param areaId 区域 1-25
     * @return
     */
    public int randomEmptyPosInArea(int areaId) {
        return randomEmptyPosInArea(areaId, 0);
    }

    /**
     * 在指定分区内随机获取一个空闲坐标
     *
     * @param areaId 区域 1-25
     * @param half   一半
     * @return 随机坐标
     */
    public int randomEmptyPosInArea(int areaId, int half) {
        int pos;
        int times = 0;
        Turple<Integer, Integer> xy = MapHelper.reduceXYInArea(areaId);
        int xBegin = xy.getA();
        int yBegin = xy.getB();
        while (true) {
            if (half > 0) {
                pos = MapHelper.randomPosHalfInArea(areaId, xBegin, yBegin, half);
            } else {
                pos = MapHelper.randomPosInArea(areaId, xBegin, yBegin);
            }
            if (!isEmptyPos(pos)) {
                times++;

                if (times >= 100) {
                    pos = freePostList.get(0);

                    if (!isEmptyPos(pos)) {
                        LogUtil.warn("空闲位置不够了,请注意, 剩余:" + freePostList.size() + ", 已分配:" + posMap.size());
                        pos = -1;
                        return pos;
                    }

                    if (freePostList.size() < 3000) {
                        LogUtil.warn("空闲位置不够了,请注意, 剩余:" + freePostList.size() + ", 已分配:" + posMap.size());
                        LogUtil.error("空闲位置不够了,请注意, 剩余:" + freePostList.size() + ", 已分配:" + posMap.size());
                    }
                    break;
                }
                continue;
            }
            break;
        }
        freePostList.remove(Integer.valueOf(pos));// 从空闲坐标中移除
        return pos;
    }

    /**
     * 皇城区域城池随机规则
     *
     * @param citys
     * @return
     */
    public int randomKingCityListPos(List<City> citys) {
        // 最多随机的系数
        if (!CheckNull.isEmpty(citys)) {
            int maxTimes = 100;
            for (int i = 0; i < maxTimes; i++) {
                // 随机出一个城池
                City city = citys.get(RandomHelper.randomInSize(citys.size()));
                StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
                for (int j = 0; j < maxTimes; j++) {
                    // 在某个城区域附件随机
                    int tempPos = MapHelper.randomPosInKingCity(staticCity.getCityPos());
                    if (isEmptyPos(tempPos)) {
                        freePostList.remove(Integer.valueOf(tempPos));// 从空闲坐标中移除
                        return tempPos;
                    }
                }
            }
        }
        // 经过一番大干之后,没有得到结果,就全区域随机
        return randomEmptyPosInArea(WorldConstant.AREA_TYPE_13);
    }

    /**
     * 服务器启动后载入世界地图中的部队、行军路线、矿点采集等
     */
    public void loadArmy() {
        for (Player player : posMap.values()) {
            if (!player.armys.isEmpty()) {
                for (Army army : player.armys.values()) {
                    addMarch(new March(player, army));

                    if (army.isCollect() && army.getType() == ArmyConstant.ARMY_TYPE_COLLECT) {//
                        addMineGuard(new Guard(player, army));
                    }
                    if (army.isGuard() && army.getType() == ArmyConstant.ARMY_TYPE_GUARD) {//
                        addPlayerGuard(army.getTarget(), army);
                    }
                }
            }
        }
    }

    public boolean isAltarPos(int pos) {
        return altarMap.containsKey(pos);
    }

    public boolean isRelicPos(int pos) {
        return relicEntityMap.containsKey(pos);
    }

    public boolean isMinePos(int pos) {
        return mineMap.containsKey(pos);
    }

    public boolean isBanditPos(int pos) {
        return banditMap.containsKey(pos);
    }

    public boolean isPlayerPos(int pos) {
        return posMap.containsKey(pos);
    }

    public boolean isCabinetLeadPos(int pos) {
        return cabinetLeadMap.containsKey(pos);
    }

    public Map<Integer, CabinetLead> getCabinetLeadMap() {
        return cabinetLeadMap;
    }

    public boolean isGestapoPos(int pos) {
        return gestapoMap.containsKey(pos);
    }

    public boolean isSuperMinePos(int pos) {
        if (superMineMap != null) {
            return superMineMap.containsKey(pos);
        }
        return false;
    }

    public boolean isAirshipWorldData(int pos) {
        if (airshipWorldDataMap != null) {
            return airshipWorldDataMap.containsKey(pos);
        }
        return false;
    }

    public boolean hasGuard(int pos) {
        return guardMap.containsKey(pos);
    }

    public StaticMine getMineByPos(int pos) {
        MineData mineId = mineMap.get(pos);
        if (null == mineId)
            return null;

        return StaticWorldDataMgr.getMineMap().get(mineId.getMineId());
    }

    public Map<Integer, MineData> getMineMap() {
        return mineMap;
    }

    public void addCollectMine(Long lordId, int pos) {
        MineData mineId = mineMap.get(pos);
        if (null == mineId)
            return;

        mineId.addCollectTeam(lordId);
    }

    public void removeCollectMineTeam(Long lordId, int pos) {
        MineData mineId = mineMap.get(pos);
        if (null == mineId)
            return;

        mineId.removeCollectTeam(lordId);
    }

    public CabinetLead getCabinetLeadByPos(int pos) {
        return cabinetLeadMap.get(pos);
    }

    public int getBanditIdByPos(int pos) {
        return banditMap.getOrDefault(pos, 0);
    }

    public Guard getGuardByPos(int pos) {
        return guardMap.get(pos);
    }

    public Map<Integer, Guard> getMineGuardMap() {
        return guardMap;
    }

    public void addMineGuard(Guard guard) {
        guardMap.put(guard.getPos(), guard);
    }

    public void removeMineGuard(int pos) {
        guardMap.remove(pos);
    }

    public int getMineResource(int pos) {
        Integer resource = mineResourceMap.get(pos);
        return null == resource ? 0 : resource;
    }

    public void removeMineResource(int pos) {
        mineResourceMap.remove(pos);
    }

    public void putMineResource(int pos, int resource) {
        mineResourceMap.put(pos, resource);
    }

    public Map<Integer, SuperMine> getSuperMineMap() {
        return superMineMap;
    }

    public Map<Integer, List<SuperMine>> getSuperMineCampMap() {
        return superMineCampMap;
    }

    /**
     * 添加行军路线，行军路线的起点和终点会被分别记录，如果起点和终点不在不一个行政区域内，将被记录两遍
     *
     * @param march
     */
    public void addMarch(March march) {
        // int startArea = getAreaIdByPos(march.getStartPos());
        List<Integer> areaList = MapHelper.getLineAcorss(march.getStartPos(), march.getTargetPos());
        for (Integer area : areaList) {
            Map<Integer, March> marchAreaMap = marchMap.get(area);
            if (null == marchAreaMap) {
                marchAreaMap = new ConcurrentHashMap<>();
                marchMap.put(area, marchAreaMap);
            }
            marchAreaMap.put(march.getArmy().getKeyId(), march);
            LogUtil.debug("=====添加行军路线", ",startPos:", march.getStartPos(), " endPos:", march.getTargetPos(),
                    " 到 area:", area);
            // 荣耀日报 行军时间添加
            honorDailyDataManager.addAndCheckHonorReport2s(march.getPlayer(), HonorDailyConstant.COND_ID_11,
                    march.getArmy().getDuration());
        }
    }

    /**
     * 删除行军路线记录
     *
     * @param player
     * @param army
     */
    public void removeMarch(Player player, Army army) {
        for (Integer area : MapHelper.AREA_MAP) {
            Map<Integer, March> marchAreaMap = marchMap.get(area);
            if (null != marchAreaMap && marchAreaMap.containsKey(army.getKeyId())) {
                marchAreaMap.remove(army.getKeyId());
            }
        }
    }

    public Map<Integer, Map<Integer, March>> getMarchMap() {
        return marchMap;
    }

    /**
     * 获取一个行政区域内的所有行军路线
     *
     * @param area
     * @return
     */
    public Map<Integer, March> getMarchInArea(int area) {
        return marchMap.get(area);
    }

    /**
     * 计算坐标所属的行政分区
     *
     * @param pos
     * @return
     */
    public static int getAreaIdByPos(int pos) {
        return MapHelper.getAreaIdByPos(pos);
    }

    /**
     * 计算坐标所属的客户端10*10的拉区分块
     *
     * @param pos
     * @return
     */
    public static int block(int pos) {
        return MapHelper.block(pos);
    }

    public static Turple<Integer, Integer> reducePos(int pos) {
        return MapHelper.reducePos(pos);
    }

    public static Set<Integer> getBlockInArea(int area) {
        return MapHelper.getBlockInArea(area);
    }

    private Set<Player> errorLordPos = new HashSet<>();

    public void rmPlayer(Player player) {
        if (player == null) {
            return;
        }
        int pos = player.lord.getPos();
        Area area;
        if (pos != -1) {
            if (!isValidPos(pos)) {
                LogUtil.error("错误 pos " + pos + "  lordId= " + player.lord.getLordId() + " 已搬迁 ");
                errorLordPos.add(player);
                return;
            }
            posMap.remove(pos);
            blockMap.get(block(pos)).remove(pos);
            freePostList.add(Integer.valueOf(pos));

            LogUtil.debug("玩家移除块=" + block(pos) + "new area=" + getAreaIdByPos(pos));

            area = areaMap.get(getAreaIdByPos(pos));
            if (null == area) {
                LogUtil.error("有分区的数据没有初始化, area:", area, "pos:", pos);
            } else {
                area.subPlayerNum(player.isRobot);
            }
        }
    }

    public void putPlayer(Player player) {
        int pos = player.lord.getPos();
        Area area;
        if (pos != -1) {
            // if (!isValidPos(pos)) {
            // LogUtil.error("错误 pos " + pos + " lordId= " + player.lord.getLordId() + " 已搬迁 ");
            // errorLordPos.add(player);
            // return;
            // }
            posMap.put(pos, player);
            blockMap.get(block(pos)).put(pos, player);
            freePostList.remove(Integer.valueOf(pos));

            LogUtil.debug("玩家加入新块=" + block(pos) + "new area=" + getAreaIdByPos(pos));

            area = areaMap.get(getAreaIdByPos(pos));
            if (null == area) {
                LogUtil.error("有分区的数据没有初始化, area:", getAreaIdByPos(pos), "pos:", pos);
            } else {
                playerDataManager.putAreaPlayer(player);
                area.addPlayerNum(player.isRobot);// 记录该分区中的玩家数量
            }
        }
    }

    public Area getAreaByAreaId(int area) {
        return areaMap.get(area);
    }

    public void removePlayerPos(int pos, Player player) {
        posMap.remove(pos);
        if (blockMap.get(block(pos)) != null) {
            blockMap.get(block(pos)).remove(pos);
        }
        freePostList.add(pos);
        playerDataManager.removeAreaPlayer(pos, player);

        Area area = areaMap.get(MapHelper.getAreaIdByPos(pos));
        if (null != area) {
            area.subPlayerNum(player.isRobot);// 记录该分区中的玩家数量
        }
    }

    /**
     * 获取某个行政分区中的所有玩家
     *
     * @param area
     * @return
     */
    public List<Player> getPlayerInAreaByBlock(int area) {
        List<Player> list = new ArrayList<>();
        Iterator<Player> it;
        for (Integer block : getBlockInArea(area)) {
            it = blockMap.get(block).values().iterator();
            while (it.hasNext()) {
                list.add(it.next());
            }
        }
        return list;
    }

    public List<Player> getPlayerInArea(int area) {
        List<Player> list = new ArrayList<>();
        // Iterator<Player> it;
        // for (Integer block : getBlockInArea(area)) {
        // it = blockMap.get(block).values().iterator();
        // while (it.hasNext()) {
        // list.add(it.next());
        // }
        // }
        // LogUtil.debug("area=" + area + ",block=" + getBlockInArea(area));
        Map<Long, Player> map = playerDataManager.getPlayerByArea(area);
        if (map != null && !map.isEmpty()) {
            list.addAll(map.values());
        }
        return list;
    }

    /**
     * 获取某个10*10分块中的所有玩家
     *
     * @param block
     * @return
     */
    public List<Player> getPlayerInBlock(int block) {
        List<Player> list = new ArrayList<>();
        Map<Integer, Player> blockPlayer = blockMap.get(block);
        if (!CheckNull.isEmpty(blockPlayer)) {
            Iterator<Player> it = blockPlayer.values().iterator();
            while (it.hasNext()) {
                list.add(it.next());
            }
        }
        return list;
    }

    /**
     * 获取某个10*10分块中的所有
     *
     * @param block
     * @return
     */
    public Map<Integer, Gestapo> getGestapoInBlock(int block) {
        return gestapoBlockMap.get(block);
    }

    /**
     * 获取某个10*10分块中的所有点兵统领
     *
     * @param block
     * @return
     */
    public Map<Integer, CabinetLead> getCabinetLeadInBlock(int block) {
        return cabinetLeadBlockMap.get(block);
    }

    /**
     * 获取某个10*10分块中的所有流寇信息
     *
     * @param block
     * @return
     */
    public Map<Integer, Integer> getBanditInBlock(int block) {
        return banditBlockMap.get(block);
    }

    /**
     * 获取某个10*10分块中的所有矿点信息
     *
     * @param block
     * @return
     */
    public Map<Integer, Integer> getMineInBlock(int block) {
        return mineBlockMap.get(block);
    }

    public void rmMineInBlock(int block, int pos) {
        Map<Integer, Integer> map = getMineInBlock(block);
        if (map != null) {
            map.remove(pos);
        }
    }

    public Player getPosData(int pos) {
        return posMap.get(pos);
    }

    public City getCityById(int cityId) {
        return cityMap.get(cityId);
    }

    public Map<Integer, City> getCityMap() {
        return cityMap;
    }

    public List<City> getCityInArea(int areaId) {
        List<City> cityList = new ArrayList<>();
        List<StaticCity> list = StaticWorldDataMgr.getCityByArea(areaId);
        if (!CheckNull.isEmpty(list)) {
            City city;
            for (StaticCity staticCity : list) {
                city = getCityById(staticCity.getCityId());
                cityList.add(city);
            }
        }
        return cityList;
    }

    /**
     * 根据阵营获取名城数
     *
     * @param camp
     * @return
     */
    public int getPeoPle4MiddleCity(int camp) {
        int cnt = 0;
        List<City> cityList = getCityInArea(WorldConstant.AREA_TYPE_13);
        if (cityList != null) {
            for (City city : cityList) {
                StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
                if (city.getCamp() == camp && staticCity.getType() == WorldConstant.CITY_TYPE_8) {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    public int getCityTypeNum4CampAndArea(int area, int camp, int type) {
        if (area < 0) {
            return 0;
        }
        int cnt = 0;
        for (City city : getCityInArea(area)) {
            StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
            if (city.getCamp() == camp && (type == 0 || staticCity.getType() == type)) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * 是否有该阵营的都城
     *
     * @param camp
     * @return
     */
    public City checkHasHome(int camp) {
        City homeCity = getCityById(WorldConstant.HOME_CITY_1);
        if (homeCity != null && homeCity.getCamp() == camp) {
            return homeCity;
        }
        homeCity = getCityById(WorldConstant.HOME_CITY_2);
        if (homeCity != null && homeCity.getCamp() == camp) {
            return homeCity;
        }
        homeCity = getCityById(WorldConstant.HOME_CITY_3);
        if (homeCity != null && homeCity.getCamp() == camp) {
            return homeCity;
        }
        homeCity = getCityById(WorldConstant.HOME_CITY_4);
        if (homeCity != null && homeCity.getCamp() == camp) {
            return homeCity;
        }
        return null;
    }

    /**
     * 杀死怪后删除数据
     *
     * @param pos
     * @param type 0 流寇 1 点兵统领 2 盖世太保
     */
    public void removeBandit(int pos, int type) {
        int block = block(pos);
        if (0 == type) {
            if (banditBlockMap.get(block) != null) {
                banditBlockMap.get(block).remove(Integer.valueOf(pos));
            }
            banditMap.remove(Integer.valueOf(pos));
        } else if (1 == type) {
            if (cabinetLeadBlockMap.get(block) != null) {
                cabinetLeadBlockMap.get(block).remove(Integer.valueOf(pos));
            }
            cabinetLeadMap.remove(Integer.valueOf(pos));
        } else if (2 == type) {
            if (gestapoBlockMap.get(block) != null) {
                gestapoBlockMap.get(block).remove(Integer.valueOf(pos));
            }
            gestapoMap.remove(Integer.valueOf(pos));
        }
        freePostList.add(Integer.valueOf(pos)); // 腾出空位

    }

    /**
     * 根据玩家刷点兵统领
     *
     * @param player
     * @param staticCabinetPlan
     */
    public void refreshCabinetLead(Player player, StaticCabinetPlan staticCabinetPlan) {
        int npcCount = staticCabinetPlan.getNpcCount();
        if (npcCount <= 0) {
            return;
        }
        int radius = 3; // 默认是7*7、
        List<Integer> availablePos = new ArrayList<>();
        List<Integer> freePos = new ArrayList<>(); // 空位
        while (true) {
            List<Integer> roundPos = MapHelper.getRoundPos(player.lord.getPos(), radius);
            for (Integer pos : roundPos) {
                if (isEmptyPos(pos)) {
                    freePos.add(pos);
                }
            }
            if (freePos.size() < npcCount) {// 周围的空位小于生成怪物数,扩大范围
                radius++;
                freePos.clear();
                continue;
            }
            break;
        }
        // 随机选位置
        for (int i = 0; i < npcCount; i++) {
            int index = RandomHelper.randomInSize(freePos.size());
            availablePos.add(freePos.remove(index));
        }

        for (int i = 0; i < availablePos.size(); i++) {
            int pos = availablePos.get(i);
            long lordId = player.lord.getLordId();
            CabinetLead lead = new CabinetLead();
            lead.setCabinetPlanId(staticCabinetPlan.getId());
            lead.setCamp(player.lord.getCamp());
            lead.setPos(pos);
            lead.setRoleId(lordId);
            cabinetLeadMap.put(pos, lead);

            int block = block(pos);
            Map<Integer, CabinetLead> leadMap = cabinetLeadBlockMap.get(block);
            if (leadMap == null) {
                Map<Integer, CabinetLead> map = new ConcurrentHashMap<>();
                map.put(pos, lead);
                cabinetLeadBlockMap.put(block, map);
            } else {
                leadMap.put(pos, lead);
            }

            freePostList.remove(Integer.valueOf(pos)); // 移除空位
            LogUtil.common("生成新的点兵统领, pos:", pos, ", LordId:", lordId, ", CabinetPlanId:", staticCabinetPlan.getId());
        }
    }

    public int getRoundPos(int pos, int radius) {
        // 3x3的格子
        List<Integer> listPos = MapHelper.getRoundPos(pos, 1);
        int newPos = 0;
        // 获取可用的空位
        listPos = listPos.stream().filter(p -> isEmptyPos(p)).collect(Collectors.toList());
        LogUtil.debug("流寇重新刷 3x3 的空位为 listPos.size():", listPos.size());
        if (!listPos.isEmpty()) {
            int index = RandomHelper.randomInSize(listPos.size());
            newPos = listPos.get(index);
        }
        return newPos;
    }


    public boolean isCityOwner(long roleId) {
        for (City city : cityMap.values()) {
            if (city.getOwnerId() == roleId) {
                return true;
            }
        }
        return false;
    }

    public City getMyOwnerCity(long roleId) {
        for (City city : cityMap.values()) {
            if (city.getOwnerId() == roleId) {
                return city;
            }
        }
        return null;
    }

    /**
     * 遍历所有city， 根据类型获取同阵营的城市
     *
     * @param cityType
     * @param camp
     * @return
     */
    public int cntCityCamp(int cityType, int camp) {
        int cnt = 0;
//        StaticCity sCity = null;
//        for (City city : cityMap.values()) {
//            sCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
//            if (sCity.getType() == cityType && city.getCamp() == camp) {
//                cnt++;
//            }
//        }

        try {
            Map<Integer, Integer> errorMap = new HashMap<>();
            for (Entry<Integer, List<Army>> entry : armyMap.entrySet()) {
                List<Army> list = entry.getValue();
                if (CheckNull.nonEmpty(list)) {
                    Set<Army> clearArmy = new HashSet<>();
                    for (Army army : list) {
                        if (army.getType() == ArmyConstant.ARMY_TYPE_HELP_SUPERMINE) {
                            clearArmy.add(army);
                        } else if (army.getType() != ArmyConstant.ARMY_TYPE_GUARD) {
                            errorMap.merge(army.getType(), 1, Integer::sum);
                        }
                    }
                    if (!clearArmy.isEmpty()) {
                        clearArmy.forEach(list::remove);
                    }
                }
            }
            if (!errorMap.isEmpty()) {
                LogUtil.error2Sentry(String.format("armyMap 剩余异常驻防数据 :%s", errorMap));
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
        return cnt;
    }

    public void addPlayerGuard(int pos, Army army) {
        List<Army> armys = armyMap.get(pos);
        if (armys == null) {
            armys = new CopyOnWriteArrayList<>(); // 需要线程安全的List
            armyMap.put(pos, armys);
        }
        armys.add(army);
    }

    public void removePlayerGuard(int pos) {
        armyMap.remove(pos);
    }

    public List<Army> getPlayerGuard(int pos) {
        return armyMap.get(pos);
    }

    public void removePlayerGuard(int pos, Army army) {
        LogUtil.debug("移除驻军前数据=" + armyMap);
        List<Army> armys = armyMap.get(pos);
        if (armys != null) {
            armys.remove(army);
        }
        LogUtil.debug("移除驻军后数据=" + armyMap);
    }

    /**
     * 都城皇城人口
     *
     * @param camp
     * @return
     */
    public int getCountryPeople(int camp) {
        int countryNum = getPeoPle4MiddleCity(camp) * WorldConstant.MILDDLE_CITY_PEOPLE;
        City homeCity = checkHasHome(camp);
        countryNum += homeCity != null ? homeCity.getExp() : 0;
        return countryNum;
    }

    public Map<Integer, Integer> getCampCapitalBanditCnt() {
        return campCapitalBanditCnt;
    }

    public Map<Integer, Long> getCampCollectTime() {
        return campCollectTime;
    }

    // =====================================test=====================================
    // public static void test() {
    // int pos;
    // int times = 0;
    // int order = 1; // 根据需求计算分配玩家的分区
    // int areaId = 6;
    // int xBegin = ((areaId - 1) % 5) * 100;
    // int yBegin = (4 - (areaId - 1) / 5) * 100;
    // while (true) {
    // pos = (RandomHelper.randomInSize(100) + xBegin) + (RandomHelper.randomInSize(100) + yBegin) * 500;
    // System.out.println(",初始坐标=" + pos + ",areaId=" + areaId + ",area=" + getAreaIdByPos(pos) + ",xBegin="
    // + xBegin + ",yBegin=" + yBegin);
    // break;
    // }
    // System.out.println(",最终初始坐标=" + pos + ",areaId=" + getAreaIdByPos(pos));
    // }

    // public static final int[] AREA_MAP = {21,22,23,24,25,
    // 16,17,18,19,20,
    // 11,12,13,14,15,
    // 6,7,8,9,10,
    // 1,2,3,4,5
    // };

    public static void main2(String[] args) {
        int pos = 121920;
        Turple<Integer, Integer> xy = reducePos(pos);
        int x = xy.getA();
        int y = xy.getB();
        System.out.println("x:" + x + ", y:" + y + ", area:" + block(pos));

        int areaId = getAreaIdByPos(pos);
        System.out.println("pos:" + pos + ", areaId:" + areaId);
        int xBegin = ((areaId - 1) % 5) * 100;
        int yBegin = ((areaId - 1) / 5) * 100;
        for (int i = 0; i < 10; i++) {
            int ranDomPos = (RandomHelper.randomInSize(100) + xBegin) + (RandomHelper.randomInSize(100) + yBegin) * 500;
            System.out.println("i:" + i + ", randomPos:" + ranDomPos);
        }

        for (Integer block : getBlockInArea(areaId)) {
            System.out.println("block:" + block);
        }

        System.out.println(getAreaIdByPos(102021));
    }

    /**
     * GM清理用
     */
    public void clearAllPlayerData() {
        posMap.clear();
        for (int i = 1; i <= 2500; i++) {
            blockMap.put(i, new HashMap<Integer, Player>());
        }
    }

    /**
     * 检查并分配坐标
     *
     * @param player
     */
    public void openPos(Player player) {
        // 检查开启世界
        if (player.lord.getPos() < 0) {
            if (!player.isRobot) {
                addNewPlayer(player, true);
                activityDataManager.syncActChange(player, ActivityConst.ACT_7DAY);
            }
        }
    }

    public static void main(String[] args) {
        // test();
        // main2(args);
    }

    /**
     * 地图清除刷新
     */
    public void synCleanMapRefreshChg() {
        for (Long roleId : focusAreaByPlayer.keySet()) {
            Player player = playerDataManager.getPlayer(roleId);
            syncMapAreaChange(player, Events.AreaChangeNoticeEvent.CLEAR_CACHE_TYPE, null);
        }

        // Base.Builder msg = PbHelper.createSynBase(SyncWorldChgRs.EXT_FIELD_NUMBER, SyncWorldChgRs.ext,
        // SyncWorldChgRs.newBuilder().setType(type).build());
        // for (Player player : playerDataManager.getPlayers().values()) {
        // if (player.lord.getPos() < 0 || !player.isLogin) {
        // continue;
        // }
        // MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        // }
    }

    // ======================== 地图焦点同步相关操作 ====================

    /**
     * 删除玩家地图焦点
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND) // 都是在同一个线程进行运行 避免多线程带来的问题
    public void rmMapFocus(Events.RmMapFocusEvent event) {
        long roleId = event.player.roleId;
        LogUtil.debug("=======移除焦点 roleId:", roleId);
        // 移除区的焦点
        Integer areaId = focusAreaByPlayer.remove(roleId);
        if (areaId != null) {
            Set<Long> roleIds = focusByArea.get(areaId);
            if (!CheckNull.isEmpty(roleIds)) {
                roleIds.remove(roleId);
            }
        }
    }

    /**
     * 通知玩家地图数据发生改变
     *
     * @param player
     * @param type   1 刷新map数据, 2 刷新area数据, 3 刷新map和area数据
     * @param blocks 改变的区块
     */
    private void syncMapAreaChange(Player player, int type, Collection<Integer> blocks) {
        if (player != null && player.ctx != null && player.isLogin) {
            SyncWorldChgRs.Builder builder = SyncWorldChgRs.newBuilder();
            builder.setType(type);
            if (!CheckNull.isEmpty(blocks)) {
                builder.addAllBlocks(blocks);
            }
            Base.Builder msg = PbHelper.createSynBase(SyncWorldChgRs.EXT_FIELD_NUMBER, SyncWorldChgRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 同步区的数据
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void syncAreaChange(Events.AreaChangeNoticeEvent event) {
        List<Integer> posList = event.posList;
        long excludeId = event.excludeId;
        int type = event.type;

        Set<Integer> areaSet = new HashSet<>();
        Set<Integer> blocks = new HashSet<>();
        for (Integer pos : posList) {
            areaSet.add(MapHelper.getAreaIdByPos(pos));
            blocks.add(MapHelper.block(pos));
        }
        for (Integer areaId : areaSet) {
            Set<Long> roleIds = focusByArea.get(areaId);
            if (!CheckNull.isEmpty(roleIds)) {
                LogUtil.debug("=======通知区域改变 areaId:", areaId, ", roleIds:", roleIds.toString());
                for (Long roleId : roleIds) {
                    if (roleId.longValue() == excludeId)
                        continue;
                    Player player = playerDataManager.getPlayer(roleId);
                    syncMapAreaChange(player, type, blocks);
                }
            }
        }
    }

    /**
     * 添加区的玩家焦点
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void addAreaFocus(Events.AddAreaChangeEvent event) {
        long roleId = event.player.roleId;
        int areaId = event.areaId;
        LogUtil.debug("=======添加区焦点 roleId:", roleId, ", areaId:", areaId);

        // 如果之前有缓存先移除
        Integer preAreaId = focusAreaByPlayer.get(roleId);
        if (preAreaId != null) {
            Set<Long> roleIds = focusByArea.get(preAreaId);
            if (!CheckNull.isEmpty(roleIds)) {
                roleIds.remove(roleId);
            }
        }

        focusAreaByPlayer.put(roleId, areaId);

        Set<Long> roleIds = focusByArea.get(areaId);
        if (roleIds == null) {
            roleIds = new CopyOnWriteArraySet<>();
            focusByArea.put(areaId, roleIds);
        }
        roleIds.add(roleId);
    }

    /**
     * 地图事件推送
     *
     * @param events
     */
    public void syncWorldChangeEvent(WorldEvent... events) {
        Set<Integer> areaSet = new HashSet<>();
        SyncWorldEventRs.Builder builder = SyncWorldEventRs.newBuilder();
        for (WorldEvent e : events) {
            areaSet.add(MapHelper.getAreaIdByPos(e.getPos()));
            builder.addEvent(PbHelper.createTwoIntPb(e.getPos(), e.getEventType()));
        }
        SyncWorldEventRs wEventPb = builder.build();
        for (Integer areaId : areaSet) {
            Set<Long> roleIds = focusByArea.get(areaId);
            if (!CheckNull.isEmpty(roleIds)) {
                for (Long roleId : roleIds) {
                    Player player = playerDataManager.getPlayer(roleId);
                    if (player != null && player.ctx != null && player.isLogin) {
                        Base.Builder msg = PbHelper.createSynBase(SyncWorldEventRs.EXT_FIELD_NUMBER,
                                SyncWorldEventRs.ext, wEventPb);
                        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
                    }
                }
            }
        }
    }

    /**
     * @Author: ZhouJie
     * @Date: 2018-03-28 19:29
     * @Description: 根据Area获取盖世太保
     */
    public List<Gestapo> getGestapoByArea(int area) {
        int now = TimeHelper.getCurrentSecond();
        List<Gestapo> gestapoList = new ArrayList<>();
        Set<Integer> blockInArea = getBlockInArea(area);
        Map<Integer, Gestapo> gestapoInBlockMap;
        for (Integer block : blockInArea) {
            // 获取盖世太保
            gestapoInBlockMap = getGestapoInBlock(block);
            if (!CheckNull.isEmpty(gestapoInBlockMap)) {
                for (Entry<Integer, Gestapo> entry : gestapoInBlockMap.entrySet()) {
                    Gestapo gestapo = entry.getValue();
                    StaticGestapoPlan staticGestapoPlan = StaticWorldDataMgr.getGestapoPlanById(gestapo.getGestapoId());
                    if (CheckNull.isNull(staticGestapoPlan)) {
                        LogUtil.error("获取盖世太保，有盖世太保未配置, StaticGestapoPlanId:", gestapo.getGestapoId());
                        continue;
                    }
                    if (gestapo.getEndTime() < now)
                        continue;
                    gestapoList.add(gestapo);
                }
            }
        }
        return gestapoList;
    }

    /**
     * @Author: ZhouJie
     * @Date: 2018-03-28 20:41
     * @Description: 添加太保到指定坐标
     */
    public void addGestapo(int pos, StaticGestapoPlan staticGestapoPlan, Player player) {
        if (staticGestapoPlan != null && isEmptyPos(pos)) {
            Integer gestapoId = staticGestapoPlan.getGestapoId();
            Gestapo gestapo = new Gestapo(pos, staticGestapoPlan, player);
            putGestapo4Block(pos, gestapo);
        }
    }

    /**
     * 地图添加太保
     *
     * @param pos
     * @param gestapo
     */
    public void putGestapo4Block(int pos, Gestapo gestapo) {
        int block = block(pos);
        Map<Integer, Gestapo> gestapoPosMap = gestapoBlockMap.get(block);
        if (CheckNull.isNull(gestapoPosMap)) {
            gestapoPosMap = new ConcurrentHashMap<>();
            gestapoBlockMap.put(block, gestapoPosMap);
        }
        gestapoPosMap.put(pos, gestapo);
        freePostList.remove(Integer.valueOf(pos));
        gestapoMap.put(pos, gestapo);
    }

    /**
     * 根据坐标点获取太保
     *
     * @param pos
     * @return
     */
    public Gestapo getGestapoByPos(int pos) {
        return gestapoMap.get(pos);
    }

    public Map<Integer, Gestapo> getGestapoMap() {
        return gestapoMap;
    }

    /**
     * 获取玩家总共拥有的召唤道具数量
     *
     * @return
     */
    public int getCostCnt(Player player) {
        int cnt = 0;
        List<StaticGestapoPlan> gestapoPlans = StaticWorldDataMgr.getGestapoList();
        for (StaticGestapoPlan gestapoPlan : gestapoPlans) {
            List<List<Integer>> costProp = gestapoPlan.getCostProp();
            if (CheckNull.isEmpty(costProp))
                continue;
            List<Integer> prop = costProp.get(0);
            if (CheckNull.isEmpty(prop))
                continue;
            cnt += new Long(rewardDataManager.getRoleResByType(player, prop.get(0), prop.get(1))).intValue();
        }
        return cnt;
    }

    /**
     * 更新消息推送的进度
     *
     * @param key
     * @param schedule
     */
    public void updSendChatCnt(int key, int schedule) {
        Integer state = sendChatCnt.get(key);
        state = state == null ? 0 : state;
        state = state + schedule;
        sendChatCnt.put(key, state);
    }

    /**
     * @return void
     * @Title: getArmyHeroAttrs
     * @Description: 获取部队的 将领兵种强化属性
     */
    /*
     * public void getArmyHeroAttrs(Army army,Player player) { Map<Integer,
     * Hero> heros = player.heros;//玩家将领 List<HeroAttr> heroAttrs = new
     * ArrayList<HeroAttr>();//将领兵种强化属性 for(TwoInt hero : army.getHero()) {
     * HeroAttr heroAttr = new HeroAttr(); Hero h = heros == null ? null :
     * heros.get(hero.getV1());//将领 int heroType = h == null ? 0 :
     * h.getHeroType();//将领类型 int intensifyLv =
     * techDataManager.getIntensifyLv4HeroType(player, heroType);//将领对应的兵种强化等级
     * heroAttr.setHeroId(hero.getV1()); heroAttr.setIntensifyLv(intensifyLv);
     * heroAttrs.add(heroAttr); } army.setHeroAttr(heroAttrs); }
     */
    public int currentSendChatCnt(int key) {
        return sendChatCnt.containsKey(key) ? sendChatCnt.get(key) : 0;
    }

    public LightningWarBoss getLightningWarBossByArea(int area) {
        return lightningWarBossMap.get(area);
    }

    public Map<Integer, LightningWarBoss> getLightningWarBossMap() {
        return lightningWarBossMap;
    }

    public Map<Integer, Integer> getSendChatCnt() {
        return sendChatCnt;
    }

    public Map<Integer, AirshipWorldData> getAirshipWorldDataMap() {
        return airshipWorldDataMap;
    }

    /**
     * 注意返回的list是 CopyOnWriteArrayList
     *
     * @return
     */
    public List<AirshipWorldData> getAllAirshipWorldData() {
        return allAirshipWorldData;
    }


    /**
     * 所有的圣坛
     *
     * @return 圣坛
     */
    public Map<Integer, Altar> getAltarMap() {
        return altarMap;
    }

    /**
     * 清除所有的圣坛
     */
    public void clearAllAltar() {
        altarMap.clear();
    }


    public Map<Integer, RelicEntity> getRelicEntityMap() {
        return relicEntityMap;
    }
}
