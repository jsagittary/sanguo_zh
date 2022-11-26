package com.gryphpoem.game.zw.gameplay.local.world.dominate.abs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideCity;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.WorldMapPlay;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.pojo.season.CampRankData;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description: 限时雄踞一方地图玩法
 * Author: zhangpeng
 * createTime: 2022-11-22 14:56
 */
public abstract class TimeLimitDominateMap implements WorldMapPlay {
    /** 当前活动次数*/
    protected int curTimes;
    /** 活动是否开放*/
    private boolean open;
    /** 世界地图玩法*/
    private int worldFunction;
    /** 当前预显示时间*/
    private Date curPreviewDate;
    /** 当前活动开始时间*/
    private Date curBeginDate;
    /** 当前活动结束时间*/
    private Date curEndTime;
    /** 开放的城池 <活动次数, 开放的城池list>*/
    protected Map<Integer, List<DominateSideCity>> curOpenCityList;

    public TimeLimitDominateMap() {
    }

    public TimeLimitDominateMap(int worldFunction) {
        this.worldFunction = worldFunction;
        this.curOpenCityList = new HashMap<>();
    }

    public void deserialize(SerializePb.SerTimeLimitDominateMap ser) {
        this.curPreviewDate = new Date(ser.getCurPreviewDate());
        this.curEndTime = new Date(ser.getCurEndTime());
        this.curBeginDate = new Date(ser.getCurBeginDate());
        this.open = ser.getOpen();
        this.curTimes = ser.getCurTimes();
        if (CheckNull.isNull(curOpenCityList)) {
            this.curOpenCityList = new HashMap<>();
        }
        if (CheckNull.nonEmpty(ser.getCityList())) {
            WorldDataManager worldDataManager = DataResource.ac.getBean(WorldDataManager.class);
            for (SerializePb.SerOpenDominateSideCity pb : ser.getCityList()) {
                List<DominateSideCity> cityList = this.curOpenCityList.computeIfAbsent(pb.getTimes(), l -> new ArrayList<>());
                if (CheckNull.nonEmpty(pb.getCityIdList())) {
                    pb.getCityIdList().forEach(cityId -> {
                        cityList.add((DominateSideCity) worldDataManager.getCityMap().get(cityId));
                    });
                }
            }
        }
    }

    @Override
    public int getWorldMapFunction() {
        return worldFunction;
    }

    public Date getCurPreviewDate() {
        return curPreviewDate;
    }

    public void setCurPreviewDate(Date curPreviewDate) {
        this.curPreviewDate = curPreviewDate;
    }

    public Date getCurBeginDate() {
        return curBeginDate;
    }

    public void setCurBeginDate(Date curBeginDate) {
        this.curBeginDate = curBeginDate;
    }

    public Date getCurEndTime() {
        return curEndTime;
    }

    public void setCurEndTime(Date curEndTime) {
        this.curEndTime = curEndTime;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public Map<Integer, List<DominateSideCity>> getCurOpenCityList() {
        return curOpenCityList;
    }

    public int getCurTimes() {
        return curTimes;
    }

    /**
     * 加入定时器
     *
     * @param name              定时器名称
     * @param r                 雄踞一方定时任务
     * @param time              定时时间
     */
    protected void addSchedule(String name, TimeLimitDominateMap.DominateJob r, Date time, TimeLimitDominateMap dominateMap) {
        Date now = new Date();
        if (now.before(time)) {
            ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(name), job -> r.onRun(dominateMap, job), time);
            LogUtil.world("雄踞一方定时器初始  name:", name, ", time:", DateHelper.formatDateMiniTime(time));
        } else {
            LogUtil.error("雄踞一方定时器超时 name:", name, ", time:", DateHelper.formatDateMiniTime(time), ", now:", now, ", time:", time);
        }
    }

    /**
     * 雄踞一方定时任务接口
     */
    private interface DominateJob {

        void onRun(TimeLimitDominateMap timeLimitDominateMap, DefultJob job);
    }

    @Override
    public int state() {
        if (!open) {
            return WorldPb.WorldFunctionStateDefine.NOT_START_VALUE;
        }
        Date now = new Date();
        if (now.before(this.getCurPreviewDate())) {
            return WorldPb.WorldFunctionStateDefine.NOT_START_VALUE;
        } else if (now.after(this.getCurPreviewDate()) && now.before(this.getCurBeginDate())) {
            return WorldPb.WorldFunctionStateDefine.ON_PREVIEW_VALUE;
        } else if (now.after(this.getCurBeginDate()) && now.before(this.getCurEndTime())) {
            return WorldPb.WorldFunctionStateDefine.IN_PROGRESS_VALUE;
        } else {
            return WorldPb.WorldFunctionStateDefine.END_VALUE;
        }
    }

