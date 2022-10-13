package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.ActParamTabLoader;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xwind
 * @date 2021/12/20
 */
@Service
public class StaticDataMgr extends AbsStaticIniService {

    //2022新年活动
    private static List<StaticFireworks> fireworksList;//放烟花
    private static Map<Integer, List<StaticFireworks>> fireworksGroupMap;
    private static Map<Integer, StaticFireworks> fireworksMap;
    private static List<Integer> fireworksInterval;//放烟花系统烟花间隔分钟
    private static List<Integer> fireworksPreview;//放烟花开始预告间隔
    private static List<List<Integer>> fireworksLimit;//放烟花每日次数限制
    private static List<StaticFishing> staticFishingList;//年年有鱼种类配置
    private static Map<Integer, StaticFishing> staticFishingMap;
    private static List<StaticFishingLv> staticFishingLvList;//年年有鱼难度配置
    private static Map<Integer, Map<Integer, StaticFishingLv>> staticFishingLvGroupMap;
    private static List<Integer> fishDayFree;//年年有鱼每日免费次数
    //遗迹
    private static List<StaticRelic> staticRelicList;
    private static List<StaticRelicShop> staticRelicShopList;
    private static Map<Integer, StaticRelicShop> staticRelicShopMap;
    private static Map<Integer, StaticRelicFraction> staticRelicFractionMap;

    @Override
    public void load() {
        staticRelicFractionMap = staticIniDao.selectStaticRelicFractionMap();
        staticRelicList = staticIniDao.selectStaticRelicList();
        staticRelicShopList = staticIniDao.selectStaticRelicShopList();
        staticRelicShopMap = staticRelicShopList.stream().collect(Collectors.toMap(StaticRelicShop::getId, Function.identity()));

        fireworksList = staticIniDao.selectStaticFireworksList();
        fireworksGroupMap = fireworksList.stream().collect(Collectors.groupingBy(StaticFireworks::getActivityId));
        fireworksMap = fireworksList.stream().collect(Collectors.toMap(StaticFireworks::getId, v -> v));
        fireworksInterval = ActParamTabLoader.getListIntSystemValue(370, "[]");
        fireworksPreview = ActParamTabLoader.getListIntSystemValue(371, "[]");
        fireworksLimit = ActParamTabLoader.getListListIntSystemValue(372, "[[]]");
        fishDayFree = ActParamTabLoader.getListIntSystemValue(373, "[]");
        staticFishingList = staticIniDao.selectStaticFishingList();
        staticFishingMap = staticFishingList.stream().collect(Collectors.toMap(StaticFishing::getId, v -> v));
        staticFishingLvList = staticIniDao.selectStaticFishingLvList();
        staticFishingLvGroupMap = staticFishingLvList.stream().collect(Collectors.groupingBy(StaticFishingLv::getActivityId, Collectors.toMap(StaticFishingLv::getLv, v -> v)));
    }

    @Override
    public void check() {

    }

    public static StaticRelicFraction getStaticRelicFraction(int curScheduleId) {
        if (CheckNull.isEmpty(staticRelicFractionMap)) return null;
        return staticRelicFractionMap.values().stream().filter(s -> CheckNull.nonEmpty(s.getArea()) &&
                s.getArea().get(0) <= curScheduleId && s.getArea().get(1) >= curScheduleId).findFirst().orElse(null);
    }

    public static List<StaticRelicShop> getStaticRelicShopList(int curScheduleId) {
        if (CheckNull.isEmpty(staticRelicShopMap)) return null;
        return staticRelicShopMap.values().stream().filter(staticRelicShop -> staticRelicShop.getArea().get(0) <= curScheduleId &&
                staticRelicShop.getArea().get(1) >= curScheduleId).collect(Collectors.toList());
    }

    public static StaticRelicShop getStaticRelicShopById(int id) {
        return staticRelicShopMap.get(id);
    }

    public static List<StaticRelic> getStaticRelic(int era) {
        return staticRelicList.stream().filter(o -> era >= o.getEra().get(0) && era <= o.getEra().get(1)).collect(Collectors.toList());
    }

    public static List<Integer> getFishDayFree() {
        return fishDayFree;
    }

    public static int fireworksLimit(int activityId) {
        List<Integer> list = fireworksLimit.stream().filter(o -> o.get(0) == activityId).findFirst().orElse(null);
        if (ListUtils.isNotBlank(list)) {
            return list.get(1);
        }
        return 0;
    }

    public static StaticFishing getStaticFishing(int id, int activityId) {
        return staticFishingList.stream().filter(o -> o.getActivityId() == activityId && o.getId() == id).findFirst().orElse(null);
    }

    public static StaticFishing getStaticFishing(int id) {
        return staticFishingMap.get(id);
    }

    public static StaticFishingLv getStaticFishingLv(int activityId, int lv) {
        Map<Integer, StaticFishingLv> map = staticFishingLvGroupMap.get(activityId);
        if (Objects.nonNull(map)) {
            return map.get(lv);
        }
        return null;
    }

    public static List<Integer> getFireworksInterval() {
        return fireworksInterval;
    }

    public static List<Integer> getFireworksPreview() {
        return fireworksPreview;
    }

    public static StaticFireworks getFireworks(int id) {
        return fireworksMap.get(id);
    }

    public static List<StaticFireworks> getFireworksList(int actId) {
        return fireworksGroupMap.get(actId);
    }
}
