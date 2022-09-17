package com.gryphpoem.game.zw.core.aspect.aop;

import com.gryphpoem.game.zw.core.aspect.ClientThreadMode;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-17 15:31
 */
@Aspect
@Component
public class ClientThreadModeAspect {
    @Pointcut("execution(@com.gryphpoem.game.zw.core.aspect.ClientThreadMode * *(..))")
    public void pointCut() {
    }

    @Around("pointCut()")
    public void runInThreadMode(ProceedingJoinPoint pjp) throws Exception {
        Signature signature = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (Objects.nonNull(method)) {
            if (method.getReturnType() != void.class) {
                LogUtil.error("不可在有返回值时使用@ClientThreadMode注解");
                Java8Utils.syncMethodInvoke(() -> {
                    Java8Utils.syncMethodInvoke(() -> {
                        try {
                            pjp.proceed();
                        } catch (Throwable e) {
                            LogUtil.error("", e);
                        }
                    });
                });
            }

            ClientThreadMode clientThreadMode = method.getAnnotation(ClientThreadMode.class);
            if (Objects.nonNull(clientThreadMode.threadMode())) {
                switch (clientThreadMode.threadMode()) {
                    case MAIN:
                        Java8Utils.syncMethodInvoke(() -> {
                            try {
                                pjp.proceed();
                            } catch (Throwable e) {
                                LogUtil.error("", e);
                            }
                        });
                    default:
                        DataResource.logicServer.addCommandByType(() -> {
                            try {
                                pjp.proceed();
                            } catch (Throwable e) {
                                LogUtil.error("", e);
                            }
                        }, clientThreadMode.threadMode());
                }
            }
        }
    }
}
