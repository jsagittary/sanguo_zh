package com.gryphpoem.game.zw.gameplay.local.world.dominate.impl;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.PlayerSiLiDominateFightRecord;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.abs.TimeLimitDominateMap;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.quartz.jobs.DominateSideJob;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.pojo.dominate.PlayerStateDominate;
import com.gryphpoem.game.zw.resource.pojo.season.CampRankData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.WorldScheduleService;
import com.gryphpoem.game.zw.service.dominate.DominateWorldMapService;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.*;

/**
 * Description: 州郡雄踞一方
 * Author: zhangpeng
 * createTime: 2022-11-22 15:45
 */
public class StateDominateWorldMap extends TimeLimitDominateMap {
    /**
     * 下次预显示时间
     */
    private Date nextPreviewDate;
    /**
     * 下次活动开始时间
     */
    private Date nextBeginDate;
    /**
     * 下次活动结束时间
     */
    private Date nextEndTime;
    /**
     * 开放的城池 <活动次数, 开放的城池list>
     */
    private Map<Integer, List<Integer>> nextOpenCityList;
    /**
     * 玩家数据
     */
    private Map<Long, PlayerStateDominate> playerStateDominateMap;

    private static class InstanceHolder {
        private static final StateDominateWorldMap INSTANCE = new StateDominateWorldMap(
                WorldPb.WorldFunctionDefine.STATES_AND_COUNTIES_DOMINATE_VALUE);
    }

    public static final StateDominateWorldMap getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public StateDominateWorldMap(int worldFunction) {
        super(worldFunction);
        this.nextOpenCityList = new HashMap<>();
        this.playerStateDominateMap = new HashMap<>();
    }

    public void deserialize(SerializePb.SerStateDominateWorldMap ser) {
        deserialize(ser.getTimeLimitMap());
        this.nextBeginDate = new Date(ser.getNextBeginDate());
        this.nextPreviewDate = new Date(ser.getNextPreviewDate());
        this.nextEndTime = new Date(ser.getNextEndTime());
        if (CheckNull.isNull(this.nextOpenCityList)) {
            this.nextOpenCityList = new HashMap<>();
        }

        if (CheckNull.nonEmpty(ser.getNextOpenCityList())) {
            ser.getNextOpenCityList().forEach(cityPb -> {
                List<Integer> list = this.nextOpenCityList.computeIfAbsent(cityPb.getTimes(), l -> new ArrayList<>());
                list.addAll(cityPb.getCityIdList());
            });
        }
    }

    public SerializePb.SerStateDominateWorldMap createWorldMapPb(boolean isSaveDb) {
        SerializePb.SerTimeLimitDominateMap timeLimitDominateMap = super.createMapPb(isSaveDb);
        SerializePb.SerStateDominateWorldMap.Builder builder = SerializePb.SerStateDominateWorldMap.newBuilder();
        builder.setTimeLimitMap(timeLimitDominateMap);
        builder.setNextBeginDate(this.nextBeginDate.getTime());
        builder.setNextEndTime(this.nextEndTime.getTime());
        builder.setNextPreviewDate(this.nextPreviewDate.getTime());
        if (CheckNull.nonEmpty(this.nextOpenCityList)) {
            SerializePb.SerOpenDominateSideCity.Builder cityPb = SerializePb.SerOpenDominateSideCity.newBuilder();
            this.nextOpenCityList.entrySet().forEach(en -> {
                cityPb.setTimes(en.getKey());
                cityPb.addAllCityId(en.getValue());
                builder.addNextOpenCity(cityPb.build());
                cityPb.clear();
            });
        }
        return builder.build();
    }

