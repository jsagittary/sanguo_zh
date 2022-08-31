package com.gryphpoem.game.zw.gameplay.local.world;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.util.CrossMapSerHelper;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEventHelper;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.battle.MapWarData;
import com.gryphpoem.game.zw.gameplay.local.world.map.*;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.GlobalWarFire;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.PlayerWarFire;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.WarFireBuff;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.MapChgEventPb;
import com.gryphpoem.game.zw.pb.GamePb5.SyncCrossMapChgRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.DbCrossMap;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFire;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFireBuff;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author QiuKun
 * @ClassName CrossWorldMap.java
 * @Description 跨服地图
 * @date 2019年3月20日
 */
public class CrossWorldMap extends WatcherWorldMap {

    /**
     * 玩家关信息<roleId,Player> ,仅仅只是为了缓存而已,好获取
     */
    private final Map<Long, PlayerMapEntity> playerMap;
    /**
     * 城池相关信息<cityId,city> ,仅仅只是为了缓存而已,好获取
     */
    private final Map<Integer, CityMapEntity> cityMap;
    /**
     * 整个地图上数据
     */
    private final Map<Integer, BaseWorldEntity> allMap;
    /**
     * 部队行军相关信息
     */
    private final MapMarch mapMarchArmy;
    /**
     * 战斗相关信息
     */
    private final MapWarData mapWarData;
    /**
     * 地图物体刷新管理
     */
    private final MapEntityGenerator mapEntityGenerator;
    /**
     * 默认开放外圈,读取s_worldwar_open表的block字段
     */
    private MapOpenType mapOpenType = MapOpenType.OUTER;
    /**
     * 开放时间
     */
    private final WorldWarOpen worldWarOpen;
    /**
     * 战火燎原
     */
    private final GlobalWarFire globalWarFire;

    @Autowired
    private PlayerDataManager playerDataManager;

    public CrossWorldMap(int mapId, int width, int height, int cellSize) {
        super(mapId, width, height, cellSize);
        cityMap = new ConcurrentHashMap<>();
        playerMap = new ConcurrentHashMap<>();
        allMap = new ConcurrentHashMap<>();
        mapMarchArmy = new MapMarch(this);
        mapWarData = new MapWarData(this);
        mapEntityGenerator = new MapEntityGenerator(this);
        worldWarOpen = new WorldWarOpen(this);
        globalWarFire = new GlobalWarFire(this);
    }

    public Map<Integer, CityMapEntity> getCityMap() {
        return cityMap;
    }

    public Map<Integer, BaseWorldEntity> getAllMap() {
        return allMap;
    }

    public Map<Long, PlayerMapEntity> getPlayerMap() {
        return playerMap;
    }

    public MapMarch getMapMarchArmy() {
        return mapMarchArmy;
    }

    public MapWarData getMapWarData() {
        return mapWarData;
    }

    public MapEntityGenerator getMapEntityGenerator() {
        return mapEntityGenerator;
    }

    public MapOpenType getMapOpenType() {
        return mapOpenType;
    }

    public void setMapOpenType(MapOpenType mapOpenType) {
        this.mapOpenType = mapOpenType;
    }

    public CityMapEntity getCityMapEntityByCityId(int cityId) {
        return cityMap.get(cityId);
    }

    /**
     * 获取安全区
     * @return 安全区
     */
    public List<WFSafeAreaMapEntity> getSafeArea() {
        // 安全区
        return allMap.values()
                .stream()
                .filter(ce -> ce.getType() == WorldEntityType.WAR_FIRE_SAFE_AREA)
                .map(ce -> (WFSafeAreaMapEntity) ce)
                .collect(Collectors.toList());
    }

    /**
     * 获取阵营的安全区
     * @param camp 阵营
     * @return 指定阵营的安全区, or Null
     */
    public WFSafeAreaMapEntity getCampSafeArea(int camp) {
        return getSafeArea()
                .stream()
                .filter(safe -> safe.getCamp() == camp)
                .findAny()
                .orElse(null);
    }

