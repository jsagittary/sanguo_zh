package com.gryphpoem.game.zw.core.util;

/**
 * @ClassName ServerType.java
 * @Description 服务类型, 用于当前启动时区服是 游戏服、 跨服 、 合服
 * @author QiuKun
 * @date 2019年4月29日
 */
public abstract class ServerType {
    /** 游戏服 */
    public static final int SERVER_TYPE_GAME = 0;
    /** 合服 */
    public static final int SERVER_TYPE_MERGE = 1;
    /** 跨服 */
    public static final int SERVER_TYPE_CORSS = 2;

    public static int selfServerType = SERVER_TYPE_GAME; // 默认是游戏服
    
    
}
