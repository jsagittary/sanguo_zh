package com.gryphpoem.game.zw.gameplay.local.world.dominate.impl;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.rank.Rank;
import com.gryphpoem.game.zw.core.rank.RankItem;
import com.gryphpoem.game.zw.core.rank.RealTimeRank;
import com.gryphpoem.game.zw.core.rank.SimpleRankComparatorFactory;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.dataMgr.StaticDominateDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideCity;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.PlayerSiLiDominateFightRecord;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.abs.TimeLimitDominateMap;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.quartz.jobs.DominateSideJob;
import com.gryphpoem.game.zw.quartz.jobs.RelicJob;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticDominateWarAward;
import com.gryphpoem.game.zw.resource.pojo.ActRank;
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

/**
 * Description: 司隶雄踞一方
 * Author: zhangpeng
 * createTime: 2022-11-22 16:02
 */
public class SiLiDominateWorldMap extends TimeLimitDominateMap {

    private static final int MAX_RANK_NUM = 1000000; // 排行榜上限
    // 玩家排行榜数据 key: 排行榜类型,不会进行存储,在服务器初始化时,会重新计算赋值
    private Map<Integer, RealTimeRank> ranks = new HashMap<>();
    private Map<Long, PlayerSiLiDominateFightRecord> recordMap = new HashMap<>();

    private static class InstanceHolder {
        private static final SiLiDominateWorldMap INSTANCE = new SiLiDominateWorldMap(
                WorldPb.WorldFunctionDefine.SI_LI_DOMINATE_SIDE_VALUE);
    }

    public static final SiLiDominateWorldMap getInstance() {
        return SiLiDominateWorldMap.InstanceHolder.INSTANCE;
    }

    public SiLiDominateWorldMap(int worldFunction) {
        super(worldFunction);
    }

