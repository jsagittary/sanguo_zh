package com.gryphpoem.game.zw.gameplay.local.manger;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.eventbus.Subscribe;
import com.gryphpoem.game.zw.core.eventbus.ThreadMode;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.util.CrossMapSerHelper;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.gameplay.local.world.map.CityMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.newyork.NewYorkWar;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.GlobalWarFire;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.DataSaveConstant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.dao.impl.p.CrossMapDao;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.DbCrossMap;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.server.SaveCrossMapServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant.CROSS_MAP_ID;
import static com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant.NEW_YORK_CITY_ID;

/**
 * @ClassName CrossWorldMapDataManager.java
 * @Description 跨服地图管理,主要用于加载数据,数据存储相关
 * @author QiuKun
 * @date 2019年3月20日
 */
@Component
public class CrossWorldMapDataManager {

    /** 所有的地图 */
    private Map<Integer, CrossWorldMap> crossWorldMapMap = new HashMap<>();

    /** 纽约争霸数据 */
    private NewYorkWar newYorkWar;

    @Autowired
    private CrossMapDao crossMapDao;

    /**
     * 初始化
     * 
     * @throws InvalidProtocolBufferException
     */
    public void init() throws InvalidProtocolBufferException {
        Map<Integer, DbCrossMap> allDbMap = crossMapDao.selectAllMap();

        CrossWorldMap crossWorldMap = new CrossWorldMap(CrossWorldMapConstant.CROSS_MAP_ID,
                CrossWorldMapConstant.CROSS_MAP_WEIGHT, CrossWorldMapConstant.CROSS_MAP_HEIGHT,
                CrossWorldMapConstant.CROSS_MAP_CELLSIZE);
        loadFromDb(crossWorldMap, allDbMap);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPlayerLogout(Events.PlayerLoginLogoutEvent event) {
        if (!event.isLogin) {
            Player player = event.player;
            Java8Utils.syncMethodInvoke(() -> execCrossMap(cMap -> cMap.rmWatcher(player.roleId)));
        }
    }

    /**
     * 从数据库进行加载数据填充到
     * 
     * @param cMap
     * @return false 表示没有从数据库加载
     * @throws InvalidProtocolBufferException
     */
    private void loadFromDb(CrossWorldMap cMap, Map<Integer, DbCrossMap> allDbMap)
            throws InvalidProtocolBufferException {
        // 数据查询
        DbCrossMap dbMap = allDbMap.get(cMap.getMapId());
        boolean isInit = false;
        if (dbMap == null) { // 说明没有数据
            cMap.initData();
            isInit = true;
        } else {
            // 数据加载
            CrossMapSerHelper.loadFromDbCrossMap(dbMap, cMap);
        }
        if (cMap.getCityMap().size() != StaticCrossWorldDataMgr.getStaticWarFireMap().size()) {
            cMap.getMapEntityGenerator().initCrossCity();
        }
        // 初始化或者刷新安全区
        cMap.getMapEntityGenerator().initAndRefreshWfSafeArea();
        crossWorldMapMap.put(cMap.getMapId(), cMap);
        // 赛季和战火燎原初始化检测
        onAcrossTheDayRun();
        if (isInit) {
            saveCrossMap(false);
        }
    }

    /**
     * 根据地图id
     * 
     * @param mapId 地图id
     * @return 地图数据
     */
    public CrossWorldMap getCrossWorldMapById(int mapId) {
        return crossWorldMapMap.get(mapId);
    }

    public Map<Integer, CrossWorldMap> getCrossWorldMapMap() {
        return crossWorldMapMap;
    }

    /**
     * 删除玩家地图焦点
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING) // 都是在同一个线程进行运行 避免多线程带来的问题
    public void rmMapFocus(Events.RmMapFocusEvent event) {
        long roleId = event.player.roleId;
        LogUtil.debug("crossMap焦点:", roleId);
        // 移除区的焦点
        Java8Utils.syncMethodInvoke(() -> {
            for (CrossWorldMap cMap : crossWorldMapMap.values()) {
                cMap.removeWatcher(roleId);
            }
        });
    }

    // 记录最后一次保存数据的时间
    private int lastSaveTime;

    /**
     * 定时保存
     */
    public void saveTimerLogic() {
        int now = TimeHelper.getCurrentSecond();
        if (now - lastSaveTime >= DataSaveConstant.CROSS_MAP_DATA_SAVE_INTERVAL_SECOND) {
            saveCrossMap(true);
            lastSaveTime = now;
        }
    }

    /**
     * 保存数据
     * 
     * @param isTimer 是否是定时器保存
     * @return
     */
    public int saveCrossMap(boolean isTimer) {
        int count = 0;
        for (CrossWorldMap cMap : crossWorldMapMap.values()) {
            try {
                if (isTimer && !cMap.isInSeason()) { // 不在赛季中 不进行保存
                    continue;
                }
                DbCrossMap data = cMap.toDbCrossMap();
                SaveCrossMapServer.getIns().saveData(data);
                count++;
            } catch (Exception e) {
                LogUtil.error("保存数据出错", e);
            }
        }
        return count;
    }

    /** 移除纽约争夺战,未进行战斗逻辑 */
    public void removeNewYorkWarBattle() {
        CrossWorldMap crossWorldMap = getCrossWorldMapById(CROSS_MAP_ID);
        CityMapEntity cityMapEntity = crossWorldMap.getCityMapEntityByCityId(NEW_YORK_CITY_ID);
        crossWorldMap.getMapWarData().getAllBattles().values().stream()
                .filter(baseMapBattle -> baseMapBattle.getBattle().getType() == WorldConstant.BATTLE_TYPE_NEW_YORK_WAR)
                .forEach(baseMapBattle -> {
                    baseMapBattle.cancelBattleAndReturnArmyNoPush(crossWorldMap.getMapWarData(),
                            BaseMapBattle.CancelBattleType.ATKCANCEL);
                    City city = cityMapEntity.getCity();
                    city.setStatus(WorldConstant.CITY_STATUS_CALM);
                });

    }

    // 此处会运行到保存线程
    public void updateDbCrossMap(DbCrossMap dbCrossMap) {
        crossMapDao.save(dbCrossMap);
    }

    /**
     * 转点执行
     */
    public void onAcrossTheDayRun() {
        execCrossMap(CrossWorldMap::onAcrossTheDayRun);
    }

    /**
     * 跑秒执行
     */
    public void runSec() {
        execCrossMap(CrossWorldMap::runSec);
    }

    public void execCrossMap(Consumer<CrossWorldMap> consumer) {
        crossWorldMapMap.values().forEach(consumer);
    }

    public NewYorkWar getNewYorkWar() {
        return newYorkWar;
    }

    public void setNewYorkWar(NewYorkWar newYorkWar) {
        this.newYorkWar = newYorkWar;
    }

    public NewYorkWar getOpenNewYorkWar() throws MwException {
        CrossWorldMap crossWorldMap = getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
        // 世界争霸未开启
        if (!crossWorldMap.isInSeason()) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "世界争霸未开启 纽约争霸活动开启失败");
        }
        // 为满足开启条件
        if (!crossWorldMap.checkOpenNewYorkWar()) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "世界争霸未开启满5周 纽约争霸活动开启失败");
        }
        // 纽约城未被占领
        CityMapEntity city = crossWorldMap.getCityMapEntityByCityId(CrossWorldMapConstant.NEW_YORK_CITY_ID);
        if (city == null || city.getCity() == null || city.getCity().getCamp() == Constant.Camp.NPC) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "纽约城未被占领，纽约争霸活动开启失败");
        }
        newYorkWar = new NewYorkWar();
        newYorkWar.setFinalOccupyCamp(city.getCity().getCamp());
        return newYorkWar;
    }

    public NewYorkWar getAndCheckNewYorkWar() throws MwException {
        CrossWorldMap crossWorldMap = getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
        // 世界争霸未开启
        if (!crossWorldMap.isInSeason()) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "世界争霸活动未开启 纽约争霸活动开启失败");
        }
        // 为满足开启条件
        if (!crossWorldMap.checkOpenNewYorkWar()) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "世界争霸未开启满5周 纽约争霸活动开启失败");
        }
        // 纽约城未被占领
        CityMapEntity city = crossWorldMap.getCityMapEntityByCityId(CrossWorldMapConstant.NEW_YORK_CITY_ID);
        if (city == null || city.getCity() == null || city.getCity().getCamp() == Constant.Camp.NPC) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "纽约城未被占领，纽约争霸活动未开启");
        }
        // 阵营未被初始化
        if (newYorkWar == null || newYorkWar.getFinalOccupyCamp() == Constant.Camp.NPC) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "纽约城阵营未被初始化 纽约争霸活动未开启");
        }
        return newYorkWar;
    }

    /**
     * 获取战火燎原对象
     * @return 返回战火燎原对象, 不做开启状态检查
     */
    public GlobalWarFire getGlobalWarFire(){
        CrossWorldMap crossWorldMap = getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
        return crossWorldMap.getGlobalWarFire();
    }
}