    /**
     * 序列化
     *
     * @return
     */
    public DbCrossMap toDbCrossMap() {
        return CrossMapSerHelper.toDbCrossMap(this);
    }

    /**
     * 是否在赛季中,定时器只有在赛季中才会去执行
     *
     * @return true 表示在赛季中
     */
    public boolean isInSeason() {
        WorldWarPlanInfo worldWarPlanInfo = worldWarOpen.getWorldWarPlanInfo();
        // 赛季进行中
        if (worldWarPlanInfo != null && worldWarPlanInfo.getStage() == WorldWarPlanInfo.STAGE_SEASON_RUNNING) {
            return true;
        }
        return false;
    }

    /**
     * 是否允许开启纽约争霸活动 (世界争霸开启5周之后)
     */
    public boolean checkOpenNewYorkWar() {
        WorldWarPlanInfo worldWarPlanInfo = worldWarOpen.getWorldWarPlanInfo();
        int now = TimeHelper.getCurrentSecond();
        if (worldWarPlanInfo != null
                && worldWarPlanInfo.getStage() == WorldWarPlanInfo.STAGE_SEASON_RUNNING
                && worldWarPlanInfo.getDisplayBegin() + 60 * 60 * 24 * 7 * (WorldConstant.NEWYORK_WAR_BEGIN_WEEK - 1) <= now) {
            return true;
        }
        return false;
    }


    /**
     * 地图上添加物体
     *
     * @param entity
     */
    public void addWorldEntity(BaseWorldEntity entity) {
        allMap.put(entity.getPos(), entity);
        if (entity.getType() == WorldEntityType.PLAYER) {
            PlayerMapEntity pEntity = (PlayerMapEntity) entity;
            playerMap.put(pEntity.getPlayer().roleId, pEntity);
        }
    }

    public BaseWorldEntity removeWorldEntity(int pos) {
        BaseWorldEntity entity = allMap.remove(pos);
        if (entity != null && entity.getType() == WorldEntityType.PLAYER) {
            PlayerMapEntity pEntity = (PlayerMapEntity) entity;
            playerMap.remove(pEntity.getPlayer().roleId);
        }
        return entity;
    }

    public WorldWarOpen getWorldWarOpen() {
        return worldWarOpen;
    }

    public GlobalWarFire getGlobalWarFire() {
        return globalWarFire;
    }

    /**
     * 是否是个空点
     *
     * @param pos
     * @return
     */
    public boolean isEmptyPos(int pos) {
        return checkPoxIsValid(pos) && !allMap.containsKey(pos);
    }

    /**
     * 获取某个块的空点
     *
     * @param cellId
     * @return
     */
    public List<Integer> getEmptyPosList(int cellId) {
        List<Integer> cellPosList = getCellPosList(cellId);
        return cellPosList.stream()
                .filter(this::isEmptyPos)
                .collect(Collectors.toList());
    }

    /**
     * 获取以某个点为中心,周围的坐标点(包括本身), 可用的空点
     *
     * @param pos
     * @param radius 半径
     * @return
     */
    public List<Integer> getRoundEmptyPos(int pos, int radius) {
        List<Integer> posList = getRoundPos(pos, radius);
        return posList.stream()
                .filter(this::isEmptyPos)
                .collect(Collectors.toList());
    }

