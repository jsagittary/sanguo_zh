package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.*;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

public class StaticIniDataMgr {
    private StaticIniDataMgr() {
    }

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    private static List<String> markList = new ArrayList<String>();
    private static List<String> familyList = new ArrayList<String>();
    private static List<String> manList = new ArrayList<String>();
    private static List<String> womanList = new ArrayList<String>();

    // 全局常量配置信息
    private static Map<Integer, StaticSystem> systemMap;

    // 活动参数配置信息
    private static Map<Integer, StaticSystem> actParamMap;

    // 多语言文字描述信息
    private static Map<String, StaticText> textMap;

    private static List<StaticSandTableAward> sandTableAwards;
    private static StaticSandTableAward sandTableKillingRewardMaxParam;

    private static Map<Integer, StaticSandTableExchange> sandTableExchangeMap;

    private static List<StaticDiaoChanDay> staticDiaoChanDayList;
    private static List<StaticDiaoChanDayTask> staticDiaoChanDayTaskList;
    private static List<StaticDiaoChanRank> staticDiaoChanRankList;
    private static List<StaticDiaoChanAward> staticDiaoChanAwardList;

    private static List<StaticSeasonTalentPlan> staticSeasonTalentPlanList;
    private static Map<Integer, StaticSeasonTalentPlan> staticSeasonTalentPlanMap;
    private static List<StaticSeasonPlan> staticSeasonPlanList;
    private static Map<Integer, StaticSeasonPlan> staticSeasonPlanMap;
    private static Map<Integer, StaticSeasonTreasury> staticSeasonTreasuryMap;
    private static Map<Integer, StaticSeasonTask> staticSeasonTaskMap;
    private static Map<Integer, List<StaticSeasonTask>> staticSeasonTaskGroupMap;
    private static Map<Integer, StaticSeasonTaskScore> staticSeasonTaskScoreMap;
    private static Map<Integer, List<StaticSeasonTaskScore>> staticSeasonTaskScoreGroup;
    private static List<StaticSeasonRank> staticSeasonRankList;
    private static Map<Integer, List<StaticSeasonRank>> staticSeasonRankGroupMap;
    private static Map<Integer, Integer> seasonRankMax = new HashMap<>();
    private static Map<Integer, StaticSeasonTalent> seasonTalentMap;

    public static void init() {
        StaticIniDataMgr.initName();

        StaticIniDataMgr.initSystem();

        Map<String, StaticText> textMap = staticDataDao.selectTextMap();
        StaticIniDataMgr.textMap = textMap;

        sandTableAwards = staticDataDao.selectStaticSandTableAwardList();
        sandTableKillingRewardMaxParam = getStaticSandTableAwardMaxParam(2);

        sandTableExchangeMap = staticDataDao.selectStaticSandTableExchangeMap();

        staticDiaoChanDayList = staticDataDao.selectStaticDiaoChanDayList();
        staticDiaoChanDayTaskList = staticDataDao.selectStaticDiaoChanDayTaskList();
        staticDiaoChanRankList = staticDataDao.selectStaticDiaoChanRankList();
        staticDiaoChanAwardList = staticDataDao.selectStaticDiaoChanAwardList();

        //赛季
        staticSeasonPlanList = staticDataDao.selectStaticSeasonPlanList();
        staticSeasonPlanMap = new HashMap<>();
        staticSeasonPlanList.forEach(tmp -> staticSeasonPlanMap.putIfAbsent(tmp.getId(), tmp));
        staticSeasonTreasuryMap = staticDataDao.selectStaticSeasonTreasuryMap();
        staticSeasonTalentPlanList = staticDataDao.selectStaticSeasonTalentPlanList();
        staticSeasonTalentPlanMap = new HashMap<>();
        staticSeasonTalentPlanList.forEach(tmp -> {
            staticSeasonTalentPlanMap.putIfAbsent(tmp.getId(), tmp);
        });
        staticSeasonTreasuryMap.values().forEach(tmp -> {
            int w = tmp.getRdmAward().stream().mapToInt(tmpList -> tmpList.get(3)).sum();
            tmp.setWeight(w);
        });
        staticSeasonTaskMap = staticDataDao.selectStaticSeasonTaskMap();
        staticSeasonTaskGroupMap = staticSeasonTaskMap.values().stream().collect(Collectors.groupingBy(StaticSeasonTask::getSeason));
        staticSeasonTaskScoreMap = staticDataDao.selectStaticSeasonTaskScoreMap();
        staticSeasonTaskScoreGroup = staticSeasonTaskScoreMap.values().stream().collect(Collectors.groupingBy(StaticSeasonTaskScore::getSeason));
        staticSeasonRankList = staticDataDao.selectStaticSeasonRankList();
        staticSeasonRankGroupMap = staticSeasonRankList.stream().collect(Collectors.groupingBy(StaticSeasonRank::getSeason));
        staticSeasonRankGroupMap.forEach((k, v) -> {
            TreeSet<Integer> rankSet = new TreeSet<>();
            v.stream().filter(tmp -> tmp.getType() == 1).collect(Collectors.toList()).forEach(tmp -> rankSet.addAll(tmp.getRank()));
            seasonRankMax.put(k, rankSet.last());
        });
        seasonTalentMap = staticDataDao.selectStaticSeasonTalentMap();
    }

