package com.gryphpoem.game.zw.util;

import com.google.protobuf.ExtensionRegistry;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.pb.*;

public class ProtoRegistry {
    public static void registry() {
        ExtensionRegistry registry = DataResource.getRegistry();
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
    }
}
