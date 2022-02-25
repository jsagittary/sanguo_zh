package com.gryphpoem.game.zw.core.intercept;

/**
 * @Description 协议拦截器拦截类型枚举
 * @author TanDonghai
 * @date 创建时间：2017年6月19日 上午10:28:27
 *
 */
public enum InterceptType {
    FUNCTION_UNLOCK(1, "功能解锁", "拦截玩家还未解锁功能的协议"),

    ;

    private int type;
    private String name;
    private String desc;

    private InterceptType(int type, String name, String desc) {
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
