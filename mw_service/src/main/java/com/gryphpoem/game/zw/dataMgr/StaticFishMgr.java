package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.SystemTabLoader;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xwind
 * @date 2021/8/6
 */
@Service
public class StaticFishMgr extends AbsStaticIniService {

    private static List<StaticFishBaitHerocombination> staticFishBaitHerocombinationList;

    private static List<List<Integer>> nonGroupBaits;

    private static List<StaticFishBait> staticFishBaitList;
    private static Map<Integer, StaticFishBait> staticFishBaitMap;//<BaitId,Object>
    private static Map<Integer, StaticFishBait> staticFishBaitPropMap;//<PropId,Object>

    private static Map<Integer, StaticFishResults> staticFishResultsMap;//<colorID,Object>

    private static Map<Integer, StaticFishattribute> staticFishattributeMap;//<fishId,Object>

    private static List<StaticFishProficiency> staticFishProficiencyList;
    private static Map<Integer,StaticFishProficiency> staticFishProficiencyMap;

    private static Map<Integer,StaticFishShop> staticFishShopMap;

    private static int shareLogLimit;

    private static List<List<Integer>> firstGetList;

    @Override
    public void load() {
        staticFishBaitHerocombinationList = staticIniDao.selectStaticFishBaitHerocombinationList();

        nonGroupBaits = SystemTabLoader.getListListIntSystemValue(1076, "[[1,10000],[3,2500]]");

        staticFishBaitList = staticIniDao.selectStaticFishBaitList();
        staticFishBaitMap = staticFishBaitList.stream().collect(Collectors.toMap(StaticFishBait::getBaitID, v -> v));
        staticFishBaitPropMap = staticFishBaitList.stream().collect(Collectors.toMap(StaticFishBait::getPropID, v -> v));

        staticFishResultsMap = staticIniDao.selectStaticFishResultsMap();

        staticFishattributeMap = staticIniDao.selectStaticFishattributeMap();

        staticFishProficiencyList = staticIniDao.selectStaticFishProficiencyList();
        staticFishProficiencyMap = staticFishProficiencyList.stream().collect(Collectors.toMap(StaticFishProficiency::getTitleID,v->v));

        staticFishShopMap = staticIniDao.selectStaticFishShopMap();

        shareLogLimit = SystemTabLoader.getIntegerSystemValue(1079,2);
        firstGetList = SystemTabLoader.getListListIntSystemValue(1084,"[[8,1],[13,1]]");
    }

    @Override
    public void check() {

    }

    public static List<List<Integer>> getFirstGetList() {
        return firstGetList;
    }

    public static int getShareLogLimit() {
        return shareLogLimit;
    }

    public static StaticFishShop getStaticFishShop(int id) {
        return staticFishShopMap.get(id);
    }

    public static List<StaticFishProficiency> getStaticFishProficiencyList() {
        return staticFishProficiencyList;
    }

    public static StaticFishProficiency getStaticFishProficiency(int titleId) {
        return staticFishProficiencyMap.get(titleId);
    }

    public static StaticFishattribute getStaticFishattribute(int fishId) {
        return staticFishattributeMap.get(fishId);
    }

    public static StaticFishResults getStaticFishResults(int colorId) {
        return staticFishResultsMap.get(colorId);
    }

    public static List<StaticFishBaitHerocombination> getStaticFishBaitHerocombinationList() {
        return staticFishBaitHerocombinationList;
    }

    public static List<List<Integer>> getNonGroupBaits() {
        return nonGroupBaits;
    }

    public static List<StaticFishBait> getStaticFishBaitList() {
        return staticFishBaitList;
    }

    public static StaticFishBait getStaticFishBait(int baitId) {
        return staticFishBaitMap.get(baitId);
    }

    public static StaticFishBait getStaticFishBaitByPropId(int propId) {
        return staticFishBaitPropMap.get(propId);
    }
}
