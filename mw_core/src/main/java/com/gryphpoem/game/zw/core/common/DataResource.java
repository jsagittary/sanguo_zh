package com.gryphpoem.game.zw.core.common;

import com.google.protobuf.ExtensionRegistry;
import com.gryphpoem.game.zw.core.AbsLogicServer;
import com.gryphpoem.game.zw.core.net.InnerServer;
import com.gryphpoem.game.zw.core.util.ChannelUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DataResource {
    private static ExtensionRegistry registry = ExtensionRegistry.newInstance();

    public static ApplicationContext ac;
    /** key:roleId */
    private static ConcurrentHashMap<Long, ChannelHandlerContext> roleChannels = new ConcurrentHashMap<>();

    public static int serverId;

    public static String serverName;

    public static String environment;

    public static AbsLogicServer logicServer;

    public static InnerServer innerServer;

    /**
     * 获取spring管理的实例
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz){
        if(Objects.nonNull(ac)){
            return ac.getBean(clazz);
        }
        return null;
    }

    /**
     * 获取指定类型的所有实例
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> getBeans(Class<T> clazz){
        List<T> beans = new ArrayList<>();
        if(Objects.nonNull(ac)){
            beans.addAll(ac.getBeansOfType(clazz).values());
        }
        return beans;
    }

    /**
     * 玩家登录时，注册玩家id
     * 
     * @param ctx
     * @param roleId
     */
    public static void registerRoleChannel(ChannelHandlerContext ctx, long roleId) {
        LogUtil.channel(roleId + " login!");
        ChannelUtil.setRoleId(ctx, roleId);

        roleChannels.put(roleId, ctx);
    }

    public static void removeRoleChannel(long roleId) {
        roleChannels.remove(roleId);
    }

    public static ChannelHandlerContext getRoleChannel(long roleId) {
        return roleChannels.get(roleId);
    }

    /**
     * 发送消息到跨服
     * 
     * @param base
     */
    public static void sendMsgToCross(Base base) {
        if (innerServer != null) {
            innerServer.sendMsg(base);
        }
    }

    public static ExtensionRegistry getRegistry() {
        return registry;
    }
}
