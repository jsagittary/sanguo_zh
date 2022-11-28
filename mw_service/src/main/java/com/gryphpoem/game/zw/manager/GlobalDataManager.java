package com.gryphpoem.game.zw.manager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.DataSaveConstant;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.dao.impl.p.GlobalDao;
import com.gryphpoem.game.zw.resource.domain.p.DbGlobal;
import com.gryphpoem.game.zw.resource.domain.p.MineData;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;
import com.gryphpoem.game.zw.resource.pojo.world.*;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.random.MineLvRandom;
import com.gryphpoem.game.zw.server.SaveGlobalServer;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.RebelService;
import com.gryphpoem.game.zw.service.WorldScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author TanDonghai
 * @ClassName GlobalDataManager.java
 * @Description 公用数据管理类
 * @date 创建时间：2017年3月23日 下午2:52:12
 */
@Component
public class GlobalDataManager {

    @Autowired
    private GlobalDao globalDao;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private RebelService rebelService;

    private GameGlobal gameGlobal;

    /**
     * 记录最后一次保存数据的时间
     */
    private int lastGlobalSaveTime;

    public void init() throws InvalidProtocolBufferException {
        this.gameGlobal = new GameGlobal();
        DbGlobal dbGlobal = globalDao.selectGlobal();
        LogUtil.debug("initGlobalData" + dbGlobal);
        if (dbGlobal == null) {
            // 首次生成分区、城池、流寇数据
            initWorldData();
            // 首次生成柏林会战
            initBerlinWar();
            // 首次的匪军叛乱
            initRebellion();
            dbGlobal = gameGlobal.ser();
            globalDao.insertGlobal(dbGlobal);
            LogUtil.debug("新生产globalId=" + dbGlobal.getGlobalId() + ",内存gameGlobalId=" + gameGlobal.getGlobalId());
            gameGlobal.setGlobalId(dbGlobal.getGlobalId());
        } else {
            gameGlobal.dser(dbGlobal);
        }
        this.gameGlobal.dayJobRun = TimeHelper.getCurrentDay();
    }