    public static List<StaticSeasonRank> getStaticSeasonRankList() {
        return staticSeasonRankList;
    }

    public static Map<Integer, StaticSeasonTreasury> getStaticSeasonTreasuryMap() {
        return staticSeasonTreasuryMap;
    }

    public static List<StaticSeasonPlan> getStaticSeasonPlanList() {
        return staticSeasonPlanList;
    }

    public static List<StaticSeasonTalentPlan> getStaticSeasonTalentPlanList() {
        return staticSeasonTalentPlanList;
    }

    public static StaticSeasonPlan getStaticSeasonPlanById(int planId) {
        return staticSeasonPlanMap.get(planId);
    }

    /////////////////////////
    public static int getDiaoChanMaxDay(int activityId) {
        return staticDiaoChanDayList.stream().filter(o -> o.getActivityId() == activityId).mapToInt(o -> o.getDay()).distinct().max().getAsInt();
    }

    public static List<StaticDiaoChanRank> getStaticDiaoChanRankListByDay(int activityId, int type, int day) {
        return staticDiaoChanRankList.stream().filter(o -> o.getActivityId() == activityId && o.getType() == type && o.getDay() == day).collect(Collectors.toList());
    }

    public static StaticDiaoChanAward getStaticDiaoChanAward(int id) {
        return staticDiaoChanAwardList.stream().filter(o -> o.getId() == id).findFirst().orElse(null);
    }

    public static List<StaticDiaoChanAward> getStaticDiaoChanAwardList(int activityId) {
        return staticDiaoChanAwardList.stream().filter(o -> o.getActivityId() == activityId).collect(Collectors.toList());
    }

    public static List<StaticDiaoChanDayTask> getStaticDiaoChanDayTaskByDay(int activityId, int day) {
        return staticDiaoChanDayTaskList.stream().filter(o -> o.getActivityId() == activityId && o.getDay() == day).sorted(Comparator.comparingInt(StaticDiaoChanDayTask::getId)).collect(Collectors.toList());
    }

    public static StaticDiaoChanDay getStaticDiaoChanDay(int id) {
        return staticDiaoChanDayList.stream().filter(o -> o.getId() == id).findFirst().orElse(null);
    }

    public static List<StaticDiaoChanDay> getStaticDiaoChanDayList(int activityId, int day) {
        return staticDiaoChanDayList.stream().filter(o -> o.getActivityId() == activityId && o.getDay() == day).collect(Collectors.toList());
    }
////////////////////////

    public static StaticSandTableAward getStaticSandTableAward(int type, int param) {
        return sandTableAwards.stream().filter(o -> o.getType() == type && o.getParam() == param).findFirst().orElse(null);
    }

    public static StaticSandTableAward getStaticSandTableAwardMaxParam(int type) {
        List<StaticSandTableAward> list_ = sandTableAwards.stream().filter(o -> o.getType() == type).sorted(Comparator.comparingInt(StaticSandTableAward::getParam)).collect(Collectors.toList());
        return list_.get(list_.size() - 1);
    }

    public static StaticSandTableExchange getStaticSandTableExchangeById(int id) {
        return sandTableExchangeMap.get(id);
    }

    public static void initSystem() {
        Map<Integer, StaticSystem> systemMap = staticDataDao.selectSystemMap();
        StaticIniDataMgr.systemMap = systemMap;
        Map<Integer, StaticSystem> actParamMap = staticDataDao.selectActParamMap();
        StaticIniDataMgr.actParamMap = actParamMap;
    }

