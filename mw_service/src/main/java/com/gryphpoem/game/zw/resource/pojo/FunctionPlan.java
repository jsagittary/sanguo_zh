package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;

import java.lang.annotation.*;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-16 9:26
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FunctionPlan {
    PlanFunction[] functions() default PlanFunction.DRAW_CARD;
}
