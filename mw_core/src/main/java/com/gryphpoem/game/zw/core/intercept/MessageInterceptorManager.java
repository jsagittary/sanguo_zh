package com.gryphpoem.game.zw.core.intercept;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.message.MessagePool;
import com.gryphpoem.game.zw.pb.BasePb.Base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description 游戏协议前置拦截器管理类（单例）
 * @author TanDonghai
 * @date 创建时间：2017年6月19日 上午10:49:44
 *
 */
public class MessageInterceptorManager {
    private static MessageInterceptorManager ins = new MessageInterceptorManager();

    /**
     * 拦截器注册容器,key:拦截方向类型，按协议方向分类，包括客户端交互协议、游戏服通信协议、与其他进程通信协议等
     */
    private ConcurrentHashMap<InterceptAspect, List<IMessageIntercept>> interceptorMap = new ConcurrentHashMap<>();

    private MessageInterceptorManager() {
    }

    public static MessageInterceptorManager getIns() {
        return ins;
    }

    /**
     * 协议拦截器注册
     * 
     * @param aspect 拦截器拦截的协议方向分类，包括客户端交互协议、游戏服通信协议、与其他进程通信协议等分类
     * @param interceptor 具体的拦截器实例
     */
    public void registerInterceptor(InterceptAspect aspect, IMessageIntercept interceptor) {
        List<IMessageIntercept> interceptorList = interceptorMap.get(aspect);
        if (null == interceptorList) {
            interceptorList = new ArrayList<>();
            interceptorMap.put(aspect, interceptorList);
        }

        if (!interceptorList.contains(interceptor)) {
            interceptorList.add(interceptor);
        }
    }

    /**
     * 执行协议拦截操作
     * 
     * @param aspect
     * @param roleId
     * @param msg
     * @throws MwException
     */
    public void doIntercept(InterceptAspect aspect, long roleId, Base msg) throws MwException {
        List<IMessageIntercept> interceptorList = interceptorMap.get(aspect);
        if (null == interceptorList || interceptorList.size() == 0) {
            return;// 没有注册拦截器，退出
        }

        Object param;
        for (IMessageIntercept intercept : interceptorList) {
            if (MessagePool.getIns().needIntercept(intercept.getInterceptType(), msg.getCmd())) {
                param = MessagePool.getIns().getInterceptParam(intercept.getInterceptType(), msg.getCmd());
                intercept.doIntercept(roleId, msg, param);
            }
        }
    }
}
