package com.gryphpoem.game.zw.gameplay.local.world.dominate.impl;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideCity;
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
import com.gryphpoem.game.zw.resource.pojo.dominate.PlayerStateDominate;
import com.gryphpoem.game.zw.resource.pojo.season.CampRankData;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.WorldScheduleService;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

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
    private Map<Integer, List<DominateSideCity>> nextOpenCityList;
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
        this.nextOpenCityList = new HashMap<>(1);
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
            WorldDataManager worldDataManager = DataResource.ac.getBean(WorldDataManager.class);
            for (SerializePb.SerOpenDominateSideCity pb : ser.getNextOpenCityList()) {
                List<DominateSideCity> cityList = this.nextOpenCityList.computeIfAbsent(pb.getTimes(), l -> new ArrayList<>());
                if (CheckNull.nonEmpty(pb.getCityIdList())) {
                    pb.getCityIdList().forEach(cityId -> {
                        cityList.add((DominateSideCity) worldDataManager.getCityMap().get(cityId));
                    });
                }
            }
        }
    }

    public SerializePb.SerStateDominateWorldMap createWorldMapPb(boolean isSaveDb) {
        SerializePb.SerTimeLimitDominateMap timeLimitDominateMap = super.createMapPb(isSaveDb);
        if (timeLimitDominateMap == null)
            return null;

        SerializePb.SerStateDominateWorldMap.Builder builder = SerializePb.SerStateDominateWorldMap.newBuilder();
        builder.setTimeLimitMap(timeLimitDominateMap);
        if (Objects.nonNull(this.nextBeginDate))
            builder.setNextBeginDate(this.nextBeginDate.getTime());
        if (Objects.nonNull(this.nextEndTime))
            builder.setNextEndTime(this.nextEndTime.getTime());
        if (Objects.nonNull(this.nextPreviewDate))
            builder.setNextPreviewDate(this.nextPreviewDate.getTime());
        if (CheckNull.nonEmpty(this.nextOpenCityList)) {
            builder.addAllNextOpenCity(this.curOpenCityList.entrySet().stream().
                    map(PbHelper::openDominateSideCityPb).collect(Collectors.toList()));
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
                String previewCron = previewTime.get(i);
                if (StringUtils.isBlank(previewCron)) continue;
                jobName = getWorldMapFunctionName() + "_" + DominateSideJob.PREVIEW + "_" + (i + 1);
                QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), jobName, "DominateSide", DominateSideJob.class, previewCron);
            }
        }

        List<String> beginTime = Constant.STATE_DOMINATE_WORLD_MAP_BEGIN_TIME;
        if (CheckNull.nonEmpty(beginTime)) {
            String jobName;
            for (int i = 0; i < beginTime.size(); i++) {
                String beginCron = beginTime.get(i);
                if (StringUtils.isBlank(beginCron)) continue;
                jobName = getWorldMapFunctionName() + "_" + DominateSideJob.BEGIN + "_" + (i + 1);
                QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), jobName, "DominateSide", DominateSideJob.class, beginCron);
            }
        }

        List<String> endTime = Constant.STATE_DOMINATE_WORLD_MAP_END;
        if (CheckNull.nonEmpty(endTime)) {
            String jobName;
            for (int i = 0; i < endTime.size(); i++) {
                String endCron = endTime.get(i);
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

        boolean init = CheckNull.isNull(getCurPreviewDate());
        Date initFirstPreviewTime = null;
        Date initFirstBeginTime = null;
        Date initFirstEndTime = null;
        Date initNextPreviewTime = null;
        Date initNextBeginTime = null;
        Date initNextEndTime = null;

        if (getCurPreviewDate() == null) {
            try {
                this.functionOpenDay = now;
                String firstPreviewTimeStr = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME.get(0);
                String secondPreviewTimeStr = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME.get(1);
                if (!StringUtils.isBlank(firstPreviewTimeStr) && !StringUtils.isBlank(secondPreviewTimeStr)) {
                    CronExpression cronExpression = new CronExpression(firstPreviewTimeStr);
                    initFirstPreviewTime = cronExpression.getNextValidTimeAfter(now);
                    cronExpression = new CronExpression(secondPreviewTimeStr);
                    initNextPreviewTime = cronExpression.getNextValidTimeAfter(now);
                }
            } catch (ParseException e) {
                LogUtil.error("", e);
            }
        }

        if (getCurBeginDate() == null) {
            try {
                String firstBeginTime = Constant.STATE_DOMINATE_WORLD_MAP_BEGIN_TIME.get(0);
                String secondBeginTime = Constant.STATE_DOMINATE_WORLD_MAP_BEGIN_TIME.get(1);
                if (!StringUtils.isBlank(firstBeginTime) && !StringUtils.isBlank(secondBeginTime)) {
                    CronExpression cronExpression = new CronExpression(firstBeginTime);
                    initFirstBeginTime = cronExpression.getNextValidTimeAfter(now);
                    cronExpression = new CronExpression(secondBeginTime);
                    initNextBeginTime = cronExpression.getNextValidTimeAfter(now);
                }
            } catch (ParseException e) {
                LogUtil.error("", e);
            }
        }

        if (getCurEndTime() == null) {
            try {
                String firstEndTime = Constant.STATE_DOMINATE_WORLD_MAP_END.get(0);
                String secondEndTime = Constant.STATE_DOMINATE_WORLD_MAP_END.get(1);
                if (!StringUtils.isBlank(firstEndTime) && !StringUtils.isBlank(secondEndTime)) {
                    CronExpression cronExpression = new CronExpression(firstEndTime);
                    initFirstEndTime = cronExpression.getNextValidTimeAfter(now);
                    cronExpression = new CronExpression(secondEndTime);
                    initNextEndTime = cronExpression.getNextValidTimeAfter(now);
                }
            } catch (ParseException e) {
                LogUtil.error("", e);
            }
        }

        // 若世界进程足够, 则活动开启
        Calendar c = Calendar.getInstance();
        int curScheduleId = DataResource.ac.getBean(WorldScheduleService.class).getCurrentSchduleId();
        if (!DateHelper.isSameDate(now, this.functionOpenDay) && !isPreviewTimeExpired(c, now) &&
                curScheduleId >= Constant.OPEN_STATE_DOMINATE_WORLD_MAP_FUNCTION_CONDITION.get(0)) {
            this.setOpen(true);
        }

        if (this.isOpen() && !init && now.after(getCurPreviewDate()) && now.before(getCurEndTime()) && CheckNull.isEmpty(this.curOpenCityList)) {
            // 若比当前时间晚且未结束且当前开放城池为空 (停服期 间 错过了预显示定时器)
            String firstPreviewTimeStr = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME.get(0);
            String secondPreviewTimeStr = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME.get(1);
            if (!StringUtils.isBlank(firstPreviewTimeStr) && !StringUtils.isBlank(secondPreviewTimeStr)) {
                CronExpression cronExpression = new CronExpression(firstPreviewTimeStr);
                Date firstPreviewTime = cronExpression.getTimeAfter(now);
                cronExpression = new CronExpression(secondPreviewTimeStr);
                Date secondPreviewTime = cronExpression.getTimeAfter(now);
                if (firstPreviewTime.getTime() > secondPreviewTime.getTime()) {
                    // 若下次第一波活动预显示时间比下次第二波活动预显示时间早, 则当前是在今天第二波活动上
                    onPreview(2);
                } else {
                    onPreview(1);
                }
            }
        }

        // 活动已开始, 停服错过开始定时器
        if (this.isOpen() && !init && now.after(getCurBeginDate()) && now.before(getCurEndTime())) {
            onBegin(this.curTimes);
        }

        // 活动已结束, 停服错过结束定时器
        if (this.isOpen() && !init && now.after(getCurEndTime())) {
            // 在停服期间, 活动已结束
            onEnd(this.curTimes);
        }

        if (init) {
            if (DateHelper.isSameDate(now, initFirstBeginTime) || DateHelper.isSameDate(now, initFirstEndTime) ||
                    DateHelper.isSameDate(now, initFirstPreviewTime) || DateHelper.isSameDate(now, initNextPreviewTime) ||
                    DateHelper.isSameDate(now, initNextBeginTime) || DateHelper.isSameDate(now, initNextEndTime)) {
                setCurPreviewDate(checkSameDate(now, initFirstPreviewTime, c));
                setCurBeginDate(checkSameDate(now, initFirstBeginTime, c));
                setCurEndTime(checkSameDate(now, initFirstEndTime, c));
                setNextPreviewDate(checkSameDate(now, initNextPreviewTime, c));
                setNextBeginDate(checkSameDate(now, initNextBeginTime, c));
                setNextEndTime(checkSameDate(now, initNextEndTime, c));
            }
        }
    }

    /**
     * 预显示时间
     *
     * @param curTimes
     */
    public void onPreview(int curTimes) throws ParseException {
        this.curTimes = curTimes;
        List<Integer> stateList = new ArrayList<>();
        Map<Integer, List<Integer>> areaMap = new HashMap<>();
        WorldDataManager worldDataManager = DataResource.ac.getBean(WorldDataManager.class);
        int curScheduleId = DataResource.ac.getBean(WorldScheduleService.class).getCurrentSchduleId();
        switch (curTimes) {
            case 1:
                if (!DateHelper.isSameDate(new Date(), this.functionOpenDay) &&
                        curScheduleId >= Constant.OPEN_STATE_DOMINATE_WORLD_MAP_FUNCTION_CONDITION.get(0)) {
                    this.setOpen(true);
                }

                if (this.isOpen()) {
                    this.curOpenCityList.clear();
                    if (CheckNull.nonEmpty(this.nextOpenCityList)) {
                        this.curOpenCityList = new HashMap<>(this.nextOpenCityList);
                        this.nextOpenCityList.clear();
                        removeCurOpenCityList(stateList, areaMap);
                    } else {
                        initCurMultiDominateCity(worldDataManager, stateList, areaMap, curScheduleId);
                    }

                    // 初始化下一次开放的城池 (与当前第一次的城池不能重复)
                    initMultiNextOpenCityList(worldDataManager, stateList, areaMap, curScheduleId, 0);
                }
                break;
            case 2:
                if (this.isOpen()) {
                    this.curOpenCityList.clear();
                    if (CheckNull.nonEmpty(this.nextOpenCityList)) {
                        this.curOpenCityList = new HashMap<>(this.nextOpenCityList);
                    } else {
                        initCurMultiDominateCity(worldDataManager, stateList, areaMap, curScheduleId);
                    }

                    this.nextOpenCityList.clear();
                    initMultiNextOpenCityList(worldDataManager, stateList, areaMap, curScheduleId, 0);
                }
                break;
        }

        // 修改当前活动时间
        switch (this.curTimes) {
            case 1:
                initNextTime(1, true, true);
                break;
            case 2:
                initNextTime(0, true, false);
                break;
        }
        
        if (this.isOpen()) {
            EventBus.getDefault().post(new Events.SyncDominateWorldMapChangeEvent(getWorldMapFunction(), createPb(false)));
        }
    }

    /**
     * 活动开始
     *
     * @param curTimes
     */
    public void onBegin(int curTimes) throws ParseException {
        // 修改当前活动时间
        switch (this.curTimes) {
            case 1:
                initNextTime(1, true, true);
                break;
            case 2:
                initNextTime(0, true, false);
                break;
        }

        if (this.isOpen()) {
            LogUtil.common("worldFunction: ", getWorldMapFunction(), ", curTimes: ", curTimes, " begin");
            if (CheckNull.nonEmpty(this.curOpenCityList)) {
                this.curOpenCityList.get(0).forEach(dsc -> dsc.reset());
            }

            String jobName = getWorldMapFunctionName() + "_" + "begin_check_" + curTimes;
            ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(jobName), (job) -> {
                checkWinOfOccupyTime(); // 检测州郡雄踞一方占领情况
            }, this.getCurBeginDate(), this.getCurEndTime(), 1);

            EventBus.getDefault().post(new Events.SyncDominateWorldMapChangeEvent(getWorldMapFunction(), createPb(false)));
        }
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
                initNextTime(1, false, false);
                break;
            case 2:
                initNextTime(0, true, false);
                break;
        }

        // 活动开放时才结算
        if (this.isOpen()) {
            this.playerStateDominateMap.clear();
            // 城池结算
            int now = TimeHelper.getCurrentSecond();
            if (CheckNull.nonEmpty(this.curOpenCityList)) {
                Optional.ofNullable(this.curOpenCityList.get(curTimes)).ifPresent(list -> {
                    list.forEach(sideCity -> {
                        retreatDominateArmy(sideCity);
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
    }

    /**
     * 初始化下一次活动时间
     *
     * @param index
     * @throws ParseException
     */
    private void initNextTime(int index, boolean checkCur, boolean checkNext) throws ParseException {
        int curIndex = index == 0 ? 1 : 0;

        Date nextDate;
        String timeStr;
        Date now = new Date();
        CronExpression cronExpression;
        Calendar c = Calendar.getInstance();
        timeStr = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME.get(curIndex);
        cronExpression = new CronExpression(timeStr);
        nextDate = cronExpression.getNextValidTimeAfter(now);
        nextDate = checkCur ? checkNotSameDate(now, nextDate, c) : nextDate;
        setCurPreviewDate(nextDate);

        timeStr = Constant.STATE_DOMINATE_WORLD_MAP_PREVIEW_TIME.get(index);
        cronExpression = new CronExpression(timeStr);
        nextDate = cronExpression.getNextValidTimeAfter(this.getCurPreviewDate());
        nextDate = checkNext ? checkNotSameDate(now, nextDate, c) : nextDate;
        setNextEndTime(nextDate);

        timeStr = Constant.STATE_DOMINATE_WORLD_MAP_BEGIN_TIME.get(curIndex);
        cronExpression = new CronExpression(timeStr);
        nextDate = cronExpression.getNextValidTimeAfter(now);
        nextDate = checkCur ? checkNotSameDate(now, nextDate, c) : nextDate;
        setCurBeginDate(nextDate);

        timeStr = Constant.STATE_DOMINATE_WORLD_MAP_BEGIN_TIME.get(index);
        cronExpression = new CronExpression(timeStr);
        nextDate = cronExpression.getNextValidTimeAfter(this.getCurBeginDate());
        nextDate = checkNext ? checkNotSameDate(now, nextDate, c) : nextDate;
        setNextBeginDate(nextDate);

        timeStr = Constant.STATE_DOMINATE_WORLD_MAP_END.get(curIndex);
        cronExpression = new CronExpression(timeStr);
        nextDate = cronExpression.getNextValidTimeAfter(now);
        nextDate = checkCur ? checkNotSameDate(now, nextDate, c) : nextDate;
        setCurEndTime(nextDate);

        timeStr = Constant.STATE_DOMINATE_WORLD_MAP_END.get(index);
        cronExpression = new CronExpression(timeStr);
        nextDate = cronExpression.getNextValidTimeAfter(this.getCurEndTime());
        nextDate = checkNext ? checkNotSameDate(now, nextDate, c) : nextDate;
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
    private void initMultiNextOpenCityList(WorldDataManager worldDataManager, List<Integer> stateList, Map<Integer, List<Integer>> areaMap, int curScheduleId, int times) {
        paddingStaticData(stateList, areaMap);
        List<Integer> cityList = randomStateCity(stateList, areaMap, curScheduleId);
        if (CheckNull.nonEmpty(cityList)) {
            cityList.forEach(cityId -> {
                City city = worldDataManager.getCityById(cityId);
                if (CheckNull.isNull(city)) return;
                DominateSideCity sideCity;
                if (city instanceof DominateSideCity) {
                    sideCity = (DominateSideCity) city;
                } else {
                    sideCity = new DominateSideCity(city);
                }
                this.nextOpenCityList.computeIfAbsent(times, l -> new ArrayList<>()).add(sideCity);
                worldDataManager.getCityMap().put(cityId, sideCity);
            });
        }
    }

    /**
     * 移除当前开放城池已使用的区域
     *
     * @param stateList
     * @param areaMap
     */
    private void removeCurOpenCityList(List<Integer> stateList, Map<Integer, List<Integer>> areaMap) {
        paddingStaticData(stateList, areaMap);
        if (CheckNull.nonEmpty(this.curOpenCityList)) {
            this.curOpenCityList.entrySet().forEach(en -> {
                List<DominateSideCity> sideCityList = en.getValue();
                if (CheckNull.isEmpty(sideCityList)) return;
                for (DominateSideCity sideCity : sideCityList) {
                    StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(sideCity.getCityId());
                    if (CheckNull.isNull(staticCity)) continue;
                    StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(staticCity.getArea());
                    if (CheckNull.isNull(staticArea)) continue;
                    if (CheckNull.isEmpty(staticArea.getGotoArea())) continue;
                    stateList.remove(Integer.valueOf(staticArea.getGotoArea().get(0)));
                    areaMap.remove(staticArea.getGotoArea().get(0));
                }
            });
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

        Set<Integer> stateSet = new HashSet<>();
        Collection<StaticArea> areaList = StaticWorldDataMgr.getAreaMap().values();
        if (CheckNull.nonEmpty(areaList)) {
            areaList.forEach(staticArea -> {
                if (CheckNull.isEmpty(staticArea.getGotoArea())) return;
                if (stateSet.add(staticArea.getGotoArea().get(0))) {
                    stateList.add(staticArea.getGotoArea().get(0));
                }
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
            createMultiDominateCity(worldDataManager, stateList, areaMap, curScheduleId, 0);
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

    public Map<Integer, List<DominateSideCity>> getNextOpenCityList() {
        return nextOpenCityList;
    }

    public void setNextOpenCityList(Map<Integer, List<DominateSideCity>> nextOpenCityList) {
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
        basePb.setPreviewTime((int) (this.getCurPreviewDate().getTime() / 1000l));
        basePb.setBeginTime((int) (this.getCurBeginDate().getTime() / 1000l));
        basePb.setEndTime((int) (this.getCurEndTime().getTime() / 1000l));

        if (this.isOpen() && state != WorldPb.WorldFunctionStateDefine.END_VALUE &&
                state != WorldPb.WorldFunctionStateDefine.NOT_START_VALUE) {
            builder.setNextPreviewTime((int) (this.nextPreviewDate.getTime() / 1000l));
            builder.setNextBeginTime((int) (this.nextBeginDate.getTime() / 1000l));
            builder.setNextEndTime((int) (this.nextEndTime.getTime() / 1000l));
            if (CheckNull.nonEmpty(this.curOpenCityList)) {
                WorldPb.CurDominateCityInfo.Builder cityPb = WorldPb.CurDominateCityInfo.newBuilder();
                this.curOpenCityList.get(0).forEach(dsc -> {
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
            if (CheckNull.nonEmpty(this.nextOpenCityList)) {
                WorldPb.CurDominateCityInfo.Builder cityPb = WorldPb.CurDominateCityInfo.newBuilder();
                this.nextOpenCityList.get(0).forEach(dsc -> {
                    cityPb.setCityId(dsc.getCityId());
                    cityPb.setCamp(dsc.getCamp());
                    cityPb.setIsOver(dsc.isOver());
                    if (CheckNull.nonEmpty(dsc.getCampRankDataMap())) {
                        dsc.getCampRankDataMap().values().forEach(crd -> {
                            cityPb.addCampRankInfo(crd.ser());
                        });
                    }
                    builder.addNextOpenCityInfo(cityPb.build());
                    cityPb.clear();
                });
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
