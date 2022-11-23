package com.gryphpoem.game.zw.gameplay.local.world.dominate.impl;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideCity;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.abs.TimeLimitDominateMap;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.quartz.jobs.RelicJob;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.pojo.season.CampRankData;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.WorldScheduleService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.*;

/**
 * Description: 司隶雄踞一方
 * Author: zhangpeng
 * createTime: 2022-11-22 16:02
 */
public class SiLiDominateWorldMap extends TimeLimitDominateMap {

    private static class InstanceHolder {
        private static final SiLiDominateWorldMap INSTANCE = new SiLiDominateWorldMap(
                WorldPb.WorldFunctionDefine.SI_LI_DOMINATE_SIDE_VALUE);
    }

    public static final SiLiDominateWorldMap getInstance() {
        return SiLiDominateWorldMap.InstanceHolder.INSTANCE;
    }

    /**
     * 都城名城列表
     */
    public static final int[] FAMOUS_CITY = new int[]{201, 202, 203, 204, 302, 309, 312, 319};

    public SiLiDominateWorldMap(int worldFunction) {
        super(worldFunction);
    }

    @Override
    public void initSchedule() {
        String jobName;
        String previewTimeCron = Constant.SI_LI_DOMINATE_PREVIEW_TIME;
        if (!StringUtils.isBlank(previewTimeCron)) {
            jobName = getWorldMapFunctionName() + "_preview";
            QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), jobName, "DominateSide", RelicJob.class, previewTimeCron);
        }

        String beginTimeCron = Constant.SI_LI_DOMINATE_BEGIN_TIME;
        if (!StringUtils.isBlank(beginTimeCron)) {
            jobName = getWorldMapFunctionName() + "_begin";
            QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), jobName, "DominateSide", RelicJob.class, beginTimeCron);
        }

        String endTimeCron = Constant.SI_LI_DOMINATE_END_TIME;
        if (!StringUtils.isBlank(endTimeCron)) {
            jobName = getWorldMapFunctionName() + "_end";
            QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), jobName, "DominateSide", RelicJob.class, endTimeCron);
        }
    }

    @Override
    public WorldPb.BaseWorldFunctionPb createPb(boolean isSaveDb) {
        int state = state();
        WorldPb.BaseWorldFunctionPb.Builder basePb = WorldPb.BaseWorldFunctionPb.newBuilder();
        basePb.setState(state);
        basePb.setOpen(this.isOpen());
        basePb.setFunction(WorldPb.WorldFunctionDefine.SI_LI_DOMINATE_SIDE_VALUE);
        if (this.isOpen() && state != WorldPb.WorldFunctionStateDefine.END_VALUE &&
                state != WorldPb.WorldFunctionStateDefine.NOT_START_VALUE) {
            if (CheckNull.nonEmpty(this.curOpenCityList)) {
                Optional.ofNullable(this.curOpenCityList.get(this.curTimes)).ifPresent(list -> {
                    WorldPb.CurDominateCityInfo.Builder cityPb = WorldPb.CurDominateCityInfo.newBuilder();
                    list.forEach(dsc -> {
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
                });
            }
        }
        return basePb.build();
    }

    @Override
    public void close() {

    }

    /**
     * 司隶-雄踞一方活动预显示
     */
    public void onPreview() {
        this.curOpenCityList.clear();
        createMultiDominateCity(DataResource.ac.getBean(WorldDataManager.class));
    }

    /**
     * 司隶-雄踞一方活动开始
     */
    public void onBegin() {
        String jobName = getWorldMapFunctionName() + "_" + "begin_check_" + curTimes;
        ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(jobName), (job) -> {
            checkWinOfOccupyTime(); // 检测柏林占领时间
        }, this.getCurBeginDate(), this.getCurEndTime(), 1);
    }

    /**
     * 司隶-雄踞一方活动结束
     */
    public void onEnd() throws ParseException {
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

            this.curOpenCityList.clear();
        }

        // 初始化下次开放活动时间
        Date nowDate = new Date();
        String previewTimeCron = Constant.SI_LI_DOMINATE_PREVIEW_TIME;
        if (!StringUtils.isBlank(previewTimeCron)) {
            CronExpression cronExpression = new CronExpression(previewTimeCron);
            Date nextDate = cronExpression.getNextValidTimeAfter(nowDate);
            setCurPreviewDate(nextDate);
        }
        String beginTimeCron = Constant.SI_LI_DOMINATE_BEGIN_TIME;
        if (!StringUtils.isBlank(beginTimeCron)) {
            CronExpression cronExpression = new CronExpression(beginTimeCron);
            Date nextDate = cronExpression.getNextValidTimeAfter(nowDate);
            setCurBeginDate(nextDate);
        }
        String endTimeCron = Constant.SI_LI_DOMINATE_END_TIME;
        if (!StringUtils.isBlank(endTimeCron)) {
            CronExpression cronExpression = new CronExpression(endTimeCron);
            Date nextDate = cronExpression.getNextValidTimeAfter(nowDate);
            setCurEndTime(nextDate);
        }
    }

    /**
     * 服务器启动后处理活动时间数据
     *
     * @throws ParseException
     */
    public void handleOnStartup() throws ParseException {
        Date now = new Date();
        WorldDataManager worldDataManager = DataResource.ac.getBean(WorldDataManager.class);
        if (getCurPreviewDate() == null) {
            String previewTimeCron = Constant.SI_LI_DOMINATE_PREVIEW_TIME;
            if (!StringUtils.isBlank(previewTimeCron)) {
                CronExpression cronExpression = new CronExpression(previewTimeCron);
                Date nextDate = cronExpression.getNextValidTimeAfter(now);
                setCurPreviewDate(nextDate);
            }
        } else {
            if (now.after(getCurPreviewDate()) && now.before(getCurEndTime()) && CheckNull.isEmpty(this.curOpenCityList)) {
                // 若比当前时间晚且未结束且未当前开放城池未空 (停服期 间 错过了预显示定时器)
                createMultiDominateCity(worldDataManager);
            }
        }

        if (getCurBeginDate() == null) {
            String beginTimeCron = Constant.SI_LI_DOMINATE_BEGIN_TIME;
            if (!StringUtils.isBlank(beginTimeCron)) {
                CronExpression cronExpression = new CronExpression(beginTimeCron);
                Date nextDate = cronExpression.getNextValidTimeAfter(now);
                setCurBeginDate(nextDate);
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
            String endTimeCron = Constant.SI_LI_DOMINATE_END_TIME;
            if (!StringUtils.isBlank(endTimeCron)) {
                CronExpression cronExpression = new CronExpression(endTimeCron);
                Date nextDate = cronExpression.getNextValidTimeAfter(now);
                setCurEndTime(nextDate);
            }
        } else {
            if (now.after(getCurEndTime())) {
                // 在停服期间, 活动已结束
                onEnd();
            }
        }

        // 若世界进程足够, 则活动开启
        int curScheduleId = DataResource.ac.getBean(WorldScheduleService.class).getCurrentSchduleId();
        if (curScheduleId >= Constant.OPEN_STATE_DOMINATE_WORLD_MAP_FUNCTION_CONDITION.get(0)) {
            this.setOpen(true);
        } else {
            this.setOpen(false);
        }
    }

    /**
     * 创建所有都城名城 雄踞一方城池
     *
     * @param worldDataManager
     */
    private void createMultiDominateCity(WorldDataManager worldDataManager) {
        for (int cityId : FAMOUS_CITY)
            createDominateCity(worldDataManager, cityId);
    }

    /**
     * 创建雄踞一方城池
     *
     * @param worldDataManager
     * @param cityId
     */
    private void createDominateCity(WorldDataManager worldDataManager, int cityId) {
        City city = worldDataManager.getCityById(cityId);
        if (CheckNull.isNull(city)) return;
        DominateSideCity sideCity;
        if (city instanceof DominateSideCity) {
            sideCity = (DominateSideCity) city;
        } else {
            sideCity = new DominateSideCity(city);
        }
        this.curOpenCityList.computeIfAbsent(this.curTimes, l -> new ArrayList<>(2)).
                add(sideCity);
        // 重置城池归属
        sideCity.setOver(false);
        sideCity.setCamp(Constant.Camp.NPC);
        worldDataManager.getCityMap().put(cityId, sideCity);
    }
}
