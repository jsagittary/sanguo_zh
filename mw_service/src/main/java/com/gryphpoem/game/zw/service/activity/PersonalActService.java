package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.PersonalAct;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public interface PersonalActService {
    void initActData(Player player, int actType);

    /**
     * 是否是个人活动类型(不随主服活动id变化而变化)
     *
     * @param actType
     * @return
     */
    static boolean isPersonalAct(int actType) {
        Map<String, AbsSimpleActivityService> resultMap = DataResource.ac.getBeansOfType(AbsSimpleActivityService.class);
        if (CheckNull.isEmpty(resultMap))
            return false;

        return Objects.nonNull(resultMap.values().stream().filter(result -> {
            PersonalAct annotation = result.getClass().getAnnotation(PersonalAct.class);
            return Objects.nonNull(annotation) && Arrays.stream(annotation.actTypes()).anyMatch(type -> type == actType);
        }).findFirst().orElse(null));
    }

    /**
     * 初始化个人活动信息
     *
     * @param player
     */
    static void initData(Player player) {
        Map<String, PersonalActService> resultMap = DataResource.ac.getBeansOfType(PersonalActService.class);
        if (CheckNull.isEmpty(resultMap))
            return;

        resultMap.values().forEach(service -> {
            PersonalAct annotation = service.getClass().getAnnotation(PersonalAct.class);
            if (CheckNull.isNull(annotation) || ObjectUtils.isEmpty(annotation.actTypes()))
                return;
            for (int actType : annotation.actTypes()) {
                service.initActData(player, actType);
            }
        });
    }

    /**
     * 若为永久性个人活动则保存活动数据
     *
     * @param actType
     * @param actPlanKeyId
     * @param player
     * @param clazz
     */
    static void savePersonalActs(int actType, int actPlanKeyId, Player player, Class<?> clazz) {
        PersonalAct annotation = clazz.getAnnotation(PersonalAct.class);
        if (CheckNull.isNull(annotation))
            return;
        if (Arrays.stream(annotation.actTypes()).noneMatch(type -> type == actType))
            return;
        player.getPersonalActs().saveData(actType, actPlanKeyId);
    }

    static Activity getActivity(Integer actKeyId, int actType, Player player) {
        Activity activity;
        if (CheckNull.isNull(actKeyId)) {
            activity = DataResource.ac.getBean(ActivityDataManager.class).getActivityInfo(player, actType);
        } else {
            activity = DataResource.ac.getBean(ActivityDataManager.class).getPersonalActivityInfo(player, actKeyId);
        }

        return activity;
    }

    static ActivityBase getActivityBase(Integer actKeyId, int actType) {
        ActivityBase ab;
        if (CheckNull.isNull(actKeyId)) {
            ab = StaticActivityDataMgr.getActivityByType(actType);
        } else {
            ab = StaticActivityDataMgr.getPersonalActivityByType(actKeyId);
        }
        return ab;
    }
}
