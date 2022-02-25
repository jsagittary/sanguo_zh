package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import com.gryphpoem.game.zw.resource.util.random.MineLvRandom;
import com.gryphpoem.game.zw.service.WorldScheduleService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StaticWorldDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // 世界地图分区信息
    private static Map<Integer, StaticArea> areaMap;

    // 所有可使用的分区列表
    private static Set<Integer> openAreaIdSet;

    // key:地图分区开启次序， value:所有可以供玩家出生的分区id
    private static Map<Integer, List<Integer>> orderOpenAreaMap;
    // 玩家可出生的区域,会按照openSequence进行排序
    private static List<StaticArea> birthArea;

    // 世界地图中的所有城池,包括新地图的 key:cityId
    private static Map<Integer, StaticCity> cityMap;
    // 老地图的的cityId(区域)
    private static Map<Integer, StaticCity> oldCityMap;

    // 按行政分区划分城池, key:area
    private static Map<Integer, List<StaticCity>> areaCityMap;

    // 按10*10分块划分城池, key:block
    private static Map<Integer, List<StaticCity>> blockCityMap;

    // 城池占用坐标集合
    private static Set<Integer> cityPosSet;

    /**
     * 世界boss占用点
     */
    private static Set<Integer> bossPosSet;
    // 矿属性
    private static Map<Integer, StaticMine> mineMap;
    // 超级矿点
    private static Map<Integer, StaticSuperMine> superMineMap;

    // 行政分区小分块配置信息， key:areaOrder_range
    private static Map<String, StaticAreaRange> areaBlockMap;
    // key:areaOrder
    private static Map<Integer, List<StaticAreaRange>> blockRangeMap;

    private static Map<Integer, StaticWorldTask> worldTaskMap;

    /**
     * 最后的世界任务
     */
    private static StaticWorldTask lastWorldTask;

    // 都城开发
    private static Map<Integer, StaticCityDev> cityDevMap;
    // 大地图流寇和矿点刷新规则
    private static List<StaticWorldRule> wroldRuleList;
    // 盖世太保配置: Key: gestapoId
    private static Map<Integer, StaticGestapoPlan> gestapoMap;

    // 每个区域的中心城市
    private static Map<Integer, StaticCity> maxTypeCityByArea;

    // 每个区域的中心城市(初始化open的区域)
    private static Map<Integer, StaticCity> maxTypeCityByOpenArea;
    // 皇城中城池的名称 (除都城以外)
    private static Set<String> capitalCityNameSet;
    // 都城建设次数,对应加的经验
    private static Map<Integer, StaticCityAward> cityAwardMap;

    // 柏林会战使用
    private static Map<Integer, StaticBerlinWar> berlinWarMap;
    // 按10*10分块划分城池, key:block(过滤掉柏城,只有炮点)
    private static Map<Integer, List<StaticBerlinWar>> blockBerlinWarMap;

    // 名城Buff使用, key:buffId
    private static Map<Integer, StaticCityBuffer> cityBufferMap;
    // 荣耀日报触发条件表 key:cond
    private static Map<Integer, StaticHonorDailyCond> honorDailyCondMap;

    // 匪军叛乱相关配置
    private static Map<Integer, StaticRebelRound> rebelRoundMap;
    // 匪军叛乱攻击轮次 key:template ,val StaticRebelRound会根据round排序
    private static Map<Integer, List<StaticRebelRound>> rebelRounds;
    // 匪军叛乱buff key:buffId val:buff
    private static Map<Integer, StaticRebelBuff> rebelBuffMap;
    // 匪军叛乱buff key:type_lv val:buff
    private static Map<String, StaticRebelBuff> rebelBuffTypeLvMap;
    // 匪军商店 key:shopId, val:物品
    private static Map<Integer, StaticRebelShop> rebelShopMap;

    // 反攻德意志的配置
    private static Map<Integer, StaticCounterAttack> counterAttackTypeMap;
    // 反攻德意志的商城
    private static Map<Integer, StaticCounterAttackShop> counterAttackShopMap;

    // 飞艇相关
    // <id,StaticAirship> 飞艇的配置
    private static Map<Integer, StaticAirship> airshipMap;
    // 刷飞艇相关配置 <keyId,StaticAirshipArea>
    private static Map<Integer, StaticAirshipArea> airshipAreaMap;
    // 飞艇 <areaOrder, List<StaticAirshipArea>>
    private static Map<Integer, List<StaticAirshipArea>> airshipAreaOrderMap;
    // 飞艇buff的配置
    private static Map<Integer, StaticAirShipBuff> airShipBuffMap;

    // 配置表中的最大的进程id
    public static int SCHEDULE_MAX_ID;

    /**
     * s_schedule表, key=id, val=表配置
     */
    private static Map<Integer, StaticSchedule> scheduleMap;

    /**
     * s_schedule_goal表, key=id, val=表配置
     */
    private static Map<Integer, StaticScheduleGoal> scheduleGoalMap;

    /**
     * s_schedule_boss表, key=id, val=表配置
     */
    private static Map<Integer, StaticScheduleBoss> scheduleBossMap;

    /**
     * key=scheduleId, val=list<"StaticScheduleBoss">
     */
    private static Map<Integer, List<StaticScheduleBoss>> scheduleBossSchedIdMap;

    /**
     * s_schedule_rank表<scheduleId,<ranking,StaticScheduleRank>> ,后面的list是根据ranking进行正序排序
     */
    private static Map<Integer, Map<Integer, StaticScheduleRank>> scheduleRankMap;

    /**
     * s_schedule_boss_rank表 <scheduleId, StaticScheduleBossRank>
     */
    private static Map<Integer, List<StaticScheduleBossRank>> scheduleBossRankIdMap;

    /**
     * s_altar_area表 <id, StaticAltarArea>
     */
    private static Map<Integer, StaticAltarArea> altarAreaMap;

    /**
     * 初始化加载静态数据
     */
    public static void init(String operation) {
        Map<Integer, StaticArea> areaMap = staticDataDao.selectAreaMap();
        StaticWorldDataMgr.areaMap = areaMap;

        Set<Integer> openAreaIdSet = new HashSet<>();
        Map<Integer, List<Integer>> orderOpenAreaMap = new HashMap<>();
        List<Integer> list;
        Set<Integer> orderSet = new HashSet<>();
        List<StaticArea> birthAreaList = new ArrayList<>();
        for (StaticArea area : areaMap.values()) {
            if (area.isOpen()) {
                openAreaIdSet.add(area.getArea());
                orderSet.add(area.getOpenOrder());
                calcUnlockTotalWeight(area);
            }
            if (area.getOpenOrder() == WorldConstant.AREA_ORDER_1) {
                birthAreaList.add(area);
            }
        }
        StaticWorldDataMgr.openAreaIdSet = openAreaIdSet;

        for (StaticArea area : areaMap.values()) {
            if (area.isOpen()) {
                int order = area.getOpenOrder();
                list = orderOpenAreaMap.get(order);
                if (null == list) {
                    list = new ArrayList<>();
                    orderOpenAreaMap.put(order, list);
                }
                list.add(area.getArea());
                for (Integer openOrder : orderSet) {
                    if (openOrder > order) {
                        list = orderOpenAreaMap.get(openOrder);
                        if (null == list) {
                            list = new ArrayList<>();
                            orderOpenAreaMap.put(openOrder, list);
                        }
                        list.add(area.getArea());
                    }
                }
            }
        }
        StaticWorldDataMgr.orderOpenAreaMap = orderOpenAreaMap;
        LogUtil.debug("orderOpenAreaMap=" + orderOpenAreaMap);

        // 出生地排序
        StaticWorldDataMgr.birthArea = birthAreaList.stream().filter(StaticArea::openSequenceAvailable)
                .sorted(Comparator.comparingInt(StaticArea::getOpenSequence)).collect(Collectors.toList());

        List<StaticAreaRange> areaBlockList = staticDataDao.selectAreaBlockList();
        Map<String, StaticAreaRange> areaBlockMap = new HashMap<>();
        Map<Integer, List<StaticAreaRange>> blockRangeMap = new HashMap<>();
        List<StaticAreaRange> rangeList = new ArrayList<>();
        for (StaticAreaRange sab : areaBlockList) {
            areaBlockMap.put(getMapKey(sab.getAreaOrder(), sab.getRange()), sab);
            rangeList = blockRangeMap.get(sab.getAreaOrder());
            if (null == rangeList) {
                rangeList = new ArrayList<>();
                blockRangeMap.put(sab.getAreaOrder(), rangeList);
            }
            rangeList.add(sab);
        }
        StaticWorldDataMgr.areaBlockMap = areaBlockMap;
        StaticWorldDataMgr.blockRangeMap = blockRangeMap;

        StaticWorldDataMgr.cityMap = staticDataDao.selectCityMap();
        // 26号区域时跨服的,所以过滤掉
        Map<Integer, StaticCity> oldCityMap = cityMap.values().stream()
                .filter(scity -> scity.getArea() < CrossWorldMapConstant.CITY_AREA)
                .collect(Collectors.toMap(StaticCity::getCityId, scity -> scity));

        StaticWorldDataMgr.oldCityMap = oldCityMap;

        Map<Integer, StaticCityDev> cityDevMap = staticDataDao.selectCityDevMap();
        StaticWorldDataMgr.cityDevMap = cityDevMap;

        Map<Integer, StaticGestapoPlan> staticGestapoMap = staticDataDao.selectGestapoMap();
        StaticWorldDataMgr.gestapoMap = staticGestapoMap;

        StaticWorldDataMgr.altarAreaMap = staticDataDao.selectAltarAreaMap();

        Set<Integer> cityPosSet = new HashSet<>();
        Map<Integer, List<StaticCity>> areaCityMap = new HashMap<>();
        Map<Integer, List<StaticCity>> blockCityMap = new HashMap<>();
        maxTypeCityByArea = new HashMap<>();
        int block;
        List<StaticCity> cityList;
        for (StaticCity city : oldCityMap.values()) {
            city.calcWeight(operation);
            cityPosSet.addAll(city.getPosList());

            cityList = areaCityMap.get(city.getArea());
            if (null == cityList) {
                cityList = new ArrayList<>();
                areaCityMap.put(city.getArea(), cityList);
            }
            cityList.add(city);

            block = MapHelper.block(city.getCityPos());
            cityList = blockCityMap.get(block);
            if (null == cityList) {
                cityList = new ArrayList<>();
                blockCityMap.put(block, cityList);
            }
            cityList.add(city);
            // 每个区域的中心城市查找
            StaticCity maxTypeCity = maxTypeCityByArea.get(city.getArea());
            if (maxTypeCity != null) {
                maxTypeCity = maxTypeCity.getType() > city.getType() ? maxTypeCity : city;
            } else {
                maxTypeCity = city;
            }
            maxTypeCityByArea.put(maxTypeCity.getArea(), maxTypeCity);
        }

        // 将柏林会战的据点占有坐标加入到cityPosSet
        StaticBerlinWarDataMgr.getAllScheduleBerlin()
                .forEach(berlin -> cityPosSet.addAll(berlin.getPosList()));

        StaticWorldDataMgr.cityPosSet = cityPosSet;
        StaticWorldDataMgr.areaCityMap = areaCityMap;
        StaticWorldDataMgr.blockCityMap = blockCityMap;

        // 柏林会战相关
        StaticWorldDataMgr.berlinWarMap = staticDataDao.selectBerlinWar();
        // 添加到城池列表
        StaticWorldDataMgr.berlinWarMap.values().forEach(sblw -> cityPosSet.addAll(sblw.getPosList()));
        StaticWorldDataMgr.blockBerlinWarMap = berlinWarMap.values().stream()
                .filter(sblw -> sblw.getType() == StaticBerlinWarDataMgr.BATTLEFRONT_TYPE)
                .collect(Collectors.groupingBy(StaticBerlinWar::getCityBlock));

        Map<Integer, StaticMine> mineMap = staticDataDao.selectMineMap();
        StaticWorldDataMgr.mineMap = mineMap;
        MineLvRandom.initData(mineMap);

        // 世界任务
        List<StaticWorldTask> worldTaskList = staticDataDao.selectWorldTask();
        Map<Integer, StaticWorldTask> worldTaskMap = new HashMap<>();
        for (StaticWorldTask task : worldTaskList) {
            worldTaskMap.put(task.getTaskId(), task);
        }
        StaticWorldDataMgr.worldTaskMap = worldTaskMap;
        // (x < y) ? -1 : ((x == y) ? 0 : 1)
        lastWorldTask = worldTaskMap.values().stream().max((swt1, swt2) -> (swt1.getTaskId() < swt2.getTaskId()) ? -1
                : (swt1.getTaskId() == swt2.getTaskId() ? 0 : 1)).get();

        maxTypeCityByOpenArea = new HashMap<>();
        for (StaticArea area : birthArea) {
            int areaId = area.getArea();
            if (maxTypeCityByArea.containsKey(areaId)) {
                maxTypeCityByOpenArea.put(areaId, maxTypeCityByArea.get(areaId));
            }
        }

        StaticWorldDataMgr.wroldRuleList = staticDataDao.selectStaticWorldRule();
        StaticNightRaidMgr.init();// 夜袭功初始化

        // 皇城中城池的名称
        capitalCityNameSet = areaCityMap.get(WorldConstant.AREA_TYPE_13).stream()
                .filter(sCity -> sCity.getType() == WorldConstant.CITY_TYPE_8 || sCity.getType() == WorldConstant.CITY_TYPE_KING)
                .map(sCity -> StaticIniDataMgr.getTextName("s_city_" + sCity.getCityId())).collect(Collectors.toSet());

        StaticWorldDataMgr.cityAwardMap = staticDataDao.selectCityAwardMap();
        StaticWorldDataMgr.superMineMap = staticDataDao.selectSuperMineMap();

        StaticWorldDataMgr.cityBufferMap = staticDataDao.selectCityBufferMap();

        StaticWorldDataMgr.honorDailyCondMap = staticDataDao.selectHonorDailyDondMap();
        initRebelCfg();
        initCounterAtkCfg();
        initAirship();
        initSchedule();
    }

    /**
     * 初始化世界进度相关配置
     */
    private static void initSchedule() {
        StaticWorldDataMgr.scheduleMap = staticDataDao.selectScheduleMap();
        // 设置配置的最大世界进程
        StaticWorldDataMgr.SCHEDULE_MAX_ID = StaticWorldDataMgr.scheduleMap.values().stream().mapToInt(StaticSchedule::getId).max().orElse(0);
        StaticWorldDataMgr.scheduleGoalMap = staticDataDao.selectScheduleGoalMap();
        StaticWorldDataMgr.scheduleBossMap = staticDataDao.selectScheduleBossMap();
        List<StaticScheduleRank> allScheduleRankList = staticDataDao.selectScheduleRankList();
        StaticWorldDataMgr.scheduleRankMap = allScheduleRankList.stream()
                .collect(Collectors.groupingBy(StaticScheduleRank::getScheduleId, HashMap::new,
                        Collectors.toMap(StaticScheduleRank::getRanking, s -> s)));

        Map<Integer, List<StaticScheduleBoss>> bossSchedIdTemp = new HashMap<>(20);
        StaticWorldDataMgr.scheduleBossMap.values().forEach(ssb -> {
            int scheduleId = ssb.getScheduleId();
            List<StaticScheduleBoss> bossList = bossSchedIdTemp.get(scheduleId);
            if (CheckNull.isNull(bossList)) {
                bossList = new ArrayList<>();
                bossSchedIdTemp.put(scheduleId, bossList);
            }
            bossList.add(ssb);
        });
        StaticWorldDataMgr.scheduleBossSchedIdMap = bossSchedIdTemp;
        StaticWorldDataMgr.bossPosSet = StaticWorldDataMgr.scheduleBossMap.values().stream().flatMap(sBoss -> {
            List<Integer> posList = sBoss.getPosList();
            if (CheckNull.isEmpty(posList)) {
                posList = new ArrayList<>(1);
                posList.add(sBoss.getPos());
            }
            return posList.stream();
        }).collect(Collectors.toSet());
        List<StaticScheduleBossRank> boosScheduleRankList = staticDataDao.selectScheduleBossRankIdMap();
        StaticWorldDataMgr.scheduleBossRankIdMap = boosScheduleRankList.stream().collect(Collectors.groupingBy(StaticScheduleBossRank::getScheduleId));
    }

    /**
     * 根据id查询世界进度配置
     *
     * @param id
     * @return
     */
    public static StaticSchedule getScheduleById(int id) {
        return scheduleMap.get(id);
    }

    public static Map<Integer, StaticSchedule> getScheduleMap() {
        return scheduleMap;
    }

    public static Map<Integer, StaticScheduleGoal> getScheduleGoalMap() {
        return scheduleGoalMap;
    }

    /**
     * 根据id查询世界限时目标配置
     *
     * @param id
     * @return
     */
    public static StaticScheduleGoal getScheduleGoalById(int id) {
        return scheduleGoalMap.get(id);
    }

    /**
     * 根据世界进度id查询世界boss配置信息
     *
     * @param id
     * @return
     */
    public static List<StaticScheduleBoss> getScheduleBossById(int id) {
        return scheduleBossSchedIdMap.get(id);
    }

    /**
     * 根据世界进度id查询世界boss排行配置信息
     * @param id
     * @return
     */
    public static List<StaticScheduleBossRank> getSchedBossRankById(int id) {
        return scheduleBossRankIdMap.get(id);
    }

    /**
     * 初始飞艇相关
     */
    private static void initAirship() {
        StaticWorldDataMgr.airshipMap = staticDataDao.selectStaticAirshipMap();
        StaticWorldDataMgr.airshipAreaMap = staticDataDao.selectStaticAirshipAreaList();
        StaticWorldDataMgr.airshipAreaOrderMap = StaticWorldDataMgr.airshipAreaMap
                .values()
                .stream()
                .collect(Collectors.groupingBy(StaticAirshipArea::getAreaOrder));
        StaticWorldDataMgr.airShipBuffMap = staticDataDao.selectAirShipBuffMap();
    }

    /**
     * 初始化反攻相关配置
     */
    private static void initCounterAtkCfg() {
        StaticWorldDataMgr.counterAttackTypeMap = staticDataDao.selectCounterAtkMap();

        StaticWorldDataMgr.counterAttackShopMap = staticDataDao.selectCounterShopMap();
    }

    /**
     * 根据类型, 轮数取反攻配置
     *
     * @param stype
     * @param curRound
     * @return
     */
    public static StaticCounterAttack getCounterAttackTypeMapByCond(int stype, int curRound) {
        return counterAttackTypeMap.values().stream().filter(c -> c.getStype() == stype && c.getNumber() == curRound)
                .findFirst().orElse(null);
    }

    /**
     * 根据商品id获取当前世界进程的商品配置
     *
     * @param id 商品id
     * @return 商品配置
     */
    public static StaticCounterAttackShop getCounterAttackShopMap(int id) {
        if (CheckNull.isEmpty(counterAttackShopMap)) {
            return null;
        }
        WorldScheduleService scheduleService = DataResource.ac.getBean(WorldScheduleService.class);
        int scheduleId = scheduleService.getCurrentSchduleId();
        return counterAttackShopMap.values().stream().filter(cas -> scheduleId >= cas.getSchedule().get(0) && scheduleId <= cas.getSchedule().get(1) && cas.getId() == id).findAny().orElse(null);
    }

    /**
     * 匪军判断配置初始化
     */
    private static void initRebelCfg() {
        StaticWorldDataMgr.rebelShopMap = staticDataDao.selectRebelShopMap();

        StaticWorldDataMgr.rebelBuffMap = staticDataDao.selectRebelBuffMap();
        StaticWorldDataMgr.rebelBuffTypeLvMap = rebelBuffMap.values().stream()
                .collect(Collectors.toMap(StaticRebelBuff::getMapKey, buff -> buff));

        StaticWorldDataMgr.rebelRoundMap = staticDataDao.selectRebelRoundMap();
        StaticWorldDataMgr.rebelRounds = rebelRoundMap.values().stream()
                .collect(Collectors.groupingBy(StaticRebelRound::getTemplate,
                        Java8Utils.toSortedList(Comparator.comparingInt(StaticRebelRound::getRound))));
    }

    private static void calcUnlockTotalWeight(StaticArea area) {
        StaticArea staticArea;
        for (Integer areaId : area.getUnlockArea()) {
            staticArea = areaMap.get(areaId);
            area.addUnlockWeight(staticArea.getLowWeight());
        }
    }

    private static String getMapKey(int areaOrder, int range) {
        return areaOrder + "_" + range;
    }

    public static Map<Integer, StaticArea> getAreaMap() {
        return areaMap;
    }

    public static Map<Integer, StaticMine> getMineMap() {
        return mineMap;
    }

    public static Map<Integer, StaticCity> getCityMap() {
        return cityMap;
    }

    public static StaticCityBuffer getCityBuffById(int buffId) {
        return cityBufferMap.get(buffId);
    }

    public static StaticCity getCityByPos(int pos) {
        for (StaticCity city : oldCityMap.values()) {
            if (city.getPosList().contains(pos)) {
                return city;
            }
        }
        return null;
    }

    public static Set<Integer> getCityPosSet() {
        return cityPosSet;
    }

    public static boolean isCityPos(int pos) {
        return cityPosSet.contains(pos);
    }

    public static Set<Integer> getOpenAreaIdSet() {
        return openAreaIdSet;
    }

    public static List<StaticCity> getCityByArea(int area) {
        return areaCityMap.get(area);
    }

    public static int randomAreaId(int order) {
        List<Integer> list = orderOpenAreaMap.get(order);
        if (CheckNull.isEmpty(list)) {
            return 1;
        }

        return list.get(RandomHelper.randomInSize(list.size()));
    }

    public static List<Integer> getOrderOpenAreaList(int order) {
        return orderOpenAreaMap.get(order);
    }

    public static List<StaticCity> getCityInBlock(int block) {
        return blockCityMap.get(block);
    }

    public static Map<String, StaticAreaRange> getAreaBlockMap() {
        return areaBlockMap;
    }

    public static Map<Integer, List<StaticAreaRange>> getBlockRangeMap() {
        return blockRangeMap;
    }

    public static StaticWorldTask getWorldTask(int id) {
        return worldTaskMap.get(id);
    }

    public static Map<Integer, StaticWorldTask> getWorldTaskMap() {
        return worldTaskMap;
    }

    public static StaticWorldTask getLastWorldTask() {
        return lastWorldTask;
    }

    public static StaticCityDev getCityDev(int lv) {
        return cityDevMap.get(lv);
    }

    public static List<StaticArea> getBirthArea() {
        return birthArea;
    }

    public static List<StaticWorldRule> getWroldRuleList() {
        return wroldRuleList;
    }

    public static StaticGestapoPlan getGestapoPlanById(int gestapoId) {
        return gestapoMap.get(gestapoId);
    }

    public static List<StaticGestapoPlan> getGestapoList() {
        return new ArrayList<>(gestapoMap.values());
    }

    public static Map<Integer, StaticCity> getMaxTypeCityByArea() {
        return maxTypeCityByArea;
    }

    public static StaticCity getMaxTypeCityByArea(int area) {
        return maxTypeCityByArea.get(area);
    }

    public static StaticCity getMaxTypeCityByOpenArea(int area) {
        return maxTypeCityByOpenArea.get(area);
    }

    public static Map<Integer, StaticCity> getMaxTypeCityByOpenArea() {
        return maxTypeCityByOpenArea;
    }

    public static Set<String> getCapitalCityNameSet() {
        return capitalCityNameSet;
    }

    public static boolean isNpcCityName(String name) {
        return capitalCityNameSet.contains(name);
    }

    public static Map<Integer, StaticSuperMine> getSuperMineMap() {
        return superMineMap;
    }

    public static StaticSuperMine getSuperMineRandom() {
        return superMineMap.values().stream().collect(Collectors.toList())
                .get(RandomHelper.randomInSize(superMineMap.size()));
    }

    public static StaticSuperMine getSuperMineById(int mineId) {
        return superMineMap.get(mineId);
    }

    public static Map<Integer, StaticCityAward> getCityAwardMap() {
        return cityAwardMap;
    }

    public static Map<Integer, List<StaticBerlinWar>> getBlockBerlinWarMap() {
        return blockBerlinWarMap;
    }

    public static Map<Integer, StaticBerlinWar> getBerlinWarMap() {
        return berlinWarMap;
    }

    public static StaticHonorDailyCond getHonorDailyCondMapById(int condId) {
        return honorDailyCondMap.get(condId);
    }

    public static List<StaticRebelRound> getRebelRoundsByTemplate(int template) {
        return rebelRounds.get(template);
    }

    public static StaticRebelBuff getRebelBuffByBuffId(int buffId) {
        return rebelBuffMap.get(buffId);
    }

    public static StaticRebelBuff getRebelBuffByTypeLv(int type, int lv) {
        String key = StaticRebelBuff.mapKey(type, lv);
        return rebelBuffTypeLvMap.get(key);
    }

    public static StaticRebelShop getRebelShopMapById(int shopId) {
        return rebelShopMap.get(shopId);
    }

    public static StaticRebelRound getRebelRoundById(int id) {
        return rebelRoundMap.get(id);
    }

    public static Map<Integer, StaticAirship> getAirshipMap() {
        return airshipMap;
    }

    public static Map<Integer, StaticAirshipArea> getAirshipAreaMap() {
        return airshipAreaMap;
    }

    public static List<StaticAirshipArea> getAirshipAreaOrderMap(int areaOrder) {
        return airshipAreaOrderMap.get(areaOrder);
    }

    public static StaticAirShipBuff getAirShipBuffById(int buffId) {
        return airShipBuffMap.get(buffId);
    }

    public static Map<Integer, Map<Integer, StaticScheduleRank>> getScheduleRankMap() {
        return scheduleRankMap;
    }

    public static Set<Integer> getBossPosSet() {
        return bossPosSet;
    }

    public static Map<Integer, StaticCity> getOldCityMap() {
        return oldCityMap;
    }

    public static Map<Integer, StaticAltarArea> getAltarAreaMap() {
        if (!CheckNull.isEmpty(altarAreaMap)) {
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_VISIT_ALTAR);
            if(Objects.nonNull(activityBase) && activityBase.getStep0() == ActivityConst.OPEN_STEP){
                int activityId = activityBase.getActivityId();
                // 过滤活动Id
                return altarAreaMap.values().stream().filter(saa -> saa.getActivityId() == activityId).collect(Collectors.toMap(StaticAltarArea::getAreaOrder, Function.identity(), (oldV, newV) -> newV));
            }
        }

        return altarAreaMap;
    }

}
