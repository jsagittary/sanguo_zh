package com.gryphpoem.game.zw.cmd.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * @ClassName Cmd.java
 * @Description
 * @author QiuKun
 * @date 2019年4月29日
 */
@Target(value = { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Cmd {
    /**
     * @return 请求协议号
     */
    int rqCmd();

    /**
     * 返回协议号
     * 
     * @return
     */
    int rsCmd() default -1;
}
