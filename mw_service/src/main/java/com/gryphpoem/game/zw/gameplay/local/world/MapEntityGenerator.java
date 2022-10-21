package com.gryphpoem.game.zw.gameplay.local.world;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticBanditDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.map.*;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.world.AirshipWorldData;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.random.MineLvRandom;
import com.gryphpoem.game.zw.service.AirshipService;
import com.gryphpoem.game.zw.service.WorldScheduleService;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author QiuKun
 * @ClassName MapEntityGenerator.java
 * @Description 地图物体生成器, 就是刷流寇 矿点 飞艇
 * @date 2019年3月26日
 */
public class MapEntityGenerator {

    private final CrossWorldMap crossWorldMap;
    /**
     * 所有飞艇信息,包括不在地图上的<keyId,AirshipWorldData>
     */
    private final Map<Integer, AirshipWorldData> airshipMap;

    /**
     * 所有地块, 10 * 10, 从1开始，最大36
     */
    private final List<Integer> allBlock = Stream.iterate(1, i -> ++i).limit(25).collect(Collectors.toList());

    public MapEntityGenerator(CrossWorldMap crossWorldMap) {
        this.crossWorldMap = crossWorldMap;
        this.airshipMap = new ConcurrentHashMap<>();
    }

    public Map<Integer, AirshipWorldData> getAirshipMap() {
        return airshipMap;
    }

    /**
     * 初始化地图信息
     */
    public void initMapEntity() {
        // 初始化城池
        initCrossCity();
        // 初始化流寇
        // initAndRefreshBandit();
        // 初始化矿点
        // initAndRefreshMine();
    }

    /**
     * 初始化飞艇信息
     */
    public void initAndRefreshAirship() {
        if (!airshipMap.isEmpty()) {
            airshipMap.values().forEach(a -> crossWorldMap.removeWorldEntity(a.getPos()));
        }
        airshipMap.clear();
        StaticAirshipArea sAirshipArea = StaticWorldDataMgr.getAirshipAreaMap()
                .get(CrossWorldMapConstant.CROSS_AREA_OPEN_ORDER);
        if (sAirshipArea == null || CheckNull.isEmpty(sAirshipArea.getAirship())) return;
        List<AirshipWorldData> aswdList = AirshipService.createAirshipWorldDataList(sAirshipArea.getAirship(),
                crossWorldMap.getMapId());
        List<Integer> emptyPos = findEmptyPos(sAirshipArea.getBlock(), aswdList.size());
        int cnt = Math.min(aswdList.size(), emptyPos.size());
        for (int i = 0; i < cnt; i++) {
            aswdList.get(i).setPos(emptyPos.get(i));
        }
        // 刷飞艇的空点不够
        if (emptyPos.size() < aswdList.size()) {
            LogUtil.error("创建飞艇时空点位置不足 emptyPosSize:", emptyPos.size(), ", airshipSize:", aswdList.size());
        }
        List<MapEvent> events = new ArrayList<>();
        for (AirshipWorldData airship : aswdList) {
            AirshipMapEntity airshipMapEntity = new AirshipMapEntity(airship.getPos(), airship);
            airshipMap.put(airship.getKeyId(), airship);
            crossWorldMap.addWorldEntity(airshipMapEntity);
            events.add(MapEvent.mapEntity(airshipMapEntity.getPos(), MapCurdEvent.CREATE));
            LogUtil.debug("----------新地图飞艇添加 pos:", crossWorldMap.posToStr(airshipMapEntity.getPos()), ", cfgId:",
                    airship.getId(), "----------------");
        }
        crossWorldMap.publishMapEvent(events);
    }

    private List<Integer> findEmptyPos(List<Integer> blockList, int needCnt) {
        int cnt = 0;
        final int blockSize = blockList.size();
        List<Integer> emptyPosList = new ArrayList<>(needCnt);
        while (emptyPosList.size() < needCnt && cnt++ < 100) {
            int index = RandomHelper.randomInSize(blockSize);
            int b = blockList.get(index);
            List<Integer> posList = crossWorldMap.getRandomEmptyPos(b, cnt);
            if (!CheckNull.isEmpty(posList)) {
                emptyPosList.add(posList.get(0));
            }
        }
        return emptyPosList;
    }

