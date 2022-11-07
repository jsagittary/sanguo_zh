package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.simulator.LifeSimulatorInfo;
import com.gryphpoem.game.zw.resource.domain.s.StaticCharacter;
import com.gryphpoem.game.zw.resource.domain.s.StaticCharacterReward;
import com.gryphpoem.game.zw.resource.domain.s.StaticFireworks;
import com.gryphpoem.game.zw.resource.domain.s.StaticFishing;
import com.gryphpoem.game.zw.resource.domain.s.StaticFishingLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityCell;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityFoundation;
import com.gryphpoem.game.zw.resource.domain.s.StaticRelic;
import com.gryphpoem.game.zw.resource.domain.s.StaticRelicFraction;
import com.gryphpoem.game.zw.resource.domain.s.StaticRelicShop;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorChoose;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorStep;
import com.gryphpoem.game.zw.resource.util.ActParamTabLoader;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    private static List<StaticSimulatorChoose> staticSimulatorChooseList; // 人生模拟器选项配置
    private static List<StaticSimulatorStep> staticSimulatorStepList; // 人生模拟器步骤配置
    private static List<StaticCharacter> staticCharacterList; // 性格
    private static List<StaticCharacterReward> staticCharacterRewardList; // 性格奖励
    private static List<StaticSimCity> staticSimCityList; // 性格奖励
    private static List<StaticHomeCityCell> staticHomeCityCellList; // 主城地图格
    private static List<StaticHomeCityFoundation> staticHomeCityFoundationList; // 主城地基


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

        staticSimulatorChooseList = staticIniDao.selectStaticSimulatorChooseList();
        staticSimulatorStepList = staticIniDao.selectStaticSimulatorStepList();
        staticCharacterList = staticIniDao.selectStaticCharacterList();
        staticCharacterRewardList = staticIniDao.selectStaticCharacterRewardList();
        staticSimCityList = staticIniDao.selectStaticSimCityList();
        staticHomeCityCellList = staticIniDao.selectStaticHomeCityCellList();
        staticHomeCityFoundationList = staticIniDao.selectStaticHomeCityFoundationList();
    }

    @Override
    public void check() {

    }

    public static StaticHomeCityCell getStaticHomeCityCellById(Integer id) {
        return staticHomeCityCellList.stream().filter(tmp -> Objects.equals(id, tmp.getId())).findFirst().orElse(null);
    }

    public static StaticHomeCityFoundation getStaticHomeCityFoundationById(Integer id) {
        return staticHomeCityFoundationList.stream().filter(tmp -> Objects.equals(id, tmp.getId())).findFirst().orElse(null);
    }

    public static StaticSimulatorChoose getStaticSimulatorChoose(int id) {
        return staticSimulatorChooseList.stream().filter(staticSimulatorChoose -> staticSimulatorChoose.getId() == id).findFirst().orElse(null);
    }

    public static StaticSimulatorStep getStaticSimulatorStep(long id) {
        return staticSimulatorStepList.stream().filter(staticSimulatorStep -> staticSimulatorStep.getId() == id).findFirst().orElse(null);
    }

    public static List<Integer> getCharacterRange(int id) {
        StaticCharacter staticCharacter = staticCharacterList.stream().filter(tmp -> tmp.getId() == id).findFirst().orElse(null);
        if (staticCharacter != null && CheckNull.nonEmpty(staticCharacter.getRange())) {
            return staticCharacter.getRange();
        } else {
            return null;
        }
    }

    public static List<StaticCharacterReward> getStaticCharacterRewardList() {
        return staticCharacterRewardList;
    }

    public static List<StaticSimCity> getStaticSimCityList() {
        return staticSimCityList;
    }

    public static List<StaticSimCity> getCanRandomSimCityList(Player player) {
        List<LifeSimulatorInfo> lifeSimulatorInfoList = player.getCityEvent().getLifeSimulatorInfoList();
        List<StaticSimCity> canRandomSimCityList = new ArrayList<>();
        for (StaticSimCity staticSimCity : staticSimCityList) {
            int needLordLv = staticSimCity.getLordLv();
            if (player.lord.getLevel() < needLordLv) {
                continue;
            }
            List<List<Integer>> needBuildLvList = staticSimCity.getBuildLv();
            boolean checkBuildLv = needBuildLvList.stream().anyMatch(tmp -> DataResource.ac.getBean(BuildingDataManager.class).checkBuildingLv(player, tmp));
            if (checkBuildLv) {
                continue;
            }
            List<Integer> open = staticSimCity.getOpen();
            boolean checkBuildOrNpcHasBind = lifeSimulatorInfoList.stream().anyMatch(tmp -> tmp.getBindType() == open.get(0) && tmp.getBindId() == open.get(1));
            if (checkBuildOrNpcHasBind) {
                continue;
            }
            canRandomSimCityList.add(staticSimCity);
        }
        return canRandomSimCityList;
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
