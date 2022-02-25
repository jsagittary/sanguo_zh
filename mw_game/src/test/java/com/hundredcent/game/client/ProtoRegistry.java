package com.hundredcent.game.client;

import com.google.protobuf.ExtensionRegistry;
import com.gryphpoem.game.zw.pb.*;

/**
 * @ClassName ProtoRegistry.java
 * @Description 协议装载
 * @author TanDonghai
 * @date 创建时间：2017年3月21日 下午5:16:12
 *
 */
public final class ProtoRegistry {
    public static ExtensionRegistry registry = ExtensionRegistry.newInstance();

    static {
        GamePb1.registerAllExtensions(registry);
        GamePb2.registerAllExtensions(registry);
        GamePb3.registerAllExtensions(registry);
        GamePb4.registerAllExtensions(registry);
        GamePb5.registerAllExtensions(registry);
        GamePb6.registerAllExtensions(registry);
        GamePb7.registerAllExtensions(registry);
        HttpPb.registerAllExtensions(registry);
        CommonPb.registerAllExtensions(registry);
        SerializePb.registerAllExtensions(registry);
        CrossPb.registerAllExtensions(registry);
        ActivityPb.registerAllExtensions(registry);
    }
}