    /**
     * 初始化定时器
     */
    @Override
    public void initSchedule() {
        List<String> previewTime = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME;
        if (CheckNull.nonEmpty(previewTime)) {
            String jobName;
            for (int i = 0; i < previewTime.size(); i++) {
                String previewCron = previewTime.get(0);
                if (StringUtils.isBlank(previewCron)) continue;
                jobName = getWorldMapFunctionName() + "_" + DominateSideJob.PREVIEW + "_" + (i + 1);
                QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), jobName, "DominateSide", DominateSideJob.class, previewCron);
            }
        }

        List<String> beginTime = Constant.STATE_DOMINATE_WORLD_MAP_BEGIN_TIME;
        if (CheckNull.nonEmpty(beginTime)) {
            String jobName;
            for (int i = 0; i < beginTime.size(); i++) {
                String beginCron = beginTime.get(0);
                if (StringUtils.isBlank(beginCron)) continue;
                jobName = getWorldMapFunctionName() + "_" + DominateSideJob.BEGIN + "_" + (i + 1);
                QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), jobName, "DominateSide", DominateSideJob.class, beginCron);
            }
        }

        List<String> endTime = Constant.STATE_DOMINATE_WORLD_MAP_END;
        if (CheckNull.nonEmpty(endTime)) {
            String jobName;
            for (int i = 0; i < endTime.size(); i++) {
                String endCron = endTime.get(0);
                if (StringUtils.isBlank(endCron)) continue;
                jobName = getWorldMapFunctionName() + "_" + DominateSideJob.END + "_" + (i + 1);
                QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), jobName, "DominateSide", DominateSideJob.class, endCron);
            }
        }
    }

    /**
     * 服务器启动, 活动时间初始化
     *
     * @throws Exception
     */
    public void handleOnStartup() throws ParseException {
        Date now = new Date();
        if (getCurPreviewDate() == null) {
            try {
                String firstPreviewTime = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME.get(0);
                String secondPreviewTime = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME.get(1);
                if (!StringUtils.isBlank(firstPreviewTime) && !StringUtils.isBlank(secondPreviewTime)) {
                    CronExpression cronExpression = new CronExpression(firstPreviewTime);
                    Date nextDate = cronExpression.getNextValidTimeAfter(now);
                    setCurPreviewDate(nextDate);

                    cronExpression = new CronExpression(secondPreviewTime);
                    nextDate = cronExpression.getNextValidTimeAfter(now);
                    if (DateHelper.isToday(nextDate)) {
                        if (!DateHelper.isToday(getCurPreviewDate())) {
                            Date preCurPreviewDate = getCurPreviewDate();
                            setCurPreviewDate(new Date(nextDate.getTime()));
                            nextDate = preCurPreviewDate;
                        }
                    }
                    setNextPreviewDate(nextDate);
                }
            } catch (ParseException e) {
                LogUtil.error("", e);
            }
        } else {
            if (now.after(getCurPreviewDate()) && now.before(getCurEndTime()) && CheckNull.isEmpty(this.curOpenCityList)) {
                // 若比当前时间晚且未结束且未当前开放城池未空 (停服期 间 错过了预显示定时器)
                String firstPreviewTimeStr = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME.get(0);
                String secondPreviewTimeStr = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME.get(1);
                if (!StringUtils.isBlank(firstPreviewTimeStr) && !StringUtils.isBlank(secondPreviewTimeStr)) {
                    CronExpression cronExpression = new CronExpression(firstPreviewTimeStr);
                    Date firstPreviewTime = cronExpression.getTimeAfter(now);
                    cronExpression = new CronExpression(secondPreviewTimeStr);
                    Date secondPreviewTime = cronExpression.getTimeAfter(now);

                    List<Integer> stateList = new ArrayList<>();
                    Map<Integer, List<Integer>> areaMap = new HashMap<>();
                    int curScheduleId = DataResource.ac.getBean(WorldScheduleService.class).getCurrentSchduleId();
                    WorldDataManager worldDataManager = DataResource.ac.getBean(WorldDataManager.class);
                    if (firstPreviewTime.getTime() > secondPreviewTime.getTime()) {
                        // 若下次第一波活动预显示时间比下次第二波活动预显示时间早, 则当前是在今天第二波活动上
                        this.curTimes = 2;
                        initCurMultiDominateCity(worldDataManager, stateList, areaMap, curScheduleId);
                        if (CheckNull.isEmpty(this.nextOpenCityList)) {
                            initMultiNextOpenCityList(stateList, areaMap, curScheduleId, 1);
                            initMultiNextOpenCityList(stateList, areaMap, curScheduleId, 2);
                        }
                    } else {
                        this.curTimes = 1;
                        initCurMultiDominateCity(worldDataManager, stateList, areaMap, curScheduleId);
                    }
                }
            }
        }

        if (getCurBeginDate() == null) {
            try {
                String firstBeginTime = Constant.STATE_DOMINATE_WORLD_MAP_BEGIN_TIME.get(0);
                String secondBeginTime = Constant.STATE_DOMINATE_WORLD_MAP_BEGIN_TIME.get(1);
                if (!StringUtils.isBlank(firstBeginTime) && !StringUtils.isBlank(secondBeginTime)) {
                    CronExpression cronExpression = new CronExpression(firstBeginTime);
                    Date nextDate = cronExpression.getNextValidTimeAfter(now);
                    setCurBeginDate(nextDate);

                    cronExpression = new CronExpression(secondBeginTime);
                    nextDate = cronExpression.getNextValidTimeAfter(now);
                    if (DateHelper.isToday(nextDate)) {
                        if (!DateHelper.isToday(getCurBeginDate())) {
                            Date preCurBeginDate = getCurBeginDate();
                            setCurBeginDate(new Date(nextDate.getTime()));
                            nextDate = preCurBeginDate;
                        }
                    }
                    setNextBeginDate(nextDate);
                }
            } catch (ParseException e) {
                LogUtil.error("", e);
            }
        } else {
            if (now.after(getCurBeginDate()) && now.before(getCurEndTime())) {
                String jobName = getWorldMapFunctionName() + "_" + "begin_check_" + curTimes;
                ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(jobName), (job) -> {
                    checkWinOfOccupyTime(); // 检测柏林占领时间
                }, this.getCurBeginDate(), this.getCurEndTime(), 1);
            }
        }

        if (getCurEndTime() == null) {
            try {
                String firstEndTime = Constant.STATE_DOMINATE_WORLD_MAP_END.get(0);
                String secondEndTime = Constant.STATE_DOMINATE_WORLD_MAP_END.get(1);
                if (!StringUtils.isBlank(firstEndTime) && !StringUtils.isBlank(secondEndTime)) {
                    CronExpression cronExpression = new CronExpression(firstEndTime);
                    Date nextDate = cronExpression.getNextValidTimeAfter(now);
                    setCurEndTime(nextDate);

                    cronExpression = new CronExpression(secondEndTime);
                    nextDate = cronExpression.getNextValidTimeAfter(now);
                    if (DateHelper.isToday(nextDate)) {
                        if (!DateHelper.isToday(getCurEndTime())) {
                            Date preCurEndDate = getCurEndTime();
                            setCurEndTime(new Date(nextDate.getTime()));
                            nextDate = preCurEndDate;
                        }
                    }
                    setNextEndTime(nextDate);
                }
            } catch (ParseException e) {
                LogUtil.error("", e);
            }
        } else {
            if (now.after(getCurEndTime())) {
                // 在停服期间, 活动已结束
                onEnd(this.curTimes);
            }
        }

        // 若世界进程足够, 则活动开启
        int curScheduleId = DataResource.ac.getBean(WorldScheduleService.class).getCurrentSchduleId();
        if (curScheduleId >= Constant.OPEN_STATE_DOMINATE_WORLD_MAP_FUNCTION_CONDITION.get(0)) {
            this.setOpen(true);
        }
    }

    /**
     * 预显示时间
     *
     * @param curTimes
     */
    public void onPreview(int curTimes) {
        this.curTimes = curTimes;
        List<Integer> stateList = new ArrayList<>();
        Map<Integer, List<Integer>> areaMap = new HashMap<>();
        WorldDataManager worldDataManager = DataResource.ac.getBean(WorldDataManager.class);
        int curScheduleId = DataResource.ac.getBean(WorldScheduleService.class).getCurrentSchduleId();
        switch (curTimes) {
            case 1:
                if (curScheduleId >= Constant.OPEN_STATE_DOMINATE_WORLD_MAP_FUNCTION_CONDITION.get(0)) {
                    this.setOpen(true);
                }

                this.curOpenCityList.clear();
                if (CheckNull.nonEmpty(this.nextOpenCityList)) {
                    this.nextOpenCityList.entrySet().forEach(nextOpenCity -> {
                        nextOpenCity.getValue().forEach(cityId -> {
                            createDominateCity(worldDataManager, cityId, nextOpenCity.getKey());
                        });
                    });

                    this.nextOpenCityList.clear();
                } else {
                    initCurMultiDominateCity(worldDataManager, stateList, areaMap, curScheduleId);
                }
                break;
            case 2:
                if (CheckNull.nonEmpty(this.nextOpenCityList))
                    this.nextOpenCityList.clear();
                if (CheckNull.isEmpty(this.curOpenCityList)) {
                    initCurMultiDominateCity(worldDataManager, stateList, areaMap, curScheduleId);
                }

                initMultiNextOpenCityList(stateList, areaMap, curScheduleId, 1);
                initMultiNextOpenCityList(stateList, areaMap, curScheduleId, 2);
                break;
        }

        EventBus.getDefault().post(new Events.SyncDominateWorldMapChangeEvent(getWorldMapFunction(), createPb(false)));
    }

    /**
     * 活动开始
     *
     * @param curTimes
     */
    public void onBegin(int curTimes) {
        LogUtil.common("worldFunction: ", getWorldMapFunction(), ", curTimes: ", curTimes, " begin");
        String jobName = getWorldMapFunctionName() + "_" + "begin_check_" + curTimes;
        ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(jobName), (job) -> {
            checkWinOfOccupyTime(); // 检测柏林占领时间
        }, this.getCurBeginDate(), this.getCurEndTime(), 1);

        EventBus.getDefault().post(new Events.SyncDominateWorldMapChangeEvent(getWorldMapFunction(), createPb(false)));
    }

    /**
     * 活动结束
     *
     * @param curTimes
     */
    public void onEnd(int curTimes) throws ParseException {
        this.curTimes = curTimes == 1 ? 2 : 1;
        // 修改当前活动时间
        switch (this.curTimes) {
            case 1:
                initNextTime(1);
                break;
            case 2:
                initNextTime(0);
                break;
        }

        this.playerStateDominateMap.clear();
        // 城池结算
        int now = TimeHelper.getCurrentSecond();
        if (CheckNull.nonEmpty(this.curOpenCityList)) {
            Optional.ofNullable(this.curOpenCityList.get(curTimes)).ifPresent(list -> {
                list.forEach(sideCity -> {
                    if (sideCity.isOver())
                        return;
                    sideCity.settleCampOccupyTime(now);
                    List<CampRankData> sortedList = null;
                    if (CheckNull.nonEmpty(sideCity.getCampRankDataMap())) {
                        sortedList = sortCampRank(sideCity.getCampRankDataMap().values());
                    }
                    if (CheckNull.nonEmpty(sortedList)) {
                        CampRankData vicCampData = sortedList.get(0);
                        sideCity.setCamp(vicCampData.camp);
                        sideCity.setOver(true);
                    }
                });
            });
        }

        EventBus.getDefault().post(new Events.SyncDominateWorldMapChangeEvent(getWorldMapFunction(), createPb(false)));
        this.curOpenCityList.clear();
    }

    /**
     * 初始化下一次活动时间
     *
     * @param index
     * @throws ParseException
     */
    private void initNextTime(int index) throws ParseException {
        int curIndex = index == 0 ? 1 : 0;

        Date nextDate;
        String timeStr;
        Date now = new Date();
        CronExpression cronExpression;
        timeStr = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME.get(curIndex);
        cronExpression = new CronExpression(timeStr);
        nextDate = cronExpression.getNextValidTimeAfter(now);
        setCurPreviewDate(nextDate);

        timeStr = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME.get(index);
        cronExpression = new CronExpression(timeStr);
        nextDate = cronExpression.getNextValidTimeAfter(this.getCurPreviewDate());
        setNextEndTime(nextDate);

        timeStr = Constant.STATE_DOMINATE_WORLD_MAP_BEGIN_TIME.get(curIndex);
        cronExpression = new CronExpression(timeStr);
        nextDate = cronExpression.getNextValidTimeAfter(now);
        setCurBeginDate(nextDate);

        timeStr = Constant.STATE_DOMINATE_WORLD_MAP_BEGIN_TIME.get(index);
        cronExpression = new CronExpression(timeStr);
        nextDate = cronExpression.getNextValidTimeAfter(this.getCurBeginDate());
        setNextBeginDate(nextDate);

        timeStr = Constant.STATE_DOMINATE_WORLD_MAP_END.get(curIndex);
        cronExpression = new CronExpression(timeStr);
        nextDate = cronExpression.getNextValidTimeAfter(now);
        setCurEndTime(nextDate);

        timeStr = Constant.STATE_DOMINATE_WORLD_MAP_END.get(index);
        cronExpression = new CronExpression(timeStr);
        nextDate = cronExpression.getNextValidTimeAfter(this.getCurEndTime());
        setNextEndTime(nextDate);
    }

    /**
     * 初始化下一次开放的城池列表
     *
     * @param stateList
     * @param areaMap
     * @param curScheduleId
     * @param times
     */
    private void initMultiNextOpenCityList(List<Integer> stateList, Map<Integer, List<Integer>> areaMap, int curScheduleId, int times) {
        paddingStaticData(stateList, areaMap);
        List<Integer> cityList = randomStateCity(stateList, areaMap, curScheduleId);
        if (CheckNull.nonEmpty(cityList)) {
            this.nextOpenCityList.put(times, cityList);
        }
    }

    /**
     * 填充静态数据
     *
     * @param stateList
     * @param areaMap
     */
    private void paddingStaticData(List<Integer> stateList, Map<Integer, List<Integer>> areaMap) {
        if (CheckNull.nonEmpty(stateList))
            return;

        Collection<StaticArea> areaList = StaticWorldDataMgr.getAreaMap().values();
        if (CheckNull.nonEmpty(areaList)) {
            areaList.forEach(staticArea -> {
                if (CheckNull.isEmpty(staticArea.getGotoArea())) return;
                stateList.add(staticArea.getGotoArea().get(0));
                areaMap.computeIfAbsent(staticArea.getGotoArea().get(0), l -> new ArrayList<>()).add(staticArea.getArea());
            });
        }
    }

    /**
     * 初始化当前雄踞一方 城
     *
     * @param worldDataManager
     * @param curScheduleId
     */
    private void initCurMultiDominateCity(WorldDataManager worldDataManager, List<Integer> stateList, Map<Integer, List<Integer>> areaMap, int curScheduleId) {
        paddingStaticData(stateList, areaMap);
        if (CheckNull.nonEmpty(areaMap)) {
            createMultiDominateCity(worldDataManager, stateList, areaMap, curScheduleId, 1);
            createMultiDominateCity(worldDataManager, stateList, areaMap, curScheduleId, 2);
        }
    }

    /**
     * 创建两次活动生成的州郡
     *
     * @param worldDataManager
     * @param stateList
     * @param areaMap
     * @param curScheduleId
     * @param times
     */
    private void createMultiDominateCity(WorldDataManager worldDataManager, List<Integer> stateList,
                                         Map<Integer, List<Integer>> areaMap, int curScheduleId, int times) {
        List<Integer> cityList = randomStateCity(stateList, areaMap, curScheduleId);
        if (CheckNull.nonEmpty(cityList)) {
            cityList.forEach(cityId -> {
                createDominateCity(worldDataManager, cityId, times);
            });
        }
    }

    /**
     * 随机州郡城池id
     *
     * @param stateList
     * @param areaMap
     * @param curScheduleId
     * @return
     */
    private List<Integer> randomStateCity(List<Integer> stateList, Map<Integer, List<Integer>> areaMap, int curScheduleId) {
        if (CheckNull.isEmpty(stateList) || CheckNull.isEmpty(areaMap))
            return null;

        List<Integer> cityList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            int areaId = stateList.get(RandomHelper.randomInSize(stateList.size()));
            if (curScheduleId >= Constant.OPEN_STATE_DOMINATE_WORLD_MAP_FUNCTION_CONDITION.get(1)) {
                List<StaticCity> staticCityList = StaticWorldDataMgr.getCityByArea(areaId);
                if (CheckNull.nonEmpty(staticCityList)) {
                    // 州城
                    StaticCity staticCity_ = staticCityList.stream().filter(staticCity -> staticCity.getType() == 6).findFirst().orElse(null);
                    if (Objects.nonNull(staticCity_)) {
                        cityList.add(staticCity_.getCityId());
                    }
                }
            }
            List<Integer> areaIdList = areaMap.get(areaId);
            if (CheckNull.nonEmpty(areaIdList)) {
                areaIdList.forEach(areaId_ -> {
                    List<StaticCity> staticCityList = StaticWorldDataMgr.getCityByArea(areaId_);
                    if (CheckNull.nonEmpty(staticCityList)) {
                        // 郡城
                        StaticCity staticCity_ = staticCityList.stream().filter(staticCity -> staticCity.getType() == 3).findFirst().orElse(null);
                        if (Objects.nonNull(staticCity_)) {
                            cityList.add(staticCity_.getCityId());
                        }
                    }
                });
            }

            stateList.remove(Integer.valueOf(areaId));
        }

        return cityList;
    }

    public int getCurTimes() {
        return curTimes;
    }

    public void setCurTimes(int curTimes) {
        this.curTimes = curTimes;
    }

    public Date getNextPreviewDate() {
        return nextPreviewDate;
    }

    public void setNextPreviewDate(Date nextPreviewDate) {
        this.nextPreviewDate = nextPreviewDate;
    }

    public Date getNextBeginDate() {
        return nextBeginDate;
    }

    public void setNextBeginDate(Date nextBeginDate) {
        this.nextBeginDate = nextBeginDate;
    }

    public Date getNextEndTime() {
        return nextEndTime;
    }

    public void setNextEndTime(Date nextEndTime) {
        this.nextEndTime = nextEndTime;
    }

    public Map<Integer, List<Integer>> getNextOpenCityList() {
        return nextOpenCityList;
    }

    public void setNextOpenCityList(Map<Integer, List<Integer>> nextOpenCityList) {
        this.nextOpenCityList = nextOpenCityList;
    }

    @Override
    public WorldPb.BaseWorldFunctionPb createPb(boolean isSaveDb) {
        WorldPb.StateDominateWorldFunctionPb.Builder builder = WorldPb.StateDominateWorldFunctionPb.newBuilder();
        int state = state();
        WorldPb.BaseWorldFunctionPb.Builder basePb = WorldPb.BaseWorldFunctionPb.newBuilder();
        basePb.setState(state);
        basePb.setOpen(this.isOpen());
        basePb.setFunction(WorldPb.WorldFunctionDefine.STATES_AND_COUNTIES_DOMINATE_VALUE);

        if (this.isOpen() && state != WorldPb.WorldFunctionStateDefine.END_VALUE &&
                state != WorldPb.WorldFunctionStateDefine.NOT_START_VALUE) {
            builder.setNextPreviewTime((int) (this.nextPreviewDate.getTime() / 1000l));
            builder.setNextBeginTime((int) (this.nextBeginDate.getTime() / 1000l));
            builder.setNextEndTime((int) (this.nextEndTime.getTime() / 1000l));
            if (CheckNull.nonEmpty(curOpenCityList)) {
                WorldPb.CurDominateCityInfo.Builder cityPb = WorldPb.CurDominateCityInfo.newBuilder();
                this.curOpenCityList.get(this.curTimes).forEach(dsc -> {
                    cityPb.setCityId(dsc.getCityId());
                    cityPb.setCamp(dsc.getCamp());
                    cityPb.setIsOver(dsc.isOver());
                    if (CheckNull.nonEmpty(dsc.getCampRankDataMap())) {
                        dsc.getCampRankDataMap().values().forEach(crd -> {
                            cityPb.addCampRankInfo(crd.ser());
                        });
                    }
                    basePb.addCurOpenCityInfo(cityPb.build());
                    cityPb.clear();
                });
            }
            switch (this.curTimes) {
                case 1:
                    this.curOpenCityList.get(2).forEach(dsc -> {
                        builder.addNextCityId(dsc.getCityId());
                    });
                    break;
                case 2:
                    builder.addAllNextCityId(this.nextOpenCityList.get(1));
                    break;
            }
        }

        basePb.setExtension(WorldPb.StateDominateWorldFunctionPb.function, builder.build());
        return basePb.build();
    }

    public int incContinuousKillCnt(long roleId, int cityId) {
        return this.playerStateDominateMap.computeIfAbsent(roleId, m -> new PlayerStateDominate(roleId)).incContinuousKillCnt(cityId);
    }

    public void clearContinuousKillCnt(long roleId, int cityId) {
        PlayerStateDominate playerStateDominate = this.playerStateDominateMap.get(roleId);
        if (Objects.nonNull(playerStateDominate)) {
            playerStateDominate.getContinuousKillCntMap().put(cityId, 0);
        }
    }

    public int getContinuousKillCnt(long roleId, int cityId) {
        PlayerStateDominate playerStateDominate = this.playerStateDominateMap.get(roleId);
        if (CheckNull.isNull(playerStateDominate))
            return 0;
        return playerStateDominate.getContinuousKillCntMap().getOrDefault(cityId, 0);
    }

    @Override
    public void close() {

    }
}