    public static void initName() {
        List<StaticIniName> staticNameList = staticDataDao.selectName();
        List<String> familyList = new ArrayList<String>();
        List<String> womanList = new ArrayList<String>();
        List<String> markList = new ArrayList<String>();
        List<String> manList = new ArrayList<String>();
        for (StaticIniName staticName : staticNameList) {
            String familyName = staticName.getFamilyname();
            String womanName = staticName.getWomanname();
            String manName = staticName.getManname();
            String mark = staticName.getMark();
            if (familyName != null && !familyName.equals("")) {
                familyList.add(familyName);
            }

            if (womanName != null && !womanName.equals("")) {
                womanList.add(womanName);
            }

            if (manName != null && !manName.equals("")) {
                manList.add(manName);
            }

            if (mark != null && !mark.equals("")) {
                markList.add(mark);
            }
        }
        StaticIniDataMgr.familyList = familyList;
        StaticIniDataMgr.womanList = womanList;
        StaticIniDataMgr.markList = markList;
        StaticIniDataMgr.manList = manList;
    }

    public static String getManNick() {
        StringBuffer sb = new StringBuffer();

        int familyIndex = RandomHelper.randomInSize(familyList.size());
        sb.append(familyList.get(familyIndex));

        int nameIndex = RandomHelper.randomInSize(manList.size());
        sb.append(manList.get(nameIndex));
        return sb.toString();
    }

    public static String getWomanNick() {
        StringBuffer sb = new StringBuffer();

        int familyIndex = RandomHelper.randomInSize(familyList.size());
        sb.append(familyList.get(familyIndex));

        int nameIndex = RandomHelper.randomInSize(womanList.size());
        sb.append(womanList.get(nameIndex));
        return sb.toString();
    }

    public static StaticIniLord getLordIniData() {
        return staticDataDao.selectLord();
    }

    public static StaticSystem getSystemConstantById(int id) {
        return systemMap.get(id);
    }

    public static Map<Integer, StaticSystem> getSystemMap() {
        return systemMap;
    }

    public static StaticSystem getActParamById(int id) {
        return actParamMap.get(id);
    }

    public static Map<String, StaticText> getTextMap() {
        return textMap;
    }

    public static String getTextName(String id) {
        StaticText text = textMap.get(id);
        return null == text ? null : text.getName();
    }

    public static StaticSandTableAward getSandTableKillingRewardMaxParam() {
        return sandTableKillingRewardMaxParam;
    }

    public static void setSandTableKillingRewardMaxParam(StaticSandTableAward sandTableKillingRewardMaxParam) {
        StaticIniDataMgr.sandTableKillingRewardMaxParam = sandTableKillingRewardMaxParam;
    }

    public static Map<Integer, StaticSeasonTask> getStaticSeasonTaskMap() {
        return staticSeasonTaskMap;
    }

    public static Map<Integer, StaticSeasonTaskScore> getStaticSeasonTaskScoreMap() {
        return staticSeasonTaskScoreMap;
    }

    public static Map<Integer, List<StaticSeasonTaskScore>> getStaticSeasonTaskScoreGroup() {
        return staticSeasonTaskScoreGroup;
    }

    public static Map<Integer, List<StaticSeasonTask>> getStaticSeasonTaskGroupMap() {
        return staticSeasonTaskGroupMap;
    }

    public static Map<Integer, List<StaticSeasonRank>> getStaticSeasonRankGroupMap() {
        return staticSeasonRankGroupMap;
    }

    public static Map<Integer, Integer> getSeasonRankMax() {
        return seasonRankMax;
    }

    public static Map<Integer, StaticSeasonTalent> getSeasonTalentMap() {
        return seasonTalentMap;
    }

    public static StaticSeasonTalentPlan getStaticSeasonTalentPlan(int id) {
        return staticSeasonTalentPlanMap.get(id);
    }

    public static StaticSeasonTalentPlan getOpenStaticSeasonTalentPlan(int planId) {
        if (ObjectUtils.isEmpty(staticSeasonTalentPlanList)) {
            return null;
        }
        List<StaticSeasonTalentPlan> list = staticSeasonTalentPlanList.stream().filter(staticSeasonTalentPlan -> staticSeasonTalentPlan.getPlanId() == planId).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(list)) {
            return null;
        }

        return Optional.ofNullable(list.stream().filter(staticSeasonTalentPlan -> staticSeasonTalentPlan.isOpen()).findFirst()).get().orElse(null);
    }
}
