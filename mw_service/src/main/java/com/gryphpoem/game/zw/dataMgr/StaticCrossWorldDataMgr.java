package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.ServerIdHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName StaticCrossWorldDataMgr.java
 * @Description
 * @date 2019年3月20日
 */
public class StaticCrossWorldDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);
    // <cityId,StaticCity> 大于26号区域的城池
    private static Map<Integer, StaticCity> cityMap;
    /**
     * <id,StaticWorldwarOpen> 开发区域规则
     */
    private static Map<Integer, StaticWorldwarOpen> openMap;
    /**
     * 世界争霸计划表
     */
    private static List<StaticWorldWarPlan> worldWarPlan;

    /**
     * 世界争霸 赛季商店 [key:id,value:StaticWorldWarShop]
     */
    private static Map<Integer, StaticWorldWarShop> worldWarShop;
    /**
     * 世界争霸 阵营军威值奖励[key:id,value:StaticWorldWarCampCityAward]
     */
    private static Map<Integer, StaticWorldWarCampCityAward> worldWarCampCityAwardMap;
    /**
     * 世界争霸 阵营军威值奖励[key:worldWarType,value:List<StaticWorldWarCampCityAward]
     */
    private static Map<Integer, List<StaticWorldWarCampCityAward>> worldWarCampCityAward;
    /**
     * 世界争霸阵营排行榜 [key:id,value:StaticWorldWarCampRank]
     */
    private static Map<Integer, StaticWorldWarCampRank> worldWarCampRank;
    /**
     * 世界争霸个人排行榜[key:worldWarType,value:List<StaticWorldWarPersonalRank>]
     */
    private static Map<Integer, List<StaticWorldWarPersonalRank>> worldWarPersonalRank;
    /**
     * 世界争霸 每日计时任务配置 [key:id]
     */
    private static Map<Integer, StaticWorldWarTask> worldWarTask;
    /**
     * 世界争霸 每日计时任务配置 [key:worldWarType]
     */
    private static Map<Integer, List<StaticWorldWarTask>> worldWarTaskList;
    /**
     * 世界争霸 周任务
     */
    private static Map<Integer, StaticWorldWarWeekTask> worldWarWeekTask;
    /**
     * 世界争霸 周任务 [key:worldWarType]
     */
    private static Map<Integer, List<StaticWorldWarWeekTask>> worldWarWeekTaskList;
    /**
     * 世界争霸 每日任务
     */
    private static Map<Integer, StaticWorldWarDailyTask> worldWarDailyTask;
    /**
     * 世界争霸 每日任务 [key:worldWarType]
     */
    private static Map<Integer, List<StaticWorldWarDailyTask>> worldWarDailyTaskList;
    /**
     * 纽约争霸成就奖励
     */
    private static List<StaticNewYorkWarAchievement> staticNewYorkWarAchievement;
    /**
     * 纽约争霸成就奖励 [key:ranking]
     */
    private static List<StaticNewYorkWarCampRank> staticNewYorkWarCampRank;
    /**
     * 纽约争霸个人排行榜奖励
     */
    private static List<StaticNewYorkWarPersonalRank> staticNewYorkWarPersonalRank;
    /**
     * 战火燎原city配置
     */
    private static Map<Integer, StaticWarFire> staticWarFireMap;
    /**
     * 战火燎原buff配置, key: buffId, value: buff
     */
    private static Map<Integer, StaticWarFireBuff> staticWarFireBuffMap;
    /**
     * 战火燎原buff配置, key: type_lv, value: buff
     */
    private static Map<String, StaticWarFireBuff> staticWarFireBuffLvMap;
    /**
     * 战火燎原 个人积分奖励
     */
    private static TreeMap<Integer, StaticWarFireRankGr> staticWarFireRankGrMap;
    /**
     * 战火燎原 阵营排名奖励
     */
    private static TreeMap<Integer, StaticWarFireRankCamp> staticWarFireRankCampMap;
    /**
     * 战火燎原 刷新规则
     */
    private static Map<Integer, StaticWarFireRange> staticWarFireRangeMap;
    /**
     * 战火燎原 兑换商店
     */
    private static Map<Integer, StaticWarFireShop> staticWarFireShopMap;

    /**
     * 初始化加载静态数据
     */
    public static void init() {
        StaticCrossWorldDataMgr.cityMap = StaticWorldDataMgr.getCityMap().values().stream()
                .filter(scity -> scity.getArea() >= CrossWorldMapConstant.CITY_AREA)
                .collect(Collectors.toMap(StaticCity::getCityId, scity -> scity));
        StaticCrossWorldDataMgr.openMap = staticDataDao.selectWorldwarOpenMap();
        // 获取本机服务器id
        ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);
        final int serverId = serverSetting.getServerID();

        StaticCrossWorldDataMgr.worldWarPlan = staticDataDao.selectWorldWarPlan().stream()
                .filter(wplan -> ServerIdHelper.checkServer(wplan.getServerId(), serverId))
                .collect(Collectors.toList());

        /** 赛季商店 */
        worldWarShop = staticDataDao.selectWorldWarShopMap();
        /** 阵营军威值奖励 */
        List<StaticWorldWarCampCityAward> staticWorldWarCampCityAwards = staticDataDao
                .selectWorldWarCampCityAwardList();
        if (staticWorldWarCampCityAwards != null) {
            worldWarCampCityAwardMap = new HashMap<>(8);
            staticWorldWarCampCityAwards.forEach(e -> worldWarCampCityAwardMap.put(e.getId(), e));
            worldWarCampCityAward = staticWorldWarCampCityAwards.stream()
                    .collect(Collectors.groupingBy(StaticWorldWarCampCityAward::getWorldWarType));
        }
        /** 世界争霸阵营排行榜 */
        worldWarCampRank = staticDataDao.selectWorldWarCampRankMap();
        /** 世界争霸个人排行榜 */
        List<StaticWorldWarPersonalRank> staticWorldWarPersonalRanks = staticDataDao.selectWorldWarPersonalRankList();
        if (staticWorldWarPersonalRanks != null) {
            worldWarPersonalRank = staticWorldWarPersonalRanks.stream()
                    .collect(Collectors.groupingBy(StaticWorldWarPersonalRank::getWorldWarType));
        }
        /** 世界争霸 任务配置 */
        List<StaticWorldWarTask> staticWorldWarTasks = staticDataDao.selectWorldWarTaskList();
        if (staticWorldWarTasks != null) {
            worldWarTask = new HashMap<>(8);
            staticWorldWarTasks.forEach(e -> worldWarTask.put(e.getId(), e));
            worldWarTaskList = staticWorldWarTasks.stream()
                    .collect(Collectors.groupingBy(StaticWorldWarTask::getWorldWarType));
        }
        /** 世界争霸 周任务配置 */
        List<StaticWorldWarWeekTask> staticWorldWarWeekTasks = staticDataDao.selectWorldWarWeekTaskList();
        if (staticWorldWarWeekTasks != null) {
            worldWarWeekTask = new HashMap<>(8);
            staticWorldWarWeekTasks.forEach(e -> worldWarWeekTask.put(e.getId(), e));
            worldWarWeekTaskList = staticWorldWarWeekTasks.stream()
                    .collect(Collectors.groupingBy(StaticWorldWarWeekTask::getWorldWarType));
        }
        /** 世界争霸 日任务配置 */
        List<StaticWorldWarDailyTask> staticWorldWarDailyTasks = staticDataDao.selectWorldWarDailyTaskList();
        if (staticWorldWarDailyTasks != null) {
            worldWarDailyTask = new HashMap<>(8);
            staticWorldWarDailyTasks.forEach(e -> worldWarDailyTask.put(e.getId(), e));
            worldWarDailyTaskList = staticWorldWarDailyTasks.stream()
                    .collect(Collectors.groupingBy(StaticWorldWarDailyTask::getWorldWarType));
        }
        staticNewYorkWarAchievement = staticDataDao.selectStaticNewYorkWarAchievement();
        staticNewYorkWarCampRank = staticDataDao.selectStaticNewYorkWarCampRank();
        staticNewYorkWarPersonalRank = staticDataDao.selectStaticNewYorkWarPersonalRank();

        // 战火燎原 据点配置
        staticWarFireMap = staticDataDao.selectWarFireList();
        StaticCrossWorldDataMgr.staticWarFireBuffMap = staticDataDao.selectWarFireBuffMap();
        StaticCrossWorldDataMgr.staticWarFireBuffLvMap = staticWarFireBuffMap.values()
                .stream()
                .collect(Collectors.toMap(StaticWarFireBuff::getMapKey, buff -> buff));
        // 战火燎原 个人积分奖励
        staticWarFireRankGrMap = new TreeMap<>();
        staticWarFireRankGrMap.putAll(staticDataDao.selectWarFireRankGr());
        // 战火燎原 阵营排名奖励
        staticWarFireRankCampMap = new TreeMap<>();
        staticWarFireRankCampMap.putAll(staticDataDao.selectWarFireRankCamp());
        staticWarFireRangeMap = staticDataDao.selectWarFireRangeMap();
        staticWarFireShopMap = staticDataDao.selectWarFireShopMap();
    }

    public static StaticWarFireBuff getWarFireBuffByTypeLv(int type, int lv) {
        String key = StaticWarFireBuff.mapKey(type, lv);
        return staticWarFireBuffLvMap.get(key);
    }

    public static StaticWarFireShop getStaticWarFireShopById(int shopId) {
        return staticWarFireShopMap.get(shopId);
    }

    public static Map<Integer, StaticWarFireShop> getStaticWarFireShopMap() {
        return staticWarFireShopMap;
    }

    public static Map<Integer, StaticWarFireRange> getStaticWarFireRangeMap() {
        return staticWarFireRangeMap;
    }

    public static TreeMap<Integer, StaticWarFireRankGr> getStaticWarFireRankGrMap() {
        return staticWarFireRankGrMap;
    }

    public static TreeMap<Integer, StaticWarFireRankCamp> getStaticWarFireRankCampMap() {
        return staticWarFireRankCampMap;
    }

    public static Map<Integer, StaticWarFire> getStaticWarFireMap() {
        return staticWarFireMap;
    }

    public static Map<Integer, StaticCity> getCityMap() {
        return cityMap;
    }

    public static StaticCity getCityById(int cityId) {
        return cityMap.get(cityId);
    }

    public static StaticWorldwarOpen getOpenMapById(int id) {
        return openMap.get(id);
    }

    public static Map<Integer, StaticWorldwarOpen> getOpenMap() {
        return openMap;
    }

    public static List<StaticWorldWarPlan> getWorldWarPlan() {
        return worldWarPlan;
    }

    public static StaticWorldWarPlan getNowPlan() {
        int now = TimeHelper.getCurrentSecond();
        List<StaticWorldWarPlan> worldWarPlan = StaticCrossWorldDataMgr.getWorldWarPlan();
        if (worldWarPlan.isEmpty()) {
            return null;
        }
        return worldWarPlan.stream().sorted(Comparator.comparingLong(wplan -> wplan.getDisplayTime().getTime()))
                .filter(wplan -> {
                    int displayTime = TimeHelper.dateToSecond(wplan.getDisplayTime());// 结束预览
                    return now <= displayTime;
                }).findFirst().orElse(null);
    }

    /**
     * 赛季商店-商品
     *
     * @param id
     * @return
     */
    public static StaticWorldWarShop getStaticWorldWarShop(int id) {
        return worldWarShop.get(id);
    }

    /**
     * 世界争霸阵营排行榜 (根据排名取rank)
     *
     * @param ranking
     * @return
     */
    public static List<StaticWorldWarCampRank> getWorldWarCampRankList(int ranking) {
        return worldWarCampRank.values().stream().collect(Collectors.groupingBy(StaticWorldWarCampRank::getRanking))
                .get(ranking);
    }

    /**
     * 获取 阵营军威值奖励
     *
     * @param id
     * @return
     */
    public static StaticWorldWarCampCityAward getWorldWarCampCityAwardById(int id) {
        return worldWarCampCityAwardMap.get(id);
    }

    /**
     * 获取阵营军威值奖励
     *
     * @param worldWarType
     * @return
     */
    public static List<StaticWorldWarCampCityAward> getWorldWarCampCityAward(int worldWarType) {
        return worldWarCampCityAward.get(worldWarType);
    }

    /**
     * 获取世界争霸个人排行榜（根据世界争霸的档位获取）
     *
     * @param worldWarType
     * @return
     */
    public static List<StaticWorldWarPersonalRank> getWorldWarPersonalRank(int worldWarType) {
        return worldWarPersonalRank.get(worldWarType);
    }

    /**
     * 获取世界争霸每日限定任务
     *
     * @param id
     * @return
     */
    public static StaticWorldWarTask getWorldWarTask(int id) {
        return worldWarTask.get(id);
    }

    /**
     * 获取世界争霸每日限定任务（根据世界争霸的档位获取）
     *
     * @param worldWarType
     * @return
     */
    public static List<StaticWorldWarTask> getWorldWarTaskList(int worldWarType) {
        return worldWarTaskList.get(worldWarType);
    }

    /**
     * 获取世界争霸 周任务
     *
     * @param id
     * @return
     */
    public static StaticWorldWarWeekTask getWorldWarWeekTask(int id) {
        return worldWarWeekTask.get(id);
    }

    /**
     * 获取世界争霸 周任务(根据worldType获取)
     *
     * @param worldType
     * @return
     */
    public static List<StaticWorldWarWeekTask> getWorldWarWeekTaskList(int worldType) {
        return worldWarWeekTaskList.get(worldType);
    }

    /**
     * 获取世界争霸 每日杀敌任务
     *
     * @param id
     * @return
     */
    public static StaticWorldWarDailyTask getWorldWarDailyTask(int id) {
        return worldWarDailyTask.get(id);
    }

    /**
     * 获取世界争霸 每日杀敌任务(根据worldType获取)
     *
     * @param worldType
     * @return
     */
    public static List<StaticWorldWarDailyTask> getWorldWarDailyTaskList(int worldType) {
        return worldWarDailyTaskList.get(worldType);
    }

    /**
     * 获取纽约争霸成就奖励
     */
    public static StaticNewYorkWarAchievement getStaticNewYorkWarAchievement(int id) {
        return staticNewYorkWarAchievement.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    /**
     * 获取纽约争霸成就奖励
     */
    public static List<StaticNewYorkWarAchievement> getStaticNewYorkWarAchievement() {
        return staticNewYorkWarAchievement;
    }

    /**
     * 纽约争霸阵营排行榜奖励
     */
    public static StaticNewYorkWarCampRank getStaticNewYorkWarCampRank(int ranking) {
        return staticNewYorkWarCampRank.stream().filter(s -> s.getRanking() == ranking).findFirst().orElse(null);
    }

    /**
     * 纽约争霸个人排行榜奖励
     */
    public static StaticNewYorkWarPersonalRank getStaticNewYorkWarPersonalRank(int ranking) {
        return staticNewYorkWarPersonalRank.stream().filter(s -> s.getRanking().get(0) <= ranking && ranking <= s.getRanking().get(1)).findFirst().orElse(null);
    }
}
