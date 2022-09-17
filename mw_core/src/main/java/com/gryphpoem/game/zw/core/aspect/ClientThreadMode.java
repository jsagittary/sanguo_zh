package com.gryphpoem.game.zw.core.aspect;

import com.gryphpoem.game.zw.core.handler.DealType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-17 15:30
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientThreadMode {
    DealType threadMode() default DealType.MAIN;
}
