package com.gryphpoem.game.zw.manager.annotation;

import java.lang.annotation.*;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-21 16:16
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BuffEffectType {
    int buffEffect();

    int type() default -1;
}
