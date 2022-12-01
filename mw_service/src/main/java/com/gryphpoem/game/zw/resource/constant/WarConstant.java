package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-12-01 21:15
 */
public class WarConstant {
    /**
     * 战报缓存最大条目
     */
    public static int MAXIMUM_ENTRIES_OF_WAR_REPORT_CACHE;

    /**
     * 战报缓存过期时间
     */
    public static int EXPIRATION_TIME_OF_WAR_REPORT_CACHE;

    /**
     * s_system表中定义的常量初始化
     */
    public static void loadSystem() {
        MAXIMUM_ENTRIES_OF_WAR_REPORT_CACHE = SystemTabLoader.getIntegerSystemValue(SystemId.MAXIMUM_ENTRIES_OF_WAR_REPORT_CACHE, 500);
        EXPIRATION_TIME_OF_WAR_REPORT_CACHE = SystemTabLoader.getIntegerSystemValue(SystemId.EXPIRATION_TIME_OF_WAR_REPORT_CACHE, 5);
    }
}