    /**
     * 飞艇跑秒处理
     */
    private void processRunSecAirship() {
        try {
            if (!CheckNull.isEmpty(airshipMap)) {
                AirshipService airshipService = DataResource.ac.getBean(AirshipService.class);
                int now = TimeHelper.getCurrentSecond();
                // boolean isInDurationTime = airshipService.isInAirshipDurationTime(now); // 是否在活动期间内
                for (Iterator<Entry<Integer, AirshipWorldData>> it = airshipMap.entrySet().iterator(); it.hasNext(); ) {
                    Entry<Integer, AirshipWorldData> next = it.next();
                    AirshipWorldData aswd = next.getValue();
                    if (aswd.getTriggerTime() < now) { // 下一个触发时间到了
                        // if (isInDurationTime) { // 在活动期间内
                        processRefreshAirship(aswd, now);
                        // } else { // 不在活动期间内
                        //     airshipRunAway(aswd, now);
                        //     it.remove();// 彻底移除飞艇
                        // }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("飞艇跑秒定时器报错 ", e);
        }
    }

    /**
     * 处理刷新逻辑
     *
     * @param aswd
     */
    private void processRefreshAirship(AirshipWorldData aswd, int now) {
        if (aswd.isLiveStatus()) {
            airshipRunAway(aswd, now);
        } else if (aswd.isRefreshStatus()) {
            // 地图上重新生成
            StaticAirshipArea staticAirshipArea = StaticWorldDataMgr.getAirshipAreaMap()
                    .get(CrossWorldMapConstant.CROSS_AREA_OPEN_ORDER);
            StaticAirship sAirship = StaticWorldDataMgr.getAirshipMap().get(aswd.getId());
            if (staticAirshipArea != null && !CheckNull.isEmpty(staticAirshipArea.getBlock())) {
                List<Integer> emptyPos = findEmptyPos(staticAirshipArea.getBlock(), 1); // 空点列表
                if (!CheckNull.isEmpty(emptyPos)) {
                    ChatDataManager chatDataManager = DataResource.ac.getBean(ChatDataManager.class);
                    aswd.setPos(emptyPos.get(0));
                    aswd.setStatus(AirshipWorldData.STATUS_LIVE);
                    aswd.setTriggerTime(now + sAirship.getLiveTime());
                    AirshipService.addNpcForce(aswd, sAirship.getForm());
                    chatDataManager.sendSysChat(ChatConst.CHAT_AIRSHIP_REAPPEAR, aswd.getAreaId(), 0, aswd.getId(),
                            aswd.getPos());
                    // 通知地图
                    AirshipMapEntity airshipMapEntity = new AirshipMapEntity(aswd.getPos(), aswd);
                    crossWorldMap.addWorldEntity(airshipMapEntity);
                    crossWorldMap.publishMapEvent(MapEvent.mapEntity(airshipMapEntity.getPos(), MapCurdEvent.CREATE));
                    LogUtil.debug("----------新地图重新刷新飞艇 : keyId:", aswd.getKeyId(), ", pos:",
                            crossWorldMap.posToStr(aswd.getPos()), ", id:", aswd.getId());
                }
            }
        }
    }

    /**
     * 飞艇逃跑处理
     *
     * @param aswd
     * @param now
     */
    private void airshipRunAway(AirshipWorldData aswd, int now) {
        AirshipMapEntity.returnAllArmyAuto(aswd, crossWorldMap);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        // 发邮件
        aswd.getJoinRoles().values().stream().flatMap(l -> l.stream()).map(br -> br.getRoleId()).distinct()
                .forEach(roleId -> {
                    Player p = playerDataManager.getPlayer(roleId);
                    if (p != null) {
                        mailDataManager.sendNormalMail(p, MailConstant.MOLD_AIRSHIP_RUN_AWAY, now, aswd.getId(),
                                aswd.getId());
                    }
                });
        if (aswd.getBelongRoleId() > 0) {
            Player bp = playerDataManager.getPlayer(aswd.getBelongRoleId());
            boolean belongHasInJoin = aswd.getJoinRoles().values().stream().flatMap(l -> l.stream())
                    .filter(br -> br.getRoleId() == aswd.getBelongRoleId()).count() > 0;
            if (bp != null && !belongHasInJoin)
                mailDataManager.sendNormalMail(bp, MailConstant.MOLD_AIRSHIP_RUN_AWAY, now, aswd.getId(), aswd.getId());
        }
        // 移除飞艇
        removeAirshipFromMap(aswd, now, AirshipWorldData.STATUS_REFRESH);
        LogUtil.debug("----------飞艇逃跑 : keyId:", aswd.getKeyId(), ", pos:", aswd.getPos(), ", id:", aswd.getId());
    }

    /**
     * 地图上移除飞艇
     *
     * @param aswd
     * @param now
     * @param airState {@link AirshipWorldData#STATUS_REFRESH} or {@link AirshipWorldData#STATUS_DEAD_REFRESH}
     */
    public void removeAirshipFromMap(AirshipWorldData aswd, int now, int airState) {
        // 移除飞艇
        int prePos = aswd.getPos();
        crossWorldMap.removeWorldEntity(aswd.getPos());
        aswd.setStatus(airState); // 状态修改
        int rebirthInterval = TimeHelper.HALF_HOUR_S;
        StaticAirship sAirship = StaticWorldDataMgr.getAirshipMap().get(aswd.getId());
        if (sAirship != null) {
            rebirthInterval = sAirship.getRebirthInterval();
        } else {
            LogUtil.error("==========错误飞艇配置缺少 id:", aswd.getId());
        }
        aswd.setTriggerTime(now + rebirthInterval);
        aswd.setPos(-1);
        aswd.getNpc().clear();// 清除npc信息
        aswd.getJoinRoles().clear();// 清除加入人员
        crossWorldMap.publishMapEvent(MapEvent.mapEntity(prePos, MapCurdEvent.DELETE));
    }

    private void refreshMapEntity(List<Integer> blocks, Map<Integer, Integer> sLvAndCnt,
                                  Function<Integer, Map<Integer, Integer>> statisticsFunc,
                                  BiFunction<Integer, Integer, BaseWorldEntity> createBaseEntityFunc) {
        // <id,每块的个数>
        // 每个块的逻辑处理
        for (Integer cellId : blocks) {
            // 统计数量<id,cnt>
            Map<Integer, Integer> idCnt = statisticsFunc.apply(cellId);
            // 计算差值
            for (Entry<Integer, Integer> pm : sLvAndCnt.entrySet()) {
                int id = pm.getKey();
                int cnt = pm.getValue();// 配置数据
                int realCnt = idCnt.getOrDefault(id, 0); // 地图上数据
                int needCnt = cnt - realCnt;
                if (needCnt > 0) {
                    List<Integer> emptyPos = crossWorldMap.getRandomEmptyPos(cellId, needCnt);
                    for (Integer pos : emptyPos) {
                        BaseWorldEntity baseEntity = createBaseEntityFunc.apply(pos, id);
                        if (baseEntity != null) crossWorldMap.addWorldEntity(baseEntity);
                    }
                }
            }
        }
    }

    private static <T> Map<Integer, Integer> mapListToMapCnt(Map<Integer, List<T>> mapList) {
        Map<Integer, Integer> lvCnt = new HashMap<>(mapList.size());
        for (Iterator<Entry<Integer, List<T>>> it = mapList.entrySet().iterator(); it.hasNext(); ) {
            Entry<Integer, List<T>> next = it.next();
            lvCnt.put(next.getKey(), next.getValue().size());
        }
        return lvCnt;
    }

    /**
     * 清除刷新流寇
     */
    public void cleanAndRefreshBandit() {
        for (Iterator<Entry<Integer, BaseWorldEntity>> it = crossWorldMap.getAllMap().entrySet().iterator(); it
                .hasNext(); ) {
            Entry<Integer, BaseWorldEntity> next = it.next();
            BaseWorldEntity worldEntity = next.getValue();
            if (worldEntity.getType() == WorldEntityType.BANDIT) {
                MineMapEntity mineEntity = (MineMapEntity) worldEntity;
                if (mineEntity.getGuard() == null) {
                    it.remove();
                }
            }
        }
        initAndRefreshBandit();
        crossWorldMap.publishMapEvent(MapEvent.mapReload());
    }

    /**
     * 清除刷新矿点
     */
    public void cleanAndRefreshMine() {
        for (Iterator<Entry<Integer, BaseWorldEntity>> it = crossWorldMap.getAllMap().entrySet().iterator(); it
                .hasNext(); ) {
            Entry<Integer, BaseWorldEntity> next = it.next();
            BaseWorldEntity worldEntity = next.getValue();
            if (worldEntity.getType() == WorldEntityType.MINE) {
                MineMapEntity mineEntity = (MineMapEntity) worldEntity;
                if (mineEntity.getGuard() == null) {
                    it.remove();
                }
            }
            //清除叛军
            if (worldEntity.getType() == WorldEntityType.BANDIT) {
                it.remove();
            }
        }
        initAndRefreshMine();
        crossWorldMap.publishMapEvent(MapEvent.mapReload());
    }

    /**
     * 初始化矿点
     */
    public void initAndRefreshMine() {
        LogUtil.world("新地图刷新Mine mapId=", crossWorldMap.getMapId());
        List<StaticAreaRange> cfgList = StaticWorldDataMgr.getBlockRangeMap()
                .get(CrossWorldMapConstant.CROSS_AREA_OPEN_ORDER);
        if (cfgList == null || cfgList.isEmpty()) {
            return;
        }
        BiFunction<Integer, Integer, BaseWorldEntity> createBaseEntityFunc = (pos, lv) -> {
            StaticMine mine = MineLvRandom.randomNewMapMineByLv(lv);
            if (mine == null) return null;
            int remainRes = mine.getReward().get(0).get(2);
            return new MineMapEntity(pos, mine.getMineId(), mine.getLv(), mine.getMineType(), remainRes);
        };

        Function<Integer, Map<Integer, Integer>> statisticsFunc = (cellId) -> {
            List<Integer> cellPosList = crossWorldMap.getCellPosList(cellId);
            Map<Integer, List<MineMapEntity>> lvListMap = cellPosList.stream()
                    .map(pos -> crossWorldMap.getAllMap().get(pos))
                    .filter(baseEntity -> baseEntity != null && baseEntity.getType() == WorldEntityType.MINE)
                    .map(baseEntity -> (MineMapEntity) baseEntity)
                    .collect(Collectors.groupingBy(MineMapEntity::getLv));
            return mapListToMapCnt(lvListMap);
        };
        for (StaticAreaRange sar : cfgList) {
            List<Integer> blocks = sar.getBlock();
            Map<Integer, Integer> sLvAndCnt = sar.getMineLv();
            refreshMapEntity(blocks, sLvAndCnt, statisticsFunc, createBaseEntityFunc);
        }
    }

    /**
     * 初始化流寇
     */
    public void initAndRefreshBandit() {
        LogUtil.world("新地图刷新Bandit mapId=", crossWorldMap.getMapId());
        //        StaticBanditArea sba = StaticBanditDataMgr.getBanditAreaMap().get(CrossWorldMapConstant.CROSS_AREA_OPEN_ORDER);
        int currWorldSchedule = DataResource.ac.getBean(WorldScheduleService.class).getCurrentSchduleId();
        List<StaticBanditArea> staticBanditAreaList = StaticBanditDataMgr.getStaticBanditAreaByAreaOrder(CrossWorldMapConstant.CROSS_AREA_OPEN_ORDER);
        if (CheckNull.isEmpty(staticBanditAreaList)) {
            return;
        }
        StaticBanditArea sba = staticBanditAreaList.stream().filter(o -> o.getSchedule().get(0) <= currWorldSchedule && o.getSchedule().get(1) >= currWorldSchedule).findFirst().orElse(null);
        if (sba == null) {
            return;
        }
        List<Integer> blocks = sba.getBlock();
        // <id,每块的个数>
        Map<Integer, Integer> sLvAndCnt = sba.getBandits();
        // 创建流寇
        BiFunction<Integer, Integer, BaseWorldEntity> createBaseEntityFunc = BanditMapEntity::new;
        // 统计流寇
        Function<Integer, Map<Integer, Integer>> statisticsFunc = (cellId) -> {
            List<Integer> cellPosList = getCrossWorldMap().getCellPosList(cellId);
            Map<Integer, List<BanditMapEntity>> idBanditList = cellPosList.stream()
                    .map(pos -> crossWorldMap.getAllMap().get(pos))
                    .filter(baseEntity -> baseEntity != null && baseEntity.getType() == WorldEntityType.BANDIT)
                    .map(baseEntity -> (BanditMapEntity) baseEntity)
                    .collect(Collectors.groupingBy(BanditMapEntity::getBanditId));
            return mapListToMapCnt(idBanditList);
        };
        // 创新流寇
        refreshMapEntity(blocks, sLvAndCnt, statisticsFunc, createBaseEntityFunc);
    }

    public void clearAndInitCity() {
        getCrossWorldMap().getAllMap().entrySet()
                .stream()
                .filter(en -> en.getValue() instanceof WFCityMapEntity)
                .forEach(en -> getCrossWorldMap().removeWorldEntity(en.getKey()));
        getCrossWorldMap().getCityMap().clear();
        initCrossCity();
    }

    /**
     * 初始城池
     */
    public void initCrossCity() {
        Map<Integer, StaticCity> cityMap = StaticCrossWorldDataMgr.getCityMap();
        Map<Integer, StaticWarFire> wfCityMap = StaticCrossWorldDataMgr.getStaticWarFireMap();
        for (StaticCity staticCity : cityMap.values()) {
            if (staticCity.getArea() != getCrossWorldMap().getMapId()) {
                continue;
            }
            City city = new City();
            int cityId = staticCity.getCityId();
            city.setCityId(cityId);
            city.setCityLv(staticCity.getLv());
            city.setCamp(staticCity.getCamp());// 城池初始属于系统
            city.setStatus(WorldConstant.CITY_STATUS_CALM);
            CityMapEntity cityMapEntity = wfCityMap.containsKey(cityId) ? new WFCityMapEntity(staticCity.getCityPos(), city) : new CityMapEntity(staticCity.getCityPos(), city);
            getCrossWorldMap().addWorldEntityMultPos(cityMapEntity, staticCity.getPosList());
            getCrossWorldMap().addCityMapEntity(cityMapEntity);
        }
    }

    /**
     * 初始化安全区
     */
    public void initAndRefreshWfSafeArea() {
        List<WFSafeAreaMapEntity> campSafeArea = crossWorldMap.getSafeArea();
        // 初始化安全区
        if (campSafeArea.size() == 0) {
            for (int i = 0; i < WorldConstant.WAR_FIRE_SAFE_AREA.size(); i++) {
                int safeId = i + 1;
                List<Integer> xy = WorldConstant.WAR_FIRE_SAFE_AREA.get(i);
                int pos = crossWorldMap.xyToPos(xy.get(0), xy.get(1));
                int cell = crossWorldMap.posToCell(pos);
                WFSafeAreaMapEntity safeAreaMapEntity = new WFSafeAreaMapEntity(pos);
                safeAreaMapEntity.setSafeId(safeId);
                safeAreaMapEntity.setCellId(cell);
                crossWorldMap.addWorldEntity(safeAreaMapEntity);
            }
        } else {
            for (int i = 0; i < WorldConstant.WAR_FIRE_SAFE_AREA.size(); i++) {
                int safeId = i + 1;
                List<Integer> xy = WorldConstant.WAR_FIRE_SAFE_AREA.get(i);
                int pos = crossWorldMap.xyToPos(xy.get(0), xy.get(1));
                int cell = crossWorldMap.posToCell(pos);
                campSafeArea.stream()
                        .filter(csa -> csa.getSafeId() == safeId && (csa.getCellId() != cell || csa.getPos() != pos))
                        .forEach(csa -> {
                            // 移除旧的安全区
                            int hisPos = csa.getPos();
                            crossWorldMap.removeWorldEntity(hisPos);
                            // 添加新的区块
                            WFSafeAreaMapEntity safeAreaMapEntity = new WFSafeAreaMapEntity(pos);
                            safeAreaMapEntity.setSafeId(safeId);
                            safeAreaMapEntity.setCellId(cell);
                            crossWorldMap.addWorldEntity(safeAreaMapEntity);
                        });
            }
        }

    }

    /**
     * 初始化战火燎原资源点
     *
     * @param addCnt 需要刷新的数量
     */
    public void initAndRefreshWFMine(int addCnt) {
        LogUtil.world("战火燎原地图刷新WFMine mapId=", crossWorldMap.getMapId());
        Map<Integer, StaticWarFireRange> sWFRangeMap = StaticCrossWorldDataMgr.getStaticWarFireRangeMap();
        if (CheckNull.isEmpty(sWFRangeMap)) {
            LogUtil.error("战火燎原地图刷新, 未找到资源点配置", crossWorldMap.getMapId());
            return;
        }
        sWFRangeMap.values().stream().findAny().ifPresent(sWFRange -> {
            List<Integer> block = sWFRange.getBlock();
            // 刷新的区块，如果没有配置
            List<Integer> blocks = sWFRange.getType() == StaticWarFireRange.RANGE_TYPE_1 && CheckNull.isEmpty(block) ? getSafetyZoneBlock() : block;
            if (CheckNull.isEmpty(blocks)) {
                LogUtil.error("战火燎原地图刷新, 未找到资源点刷新区块配置", crossWorldMap.getMapId());
                return;
            }
            List<Integer> emptyPos = findEmptyPos(blocks, addCnt);
            if (emptyPos.size() < addCnt) {
                LogUtil.error("战火燎原地图刷新, 没有足够的空闲坐标", crossWorldMap.getMapId());
            }
            // 事件
            List<MapEvent> events = new ArrayList<>();
            for (Integer pos : emptyPos) {
                BaseWorldEntity baseEntity = new WFMineMapEntity(pos, sWFRange.getId(), 0, sWFRange.getType(), sWFRange.getResource());
                crossWorldMap.addWorldEntity(baseEntity);
                events.add(MapEvent.mapEntity(pos, MapCurdEvent.CREATE));
            }

            // 通知地图刷新
            crossWorldMap.publishMapEvent(events);
        });

    }

    /**
     * 取安全区以外的区块
     *
     * @return 区块
     */
    public List<Integer> getSafetyZoneBlock() {
        // 所有的安全区
        List<Integer> safeCell = getCrossWorldMap().getAllMap()
                .values()
                .stream()
                .filter(entity -> entity.getType() == WorldEntityType.WAR_FIRE_SAFE_AREA)
                .map(entity -> (WFSafeAreaMapEntity) entity)
                .map(WFSafeAreaMapEntity::getCellId)
                .collect(Collectors.toList());
        return allBlock.stream().filter(block -> !safeCell.contains(block)).collect(Collectors.toList());
    }

    public CrossWorldMap getCrossWorldMap() {
        return crossWorldMap;
    }

    /**
     * 跑秒执行
     */
    public void runSec() {
        processRunSecAirship();
    }

}
