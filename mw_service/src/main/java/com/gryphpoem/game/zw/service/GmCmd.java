package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.resource.domain.Player;

import java.lang.annotation.*;

/**
 * 用于注解实现GM命令逻辑的方法，只能用于实现了{@link GmCmdService}的子类 or {@link GmService}
 * <p>
 *     1、功能业务的GM命令实现{@link GmCmdService#handleGmCmd(Player, String...)}后在自己的模块中处理<br/>
 *     2、系统层面的GM命令可以放在{@link GmService}中实现
 * </p>
 * @author xwind
 * @date 2021/7/28
 */

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmCmd {
    String value() default "";
}
