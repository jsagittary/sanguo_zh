package com.gryphpoem.game.zw.dataMgr.cross;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.AbsStaticIniService;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.NewCrossConstant;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGroup;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StaticNewCrossDataMgr extends AbsStaticIniService {

    private static Map<Integer, StaticCrossGamePlayPlan> crossGamePlayPlanMap = new HashMap<>();

    private static Map<Integer, StaticCrossGroup> crossGroupMap = new HashMap<>();

    @Override
    public void load() {
        crossGroupMap = super.staticIniDao.selectStaticCrossGroup();
        int serverId = DataResource.getBean(ServerSetting.class).getServerID();
        crossGroupMap = crossGroupMap.values().stream().filter(crossGroup -> crossGroup.allElementServer().stream()
                .anyMatch(element -> element.getServerId() == serverId)).collect(Collectors.toMap(StaticCrossGroup::getGroup, Function.identity()));
        if (!ObjectUtils.isEmpty(crossGroupMap))
            crossGamePlayPlanMap = super.staticIniDao.selectStaticCrossGamePlanMap(crossGroupMap.keySet());

//        Optional.ofNullable(crossGroupMap).ifPresent(map -> map.values().forEach(crossGroup -> {
//            Optional.ofNullable(crossGroup.allElementServer()).ifPresent(list -> {
//                list.forEach(staticElementServer -> {
//                    staticCrossGroupMap.computeIfAbsent(staticElementServer.getServerId(),
//                            a -> new HashMap<>()).computeIfAbsent(staticElementServer.getCamp(), b -> new ArrayList<>()).add(crossGroup.getGroup());
//                });
//            });
//        }));

    }

    @Override
    public void check() {
        if (ObjectUtils.isEmpty(crossGamePlayPlanMap) || ObjectUtils.isEmpty(crossGroupMap)) {
            LogUtil.error("加载跨服战火数据出错! crossGamePlayPlanMap: ", ObjectUtils.isEmpty(crossGamePlayPlanMap) ? "null" : 1,
                    ObjectUtils.isEmpty(crossGroupMap) ? "null" : 1);
        }
    }

    public static StaticCrossGroup getStaticCrossGroup(int groupId) {
        return crossGroupMap.get(groupId);
    }

    /**
     * 获取跨服战火正在开放的配置
     *
     * @param player
     * @param function
     * @return
     */
    public static StaticCrossGamePlayPlan getOpenPlan(Player player, int function) {
        int now = TimeHelper.getCurrentSecond();
        int earlyEntryCrossMapPeriod = getEarlyEntryCrossMapPeriod(function);
        List<StaticCrossGamePlayPlan> plans = crossGamePlayPlanMap.values().stream().
                filter(plan -> {
                    return Objects.nonNull(plan.getBeginTime()) && Objects.nonNull(plan.getEndTime()) &&
                            plan.getActivityType() == function && now >= TimeHelper.
                            dateToSecond(plan.getBeginTime()) - earlyEntryCrossMapPeriod &&
                            now <= TimeHelper.dateToSecond(plan.getEndTime());
                }).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(plans))
            return null;

        return plans.stream().filter(plan -> {
            StaticCrossGroup crossGroup = crossGroupMap.get(plan.getGroup());
            if (CheckNull.isNull(crossGroup))
                return false;
            return crossGroup.allElementServer().stream().anyMatch(element -> element.getServerId() ==
                    DataResource.getBean(ServerSetting.class).getServerID() && element.getCamp() == player.getCamp());
        }).findAny().orElse(null);
    }

    private static int getEarlyEntryCrossMapPeriod(int functionId) {
        CrossFunction crossFunction = CrossFunction.convertTo(functionId);
        switch (crossFunction) {
            case CROSS_WAR_FIRE:
                return Constant.EARLY_ENTRY_CROSS_MAP_PERIOD;
        }

        return 0;
    }

    /**
     * 获取当前存活的活动
     *
     * @param function
     * @return
     */
    public static StaticCrossGamePlayPlan getOpenPlan(int function) {
        List<StaticCrossGamePlayPlan> plans = crossGamePlayPlanMap.values().stream().
                filter(staticCrossGamePlayPlan -> staticCrossGamePlayPlan.getStage()
                        == NewCrossConstant.STAGE_RUNNING && staticCrossGamePlayPlan
                        .getActivityType() == function).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(plans))
            return null;

        return plans.stream().filter(plan -> {
            StaticCrossGroup crossGroup = crossGroupMap.get(plan.getGroup());
            if (CheckNull.isNull(crossGroup))
                return false;
            return crossGroup.allElementServer().stream().anyMatch(element -> element.getServerId() ==
                    DataResource.getBean(ServerSetting.class).getServerID());
        }).findAny().orElse(null);
    }

    public static StaticCrossGamePlayPlan getNotOverPlan(Player player, int function) {
        List<StaticCrossGamePlayPlan> plans = crossGamePlayPlanMap.values().stream().
                filter(staticCrossGamePlayPlan -> staticCrossGamePlayPlan.getStage()
                        != NewCrossConstant.STAGE_OVER && staticCrossGamePlayPlan
                        .getActivityType() == function).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(plans))
            return null;

        return plans.stream().filter(plan -> {
            StaticCrossGroup crossGroup = crossGroupMap.get(plan.getGroup());
            if (CheckNull.isNull(crossGroup))
                return false;
            return crossGroup.allElementServer().stream().anyMatch(element -> element.getServerId() ==
                    DataResource.getBean(ServerSetting.class).getServerID() && element.getCamp() == player.getCamp());
        }).findAny().orElse(null);
    }

    public static List<StaticCrossGamePlayPlan> getPlans(int function) {
        List<StaticCrossGamePlayPlan> plans = crossGamePlayPlanMap.values().stream().
                filter(staticCrossGamePlayPlan -> staticCrossGamePlayPlan.getActivityType() == function).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(plans))
            return null;

        return plans.stream().filter(plan -> {
            StaticCrossGroup crossGroup = crossGroupMap.get(plan.getGroup());
            if (CheckNull.isNull(crossGroup))
                return false;
            return crossGroup.allElementServer().stream().anyMatch(element -> element.getServerId() ==
                    DataResource.getBean(ServerSetting.class).getServerID());
        }).collect(Collectors.toList());
    }

    /**
     * 获取未到结束时间的plan
     *
     * @param now
     * @param function
     * @param player
     * @return
     */
    public static StaticCrossGamePlayPlan getNotEndPlan(long now, int function, Player player) {
        List<StaticCrossGamePlayPlan> plans = crossGamePlayPlanMap.values().stream().
                filter(staticCrossGamePlayPlan -> Objects.nonNull(staticCrossGamePlayPlan.getDisplayTime()) &&
                        staticCrossGamePlayPlan.getDisplayTime().getTime() >= now && staticCrossGamePlayPlan
                        .getActivityType() == function).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(plans))
            return null;

        return plans.stream().filter(plan -> {
            StaticCrossGroup crossGroup = crossGroupMap.get(plan.getGroup());
            if (CheckNull.isNull(crossGroup))
                return false;
            return crossGroup.allElementServer().stream().anyMatch(element -> element.getServerId() ==
                    DataResource.getBean(ServerSetting.class).getServerID() && element.getCamp() == player.getCamp());
        }).findAny().orElse(null);
    }

    public static StaticCrossGamePlayPlan getStaticCrossGamePlayPlan(int gamePlanKey) {
        return crossGamePlayPlanMap.get(gamePlanKey);
    }
}