    @Override
    public String getWorldMapFunctionName() {
        return "Dominate_" + getWorldMapFunction();
    }

    public SerializePb.SerTimeLimitDominateMap createMapPb(boolean isSaveDb) {
        SerializePb.SerTimeLimitDominateMap.Builder builder = SerializePb.SerTimeLimitDominateMap.newBuilder();
        builder.setCurTimes(this.curTimes);
        builder.setCurEndTime(this.curEndTime.getTime());
        builder.setCurBeginDate(this.curBeginDate.getTime());
        builder.setCurPreviewDate(this.curPreviewDate.getTime());
        builder.setOpen(open);
        builder.setWorldFunction(getWorldMapFunction());
        if (CheckNull.nonEmpty(curOpenCityList)) {
            builder.addAllCity(this.curOpenCityList.entrySet().stream().
                    map(PbHelper::openDominateSideCityPb).collect(Collectors.toList()));
        }
        return builder.build();
    }

    /**
     * 检测当前活动是否有城池阵营胜出
     */
    public void checkWinOfOccupyTime() {
        if (state() != WorldPb.WorldFunctionStateDefine.IN_PROGRESS_VALUE) {
            LogUtil.error("雄踞一方活动未在进行中");
            return;
        }
        List<DominateSideCity> sideCityList;
        if (CheckNull.isEmpty(this.curOpenCityList) || CheckNull.isEmpty(
                sideCityList = this.curOpenCityList.get(this.curTimes))) {
            LogUtil.error("活动进行中, 正在开放的城池为空");
            return;
        }

        boolean sync = false;
        int now = TimeHelper.getCurrentSecond();
        for (DominateSideCity sideCity : sideCityList) {
            if (CheckNull.isNull(sideCity)) continue;
            if (sideCity.isOver()) continue;
            for (int camp : Constant.Camp.camps) {
                int campOccupyTime = sideCity.getCampOccupyTime(now, camp, getVictoryConfig());
                if (campOccupyTime >= Constant.TEN_THROUSAND) {
                    sideCity.setOver(true);
                    sideCity.setCamp(camp);
                    if (!sync) sync = true;
                    break;
                }
            }
        }

        if (sync) {
            EventBus.getDefault().post(new Events.SyncDominateWorldMapChangeEvent(getWorldMapFunction(), createPb(false)));
        }
    }

    public List<Integer> getVictoryConfig() {
        switch (getWorldMapFunction()) {
            case WorldPb.WorldFunctionDefine.STATES_AND_COUNTIES_DOMINATE_VALUE:
                return Constant.STATE_DOMINATE_WORLD_MAP_VICTORY_OCCUPY_CONFIG;
            case WorldPb.WorldFunctionDefine.SI_LI_DOMINATE_SIDE_VALUE:
                return Constant.SI_LI_DOMINATE_VICTORY_CONFIG;
            default:
                return null;
        }
    }

    /**
     * 将当前City转化为DominateSideCity
     *
     * @param worldDataManager
     * @param cityId
     * @param times
     */
    protected void createDominateCity(WorldDataManager worldDataManager, int cityId, int times) {
        City city = worldDataManager.getCityById(cityId);
        if (CheckNull.isNull(city)) return;
        DominateSideCity sideCity;
        if (city instanceof DominateSideCity) {
            sideCity = (DominateSideCity) city;
        } else {
            sideCity = new DominateSideCity(city);
        }
        this.curOpenCityList.computeIfAbsent(times, l -> new ArrayList<>(2)).
                add(sideCity);
        // 重置城池归属
        sideCity.reset();
        worldDataManager.getCityMap().put(cityId, sideCity);
    }

    protected List<CampRankData> sortCampRank(Collection<CampRankData> cols) {
        List<CampRankData> list = new ArrayList<>(cols);
        list.sort(COMPARATOR_CAMP_RANK);
        int i = 0;
        for (CampRankData campRankData : list) {
            campRankData.rank = ++i;
        }
        return list;
    }

    private static final Comparator<CampRankData> COMPARATOR_CAMP_RANK = (o1, o2) -> {
        if (o1.value < o2.value) {
            return 1;
        } else if (o1.value > o2.value) {
            return -1;
        } else {
            if (o1.time > o2.time) {
                return 1;
            } else if (o1.time < o2.time) {
                return -1;
            }
        }
        return 0;
    };
}