    @Override
    public void initSchedule() {
        String jobName;
        String previewTimeCron = Constant.SI_LI_DOMINATE_PREVIEW_TIME;
        if (!StringUtils.isBlank(previewTimeCron)) {
            jobName = getWorldMapFunctionName() + "_" + DominateSideJob.PREVIEW;
            QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), jobName, "DominateSide", DominateSideJob.class, previewTimeCron);
        }

        String beginTimeCron = Constant.SI_LI_DOMINATE_BEGIN_TIME;
        if (!StringUtils.isBlank(beginTimeCron)) {
            jobName = getWorldMapFunctionName() + "_" + DominateSideJob.BEGIN;
            QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), jobName, "DominateSide", DominateSideJob.class, beginTimeCron);
        }

        String endTimeCron = Constant.SI_LI_DOMINATE_END_TIME;
        if (!StringUtils.isBlank(endTimeCron)) {
            jobName = getWorldMapFunctionName() + "_" + DominateSideJob.END;
            QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), jobName, "DominateSide", DominateSideJob.class, endTimeCron);
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
        this.curOpenCityList.clear();
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
        this.ranks.clear();
        this.recordMap.clear();
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
        Map<Integer, Map<Integer, Integer>> campOccupyMap = null;
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

            campOccupyMap = new HashMap<>();
            for (DominateSideCity sideCity : this.curOpenCityList.get(curTimes)) {
                StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(sideCity.getCityId());
                if (CheckNull.isNull(staticCity)) continue;
                int curCount = campOccupyMap.computeIfAbsent(sideCity.getCamp(), m -> new HashMap<>()).
                        computeIfAbsent(staticCity.getType(), l -> 0);
                curCount++;
                campOccupyMap.get(sideCity.getCamp()).put(staticCity.getType(), curCount);
            }
        }

        Date nowDate = new Date();
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        if (CheckNull.nonEmpty(this.recordMap)) {
            Date tmpDate = new Date();
            for (Map.Entry<Long, PlayerSiLiDominateFightRecord> entry : this.recordMap.entrySet()) {
                long curRoleId = entry.getKey();
                PlayerSiLiDominateFightRecord record = entry.getValue();
                if (CheckNull.isNull(record) || record.getKillCnt() < Constant.MINIMUM_FOR_KILLING_TO_RECEIVE_OCCUPATION_REWARDS)
                    continue;
                tmpDate.setTime(record.getKillRankTime());
                if (!DateHelper.isSameDate(tmpDate, nowDate))
                    continue;
                Player curPlayer = playerDataManager.getPlayer(curRoleId);
                if (CheckNull.isNull(curPlayer)) continue;
                // 金珠奖励
                StaticDominateWarAward award = StaticDominateDataMgr.findKillRankAward(record.getKillCnt());

                List<CommonPb.Award> awardList = null;
                if (Objects.nonNull(award)) {
                    awardList = new ArrayList<>();
                    awardList.add(PbHelper.createAwardPb(AwardType.SPECIAL, AwardType.Special.SHENG_WU, award.getAward()));
                }

                // 大中城奖励
                int totalCityOccupyCnt = 0;
                if (CheckNull.nonEmpty(campOccupyMap) &&
                        CheckNull.nonEmpty(Constant.BONUS_OCCUPATION_OF_SINGLE_BIG_CITY_AND_MIDDLE_CITY)) {
                    List<List<Integer>> cityAward = null;
                    Map<Integer, Integer> cityCountMap = campOccupyMap.get(curPlayer.lord.getCamp());
                    if (CheckNull.nonEmpty(cityCountMap)) {
                        int cnt;
                        if ((cnt = cityCountMap.getOrDefault(WorldConstant.CITY_TYPE_8, 0)) > 0) {
                            totalCityOccupyCnt += cnt;
                            cityAward = new ArrayList<>();
                            cityAward.add(Constant.BONUS_OCCUPATION_OF_SINGLE_BIG_CITY_AND_MIDDLE_CITY.get(0));
                            if (CheckNull.isNull(awardList)) awardList = new ArrayList<>();
                            awardList.addAll(PbHelper.createMultipleAwardsPb(cityAward, cnt));
                        }

                        if ((cnt = cityCountMap.getOrDefault(WorldConstant.CITY_TYPE_HOME, 0)) > 0) {
                            totalCityOccupyCnt += cnt;
                            if (CheckNull.nonEmpty(cityAward)) {
                                cityAward.clear();
                            } else {
                                cityAward = new ArrayList<>();
                            }
                            cityAward.add(Constant.BONUS_OCCUPATION_OF_SINGLE_BIG_CITY_AND_MIDDLE_CITY.get(1));
                            if (CheckNull.isNull(awardList)) awardList = new ArrayList<>();
                            awardList.addAll(PbHelper.createMultipleAwardsPb(cityAward, cnt));
                        }
                    }
                }

                if (CheckNull.nonEmpty(awardList)) {
                    mailDataManager.sendAttachMail(curPlayer, awardList, MailConstant.MOLD_SI_LI_DOMINATE_AWARD, AwardFrom.DO_SOME,
                            curPlayer.lord.getCamp(), totalCityOccupyCnt, record.getKillCnt(), Objects.nonNull(award) ? award.getAward() : 0);
                }
            }
        }


        // 初始化下次开放活动时间
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

        // 清理数据
        close();
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
                onPreview();
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
                if (CheckNull.nonEmpty(ranks)) {
                    RealTimeRank realTimeRank = getPlayerRanks(0);
                    RankItem rankItem = realTimeRank.getRankItem(1);
                    if (Objects.nonNull(rankItem) &&
                            !DateHelper.isToday(new Date(rankItem.getLastModifyTime()))) {
                        this.ranks.clear();
                        this.recordMap.clear();
                    }
                }
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
        if (CheckNull.isEmpty(Constant.SI_LI_DOMINATE_OPEN_CITY)) return;
        for (List<Integer> cityIdList : Constant.SI_LI_DOMINATE_OPEN_CITY) {
            if (CheckNull.isEmpty(cityIdList)) continue;
            cityIdList.forEach(cityId -> createDominateCity(worldDataManager, cityId));
        }
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

    public PlayerSiLiDominateFightRecord getPlayerRecord(long roleId) {
        PlayerSiLiDominateFightRecord record = this.recordMap.get(roleId);
        if (CheckNull.isNull(record)) {
            record = new PlayerSiLiDominateFightRecord();
            this.recordMap.put(roleId, record);
        }
        return record;
    }

    public RealTimeRank getPlayerRanks(int type) {
        // 如果没有刷新则刷新数据
        return ranks.computeIfAbsent(type, k -> {
            Comparator<RankItem<Integer>> c = SimpleRankComparatorFactory.createDescComparable();
            return new RealTimeRank(MAX_RANK_NUM, c);
        });
    }

    public void addPlayerRank(long lordId, int value, int now, int rankType) {
        RealTimeRank killArmyRank = getPlayerRanks(rankType);
        killArmyRank.update(new RankItem(lordId, value, System.currentTimeMillis()));
    }

}