    /**
     * 随机获取空点
     *
     * @param cellId
     * @param cnt    需要几个点
     * @return
     */
    public List<Integer> getRandomEmptyPos(int cellId, int cnt) {
        List<Integer> emptyPosList = getEmptyPosList(cellId);
        if (emptyPosList == null || emptyPosList.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.shuffle(emptyPosList);
        return emptyPosList.stream()
                .limit(cnt)
                .collect(Collectors.toList());
    }

    public int getEmptyPosSafeArea(int camp) {
        WFSafeAreaMapEntity campSafeArea = getCampSafeArea(camp);
        if (Objects.nonNull(campSafeArea)) {
            List<Integer> block = Collections.singletonList(campSafeArea.getCellId());
            int size = block.size();
            int cnt = 0;
            while (cnt < size) {
                int openCellId = block.get(RandomHelper.randomInSize(block.size()));
                List<Integer> randomEmptyPos = getRandomEmptyPos(openCellId, 1);
                if (!randomEmptyPos.isEmpty()) {
                    return randomEmptyPos.get(0);
                }
                cnt++;
            }
            LogUtil.error("新地图没有充足的空点 mapId:", getMapId());
        } else {
            LogUtil.error("新地图安全区没有初始化安全区, mapId:", getMapId(), ", camp:", camp);
        }
        return 0;
    }

    /**
     * 随机获取一个阵营安全区内的随机点
     *
     * @param camp 阵营
     * @return 随机坐标
     */
    public int getRandomOpenEmptyPosSafeArea(int camp) {
        WFSafeAreaMapEntity campSafeArea = getCampSafeArea(camp);
        if (Objects.nonNull(campSafeArea)) {
            List<Integer> block = Collections.singletonList(campSafeArea.getCellId());
            int size = block.size();
            int cnt = 0;
            while (cnt < size) {
                int openCellId = block.get(RandomHelper.randomInSize(block.size()));
                List<Integer> randomEmptyPos = getRandomEmptyPos(openCellId, 1);
                if (!randomEmptyPos.isEmpty()) {
                    return randomEmptyPos.get(0);
                }
                cnt++;
            }
            cnt = 0;
            // 地方满额外的地方
            List<Integer> cellIdList = Constant.CROSS_MAP_RANDOM_CELLID.get(camp);
            while (cnt < size) {
                int openCellId = cellIdList.get(RandomHelper.randomInSize(cellIdList.size()));
                List<Integer> randomEmptyPos = getRandomEmptyPos(openCellId, 1);
                if (!randomEmptyPos.isEmpty()) {
                    return randomEmptyPos.get(0);
                }
                cnt++;
            }
            LogUtil.error("新地图没有充足的空点 mapId:", getMapId());
        } else {
            LogUtil.error("新地图安全区没有初始化安全区, mapId:", getMapId(), ", camp:", camp);
        }
        return 0;
    }

    public int getRandomOpenEmptyPos(int camp) {
        List<Integer> block = mapEntityGenerator.getSafetyZoneBlock();
        if (!CheckNull.isEmpty(block)) {
            int size = block.size();
            int cnt = 0;
            while (cnt < size) {
                int openCellId = block.get(RandomHelper.randomInSize(block.size()));
                List<Integer> randomEmptyPos = getRandomEmptyPos(openCellId, 1);
                if (!randomEmptyPos.isEmpty()) {
                    return randomEmptyPos.get(0);
                }
                cnt++;
            }
            cnt = 0;
            // 地方满额外的地方
            List<Integer> cellIdList = Constant.CROSS_MAP_RANDOM_CELLID.get(camp);
            while (cnt < size) {
                int openCellId = cellIdList.get(RandomHelper.randomInSize(cellIdList.size()));
                List<Integer> randomEmptyPos = getRandomEmptyPos(openCellId, 1);
                if (!randomEmptyPos.isEmpty()) {
                    return randomEmptyPos.get(0);
                }
                cnt++;
            }
            LogUtil.error("新地图没有充足的空点 mapId:", getMapId());
        }
        return 0;
    }

    /**
     * 玩家是否在安全区内
     *
     * @param player 玩家
     * @return true 安全区内
     */
    public boolean isInSafeArea(Player player) {
        if (player.lord.getArea() != CrossWorldMapConstant.CITY_AREA) {
            return false;
        }
        // 玩家坐标
        int pos = player.lord.getPos();
        // 阵营
        int camp = player.lord.getCamp();
        WFSafeAreaMapEntity campSafeArea = getCampSafeArea(camp);
        return Objects.nonNull(campSafeArea) && campSafeArea.getCellId() == posToCell(pos);
    }

    /**
     * 初始化地图信息
     */
    public void initData() {
        mapEntityGenerator.initMapEntity();
    }

    /**
     * 用于 一个物体占用多个格子使用
     *
     * @param entity
     * @param posList
     */
    public void addWorldEntityMultPos(BaseWorldEntity entity, List<Integer> posList) {
        if (posList != null && !posList.isEmpty()) {
            for (Integer pos : posList) {
                allMap.put(pos, entity);
            }
        } else {
            addWorldEntity(entity);
        }
    }

    /**
     * 添加city 只会在初始时候使用
     *
     * @param city
     */
    public void addCityMapEntity(CityMapEntity city) {
        cityMap.put(city.getCity().getCityId(), city);
    }

    /**
     * 发布地图改变事件
     *
     * @param events
     */
    public void publishMapEvent(Collection<MapEvent> events) {
        if (events == null || events.isEmpty()) return;
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        List<Player> players = getPlayerCareCellId().keySet().stream()
                .map(playerDataManager::getPlayer)
                .filter(p -> p != null && p.ctx != null)
                .collect(Collectors.toList());
        for (Player player : players) {
            noticePlayerEvent(player, events);
        }
    }

    /**
     * 发布地图改变事件
     *
     * @param events
     */
    public void publishMapEvent(MapEvent... events) {
        if (events == null) return;
        List<MapEvent> eventList = new ArrayList<>(events.length);
        eventList.addAll(Arrays.asList(events));
        publishMapEvent(eventList);
    }

    private void noticePlayerEvent(Player player, Collection<MapEvent> events) {
        if (events == null || events.isEmpty()) return;
        Set<Integer> careCellSet = getPlayerCareCellId().get(player.roleId);
        if (careCellSet == null || careCellSet.isEmpty()) return;
        List<MapChgEventPb> mapEventPbs = new ArrayList<>(events.size());
        for (MapEvent event : events) {
            if (event.isAllMapNotice()) {
                MapChgEventPb mapChgEventPb = MapEventHelper.transformMapChgEventPb(player, event, this);
                if (mapChgEventPb != null) mapEventPbs.add(mapChgEventPb);
            } else {
                int cell = posToCell(event.getPos());
                if (careCellSet.contains(cell)) { // 关注了才 给值
                    MapChgEventPb mapChgEventPb = MapEventHelper.transformMapChgEventPb(player, event, this);
                    if (mapChgEventPb != null) mapEventPbs.add(mapChgEventPb);
                }
            }
        }
        sendSyncCrossMapChgMsg(player, mapEventPbs);
    }

    private void sendSyncCrossMapChgMsg(Player player, List<MapChgEventPb> mapEventPbs) {
        if (player.ctx != null && player.isLogin) {
            SyncCrossMapChgRs.Builder builder = SyncCrossMapChgRs.newBuilder();
            builder.setMapId(getMapId());
            builder.addAllEvent(mapEventPbs);
            Base.Builder msg = PbHelper.createSynBase(SyncCrossMapChgRs.EXT_FIELD_NUMBER, SyncCrossMapChgRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 跨天处理、反序列化执行
     */
    public void onAcrossTheDayRun() {
        worldWarOpen.onAcrossDayRun();
        globalWarFire.onAcrossDayRun();
    }

    /**
     * 跑秒定时器
     */
    public void runSec() {
        mapMarchArmy.runSec();
        mapWarData.runSec();
//        globalWarFire.runSec();
        // 战火燎原地图没有叛军
        // mapEntityGenerator.runSec();
    }

    public void refreshAirship() {
        // mapEntityGenerator.initAndRefreshAirship();
    }

    /**
     * 获取玩家阵营已获取的城池buff
     * @param player 玩家对象
     * @return 城池buff, key: buff类型, value: buff效果值
     */
    public Map<Integer, Integer> getCityBuff(Player player) {
        // 玩家阵营
        int camp = player.lord.getCamp();
        int now = TimeHelper.getCurrentSecond();
        return cityMap.values()
                .stream()
                .filter(cityEntity -> {
                    if (cityEntity instanceof WFCityMapEntity) {
                        // 战火燎原城池, 并且是当前的阵营的所属, 已经过了等待占领的时间
                        WFCityMapEntity wfCityEntity = (WFCityMapEntity) cityEntity;
                        return now >= wfCityEntity.getOccupyTime() && wfCityEntity.getCity().getCamp() == camp;
                    }
                    return false;
                })
                .map(cityEntity -> StaticCrossWorldDataMgr.getStaticWarFireMap().get(cityEntity.getCity().getCityId()))
                .filter(swf -> Objects.nonNull(swf) && !CheckNull.isEmpty(swf.getBuff()))
                .flatMap(swf -> swf.getBuff().stream())
                .collect(Collectors.toMap(buff -> buff.get(0), buff -> buff.get(1), Integer::sum));
    }

    /**
     * 伤兵恢复
     * @param attacker 进攻方
     * @param defender 防守方
     * @param recoverArmyAwardMap 伤兵恢复
     */
    public void rebelBuffRecoverArmy(Fighter attacker, Fighter defender, Map<Long, List<CommonPb.Award>> recoverArmyAwardMap) {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        if (globalWarFire.getStage() == GlobalWarFire.STAGE_RUNNING) {
            List<Force> forces = Stream.of(attacker.forces, defender.forces)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            if (!CheckNull.isEmpty(forces)) {
                forces.stream()
                        .filter(force -> force.totalLost > 0)
                        .forEach(force -> {
                            int id = force.id;
                            long roleId = force.ownerId;
                            Optional.ofNullable(playerDataManager.getPlayer(roleId))
                                    .ifPresent(player -> {
                                        int addRatio = 0;
                                        Map<Integer, Integer> cityBuff = getCityBuff(player);
                                        if (!CheckNull.isEmpty(cityBuff)) {
                                            addRatio += cityBuff.getOrDefault(StaticWarFire.BUFF_TYPE_2, 0);
                                        }
                                        PlayerWarFire pwf = globalWarFire.getPlayerWarFire(roleId);
                                        WarFireBuff buff = pwf.getBuffs().get(StaticWarFireBuff.BUFF_TYPE_RECOVER_ARMY);
                                        if (Objects.nonNull(buff)) {
                                            StaticWarFireBuff sBuff = StaticCrossWorldDataMgr.getWarFireBuffByTypeLv(buff.getType(), buff.getLv());
                                            if (Objects.nonNull(sBuff)) {
                                                addRatio += sBuff.getBuffVal();
                                            }
                                        }
                                        if (addRatio > 0) {
                                            int recArmy = (int) (force.totalLost * (addRatio / Constant.TEN_THROUSAND));
                                            if (recArmy > 0) {
                                                Hero hero = player.heros.get(id);
                                                if (Objects.isNull(hero)) {
                                                    return;
                                                }
                                                StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                                                if (!CheckNull.isNull(staticHero)) {
                                                    int heroArmyCapacity = hero.getAttr()[HeroConstant.ATTR_LEAD];
                                                    int addArm = recArmy + hero.getCount() >= heroArmyCapacity
                                                            ? heroArmyCapacity - hero.getCount() : recArmy;
                                                    // 返回兵力
                                                    hero.addArm(addArm);
                                                    int armyType = staticHero.getType();
                                                    List<CommonPb.Award> awards = recoverArmyAwardMap.computeIfAbsent(roleId, (k) -> new ArrayList<>());
                                                    awards.add(PbHelper.createAwardPb(AwardType.ARMY, armyType, addArm));
                                                    LogUtil.debug("战火燎原回复兵力 roleId:", roleId, ", heroId:", hero.getHeroId(), ", recArm:", addArm);
                                                    //记录玩家兵力变化信息
                                                    // LogLordHelper.filterHeroArm(AwardFrom.WAR_FIRE_BATTLE, player.account, player.lord, hero.getHeroId(), hero.getCount(), addArm,
                                                    //         Constant.ACTION_ADD, armyType, hero.getQuality());

                                                    // 上报玩家兵力变化信息
                                                    LogLordHelper.playerArm(
                                                            AwardFrom.WAR_FIRE_BATTLE,
                                                            player,
                                                            armyType,
                                                            Constant.ACTION_ADD,
                                                            addArm,
                                                            playerDataManager.getArmCount(player.resource, armyType)
                                                    );
                                                }
                                            }
                                        }
                                    });
                        });
            }
        }
    }


}
