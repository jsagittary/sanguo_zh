package com.gryphpoem.game.zw.core.registry;

/**
 * @ClassName RegistryConstant.java
 * @Description
 * @author QiuKun
 * @date 2019年5月10日
 */
public interface RegistryConstant {
    /**
     * 服务器类型 跨服
     */
    int SERVER_TYPE_CROSS = 1;

    /**
     * zk中跨服的目录
     */
    String CROSS_SERVER_NAME = "cross";

    /**
     * zk中的跟目录
     */
    String BASE_PATH = "honor";

    /**
     * 跨服名称前缀
     */
    String CROSS_SERVER_ID_PREFIX = "cross_";
}
