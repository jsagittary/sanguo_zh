package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.pojo.FunctionPlan;
import com.gryphpoem.game.zw.resource.pojo.plan.FunctionPlanData;
import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;
import com.gryphpoem.game.zw.resource.pojo.plan.PlayerFunctionPlanData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.ClassUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-16 9:21
 */
@Component
public class FunctionPlanDataManager {

    private Map<PlanFunction, Class<?>> functionClassMap = new HashMap<>();

    /**
     *  读取功能计划类class
     */
    @PostConstruct
    public void init() {
        Set<Class<?>> classSet = ClassUtil.getClasses(FunctionPlan.class.getPackage());
        if (CheckNull.isEmpty(classSet))
            return;
        for (Class<?> clazz : classSet) {
            FunctionPlan functionPlan = clazz.getAnnotation(FunctionPlan.class);
            if (CheckNull.isNull(functionPlan))
                continue;

            List<Class<?>> superClasses = ClassUtil.getSuperClass(clazz);
            if (CheckNull.isEmpty(superClasses)) continue;
            if (ObjectUtils.isEmpty(functionPlan.functions())) {
                continue;
            }
            if (!superClasses.contains(FunctionPlanData.class))
                continue;
            for (PlanFunction enumT : functionPlan.functions()) {
                functionClassMap.put(enumT, clazz);
            }
        }
    }

    /**
     * 获取玩家功能计划信息
     *
     * @param playerFunctionPlanData
     * @param planFunction
     * @param planKeyId
     * @return
     */
    public FunctionPlanData functionPlanData(PlayerFunctionPlanData playerFunctionPlanData, PlanFunction planFunction, int planKeyId, boolean add) {
        if (!functionClassMap.containsKey(planFunction))
            return null;
        FunctionPlanData functionPlanData = playerFunctionPlanData.getData(planKeyId);
        if (!add || Objects.nonNull(functionPlanData))
            return functionPlanData;
        return playerFunctionPlanData.updateData(newFunctionPlanData(planFunction, planKeyId));
    }

    /**
     * 创建functionData
     *
     * @param planFunction
     * @param planKeyId
     * @return
     */
    public FunctionPlanData newFunctionPlanData(PlanFunction planFunction, int planKeyId) {
        Class<?> clazz = functionClassMap.get(planFunction);
        if (CheckNull.isNull(clazz))
            return null;

        FunctionPlanData data;
        try {
            data = (FunctionPlanData) clazz.getConstructor(Integer.class).newInstance(planKeyId);
            return data;
        } catch (Exception e) {
            LogUtil.error("", e);
        }

        return null;
    }

    /**
     * 移除functionData
     *
     * @param playerFunctionPlanData
     * @param planKeyId
     */
    public FunctionPlanData removeFunctionPlanData(PlayerFunctionPlanData playerFunctionPlanData, int planKeyId) {
        if (CheckNull.isNull(playerFunctionPlanData))
            return null;
        return playerFunctionPlanData.removeData(planKeyId);
    }
}