    /**
     * 初始化尸潮来袭(原匪军叛乱)
     */
    private void initRebellion() {
        // 首次开放规则
        int openServerDay = serverSetting.getOpenServerDay(new Date());
        // 配置的开启时间
        List<List<Integer>> timeCfg = Constant.REBEL_START_TIME_CFG;
        // 开服第一天
        if (openServerDay <= 1 && !CheckNull.isEmpty(timeCfg)) {
            // 开服时间
            Date openServerDate = serverSetting.getOpenServerDate();
            // 开服第三天预显示
            Date preViewDate = DateHelper.afterDayTimeDate(openServerDate, 7);
            // 预显示的时间
            int preViewTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(preViewDate, 0, 0, 0, 0).getTime() / 1000);
            // 开启天数
            Date openTimeDate = DateHelper.afterDayTimeDate(openServerDate, 8);
            // 几点
            int hourCfg = timeCfg.get(1).get(1);
            // 轮次开启时间
            int roundStartTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(openTimeDate, 0, hourCfg, 0, 0).getTime() / 1000);
            // 持续多久
            int duringTimeCfg = timeCfg.get(1).get(2);
            // 轮次结束时间
            int roundEndTime = roundStartTime + duringTimeCfg;
            // 结束时间
            int endTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(openTimeDate, 1, 23, 59, 58).getTime() / 1000);
            // 初始化下次的开放的时间
            rebelService.initRebellion(preViewTime, roundStartTime, roundEndTime, endTime, false);
        }
    }

    /**
     * 初始化柏林会战
     */
    public void initBerlinWar() {
        BerlinWar berlinWar = this.gameGlobal.getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            berlinWar = BerlinWar.createNewBerlinWar();
            this.gameGlobal.setBerlinWar(berlinWar);
        }
        berlinWar.initBerlinWar();
    }

    /**
     * 初始化某个区域的数据
     *
     * @param staticArea
     */
    private void initWorldDataByArea(StaticArea staticArea) {
        Area area = new Area();
        area.setArea(staticArea.getArea());
        gameGlobal.getAreaMap().put(area.getArea(), area);

        if (staticArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) {
            area.setStatus(WorldConstant.AREA_STATUS_OPEN);// 分区开启，玩家可进入
        } else {
            // 世界任务触发
            int worldTaskId = gameGlobal.getGlobalSchedule() != null
                    ? gameGlobal.getGlobalSchedule().getCurrentScheduleId() : 1;
            if (worldTaskId > TaskType.WORLD_BOSS_TASK_ID_1 && staticArea.getOpenOrder() == 2) {
                area.setStatus(WorldConstant.AREA_STATUS_PASS);
            } else if (worldTaskId >= TaskType.WORLD_BOSS_TASK_ID_2 && staticArea.getOpenOrder() == 3) {
                area.setStatus(WorldConstant.AREA_STATUS_PASS);
            } else {
                area.setStatus(WorldConstant.AREA_STATUS_CLOSE);
            }
        }

        // 初始化城池数据
        initWorldCity(area.getArea());

        // 初始化流寇数据
        initWorldBandit(staticArea);

        // 初始化矿点信息
        initWorldMine(staticArea);

        // 初始化闪电战数据
        // initLightningWar(area.getArea());
    }

    /**
     * 新服首次生成分区、城池、流寇数据
     */
    private void initWorldData() {
        for (StaticArea staticArea : StaticWorldDataMgr.getAreaMap().values()) {
            if (staticArea.isOpen()) {
                initWorldDataByArea(staticArea);
            }
        }
    }

    /**
     * 开服后需要新生产的区域
     */
    public void initNewWorldData() {
        for (StaticArea staticArea : StaticWorldDataMgr.getAreaMap().values()) {
            if (staticArea.isOpen() && !gameGlobal.getAreaMap().containsKey(staticArea.getArea())) {
                LogUtil.debug("开启新的area=" + staticArea.getArea());
                initWorldDataByArea(staticArea);
            }
        }
    }

    /**
     * gm使用 重置区域的状态,临时使用
     */
    public void resetAreaState() {
        LogUtil.debug("----------重置区域状态命令开始----------");
        for (StaticArea staticArea : StaticWorldDataMgr.getAreaMap().values()) {
            Area area = gameGlobal.getAreaMap().get(staticArea.getArea());
            if (area != null) {
                if (staticArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) {
                    area.setStatus(WorldConstant.AREA_STATUS_OPEN);// 分区开启，玩家可进入
                } else {
                    // 世界任务触发
                    int worldTaskId = worldScheduleService.getCurrentSchduleId();
                    if (worldTaskId > TaskType.WORLD_BOSS_TASK_ID_1 && staticArea.getOpenOrder() == 2) {
                        area.setStatus(WorldConstant.AREA_STATUS_PASS);
                    } else if (worldTaskId >= TaskType.WORLD_BOSS_TASK_ID_2 && staticArea.getOpenOrder() == 3) {
                        area.setStatus(WorldConstant.AREA_STATUS_PASS);
                    } else {
                        area.setStatus(WorldConstant.AREA_STATUS_CLOSE);
                    }
                }
                LogUtil.debug("----------重置区域状态------- areaId", area.getArea(), ", status:", area.getStatus());
            }
        }
        LogUtil.debug("----------重置区域状态命令结束-------");
    }

    /**
     * 击杀boss开启区块
     *
     * @param openOrder
     */
    public void openAreaData(int openOrder) {
        Area area;
        for (StaticArea staticArea : StaticWorldDataMgr.getAreaMap().values()) {
            if (staticArea.isOpen()) { // staticArea.isOpen() ,staticArea.getOpenOrder() >= openOrder
                area = gameGlobal.getAreaMap().get(staticArea.getArea());
                if (area == null) {
                    area = new Area();
                    area.setArea(staticArea.getArea());
                    gameGlobal.getAreaMap().put(area.getArea(), area);
                    if (staticArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) {
                        area.setStatus(WorldConstant.AREA_STATUS_OPEN);// 分区开启，玩家可进入
                    } else {
                        area.setStatus(WorldConstant.AREA_STATUS_CLOSE);
                    }

                    // 初始化城池数据
                    initWorldCity(area.getArea());

                    // // 初始化流寇数据
                    // initWorldBandit(staticArea);
                    //
                    // // 初始化矿点信息
                    // initWorldMine(staticArea);
                }
                if (staticArea.getOpenOrder() == openOrder) {// WorldConstant.AREA_ORDER_1
                    area.setStatus(WorldConstant.AREA_STATUS_PASS);// 分区开启，玩家可进入
                }
            }
        }
    }

    /**
     * 初始化闪电战boss(不包括NPC阵型,在FightService中设置)
     *
     * @param area
     */
    private void initLightningWar(int area) {
        LightningWarBoss boss;
        StaticCity staticCity = StaticWorldDataMgr.getMaxTypeCityByOpenArea(area);
        if (CheckNull.isNull(staticCity)) {
            return;
        }
        StaticLightningWar lightningWar = StaticLightningWarDataMgr.getLightningWar();
        if (CheckNull.isNull(lightningWar)) {
            LogUtil.error("闪电战未配置");
        } else {
            if (CheckNull.isNull(staticCity)) {
                LogUtil.error("有分区的城池数据为配置, area:", area);
            } else {
                LogUtil.error("开始生成闪电战Boss, area:", area);
                boss = new LightningWarBoss();
                boss.setId(staticCity.getCityId());
                boss.setPos(staticCity.getCityPos());
                boss.setStatus(WorldConstant.BOSS_STATUS_CALM);
                gameGlobal.getLightningWarBossMap().put(area, boss);
            }
        }
    }

    /**
     * 初始化城池数据
     *
     * @param area
     */
    private void initWorldCity(int area) {
        City city;
        List<StaticCity> cityList = StaticWorldDataMgr.getCityByArea(area);
        if (CheckNull.isEmpty(cityList)) {
            LogUtil.error("有分区的城池数据为配置, area:", area);
        } else {
            LogUtil.error("开始生成城池, count:", cityList.size());
            StaticNpc npc;
            List<CityHero> heroList;
            FightService fightService = DataResource.ac.getBean(FightService.class);
            for (StaticCity staticCity : cityList) {
                city = new City();
                city.setCityId(staticCity.getCityId());
                city.setCityLv(staticCity.getLv());
                city.setCamp(Constant.Camp.NPC);// 城池初始属于系统
                city.setStatus(WorldConstant.CITY_STATUS_CALM);

                if (staticCity.getType() == WorldConstant.CITY_TYPE_HOME) {
                    city.setCityLv(1);// 都城初始开发
                }

                heroList = new ArrayList<>();
                for (List<Integer> npcIdList : staticCity.getForm()) {
                    CityHero cityHero = fightService.createCityHero(npcIdList);
                    if (CheckNull.isNull(cityHero)) continue;
                    heroList.add(cityHero);
                }
                gameGlobal.getCityMap().put(city.getCityId(), city);
            }
        }
    }

    /**
     * 获取某个块空余随机的点
     *
     * @param area
     * @param block 这里的分块不是大地图中的分块id，而是相对本分区从1开始计算的分块，范围1-100
     * @param cnt   预计需要的空点
     * @return
     */
    private List<Integer> randomEmptyByAreaBlock(int area, int block, int cnt) {
        List<Integer> emptyPosLs = new ArrayList<>();
        List<Integer> posList = MapHelper.getPosListByAreaBlock(area, block);
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
     * 初始化流寇数据
     *
     * @param sa
     */
    private void initWorldBandit(StaticArea sa) {
        final int area = sa.getArea();
//        StaticBanditArea sba = StaticBanditDataMgr.getBanditAreaMap().get(sa.getOpenOrder());
        int currWorldSchedule = worldScheduleService.getCurrentSchduleId();
        List<StaticBanditArea> staticBanditAreaList = StaticBanditDataMgr.getStaticBanditAreaByAreaOrder(sa.getOpenOrder());
        StaticBanditArea sba = staticBanditAreaList.stream().filter(o -> o.getSchedule().get(0) <= currWorldSchedule && o.getSchedule().get(1) >= currWorldSchedule).findFirst().orElse(null);
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
            for (Entry<Integer, Integer> pm : sLvAndCnt.entrySet()) {
                int lv = pm.getKey();
                int cnt = pm.getValue();// 配置数据
                int needCnt = cnt;
                LogUtil.world("----------刷流寇  区域:", area, ", 大地图的块:", mapBlock, ", 本区域块:", b, ", 流寇等级:", lv, ", 配置数量:",
                        cnt, ", 需要刷的数量:", needCnt, "----------");
                if (needCnt > 0) {
                    List<Integer> emptyPos = randomEmptyByAreaBlock(area, b, needCnt);
                    emptyPos.forEach(pos -> {
                        List<StaticBandit> banditList = StaticBanditDataMgr.getBanditByLv(lv);
                        if (!CheckNull.isEmpty(banditList)) {
                            // 在同等级中随机一个流寇
                            StaticBandit sBandit = banditList.get(RandomHelper.randomInSize(banditList.size()));
                            gameGlobal.getBanditMap().put(pos, sBandit.getBanditId());
                            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                            LogUtil.world("---------- 新增流寇 位置:", pos, " [x=", xy.getA(), ",y=", xy.getB(), "] lv:",
                                    sBandit.getLv(), "----------");
                        }
                    });
                }
            }
        }

        // ------------------------原来的-------------------------
        // int lv;
        // int pos;
        // int count;
        // List<StaticBandit> banditList;
        // StaticBanditArea staticBanditArea =
        // StaticBanditDataMgr.getBanditAreaMap().get(staticArea.getOpenOrder());
        // if (null == staticBanditArea) {
        // LogUtil.error("有分区的流寇数据没有配置, area:", staticArea.getOpenOrder());
        // } else {
        // for (Entry<Integer, Integer> entry :
        // staticBanditArea.getBandits().entrySet()) {
        // lv = entry.getKey();// 流寇等级
        // count = entry.getValue();// 该等级流寇刷新数量
        // LogUtil.error("开始生成流寇, lv:", lv, ", count:", count);
        // for (int i = 0; i < count; i++) {
        // int loop = 0;
        // boolean noPos = false;//
        // 记录当前分区是否已经没有可分配的坐标，如果连续1000次都随机不到空闲坐标，则认为坐标不足，跳出本分区的逻辑
        // do {
        // pos = MapHelper.randomPosInArea(staticArea.getArea());
        // loop++;
        //
        // if (loop > 1000) {
        // noPos = true;
        // break;
        // }
        // } while (isNpcPos(pos));
        //
        // if (noPos) {
        // LogUtil.error("分区已经没有足够的空闲坐标， area:", staticArea.getArea());
        // break;
        // }
        //
        // banditList = StaticBanditDataMgr.getBanditByLv(lv);
        // if (CheckNull.isEmpty(banditList)) {
        // LogUtil.error("该等级没有配置流寇, lv:" + lv);
        // break;
        // }
        // // 在同等级中随机一个流寇
        // gameGlobal.getBanditMap().put(pos,
        // banditList.get(RandomHelper.randomInSize(banditList.size())).getBanditId());
        // }
        // }
        // }
    }

    /**
     * 初始化矿点信息
     *
     * @param sa
     */
    private void initWorldMine(StaticArea sa) {
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
                    for (Entry<Integer, Integer> pm : sLvAndCnt.entrySet()) {
                        int lv = pm.getKey();
                        int cnt = pm.getValue();// 配置数据
                        int needCnt = cnt;
                        LogUtil.world("----------刷矿点  区域:", area, ", 大地图的块:", mapBlock, ", 本区域块:", b, ", 矿点等级:", lv,
                                ", 配置数量:", cnt, ", 需要刷的数量:", needCnt, "----------");
                        if (needCnt > 0) {
                            List<Integer> emptyPos = randomEmptyByAreaBlock(area, b, needCnt);
                            emptyPos.forEach(pos -> {
                                StaticMine mine = MineLvRandom.randomMineByLv(lv);
                                if (mine != null) {
                                    // addMine(pos, mine);
                                    gameGlobal.getMineMap().put(pos, new MineData(mine.getMineId()));
                                    gameGlobal.getMineResourceMap().put(pos, mine.getReward().get(0).get(2));
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
        // ------------------------原来的-------------------------
        // int pos;
        // List<StaticAreaRange> rangeList =
        // StaticWorldDataMgr.getBlockRangeMap().get(staticArea.getOpenOrder());
        // if (CheckNull.isEmpty(rangeList)) {
        // LogUtil.error("有分区等级的矿点数据未配置, order:", staticArea.getOpenOrder());
        // } else {
        // int mineLv;
        // StaticMine mine;
        // for (StaticAreaRange sab : rangeList) {
        // for (Entry<Integer, Integer> entry : sab.getMineLv().entrySet()) {
        // mineLv = entry.getKey();
        // LogUtil.error("开始生成矿点, range:", sab.getRange(), ", lv:", mineLv, ",
        // count:", entry.getValue());
        // for (int i = 0; i < entry.getValue(); i++) {// 每个等级刷新一定数量的矿
        // int loop = 0;
        // boolean noPos = false;//
        // 记录当前分区是否已经没有可分配的坐标，如果连续1000次都随机不到空闲坐标，则认为坐标不足，跳出本分区的逻辑
        // do {
        // pos = MapHelper.randomPosInBlockList(staticArea.getArea(),
        // sab.getBlock());
        // loop++;
        //
        // if (loop > 1000) {
        // noPos = true;
        // break;
        // }
        // } while (isNpcPos(pos));
        //
        // if (noPos) {
        // LogUtil.error("分区已经没有足够的空闲坐标， area:", staticArea.getArea());
        // break;
        // }
        //
        // mine = MineLvRandom.randomMineByLv(mineLv);
        // if (null != mine) {
        // LogUtil.error(
        // "staticArea=" + staticArea.getArea() + ",sOpenOrder=" +
        // staticArea.getOpenOrder()
        // + ",pos=" + pos + ",pos area=" + MapHelper.getAreaIdByPos(pos));
        // gameGlobal.getMineMap().put(pos, mine.getMineId());
        // gameGlobal.getMineResourceMap().put(pos,
        // mine.getReward().get(0).get(2));
        // }
        // }
        // }
        // }
        // }
    }

    /**
     * 判断坐标是否是系统城池或NPC占领坐标, 注意:只能初始化地图的时候使用, 此方法没有判断玩家占用坐标的情况
     *
     * @param pos
     * @return
     */
    public boolean isNpcPos(int pos) {
        return StaticWorldDataMgr.isCityPos(pos) || gameGlobal.getBanditMap().containsKey(pos)
                || gameGlobal.getMineMap().containsKey(pos) || gameGlobal.getCabinetLeadMap().containsKey(pos);
    }

    public GameGlobal getGameGlobal() {
        return gameGlobal;
    }

    public void updateGlobal(DbGlobal dbGlobal) {
        globalDao.save(dbGlobal);
    }

    /**
     * 公用数据保存定时任务
     */
    public void saveGlobalTimerLogic() {
        int now = TimeHelper.getCurrentSecond();
        if (now - lastGlobalSaveTime >= DataSaveConstant.GLOBAL_DATA_SAVE_INTERVAL_SECOND) {
            try {
                SaveGlobalServer.getIns().saveData(gameGlobal.ser());
            } catch (Exception e) {
                LogUtil.error("Global数据保存定时任务出错", e);
            }

            lastGlobalSaveTime = now;
        }
    }

    public int getTotalGold() {
        return gameGlobal.getTrophy().getGold().intValue();
    }

    /**
     * 获取某个VIP等级以上级别的人数
     *
     * @param vipLv
     * @return
     */
    public int getTotalVIPCnt(int vipLv) {
        final int maxVipLV = StaticVipDataMgr.getMaxVipLv();
        int count = 0;
        for (int i = vipLv; i <= maxVipLV; i++) {
            Integer cnt = gameGlobal.getTrophy().getVipCnt().get(i);
            cnt = cnt != null ? cnt : 0;
            count += cnt;
        }
        return count;
    }
}
