package com.gryphpoem.game.zw.core.intercept;

/**
 * @Description 拦截器拦截方向分类枚举
 * @author TanDonghai
 * @date 创建时间：2017年6月19日 上午11:04:13
 *
 */
public enum InterceptAspect {

    CLIENT_MESSAGE(1, "客户端协议", "拦截方向：所有客户端与游戏服务器通信的协议，主要拦截客户端发往服务端的协议"),

    ;
    private int type;
    private String name;
    private String desc;

    private InterceptAspect(int type, String name, String desc) {
        this.type = type;
        this.name = name;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
